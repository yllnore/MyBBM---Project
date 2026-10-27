package com.mybbm.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.mybbm.app.data.local.entity.PerjalananEntity
import com.mybbm.app.data.local.entity.SyncState
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room untuk tabel [perjalanan].
 *
 * Menyediakan operasi CRUD untuk data perjalanan milik pengguna serta query
 * pendukung untuk cache rute (PRD Bagian 7.7) dan sinkronisasi.
 */
@Dao
interface PerjalananDao {

    // =========================================================================
    // INSERT / UPDATE
    // =========================================================================

    /**
     * Menyisipkan perjalanan baru. Jika PK konflik, baris lama digantikan.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(perjalanan: PerjalananEntity)

    /**
     * Memperbarui data perjalanan yang sudah ada.
     */
    @Update
    suspend fun update(perjalanan: PerjalananEntity)

    // =========================================================================
    // CACHE RUTE — PRD Bagian 7.7
    // =========================================================================

    /**
     * Mencari perjalanan yang sudah di-cache berdasarkan kunci koordinat
     * (asal dan tujuan yang dibulatkan ke 4 angka desimal ≈ ±11 m).
     *
     * Hanya mencocokkan perjalanan dengan sumber jarak [DIRECTIONS_API] dan
     * yang belum dihapus lunak ([deletedAt] IS NULL).
     *
     * @param asalLat    Lintang asal (sudah dibulatkan ke 4 desimal).
     * @param asalLng    Bujur asal (sudah dibulatkan ke 4 desimal).
     * @param tujuanLat  Lintang tujuan (sudah dibulatkan ke 4 desimal).
     * @param tujuanLng  Bujur tujuan (sudah dibulatkan ke 4 desimal).
     * @return [PerjalananEntity] dari cache jika ada, null jika tidak.
     */
    @Query(
        """
        SELECT * FROM perjalanan
        WHERE  ROUND(asal_lat,   4) = ROUND(:asalLat,   4)
          AND  ROUND(asal_lng,   4) = ROUND(:asalLng,   4)
          AND  ROUND(tujuan_lat, 4) = ROUND(:tujuanLat, 4)
          AND  ROUND(tujuan_lng, 4) = ROUND(:tujuanLng, 4)
          AND  sumber_jarak        = 'DIRECTIONS_API'
          AND  deleted_at          IS NULL
        ORDER  BY created_at DESC
        LIMIT  1
        """
    )
    suspend fun findCached(
        asalLat: Double,
        asalLng: Double,
        tujuanLat: Double,
        tujuanLng: Double
    ): PerjalananEntity?

    // =========================================================================
    // QUERY BACA
    // =========================================================================

    /**
     * Mengambil satu perjalanan berdasarkan ID.
     */
    @Query("SELECT * FROM perjalanan WHERE id_perjalanan = :id LIMIT 1")
    suspend fun getById(id: String): PerjalananEntity?

    /**
     * Mengamati semua perjalanan aktif, diurutkan dari terbaru.
     */
    @Query(
        """
        SELECT * FROM perjalanan
        WHERE  deleted_at IS NULL
        ORDER  BY created_at DESC
        """
    )
    fun observeAll(): Flow<List<PerjalananEntity>>

    /**
     * Mengambil semua perjalanan yang menunggu sinkronisasi.
     * Dipanggil oleh SyncWorker saat push ke Supabase.
     */
    @Query("SELECT * FROM perjalanan WHERE sync_state = ${SyncState.PENDING}")
    suspend fun getPending(): List<PerjalananEntity>

    // =========================================================================
    // SOFT DELETE
    // =========================================================================

    /**
     * Melakukan soft delete pada perjalanan.
     */
    @Query(
        """
        UPDATE perjalanan
        SET    deleted_at = :now,
               updated_at = :now,
               sync_state = ${SyncState.PENDING}
        WHERE  id_perjalanan = :id
        """
    )
    suspend fun softDelete(id: String, now: Long)

    // =========================================================================
    // SINKRONISASI
    // =========================================================================

    /**
     * Menandai perjalanan-perjalanan sebagai tersinkronisasi.
     */
    @Query(
        """
        UPDATE perjalanan
        SET    sync_state = ${SyncState.SYNCED}
        WHERE  id_perjalanan IN (:ids)
        """
    )
    suspend fun markSynced(ids: List<String>)
}
