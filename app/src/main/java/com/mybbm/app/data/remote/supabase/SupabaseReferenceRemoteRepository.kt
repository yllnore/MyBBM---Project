package com.mybbm.app.data.remote.supabase

import com.mybbm.app.data.remote.dto.DateUtils
import com.mybbm.app.data.remote.dto.HargaBbmDto
import com.mybbm.app.data.remote.dto.JenisBbmDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

/**
 * Implementasi repositori remote [ReferenceRemoteRepository] menggunakan Supabase PostgREST SDK.
 *
 * Sesuai PRD Bagian 6.4 dan 8.3:
 * - Menjalankan kueri SELECT terhadap tabel [jenis_bbm] dan [harga_bbm].
 * - Menerapkan filter: gte("updated_at", cursor)
 * - Menerapkan pengurutan: order("updated_at", Order.ASCENDING)
 * - Menerapkan limit: limit(1000)
 * - Menangani paginasi berulang hingga seluruh baris delta habis ditarik.
 */
class SupabaseReferenceRemoteRepository(
    private val supabase: SupabaseClient
) : ReferenceRemoteRepository {

    override suspend fun pullJenisBbm(
        cursorIso: String,
        limit: Long
    ): List<JenisBbmDto> {
        return supabase.from(TABLE_JENIS_BBM).select {
            filter {
                gte(COLUMN_UPDATED_AT, cursorIso)
            }
            order(COLUMN_UPDATED_AT, order = Order.ASCENDING)
            limit(limit)
        }.decodeList<JenisBbmDto>()
    }

    override suspend fun pullHargaBbm(
        cursorIso: String,
        limit: Long
    ): List<HargaBbmDto> {
        return supabase.from(TABLE_HARGA_BBM).select {
            filter {
                gte(COLUMN_UPDATED_AT, cursorIso)
            }
            order(COLUMN_UPDATED_AT, order = Order.ASCENDING)
            limit(limit)
        }.decodeList<HargaBbmDto>()
    }

    override suspend fun pullAllJenisBbm(
        cursorEpochMillis: Long
    ): List<JenisBbmDto> {
        val allResults = mutableListOf<JenisBbmDto>()
        val seenIds = mutableSetOf<String>()
        var currentCursorIso = DateUtils.epochMillisToIso(cursorEpochMillis)

        while (true) {
            val batch = pullJenisBbm(
                cursorIso = currentCursorIso,
                limit = ReferenceRemoteRepository.DEFAULT_PAGE_SIZE
            )

            if (batch.isEmpty()) {
                break
            }

            // Hindari duplikasi jika ada baris yang ditarik ulang karena stempel waktu sama
            val uniqueItems = batch.filter { seenIds.add(it.idBbm) }
            allResults.addAll(uniqueItems)

            // Jika jumlah baris kurang dari batas batch, seluruh data telah selesai ditarik
            if (batch.size < ReferenceRemoteRepository.DEFAULT_PAGE_SIZE) {
                break
            }

            // Jika seluruh item pada batch sudah pernah dilihat, cegah perulangan tak berujung
            if (uniqueItems.isEmpty()) {
                break
            }

            // Perbarui kursor dengan stempel waktu updated_at baris terakhir
            currentCursorIso = batch.last().updatedAt
        }

        return allResults
    }

    override suspend fun pullAllHargaBbm(
        cursorEpochMillis: Long
    ): List<HargaBbmDto> {
        val allResults = mutableListOf<HargaBbmDto>()
        val seenIds = mutableSetOf<String>()
        var currentCursorIso = DateUtils.epochMillisToIso(cursorEpochMillis)

        while (true) {
            val batch = pullHargaBbm(
                cursorIso = currentCursorIso,
                limit = ReferenceRemoteRepository.DEFAULT_PAGE_SIZE
            )

            if (batch.isEmpty()) {
                break
            }

            // Hindari duplikasi jika ada baris yang ditarik ulang karena stempel waktu sama
            val uniqueItems = batch.filter { seenIds.add(it.idHarga) }
            allResults.addAll(uniqueItems)

            // Jika jumlah baris kurang dari batas batch, seluruh data telah selesai ditarik
            if (batch.size < ReferenceRemoteRepository.DEFAULT_PAGE_SIZE) {
                break
            }

            // Jika seluruh item pada batch sudah pernah dilihat, cegah perulangan tak berujung
            if (uniqueItems.isEmpty()) {
                break
            }

            // Perbarui kursor dengan stempel waktu updated_at baris terakhir
            currentCursorIso = batch.last().updatedAt
        }

        return allResults
    }

    companion object {
        private const val TABLE_JENIS_BBM = "jenis_bbm"
        private const val TABLE_HARGA_BBM = "harga_bbm"
        private const val COLUMN_UPDATED_AT = "updated_at"
    }
}
