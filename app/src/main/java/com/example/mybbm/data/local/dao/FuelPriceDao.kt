package com.example.mybbm.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.mybbm.data.local.entity.FuelPriceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelPriceDao {
    @Query("SELECT * FROM fuel_prices")
    fun getAllFuelPrices(): Flow<List<FuelPriceEntity>>

    @Query("SELECT * FROM fuel_prices WHERE fuelType = :fuelType")
    suspend fun getFuelPriceByType(fuelType: String): FuelPriceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelPrice(fuelPrice: FuelPriceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelPrices(fuelPrices: List<FuelPriceEntity>)

    @Query("SELECT COUNT(*) FROM fuel_prices")
    suspend fun getFuelPriceCount(): Int
}
