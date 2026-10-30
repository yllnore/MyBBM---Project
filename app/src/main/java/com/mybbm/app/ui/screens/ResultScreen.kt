package com.mybbm.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mybbm.app.R
import com.mybbm.app.domain.model.SpbuInfo
import com.mybbm.app.util.FuelCalculator
import com.mybbm.app.viewmodel.BbmViewModel

@Composable
fun ResultScreen(
    viewModel: BbmViewModel,
    onNavigateBack: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {}
) {
    val result by viewModel.calculationResult.collectAsState()

    val configuration = LocalConfiguration.current
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }

    var isExpanded by remember { mutableStateOf(false) }
    var sheetHeightFraction by remember { mutableStateOf(0.44f) }

    LaunchedEffect(isExpanded) {
        sheetHeightFraction = if (isExpanded) 0.92f else 0.44f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8ECEF)) // Map simulation background
    ) {
        // 1. Map Canvas Simulation with Route Line & Gas Station Markers
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 300.dp)
        ) {
            // Google Map Watermark / Indicator at bottom left of map
            Text(
                text = "Google",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0x995F6368)
                ),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp)
            )

            // Back button on top left of map
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(top = 40.dp, start = 16.dp)
                    .size(44.dp)
                    .shadow(4.dp, CircleShape)
                    .background(Color.White, CircleShape)
                    .clickable { onNavigateBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color(0xFF090F59),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Top Right Floating Action Buttons (FABs)
            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 40.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(6.dp, CircleShape)
                        .background(Color(0xFFC0BEF8), CircleShape)
                        .clickable { onNavigateToSettings() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.settings_icon),
                        contentDescription = "Settings",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(6.dp, CircleShape)
                        .background(Color(0xFFC0BEF8), CircleShape)
                        .clickable { onNavigateToHistory() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.history),
                        contentDescription = "Histori",
                        modifier = Modifier.size(24.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(6.dp, CircleShape)
                        .background(Color(0xFFC0BEF8), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.current_location_icon),
                        contentDescription = "Layer",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // 2. Result Bottom Sheet (Draggable & Expandable)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(sheetHeightFraction)
                .shadow(
                    elevation = 24.dp,
                    spotColor = Color(0x40000000),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .background(
                    color = Color(0xFFC0BEF8), // Lilac
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag Handle Bar (Draggable & Clickable)
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
                        }
                    )
                    .clickable { isExpanded = !isExpanded },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(4.dp)
                        .background(
                            color = Color(0xFF7289D2),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Result Card (Lilac container with 2 columns)
            val totalCostText = result?.let { FuelCalculator.formatRupiah(it.totalCost) } ?: "Rp. 85.000"
            val totalLiterText = result?.let { FuelCalculator.formatLiters(it.totalLiters) } ?: "6.8 Liter"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFA59EF2))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Total Biaya BBM
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "TOTAL BIAYA BBM -",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF090F59),
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = totalCostText,
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF090F59)
                            )
                        )
                    }

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight(0.7f)
                            .background(Color(0x50090F59))
                    )

                    // Right Column: Estimasi BBM
                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .padding(start = 12.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "ESTIMASI BBM -",
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF090F59),
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = totalLiterText,
                            style = TextStyle(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF090F59)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Kembali ke Beranda (Posisi di atas SPBU Terdekat)
            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7542EF)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "Kembali ke Beranda",
                    style = TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Content (SPBU Terdekat muncul saat di-drag ke atas)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // SPBU Terdekat Section Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "SPBU Terdekat:",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF090F59)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // SPBU List Items
                val spbuList = result?.nearbySpbu ?: listOf(
                    SpbuInfo("SPBU Pertamina", "1,2 km"),
                    SpbuInfo("SPBU Shell", "2,1 km")
                )

                spbuList.forEach { spbu ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .background(Color(0xFFF3F1FF), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.pertamax),
                                contentDescription = "SPBU",
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = spbu.name,
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF090F59)
                                )
                            )
                        }
                        Text(
                            text = spbu.distanceFormatted,
                            style = TextStyle(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF090F59)
                            )
                        )
                    }
                }
            }
        }
    }
}
