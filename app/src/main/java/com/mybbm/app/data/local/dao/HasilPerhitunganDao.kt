package com.mybbm.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mybbm.app.data.local.entity.HasilPerhitunganEntity
import com.mybbm.app.data.local.entity.SyncState
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room untuk tabel [hasil_perhitungan].
 *
 * Menyimpan snapshot immutable hasil perhitungan biaya BBM. Data ini dicadangkan ke
 * Supabase saat online dan tidak pernah diubah setelah disimpan (kecuali soft delete).
 */
@Dao
interface HasilPerhitunganDao {

    // =========================================================================
    // INSERT
    // =========================================================================

    /**
     * Menyisipkan satu hasil perhitungan baru.
     * Jika PK konflik, baris lama digantikan (idempoten saat retry).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(hasilPerhitungan: HasilPerhitunganEntity)

    // =========================================================================
    // QUERY BACA
    // =========================================================================

    /**
     * Mengamati seluruh riwayat perhitungan aktif, diurutkan dari terbaru.
     * Digunakan oleh layar riwayat untuk menampilkan daftar kalkulasi.
     */
    @Query(
        """
        SELECT * FROM hasil_perhitungan
        WHERE  deleted_at IS NULL
        ORDER  BY created_at DESC
        """
    )
    fun observeAll(): Flow<List<HasilPerhitunganEntity>>

    /**
     * Mengamati semua hasil perhitungan untuk perjalanan tertentu.
     */
    @Query(
        """
        SELECT * FROM hasil_perhitungan
        WHERE  id_perjalanan = :idPerjalanan
          AND  deleted_at    IS NULL
        ORDER  BY created_at DESC
        """
    )
    fun observeByPerjalanan(idPerjalanan: String): Flow<List<HasilPerhitunganEntity>>

    /**
     * Mengambil satu hasil perhitungan berdasarkan ID.
     */
    @Query("SELECT * FROM hasil_perhitungan WHERE id_hasil = :id LIMIT 1")
    suspend fun getById(id: String): HasilPerhitunganEntity?

    /**
     * Mengambil semua hasil yang menunggu sinkronisasi.
     * Dipanggil oleh SyncWorker (push dilakukan setelah KENDARAAN dan PERJALANAN).
     */
    @Query("SELECT * FROM hasil_perhitungan WHERE sync_state = ${SyncState.PENDING}")
    suspend fun getPending(): List<HasilPerhitunganEntity>

    // =========================================================================
    // SOFT DELETE
    // =========================================================================

    /**
     * Melakukan soft delete pada satu hasil perhitungan.
     */
    @Query(
        """
        UPDATE hasil_perhitungan
        SET    deleted_at = :now,
               updated_at = :now,
               sync_state = ${SyncState.PENDING}
        WHERE  id_hasil = :id
        """
    )
    suspend fun softDelete(id: String, now: Long)

    // =========================================================================
    // SINKRONISASI
    // =========================================================================

    /**
     * Menandai hasil perhitungan sebagai tersinkronisasi setelah push berhasil.
     */
    @Query(
        """
        UPDATE hasil_perhitungan
        SET    sync_state = ${SyncState.SYNCED}
        WHERE  id_hasil IN (:ids)
        """
    )
    suspend fun markSynced(ids: List<String>)
}
