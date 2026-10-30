package com.mybbm.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ToastBerhasilDihapus(
    onCloseClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .width(213.dp)
            .height(64.dp)
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
            )
            .padding(horizontal = 12.dp)
    ) {
        // Teks "Berhasil Dihapus!"
        Text(
            text = "Berhasil dihapus!",
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight(700),
                color = Color(0xFF090F59),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .width(163.dp)
                .height(18.dp)
                .align(Alignment.Center)
        )

        // Icon Close
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = Color(0xFF6051BF),
            modifier = Modifier
                .size(18.dp)
                .align(Alignment.TopEnd)
                .padding(top = 4.dp, end = 2.dp)
                .clickable { onCloseClick() }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ToastBerhasilDihapusPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        ToastBerhasilDihapus()
    }
}