package com.mybbm.app.domain.model

/**
 * Model data representasi hasil pencarian geocoding dari Nominatim OpenStreetMap API.
 *
 * @property displayName Alamat lengkap yang dikembalikan oleh Nominatim.
 * @property lat Garis lintang (latitude).
 * @property lon Garis bujur (longitude).
 * @property name Nama tempat spesifik (jika ada).
 */
data class LocationSearchResult(
    val displayName: String,
    val lat: Double,
    val lon: Double,
    val name: String = ""
) {
    /**
     * Nama tempat atau judul utama (misalnya nama jalan, instansi, atau bagian sebelum koma pertama).
     */
    val title: String
        get() = when {
            name.isNotBlank() -> name
            displayName.contains(",") -> displayName.substringBefore(",").trim()
            else -> displayName
        }

    /**
     * Deskripsi alamat lanjutan atau wilayah (kota, provinsi) sebagai sub-judul.
     */
    val subtitle: String
        get() = when {
            name.isNotBlank() && displayName.startsWith(name, ignoreCase = true) -> {
                displayName.removePrefix(name).trimStart(',', ' ')
            }
            displayName.contains(",") -> displayName.substringAfter(",").trim()
            else -> ""
        }

    /**
     * Konversi langsung ke koordinat [LatLng] untuk peta atau rute OSRM.
     */
    val coordinate: LatLng
        get() = LatLng(lat, lon)
}
