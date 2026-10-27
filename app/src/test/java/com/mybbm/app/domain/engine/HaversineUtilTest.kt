package com.mybbm.app.domain.engine

import com.mybbm.app.domain.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class HaversineUtilTest {

    @Test
    fun titikSamaMenghasilkanJarakNol() {
        val titik = LatLng(-0.502106, 117.153709) // Samarinda
        val jarak = HaversineUtil.hitungJarakKm(titik, titik)
        assertEquals(BigDecimal("0.00"), jarak)
    }

    @Test
    fun estimasiSamarindaKeBalikpapan() {
        // Samarinda (-0.502106, 117.153709) ke Balikpapan (-1.265386, 116.831200)
        // Garis lurus ~ 92 km. Dengan faktor 1.3 ~ 119-120 km.
        val samarinda = LatLng(-0.502106, 117.153709)
        val balikpapan = LatLng(-1.265386, 116.831200)

        val jarakEstimasi = HaversineUtil.hitungJarakKm(samarinda, balikpapan)

        assertTrue(jarakEstimasi > BigDecimal("100.00"))
        assertTrue(jarakEstimasi < BigDecimal("150.00"))
    }
}
