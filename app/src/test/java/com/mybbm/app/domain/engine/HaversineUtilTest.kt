package com.mybbm.app.domain.engine

import com.mybbm.app.domain.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class HaversineUtilTest {

    @Test
    fun testHitungJarakKm_samarindaKeBalikpapan() {
        val samarinda = LatLng(-0.502106, 117.153709)
        val balikpapan = LatLng(-1.265386, 116.831200)

        val jarakKm = HaversineUtil.hitungJarakKm(samarinda, balikpapan)
        // Jarak lurus ~92 km, dengan faktor jalan 1.25 -> ~115 km
        assertTrue(jarakKm > BigDecimal("100.0"))
        assertTrue(jarakKm < BigDecimal("130.0"))
    }

    @Test
    fun testHitungJarakKm_titikSama() {
        val titik = LatLng(-0.502106, 117.153709)
        val jarakKm = HaversineUtil.hitungJarakKm(titik, titik)
        assertEquals(BigDecimal("0.00"), jarakKm)
    }
}

