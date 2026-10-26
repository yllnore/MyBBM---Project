package com.example.mybbm.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.mybbm.viewmodel.BbmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: BbmViewModel,
    onNavigateToResult: () -> Unit
) {
    val origin by viewModel.originText.collectAsState()
    val destination by viewModel.destinationText.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()
    val fuelPrices by viewModel.fuelPrices.collectAsState()
    val selectedVehicle by viewModel.selectedVehicle.collectAsState()
    val selectedFuelPrice by viewModel.selectedFuelPrice.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var vehicleExpanded by remember { mutableStateOf(false) }
    var fuelExpanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🚗 MYBBM - Kalkulator Perjalanan",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Hitung estimasi BBM & biaya perjalanan Anda di Kalimantan Timur.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Location Input Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Rute Perjalanan", style = MaterialTheme.typography.titleSmall)

                    OutlinedTextField(
                        value = origin,
                        onValueChange = { viewModel.originText.value = it },
                        label = { Text("Lokasi Asal (Cth: Samarinda)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        IconButton(onClick = { viewModel.swapLocations() }) {
                            Icon(Icons.Default.SwapVert, contentDescription = "Tukar Lokasi")
                        }
                    }

                    OutlinedTextField(
                        value = destination,
                        onValueChange = { viewModel.destinationText.value = it },
                        label = { Text("Lokasi Tujuan (Cth: Balikpapan)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Vehicle & Fuel Selection Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(text = "Kendaraan & Bahan Bakar", style = MaterialTheme.typography.titleSmall)

                    // Vehicle Dropdown
                    ExposedDropdownMenuBox(
                        expanded = vehicleExpanded,
                        onExpandedChange = { vehicleExpanded = !vehicleExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedVehicle?.let { "${it.name} (${it.fuelEfficiency} km/L)" } ?: "Pilih Kendaraan",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Kendaraan") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = vehicleExpanded,
                            onDismissRequest = { vehicleExpanded = false }
                        ) {
                            vehicles.forEach { vehicle ->
                                DropdownMenuItem(
                                    text = { Text("${vehicle.name} — ${vehicle.fuelEfficiency} km/L") },
                                    onClick = {
                                        viewModel.selectedVehicle.value = vehicle
                                        vehicleExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Fuel Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = fuelExpanded,
                        onExpandedChange = { fuelExpanded = !fuelExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedFuelPrice?.let { "${it.fuelType} (Rp${it.pricePerLiter.toInt()}/L)" } ?: "Pilih BBM",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Jenis BBM") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fuelExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = fuelExpanded,
                            onDismissRequest = { fuelExpanded = false }
                        ) {
                            fuelPrices.forEach { fuel ->
                                DropdownMenuItem(
                                    text = { Text("${fuel.fuelType} — Rp${fuel.pricePerLiter.toInt()}/L") },
                                    onClick = {
                                        viewModel.selectedFuelPrice.value = fuel
                                        fuelExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (!errorMessage.isNullOrEmpty()) {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Calculate Button
            Button(
                onClick = {
                    viewModel.calculateTrip(115.0) // Sample distance Kaltim (e.g. Samarinda - Balikpapan ~115 km)
                    if (viewModel.errorMessage.value == null) {
                        onNavigateToResult()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Hitung Biaya BBM", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
