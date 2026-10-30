package com.mybbm.app.ui.screens

import com.mybbm.app.R
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TentangMyBbmScreen(
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xFFECEBFF))
    ) {
        // Top Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFFC0B9FB))
                .padding(start = 12.dp, top = 40.dp, end = 24.dp, bottom = 16.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color(0xFF090F59),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Tentang MYBBM",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontFamily = FontFamily(Font(R.font.roboto_regular)),
                    fontWeight = FontWeight(700),
                    color = Color(0xFF090F59)
                )
            )
        }

        // Body Content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            // Logo Utama Aplikasi (logo_fix.png)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(90.dp)
                    .shadow(
                        elevation = 12.dp,
                        spotColor = Color(0xFFBFB7FA),
                        ambientColor = Color(0xFFBFB7FA),
                        shape = RoundedCornerShape(22.dp)
                    )
                    .background(color = Color(0xFFFFFFFF), shape = RoundedCornerShape(22.dp))
                    .clip(RoundedCornerShape(22.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_fix),
                    contentDescription = "Logo MYBBM",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(1.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Judul Aplikasi
            Text(
                text = "MYBBM",
                style = TextStyle(
                    fontSize = 28.sp,
                    fontFamily = FontFamily(Font(R.font.roboto_regular)),
                    fontWeight = FontWeight(800),
                    color = Color(0xFF090F59)
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Container Kartu Informasi
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = Color(0xFFD3CFF6),
                        shape = RoundedCornerShape(21.dp)
                    )
                    .background(
                        color = Color(0xFFE7E4FE),
                        shape = RoundedCornerShape(21.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Item 1: Versi aplikasi (Ganti R.drawable.versi_apk jika nama file xml-mu beda)
                AboutInfoRowVector(
                    iconResId = R.drawable.versi_apk,
                    title = "Versi aplikasi",
                    value = "1.0.0"
                )

                HorizontalDivider(
                    color = Color(0xFFD3CFF6),
                    thickness = 1.dp,
                    modifier = Modifier.padding(start = 60.dp, top = 4.dp, bottom = 4.dp)
                )

                // Item 2: Informasi aplikasi
                AboutInfoRowVector(
                    iconResId = R.drawable.informasi,
                    title = "Informasi aplikasi",
                    value = "Fuel Calculator + SPBU Finder"
                )

                HorizontalDivider(
                    color = Color(0xFFD3CFF6),
                    thickness = 1.dp,
                    modifier = Modifier.padding(start = 60.dp, top = 4.dp, bottom = 4.dp)
                )

                // Item 3: Pengembang
                AboutInfoRowVector(
                    iconResId = R.drawable.profil,
                    title = "Pengembang",
                    value = "MYBBM Team"
                )
            }
        }
    }
}

@Composable
fun AboutInfoRowVector(
    iconResId: Int,
    title: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Lingkaran Ungu Background Ikon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(46.dp)
                .background(color = Color(0xFFC9C4FB), shape = CircleShape)
        ) {
            // TINT DIHAPUS agar warna asli bawaan SVG muncul transparan
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = title,
                tint = Color.Unspecified, // <-- Pakai Color.Unspecified agar tidak diwarnai kotak solid
                modifier = Modifier.size(35.dp)
            )
        }

        // Teks Deskripsi
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.roboto_regular)),
                    fontWeight = FontWeight(600),
                    color = Color(0xFF090F59)
                )
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontFamily = FontFamily(Font(R.font.roboto_regular)),
                    fontWeight = FontWeight(400),
                    color = Color(0xFF606995)
                )
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TentangMyBbmScreenPreview() {
    TentangMyBbmScreen()
}