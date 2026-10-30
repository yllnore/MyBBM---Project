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
}
