package com.mybbm.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mybbm.app.domain.model.VehicleModel
import java.math.BigDecimal

@Composable
fun EditVehicleDialog(
    vehicle: VehicleModel? = null,
    onSimpanClick: (id: String, name: String, type: String, efficiency: BigDecimal) -> Unit = { _, _, _, _ -> },
    onBatalClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    var nameInput by remember(vehicle) { mutableStateOf(vehicle?.name ?: "") }
    var typeInput by remember(vehicle) { mutableStateOf(vehicle?.type ?: "Mobil") }
    var efficiencyInput by remember(vehicle) { mutableStateOf(vehicle?.fuelEfficiency?.toPlainString() ?: "") }

    Box(
        modifier = Modifier
            .width(256.dp)
            .height(210.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFBFB7FA),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = Color(0xFFF1EFFF),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
        ) {
            // Header: Judul Dialog + Icon Hapus
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Detail Kendaraan",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight(700),
                        color = Color(0xFF090F59)
                    )
                )

                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Hapus Kendaraan",
                    tint = Color(0xFF8271D4),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onDeleteClick() }
                )
            }

            // Input Field 1: Nama Kendaraan
            DialogInputField(
                value = nameInput,
                onValueChange = { nameInput = it },
                placeholder = "Nama Kendaraan",
                heightDp = 32.dp
            )

            // Input Field 2: Konsumsi BBM km/L
            DialogInputField(
                value = efficiencyInput,
                onValueChange = { efficiencyInput = it },
                placeholder = "Konsumsi BBM km/L",
                heightDp = 32.dp,
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Baris Tombol Simpan & Batal
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Simpan (Biru)
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 2.2.dp,
                            spotColor = Color(0x40000000),
                            ambientColor = Color(0x40000000),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .width(95.dp)
                        .height(30.dp)
                        .background(
                            color = Color(0xFF5DB1E7),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            val eff = efficiencyInput.toBigDecimalOrNull() ?: vehicle?.fuelEfficiency ?: BigDecimal("12.0")
                            val id = vehicle?.id ?: ""
                            onSimpanClick(id, nameInput, typeInput, eff)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Simpan",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight(700),
                            color = Color(0xFFFFFFFF)
                        )
                    )
                }

                // Tombol Batal (Abu-abu)
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 2.2.dp,
                            spotColor = Color(0x40000000),
                            ambientColor = Color(0x40000000),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .width(95.dp)
                        .height(30.dp)
                        .background(
                            color = Color(0xFFE6E6E6),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onBatalClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Batal",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Default,
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
fun EditVehicleDialogPreview() {
    EditVehicleDialog()
}