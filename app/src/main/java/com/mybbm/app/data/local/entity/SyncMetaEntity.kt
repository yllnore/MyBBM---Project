package com.mybbm.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entitas Room untuk tabel [sync_meta].
 *
 * Menyimpan metadata yang diperlukan untuk proses sinkronisasi, seperti kursor
 * delta-sync untuk tabel referensi.
 *
 * Contoh entri:
 *   key = "reference_cursor"
 *   value = 1698307200000L  // epoch milli-detik dari updated_at terbesar yang berhasil ditarik
 */
@Entity(tableName = "sync_meta")
data class SyncMetaEntity(
    /** Kunci yang mengidentifikasi jenis metadata. Contoh: "reference_cursor". */
    @PrimaryKey val key: String,

    /**
     * Nilai metadata dalam epoch milli-detik (UTC).
     * Untuk kursor sinkronisasi, ini adalah nilai [updatedAt] terbesar
     * dari data yang terakhir berhasil ditarik dari Supabase.
     */
    val value: Long
)
