package com.example.mybbm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.mybbm.ui.MainScreen
import com.example.mybbm.ui.theme.MYBBMTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MYBBMTheme {
                MainScreen()
            }
        }
    }
}
