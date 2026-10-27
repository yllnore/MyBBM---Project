package com.mybbm.app.data.remote.supabase

import com.mybbm.app.data.remote.dto.HargaBbmDto
import com.mybbm.app.data.remote.dto.JenisBbmDto

/**
 * Antarmuka repositori remote untuk penarikan (pull) data referensi dari Supabase PostgREST.
 *
 * Mengacu pada PRD Bagian 6.4 dan 8.3:
 * - Klien hanya membaca (read-only) tabel referensi [jenis_bbm] dan [harga_bbm].
 * - Menggunakan delta-sync berdasarkan kolom `updated_at`.
 * - Menerapkan filter: gte("updated_at", cursor)
 * - Menerapkan pengurutan menaik: order("updated_at", Order.ASCENDING)
 * - Batas paginasi: 1.000 baris per batch permintaan.
 */
interface ReferenceRemoteRepository {

    /**
     * Menarik satu batch perubahan [JenisBbmDto] dari Supabase PostgREST.
     *
     * @param cursorIso Nilai kursor waktu dalam format ISO-8601 (contoh: "1970-01-01T00:00:00Z").
     * @param limit Batas maksimum baris yang ditarik per request (default: 1.000 baris).
     * @return Daftar [JenisBbmDto] yang berhasil ditarik.
     */
    suspend fun pullJenisBbm(
        cursorIso: String,
        limit: Long = DEFAULT_PAGE_SIZE
    ): List<JenisBbmDto>

    /**
     * Menarik satu batch perubahan [HargaBbmDto] dari Supabase PostgREST.
     *
     * @param cursorIso Nilai kursor waktu dalam format ISO-8601 (contoh: "1970-01-01T00:00:00Z").
     * @param limit Batas maksimum baris yang ditarik per request (default: 1.000 baris).
     * @return Daftar [HargaBbmDto] yang berhasil ditarik.
     */
    suspend fun pullHargaBbm(
        cursorIso: String,
        limit: Long = DEFAULT_PAGE_SIZE
    ): List<HargaBbmDto>

    /**
     * Menarik seluruh data perubahan [JenisBbmDto] sejak kursor tertentu dengan paginasi berulang.
     * Loop berulang menarik batch 1.000 baris hingga tidak ada data tersisa (PRD Bagian 6.4).
     *
     * @param cursorEpochMillis Stempel waktu kursor dalam epoch milli-detik UTC.
     * @return Seluruh daftar [JenisBbmDto] yang ditarik.
     */
    suspend fun pullAllJenisBbm(
        cursorEpochMillis: Long = 0L
    ): List<JenisBbmDto>

    /**
     * Menarik seluruh data perubahan [HargaBbmDto] sejak kursor tertentu dengan paginasi berulang.
     * Loop berulang menarik batch 1.000 baris hingga tidak ada data tersisa (PRD Bagian 6.4).
     *
     * @param cursorEpochMillis Stempel waktu kursor dalam epoch milli-detik UTC.
     * @return Seluruh daftar [HargaBbmDto] yang ditarik.
     */
    suspend fun pullAllHargaBbm(
        cursorEpochMillis: Long = 0L
    ): List<HargaBbmDto>

    companion object {
        /** Batas paginasi baku sesuai PRD Bagian 6.4 dan 8.3: 1.000 baris */
        const val DEFAULT_PAGE_SIZE: Long = 1000L
    }
}
