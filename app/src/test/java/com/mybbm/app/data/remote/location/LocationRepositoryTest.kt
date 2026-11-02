package com.mybbm.app.data.remote.location

import com.mybbm.app.domain.model.LocationSearchResult
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocationRepositoryTest {

    private val repository = LocationRepository()

    @Test
    fun testLocationSearchResult_formatting() {
        val resultWithName = LocationSearchResult(
            displayName = "Politeknik Negeri Samarinda, Jalan Dr. Cipto Mangunkusumo, Samarinda Seberang, Samarinda, Kalimantan Timur, Indonesia",
            lat = -0.502106,
            lon = 117.153709,
            name = "Politeknik Negeri Samarinda"
        )

        assertEquals("Politeknik Negeri Samarinda", resultWithName.title)
        assertEquals("Jalan Dr. Cipto Mangunkusumo, Samarinda Seberang, Samarinda, Kalimantan Timur, Indonesia", resultWithName.subtitle)
        assertEquals(-0.502106, resultWithName.coordinate.lat, 0.000001)
        assertEquals(117.153709, resultWithName.coordinate.lng, 0.000001)

        val resultWithoutName = LocationSearchResult(
            displayName = "Big Mall Samarinda, Jalan Untung Suropati, Samarinda",
            lat = -0.520556,
            lon = 117.120556,
            name = ""
        )

        assertEquals("Big Mall Samarinda", resultWithoutName.title)
        assertEquals("Jalan Untung Suropati, Samarinda", resultWithoutName.subtitle)
    }

    @Test
    fun testSearchLocation_emptyQueryReturnsEmptyList() = runBlocking {
        val resultBlank = repository.searchLocation("")
        val resultSingleChar = repository.searchLocation("a")

        assertTrue(resultBlank.isEmpty())
        assertTrue(resultSingleChar.isEmpty())
    }
}
