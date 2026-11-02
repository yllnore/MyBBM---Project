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
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val formatter = DecimalFormat("#,##0.#", symbols)
        return "${formatter.format(liters)} Liter"
    }

    fun formatDistance(km: BigDecimal): String {
        val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
            groupingSeparator = '.'
            decimalSeparator = ','
        }
        val formatter = DecimalFormat("#,##0.#", symbols)
        return "${formatter.format(km)} km"
    }

    /**
     * Memformat angka bulat (Long / BigDecimal) dengan pemisah ribuan titik (ID).
     * Contoh: 1000 -> "1.000", 12500 -> "12.500", 10000 -> "10.000".
     */
    fun formatThousand(value: Long): String {
        val symbols = DecimalFormatSymbols(Locale("id", "ID")).apply {
            groupingSeparator = '.'
        }
        val formatter = DecimalFormat("#,##0", symbols)
        return formatter.format(value)
    }

    fun formatThousand(value: BigDecimal): String {
        return formatThousand(value.toLong())
    }

    /**
     * Format konsumsi efisiensi kendaraan (km/L).
     * Jika >= 1000, memformat ribuan dengan titik (contoh 1000 -> "1.000").
     * Jika desimal (contoh 12.5), mempertahankan "12.5" atau "14.0".
     */
    fun formatEfficiency(efficiency: BigDecimal): String {
        val plain = efficiency.toPlainString()
        val parts = plain.split('.')
        val intPart = parts[0]
        val decPart = if (parts.size > 1) parts[1] else null

        val intNum = intPart.toLongOrNull() ?: return plain
        val formattedInt = formatThousand(intNum)

        return if (decPart != null && decPart.isNotEmpty() && decPart != "0") {
            "$formattedInt.$decPart"
        } else if (decPart == "0" && intNum < 1000) {
            "$intNum.0"
        } else {
            formattedInt
        }
    }

    /**
     * Parsing efisiensi BBM dari string masukan pengguna secara aman:
     * - "1.000" -> BigDecimal("1000")
     * - "1000" -> BigDecimal("1000")
     * - "12.5" -> BigDecimal("12.5")
     * - "12,5" -> BigDecimal("12.5")
     * - "1.000,5" -> BigDecimal("1000.5")
     * - "10.000" -> BigDecimal("10000")
     */
    fun parseEfficiency(input: String): BigDecimal? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null

        // Jika mengandung koma, koma adalah pemisah desimal
        if (trimmed.contains(',')) {
            val sanitized = trimmed.replace(".", "").replace(",", ".")
            return sanitized.toBigDecimalOrNull()
        }

        val dotCount = trimmed.count { it == '.' }
        if (dotCount > 1) {
            // Lebih dari 1 titik menandakan pemisah ribuan (misal "1.000.000")
            val sanitized = trimmed.replace(".", "")
            return sanitized.toBigDecimalOrNull()
        }

        if (dotCount == 1) {
            val parts = trimmed.split('.')
            val integerPart = parts[0]
            val fractionalPart = parts[1]
            // Format ribuan Indonesia: 1 titik diikuti tepat 3 angka (misal "1.000", "10.000")
            return if (fractionalPart.length == 3 && fractionalPart.all { it.isDigit() }) {
                (integerPart + fractionalPart).toBigDecimalOrNull()
            } else {
                trimmed.toBigDecimalOrNull()
            }
        }

        return trimmed.toBigDecimalOrNull()
    }

    /**
     * Memformat input harga BBM saat pengguna mengetik angka.
     * Mengelompokkan digit menjadi ribuan dengan titik (contoh "1000" -> "1.000", "12500" -> "12.500").
     */
    fun formatDigitsAsThousand(input: String): String {
        val digits = input.filter { it.isDigit() }
        if (digits.isEmpty()) return ""
        val number = digits.toLongOrNull() ?: return digits
        return formatThousand(number)
    }

    /**
     * Memformat input efisiensi kendaraan saat pengguna mengetik.
     * Mengubah 1000 -> "1.000", namun tetap mendukung pengetikan desimal (seperti 12.5 atau 12,5).
     */
    fun formatThousandInput(input: String): String {
        if (input.isEmpty()) return ""

        // Penanganan koma desimal
        if (input.contains(',')) {
            val parts = input.split(',', limit = 2)
            val intPart = parts[0].filter { it.isDigit() }
            val decPart = parts.getOrNull(1)?.filter { it.isDigit() } ?: ""
            val formattedInt = if (intPart.isNotEmpty()) {
                intPart.toLongOrNull()?.let { formatThousand(it) } ?: intPart
            } else {
                "0"
            }
            return if (input.endsWith(',')) "$formattedInt," else "$formattedInt,$decPart"
        }

        val dotCount = input.count { it == '.' }
        if (dotCount > 1) {
            val digits = input.filter { it.isDigit() }
            return digits.toLongOrNull()?.let { formatThousand(it) } ?: input
        }

        if (dotCount == 1) {
            val parts = input.split('.', limit = 2)
            val beforeDot = parts[0].filter { it.isDigit() }
            val afterDot = parts.getOrNull(1)?.filter { it.isDigit() } ?: ""

            if (input.endsWith('.')) {
                val formattedInt = if (beforeDot.isNotEmpty()) {
                    beforeDot.toLongOrNull()?.let { formatThousand(it) } ?: beforeDot
                } else "0"
                return "$formattedInt."
            }

            // Jika tepat 3 digit dan sebelum titik 1-3 digit, merupakan format ribuan (misal 1.000)
            if (afterDot.length == 3 && beforeDot.length in 1..3) {
                val digits = beforeDot + afterDot
                return digits.toLongOrNull()?.let { formatThousand(it) } ?: input
            }

            // Desimal standar seperti "12.5"
            val formattedInt = if (beforeDot.isNotEmpty()) {
                beforeDot.toLongOrNull()?.let { formatThousand(it) } ?: beforeDot
            } else "0"
            return "$formattedInt.$afterDot"
        }

        val digits = input.filter { it.isDigit() }
        if (digits.isEmpty()) return ""
        return digits.toLongOrNull()?.let { formatThousand(it) } ?: digits
    }

    /**
     * Parsing harga BBM (Rupiah selalu bilangan bulat).
     * Contoh: "10.000" -> BigDecimal("10000"), "12.500" -> BigDecimal("12500")
     */
    fun parseThousand(input: String): BigDecimal? {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return null
        val digits = trimmed.filter { it.isDigit() }
        if (digits.isEmpty()) return null
        return digits.toBigDecimalOrNull()
    }
}
