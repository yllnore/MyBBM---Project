package com.mybbm.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entitas Room untuk tabel [perjalanan].
 *
 * Menyimpan data perjalanan yang diperhitungkan biayanya, termasuk koordinat asal/tujuan,
 * jarak, dan rute yang telah di-cache. Data ini dicadangkan ke Supabase.
 *
 * Kolom [jarakKm] disimpan sebagai [String] untuk menjaga presisi [BigDecimal].
 *
 * Nilai [sumberJarak] yang valid: "DIRECTIONS_API" | "HAVERSINE" | "MANUAL".
 *
 * Kolom yang **tidak** disimpan di Room:
 * - `user_id` — diambil dari sesi Supabase saat push.
 * - `local_device_id` — dibaca dari tabel [DeviceInfoEntity] saat push.
 */
@Entity(
    tableName = "perjalanan",
    indices = [Index(value = ["updated_at"])]
)
data class PerjalananEntity(
    /** UUID v4 — dibangkitkan di klien. */
    @PrimaryKey
    @ColumnInfo(name = "id_perjalanan")
    val idPerjalanan: String,

    /** Label opsional yang diberikan pengguna untuk rute ini. */
    val label: String?,

    /** Nama tempat asal dari Places API. Null jika koordinat diinput manual. */
    @ColumnInfo(name = "asal_nama")
    val asalNama: String?,

    /** Nama tempat tujuan dari Places API. Null jika koordinat diinput manual. */
    @ColumnInfo(name = "tujuan_nama")
    val tujuanNama: String?,

    /** Google Place ID asal. Null jika koordinat diinput manual. */
    @ColumnInfo(name = "asal_place_id")
    val asalPlaceId: String?,

    /** Google Place ID tujuan. Null jika koordinat diinput manual. */
    @ColumnInfo(name = "tujuan_place_id")
    val tujuanPlaceId: String?,

    /** Koordinat lintang asal. Rentang: -90.0 s.d. 90.0. */
    @ColumnInfo(name = "asal_lat")
    val asalLat: Double,

    /** Koordinat bujur asal. Rentang: -180.0 s.d. 180.0. */
    @ColumnInfo(name = "asal_lng")
    val asalLng: Double,

    /** Koordinat lintang tujuan. Rentang: -90.0 s.d. 90.0. */
    @ColumnInfo(name = "tujuan_lat")
    val tujuanLat: Double,

    /** Koordinat bujur tujuan. Rentang: -180.0 s.d. 180.0. */
    @ColumnInfo(name = "tujuan_lng")
    val tujuanLng: Double,

    /**
     * Jarak rute dalam kilometer.
     * Disimpan sebagai [String] agar [BigDecimal] tidak kehilangan presisi.
     * Contoh: "120.50"
     */
    @ColumnInfo(name = "jarak_km")
    val jarakKm: String,

    /** Estimasi waktu tempuh dalam menit. Null jika sumber jarak bukan DIRECTIONS_API. */
    @ColumnInfo(name = "durasi_menit")
    val durasiMenit: Int?,

    /**
     * Encoded polyline rute dari Directions API.
     * Digunakan untuk menampilkan rute di peta saat offline.
     * Null jika sumber jarak bukan DIRECTIONS_API.
     */
    @ColumnInfo(name = "encoded_polyline")
    val encodedPolyline: String?,

    /**
     * Sumber data jarak rute.
     * Nilai yang valid: "DIRECTIONS_API" | "HAVERSINE" | "MANUAL"
     */
    @ColumnInfo(name = "sumber_jarak")
    val sumberJarak: String,

    /** Stempel waktu pembuatan, epoch milli-detik (UTC). */
    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    /** Stempel waktu pembaruan terakhir, epoch milli-detik (UTC). Dipakai untuk LWW saat push. */
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long,

    /**
     * Stempel waktu penghapusan lunak (soft delete), epoch milli-detik (UTC).
     * Null berarti perjalanan masih aktif.
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
