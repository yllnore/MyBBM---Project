package com.mybbm.app.domain.engine

import com.mybbm.app.domain.model.LatLng
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Utilitas perhitungan jarak garis lurus antar dua koordinat menggunakan formula Haversine.
 *
 * Digunakan sebagai **fallback** ketika Directions API tidak dapat diakses (perangkat
 * luring atau API gagal). Hasilnya diberi penanda `sumber_jarak = "HAVERSINE"` dan
 * UI menampilkan label **"Estimasi"** agar pengguna mengetahui bahwa ini bukan jarak
 * rute sebenarnya.
 *
 * ## Faktor Koreksi Jalan ([ROAD_FACTOR])
 * Jarak Haversine adalah jarak lurus (garis terpendek di permukaan bumi). Untuk
 * mendekati jarak aktual di jalan, hasil dikalikan dengan faktor 1,3 yang dapat
 * dikonfigurasi. Nilai ini merupakan estimasi rata-rata untuk jalan di Kalimantan
 * dan dapat disesuaikan di kemudian hari tanpa mengubah skema database.
 *
 * ## Keterbatasan
 * - Tidak mempertimbangkan kelengkungan jalan, penghalang alam, atau lalu lintas.
 * - Galat estimasi bisa signifikan untuk rute dengan banyak tikungan atau jalan
 *   yang tidak langsung (contoh: rute penyeberangan ferry).
 * - Selalu gunakan Directions API jika jaringan tersedia.
 *
 * Sepenuhnya **murni Kotlin** — tidak bergantung pada Android SDK.
 */
object HaversineUtil {

    /** Jari-jari bumi rata-rata dalam kilometer (model bola). */
    private const val EARTH_RADIUS_KM = 6371.0

    /**
     * Faktor pengali untuk mengkonversi jarak Haversine ke estimasi jarak jalan.
     *
     * Nilai default 1,3 sesuai spesifikasi PRD Bagian 7.8.
     * Dapat diganti saat memanggil [hitungJarakKm] melalui parameter [roadFactor].
     */
    const val ROAD_FACTOR = 1.3

    /**
     * Menghitung jarak garis lurus antara dua titik koordinat menggunakan formula Haversine,
     * lalu mengalikannya dengan [roadFactor] sebagai estimasi jarak jalan.
     *
     * Hasil dikembalikan sebagai [BigDecimal] berskala 2 dengan pembulatan HALF_UP agar
     * konsisten dengan kolom `jarak_km` di [com.mybbm.app.data.local.entity.PerjalananEntity].
     *
     * @param asal       Koordinat titik asal.
     * @param tujuan     Koordinat titik tujuan.
     * @param roadFactor Faktor koreksi jalan. Default: [ROAD_FACTOR] (1.3).
     * @return Estimasi jarak jalan dalam kilometer, skala 2, dibulatkan HALF_UP.
     *
     * ## Contoh
     * ```kotlin
     * val samarinda = LatLng(-0.5022, 117.1536)
     * val balikpapan = LatLng(-1.2675, 116.8289)
     * val jarakEstimasi = HaversineUtil.hitungJarakKm(samarinda, balikpapan)
     * // → sekitar BigDecimal("146.83") (estimasi; nilai aktual bervariasi)
     * ```
     */
    fun hitungJarakKm(
        asal: LatLng,
        tujuan: LatLng,
        roadFactor: Double = ROAD_FACTOR
    ): BigDecimal {
        val jarakLurus = haversineKm(asal, tujuan)
        val estimasiJalan = jarakLurus * roadFactor
        return BigDecimal(estimasiJalan).setScale(2, RoundingMode.HALF_UP)
    }

    /**
     * Menghitung jarak Haversine murni (garis lurus di permukaan bola bumi) tanpa
     * faktor koreksi jalan. Hasilnya dalam kilometer sebagai [Double].
     *
     * Gunakan [hitungJarakKm] untuk mendapatkan [BigDecimal] dengan faktor koreksi
     * yang siap disimpan ke database.
     *
     * Formula:
     * ```
     * a = sin²(Δlat/2) + cos(lat1) × cos(lat2) × sin²(Δlng/2)
     * c = 2 × asin(√a)
     * d = R × c
     * ```
     *
     * @param a Titik pertama.
     * @param b Titik kedua.
     * @return Jarak garis lurus dalam kilometer.
     */
    fun haversineKm(a: LatLng, b: LatLng): Double {
        val dLat = Math.toRadians(b.lat - a.lat)
        val dLng = Math.toRadians(b.lng - a.lng)

        val h = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(a.lat)) *
                cos(Math.toRadians(b.lat)) *
                sin(dLng / 2).pow(2)

        return 2 * EARTH_RADIUS_KM * asin(sqrt(h))
    }
}
