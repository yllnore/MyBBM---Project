package com.mybbm.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mybbm.app.R // Import file gambar R milik aplikasi Anda
import com.mybbm.app.viewmodel.BbmViewModel

@Composable
fun HomeScreen(
    onCalculateClick: () -> Unit = {},
    viewModel: BbmViewModel,
    onNavigateToResult: () -> Unit
) {
    // 1. Root Container
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // Background abu-abu sementara
    ) {

        // KOMPONEN MAP ASLI NANTINYA BISA DITARUH DI SINI
        // GoogleMap(...)

        // 2. Tombol Mengambang di Kanan Atas (FABs)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 40.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingMapAction(iconResId = R.drawable.settings_icon)
            FloatingMapAction(iconResId = R.drawable.history)
            FloatingMapAction(iconResId = R.drawable.current_location_icon)
        }

        // 3. Panel Input Perjalanan (Bottom Sheet)
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .shadow(
                    elevation = 24.dp,
                    spotColor = Color(0x40000000),
                    ambientColor = Color(0x40000000),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .background(
                    color = Color(0xFFC0BEF8), // Warna ungu muda
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
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

            // Form Input
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Input Lokasi Awal
                LocationInputField(
                    placeholder = "Lokasi Awal",
                    iconResId = R.drawable.lokasi_awal
                )

                // Input Lokasi Tujuan
                LocationInputField(
                    placeholder = "Lokasi Tujuan",
                    iconResId = R.drawable.lokasi_tujuan
                )

                // Input Pilihan
                SelectionInputField(placeholder = "Pilih Kendaraan")
                SelectionInputField(placeholder = "Jenis BBM")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Hitung Biaya
            Button(
                onClick = onCalculateClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7542EF)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .shadow(
                        elevation = 8.dp,
                        spotColor = Color(0x5544308D),
                        shape = RoundedCornerShape(24.dp)
                    )
            ) {
                Text(
                    text = "Hitung Biaya Perjalanan",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

// Sub-komponen Input Lokasi
@Composable
private fun LocationInputField(
    placeholder: String,
    iconResId: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(color = Color(0xFFDFE6F9), shape = RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = Color(0xFF303B65), shape = RoundedCornerShape(8.dp))
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
            modifier = Modifier.size(24.dp)
        )
    }
}

// Sub-komponen Input Seleksi (Dropdown)
@Composable
private fun SelectionInputField(
    placeholder: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(color = Color(0xFFDFE6F9), shape = RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = Color(0xFF303B65), shape = RoundedCornerShape(8.dp))
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
    }
}

// Sub-komponen Tombol Melayang di Map
@Composable
private fun FloatingMapAction(iconResId: Int) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .shadow(6.dp, CircleShape)
            .background(Color(0xFFC0BEF8), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = iconResId),
            contentDescription = "Map Action",
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showSystemUi = true, device = "spec:width=412dp,height=917dp,dpi=440")
@Composable
fun HomeScreenPreview() {
    // Karena Preview tidak bisa menjalankan ViewModel asli,
    // kita berikan nilai "null" atau mock sementara jika memungkinkan.
    // Jika BbmViewModel wajib ada, Anda bisa memanggilnya seperti di bawah ini,
    // asalkan Anda meng-import viewModel-nya.

    HomeScreen(
        onCalculateClick = {},
        viewModel = androidx.lifecycle.viewmodel.compose.viewModel(), // Menggunakan dummy ViewModel untuk Preview
        onNavigateToResult = {}
    )
}