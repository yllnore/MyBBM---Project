package com.mybbm.app.data.remote.supabase

import com.mybbm.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

/**
 * Singleton konfigurasi dan inisialisasi client Supabase.
 * Menggunakan URL dan Anon Key dari BuildConfig (local.properties) dengan fallback aman.
 */
object SupabaseClientInstance {
    private val rawUrl: String = (BuildConfig.SUPABASE_URL.ifBlank { "https://vbmxsertxcthtdvxxksr.supabase.co" })
        .trim()
        .removeSuffix("/")
        .removeSuffix("/rest/v1")
        .removeSuffix("/")

    val client: SupabaseClient by lazy {
        createSupabaseClient(
            supabaseUrl = rawUrl,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY.ifBlank { "sb_publishable_LsxMia4oSWlDv8QUUv9X9Q_YZIofnWn" }
        ) {
            install(Postgrest)
        }
    }
}
