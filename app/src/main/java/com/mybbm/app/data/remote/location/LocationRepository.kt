package com.mybbm.app.data.remote.location

import android.util.Log
import com.mybbm.app.domain.model.LocationSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Repository untuk melakukan pencarian lokasi (Geocoding Autocomplete)
 * menggunakan OpenStreetMap Nominatim API.
 */
class LocationRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "LocationRepository"
        private const val BASE_URL = "https://nominatim.openstreetmap.org/search"
        private const val USER_AGENT = "MyBBM-Android-App/1.0 (contact: admin@mybbm.app)"

        // Database landmark dan sub-lokasi populer di Kalimantan Timur untuk perluasan saran relevan
        private val KNOWN_LANDMARKS = listOf(
            LocationSearchResult(
                name = "POLNES - Politeknik Negeri Samarinda",
                displayName = "Jalan Cipto Mangunkusumo, Gunung Panjang, Kota Samarinda, Kalimantan Timur, Indonesia",
                lat = -0.502106,
                lon = 117.153709
            ),
            LocationSearchResult(
                name = "Kemaritiman Polnes",
                displayName = "Sungai Keledang, Kota Samarinda, Kalimantan Timur, Indonesia",
                lat = -0.503410,
                lon = 117.152800
            ),
            LocationSearchResult(
                name = "Direktorat POLNES",
                displayName = "Sungai Keledang, Kota Samarinda, Kalimantan Timur, Indonesia",
                lat = -0.501500,
                lon = 117.154200
            ),
            LocationSearchResult(
                name = "Gedung Auditorium (Serba Guna) Polnes",
                displayName = "POLNES - Politeknik Negeri Samarinda, Sungai Keledang, Kota Samarinda, Kalimantan Timur",
                lat = -0.502800,
                lon = 117.153900
            ),
            LocationSearchResult(
                name = "Gedung Teknologi Informasi POLNES",
                displayName = "Kampus Polnes, Jl. Cipto Mangunkusumo, Gunung Panjang, Samarinda",
                lat = -0.502400,
                lon = 117.153500
            ),
            LocationSearchResult(
                name = "Perpustakaan Terpadu POLNES",
                displayName = "Kampus Polnes, Jl. Cipto Mangunkusumo, Samarinda Seberang",
                lat = -0.501900,
                lon = 117.153800
            ),
            LocationSearchResult(
                name = "Universitas Mulawarman (UNMUL)",
                displayName = "Jl. Kuaro, Gn. Kelua, Kec. Samarinda Ulu, Kota Samarinda, Kalimantan Timur",
                lat = -0.468889,
                lon = 117.156111
            ),
            LocationSearchResult(
                name = "Rektorat Universitas Mulawarman",
                displayName = "Kampus Gunung Kelua, Samarinda Ulu, Kota Samarinda, Kalimantan Timur",
                lat = -0.469500,
                lon = 117.157200
            ),
            LocationSearchResult(
                name = "Big Mall Samarinda",
                displayName = "Jl. Untung Suropati No.08, Karang Asam Untung, Sungai Kunjang, Samarinda",
                lat = -0.520556,
                lon = 117.120556
            ),
            LocationSearchResult(
                name = "Samarinda Central Plaza (SCP)",
                displayName = "Jl. Pulau Irian No.1, Karang Mumus, Kec. Samarinda Kota, Kota Samarinda",
                lat = -0.501100,
                lon = 117.148200
            ),
            LocationSearchResult(
                name = "Plaza Mulia Samarinda",
                displayName = "Jl. Bhayangkara No.58, Bugis, Kec. Samarinda Kota, Kota Samarinda",
                lat = -0.493200,
                lon = 117.144800
            ),
            LocationSearchResult(
                name = "Jembatan Mahakam Samarinda",
                displayName = "Jembatan Mahakam I, Karang Asam Ilir, Sungai Kunjang, Samarinda",
                lat = -0.518889,
                lon = 117.128611
            ),
            LocationSearchResult(
                name = "Jembatan Mahakam IV (Kembar)",
                displayName = "Jl. Slamet Riyadi, Karang Asam Ilir, Sungai Kunjang, Samarinda",
                lat = -0.519500,
                lon = 117.129200
            ),
            LocationSearchResult(
                name = "Taman Samarendah",
                displayName = "Jl. Bhayangkara, Bugis, Kec. Samarinda Kota, Kota Samarinda, Kalimantan Timur",
                lat = -0.495500,
                lon = 117.145000
            ),
            LocationSearchResult(
                name = "Bandara APT Pranoto Samarinda",
                displayName = "Sungai Siring, Kec. Samarinda Utara, Kota Samarinda, Kalimantan Timur",
                lat = -0.291389,
                lon = 117.251944
            ),
            LocationSearchResult(
                name = "Bandara SAMS Sepinggan Balikpapan",
                displayName = "Jl. Marsma R. Iswahyudi, Sepinggan, Kecamatan Balikpapan Selatan, Balikpapan",
                lat = -1.268333,
                lon = 116.894444
            ),
            LocationSearchResult(
                name = "Pantai Manggar Segarasari",
                displayName = "Jl. Pantai Manggar Segarasari, Manggar Baru, Balikpapan Timur, Balikpapan",
                lat = -1.218889,
                lon = 116.981944
            )
        )
    }

    /**
     * Mengambil daftar saran lokasi dari Nominatim API berdasarkan [query]
     * yang dipadukan dengan ekspansi kata kunci relevan sehingga hasil pencarian
     * terisi penuh dan tetap 100% relevan dengan apa yang dicari pengguna.
     */
    suspend fun searchLocation(query: String): List<LocationSearchResult> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.length < 2) {
            return@withContext emptyList()
        }

        // 1. Ambil titik relevan dari landmark yang cocok dengan kata kunci
        val localMatches = KNOWN_LANDMARKS.filter {
            it.name.contains(trimmedQuery, ignoreCase = true) ||
            it.displayName.contains(trimmedQuery, ignoreCase = true) ||
            (trimmedQuery.equals("polnes", ignoreCase = true) && (it.name.contains("polnes", ignoreCase = true) || it.displayName.contains("polnes", ignoreCase = true))) ||
            (trimmedQuery.equals("unmul", ignoreCase = true) && (it.name.contains("mulawarman", ignoreCase = true) || it.displayName.contains("mulawarman", ignoreCase = true)))
        }

        // 2. Ekspansi query pencarian Nominatim untuk hasil lebih luas
        val expandedQuery = when (trimmedQuery.lowercase()) {
            "polnes" -> "Politeknik Negeri Samarinda"
            "unmul" -> "Universitas Mulawarman"
            else -> trimmedQuery
        }

        val encodedQuery = try {
            URLEncoder.encode(expandedQuery, "UTF-8")
        } catch (e: Exception) {
            expandedQuery
        }

        // Endpoint Nominatim: format=json&countrycodes=id&limit=10
        val url = "$BASE_URL?q=$encodedQuery&format=json&countrycodes=id&limit=10"

        val nominatimResults = mutableListOf<LocationSearchResult>()
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrBlank()) {
                        val jsonArray = JSONArray(bodyString)
                        for (i in 0 until jsonArray.length()) {
                            val obj = jsonArray.optJSONObject(i) ?: continue
                            val displayName = obj.optString("display_name", "")
                            val name = obj.optString("name", "")
                            val latStr = obj.optString("lat", "")
                            val lonStr = obj.optString("lon", "")

                            val lat = latStr.toDoubleOrNull() ?: 0.0
                            val lon = lonStr.toDoubleOrNull() ?: 0.0

                            if (displayName.isNotBlank() && (lat != 0.0 || lon != 0.0)) {
                                nominatimResults.add(
                                    LocationSearchResult(
                                        displayName = displayName,
                                        lat = lat,
                                        lon = lon,
                                        name = name.ifBlank { displayName.split(",").firstOrNull()?.trim() ?: "" }
                                    )
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching locations from Nominatim: ${e.message}", e)
        }

        // 3. Gabungkan hasil lokal relevan terlebih dahulu, lalu tambahkan hasil Nominatim tanpa duplikasi
        val combined = mutableListOf<LocationSearchResult>()
        combined.addAll(localMatches)

        for (item in nominatimResults) {
            val isDuplicate = combined.any { existing ->
                existing.name.equals(item.name, ignoreCase = true) ||
                (Math.abs(existing.lat - item.lat) < 0.0008 && Math.abs(existing.lon - item.lon) < 0.0008)
            }
            if (!isDuplicate) {
                combined.add(item)
            }
        }

        Log.d(TAG, "Search for '$trimmedQuery' returned ${combined.size} items")
        return@withContext combined
    }

    /**
     * Melakukan reverse geocoding dari koordinat GPS [lat] dan [lon]
     * menggunakan OpenStreetMap Nominatim API untuk mendapatkan nama jalan/tempat riil.
     */
    suspend fun reverseGeocode(lat: Double, lon: Double): String? = withContext(Dispatchers.IO) {
        val url = "https://nominatim.openstreetmap.org/reverse?lat=$lat&lon=$lon&format=json"
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val bodyString = response.body?.string() ?: return@withContext null
                val json = JSONObject(bodyString)
                val displayName = json.optString("display_name", "")
                val name = json.optString("name", "")
                val addressObj = json.optJSONObject("address")

                val road = addressObj?.optString("road")
                val city = addressObj?.optString("city")
                    ?: addressObj?.optString("town")
                    ?: addressObj?.optString("municipality")

                val cleanName = when {
                    !road.isNullOrBlank() && !city.isNullOrBlank() -> "$road, $city"
                    name.isNotBlank() && !city.isNullOrBlank() -> "$name, $city"
                    displayName.isNotBlank() -> displayName.split(",").take(2).joinToString(", ").trim()
                    else -> null
                }
                return@withContext cleanName
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reverse geocoding: ${e.message}", e)
            return@withContext null
        }
    }
}
