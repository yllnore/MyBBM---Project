package com.mybbm.app.ui.screens // Disesuaikan dengan struktur folder MyBBM

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mybbm.app.R // Import file gambar R milik aplikasi MyBBM

@Composable
fun MemasukkanLokasiAwalScreen(
    onCalculateClick: () -> Unit = {},
    onLocationItemClick: (String) -> Unit = {} // Fungsi ketika list hasil pencarian diklik
) {
    // 1. Root Container (Latar Belakang)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Background sementara sebelum ada map
    ) {

        // 2. Bottom Sheet Panel Utama (Warna Ungu Muda)
        // Dibuat lebih tinggi ke atas (fillMaxHeight(0.95f)) karena ini layar pencarian
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.95f) // Mengambil 95% tinggi layar
                .shadow(
                    elevation = 24.dp,
                    spotColor = Color(0x40000000),
                    ambientColor = Color(0x40000000),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .background(
                    color = Color(0xFFC0BEF8),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // --- BAGIAN ATAS (FORM & TOMBOL) ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Garis Handle (Drag Handle)
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            color = Color(0xFF7289D2),
                            shape = RoundedCornerShape(2.dp)
                        )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Input 1: Terisi "Polnes" (Sesuai gambar)
                BasicInputField(placeholder = "Polnes")

                Spacer(modifier = Modifier.height(12.dp))

                // Input 2: Lokasi Tujuan
                LocationInputField(
                    placeholder = "Lokasi Tujuan",
                    iconColor = Color(0xFFFF2125), // Merah
                    iconResId = R.drawable.lokasi_tujuan
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Input 3: Pilih Kendaraan
                BasicInputField(placeholder = "Pilih Kendaraan")

                Spacer(modifier = Modifier.height(12.dp))

                // Input 4: Jenis BBM
                BasicInputField(placeholder = "Jenis BBM")

                Spacer(modifier = Modifier.height(24.dp))

                // Tombol Hitung Biaya Perjalanan
                Button(
                    onClick = onCalculateClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .shadow(
                            elevation = 4.dp,
                            spotColor = Color(0x5544308D),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Text(
                        text = "Hitung Biaya Perjalanan",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight.Normal,
                            color = Color.White
                        )
                    )
                }
            }

            // Garis Pembatas Hitam Pertama (Sebelum List Pencarian)
            HorizontalDivider(
                color = Color.Black,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            // --- BAGIAN BAWAH (DAFTAR HASIL PENCARIAN) ---
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                SearchResultItem(
                    title = "POLNES - Politeknik Negeri Samarinda",
                    subtitle = "Jalan Cipto Mangun Kusumo, Gunung Panjang, Kota Samarinda, Kalimantan Timur, Indonesia",
                    onClick = { onLocationItemClick("POLNES") }
                )

                SearchResultItem(
                    title = "Kemaritiman Polnes",
                    subtitle = "Sungai Keledang, Kota Samarinda, Kalimantan Timur, Indonesia",
                    onClick = { onLocationItemClick("Kemaritiman Polnes") }
                )

                SearchResultItem(
                    title = "Direktorat POLNES",
                    subtitle = "Sungai Keledang, Kota Samarinda, Kalimantan Timur, Indonesia",
                    onClick = { onLocationItemClick("Direktorat POLNES") }
                )

                SearchResultItem(
                    title = "Gedung Auditorium (Serba Guna) Polnes",
                    subtitle = "POLNES - Politeknik Negeri Samarinda, Sungai Keledang, Kota Samarinda, Kalimantan Timur, Indonesia",
                    onClick = { onLocationItemClick("Gedung Auditorium Polnes") }
                )
            }
        }
    }
}

// Sub-komponen 1: Input Dasar (Tanpa Ikon)
@Composable
private fun BasicInputField(
    placeholder: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(color = Color(0xFFDFE6F9), shape = RoundedCornerShape(6.dp))
            .border(width = 1.dp, color = Color(0xFF11133E), shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = placeholder,
            style = TextStyle(
                fontSize = 15.sp,
                color = Color(0xFF11133E)
            )
        )
    }
}

// Sub-komponen 2: Input Lokasi dengan Ikon
@Composable
private fun LocationInputField(
    placeholder: String,
    iconColor: Color,
    iconResId: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(color = Color(0xFFDFE6F9), shape = RoundedCornerShape(6.dp))
            .border(width = 1.dp, color = Color(0xFF11133E), shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = placeholder,
            style = TextStyle(
                fontSize = 15.sp,
                color = Color(0xFF11133E)
            )
        )
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = placeholder,
            colorFilter = ColorFilter.tint(iconColor),
            modifier = Modifier.size(20.dp)
        )
    }
}

// Sub-komponen 3: Item List Hasil Pencarian
@Composable
private fun SearchResultItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF11133E)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = TextStyle(
                    fontSize = 12.sp, // Ukuran teks subtitle lebih kecil
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF11133E)
                ),
                lineHeight = 16.sp
            )
        }
        // Garis Pembatas (Divider) di setiap akhir item
        HorizontalDivider(
            color = Color.Black,
            thickness = 1.dp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ----------------------------------------------------
// PREVIEW SECTION
// ----------------------------------------------------

@Preview(
    name = "Memasukkan Lokasi Awal UI",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=917dp,dpi=440"
)
@Composable
fun MemasukkanLokasiAwalPreview() {
    MemasukkanLokasiAwalScreen()
}