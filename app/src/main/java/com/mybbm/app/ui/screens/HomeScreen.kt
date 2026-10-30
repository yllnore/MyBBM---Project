package com.mybbm.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mybbm.app.R
import com.mybbm.app.viewmodel.BbmViewModel

@Composable
fun HomeScreen(
    viewModel: BbmViewModel,
    onNavigateToResult: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {}
) {
    val origin by viewModel.originText.collectAsState()
    val destination by viewModel.destinationText.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val fuelPrices by viewModel.fuelPrices.collectAsState()
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()
    val selectedFuelPrice by viewModel.selectedFuelPrice.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showVehicleDialog by remember { mutableStateOf(false) }
    var showFuelDialog by remember { mutableStateOf(false) }

    // 1. Root Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFECEBFF))
    ) {

        // Placeholder Area Map / Tampilan Peta
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.60f)
                .background(Color(0xFFDFE6F9)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.current_location_icon),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "Kalimantan Timur",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF303B65)
                    )
                )
            }
        }

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
                onClick = { /* Fokus lokasi saat ini */ }
            )
        }

        // 3. Panel Input Perjalanan (Bottom Sheet)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    spotColor = Color(0x40000000),
                    ambientColor = Color(0x40000000),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .background(
                    color = Color(0xFFC0BEF8), // Warna ungu muda
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Garis Handle (Drag Handle)
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        color = Color(0xFF7289D2),
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Form Input
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Input Lokasi Awal
                LocationInputField(
                    value = origin,
                    placeholder = "Lokasi Awal",
                    iconResId = R.drawable.lokasi_awal,
                    onValueChange = { viewModel.originText.value = it }
                )

                // Input Lokasi Tujuan
                LocationInputField(
                    value = destination,
                    placeholder = "Lokasi Tujuan",
                    iconResId = R.drawable.lokasi_tujuan,
                    onValueChange = { viewModel.destinationText.value = it }
                )

                // Input Pilihan Kendaraan
                SelectionInputField(
                    text = selectedVehicle?.let { "${it.name} (${it.fuelEfficiency} km/L)" } ?: "Pilih Kendaraan",
                    iconResId = R.drawable.mobil,
                    onClick = { showVehicleDialog = true }
                )

                // Input Pilihan Jenis BBM
                SelectionInputField(
                    text = selectedFuelPrice?.let { "${it.fuelType} (Rp ${it.pricePerLiter.toInt()}/L)" } ?: "Jenis BBM",
                    iconResId = R.drawable.solar,
                    onClick = { showFuelDialog = true }
                )
            }

            if (!errorMessage.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = errorMessage ?: "",
                    color = Color(0xFFD32F2F),
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tombol Hitung Biaya
            Button(
                onClick = {
                    viewModel.calculateTrip {
                        onNavigateToResult()
                    }
                },
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7542EF)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(
                        elevation = 8.dp,
                        spotColor = Color(0x5544308D),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
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
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // Modal Pilih Kendaraan
    if (showVehicleDialog) {
        AlertDialog(
            onDismissRequest = { showVehicleDialog = false },
            title = { Text("Pilih Kendaraan") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    vehicles.forEach { vehicle ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectedVehicle.value = vehicle
                                    showVehicleDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedVehicle?.id == vehicle.id) Color(0xFFC0BEF8) else Color(0xFFF1EFFF)
                            )
                        ) {
                            Text(
                                text = "${vehicle.name} (${vehicle.fuelEfficiency} km/L)",
                                modifier = Modifier.padding(14.dp),
                                style = TextStyle(fontWeight = FontWeight.Medium, color = Color(0xFF090F59))
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Modal Pilih BBM
    if (showFuelDialog) {
        AlertDialog(
            onDismissRequest = { showFuelDialog = false },
            title = { Text("Pilih Jenis BBM") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    fuelPrices.forEach { fuel ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectedFuelPrice.value = fuel
                                    showFuelDialog = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (selectedFuelPrice?.idHarga == fuel.idHarga) Color(0xFFC0BEF8) else Color(0xFFF1EFFF)
                            )
                        ) {
                            Text(
                                text = "${fuel.fuelType} · Rp ${fuel.pricePerLiter.toInt()}/L",
                                modifier = Modifier.padding(14.dp),
                                style = TextStyle(fontWeight = FontWeight.Medium, color = Color(0xFF090F59))
                            )
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

// Sub-komponen Input Lokasi
@Composable
private fun LocationInputField(
    value: String,
    placeholder: String,
    iconResId: Int,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(color = Color(0xFFDFE6F9), shape = RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = Color(0xFF303B65), shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = TextStyle(
                fontSize = 15.sp,
                color = Color(0xFF11133E)
            ),
            singleLine = true,
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(fontSize = 15.sp, color = Color(0xFF8B92A8))
                    )
                }
                innerTextField()
            }
        )
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = placeholder,
            modifier = Modifier.size(24.dp)
        )
    }
}

// Sub-komponen Input Seleksi (Dropdown)
@Composable
private fun SelectionInputField(
    text: String,
    iconResId: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(color = Color(0xFFDFE6F9), shape = RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = Color(0xFF303B65), shape = RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 15.sp,
                color = Color(0xFF11133E)
            ),
            modifier = Modifier.weight(1f)
        )
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(22.dp)
        )
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
