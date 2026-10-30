package com.mybbm.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mybbm.app.R

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onMenuItemClick: (String) -> Unit = {}
) {
    // Outer Frame Canvas
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = Color(0xFFCCC7FB),
                    shape = RoundedCornerShape(size = 27.dp)
                )
                .width(412.dp)
                .height(917.dp)
                .background(color = Color(0xFFECEBFF), shape = RoundedCornerShape(size = 27.dp))
        ) {
            // Container Bagian Atas (Header Ungu)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFBDB8F8),
                        shape = RoundedCornerShape(topStart = 27.dp, topEnd = 27.dp)
                    )
            ) {
                // 1. Status Bar (Jam)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                }

                // 2. Header Bar (Tombol Back & Judul "Pengaturan")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .width(412.dp)
                        .height(66.dp)
                        .padding(start = 20.dp, end = 24.dp, bottom = 12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "Back",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(29.dp)
                            .clickable { onBackClick() }
                    )

                    Text(
                        text = "Pengaturan",
                        style = TextStyle(
                            fontSize = 27.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight(700),
                            color = Color(0xFF090F59)
                        )
                    )
                }
            }

            // 3. Konten Daftar Menu Pengaturan
            Column(
                verticalArrangement = Arrangement.spacedBy(19.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .width(412.dp)
                    .padding(start = 20.dp, top = 27.dp, end = 20.dp)
            ) {
                // Item 1: Kendaraan
                SettingMenuItem(
                    iconRes = R.drawable.mobil,
                    title = "Kendaraan",
                    onClick = { onMenuItemClick("Kendaraan") }
                )

                // Item 2: BBM (Ikon diubah menjadi R.drawable.solar)
                SettingMenuItem(
                    iconRes = R.drawable.solar,
                    title = "BBM",
                    onClick = { onMenuItemClick("BBM") }
                )

                // Item 3: Tentang MYBBM
                SettingMenuItem(
                    iconRes = R.drawable.icon,
                    title = "Tentang MYBBM",
                    onClick = { onMenuItemClick("Tentang MYBBM") }
                )
            }
        }
    }
}

@Composable
fun SettingMenuItem(
    iconRes: Int,
    title: String,
    onClick: () -> Unit = {}
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .shadow(
                elevation = 3.dp,
                spotColor = Color(0x166765AA),
                ambientColor = Color(0x166765AA)
            )
            .border(
                width = 1.7.dp,
                color = Color(0xFFCCC9F4),
                shape = RoundedCornerShape(size = 21.dp)
            )
            .width(372.dp)
            .height(95.dp)
            .background(color = Color(0xFFF1EFFF), shape = RoundedCornerShape(size = 21.dp))
            .padding(start = 16.dp, end = 20.dp)
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Box Ungu untuk Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFBDB8F8)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(36.dp)
                )
            }

            Text(
                text = title,
                style = TextStyle(
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight(500),
                    color = Color(0xFF090F59)
                )
            )
        }

        // Icon Panah Kanan (Chevron)
        Image(
            painter = painterResource(id = R.drawable.open),
            contentDescription = "Open",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .width(12.dp)
                .height(20.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}