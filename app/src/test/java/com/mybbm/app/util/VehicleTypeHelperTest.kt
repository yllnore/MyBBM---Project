package com.mybbm.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VehicleTypeHelperTest {

    @Test
    fun normalizeType_mapsPresetsCaseInsensitively() {
        assertEquals("Mobil", VehicleTypeHelper.normalizeType("mobil"))
        assertEquals("Motor", VehicleTypeHelper.normalizeType("MOTOR"))
        assertEquals("Pickup", VehicleTypeHelper.normalizeType("pickup"))
        assertEquals("Pickup", VehicleTypeHelper.normalizeType("Pick Up"))
        assertEquals("Truk", VehicleTypeHelper.normalizeType("truk"))
        assertEquals("Truk", VehicleTypeHelper.normalizeType("Truck"))
        assertEquals("Bus", VehicleTypeHelper.normalizeType("BUS"))
    }

    @Test
    fun normalizeType_preservesCustomTypeAndDefaultsBlankToMobil() {
        assertEquals("Mobil", VehicleTypeHelper.normalizeType("   "))
        assertEquals("Angkot", VehicleTypeHelper.normalizeType("angkot"))
        assertEquals("Genset", VehicleTypeHelper.normalizeType("Genset"))
    }

    @Test
    fun isPresetType_distinguishesPresetsFromCustom() {
        VehicleTypeHelper.PRESET_TYPES.forEach { preset ->
            assertTrue("$preset should be preset", VehicleTypeHelper.isPresetType(preset))
        }
        assertFalse(VehicleTypeHelper.isPresetType("Angkot"))
        assertFalse(VehicleTypeHelper.isPresetType("Lainnya"))
    }
}
