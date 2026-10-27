package com.mybbm.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas Room untuk tabel [kendaraan].
 *
 * Data milik pengguna yang disimpan lokal dan dicadangkan ke Supabase.
 * Kolom [konsumsiKmPerLiter] disimpan sebagai [String] untuk menjaga presisi
 * menggunakan [BigDecimal] melalui [com.mybbm.app.data.local.converter.BigDecimalConverter].
 *
 * **Aturan [isDefault]:** hanya satu kendaraan aktif ([deletedAt] = null) per perangkat
 * yang boleh memiliki [isDefault] = true. Aturan ini ditegakkan oleh [com.mybbm.app.data.local.dao.KendaraanDao.setDefault]
 * dalam transaksi atomik.
 *
 * Nilai [jenisKendaraan] yang valid: "MOTOR" | "MOBIL" | "TRUK" | "LAINNYA".
 *
 * Kolom yang **tidak** disimpan di Room:
 * - `user_id` — diambil dari sesi Supabase saat push dan dijamin oleh RLS.
 * - `local_device_id` — dibaca dari tabel [DeviceInfoEntity] dan ditambahkan mapper DTO saat push.
 */
@Entity(
    tableName = "kendaraan",
    indices = [Index(value = ["updated_at"])]
)
data class KendaraanEntity(
    /** UUID v4 — dibangkitkan di klien. */
    @PrimaryKey
    @ColumnInfo(name = "id_kendaraan")
    val idKendaraan: String,

    /** Nama deskriptif kendaraan. Maks 60 karakter. Contoh: "Honda Beat 2021". */
    @ColumnInfo(name = "nama_kendaraan")
    val namaKendaraan: String,

    /**
     * Kategori kendaraan.
     * Nilai yang valid: "MOTOR" | "MOBIL" | "TRUK" | "LAINNYA"
     */
    @ColumnInfo(name = "jenis_kendaraan")
    val jenisKendaraan: String,

    /**
     * Rasio konsumsi bahan bakar dalam km per liter.
     * Disimpan sebagai [String] agar [BigDecimal] tidak kehilangan presisi.
     * Contoh: "40.00"
     */
    @ColumnInfo(name = "konsumsi_km_per_liter")
    val konsumsiKmPerLiter: String,

    /**
     * Kendaraan utama yang dipilih otomatis pada kalkulator.
     * Hanya satu kendaraan aktif per perangkat yang boleh bernilai true.
     * Default: false.
     */
    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = false,

    /** Stempel waktu pembuatan, epoch milli-detik (UTC). */
    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    /** Stempel waktu pembaruan terakhir, epoch milli-detik (UTC). Dipakai untuk LWW saat push. */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    /**
     * Stempel waktu penghapusan lunak (soft delete), epoch milli-detik (UTC).
     * Null berarti kendaraan masih aktif.
     */
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,

    /**
     * Status sinkronisasi. Gunakan konstanta dari [SyncState].
     * Kolom ini hanya ada di Room; tidak dikirim ke Supabase.
     */
    @ColumnInfo(name = "sync_state")
    val syncState: Int = SyncState.PENDING
)
