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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal

@Composable
fun AddVehicleDialog(
    onSimpanClick: (name: String, type: String, efficiency: BigDecimal) -> Unit = { _, _, _ -> },
    onBatalClick: () -> Unit = {}
) {
    var nameInput by remember { mutableStateOf("") }
    var typeInput by remember { mutableStateOf("") }
    var efficiencyInput by remember { mutableStateOf("") }

    // Card Container Utama
    Box(
        modifier = Modifier
            .width(256.dp)
            .height(235.dp)
            .border(
                width = 1.dp,
                color = Color(0xFFBFB7FA),
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = Color(0xFFF1EFFF),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Judul Dialog
            Text(
                text = "Tambah Kendaraan Baru",
                style = TextStyle(
                    fontSize = 16.sp,
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
                onValueChange = { nameInput = it },
                placeholder = "Nama Kendaraan",
                heightDp = 32.dp
            )

            // Input Field 2: Jenis Kendaraan (Mobil / Motor)
            DialogInputField(
                value = typeInput,
                onValueChange = { typeInput = it },
                placeholder = "Jenis Kendaraan (Mobil/Motor)",
                heightDp = 32.dp
            )

            // Input Field 3: Konsumsi BBM km/L
            DialogInputField(
                value = efficiencyInput,
                onValueChange = { efficiencyInput = it },
                placeholder = "Konsumsi BBM km/L (misal 15.0)",
                heightDp = 32.dp,
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(4.dp))

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
                            ambientColor = Color(0x40000000)
                        )
                        .width(95.dp)
                        .height(30.dp)
                        .background(
                            color = Color(0xFF5DB1E7),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            val eff = efficiencyInput.toBigDecimalOrNull() ?: BigDecimal("12.0")
                            val finalName = nameInput.ifBlank { "KENDARAAN BARU" }
                            val finalType = typeInput.ifBlank { "Mobil" }
                            onSimpanClick(finalName, finalType, eff)
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
                            ambientColor = Color(0x40000000)
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
            fontSize = 12.sp,
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight(500),
            color = Color(0xFF090F59)
        ),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .width(209.dp)
                    .height(heightDp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFBFB7FA),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            fontSize = 11.sp,
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

@Preview(showBackground = true)
@Composable
fun AddVehicleDialogPreview() {
    AddVehicleDialog()
}