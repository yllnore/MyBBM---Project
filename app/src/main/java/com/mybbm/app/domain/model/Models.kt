package com.mybbm.app.domain.model

import java.math.BigDecimal

data class VehicleModel(
    val id: String,
    val name: String,
    val type: String, // "Mobil" atau "Motor"
    val fuelEfficiency: BigDecimal, // km/L
    val isDefault: Boolean = false
)

data class FuelModel(
    val id: String,
    val name: String,
    val pricePerLiter: BigDecimal,
    val iconResId: Int? = null,
    val colorHex: String? = null
)

data class HistoryModel(
    val id: String,
    val origin: String,
    val destination: String,
    val distanceKm: BigDecimal,
    val totalCost: BigDecimal,
    val totalLiters: BigDecimal,
    val fuelName: String,
    val vehicleName: String,
    val formattedDate: String
)

data class SpbuInfo(
    val name: String,
    val distanceFormatted: String,
    val brand: String = "Pertamina"
)

data class CalculationResult(
    val originName: String,
    val destinationName: String,
    val vehicleName: String,
    val fuelType: String,
    val fuelEfficiency: BigDecimal,
    val pricePerLiter: BigDecimal,
    val distanceKm: BigDecimal,
    val totalLiters: BigDecimal,
    val totalCost: BigDecimal,
    val nearbySpbu: List<SpbuInfo> = emptyList()
)
