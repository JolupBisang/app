package com.imhungry.jjongseol.util

import android.util.Log
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeUtils {
    fun isoToMillis(isoTimestamp: String): Long {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSS]")
        val localDateTime = LocalDateTime.parse(isoTimestamp, formatter)

        val utcZoned = localDateTime.atZone(ZoneOffset.UTC)

        val koreaZoneId = ZoneId.of("Asia/Seoul")
        val koreaZoned = utcZoned.withZoneSameInstant(koreaZoneId)

        val millis = koreaZoned.toInstant().toEpochMilli()
        return millis
    }

    fun koreanIsoToMillis(isoTimestamp: String): Long {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss[.SSSSSS]")
        val localDateTime = LocalDateTime.parse(isoTimestamp, formatter)

        val zoneId = ZoneId.of("Asia/Seoul")
        val millis = localDateTime.atZone(zoneId).toInstant().toEpochMilli()
        return millis
    }

    fun getElapsedString(startMillis: Long?, isoTimestamp: String): String {
        if (startMillis == null) return "00:00:00"
        val millis = isoToMillis(isoTimestamp)
        val elapsed = ((millis - startMillis) / 1000).coerceAtLeast(0)
        val h = elapsed / 3600
        val m = (elapsed % 3600) / 60
        val s = elapsed % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    fun localIsoToDateString(isoLocalTimestamp: String?): String {
        if (isoLocalTimestamp.isNullOrBlank()) return "-"
        return try {
            val localDateTime = LocalDateTime.parse(isoLocalTimestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val datePart = localDateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            val dayOfWeek = localDateTime.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.KOREAN)
            "$datePart $dayOfWeek"
        } catch (e: Exception) {
            Log.e("DateTimeUtils", "파싱 실패: $isoLocalTimestamp", e)
            "-"
        }
    }

    fun localIsoToTimeString(isoLocalTimestamp: String?): String {
        if (isoLocalTimestamp.isNullOrBlank()) return "-"
        return try {
            val localDateTime = LocalDateTime.parse(isoLocalTimestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            Log.e("DateTimeUtils", "파싱 실패: $isoLocalTimestamp", e)
            "-"
        }
    }

    fun localIsoToTimeStringPlusMinutes(isoLocalTimestamp: String?, targetTime: Int): String {
        if (isoLocalTimestamp.isNullOrBlank()) return "-"
        return try {
            val localDateTime = LocalDateTime.parse(isoLocalTimestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val updatedDateTime = localDateTime.plusMinutes(targetTime.toLong())
            updatedDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            Log.e("DateTimeUtils", "파싱 실패: $isoLocalTimestamp", e)
            "-"
        }
    }
    fun getMinutesBetweenMillis(startMillis: Long, endMillis: Long): Long {
        val diffMillis = endMillis - startMillis
        return diffMillis / 1000 / 60
    }

    fun millisToHourMinute(millis: Long): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.of("Asia/Seoul"))
        return formatter.format(Instant.ofEpochMilli(millis))
    }
}
