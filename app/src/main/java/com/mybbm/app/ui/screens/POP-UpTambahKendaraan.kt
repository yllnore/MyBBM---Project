package com.mybbm.app.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.mybbm.app.R
import com.mybbm.app.util.VehicleTypeHelper
import com.mybbm.app.util.VehicleTypeIcon
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mybbm.app.util.FuelCalculator
import java.math.BigDecimal

@Composable
fun AddVehicleDialog(
    onSimpanClick: (name: String, type: String, efficiency: BigDecimal) -> Unit = { _, _, _ -> },
    onBatalClick: () -> Unit = {}
) {
    var nameInput by remember { mutableStateOf("") }
    var typeInput by remember { mutableStateOf("Mobil") }
    var efficiencyInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Card Container Utama
    Box(
        modifier = Modifier
            .width(320.dp)
            .wrapContentHeight()
            .heightIn(min = 305.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFBFB7FA),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = Color(0xFFF1EFFF),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 20.dp, horizontal = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Judul Dialog
            Text(
                text = "Tambah Kendaraan Baru",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight(700),
                    color = Color(0xFF090F59),
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

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

            // Input Field 2: Pilihan Jenis Kendaraan (Mobil / Motor) sejajar presisi dengan input field (260.dp)
            VehicleTypeSelector(
                selectedType = typeInput,
                onSelectType = { typeInput = it }
            )

            // Input Field 3: Konsumsi BBM km/L (Otomatis format ribuan titik seperti 1.000)
            DialogInputField(
                value = efficiencyInput,
                onValueChange = {
                    efficiencyInput = FuelCalculator.formatThousandInput(it)
                    errorMessage = null
                },
                placeholder = "Konsumsi BBM km/L (misal 15.0)",
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
                            val trimmedType = typeInput.trim()
                            val eff = FuelCalculator.parseEfficiency(efficiencyInput)
                            if (trimmedName.isEmpty() || trimmedType.isEmpty() || eff == null || eff <= BigDecimal.ZERO) {
                                errorMessage = "Harap isi nama, jenis kendaraan, dan angka yang valid"
                            } else {
                                errorMessage = null
                                val finalType = VehicleTypeHelper.normalizeType(trimmedType)
                                onSimpanClick(trimmedName, finalType, eff)
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

@Composable
fun DialogInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    heightDp: Dp,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        textStyle = TextStyle(
            fontSize = 14.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight(500),
            color = Color(0xFF090F59)
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .width(260.dp)
                    .height(heightDp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFBFB7FA),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight(400),
                            color = Color(0xFF8B92B2)
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun VehicleTypeSelector(
    selectedType: String,
    onSelectType: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var isCustomMode by remember(selectedType) {
        mutableStateOf(selectedType.isBlank() || !VehicleTypeHelper.isPresetType(selectedType))
    }

    val displayLabel = if (isCustomMode) {
        VehicleTypeHelper.CUSTOM_OPTION
    } else {
        VehicleTypeHelper.normalizeType(selectedType)
    }

    Column(
        modifier = Modifier.width(260.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = if (expanded) Color(0xFF7542EF) else Color(0xFFBFB7FA),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VehicleTypeIcon(
                        type = if (isCustomMode) VehicleTypeHelper.CUSTOM_OPTION else displayLabel,
                        tint = Color(0xFF090F59),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = displayLabel,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Default,
                            fontWeight = FontWeight(500),
                            color = Color(0xFF090F59)
                        )
                    )
                }

                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = "Pilih Jenis Kendaraan",
                    tint = Color(0xFF4A4E69),
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .width(260.dp)
                    .background(Color.White)
            ) {
                VehicleTypeHelper.DROPDOWN_OPTIONS.forEach { option ->
                    val isOptionSelected = (option == VehicleTypeHelper.CUSTOM_OPTION && isCustomMode) ||
                        (!isCustomMode && option.equals(displayLabel, ignoreCase = true))
                    DropdownMenuItem(
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                VehicleTypeIcon(
                                    type = option,
                                    tint = if (isOptionSelected) Color(0xFF7542EF) else Color(0xFF090F59),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (option == VehicleTypeHelper.CUSTOM_OPTION) "Lainnya (Kustom)" else option,
                                    style = TextStyle(
                                        fontSize = 14.sp,
                                        fontWeight = if (isOptionSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isOptionSelected) Color(0xFF7542EF) else Color(0xFF090F59)
                                    )
                                )
                            }
                        },
                        onClick = {
                            expanded = false
                            if (option == VehicleTypeHelper.CUSTOM_OPTION) {
                                isCustomMode = true
                                if (VehicleTypeHelper.isPresetType(selectedType)) {
                                    onSelectType("")
                                }
                            } else {
                                isCustomMode = false
                                onSelectType(option)
                            }
                        }
                    )
                }
            }
        }

        if (isCustomMode) {
            DialogInputField(
                value = if (VehicleTypeHelper.isPresetType(selectedType)) "" else selectedType,
                onValueChange = { onSelectType(it) },
                placeholder = "Ketik jenis (misal: Angkot, Genset)",
                heightDp = 38.dp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddVehicleDialogPreview() {
    AddVehicleDialog()
}
