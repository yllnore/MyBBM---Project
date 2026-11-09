package com.mybbm.app.data.remote.supabase

import android.util.Log
import com.mybbm.app.R
import com.mybbm.app.data.local.dao.BbmKustomDao
import com.mybbm.app.data.local.dao.KendaraanKustomDao
import com.mybbm.app.data.local.entity.BbmKustomEntity
import com.mybbm.app.data.local.entity.KendaraanKustomEntity
import com.mybbm.app.domain.model.FuelModel
import com.mybbm.app.domain.model.VehicleModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.math.BigDecimal

/**
 * DTO untuk tabel Supabase `kendaraan`.
 * Menggunakan @SerialName yang persis dengan nama kolom di Supabase:
 * id_kendaraan, nama_kendaraan, jenis_kendaraan, konsumsi_km_per_liter, is_default.
 */
@Serializable
data class KendaraanDto(
    @SerialName("id_kendaraan")
    val idKendaraan: String,
    @SerialName("nama_kendaraan")
    val namaKendaraan: String,
    @SerialName("jenis_kendaraan")
    val jenisKendaraan: String,
    @SerialName("konsumsi_km_per_liter")
    val konsumsiKmPerLiter: Double,
    @SerialName("is_default")
    val isDefault: Boolean = true,
    @SerialName("user_id")
    val userId: String? = null,
    @SerialName("local_device_id")
    val localDeviceId: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null,
    @SerialName("deleted_at")
    val deletedAt: String? = null
) {
    fun toVehicleModel(): VehicleModel {
        val displayName = namaKendaraan.uppercase().trim()
        val finalType = if (jenisKendaraan.contains("motor", ignoreCase = true)) "Motor" else "Mobil"
        return VehicleModel(
            id = idKendaraan,
            name = displayName,
            type = finalType,
            fuelEfficiency = BigDecimal.valueOf(konsumsiKmPerLiter),
            isDefault = isDefault
        )
    }

    fun toEntity(): KendaraanKustomEntity {
        val finalType = if (jenisKendaraan.contains("motor", ignoreCase = true)) "Motor" else "Mobil"
        return KendaraanKustomEntity(
            id = idKendaraan,
            nama = namaKendaraan.uppercase().trim(),
            tipe = finalType,
            rasioKonsumsi = konsumsiKmPerLiter,
            isDefault = true,
            createdAt = 0L
        )
    }
}

/**
 * DTO untuk tabel Supabase `jenis_bbm`.
 * Kolom: id_bbm, nama_bbm, jenis_bahan_bakar.
 */
@Serializable
data class JenisBbmDto(
    @SerialName("id_bbm")
    val idBbm: String,
    @SerialName("nama_bbm")
    val namaBbm: String,
    @SerialName("jenis_bahan_bakar")
    val jenisBahanBakar: String = "BENSIN",
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
)

/**
 * DTO untuk tabel Supabase `harga_bbm`.
 * Kolom: id_harga, id_bbm, wilayah, harga_per_liter, dengan relasi jenis_bbm.
 */
@Serializable
data class HargaBbmDto(
    @SerialName("id_harga")
    val idHarga: String,
    @SerialName("id_bbm")
    val idBbm: String,
    @SerialName("wilayah")
    val wilayah: String,
    @SerialName("harga_per_liter")
    val hargaPerLiter: Double,
    @SerialName("jenis_bbm")
    val jenisBbm: JenisBbmDto? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("updated_at")
    val updatedAt: String? = null
) {
    fun toFuelModel(): FuelModel {
        val bbmName = jenisBbm?.namaBbm ?: "BBM"
        val displayName = bbmName.uppercase().trim()
        val finalPrice = hargaPerLiter
        val iconRes = when {
            displayName.contains("PERTALITE") -> R.drawable.pertalite
            displayName.contains("TURBO") -> R.drawable.pertamax_turbo
            displayName.contains("PERTAMAX") -> R.drawable.pertamax
            displayName.contains("SOLAR") || displayName.contains("DEX") -> R.drawable.solar
            else -> R.drawable.pertamax
        }
        val finalColor = when {
            displayName.contains("PERTALITE") -> "#10B981"
            displayName.contains("TURBO") -> "#EF4444"
            displayName.contains("PERTAMAX") -> "#7542EF"
            displayName.contains("BIOSOLAR") || displayName.contains("SOLAR") -> "#4B5563"
            displayName.contains("DEXLITE") -> "#059669"
            displayName.contains("PERTAMINA DEX") -> "#2563EB"
            else -> "#7542EF"
        }
        return FuelModel(
            id = idHarga,
            name = displayName,
            pricePerLiter = BigDecimal.valueOf(finalPrice),
            iconResId = iconRes,
            colorHex = finalColor,
            isDefault = true
        )
    }

    fun toEntity(): BbmKustomEntity {
        val fuelModel = toFuelModel()
        return BbmKustomEntity(
            id = idHarga,
            nama = fuelModel.name,
            harga = hargaPerLiter,
            iconResId = fuelModel.iconResId,
            warnaHex = fuelModel.colorHex,
            isDefault = true,
            createdAt = 0L
        )
    }
}

// Aliases untuk backward compatibility
typealias DefaultKendaraanDto = KendaraanDto
typealias DefaultBbmDto = HargaBbmDto

/**
 * Repository untuk sinkronisasi dataset riil dari Supabase ke Room Database.
 * Mengambil data dari tabel `kendaraan` dan `harga_bbm` (difilter wilayah = KALIMANTAN_TIMUR,
 * di-join dengan `jenis_bbm`), kemudian menyimpannya ke cache Room lokal.
 */
class SupabaseDefaultRepository(
    private val supabaseClient: SupabaseClient = SupabaseClientInstance.client
) {
    companion object {
        private const val TAG = "SupabaseDefaultRepo"
    }

    /**
     * Mengambil daftar kendaraan riil dari tabel `kendaraan` Supabase.
     */
    suspend fun fetchKendaraanFromSupabase(): List<KendaraanDto> = withContext(Dispatchers.IO) {
        try {
            val response = supabaseClient.from("kendaraan")
                .select()
                .decodeList<KendaraanDto>()
            Log.d(TAG, "Berhasil memuat ${response.size} kendaraan dari Supabase")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengambil kendaraan dari Supabase: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Mengambil daftar harga BBM riil dari tabel `harga_bbm` Supabase
     * difilter berdasarkan wilayah KALIMANTAN_TIMUR dan di-join dengan tabel `jenis_bbm`.
     */
    suspend fun fetchBbmFromSupabase(): List<HargaBbmDto> = withContext(Dispatchers.IO) {
        try {
            val response = supabaseClient.from("harga_bbm")
                .select(Columns.raw("id_harga,id_bbm,wilayah,harga_per_liter,jenis_bbm(id_bbm,nama_bbm,jenis_bahan_bakar)")) {
                    filter {
                        eq("wilayah", "KALIMANTAN_TIMUR")
                    }
                }
                .decodeList<HargaBbmDto>()
            Log.d(TAG, "Berhasil memuat ${response.size} harga_bbm dari Supabase")
            response
        } catch (e: Exception) {
            Log.e(TAG, "Gagal mengambil harga_bbm dari Supabase: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Menarik data kendaraan dari Supabase dan menyimpannya secara otomatis ke Room Database.
     */
    suspend fun syncKendaraan(dao: KendaraanKustomDao): List<VehicleModel> = withContext(Dispatchers.IO) {
        val dtos = fetchKendaraanFromSupabase()
        if (dtos.isNotEmpty()) {
            val entities = dtos.map { it.toEntity() }
            dao.syncDefaultKendaraan(entities)
            Log.d(TAG, "Berhasil sinkronisasi ${entities.size} kendaraan ke Room Database")
            dtos.map { it.toVehicleModel() }
        } else {
            emptyList()
        }
    }

    /**
     * Menarik data harga BBM dari Supabase dan menyimpannya secara otomatis ke Room Database.
     */
    suspend fun syncBbm(dao: BbmKustomDao): List<FuelModel> = withContext(Dispatchers.IO) {
        val dtos = fetchBbmFromSupabase()
        if (dtos.isNotEmpty()) {
            val entities = dtos.map { it.toEntity() }
            dao.syncDefaultBbm(entities)
            Log.d(TAG, "Berhasil sinkronisasi ${entities.size} harga BBM ke Room Database")
            dtos.map { it.toFuelModel() }
        } else {
            emptyList()
        }
    }

    // Metode helper kompatibilitas
    suspend fun getDefaultKendaraan(): List<VehicleModel> = withContext(Dispatchers.IO) {
        fetchKendaraanFromSupabase().map { it.toVehicleModel() }
    }

    suspend fun getDefaultBbm(): List<FuelModel> = withContext(Dispatchers.IO) {
        fetchBbmFromSupabase().map { it.toFuelModel() }
    }
}
