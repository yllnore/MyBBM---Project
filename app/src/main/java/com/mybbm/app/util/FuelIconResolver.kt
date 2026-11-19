package com.mybbm.app.util

import androidx.annotation.DrawableRes
import com.mybbm.app.R

/**
 * Utility untuk memetakan nama jenis BBM secara deterministik ke resource drawable statis.
 * Mencegah crash `IllegalArgumentException` pada Compose painterResource yang disebabkan oleh
 * perubahan compiled integer ID (AAPT churn) di Room Database.
 */
object FuelIconResolver {

    @DrawableRes
    fun resolveIcon(fuelName: String?): Int {
        if (fuelName.isNullOrBlank()) return R.drawable.pertamax

        val normalized = fuelName.uppercase().trim()
        return when {
            normalized.contains("PERTALITE") -> R.drawable.pertalite
            normalized.contains("TURBO") -> R.drawable.pertamax_turbo
            normalized.contains("SOLAR") ||
            normalized.contains("BIO") ||
            normalized.contains("DEXLITE") ||
            normalized.contains("DEX") -> R.drawable.solar
            else -> R.drawable.pertamax
        }
    }
}
