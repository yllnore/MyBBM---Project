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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
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
    SPLASH,

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

enum class MapPickMode {
    NONE,
    ORIGIN,
    DESTINATION
}

class BbmViewModel(application: Application) : AndroidViewModel(application) {

    // 1. Offline Storage (Room Database - Cache Supabase + Data Pengguna)
    private val db = AppDatabase.getInstance(application)
    private val kendaraanKustomDao = db.kendaraanKustomDao()
    private val bbmKustomDao = db.bbmKustomDao()
    private val riwayatDao = db.riwayatDao()

    // 2. Online Repository (Supabase Realtime Hybrid Cache)
    private val supabaseDefaultRepository = SupabaseDefaultRepository()

    // Layanan remote & domain lainnya
    private val hitungBiayaUseCase = HitungBiayaUseCase()
    private val locationRepository = LocationRepository()
    private val osrmRouteRepository = OsrmRouteRepository()
    private val spbuRepository = SpbuRepository()

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
    private val _currentScreen = MutableStateFlow(AppScreen.SPLASH)
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

    // Map Location Picker Mode State
    private val _mapPickMode = MutableStateFlow(MapPickMode.NONE)
    val mapPickMode: StateFlow<MapPickMode> = _mapPickMode.asStateFlow()

    fun startPickingLocation(mode: MapPickMode) {
        _mapPickMode.value = mode
    }

    fun cancelPickingLocation() {
        _mapPickMode.value = MapPickMode.NONE
    }

    fun confirmPickedLocation(latLng: LatLng, addressName: String) {
        val mode = _mapPickMode.value
        if (mode == MapPickMode.ORIGIN) {
            _origin.value = addressName
            _originCoord.value = latLng
            checkAndCalculateOsrmRoute()
        } else if (mode == MapPickMode.DESTINATION) {
            _destination.value = addressName
            _destinationCoord.value = latLng
            checkAndCalculateOsrmRoute()
        }
        _mapPickMode.value = MapPickMode.NONE
    }

    fun confirmPickedLocationWithReverseGeocode(latLng: LatLng, context: Context) {
        val mode = _mapPickMode.value
        if (mode == MapPickMode.NONE) return

        viewModelScope.launch {
            val address = try {
                locationRepository.reverseGeocode(latLng.lat, latLng.lng)
            } catch (_: Exception) {
                null
            }

            val fallbackName = if (mode == MapPickMode.ORIGIN) "Lokasi Asal Dipilih" else "Lokasi Tujuan Dipilih"
            val finalName = address ?: fallbackName

            confirmPickedLocation(latLng, finalName)
            showToast("Lokasi berhasil dipilih!")
        }
    }

    // 3. Flow Data Kendaraan dari Room Database (Cache Supabase + Kustom Pengguna)
    val vehicles: StateFlow<List<VehicleModel>> = kendaraanKustomDao.getAllKendaraan()
        .map { list ->
            list.map {
                VehicleModel(
                    id = it.id,
                    name = it.nama,
                    type = it.tipe,
                    fuelEfficiency = BigDecimal.valueOf(it.rasioKonsumsi),
                    isDefault = it.isDefault
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // 4. Flow Data Jenis BBM dari Room Database (Cache Supabase + Kustom Pengguna)
    val fuels: StateFlow<List<FuelModel>> = bbmKustomDao.getAllBbm()
        .map { list ->
            list.map {
                val iconRes = it.iconResId ?: when {
                    it.nama.contains("PERTALITE", ignoreCase = true) -> R.drawable.pertalite
                    it.nama.contains("TURBO", ignoreCase = true) -> R.drawable.pertamax_turbo
                    it.nama.contains("PERTAMAX", ignoreCase = true) -> R.drawable.pertamax
                    it.nama.contains("SOLAR", ignoreCase = true) || it.nama.contains("DEX", ignoreCase = true) -> R.drawable.solar
                    else -> R.drawable.pertamax
                }
                val colorHex = it.warnaHex ?: when {
                    it.nama.contains("PERTALITE", ignoreCase = true) -> "#10B981"
                    it.nama.contains("TURBO", ignoreCase = true) -> "#EF4444"
                    it.nama.contains("PERTAMAX", ignoreCase = true) -> "#7542EF"
                    it.nama.contains("BIOSOLAR", ignoreCase = true) || it.nama.contains("SOLAR", ignoreCase = true) -> "#4B5563"
                    it.nama.contains("DEXLITE", ignoreCase = true) -> "#059669"
                    it.nama.contains("PERTAMINA DEX", ignoreCase = true) -> "#2563EB"
                    else -> "#7542EF"
                }
                FuelModel(
                    id = it.id,
                    name = it.nama,
                    pricePerLiter = BigDecimal.valueOf(it.harga),
                    iconResId = iconRes,
                    colorHex = colorHex,
                    isDefault = it.isDefault
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

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
     * Mengambil data riil dari Supabase (tabel `kendaraan` dan `harga_bbm`)
     * dan menyimpannya ke cache lokal Room Database.
     * Karena `vehicles` dan `fuels` mengobservasi Flow dari Room,
     * tampilan UI akan otomatis terupdate secara realtime begitu data tersimpan.
     */
    fun loadDefaultDatasetsFromSupabase() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                supabaseDefaultRepository.syncKendaraan(kendaraanKustomDao)
                supabaseDefaultRepository.syncBbm(bbmKustomDao)
            } catch (e: Exception) {
                android.util.Log.e("BbmViewModel", "Gagal sinkronisasi data dari Supabase: ${e.message}", e)
            }
        }
    }

    private val screenBackStack = mutableListOf<AppScreen>()

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            screenBackStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack() {
        if (screenBackStack.isNotEmpty()) {
            val prev = screenBackStack.removeAt(screenBackStack.size - 1)
            _currentScreen.value = prev
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
        if (value.isBlank()) {
            _originCoord.value = null
            _routePoints.value = emptyList()
            _estimatedDistanceKm.value = null
        } else {
            _originCoord.value = resolveCoordinate(value, isOrigin = true)
            updateRoutePreview()
        }
    }

    fun setOriginText(value: String) {
        _origin.value = value
        if (value.isBlank()) {
            _originCoord.value = null
            _routePoints.value = emptyList()
            _estimatedDistanceKm.value = null
        }
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
        if (value.isBlank()) {
            _destinationCoord.value = null
            _routePoints.value = emptyList()
            _estimatedDistanceKm.value = null
        } else {
            _destinationCoord.value = resolveCoordinate(value, isOrigin = false)
            updateRoutePreview()
        }
    }

    fun setDestinationText(value: String) {
        _destination.value = value
        if (value.isBlank()) {
            _destinationCoord.value = null
            _routePoints.value = emptyList()
            _estimatedDistanceKm.value = null
        }
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
        val allVehicles = vehicles.value
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
        val allFuels = fuels.value
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
        val match = vehicles.value.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
        _selectedVehicle.value = match
    }

    fun setFuelQuery(name: String) {
        val trimmed = name.trim()
        val match = fuels.value.firstOrNull { it.name.equals(trimmed, ignoreCase = true) }
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
        val vehicle = _selectedVehicle.value ?: vehicles.value.firstOrNull() ?: VehicleModel("1", "KENDARAAN", "Mobil", BigDecimal("15.0"))
        val fuel = _selectedFuel.value ?: fuels.value.firstOrNull() ?: FuelModel("1", "BBM", BigDecimal("10000"))

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

            // Mengambil daftar SPBU terdekat di sepanjang rute menggunakan SpbuRepository
            viewModelScope.launch(Dispatchers.IO) {
                val realSpbuList = spbuRepository.getSpbuAlongRoute(output.polylinePoints)
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
            ?: VehicleModel("hist", item.vehicleName, "Mobil", BigDecimal("15.0"))

        val matchingFuel = fuels.value.find { it.name.equals(item.fuelName, true) }
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
                val realSpbuList = spbuRepository.getSpbuAlongRoute(routePts)
                _calculationResult.value = _calculationResult.value?.copy(nearbySpbu = realSpbuList)
            }
        }
    }

    // CRUD Vehicle (Room Database)
    fun addVehicle(name: String, type: String, efficiency: BigDecimal) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = KendaraanKustomEntity(
                id = UUID.randomUUID().toString(),
                nama = name.uppercase().trim(),
                tipe = type,
                rasioKonsumsi = efficiency.toDouble(),
                isDefault = false
            )
            kendaraanKustomDao.insertKendaraan(entity)
            _toastMessage.value = "Berhasil ditambahkan!"
            showAddVehicleDialog.value = false
        }
    }

    fun updateVehicle(id: String, name: String, type: String, efficiency: BigDecimal) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = vehicles.value.find { it.id == id }
            val entity = KendaraanKustomEntity(
                id = id,
                nama = name.uppercase().trim(),
                tipe = type,
                rasioKonsumsi = efficiency.toDouble(),
                isDefault = existing?.isDefault ?: false
            )
            kendaraanKustomDao.insertKendaraan(entity)
            _toastMessage.value = "Berhasil disimpan!"
            showEditVehicleDialog.value = false
        }
    }

    fun deleteVehicle(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            kendaraanKustomDao.deleteKendaraanById(id)
            _toastMessage.value = "Berhasil dihapus!"
            showDeleteVehicleDialog.value = false
        }
    }

    // CRUD Fuel (Room Database)
    fun addFuel(name: String, price: BigDecimal) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = BbmKustomEntity(
                id = UUID.randomUUID().toString(),
                nama = name.uppercase().trim(),
                harga = price.toDouble(),
                iconResId = R.drawable.pertamax,
                isDefault = false
            )
            bbmKustomDao.insertBbm(entity)
            _toastMessage.value = "Berhasil ditambahkan!"
            showAddFuelDialog.value = false
        }
    }

    fun updateFuel(id: String, name: String, price: BigDecimal) {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = fuels.value.find { it.id == id }
            val entity = BbmKustomEntity(
                id = id,
                nama = name.uppercase().trim(),
                harga = price.toDouble(),
                iconResId = existing?.iconResId ?: R.drawable.pertamax,
                warnaHex = existing?.colorHex,
                isDefault = existing?.isDefault ?: false
            )
            bbmKustomDao.insertBbm(entity)
            _toastMessage.value = "Berhasil disimpan!"
            showEditFuelDialog.value = false
        }
    }

    fun deleteFuel(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
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
