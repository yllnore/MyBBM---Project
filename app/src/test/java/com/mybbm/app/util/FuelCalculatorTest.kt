package com.mybbm.app.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class FuelCalculatorTest {

    @Test
    fun testCalculateFuelNeeded() {
        val distance = BigDecimal("120.0")
        val efficiency = BigDecimal("40.0")
        val liters = FuelCalculator.calculateFuelNeeded(distance, efficiency)
        assertEquals(BigDecimal("3.00"), liters)
    }

    @Test
    fun testCalculateTotalCost() {
        val liters = BigDecimal("3.00")
        val pricePerLiter = BigDecimal("10000")
        val cost = FuelCalculator.calculateTotalCost(liters, pricePerLiter)
        assertEquals(BigDecimal("30000"), cost)
    }

    @Test
    fun testInvalidEfficiency() {
        val liters = FuelCalculator.calculateFuelNeeded(BigDecimal("120.0"), BigDecimal.ZERO)
        assertEquals(BigDecimal.ZERO, liters)
    }

    @Test
    fun testFormatThousand() {
        assertEquals("1.000", FuelCalculator.formatThousand(1000L))
        assertEquals("10.000", FuelCalculator.formatThousand(10000L))
        assertEquals("12.500", FuelCalculator.formatThousand(12500L))
        assertEquals("6.800", FuelCalculator.formatThousand(6800L))
        assertEquals("1.000.000", FuelCalculator.formatThousand(1000000L))
    }

    @Test
    fun testFormatEfficiency() {
        // Angka 1000 diformat menjadi 1.000
        assertEquals("1.000", FuelCalculator.formatEfficiency(BigDecimal("1000")))
        assertEquals("1.000", FuelCalculator.formatEfficiency(BigDecimal("1000.0")))
        assertEquals("1.000.5", FuelCalculator.formatEfficiency(BigDecimal("1000.5")))
        // Desimal standar dipertahankan
        assertEquals("12.5", FuelCalculator.formatEfficiency(BigDecimal("12.5")))
        assertEquals("14.0", FuelCalculator.formatEfficiency(BigDecimal("14.0")))
        assertEquals("55.0", FuelCalculator.formatEfficiency(BigDecimal("55.0")))
    }

    @Test
    fun testParseEfficiency() {
        // "1.000" harus di-parse menjadi 1000, bukan 1.0!
        assertEquals(BigDecimal("1000"), FuelCalculator.parseEfficiency("1.000"))
        assertEquals(BigDecimal("1000"), FuelCalculator.parseEfficiency("1000"))
        assertEquals(BigDecimal("10000"), FuelCalculator.parseEfficiency("10.000"))
        assertEquals(BigDecimal("12.5"), FuelCalculator.parseEfficiency("12.5"))
        assertEquals(BigDecimal("12.5"), FuelCalculator.parseEfficiency("12,5"))
        assertEquals(BigDecimal("1000.5"), FuelCalculator.parseEfficiency("1.000,5"))
        assertEquals(BigDecimal("14.0"), FuelCalculator.parseEfficiency("14.0"))
    }

    @Test
    fun testFormatDigitsAsThousand() {
        assertEquals("1.000", FuelCalculator.formatDigitsAsThousand("1000"))
        assertEquals("10.000", FuelCalculator.formatDigitsAsThousand("10000"))
        assertEquals("12.500", FuelCalculator.formatDigitsAsThousand("12500"))
        assertEquals("6.800", FuelCalculator.formatDigitsAsThousand("6800"))
    }

    @Test
    fun testFormatThousandInput() {
        assertEquals("1.000", FuelCalculator.formatThousandInput("1000"))
        assertEquals("10.000", FuelCalculator.formatThousandInput("10000"))
        assertEquals("12.5", FuelCalculator.formatThousandInput("12.5"))
        assertEquals("12,5", FuelCalculator.formatThousandInput("12,5"))
    }

    @Test
    fun testParseThousand() {
        assertEquals(BigDecimal("1000"), FuelCalculator.parseThousand("1.000"))
        assertEquals(BigDecimal("10000"), FuelCalculator.parseThousand("10.000"))
        assertEquals(BigDecimal("12500"), FuelCalculator.parseThousand("12.500"))
        assertEquals(BigDecimal("6800"), FuelCalculator.parseThousand("6.800"))
    }

    @Test
    fun testValidationEdgeCasesForDialogInputs() {
        // Input kosong / spasi / simbol tidak valid harus null
        assertEquals(null, FuelCalculator.parseEfficiency(""))
        assertEquals(null, FuelCalculator.parseEfficiency("   "))
        assertEquals(null, FuelCalculator.parseEfficiency("."))
        assertEquals(null, FuelCalculator.parseEfficiency(","))
        assertEquals(null, FuelCalculator.parseThousand(""))
        assertEquals(null, FuelCalculator.parseThousand("   "))

        // Nilai nol (0 / 0.0 / 000) harus <= BigDecimal.ZERO agar ditolak validasi dialog
        val zeroEff = FuelCalculator.parseEfficiency("0")
        val zeroDecEff = FuelCalculator.parseEfficiency("0.0")
        val zeroPrice = FuelCalculator.parseThousand("000")
        org.junit.Assert.assertTrue(zeroEff != null && zeroEff <= BigDecimal.ZERO)
        org.junit.Assert.assertTrue(zeroDecEff != null && zeroDecEff <= BigDecimal.ZERO)
        org.junit.Assert.assertTrue(zeroPrice != null && zeroPrice <= BigDecimal.ZERO)
    }

    @Test
    fun testCalculateFormattedCursorOffset() {
        // Mengetik di akhir: 900 -> 9000 -> "9.000", kursor harus di paling belakang (5)
        assertEquals(5, FuelCalculator.calculateFormattedCursorOffset("9000", 4, "9.000"))
        // Menghapus dari akhir: "9.00" -> "900", kursor di paling belakang (3)
        assertEquals(3, FuelCalculator.calculateFormattedCursorOffset("9.00", 4, "900"))
        // Mengetik di tengah: "9.5000" (kursor setelah '5' di indeks 3) -> "95.000", kursor setelah '5' (indeks 2)
        assertEquals(2, FuelCalculator.calculateFormattedCursorOffset("9.5000", 3, "95.000"))
        // Desimal koma: "12,35" (kursor di akhir) -> indeks 5
        assertEquals(5, FuelCalculator.calculateFormattedCursorOffset("12,35", 5, "12,35"))
        // Input kosong
        assertEquals(0, FuelCalculator.calculateFormattedCursorOffset("", 0, ""))
    }
}
