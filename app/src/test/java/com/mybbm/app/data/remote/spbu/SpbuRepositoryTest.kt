package com.mybbm.app.data.remote.spbu

import com.mybbm.app.domain.engine.HaversineUtil
import com.mybbm.app.domain.model.LatLng
import com.mybbm.app.domain.model.SpbuInfo
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class SpbuRepositoryTest {

    private val repository = SpbuRepository()

    @Test
    fun testDetectBrand() {
        assertEquals("Pertamina", repository.detectBrand("SPBU Pertamina 61.751.02"))
        assertEquals("Shell", repository.detectBrand("SPBU Shell MT Haryono"))
        assertEquals("BP", repository.detectBrand("BP AKR Fuel Station"))
        assertEquals("Vivo", repository.detectBrand("SPBU Vivo Cilandak"))
        assertEquals("Pertamina", repository.detectBrand("SPBU Tanpa Merek"))
    }

    @Test
    fun testFormatDistance() {
        assertEquals("350 m", repository.formatDistance(0.35))
        assertEquals("1,2 km", repository.formatDistance(1.2))
        assertEquals("4,5 km", repository.formatDistance(4.53))
    }

    @Test
    fun testFormatSpbuName() {
        val nameWithRaw = repository.formatSpbuName("SPBU Juanda", "Pertamina", "", "Pertamina")
        assertEquals("SPBU Juanda", nameWithRaw)

        val nameFromBrand = repository.formatSpbuName("", "Shell", "", "Shell")
        assertEquals("SPBU Shell", nameFromBrand)

        val nameFromOperator = repository.formatSpbuName("", "", "AKR", "BP")
        assertEquals("SPBU AKR", nameFromOperator)

        val nameFallback = repository.formatSpbuName("", "", "", "Pertamina")
        assertEquals("SPBU Pertamina", nameFallback)
    }

    @Test
    fun testHaversineDistanceOrdering() {
        val origin = LatLng(-0.502106, 117.153709) // Polnes Samarinda
        val spbuNear = LatLng(-0.501092, 117.124156) // ~3.3 km
        val spbuFar = LatLng(-1.265000, 116.890000) // Balikpapan (~90 km)

        val distNear = HaversineUtil.hitungJarakKm(origin, spbuNear, faktorJalan = 1.0).toDouble()
        val distFar = HaversineUtil.hitungJarakKm(origin, spbuFar, faktorJalan = 1.0).toDouble()

        assertTrue(distNear < distFar)
        assertTrue(distNear < 5.0)
        assertTrue(distFar > 50.0)
    }

    @Test
    fun testBuildBatchOverpassQuery_combinesAllSamplePointsInSingleUnion() {
        val points = listOf(
            LatLng(-0.5021, 117.1537),
            LatLng(-0.8500, 117.0000),
            LatLng(-1.2653, 116.8312)
        )

        val query = repository.buildBatchOverpassQuery(points)

        assertTrue(query.startsWith("[out:json][timeout:10];("))
        assertTrue(query.endsWith(");out center 25;"))
        points.forEach { pt ->
            assertTrue(query.contains("node[\"amenity\"=\"fuel\"](around:5000,${pt.lat},${pt.lng});"))
            assertTrue(query.contains("way[\"amenity\"=\"fuel\"](around:5000,${pt.lat},${pt.lng});"))
        }
    }

    @Test
    fun testSelectDistinctSpbuAlongPoints_deduplicatesAndDistributesAcrossRoute() {
        val samplePoints = listOf(
            LatLng(-0.5000, 117.1500),
            LatLng(-0.7500, 117.0500),
            LatLng(-1.0000, 116.9500),
            LatLng(-1.2500, 116.8500)
        )

        val candidates = listOf(
            SpbuInfo("SPBU Invalid", "0 m", "Pertamina", 0.0, 0.0),
            SpbuInfo("SPBU Samarinda A", "1,0 km", "Pertamina", -0.5010, 117.1505),
            SpbuInfo("spbu samarinda a", "1,0 km", "Pertamina", -0.5012, 117.1506), // duplikat nama
            SpbuInfo("SPBU Dekat A", "1,1 km", "Pertamina", -0.5011, 117.1506), // duplikat koordinat (< 0.0015)
            SpbuInfo("SPBU Tengah 1", "2,0 km", "Pertamina", -0.7510, 117.0505),
            SpbuInfo("SPBU Tengah 2", "1,5 km", "Shell", -1.0010, 116.9505),
            SpbuInfo("SPBU Balikpapan", "0,5 km", "Pertamina", -1.2505, 116.8502)
        )

        val selected = repository.selectDistinctSpbuAlongPoints(candidates, samplePoints, maxResults = 5)

        assertEquals(4, selected.size)
        assertEquals("SPBU Samarinda A", selected[0].name)
        assertEquals("SPBU Tengah 1", selected[1].name)
        assertEquals("SPBU Tengah 2", selected[2].name)
        assertEquals("SPBU Balikpapan", selected[3].name)
    }

    @Test
    fun testGetSpbuAlongRoute_usesSingleBatchOverpassRequest() = runBlocking {
        val requestCounter = AtomicInteger(0)
        val fakeOverpassJson = """
            {
              "elements": [
                {
                  "type": "node",
                  "lat": -0.5025,
                  "lon": 117.1540,
                  "tags": { "name": "SPBU Pertamina Diponegoro", "brand": "Pertamina" }
                },
                {
                  "type": "node",
                  "lat": -0.5026,
                  "lon": 117.1541,
                  "tags": { "name": "SPBU Pertamina Diponegoro", "brand": "Pertamina" }
                },
                {
                  "type": "way",
                  "center": { "lat": -0.8800, "lon": 117.0100 },
                  "tags": { "name": "SPBU Bukit Soeharto", "brand": "Pertamina" }
                },
                {
                  "type": "node",
                  "lat": -1.2640,
                  "lon": 116.8320,
                  "tags": { "name": "SPBU Shell Balikpapan", "brand": "Shell" }
                }
              ]
            }
        """.trimIndent()

        val fakeClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                requestCounter.incrementAndGet()
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(fakeOverpassJson.toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()

        val testRepo = SpbuRepository(fakeClient)
        val route = listOf(
            LatLng(-0.5021, 117.1537),
            LatLng(-0.6900, 117.0800),
            LatLng(-0.8800, 117.0100),
            LatLng(-1.0700, 116.9200),
            LatLng(-1.2653, 116.8312)
        )

        val result = testRepo.getSpbuAlongRoute(route)

        assertEquals("Harus hanya mengirim 1 batch HTTP request ke Overpass", 1, requestCounter.get())
        assertEquals(3, result.size)
        assertTrue(result.any { it.name == "SPBU Pertamina Diponegoro" && it.brand == "Pertamina" })
        assertTrue(result.any { it.name == "SPBU Bukit Soeharto" && it.brand == "Pertamina" })
        assertTrue(result.any { it.name == "SPBU Shell Balikpapan" && it.brand == "Shell" })
    }

    @Test
    fun testGetSpbuAlongRoute_emptyRouteReturnsEmptyList() = runBlocking {
        assertTrue(repository.getSpbuAlongRoute(emptyList()).isEmpty())
    }

    @Test
    fun testGetSpbuAlongRoute_fallsBackGracefullyWhenOverpassAndNominatimFail() = runBlocking {
        val requestCounter = AtomicInteger(0)
        val failingClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                requestCounter.incrementAndGet()
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(429)
                    .message("Too Many Requests")
                    .body("".toResponseBody("text/plain".toMediaType()))
                    .build()
            }
            .build()

        val testRepo = SpbuRepository(failingClient)
        val route = listOf(
            LatLng(-0.5021, 117.1537),
            LatLng(-0.6900, 117.0800),
            LatLng(-0.8800, 117.0100),
            LatLng(-1.0700, 116.9200),
            LatLng(-1.2653, 116.8312)
        )

        val result = testRepo.getSpbuAlongRoute(route)

        assertTrue("Fallback lokal harus tetap mengembalikan SPBU", result.isNotEmpty())
        assertTrue("Maksimal 5 SPBU dikembalikan", result.size <= 5)
        assertTrue("Fallback hemat request (1 Overpass + maks 2 Nominatim)", requestCounter.get() <= 3)
    }

    @Test
    fun testGetSpbuAlongRoute_twoPointOfflineRouteCoversOriginAndDestination() = runBlocking {
        val offlineClient = OkHttpClient.Builder()
            .addInterceptor {
                throw java.io.IOException("Device offline")
            }
            .build()

        val testRepo = SpbuRepository(offlineClient)
        val offlineTwoPointRoute = listOf(
            LatLng(-0.502106, 117.153709), // Samarinda
            LatLng(-1.265386, 116.831200)  // Balikpapan
        )

        val result = testRepo.getSpbuAlongRoute(offlineTwoPointRoute)

        assertTrue("Rute offline 2 titik harus mengembalikan SPBU", result.isNotEmpty())
        assertTrue(
            "Rute offline Samarinda-Balikpapan harus mencakup SPBU dekat tujuan Balikpapan",
            result.any { it.lat < -1.0 }
        )
        assertTrue(
            "Rute offline Samarinda-Balikpapan harus mencakup SPBU dekat asal Samarinda",
            result.any { it.lat > -0.7 }
        )
    }

    @Test
    fun testGetSpbuAlongRoute_acceptsEquatorialCoordinatesWithZeroLatitude() = runBlocking {
        val equatorJson = """
            {
              "elements": [
                {
                  "type": "node",
                  "lat": 0.0,
                  "lon": 117.5000,
                  "tags": { "name": "SPBU Khatulistiwa Santan", "brand": "Pertamina" }
                }
              ]
            }
        """.trimIndent()

        val fakeClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(equatorJson.toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()

        val testRepo = SpbuRepository(fakeClient)
        val route = listOf(
            LatLng(-0.0100, 117.4900),
            LatLng(0.0100, 117.5100)
        )

        val result = testRepo.getSpbuAlongRoute(route)
        assertEquals(1, result.size)
        assertEquals("SPBU Khatulistiwa Santan", result.first().name)
        assertEquals(0.0, result.first().lat, 0.0001)
    }

    @Test
    fun testSelectDistinctSpbuAlongPoints_formatsDistanceConsistentlyFromOrigin() {
        val samplePoints = listOf(
            LatLng(-0.5000, 117.1500), // Samarinda (origin)
            LatLng(-1.2500, 116.8500)  // Balikpapan (~90 km away)
        )
        val candidates = listOf(
            SpbuInfo("SPBU Samarinda", "0 m", "Pertamina", -0.5010, 117.1505),
            SpbuInfo("SPBU Balikpapan", "0 m", "Pertamina", -1.2505, 116.8502)
        )

        val selected = repository.selectDistinctSpbuAlongPoints(candidates, samplePoints, maxResults = 5)

        assertEquals(2, selected.size)
        assertTrue("SPBU dekat origin harus < 1 km (${selected[0].distanceFormatted})", selected[0].distanceFormatted.endsWith("m"))
        assertTrue("SPBU dekat tujuan harus menunjukkan jarak dari origin dalam km (${selected[1].distanceFormatted})", selected[1].distanceFormatted.endsWith("km"))
    }
}
