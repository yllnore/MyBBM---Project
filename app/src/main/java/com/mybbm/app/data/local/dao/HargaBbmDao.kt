package com.mybbm.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mybbm.app.data.local.entity.HargaBbmEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room untuk tabel [harga_bbm].
 *
 * Menyediakan akses baca ke data referensi harga BBM yang disinkronkan dari Supabase.
 * Klien tidak dapat menulis ke tabel ini melalui API; penulisan dilakukan oleh
 * developer via Supabase Studio (sesuai PRD Bagian 8.5).
 *
 * Satu-satunya pengecualian adalah pemuatan seed data saat instalasi pertama dan
 * penerimaan delta-sync dari SyncWorker, keduanya menggunakan [insertAll].
 */
@Dao
interface HargaBbmDao {

    // =========================================================================
    // UPSERT — dipakai oleh SyncWorker saat delta-sync dan seed awal
    // =========================================================================

    /**
     * Menyisipkan atau memperbarui daftar harga BBM.
     * Jika baris dengan PK yang sama sudah ada, seluruh baris digantikan.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(hargaBbm: List<HargaBbmEntity>)

    // =========================================================================
    // PRICE RESOLVER — Bagian 5.5 PRD
    // =========================================================================

    /**
     * Mencari catatan harga BBM yang berlaku pada tanggal [tanggal] untuk BBM [idBbm]
     * di wilayah [wilayah].
     *
     * **Algoritma Price Resolver (PRD Bagian 5.5):**
     * 1. Filter: `id_bbm = :idBbm AND wilayah = :wilayah AND berlaku_mulai <= :tanggal`
     * 2. Urutkan `berlaku_mulai DESC`, ambil baris pertama.
     * 3. Jika tidak ada hasil → kembalikan null; use case harus mengembalikan
     *    [com.mybbm.app.domain.engine.FuelCostEngine.CalcError.HARGA_TIDAK_TERSEDIA].
     *
     * @param idBbm    UUID jenis BBM yang dipilih pengguna.
     * @param wilayah  Kode wilayah. Saat ini selalu "KALIMANTAN_TIMUR".
     * @param tanggal  Tanggal perhitungan dalam epoch milli-detik (UTC).
     *                 Gunakan awal hari (00:00 UTC) agar hasilnya deterministik.
     * @return [HargaBbmEntity] yang berlaku, atau null jika tidak ada data.
     */
    @Query(
        """
        SELECT * FROM harga_bbm
        WHERE  id_bbm       = :idBbm
          AND  wilayah      = :wilayah
          AND  berlaku_mulai <= :tanggal
        ORDER  BY berlaku_mulai DESC
        LIMIT  1
        """
    )
    suspend fun hargaTerkini(idBbm: String, wilayah: String, tanggal: Long): HargaBbmEntity?

    // =========================================================================
    // DELTA-SYNC SUPPORT
    // =========================================================================

    /**
     * Mencari nilai [HargaBbmEntity.updatedAt] terbesar yang tersimpan di Room.
     * Digunakan oleh SyncWorker sebagai kursor untuk delta-sync (PRD Bagian 6.4).
     *
     * @return Epoch milli-detik terbesar, atau null jika tabel kosong (instalasi baru).
     */
    @Query("SELECT MAX(updated_at) FROM harga_bbm")
    suspend fun getMaxUpdatedAt(): Long?

    // =========================================================================
    // OBSERVASI
    // =========================================================================

    /**
     * Mengamati semua harga yang berlaku untuk BBM [idBbm] secara reaktif.
     * Berguna untuk menampilkan riwayat harga di layar detail.
     */
    @Query(
        """
        SELECT * FROM harga_bbm
        WHERE  id_bbm = :idBbm
        ORDER  BY berlaku_mulai DESC
        """
    )
    fun observeByBbm(idBbm: String): Flow<List<HargaBbmEntity>>

    /**
     * Mengamati semua harga BBM yang masih aktif (berlaku_hingga IS NULL)
     * untuk wilayah tertentu.
     */
    @Query(
        """
        SELECT * FROM harga_bbm
        WHERE  wilayah        = :wilayah
          AND  berlaku_hingga IS NULL
        ORDER  BY berlaku_mulai DESC
        """
    )
    fun observeHargaAktif(wilayah: String): Flow<List<HargaBbmEntity>>
}
