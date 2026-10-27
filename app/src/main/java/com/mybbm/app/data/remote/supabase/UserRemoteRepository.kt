package com.mybbm.app.data.remote.supabase

import com.mybbm.app.data.remote.dto.HasilPerhitunganDto
import com.mybbm.app.data.remote.dto.KendaraanDto
import com.mybbm.app.data.remote.dto.PerjalananDto

/**
 * Antarmuka repositori remote untuk operasi push (upsert) data milik pengguna ke Supabase.
 *
 * Sesuai PRD Bagian 6.1 dan 8.3:
 * - Hanya data berstatus PENDING yang dikirim.
 * - Upsert dilakukan secara idempoten berdasarkan primary key.
 * - Urutan push: KENDARAAN -> PERJALANAN -> HASIL_PERHITUNGAN (memenuhi FK dependency).
 * - Kolom user_id diisi server via DEFAULT auth.uid(); tidak ada di DTO.
 * - Kolom local_device_id dibawa dari tabel device_info Room ke setiap DTO.
 */
interface UserRemoteRepository {

    /**
     * Mengunggah daftar kendaraan ke Supabase via upsert idempoten.
     * Konflik diselesaikan berdasarkan primary key id_kendaraan.
     *
     * @param items Daftar [KendaraanDto] yang akan di-upsert.
     * @throws Exception jika terjadi galat jaringan atau respons 5xx.
     */
    suspend fun pushKendaraan(items: List<KendaraanDto>)

    /**
     * Mengunggah daftar perjalanan ke Supabase via upsert idempoten.
     * Konflik diselesaikan berdasarkan primary key id_perjalanan.
     * Harus dipanggil SETELAH [pushKendaraan] untuk memenuhi constraint FK.
     *
     * @param items Daftar [PerjalananDto] yang akan di-upsert.
     * @throws Exception jika terjadi galat jaringan atau respons 5xx.
     */
    suspend fun pushPerjalanan(items: List<PerjalananDto>)

    /**
     * Mengunggah daftar hasil perhitungan ke Supabase via upsert idempoten.
     * Konflik diselesaikan berdasarkan primary key id_hasil.
     * Harus dipanggil SETELAH [pushPerjalanan] untuk memenuhi constraint FK.
     *
     * @param items Daftar [HasilPerhitunganDto] yang akan di-upsert.
     * @throws Exception jika terjadi galat jaringan atau respons 5xx.
     */
    suspend fun pushHasilPerhitungan(items: List<HasilPerhitunganDto>)
}
