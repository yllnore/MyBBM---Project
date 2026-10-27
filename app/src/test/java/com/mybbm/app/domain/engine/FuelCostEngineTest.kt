package com.mybbm.app.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

/**
 * Pengujian unit untuk [FuelCostEngine] berdasarkan kasus uji acuan PRD Bagian 5.4.
 */
class FuelCostEngineTest {

    @Test
    fun kasusUjiAcuan1() {
        val input = FuelCostEngine.Input(
            jarakKm = BigDecimal("120.00"),
            konsumsiKmPerLiter = BigDecimal("40.00"),
            hargaPerLiter = BigDecimal("10000.00")
        )
        val result = FuelCostEngine.hitung(input)
        assertTrue(result is FuelCostEngine.Result.Success)
        val output = (result as FuelCostEngine.Result.Success).output
        assertEquals(BigDecimal("3.000"), output.literDibutuhkan)
        assertEquals(BigDecimal("30000.00"), output.totalBiaya)
    }

    @Test
    fun kasusUjiAcuan2() {
        val input = FuelCostEngine.Input(
            jarakKm = BigDecimal("125.50"),
            konsumsiKmPerLiter = BigDecimal("12.00"),
            hargaPerLiter = BigDecimal("12500.00")
        )
        val result = FuelCostEngine.hitung(input)
        assertTrue(result is FuelCostEngine.Result.Success)
        val output = (result as FuelCostEngine.Result.Success).output
        assertEquals(BigDecimal("10.458"), output.literDibutuhkan)
        assertEquals(BigDecimal("130729.17"), output.totalBiaya)
    }

    @Test
    fun kasusUjiAcuan3() {
        val input = FuelCostEngine.Input(
            jarakKm = BigDecimal("0.00"),
            konsumsiKmPerLiter = BigDecimal("15.00"),
            hargaPerLiter = BigDecimal("10000.00")
        )
        val result = FuelCostEngine.hitung(input)
        assertTrue(result is FuelCostEngine.Result.Success)
        val output = (result as FuelCostEngine.Result.Success).output
        assertEquals(BigDecimal("0.000"), output.literDibutuhkan)
        assertEquals(BigDecimal("0.00"), output.totalBiaya)
    }

    @Test
    fun kasusUjiAcuan4() {
        val input = FuelCostEngine.Input(
            jarakKm = BigDecimal("1.00"),
            konsumsiKmPerLiter = BigDecimal("3.00"),
            hargaPerLiter = BigDecimal("10000.00")
        )
        val result = FuelCostEngine.hitung(input)
        assertTrue(result is FuelCostEngine.Result.Success)
        val output = (result as FuelCostEngine.Result.Success).output
        assertEquals(BigDecimal("0.333"), output.literDibutuhkan)
        assertEquals(BigDecimal("3333.33"), output.totalBiaya)
    }

    @Test
    fun validasiJarakNegatif() {
        val input = FuelCostEngine.Input(
            jarakKm = BigDecimal("-5.00"),
            konsumsiKmPerLiter = BigDecimal("40.00"),
            hargaPerLiter = BigDecimal("10000.00")
        )
        val result = FuelCostEngine.hitung(input)
        assertTrue(result is FuelCostEngine.Result.Failure)
        assertEquals(FuelCostEngine.CalcError.JARAK_NEGATIF, (result as FuelCostEngine.Result.Failure).error)
    }

    @Test
    fun validasiKonsumsiNol() {
        val input = FuelCostEngine.Input(
            jarakKm = BigDecimal("10.00"),
            konsumsiKmPerLiter = BigDecimal("0.00"),
            hargaPerLiter = BigDecimal("10000.00")
        )
        val result = FuelCostEngine.hitung(input)
        assertTrue(result is FuelCostEngine.Result.Failure)
        assertEquals(FuelCostEngine.CalcError.KONSUMSI_TIDAK_VALID, (result as FuelCostEngine.Result.Failure).error)
    }
}
