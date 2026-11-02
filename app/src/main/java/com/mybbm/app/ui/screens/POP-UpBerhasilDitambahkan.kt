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
fun SuccessToast(
    onCloseClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .width(270.dp)
            .height(72.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFBFB7FA),
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 22.dp, bottomEnd = 22.dp)
            )
            .background(
                color = Color(0xFFF1EFFF),
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 22.dp, bottomEnd = 22.dp)
            )
            .padding(horizontal = 16.dp)
    ) {
        // Teks "Berhasil ditambahkan!"
        Text(
            text = "Berhasil ditambahkan!",
            style = TextStyle(
                fontSize = 17.sp,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight(700),
                color = Color(0xFF090F59),
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 20.dp)
        )

        // Icon Close Vector Bawaan
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = Color(0xFF6051BF),
            modifier = Modifier
                .size(22.dp)
                .align(Alignment.TopEnd)
                .padding(top = 6.dp, end = 4.dp)
                .clickable { onCloseClick() }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SuccessToastPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        SuccessToast()
    }
}