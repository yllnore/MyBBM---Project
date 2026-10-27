package com.mybbm.app.domain.usecase

import com.mybbm.app.data.local.dao.DeviceInfoDao
import com.mybbm.app.data.local.dao.HasilPerhitunganDao
import com.mybbm.app.data.local.dao.KendaraanDao
import com.mybbm.app.data.local.dao.PerjalananDao
import com.mybbm.app.data.remote.dto.HasilPerhitunganDto
import com.mybbm.app.data.remote.dto.KendaraanDto
import com.mybbm.app.data.remote.dto.PerjalananDto
import com.mybbm.app.data.remote.supabase.UserRemoteRepository

/**
 * Use case yang menangani seluruh alur push data pengguna dari Room ke Supabase.
 *
 * Alur eksekusi (PRD Bagian 6.1, 6.2, 6.4):
 * 1. Membaca local_device_id dari tabel device_info di Room.
 * 2. Mengambil baris berstatus PENDING dari setiap DAO pengguna.
 * 3. Mengonversi entitas ke DTO (menyertakan local_device_id).
 * 4. Push ke Supabase via [UserRemoteRepository] dengan urutan:
 *    KENDARAAN -> PERJALANAN -> HASIL_PERHITUNGAN (memenuhi FK dependency).
 * 5. Jika push berhasil (tanpa exception), tandai baris tersebut sebagai SYNCED di Room.
 *
 * Pastikan [com.mybbm.app.data.remote.supabase.SupabaseAuthManager.ensureSession] telah dipanggil oleh pemanggil
 * (SyncWorker) sebelum use case ini dijalankan.
 */
class PushDataUseCase(
    private val deviceInfoDao: DeviceInfoDao,
    private val kendaraanDao: KendaraanDao,
    private val perjalananDao: PerjalananDao,
    private val hasilPerhitunganDao: HasilPerhitunganDao,
    private val userRemoteRepository: UserRemoteRepository
) {

    /**
     * Menjalankan alur push data pengguna ke Supabase.
     * @return [PushResult] yang merangkum hasil eksekusi.
     */
    suspend fun execute(): PushResult {
        // Langkah 1: Baca local_device_id dari database lokal
        val localDeviceId = deviceInfoDao.getLocalDeviceId()
            ?: return PushResult.failure("local_device_id tidak ditemukan di device_info.")

        // Langkah 2: Ambil semua baris PENDING dari setiap tabel pengguna
        val pendingKendaraan = kendaraanDao.getPending()
        val pendingPerjalanan = perjalananDao.getPending()
        val pendingHasil = hasilPerhitunganDao.getPending()

        // Jika tidak ada data PENDING, selesai tanpa pemanggilan jaringan
        if (pendingKendaraan.isEmpty() && pendingPerjalanan.isEmpty() && pendingHasil.isEmpty()) {
            return PushResult.success(kendaraanCount = 0, perjalananCount = 0, hasilCount = 0)
        }

        // Langkah 3: Konversi entitas ke DTO (menyertakan local_device_id)
        val kendaraanDtos = pendingKendaraan.map { KendaraanDto.fromEntity(it, localDeviceId) }
        val perjalananDtos = pendingPerjalanan.map { PerjalananDto.fromEntity(it, localDeviceId) }
        val hasilDtos = pendingHasil.map { HasilPerhitunganDto.fromEntity(it, localDeviceId) }

        // Langkah 4 & 5: Push ke Supabase dengan urutan yang benar, lalu tandai SYNCED
        // Urutan WAJIB: KENDARAAN -> PERJALANAN -> HASIL_PERHITUNGAN (memenuhi FK)

        if (kendaraanDtos.isNotEmpty()) {
            userRemoteRepository.pushKendaraan(kendaraanDtos)
            kendaraanDao.markSynced(pendingKendaraan.map { it.idKendaraan })
        }

        if (perjalananDtos.isNotEmpty()) {
            userRemoteRepository.pushPerjalanan(perjalananDtos)
            perjalananDao.markSynced(pendingPerjalanan.map { it.idPerjalanan })
        }

        if (hasilDtos.isNotEmpty()) {
            userRemoteRepository.pushHasilPerhitungan(hasilDtos)
            hasilPerhitunganDao.markSynced(pendingHasil.map { it.idHasil })
        }

        return PushResult.success(
            kendaraanCount = kendaraanDtos.size,
            perjalananCount = perjalananDtos.size,
            hasilCount = hasilDtos.size
        )
    }
}

/**
 * Hasil eksekusi [PushDataUseCase].
 */
data class PushResult(
    val isSuccess: Boolean,
    val kendaraanCount: Int = 0,
    val perjalananCount: Int = 0,
    val hasilCount: Int = 0,
    val errorMessage: String? = null
) {
    val totalPushed: Int get() = kendaraanCount + perjalananCount + hasilCount

    companion object {
        fun success(kendaraanCount: Int, perjalananCount: Int, hasilCount: Int) = PushResult(
            isSuccess = true,
            kendaraanCount = kendaraanCount,
            perjalananCount = perjalananCount,
            hasilCount = hasilCount
        )

        fun failure(message: String) = PushResult(
            isSuccess = false,
            errorMessage = message
        )
    }
}
