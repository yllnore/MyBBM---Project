package com.example.mybbm

import com.example.mybbm.util.FuelCalculator
import org.junit.Assert.assertEquals
import org.junit.Test

class FuelCalculatorTest {

    @Test
    fun testCalculateLiters() {
        val distance = 120.0
        val efficiency = 40.0
        val liters = FuelCalculator.calculateLiters(distance, efficiency)
        assertEquals(3.0, liters, 0.001)
    }

    @Test
    fun testCalculateCost() {
        val liters = 3.0
        val pricePerLiter = 10000.0
        val cost = FuelCalculator.calculateCost(liters, pricePerLiter)
        assertEquals(30000.0, cost, 0.001)
    }

    @Test
    fun testInvalidEfficiency() {
        val liters = FuelCalculator.calculateLiters(120.0, 0.0)
        assertEquals(0.0, liters, 0.001)
    }
}
