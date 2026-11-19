package com.mybbm.app.data.remote.spbu

import android.util.Log
import com.mybbm.app.domain.engine.HaversineUtil
import com.mybbm.app.domain.model.LatLng
import com.mybbm.app.domain.model.SpbuInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
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
     * Menggunakan 1x Single Batch Overpass Union Query agar 5x lebih cepat dan bebas HTTP 429.
     */
    suspend fun getSpbuAlongRoute(routePoints: List<LatLng>): List<SpbuInfo> = withContext(Dispatchers.IO) {
        if (routePoints.isEmpty()) return@withContext emptyList()
        if (routePoints.size <= 3) {
            return@withContext getNearbySpbu(routePoints.first())
        }

        // Ambil 5 sampel titik di sepanjang rute (awal, 25%, 50%, 75%, akhir)
        val samplePoints = listOf(
            0,
            routePoints.size / 4,
            routePoints.size / 2,
            (routePoints.size * 3) / 4,
            routePoints.size - 1
        ).distinct().map { routePoints[it] }

        // 1. Single Batch Overpass Union Query (1x HTTP request untuk semua titik sampel)
        val batchCandidates = tryOverpassAlongPoints(samplePoints)
        if (batchCandidates.isNotEmpty()) {
            return@withContext selectDistinctSpbuAlongPoints(batchCandidates, samplePoints, maxResults = 5)
        }

        // 2. Fallback paralel hemat request ke Nominatim (maks 2 titik kunci: awal & akhir)
        Log.w(TAG, "Batch Overpass gagal/kosong, beralih ke fallback paralel Nominatim...")
        val keyPoints = listOf(samplePoints.first(), samplePoints.last()).distinct()
        val nominatimCandidates = coroutineScope {
            keyPoints.map { point ->
                async(Dispatchers.IO) { tryNominatim(point) }
            }.awaitAll().flatten()
        }
        if (nominatimCandidates.isNotEmpty()) {
            return@withContext selectDistinctSpbuAlongPoints(nominatimCandidates, samplePoints, maxResults = 5)
        }

        // 3. Fallback lokal offline (tanpa jaringan)
        Log.w(TAG, "Fallback Nominatim kosong, menggunakan data SPBU lokal terdekat...")
        val localCandidates = keyPoints.flatMap { getLocalFallback(it) }
        return@withContext selectDistinctSpbuAlongPoints(localCandidates, samplePoints, maxResults = 5)
    }

    internal fun buildBatchOverpassQuery(samplePoints: List<LatLng>): String {
        val unionClauses = samplePoints.joinToString("\n") { pt ->
            """
            node["amenity"="fuel"](around:$RADIUS_METERS,${pt.lat},${pt.lng});
            way["amenity"="fuel"](around:$RADIUS_METERS,${pt.lat},${pt.lng});
            """.trimIndent()
        }
        return "[out:json][timeout:10];(\n$unionClauses\n);out center 25;"
    }

    internal fun selectDistinctSpbuAlongPoints(
        candidates: List<SpbuInfo>,
        samplePoints: List<LatLng>,
        maxResults: Int = 5
    ): List<SpbuInfo> {
        val uniqueCandidates = mutableListWithDistinctCoordinates(candidates)
        if (uniqueCandidates.isEmpty() || samplePoints.isEmpty()) {
            return uniqueCandidates.take(maxResults)
        }

        val remaining = uniqueCandidates.toMutableList()
        val selected = mutableListOf<SpbuInfo>()

        // Distribusikan 1 SPBU unik terdekat untuk setiap titik sampel di sepanjang rute
        for (point in samplePoints) {
            if (selected.size >= maxResults || remaining.isEmpty()) break
            val bestMatch = remaining.minByOrNull { spbu ->
                HaversineUtil.hitungJarakKm(point, LatLng(spbu.lat, spbu.lon), faktorJalan = 1.0).toDouble()
            } ?: break

            val distKm = HaversineUtil.hitungJarakKm(point, LatLng(bestMatch.lat, bestMatch.lon), faktorJalan = 1.0).toDouble()
            selected.add(bestMatch.copy(distanceFormatted = formatDistance(distKm)))
            remaining.remove(bestMatch)
        }

        // Jika slot masih tersisa dan masih ada kandidat unik, lengkapi berdasarkan jarak ke origin
        if (selected.size < maxResults && remaining.isNotEmpty()) {
            val origin = samplePoints.first()
            val extras = remaining
                .map { spbu ->
                    val distKm = HaversineUtil.hitungJarakKm(origin, LatLng(spbu.lat, spbu.lon), faktorJalan = 1.0).toDouble()
                    spbu.copy(distanceFormatted = formatDistance(distKm)) to distKm
                }
                .sortedBy { it.second }
                .take(maxResults - selected.size)
                .map { it.first }
            selected.addAll(extras)
        }

        return selected.take(maxResults)
    }

    private fun tryOverpassAlongPoints(points: List<LatLng>): List<SpbuInfo> {
        if (points.isEmpty()) return emptyList()
        val query = buildBatchOverpassQuery(points)
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
                    Log.w(TAG, "Batch Overpass API gagal dengan HTTP ${response.code}: ${response.message}")
                    return emptyList()
                }

                val body = response.body?.string() ?: return emptyList()
                val parsed = parseOverpassResponse(body, points.first())
                Log.i(TAG, "Batch Overpass API sukses menemukan ${parsed.size} kandidat SPBU sepanjang rute")
                parsed.map { it.first }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Batch Overpass API call error: ${e.message}")
            emptyList()
        }
    }

    private fun parseOverpassResponse(body: String, referenceOrigin: LatLng): List<Pair<SpbuInfo, Double>> {
        val root = Json.parseToJsonElement(body).jsonObject
        val elements = root["elements"]?.jsonArray ?: return emptyList()
        val spbuResults = mutableListOf<Pair<SpbuInfo, Double>>()

        for (elemNode in elements) {
            val elem = elemNode.jsonObject
            val type = elem["type"]?.jsonPrimitive?.content.orEmpty()

            val lat: Double
            val lon: Double
            if (type == "way") {
                val center = elem["center"]?.jsonObject
                lat = center?.get("lat")?.jsonPrimitive?.doubleOrNull ?: 0.0
                lon = center?.get("lon")?.jsonPrimitive?.doubleOrNull ?: 0.0
            } else {
                lat = elem["lat"]?.jsonPrimitive?.doubleOrNull ?: 0.0
                lon = elem["lon"]?.jsonPrimitive?.doubleOrNull ?: 0.0
            }

            if (lat == 0.0 || lon == 0.0 || lat.isNaN() || lon.isNaN()) continue

            val spbuCoord = LatLng(lat, lon)
            val distanceKm = HaversineUtil.hitungJarakKm(referenceOrigin, spbuCoord, faktorJalan = 1.0).toDouble()

            val tags = elem["tags"]?.jsonObject
            val rawName = tags?.get("name")?.jsonPrimitive?.content?.trim().orEmpty()
            val rawBrand = tags?.get("brand")?.jsonPrimitive?.content?.trim().orEmpty()
            val rawOperator = tags?.get("operator")?.jsonPrimitive?.content?.trim().orEmpty()

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

        return spbuResults
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
                val sorted = parseOverpassResponse(body, origin).sortedBy { it.second }.take(5).map { it.first }
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
                val array = Json.parseToJsonElement(body).jsonArray

                val list = mutableListOf<Pair<SpbuInfo, Double>>()
                for (elemNode in array) {
                    val obj = elemNode.jsonObject
                    val lat = obj["lat"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: continue
                    val lon = obj["lon"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: continue

                    val spbuCoord = LatLng(lat, lon)
                    val distanceKm = HaversineUtil.hitungJarakKm(origin, spbuCoord, faktorJalan = 1.0).toDouble()

                    val displayName = obj["display_name"]?.jsonPrimitive?.content.orEmpty()
                    val rawName = obj["name"]?.jsonPrimitive?.content.orEmpty()
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
