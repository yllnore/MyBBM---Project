package com.mybbm.app.domain.usecase

import com.mybbm.app.data.local.dao.HasilPerhitunganDao
import com.mybbm.app.data.local.dao.HargaBbmDao
import com.mybbm.app.data.local.dao.KendaraanDao
import com.mybbm.app.data.local.dao.PerjalananDao
import com.mybbm.app.data.local.entity.HasilPerhitunganEntity
import com.mybbm.app.data.local.entity.HargaBbmEntity
import com.mybbm.app.data.local.entity.KendaraanEntity
import com.mybbm.app.data.local.entity.PerjalananEntity
import com.mybbm.app.data.local.entity.SyncState
import com.mybbm.app.domain.engine.FuelCostEngine
import kotlinx.coroutines.flow.first
import java.math.BigDecimal
import java.util.UUID

/**
 * Use case utama untuk menghitung biaya BBM dan menyimpan hasilnya.
 *
 * Merangkai seluruh alur berikut sesuai spesifikasi PRD Bagian 5.6:
 * 1. Mengambil data perjalanan dari Room berdasarkan [idPerjalanan].
 * 2. Mengambil data kendaraan yang akan digunakan:
 *    - Jika [idKendaraan] diberikan (tidak null), gunakan kendaraan tersebut.
 *    - Jika null, gunakan kendaraan default aktif ([KendaraanDao.observeDefault]).
 * 3. Mencari harga BBM terkini melalui Price Resolver ([HargaBbmDao.hargaTerkini]).
 * 4. Mengeksekusi [FuelCostEngine.hitung] dengan input dari data di atas.
 * 5. Menyimpan [HasilPerhitunganEntity] ke Room dalam transaksi atomik
 *    (sync_state = [SyncState.PENDING]).
 * 6. Mengembalikan [Result] yang membawa output perhitungan dan penanda `isStale`.
 *
 * **Kendaraan default:** pada layar kalkulator, [idKendaraan] = null sehingga use case
 * otomatis mengambil kendaraan dengan `is_default = 1`. Pengguna tidak perlu memilih
 * ulang setiap kali membuka aplikasi (PRD Bagian 5.6).
 *
 * **isStale:** jika harga BBM yang digunakan berusia lebih dari [STALE_THRESHOLD_DAYS] hari
 * terhitung dari [tanggalPerhitungan], hasil diberi penanda `isStale = true`. UI wajib
 * menampilkan peringatan agar pengguna tahu data harga mungkin sudah tidak akurat.
 * Perhitungan tetap diizinkan (tidak diblokir).
 *
 * Kelas ini sepenuhnya **suspend** dan berjalan di coroutine konteks pemanggil (biasanya
 * [kotlinx.coroutines.Dispatchers.IO] melalui ViewModel).
 *
 * @property kendaraanDao      DAO untuk mengambil data kendaraan.
 * @property perjalananDao     DAO untuk mengambil data perjalanan.
 * @property hargaBbmDao       DAO untuk Price Resolver.
 * @property hasilDao          DAO untuk menyimpan hasil perhitungan.
 * @property wilayah           Kode wilayah. Default: "KALIMANTAN_TIMUR".
 */
class HitungBiayaUseCase(
    private val kendaraanDao: KendaraanDao,
    private val perjalananDao: PerjalananDao,
    private val hargaBbmDao: HargaBbmDao,
    private val hasilDao: HasilPerhitunganDao,
    private val wilayah: String = WILAYAH_DEFAULT
) {

    // =========================================================================
    // Tipe Hasil
    // =========================================================================

    /**
     * Hasil eksekusi use case ini.
     */
    sealed interface Result {

        /**
         * Perhitungan berhasil dan hasil disimpan ke Room.
         *
         * @property idHasil         UUID dari [HasilPerhitunganEntity] yang baru disimpan.
         * @property literDibutuhkan Kebutuhan bahan bakar, skala 3.
         * @property totalBiaya      Total biaya IDR, skala 2.
         * @property isStale         true jika harga BBM yang digunakan berusia >
         *                            [STALE_THRESHOLD_DAYS] hari; UI harus menampilkan
         *                            peringatan "harga mungkin sudah tidak terkini".
         * @property namaKendaraan   Nama kendaraan yang digunakan (untuk konfirmasi UI).
         * @property namaBbm         Nama jenis BBM yang dihitung (untuk konfirmasi UI).
         */
        data class Success(
            val idHasil: String,
            val literDibutuhkan: BigDecimal,
            val totalBiaya: BigDecimal,
            val isStale: Boolean,
            val namaKendaraan: String,
            val namaBbm: String
        ) : Result

        /**
         * Gagal karena data tidak ditemukan atau validasi input tidak lolos.
         */
        data class Failure(val reason: FailureReason) : Result
    }

    /**
     * Alasan kegagalan use case.
     */
    enum class FailureReason {
        /** ID perjalanan tidak ditemukan di Room atau sudah dihapus lunak. */
        PERJALANAN_TIDAK_DITEMUKAN,

        /** Tidak ada kendaraan yang tersedia (tidak ada default dan idKendaraan null). */
        KENDARAAN_TIDAK_DITEMUKAN,

        /** ID kendaraan spesifik tidak ditemukan di Room. */
        KENDARAAN_ID_TIDAK_VALID,

        /** Tidak ada catatan harga untuk kombinasi BBM + wilayah + tanggal. */
        HARGA_TIDAK_TERSEDIA,

        /** Validasi input [FuelCostEngine] gagal (jarak negatif, konsumsi nol, dll). */
        INPUT_TIDAK_VALID
    }

    // =========================================================================
    // Fungsi Eksekusi Utama
    // =========================================================================

    /**
     * Menjalankan alur perhitungan biaya BBM secara lengkap.
     *
     * Harus dipanggil dari coroutine (misalnya `viewModelScope.launch { ... }`).
     *
     * @param idPerjalanan         UUID perjalanan yang akan dihitung.
     * @param idBbm                UUID jenis BBM yang dipilih pengguna.
     * @param idKendaraan          UUID kendaraan yang dipilih, atau null untuk menggunakan
     *                             kendaraan default.
     * @param tanggalPerhitungan   Tanggal perhitungan dalam epoch milli-detik (UTC).
     *                             Default: [System.currentTimeMillis].
     *                             Harus menunjuk ke awal hari (00:00 UTC) untuk
     *                             konsistensi Price Resolver.
     * @return [Result.Success] dengan detail perhitungan jika berhasil,
     *         atau [Result.Failure] dengan keterangan kegagalan.
     */
    suspend fun execute(
        idPerjalanan: String,
        idBbm: String,
        idKendaraan: String? = null,
        tanggalPerhitungan: Long = System.currentTimeMillis()
    ): Result {

        // ── Langkah 1: Ambil Perjalanan ───────────────────────────────────────
        val perjalanan: PerjalananEntity = perjalananDao.getById(idPerjalanan)
            ?.takeIf { it.deletedAt == null }
            ?: return Result.Failure(FailureReason.PERJALANAN_TIDAK_DITEMUKAN)

        // ── Langkah 2: Ambil Kendaraan ────────────────────────────────────────
        val kendaraan: KendaraanEntity = resolveKendaraan(idKendaraan)
            ?: return Result.Failure(
                if (idKendaraan != null) FailureReason.KENDARAAN_ID_TIDAK_VALID
                else FailureReason.KENDARAAN_TIDAK_DITEMUKAN
            )

        // ── Langkah 3: Price Resolver ─────────────────────────────────────────
        val harga: HargaBbmEntity = hargaBbmDao.hargaTerkini(
            idBbm  = idBbm,
            wilayah = wilayah,
            tanggal = tanggalPerhitungan
        ) ?: return Result.Failure(FailureReason.HARGA_TIDAK_TERSEDIA)

        // ── Langkah 4: Eksekusi FuelCostEngine ───────────────────────────────
        val engineInput = FuelCostEngine.Input(
            jarakKm             = BigDecimal(perjalanan.jarakKm),
            konsumsiKmPerLiter  = BigDecimal(kendaraan.konsumsiKmPerLiter),
            hargaPerLiter       = BigDecimal(harga.hargaPerLiter)
        )

        val engineResult = FuelCostEngine.hitung(engineInput)
        if (engineResult is FuelCostEngine.Result.Failure) {
            return Result.Failure(FailureReason.INPUT_TIDAK_VALID)
        }
        val output = (engineResult as FuelCostEngine.Result.Success).output

        // ── Langkah 5: Tentukan penanda isStale ──────────────────────────────
        val isStale = isHargaStale(harga.berlakuMulai, tanggalPerhitungan)

        // ── Langkah 6: Simpan HasilPerhitungan ───────────────────────────────
        val now = System.currentTimeMillis()
        val idHasil = UUID.randomUUID().toString()

        val hasilEntity = HasilPerhitunganEntity(
            idHasil          = idHasil,
            idPerjalanan     = idPerjalanan,
            idKendaraan      = kendaraan.idKendaraan,
            idBbm            = idBbm,
            idHarga          = harga.idHarga,
            literDibutuhkan  = output.literDibutuhkan.toPlainString(),
            totalBiaya       = output.totalBiaya.toPlainString(),
            createdAt        = now,
            updatedAt        = now,
            deletedAt        = null,
            syncState        = SyncState.PENDING
        )

        // Penyimpanan dilakukan dalam satu transaksi Room via insert()
        // (OnConflictStrategy.REPLACE menjamin idempoten jika ada retry)
        hasilDao.insert(hasilEntity)

        return Result.Success(
            idHasil          = idHasil,
            literDibutuhkan  = output.literDibutuhkan,
            totalBiaya       = output.totalBiaya,
            isStale          = isStale,
            namaKendaraan    = kendaraan.namaKendaraan,
            namaBbm          = idBbm   // ViewModel dapat memetakan ini ke nama via JenisBbmDao
        )
    }

    // =========================================================================
    // Fungsi Bantu Privat
    // =========================================================================

    /**
     * Menentukan kendaraan yang akan digunakan.
     *
     * - Jika [idKendaraan] tidak null, cari kendaraan spesifik di Room.
     * - Jika null, ambil kendaraan default aktif ([KendaraanDao.observeDefault]).
     *   Karena use case bersifat suspend (bukan reaktif), kita gunakan [kotlinx.coroutines.flow.first]
     *   untuk mengambil nilai terbaru satu kali saja.
     *
     * @return [KendaraanEntity] yang aktif ([deletedAt] = null), atau null jika tidak ada.
     */
    private suspend fun resolveKendaraan(idKendaraan: String?): KendaraanEntity? {
        return if (idKendaraan != null) {
            kendaraanDao.getById(idKendaraan)
                ?.takeIf { it.deletedAt == null }
        } else {
            // Ambil nilai pertama dari Flow (non-blocking)
            kendaraanDao.observeDefault().first()
        }
    }

    /**
     * Memeriksa apakah catatan harga sudah "basi" (berusia > [STALE_THRESHOLD_DAYS]).
     *
     * @param berlakuMulai       Epoch milli-detik tanggal mulai berlaku harga.
     * @param tanggalPerhitungan Epoch milli-detik tanggal perhitungan.
     * @return true jika selisih > [STALE_THRESHOLD_DAYS] hari.
     */
    private fun isHargaStale(berlakuMulai: Long, tanggalPerhitungan: Long): Boolean {
        val selisihMs = tanggalPerhitungan - berlakuMulai
        val selisihHari = selisihMs / MS_PER_DAY
        return selisihHari > STALE_THRESHOLD_DAYS
    }

    companion object {
        /** Wilayah default yang didukung aplikasi. */
        const val WILAYAH_DEFAULT = "KALIMANTAN_TIMUR"

        /**
         * Ambang batas usia harga dalam hari sebelum dianggap "basi".
         * Jika harga terakhir lebih tua dari ini, UI menampilkan peringatan.
         * Dapat dikonfigurasi melalui konstruktor atau Remote Config di masa mendatang.
         * Sesuai PRD Bagian 5.5.
         */
        const val STALE_THRESHOLD_DAYS = 30L

        private const val MS_PER_DAY = 24L * 60 * 60 * 1000
    }
}
