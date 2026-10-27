package com.mybbm.app.data.remote.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Penyedia instance tunggal (Singleton) [SupabaseClient] untuk aplikasi MyBBM.
 *
 * Mendaftarkan plugin:
 * 1. [Auth] - Autentikasi anonim (Anonymous Sign-In) untuk pengguna offline-first (PRD Bagian 6.2).
 * 2. [Postgrest] - Komunikasi dengan database PostgreSQL Supabase melalui PostgREST API (PRD Bagian 8.2).
 *
 * Keamanan:
 * - Hanya menggunakan kunci publik anon (publishable key).
 * - Kunci service_role TIDAK BOLEH berada di dalam kode aplikasi klien (PRD Bagian 8.2).
 */
object SupabaseClientProvider {

    @Volatile
    private var instance: SupabaseClient? = null

    /**
     * Mengembalikan instance tunggal [SupabaseClient].
     * Jika belum diinisialisasi, akan mencoba membaca nilai dari BuildConfig.
     *
     * @param supabaseUrl URL project Supabase (opsional, jika ingin meng-override BuildConfig)
     * @param supabaseAnonKey Kunci publik anon Supabase (opsional, jika ingin meng-override BuildConfig)
     */
    fun getInstance(
        supabaseUrl: String = getSupabaseUrlFromBuildConfig(),
        supabaseAnonKey: String = getSupabaseAnonKeyFromBuildConfig()
    ): SupabaseClient {
        return instance ?: synchronized(this) {
            instance ?: createClient(supabaseUrl, supabaseAnonKey).also { instance = it }
        }
    }

    /**
     * Membangun klien Supabase baru dengan konfigurasi plugin Auth dan Postgrest.
     *
     * @param supabaseUrl URL proyek Supabase (https://<project-ref>.supabase.co)
     * @param supabaseAnonKey Kunci anonim publik Supabase
     */
    fun createClient(
        supabaseUrl: String,
        supabaseAnonKey: String
    ): SupabaseClient {
        require(supabaseUrl.isNotBlank()) { "Supabase URL tidak boleh kosong." }
        require(supabaseAnonKey.isNotBlank()) { "Supabase Anon Key tidak boleh kosong." }

        return createSupabaseClient(
            supabaseUrl = supabaseUrl,
            supabaseKey = supabaseAnonKey
        ) {
            install(Auth)
            install(Postgrest)
        }
    }

    /**
     * Mengatur instance klien secara eksplisit (berguna untuk testing atau Dependency Injection).
     */
    fun setClient(client: SupabaseClient?) {
        synchronized(this) {
            instance = client
        }
    }

    /**
     * Membaca konstanta SUPABASE_URL dari BuildConfig secara aman menggunakan refleksi,
     * sehingga kode tetap dapat dikompilasi sebelum kelas BuildConfig di-generate oleh AGP.
     */
    private fun getSupabaseUrlFromBuildConfig(): String {
        return try {
            val buildConfigClass = Class.forName("com.mybbm.app.BuildConfig")
            val field = buildConfigClass.getField("SUPABASE_URL")
            field.get(null) as? String ?: ""
        } catch (_: Exception) {
            ""
        }
    }

    /**
     * Membaca konstanta SUPABASE_ANON_KEY dari BuildConfig secara aman menggunakan refleksi.
     */
    private fun getSupabaseAnonKeyFromBuildConfig(): String {
        return try {
            val buildConfigClass = Class.forName("com.mybbm.app.BuildConfig")
            val field = buildConfigClass.getField("SUPABASE_ANON_KEY")
            field.get(null) as? String ?: ""
        } catch (_: Exception) {
            ""
        }
    }
}
