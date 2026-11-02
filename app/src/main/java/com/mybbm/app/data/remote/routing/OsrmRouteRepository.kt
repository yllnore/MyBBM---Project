package com.mybbm.app.data.remote.routing

import android.util.Log
import com.mybbm.app.domain.engine.HaversineUtil
import com.mybbm.app.domain.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.TimeUnit

/**
 * Hasil perutean dari [OsrmRouteRepository].
 */
data class RouteData(
    val distanceKm: BigDecimal,
    val durationMinutes: Double,
    val polylinePoints: List<LatLng>,
    val isFallback: Boolean = false
)

/**
 * Service & Repository untuk memanggil OSRM (Open Source Routing Machine) API
 * dengan fallback otomatis ke HaversineUtil.
 */
class OsrmRouteRepository(
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
) {

    /**
     * Mengambil rute mengemudi antara [origin] dan [destination].
     * Jika gagal atau offline, mengembalikan estimasi fallback menggunakan [HaversineUtil].
     */
    suspend fun getDrivingRoute(
        origin: LatLng,
        destination: LatLng
    ): RouteData = withContext(Dispatchers.IO) {
        // Format OSRM: /route/v1/driving/{lon1},{lat1};{lon2},{lat2}?overview=full&geometries=geojson
        val url = "https://router.project-osrm.org/route/v1/driving/${origin.lng},${origin.lat};${destination.lng},${destination.lat}?overview=full&geometries=geojson"

        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "MyBBM-Android-App")
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyString = response.body?.string()
                    if (!bodyString.isNullOrBlank()) {
                        val json = JSONObject(bodyString)
                        val code = json.optString("code")
                        if (code.equals("Ok", ignoreCase = true)) {
                            val routes = json.optJSONArray("routes")
                            if (routes != null && routes.length() > 0) {
                                val firstRoute = routes.getJSONObject(0)
                                val distanceMeters = firstRoute.optDouble("distance", 0.0)
                                val durationSeconds = firstRoute.optDouble("duration", 0.0)

                                val distanceKm = BigDecimal.valueOf(distanceMeters / 1000.0)
                                    .setScale(2, RoundingMode.HALF_UP)
                                val durationMinutes = durationSeconds / 60.0

                                val points = mutableListOf<LatLng>()
                                val geometry = firstRoute.optJSONObject("geometry")
                                val coordinates = geometry?.optJSONArray("coordinates")
                                if (coordinates != null) {
                                    for (i in 0 until coordinates.length()) {
                                        val coordPair = coordinates.getJSONArray(i)
                                        // GeoJSON coordinates format: [longitude, latitude]
                                        val lon = coordPair.getDouble(0)
                                        val lat = coordPair.getDouble(1)
                                        points.add(LatLng(lat, lon))
                                    }
                                }

                                if (points.isEmpty()) {
                                    points.add(origin)
                                    points.add(destination)
                                }

                                Log.i(TAG, "OSRM Route sukses didapat: $distanceKm km, ${points.size} titik")
                                return@withContext RouteData(
                                    distanceKm = distanceKm,
                                    durationMinutes = durationMinutes,
                                    polylinePoints = points,
                                    isFallback = false
                                )
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Gagal memanggil OSRM API (${e.message}), beralih ke fallback Haversine.", e)
        }

        // Fallback ke Haversine dengan faktor jalan 1.25
        val fallbackDistance = HaversineUtil.hitungJarakKm(origin, destination, faktorJalan = 1.25)
        Log.i(TAG, "Fallback Haversine digunakan: $fallbackDistance km")

        RouteData(
            distanceKm = fallbackDistance,
            durationMinutes = (fallbackDistance.toDouble() / 40.0) * 60.0, // Estimasi 40 km/jam
            polylinePoints = listOf(origin, destination),
            isFallback = true
        )
    }

    companion object {
        private const val TAG = "OsrmRouteRepository"
    }
}

