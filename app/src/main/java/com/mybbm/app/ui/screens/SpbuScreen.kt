package com.mybbm.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

data class SpbuItem(
    val name: String,
    val address: String,
    val distanceKmFromStart: Double,
    val lat: Double,
    val lng: Double
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpbuScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    // Sample relevant SPBU along Kaltim route (Samarinda - Balikpapan corridor)
    val spbuList = remember {
        listOf(
            SpbuItem("SPBU Pertamina 64.751.01", "Jl. Cipto Mangunkusumo, Samarinda Seberang", 12.5, -0.525, 117.123),
            SpbuItem("SPBU Pertamina 64.752.05", "Jl. Poros Samarinda - Balikpapan Km 38", 45.0, -0.854, 117.012),
            SpbuItem("SPBU Pertamina 64.761.12", "Jl. Soekarno Hatta Km 15, Balikpapan", 98.2, -1.152, 116.923),
            SpbuItem("SPBU Pertamina 64.761.02", "Jl. MT Haryono, Balikpapan", 112.0, -1.234, 116.856)
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("SPBU Sepanjang Rute (Kaltim)") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Ditemukan ${spbuList.size} SPBU di koridor rute perjalanan Anda.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                items(spbuList) { spbu ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(
                                        Icons.Default.LocalGasStation,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Text(text = spbu.name, style = MaterialTheme.typography.titleMedium)
                                }
                                Text(
                                    text = "~${spbu.distanceKmFromStart} km",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(text = spbu.address, style = MaterialTheme.typography.bodySmall)

                            Button(
                                onClick = {
                                    val gmmIntentUri = Uri.parse("google.navigation:q=${spbu.lat},${spbu.lng}&mode=d")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                                    mapIntent.setPackage("com.google.android.apps.maps")
                                    try {
                                        context.startActivity(mapIntent)
                                    } catch (e: Exception) {
                                        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${spbu.lat},${spbu.lng}"))
                                        context.startActivity(webIntent)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Navigasi ke SPBU Ini")
                            }
                        }
                    }
                }
            }
        }
    }
}
