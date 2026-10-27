package com.mybbm.app.data.remote.datasource

import com.mybbm.app.domain.model.LatLng
import java.math.BigDecimal

/**
 * Kontrak antarmuka untuk memperoleh data rute dari sumber eksternal (Directions API).
 *
 * Abstraksi ini memisahkan implementasi jaringan dari Core Engine dan use case
 * sehingga:
 * - **Pengujian unit:** implementasi palsu ([RouteRemoteDataSource]) dapat disuntikkan
 *   tanpa memerlukan koneksi jaringan.
 * - **Portabilitas implementasi:** perpindahan dari Opsi A (panggilan langsung) ke
 *   Opsi B (proksi Supabase Edge Function) tidak memengaruhi lapisan domain sama sekali.
 *   Cukup ganti implementasi tanpa mengubah use case.
 *
 * Sesuai PRD Bagian 7.5 dan Bagian 7.4.
 */
interface RouteRemoteDataSource {

    /**
     * Mengambil data rute antara dua titik koordinat dari Directions API.
     *
     * Implementasi harus:
     * - Menggunakan parameter `mode=driving`, `region=id`, `language=id`, `units=metric`.
     * - Menjumlahkan `routes[0].legs[*].distance.value` (meter) lalu dibagi 1000
     *   untuk mendapatkan jarak dalam km ([BigDecimal], skala 2).
     * - Menyimpan `routes[0].overview_polyline.points` sebagai encoded polyline.
     * - Menangani status respons API:
     *   - `OK` → kembalikan [RouteResult.Success].
     *   - `ZERO_RESULTS` → kembalikan [RouteResult.ZeroResults].
     *   - `OVER_QUERY_LIMIT` / `UNKNOWN_ERROR` → kembalikan [RouteResult.ApiError]
     *     dengan pesan yang sesuai.
     *   - Timeout / IOException → kembalikan [RouteResult.NetworkError].
     *   - `REQUEST_DENIED` → kembalikan [RouteResult.ConfigError] dan catat ke log.
     *
     * @param origin      Koordinat titik asal perjalanan.
     * @param destination Koordinat titik tujuan perjalanan.
     * @return [RouteResult] yang membungkus data rute atau keterangan kegagalan.
     */
    suspend fun getRoute(origin: LatLng, destination: LatLng): RouteResult

    // =========================================================================
    // Tipe Hasil
    // =========================================================================

    /**
     * Hasil pemanggilan [getRoute].
     *
     * Implementasi [RouteRemoteDataSource] harus menggunakan sealed class ini agar
     * pemanggil (repository, use case) wajib menangani semua kemungkinan secara
     * eksplisit melalui `when`.
     */
    sealed class RouteResult {

        /**
         * Rute berhasil diperoleh dari Directions API.
         *
         * @property jarakKm         Jarak total rute dalam kilometer (skala 2, HALF_UP).
         * @property durasiMenit     Estimasi waktu tempuh dalam menit.
         * @property encodedPolyline Encoded polyline rute untuk ditampilkan di peta offline.
         */
        data class Success(
            val jarakKm: BigDecimal,
            val durasiMenit: Int,
            val encodedPolyline: String
        ) : RouteResult()

        /**
         * API berhasil dihubungi tetapi tidak ada rute yang ditemukan (`ZERO_RESULTS`).
         * UI harus menampilkan pesan "rute tidak ditemukan" dan menawarkan input manual.
         */
        object ZeroResults : RouteResult()

        /**
         * Gagal terhubung ke Directions API karena masalah jaringan (timeout, IOException, dll).
         * Pemanggil harus beralih ke fallback Haversine.
         *
         * @property cause Exception penyebab kegagalan (opsional, untuk logging).
         */
        data class NetworkError(val cause: Throwable? = null) : RouteResult()

        /**
         * API mengembalikan status error yang bukan `ZERO_RESULTS` (`OVER_QUERY_LIMIT`,
         * `UNKNOWN_ERROR`, dll). Pemanggil harus beralih ke fallback Haversine.
         *
         * @property statusCode Status string dari Directions API.
         */
        data class ApiError(val statusCode: String) : RouteResult()

        /**
         * Kunci API tidak valid atau tidak memiliki izin yang diperlukan (`REQUEST_DENIED`).
         * Harus dicatat ke log; jangan coba ulang secara otomatis.
         */
        object ConfigError : RouteResult()
    }
}
