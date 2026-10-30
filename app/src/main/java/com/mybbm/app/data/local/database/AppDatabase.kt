package com.mybbm.app.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mybbm.app.data.local.converter.BigDecimalConverter
import com.mybbm.app.data.local.dao.FuelDao
import com.mybbm.app.data.local.dao.HistoryDao
import com.mybbm.app.data.local.dao.VehicleDao
import com.mybbm.app.data.local.entity.FuelEntity
import com.mybbm.app.data.local.entity.HistoryEntity
import com.mybbm.app.data.local.entity.VehicleEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID

@Database(
    entities = [VehicleEntity::class, FuelEntity::class, HistoryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(BigDecimalConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun vehicleDao(): VehicleDao
    abstract fun fuelDao(): FuelDao
    abstract fun historyDao(): HistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mybbm_database.db"
                )
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(private val appContext: Context) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getInstance(appContext)
                    populateInitialData(database)
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
                // Initial Vehicles matching Figma Demo
                val defaultVehicles = listOf(
                    VehicleEntity(UUID.randomUUID().toString(), "AVANZA", "Mobil", BigDecimal("12.5")),
                    VehicleEntity(UUID.randomUUID().toString(), "TOYOTA", "Mobil", BigDecimal("14.0")),
                    VehicleEntity(UUID.randomUUID().toString(), "HONDA", "Mobil", BigDecimal("15.0")),
                    VehicleEntity(UUID.randomUUID().toString(), "HONDA BEAT", "Motor", BigDecimal("55.0")),
                    VehicleEntity(UUID.randomUUID().toString(), "HONDA VARIO", "Motor", BigDecimal("48.0")),
                    VehicleEntity(UUID.randomUUID().toString(), "NMAX", "Motor", BigDecimal("40.0")),
                    VehicleEntity(UUID.randomUUID().toString(), "FAZZIO", "Motor", BigDecimal("50.0"))
                )
                database.vehicleDao().insertAll(defaultVehicles)

                // Initial Fuel types matching Figma Demo
                val defaultFuels = listOf(
                    FuelEntity(UUID.randomUUID().toString(), "PERTALITE", BigDecimal("10000"), "#10B981"),
                    FuelEntity(UUID.randomUUID().toString(), "PERTAMAX", BigDecimal("12500"), "#7542EF"),
                    FuelEntity(UUID.randomUUID().toString(), "PERTAMAX TURBO", BigDecimal("13800"), "#EF4444"),
                    FuelEntity(UUID.randomUUID().toString(), "SOLAR", BigDecimal("6800"), "#4B5563"),
                    FuelEntity(UUID.randomUUID().toString(), "PERTAMAX GREEN 95", BigDecimal("13500"), "#22C55E")
                )
                database.fuelDao().insertAll(defaultFuels)
            }
        }
    }
}
