package com.mybbm.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mybbm.app.data.local.entity.DeviceInfoEntity

/**
 * DAO Room untuk tabel [device_info].
 *
 * Tabel ini menyimpan pasangan kunci-nilai metadata perangkat, termasuk `local_device_id`
 * yang dibangkitkan saat aplikasi pertama kali dipasang (PRD Bagian 6.2).
 *
 * UUID ini digunakan oleh PushDataUseCase untuk mengisi kolom local_device_id
 * pada setiap DTO pengguna yang dikirim ke Supabase.
 */
@Dao
interface DeviceInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(info: DeviceInfoEntity)

    @Query("SELECT * FROM device_info WHERE key = :key LIMIT 1")
    suspend fun getByKey(key: String): DeviceInfoEntity?

    @Query("SELECT value FROM device_info WHERE key = 'local_device_id' LIMIT 1")
    suspend fun getLocalDeviceId(): String?
}
