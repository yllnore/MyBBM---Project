package com.example.mybbm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trip_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val originName: String,
    val destinationName: String,
    val distanceKm: Double,
    val vehicleName: String,
    val fuelEfficiency: Double,
    val fuelType: String,
    val pricePerLiter: Double,
    val totalLiters: Double,
    val totalCost: Double
)
