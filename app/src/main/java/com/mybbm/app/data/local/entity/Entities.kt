package com.mybbm.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entitas tabel lokal `kendaraan_kustom` untuk menyimpan kendaraan kustom buatan pengguna secara offline.
 * Data bawaan pabrik tidak disimpan di sini, melainkan dibaca dari Supabase (Hybrid).
 */
@Entity(tableName = "kendaraan_kustom")
data class KendaraanKustomEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val nama: String,
    val tipe: String, // "Mobil" atau "Motor"
    @ColumnInfo(name = "rasio_konsumsi")
    val rasioKonsumsi: Double, // km/L
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Entitas tabel lokal `bbm_kustom` untuk menyimpan jenis BBM kustom buatan pengguna secara offline.
 * Data bawaan pabrik tidak disimpan di sini, melainkan dibaca dari Supabase (Hybrid).
 */
@Entity(tableName = "bbm_kustom")
data class BbmKustomEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val nama: String,
    val harga: Double, // Harga per liter dalam Rupiah
    @ColumnInfo(name = "icon_res_id")
    val iconResId: Int? = null,
    @ColumnInfo(name = "warna_hex")
    val warnaHex: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Entitas tabel lokal `riwayat` untuk menyimpan riwayat perjalanan dan perhitungan BBM secara offline.
 */
@Entity(tableName = "riwayat")
data class RiwayatEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "lokasi_asal")
    val lokasiAsal: String,
    @ColumnInfo(name = "lokasi_tujuan")
    val lokasiTujuan: String,
    @ColumnInfo(name = "jarak_km")
    val jarakKm: Double,
    @ColumnInfo(name = "total_biaya")
    val totalBiaya: Double,
    val timestamp: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "total_liter")
    val totalLiter: Double = 0.0,
    @ColumnInfo(name = "nama_kendaraan")
    val namaKendaraan: String = "",
    @ColumnInfo(name = "nama_bbm")
    val namaBbm: String = "",
    @ColumnInfo(name = "origin_lat")
    val originLat: Double = -0.502106,
    @ColumnInfo(name = "origin_lng")
    val originLng: Double = 117.153709,
    @ColumnInfo(name = "dest_lat")
    val destLat: Double = -1.265386,
    @ColumnInfo(name = "dest_lng")
    val destLng: Double = 116.831200
)

// Aliases untuk backward compatibility
typealias KendaraanEntity = KendaraanKustomEntity
typealias BbmEntity = BbmKustomEntity
typealias VehicleEntity = KendaraanKustomEntity
typealias FuelEntity = BbmKustomEntity
typealias HistoryEntity = RiwayatEntity
