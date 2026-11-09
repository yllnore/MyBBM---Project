package com.mybbm.app.data.local

import com.mybbm.app.R
import com.mybbm.app.data.local.entity.BbmKustomEntity
import com.mybbm.app.data.local.entity.KendaraanKustomEntity
import com.mybbm.app.data.local.entity.RiwayatEntity
import com.mybbm.app.data.remote.supabase.HargaBbmDto
import com.mybbm.app.data.remote.supabase.JenisBbmDto
import com.mybbm.app.data.remote.supabase.KendaraanDto
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
            rasioKonsumsi = 15.0,
            isDefault = false
        )

        assertEquals(id, kendaraan.id)
        assertEquals("HONDA JAZZ", kendaraan.nama)
        assertEquals("Mobil", kendaraan.tipe)
        assertEquals(15.0, kendaraan.rasioKonsumsi, 0.001)
        assertEquals(false, kendaraan.isDefault)
    }

    @Test
    fun testBbmKustomEntityCreation() {
        val id = UUID.randomUUID().toString()
        val bbm = BbmKustomEntity(
            id = id,
            nama = "PERTAMAX RACING",
            harga = 18500.0,
            iconResId = R.drawable.pertamax,
            warnaHex = "#7542EF",
            isDefault = false
        )

        assertEquals(id, bbm.id)
        assertEquals("PERTAMAX RACING", bbm.nama)
        assertEquals(18500.0, bbm.harga, 0.001)
        assertEquals(R.drawable.pertamax, bbm.iconResId)
        assertEquals("#7542EF", bbm.warnaHex)
        assertEquals(false, bbm.isDefault)
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
    fun testKendaraanDtoMapping() {
        val dto = KendaraanDto(
            idKendaraan = "veh-1",
            namaKendaraan = "Honda Beat 110 eSP",
            jenisKendaraan = "MOTOR",
            konsumsiKmPerLiter = 55.0,
            isDefault = true
        )
        val model = dto.toVehicleModel()
        assertEquals("veh-1", model.id)
        assertEquals("HONDA BEAT 110 ESP", model.name)
        assertEquals("Motor", model.type)
        assertEquals(55.0, model.fuelEfficiency.toDouble(), 0.001)
        assertTrue(model.isDefault)

        val entity = dto.toEntity()
        assertEquals("veh-1", entity.id)
        assertEquals("HONDA BEAT 110 ESP", entity.nama)
        assertEquals("Motor", entity.tipe)
        assertEquals(55.0, entity.rasioKonsumsi, 0.001)
        assertTrue(entity.isDefault)
    }

    @Test
    fun testHargaBbmDtoMapping() {
        val jenisBbm = JenisBbmDto(
            idBbm = "bbm-1",
            namaBbm = "Pertalite (RON 90)",
            jenisBahanBakar = "BENSIN"
        )
        val dto = HargaBbmDto(
            idHarga = "hrg-1",
            idBbm = "bbm-1",
            wilayah = "KALIMANTAN_TIMUR",
            hargaPerLiter = 10000.0,
            jenisBbm = jenisBbm
        )
        val model = dto.toFuelModel()
        assertEquals("hrg-1", model.id)
        assertEquals("PERTALITE (RON 90)", model.name)
        assertEquals(10000.0, model.pricePerLiter.toDouble(), 0.001)
        assertEquals("#10B981", model.colorHex)
        assertTrue(model.isDefault)

        val entity = dto.toEntity()
        assertEquals("hrg-1", entity.id)
        assertEquals("PERTALITE (RON 90)", entity.nama)
        assertEquals(10000.0, entity.harga, 0.001)
        assertTrue(entity.isDefault)
    }
}
