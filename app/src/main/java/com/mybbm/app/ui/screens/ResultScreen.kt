package com.mybbm.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mybbm.app.util.FuelCalculator
import com.mybbm.app.viewmodel.BbmViewModel

@Composable
fun ResultScreen(
    viewModel: BbmViewModel,
    onNavigateBack: () -> Unit,
    onViewSpbu: () -> Unit
) {
    val result by viewModel.calculationResult.collectAsState()
    val scrollState = rememberScrollState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (result == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Belum ada hasil perhitungan.", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateBack) {
                    Text("Kembali")
                }
            }
        } else {
            val res = result!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "📍 Ringkasan Perjalanan", style = MaterialTheme.typography.titleMedium)
                        HorizontalDivider()
                        Text(text = "Asal: ${res.originName}")
                        Text(text = "Tujuan: ${res.destinationName}")
                        Text(text = "Jarak: ${FuelCalculator.formatDistance(res.distanceKm)}")
                        Text(text = "Kendaraan: ${res.vehicleName} (${res.fuelEfficiency} km/L)")
                        Text(text = "Jenis BBM: ${res.fuelType}")
                        Text(text = "Harga/Liter: ${FuelCalculator.formatRupiah(res.pricePerLiter)}")
                    }
                }

                // Estimation Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "💡 Hasil Estimasi",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        HorizontalDivider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Kebutuhan BBM:", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = FuelCalculator.formatLiters(res.totalLiters),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Estimasi Biaya:", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = FuelCalculator.formatRupiah(res.totalCost),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Actions
                Button(
                    onClick = onViewSpbu,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("🔍 Lihat SPBU Sepanjang Rute")
                }

                OutlinedButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Hitung Perjalanan Baru")
                }
            }
        }
    }
}
