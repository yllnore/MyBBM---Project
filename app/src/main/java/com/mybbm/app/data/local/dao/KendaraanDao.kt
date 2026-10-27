package com.mybbm.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mybbm.app.data.local.entity.KendaraanEntity
import com.mybbm.app.data.local.entity.SyncState
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room untuk tabel [kendaraan].
 *
 * Menyediakan operasi CRUD lengkap beserta logika transaksi atomik untuk mengatur
 * kendaraan default sesuai spesifikasi PRD Bagian 4.6.
 *
 * **Aturan kendaraan default:**
 * Hanya satu kendaraan aktif ([deletedAt] = null) yang boleh memiliki [isDefault] = true.
 * Gunakan [setDefault] untuk mengubah kendaraan default secara aman dalam satu transaksi.
 */
@Dao
interface KendaraanDao {

    // =========================================================================
    // INSERT / UPDATE
    // =========================================================================

    /**
     * Menyisipkan kendaraan baru ke database.
     * Jika terjadi konflik PK, baris lama digantikan seluruhnya.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(kendaraan: KendaraanEntity)

    /**
     * Menyisipkan beberapa kendaraan sekaligus (misalnya saat restore/seed).
     * Jika terjadi konflik PK, baris lama digantikan seluruhnya.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(kendaraan: List<KendaraanEntity>)

    /**
     * Memperbarui data kendaraan yang sudah ada.
     * Biasanya dipakai untuk memperbarui [updatedAt] dan [syncState] setelah push berhasil.
     */
    @Update
    suspend fun update(kendaraan: KendaraanEntity)

    // =========================================================================
    // LOGIKA KENDARAAN DEFAULT
    // =========================================================================

    /**
     * Menghapus status default dari **semua** kendaraan aktif yang saat ini berstatus default.
     *
     * Query ini juga:
     * - Memperbarui [KendaraanEntity.updatedAt] ke [now] agar perubahan terdeteksi saat push.
     * - Menandai [KendaraanEntity.syncState] = [SyncState.PENDING] agar baris dikirim ulang.
     *
     * Dipanggil **hanya** dari [setDefault] dalam transaksi yang sama; jangan panggil
     * langsung dari luar DAO.
     *
     * @param now Stempel waktu sekarang dalam epoch milli-detik (UTC).
     */
    @Query(
        """
        UPDATE kendaraan
        SET    is_default = 0,
               sync_state = ${SyncState.PENDING},
               updated_at = :now
        WHERE  is_default = 1
          AND  deleted_at IS NULL
        """
    )
    suspend fun clearDefault(now: Long)

    /**
     * Menetapkan kendaraan dengan [id] sebagai kendaraan default.
     *
     * Query ini juga:
     * - Memperbarui [KendaraanEntity.updatedAt] ke [now].
     * - Menandai [KendaraanEntity.syncState] = [SyncState.PENDING].
     *
     * Dipanggil **hanya** dari [setDefault] dalam transaksi yang sama; jangan panggil
     * langsung dari luar DAO.
     *
     * @param id    UUID kendaraan yang akan dijadikan default.
     * @param now   Stempel waktu sekarang dalam epoch milli-detik (UTC).
     */
    @Query(
        """
        UPDATE kendaraan
        SET    is_default = 1,
               sync_state = ${SyncState.PENDING},
               updated_at = :now
        WHERE  id_kendaraan = :id
        """
    )
    suspend fun markDefault(id: String, now: Long)

    /**
     * Mengganti kendaraan default secara atomik dalam satu transaksi Room.
     *
     * Urutan eksekusi:
     * 1. [clearDefault] — melepas status default dari kendaraan sebelumnya.
     * 2. [markDefault] — menetapkan [id] sebagai kendaraan default baru.
     *
     * Setelah operasi ini selesai, kedua baris yang berubah akan memiliki
     * [KendaraanEntity.syncState] = [SyncState.PENDING] dan siap dikirim ke Supabase.
     *
     * Jika terjadi pengecualian di antara dua langkah, seluruh transaksi di-rollback
     * sehingga database tidak berada dalam kondisi tidak konsisten.
     *
     * @param id    UUID kendaraan baru yang akan menjadi default.
     * @param now   Stempel waktu sekarang dalam epoch milli-detik (UTC).
     *              Gunakan [System.currentTimeMillis].
     */
    @Transaction
    suspend fun setDefault(id: String, now: Long) {
        clearDefault(now)
        markDefault(id, now)
    }

    // =========================================================================
    // QUERY OBSERVASI (Flow)
    // =========================================================================

    /**
     * Mengamati kendaraan default aktif secara reaktif.
     *
     * Mengembalikan [Flow] yang memancarkan:
     * - [KendaraanEntity] saat ada kendaraan dengan [isDefault] = true dan [deletedAt] = null.
     * - `null` jika tidak ada kendaraan default (misalnya semua kendaraan dihapus).
     *
     * Digunakan oleh ViewModel untuk menampilkan kendaraan yang dipilih otomatis
     * pada layar kalkulator.
     *
     * @return [Flow] yang akan memancarkan nilai terbaru setiap kali tabel kendaraan berubah.
     */
    @Query(
        """
        SELECT * FROM kendaraan
        WHERE  is_default = 1
          AND  deleted_at IS NULL
        LIMIT  1
        """
    )
    fun observeDefault(): Flow<KendaraanEntity?>

    // =========================================================================
    // QUERY BACA
    // =========================================================================

    /**
     * Mengambil semua kendaraan aktif (belum dihapus lunak), diurutkan dari terbaru.
     */
    @Query(
        """
        SELECT * FROM kendaraan
        WHERE  deleted_at IS NULL
        ORDER  BY created_at DESC
        """
    )
    fun observeAll(): Flow<List<KendaraanEntity>>

    /**
     * Mengambil satu kendaraan berdasarkan ID.
     * Mengembalikan null jika tidak ditemukan.
     */
    @Query("SELECT * FROM kendaraan WHERE id_kendaraan = :id LIMIT 1")
    suspend fun getById(id: String): KendaraanEntity?

    /**
     * Mengambil semua kendaraan yang menunggu sinkronisasi ([syncState] = [SyncState.PENDING]).
     * Dipanggil oleh SyncWorker saat melakukan push ke Supabase.
     */
    @Query("SELECT * FROM kendaraan WHERE sync_state = ${SyncState.PENDING}")
    suspend fun getPending(): List<KendaraanEntity>

    /**
     * Mengambil kendaraan aktif yang paling baru dibuat.
     * Digunakan untuk menentukan kendaraan default baru setelah kendaraan default sebelumnya
     * dihapus secara lunak.
     */
    @Query(
        """
        SELECT * FROM kendaraan
        WHERE  deleted_at IS NULL
        ORDER  BY created_at DESC
        LIMIT  1
        """
    )
    suspend fun getLatestActive(): KendaraanEntity?

    // =========================================================================
    // SOFT DELETE
    // =========================================================================

    /**
     * Melakukan soft delete pada kendaraan dengan menandai [deletedAt].
     *
     * Setelah operasi ini, periksa apakah kendaraan yang dihapus adalah default,
     * dan jika ya, tetapkan [getLatestActive] sebagai default baru menggunakan [setDefault].
     *
     * @param id    UUID kendaraan yang akan dihapus.
     * @param now   Stempel waktu sekarang dalam epoch milli-detik (UTC).
     */
    @Query(
        """
        UPDATE kendaraan
        SET    deleted_at = :now,
               updated_at = :now,
               sync_state = ${SyncState.PENDING}
        WHERE  id_kendaraan = :id
        """
    )
    suspend fun softDelete(id: String, now: Long)

    // =========================================================================
    // SINKRONISASI: TANDAI SYNCED
    // =========================================================================

    /**
     * Menandai kendaraan-kendaraan sebagai sudah tersinkronisasi ([syncState] = [SyncState.SYNCED]).
     * Dipanggil oleh SyncWorker setelah push ke Supabase berhasil.
     *
     * @param ids Daftar UUID kendaraan yang berhasil diunggah.
     */
    @Query(
        """
        UPDATE kendaraan
        SET    sync_state = ${SyncState.SYNCED}
        WHERE  id_kendaraan IN (:ids)
        """
    )
    suspend fun markSynced(ids: List<String>)
}
