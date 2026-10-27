package com.mybbm.app.data.remote.dto

import com.mybbm.app.data.local.entity.PerjalananEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) untuk tabel [perjalanan] di Supabase PostgreSQL.
 *
 * Digunakan untuk operasi push (upsert) data perjalanan milik pengguna ke cloud.
 * Sesuai PRD Bagian 6.2 dan 8.3:
 * - Kolom [localDeviceId] diisi dari tabel device_info di Room saat proses push.
 * - Kolom user_id TIDAK ada di sini; diisi oleh server via DEFAULT auth.uid() dan RLS.
 *
 * Nilai [sumberJarak] yang valid: DIRECTIONS_API, HAVERSINE, MANUAL.
 * Push harus dilakukan SETELAH KENDARAAN untuk memenuhi constraint FK PostgreSQL.
 */
@Serializable
data class PerjalananDto(

    @SerialName("id_perjalanan")
    val idPerjalanan: String,

    /**
     * UUID perangkat asal data.
     * Diisi oleh PushDataUseCase dari tabel device_info.
     */
    @SerialName("local_device_id")
    val localDeviceId: String,

    @SerialName("label")
    val label: String? = null,

    @SerialName("asal_nama")
    val asalNama: String? = null,

    @SerialName("tujuan_nama")
    val tujuanNama: String? = null,

    @SerialName("asal_place_id")
    val asalPlaceId: String? = null,

    @SerialName("tujuan_place_id")
    val tujuanPlaceId: String? = null,

    @SerialName("asal_lat")
    val asalLat: Double,

    @SerialName("asal_lng")
    val asalLng: Double,

    @SerialName("tujuan_lat")
    val tujuanLat: Double,

    @SerialName("tujuan_lng")
    val tujuanLng: Double,

    /**
     * Jarak rute dalam kilometer.
     * Dikirim sebagai String untuk menjaga presisi NUMERIC PostgreSQL.
     */
    @Serializable(with = NumericAsStringSerializer::class)
    @SerialName("jarak_km")
    val jarakKm: String,

    @SerialName("durasi_menit")
    val durasiMenit: Int? = null,

    /** Sumber data jarak: DIRECTIONS_API, HAVERSINE, atau MANUAL. */
    @SerialName("sumber_jarak")
    val sumberJarak: String,

    @SerialName("encoded_polyline")
    val encodedPolyline: String? = null,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String,

    @SerialName("deleted_at")
    val deletedAt: String? = null
) {
    companion object {
        /**
         * Mengonversi [PerjalananEntity] dari Room menjadi [PerjalananDto] untuk push.
         * @param entity Entitas Room yang akan dikirim.
         * @param localDeviceId UUID perangkat dari tabel device_info.
         */
        fun fromEntity(entity: PerjalananEntity, localDeviceId: String): PerjalananDto {
            return PerjalananDto(
                idPerjalanan = entity.idPerjalanan,
                localDeviceId = localDeviceId,
                label = entity.label,
                asalNama = entity.asalNama,
                tujuanNama = entity.tujuanNama,
                asalPlaceId = entity.asalPlaceId,
                tujuanPlaceId = entity.tujuanPlaceId,
                asalLat = entity.asalLat,
                asalLng = entity.asalLng,
                tujuanLat = entity.tujuanLat,
                tujuanLng = entity.tujuanLng,
                jarakKm = entity.jarakKm,
                durasiMenit = entity.durasiMenit,
                sumberJarak = entity.sumberJarak,
                encodedPolyline = entity.encodedPolyline,
                createdAt = DateUtils.epochMillisToIso(entity.createdAt),
                updatedAt = DateUtils.epochMillisToIso(entity.updatedAt),
                deletedAt = entity.deletedAt?.let { DateUtils.epochMillisToIso(it) }
            )
        }
    }
}
