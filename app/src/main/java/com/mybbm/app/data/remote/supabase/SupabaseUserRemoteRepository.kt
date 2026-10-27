package com.mybbm.app.data.remote.supabase

import com.mybbm.app.data.remote.dto.HasilPerhitunganDto
import com.mybbm.app.data.remote.dto.KendaraanDto
import com.mybbm.app.data.remote.dto.PerjalananDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

/**
 * Implementasi [UserRemoteRepository] menggunakan Supabase PostgREST SDK.
 *
 * Sesuai PRD Bagian 8.3:
 * Setiap operasi menggunakan upsert idempoten berdasarkan primary key tabel.
 * Server mengisi kolom user_id via DEFAULT auth.uid() dan RLS WITH CHECK.
 *
 * Urutan push yang benar (memenuhi constraint FK PostgreSQL):
 * 1. KENDARAAN
 * 2. PERJALANAN
 * 3. HASIL_PERHITUNGAN
 */
class SupabaseUserRemoteRepository(
    private val supabase: SupabaseClient
) : UserRemoteRepository {

    /**
     * Mengunggah daftar kendaraan ke Supabase via upsert.
     * Konflik diselesaikan berdasarkan id_kendaraan.
     */
    override suspend fun pushKendaraan(items: List<KendaraanDto>) {
        if (items.isEmpty()) return
        supabase.from(TABLE_KENDARAAN).upsert(items) {
            onConflict = COLUMN_ID_KENDARAAN
        }
    }

    /**
     * Mengunggah daftar perjalanan ke Supabase via upsert.
     * Konflik diselesaikan berdasarkan id_perjalanan.
     */
    override suspend fun pushPerjalanan(items: List<PerjalananDto>) {
        if (items.isEmpty()) return
        supabase.from(TABLE_PERJALANAN).upsert(items) {
            onConflict = COLUMN_ID_PERJALANAN
        }
    }

    /**
     * Mengunggah daftar hasil perhitungan ke Supabase via upsert.
     * Konflik diselesaikan berdasarkan id_hasil.
     */
    override suspend fun pushHasilPerhitungan(items: List<HasilPerhitunganDto>) {
        if (items.isEmpty()) return
        supabase.from(TABLE_HASIL_PERHITUNGAN).upsert(items) {
            onConflict = COLUMN_ID_HASIL
        }
    }

    companion object {
        private const val TABLE_KENDARAAN = "kendaraan"
        private const val TABLE_PERJALANAN = "perjalanan"
        private const val TABLE_HASIL_PERHITUNGAN = "hasil_perhitungan"

        private const val COLUMN_ID_KENDARAAN = "id_kendaraan"
        private const val COLUMN_ID_PERJALANAN = "id_perjalanan"
        private const val COLUMN_ID_HASIL = "id_hasil"
    }
}
