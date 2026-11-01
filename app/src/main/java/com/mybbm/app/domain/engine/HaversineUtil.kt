package com.mybbm.app.domain.engine

import com.mybbm.app.domain.model.LatLng
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Utilitas untuk menghitung jarak lingkaran besar (great-circle distance)
 * antara dua koordinat bola bumi menggunakan rumus Haversine.
 *
 * Digunakan sebagai mekanisme fallback luring (offline) ketika OSRM API tidak tersedia.
 */
object HaversineUtil {

    private const val RADIUS_BUMI_KM = 6371.0
    private const val FAKTOR_JALAN_RAYA = 1.25 // Penyesuaian jarak lurus ke jarak jalan raya

    /**
     * Menghitung estimasi jarak jalan raya dalam kilometer antara dua titik koordinat.
     */
    fun hitungJarakKm(
        origin: LatLng,
        destination: LatLng,
        faktorJalan: Double = FAKTOR_JALAN_RAYA
    ): BigDecimal {
        val lat1Rad = Math.toRadians(origin.lat)
        val lon1Rad = Math.toRadians(origin.lng)
        val lat2Rad = Math.toRadians(destination.lat)
        val lon2Rad = Math.toRadians(destination.lng)

        val deltaLat = lat2Rad - lat1Rad
        val deltaLon = lon2Rad - lon1Rad

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLon / 2) * sin(deltaLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        val jarakLurusKm = RADIUS_BUMI_KM * c
        val jarakJalanKm = jarakLurusKm * faktorJalan

        return BigDecimal.valueOf(jarakJalanKm).setScale(2, RoundingMode.HALF_UP)
    }
}

