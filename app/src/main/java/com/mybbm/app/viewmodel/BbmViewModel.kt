package com.mybbm.app.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mybbm.app.R
import com.mybbm.app.data.local.database.AppDatabase
import com.mybbm.app.data.local.entity.BbmKustomEntity
import com.mybbm.app.data.local.entity.KendaraanKustomEntity
import com.mybbm.app.data.local.entity.RiwayatEntity
import com.mybbm.app.data.remote.location.LocationRepository
import com.mybbm.app.data.remote.routing.OsrmRouteRepository
import com.mybbm.app.data.remote.spbu.SpbuRepository
import com.mybbm.app.data.remote.supabase.SupabaseDefaultRepository
import com.mybbm.app.domain.engine.HaversineUtil
import com.mybbm.app.domain.model.CalculationResult
import com.mybbm.app.domain.model.FuelModel
import com.mybbm.app.domain.model.HistoryModel
import com.mybbm.app.domain.model.LatLng
import com.mybbm.app.domain.model.LocationSearchResult
import com.mybbm.app.domain.model.SearchSuggestionItem
import com.mybbm.app.domain.model.VehicleModel
import com.mybbm.app.domain.usecase.HitungBiayaUseCase
import com.mybbm.app.util.DeviceLocationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

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

    // 1. Offline Storage (Room Database - Data Milik Pengguna)
    private val db = AppDatabase.getInstance(application)
    private val kendaraanKustomDao = db.kendaraanKustomDao()
    private val bbmKustomDao = db.bbmKustomDao()
    private val riwayatDao = db.riwayatDao()

    // 2. Online Read-Only (Supabase Dataset Bawaan Pabrik)
    private val supabaseDefaultRepository = SupabaseDefaultRepository()

    // Layanan remote & domain lainnya
    private val hitungBiayaUseCase = HitungBiayaUseCase()
    private val locationRepository = LocationRepository()
    private val osrmRouteRepository = OsrmRouteRepository()
    private val spbuRepository = SpbuRepository()

    // State Dataset Default dari Supabase (dengan data bawaan pabrik instan)
    private val _defaultKendaraan = MutableStateFlow<List<VehicleModel>>(SupabaseDefaultRepository.FACTORY_DEFAULT_VEHICLES)
    private val _defaultBbm = MutableStateFlow<List<FuelModel>>(SupabaseDefaultRepository.FACTORY_DEFAULT_FUELS)

    // Location Autocomplete Search State
    private val _locationQueryFlow = MutableStateFlow("")
    private val _searchResults = MutableStateFlow<List<LocationSearchResult>>(emptyList())
    val searchResults: StateFlow<List<LocationSearchResult>> = _searchResults.asStateFlow()

    // State tunggal penampung seluruh hasil saran autocomplete (Lokasi, Kendaraan, atau BBM)
    private val _activeSearchList = MutableStateFlow<List<SearchSuggestionItem>>(emptyList())
    val activeSearchList: StateFlow<List<SearchSuggestionItem>> = _activeSearchList.asStateFlow()

    private val _isSearchingLocation = MutableStateFlow(false)
    val isSearchingLocation: StateFlow<Boolean> = _isSearchingLocation.asStateFlow()

    private val _isDetectingLocation = MutableStateFlow(false)
    val isDetectingLocation: StateFlow<Boolean> = _isDetectingLocation.asStateFlow()

    // Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Form Input State
    private val _origin = MutableStateFlow("")
    val origin: StateFlow<String> = _origin.asStateFlow()
    val lokasiAwal: StateFlow<String> get() = origin

    private val _destination = MutableStateFlow("")
    val destination: StateFlow<String> = _destination.asStateFlow()
    val lokasiTujuan: StateFlow<String> get() = destination

    private val _selectedVehicle = MutableStateFlow<VehicleModel?>(null)
    val selectedVehicle: StateFlow<VehicleModel?> = _selectedVehicle.asStateFlow()

    private val _selectedFuel = MutableStateFlow<FuelModel?>(null)
    val selectedFuel: StateFlow<FuelModel?> = _selectedFuel.asStateFlow()

    // Map Camera Persistence Across Navigation
    private val _homeMapCenter = MutableStateFlow<LatLng?>(null)
    val homeMapCenter: StateFlow<LatLng?> = _homeMapCenter.asStateFlow()

    private val _homeMapZoom = MutableStateFlow<Double?>(null)
    val homeMapZoom: StateFlow<Double?> = _homeMapZoom.asStateFlow()

    private val _resultMapCenter = MutableStateFlow<LatLng?>(null)
    val resultMapCenter: StateFlow<LatLng?> = _resultMapCenter.asStateFlow()

    private val _resultMapZoom = MutableStateFlow<Double?>(null)
    val resultMapZoom: StateFlow<Double?> = _resultMapZoom.asStateFlow()

    fun updateHomeMapCamera(center: LatLng, zoom: Double) {
        _homeMapCenter.value = center
        _homeMapZoom.value = zoom
    }

    fun updateResultMapCamera(center: LatLng, zoom: Double) {
        _resultMapCenter.value = center
        _resultMapZoom.value = zoom
    }

    // Koordinat Asal & Tujuan untuk peta OSM
    private val _originCoord = MutableStateFlow<LatLng?>(null)
    val originCoord: StateFlow<LatLng?> = _originCoord.asStateFlow()

    private val _destinationCoord = MutableStateFlow<LatLng?>(null)
    val destinationCoord: StateFlow<LatLng?> = _destinationCoord.asStateFlow()

    // Rute polyline points untuk digambar di peta & estimasi jarak OSRM
    private val _routePoints = MutableStateFlow<List<LatLng>>(emptyList())
    val routePoints: StateFlow<List<LatLng>> = _routePoints.asStateFlow()

    private val _estimatedDistanceKm = MutableStateFlow<BigDecimal?>(null)
    val estimatedDistanceKm: StateFlow<BigDecimal?> = _estimatedDistanceKm.asStateFlow()

    // Calculation Result
    private val _calculationResult = MutableStateFlow<CalculationResult?>(null)
    val calculationResult: StateFlow<CalculationResult?> = _calculationResult.asStateFlow()

    // Toast message
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
    val showDeleteAllHistoryDialog = MutableStateFlow(false)

    // Flow Data Kustom Pengguna dari Room (Offline)
    private val customVehiclesFlow: Flow<List<VehicleModel>> = kendaraanKustomDao.getAllKendaraanKustom()
        .map { list ->
            list.map {
                VehicleModel(
                    id = it.id,
                    name = it.nama,
                    type = it.tipe,
                    fuelEfficiency = BigDecimal.valueOf(it.rasioKonsumsi),
                    isDefault = false
                )
            }
        }

    private val customFuelsFlow: Flow<List<FuelModel>> = bbmKustomDao.getAllBbmKustom()
        .map { list ->
            list.map {
                val iconRes = it.iconResId ?: when (it.nama.uppercase()) {
                    "PERTAMAX" -> R.drawable.pertamax
                    "PERTAMAX TURBO" -> R.drawable.pertamax_turbo
                    "SOLAR" -> R.drawable.solar
                    "PERTALITE" -> R.drawable.pertalite
                    else -> R.drawable.pertamax
                }
                FuelModel(
                    id = it.id,
                    name = it.nama,
                    pricePerLiter = BigDecimal.valueOf(it.harga),
                    iconResId = iconRes,
                    colorHex = it.warnaHex
                )
            }
        }

    // 3. PENGGABUNGAN DATA (Hybrid Online + Offline) menggunakan `combine` Flow
    val vehicles: StateFlow<List<VehicleModel>> = combine(_defaultKendaraan, customVehiclesFlow) { defaults, customs ->
        defaults + customs
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SupabaseDefaultRepository.FACTORY_DEFAULT_VEHICLES)

    val fuels: StateFlow<List<FuelModel>> = combine(_defaultBbm, customFuelsFlow) { defaults, customs ->
        defaults + customs
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SupabaseDefaultRepository.FACTORY_DEFAULT_FUELS)

    // Riwayat Perjalanan (100% Offline Room)
    val histories: StateFlow<List<HistoryModel>> = riwayatDao.getAllRiwayat()
        .map { list ->
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            list.map {
                HistoryModel(
                    id = it.id,
                    origin = it.lokasiAsal,
                    destination = it.lokasiTujuan,
                    distanceKm = BigDecimal.valueOf(it.jarakKm),
                    totalCost = BigDecimal.valueOf(it.totalBiaya),
                    totalLiters = BigDecimal.valueOf(it.totalLiter),
                    fuelName = it.namaBbm,
                    vehicleName = it.namaKendaraan,
                    formattedDate = sdf.format(Date(it.timestamp)),
                    originLat = it.originLat,
                    originLng = it.originLng,
                    destLat = it.destLat,
                    destLng = it.destLng
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Kamus koordinat kota dan titik penting di Kalimantan Timur
    private val locationCoordinates = mapOf(
        "polnes" to LatLng(-0.536500, 117.128500),
        "politeknik negeri samarinda" to LatLng(-0.536500, 117.128500),
        "samarinda" to LatLng(-0.502106, 117.153709),
        "samarinda seberang" to LatLng(-0.525000, 117.123000),
        "big mall" to LatLng(-0.520556, 117.120556),
        "jembatan mahakam" to LatLng(-0.518889, 117.128611),
        "unmul" to LatLng(-0.468889, 117.156111),
        "universitas mulawarman" to LatLng(-0.468889, 117.156111),
        "balikpapan" to LatLng(-1.265386, 116.831200),
        "bsb" to LatLng(-1.278333, 116.858333),
        "balikpapan superblock" to LatLng(-1.278333, 116.858333),
        "pantai manggar" to LatLng(-1.218889, 116.981944),
        "bandara sepinggan" to LatLng(-1.268333, 116.894444),
        "bontang" to LatLng(0.133333, 117.500000),
        "tenggarong" to LatLng(-0.416667, 116.983333),
        "penajam" to LatLng(-1.328333, 116.758333)
    )

    init {
        loadDefaultDatasetsFromSupabase()
        setupLocationSearchFlow()
    }

    @OptIn(FlowPreview::class)
    private fun setupLocationSearchFlow() {
        viewModelScope.launch {
            _locationQueryFlow
                .debounce(500L)
                .distinctUntilChanged()
                .collectLatest { query ->
                    val trimmed = query.trim()
                    if (trimmed.length < 2) {
                        _searchResults.value = emptyList()
                        _activeSearchList.value = _activeSearchList.value.filterNot { it is SearchSuggestionItem.Location }
                        _isSearchingLocation.value = false
                    } else {
                        _isSearchingLocation.value = true
                        try {
                            val results = locationRepository.searchLocation(trimmed)
                            if (_locationQueryFlow.value.trim().length >= 2) {
                                _searchResults.value = results
                                _activeSearchList.value = _activeSearchList.value.filterNot { it is SearchSuggestionItem.Location } + results.map { SearchSuggestionItem.Location(it) }
                            }
                        } catch (e: Exception) {
                            _searchResults.value = emptyList()
                            _activeSearchList.value = _activeSearchList.value.filterNot { it is SearchSuggestionItem.Location }
                        } finally {
                            _isSearchingLocation.value = false
                        }
                    }
                }
        }
    }

    /**
     * Memuat dataset bawaan pabrik dari Supabase (Online Read-Only)
     * saat ViewModel pertama kali dibuat.
     */
    fun loadDefaultDatasetsFromSupabase() {
        viewModelScope.launch(Dispatchers.IO) {
            val fuelsData = supabaseDefaultRepository.getDefaultBbm()
            _defaultBbm.value = fuelsData

            val vehiclesData = supabaseDefaultRepository.getDefaultKendaraan()
            _defaultKendaraan.value = vehiclesData
        }
    }

    private val _previousScreen = MutableStateFlow<AppScreen?>(null)
    val previousScreen: StateFlow<AppScreen?> = _previousScreen.asStateFlow()

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            _previousScreen.value = _currentScreen.value
            _currentScreen.value = screen
        }
    }

    fun navigateBack() {
        val prev = _previousScreen.value
        if (prev != null && prev != AppScreen.SETTINGS && prev != AppScreen.ABOUT) {
            _currentScreen.value = prev
            _previousScreen.value = AppScreen.HOME
        } else {
            _currentScreen.value = AppScreen.HOME
        }
    }

    /**
     * Mengosongkan seluruh form input kalkulasi perjalanan (Lokasi Awal, Lokasi Tujuan,
     * Kendaraan, Jenis BBM, koordinat rute, dan hasil kalkulasi) sehingga pengguna
     * memulai dari awal saat kembali ke beranda.
     */
    fun resetTripForm() {
        _origin.value = ""
        _destination.value = ""
        _selectedVehicle.value = null
        _selectedFuel.value = null
        _originCoord.value = null
        _destinationCoord.value = null
        _routePoints.value = emptyList()
        _estimatedDistanceKm.value = null
        _calculationResult.value = null
        _searchResults.value = emptyList()
        _activeSearchList.value = emptyList()
        _isSearchingLocation.value = false
        _isDetectingLocation.value = false
        _resultMapCenter.value = null
        _resultMapZoom.value = null
    }

    fun setOrigin(value: String) {
        _origin.value = value
        _originCoord.value = resolveCoordinate(value, isOrigin = true)
        updateRoutePreview()
    }

    fun setOriginText(value: String) {
        _origin.value = value
    }

    /**
     * Memilih lokasi asal langsung dari hasil pencarian Nominatim.
     * Mengambil lat & lon, menyimpannya ke state, dan jika kedua koordinat terisi
     * otomatis memanggil kalkulasi rute OSRM untuk mendapatkan jarak (km).
     */
    fun selectOriginResult(result: LocationSearchResult) {
        _origin.value = result.title
        _originCoord.value = LatLng(result.lat, result.lon)
        clearSearchResults()
        checkAndCalculateOsrmRoute()
    }

    /**
     * Memilih lokasi asal secara eksplisit dengan nama dan koordinat.
     */
    fun selectOrigin(name: String, coord: LatLng) {
        _origin.value = name
        _originCoord.value = coord
        clearSearchResults()
        checkAndCalculateOsrmRoute()
    }

    fun setDestination(value: String) {
        _destination.value = value
        _destinationCoord.value = resolveCoordinate(value, isOrigin = false)
        updateRoutePreview()
    }

    fun setDestinationText(value: String) {
        _destination.value = value
    }

    /**
     * Memilih lokasi tujuan langsung dari hasil pencarian Nominatim.
     * Mengambil lat & lon, menyimpannya ke state, dan jika kedua koordinat terisi
     * otomatis memanggil kalkulasi rute OSRM untuk mendapatkan jarak (km).
     */
    fun selectDestinationResult(result: LocationSearchResult) {
        _destination.value = result.title
        _destinationCoord.value = LatLng(result.lat, result.lon)
        clearSearchResults()
        checkAndCalculateOsrmRoute()
    }

    /**
     * Memilih lokasi tujuan secara eksplisit dengan nama dan koordinat.
     */
    fun selectDestination(name: String, coord: LatLng) {
        _destination.value = name
        _destinationCoord.value = coord
        clearSearchResults()
        checkAndCalculateOsrmRoute()
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    /**
     * Melakukan reverse geocoding untuk mendapatkan alamat dari koordinat LatLng.
     */
    suspend fun getAddressFromCoordinates(latLng: LatLng): String? {
        return try {
            locationRepository.reverseGeocode(latLng.lat, latLng.lng)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Mengambil koordinat GPS fisik pengguna saat ini dan melakukan
     * reverse geocoding via OpenStreetMap Nominatim untuk mendapatkan nama jalan/wilayah riil.
     */
    fun useCurrentLocation(context: Context, onLocationResolved: (String, LatLng) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            _isDetectingLocation.value = true
            try {
                val latLng = DeviceLocationHelper.getCurrentLocation(context)
                if (latLng != null) {
                    val roadAddress = locationRepository.reverseGeocode(latLng.lat, latLng.lng)
                    val locationTitle = roadAddress ?: "Lokasi Saat Ini"

                    _origin.value = locationTitle
                    _originCoord.value = latLng
                    clearSearchResults()
                    checkAndCalculateOsrmRoute()
                    onLocationResolved(locationTitle, latLng)
                } else {
                    val fallbackCoord = LatLng(-0.502106, 117.153709)
                    val fallbackTitle = "POLNES - Samarinda"
                    _origin.value = fallbackTitle
                    _originCoord.value = fallbackCoord
                    clearSearchResults()
                    checkAndCalculateOsrmRoute()
                    onLocationResolved(fallbackTitle, fallbackCoord)
                }
            } catch (e: Exception) {
                val fallbackCoord = LatLng(-0.502106, 117.153709)
                val fallbackTitle = "POLNES - Samarinda"
                _origin.value = fallbackTitle
                _originCoord.value = fallbackCoord
                clearSearchResults()
                onLocationResolved(fallbackTitle, fallbackCoord)
            } finally {
                _isDetectingLocation.value = false
            }
        }
    }

    /**
     * Memanggil pencarian lokasi ke OpenStreetMap Nominatim API menggunakan Flow debounce 500ms.
     */
    fun searchLocation(query: String) {
        _locationQueryFlow.value = query
    }

    /**
     * Memfilter daftar kendaraan lokal dan mengisi state tunggal `activeSearchList`.
     */
    fun filterVehicles(query: String) {
        _locationQueryFlow.value = ""
        _isSearchingLocation.value = false
        val allVehicles = if (vehicles.value.isNotEmpty()) {
            vehicles.value
        } else if (_defaultKendaraan.value.isNotEmpty()) {
            _defaultKendaraan.value
        } else {
            SupabaseDefaultRepository.FACTORY_DEFAULT_VEHICLES
        }
        val trimmed = query.trim()
        val filtered = if (trimmed.isBlank() || trimmed.equals(_selectedVehicle.value?.name, ignoreCase = true)) {
            allVehicles
        } else {
            allVehicles.filter {
                it.name.contains(trimmed, ignoreCase = true) ||
                it.type.contains(trimmed, ignoreCase = true)
            }
        }
        _activeSearchList.value = filtered.map { SearchSuggestionItem.Vehicle(it) }
    }

    /**
     * Memfilter daftar jenis BBM lokal dan mengisi state tunggal `activeSearchList`.
     */
    fun filterFuels(query: String) {
        _locationQueryFlow.value = ""
        _isSearchingLocation.value = false
        val allFuels = if (fuels.value.isNotEmpty()) {
            fuels.value
        } else if (_defaultBbm.value.isNotEmpty()) {
            _defaultBbm.value
        } else {
            SupabaseDefaultRepository.FACTORY_DEFAULT_FUELS
        }
        val trimmed = query.trim()
        val filtered = if (trimmed.isBlank() || trimmed.equals(_selectedFuel.value?.name, ignoreCase = true)) {
            allFuels
        } else {
            allFuels.filter {
                it.name.contains(trimmed, ignoreCase = true)
            }
        }
        _activeSearchList.value = filtered.map { SearchSuggestionItem.Fuel(it) }
    }

    fun clearSearchResults() {
        _locationQueryFlow.value = ""
        _searchResults.value = emptyList()
        _activeSearchList.value = _activeSearchList.value.filterNot { it is SearchSuggestionItem.Location }
        _isSearchingLocation.value = false
    }

    /**
     * Menghitung rute & jarak OSRM secara otomatis jika koordinat asal & tujuan telah lengkap.
     */
    fun checkAndCalculateOsrmRoute() {
        val o = _originCoord.value ?: return
        val d = _destinationCoord.value ?: return
        viewModelScope.launch {
            try {
                val route = osrmRouteRepository.getDrivingRoute(o, d)
                _routePoints.value = route.polylinePoints
                _estimatedDistanceKm.value = route.distanceKm
            } catch (_: Exception) {
                val dist = HaversineUtil.hitungJarakKm(o, d)
                _estimatedDistanceKm.value = dist
                _routePoints.value = listOf(o, d)
            }
        }
    }

    fun updateRoutePreview() {
        checkAndCalculateOsrmRoute()
    }

    fun selectVehicle(vehicle: VehicleModel?) {
        _selectedVehicle.value = vehicle
    }

    fun selectFuel(fuel: FuelModel?) {
        _selectedFuel.value = fuel
    }

    fun setVehicleQuery(name: String) {
        val trimmed = name.trim()
        val allVehicles = if (vehicles.value.isNotEmpty()) vehicles.value else _defaultKendaraan.value
        val match = allVehicles.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
        _selectedVehicle.value = match
    }

    fun setFuelQuery(name: String) {
        val trimmed = name.trim()
        val allFuels = if (fuels.value.isNotEmpty()) fuels.value else _defaultBbm.value
        val match = allFuels.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
        _selectedFuel.value = match
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    private fun resolveCoordinate(input: String, isOrigin: Boolean): LatLng {
        val clean = input.trim().lowercase(Locale.ROOT)
        for ((key, coord) in locationCoordinates) {
            if (clean.contains(key)) {
                return coord
            }
        }
        return if (isOrigin) {
            LatLng(-0.502106, 117.153709) // Default: Samarinda / Polnes
        } else {
            LatLng(-1.265386, 116.831200) // Default: Balikpapan
        }
    }

    /**
     * Menjalankan kalkulasi rute via OSRM API dan menghitung estimasi biaya BBM.
     * Hasil kalkulasi disimpan ke Room Database (Offline RiwayatEntity).
     */
    fun calculateTripCost() {
        val vehicle = _selectedVehicle.value ?: VehicleModel("1", "HONDA", "Mobil", BigDecimal("15.0"))
        val fuel = _selectedFuel.value ?: FuelModel("1", "PERTAMAX", BigDecimal("12500"))

        val originCoord = _originCoord.value ?: resolveCoordinate(_origin.value, isOrigin = true)
        val destCoord = _destinationCoord.value ?: resolveCoordinate(_destination.value, isOrigin = false)

        viewModelScope.launch {
            val output = hitungBiayaUseCase.execute(
                origin = originCoord,
                destination = destCoord,
                fuelEfficiencyKmPerL = vehicle.fuelEfficiency,
                pricePerLiter = fuel.pricePerLiter
            )

            _routePoints.value = output.polylinePoints

            val result = CalculationResult(
                originName = _origin.value.ifBlank { "Samarinda (Polnes)" },
                destinationName = _destination.value.ifBlank { "Balikpapan" },
                vehicleName = vehicle.name,
                fuelType = fuel.name,
                fuelEfficiency = vehicle.fuelEfficiency,
                pricePerLiter = fuel.pricePerLiter,
                distanceKm = output.distanceKm,
                totalLiters = output.totalLiters,
                totalCost = output.totalCost,
                routePoints = output.polylinePoints,
                nearbySpbu = emptyList()
            )
            _calculationResult.value = result

            // Simpan otomatis ke Room Riwayat (100% Offline) dengan koordinat presisi asli
            val riwayat = RiwayatEntity(
                id = UUID.randomUUID().toString(),
                lokasiAsal = result.originName,
                lokasiTujuan = result.destinationName,
                jarakKm = output.distanceKm.toDouble(),
                totalBiaya = output.totalCost.toDouble(),
                timestamp = System.currentTimeMillis(),
                totalLiter = output.totalLiters.toDouble(),
                namaKendaraan = vehicle.name,
                namaBbm = fuel.name,
                originLat = originCoord.lat,
                originLng = originCoord.lng,
                destLat = destCoord.lat,
                destLng = destCoord.lng
            )
            riwayatDao.insertRiwayat(riwayat)

            navigateTo(AppScreen.RESULT)

            // Mengambil daftar SPBU terdekat riil menggunakan Overpass API
            viewModelScope.launch(Dispatchers.IO) {
                val realSpbuList = spbuRepository.getNearbySpbu(originCoord)
                _calculationResult.value = _calculationResult.value?.copy(nearbySpbu = realSpbuList)
            }
        }
    }

    /**
     * Memilih item riwayat dari layar Histori dan menampilkan hasilnya
     * secara langsung di Result Screen.
     */
    fun selectHistoryItem(item: HistoryModel) {
        _origin.value = item.origin
        _destination.value = item.destination

        // Reset saved map camera agar ResultScreen melakukan animasi zoom-out/zoom-in auto-fit mencakup kedua pin
        _resultMapCenter.value = null
        _resultMapZoom.value = null

        // Gunakan koordinat asli yang tersimpan presisi di riwayat (tanpa tebak-tebakan)
        val originCoord = LatLng(item.originLat, item.originLng)
        val destCoord = LatLng(item.destLat, item.destLng)
        _originCoord.value = originCoord
        _destinationCoord.value = destCoord

        // Cari kendaraan dan jenis BBM yang sesuai dengan riwayat
        val matchingVehicle = vehicles.value.find { it.name.equals(item.vehicleName, true) }
            ?: _defaultKendaraan.value.find { it.name.equals(item.vehicleName, true) }
            ?: VehicleModel("hist", item.vehicleName, "Mobil", BigDecimal("15.0"))

        val matchingFuel = fuels.value.find { it.name.equals(item.fuelName, true) }
            ?: _defaultBbm.value.find { it.name.equals(item.fuelName, true) }
            ?: FuelModel("hist", item.fuelName, if (item.totalLiters > BigDecimal.ZERO) item.totalCost.divide(item.totalLiters, 0, RoundingMode.HALF_UP) else BigDecimal("10000"))

        _selectedVehicle.value = matchingVehicle
        _selectedFuel.value = matchingFuel

        viewModelScope.launch {
            // Ambil rute OSRM riil agar kedua pin (asal & tujuan) serta garis rute muncul sempurna di peta
            val route = try {
                osrmRouteRepository.getDrivingRoute(originCoord, destCoord)
            } catch (_: Exception) {
                null
            }

            val routePts = route?.polylinePoints?.ifEmpty { listOf(originCoord, destCoord) } ?: listOf(originCoord, destCoord)
            _routePoints.value = routePts

            // Gunakan 100% data historis asli untuk biaya dan jarak, dengan routePoints yang lengkap
            val result = CalculationResult(
                originName = item.origin.ifBlank { "Lokasi Asal" },
                destinationName = item.destination.ifBlank { "Lokasi Tujuan" },
                vehicleName = item.vehicleName,
                fuelType = item.fuelName,
                fuelEfficiency = matchingVehicle.fuelEfficiency,
                pricePerLiter = matchingFuel.pricePerLiter,
                distanceKm = item.distanceKm,
                totalLiters = item.totalLiters,
                totalCost = item.totalCost,
                routePoints = routePts,
                nearbySpbu = emptyList()
            )
            _calculationResult.value = result
            navigateTo(AppScreen.RESULT)

            viewModelScope.launch(Dispatchers.IO) {
                val realSpbuList = spbuRepository.getNearbySpbu(destCoord)
                _calculationResult.value = _calculationResult.value?.copy(nearbySpbu = realSpbuList)
            }
        }
    }

    // CRUD Vehicle (Offline via Room untuk kendaraan pengguna kustom)
    fun addVehicle(name: String, type: String, efficiency: BigDecimal) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = KendaraanKustomEntity(
                id = UUID.randomUUID().toString(),
                nama = name.uppercase().trim(),
                tipe = type,
                rasioKonsumsi = efficiency.toDouble()
            )
            kendaraanKustomDao.insertKendaraan(entity)
            _toastMessage.value = "Berhasil ditambahkan!"
            showAddVehicleDialog.value = false
        }
    }

    fun updateVehicle(id: String, name: String, type: String, efficiency: BigDecimal) {
        viewModelScope.launch(Dispatchers.IO) {
            val isDefaultItem = _defaultKendaraan.value.any { it.id == id }
            if (isDefaultItem) {
                _defaultKendaraan.value = _defaultKendaraan.value.map {
                    if (it.id == id) {
                        it.copy(name = name.uppercase().trim(), type = type, fuelEfficiency = efficiency)
                    } else it
                }
            } else {
                val entity = KendaraanKustomEntity(
                    id = id,
                    nama = name.uppercase().trim(),
                    tipe = type,
                    rasioKonsumsi = efficiency.toDouble()
                )
                kendaraanKustomDao.updateKendaraan(entity)
            }
            _toastMessage.value = "Berhasil disimpan!"
            showEditVehicleDialog.value = false
        }
    }

    fun deleteVehicle(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _defaultKendaraan.value = _defaultKendaraan.value.filter { it.id != id }
            kendaraanKustomDao.deleteKendaraanById(id)
            _toastMessage.value = "Berhasil dihapus!"
            showDeleteVehicleDialog.value = false
        }
    }

    // CRUD Fuel (Offline via Room untuk BBM pengguna kustom)
    fun addFuel(name: String, price: BigDecimal) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = BbmKustomEntity(
                id = UUID.randomUUID().toString(),
                nama = name.uppercase().trim(),
                harga = price.toDouble(),
                iconResId = R.drawable.pertamax
            )
            bbmKustomDao.insertBbm(entity)
            _toastMessage.value = "Berhasil ditambahkan!"
            showAddFuelDialog.value = false
        }
    }

    fun updateFuel(id: String, name: String, price: BigDecimal) {
        viewModelScope.launch(Dispatchers.IO) {
            val isDefaultItem = _defaultBbm.value.any { it.id == id }
            if (isDefaultItem) {
                _defaultBbm.value = _defaultBbm.value.map {
                    if (it.id == id) {
                        it.copy(name = name.uppercase().trim(), pricePerLiter = price)
                    } else it
                }
            } else {
                val entity = BbmKustomEntity(
                    id = id,
                    nama = name.uppercase().trim(),
                    harga = price.toDouble(),
                    iconResId = R.drawable.pertamax
                )
                bbmKustomDao.updateBbm(entity)
            }
            _toastMessage.value = "Berhasil disimpan!"
            showEditFuelDialog.value = false
        }
    }

    fun deleteFuel(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _defaultBbm.value = _defaultBbm.value.filter { it.id != id }
            bbmKustomDao.deleteBbmById(id)
            _toastMessage.value = "Berhasil dihapus!"
            showDeleteFuelDialog.value = false
        }
    }

    // CRUD History (100% Offline via Room)
    fun deleteHistory(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            riwayatDao.deleteRiwayatById(id)
            _toastMessage.value = "Histori berhasil dihapus!"
            showDeleteHistoryDialog.value = false
        }
    }

    fun confirmDeleteAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            riwayatDao.deleteAllRiwayat()
            _toastMessage.value = "Semua riwayat berhasil dihapus!"
            showDeleteAllHistoryDialog.value = false
        }
    }
}
