package com.mybbm.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddBbmDialog(
    onSimpanClick: () -> Unit = {},
    onBatalClick: () -> Unit = {}
) {
    // Card Container Utama (Ukuran 256.dp x 170.dp)
    Box(
        modifier = Modifier
            .width(256.dp)
            .height(170.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFBFB7FA),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
            )
            .background(
                color = Color(0xFFF1EFFF),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
            )
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
        ) {
            // Judul Dialog
            Text(
                text = "Tambah BBM Baru",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight(700),
                    color = Color(0xFF090F59),
                    textAlign = TextAlign.Center
                )
            )

            // Input Field 1: Nama Jenis BBM
            BbmDialogInputField(
                placeholder = "Nama Jenis BBM",
                heightDp = 26.dp
            )

            // Input Field 2: Harga BBM km/L
            BbmDialogInputField(
                placeholder = "Harga BBM km/L",
                heightDp = 27.dp
            )

            // Baris Tombol Simpan & Batal
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                // Tombol Simpan (Biru)
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 2.2.dp,
                            spotColor = Color(0x40000000),
                            ambientColor = Color(0x40000000),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
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
                            ambientColor = Color(0x40000000),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
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
fun BbmDialogInputField(
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
fun AddBbmDialogPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        AddBbmDialog()
    }
}