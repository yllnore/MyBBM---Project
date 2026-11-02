package com.mybbm.app.data.local

import com.mybbm.app.R
import com.mybbm.app.data.local.entity.BbmKustomEntity
import com.mybbm.app.data.local.entity.KendaraanKustomEntity
import com.mybbm.app.data.local.entity.RiwayatEntity
import com.mybbm.app.data.remote.supabase.DefaultBbmDto
import com.mybbm.app.data.remote.supabase.DefaultKendaraanDto
import com.mybbm.app.data.remote.supabase.SupabaseDefaultRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class RoomDatabaseTest {

    @Test
    fun testKendaraanKustomEntityCreation() {
        val id = UUID.randomUUID().toString()
        val kendaraan = KendaraanKustomEntity(
            id = id,
            nama = "HONDA JAZZ",
            tipe = "Mobil",
            rasioKonsumsi = 15.0
        )

        assertEquals(id, kendaraan.id)
        assertEquals("HONDA JAZZ", kendaraan.nama)
        assertEquals("Mobil", kendaraan.tipe)
        assertEquals(15.0, kendaraan.rasioKonsumsi, 0.001)
    }

    @Test
    fun testBbmKustomEntityCreation() {
        val id = UUID.randomUUID().toString()
        val bbm = BbmKustomEntity(
            id = id,
            nama = "PERTAMAX RACING",
            harga = 18500.0,
            iconResId = R.drawable.pertamax,
            warnaHex = "#7542EF"
        )

        assertEquals(id, bbm.id)
        assertEquals("PERTAMAX RACING", bbm.nama)
        assertEquals(18500.0, bbm.harga, 0.001)
        assertEquals(R.drawable.pertamax, bbm.iconResId)
        assertEquals("#7542EF", bbm.warnaHex)
    }

    @Test
    fun testRiwayatEntityCreation() {
        val id = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        val riwayat = RiwayatEntity(
            id = id,
            lokasiAsal = "Samarinda",
            lokasiTujuan = "Balikpapan",
            jarakKm = 115.5,
            totalBiaya = 96250.0,
            timestamp = timestamp,
            totalLiter = 7.7,
            namaKendaraan = "AVANZA",
            namaBbm = "PERTAMAX"
        )

        assertEquals(id, riwayat.id)
        assertEquals("Samarinda", riwayat.lokasiAsal)
        assertEquals("Balikpapan", riwayat.lokasiTujuan)
        assertEquals(115.5, riwayat.jarakKm, 0.001)
        assertEquals(96250.0, riwayat.totalBiaya, 0.001)
        assertEquals(timestamp, riwayat.timestamp)
        assertEquals("AVANZA", riwayat.namaKendaraan)
        assertEquals("PERTAMAX", riwayat.namaBbm)
    }

    @Test
    fun testSupabaseDefaultDatasetsFallback() {
        val defaultFuels = SupabaseDefaultRepository.FACTORY_DEFAULT_FUELS
        assertEquals(4, defaultFuels.size)
        val fuelNames = defaultFuels.map { it.name }
        assertTrue(fuelNames.contains("PERTALITE"))
        assertTrue(fuelNames.contains("PERTAMAX"))
        assertTrue(fuelNames.contains("PERTAMAX TURBO"))
        assertTrue(fuelNames.contains("SOLAR"))

        val defaultVehicles = SupabaseDefaultRepository.FACTORY_DEFAULT_VEHICLES
        assertTrue(defaultVehicles.isNotEmpty())
        val vehicleNames = defaultVehicles.map { it.name }
        assertTrue(vehicleNames.contains("AVANZA"))
        assertTrue(vehicleNames.contains("HONDA BEAT"))
    }

    @Test
    fun testDefaultBbmDtoMapping() {
        val dto = DefaultBbmDto(
            id = "sup-1",
            nama = "Pertalite",
            harga = 10000.0,
            warnaHex = "#10B981"
        )
        val model = dto.toFuelModel()
        assertEquals("sup-1", model.id)
        assertEquals("PERTALITE", model.name)
        assertEquals(10000.0, model.pricePerLiter.toDouble(), 0.001)
        assertEquals("#10B981", model.colorHex)
    }

    @Test
    fun testDefaultKendaraanDtoMapping() {
        val dto = DefaultKendaraanDto(
            idKendaraan = "veh-1",
            namaKendaraan = "Honda Beat",
            jenisKendaraan = "MOTOR",
            konsumsiKmPerLiter = 55.0
        )
        val model = dto.toVehicleModel()
        assertEquals("veh-1", model.id)
        assertEquals("HONDA BEAT", model.name)
        assertEquals("Motor", model.type)
        assertEquals(55.0, model.fuelEfficiency.toDouble(), 0.001)
        assertTrue(model.isDefault)
    }
}
