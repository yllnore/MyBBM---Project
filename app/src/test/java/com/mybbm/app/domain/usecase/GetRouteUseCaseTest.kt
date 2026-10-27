package com.mybbm.app.domain.usecase

import com.mybbm.app.data.local.dao.PerjalananDao
import com.mybbm.app.data.local.entity.PerjalananEntity
import com.mybbm.app.data.local.entity.SyncState
import com.mybbm.app.data.remote.datasource.RouteRemoteDataSource
import com.mybbm.app.domain.model.LatLng
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.math.BigDecimal

class GetRouteUseCaseTest {

    private class FakePerjalananDao : PerjalananDao {
        val storage = mutableListOf<PerjalananEntity>()

        override suspend fun insert(perjalanan: PerjalananEntity) {
            storage.removeAll { it.idPerjalanan == perjalanan.idPerjalanan }
            storage.add(perjalanan)
        }

        override suspend fun update(perjalanan: PerjalananEntity) {}

        override suspend fun findCached(
            asalLat: Double,
            asalLng: Double,
            tujuanLat: Double,
            tujuanLng: Double
        ): PerjalananEntity? {
            return storage.find {
                Math.abs(it.asalLat - asalLat) < 0.0001 &&
                        Math.abs(it.asalLng - asalLng) < 0.0001 &&
                        Math.abs(it.tujuanLat - tujuanLat) < 0.0001 &&
                        Math.abs(it.tujuanLng - tujuanLng) < 0.0001 &&
                        it.sumberJarak == "DIRECTIONS_API" &&
                        it.deletedAt == null
            }
        }

        override suspend fun getById(id: String): PerjalananEntity? = storage.find { it.idPerjalanan == id }
        override fun observeAll(): Flow<List<PerjalananEntity>> = emptyFlow()
        override suspend fun getPending(): List<PerjalananEntity> = storage.filter { it.syncState == SyncState.PENDING }
        override suspend fun softDelete(id: String, now: Long) {}
        override suspend fun markSynced(ids: List<String>) {}
    }

    private class FakeRouteRemoteDataSource(
        var response: RouteRemoteDataSource.RouteResult
    ) : RouteRemoteDataSource {
        var callCount = 0

        override suspend fun getRoute(origin: LatLng, destination: LatLng): RouteRemoteDataSource.RouteResult {
            callCount++
            return response
        }
    }

    @Test
    fun ketikaAdaDiCache_langsungKembalikanDariCache() = runBlocking {
        val dao = FakePerjalananDao()
        val cached = PerjalananEntity(
            idPerjalanan = "cached-1",
            label = "Rute Harian",
            asalNama = "Samarinda",
            tujuanNama = "Balikpapan",
            asalPlaceId = null,
            tujuanPlaceId = null,
            asalLat = -0.5021,
            asalLng = 117.1537,
            tujuanLat = -1.2653,
            tujuanLng = 116.8312,
            jarakKm = "115.50",
            durasiMenit = 120,
            encodedPolyline = "abc_polyline",
            sumberJarak = "DIRECTIONS_API",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dao.insert(cached)

        val fakeApi = FakeRouteRemoteDataSource(
            RouteRemoteDataSource.RouteResult.Success(
                jarakKm = BigDecimal("120.00"),
                durasiMenit = 130,
                encodedPolyline = "new_polyline"
            )
        )

        val useCase = GetRouteUseCase(dao, fakeApi)
        val result = useCase.execute(
            origin = LatLng(-0.5021, 117.1537),
            destination = LatLng(-1.2653, 116.8312)
        )

        assertTrue(result is GetRouteResult.Success)
        val success = result as GetRouteResult.Success
        assertTrue(success.isFromCache)
        assertFalse(success.isFallback)
        assertEquals("cached-1", success.perjalanan.idPerjalanan)
        assertEquals("115.50", success.perjalanan.jarakKm)
        assertEquals(0, fakeApi.callCount) // API tidak boleh dipanggil jika ada cache
    }

    @Test
    fun ketikaTidakAdaDiCacheDanApiSukses_simpanPerjalananDanKembalikanData() = runBlocking {
        val dao = FakePerjalananDao()
        val fakeApi = FakeRouteRemoteDataSource(
            RouteRemoteDataSource.RouteResult.Success(
                jarakKm = BigDecimal("120.00"),
                durasiMenit = 135,
                encodedPolyline = "poly_123"
            )
        )

        val useCase = GetRouteUseCase(dao, fakeApi)
        val result = useCase.execute(
            origin = LatLng(-0.5021, 117.1537),
            destination = LatLng(-1.2653, 116.8312),
            originName = "Samarinda",
            destinationName = "Balikpapan"
        )

        assertTrue(result is GetRouteResult.Success)
        val success = result as GetRouteResult.Success
        assertFalse(success.isFromCache)
        assertFalse(success.isFallback)
        assertEquals("120.00", success.perjalanan.jarakKm)
        assertEquals(135, success.perjalanan.durasiMenit)
        assertEquals("DIRECTIONS_API", success.perjalanan.sumberJarak)
        assertEquals(1, fakeApi.callCount)
        assertEquals(1, dao.storage.size)
    }

    @Test
    fun ketikaApiGagal_gunakanFallbackHaversineDanSimpan() = runBlocking {
        val dao = FakePerjalananDao()
        val fakeApi = FakeRouteRemoteDataSource(
            RouteRemoteDataSource.RouteResult.NetworkError(IOException("Connection timeout"))
        )

        val useCase = GetRouteUseCase(dao, fakeApi)
        val result = useCase.execute(
            origin = LatLng(-0.5021, 117.1537),
            destination = LatLng(-1.2653, 116.8312)
        )

        assertTrue(result is GetRouteResult.Success)
        val success = result as GetRouteResult.Success
        assertFalse(success.isFromCache)
        assertTrue(success.isFallback)
        assertEquals("HAVERSINE", success.perjalanan.sumberJarak)
        assertEquals(1, fakeApi.callCount)
        assertEquals(1, dao.storage.size)
    }

    @Test
    fun ketikaApiZeroResults_kembalikanRouteNotFound() = runBlocking {
        val dao = FakePerjalananDao()
        val fakeApi = FakeRouteRemoteDataSource(RouteRemoteDataSource.RouteResult.ZeroResults)

        val useCase = GetRouteUseCase(dao, fakeApi)
        val result = useCase.execute(
            origin = LatLng(0.0, 0.0),
            destination = LatLng(10.0, 10.0)
        )

        assertTrue(result is GetRouteResult.RouteNotFound)
        assertEquals(1, fakeApi.callCount)
        assertEquals(0, dao.storage.size)
    }
}
