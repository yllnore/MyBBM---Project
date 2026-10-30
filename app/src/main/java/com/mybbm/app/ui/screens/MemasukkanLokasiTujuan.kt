package com.mybbm.app.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
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
fun MemasukkanLokasiTujuanScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFE3E7E7))
    ) {
        // Space mengintip area Map di paling atas
        Spacer(modifier = Modifier.height(32.dp))

        // Card Container Utama Bottom Sheet (Warna Ungu Full Ke Bawah)
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
            // Drag Handle Bar (Garis Kapsul Atas)
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

            // Form Fields Lengkap (1. Lokasi Awal, 2. Input Tujuan, 3. Kendaraan, 4. BBM)
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

                // 2. Input Lokasi Tujuan (Sedang Mengetik Balikpapan)
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
                        text = "Balikpapan",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.roboto_regular)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFF11133E)
                        )
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

            Spacer(modifier = Modifier.height(16.dp))

            // Garis Pembatas Utama Sesuai Figma
            Divider(color = Color(0xFF000000), thickness = 1.dp)

            // Lista Hasil Pencarian / Autocomplete Dropdown
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 19.dp)
            ) {
                // Item 1: Balikpapan
                LokasiSearchResultItem(
                    title = "Balikpapan",
                    subtitle = "Kota Balikpapan, Kalimantan Timur, Indonesia"
                )
                Divider(color = Color(0xFF000000), thickness = 1.dp)

                // Item 2: Balikpapan Kota
                LokasiSearchResultItem(
                    title = "Balikpapan Kota",
                    subtitle = "Kota Balikpapan, Kalimantan Timur, Indonesia"
                )
                Divider(color = Color(0xFF000000), thickness = 1.dp)

                // Item 3: Balikpapan Superblock (BSB)
                LokasiSearchResultItem(
                    title = "Balikpapan Superblock (BSB)",
                    subtitle = "Jalan Jenderal Sudirman, Gunung Bahagia, Kota Balikpapan, \nKalimantan Timur, Indonesia"
                )
                Divider(color = Color(0xFF000000), thickness = 1.dp)

                // Item 4: Balikpapan Selatan
                LokasiSearchResultItem(
                    title = "Balikpapan Selatan",
                    subtitle = "Kota Balikpapan, Kalimantan Timur, Indonesia"
                )
                Divider(color = Color(0xFF000000), thickness = 1.dp)
            }
        }
    }
}

@Composable
fun LokasiSearchResultItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.roboto_regular)),
                fontWeight = FontWeight(700),
                color = Color(0xFF11133E)
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = FontFamily(Font(R.font.roboto_regular)),
                fontWeight = FontWeight(400),
                color = Color(0xFF11133E)
            )
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MemasukkanLokasiTujuanScreenPreview() {
    MemasukkanLokasiTujuanScreen()
}