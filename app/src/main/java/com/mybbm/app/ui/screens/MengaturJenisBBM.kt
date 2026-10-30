package com.mybbm.app.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class CleanMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScreenDesignBbm()
        }
    }
}

@Composable
fun ScreenDesignBbm() {
    Box(
        modifier = Modifier
            .width(412.dp)
            .height(917.dp)
            .background(color = Color(0xFFFFFFFF)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .shadow(
                    elevation = 16.dp,
                    spotColor = Color(0x4051667B),
                    ambientColor = Color(0x4051667B)
                )
                .width(412.dp)
                .height(917.dp)
                .background(
                    color = Color(0xFFE3E7E7),
                    shape = RoundedCornerShape(size = 29.dp)
                )
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .width(412.dp)
                    .height(917.dp)
                    .background(
                        color = Color(0xFFBDB8F8),
                        shape = RoundedCornerShape(size = 29.dp)
                    )
                    .padding(start = 19.dp, top = 10.dp, end = 19.dp, bottom = 34.dp)
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            color = Color(0x6651667B),
                            shape = RoundedCornerShape(2.dp)
                        )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Input Fields
                FieldInputBbm(text = "POLNES - Politeknik Negeri Samarinda")
                Spacer(modifier = Modifier.height(12.dp))
                FieldInputBbm(text = "Balikpapan")
                Spacer(modifier = Modifier.height(12.dp))
                FieldInputBbm(text = "Honda")
                Spacer(modifier = Modifier.height(12.dp))
                FieldInputBbm(text = "") // Input ke-4 dikosongkan

                Spacer(modifier = Modifier.height(20.dp))

                // Tombol Hitung Biaya Perjalanan
                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7043EC)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .width(374.dp)
                        .height(48.dp)
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                ) {
                    Text(
                        text = "Hitung Biaya Perjalanan",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight(600),
                            color = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Pembatas Garis Horizontal
                Box(
                    modifier = Modifier
                        .padding(0.dp)
                        .width(413.dp)
                        .height(1.dp)
                        .background(color = Color(0xFF000000))
                )
            }
        }
    }
}

@Composable
fun FieldInputBbm(text: String) {
    Box(
        modifier = Modifier
            .width(374.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFD3E3FD))
            .border(1.dp, Color(0xFF51667B), RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        if (text.isNotEmpty()) {
            Text(
                text = text,
                style = TextStyle(
                    fontSize = 14.sp,
                    color = Color(0xFF1F1F1F)
                )
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
fun ScreenDesignBbmPreview() {
    ScreenDesignBbm()
}