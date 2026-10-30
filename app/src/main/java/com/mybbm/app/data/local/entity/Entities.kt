package com.mybbm.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "kendaraan")
data class VehicleEntity(
    @PrimaryKey
    val id: String,
    val nama: String,
    val jenis: String, // "Mobil" atau "Motor"
    @ColumnInfo(name = "konsumsi_bbm")
    val konsumsiBbm: BigDecimal,
    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "jenis_bbm")
data class FuelEntity(
    @PrimaryKey
    val id: String,
    val nama: String,
    val harga: BigDecimal,
    @ColumnInfo(name = "warna_hex")
    val warnaHex: String? = null,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "perjalanan")
data class HistoryEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "lokasi_asal")
    val lokasiAsal: String,
    @ColumnInfo(name = "lokasi_tujuan")
    val lokasiTujuan: String,
    @ColumnInfo(name = "jarak_km")
    val jarakKm: BigDecimal,
    @ColumnInfo(name = "nama_kendaraan")
    val namaKendaraan: String,
    @ColumnInfo(name = "nama_bbm")
    val namaBbm: String,
    @ColumnInfo(name = "total_liter")
    val totalLiter: BigDecimal,
    @ColumnInfo(name = "total_biaya")
    val totalBiaya: BigDecimal,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
