package com.mybbm.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mybbm.app.ui.screens.*
import com.mybbm.app.viewmodel.AppScreen
import com.mybbm.app.viewmodel.BbmViewModel
import kotlinx.coroutines.delay
import java.math.BigDecimal

@Composable
fun MainAppScreen(viewModel: BbmViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    // Dialog States
    val showAddVehicle by viewModel.showAddVehicleDialog.collectAsState()
    val showEditVehicle by viewModel.showEditVehicleDialog.collectAsState()
    val showDeleteVehicle by viewModel.showDeleteVehicleDialog.collectAsState()
    val selectedVehicleToEdit by viewModel.vehicleToEditOrDelete.collectAsState()

    val showAddFuel by viewModel.showAddFuelDialog.collectAsState()
    val showEditFuel by viewModel.showEditFuelDialog.collectAsState()
    val showDeleteFuel by viewModel.showDeleteFuelDialog.collectAsState()
    val selectedFuelToEdit by viewModel.fuelToEditOrDelete.collectAsState()

    val showDeleteHistory by viewModel.showDeleteHistoryDialog.collectAsState()
    val selectedHistoryToDelete by viewModel.historyToDelete.collectAsState()

    // Auto-dismiss toast after 2.5 seconds
    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2500)
            viewModel.clearToast()
        }
    }

    // Intercept back button when not in HOME
    BackHandler(enabled = currentScreen != AppScreen.HOME) {
        when (currentScreen) {
            AppScreen.ABOUT -> viewModel.navigateTo(AppScreen.SETTINGS)
            AppScreen.VEHICLES -> viewModel.navigateTo(AppScreen.SETTINGS)
            AppScreen.FUELS -> viewModel.navigateTo(AppScreen.SETTINGS)
            else -> viewModel.navigateTo(AppScreen.HOME)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Current Active Screen
        when (currentScreen) {
            AppScreen.HOME -> {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToResult = { viewModel.navigateTo(AppScreen.RESULT) },
                    onNavigateToSettings = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    onNavigateToHistory = { viewModel.navigateTo(AppScreen.HISTORI) },
                    onNavigateToVehicles = { viewModel.navigateTo(AppScreen.VEHICLES) },
                    onNavigateToFuels = { viewModel.navigateTo(AppScreen.FUELS) }
                )
            }
            AppScreen.RESULT -> {
                ResultScreen(
                    viewModel = viewModel,
                    onNavigateBack = { viewModel.navigateTo(AppScreen.HOME) },
                    onNavigateToSettings = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    onNavigateToHistory = { viewModel.navigateTo(AppScreen.HISTORI) }
                )
            }
            AppScreen.SETTINGS -> {
                SettingsScreen(
                    onBackClick = { viewModel.navigateTo(AppScreen.HOME) },
                    onMenuItemClick = { item ->
                        when (item) {
                            "Kendaraan" -> viewModel.navigateTo(AppScreen.VEHICLES)
                            "BBM" -> viewModel.navigateTo(AppScreen.FUELS)
                            "Tentang MYBBM" -> viewModel.navigateTo(AppScreen.ABOUT)
                        }
                    }
                )
            }
            AppScreen.ABOUT -> {
                TentangMyBbmScreen(
                    onBackClick = { viewModel.navigateTo(AppScreen.SETTINGS) }
                )
            }
            AppScreen.VEHICLES -> {
                VehicleSettingsScreen(
                    onBackClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    viewModel = viewModel
                )
            }
            AppScreen.FUELS -> {
                BbmSettingsScreen(
                    onBackClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                    viewModel = viewModel
                )
            }
            AppScreen.HISTORI -> {
                HistoriScreen(
                    onCloseClick = { viewModel.navigateTo(AppScreen.HOME) },
                    viewModel = viewModel
                )
            }
            AppScreen.INPUT_ORIGIN -> {
                MemasukkanLokasiAwalScreen(
                    onCalculateClick = {
                        viewModel.calculateTripCost()
                    },
                    onLocationItemClick = { chosen ->
                        viewModel.setOrigin(chosen)
                        viewModel.navigateTo(AppScreen.HOME)
                    }
                )
            }
            AppScreen.INPUT_DESTINATION -> {
                MemasukkanLokasiTujuanScreen(
                    onLocationSelected = { chosen ->
                        viewModel.setDestination(chosen)
                        viewModel.navigateTo(AppScreen.HOME)
                    },
                    onBackClick = { viewModel.navigateTo(AppScreen.HOME) }
                )
            }
        }

        // 2. Modals & Dialog Overlays
        if (showAddVehicle) {
            DialogScrim(onDismiss = { viewModel.showAddVehicleDialog.value = false }) {
                AddVehicleDialog(
                    onSimpanClick = { name, type, efficiency ->
                        viewModel.addVehicle(name, type, efficiency)
                    },
                    onBatalClick = { viewModel.showAddVehicleDialog.value = false }
                )
            }
        }

        if (showEditVehicle) {
            DialogScrim(onDismiss = { viewModel.showEditVehicleDialog.value = false }) {
                EditVehicleDialog(
                    vehicle = selectedVehicleToEdit,
                    onSimpanClick = { id, name, type, efficiency ->
                        viewModel.updateVehicle(id, name, type, efficiency)
                    },
                    onBatalClick = { viewModel.showEditVehicleDialog.value = false },
                    onDeleteClick = {
                        viewModel.showEditVehicleDialog.value = false
                        viewModel.showDeleteVehicleDialog.value = true
                    }
                )
            }
        }

        if (showDeleteVehicle) {
            DialogScrim(onDismiss = { viewModel.showDeleteVehicleDialog.value = false }) {
                DeleteConfirmationDialog(
                    onBatalClick = { viewModel.showDeleteVehicleDialog.value = false },
                    onHapusClick = {
                        selectedVehicleToEdit?.let {
                            viewModel.deleteVehicle(it.id)
                        }
                    }
                )
            }
        }

        if (showAddFuel) {
            DialogScrim(onDismiss = { viewModel.showAddFuelDialog.value = false }) {
                AddBbmDialog(
                    onSimpanClick = { name, price ->
                        viewModel.addFuel(name, price)
                    },
                    onBatalClick = { viewModel.showAddFuelDialog.value = false }
                )
            }
        }

        if (showEditFuel) {
            DialogScrim(onDismiss = { viewModel.showEditFuelDialog.value = false }) {
                EditBbmDialog(
                    fuel = selectedFuelToEdit,
                    onSimpanClick = { id, name, price ->
                        viewModel.updateFuel(id, name, price)
                    },
                    onBatalClick = { viewModel.showEditFuelDialog.value = false },
                    onDeleteClick = {
                        viewModel.showEditFuelDialog.value = false
                        viewModel.showDeleteFuelDialog.value = true
                    }
                )
            }
        }

        if (showDeleteFuel) {
            DialogScrim(onDismiss = { viewModel.showDeleteFuelDialog.value = false }) {
                DeleteBbmDialog(
                    onBatalClick = { viewModel.showDeleteFuelDialog.value = false },
                    onHapusClick = {
                        selectedFuelToEdit?.let {
                            viewModel.deleteFuel(it.id)
                        }
                    }
                )
            }
        }

        if (showDeleteHistory) {
            DeleteHistoryDialog(
                onDismissRequest = { viewModel.showDeleteHistoryDialog.value = false },
                onConfirmDelete = {
                    selectedHistoryToDelete?.let {
                        viewModel.deleteHistory(it.id)
                    } ?: run {
                        viewModel.showDeleteHistoryDialog.value = false
                    }
                }
            )
        }

        // 3. Floating Toast Notifications
        if (toastMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                if (toastMessage!!.contains("hapus", ignoreCase = true)) {
                    ToastBerhasilDihapus(onCloseClick = { viewModel.clearToast() })
                } else {
                    SuccessToast(onCloseClick = { viewModel.clearToast() })
                }
            }
        }
    }
}

@Composable
private fun DialogScrim(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x80000000))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { /* Prevent dismissing when clicking inside dialog */ }) {
            content()
        }
    }
}
