package com.mybbm.app.data.local.seed

import android.content.Context
import android.util.Log
import com.mybbm.app.data.local.entity.BbmKustomEntity
import com.mybbm.app.data.local.entity.KendaraanKustomEntity
import com.mybbm.app.util.FuelIconResolver
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

@Serializable
private data class SeedReferencePayload(
    @SerialName("jenis_bbm")
    val jenisBbm: List<SeedJenisBbmDto> = emptyList(),
    @SerialName("harga_bbm")
    val hargaBbm: List<SeedHargaBbmDto> = emptyList()
)

@Serializable
private data class SeedJenisBbmDto(
    @SerialName("id_bbm")
    val idBbm: String,
    @SerialName("nama_bbm")
    val namaBbm: String,
    @SerialName("jenis_bahan_bakar")
    val jenisBahanBakar: String = "BENSIN",
    @SerialName("is_active")
    val isActive: Boolean = true
)

@Serializable
private data class SeedHargaBbmDto(
    @SerialName("id_harga")
    val idHarga: String,
    @SerialName("id_bbm")
    val idBbm: String,
    @SerialName("wilayah")
    val wilayah: String = "",
    @SerialName("harga_per_liter")
    val hargaPerLiter: JsonPrimitive
)

/**
 * Penyedia data seed referensi lokal (Offline-First) untuk kendaraan populer Indonesia
 * dan harga resmi BBM wilayah Kalimantan Timur (dibaca dari `assets/seed_reference.json`).
 *
 * Memastikan aplikasi langsung memiliki daftar referensi valid saat pertama kali dipasang
 * meskipun perangkat sedang offline atau backend Supabase tidak tersedia.
 */
object OfflineSeedHelper {

    private const val TAG = "OfflineSeedHelper"
    private const val SEED_ASSET_FILE = "seed_reference.json"
    private const val DEFAULT_REGION = "KALIMANTAN_TIMUR"

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    /**
     * Daftar 10 kendaraan populer Indonesia sebagai data referensi bawaan offline.
     */
    fun getDefaultVehicles(): List<KendaraanKustomEntity> = listOf(
        KendaraanKustomEntity(
            id = "seed-veh-01",
            nama = "HONDA BEAT 110 ESP",
            tipe = "Motor",
            rasioKonsumsi = 60.0,
            isDefault = true,
            createdAt = 0L
        ),
        KendaraanKustomEntity(
            id = "seed-veh-02",
            nama = "HONDA SCOOPY 110",
            tipe = "Motor",
            rasioKonsumsi = 59.0,
            isDefault = true,
            createdAt = 0L
        ),
        KendaraanKustomEntity(
            id = "seed-veh-03",
            nama = "YAMAHA FAZZIO",
            tipe = "Motor",
            rasioKonsumsi = 55.0,
            isDefault = true,
            createdAt = 0L
        ),
        KendaraanKustomEntity(
            id = "seed-veh-04",
            nama = "HONDA VARIO 125",
            tipe = "Motor",
            rasioKonsumsi = 51.7,
            isDefault = true,
            createdAt = 0L
        ),
        KendaraanKustomEntity(
            id = "seed-veh-05",
            nama = "YAMAHA NMAX 155",
            tipe = "Motor",
            rasioKonsumsi = 40.0,
            isDefault = true,
            createdAt = 0L
        ),
        KendaraanKustomEntity(
            id = "seed-veh-06",
            nama = "HONDA PCX 160",
            tipe = "Motor",
            rasioKonsumsi = 45.0,
            isDefault = true,
            createdAt = 0L
        ),
        KendaraanKustomEntity(
            id = "seed-veh-07",
            nama = "HONDA BRIO SATYA 1.2",
            tipe = "Mobil",
            rasioKonsumsi = 18.0,
            isDefault = true,
            createdAt = 0L
        ),
        KendaraanKustomEntity(
            id = "seed-veh-08",
            nama = "TOYOTA AVANZA 1.3",
            tipe = "Mobil",
            rasioKonsumsi = 14.0,
            isDefault = true,
            createdAt = 0L
        ),
        KendaraanKustomEntity(
            id = "seed-veh-09",
            nama = "MITSUBISHI XPANDER 1.5",
            tipe = "Mobil",
            rasioKonsumsi = 13.5,
            isDefault = true,
            createdAt = 0L
        ),
        KendaraanKustomEntity(
            id = "seed-veh-10",
            nama = "TOYOTA KIJANG INNOVA REBORN",
            tipe = "Mobil",
            rasioKonsumsi = 11.5,
            isDefault = true,
            createdAt = 0L
        )
    )

    /**
     * Memuat daftar jenis & harga BBM default wilayah KALIMANTAN_TIMUR dari `assets/seed_reference.json`.
     * Jika [context] null atau pembacaan aset gagal, otomatis kembali ke snapshot bawaan yang identik.
     */
    fun getDefaultFuels(context: Context? = null): List<BbmKustomEntity> {
        if (context != null) {
            try {
                val jsonText = context.assets.open(SEED_ASSET_FILE).bufferedReader().use { it.readText() }
                val parsed = parseFuelsFromJson(jsonText)
                if (parsed.isNotEmpty()) {
                    return parsed
                }
            } catch (e: Exception) {
                Log.w(TAG, "Gagal membaca $SEED_ASSET_FILE, menggunakan fallback statis: ${e.message}")
            }
        }
        return fallbackDefaultFuels()
    }

    /**
     * Mem-parsing JSON berformat `seed_reference.json` menjadi daftar [BbmKustomEntity]
     * untuk wilayah [DEFAULT_REGION] yang aktif dan memiliki harga > 0.
     */
    fun parseFuelsFromJson(jsonText: String): List<BbmKustomEntity> {
        val sanitized = jsonText.trimStart('\uFEFF').trim()
        if (sanitized.isBlank()) return emptyList()
        return try {
            val payload = json.decodeFromString<SeedReferencePayload>(sanitized)
            val jenisById = payload.jenisBbm
                .filter { it.isActive && it.namaBbm.isNotBlank() }
                .associateBy { it.idBbm }

            payload.hargaBbm
                .filter { it.wilayah.equals(DEFAULT_REGION, ignoreCase = true) }
                .mapNotNull { hargaDto ->
                    val jenis = jenisById[hargaDto.idBbm] ?: return@mapNotNull null
                    val price = hargaDto.hargaPerLiter.content.trim().toDoubleOrNull()
                        ?: return@mapNotNull null
                    if (price <= 0.0) return@mapNotNull null

                    val displayName = jenis.namaBbm.uppercase().trim()
                    BbmKustomEntity(
                        id = hargaDto.idHarga,
                        nama = displayName,
                        harga = price,
                        iconResId = FuelIconResolver.resolveIcon(displayName),
                        warnaHex = resolveColorHex(displayName),
                        isDefault = true,
                        createdAt = 0L
                    )
                }
        } catch (e: Exception) {
            Log.w(TAG, "Gagal parsing JSON seed BBM: ${e.message}")
            emptyList()
        }
    }

    private fun resolveColorHex(displayName: String): String = when {
        displayName.contains("PERTALITE") -> "#10B981"
        displayName.contains("TURBO") -> "#EF4444"
        displayName.contains("PERTAMAX") -> "#7542EF"
        displayName.contains("BIOSOLAR") || displayName.contains("SOLAR") -> "#4B5563"
        displayName.contains("DEXLITE") -> "#059669"
        displayName.contains("PERTAMINA DEX") -> "#2563EB"
        else -> "#7542EF"
    }

    private fun fallbackDefaultFuels(): List<BbmKustomEntity> = listOf(
        createFuelEntity("b1c2d3e4-0002-0002-0002-000000000001", "PERTALITE", 10000.0),
        createFuelEntity("b1c2d3e4-0002-0002-0002-000000000002", "PERTAMAX", 12500.0),
        createFuelEntity("b1c2d3e4-0002-0002-0002-000000000003", "PERTAMAX TURBO", 14000.0),
        createFuelEntity("b1c2d3e4-0002-0002-0002-000000000004", "DEXLITE", 13700.0),
        createFuelEntity("b1c2d3e4-0002-0002-0002-000000000005", "PERTAMINA DEX", 14950.0),
        createFuelEntity("b1c2d3e4-0002-0002-0002-000000000006", "BIOSOLAR", 6800.0)
    )

    private fun createFuelEntity(id: String, displayName: String, price: Double): BbmKustomEntity =
        BbmKustomEntity(
            id = id,
            nama = displayName,
            harga = price,
            iconResId = FuelIconResolver.resolveIcon(displayName),
            warnaHex = resolveColorHex(displayName),
            isDefault = true,
            createdAt = 0L
        )
}
