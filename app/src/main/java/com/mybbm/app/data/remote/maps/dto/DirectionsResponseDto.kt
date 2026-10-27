package com.mybbm.app.data.remote.maps.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) untuk menangkap payload JSON dari Google Directions API.
 * Endpoint: https://maps.googleapis.com/maps/api/directions/json
 *
 * Mengacu pada PRD Bagian 7.4 dan 7.5:
 * Menggunakan anotasi [@Serializable] dari kotlinx.serialization untuk memetakan
 * rute, jarak (meter), durasi (detik), overview polyline, dan status API.
 */
@Serializable
data class DirectionsResponseDto(
    /**
     * Kode status respons dari Directions API:
     * - "OK" : Berhasil menemukan rute.
     * - "ZERO_RESULTS" : Tidak ada rute antara asal dan tujuan.
     * - "OVER_QUERY_LIMIT" : Kuota terlampaui.
     * - "REQUEST_DENIED" : Permintaan ditolak (API Key tidak sah / tidak aktif).
     * - "INVALID_REQUEST" : Parameter tidak valid.
     * - "UNKNOWN_ERROR" : Galat sementara pada server Google.
     */
    @SerialName("status")
    val status: String,

    /** Daftar alternatif rute yang ditemukan. */
    @SerialName("routes")
    val routes: List<DirectionsRouteDto> = emptyList(),

    /** Pesan kesalahan teknis tambahan saat status != "OK". */
    @SerialName("error_message")
    val errorMessage: String? = null
)

/**
 * Representasi satu rute perjalanan dalam respons Directions API.
 */
@Serializable
data class DirectionsRouteDto(
    /** Ringkasan nama jalan utama rute. */
    @SerialName("summary")
    val summary: String? = null,

    /** Bagian rute (legs). Pada rute tanpa waypoint, selalu berisi tepat 1 leg. */
    @SerialName("legs")
    val legs: List<DirectionsLegDto> = emptyList(),

    /** Garis polyline terenkode untuk merender visual rute di peta. */
    @SerialName("overview_polyline")
    val overviewPolyline: DirectionsPolylineDto? = null
)

/**
 * Representasi satu bagian perjalanan (leg) antara dua titik perhentian.
 */
@Serializable
data class DirectionsLegDto(
    /** Jarak total leg ini. */
    @SerialName("distance")
    val distance: DirectionsValueDto? = null,

    /** Durasi estimasi perjalanan leg ini. */
    @SerialName("duration")
    val duration: DirectionsValueDto? = null,

    /** Alamat asal manusiawi. */
    @SerialName("start_address")
    val startAddress: String? = null,

    /** Alamat tujuan manusiawi. */
    @SerialName("end_address")
    val endAddress: String? = null
)

/**
 * Pembungkus teks polyline terenkode (Encoded Polyline Algorithm Format).
 */
@Serializable
data class DirectionsPolylineDto(
    /** String koordinat polyline terenkode. */
    @SerialName("points")
    val points: String? = null
)

/**
 * Pasangan nilai numerik dan teks representasi manusiawi dari Directions API
 * (misal jarak dalam meter atau durasi dalam detik).
 */
@Serializable
data class DirectionsValueDto(
    /** Nilai numerik mutlak: meter untuk jarak, detik untuk durasi. */
    @SerialName("value")
    val value: Long = 0L,

    /** Representasi terbaca (contoh: "120 km", "2 jam 15 mnt"). */
    @SerialName("text")
    val text: String = ""
)
