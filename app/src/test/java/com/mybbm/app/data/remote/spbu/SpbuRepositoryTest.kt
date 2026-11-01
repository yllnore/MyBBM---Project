package com.mybbm.app.data.remote.spbu

import com.mybbm.app.domain.engine.HaversineUtil
import com.mybbm.app.domain.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpbuRepositoryTest {

    private val repository = SpbuRepository()

    @Test
    fun testDetectBrand() {
        assertEquals("Pertamina", repository.detectBrand("SPBU Pertamina 61.751.02"))
        assertEquals("Shell", repository.detectBrand("SPBU Shell MT Haryono"))
        assertEquals("BP", repository.detectBrand("BP AKR Fuel Station"))
        assertEquals("Vivo", repository.detectBrand("SPBU Vivo Cilandak"))
        assertEquals("Pertamina", repository.detectBrand("SPBU Tanpa Merek"))
    }

    @Test
    fun testFormatDistance() {
        assertEquals("350 m", repository.formatDistance(0.35))
        assertEquals("1,2 km", repository.formatDistance(1.2))
        assertEquals("4,5 km", repository.formatDistance(4.53))
    }

    @Test
    fun testFormatSpbuName() {
        val nameWithRaw = repository.formatSpbuName("SPBU Juanda", "Pertamina", "", "Pertamina")
        assertEquals("SPBU Juanda", nameWithRaw)

        val nameFromBrand = repository.formatSpbuName("", "Shell", "", "Shell")
        assertEquals("SPBU Shell", nameFromBrand)

        val nameFromOperator = repository.formatSpbuName("", "", "AKR", "BP")
        assertEquals("SPBU AKR", nameFromOperator)

        val nameFallback = repository.formatSpbuName("", "", "", "Pertamina")
        assertEquals("SPBU Pertamina", nameFallback)
    }

    @Test
    fun testHaversineDistanceOrdering() {
        val origin = LatLng(-0.502106, 117.153709) // Polnes Samarinda
        val spbuNear = LatLng(-0.501092, 117.124156) // ~3.3 km
        val spbuFar = LatLng(-1.265000, 116.890000) // Balikpapan (~90 km)

        val distNear = HaversineUtil.hitungJarakKm(origin, spbuNear, faktorJalan = 1.0).toDouble()
        val distFar = HaversineUtil.hitungJarakKm(origin, spbuFar, faktorJalan = 1.0).toDouble()

        assertTrue(distNear < distFar)
        assertTrue(distNear < 5.0)
        assertTrue(distFar > 50.0)
    }
}
