package com.mybbm.app.data.remote.maps

import android.util.Log
import com.mybbm.app.BuildConfig
import com.mybbm.app.data.remote.datasource.RouteRemoteDataSource
import com.mybbm.app.data.remote.datasource.RouteRemoteDataSource.RouteResult
import com.mybbm.app.data.remote.maps.dto.DirectionsResponseDto
import com.mybbm.app.domain.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.TimeUnit

/**
 * Implementasi [RouteRemoteDataSource] untuk memanggil Google Directions API langsung dari klien
 * (Opsi A — Default v1 sesuai PRD Bagian 7.4 & 7.5).
 *
 * Spesifikasi pemanggilan (PRD Bagian 7.5):
 * - Endpoint: https://maps.googleapis.com/maps/api/directions/json
 * - Parameter: origin, destination, mode=driving, alternatives=false, region=id, language=id, units=metric, key
 * - Header keamanan: X-Android-Package dan X-Android-Cert (opsional)
 * - Jarak: total meter dari seluruh legs dibagi 1000 (BigDecimal skala 2, HALF_UP)
 * - Durasi: total detik dari seluruh legs dikonversi ke menit pembulatan ke atas
 * - Polyline: routes[0].overview_polyline.points
 *
 * Penanganan status API:
 * - OK              -> [RouteResult.Success]
 * - ZERO_RESULTS    -> [RouteResult.ZeroResults]
 * - OVER_QUERY_LIMIT, UNKNOWN_ERROR -> [RouteResult.ApiError] (memicu fallback di use case)
 * - REQUEST_DENIED  -> [RouteResult.ConfigError] (dicatat di log)
 * - Galat jaringan  -> [RouteResult.NetworkError]
 */
class GoogleMapsRouteDataSource(
    private val apiKey: String = BuildConfig.MAPS_API_KEY,
    private val packageName: String = "com.mybbm.app",
    private val sha1Fingerprint: String? = null,
    private val httpClient: OkHttpClient = defaultOkHttpClient
) : RouteRemoteDataSource {

    private val jsonParser = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    override suspend fun getRoute(origin: LatLng, destination: LatLng): RouteResult = withContext(Dispatchers.IO) {
        // Validasi keberadaan API Key
        if (apiKey.isBlank()) {
            Log.w(TAG, "MAPS_API_KEY kosong di BuildConfig! Harap konfigurasikan local.properties.")
            return@withContext RouteResult.ConfigError
        }

        // Susun URL dan query parameters (PRD Bagian 7.5)
        val httpUrl = BASE_URL.toHttpUrlOrNull()?.newBuilder()
            ?.addQueryParameter("origin", "${origin.lat},${origin.lng}")
            ?.addQueryParameter("destination", "${destination.lat},${destination.lng}")
            ?.addQueryParameter("mode", "driving")
            ?.addQueryParameter("alternatives", "false")
            ?.addQueryParameter("region", "id")
            ?.addQueryParameter("language", "id")
            ?.addQueryParameter("units", "metric")
            ?.addQueryParameter("key", apiKey)
            ?.build()

        if (httpUrl == null) {
            Log.e(TAG, "Gagal menyusun URL Directions API")
            return@withContext RouteResult.ApiError("INVALID_URL")
        }

        // Siapkan request dengan header identitas aplikasi Android
        val requestBuilder = Request.Builder()
            .url(httpUrl)
            .addHeader("X-Android-Package", packageName)

        if (!sha1Fingerprint.isNullOrBlank()) {
            requestBuilder.addHeader("X-Android-Cert", sha1Fingerprint)
        }

        val request = requestBuilder.build()

        try {
            httpClient.newCall(request).execute().use { response ->
                val responseBody = response.body?.string()

                if (!response.isSuccessful) {
                    val code = response.code
                    Log.e(TAG, "HTTP error $code dari Directions API: $responseBody")
                    return@withContext if (code in 400..499) {
                        RouteResult.ConfigError
                    } else {
                        RouteResult.NetworkError(IOException("HTTP error $code"))
                    }
                }

                if (responseBody.isNullOrBlank()) {
                    Log.e(TAG, "Body respons Directions API kosong")
                    return@withContext RouteResult.ApiError("EMPTY_RESPONSE")
                }

                // Decode payload JSON menggunakan kotlinx.serialization
                val directionsDto = try {
                    jsonParser.decodeFromString<DirectionsResponseDto>(responseBody)
                } catch (e: Exception) {
                    Log.e(TAG, "Gagal mengurai respons JSON: ${e.message}", e)
                    return@withContext RouteResult.ApiError("MALFORMED_JSON: ${e.message}")
                }

                // Evaluasi status respons dari Google Maps
                when (directionsDto.status) {
                    STATUS_OK -> {
                        val routes = directionsDto.routes
                        if (routes.isEmpty()) {
                            Log.w(TAG, "Status OK namun array routes kosong")
                            return@withContext RouteResult.ZeroResults
                        }

                        val firstRoute = routes[0]
                        val legs = firstRoute.legs

                        // 1. Hitung total jarak dalam meter -> konversi ke km (skala 2, HALF_UP)
                        val totalMeters = legs.sumOf { it.distance?.value ?: 0L }
                        val jarakKm = BigDecimal(totalMeters)
                            .divide(METERS_IN_KM, 2, RoundingMode.HALF_UP)

                        // 2. Hitung total durasi dalam detik -> konversi ke menit pembulatan ke atas
                        val totalSeconds = legs.sumOf { it.duration?.value ?: 0L }
                        val durasiMenit = if (totalSeconds > 0) {
                            ((totalSeconds + 59) / 60).toInt()
                        } else {
                            0
                        }

                        // 3. Ambil encoded overview polyline
                        val encodedPolyline = firstRoute.overviewPolyline?.points ?: ""

                        Log.d(TAG, "Directions API berhasil: jarak=${jarakKm}km, durasi=${durasiMenit}mnt")

                        RouteResult.Success(
                            jarakKm = jarakKm,
                            durasiMenit = durasiMenit,
                            encodedPolyline = encodedPolyline
                        )
                    }

                    STATUS_ZERO_RESULTS -> {
                        Log.i(TAG, "Rute tidak ditemukan antara asal (${origin.lat},${origin.lng}) dan tujuan (${destination.lat},${destination.lng})")
                        RouteResult.ZeroResults
                    }

                    STATUS_OVER_QUERY_LIMIT -> {
                        Log.w(TAG, "Kuota Directions API terlampaui (OVER_QUERY_LIMIT). Beralih ke fallback.")
                        RouteResult.ApiError(STATUS_OVER_QUERY_LIMIT)
                    }

                    STATUS_REQUEST_DENIED -> {
                        Log.e(TAG, "Akses Directions API ditolak (REQUEST_DENIED): ${directionsDto.errorMessage}")
                        RouteResult.ConfigError
                    }

                    STATUS_UNKNOWN_ERROR -> {
                        Log.w(TAG, "Server Google mengalami galat sementara (UNKNOWN_ERROR). Beralih ke fallback.")
                        RouteResult.ApiError(STATUS_UNKNOWN_ERROR)
                    }

                    else -> {
                        Log.w(TAG, "Status tidak dikenal dari Directions API: ${directionsDto.status} - ${directionsDto.errorMessage}")
                        RouteResult.ApiError(directionsDto.status)
                    }
                }
            }
        } catch (e: IOException) {
            Log.w(TAG, "Galat jaringan saat menghubungi Directions API: ${e.message}")
            RouteResult.NetworkError(e)
        } catch (e: Exception) {
            Log.e(TAG, "Kesalahan tak terduga saat menghubungi Directions API: ${e.message}", e)
            RouteResult.NetworkError(e)
        }
    }

    companion object {
        private const val TAG = "GoogleMapsRouteDataSource"
        private const val BASE_URL = "https://maps.googleapis.com/maps/api/directions/json"

        private val METERS_IN_KM = BigDecimal("1000")

        private const val STATUS_OK = "OK"
        private const val STATUS_ZERO_RESULTS = "ZERO_RESULTS"
        private const val STATUS_OVER_QUERY_LIMIT = "OVER_QUERY_LIMIT"
        private const val STATUS_REQUEST_DENIED = "REQUEST_DENIED"
        private const val STATUS_UNKNOWN_ERROR = "UNKNOWN_ERROR"

        /**
         * Instance OkHttpClient default dengan timeout 15 detik.
         */
        val defaultOkHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build()
        }
    }
}
