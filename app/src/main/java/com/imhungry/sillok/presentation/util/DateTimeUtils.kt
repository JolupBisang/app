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
    fun utcToKoreaTime(utcTime: String): String {
        return try {
            val baseTimestamp = utcTime.take(19)
            val localDateTime =
                LocalDateTime.parse(baseTimestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val utcZoned = localDateTime.atZone(ZoneOffset.UTC)
            val koreaZoned = utcZoned.withZoneSameInstant(ZoneId.of("Asia/Seoul"))
            koreaZoned.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            ""
        }
    }

    fun isoLocalDateTimeToMillis(isoLocalDateTime: String?): Long {
        if (isoLocalDateTime.isNullOrBlank()) return 0L
        return try {
            val localDateTime =
                LocalDateTime.parse(isoLocalDateTime.take(19), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val koreaZoned = localDateTime.atZone(ZoneId.of("Asia/Seoul"))
            koreaZoned.toInstant().toEpochMilli()
        } catch (e: Exception) {
            0L
        }
    }

    fun isoLocalDateTimeToTimeString(isoLocalDateTime: String?): String {
        if (isoLocalDateTime.isNullOrBlank()) return "00:00:00"
        return try {
            val baseTimestamp = isoLocalDateTime.take(19)
            val localDateTime =
                LocalDateTime.parse(baseTimestamp, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val hour = localDateTime.hour
            val minute = localDateTime.minute
            val second = localDateTime.second
            String.format("%02d:%02d:%02d", hour, minute, second)
        } catch (e: Exception) {
            "00:00:00"
        }
    }

    fun getElapsedStringFromMillis(startMillis: Long?, endMillis: Long): String {
        if (startMillis == null || startMillis <= 0) return "00:00:00"
        val elapsed = ((endMillis - startMillis) / 1000).coerceAtLeast(0)
        val h = elapsed / 3600
        val m = (elapsed % 3600) / 60
        val s = elapsed % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    fun getElapsedStringFromMicros(startMicros: Long?, endMicros: Long): String {
        if (startMicros == null || startMicros <= 0) return "00:00:00"
        val elapsed = ((endMicros - startMicros) / 1000000).coerceAtLeast(0)
        val h = elapsed / 3600
        val m = (elapsed % 3600) / 60
        val s = elapsed % 60
        return String.format("%02d:%02d:%02d", h, m, s)
    }

    fun localIsoToDateString(isoLocalTimestamp: String?): String {
        if (isoLocalTimestamp.isNullOrBlank()) return "-"
        return try {
            val localDateTime =
                LocalDateTime.parse(isoLocalTimestamp.take(19), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val datePart = localDateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
            val dayOfWeek = localDateTime.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.KOREAN)
            "$datePart $dayOfWeek"
        } catch (e: Exception) {
            Log.e("DateTimeUtils", "파싱 실패: $isoLocalTimestamp", e)
            "-"
        }
    }

    fun localIsoToDateStringWithoutDayOfWeek(isoLocalTimestamp: String?): String {
        if (isoLocalTimestamp.isNullOrBlank()) return "-"
        return try {
            val localDateTime =
                LocalDateTime.parse(isoLocalTimestamp.take(19), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            localDateTime.format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
        } catch (e: Exception) {
            Log.e("DateTimeUtils", "파싱 실패: $isoLocalTimestamp", e)
            "-"
        }
    }

    fun localIsoToTimeString(isoLocalTimestamp: String?): String {
        if (isoLocalTimestamp.isNullOrBlank()) return "-"
        return try {
            val localDateTime =
                LocalDateTime.parse(isoLocalTimestamp.take(19), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            Log.e("DateTimeUtils", "파싱 실패: $isoLocalTimestamp", e)
            "-"
        }
    }

    fun calcEndDate(startIsoLocal: String?, targetMinutes: Int): String {
        if (startIsoLocal.isNullOrBlank()) return ""
        return try {
            val start = LocalDateTime.parse(startIsoLocal.take(19), DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            val end = start.plusMinutes(targetMinutes.toLong())
            end.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            ""
        }
    }

    fun getMinutesBetweenMillis(startMillis: Long, endMillis: Long): Long {
        val diffMillis = endMillis - startMillis
        return diffMillis / 1000 / 60
    }

    fun getDurationMinutes(startMillis: Long?, endMillis: Long?): Long {
        if (startMillis == null || endMillis == null || startMillis <= 0 || endMillis <= 0) {
            return 0L
        }
        if (endMillis < startMillis) {
            return 0L
        }
        return getMinutesBetweenMillis(startMillis, endMillis)
    }

    fun millisToHourMinute(millis: Long): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.of("Asia/Seoul"))
        return formatter.format(Instant.ofEpochMilli(millis))
    }

    fun microsToHourMinute(micros: Long): String {
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
            .withZone(ZoneId.of("Asia/Seoul"))
        return formatter.format(Instant.ofEpochMilli(micros / 1000))
    }

    fun timeStringToMillis(timeString: String): Long? {
        return try {
            val parts = timeString.split(":")

            if (parts.size != 3) {
                return null
            }

            val hours = parts[0].toLong()
            val minutes = parts[1].toLong()
            val seconds = parts[2].toLong()

            (hours * 3600 + minutes * 60 + seconds) * 1000

        } catch (e: NumberFormatException) {
            null
        }
    }


    fun getCurrentTime(): String {
        return try {
            val now = ZonedDateTime.now(ZoneId.of("Asia/Seoul"))
            Log.d("DateTimeUtils", "현재 시간: $now")
            now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } catch (e: Exception) {
            Log.e("DateTimeUtils", "현재 시간 변환 실패", e)
            ""
        }
    }

    /**
     * ISO 8601 형식의 문자열을 마이크로초로 변환
     * 예: "2025-11-17T21:41:02.521051" -> 마이크로초 (Long)
     * 소수점 자릿수는 가변적일 수 있음 (0~9자리)
     */
    fun isoLocalDateTimeToMicros(isoLocalDateTime: String?): Long {
        if (isoLocalDateTime.isNullOrBlank()) return 0L
        return try {
            // "T"로 날짜와 시간 분리
            val parts = isoLocalDateTime.split("T")
            if (parts.size != 2) return 0L

            // 날짜 부분 파싱
            val datePart = parts[0]
            val timePart = parts[1]

            // 시간 부분에서 소수점 분리
            val timeParts = timePart.split(".")
            val timeWithoutFraction = timeParts[0] // "21:41:02"
            val fractionPart = if (timeParts.size > 1) timeParts[1] else "0" // "521051" 또는 "0"

            // LocalDateTime 파싱 (소수점 제외)
            val localDateTime = LocalDateTime.parse(
                "${datePart}T${timeWithoutFraction}",
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
            )

            // 초 단위를 마이크로초로 변환
            val koreaZoned = localDateTime.atZone(ZoneId.of("Asia/Seoul"))
            val epochMicros = koreaZoned.toInstant().toEpochMilli() * 1000L

            // 소수점 부분을 마이크로초로 변환 (최대 6자리)
            val fractionMicros = if (fractionPart.isNotEmpty()) {
                val fractionDigits = fractionPart.take(6) // 최대 6자리만 사용
                val paddedFraction = fractionDigits.padEnd(6, '0') // 6자리로 패딩
                paddedFraction.toLongOrNull() ?: 0L
            } else {
                0L
            }

            epochMicros + fractionMicros
        } catch (e: Exception) {
            Log.e("DateTimeUtils", "마이크로초 변환 실패: $isoLocalDateTime", e)
            0L
        }
    }
}
