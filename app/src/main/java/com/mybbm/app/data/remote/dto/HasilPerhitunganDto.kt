package com.mybbm.app.data.remote.dto

import com.mybbm.app.data.local.entity.HasilPerhitunganEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) untuk tabel [hasil_perhitungan] di Supabase PostgreSQL.
 *
 * Digunakan untuk operasi push (upsert) hasil kalkulasi biaya BBM milik pengguna ke cloud.
 * Sesuai PRD Bagian 6.2 dan 8.3:
 * - Kolom [localDeviceId] diisi dari tabel device_info di Room saat proses push.
 * - Kolom user_id TIDAK ada di sini; diisi oleh server via DEFAULT auth.uid() dan RLS.
 * - Push dilakukan SETELAH KENDARAAN dan PERJALANAN untuk memenuhi constraint FK PostgreSQL.
 *
 * Nilai [literDibutuhkan] dan [totalBiaya] dikirim sebagai String untuk menjaga
 * presisi NUMERIC PostgreSQL sesuai PRD Bagian 5.2.
 */
@Serializable
data class HasilPerhitunganDto(

    @SerialName("id_hasil")
    val idHasil: String,

    /**
     * UUID perangkat asal data.
     * Diisi oleh PushDataUseCase dari tabel device_info.
     */
    @SerialName("local_device_id")
    val localDeviceId: String,

    @SerialName("id_perjalanan")
    val idPerjalanan: String,

    @SerialName("id_kendaraan")
    val idKendaraan: String,

    @SerialName("id_bbm")
    val idBbm: String,

    /** FK ke tabel harga_bbm. Snapshot harga saat perhitungan. */
    @SerialName("id_harga")
    val idHarga: String,

    /**
     * Kebutuhan bahan bakar dalam liter.
     * Dikirim sebagai String untuk menjaga presisi NUMERIC(8,3) PostgreSQL.
     */
    @Serializable(with = NumericAsStringSerializer::class)
    @SerialName("liter_dibutuhkan")
    val literDibutuhkan: String,

    /**
     * Total biaya perjalanan dalam IDR.
     * Dikirim sebagai String untuk menjaga presisi NUMERIC(14,2) PostgreSQL.
     */
    @Serializable(with = NumericAsStringSerializer::class)
    @SerialName("total_biaya")
    val totalBiaya: String,

    @SerialName("created_at")
    val createdAt: String,

    @SerialName("updated_at")
    val updatedAt: String,

    @SerialName("deleted_at")
    val deletedAt: String? = null
) {
    companion object {
        /**
         * Mengonversi [HasilPerhitunganEntity] dari Room menjadi [HasilPerhitunganDto] untuk push.
         * @param entity Entitas Room yang akan dikirim.
         * @param localDeviceId UUID perangkat dari tabel device_info.
         */
        fun fromEntity(entity: HasilPerhitunganEntity, localDeviceId: String): HasilPerhitunganDto {
            return HasilPerhitunganDto(
                idHasil = entity.idHasil,
                localDeviceId = localDeviceId,
                idPerjalanan = entity.idPerjalanan,
                idKendaraan = entity.idKendaraan,
                idBbm = entity.idBbm,
                idHarga = entity.idHarga,
                literDibutuhkan = entity.literDibutuhkan,
                totalBiaya = entity.totalBiaya,
                createdAt = DateUtils.epochMillisToIso(entity.createdAt),
                updatedAt = DateUtils.epochMillisToIso(entity.updatedAt),
                deletedAt = entity.deletedAt?.let { DateUtils.epochMillisToIso(it) }
            )
        }
    }
}
