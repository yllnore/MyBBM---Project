package com.mybbm.app.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mybbm.app.ui.navigation.Screen
import com.mybbm.app.ui.screens.*
import com.mybbm.app.viewmodel.BbmViewModel

@Composable
fun MainScreen(viewModel: BbmViewModel = viewModel()) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        // Layar Beranda Utama (Figma)
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToResult = { navController.navigate(Screen.Result.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToHistory = { navController.navigate(Screen.History.route) }
            )
        }

        // Layar Riwayat (Figma Histori.kt)
        composable(Screen.History.route) {
            HistoriScreen(
                viewModel = viewModel,
                onCloseClick = { navController.popBackStack() }
            )
        }

        // Layar Pengaturan (Figma Pengaturan.kt)
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onMenuItemClick = { item ->
                    when (item) {
                        "Kendaraan" -> navController.navigate(Screen.Vehicle.route)
                        "BBM" -> navController.navigate(Screen.Fuel.route)
                        "Tentang MYBBM" -> navController.navigate(Screen.About.route)
                    }
                }
            )
        }

        // Layar Kelola Kendaraan (Figma Kendaraan.kt)
        composable(Screen.Vehicle.route) {
            VehicleSettingsScreen(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() }
            )
        }

        // Layar Kelola BBM (Figma JenisBBMTerisi.kt)
        composable(Screen.Fuel.route) {
            ScreenPertamaxBbm()
        }

        // Layar Tentang Aplikasi (Figma TentangMYBBM.kt)
        composable(Screen.About.route) {
            TentangMyBbmScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Layar Hasil Estimasi Biaya
        composable(Screen.Result.route) {
            ResultScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
