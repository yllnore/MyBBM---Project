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
fun MengaturLokasiAwalScreen(
    onCalculateClick: () -> Unit = {},
    onCurrentLocationClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
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
                    color = Color(0xFFC0BEF8),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag Handle
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

                // Input 1: Lokasi Awal (Menggunakan Teks "Lokasi Awal" & Ikon Biru Tua)
                LocationInputField(
                    placeholder = "Lokasi Awal",
                    iconColor = Color(0xFF1F6BE8), // Biru Tua
                    iconResId = R.drawable.lokasi_awal
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Input 2: Lokasi Tujuan
                LocationInputField(
                    placeholder = "Lokasi Tujuan",
                    iconColor = Color(0xFFFF2125), // Merah
                    iconResId = R.drawable.lokasi_tujuan
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Input 3: Pilih Kendaraan
                SelectionInputField(placeholder = "Pilih Kendaraan")

                Spacer(modifier = Modifier.height(12.dp))

                // Input 4: Jenis BBM
                SelectionInputField(placeholder = "Jenis BBM")

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

            // Garis Pembatas Hitam
            HorizontalDivider(
                color = Color.Black,
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth()
            )

            // Baris Lokasi Saat Ini
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCurrentLocationClick() }
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.current_location_icon),
                    contentDescription = "Lokasi Saat Ini",
                    colorFilter = ColorFilter.tint(Color.Black),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Lokasi Saat Ini",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF11133E)
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

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

@Composable
private fun SelectionInputField(
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

@Preview(
    name = "Mengatur Lokasi Awal UI",
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=412dp,height=917dp,dpi=440"
)
@Composable
fun MengaturLokasiAwalPreview() {
    MengaturLokasiAwalScreen()
}