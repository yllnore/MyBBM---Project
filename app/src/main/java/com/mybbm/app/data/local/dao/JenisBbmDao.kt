package com.mybbm.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mybbm.app.data.local.entity.JenisBbmEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room untuk tabel [jenis_bbm].
 *
 * Menyediakan akses baca ke data referensi jenis BBM yang disinkronkan dari Supabase.
 * Pembaruan data dilakukan saat delta-sync oleh SyncWorker atau seed awal via [insertAll].
 */
@Dao
interface JenisBbmDao {

    /**
     * Menyisipkan atau memperbarui daftar jenis BBM.
     * Jika baris dengan PK yang sama sudah ada, seluruh baris digantikan.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<JenisBbmEntity>)

    /**
     * Mengamati seluruh jenis BBM aktif secara reaktif.
     */
    @Query("SELECT * FROM jenis_bbm WHERE is_active = 1 ORDER BY nama_bbm ASC")
    fun observeActive(): Flow<List<JenisBbmEntity>>

    /**
     * Mengambil seluruh jenis BBM aktif.
     */
    @Query("SELECT * FROM jenis_bbm WHERE is_active = 1 ORDER BY nama_bbm ASC")
    suspend fun getActive(): List<JenisBbmEntity>

    /**
     * Mengambil satu jenis BBM berdasarkan ID.
     */
    @Query("SELECT * FROM jenis_bbm WHERE id_bbm = :id LIMIT 1")
    suspend fun getById(id: String): JenisBbmEntity?
}
