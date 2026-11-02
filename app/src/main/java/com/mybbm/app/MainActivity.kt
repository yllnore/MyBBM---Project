package com.mybbm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import com.mybbm.app.ui.MainAppScreen
import com.mybbm.app.ui.theme.MyBBMTheme
import com.mybbm.app.viewmodel.BbmViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BbmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyBBMTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFE8ECEF)
                ) {
                    MainAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}
