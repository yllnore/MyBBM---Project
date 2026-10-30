package com.mybbm.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddVehicleDialog(
    onSimpanClick: () -> Unit = {},
    onBatalClick: () -> Unit = {}
) {
    // Card Container Utama
    Box(
        modifier = Modifier
            .width(256.dp)
            .height(221.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFBFB7FA),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
            )
            .background(
                color = Color(0xFFF1EFFF),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
            )
            .padding(top = 16.dp, bottom = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Judul Dialog
            Text(
                text = "Tambah Kendaraan Baru",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight(700),
                    color = Color(0xFF090F59),
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Input Field 1: Nama Kendaraan
            DialogInputField(
                placeholder = "Nama Kendaraan",
                heightDp = 26.dp
            )

            // Input Field 2: Jenis Kendaraan
            DialogInputField(
                placeholder = "Jenis Kendaraan",
                heightDp = 26.dp
            )

            // Input Field 3: Konsumsi BBM km/L
            DialogInputField(
                placeholder = "Konsumsi BBM km/L",
                heightDp = 27.dp
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Baris Tombol Simpan & Batal
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Simpan (Biru)
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 2.2.dp,
                            spotColor = Color(0x40000000),
                            ambientColor = Color(0x40000000)
                        )
                        .width(95.dp)
                        .height(27.dp)
                        .background(
                            color = Color(0xFF5DB1E7),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                        )
                        .clickable { onSimpanClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Simpan",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight(700),
                            color = Color(0xFFFFFFFF)
                        )
                    )
                }

                // Tombol Batal (Abu-abu)
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 2.2.dp,
                            spotColor = Color(0x40000000),
                            ambientColor = Color(0x40000000)
                        )
                        .width(95.dp)
                        .height(27.dp)
                        .background(
                            color = Color(0xFFE6E6E6),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                        )
                        .clickable { onBatalClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Batal",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight(700),
                            color = Color(0xFF090F59)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun DialogInputField(
    placeholder: String,
    heightDp: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .width(209.dp)
            .height(heightDp)
            .border(
                width = 1.dp,
                color = Color(0xFFBFB7FA),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = placeholder,
            style = TextStyle(
                fontSize = 12.sp,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight(400),
                color = Color(0xFF606995)
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddVehicleDialogPreview() {
    AddVehicleDialog()
}