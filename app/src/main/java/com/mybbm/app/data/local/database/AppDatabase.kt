package com.mybbm.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mybbm.app.data.local.dao.BbmKustomDao
import com.mybbm.app.data.local.dao.KendaraanKustomDao
import com.mybbm.app.data.local.dao.RiwayatDao
import com.mybbm.app.data.local.entity.BbmKustomEntity
import com.mybbm.app.data.local.entity.KendaraanKustomEntity
import com.mybbm.app.data.local.entity.RiwayatEntity

/**
 * Room Database untuk arsitektur Hybrid (Online + Offline).
 * Database ini menyimpan cache data kendaraan & BBM dari Supabase beserta data kustom pengguna
 * dan riwayat perjalanan secara offline.
 */
@Database(
    entities = [KendaraanKustomEntity::class, BbmKustomEntity::class, RiwayatEntity::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun kendaraanKustomDao(): KendaraanKustomDao
    abstract fun bbmKustomDao(): BbmKustomDao
    abstract fun riwayatDao(): RiwayatDao

    // Kompatibilitas untuk pemanggil lama
    fun kendaraanDao(): KendaraanKustomDao = kendaraanKustomDao()
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
