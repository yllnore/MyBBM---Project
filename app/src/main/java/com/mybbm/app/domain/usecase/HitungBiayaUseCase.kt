package com.mybbm.app.domain.usecase

import com.mybbm.app.data.remote.routing.OsrmRouteRepository
import com.mybbm.app.data.remote.routing.RouteData
import com.mybbm.app.domain.model.LatLng
import com.mybbm.app.util.FuelCalculator
import java.math.BigDecimal

/**
 * UseCase untuk menghitung estimasi rute dan biaya perjalanan
 * menggunakan OSRM API / Haversine dan FuelCalculator.
 */
class HitungBiayaUseCase(
    private val routeRepository: OsrmRouteRepository = OsrmRouteRepository()
) {

    data class Output(
        val distanceKm: BigDecimal,
        val totalLiters: BigDecimal,
        val totalCost: BigDecimal,
        val polylinePoints: List<LatLng>,
        val isFallback: Boolean
    )

    suspend fun execute(
        origin: LatLng,
        destination: LatLng,
        fuelEfficiencyKmPerL: BigDecimal,
        pricePerLiter: BigDecimal
    ): Output {
        val routeData: RouteData = routeRepository.getDrivingRoute(origin, destination)
        val fuelNeeded = FuelCalculator.calculateFuelNeeded(routeData.distanceKm, fuelEfficiencyKmPerL)
        val totalCost = FuelCalculator.calculateTotalCost(fuelNeeded, pricePerLiter)

        return Output(
            distanceKm = routeData.distanceKm,
            totalLiters = fuelNeeded,
            totalCost = totalCost,
            polylinePoints = routeData.polylinePoints,
            isFallback = routeData.isFallback
        )
    }
}

