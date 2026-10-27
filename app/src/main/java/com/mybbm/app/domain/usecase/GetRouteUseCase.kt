package com.mybbm.app.domain.usecase

import android.util.Log
import com.mybbm.app.data.local.dao.PerjalananDao
import com.mybbm.app.data.local.entity.PerjalananEntity
import com.mybbm.app.data.local.entity.SyncState
import com.mybbm.app.data.remote.datasource.RouteRemoteDataSource
import com.mybbm.app.data.remote.datasource.RouteRemoteDataSource.RouteResult
import com.mybbm.app.domain.engine.HaversineUtil
import com.mybbm.app.domain.model.LatLng
import java.util.UUID

/**
 * Use case untuk memperoleh data rute (jarak, durasi, polyline) antara dua koordinat geografis.
 *
 * Menerapkan diagram alur PRD Bagian 7.2, 7.5, 7.7 (Cache), dan 7.8 (Fallback):
 * 1. **Pemeriksaan Cache Lokal (Room):**
 *    Mencari rute identik yang tersimpan di Room via [PerjalananDao.findCached].
 *    Jika ditemukan dan usia cache ≤ 90 hari, langsung kembalikan tanpa pemanggilan jaringan.
 * 2. **Panggilan Directions API (Jika Tidak Ada di Cache / Force Refresh):**
 *    Menghubungi [RouteRemoteDataSource] untuk rute mengemudi di Kalimantan Timur.
 * 3. **Penyimpanan Cache (Jika API Berhasil):**
 *    Menyimpan [PerjalananEntity] baru dengan `sumber_jarak = "DIRECTIONS_API"` ke Room
 *    dan menandai status sync_state = [SyncState.PENDING].
 * 4. **Fallback Luring Haversine (Jika API Gagal / Kuota Habis / Perangkat Luring):**
 *    Menghitung estimasi jarak menggunakan [HaversineUtil.hitungJarakKm] (faktor jalan 1.3),
 *    menyimpan ke Room dengan `sumber_jarak = "HAVERSINE"` dan mengembalikan data berlabel estimasi.
 */
class GetRouteUseCase(
    private val perjalananDao: PerjalananDao,
    private val routeRemoteDataSource: RouteRemoteDataSource
) {

    /**
     * Mengeksekusi pencarian rute dengan alur cache -> API -> fallback Haversine.
     *
     * @param origin             Koordinat titik awal perjalanan.
     * @param destination        Koordinat titik tujuan perjalanan.
     * @param originName         Nama tempat asal (opsional, dari Places API atau input pengguna).
     * @param destinationName    Nama tempat tujuan (opsional, dari Places API atau input pengguna).
     * @param originPlaceId      Google Place ID asal (opsional).
     * @param destinationPlaceId Google Place ID tujuan (opsional).
     * @param label              Label deskriptif rute (opsional, contoh: "Kantor ke Rumah").
     * @param forceRefresh       Jika true, mengabaikan cache lokal dan memaksa pemanggilan API.
     * @return [GetRouteResult] yang membawa data [PerjalananEntity] dan status perolehan.
     */
    suspend fun execute(
        origin: LatLng,
        destination: LatLng,
        originName: String? = null,
        destinationName: String? = null,
        originPlaceId: String? = null,
        destinationPlaceId: String? = null,
        label: String? = null,
        forceRefresh: Boolean = false
    ): GetRouteResult {
        val now = System.currentTimeMillis()

        // =========================================================================
        // LANGKAH 1: Cek Cache Lokal di Room (PRD Bagian 7.2 & 7.7)
        // =========================================================================
        if (!forceRefresh) {
            val cached = perjalananDao.findCached(
                asalLat = origin.lat,
                asalLng = origin.lng,
                tujuanLat = destination.lat,
                tujuanLng = destination.lng
            )

            if (cached != null) {
                val ageMs = now - cached.createdAt
                if (ageMs <= CACHE_TTL_MS) {
                    Log.d(TAG, "Rute ditemukan di cache Room (ID: ${cached.idPerjalanan}, jarak: ${cached.jarakKm} km)")
                    return GetRouteResult.Success(
                        perjalanan = cached,
                        isFromCache = true,
                        isFallback = false
                    )
                } else {
                    Log.d(TAG, "Cache rute kedaluwarsa (> 90 hari), mencoba memperbarui via Directions API.")
                }
            }
        }

        // =========================================================================
        // LANGKAH 2: Panggil Directions API (PRD Bagian 7.2 & 7.5)
        // =========================================================================
        Log.d(TAG, "Memanggil Directions API untuk rute: ($origin) -> ($destination)...")
        val apiResult = routeRemoteDataSource.getRoute(origin, destination)

        return when (apiResult) {
            is RouteResult.Success -> {
                // =====================================================================
                // LANGKAH 3: Simpan Hasil API ke Room sebagai Cache Baru
                // =====================================================================
                val perjalananBaru = PerjalananEntity(
                    idPerjalanan = UUID.randomUUID().toString(),
                    label = label,
                    asalNama = originName,
                    tujuanNama = destinationName,
                    asalPlaceId = originPlaceId,
                    tujuanPlaceId = destinationPlaceId,
                    asalLat = origin.lat,
                    asalLng = origin.lng,
                    tujuanLat = destination.lat,
                    tujuanLng = destination.lng,
                    jarakKm = apiResult.jarakKm.toPlainString(),
                    durasiMenit = apiResult.durasiMenit,
                    encodedPolyline = apiResult.encodedPolyline,
                    sumberJarak = SUMBER_DIRECTIONS_API,
                    createdAt = now,
                    updatedAt = now,
                    deletedAt = null,
                    syncState = SyncState.PENDING
                )

                perjalananDao.insert(perjalananBaru)
                Log.i(TAG, "Rute baru berhasil disimpan ke Room (ID: ${perjalananBaru.idPerjalanan}, jarak: ${perjalananBaru.jarakKm} km)")

                GetRouteResult.Success(
                    perjalanan = perjalananBaru,
                    isFromCache = false,
                    isFallback = false
                )
            }

            is RouteResult.ZeroResults -> {
                Log.w(TAG, "Directions API mengembalikan ZERO_RESULTS")
                GetRouteResult.RouteNotFound(
                    message = "Tidak ada rute mengemudi yang ditemukan antara titik asal dan tujuan."
                )
            }

            is RouteResult.NetworkError,
            is RouteResult.ApiError,
            is RouteResult.ConfigError -> {
                // =====================================================================
                // LANGKAH 4: Fallback Luring Menggunakan Haversine (PRD Bagian 7.2 & 7.8)
                // =====================================================================
                Log.w(TAG, "Directions API gagal ($apiResult). Menjalankan fallback Haversine.")

                val jarakEstimasi = HaversineUtil.hitungJarakKm(origin, destination)

                val fallbackPerjalanan = PerjalananEntity(
                    idPerjalanan = UUID.randomUUID().toString(),
                    label = label,
                    asalNama = originName,
                    tujuanNama = destinationName,
                    asalPlaceId = originPlaceId,
                    tujuanPlaceId = destinationPlaceId,
                    asalLat = origin.lat,
                    asalLng = origin.lng,
                    tujuanLat = destination.lat,
                    tujuanLng = destination.lng,
                    jarakKm = jarakEstimasi.toPlainString(),
                    durasiMenit = null,
                    encodedPolyline = null,
                    sumberJarak = SUMBER_HAVERSINE,
                    createdAt = now,
                    updatedAt = now,
                    deletedAt = null,
                    syncState = SyncState.PENDING
                )

                perjalananDao.insert(fallbackPerjalanan)
                Log.i(TAG, "Rute estimasi Haversine berhasil disimpan ke Room (ID: ${fallbackPerjalanan.idPerjalanan}, jarak: ${fallbackPerjalanan.jarakKm} km)")

                GetRouteResult.Success(
                    perjalanan = fallbackPerjalanan,
                    isFromCache = false,
                    isFallback = true
                )
            }
        }
    }

    companion object {
        private const val TAG = "GetRouteUseCase"

        const val SUMBER_DIRECTIONS_API = "DIRECTIONS_API"
        const val SUMBER_HAVERSINE = "HAVERSINE"

        /** Masa berlaku cache rute: 90 hari (PRD Bagian 7.7). */
        const val CACHE_TTL_MS = 90L * 24 * 60 * 60 * 1000L
    }
}

/**
 * Representasi hasil eksekusi [GetRouteUseCase].
 */
sealed class GetRouteResult {

    /**
     * Rute berhasil diperoleh (dari cache lokal, Directions API, atau fallback Haversine).
     *
     * @property perjalanan Data entitas perjalanan yang siap digunakan untuk kalkulasi biaya BBM.
     * @property isFromCache True jika data diambil dari cache Room tanpa pemanggilan API.
     * @property isFallback True jika data dihitung via rumus Haversine (jarak estimasi).
     */
    data class Success(
        val perjalanan: PerjalananEntity,
        val isFromCache: Boolean,
        val isFallback: Boolean
    ) : GetRouteResult()

    /**
     * Tidak ada rute darat yang menghubungkan titik asal dan tujuan (`ZERO_RESULTS`).
     */
    data class RouteNotFound(
        val message: String
    ) : GetRouteResult()

    /**
     * Terjadi kesalahan fatal yang menghalangi perolehan rute.
     */
    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : GetRouteResult()
}
