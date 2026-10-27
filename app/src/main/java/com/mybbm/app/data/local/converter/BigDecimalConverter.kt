package com.mybbm.app.data.local.converter

import androidx.room.TypeConverter
import java.math.BigDecimal

/**
 * TypeConverter Room untuk mengubah [BigDecimal] menjadi [String] dan sebaliknya.
 *
 * **Mengapa String, bukan Double?**
 * Room tidak mendukung [BigDecimal] secara native. Jika menggunakan [Double], terjadi
 * galat presisi floating-point yang menghasilkan perhitungan biaya BBM tidak akurat
 * (misal: 10000.00 dapat menjadi 9999.9999999...). Dengan menyimpan sebagai [String]
 * representasi desimal yang tepat selalu terjaga.
 *
 * Pendaftaran converter ini di [com.mybbm.app.data.local.database.AppDatabase]
 * membuat Room dapat membaca/menulis kolom bertipe [String] di database dan
 * mengekspos [BigDecimal] kepada lapisan domain secara transparan.
 *
 * Seluruh entitas yang menyimpan nilai desimal menggunakan [String] di kolom-nya:
 * - [com.mybbm.app.data.local.entity.HargaBbmEntity.hargaPerLiter]
 * - [com.mybbm.app.data.local.entity.KendaraanEntity.konsumsiKmPerLiter]
 * - [com.mybbm.app.data.local.entity.PerjalananEntity.jarakKm]
 * - [com.mybbm.app.data.local.entity.HasilPerhitunganEntity.literDibutuhkan]
 * - [com.mybbm.app.data.local.entity.HasilPerhitunganEntity.totalBiaya]
 *
 * Meskipun kolom-kolom tersebut sudah bertipe [String] di entitas (bukan [BigDecimal]),
 * converter ini tetap didaftarkan agar lapisan atas (misalnya query yang mengembalikan
 * [BigDecimal] langsung) dapat memanfaatkannya di masa mendatang tanpa perubahan skema.
 */
class BigDecimalConverter {

    /**
     * Mengubah [BigDecimal] menjadi [String] untuk disimpan di Room.
     *
     * @param value Nilai [BigDecimal] yang akan disimpan. Null jika kolom nullable.
     * @return Representasi String tanpa kehilangan presisi, atau null.
     */
    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? = value?.toPlainString()

    /**
     * Mengubah [String] dari database menjadi [BigDecimal].
     *
     * @param value String representasi desimal, misalnya "10000.00".
     * @return [BigDecimal] dengan presisi penuh, atau null.
     * @throws NumberFormatException jika [value] bukan representasi angka yang valid.
     */
    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? = value?.let { BigDecimal(it) }
}
