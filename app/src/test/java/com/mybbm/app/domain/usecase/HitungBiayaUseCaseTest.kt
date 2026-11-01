package com.mybbm.app.domain.usecase

import com.mybbm.app.domain.model.LatLng
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class HitungBiayaUseCaseTest {

    private val useCase = HitungBiayaUseCase()

    @Test
    fun testExecute_menghitungBiayaDenganBenar() = runBlocking {
        val samarinda = LatLng(-0.502106, 117.153709)
        val balikpapan = LatLng(-1.265386, 116.831200)

        // Efisiensi: 10 km/L, Harga: Rp 10.000 / Liter
        val output = useCase.execute(
            origin = samarinda,
            destination = balikpapan,
            fuelEfficiencyKmPerL = BigDecimal("10.0"),
            pricePerLiter = BigDecimal("10000")
        )

        assertNotNull(output)
        assertTrue(output.distanceKm > BigDecimal.ZERO)
        assertTrue(output.totalLiters > BigDecimal.ZERO)
        assertTrue(output.totalCost > BigDecimal.ZERO)
        assertTrue(output.polylinePoints.isNotEmpty())
    }
}

