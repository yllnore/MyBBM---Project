package com.mybbm.app.util

import com.mybbm.app.R
import org.junit.Assert.assertEquals
import org.junit.Test

class FuelIconResolverTest {

    @Test
    fun testResolvePertalite() {
        assertEquals(R.drawable.pertalite, FuelIconResolver.resolveIcon("Pertalite"))
        assertEquals(R.drawable.pertalite, FuelIconResolver.resolveIcon("PERTALITE"))
        assertEquals(R.drawable.pertalite, FuelIconResolver.resolveIcon("pertalite ro 90"))
    }

    @Test
    fun testResolvePertamaxTurbo() {
        assertEquals(R.drawable.pertamax_turbo, FuelIconResolver.resolveIcon("Pertamax Turbo"))
        assertEquals(R.drawable.pertamax_turbo, FuelIconResolver.resolveIcon("TURBO 98"))
    }

    @Test
    fun testResolveSolar() {
        assertEquals(R.drawable.solar, FuelIconResolver.resolveIcon("Solar"))
        assertEquals(R.drawable.solar, FuelIconResolver.resolveIcon("Bio Solar"))
        assertEquals(R.drawable.solar, FuelIconResolver.resolveIcon("Dexlite"))
        assertEquals(R.drawable.solar, FuelIconResolver.resolveIcon("Pertamina Dex"))
    }

    @Test
    fun testResolvePertamaxAndFallback() {
        assertEquals(R.drawable.pertamax, FuelIconResolver.resolveIcon("Pertamax"))
        assertEquals(R.drawable.pertamax, FuelIconResolver.resolveIcon("BBM Kustom Baru"))
        assertEquals(R.drawable.pertamax, FuelIconResolver.resolveIcon(""))
    }
}
