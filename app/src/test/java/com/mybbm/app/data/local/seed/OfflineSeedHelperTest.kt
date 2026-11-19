package com.mybbm.app.data.local.seed

import com.mybbm.app.R
import com.mybbm.app.util.FuelIconResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class OfflineSeedHelperTest {

    @Test
    fun getDefaultVehicles_returns10ValidPopularIndonesianVehicles() {
        val vehicles = OfflineSeedHelper.getDefaultVehicles()

        assertEquals(10, vehicles.size)
        assertTrue(vehicles.all { it.id.isNotBlank() })
        assertTrue(vehicles.all { it.nama.isNotBlank() })
        assertTrue(vehicles.all { it.rasioKonsumsi > 0.0 })
        assertTrue(vehicles.all { it.tipe == "Motor" || it.tipe == "Mobil" })
        assertTrue(vehicles.all { it.isDefault })
        assertEquals(vehicles.size, vehicles.map { it.id }.distinct().size)

        val motorCount = vehicles.count { it.tipe == "Motor" }
        val mobilCount = vehicles.count { it.tipe == "Mobil" }
        assertTrue("Harus ada kendaraan Motor", motorCount > 0)
        assertTrue("Harus ada kendaraan Mobil", mobilCount > 0)
    }

    @Test
    fun getDefaultFuels_withoutContext_returnsValidFallbackFuels() {
        val fuels = OfflineSeedHelper.getDefaultFuels(context = null)

        assertEquals(6, fuels.size)
        assertTrue(fuels.all { it.id.isNotBlank() })
        assertTrue(fuels.all { it.nama.isNotBlank() })
        assertTrue(fuels.all { it.harga > 0.0 })
        assertTrue(fuels.all { it.isDefault })
        assertTrue(fuels.all { it.warnaHex != null && it.warnaHex.startsWith("#") })

        fuels.forEach { fuel ->
            assertEquals(
                "Ikon BBM ${fuel.nama} harus sesuai FuelIconResolver",
                FuelIconResolver.resolveIcon(fuel.nama),
                fuel.iconResId
            )
        }
    }

    @Test
    fun parseFuelsFromJson_parsesRealSeedReferenceAssetCorrectly() {
        val assetFile = listOf(
            File("src/main/assets/seed_reference.json"),
            File("app/src/main/assets/seed_reference.json")
        ).firstOrNull { it.exists() }
        assertNotNull("File assets/seed_reference.json harus ada", assetFile)

        val jsonText = assetFile!!.readText()
        val fuels = OfflineSeedHelper.parseFuelsFromJson(jsonText)

        assertEquals(6, fuels.size)
        val byName = fuels.associateBy { it.nama }

        assertEquals(10000.0, byName["PERTALITE"]?.harga ?: 0.0, 0.01)
        assertEquals(R.drawable.pertalite, byName["PERTALITE"]?.iconResId)

        assertEquals(12500.0, byName["PERTAMAX"]?.harga ?: 0.0, 0.01)
        assertEquals(R.drawable.pertamax, byName["PERTAMAX"]?.iconResId)

        assertEquals(14000.0, byName["PERTAMAX TURBO"]?.harga ?: 0.0, 0.01)
        assertEquals(R.drawable.pertamax_turbo, byName["PERTAMAX TURBO"]?.iconResId)

        assertEquals(13700.0, byName["DEXLITE"]?.harga ?: 0.0, 0.01)
        assertEquals(R.drawable.solar, byName["DEXLITE"]?.iconResId)

        assertEquals(14950.0, byName["PERTAMINA DEX"]?.harga ?: 0.0, 0.01)
        assertEquals(R.drawable.solar, byName["PERTAMINA DEX"]?.iconResId)

        assertEquals(6800.0, byName["BIOSOLAR"]?.harga ?: 0.0, 0.01)
        assertEquals(R.drawable.solar, byName["BIOSOLAR"]?.iconResId)
    }

    @Test
    fun parseFuelsFromJson_handlesInvalidAndEmptyJsonGracefully() {
        assertTrue(OfflineSeedHelper.parseFuelsFromJson("").isEmpty())
        assertTrue(OfflineSeedHelper.parseFuelsFromJson("{not valid json").isEmpty())
        assertTrue(OfflineSeedHelper.parseFuelsFromJson("""{"jenis_bbm":[],"harga_bbm":[]}""").isEmpty())
    }

    @Test
    fun parseFuelsFromJson_supportsNumericPriceAndSkipsInactiveOrNonKaltim() {
        val sampleJson = """
            {
              "jenis_bbm": [
                {"id_bbm": "1", "nama_bbm": "Pertalite", "is_active": true},
                {"id_bbm": "2", "nama_bbm": "Premium", "is_active": false}
              ],
              "harga_bbm": [
                {"id_harga": "h1", "id_bbm": "1", "wilayah": "KALIMANTAN_TIMUR", "harga_per_liter": 10000},
                {"id_harga": "h2", "id_bbm": "1", "wilayah": "DKI_JAKARTA", "harga_per_liter": 9500},
                {"id_harga": "h3", "id_bbm": "2", "wilayah": "KALIMANTAN_TIMUR", "harga_per_liter": 6450}
              ]
            }
        """.trimIndent()

        val fuels = OfflineSeedHelper.parseFuelsFromJson(sampleJson)
        assertEquals(1, fuels.size)
        assertEquals("PERTALITE", fuels.first().nama)
        assertEquals(10000.0, fuels.first().harga, 0.01)
        assertFalse(fuels.any { it.nama == "PREMIUM" })
    }
}
