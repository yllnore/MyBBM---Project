package com.mybbm.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mybbm.app.data.local.entity.SyncMetaEntity

/**
 * DAO Room untuk tabel [sync_meta].
 *
 * Menyimpan metadata sinkronisasi sebagai pasangan kunci-nilai.
 * Kunci yang digunakan:
 *   - reference_cursor: epoch milli-detik dari baris terakhir yang berhasil ditarik (PRD Bagian 6.4).
 *
 * Kursor ini digunakan oleh SyncWorker untuk menjalankan delta-sync yang efisien.
 * Nilai diambil dari max(updated_at) data yang ditarik, bukan jam perangkat,
 * untuk menghindari clock skew (PRD Bagian 6.4).
 */
@Dao
interface SyncMetaDao {

    /** Menyisipkan atau menggantikan metadata sinkronisasi. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(meta: SyncMetaEntity)

    /**
     * Membaca nilai kursor berdasarkan kunci.
     * @param key Kunci metadata, contoh: reference_cursor.
     * @return Nilai epoch milli-detik, atau null jika kunci belum ada.
     */
    @Query("SELECT value FROM sync_meta WHERE key = :key LIMIT 1")
    suspend fun getValue(key: String): Long?

    /**
     * Membaca kursor delta-sync untuk data referensi (jenis_bbm dan harga_bbm).
     * Mengembalikan null jika belum ada pull sebelumnya (pull pertama = tarik semua data).
     */
    @Query("SELECT value FROM sync_meta WHERE key = 'reference_cursor' LIMIT 1")
    suspend fun getReferenceCursor(): Long?
}
