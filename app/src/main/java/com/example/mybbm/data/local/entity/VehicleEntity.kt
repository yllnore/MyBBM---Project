package com.example.mybbm.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class VehicleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val type: String, // "Sepeda Motor" atau "Mobil"
    val fuelEfficiency: Double, // km/L
    val isDefault: Boolean = false
)
