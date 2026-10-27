package com.mybbm.app.data.remote.dto

import com.mybbm.app.data.local.entity.HargaBbmEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data Transfer Object (DTO) untuk tabel [harga_bbm] di Supabase PostgreSQL.
 *
 * Menggunakan anotasi [@Serializable] dari kotlinx.serialization untuk memetakan
 * payload respons JSON dari PostgREST API.
 *
 * Kolom [hargaPerLiter] didekodekan menggunakan [NumericAsStringSerializer]
 * untuk menghindari kehilangan presisi desimal akibat konversi floating point.
 */
@Serializable
data class HargaBbmDto(
    /** UUID v4 unik catatan harga, pemetaan dari kolom id_harga. */
    @SerialName("id_harga")
    val idHarga: String,

    /** Foreign Key ke tabel jenis_bbm (id_bbm). */
    @SerialName("id_bbm")
    val idBbm: String,

    /** Wilayah berlakunya harga. Nilai baku: "KALIMANTAN_TIMUR". */
    @SerialName("wilayah")
    val wilayah: String = "KALIMANTAN_TIMUR",

    /**
     * Harga BBM per liter dalam IDR.
     * Didekode sebagai String langsung dari JSON number / string untuk menjaga presisi BigDecimal.
     */
    @Serializable(with = NumericAsStringSerializer::class)
    @SerialName("harga_per_liter")
    val hargaPerLiter: String,

    /** Tanggal mulai berlakunya harga (format SQL DATE: "YYYY-MM-DD"). */
    @SerialName("berlaku_mulai")
    val berlakuMulai: String,

    /** Tanggal akhir berlakunya harga (format SQL DATE: "YYYY-MM-DD"), null jika masih berlaku aktif. */
    @SerialName("berlaku_hingga")
    val berlakuHingga: String? = null,

    /** Stempel waktu pembaruan terakhir di Supabase (format ISO-8601 timestamptz). */
    @SerialName("updated_at")
    val updatedAt: String
) {
    /**
     * Mengonversi DTO ke entitas Room [HargaBbmEntity].
     */
    fun toEntity(): HargaBbmEntity {
        return HargaBbmEntity(
            idHarga = idHarga,
            idBbm = idBbm,
            wilayah = wilayah,
            hargaPerLiter = hargaPerLiter,
            berlakuMulai = DateUtils.parseDateToEpochMillis(berlakuMulai),
            berlakuHingga = berlakuHingga?.let { DateUtils.parseDateToEpochMillis(it) },
            updatedAt = DateUtils.parseIsoToEpochMillis(updatedAt)
        )
    }

    companion object {
        /**
         * Mengonversi entitas Room [HargaBbmEntity] menjadi [HargaBbmDto].
         */
        fun fromEntity(entity: HargaBbmEntity): HargaBbmDto {
            return HargaBbmDto(
                idHarga = entity.idHarga,
                idBbm = entity.idBbm,
                wilayah = entity.wilayah,
                hargaPerLiter = entity.hargaPerLiter,
                berlakuMulai = DateUtils.epochMillisToDateString(entity.berlakuMulai),
                berlakuHingga = entity.berlakuHingga?.let { DateUtils.epochMillisToDateString(it) },
                updatedAt = DateUtils.epochMillisToIso(entity.updatedAt)
            )
        }
    }
}
