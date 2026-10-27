package com.mybbm.app.data.remote.maps

import com.mybbm.app.data.remote.datasource.RouteRemoteDataSource
import com.mybbm.app.domain.model.LatLng
import kotlinx.coroutines.runBlocking
import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.math.BigDecimal

class GoogleMapsRouteDataSourceTest {

    private fun createMockClient(responseCode: Int, responseJson: String): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(responseCode)
                    .message("OK")
                    .body(responseJson.toResponseBody("application/json".toMediaTypeOrNull()))
                    .build()
            }
            .build()
    }

    @Test
    fun getRoute_blankApiKey_mengembalikanConfigError() = runBlocking {
        val dataSource = GoogleMapsRouteDataSource(apiKey = "")
        val result = dataSource.getRoute(LatLng(0.0, 0.0), LatLng(1.0, 1.0))
        assertTrue(result is RouteRemoteDataSource.RouteResult.ConfigError)
    }

    @Test
    fun getRoute_statusOk_berhasilParsingJarakDurasiDanPolyline() = runBlocking {
        val mockJson = """
            {
              "status": "OK",
              "routes": [
                {
                  "overview_polyline": {
                    "points": "_p~iF~ps|U_ulLnnqC_mqNvxq`@"
                  },
                  "legs": [
                    {
                      "distance": {
                        "text": "120 km",
                        "value": 120500
                      },
                      "duration": {
                        "text": "2 jam 15 mnt",
                        "value": 8100
                      }
                    }
                  ]
                }
              ]
            }
        """.trimIndent()

        val dataSource = GoogleMapsRouteDataSource(
            apiKey = "test_key",
            httpClient = createMockClient(200, mockJson)
        )

        val result = dataSource.getRoute(
            LatLng(-0.5021, 117.1537),
            LatLng(-1.2653, 116.8312)
        )

        assertTrue(result is RouteRemoteDataSource.RouteResult.Success)
        val success = result as RouteRemoteDataSource.RouteResult.Success
        assertEquals(BigDecimal("120.50"), success.jarakKm)
        assertEquals(135, success.durasiMenit) // 8100 detik / 60 = 135 menit
        assertEquals("_p~iF~ps|U_ulLnnqC_mqNvxq`@", success.encodedPolyline)
    }

    @Test
    fun getRoute_zeroResults_mengembalikanZeroResults() = runBlocking {
        val mockJson = """
            {
              "status": "ZERO_RESULTS",
              "routes": []
            }
        """.trimIndent()

        val dataSource = GoogleMapsRouteDataSource(
            apiKey = "test_key",
            httpClient = createMockClient(200, mockJson)
        )

        val result = dataSource.getRoute(LatLng(0.0, 0.0), LatLng(10.0, 10.0))
        assertTrue(result is RouteRemoteDataSource.RouteResult.ZeroResults)
    }

    @Test
    fun getRoute_overQueryLimit_mengembalikanApiError() = runBlocking {
        val mockJson = """
            {
              "status": "OVER_QUERY_LIMIT",
              "error_message": "You have exceeded your daily request quota for this API."
            }
        """.trimIndent()

        val dataSource = GoogleMapsRouteDataSource(
            apiKey = "test_key",
            httpClient = createMockClient(200, mockJson)
        )

        val result = dataSource.getRoute(LatLng(0.0, 0.0), LatLng(1.0, 1.0))
        assertTrue(result is RouteRemoteDataSource.RouteResult.ApiError)
        assertEquals("OVER_QUERY_LIMIT", (result as RouteRemoteDataSource.RouteResult.ApiError).statusCode)
    }

    @Test
    fun getRoute_requestDenied_mengembalikanConfigError() = runBlocking {
        val mockJson = """
            {
              "status": "REQUEST_DENIED",
              "error_message": "The provided API key is invalid."
            }
        """.trimIndent()

        val dataSource = GoogleMapsRouteDataSource(
            apiKey = "test_key",
            httpClient = createMockClient(200, mockJson)
        )

        val result = dataSource.getRoute(LatLng(0.0, 0.0), LatLng(1.0, 1.0))
        assertTrue(result is RouteRemoteDataSource.RouteResult.ConfigError)
    }
}
