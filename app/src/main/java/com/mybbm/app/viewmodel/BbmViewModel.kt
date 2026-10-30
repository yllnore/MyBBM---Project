package com.mybbm.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mybbm.app.R
import com.mybbm.app.data.local.database.AppDatabase
import com.mybbm.app.data.local.entity.FuelEntity
import com.mybbm.app.data.local.entity.HistoryEntity
import com.mybbm.app.data.local.entity.VehicleEntity
import com.mybbm.app.domain.model.CalculationResult
import com.mybbm.app.domain.model.FuelModel
import com.mybbm.app.domain.model.HistoryModel
import com.mybbm.app.domain.model.SpbuInfo
import com.mybbm.app.domain.model.VehicleModel
import com.mybbm.app.util.FuelCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.abs

enum class AppScreen {
    HOME,
    RESULT,
    SETTINGS,
    ABOUT,
    VEHICLES,
    FUELS,
    HISTORI,
    INPUT_ORIGIN,
    INPUT_DESTINATION
}

class BbmViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val vehicleDao = db.vehicleDao()
    private val fuelDao = db.fuelDao()
    private val historyDao = db.historyDao()

    // Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Form Input State (Awal masuk kosong sesuai instruksi & Figma)
    private val _origin = MutableStateFlow("")
    val origin: StateFlow<String> = _origin.asStateFlow()

    private val _destination = MutableStateFlow("")
    val destination: StateFlow<String> = _destination.asStateFlow()

    private val _selectedVehicle = MutableStateFlow<VehicleModel?>(null)
    val selectedVehicle: StateFlow<VehicleModel?> = _selectedVehicle.asStateFlow()

    private val _selectedFuel = MutableStateFlow<FuelModel?>(null)
    val selectedFuel: StateFlow<FuelModel?> = _selectedFuel.asStateFlow()

    // Calculation Result
    private val _calculationResult = MutableStateFlow<CalculationResult?>(null)
    val calculationResult: StateFlow<CalculationResult?> = _calculationResult.asStateFlow()

    // Toast message (e.g. "Berhasil dihapus!", "Berhasil ditambahkan!")
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Dialog state
    val showAddVehicleDialog = MutableStateFlow(false)
    val showEditVehicleDialog = MutableStateFlow(false)
    val showDeleteVehicleDialog = MutableStateFlow(false)
    val vehicleToEditOrDelete = MutableStateFlow<VehicleModel?>(null)

    val showAddFuelDialog = MutableStateFlow(false)
    val showEditFuelDialog = MutableStateFlow(false)
    val showDeleteFuelDialog = MutableStateFlow(false)
    val fuelToEditOrDelete = MutableStateFlow<FuelModel?>(null)

    val showDeleteHistoryDialog = MutableStateFlow(false)
    val historyToDelete = MutableStateFlow<HistoryModel?>(null)

    // Data Streams from Room
    val vehicles: StateFlow<List<VehicleModel>> = vehicleDao.getAllVehicles()
        .map { list ->
            list.map {
                VehicleModel(
                    id = it.id,
                    name = it.nama,
                    type = it.jenis,
                    fuelEfficiency = it.konsumsiBbm,
                    isDefault = it.isDefault
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val fuels: StateFlow<List<FuelModel>> = fuelDao.getAllFuels()
        .map { list ->
            list.map {
                val iconRes = when (it.nama.uppercase()) {
                    "PERTAMAX" -> R.drawable.pertamax
                    "PERTAMAX TURBO" -> R.drawable.pertamax_turbo
                    "SOLAR" -> R.drawable.solar
                    "PERTALITE" -> R.drawable.pertalite
                    else -> R.drawable.pertamax
                }
                FuelModel(
                    id = it.id,
                    name = it.nama,
                    pricePerLiter = it.harga,
                    iconResId = iconRes,
                    colorHex = it.warnaHex
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val histories: StateFlow<List<HistoryModel>> = historyDao.getAllHistories()
        .map { list ->
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            list.map {
                HistoryModel(
                    id = it.id,
                    origin = it.lokasiAsal,
                    destination = it.lokasiTujuan,
                    distanceKm = it.jarakKm,
                    totalCost = it.totalBiaya,
                    totalLiters = it.totalLiter,
                    fuelName = it.namaBbm,
                    vehicleName = it.namaKendaraan,
                    formattedDate = sdf.format(Date(it.createdAt))
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Awal masuk kosong (tidak auto-select agar sesuai Figma)


    // Navigation methods
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun navigateBack() {
        _currentScreen.value = AppScreen.HOME
    }

    // Form selection
    fun setOrigin(value: String) {
        _origin.value = value
    }

    fun setDestination(value: String) {
        _destination.value = value
    }

    fun selectVehicle(vehicle: VehicleModel) {
        _selectedVehicle.value = vehicle
    }

    fun selectFuel(fuel: FuelModel) {
        _selectedFuel.value = fuel
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    private fun estimateDistanceKm(origin: String, destination: String): BigDecimal {
        val o = origin.lowercase()
        val d = destination.lowercase()
        return when {
            (o.contains("polnes") && d.contains("bsb")) || (d.contains("polnes") && o.contains("bsb")) -> BigDecimal("115.0")
            (o.contains("samarinda") && d.contains("balikpapan")) || (d.contains("samarinda") && o.contains("balikpapan")) -> BigDecimal("115.0")
            o.contains("seberang") || d.contains("seberang") -> BigDecimal("12.5")
            o.contains("manggar") || d.contains("manggar") -> BigDecimal("22.0")
            o.contains("kemaritiman") || d.contains("kemaritiman") -> BigDecimal("4.2")
            o.contains("auditorium") || d.contains("auditorium") -> BigDecimal("1.5")
            else -> {
                val hash = abs((origin + destination).hashCode()) % 45 + 8
                BigDecimal(hash.toString() + ".0")
            }
        }
    }

    // Calculation action matching Figma demo
    fun calculateTripCost() {
        val vehicle = _selectedVehicle.value ?: VehicleModel("1", "HONDA", "Mobil", BigDecimal("15.0"))
        val fuel = _selectedFuel.value ?: FuelModel("1", "PERTAMAX", BigDecimal("12500"))

        val distanceKm = estimateDistanceKm(_origin.value, _destination.value)
        val liters = FuelCalculator.calculateFuelNeeded(distanceKm, vehicle.fuelEfficiency)
        val cost = FuelCalculator.calculateTotalCost(liters, fuel.pricePerLiter)

        val nearby = listOf(
            SpbuInfo("SPBU Pertamina", "1,2 km", "Pertamina"),
            SpbuInfo("SPBU Shell", "2,1 km", "Shell"),
            SpbuInfo("SPBU Pertamina KM 4", "3,5 km", "Pertamina")
        )

        val result = CalculationResult(
            originName = _origin.value,
            destinationName = _destination.value,
            vehicleName = vehicle.name,
            fuelType = fuel.name,
            fuelEfficiency = vehicle.fuelEfficiency,
            pricePerLiter = fuel.pricePerLiter,
            distanceKm = distanceKm,
            totalLiters = liters,
            totalCost = cost,
            nearbySpbu = nearby
        )
        _calculationResult.value = result

        // Save into history automatically
        viewModelScope.launch {
            val history = HistoryEntity(
                id = UUID.randomUUID().toString(),
                lokasiAsal = _origin.value,
                lokasiTujuan = _destination.value,
                jarakKm = distanceKm,
                namaKendaraan = vehicle.name,
                namaBbm = fuel.name,
                totalLiter = liters,
                totalBiaya = cost
            )
            historyDao.insertHistory(history)
        }

        navigateTo(AppScreen.RESULT)
    }

    // CRUD Vehicle
    fun addVehicle(name: String, type: String, efficiency: BigDecimal) {
        viewModelScope.launch {
            val entity = VehicleEntity(
                id = UUID.randomUUID().toString(),
                nama = name.uppercase().trim(),
                jenis = type,
                konsumsiBbm = efficiency
            )
            vehicleDao.insertVehicle(entity)
            _toastMessage.value = "Berhasil ditambahkan!"
            showAddVehicleDialog.value = false
        }
    }

    fun updateVehicle(id: String, name: String, type: String, efficiency: BigDecimal) {
        viewModelScope.launch {
            val entity = VehicleEntity(
                id = id,
                nama = name.uppercase().trim(),
                jenis = type,
                konsumsiBbm = efficiency
            )
            vehicleDao.updateVehicle(entity)
            _toastMessage.value = "Berhasil diperbarui!"
            showEditVehicleDialog.value = false
        }
    }

    fun deleteVehicle(id: String) {
        viewModelScope.launch {
            vehicleDao.deleteVehicleById(id)
            _toastMessage.value = "Berhasil dihapus!"
            showDeleteVehicleDialog.value = false
        }
    }

    // CRUD Fuel
    fun addFuel(name: String, price: BigDecimal) {
        viewModelScope.launch {
            val entity = FuelEntity(
                id = UUID.randomUUID().toString(),
                nama = name.uppercase().trim(),
                harga = price
            )
            fuelDao.insertFuel(entity)
            _toastMessage.value = "Berhasil ditambahkan!"
            showAddFuelDialog.value = false
        }
    }

    fun updateFuel(id: String, name: String, price: BigDecimal) {
        viewModelScope.launch {
            val entity = FuelEntity(
                id = id,
                nama = name.uppercase().trim(),
                harga = price
            )
            fuelDao.updateFuel(entity)
            _toastMessage.value = "Berhasil diperbarui!"
            showEditFuelDialog.value = false
        }
    }

    fun deleteFuel(id: String) {
        viewModelScope.launch {
            fuelDao.deleteFuelById(id)
            _toastMessage.value = "Berhasil dihapus!"
            showDeleteFuelDialog.value = false
        }
    }

    // CRUD History
    fun deleteHistory(id: String) {
        viewModelScope.launch {
            historyDao.deleteHistoryById(id)
            _toastMessage.value = "Berhasil dihapus!"
            showDeleteHistoryDialog.value = false
        }
    }
}
