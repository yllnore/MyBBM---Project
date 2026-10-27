package com.mybbm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mybbm.app.ui.MainScreen
import com.mybbm.app.ui.theme.MYBBMTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MYBBMTheme {
                MainScreen()
            }
        }
    }
}
