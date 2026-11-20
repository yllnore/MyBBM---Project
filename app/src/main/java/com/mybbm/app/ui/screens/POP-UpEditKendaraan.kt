package com.mybbm.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mybbm.app.domain.model.VehicleModel
import com.mybbm.app.util.FuelCalculator
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
    var efficiencyInput by remember(vehicle) {
        mutableStateOf(
            vehicle?.fuelEfficiency?.let { FuelCalculator.formatEfficiency(it) } ?: ""
        )
    }
    var errorMessage by remember(vehicle) { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .width(320.dp)
            .wrapContentHeight()
            .heightIn(min = 315.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFBFB7FA),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = Color(0xFFF1EFFF),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 18.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
        ) {
            // Header: Judul Dialog + Icon Hapus
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Detail Kendaraan",
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight(700),
                        color = Color(0xFF090F59)
                    )
                )

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onDeleteClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Hapus Kendaraan",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Input Field 1: Nama Kendaraan
            DialogInputField(
                value = nameInput,
                onValueChange = {
                    nameInput = it
                    errorMessage = null
                },
                placeholder = "Nama Kendaraan",
                heightDp = 38.dp
            )

            // Input Field 2: Segmented Selector Jenis Kendaraan (Mobil | Motor)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .background(Color(0xFFE4E1FC), RoundedCornerShape(12.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab Mobil
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (typeInput == "Mobil") Color(0xFF7542EF) else Color.Transparent,
                            RoundedCornerShape(9.dp)
                        )
                        .clickable { typeInput = "Mobil" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Mobil",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (typeInput == "Mobil") Color.White else Color(0xFF4A4E69)
                        )
                    )
                }

                // Tab Motor
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (typeInput == "Motor") Color(0xFF7542EF) else Color.Transparent,
                            RoundedCornerShape(9.dp)
                        )
                        .clickable { typeInput = "Motor" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Motor",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (typeInput == "Motor") Color.White else Color(0xFF4A4E69)
                        )
                    )
                }
            }

            // Input Field 3: Konsumsi BBM km/L (Otomatis format ribuan titik seperti 1.000)
            DialogInputField(
                value = efficiencyInput,
                onValueChange = {
                    efficiencyInput = FuelCalculator.formatThousandInput(it)
                    errorMessage = null
                },
                placeholder = "Konsumsi BBM km/L",
                heightDp = 38.dp,
                keyboardType = KeyboardType.Decimal
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = TextStyle(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFDC2626),
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Baris Tombol Simpan & Batal
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tombol Simpan (Ungu Tema Brand #7542EF)
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 2.2.dp,
                            spotColor = Color(0x40000000),
                            ambientColor = Color(0x40000000),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .width(120.dp)
                        .height(38.dp)
                        .background(
                            color = Color(0xFF7542EF),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            val trimmedName = nameInput.trim()
                            val eff = FuelCalculator.parseEfficiency(efficiencyInput)
                            val id = vehicle?.id?.takeIf { it.isNotBlank() }
                            if (trimmedName.isEmpty() || eff == null || eff <= BigDecimal.ZERO) {
                                errorMessage = "Harap isi nama dan nilai angka yang valid"
                            } else if (id != null) {
                                errorMessage = null
                                val finalType = typeInput.ifBlank { "Mobil" }
                                onSimpanClick(id, trimmedName, finalType, eff)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Simpan",
                        style = TextStyle(
                            fontSize = 14.sp,
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
                        .width(120.dp)
                        .height(38.dp)
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
                            fontSize = 14.sp,
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
