package com.mybbm.app.data.remote.dto

import com.mybbm.app.data.local.entity.JenisBbmEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) untuk tabel [jenis_bbm] di Supabase PostgreSQL.
 *
 * Menggunakan anotasi [@Serializable] dari kotlinx.serialization untuk memetakan
 * payload respons JSON dari PostgREST API.
 *
 * Kolom audit [updated_at] berformat ISO-8601 timestamptz dari PostgreSQL,
 * yang dikonversi menjadi epoch milli-detik saat disimpan ke Room via [toEntity].
 */
@Serializable
data class JenisBbmDto(
    /** UUID v4 unik untuk BBM, pemetaan dari kolom id_bbm. */
    @SerialName("id_bbm")
    val idBbm: String,

    /** Nama resmi BBM, contoh: "Pertalite", "Pertamax", "Biosolar". */
    @SerialName("nama_bbm")
    val namaBbm: String,

    /** Kategori bahan bakar: "BENSIN" atau "DIESEL". */
    @SerialName("jenis_bahan_bakar")
    val jenisBahanBakar: String,

    /** Status aktif BBM. Default: true. */
    @SerialName("is_active")
    val isActive: Boolean = true,

    /** Stempel waktu pembaruan terakhir di Supabase (format ISO-8601 timestamptz). */
    @SerialName("updated_at")
    val updatedAt: String
) {
    /**
     * Mengonversi DTO ke entitas Room [JenisBbmEntity].
     * Kolom [updated_at] ISO-8601 diubah menjadi epoch milli-detik.
     */
    fun toEntity(): JenisBbmEntity {
        return JenisBbmEntity(
            idBbm = idBbm,
            namaBbm = namaBbm,
            jenisBahanBakar = jenisBahanBakar,
            isActive = isActive,
            updatedAt = DateUtils.parseIsoToEpochMillis(updatedAt)
        )
    }

    companion object {
        /**
         * Mengonversi entitas Room [JenisBbmEntity] menjadi [JenisBbmDto].
         */
        fun fromEntity(entity: JenisBbmEntity): JenisBbmDto {
            return JenisBbmDto(
                idBbm = entity.idBbm,
                namaBbm = entity.namaBbm,
                jenisBahanBakar = entity.jenisBahanBakar,
                isActive = entity.isActive,
                updatedAt = DateUtils.epochMillisToIso(entity.updatedAt)
            )
        }
    }
}
