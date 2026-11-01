package com.mybbm.app.data.remote.supabase

import android.util.Log
import com.mybbm.app.R
import com.mybbm.app.domain.model.FuelModel
import com.mybbm.app.domain.model.VehicleModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.UUID

@Serializable
data class DefaultBbmDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("nama")
    val nama: String? = null,
    @SerialName("nama_bbm")
    val namaBbm: String? = null,
    @SerialName("harga")
    val harga: Double? = null,
    @SerialName("harga_per_liter")
    val hargaPerLiter: Double? = null,
    @SerialName("warna_hex")
    val warnaHex: String? = null
) {
    fun toFuelModel(): FuelModel {
        val displayName = (nama ?: namaBbm ?: "BBM").uppercase().trim()
        val finalPrice = harga ?: hargaPerLiter ?: 10000.0
        val iconRes = when (displayName) {
            "PERTALITE" -> R.drawable.pertalite
            "PERTAMAX" -> R.drawable.pertamax
            "PERTAMAX TURBO" -> R.drawable.pertamax_turbo
            "SOLAR", "BIOSOLAR", "DEXLITE", "PERTAMINA DEX" -> R.drawable.solar
            else -> R.drawable.pertamax
        }
        val finalColor = warnaHex ?: when (displayName) {
            "PERTALITE" -> "#10B981"
            "PERTAMAX" -> "#7542EF"
            "PERTAMAX TURBO" -> "#EF4444"
            "SOLAR", "BIOSOLAR" -> "#4B5563"
            "DEXLITE" -> "#059669"
            "PERTAMINA DEX" -> "#2563EB"
            else -> "#7542EF"
        }
        return FuelModel(
            id = id ?: UUID.randomUUID().toString(),
            name = displayName,
            pricePerLiter = BigDecimal.valueOf(finalPrice),
            iconResId = iconRes,
            colorHex = finalColor
        )
    }
}

@Serializable
data class DefaultKendaraanDto(
    @SerialName("id")
    val id: String? = null,
    @SerialName("id_kendaraan")
    val idKendaraan: String? = null,
    @SerialName("nama")
    val nama: String? = null,
    @SerialName("nama_kendaraan")
    val namaKendaraan: String? = null,
    @SerialName("tipe")
    val tipe: String? = null,
    @SerialName("jenis_kendaraan")
    val jenisKendaraan: String? = null,
    @SerialName("rasio_konsumsi")
    val rasioKonsumsi: Double? = null,
    @SerialName("konsumsi_km_per_liter")
    val konsumsiKmPerLiter: Double? = null
) {
    fun toVehicleModel(): VehicleModel {
        val displayName = (nama ?: namaKendaraan ?: "KENDARAAN").uppercase().trim()
        val rawType = (tipe ?: jenisKendaraan ?: "Mobil").trim()
        val finalType = if (rawType.equals("Motor", ignoreCase = true)) "Motor" else "Mobil"
        val finalRatio = rasioKonsumsi ?: konsumsiKmPerLiter ?: 15.0
        return VehicleModel(
            id = id ?: idKendaraan ?: UUID.randomUUID().toString(),
            name = displayName,
            type = finalType,
            fuelEfficiency = BigDecimal.valueOf(finalRatio),
            isDefault = true
        )
    }
}

/**
 * Repository Read-Only untuk mengambil dataset default (bawaan pabrik)
 * dari tabel Supabase `default_bbm` dan `default_kendaraan`.
 * Memiliki mekanisme graceful fallback ke data pabrik lokal jika offline
 * atau jika tabel belum dibuat di Supabase.
 */
class SupabaseDefaultRepository(
    private val supabaseClient: SupabaseClient = SupabaseClientInstance.client
) {
    companion object {
        private const val TAG = "SupabaseDefaultRepo"

        val FACTORY_DEFAULT_FUELS: List<FuelModel> = listOf(
            FuelModel(
                id = "def-fuel-pertalite",
                name = "PERTALITE",
                pricePerLiter = BigDecimal("10000"),
                iconResId = R.drawable.pertalite,
                colorHex = "#10B981"
            ),
            FuelModel(
                id = "def-fuel-pertamax",
                name = "PERTAMAX",
                pricePerLiter = BigDecimal("12500"),
                iconResId = R.drawable.pertamax,
                colorHex = "#7542EF"
            ),
            FuelModel(
                id = "def-fuel-turbo",
                name = "PERTAMAX TURBO",
                pricePerLiter = BigDecimal("13800"),
                iconResId = R.drawable.pertamax_turbo,
                colorHex = "#EF4444"
            ),
            FuelModel(
                id = "def-fuel-solar",
                name = "SOLAR",
                pricePerLiter = BigDecimal("6800"),
                iconResId = R.drawable.solar,
                colorHex = "#4B5563"
            )
        )

        val FACTORY_DEFAULT_VEHICLES: List<VehicleModel> = listOf(
            VehicleModel("def-veh-1", "AVANZA", "Mobil", BigDecimal("12.5"), isDefault = true),
            VehicleModel("def-veh-2", "TOYOTA", "Mobil", BigDecimal("14.0"), isDefault = true),
            VehicleModel("def-veh-3", "HONDA", "Mobil", BigDecimal("15.0"), isDefault = true),
            VehicleModel("def-veh-4", "HONDA BEAT", "Motor", BigDecimal("55.0"), isDefault = true),
            VehicleModel("def-veh-5", "HONDA VARIO", "Motor", BigDecimal("48.0"), isDefault = true),
            VehicleModel("def-veh-6", "NMAX", "Motor", BigDecimal("40.0"), isDefault = true),
            VehicleModel("def-veh-7", "FAZZIO", "Motor", BigDecimal("50.0"), isDefault = true)
        )
    }

    /**
     * Membaca dataset BBM default dari tabel Supabase `default_bbm`.
     */
    suspend fun getDefaultBbm(): List<FuelModel> = withContext(Dispatchers.IO) {
        try {
            val response = supabaseClient.postgrest["default_bbm"]
                .select()
                .decodeList<DefaultBbmDto>()

            if (response.isNotEmpty()) {
                Log.d(TAG, "Berhasil memuat ${response.size} default BBM dari Supabase")
                return@withContext response.map { it.toFuelModel() }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Gagal mengambil default_bbm dari Supabase: ${e.message}. Menggunakan dataset bawaan.")
        }
        return@withContext FACTORY_DEFAULT_FUELS
    }

    /**
     * Membaca dataset Kendaraan default dari tabel Supabase `default_kendaraan`.
     */
    suspend fun getDefaultKendaraan(): List<VehicleModel> = withContext(Dispatchers.IO) {
        try {
            val response = supabaseClient.postgrest["default_kendaraan"]
                .select()
                .decodeList<DefaultKendaraanDto>()

            if (response.isNotEmpty()) {
                Log.d(TAG, "Berhasil memuat ${response.size} default kendaraan dari Supabase")
                return@withContext response.map { it.toVehicleModel() }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Gagal mengambil default_kendaraan dari Supabase: ${e.message}. Menggunakan dataset bawaan.")
        }
        return@withContext FACTORY_DEFAULT_VEHICLES
    }
}
