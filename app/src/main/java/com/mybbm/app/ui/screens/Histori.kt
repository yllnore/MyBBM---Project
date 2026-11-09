package com.mybbm.app.ui.screens

import java.math.BigDecimal

import com.mybbm.app.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    val histories by (viewModel?.histories?.collectAsState() ?: remember {
        mutableStateOf(emptyList())
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
                Text(
                    text = "Riwayat Perjalanan",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontFamily = FontFamily(Font(R.font.roboto_regular)),
                        fontWeight = FontWeight(700),
                        color = Color(0xFF090F59)
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (histories.isNotEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .background(color = Color(0xFFCCC9F4), shape = CircleShape)
                                .clickable { viewModel?.showDeleteAllHistoryDialog?.value = true }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.hapus),
                                contentDescription = "Hapus Semua",
                                tint = Color(0xFF090F59),
                                modifier = Modifier.size(20.dp)
                            )
                        }
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Belum ada riwayat perjalanan.",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6B7280)
                        )
                    )
                }
            } else {
                val totalCostSummary = remember(histories) {
                    histories.fold(BigDecimal.ZERO) { acc, h -> acc.add(h.totalCost) }
                }
                val totalTrips = histories.size

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE4E0FD))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "TOTAL ESTIMASI PENGELUARAN",
                                        style = TextStyle(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF090F59),
                                            letterSpacing = 0.5.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = FuelCalculator.formatRupiah(totalCostSummary),
                                        style = TextStyle(
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF090F59)
                                        )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF7542EF), RoundedCornerShape(10.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "$totalTrips Perjalanan",
                                        style = TextStyle(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }

                    items(histories) { item ->
                        HistoriCardItem(
                            routeText = "${item.origin} → ${item.destination}",
                            detailText = "${FuelCalculator.formatDistance(item.distanceKm)} · ${FuelCalculator.formatRupiah(item.totalCost)}",
                            onItemClick = {
                                viewModel?.selectHistoryItem(item)
                            },
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
    onItemClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .border(
                width = 1.3.dp,
                color = Color(0xFFDFD9FF),
                shape = RoundedCornerShape(size = 14.dp)
            )
            .background(
                color = Color(0xFFF5F3FF),
                shape = RoundedCornerShape(size = 14.dp)
            )
            .clickable { onItemClick() }
            .padding(horizontal = 12.dp)
    ) {
        // Icon Map Pin Kiri
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = Color(0xFFE3DCFF),
                    shape = RoundedCornerShape(size = 12.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Map Pin",
                tint = Color(0xFF090F59),
                modifier = Modifier.size(30.dp)
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
