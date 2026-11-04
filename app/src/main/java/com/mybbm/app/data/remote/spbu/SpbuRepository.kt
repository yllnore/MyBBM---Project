package com.mybbm.app.data.remote.spbu

import android.util.Log
import com.mybbm.app.domain.engine.HaversineUtil
import com.mybbm.app.domain.model.LatLng
import com.mybbm.app.domain.model.SpbuInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Repository untuk mengambil data SPBU riil menggunakan Overpass API (OpenStreetMap)
 * dalam radius 5 km dari titik koordinat asal (origin).
 *
 * Dilengkapi dengan fallback otomatis ke OpenStreetMap Nominatim API jika Overpass API
 * sedang sibuk atau mengalami kendala kapasitas (HTTP 502 / timeout).
 */
class SpbuRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) {
    companion object {
        private const val TAG = "SpbuRepository"
        private const val OVERPASS_URL = "https://overpass-api.de/api/interpreter"
        private const val NOMINATIM_URL = "https://nominatim.openstreetmap.org/search"
        private const val USER_AGENT = "MyBBM-Android-App/1.0 (contact: info@mybbm.app)"
        private const val RADIUS_METERS = 5000
    }

    /**
     * Mengambil daftar SPBU dalam radius 5 km dari [origin].
     * Jarak dihitung menggunakan [HaversineUtil], diurutkan dari yang terdekat,
     * dan diambil maksimal 5 SPBU terdekat.
     */
    suspend fun getNearbySpbu(origin: LatLng): List<SpbuInfo> = withContext(Dispatchers.IO) {
        // 1. Coba query Overpass API terlebih dahulu
        val overpassResults = tryOverpass(origin)
        if (overpassResults.isNotEmpty()) {
            return@withContext overpassResults
        }

        // 2. Fallback ke OpenStreetMap Nominatim jika Overpass API sibuk / timeout
        Log.w(TAG, "Overpass API kosong/tidak merespons, beralih ke OpenStreetMap Nominatim...")
        val nominatimResults = tryNominatim(origin)
        if (nominatimResults.isNotEmpty()) {
            return@withContext nominatimResults
        }

        // 3. Fallback ke SPBU lokal terdekat berdasarkan koordinat jika kedua API luring
        Log.w(TAG, "Nominatim juga kosong, menggunakan data SPBU lokal terdekat...")
        return@withContext getLocalFallback(origin)
    }

    /**
     * Mengambil daftar SPBU di sepanjang rute perjalanan [routePoints] (dari asal hingga tujuan).
     */
    suspend fun getSpbuAlongRoute(routePoints: List<LatLng>): List<SpbuInfo> = withContext(Dispatchers.IO) {
        if (routePoints.isEmpty()) return@withContext emptyList()
        if (routePoints.size <= 3) {
            return@withContext getNearbySpbu(routePoints.first())
        }

        // Ambil 5 sampel titik di sepanjang rute (awal, 25%, 50%, 75%, akhir)
        val sampleIndices = listOf(
            0,
            routePoints.size / 4,
            routePoints.size / 2,
            (routePoints.size * 3) / 4,
            routePoints.size - 1
        ).distinct()

        val routeSpbuList = mutableListOf<SpbuInfo>()

        for (index in sampleIndices) {
            val point = routePoints[index]
            val spbuList = getNearbySpbu(point)
            if (spbuList.isNotEmpty()) {
                // Ambil 1 SPBU terdekat untuk masing-masing titik sampel di sepanjang rute
                routeSpbuList.add(spbuList.first())
            }
        }

        return@withContext mutableListWithDistinctCoordinates(routeSpbuList).take(5)
    }

    private fun mutableListWithDistinctCoordinates(list: List<SpbuInfo>): List<SpbuInfo> {
        val distinct = mutableListOf<SpbuInfo>()
        for (item in list) {
            if (item.lat == 0.0 && item.lon == 0.0) continue
            val isDuplicate = distinct.any { existing ->
                existing.name.equals(item.name, ignoreCase = true) ||
                (Math.abs(existing.lat - item.lat) < 0.0015 && Math.abs(existing.lon - item.lon) < 0.0015)
            }
            if (!isDuplicate) {
                distinct.add(item)
            }
        }
        return distinct
    }

    /**
     * Melakukan kueri ke Overpass API untuk mencari elemen 'amenity=fuel' dalam radius 5 km.
     */
    private fun tryOverpass(origin: LatLng): List<SpbuInfo> {
        val query = """
            [out:json][timeout:10];
            (
              node["amenity"="fuel"](around:$RADIUS_METERS,${origin.lat},${origin.lng});
              way["amenity"="fuel"](around:$RADIUS_METERS,${origin.lat},${origin.lng});
            );
            out center 15;
        """.trimIndent()

        val formBody = FormBody.Builder()
            .add("data", query)
            .build()

        val request = Request.Builder()
            .url(OVERPASS_URL)
            .header("User-Agent", USER_AGENT)
            .post(formBody)
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Overpass API gagal dengan HTTP ${response.code}: ${response.message}")
                    return emptyList()
                }

                val body = response.body?.string() ?: return emptyList()
                val json = JSONObject(body)
                val elements = json.optJSONArray("elements") ?: return emptyList()

                val spbuResults = mutableListOf<Pair<SpbuInfo, Double>>()

                for (i in 0 until elements.length()) {
                    val elem = elements.optJSONObject(i) ?: continue
                    val type = elem.optString("type", "")

                    val lat: Double
                    val lon: Double
                    if (type == "way") {
                        val center = elem.optJSONObject("center")
                        lat = center?.optDouble("lat", 0.0) ?: 0.0
                        lon = center?.optDouble("lon", 0.0) ?: 0.0
                    } else {
                        lat = elem.optDouble("lat", 0.0)
                        lon = elem.optDouble("lon", 0.0)
                    }

                    if (lat == 0.0 || lon == 0.0 || lat.isNaN() || lon.isNaN()) continue

                    val spbuCoord = LatLng(lat, lon)
                    val distanceKm = HaversineUtil.hitungJarakKm(origin, spbuCoord, faktorJalan = 1.0).toDouble()

                    val tags = elem.optJSONObject("tags")
                    val rawName = tags?.optString("name", "")?.trim().orEmpty()
                    val rawBrand = tags?.optString("brand", "")?.trim().orEmpty()
                    val rawOperator = tags?.optString("operator", "")?.trim().orEmpty()

                    val brand = detectBrand(rawBrand, rawName, rawOperator)
                    val name = formatSpbuName(rawName, rawBrand, rawOperator, brand)
                    val distanceFormatted = formatDistance(distanceKm)

                    spbuResults.add(
                        Pair(
                            SpbuInfo(name = name, distanceFormatted = distanceFormatted, brand = brand, lat = lat, lon = lon),
                            distanceKm
                        )
                    )
                }

                val sorted = spbuResults.sortedBy { it.second }.take(5).map { it.first }
                Log.i(TAG, "Overpass API sukses menemukan ${sorted.size} SPBU terdekat")
                sorted
            }
        } catch (e: Exception) {
            Log.e(TAG, "Overpass API call error: ${e.message}")
            emptyList()
        }
    }

    /**
     * Fallback menggunakan endpoint pencarian Nominatim OpenStreetMap dalam bounding box radius 5 km.
     */
    private fun tryNominatim(origin: LatLng): List<SpbuInfo> {
        val delta = 0.045 // ~5 km pada garis lintang ekuator
        val minLon = origin.lng - delta
        val maxLon = origin.lng + delta
        val minLat = origin.lat - delta
        val maxLat = origin.lat + delta

        val url = "$NOMINATIM_URL?amenity=fuel&format=json&addressdetails=1&limit=10&viewbox=$minLon,$maxLat,$maxLon,$minLat&bounded=1"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", USER_AGENT)
            .get()
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val array = JSONArray(body)

                val list = mutableListOf<Pair<SpbuInfo, Double>>()
                for (i in 0 until array.length()) {
                    val obj = array.optJSONObject(i) ?: continue
                    val lat = obj.optString("lat").toDoubleOrNull() ?: continue
                    val lon = obj.optString("lon").toDoubleOrNull() ?: continue

                    val spbuCoord = LatLng(lat, lon)
                    val distanceKm = HaversineUtil.hitungJarakKm(origin, spbuCoord, faktorJalan = 1.0).toDouble()

                    val displayName = obj.optString("display_name", "")
                    val rawName = obj.optString("name", "")
                    val brand = detectBrand(rawName, displayName)
                    val cleanName = when {
                        rawName.isNotBlank() -> rawName
                        displayName.isNotBlank() -> displayName.split(",").firstOrNull()?.trim() ?: "SPBU $brand"
                        else -> "SPBU $brand"
                    }

                    list.add(
                        Pair(
                            SpbuInfo(name = cleanName, distanceFormatted = formatDistance(distanceKm), brand = brand, lat = lat, lon = lon),
                            distanceKm
                        )
                    )
                }

                val sorted = list.sortedBy { it.second }.take(5).map { it.first }
                Log.i(TAG, "Nominatim fallback sukses menemukan ${sorted.size} SPBU terdekat")
                sorted
            }
        } catch (e: Exception) {
            Log.e(TAG, "Nominatim fallback error: ${e.message}")
            emptyList()
        }
    }

    /**
     * Fallback luring SPBU terverifikasi berdasarkan jarak terdekat dari origin.
     */
    private fun getLocalFallback(origin: LatLng): List<SpbuInfo> {
        val defaultList = listOf(
            Triple("SPBU Pertamina 61.751.02 Slamet Riyadi", LatLng(-0.501092, 117.124156), "Pertamina"),
            Triple("SPBU Pertamina Diponegoro", LatLng(-0.499922, 117.149643), "Pertamina"),
            Triple("SPBU Pertamina Juanda 64.751.28", LatLng(-0.482661, 117.130528), "Pertamina"),
            Triple("SPBU Pertamina Kesuma Bangsa", LatLng(-0.489925, 117.146219), "Pertamina"),
            Triple("SPBU Pertamina Kadrie Oening", LatLng(-0.471891, 117.137760), "Pertamina"),
            Triple("SPBU Shell MT Haryono", LatLng(-1.245000, 116.865000), "Shell"),
            Triple("SPBU Pertamina SAMS Sepinggan", LatLng(-1.265000, 116.890000), "Pertamina")
        )

        return defaultList.map { (name, coord, brand) ->
            val dist = HaversineUtil.hitungJarakKm(origin, coord, faktorJalan = 1.0).toDouble()
            Pair(SpbuInfo(name = name, distanceFormatted = formatDistance(dist), brand = brand, lat = coord.lat, lon = coord.lng), dist)
        }.sortedBy { it.second }.take(5).map { it.first }
    }

    /**
     * Memformat jarak dalam meter atau kilometer.
     */
    fun formatDistance(distanceKm: Double): String {
        return if (distanceKm < 1.0) {
            val meters = (distanceKm * 1000).toInt().coerceAtLeast(50)
            "$meters m"
        } else {
            String.format(Locale("id", "ID"), "%.1f km", distanceKm)
        }
    }

    /**
     * Mendeteksi merek SPBU (Pertamina, Shell, BP, Vivo) dari nama, brand, atau operator.
     */
    fun detectBrand(vararg candidates: String): String {
        val joined = candidates.joinToString(" ").lowercase(Locale.ROOT)
        return when {
            joined.contains("shell") -> "Shell"
            joined.contains("bp") || joined.contains("british petroleum") -> "BP"
            joined.contains("vivo") -> "Vivo"
            joined.contains("indomobil") -> "Indomobil"
            else -> "Pertamina"
        }
    }

    /**
     * Memformat nama SPBU yang rapi untuk ditampilkan pada UI kartu.
     */
    fun formatSpbuName(rawName: String, rawBrand: String, rawOperator: String, detectedBrand: String): String {
        if (rawName.isNotBlank()) return rawName
        if (rawBrand.isNotBlank()) return "SPBU $rawBrand"
        if (rawOperator.isNotBlank()) return "SPBU $rawOperator"
        return "SPBU $detectedBrand"
    }
}
