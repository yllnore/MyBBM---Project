package com.mybbm.app.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object FuelCalculator {

    fun calculateFuelNeeded(distanceKm: BigDecimal, efficiencyKmPerLiter: BigDecimal): BigDecimal {
        if (efficiencyKmPerLiter <= BigDecimal.ZERO) return BigDecimal.ZERO
        return distanceKm.divide(efficiencyKmPerLiter, 2, RoundingMode.HALF_UP)
    }

    fun calculateTotalCost(liters: BigDecimal, pricePerLiter: BigDecimal): BigDecimal {
        return (liters.multiply(pricePerLiter)).setScale(0, RoundingMode.HALF_UP)
    }

    fun formatRupiah(amount: BigDecimal): String {
        val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val formatter = DecimalFormat("#,###", symbols)
        return "Rp. " + formatter.format(amount.toLong())
    }

    fun formatLiters(liters: BigDecimal): String {
        val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
            decimalSeparator = ','
        }
        val formatter = DecimalFormat("0.#", symbols)
        return "${formatter.format(liters)} Liter"
    }

    fun formatDistance(km: BigDecimal): String {
        val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
            decimalSeparator = ','
        }
        val formatter = DecimalFormat("0.#", symbols)
        return "${formatter.format(km)} km"
    }
}
