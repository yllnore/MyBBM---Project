package com.mybbm.app.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import com.mybbm.app.domain.model.LatLng
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

/**
 * Helper untuk mendeteksi koordinat GPS fisik perangkat menggunakan Android [LocationManager]
 * tanpa dependensi Google Play Services / Google API (100% kompatibel OpenStreetMap / AOSP).
 */
object DeviceLocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): LatLng? {
        if (!hasLocationPermission(context)) {
            return null
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null

        val isGpsEnabled = try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (_: Exception) { false }

        val isNetworkEnabled = try {
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (_: Exception) { false }

        if (!isGpsEnabled && !isNetworkEnabled) {
            return null
        }

        // 1. Periksa lokasi terakhir yang diketahui (last known location)
        val lastGps = if (isGpsEnabled) try { locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (_: Exception) { null } else null
        val lastNet = if (isNetworkEnabled) try { locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) { null } else null

        val bestLast = when {
            lastGps != null && lastNet != null -> if (lastGps.time > lastNet.time) lastGps else lastNet
            lastGps != null -> lastGps
            lastNet != null -> lastNet
            else -> null
        }

        // Jika lokasi terakhir masih baru (< 2 menit), langsung gunakan tanpa menunggu update provider
        if (bestLast != null && (System.currentTimeMillis() - bestLast.time) < 120_000) {
            return LatLng(bestLast.latitude, bestLast.longitude)
        }

        // 2. Minta pembaruan lokasi tunggal dari provider aktif dengan batas timeout 3.5 detik
        val freshLocation = withTimeoutOrNull(3500L) {
            suspendCancellableCoroutine<LatLng?> { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        try { locationManager.removeUpdates(this) } catch (_: Exception) {}
                        if (continuation.isActive) {
                            continuation.resume(LatLng(location.latitude, location.longitude))
                        }
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                }

                val provider = if (isGpsEnabled) LocationManager.GPS_PROVIDER else LocationManager.NETWORK_PROVIDER
                try {
                    locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                    continuation.invokeOnCancellation {
                        try { locationManager.removeUpdates(listener) } catch (_: Exception) {}
                    }
                } catch (e: Exception) {
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
            }
        }

        // Jika berhasil mendapatkan koordinat baru gunakan itu, jika timeout fallback ke bestLast jika tersedia
        return freshLocation ?: bestLast?.let { LatLng(it.latitude, it.longitude) }
    }
}
