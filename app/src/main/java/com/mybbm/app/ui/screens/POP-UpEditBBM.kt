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
import com.mybbm.app.domain.model.FuelModel
import java.math.BigDecimal

@Composable
fun EditBbmDialog(
    fuel: FuelModel? = null,
    onSimpanClick: (id: String, name: String, price: BigDecimal) -> Unit = { _, _, _ -> },
    onBatalClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    var nameInput by remember(fuel) { mutableStateOf(fuel?.name ?: "") }
    var priceInput by remember(fuel) { mutableStateOf(fuel?.pricePerLiter?.toPlainString() ?: "") }

    Box(
        modifier = Modifier
            .width(256.dp)
            .height(190.dp)
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
                    text = "Edit Detail BBM",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight(700),
                        color = Color(0xFF090F59)
                    )
                )

                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Hapus BBM",
                    tint = Color(0xFF8271D4),
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onDeleteClick() }
                )
            }

            // Input Field 1: Nama Jenis BBM
            DialogInputField(
                value = nameInput,
                onValueChange = { nameInput = it },
                placeholder = "Nama Jenis BBM",
                heightDp = 32.dp
            )

            // Input Field 2: Harga BBM per Liter
            DialogInputField(
                value = priceInput,
                onValueChange = { priceInput = it },
                placeholder = "Harga BBM per Liter",
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
                            val price = priceInput.toBigDecimalOrNull() ?: fuel?.pricePerLiter ?: BigDecimal("10000")
                            val id = fuel?.id ?: ""
                            onSimpanClick(id, nameInput, price)
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
fun EditBbmDialogPreview() {
    EditBbmDialog()
}