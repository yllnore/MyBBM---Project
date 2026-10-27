package com.mybbm.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas Room untuk tabel [hasil_perhitungan].
 *
 * Menyimpan snapshot hasil perhitungan biaya BBM. Nilai [literDibutuhkan] dan [totalBiaya]
 * disimpan sebagai nilai akhir pada saat perhitungan (immutable snapshot), sehingga riwayat
 * tidak berubah meski harga BBM diperbarui di kemudian hari.
 *
 * Kolom [literDibutuhkan] dan [totalBiaya] disimpan sebagai [String] untuk menjaga presisi
 * [BigDecimal] sesuai PRD Bagian 5.2.
 *
 * Kolom yang **tidak** disimpan di Room:
 * - `user_id` — diambil dari sesi Supabase saat push.
 * - `local_device_id` — dibaca dari tabel [DeviceInfoEntity] saat push.
 */
@Entity(
    tableName = "hasil_perhitungan",
    foreignKeys = [
        ForeignKey(
            entity = PerjalananEntity::class,
            parentColumns = ["id_perjalanan"],
            childColumns = ["id_perjalanan"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = KendaraanEntity::class,
            parentColumns = ["id_kendaraan"],
            childColumns = ["id_kendaraan"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = JenisBbmEntity::class,
            parentColumns = ["id_bbm"],
            childColumns = ["id_bbm"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = HargaBbmEntity::class,
            parentColumns = ["id_harga"],
            childColumns = ["id_harga"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["id_perjalanan"]),
        Index(value = ["id_kendaraan"]),
        Index(value = ["id_bbm"]),
        Index(value = ["id_harga"]),
        Index(value = ["updated_at"])
    ]
)
data class HasilPerhitunganEntity(
    /** UUID v4 — dibangkitkan di klien. */
    @PrimaryKey
    @ColumnInfo(name = "id_hasil")
    val idHasil: String,

    /** FK → [PerjalananEntity.idPerjalanan]. */
    @ColumnInfo(name = "id_perjalanan")
    val idPerjalanan: String,

    /** FK → [KendaraanEntity.idKendaraan]. */
    @ColumnInfo(name = "id_kendaraan")
    val idKendaraan: String,

    /** FK → [JenisBbmEntity.idBbm]. */
    @ColumnInfo(name = "id_bbm")
    val idBbm: String,

    /** FK → [HargaBbmEntity.idHarga]. Snapshot catatan harga yang dipakai saat perhitungan. */
    @ColumnInfo(name = "id_harga")
    val idHarga: String,

    /**
     * Kebutuhan bahan bakar dalam liter, dibulatkan ke 3 angka desimal (HALF_UP).
     * Disimpan sebagai [String] untuk menjaga presisi [BigDecimal].
     * Contoh: "3.000"
     */
    @ColumnInfo(name = "liter_dibutuhkan")
    val literDibutuhkan: String,

    /**
     * Total biaya dalam IDR, dibulatkan ke 2 angka desimal (HALF_UP).
     * Disimpan sebagai [String] untuk menjaga presisi [BigDecimal].
     * Contoh: "30000.00"
     */
    @ColumnInfo(name = "total_biaya")
    val totalBiaya: String,

    /** Stempel waktu pembuatan, epoch milli-detik (UTC). */
    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    /** Stempel waktu pembaruan terakhir, epoch milli-detik (UTC). Dipakai untuk LWW saat push. */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    /**
     * Stempel waktu penghapusan lunak (soft delete), epoch milli-detik (UTC).
     * Null berarti entri masih aktif dalam riwayat.
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
