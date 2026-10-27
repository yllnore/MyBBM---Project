package com.mybbm.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mybbm.app.util.FuelCalculator
import com.mybbm.app.viewmodel.BbmViewModel

@Composable
fun SettingsScreen(viewModel: BbmViewModel) {
    val fuelPrices by viewModel.fuelPrices.collectAsState()
    val vehicles by viewModel.vehicles.collectAsState()

    var showAddVehicleDialog by remember { mutableStateOf(false) }
    var editFuelType by remember { mutableStateOf<String?>(null) }
    var editPriceText by remember { mutableStateOf("") }

    var newVehicleName by remember { mutableStateOf("") }
    var newVehicleType by remember { mutableStateOf("Mobil") }
    var newVehicleEfficiency by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(text = "Pengaturan Aplikasi", style = MaterialTheme.typography.headlineMedium)
                Text(text = "Wilayah: Kalimantan Timur", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }

            // Fuel Prices Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "⛽ Harga BBM (per Liter)", style = MaterialTheme.typography.titleMedium)
                        Text(text = "Sesuaikan harga referensi BBM wilayah Kaltim.", style = MaterialTheme.typography.bodySmall)
                        HorizontalDivider()

                        fuelPrices.forEach { fuel ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = fuel.fuelType, style = MaterialTheme.typography.bodyLarge)
                                    Text(
                                        text = FuelCalculator.formatRupiah(fuel.pricePerLiter),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                TextButton(onClick = {
                                    editFuelType = fuel.fuelType
                                    editPriceText = fuel.pricePerLiter.toInt().toString()
                                }) {
                                    Icon(Icons.Default.Edit, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Ubah")
                                }
                            }
                        }
                    }
                }
            }

            // Vehicles Management Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "🚗 Daftar Kendaraan", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = { showAddVehicleDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Tambah")
                    }
                }
            }

            items(vehicles) { vehicle ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = vehicle.name, style = MaterialTheme.typography.titleSmall)
                            Text(text = "Jenis: ${vehicle.type} | Konsumsi: ${vehicle.fuelEfficiency} km/L", style = MaterialTheme.typography.bodySmall)
                        }
                        if (!vehicle.isDefault) {
                            IconButton(onClick = { viewModel.deleteVehicle(vehicle) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                            }
                        } else {
                            Text(text = "Bawaan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }

    // Edit Fuel Price Dialog
    if (editFuelType != null) {
        AlertDialog(
            onDismissRequest = { editFuelType = null },
            title = { Text("Ubah Harga ${editFuelType}") },
            text = {
                OutlinedTextField(
                    value = editPriceText,
                    onValueChange = { editPriceText = it },
                    label = { Text("Harga per Liter (Rp)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val price = editPriceText.toDoubleOrNull()
                    if (price != null && price >= 0) {
                        viewModel.updateFuelPrice(editFuelType!!, price)
                    }
                    editFuelType = null
                }) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { editFuelType = null }) {
                    Text("Batal")
                }
            }
        )
    }

    // Add Vehicle Dialog
    if (showAddVehicleDialog) {
        AlertDialog(
            onDismissRequest = { showAddVehicleDialog = false },
            title = { Text("Tambah Kendaraan Baru") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newVehicleName,
                        onValueChange = { newVehicleName = it },
                        label = { Text("Nama Kendaraan (Cth: Motorku)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newVehicleEfficiency,
                        onValueChange = { newVehicleEfficiency = it },
                        label = { Text("Konsumsi BBM (km/L)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val eff = newVehicleEfficiency.toDoubleOrNull()
                    if (newVehicleName.isNotBlank() && eff != null && eff > 0) {
                        viewModel.addVehicle(newVehicleName, newVehicleType, eff)
                        newVehicleName = ""
                        newVehicleEfficiency = ""
                        showAddVehicleDialog = false
                    }
                }) {
                    Text("Tambah")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddVehicleDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
