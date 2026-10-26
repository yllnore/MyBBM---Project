package com.example.mybbm.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mybbm.data.local.MyBbmDatabase
import com.example.mybbm.data.local.entity.FuelPriceEntity
import com.example.mybbm.data.local.entity.HistoryEntity
import com.example.mybbm.data.local.entity.VehicleEntity
import com.example.mybbm.data.repository.BbmRepository
import com.example.mybbm.util.FuelCalculator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BbmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BbmRepository

    val vehicles: StateFlow<List<VehicleEntity>>
    val fuelPrices: StateFlow<List<FuelPriceEntity>>
    val history: StateFlow<List<HistoryEntity>>

    var originText = MutableStateFlow("")
    var destinationText = MutableStateFlow("")
    var selectedVehicle = MutableStateFlow<VehicleEntity?>(null)
    var selectedFuelPrice = MutableStateFlow<FuelPriceEntity?>(null)

    // Calculation result state
    var calculationResult = MutableStateFlow<CalculationResult?>(null)
    var errorMessage = MutableStateFlow<String?>(null)

    init {
        val database = MyBbmDatabase.getDatabase(application)
        repository = BbmRepository(database.vehicleDao(), database.fuelPriceDao(), database.historyDao())

        vehicles = repository.allVehicles
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        fuelPrices = repository.allFuelPrices
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        history = repository.allHistory
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Set default selected vehicle and fuel price when lists load
        viewModelScope.launch {
            vehicles.collect { list ->
                if (list.isNotEmpty() && selectedVehicle.value == null) {
                    selectedVehicle.value = list.first()
                }
            }
        }
        viewModelScope.launch {
            fuelPrices.collect { list ->
                if (list.isNotEmpty() && selectedFuelPrice.value == null) {
                    selectedFuelPrice.value = list.first()
                }
            }
        }
    }

    fun swapLocations() {
        val temp = originText.value
        originText.value = destinationText.value
        destinationText.value = temp
    }

    fun calculateTrip(distanceKm: Double = 45.0) { // default 45km if manual text or test
        val origin = originText.value.trim()
        val destination = destinationText.value.trim()
        val vehicle = selectedVehicle.value
        val fuelPrice = selectedFuelPrice.value

        if (origin.isEmpty() || destination.isEmpty()) {
            errorMessage.value = "Lokasi asal dan tujuan tidak boleh kosong."
            return
        }
        if (origin.equals(destination, ignoreCase = true)) {
            errorMessage.value = "Lokasi asal dan tujuan tidak boleh sama."
            return
        }
        if (vehicle == null) {
            errorMessage.value = "Silakan pilih kendaraan terlebih dahulu."
            return
        }
        if (fuelPrice == null) {
            errorMessage.value = "Silakan pilih jenis BBM terlebih dahulu."
            return
        }

        errorMessage.value = null

        val liters = FuelCalculator.calculateLiters(distanceKm, vehicle.fuelEfficiency)
        val cost = FuelCalculator.calculateCost(liters, fuelPrice.pricePerLiter)

        val result = CalculationResult(
            originName = origin,
            destinationName = destination,
            distanceKm = distanceKm,
            vehicleName = vehicle.name,
            fuelEfficiency = vehicle.fuelEfficiency,
            fuelType = fuelPrice.fuelType,
            pricePerLiter = fuelPrice.pricePerLiter,
            totalLiters = liters,
            totalCost = cost
        )
        calculationResult.value = result

        // Automatically save history
        viewModelScope.launch {
            repository.insertHistory(
                HistoryEntity(
                    originName = origin,
                    destinationName = destination,
                    distanceKm = distanceKm,
                    vehicleName = vehicle.name,
                    fuelEfficiency = vehicle.fuelEfficiency,
                    fuelType = fuelPrice.fuelType,
                    pricePerLiter = fuelPrice.pricePerLiter,
                    totalLiters = liters,
                    totalCost = cost
                )
            )
        }
    }

    fun addVehicle(name: String, type: String, efficiency: Double) {
        if (name.isBlank() || efficiency <= 0.0) return
        viewModelScope.launch {
            repository.insertVehicle(
                VehicleEntity(name = name, type = type, fuelEfficiency = efficiency, isDefault = false)
            )
        }
    }

    fun deleteVehicle(vehicle: VehicleEntity) {
        viewModelScope.launch {
            repository.deleteVehicle(vehicle)
        }
    }

    fun updateFuelPrice(fuelType: String, newPrice: Double) {
        if (newPrice < 0) return
        viewModelScope.launch {
            repository.updateFuelPrice(FuelPriceEntity(fuelType = fuelType, pricePerLiter = newPrice))
        }
    }

    fun deleteHistoryItem(historyItem: HistoryEntity) {
        viewModelScope.launch {
            repository.deleteHistory(historyItem)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }
}

data class CalculationResult(
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
