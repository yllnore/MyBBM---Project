package com.mybbm.app.data.local.entity

/**
 * Konstanta status sinkronisasi untuk kolom [syncState] pada entitas pengguna.
 *
 * - [SYNCED]: Data sudah berhasil disinkronkan ke Supabase.
 * - [PENDING]: Data belum disinkronkan dan menunggu giliran di antrian sync.
 *
 * Kolom ini hanya ada di Room (tidak dikirim ke cloud).
 */
object SyncState {
    /** Data telah berhasil diunggah ke Supabase. */
    const val SYNCED = 0

    /** Data belum diunggah; perlu disinkronkan pada kesempatan berikutnya. */
    const val PENDING = 1
}
