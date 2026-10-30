package com.mybbm.app.data.local.dao

import androidx.room.*
import com.mybbm.app.data.local.entity.FuelEntity
import com.mybbm.app.data.local.entity.HistoryEntity
import com.mybbm.app.data.local.entity.VehicleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM kendaraan ORDER BY created_at DESC")
    fun getAllVehicles(): Flow<List<VehicleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: VehicleEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vehicles: List<VehicleEntity>)

    @Update
    suspend fun updateVehicle(vehicle: VehicleEntity)

    @Query("DELETE FROM kendaraan WHERE id = :id")
    suspend fun deleteVehicleById(id: String)
}

@Dao
interface FuelDao {
    @Query("SELECT * FROM jenis_bbm ORDER BY nama ASC")
    fun getAllFuels(): Flow<List<FuelEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuel(fuel: FuelEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(fuels: List<FuelEntity>)

    @Update
    suspend fun updateFuel(fuel: FuelEntity)

    @Query("DELETE FROM jenis_bbm WHERE id = :id")
    suspend fun deleteFuelById(id: String)
}

@Dao
interface HistoryDao {
    @Query("SELECT * FROM perjalanan ORDER BY created_at DESC")
    fun getAllHistories(): Flow<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity)

    @Query("DELETE FROM perjalanan WHERE id = :id")
    suspend fun deleteHistoryById(id: String)

    @Query("DELETE FROM perjalanan")
    suspend fun deleteAllHistories()
}
