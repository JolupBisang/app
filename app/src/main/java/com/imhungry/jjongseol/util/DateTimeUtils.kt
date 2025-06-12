package com.imhungry.jjongseol.util

import android.util.Log
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateTimeUtils {
    fun isoToMillis(isoTimestamp: String): Long {
        val patterns = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSSS",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss.SS",
            "yyyy-MM-dd'T'HH:mm:ss.S",
        )

        for (pattern in patterns) {
            try {
                val formatter = DateTimeFormatter.ofPattern(pattern)
                val localDateTime = LocalDateTime.parse(isoTimestamp, formatter)
                val utcZoned = localDateTime.atZone(ZoneOffset.UTC)
                val koreaZoned = utcZoned.withZoneSameInstant(ZoneId.of("Asia/Seoul"))
                return koreaZoned.toInstant().toEpochMilli()
            } catch (e: Exception) {
                // 패턴 불일치 → 다음 패턴 시도
            }
        }

        throw IllegalArgumentException("지원되지 않는 ISO 포맷: $isoTimestamp")
    }

//    fun isoToMillis(isoTimestamp: String): Long {
//        // ISO 형식 파싱 (마이크로초/나노초 자동 지원)
//        val odt = OffsetDateTime.parse(isoTimestamp)
//        // Asia/Seoul 기준 millis 반환
//        return odt.atZoneSameInstant(ZoneId.of("Asia/Seoul")).toInstant().toEpochMilli()
//    }

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

    fun koreaToUtcTime(koreaTime: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        val localDateTime = LocalDateTime.parse(koreaTime, formatter)
        val seoulZoned = ZonedDateTime.of(localDateTime, ZoneId.of("Asia/Seoul"))
        val utcZoned = seoulZoned.withZoneSameInstant(ZoneId.of("UTC"))
        return utcZoned.format(formatter)
    }
}
