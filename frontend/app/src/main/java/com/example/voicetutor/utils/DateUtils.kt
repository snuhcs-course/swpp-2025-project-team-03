package com.example.voicetutor.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private fun parseIsoDate(isoDate: String): Calendar? {
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ssZ",
        "yyyy-MM-dd'T'HH:mmXXX",
        "yyyy-MM-dd'T'HH:mmZ",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd",
    )

    for (pattern in patterns) {
        try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(isoDate)
            if (date != null) {
                val calendar = Calendar.getInstance()
                calendar.time = date
                return calendar
            }
        } catch (_: Exception) {
            continue
        }
    }
    return null
}

fun formatDueDate(dueDate: String): String {
    return try {
        if (dueDate.matches(Regex("^\\d{4}-\\d{2}-\\d{2}$"))) {
            return dueDate
        }
        val calendar = parseIsoDate(dueDate)
        if (calendar != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            sdf.timeZone = TimeZone.getDefault()
            sdf.format(calendar.time)
        } else {
            dueDate
        }
    } catch (_: Exception) {
        dueDate
    }
}

fun formatSubmittedTime(isoTime: String): String {
    return try {
        val calendar = parseIsoDate(isoTime)
        if (calendar != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            sdf.timeZone = TimeZone.getDefault()
            sdf.format(calendar.time)
        } else {
            isoTime
        }
    } catch (_: Exception) {
        isoTime
    }
}

fun formatDateOnly(isoDate: String): String {
    return try {
        val calendar = parseIsoDate(isoDate)
        if (calendar != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            sdf.timeZone = TimeZone.getDefault()
            sdf.format(calendar.time)
        } else {
            isoDate
        }
    } catch (_: Exception) {
        isoDate
    }
}

fun formatTimeOnly(isoTime: String): String {
    return try {
        val calendar = parseIsoDate(isoTime)
        if (calendar != null) {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            sdf.timeZone = TimeZone.getDefault()
            sdf.format(calendar.time)
        } else {
            isoTime
        }
    } catch (_: Exception) {
        isoTime
    }
}

fun formatDateKorean(isoDate: String): String {
    return try {
        val calendar = parseIsoDate(isoDate)
        if (calendar != null) {
            val sdf = SimpleDateFormat("yyyy년 MM월 dd일", Locale.getDefault())
            sdf.timeZone = TimeZone.getDefault()
            sdf.format(calendar.time)
        } else {
            isoDate
        }
    } catch (_: Exception) {
        isoDate
    }
}
