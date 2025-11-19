package com.example.voicetutor.utils

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * ISO 8601 형식의 타임스탬프를 사용자 친화적인 형식으로 변환하는 유틸리티 함수들
 */

/**
 * 마감일을 사용자 친화적인 형식으로 포맷팅
 * 예: "2025-11-15 23:59" 또는 "2025년 11월 15일 23:59"
 */
fun formatDueDate(dueDate: String): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(dueDate)
        val localDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        localDateTime.format(formatter)
    } catch (e: Exception) {
        // 파싱 실패 시 원본 반환
        dueDate
    }
}

/**
 * 제출 시간을 사용자 친화적인 형식으로 포맷팅
 * 예: "2025-11-04 11:59"
 */
fun formatSubmittedTime(isoTime: String): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(isoTime)
        val localDateTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        localDateTime.format(formatter)
    } catch (e: Exception) {
        isoTime
    }
}

/**
 * 날짜만 표시 (시간 없이)
 * 예: "2025-11-15"
 */
fun formatDateOnly(isoDate: String): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(isoDate)
        val localDate = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        localDate.format(formatter)
    } catch (e: Exception) {
        isoDate
    }
}

/**
 * 시간만 표시
 * 예: "23:59"
 */
fun formatTimeOnly(isoTime: String): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(isoTime)
        val localTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalTime()
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        localTime.format(formatter)
    } catch (e: Exception) {
        isoTime
    }
}

/**
 * 한국어 형식으로 날짜 표시
 * 예: "2025년 11월 15일"
 */
fun formatDateKorean(isoDate: String): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(isoDate)
        val localDate = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
        val formatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
        localDate.format(formatter)
    } catch (e: Exception) {
        isoDate
    }
}
