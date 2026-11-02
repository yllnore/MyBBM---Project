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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun MengaturLokasiTujuanScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFE3E7E7))
    ) {
        // Space kecil di paling atas untuk mengintip area Map (32.dp)
        Spacer(modifier = Modifier.height(32.dp))

        // Card Sheet Utama
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
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
                .padding(top = 12.dp)
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

            // List Field Form Input
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 19.dp)
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

                // 2. Lokasi Tujuan (Kotak Kosong)
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
                    // Kotak kosong
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
                    .padding(horizontal = 19.dp)
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

            Spacer(modifier = Modifier.height(20.dp))

            // Garis Pembatas Hitam Sesuai Figma
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(color = Color(0xFF000000))
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MengaturLokasiTujuanScreenPreview() {
    MengaturLokasiTujuanScreen()
}