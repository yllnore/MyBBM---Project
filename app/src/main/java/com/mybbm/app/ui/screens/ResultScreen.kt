package com.mybbm.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mybbm.app.domain.engine.FuelCostEngine
import com.mybbm.app.util.FuelCalculator
import com.mybbm.app.viewmodel.BbmViewModel

@Composable
fun ResultScreen(
    viewModel: BbmViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val result by viewModel.calculationResult.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFFFF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .shadow(elevation = 16.dp, spotColor = Color(0x4051667B))
                .fillMaxSize()
                .background(Color(0xFFBDB8F8))
                .padding(start = 20.dp, top = 40.dp, end = 20.dp, bottom = 24.dp)
        ) {
            // Header Top Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color(0xFF090F59),
                        modifier = Modifier.size(28.dp)
                    )
                }
                Text(
                    text = "Estimasi Biaya Perjalanan",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF090F59)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rincian Input Field (Sesuai Desain Figma Ter-isiSemua.kt)
            if (result != null) {
                val res = result!!
                ItemFieldText(text = "Asal: ${res.originName}")
                Spacer(modifier = Modifier.height(10.dp))
                ItemFieldText(text = "Tujuan: ${res.destinationName}")
                Spacer(modifier = Modifier.height(10.dp))
                ItemFieldText(text = "Kendaraan: ${res.vehicleName} (${res.fuelEfficiency} km/L)")
                Spacer(modifier = Modifier.height(10.dp))
                ItemFieldText(text = "BBM: ${res.fuelType} (${FuelCalculator.formatRupiah(res.pricePerLiter)}/L)")

                Spacer(modifier = Modifier.height(20.dp))

                // Card Hasil Estimasi Biaya
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp), spotColor = Color(0x306765AA)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF7043EC))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ESTIMASI TOTAL BIAYA",
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD4D4F7),
                                letterSpacing = 1.sp
                            )
                        )
                        Text(
                            text = FuelCalculator.formatRupiah(res.totalCost),
                            style = TextStyle(
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = Color(0x40FFFFFF)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Kebutuhan Liter",
                                    style = TextStyle(fontSize = 12.sp, color = Color(0xFFD4D4F7))
                                )
                                Text(
                                    text = FuelCalculator.formatLiters(res.totalLiters),
                                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Jarak Tempuh",
                                    style = TextStyle(fontSize = 12.sp, color = Color(0xFFD4D4F7))
                                )
                                Text(
                                    text = FuelCalculator.formatDistance(res.distanceKm),
                                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tombol Kembali Hitung Ulang
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF090F59)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Hitung Rute Lain",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    )
                }
            } else {
                Text(
                    text = "Belum ada perhitungan.",
                    style = TextStyle(color = Color(0xFF090F59), fontSize = 16.sp)
                )
            }
        }
    }
}
