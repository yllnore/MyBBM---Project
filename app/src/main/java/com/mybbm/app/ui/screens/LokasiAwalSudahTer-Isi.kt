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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LokasiAwalSudahTerIsiScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFE3E7E7)) // Background abu-abu
    ) {
        // ----------------------------------------------------
        // 3 TOMBOL MELAYANG DI POJOK KANAN ATAS
        // ----------------------------------------------------
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp)
        ) {
            // 1. Tombol Settings
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(14.dp))
                    .background(color = Color(0xFFB5C7FF), shape = RoundedCornerShape(14.dp))
            ) {
                IconButton(onClick = { /* Action Settings */ }) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = Color(0xFF11133E),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // 2. Tombol History / Riwayat
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(14.dp))
                    .background(color = Color(0xFFB5C7FF), shape = RoundedCornerShape(14.dp))
            ) {
                IconButton(onClick = { /* Action History */ }) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = Color(0xFF11133E),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            // 3. Tombol My Location / GPS
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(14.dp))
                    .background(color = Color(0xFFB5C7FF), shape = RoundedCornerShape(14.dp))
            ) {
                IconButton(onClick = { /* Action Location */ }) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "My Location",
                        tint = Color(0xFF11133E),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // ----------------------------------------------------
        // BOTTOM SHEET CARD (FORM INPUT)
        // ----------------------------------------------------
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .shadow(
                    elevation = 16.dp,
                    spotColor = Color(0x4051667B),
                    ambientColor = Color(0x4051667B),
                    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
                )
                .background(
                    color = Color(0xFFBDB8F8),
                    shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp)
                )
                .padding(horizontal = 19.dp, vertical = 12.dp)
        ) {
            // Drag Handle Bar
            Box(
                modifier = Modifier
                    .width(41.dp)
                    .height(5.dp)
                    .background(
                        color = Color(0xFF7289D2),
                        shape = RoundedCornerShape(size = 3.dp)
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // List Field
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 1. Lokasi Awal (POLNES)
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(
                            width = 1.3.dp,
                            color = Color(0xFF303B65),
                            shape = RoundedCornerShape(size = 6.dp)
                        )
                        .background(
                            color = Color(0xFFCCDEFC),
                            shape = RoundedCornerShape(size = 6.dp)
                        )
                        .padding(horizontal = 13.dp)
                ) {
                    Text(
                        text = "POLNES - Politeknik Negeri Samarinda",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.roboto_regular)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF11133E)
                        )
                    )
                }

                // 2. Lokasi Tujuan
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(
                            width = 1.3.dp,
                            color = Color(0xFF303B65),
                            shape = RoundedCornerShape(size = 6.dp)
                        )
                        .background(
                            color = Color(0xFFCCDEFC),
                            shape = RoundedCornerShape(size = 6.dp)
                        )
                        .padding(horizontal = 13.dp)
                ) {
                    Text(
                        text = "Lokasi Tujuan",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.roboto_regular)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF11133E)
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Pin Icon",
                        tint = Color.Red,
                        modifier = Modifier
                            .width(24.dp)
                            .height(28.dp)
                    )
                }

                // 3. Pilih Kendaraan
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(
                            width = 1.3.dp,
                            color = Color(0xFF303B65),
                            shape = RoundedCornerShape(size = 6.dp)
                        )
                        .background(
                            color = Color(0xFFCCDEFC),
                            shape = RoundedCornerShape(size = 6.dp)
                        )
                        .padding(horizontal = 13.dp)
                ) {
                    Text(
                        text = "Pilih Kendaraan",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.roboto_regular)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF11133E)
                        )
                    )
                }

                // 4. Jenis BBM
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .border(
                            width = 1.3.dp,
                            color = Color(0xFF303B65),
                            shape = RoundedCornerShape(size = 6.dp)
                        )
                        .background(
                            color = Color(0xFFCCDEFC),
                            shape = RoundedCornerShape(size = 6.dp)
                        )
                        .padding(horizontal = 13.dp)
                ) {
                    Text(
                        text = "Jenis BBM",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.roboto_regular)),
                            fontWeight = FontWeight(400),
                            color = Color(0xFF11133E)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tombol Hitung Biaya Perjalanan
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .shadow(
                        elevation = 5.dp,
                        spotColor = Color(0x5544308D),
                        ambientColor = Color(0x5544308D),
                        shape = RoundedCornerShape(size = 28.dp)
                    )
                    .background(
                        color = Color(0xFF7542EF),
                        shape = RoundedCornerShape(size = 28.dp)
                    )
            ) {
                Text(
                    text = "Hitung Biaya Perjalanan",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.roboto_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFFFFFFFF)
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LokasiAwalSudahTerIsiScreenPreview() {
    LokasiAwalSudahTerIsiScreen()
}