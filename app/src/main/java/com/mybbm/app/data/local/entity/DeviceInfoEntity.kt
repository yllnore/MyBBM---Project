package com.mybbm.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Menyimpan metadata perangkat sebagai pasangan kunci-nilai.
 *
 * Hanya ada satu baris dengan key = "local_device_id" yang berisi UUID perangkat
 * yang dibangkitkan saat aplikasi pertama kali dipasang. Nilai ini tidak berubah
 * selama aplikasi terpasang dan digunakan sebagai pengenal perangkat saat sinkronisasi.
 *
 * Contoh entri:
 *   key = "local_device_id"
 *   value = "550e8400-e29b-41d4-a716-446655440000"
 */
@Entity(tableName = "device_info")
data class DeviceInfoEntity(
    @PrimaryKey val key: String,
    val value: String
)
