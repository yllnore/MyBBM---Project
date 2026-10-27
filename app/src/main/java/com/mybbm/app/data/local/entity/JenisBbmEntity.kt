package com.mybbm.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas Room untuk tabel [jenis_bbm].
 *
 * Merupakan data referensi yang hanya dapat dibaca oleh klien; penulisan dilakukan
 * oleh pengembang melalui Supabase Studio. Data ini disinkronkan dari Supabase
 * menggunakan mekanisme delta-sync berdasarkan kolom [updatedAt].
 *
 * Nilai [jenisBahanBakar] yang valid: "BENSIN" atau "DIESEL".
 */
@Entity(tableName = "jenis_bbm")
data class JenisBbmEntity(
    /** UUID v4 — pengenal unik BBM, identik dengan id_bbm di Supabase. */
    @PrimaryKey
    @ColumnInfo(name = "id_bbm")
    val idBbm: String,

    /** Nama resmi BBM, contoh: "Pertalite", "Pertamax", "Biosolar". */
    @ColumnInfo(name = "nama_bbm")
    val namaBbm: String,

    /**
     * Kategori bahan bakar.
     * Nilai yang valid: "BENSIN" | "DIESEL"
     */
    @ColumnInfo(name = "jenis_bahan_bakar")
    val jenisBahanBakar: String,

    /** Apakah BBM ini masih aktif ditawarkan. Default: true. */
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    /**
     * Stempel waktu terakhir diperbarui di Supabase, dalam epoch milli-detik (UTC).
     * Digunakan sebagai kursor pada delta-sync.
     */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)
