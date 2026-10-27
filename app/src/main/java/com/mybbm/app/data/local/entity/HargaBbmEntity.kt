package com.mybbm.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas Room untuk tabel [harga_bbm].
 *
 * Menyimpan riwayat harga BBM per wilayah. Hanya ada satu catatan harga aktif per
 * kombinasi (id_bbm, wilayah) yang ditandai dengan [berlakuHingga] = null.
 *
 * Kolom [hargaPerLiter] disimpan sebagai [String] untuk menjaga presisi desimal
 * menggunakan [BigDecimal] melalui [com.mybbm.app.data.local.converter.BigDecimalConverter].
 *
 * Wilayah saat ini hanya: "KALIMANTAN_TIMUR".
 */
@Entity(
    tableName = "harga_bbm",
    foreignKeys = [
        ForeignKey(
            entity = JenisBbmEntity::class,
            parentColumns = ["id_bbm"],
            childColumns = ["id_bbm"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_bbm", "wilayah", "berlaku_mulai"], unique = true),
        Index(value = ["id_bbm"]),
        Index(value = ["updated_at"])
    ]
)
data class HargaBbmEntity(
    /** UUID v4 — pengenal unik catatan harga, identik dengan id_harga di Supabase. */
    @PrimaryKey
    @ColumnInfo(name = "id_harga")
    val idHarga: String,

    /** FK → [JenisBbmEntity.idBbm]. */
    @ColumnInfo(name = "id_bbm")
    val idBbm: String,

    /**
     * Kode wilayah. Saat ini selalu "KALIMANTAN_TIMUR".
     */
    val wilayah: String,

    /**
     * Harga BBM dalam IDR per liter.
     * Disimpan sebagai [String] agar [BigDecimal] tidak kehilangan presisi.
     * Contoh: "10000.00"
     */
    @ColumnInfo(name = "harga_per_liter")
    val hargaPerLiter: String,

    /**
     * Tanggal mulai berlaku, disimpan sebagai epoch milli-detik (UTC) dari
     * representasi tanggal (DATE) di Supabase.
     */
    @ColumnInfo(name = "berlaku_mulai")
    val berlakuMulai: Long,

    /**
     * Tanggal berakhir berlaku (epoch milli-detik).
     * Null berarti harga ini adalah harga yang sedang berlaku.
     */
    @ColumnInfo(name = "berlaku_hingga")
    val berlakuHingga: Long?,

    /**
     * Stempel waktu terakhir diperbarui di Supabase, dalam epoch milli-detik (UTC).
     * Digunakan sebagai kursor pada delta-sync.
     */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
