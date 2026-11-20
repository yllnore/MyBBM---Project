package com.mybbm.app.ui.screens

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mybbm.app.R
import com.mybbm.app.domain.model.LatLng
import com.mybbm.app.domain.model.SearchSuggestionItem
import com.mybbm.app.ui.components.MapVisualOverlay
import com.mybbm.app.ui.components.OsmMapView
import com.mybbm.app.util.DeviceLocationHelper
import com.mybbm.app.util.FuelIconResolver
import com.mybbm.app.viewmodel.BbmViewModel
import com.mybbm.app.viewmodel.MapPickMode
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class ActiveInputMode {
    NONE,
    ORIGIN,
    DESTINATION,
    VEHICLE,
    FUEL
}

/**
 * State pengontrol ekspansi BottomSheet form perjalanan.
 * Menyediakan method suspend expand() dan collapse() untuk dipanggil via coroutine.
 */
class BottomSheetState(
    initialExpanded: Boolean = false
) {
    var isExpanded by mutableStateOf(initialExpanded)

    suspend fun expand() {
        isExpanded = true
    }

    suspend fun collapse() {
        isExpanded = false
    }
}

@Composable
fun rememberBottomSheetState(initialExpanded: Boolean = false): BottomSheetState {
    return remember { BottomSheetState(initialExpanded) }
}

/**
 * Menentukan apakah BottomSheet harus tetap expanded atau collapse setelah pengguna selesai menggeser (drag/fling).
 */
internal fun resolveBottomSheetExpansionAfterDrag(
    currentlyExpanded: Boolean,
    cumulativeDragPx: Float,
    velocityPxPerSec: Float,
    thresholdPx: Float,
    velocityThresholdPxPerSec: Float = 350f
): Boolean {
    return if (currentlyExpanded) {
        val shouldCollapse = cumulativeDragPx > thresholdPx || velocityPxPerSec > velocityThresholdPxPerSec
        !shouldCollapse
    } else {
        cumulativeDragPx < -thresholdPx || velocityPxPerSec < -velocityThresholdPxPerSec
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: BbmViewModel,
    onNavigateToResult: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToVehicles: () -> Unit = {},
    onNavigateToFuels: () -> Unit = {}
) {
    val origin by viewModel.origin.collectAsState()
    val destination by viewModel.destination.collectAsState()
    val originCoord by viewModel.originCoord.collectAsState()
    val destinationCoord by viewModel.destinationCoord.collectAsState()
    val homeMapCenter by viewModel.homeMapCenter.collectAsState()
    val homeMapZoom by viewModel.homeMapZoom.collectAsState()
    val routePoints by viewModel.routePoints.collectAsState()
    val estimatedDistanceKm by viewModel.estimatedDistanceKm.collectAsState()
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()
    val selectedFuel by viewModel.selectedFuel.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val fuels by viewModel.fuels.collectAsState()
    val activeSearchList by viewModel.activeSearchList.collectAsState()
    val isSearchingLocation by viewModel.isSearchingLocation.collectAsState()
    val isDetectingLocation by viewModel.isDetectingLocation.collectAsState()
    val mapPickMode by viewModel.mapPickMode.collectAsState()
    val context = LocalContext.current

    // 1. Auto-Expand BottomSheet State & Coroutine
    val bottomSheetState = rememberBottomSheetState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val density = LocalDensity.current
    var activeMode by remember { mutableStateOf(ActiveInputMode.NONE) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    // Cegah auto-focus bawaan sistem saat layar pertama kali muncul meng-expand bottom sheet secara tidak sengaja
    var allowFocusExpand by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        focusManager.clearFocus(force = true)
        delay(150L)
        allowFocusExpand = true
    }

    // FocusRequesters untuk kontrol fokus dan kursor otomatis antar field
    val originFocusRequester = remember { FocusRequester() }
    val destinationFocusRequester = remember { FocusRequester() }
    val vehicleFocusRequester = remember { FocusRequester() }
    val fuelFocusRequester = remember { FocusRequester() }

    // Search query states for filtering when typing
    var originQuery by remember { mutableStateOf("") }
    var originTextFieldValue by remember { mutableStateOf(TextFieldValue(origin, selection = TextRange(origin.length))) }

    var destinationQuery by remember { mutableStateOf("") }
    var destinationTextFieldValue by remember { mutableStateOf(TextFieldValue(destination, selection = TextRange(destination.length))) }

    var vehicleQuery by remember { mutableStateOf("") }
    var vehicleTextFieldValue by remember { mutableStateOf(TextFieldValue(selectedVehicle?.name ?: "", selection = TextRange((selectedVehicle?.name ?: "").length))) }

    var fuelQuery by remember { mutableStateOf("") }
    var fuelTextFieldValue by remember { mutableStateOf(TextFieldValue(selectedFuel?.name ?: "", selection = TextRange((selectedFuel?.name ?: "").length))) }

    // Fungsi animasi ketik (typewriter) halus saat lokasi terdeteksi, dengan kursor ditempatkan di paling belakang/akhir teks (setelah huruf terakhir)
    val triggerTypingAnimation: (String) -> Unit = { fullText ->
        coroutineScope.launch {
            originTextFieldValue = TextFieldValue("", selection = TextRange(0))
            for (i in 1..fullText.length) {
                val sub = fullText.substring(0, i)
                originTextFieldValue = TextFieldValue(sub, selection = TextRange(sub.length))
                originQuery = sub
                delay(25L)
            }
            // Selesai animasi ketik, taruh kursor di paling belakang teks (setelah huruf terakhir, misal: Blok BM, Samarinda|)
            originTextFieldValue = TextFieldValue(fullText, selection = TextRange(fullText.length))
            originQuery = fullText
            viewModel.setOriginText(fullText)
        }
    }

    // Filter saran autocomplete sesuai input field yang sedang aktif
    val filteredSuggestions = remember(activeSearchList, vehicles, fuels, activeMode, vehicleQuery, fuelQuery, selectedVehicle, selectedFuel) {
        when (activeMode) {
            ActiveInputMode.ORIGIN, ActiveInputMode.DESTINATION -> {
                activeSearchList.filterIsInstance<SearchSuggestionItem.Location>()
            }
            ActiveInputMode.VEHICLE -> {
                val trimmed = vehicleQuery.trim()
                val baseVehicles = vehicles
                val list = if (trimmed.isBlank() || trimmed.equals(selectedVehicle?.name, ignoreCase = true)) {
                    baseVehicles
                } else {
                    baseVehicles.filter {
                        it.name.contains(trimmed, ignoreCase = true) ||
                        it.type.contains(trimmed, ignoreCase = true)
                    }
                }
                list.map { SearchSuggestionItem.Vehicle(it) }
            }
            ActiveInputMode.FUEL -> {
                val trimmed = fuelQuery.trim()
                val baseFuels = fuels
                val list = if (trimmed.isBlank() || trimmed.equals(selectedFuel?.name, ignoreCase = true)) {
                    baseFuels
                } else {
                    baseFuels.filter {
                        it.name.contains(trimmed, ignoreCase = true)
                    }
                }
                list.map { SearchSuggestionItem.Fuel(it) }
            }
            ActiveInputMode.NONE -> emptyList()
        }
    }

    // State untuk kontrol posisi kamera dan marker lokasi fisik pengguna saat ini
    var userLocationCoord by remember { mutableStateOf<LatLng?>(null) }
    var mapCameraTarget by remember { mutableStateOf<LatLng?>(null) }
    var mapCameraTrigger by remember { mutableLongStateOf(0L) }
    var isLocatingUser by remember { mutableStateOf(false) }
    var pendingLocationAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Helper aksi gunakan lokasi saat ini untuk kolom asal di bottom sheet
    val handleUseCurrentLocationForOrigin: () -> Unit = {
        viewModel.useCurrentLocation(context) { name, latLng ->
            userLocationCoord = latLng
            triggerTypingAnimation(name)
            coroutineScope.launch {
                bottomSheetState.expand()
            }
            if (destinationQuery.isBlank() && destination.isBlank()) {
                activeMode = ActiveInputMode.DESTINATION
                viewModel.clearSearchResults()
                coroutineScope.launch {
                    delay(name.length * 25L + 100L)
                    destinationFocusRequester.requestFocus()
                }
            } else {
                activeMode = ActiveInputMode.VEHICLE
                viewModel.filterVehicles("")
                coroutineScope.launch {
                    delay(name.length * 25L + 100L)
                    vehicleFocusRequester.requestFocus()
                }
            }
        }
    }

    // Permission launcher untuk mendeteksi GPS fisik riil perangkat
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val isGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (isGranted) {
            val pending = pendingLocationAction
            if (pending != null) {
                pending.invoke()
                pendingLocationAction = null
            } else {
                handleUseCurrentLocationForOrigin()
            }
        } else {
            pendingLocationAction = null
            viewModel.showToast("Izin lokasi diperlukan untuk mengakses fitur GPS.")
        }
    }

    // Aksi memusatkan peta ke lokasi pengguna saat ini (GPS)
    val centerMapToCurrentLocation: () -> Unit = {
        coroutineScope.launch {
            if (isLocatingUser) return@launch
            isLocatingUser = true
            try {
                if (DeviceLocationHelper.hasLocationPermission(context)) {
                    // Tutup keyboard dan persempit panel agar pengguna melihat peta secara leluasa
                    focusManager.clearFocus()
                    activeMode = ActiveInputMode.NONE
                    bottomSheetState.collapse()

                    val loc = DeviceLocationHelper.getCurrentLocation(context)
                    if (loc != null) {
                        userLocationCoord = loc
                        mapCameraTarget = loc
                        mapCameraTrigger = System.currentTimeMillis()
                        viewModel.showToast("Peta dipusatkan ke lokasi Anda")
                    } else {
                        viewModel.showToast("Tidak dapat mendeteksi lokasi GPS. Pastikan GPS aktif.")
                    }
                } else {
                    pendingLocationAction = {
                        coroutineScope.launch {
                            focusManager.clearFocus()
                            activeMode = ActiveInputMode.NONE
                            bottomSheetState.collapse()

                            val loc = DeviceLocationHelper.getCurrentLocation(context)
                            if (loc != null) {
                                userLocationCoord = loc
                                mapCameraTarget = loc
                                mapCameraTrigger = System.currentTimeMillis()
                                viewModel.showToast("Peta dipusatkan ke lokasi Anda")
                            } else {
                                viewModel.showToast("Tidak dapat mendeteksi lokasi GPS. Pastikan GPS aktif.")
                            }
                        }
                    }
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            } finally {
                isLocatingUser = false
            }
        }
    }

    // Sinkronisasi teks awal dengan ViewModel selection
    LaunchedEffect(origin) {
        if (originTextFieldValue.text != origin) {
            originTextFieldValue = TextFieldValue(origin, selection = TextRange(origin.length))
            originQuery = origin
        }
    }
    LaunchedEffect(destination) {
        if (destinationTextFieldValue.text != destination) {
            destinationTextFieldValue = TextFieldValue(destination, selection = TextRange(destination.length))
            destinationQuery = destination
        }
    }
    LaunchedEffect(selectedVehicle) {
        val name = selectedVehicle?.name ?: ""
        if (vehicleTextFieldValue.text != name) {
            vehicleTextFieldValue = TextFieldValue(name, selection = TextRange(name.length))
            vehicleQuery = name
        }
    }
    LaunchedEffect(selectedFuel) {
        val name = selectedFuel?.name ?: ""
        if (fuelTextFieldValue.text != name) {
            fuelTextFieldValue = TextFieldValue(name, selection = TextRange(name.length))
            fuelQuery = name
        }
    }

    val isImeVisible = WindowInsets.isImeVisible
    val isSheetExpanded = bottomSheetState.isExpanded || activeMode != ActiveInputMode.NONE || isImeVisible

    val collapseBottomSheet: () -> Unit = {
        dragOffsetPx = 0f
        activeMode = ActiveInputMode.NONE
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
        viewModel.clearSearchResults()
        coroutineScope.launch {
            bottomSheetState.collapse()
        }
    }

    val expandBottomSheetDefault: () -> Unit = {
        dragOffsetPx = 0f
        allowFocusExpand = true
        coroutineScope.launch {
            bottomSheetState.expand()
            if (activeMode == ActiveInputMode.NONE) {
                activeMode = when {
                    originQuery.isBlank() -> ActiveInputMode.ORIGIN
                    destinationQuery.isBlank() -> ActiveInputMode.DESTINATION
                    vehicleQuery.isBlank() -> ActiveInputMode.VEHICLE
                    fuelQuery.isBlank() -> ActiveInputMode.FUEL
                    else -> ActiveInputMode.ORIGIN
                }
            }
        }
    }

    // Reset posisi BottomSheet dan saran saat form di-reset ke awal (misal saat kembali ke beranda)
    LaunchedEffect(origin, destination, selectedVehicle, selectedFuel) {
        if (origin.isBlank() && destination.isBlank() && selectedVehicle == null && selectedFuel == null) {
            collapseBottomSheet()
        }
    }

    // Tombol Back sistem menutup BottomSheet / pick mode sebelum keluar dari aplikasi
    BackHandler(enabled = isSheetExpanded || mapPickMode != MapPickMode.NONE) {
        if (mapPickMode != MapPickMode.NONE) {
            viewModel.cancelPickingLocation()
        } else {
            collapseBottomSheet()
        }
    }

    // Animasi tinggi BottomSheet (0.44f saat compact, 0.86f saat expanded agar jauh dari status bar/notch)
    val baseSheetFraction by animateFloatAsState(
        targetValue = if (isSheetExpanded) 0.86f else 0.44f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "sheetHeightFraction"
    )

    // Root Container
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val configuration = LocalConfiguration.current
        val screenHeightDp = configuration.screenHeightDp.dp
        val dragOffsetDp = with(density) { dragOffsetPx.toDp() }
        val bottomSheetHeightDp = (screenHeightDp * baseSheetFraction - dragOffsetDp)
            .coerceIn(screenHeightDp * 0.40f, screenHeightDp * 0.90f)
        val dragThresholdPx = with(density) { 44.dp.toPx() }
        val currentIsSheetExpanded by rememberUpdatedState(isSheetExpanded)

        val finishSheetDrag: (Float) -> Unit = { velocity ->
            val shouldRemainExpanded = resolveBottomSheetExpansionAfterDrag(
                currentlyExpanded = currentIsSheetExpanded,
                cumulativeDragPx = dragOffsetPx,
                velocityPxPerSec = velocity,
                thresholdPx = dragThresholdPx
            )
            dragOffsetPx = 0f
            if (shouldRemainExpanded) {
                expandBottomSheetDefault()
            } else {
                collapseBottomSheet()
            }
        }

        val sheetNestedScrollConnection = remember(dragThresholdPx) {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    if (source == NestedScrollSource.UserInput && dragOffsetPx > 0f && available.y < 0f) {
                        val consumed = available.y.coerceAtLeast(-dragOffsetPx)
                        dragOffsetPx += consumed
                        return Offset(0f, consumed)
                    }
                    return Offset.Zero
                }

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource
                ): Offset {
                    if (source == NestedScrollSource.UserInput) {
                        if (currentIsSheetExpanded && available.y > 0f) {
                            dragOffsetPx += available.y
                            return Offset(0f, available.y)
                        } else if (!currentIsSheetExpanded && available.y < 0f) {
                            dragOffsetPx += available.y
                            return Offset(0f, available.y)
                        }
                    }
                    return Offset.Zero
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    if (dragOffsetPx != 0f) {
                        finishSheetDrag(available.y)
                        return available
                    }
                    return Velocity.Zero
                }
            }
        }

        // 1. Latar Belakang Peta OSM Interaktif (Tema Terang #E8ECEF / #ECEBFF & Polyline Halus #7542EF)
        OsmMapView(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomSheetHeightDp),
            centerCoordinate = LatLng(-0.5021, 117.1536), // Samarinda (Polnes)
            zoomLevel = 12.5,
            savedCameraCenter = homeMapCenter,
            savedCameraZoom = homeMapZoom,
            onCameraMoved = { center, zoom ->
                viewModel.updateHomeMapCamera(center, zoom)
            },
            originCoordinate = originCoord,
            originLabel = origin.ifBlank { "Lokasi Asal" },
            destinationCoordinate = destinationCoord,
            destinationLabel = destination.ifBlank { "Lokasi Tujuan" },
            userLocationCoordinate = userLocationCoord,
            cameraTarget = mapCameraTarget,
            cameraZoom = 16.5,
            cameraTrigger = mapCameraTrigger,
            routePoints = routePoints,
            onMapTap = {
                if (currentIsSheetExpanded) {
                    collapseBottomSheet()
                }
            }
        )

        // Tombol Mengambang di Kanan Atas (FABs)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingMapAction(
                iconResId = R.drawable.settings_icon,
                onClick = onNavigateToSettings
            )
            FloatingMapAction(
                iconResId = R.drawable.history,
                onClick = onNavigateToHistory
            )
            FloatingMapAction(
                iconResId = R.drawable.current_location_icon,
                isLoading = isLocatingUser,
                onClick = centerMapToCurrentLocation
            )
        }

        // Map Location Picker Overlay (Jika sedang mode pilih lokasi di peta)
        if (mapPickMode != MapPickMode.NONE) {
            // Pin statis di tengah area map yang terlihat (di atas bottom sheet)
            // Bisa langsung dipencet/diklik untuk memilih lokasi!
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeightDp - bottomSheetHeightDp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .clickable {
                            val targetCoord = homeMapCenter ?: LatLng(-0.5021, 117.1536)
                            viewModel.confirmPickedLocationWithReverseGeocode(targetCoord, context)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = if (mapPickMode == MapPickMode.ORIGIN) R.drawable.lokasi_awal else R.drawable.lokasi_tujuan),
                        contentDescription = "Center Pin",
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            // Tombol Konfirmasi di bawah
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 36.dp, start = 24.dp, end = 24.dp)
            ) {
                Button(
                    onClick = {
                        val targetCoord = homeMapCenter ?: LatLng(-0.5021, 117.1536)
                        viewModel.confirmPickedLocationWithReverseGeocode(targetCoord, context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7542EF)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(8.dp, RoundedCornerShape(24.dp))
                ) {
                    Text(
                        text = "Pilih Lokasi Ini",
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }
            }
        }

        // Panel Input Perjalanan (Draggable 1:1 & Auto-Expand Bottom Sheet)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(bottomSheetHeightDp)
                .shadow(
                    elevation = 24.dp,
                    spotColor = Color(0x40000000),
                    ambientColor = Color(0x40000000),
                    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
                )
                .background(
                    color = Color(0xFFC0BEF8), // Lilac warna tema
                    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
                )
                .nestedScroll(sheetNestedScrollConnection)
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        dragOffsetPx += delta
                    },
                    onDragStopped = { velocity ->
                        finishSheetDrag(velocity)
                    }
                )
                .imePadding() // Menjamin tidak tertutup keyboard layar
                .padding(horizontal = 20.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle Bar dengan area sentuh luas (44dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clickable {
                        if (isSheetExpanded) {
                            collapseBottomSheet()
                        } else {
                            expandBottomSheetDefault()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(46.dp)
                        .height(5.dp)
                        .background(
                            color = Color(0xFF7289D2),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 2. Keempat TextField dalam urutan Column utama (Scrollable agar ramah keyboard virtual)
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Lokasi Awal
                EditableInputField(
                    textFieldValue = originTextFieldValue,
                    placeholder = "Lokasi Awal",
                    iconResId = R.drawable.lokasi_awal,
                    isActive = activeMode == ActiveInputMode.ORIGIN,
                    focusRequester = originFocusRequester,
                    onExplicitTap = { allowFocusExpand = true },
                    onValueChange = {
                        allowFocusExpand = true
                        originTextFieldValue = it
                        originQuery = it.text
                        activeMode = ActiveInputMode.ORIGIN
                        viewModel.setOriginText(it.text)
                        viewModel.searchLocation(it.text)
                    },
                    onFocusChanged = { isFocused ->
                        if (isFocused) {
                            if (!allowFocusExpand) {
                                focusManager.clearFocus(force = true)
                            } else if (activeMode != ActiveInputMode.ORIGIN) {
                                dragOffsetPx = 0f
                                coroutineScope.launch { bottomSheetState.expand() }
                                activeMode = ActiveInputMode.ORIGIN
                                if (originTextFieldValue.text.trim().length >= 2) {
                                    viewModel.searchLocation(originTextFieldValue.text)
                                } else {
                                    viewModel.clearSearchResults()
                                }
                            }
                        }
                    },
                    imeAction = ImeAction.Next,
                    onImeAction = {
                        if (originTextFieldValue.text.isNotBlank()) {
                            viewModel.setOrigin(originTextFieldValue.text)
                        } else {
                            viewModel.setOrigin("")
                        }
                        allowFocusExpand = true
                        activeMode = ActiveInputMode.DESTINATION
                        viewModel.clearSearchResults()
                        coroutineScope.launch {
                            bottomSheetState.expand()
                            delay(50L)
                            destinationFocusRequester.requestFocus()
                        }
                    },
                    onIconClick = {
                        coroutineScope.launch {
                            if (mapPickMode == MapPickMode.ORIGIN) {
                                viewModel.cancelPickingLocation()
                            } else {
                                collapseBottomSheet()
                                viewModel.startPickingLocation(MapPickMode.ORIGIN)
                            }
                        }
                    }
                )

                // 2. Lokasi Tujuan
                EditableInputField(
                    textFieldValue = destinationTextFieldValue,
                    placeholder = "Lokasi Tujuan",
                    iconResId = R.drawable.lokasi_tujuan,
                    isActive = activeMode == ActiveInputMode.DESTINATION,
                    focusRequester = destinationFocusRequester,
                    onExplicitTap = { allowFocusExpand = true },
                    onValueChange = {
                        allowFocusExpand = true
                        destinationTextFieldValue = it
                        destinationQuery = it.text
                        activeMode = ActiveInputMode.DESTINATION
                        viewModel.setDestinationText(it.text)
                        viewModel.searchLocation(it.text)
                    },
                    onFocusChanged = { isFocused ->
                        if (isFocused) {
                            if (!allowFocusExpand) {
                                focusManager.clearFocus(force = true)
                            } else if (activeMode != ActiveInputMode.DESTINATION) {
                                dragOffsetPx = 0f
                                coroutineScope.launch { bottomSheetState.expand() }
                                activeMode = ActiveInputMode.DESTINATION
                                if (destinationTextFieldValue.text.trim().length >= 2) {
                                    viewModel.searchLocation(destinationTextFieldValue.text)
                                } else {
                                    viewModel.clearSearchResults()
                                }
                            }
                        }
                    },
                    imeAction = ImeAction.Next,
                    onImeAction = {
                        if (destinationTextFieldValue.text.isNotBlank()) {
                            viewModel.setDestination(destinationTextFieldValue.text)
                        } else {
                            viewModel.setDestination("")
                        }
                        allowFocusExpand = true
                        viewModel.clearSearchResults()
                        activeMode = ActiveInputMode.VEHICLE
                        viewModel.filterVehicles("")
                        coroutineScope.launch {
                            bottomSheetState.expand()
                            delay(50L)
                            vehicleFocusRequester.requestFocus()
                        }
                    },
                    onIconClick = {
                        coroutineScope.launch {
                            if (mapPickMode == MapPickMode.DESTINATION) {
                                viewModel.cancelPickingLocation()
                            } else {
                                collapseBottomSheet()
                                viewModel.startPickingLocation(MapPickMode.DESTINATION)
                            }
                        }
                    }
                )

                // 3. Kendaraan
                EditableInputField(
                    textFieldValue = vehicleTextFieldValue,
                    placeholder = "Pilih Kendaraan",
                    iconResId = null,
                    isActive = activeMode == ActiveInputMode.VEHICLE,
                    focusRequester = vehicleFocusRequester,
                    onExplicitTap = { allowFocusExpand = true },
                    onValueChange = {
                        allowFocusExpand = true
                        vehicleTextFieldValue = it
                        vehicleQuery = it.text
                        viewModel.setVehicleQuery(it.text)
                        viewModel.filterVehicles(it.text)
                    },
                    onFocusChanged = { isFocused ->
                        if (isFocused) {
                            if (!allowFocusExpand) {
                                focusManager.clearFocus(force = true)
                            } else {
                                dragOffsetPx = 0f
                                coroutineScope.launch { bottomSheetState.expand() }
                                activeMode = ActiveInputMode.VEHICLE
                                viewModel.filterVehicles("")
                            }
                        }
                    },
                    imeAction = ImeAction.Next,
                    onImeAction = {
                        allowFocusExpand = true
                        activeMode = ActiveInputMode.FUEL
                        viewModel.filterFuels("")
                        coroutineScope.launch {
                            bottomSheetState.expand()
                            delay(50L)
                            fuelFocusRequester.requestFocus()
                        }
                    }
                )

                // 4. Jenis BBM
                EditableInputField(
                    textFieldValue = fuelTextFieldValue,
                    placeholder = "Jenis BBM",
                    iconResId = null,
                    isActive = activeMode == ActiveInputMode.FUEL,
                    focusRequester = fuelFocusRequester,
                    onExplicitTap = { allowFocusExpand = true },
                    onValueChange = {
                        allowFocusExpand = true
                        fuelTextFieldValue = it
                        fuelQuery = it.text
                        viewModel.setFuelQuery(it.text)
                        viewModel.filterFuels(it.text)
                    },
                    onFocusChanged = { isFocused ->
                        if (isFocused) {
                            if (!allowFocusExpand) {
                                focusManager.clearFocus(force = true)
                            } else {
                                dragOffsetPx = 0f
                                coroutineScope.launch { bottomSheetState.expand() }
                                activeMode = ActiveInputMode.FUEL
                                viewModel.filterFuels("")
                            }
                        }
                    },
                    imeAction = ImeAction.Done,
                    onImeAction = {
                        collapseBottomSheet()
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Tombol Hitung Biaya Perjalanan (Tetap berada di bawah ke-4 TextField)
            Button(
                onClick = {
                    if (originCoord == null && originQuery.isBlank()) {
                        viewModel.showToast("Silakan tentukan lokasi awal terlebih dahulu")
                        return@Button
                    }
                    if (destinationCoord == null && destinationQuery.isBlank()) {
                        viewModel.showToast("Silakan tentukan lokasi tujuan terlebih dahulu")
                        return@Button
                    }
                    if (selectedVehicle == null && vehicleQuery.isBlank()) {
                        viewModel.showToast("Silakan pilih kendaraan terlebih dahulu")
                        return@Button
                    }
                    if (selectedFuel == null && fuelQuery.isBlank()) {
                        viewModel.showToast("Silakan pilih jenis BBM terlebih dahulu")
                        return@Button
                    }
                    viewModel.calculateTripCost()
                    onNavigateToResult()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7542EF)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shadow(
                        elevation = 6.dp,
                        spotColor = Color(0x5544308D),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Text(
                    text = "Hitung Biaya Perjalanan",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                )
            }

            // Info estimasi jarak OSRM jika rute telah dihitung
            if (estimatedDistanceKm != null && originCoord != null && destinationCoord != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFB5B3F2), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Jarak Estimasi:",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF11133E)
                        )
                    )
                    Text(
                        text = "$estimatedDistanceKm km",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF7542EF)
                        )
                    )
                }
            }

            // 3. Penampung Dinamis di BAGIAN PALING BAWAH (Tepat setelah tombol Hitung Biaya)
            AnimatedVisibility(
                visible = activeMode != ActiveInputMode.NONE,
                enter = expandVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = true)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    // Garis Pembatas Horizontal Sesuai Referensi Gambar
                    HorizontalDivider(
                        color = Color.Black,
                        thickness = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 4. Render daftar saran di dalam penampung bawah yang scrollable secara natural
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()).padding(bottom = 36.dp)
                    ) {
                        // Opsi Lokasi Saat Ini (Hanya saat aktif di mode Lokasi Awal)
                        if (activeMode == ActiveInputMode.ORIGIN) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (DeviceLocationHelper.hasLocationPermission(context)) {
                                            handleUseCurrentLocationForOrigin()
                                        } else {
                                            pendingLocationAction = handleUseCurrentLocationForOrigin
                                            locationPermissionLauncher.launch(
                                                arrayOf(
                                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                                )
                                            )
                                        }
                                    }
                                    .padding(vertical = 12.dp, horizontal = 4.dp)
                            ) {
                                if (isDetectingLocation) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.5.dp,
                                        color = Color(0xFF7542EF)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Lokasi Saat Ini",
                                        tint = Color.Black,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                                Text(
                                    text = if (isDetectingLocation) "Mendeteksi Lokasi GPS..." else "Lokasi Saat Ini",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF11133E)
                                    )
                                )
                            }
                            HorizontalDivider(
                                color = Color(0xFFE2E0FA),
                                thickness = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // State Loading saat mencari di Nominatim (hanya saat mode pencarian lokasi)
                        if (isSearchingLocation && (activeMode == ActiveInputMode.ORIGIN || activeMode == ActiveInputMode.DESTINATION)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF7542EF)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Mencari lokasi di OpenStreetMap...",
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        color = Color(0xFF303B65)
                                    )
                                )
                            }
                        } else {
                            val currentActiveQuery = when (activeMode) {
                                ActiveInputMode.ORIGIN -> originQuery
                                ActiveInputMode.DESTINATION -> destinationQuery
                                ActiveInputMode.VEHICLE -> vehicleQuery
                                ActiveInputMode.FUEL -> fuelQuery
                                ActiveInputMode.NONE -> ""
                            }
                            if (activeMode != ActiveInputMode.NONE && filteredSuggestions.isEmpty() && currentActiveQuery.trim().length >= 2) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp, horizontal = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Tidak ada lokasi ditemukan untuk \"$currentActiveQuery\".",
                                        style = TextStyle(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF303B65),
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                    if (activeMode == ActiveInputMode.ORIGIN || activeMode == ActiveInputMode.DESTINATION) {
                                        Button(
                                            onClick = {
                                                val pickMode = if (activeMode == ActiveInputMode.ORIGIN) MapPickMode.ORIGIN else MapPickMode.DESTINATION
                                                collapseBottomSheet()
                                                viewModel.startPickingLocation(pickMode)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7542EF)),
                                            shape = RoundedCornerShape(16.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Pilih Lokasi dari Peta",
                                                style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 5 & 6. Menampilkan item saran yang sudah terfilter sesuai kategori field yang aktif
                        val displaySuggestions = when (activeMode) {
                            ActiveInputMode.ORIGIN, ActiveInputMode.DESTINATION -> filteredSuggestions.take(10)
                            ActiveInputMode.VEHICLE, ActiveInputMode.FUEL -> filteredSuggestions
                            ActiveInputMode.NONE -> emptyList()
                        }

                        displaySuggestions.forEach { item ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        when (item) {
                                            is SearchSuggestionItem.Location -> {
                                                mapCameraTarget = LatLng(item.searchResult.lat, item.searchResult.lon)
                                                mapCameraTrigger = System.currentTimeMillis()

                                                if (activeMode == ActiveInputMode.ORIGIN) {
                                                    viewModel.selectOriginResult(item.searchResult)
                                                    originTextFieldValue = TextFieldValue(item.title, selection = TextRange(item.title.length))
                                                    originQuery = item.title
                                                    if (destinationQuery.isBlank() && destination.isBlank()) {
                                                        activeMode = ActiveInputMode.DESTINATION
                                                        viewModel.clearSearchResults()
                                                        coroutineScope.launch {
                                                            bottomSheetState.expand()
                                                            delay(50L)
                                                            destinationFocusRequester.requestFocus()
                                                        }
                                                    } else {
                                                        activeMode = ActiveInputMode.VEHICLE
                                                        viewModel.filterVehicles("")
                                                        coroutineScope.launch {
                                                            bottomSheetState.expand()
                                                            delay(50L)
                                                            vehicleFocusRequester.requestFocus()
                                                        }
                                                    }
                                                } else {
                                                    viewModel.selectDestinationResult(item.searchResult)
                                                    destinationTextFieldValue = TextFieldValue(item.title, selection = TextRange(item.title.length))
                                                    destinationQuery = item.title
                                                    activeMode = ActiveInputMode.VEHICLE
                                                    viewModel.filterVehicles("")
                                                    coroutineScope.launch {
                                                        bottomSheetState.expand()
                                                        delay(50L)
                                                        vehicleFocusRequester.requestFocus()
                                                    }
                                                }
                                            }
                                            is SearchSuggestionItem.Vehicle -> {
                                                viewModel.selectVehicle(item.vehicle)
                                                vehicleTextFieldValue = TextFieldValue(item.vehicle.name, selection = TextRange(item.vehicle.name.length))
                                                vehicleQuery = item.vehicle.name
                                                activeMode = ActiveInputMode.FUEL
                                                viewModel.filterFuels("")
                                                coroutineScope.launch {
                                                    bottomSheetState.expand()
                                                    delay(50L)
                                                    fuelFocusRequester.requestFocus()
                                                }
                                            }
                                            is SearchSuggestionItem.Fuel -> {
                                                viewModel.selectFuel(item.fuel)
                                                fuelTextFieldValue = TextFieldValue(item.fuel.name, selection = TextRange(item.fuel.name.length))
                                                fuelQuery = item.fuel.name
                                                collapseBottomSheet()
                                            }
                                        }
                                    }
                                    .padding(vertical = 11.dp, horizontal = 4.dp)
                            ) {
                                when (item) {
                                    is SearchSuggestionItem.Vehicle -> {
                                        val iconRes = if (item.vehicle.type.equals("Motor", ignoreCase = true)) R.drawable.motor else R.drawable.mobil
                                        Image(
                                            painter = painterResource(id = iconRes),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    is SearchSuggestionItem.Fuel -> {
                                        val iconRes = FuelIconResolver.resolveIcon(item.fuel.name)
                                        Image(
                                            painter = painterResource(id = iconRes),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    is SearchSuggestionItem.Location -> {
                                        // Text-only matching reference image or subtle pin
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = TextStyle(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF11133E)
                                        )
                                    )
                                    if (item.subtitle.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = item.subtitle,
                                            style = TextStyle(
                                                fontSize = 12.sp,
                                                color = Color(0xFF303B65)
                                            ),
                                            maxLines = 2
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(
                                color = Color(0xFFE2E0FA),
                                thickness = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

/**
 * Sub-komponen Input Field standar yang bersih dan konsisten dengan desain Figma/Mockup.
 * Mendukung kontrol kursor presisi menggunakan [TextFieldValue].
 */
@Composable
private fun EditableInputField(
    textFieldValue: TextFieldValue,
    placeholder: String,
    iconResId: Int?,
    isActive: Boolean,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onExplicitTap: () -> Unit = {},
    onValueChange: (TextFieldValue) -> Unit,
    onFocusChanged: (Boolean) -> Unit,
    imeAction: ImeAction = ImeAction.Default,
    onImeAction: () -> Unit = {},
    onIconClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(color = Color(0xFFCCDEFC), shape = RoundedCornerShape(8.dp))
            .border(
                width = if (isActive) 1.8.dp else 1.3.dp,
                color = if (isActive) Color(0xFF7542EF) else Color(0xFF303B65),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable {
                onExplicitTap()
                focusRequester.requestFocus()
                onFocusChanged(true)
            }
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF11133E)
                ),
                cursorBrush = SolidColor(Color(0xFF7542EF)),
                keyboardOptions = KeyboardOptions(imeAction = imeAction),
                keyboardActions = KeyboardActions(
                    onDone = { onImeAction() },
                    onNext = { onImeAction() },
                    onSearch = { onImeAction() }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { focusState ->
                        onFocusChanged(focusState.isFocused)
                    },
                decorationBox = { innerTextField ->
                    if (textFieldValue.text.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF11133E)
                            )
                        )
                    }
                    innerTextField()
                }
            )
        }

        if (iconResId != null) {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = placeholder,
                modifier = Modifier
                    .size(28.dp)
                    .clickable {
                        if (onIconClick != null) {
                            onIconClick()
                        } else {
                            focusRequester.requestFocus()
                            onFocusChanged(true)
                        }
                    }
            )
        }
    }
}



// Sub-komponen Tombol Melayang di Map
@Composable
private fun FloatingMapAction(
    iconResId: Int,
    isLoading: Boolean = false,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .shadow(6.dp, CircleShape)
            .background(Color(0xFFC0BEF8), CircleShape)
            .clickable(enabled = !isLoading) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.5.dp,
                color = Color(0xFF11133E)
            )
        } else {
            Image(
                painter = painterResource(id = iconResId),
                contentDescription = "Map Action",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Preview(showSystemUi = true, device = "spec:width=412dp,height=917dp,dpi=440")
@Composable
fun HomeScreenPreview() {
    HomeScreen(
        viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
        onNavigateToResult = {}
    )
}
