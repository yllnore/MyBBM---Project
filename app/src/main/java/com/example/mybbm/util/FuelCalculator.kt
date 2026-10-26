package com.example.mybbm.util

import java.text.NumberFormat
import java.util.Locale

object FuelCalculator {

    fun calculateLiters(distanceKm: Double, efficiencyKmPerL: Double): Double {
        if (efficiencyKmPerL <= 0.0) return 0.0
        return distanceKm / efficiencyKmPerL
    }

    fun calculateCost(liters: Double, pricePerLiter: Double): Double {
        if (liters <= 0.0 || pricePerLiter <= 0.0) return 0.0
        return liters * pricePerLiter
    }

    fun formatRupiah(amount: Double): String {
        val localeID = Locale.Builder().setLanguage("id").setRegion("ID").build()
        val formatter = NumberFormat.getCurrencyInstance(localeID)
        formatter.maximumFractionDigits = 0
        return formatter.format(amount).replace("IDR", "Rp").replace("Rp", "Rp ")
    }

    fun formatLiters(liters: Double): String {
        return String.format(Locale.getDefault(), "%.2f Liter", liters)
    }

    fun formatDistance(distanceKm: Double): String {
        return String.format(Locale.getDefault(), "%.1f km", distanceKm)
    }
}
