package com.mybbm.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mybbm.app.data.local.database.AppDatabase
import com.mybbm.app.data.local.entity.HargaBbmEntity
import com.mybbm.app.data.local.entity.KendaraanEntity
import com.mybbm.app.data.local.entity.SyncState
import com.mybbm.app.data.remote.maps.GoogleMapsRouteDataSource
import com.mybbm.app.domain.model.LatLng
import com.mybbm.app.domain.usecase.GetRouteResult
import com.mybbm.app.domain.usecase.GetRouteUseCase
import com.mybbm.app.domain.usecase.HitungBiayaUseCase
import com.mybbm.app.worker.SyncWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

/**
 * Model UI untuk data kendaraan di layar Composable.
 */
data class VehicleUiModel(
    val id: String,
    val name: String,
    val type: String,
    val fuelEfficiency: Double,
    val isDefault: Boolean
)

/**
 * Model UI untuk data harga referensi BBM.
 */
data class FuelPriceUiModel(
    val idHarga: String,
    val idBbm: String,
    val fuelType: String,
    val pricePerLiter: Double
)

/**
 * Model UI untuk baris riwayat perhitungan.
 */
data class HistoryUiModel(
    val id: String,
    val originName: String,
    val destinationName: String,
    val distanceKm: Double,
    val vehicleName: String,
    val fuelType: String,
    val totalLiters: Double,
    val totalCost: Double,
    val timestamp: Long
)

/**
 * Model UI untuk rincian hasil estimasi perjalanan.
 */
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

/**
 * ViewModel utama MyBBM yang menghubungkan antarmuka pengguna (Jetpack Compose)
 * dengan arsitektur domain (GetRouteUseCase, HitungBiayaUseCase) dan Room Database.
 */
class BbmViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getInstance(application)
    private val kendaraanDao = database.kendaraanDao()
    private val hargaBbmDao = database.hargaBbmDao()
    private val jenisBbmDao = database.jenisBbmDao()
    private val perjalananDao = database.perjalananDao()
    private val hasilPerhitunganDao = database.hasilPerhitunganDao()

    private val routeDataSource = GoogleMapsRouteDataSource()
    private val getRouteUseCase = GetRouteUseCase(perjalananDao, routeDataSource)
    private val hitungBiayaUseCase = HitungBiayaUseCase(
        kendaraanDao = kendaraanDao,
        perjalananDao = perjalananDao,
        hargaBbmDao = hargaBbmDao,
        hasilDao = hasilPerhitunganDao
    )

    // Daftar kendaraan yang tersimpan di Room
    val vehicles: StateFlow<List<VehicleUiModel>> = kendaraanDao.observeAll()
        .map { list ->
            list.map { k ->
                VehicleUiModel(
                    id = k.idKendaraan,
                    name = k.namaKendaraan,
                    type = k.jenisKendaraan,
                    fuelEfficiency = k.konsumsiKmPerLiter.toDoubleOrNull() ?: 12.0,
                    isDefault = k.isDefault
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Daftar harga BBM aktif untuk wilayah Kalimantan Timur
    val fuelPrices: StateFlow<List<FuelPriceUiModel>> = combine(
        hargaBbmDao.observeHargaAktif("KALIMANTAN_TIMUR"),
        jenisBbmDao.observeActive()
    ) { hargaList, jenisList ->
        val jenisMap = jenisList.associateBy { it.idBbm }
        hargaList.map { h ->
            val jenis = jenisMap[h.idBbm]
            FuelPriceUiModel(
                idHarga = h.idHarga,
                idBbm = h.idBbm,
                fuelType = jenis?.namaBbm ?: "BBM",
                pricePerLiter = h.hargaPerLiter.toDoubleOrNull() ?: 10000.0
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Riwayat perhitungan yang menggabungkan hasil, perjalanan, kendaraan, dan jenis BBM
    val history: StateFlow<List<HistoryUiModel>> = combine(
        hasilPerhitunganDao.observeAll(),
        perjalananDao.observeAll(),
        kendaraanDao.observeAll(),
        jenisBbmDao.observeActive()
    ) { hasilList, perjalananList, kendaraanList, jenisBbmList ->
        val perjalananMap = perjalananList.associateBy { it.idPerjalanan }
        val kendaraanMap = kendaraanList.associateBy { it.idKendaraan }
        val jenisBbmMap = jenisBbmList.associateBy { it.idBbm }

        hasilList.mapNotNull { hasil ->
            val perjalanan = perjalananMap[hasil.idPerjalanan]
            val kendaraan = kendaraanMap[hasil.idKendaraan]
            val jenisBbm = jenisBbmMap[hasil.idBbm]

            HistoryUiModel(
                id = hasil.idHasil,
                originName = perjalanan?.asalNama ?: "Asal",
                destinationName = perjalanan?.tujuanNama ?: "Tujuan",
                distanceKm = perjalanan?.jarakKm?.toDoubleOrNull() ?: 0.0,
                vehicleName = kendaraan?.namaKendaraan ?: "Kendaraan",
                fuelType = jenisBbm?.namaBbm ?: "BBM",
                totalLiters = hasil.literDibutuhkan.toDoubleOrNull() ?: 0.0,
                totalCost = hasil.totalBiaya.toDoubleOrNull() ?: 0.0,
                timestamp = hasil.createdAt
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Input form state
    val originText = MutableStateFlow("Samarinda")
    val destinationText = MutableStateFlow("Balikpapan")
    val selectedVehicle = MutableStateFlow<VehicleUiModel?>(null)
    val selectedFuelPrice = MutableStateFlow<FuelPriceUiModel?>(null)

    // Output and status state
    val calculationResult = MutableStateFlow<CalculationResult?>(null)
    val errorMessage = MutableStateFlow<String?>(null)
    val isLoading = MutableStateFlow(false)

    // Peta koordinat lokasi populer di Kalimantan Timur
    private val kaltimCoordinates = mapOf(
        "samarinda" to LatLng(-0.502106, 117.153709),
        "balikpapan" to LatLng(-1.265386, 116.831200),
        "bontang" to LatLng(0.133333, 117.500000),
        "tenggarong" to LatLng(-0.416667, 116.983333),
        "kutai kartanegara" to LatLng(-0.416667, 116.983333),
        "penajam" to LatLng(-1.328333, 116.758333),
        "sangatta" to LatLng(0.483333, 117.550000),
        "kutai timur" to LatLng(0.483333, 117.550000),
        "berau" to LatLng(2.166667, 117.500000),
        "tanah grogot" to LatLng(-1.883333, 116.216667),
        "paser" to LatLng(-1.883333, 116.216667)
    )

    init {
        // Inisialisasi kendaraan default awal jika database masih kosong
        viewModelScope.launch(Dispatchers.IO) {
            val existing = kendaraanDao.getLatestActive()
            if (existing == null) {
                val defaultId = UUID.randomUUID().toString()
                val now = System.currentTimeMillis()
                val defaultVehicle = KendaraanEntity(
                    idKendaraan = defaultId,
                    namaKendaraan = "Avanza / Xenia (Standar)",
                    jenisKendaraan = "MOBIL",
                    konsumsiKmPerLiter = "12.00",
                    isDefault = true,
                    createdAt = now,
                    updatedAt = now,
                    syncState = SyncState.PENDING
                )
                kendaraanDao.insert(defaultVehicle)
            }
        }

        // Otomatis pilih kendaraan pertama atau default saat list kendaraan dimuat
        viewModelScope.launch {
            vehicles.collect { list ->
                if (list.isNotEmpty() && selectedVehicle.value == null) {
                    selectedVehicle.value = list.firstOrNull { it.isDefault } ?: list.first()
                }
            }
        }

        // Otomatis pilih BBM pertama (cth: Pertalite) saat list harga dimuat
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

    private fun resolveCoordinates(input: String, isOrigin: Boolean): LatLng {
        val clean = input.trim().lowercase(Locale.ROOT)
        if (clean.contains(",")) {
            val parts = clean.split(",")
            if (parts.size == 2) {
                val lat = parts[0].trim().toDoubleOrNull()
                val lng = parts[1].trim().toDoubleOrNull()
                if (lat != null && lng != null) {
                    return LatLng(lat, lng)
                }
            }
        }
        for ((key, coord) in kaltimCoordinates) {
            if (clean.contains(key)) {
                return coord
            }
        }
        return if (isOrigin) {
            LatLng(-0.502106, 117.153709) // Default: Samarinda
        } else {
            LatLng(-1.265386, 116.831200) // Default: Balikpapan
        }
    }

    fun calculateTrip(onSuccess: (() -> Unit)? = null) {
        calculateTripInternal(manualDistanceKm = null, onSuccess = onSuccess)
    }

    fun calculateTrip(manualDistanceKm: Double, onSuccess: (() -> Unit)? = null) {
        calculateTripInternal(manualDistanceKm = manualDistanceKm, onSuccess = onSuccess)
    }

    private fun calculateTripInternal(manualDistanceKm: Double?, onSuccess: (() -> Unit)?) {
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
        isLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val originCoord = resolveCoordinates(origin, isOrigin = true)
                val destCoord = resolveCoordinates(destination, isOrigin = false)

                // 1. Eksekusi GetRouteUseCase (Directions API dengan fallback Haversine & cache Room)
                val routeResult = getRouteUseCase.execute(
                    origin = originCoord,
                    destination = destCoord,
                    originName = origin,
                    destinationName = destination
                )

                val perjalanan = when (routeResult) {
                    is GetRouteResult.Success -> routeResult.perjalanan
                    is GetRouteResult.RouteNotFound -> {
                        withContext(Dispatchers.Main) {
                            errorMessage.value = routeResult.message
                            isLoading.value = false
                        }
                        return@launch
                    }
                    is GetRouteResult.Error -> {
                        withContext(Dispatchers.Main) {
                            errorMessage.value = routeResult.message
                            isLoading.value = false
                        }
                        return@launch
                    }
                }

                // Jika ada manualDistanceKm yang dipaksakan (misal untuk testing spesifik)
                if (manualDistanceKm != null) {
                    perjalananDao.update(
                        perjalanan.copy(
                            jarakKm = String.format(Locale.US, "%.2f", manualDistanceKm)
                        )
                    )
                }

                // 2. Eksekusi HitungBiayaUseCase (Price Resolver, BigDecimal math, dan simpan hasil snapshot)
                val hitungResult = hitungBiayaUseCase.execute(
                    idPerjalanan = perjalanan.idPerjalanan,
                    idBbm = fuelPrice.idBbm,
                    idKendaraan = vehicle.id
                )

                when (hitungResult) {
                    is HitungBiayaUseCase.Result.Success -> {
                        val distance = manualDistanceKm ?: (perjalanan.jarakKm.toDoubleOrNull() ?: 0.0)
                        val liters = hitungResult.literDibutuhkan.toDouble()
                        val cost = hitungResult.totalBiaya.toDouble()

                        val result = CalculationResult(
                            originName = origin,
                            destinationName = destination,
                            distanceKm = distance,
                            vehicleName = vehicle.name,
                            fuelEfficiency = vehicle.fuelEfficiency,
                            fuelType = fuelPrice.fuelType,
                            pricePerLiter = fuelPrice.pricePerLiter,
                            totalLiters = liters,
                            totalCost = cost
                        )

                        // 3. Picu background sync satu kali ke Supabase
                        try {
                            SyncWorker.enqueueOneTimeSync(getApplication())
                        } catch (e: Exception) {
                            android.util.Log.w("BbmViewModel", "Gagal memicu SyncWorker: ${e.message}")
                        }

                        withContext(Dispatchers.Main) {
                            calculationResult.value = result
                            isLoading.value = false
                            errorMessage.value = null
                            onSuccess?.invoke()
                        }
                    }
                    is HitungBiayaUseCase.Result.Failure -> {
                        withContext(Dispatchers.Main) {
                            errorMessage.value = "Perhitungan gagal: ${hitungResult.reason.name}"
                            isLoading.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage.value = "Terjadi kesalahan: ${e.message}"
                    isLoading.value = false
                }
            }
        }
    }

    fun addVehicle(name: String, type: String, efficiency: Double) {
        if (name.isBlank() || efficiency <= 0.0) return
        viewModelScope.launch(Dispatchers.IO) {
            val id = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()
            val hasDefault = kendaraanDao.observeDefault().firstOrNull() != null
            val entity = KendaraanEntity(
                idKendaraan = id,
                namaKendaraan = name,
                jenisKendaraan = if (type.uppercase() in listOf("MOBIL", "MOTOR", "TRUK", "LAINNYA")) type.uppercase() else "MOBIL",
                konsumsiKmPerLiter = String.format(Locale.US, "%.2f", efficiency),
                isDefault = !hasDefault,
                createdAt = now,
                updatedAt = now,
                syncState = SyncState.PENDING
            )
            kendaraanDao.insert(entity)
            try {
                SyncWorker.enqueueOneTimeSync(getApplication())
            } catch (e: Exception) {
                android.util.Log.w("BbmViewModel", "Gagal memicu SyncWorker: ${e.message}")
            }
        }
    }

    fun deleteVehicle(vehicle: VehicleUiModel) {
        viewModelScope.launch(Dispatchers.IO) {
            val now = System.currentTimeMillis()
            kendaraanDao.softDelete(vehicle.id, now)
            if (vehicle.isDefault) {
                val next = kendaraanDao.getLatestActive()
                if (next != null) {
                    kendaraanDao.setDefault(next.idKendaraan, now)
                }
            }
            try {
                SyncWorker.enqueueOneTimeSync(getApplication())
            } catch (e: Exception) {
                android.util.Log.w("BbmViewModel", "Gagal memicu SyncWorker: ${e.message}")
            }
        }
    }

    fun updateFuelPrice(fuelType: String, newPrice: Double) {
        if (newPrice < 0) return
        viewModelScope.launch(Dispatchers.IO) {
            val jenis = jenisBbmDao.getActive().find { it.namaBbm.equals(fuelType, ignoreCase = true) }
            if (jenis != null) {
                val now = System.currentTimeMillis()
                val currentHarga = hargaBbmDao.hargaTerkini(jenis.idBbm, "KALIMANTAN_TIMUR", now)
                if (currentHarga != null) {
                    val updated = currentHarga.copy(
                        hargaPerLiter = String.format(Locale.US, "%.2f", newPrice),
                        updatedAt = now
                    )
                    hargaBbmDao.insertAll(listOf(updated))
                } else {
                    val newHarga = HargaBbmEntity(
                        idHarga = UUID.randomUUID().toString(),
                        idBbm = jenis.idBbm,
                        wilayah = "KALIMANTAN_TIMUR",
                        hargaPerLiter = String.format(Locale.US, "%.2f", newPrice),
                        berlakuMulai = now,
                        berlakuHingga = null,
                        updatedAt = now
                    )
                    hargaBbmDao.insertAll(listOf(newHarga))
                }
            }
        }
    }

    fun deleteHistoryItem(historyItem: HistoryUiModel) {
        viewModelScope.launch(Dispatchers.IO) {
            hasilPerhitunganDao.softDelete(historyItem.id, System.currentTimeMillis())
            try {
                SyncWorker.enqueueOneTimeSync(getApplication())
            } catch (e: Exception) {
                android.util.Log.w("BbmViewModel", "Gagal memicu SyncWorker: ${e.message}")
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentHistory = history.value
            val now = System.currentTimeMillis()
            currentHistory.forEach { item ->
                hasilPerhitunganDao.softDelete(item.id, now)
            }
            try {
                SyncWorker.enqueueOneTimeSync(getApplication())
            } catch (e: Exception) {
                android.util.Log.w("BbmViewModel", "Gagal memicu SyncWorker: ${e.message}")
            }
        }
    }
}
