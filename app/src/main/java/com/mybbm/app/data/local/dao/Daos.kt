package com.mybbm.app.data.local.dao

import androidx.room.*
import com.mybbm.app.data.local.entity.BbmKustomEntity
import com.mybbm.app.data.local.entity.KendaraanKustomEntity
import com.mybbm.app.data.local.entity.RiwayatEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) untuk tabel `kendaraan_kustom` (data buatan user).
 */
@Dao
interface KendaraanKustomDao {
    @Query("SELECT * FROM kendaraan_kustom ORDER BY created_at DESC")
    fun getAllKendaraanKustom(): Flow<List<KendaraanKustomEntity>>

    @Query("SELECT * FROM kendaraan_kustom ORDER BY created_at DESC")
    suspend fun getAllKendaraanKustomList(): List<KendaraanKustomEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKendaraan(kendaraan: KendaraanKustomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(kendaraanList: List<KendaraanKustomEntity>)

    @Update
    suspend fun updateKendaraan(kendaraan: KendaraanKustomEntity)

    @Delete
    suspend fun deleteKendaraan(kendaraan: KendaraanKustomEntity)

    @Query("DELETE FROM kendaraan_kustom WHERE id = :id")
    suspend fun deleteKendaraanById(id: String)

    @Query("DELETE FROM kendaraan_kustom")
    suspend fun deleteAllKendaraan()
}

/**
 * Data Access Object (DAO) untuk tabel `bbm_kustom` (data buatan user).
 */
@Dao
interface BbmKustomDao {
    @Query("SELECT * FROM bbm_kustom ORDER BY created_at DESC")
    fun getAllBbmKustom(): Flow<List<BbmKustomEntity>>

    @Query("SELECT * FROM bbm_kustom ORDER BY created_at DESC")
    suspend fun getAllBbmKustomList(): List<BbmKustomEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBbm(bbm: BbmKustomEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bbmList: List<BbmKustomEntity>)

    @Update
    suspend fun updateBbm(bbm: BbmKustomEntity)

    @Delete
    suspend fun deleteBbm(bbm: BbmKustomEntity)

    @Query("DELETE FROM bbm_kustom WHERE id = :id")
    suspend fun deleteBbmById(id: String)

    @Query("DELETE FROM bbm_kustom")
    suspend fun deleteAllBbm()
}

/**
 * Data Access Object (DAO) untuk tabel `riwayat`.
 */
@Dao
interface RiwayatDao {
    @Query("SELECT * FROM riwayat ORDER BY timestamp DESC")
    fun getAllRiwayat(): Flow<List<RiwayatEntity>>

    @Query("SELECT * FROM riwayat ORDER BY timestamp DESC")
    suspend fun getAllRiwayatList(): List<RiwayatEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRiwayat(riwayat: RiwayatEntity)

    @Delete
    suspend fun deleteRiwayat(riwayat: RiwayatEntity)

    @Query("DELETE FROM riwayat WHERE id = :id")
    suspend fun deleteRiwayatById(id: String)

    @Query("DELETE FROM riwayat")
    suspend fun deleteAllRiwayat()
}

// Aliases untuk backward compatibility
typealias BbmDao = BbmKustomDao
typealias VehicleDao = KendaraanKustomDao
typealias FuelDao = BbmKustomDao
typealias HistoryDao = RiwayatDao
