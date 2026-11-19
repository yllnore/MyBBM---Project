package com.mybbm.app.viewmodel

import com.mybbm.app.data.remote.location.LocationRepository
import com.mybbm.app.domain.model.LatLng
import com.mybbm.app.domain.model.LocationSearchResult
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class BbmViewModelCoordinateResolveTest {

    @Test
    fun resolveCoordinateSmart_usesExistingCoordWithoutSearching() = runBlocking {
        val searchCalls = AtomicInteger(0)
        val existing = LatLng(-0.1234, 117.5678)

        val resolved = BbmViewModel.resolveCoordinateSmart(
            query = "Bandara APT Pranoto",
            existingCoord = existing,
            isOrigin = true,
            cachedResults = emptyList(),
            searchLocations = {
                searchCalls.incrementAndGet()
                emptyList()
            }
        )

        assertEquals(existing, resolved)
        assertEquals(0, searchCalls.get())
    }

    @Test
    fun resolveCoordinateSmart_prefersCachedAutocompleteMatchBeforeSearch() = runBlocking {
        val searchCalls = AtomicInteger(0)
        val cached = listOf(
            LocationSearchResult(
                name = "Bandara APT Pranoto Samarinda",
                displayName = "Sungai Siring, Samarinda",
                lat = -0.291389,
                lon = 117.251944
            )
        )

        val resolved = BbmViewModel.resolveCoordinateSmart(
            query = "Bandara APT Pranoto",
            existingCoord = null,
            isOrigin = true,
            cachedResults = cached,
            searchLocations = {
                searchCalls.incrementAndGet()
                emptyList()
            }
        )

        assertEquals(-0.291389, resolved.lat, 0.0001)
        assertEquals(117.251944, resolved.lng, 0.0001)
        assertEquals(0, searchCalls.get())
    }

    @Test
    fun resolveCoordinateSmart_resolvesOfflineKnownLandmarkInsteadOfSamarindaDefault() = runBlocking {
        val offlineClient = OkHttpClient.Builder()
            .addInterceptor { throw IOException("No network") }
            .build()
        val offlineLocationRepo = LocationRepository(offlineClient)

        val resolved = BbmViewModel.resolveCoordinateSmart(
            query = "Bandara APT Pranoto",
            existingCoord = null,
            isOrigin = true,
            cachedResults = emptyList(),
            searchLocations = { offlineLocationRepo.searchLocation(it) }
        )

        assertEquals(-0.291389, resolved.lat, 0.0001)
        assertEquals(117.251944, resolved.lng, 0.0001)
        assertFalse(
            "Tidak boleh jatuh ke default Samarinda (-0.502106)",
            kotlin.math.abs(resolved.lat - (-0.502106)) < 0.01
        )
    }

    @Test
    fun resolveCoordinateSmart_fallsBackToStaticDictionaryWhenSearchFails() = runBlocking {
        val resolvedOrigin = BbmViewModel.resolveCoordinateSmart(
            query = "Bontang",
            existingCoord = null,
            isOrigin = true,
            cachedResults = emptyList(),
            searchLocations = { throw IOException("Timeout") }
        )
        val resolvedDefaultDest = BbmViewModel.resolveCoordinateSmart(
            query = "Lokasi Tidak Dikenal XYZ",
            existingCoord = null,
            isOrigin = false,
            cachedResults = emptyList(),
            searchLocations = { emptyList() }
        )

        assertEquals(0.133333, resolvedOrigin.lat, 0.0001)
        assertEquals(117.500000, resolvedOrigin.lng, 0.0001)
        assertEquals(-1.265386, resolvedDefaultDest.lat, 0.0001)
        assertEquals(116.831200, resolvedDefaultDest.lng, 0.0001)
    }
}
