package com.example.mybbm.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mybbm.data.local.dao.FuelPriceDao
import com.example.mybbm.data.local.dao.HistoryDao
import com.example.mybbm.data.local.dao.VehicleDao
import com.example.mybbm.data.local.entity.FuelPriceEntity
import com.example.mybbm.data.local.entity.HistoryEntity
import com.example.mybbm.data.local.entity.VehicleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [VehicleEntity::class, FuelPriceEntity::class, HistoryEntity::class], version = 1, exportSchema = false)
abstract class MyBbmDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao
    abstract fun fuelPriceDao(): FuelPriceDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: MyBbmDatabase? = null

        fun getDatabase(context: Context): MyBbmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyBbmDatabase::class.java,
                    "mybbm_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: MyBbmDatabase) {
            val vehicleDao = database.vehicleDao()
            val fuelPriceDao = database.fuelPriceDao()

            if (vehicleDao.getVehicleCount() == 0) {
                vehicleDao.insertVehicles(
                    listOf(
                        VehicleEntity(name = "Sepeda motor irit", type = "Sepeda Motor", fuelEfficiency = 50.0, isDefault = true),
                        VehicleEntity(name = "Sepeda motor standar", type = "Sepeda Motor", fuelEfficiency = 40.0, isDefault = true),
                        VehicleEntity(name = "Sepeda motor besar", type = "Sepeda Motor", fuelEfficiency = 30.0, isDefault = true),
                        VehicleEntity(name = "Mobil irit", type = "Mobil", fuelEfficiency = 20.0, isDefault = true),
                        VehicleEntity(name = "Mobil standar", type = "Mobil", fuelEfficiency = 12.0, isDefault = true),
                        VehicleEntity(name = "Mobil besar", type = "Mobil", fuelEfficiency = 8.0, isDefault = true)
                    )
                )
            }

            if (fuelPriceDao.getFuelPriceCount() == 0) {
                fuelPriceDao.insertFuelPrices(
                    listOf(
                        FuelPriceEntity(fuelType = "Pertalite", pricePerLiter = 10000.0),
                        FuelPriceEntity(fuelType = "Pertamax", pricePerLiter = 12500.0),
                        FuelPriceEntity(fuelType = "Pertamax Turbo", pricePerLiter = 14500.0),
                        FuelPriceEntity(fuelType = "Dexlite", pricePerLiter = 14000.0),
                        FuelPriceEntity(fuelType = "Pertamina Dex", pricePerLiter = 14500.0)
                    )
                )
            }
        }
    }
}
