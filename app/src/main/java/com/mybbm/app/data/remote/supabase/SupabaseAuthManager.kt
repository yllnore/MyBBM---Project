package com.mybbm.app.data.remote.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.user.UserSession

/**
 * Utilitas manajemen autentikasi Supabase untuk aplikasi MyBBM.
 *
 * Menerapkan alur Anonymous Auth sesuai PRD Bagian 6.2 dan 8.3:
 * - Aplikasi bersifat zero-login & offline-first.
 * - Sesi autentikasi dibuat otomatis di latar belakang melalui [signInAnonymously]
 *   saat perangkat pertama kali tersambung ke jaringan internet dan belum memiliki sesi.
 * - Sesi anonim menghasilkan JWT yang mengidentifikasi pengguna via auth.uid()
 *   untuk penerapan Row Level Security (RLS) pada tabel cloud.
 */
class SupabaseAuthManager(
    private val supabase: SupabaseClient
) {
    /**
     * Memastikan sesi autentikasi Supabase aktif (PRD Bagian 8.3).
     *
     * Jika sesi saat ini kosong (`currentSessionOrNull() == null`), fungsi akan
     * secara otomatis memanggil [signInAnonymously] untuk membuat sesi anonim baru.
     * Jika sesi sudah ada, fungsi tidak melakukan pemanggilan ulang ke jaringan.
     *
     * @return [UserSession] aktif setelah diperiksa atau dibuat baru.
     */
    suspend fun ensureSession(): UserSession? {
        val currentSession = supabase.auth.currentSessionOrNull()
        if (currentSession != null) {
            return currentSession
        }

        // Lakukan Anonymous Sign-In tanpa interaksi pengguna
        supabase.auth.signInAnonymously()
        return supabase.auth.currentSessionOrNull()
    }

    /**
     * Mengembalikan sesi pengguna aktif saat ini, atau null jika belum ada sesi.
     */
    fun getCurrentSession(): UserSession? {
        return supabase.auth.currentSessionOrNull()
    }

    /**
     * Mengembalikan user ID (auth.uid()) dari sesi aktif.
     * Mengembalikan null jika belum ada sesi aktif.
     */
    fun getCurrentUserId(): String? {
        return supabase.auth.currentSessionOrNull()?.user?.id
    }

    /**
     * Memeriksa apakah saat ini sudah ada sesi autentikasi yang valid.
     */
    fun hasActiveSession(): Boolean {
        return supabase.auth.currentSessionOrNull() != null
    }

    /**
     * Melakukan proses sign-out (menghapus sesi aktif).
     */
    suspend fun signOut() {
        supabase.auth.signOut()
    }
}
