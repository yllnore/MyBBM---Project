package com.mybbm.app.worker

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mybbm.app.data.local.database.AppDatabase
import com.mybbm.app.data.local.entity.SyncMetaEntity
import com.mybbm.app.data.remote.supabase.SupabaseAuthManager
import com.mybbm.app.data.remote.supabase.SupabaseClientProvider
import com.mybbm.app.data.remote.supabase.SupabaseReferenceRemoteRepository
import com.mybbm.app.data.remote.supabase.SupabaseUserRemoteRepository
import com.mybbm.app.domain.usecase.PushDataUseCase
import java.util.concurrent.TimeUnit
import kotlin.math.max

/**
 * Worker latar belakang untuk sinkronisasi data MyBBM menggunakan AndroidX WorkManager.
 *
 * Menerapkan spesifikasi PRD Bagian 6 (Sinkronisasi Data):
 * 1. Bagian 6.2 — Memastikan sesi Anonymous Auth aktif (signInAnonymously jika belum ada).
 * 2. Bagian 6.4 — Delta-sync pull referensi (jenis_bbm & harga_bbm) berdasarkan kursor updated_at.
 * 3. Bagian 6.4 & 8.3 — Push data pengguna (kendaraan, perjalanan, hasil) berstatus PENDING.
 * 4. Bagian 6.7 — Kebijakan retry: Exponential Backoff (30s - 1h), abaikan 403 (RLS violation).
 */
class SyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.i(TAG, "Sinkronisasi MyBBM dimulai (attempt: $runAttemptCount)...")

        val database = AppDatabase.getInstance(applicationContext)

        val supabase = try {
            SupabaseClientProvider.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "SupabaseClientProvider gagal: ${e.message}", e)
            return Result.failure()
        }

        val authManager = SupabaseAuthManager(supabase)
        val referenceRepository = SupabaseReferenceRemoteRepository(supabase)
        val userRepository = SupabaseUserRemoteRepository(supabase)
        val pushDataUseCase = PushDataUseCase(
            deviceInfoDao = database.deviceInfoDao(),
            kendaraanDao = database.kendaraanDao(),
            perjalananDao = database.perjalananDao(),
            hasilPerhitunganDao = database.hasilPerhitunganDao(),
            userRemoteRepository = userRepository
        )

        try {
            // =========================================================================
            // LANGKAH 1: Pastikan Sesi Autentikasi (PRD Bagian 6.2 & 8.3)
            // =========================================================================
            Log.d(TAG, "Memeriksa sesi autentikasi Supabase...")
            val session = authManager.ensureSession()
            if (session == null) {
                Log.w(TAG, "Sesi tidak dapat dibuat, mencoba ulang...")
                return Result.retry()
            }
            Log.d(TAG, "Sesi aktif: auth.uid = ${authManager.getCurrentUserId()}")

            // =========================================================================
            // LANGKAH 2: Delta-Sync Pull Referensi (PRD Bagian 6.4)
            // =========================================================================
            Log.d(TAG, "Memulai pull data referensi...")
            val currentCursor = database.syncMetaDao().getReferenceCursor() ?: 0L

            val jenisBbmList = referenceRepository.pullAllJenisBbm(currentCursor)
            if (jenisBbmList.isNotEmpty()) {
                val entities = jenisBbmList.map { it.toEntity() }
                database.jenisBbmDao().insertAll(entities)
                Log.d(TAG, "Berhasil pull ${entities.size} jenis BBM baru/terbarui.")
            }

            val hargaBbmList = referenceRepository.pullAllHargaBbm(currentCursor)
            if (hargaBbmList.isNotEmpty()) {
                val entities = hargaBbmList.map { it.toEntity() }
                database.hargaBbmDao().insertAll(entities)
                Log.d(TAG, "Berhasil pull ${entities.size} catatan harga BBM baru/terbarui.")
            }

            // Perbarui kursor dengan stempel waktu terbesar yang berhasil ditarik
            val maxJenisUpdated = jenisBbmList.maxOfOrNull {
                com.mybbm.app.data.remote.dto.DateUtils.parseIsoToEpochMillis(it.updatedAt)
            } ?: 0L
            val maxHargaUpdated = hargaBbmList.maxOfOrNull {
                com.mybbm.app.data.remote.dto.DateUtils.parseIsoToEpochMillis(it.updatedAt)
            } ?: 0L
            val newCursor = max(currentCursor, max(maxJenisUpdated, maxHargaUpdated))

            if (newCursor > currentCursor) {
                database.syncMetaDao().upsert(
                    SyncMetaEntity(
                        key = "reference_cursor",
                        value = newCursor
                    )
                )
                Log.d(TAG, "Kursor referensi diperbarui ke: $newCursor")
            }

            // =========================================================================
            // LANGKAH 3: Push Data Pengguna PENDING ke Cloud (PRD Bagian 6.4 & 8.3)
            // =========================================================================
            Log.d(TAG, "Memulai push data pengguna yang berstatus PENDING...")
            val pushResult = pushDataUseCase.execute()

            if (!pushResult.isSuccess) {
                Log.e(TAG, "Push data pengguna gagal: ${pushResult.errorMessage}")
                return Result.retry()
            }

            Log.i(
                TAG,
                "Sinkronisasi berhasil! Pushed: ${pushResult.kendaraanCount} kendaraan, " +
                        "${pushResult.perjalananCount} perjalanan, ${pushResult.hasilCount} hasil perhitungan."
            )
            return Result.success()

        } catch (e: Throwable) {
            return handleError(e)
        }
    }

    /**
     * Menangani galat sesuai kebijakan retry PRD Bagian 6.7:
     * - Galat pelanggaran RLS (403) dicatat sebagai galat konfigurasi dan TIDAK diulang.
     * - Galat jaringan, 401, 429, dan 5xx diulang via exponential backoff WorkManager.
     */
    private fun handleError(e: Throwable): Result {
        val message = e.message ?: ""
        Log.e(TAG, "Galat saat eksekusi sinkronisasi: $message", e)

        if (message.contains("403") || message.contains("Forbidden") || message.contains("row-level security")) {
            Log.e(TAG, "Pelanggaran RLS terdeteksi (403). Tidak dilakukan retry: $message")
            return Result.failure()
        }

        // Retry dengan exponential backoff
        return Result.retry()
    }

    companion object {
        private const val TAG = "SyncWorker"

        const val PERIODIC_WORK_NAME = "MyBBM_Periodic_Sync"
        const val ONE_TIME_WORK_NAME = "MyBBM_OneTime_Sync"

        /**
         * Menjadwalkan sinkronisasi berkala setiap 6 jam dengan constraint koneksi internet
         * sesuai PRD Bagian 6.3.
         */
        fun enqueuePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30, TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
            Log.i(TAG, "Periodic sync berhasil dijadwalkan (interval 6 jam, constraint CONNECTED).")
        }

        /**
         * Memicu sinkronisasi satu kali (one-time) segera saat ada data baru tersimpan atau
         * saat dipicu secara manual (PRD Bagian 6.3).
         */
        fun enqueueOneTimeSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30, TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                ONE_TIME_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                syncRequest
            )
            Log.i(TAG, "One-time sync berhasil dipicu.")
        }
    }
}
