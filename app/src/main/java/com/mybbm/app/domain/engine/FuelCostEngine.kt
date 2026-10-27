package com.mybbm.app.domain.engine

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

/**
 * Core Engine perhitungan biaya BBM.
 *
 * **Sepenuhnya murni Kotlin** — tidak bergantung pada Android SDK, Google Maps SDK,
 * maupun jaringan, sehingga seluruh logika dapat diuji dengan unit test biasa (JVM).
 *
 * ## Rumus
 * ```
 * Kebutuhan Liter = Jarak (km) / Konsumsi Kendaraan (km/L)
 * Total Biaya     = Kebutuhan Liter (penuh) × Harga BBM (per liter)
 * ```
 *
 * ## Spesifikasi Presisi (PRD Bagian 5.2)
 * | Nilai            | Skala | Mode Pembulatan |
 * |-----------------|-------|-----------------|
 * | Jarak (km)      | 2     | HALF_UP         |
 * | Konsumsi (km/L) | 2     | HALF_UP         |
 * | Liter           | 3     | HALF_UP (via MathContext 12 digit sebelum dibulatkan) |
 * | Harga per liter | 2     | HALF_UP         |
 * | Total biaya     | 2     | HALF_UP         |
 *
 * **Catatan penting:** [Output.totalBiaya] dihitung dari liter **sebelum** dibulatkan ke 3
 * desimal ([literPenuh]) untuk mencegah akumulasi galat. Baru kemudian keduanya dibulatkan
 * dan disimpan dalam [Output].
 */
object FuelCostEngine {

    /**
     * [MathContext] untuk operasi pembagian.
     *
     * Menggunakan presisi 12 digit signifikan dengan [RoundingMode.HALF_UP] agar hasil
     * pembagian tidak kehilangan ketelitian sebelum pembulatan akhir ke 3 desimal.
     * Sesuai spesifikasi PRD Bagian 5.2.
     */
    private val DIV_CONTEXT = MathContext(12, RoundingMode.HALF_UP)

    /**
     * Batas maksimum jarak yang diizinkan (5.000 km).
     * Melebihi nilai ini dianggap sebagai input tidak wajar.
     */
    private val MAX_JARAK_KM = BigDecimal("5000")

    // =========================================================================
    // Data Classes
    // =========================================================================

    /**
     * Input untuk fungsi [hitung].
     *
     * Semua nilai harus sudah dalam bentuk [BigDecimal] dengan presisi yang sesuai;
     * gunakan [BigDecimal] dari String (bukan dari Double) untuk menghindari galat
     * representasi floating-point.
     *
     * @property jarakKm            Jarak perjalanan dalam kilometer (≥ 0 dan ≤ 5.000).
     * @property konsumsiKmPerLiter Rasio konsumsi kendaraan dalam km per liter (> 0 dan ≤ 100).
     * @property hargaPerLiter      Harga BBM per liter dalam IDR (> 0).
     */
    data class Input(
        val jarakKm: BigDecimal,
        val konsumsiKmPerLiter: BigDecimal,
        val hargaPerLiter: BigDecimal
    )

    /**
     * Output dari fungsi [hitung] apabila tidak ada kesalahan.
     *
     * @property literDibutuhkan Kebutuhan bahan bakar dalam liter, skala 3, dibulatkan HALF_UP.
     * @property totalBiaya      Total biaya dalam IDR, skala 2, dibulatkan HALF_UP.
     *                            Dihitung dari liter *sebelum* dibulatkan untuk menghindari
     *                            akumulasi galat pembulatan bertingkat.
     */
    data class Output(
        val literDibutuhkan: BigDecimal,   // skala 3
        val totalBiaya: BigDecimal         // skala 2
    )

    /**
     * Jenis kesalahan yang dapat terjadi saat validasi input.
     */
    enum class CalcError {
        /** [Input.jarakKm] bernilai negatif. */
        JARAK_NEGATIF,

        /** [Input.jarakKm] melebihi batas 5.000 km. */
        JARAK_TERLALU_BESAR,

        /** [Input.konsumsiKmPerLiter] ≤ 0 atau > 100. */
        KONSUMSI_TIDAK_VALID,

        /** [Input.hargaPerLiter] ≤ 0. */
        HARGA_TIDAK_VALID,

        /** Tidak ada catatan harga BBM yang tersedia untuk BBM dan tanggal yang dipilih. */
        HARGA_TIDAK_TERSEDIA
    }

    /**
     * Hasil operasi [hitung]; mengikuti pola sealed interface agar pemanggil
     * wajib menangani kedua kemungkinan secara eksplisit melalui `when`.
     */
    sealed interface Result {
        /** Perhitungan berhasil. */
        data class Success(val output: Output) : Result

        /** Perhitungan gagal karena validasi tidak lolos. */
        data class Failure(val error: CalcError) : Result
    }

    // =========================================================================
    // Fungsi Utama
    // =========================================================================

    /**
     * Menghitung kebutuhan liter dan total biaya BBM untuk sebuah perjalanan.
     *
     * ## Urutan Validasi
     * 1. [Input.jarakKm] tidak boleh negatif → [CalcError.JARAK_NEGATIF]
     * 2. [Input.jarakKm] tidak boleh melebihi 5.000 km → [CalcError.JARAK_TERLALU_BESAR]
     * 3. [Input.konsumsiKmPerLiter] harus > 0 dan ≤ 100 → [CalcError.KONSUMSI_TIDAK_VALID]
     * 4. [Input.hargaPerLiter] harus > 0 → [CalcError.HARGA_TIDAK_VALID]
     *
     * ## Contoh Kasus Uji Acuan (PRD Bagian 5.4)
     * | Jarak   | Konsumsi | Harga       | Liter  | Total        |
     * |---------|----------|-------------|--------|--------------|
     * | 120,00  | 40,00    | 10.000,00   | 3,000  | 30.000,00    |
     * | 125,50  | 12,00    | 12.500,00   | 10,458 | 130.729,17   |
     * | 0,00    | 15,00    | 10.000,00   | 0,000  | 0,00         |
     * | 1,00    | 3,00     | 10.000,00   | 0,333  | 3.333,33     |
     *
     * @param input Data input perhitungan.
     * @return [Result.Success] dengan [Output] jika valid, atau [Result.Failure] jika tidak.
     */
    fun hitung(input: Input): Result {
        // ── Validasi ──────────────────────────────────────────────────────────
        if (input.jarakKm < BigDecimal.ZERO)
            return Result.Failure(CalcError.JARAK_NEGATIF)

        if (input.jarakKm > MAX_JARAK_KM)
            return Result.Failure(CalcError.JARAK_TERLALU_BESAR)

        if (input.konsumsiKmPerLiter <= BigDecimal.ZERO ||
            input.konsumsiKmPerLiter > BigDecimal("100")
        ) return Result.Failure(CalcError.KONSUMSI_TIDAK_VALID)

        if (input.hargaPerLiter <= BigDecimal.ZERO)
            return Result.Failure(CalcError.HARGA_TIDAK_VALID)

        // ── Perhitungan ───────────────────────────────────────────────────────
        // literPenuh: belum dibulatkan, presisi 12 digit via DIV_CONTEXT
        val literPenuh = input.jarakKm.divide(input.konsumsiKmPerLiter, DIV_CONTEXT)

        // totalPenuh: dikalkulasi dari liter PENUH (bukan dari liter yang sudah dibulatkan)
        // agar tidak terjadi akumulasi galat pembulatan bertingkat
        val totalPenuh = literPenuh.multiply(input.hargaPerLiter)

        return Result.Success(
            Output(
                literDibutuhkan = literPenuh.setScale(3, RoundingMode.HALF_UP),
                totalBiaya      = totalPenuh.setScale(2, RoundingMode.HALF_UP)
            )
        )
    }
}
