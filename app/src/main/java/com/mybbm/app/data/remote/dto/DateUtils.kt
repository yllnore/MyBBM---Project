package com.mybbm.app.data.remote.dto

import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Utilitas konversi waktu dan tanggal antara Supabase PostgreSQL (ISO-8601 & DATE)
 * dengan representasi Room Database (epoch milli-detik).
 */
object DateUtils {

    /**
     * Mengubah string ISO-8601 timestamptz dari Supabase menjadi epoch milli-detik (UTC).
     * Contoh input: "2024-01-01T00:00:00Z" atau "2024-01-01T07:00:00+07:00".
     */
    fun parseIsoToEpochMillis(isoString: String): Long {
        return try {
            OffsetDateTime.parse(isoString).toInstant().toEpochMilli()
        } catch (_: Exception) {
            try {
                Instant.parse(isoString).toEpochMilli()
            } catch (_: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    /**
     * Mengubah epoch milli-detik (UTC) menjadi string ISO-8601 UTC.
     * Digunakan untuk parameter filter query PostgREST: gte("updated_at", cursorIso).
     */
    fun epochMillisToIso(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis).toString()
    }

    /**
     * Mengubah string tanggal SQL (DATE: "YYYY-MM-DD") menjadi epoch milli-detik pada awal hari UTC.
     * Contoh: "2024-01-01" -> 1704067200000.
     */
    fun parseDateToEpochMillis(dateString: String): Long {
        return try {
            LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
                .atStartOfDay(ZoneOffset.UTC)
                .toInstant()
                .toEpochMilli()
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * Mengubah epoch milli-detik menjadi string tanggal SQL (DATE: "YYYY-MM-DD") UTC.
     */
    fun epochMillisToDateString(epochMillis: Long): String {
        return Instant.ofEpochMilli(epochMillis)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .format(DateTimeFormatter.ISO_LOCAL_DATE)
    }
}
