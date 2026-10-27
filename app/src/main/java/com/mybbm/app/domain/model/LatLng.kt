package com.mybbm.app.domain.model

/**
 * Representasi koordinat geografis di lapisan domain.
 *
 * Sengaja didefinisikan di sini (bukan menggunakan [com.google.android.gms.maps.model.LatLng]
 * dari Google Maps SDK) agar Core Engine dan semua use case tetap murni Kotlin dan tidak
 * bergantung pada SDK Android maupun Google Maps. Hal ini memudahkan pengujian unit tanpa
 * emulator atau perangkat fisik.
 *
 * @property lat Lintang (latitude). Rentang valid: -90.0 hingga 90.0.
 * @property lng Bujur (longitude). Rentang valid: -180.0 hingga 180.0.
 */
data class LatLng(
    val lat: Double,
    val lng: Double
)
