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
import androidx.compose.foundation.shape.CircleShape
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
fun DeleteBbmDialog(
    onHapusClick: () -> Unit = {},
    onBatalClick: () -> Unit = {}
) {
    // Card Container Utama (Ukuran Tetap 256.dp x 119.dp)
    Box(
        modifier = Modifier
            .width(256.dp)
            .height(119.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFBFB7FA),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                )
            )
            .background(
                color = Color(0xFFF1EFFF),
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = 20.dp,
                    bottomEnd = 20.dp
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
        ) {
            // Teks Pertanyaan Konfirmasi BBM (2 Baris Presisi Rata Tengah)
            Text(
                text = "Apakah Anda yakin ingin\nmenghapus BBM ini?",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight(700),
                    color = Color(0xFF090F59),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .width(210.dp)
                    .height(44.dp)
            )

            // Baris Tombol Hapus & Batal
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Hapus (Biru Kapsul Membulat)
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 2.dp,
                            spotColor = Color(0x40000000),
                            ambientColor = Color(0x40000000),
                            shape = CircleShape
                        )
                        .width(95.dp)
                        .height(28.dp)
                        .background(
                            color = Color(0xFF5DB1E7),
                            shape = CircleShape
                        )
                        .clickable { onHapusClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hapus",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight(700),
                            color = Color(0xFFFFFFFF)
                        ),
                        textAlign = TextAlign.Center
                    )
                }

                // Tombol Batal (Abu-abu Kapsul Membulat)
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 2.dp,
                            spotColor = Color(0x40000000),
                            ambientColor = Color(0x40000000),
                            shape = CircleShape
                        )
                        .width(95.dp)
                        .height(28.dp)
                        .background(
                            color = Color(0xFFE6E6E6),
                            shape = CircleShape
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
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeleteBbmDialogPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        DeleteBbmDialog()
    }
}