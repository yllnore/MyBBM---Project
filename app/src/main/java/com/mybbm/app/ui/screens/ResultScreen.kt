package com.mybbm.app.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.mybbm.app.R
import com.mybbm.app.domain.model.LatLng
import com.mybbm.app.domain.model.SpbuInfo
import com.mybbm.app.ui.components.MapVisualOverlay
import com.mybbm.app.ui.components.OsmMapView
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
    val originCoord by viewModel.originCoord.collectAsState()
    val destinationCoord by viewModel.destinationCoord.collectAsState()
    val routePoints by viewModel.routePoints.collectAsState()
    val resultMapCenter by viewModel.resultMapCenter.collectAsState()
    val resultMapZoom by viewModel.resultMapZoom.collectAsState()

    val configuration = LocalConfiguration.current
    val screenHeightPx = with(LocalDensity.current) { configuration.screenHeightDp.dp.toPx() }

    var isExpanded by remember { mutableStateOf(false) }
    var sheetHeightFraction by remember { mutableStateOf(0.44f) }
    var resultMapTarget by remember { mutableStateOf<LatLng?>(null) }
    var resultMapTrigger by remember { mutableLongStateOf(0L) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isExpanded) {
        sheetHeightFraction = if (isExpanded) 0.92f else 0.44f
    }

    val nearbySpbu = result?.nearbySpbu ?: emptyList()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. Latar Belakang Peta OSM Interaktif (Tema Terang #E8ECEF / #ECEBFF & Polyline Halus #7542EF)
        OsmMapView(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 260.dp),
            centerCoordinate = LatLng(-0.5021, 117.1536),
            zoomLevel = 12.0,
            savedCameraCenter = resultMapCenter,
            savedCameraZoom = resultMapZoom,
            onCameraMoved = { center, zoom ->
                viewModel.updateResultMapCamera(center, zoom)
            },
            originCoordinate = originCoord,
            originLabel = result?.originName ?: "Asal",
            destinationCoordinate = destinationCoord,
            destinationLabel = result?.destinationName ?: "Tujuan",
            routePoints = result?.routePoints?.ifEmpty { routePoints } ?: routePoints,
            spbuCoordinates = nearbySpbu.filter { it.lat != 0.0 && it.lon != 0.0 }.map { LatLng(it.lat, it.lon) },
            cameraTarget = resultMapTarget,
            cameraZoom = 16.5,
            cameraTrigger = resultMapTrigger
        )

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
                    .shadow(4.dp, CircleShape)
                    .background(Color(0xFFE5E7EB), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.current_location_icon),
                    contentDescription = "Disabled",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color(0xFF9CA3AF))
                )
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
                    text = "Hitung perjalanan lain",
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

                // SPBU List Items (Data riil dari OpenStreetMap Overpass API)
                val spbuList = result?.nearbySpbu.orEmpty()

                if (spbuList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color(0xFF7542EF),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Mencari SPBU terdekat...",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF303B65)
                                )
                            )
                        }
                    }
                } else {
                    var expandedSpbuIndex by remember { mutableStateOf<Int?>(null) }
                    
                    spbuList.forEachIndexed { index, spbu ->
                        val isCardExpanded = expandedSpbuIndex == index
                        val context = LocalContext.current

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF3F1FF), shape = RoundedCornerShape(12.dp))
                                .clickable {
                                    expandedSpbuIndex = if (isCardExpanded) null else index
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.pertamax),
                                        contentDescription = spbu.brand,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Text(
                                        text = spbu.name,
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF090F59)
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
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

                            // Bagian Accordion (Expand ke bawah saat diklik)
                            AnimatedVisibility(
                                visible = isCardExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 14.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    HorizontalDivider(color = Color(0xFF7542EF).copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Tombol 1: Lihat di Peta
                                    Button(
                                        onClick = {
                                            expandedSpbuIndex = null
                                            isExpanded = false
                                            coroutineScope.launch {
                                                delay(100L) // Jeda agar animasi turunnya bottom sheet mulus tanpa bentrok dengan render peta
                                                if (spbu.lat != 0.0 && spbu.lon != 0.0) {
                                                    resultMapTarget = LatLng(spbu.lat, spbu.lon)
                                                    resultMapTrigger = System.currentTimeMillis()
                                                    viewModel.updateResultMapCamera(LatLng(spbu.lat, spbu.lon), 16.5)
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7542EF)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                    ) {
                                        Text(
                                            text = "Lihat di Peta",
                                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        )
                                    }

                                    // Tombol 2: Buka Google Maps
                                    OutlinedButton(
                                        onClick = {
                                            expandedSpbuIndex = null
                                            if (spbu.lat != 0.0 && spbu.lon != 0.0) {
                                                val uri = Uri.parse("geo:${spbu.lat},${spbu.lon}?q=${Uri.encode(spbu.name)}")
                                                val intent = Intent(Intent.ACTION_VIEW, uri)
                                                intent.setPackage("com.google.android.apps.maps")
                                                try {
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${spbu.lat},${spbu.lon}"))
                                                    context.startActivity(webIntent)
                                                }
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, Color(0xFF7542EF)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(40.dp)
                                    ) {
                                        Text(
                                            text = "Buka Google Maps",
                                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7542EF))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
