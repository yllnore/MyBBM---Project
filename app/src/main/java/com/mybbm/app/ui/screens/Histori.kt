package com.mybbm.app.ui.screens

import com.mybbm.app.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mybbm.app.util.FuelCalculator
import com.mybbm.app.viewmodel.BbmViewModel

@Composable
fun HistoriScreen(
    onCloseClick: () -> Unit = {},
    viewModel: BbmViewModel? = null
) {
    val histories by (viewModel?.histories?.collectAsState() ?: androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(emptyList())
    })

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFC0B9FB)) // Header warna ungu terang
    ) {
        // Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, top = 40.dp, end = 24.dp, bottom = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Perjalanan Sebelumnya",
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontFamily = FontFamily(Font(R.font.roboto_regular)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFF090F59)
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Pick up where you left off",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.roboto_regular)),
                            fontWeight = FontWeight(500),
                            color = Color(0xFF8271D4)
                        )
                    )
                }

                // Tombol Close Bulat
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .background(color = Color(0xFFCCC9F4), shape = CircleShape)
                ) {
                    IconButton(onClick = onCloseClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF090F59),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // List Content Section (Background Ungu Muda Kebawah)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = Color(0xFFF5F3FF),
                    shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
                )
                .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            if (histories.isEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(6) {
                        HistoriCardItem(
                            routeText = "POLNES → Balikpapan Superblock",
                            detailText = "115 km · Rp 95.000",
                            onDeleteClick = {
                                viewModel?.showDeleteHistoryDialog?.value = true
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(histories) { item ->
                        HistoriCardItem(
                            routeText = "${item.origin} → ${item.destination}",
                            detailText = "${FuelCalculator.formatDistance(item.distanceKm)} · ${FuelCalculator.formatRupiah(item.totalCost)}",
                            onDeleteClick = {
                                viewModel?.historyToDelete?.value = item
                                viewModel?.showDeleteHistoryDialog?.value = true
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HistoriCardItem(
    routeText: String,
    detailText: String,
    onDeleteClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .height(74.dp)
            .border(
                width = 1.3.dp,
                color = Color(0xFFDFD9FF),
                shape = RoundedCornerShape(size = 14.dp)
            )
            .background(
                color = Color(0xFFF5F3FF),
                shape = RoundedCornerShape(size = 14.dp)
            )
            .padding(horizontal = 12.dp)
    ) {
        // Icon Map Pin Kiri
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .background(
                    color = Color(0xFFE3DCFF),
                    shape = RoundedCornerShape(size = 11.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Map Pin",
                tint = Color(0xFF090F59),
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Info Teks Tengah
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = routeText,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.roboto_regular)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFF090F59)
                ),
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detailText,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.roboto_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF9184DD)
                )
            )
        }

        // Tombol Delete Kanan
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = Color(0xFFEDE9FF),
                    shape = RoundedCornerShape(size = 9.dp)
                )
        ) {
            IconButton(onClick = onDeleteClick) {
                Icon(
                    painter = painterResource(id = R.drawable.hapus),
                    contentDescription = "Hapus Histori",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(35.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HistoriScreenPreview() {
    HistoriScreen()
}
