package com.mybbm.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mybbm.app.R
import com.mybbm.app.domain.model.VehicleModel
import com.mybbm.app.viewmodel.BbmViewModel

@Composable
fun VehicleSettingsScreen(
    onBackClick: () -> Unit = {},
    onAddClick: () -> Unit = {},
    onEditVehicleClick: (String) -> Unit = {},
    viewModel: BbmViewModel? = null
) {
    val vehicles by (viewModel?.vehicles?.collectAsState() ?: androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(emptyList())
    })

    // Outer Frame Canvas (412.dp x 917.dp)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start,
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = Color(0xFFCCC7FB),
                    shape = RoundedCornerShape(size = 27.dp)
                )
                .fillMaxSize()
                .background(color = Color(0xFFECEBFF), shape = RoundedCornerShape(size = 27.dp))
        ) {
            // Container Bagian Atas (Header Ungu)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFFBDB8F8),
                        shape = RoundedCornerShape(topStart = 27.dp, topEnd = 27.dp)
                    )
            ) {
                // Status Bar / Spacer
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                }

                // Title Bar (Kembali, Judul "Kendaraan", & Tambah (+))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(66.dp)
                        .padding(start = 20.dp, end = 20.dp, bottom = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF090F59),
                            modifier = Modifier
                                .size(28.dp)
                                .clickable { onBackClick() }
                        )

                        Text(
                            text = "Kendaraan",
                            style = TextStyle(
                                fontSize = 27.sp,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight(700),
                                color = Color(0xFF090F59)
                            )
                        )
                    }

                    // Icon Plus (+)
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Kendaraan",
                        tint = Color(0xFF090F59),
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                if (viewModel != null) {
                                    viewModel.showAddVehicleDialog.value = true
                                } else {
                                    onAddClick()
                                }
                            }
                    )
                }
            }

            // Daftar Item Kendaraan dengan Vertical Scroll
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 19.dp, top = 20.dp, end = 20.dp, bottom = 20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                if (vehicles.isEmpty()) {
                    // Default fallback visual sesuai Figma
                    VehicleCardItem(
                        name = "AVANZA",
                        consumption = "12.5 km/L",
                        iconRes = R.drawable.mobil,
                        onEditClick = { onEditVehicleClick("AVANZA") }
                    )
                    VehicleCardItem(
                        name = "TOYOTA",
                        consumption = "14.0 km/L",
                        iconRes = R.drawable.mobil,
                        onEditClick = { onEditVehicleClick("TOYOTA") }
                    )
                    VehicleCardItem(
                        name = "HONDA",
                        consumption = "15.0 km/L",
                        iconRes = R.drawable.mobil,
                        onEditClick = { onEditVehicleClick("HONDA") }
                    )
                    VehicleCardItem(
                        name = "HONDA BEAT",
                        consumption = "55.0 km/L",
                        iconRes = R.drawable.motor,
                        onEditClick = { onEditVehicleClick("HONDA BEAT") }
                    )
                    VehicleCardItem(
                        name = "HONDA VARIO",
                        consumption = "48.0 km/L",
                        iconRes = R.drawable.motor,
                        onEditClick = { onEditVehicleClick("HONDA VARIO") }
                    )
                    VehicleCardItem(
                        name = "NMAX",
                        consumption = "40.0 km/L",
                        iconRes = R.drawable.motor,
                        onEditClick = { onEditVehicleClick("NMAX") }
                    )
                    VehicleCardItem(
                        name = "FAZZIO",
                        consumption = "50.0 km/L",
                        iconRes = R.drawable.motor,
                        onEditClick = { onEditVehicleClick("FAZZIO") }
                    )
                } else {
                    vehicles.forEach { v ->
                        val iconRes = if (v.type.equals("Motor", ignoreCase = true)) R.drawable.motor else R.drawable.mobil
                        VehicleCardItem(
                            name = v.name,
                            consumption = "${v.fuelEfficiency} km/L",
                            iconRes = iconRes,
                            onEditClick = {
                                if (viewModel != null) {
                                    viewModel.vehicleToEditOrDelete.value = v
                                    viewModel.showEditVehicleDialog.value = true
                                } else {
                                    onEditVehicleClick(v.name)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VehicleCardItem(
    name: String,
    consumption: String,
    iconRes: Int,
    onEditClick: () -> Unit = {}
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .shadow(
                elevation = 3.dp,
                spotColor = Color(0x166765AA),
                ambientColor = Color(0x166765AA)
            )
            .border(
                width = 1.7.dp,
                color = Color(0xFFCCC9F4),
                shape = RoundedCornerShape(size = 21.dp)
            )
            .fillMaxWidth()
            .height(95.dp)
            .background(color = Color(0xFFF1EFFF), shape = RoundedCornerShape(size = 21.dp))
            .padding(start = 16.dp, end = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Box Ungu Muda untuk Ikon Kendaraan
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFBDB8F8)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = name,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Teks Nama Kendaraan & Konsumsi BBM
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = name,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight(700),
                        color = Color(0xFF090F59)
                    )
                )
                Text(
                    text = consumption,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight(500),
                        color = Color(0xFF090F59)
                    )
                )
            }
        }

        // Icon Pensil Edit
        Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "Edit $name",
            tint = Color(0xFF090F59),
            modifier = Modifier
                .size(20.dp)
                .clickable { onEditClick() }
        )
    }
}

@Preview(showBackground = true, widthDp = 412, heightDp = 917)
@Composable
fun VehicleSettingsScreenPreview() {
    VehicleSettingsScreen()
}
