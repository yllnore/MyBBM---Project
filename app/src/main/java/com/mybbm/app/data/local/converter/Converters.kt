package com.mybbm.app.data.local.converter

import androidx.room.TypeConverter
import java.math.BigDecimal

class BigDecimalConverter {
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? {
        return value?.toPlainString()
    }

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? {
        return value?.let {
            try {
                BigDecimal(it)
            } catch (e: Exception) {
                BigDecimal.ZERO
            }
        }
    }
}
