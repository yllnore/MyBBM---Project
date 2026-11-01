package com.mybbm.app.domain.model

data class VehicleModel(
    val id: String,
    val name: String,
    val type: String, // "Mobil" atau "Motor"
    val fuelEfficiency: java.math.BigDecimal, // km/L
    val isDefault: Boolean = false
)

data class FuelModel(
    val id: String,
    val name: String,
    val pricePerLiter: java.math.BigDecimal,
    val iconResId: Int? = null,
    val colorHex: String? = null
)

data class HistoryModel(
    val id: String,
    val origin: String,
    val destination: String,
    val distanceKm: java.math.BigDecimal,
    val totalCost: java.math.BigDecimal,
    val totalLiters: java.math.BigDecimal,
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
    val fuelEfficiency: java.math.BigDecimal,
    val pricePerLiter: java.math.BigDecimal,
    val distanceKm: java.math.BigDecimal,
    val totalLiters: java.math.BigDecimal,
    val totalCost: java.math.BigDecimal,
    val routePoints: List<LatLng> = emptyList(),
    val nearbySpbu: List<SpbuInfo> = emptyList()
)
