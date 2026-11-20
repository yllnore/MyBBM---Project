package com.mybbm.app.util

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AirportShuttle
import androidx.compose.material.icons.outlined.Commute
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import com.mybbm.app.R

/**
 * Helper terpusat untuk mengelola daftar opsi jenis kendaraan, normalisasi tipe,
 * dan pemetaan ikon agar konsisten di seluruh dialog, daftar kendaraan, dan Beranda.
 */
object VehicleTypeHelper {

    const val CUSTOM_OPTION = "Lainnya"

    val PRESET_TYPES: List<String> = listOf(
        "Mobil",
        "Motor",
        "Pickup",
        "Truk",
        "Bus"
    )

    val DROPDOWN_OPTIONS: List<String> = PRESET_TYPES + CUSTOM_OPTION

    fun isPresetType(type: String): Boolean {
        val trimmed = type.trim()
        return PRESET_TYPES.any { it.equals(trimmed, ignoreCase = true) }
    }

    fun normalizeType(rawType: String): String {
        val trimmed = rawType.trim()
        if (trimmed.isBlank()) return "Mobil"

        val matchedPreset = PRESET_TYPES.firstOrNull { it.equals(trimmed, ignoreCase = true) }
        if (matchedPreset != null) return matchedPreset

        return when {
            trimmed.contains("motor", ignoreCase = true) -> "Motor"
            trimmed.contains("pickup", ignoreCase = true) || trimmed.contains("pick up", ignoreCase = true) -> "Pickup"
            trimmed.contains("truk", ignoreCase = true) || trimmed.contains("truck", ignoreCase = true) -> "Truk"
            trimmed.contains("bus", ignoreCase = true) -> "Bus"
            trimmed.equals("Mobil", ignoreCase = true) -> "Mobil"
            else -> trimmed.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
    }
}

@Composable
fun VehicleTypeIcon(
    type: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    when (VehicleTypeHelper.normalizeType(type)) {
        "Mobil" -> Image(
            painter = painterResource(id = R.drawable.mobil),
            contentDescription = "Mobil",
            colorFilter = ColorFilter.tint(tint),
            modifier = modifier
        )
        "Motor" -> Image(
            painter = painterResource(id = R.drawable.motor),
            contentDescription = "Motor",
            colorFilter = ColorFilter.tint(tint),
            modifier = modifier
        )
        "Pickup" -> Icon(
            imageVector = Icons.Outlined.AirportShuttle,
            contentDescription = "Pickup",
            tint = tint,
            modifier = modifier
        )
        "Truk" -> Icon(
            imageVector = Icons.Outlined.LocalShipping,
            contentDescription = "Truk",
            tint = tint,
            modifier = modifier
        )
        "Bus" -> Icon(
            imageVector = Icons.Outlined.DirectionsBus,
            contentDescription = "Bus",
            tint = tint,
            modifier = modifier
        )
        else -> Icon(
            imageVector = Icons.Outlined.Commute,
            contentDescription = type.ifBlank { "Kendaraan" },
            tint = tint,
            modifier = modifier
        )
    }
}
