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
}
