package com.mybbm.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mybbm.app.data.local.converter.BigDecimalConverter
import com.mybbm.app.data.local.dao.DeviceInfoDao
import com.mybbm.app.data.local.dao.HargaBbmDao
import com.mybbm.app.data.local.dao.HasilPerhitunganDao
import com.mybbm.app.data.local.dao.JenisBbmDao
import com.mybbm.app.data.local.dao.KendaraanDao
import com.mybbm.app.data.local.dao.PerjalananDao
import com.mybbm.app.data.local.dao.SyncMetaDao
import com.mybbm.app.data.local.entity.DeviceInfoEntity
import com.mybbm.app.data.local.entity.HargaBbmEntity
import com.mybbm.app.data.local.entity.HasilPerhitunganEntity
import com.mybbm.app.data.local.entity.JenisBbmEntity
import com.mybbm.app.data.local.entity.KendaraanEntity
import com.mybbm.app.data.local.entity.PerjalananEntity
import com.mybbm.app.data.local.entity.SyncMetaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

/**
 * Database Room utama aplikasi MyBBM.
 *
 * Meregistrasikan semua entitas, TypeConverter, dan DAO yang diperlukan.
 * Versi database saat ini: [DATABASE_VERSION].
 *
 * **Entitas yang diregistrasikan:**
 * - [DeviceInfoEntity] — metadata perangkat (local_device_id).
 * - [JenisBbmEntity] — data referensi jenis BBM (dari Supabase).
 * - [HargaBbmEntity] — data referensi riwayat harga BBM (dari Supabase).
 * - [KendaraanEntity] — data kendaraan milik pengguna.
 * - [PerjalananEntity] — data perjalanan milik pengguna.
 * - [HasilPerhitunganEntity] — snapshot hasil perhitungan biaya BBM.
 * - [SyncMetaEntity] — kursor dan metadata untuk proses sinkronisasi.
 *
 * **Seed data:**
 * Saat database pertama kali dibuat ([RoomDatabase.Callback.onCreate]):
 * 1. UUID v4 dibangkitkan dan disimpan sebagai `local_device_id` di [DeviceInfoEntity] (PRD Bagian 6.2).
 * 2. File aset `seed_reference.json` dimuat ke tabel [JenisBbmEntity] dan [HargaBbmEntity]
 *    agar aplikasi dapat digunakan sepenuhnya offline (PRD Bagian 6.8).
 *
 * **Pola singleton:**
 * Gunakan [AppDatabase.getInstance] untuk mendapatkan instance tunggal database.
 */
@Database(
    entities = [
        DeviceInfoEntity::class,
        JenisBbmEntity::class,
        HargaBbmEntity::class,
        KendaraanEntity::class,
        PerjalananEntity::class,
        HasilPerhitunganEntity::class,
        SyncMetaEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(BigDecimalConverter::class)
abstract class AppDatabase : RoomDatabase() {

    /** DAO untuk operasi tabel kendaraan, termasuk logika default atomik. */
    abstract fun kendaraanDao(): KendaraanDao

    /** DAO untuk membaca dan memperbarui data referensi jenis BBM. */
    abstract fun jenisBbmDao(): JenisBbmDao

    /** DAO untuk membaca data referensi harga BBM dan menjalankan Price Resolver. */
    abstract fun hargaBbmDao(): HargaBbmDao

    /** DAO untuk operasi CRUD perjalanan dan cache rute. */
    abstract fun perjalananDao(): PerjalananDao

    /** DAO untuk menyimpan dan membaca snapshot hasil perhitungan biaya BBM. */
    abstract fun hasilPerhitunganDao(): HasilPerhitunganDao

    /** DAO untuk metadata perangkat (local_device_id). */
    abstract fun deviceInfoDao(): DeviceInfoDao

    /** DAO untuk metadata sinkronisasi (reference_cursor). */
    abstract fun syncMetaDao(): SyncMetaDao

    companion object {

        /**
         * Versi skema database saat ini.
         */
        const val DATABASE_VERSION = 1

        /** Nama file SQLite di storage perangkat. */
        private const val DATABASE_NAME = "mybbm.db"

        /**
         * Nama file aset yang memuat data seed untuk tabel referensi.
         */
        private const val SEED_ASSET_NAME = "seed_reference.json"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Mengembalikan instance singleton [AppDatabase].
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(SeedCallback(context.applicationContext))
                .build()
        }

        /**
         * Callback yang menginisialisasi database saat pertama dibuat.
         */
        private class SeedCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Inisialisasi berjalan di latar belakang agar tidak memblokir UI
                CoroutineScope(Dispatchers.IO).launch {
                    initDeviceInfo(db)
                    loadSeedData(db)
                }
            }

            /**
             * Membangkitkan local_device_id (UUID v4) saat instalasi pertama (PRD Bagian 6.2).
             */
            private fun initDeviceInfo(db: SupportSQLiteDatabase) {
                try {
                    val deviceId = UUID.randomUUID().toString()
                    db.execSQL(
                        "INSERT OR IGNORE INTO device_info (key, value) VALUES ('local_device_id', ?)",
                        arrayOf(deviceId)
                    )
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "Gagal inisialisasi local_device_id: ${e.message}", e)
                }
            }

            /**
             * Membaca [SEED_ASSET_NAME] dari aset APK dan menyisipkan data referensi awal.
             */
            private fun loadSeedData(db: SupportSQLiteDatabase) {
                try {
                    val json = context.assets.open(SEED_ASSET_NAME)
                        .bufferedReader()
                        .use { it.readText() }
                    val root = JSONObject(json)

                    db.beginTransaction()
                    try {
                        // Sisipkan JENIS_BBM
                        val jenisBbmArray = root.optJSONArray("jenis_bbm")
                        if (jenisBbmArray != null) {
                            for (i in 0 until jenisBbmArray.length()) {
                                val obj = jenisBbmArray.getJSONObject(i)
                                db.execSQL(
                                    """
                                    INSERT OR IGNORE INTO jenis_bbm
                                        (id_bbm, nama_bbm, jenis_bahan_bakar, is_active, updated_at)
                                    VALUES (?, ?, ?, ?, ?)
                                    """.trimIndent(),
                                    arrayOf(
                                        obj.getString("id_bbm"),
                                        obj.getString("nama_bbm"),
                                        obj.getString("jenis_bahan_bakar"),
                                        if (obj.optBoolean("is_active", true)) 1 else 0,
                                        obj.getLong("updated_at")
                                    )
                                )
                            }
                        }

                        // Sisipkan HARGA_BBM
                        val hargaBbmArray = root.optJSONArray("harga_bbm")
                        if (hargaBbmArray != null) {
                            for (i in 0 until hargaBbmArray.length()) {
                                val obj = hargaBbmArray.getJSONObject(i)
                                val berlakuHingga: Any? = if (obj.isNull("berlaku_hingga")) {
                                    null
                                } else {
                                    obj.getLong("berlaku_hingga")
                                }
                                db.execSQL(
                                    """
                                    INSERT OR IGNORE INTO harga_bbm
                                        (id_harga, id_bbm, wilayah, harga_per_liter,
                                         berlaku_mulai, berlaku_hingga, updated_at)
                                    VALUES (?, ?, ?, ?, ?, ?, ?)
                                    """.trimIndent(),
                                    arrayOf(
                                        obj.getString("id_harga"),
                                        obj.getString("id_bbm"),
                                        obj.getString("wilayah"),
                                        obj.getString("harga_per_liter"),
                                        obj.getLong("berlaku_mulai"),
                                        berlakuHingga,
                                        obj.getLong("updated_at")
                                    )
                                )
                            }
                        }

                        db.setTransactionSuccessful()
                    } finally {
                        db.endTransaction()
                    }
                } catch (e: Exception) {
                    android.util.Log.e(
                        "AppDatabase",
                        "Gagal memuat seed_reference.json: ${e.message}",
                        e
                    )
                }
            }
        }
    }
}
