package com.mybbm.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mybbm.app.data.local.dao.BbmKustomDao
import com.mybbm.app.data.local.dao.DeviceInfoDao
import com.mybbm.app.data.local.dao.HargaBbmDao
import com.mybbm.app.data.local.dao.HasilPerhitunganDao
import com.mybbm.app.data.local.dao.JenisBbmDao
import com.mybbm.app.data.local.dao.KendaraanDao
import com.mybbm.app.data.local.dao.KendaraanKustomDao
import com.mybbm.app.data.local.dao.PerjalananDao
import com.mybbm.app.data.local.dao.RiwayatDao
import com.mybbm.app.data.local.dao.SyncMetaDao
import com.mybbm.app.data.local.entity.BbmKustomEntity
import com.mybbm.app.data.local.entity.DeviceInfoEntity
import com.mybbm.app.data.local.entity.HargaBbmEntity
import com.mybbm.app.data.local.entity.HasilPerhitunganEntity
import com.mybbm.app.data.local.entity.JenisBbmEntity
import com.mybbm.app.data.local.entity.KendaraanEntity
import com.mybbm.app.data.local.entity.KendaraanKustomEntity
import com.mybbm.app.data.local.entity.PerjalananEntity
import com.mybbm.app.data.local.entity.RiwayatEntity
import com.mybbm.app.data.local.entity.SyncMetaEntity

/**
 * Room Database untuk arsitektur Hybrid (Online + Offline).
 * Database ini menyimpan data kustom pengguna, riwayat perjalanan, serta sync & reference cache.
 */
@Database(
    entities = [
        KendaraanKustomEntity::class,
        BbmKustomEntity::class,
        RiwayatEntity::class,
        KendaraanEntity::class,
        JenisBbmEntity::class,
        HargaBbmEntity::class,
        PerjalananEntity::class,
        HasilPerhitunganEntity::class,
        DeviceInfoEntity::class,
        SyncMetaEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun kendaraanKustomDao(): KendaraanKustomDao
    abstract fun bbmKustomDao(): BbmKustomDao
    abstract fun riwayatDao(): RiwayatDao

    abstract fun kendaraanDao(): KendaraanDao
    abstract fun jenisBbmDao(): JenisBbmDao
    abstract fun hargaBbmDao(): HargaBbmDao
    abstract fun perjalananDao(): PerjalananDao
    abstract fun hasilPerhitunganDao(): HasilPerhitunganDao
    abstract fun deviceInfoDao(): DeviceInfoDao
    abstract fun syncMetaDao(): SyncMetaDao

    // Kompatibilitas untuk pemanggil lama
    fun bbmDao(): BbmKustomDao = bbmKustomDao()
    fun vehicleDao(): KendaraanKustomDao = kendaraanKustomDao()
    fun fuelDao(): BbmKustomDao = bbmKustomDao()
    fun historyDao(): RiwayatDao = riwayatDao()

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mybbm_hybrid_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
