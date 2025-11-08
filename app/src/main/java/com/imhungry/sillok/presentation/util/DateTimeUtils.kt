package com.imhungry.sillok.presentation.util

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
object DateTimeUtils {
    fun isoToMillis(isoTimestamp: String): Long {
        return try {
            // 'yyyy-MM-dd'T'HH:mm:ss'까지만 자르기 (19자, 소수점 초나 타임존 정보 제거)
            val baseTimestamp = isoTimestamp.take(19)
            val localDateTime =
                LocalDateTime.parse(baseTimestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val utcZoned = localDateTime.atZone(ZoneOffset.UTC)
            val koreaZoned = utcZoned.withZoneSameInstant(ZoneId.of("Asia/Seoul"))
            koreaZoned.toInstant().toEpochMilli()
        } catch (e: Exception) {
            Log.e("DateTimeUtils", "isoToMillis 파싱 실패: $isoTimestamp", e)
            0
        }
    }

//    fun isoToMillis(isoTimestamp: String): Long {
//        val patterns = listOf(
//            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS",
//            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSS",
//            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS",
//            "yyyy-MM-dd'T'HH:mm:ss.SSSSSS",
//            "yyyy-MM-dd'T'HH:mm:ss.SSSSS",
//            "yyyy-MM-dd'T'HH:mm:ss.SSSS",
//            "yyyy-MM-dd'T'HH:mm:ss.SSS",
//            "yyyy-MM-dd'T'HH:mm:ss.SS",
//            "yyyy-MM-dd'T'HH:mm:ss.S",
//        )
//
//        for (pattern in patterns) {
//            try {
//                val formatter = DateTimeFormatter.ofPattern(pattern)
//                val localDateTime = LocalDateTime.parse(isoTimestamp, formatter)
//                val utcZoned = localDateTime.atZone(ZoneOffset.UTC)
//                val koreaZoned = utcZoned.withZoneSameInstant(ZoneId.of("Asia/Seoul"))
//                return koreaZoned.toInstant().toEpochMilli()
//            } catch (e: Exception) {
//                // 패턴 불일치 → 다음 패턴 시도
//            }
//        }
//        return 0
//    }

    fun getElapsedString(startMillis: Long?, isoTimestamp: String): String {
        if (startMillis == null) return "00:00:00"
        val millis = isoToMillis(isoTimestamp)
        val elapsed = ((millis - startMillis) / 1000).coerceAtLeast(0)
        val h = elapsed / 3600
        val m = (elapsed % 3600) / 60
        val s = elapsed % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    /**
     * startMillis와 endMillis를 받아 경과 시간 문자열 반환 (HH:MM:SS 형식)
     */
    fun getElapsedStringFromMillis(startMillis: Long?, endMillis: Long): String {
        if (startMillis == null || startMillis <= 0) return "00:00:00"
        val elapsed = ((endMillis - startMillis) / 1000).coerceAtLeast(0)
        val h = elapsed / 3600
        val m = (elapsed % 3600) / 60
        val s = elapsed % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    fun localIsoToDateString(isoLocalTimestamp: String?): String {
        if (isoLocalTimestamp.isNullOrBlank()) return "-"
        return try {
            val localDateTime =
                LocalDateTime.parse(isoLocalTimestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val datePart = localDateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            val dayOfWeek = localDateTime.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
            "$datePart $dayOfWeek"
        } catch (e: Exception) {
            Log.e("DateTimeUtils", "파싱 실패: $isoLocalTimestamp", e)
            "-"
        }
    }

    fun localIsoToTimeString(isoLocalTimestamp: String?): String {
        if (isoLocalTimestamp.isNullOrBlank()) return "-"
        return try {
            val localDateTime =
                LocalDateTime.parse(isoLocalTimestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            Log.e("DateTimeUtils", "파싱 실패: $isoLocalTimestamp", e)
            "-"
        }
    }

    fun localIsoToTimeStringPlusMinutes(isoLocalTimestamp: String?, targetTime: Int): String {
        if (isoLocalTimestamp.isNullOrBlank()) return "-"
        return try {
            val localDateTime =
                LocalDateTime.parse(isoLocalTimestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val updatedDateTime = localDateTime.plusMinutes(targetTime.toLong())
            updatedDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            Log.e("DateTimeUtils", "파싱 실패: $isoLocalTimestamp", e)
            "-"
        }
    }

    /**
     * 회의 시작 시각(ISO_LOCAL_DATE_TIME)과 목표 시간(분)을 받아
     * 종료 시각을 ISO_LOCAL_DATE_TIME 문자열로 반환합니다.
     * 입력/출력 예: 2025-09-10T18:00:00
     */
    fun calcEndTimeIsoLocal(startIsoLocal: String?, targetMinutes: Int): String {
        if (startIsoLocal.isNullOrBlank()) return ""
        return try {
            val start = LocalDateTime.parse(startIsoLocal, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val end = start.plusMinutes(targetMinutes.toLong())
            end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            Log.e("DateTimeUtils", "종료 시각 계산 실패: $startIsoLocal, target=$targetMinutes", e)
            ""
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
        return try {
            // 'yyyy-MM-dd'T'HH:mm:ss'까지만 자르기 (19자, 소수점 초 제거)
            val baseTimestamp = koreaTime.take(19)
            val localDateTime =
                LocalDateTime.parse(baseTimestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val seoulZoned = ZonedDateTime.of(localDateTime, ZoneId.of("Asia/Seoul"))
            val utcZoned = seoulZoned.withZoneSameInstant(ZoneId.of("UTC"))
            utcZoned.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            Log.e("DateTimeUtils", "koreaToUtcTime 파싱 실패: $koreaTime", e)
            ""
        }
    }

}
