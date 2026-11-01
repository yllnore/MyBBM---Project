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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

@Composable
fun DeleteHistoryDialog(
    onDismissRequest: () -> Unit = {},
    onConfirmDelete: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismissRequest) {
        // Container Utama Pop Up (Diperbesar 320.dp)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .width(320.dp)
                .border(
                    width = 1.dp,
                    color = Color(0xFFBFB7FA),
                    shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 22.dp, bottomEnd = 22.dp)
                )
                .background(
                    color = Color(0xFFF1EFFF),
                    shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 22.dp, bottomEnd = 22.dp)
                )
                .padding(horizontal = 22.dp, vertical = 22.dp)
        ) {
            // Teks Pertanyaan Konfirmasi
            Text(
                text = "Apakah Anda yakin ingin menghapus riwayat ini?",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontFamily = FontFamily(Font(R.font.roboto_regular)),
                    fontWeight = FontWeight(700),
                    color = Color(0xFF090F59),
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Baris Tombol Aksi (Hapus & Batal)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Tombol Hapus (Biru Muda)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(120.dp)
                        .height(38.dp)
                        .shadow(
                            elevation = 2.2.dp,
                            spotColor = Color(0x40000000),
                            ambientColor = Color(0x40000000),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                        )
                        .background(
                            color = Color(0xFF5DB1E7),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                        )
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                        .clickable {
                            onConfirmDelete()
                            onDismissRequest()
                        }
                ) {
                    Text(
                        text = "Hapus",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.roboto_regular)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFFFFFFFF)
                        )
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Tombol Batal (Abu-abu Muda)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .width(120.dp)
                        .height(38.dp)
                        .shadow(
                            elevation = 2.2.dp,
                            spotColor = Color(0x40000000),
                            ambientColor = Color(0x40000000),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                        )
                        .background(
                            color = Color(0xFFE6E6E6),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 20.dp, bottomEnd = 20.dp)
                        )
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 20.dp, bottomEnd = 20.dp))
                        .clickable {
                            onDismissRequest()
                        }
                ) {
                    Text(
                        text = "Batal",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = FontFamily(Font(R.font.roboto_regular)),
                            fontWeight = FontWeight(700),
                            color = Color(0xFF090F59)
                        )
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeleteHistoryDialogPreview() {
    DeleteHistoryDialog()
}