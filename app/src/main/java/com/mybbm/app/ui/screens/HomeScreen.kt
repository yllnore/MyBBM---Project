package com.mybbm.app.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import com.mybbm.app.R
import com.mybbm.app.domain.model.FuelModel
import com.mybbm.app.domain.model.VehicleModel
import com.mybbm.app.viewmodel.BbmViewModel

enum class ActiveInputMode {
    NONE,
    ORIGIN,
    DESTINATION,
    VEHICLE,
    FUEL
}

data class LocationSuggestion(
    val title: String,
    val subtitle: String
)

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
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()
    val selectedFuel by viewModel.selectedFuel.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val fuels by viewModel.fuels.collectAsState()

    // Bottom sheet expansion state
    var isExpanded by remember { mutableStateOf(false) }
    var activeMode by remember { mutableStateOf(ActiveInputMode.NONE) }

    // Search query states for filtering when typing
    var originQuery by remember { mutableStateOf("") }
    var destinationQuery by remember { mutableStateOf("") }
    var vehicleQuery by remember { mutableStateOf("") }
    var fuelQuery by remember { mutableStateOf("") }

    // Sync input with selection
    LaunchedEffect(origin) { originQuery = origin }
    LaunchedEffect(destination) { destinationQuery = destination }
    LaunchedEffect(selectedVehicle) { vehicleQuery = selectedVehicle?.name ?: "" }
    LaunchedEffect(selectedFuel) { fuelQuery = selectedFuel?.name ?: "" }

    val configuration = LocalConfiguration.current
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }

    var sheetHeightFraction by remember { mutableStateOf(0.44f) }

    // Sync sheetHeightFraction with isExpanded state
    LaunchedEffect(isExpanded) {
        sheetHeightFraction = if (isExpanded) 0.92f else 0.44f
    }

    // Address Suggestions Data (Kalimantan Timur acuan PRD & Figma)
    val originSuggestions = remember {
        listOf(
            LocationSuggestion("POLNES - Politeknik Negeri Samarinda", "Jl. Dr. Cipto Mangunkusumo, Samarinda Seberang"),
            LocationSuggestion("Samarinda Seberang", "Kota Samarinda, Kalimantan Timur"),
            LocationSuggestion("Big Mall Samarinda", "Jl. Untung Suropati No.99, Karang Asam Ulu, Samarinda"),
            LocationSuggestion("Jembatan Mahakam IV", "Kota Samarinda, Kalimantan Timur"),
            LocationSuggestion("Universitas Mulawarman (UNMUL)", "Jl. Kuaro, Gn. Kelua, Samarinda")
        )
    }

    val destinationSuggestions = remember {
        listOf(
            LocationSuggestion("Balikpapan", "Kota Balikpapan, Kalimantan Timur, Indonesia"),
            LocationSuggestion("Balikpapan Kota", "Kota Balikpapan, Kalimantan Timur, Indonesia"),
            LocationSuggestion("Balikpapan Superblock (BSB)", "Jalan Jenderal Sudirman, Gunung Bahagia, Kota Balikpapan, Kalimantan Timur, Indonesia"),
            LocationSuggestion("Balikpapan Selatan", "Kota Balikpapan, Kalimantan Timur, Indonesia"),
            LocationSuggestion("Pantai Manggar Segarasari", "Manggar Baru, Balikpapan Timur"),
            LocationSuggestion("Bandara SAMS Sepinggan", "Jl. Marsma R. Iswahyudi, Sepinggan, Balikpapan")
        )
    }

    // 1. Root Container (Latar Belakang Simulasi Map)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8ECEF))
    ) {
        // Google Map Watermark at bottom left
        Text(
            text = "Google",
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0x995F6368)
            ),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
        )

        // 2. Tombol Mengambang di Kanan Atas (FABs)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp),
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
                onClick = onNavigateToVehicles
            )
        }

        // 3. Panel Input Perjalanan (Draggable Bottom Sheet)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(sheetHeightFraction)
                .shadow(
                    elevation = 24.dp,
                    spotColor = Color(0x40000000),
                    ambientColor = Color(0x40000000),
                    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
                )
                .background(
                    color = Color(0xFFC0BEF8), // Warna ungu muda lilac Figma
                    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
                )
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle Bar (Bisa di drag terus menerus mengikuti gerakan jari)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            val change = -delta / screenHeightPx
                            sheetHeightFraction = (sheetHeightFraction + change).coerceIn(0.44f, 0.92f)
                            isExpanded = sheetHeightFraction > 0.68f
                        },
                        onDragStopped = { velocity ->
                            isExpanded = if (velocity < -400f || sheetHeightFraction > 0.65f) {
                                true
                            } else {
                                false
                            }
                            sheetHeightFraction = if (isExpanded) 0.92f else 0.44f
                            if (!isExpanded) {
                                activeMode = ActiveInputMode.NONE
                            }
                        }
                    )
                    .clickable { isExpanded = !isExpanded },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(5.dp)
                        .background(
                            color = Color(0xFF7289D2),
                            shape = RoundedCornerShape(3.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Form Input (4 Fields: Lokasi Awal, Tujuan, Kendaraan, BBM)
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Lokasi Awal
                EditableInputField(
                    value = originQuery,
                    placeholder = "Lokasi Awal",
                    iconResId = R.drawable.lokasi_awal,
                    isActive = activeMode == ActiveInputMode.ORIGIN,
                    onValueChange = {
                        originQuery = it
                        viewModel.setOrigin(it)
                    },
                    onFocus = {
                        activeMode = ActiveInputMode.ORIGIN
                        isExpanded = true
                    }
                )

                // 2. Lokasi Tujuan
                EditableInputField(
                    value = destinationQuery,
                    placeholder = "Lokasi Tujuan",
                    iconResId = R.drawable.lokasi_tujuan,
                    isActive = activeMode == ActiveInputMode.DESTINATION,
                    onValueChange = {
                        destinationQuery = it
                        viewModel.setDestination(it)
                    },
                    onFocus = {
                        activeMode = ActiveInputMode.DESTINATION
                        isExpanded = true
                    }
                )

                // 3. Pilih Kendaraan
                EditableInputField(
                    value = vehicleQuery,
                    placeholder = "Pilih Kendaraan",
                    iconResId = null,
                    isActive = activeMode == ActiveInputMode.VEHICLE,
                    onValueChange = {
                        vehicleQuery = it
                    },
                    onFocus = {
                        activeMode = ActiveInputMode.VEHICLE
                        isExpanded = true
                    }
                )

                // 4. Jenis BBM
                EditableInputField(
                    value = fuelQuery,
                    placeholder = "Jenis BBM",
                    iconResId = null,
                    isActive = activeMode == ActiveInputMode.FUEL,
                    onValueChange = {
                        fuelQuery = it
                    },
                    onFocus = {
                        activeMode = ActiveInputMode.FUEL
                        isExpanded = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Hitung Biaya Perjalanan
            Button(
                onClick = {
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

            // Jika sedang expanded, tampilkan area di bawah tombol Hitung
            if (isExpanded) {
                Spacer(modifier = Modifier.height(14.dp))

                // Garis Pembatas Horizontal Sesuai Figma
                HorizontalDivider(
                    color = Color(0xFF303B65),
                    thickness = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Konten Rekomendasi / Pilihan di Bawah Garis
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (activeMode) {
                        ActiveInputMode.ORIGIN -> {
                            // Item 1: Opsi "Lokasi Saat Ini" (sesuai Gambar 3)
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setOrigin("Lokasi Saat Ini")
                                            originQuery = "Lokasi Saat Ini"
                                            activeMode = ActiveInputMode.DESTINATION
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Lokasi Saat Ini",
                                        tint = Color.Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Lokasi Saat Ini",
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF11133E)
                                        )
                                    )
                                }
                                HorizontalDivider(color = Color(0x30303B65), thickness = 0.8.dp)
                            }

                            // Filter daftar alamat berdasarkan kata kunci yang diketik user
                            val filteredOrigins = originSuggestions.filter {
                                originQuery.isEmpty() || it.title.contains(originQuery, ignoreCase = true) || it.subtitle.contains(originQuery, ignoreCase = true)
                            }

                            items(filteredOrigins) { item ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setOrigin(item.title)
                                            originQuery = item.title
                                            activeMode = ActiveInputMode.DESTINATION
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = item.title,
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF11133E)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.subtitle,
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            color = Color(0xFF303B65)
                                        )
                                    )
                                }
                                HorizontalDivider(color = Color(0x30303B65), thickness = 0.8.dp)
                            }
                        }

                        ActiveInputMode.DESTINATION -> {
                            val filteredDestinations = destinationSuggestions.filter {
                                destinationQuery.isEmpty() || it.title.contains(destinationQuery, ignoreCase = true) || it.subtitle.contains(destinationQuery, ignoreCase = true)
                            }

                            items(filteredDestinations) { item ->
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setDestination(item.title)
                                            destinationQuery = item.title
                                            activeMode = ActiveInputMode.VEHICLE
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp)
                                ) {
                                    Text(
                                        text = item.title,
                                        style = TextStyle(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF11133E)
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = item.subtitle,
                                        style = TextStyle(
                                            fontSize = 12.sp,
                                            color = Color(0xFF303B65)
                                        )
                                    )
                                }
                                HorizontalDivider(color = Color(0x30303B65), thickness = 0.8.dp)
                            }
                        }

                        ActiveInputMode.VEHICLE -> {
                            // Tampilkan daftar Kendaraan di bawah garis pemisah (sesuai Gambar 4)
                            val filteredVehicles = vehicles.filter {
                                vehicleQuery.isEmpty() || it.name.contains(vehicleQuery, ignoreCase = true)
                            }

                            items(filteredVehicles) { vehicle ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectVehicle(vehicle)
                                            vehicleQuery = vehicle.name
                                            activeMode = ActiveInputMode.FUEL
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp)
                                ) {
                                    val iconRes = if (vehicle.type.equals("Motor", ignoreCase = true)) R.drawable.motor else R.drawable.mobil
                                    Image(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = vehicle.name,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Column {
                                        Text(
                                            text = vehicle.name,
                                            style = TextStyle(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF11133E)
                                            )
                                        )
                                        Text(
                                            text = "${vehicle.type} · ${vehicle.fuelEfficiency} km/L",
                                            style = TextStyle(
                                                fontSize = 12.sp,
                                                color = Color(0xFF303B65)
                                            )
                                        )
                                    }
                                }
                                HorizontalDivider(color = Color(0x30303B65), thickness = 0.8.dp)
                            }
                        }

                        ActiveInputMode.FUEL -> {
                            // Tampilkan daftar Jenis BBM di bawah garis pemisah (sesuai Gambar 5)
                            val filteredFuels = fuels.filter {
                                fuelQuery.isEmpty() || it.name.contains(fuelQuery, ignoreCase = true)
                            }

                            items(filteredFuels) { fuel ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.selectFuel(fuel)
                                            fuelQuery = fuel.name
                                            activeMode = ActiveInputMode.NONE
                                        }
                                        .padding(vertical = 10.dp, horizontal = 4.dp)
                                ) {
                                    val iconRes = fuel.iconResId ?: R.drawable.pertamax
                                    Image(
                                        painter = painterResource(id = iconRes),
                                        contentDescription = fuel.name,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Column {
                                        Text(
                                            text = fuel.name,
                                            style = TextStyle(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF11133E)
                                            )
                                        )
                                        Text(
                                            text = "Rp ${fuel.pricePerLiter} / Liter",
                                            style = TextStyle(
                                                fontSize = 12.sp,
                                                color = Color(0xFF303B65)
                                            )
                                        )
                                    }
                                }
                                HorizontalDivider(color = Color(0x30303B65), thickness = 0.8.dp)
                            }
                        }

                        ActiveInputMode.NONE -> {
                            item {
                                Text(
                                    text = "Pilih atau ketik lokasi, kendaraan, atau jenis BBM di atas.",
                                    style = TextStyle(
                                        fontSize = 13.sp,
                                        color = Color(0xFF303B65)
                                    ),
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Sub-komponen Input Field yang bisa diketik langsung & fokus
@Composable
private fun EditableInputField(
    value: String,
    placeholder: String,
    iconResId: Int?,
    isActive: Boolean,
    onValueChange: (String) -> Unit,
    onFocus: () -> Unit
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
            .clickable { onFocus() }
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
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF11133E)
                ),
                cursorBrush = SolidColor(Color(0xFF7542EF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFocus() },
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
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
                    .size(24.dp)
                    .clickable { onFocus() }
            )
        }
    }
}

// Sub-komponen Tombol Melayang di Map
@Composable
private fun FloatingMapAction(
    iconResId: Int,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .shadow(6.dp, CircleShape)
            .background(Color(0xFFC0BEF8), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = "Map Action",
            modifier = Modifier.size(24.dp)
        )
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
