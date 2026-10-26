package com.example.mybbm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_prices")
data class FuelPriceEntity(
    @PrimaryKey
    val fuelType: String, // "Pertalite", "Pertamax", "Pertamax Turbo", "Dexlite", "Pertamina Dex"
    val pricePerLiter: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)
