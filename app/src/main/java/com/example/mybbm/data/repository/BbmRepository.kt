package com.example.mybbm.data.repository

import com.example.mybbm.data.local.dao.FuelPriceDao
import com.example.mybbm.data.local.dao.HistoryDao
import com.example.mybbm.data.local.dao.VehicleDao
import com.example.mybbm.data.local.entity.FuelPriceEntity
import com.example.mybbm.data.local.entity.HistoryEntity
import com.example.mybbm.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

class BbmRepository(
    private val vehicleDao: VehicleDao,
    private val fuelPriceDao: FuelPriceDao,
    private val historyDao: HistoryDao
) {
    val allVehicles: Flow<List<VehicleEntity>> = vehicleDao.getAllVehicles()
    val allFuelPrices: Flow<List<FuelPriceEntity>> = fuelPriceDao.getAllFuelPrices()
    val allHistory: Flow<List<HistoryEntity>> = historyDao.getAllHistory()

    suspend fun insertVehicle(vehicle: VehicleEntity) {
        vehicleDao.insertVehicle(vehicle)
    }

    suspend fun updateVehicle(vehicle: VehicleEntity) {
        vehicleDao.updateVehicle(vehicle)
    }

    suspend fun deleteVehicle(vehicle: VehicleEntity) {
        vehicleDao.deleteVehicle(vehicle)
    }

    suspend fun updateFuelPrice(fuelPrice: FuelPriceEntity) {
        fuelPriceDao.insertFuelPrice(fuelPrice)
    }

    suspend fun getFuelPriceByType(fuelType: String): FuelPriceEntity? {
        return fuelPriceDao.getFuelPriceByType(fuelType)
    }

    suspend fun insertHistory(history: HistoryEntity): Long {
        return historyDao.insertHistory(history)
    }

    suspend fun deleteHistory(history: HistoryEntity) {
        historyDao.deleteHistory(history)
    }

    suspend fun clearAllHistory() {
        historyDao.clearAllHistory()
    }
}
