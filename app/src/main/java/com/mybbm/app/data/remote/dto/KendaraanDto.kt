package com.mybbm.app.data.remote.dto

import com.mybbm.app.data.local.entity.KendaraanEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) untuk tabel [kendaraan] di Supabase PostgreSQL.
 *
 * Digunakan untuk operasi push (upsert) data kendaraan milik pengguna ke cloud.
 * Sesuai PRD Bagian 6.2 dan 8.3:
 * - Kolom [localDeviceId] diisi dari tabel device_info di Room saat proses push.
 * - Kolom user_id TIDAK ada di sini; diisi oleh server via DEFAULT auth.uid() dan RLS.
 * - Kolom syncState TIDAK dikirim ke Supabase; hanya penanda lokal Room.
 *
 * Nilai [jenisKendaraan] yang valid: MOTOR, MOBIL, TRUK, LAINNYA.
 */
@Serializable
data class KendaraanDto(

    @SerialName("id_kendaraan")
    val idKendaraan: String,

    /**
     * UUID perangkat asal data.
     * Diisi oleh PushDataUseCase dari tabel device_info (key = 'local_device_id').
     */
    @SerialName("local_device_id")
    val localDeviceId: String,

    @SerialName("nama_kendaraan")
    val namaKendaraan: String,

    @SerialName("jenis_kendaraan")
    val jenisKendaraan: String,

    /**
     * Rasio konsumsi bahan bakar dalam km/liter.
     * Dikirim sebagai String untuk menjaga presisi NUMERIC PostgreSQL.
     */
    @Serializable(with = NumericAsStringSerializer::class)
    @SerialName("konsumsi_km_per_liter")
    val konsumsiKmPerLiter: String,

    @SerialName("is_default")
    val isDefault: Boolean,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String,

    @SerialName("deleted_at")
    val deletedAt: String? = null
) {
    companion object {
        /**
         * Mengonversi [KendaraanEntity] dari Room menjadi [KendaraanDto] untuk push.
         * @param entity Entitas Room yang akan dikirim.
         * @param localDeviceId UUID perangkat dari tabel device_info.
         */
        fun fromEntity(entity: KendaraanEntity, localDeviceId: String): KendaraanDto {
            return KendaraanDto(
                idKendaraan = entity.idKendaraan,
                localDeviceId = localDeviceId,
                namaKendaraan = entity.namaKendaraan,
                jenisKendaraan = entity.jenisKendaraan,
                konsumsiKmPerLiter = entity.konsumsiKmPerLiter,
                isDefault = entity.isDefault,
                createdAt = DateUtils.epochMillisToIso(entity.createdAt),
                updatedAt = DateUtils.epochMillisToIso(entity.updatedAt),
                deletedAt = entity.deletedAt?.let { DateUtils.epochMillisToIso(it) }
            )
        }
    }
}
