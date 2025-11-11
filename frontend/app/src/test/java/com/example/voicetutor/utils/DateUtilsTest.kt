package com.example.voicetutor.utils

import org.junit.Test
import org.junit.Assert.*
import java.time.ZoneId
import java.time.ZonedDateTime

class DateUtilsTest {

    @Test
    fun formatDueDate_validISO8601_returnsFormattedDate() {
        // Given
        val isoDate = "2025-11-15T23:59:00+09:00"

        // When
        val result = formatDueDate(isoDate)

        // Then
        assertTrue(result.contains("2025-11-15"))
        assertTrue(result.contains("23:59"))
    }

    @Test
    fun formatDueDate_invalidFormat_returnsOriginal() {
        // Given
        val invalidDate = "invalid-date-format"

        // When
        val result = formatDueDate(invalidDate)

        // Then
        assertEquals(invalidDate, result)
    }

    @Test
    fun formatDueDate_emptyString_returnsOriginal() {
        // Given
        val emptyDate = ""

        // When
        val result = formatDueDate(emptyDate)

        // Then
        assertEquals(emptyDate, result)
    }

    @Test
    fun formatSubmittedTime_validISO8601_returnsFormattedTime() {
        // Given
        val isoTime = "2025-11-04T11:59:00+09:00"

        // When
        val result = formatSubmittedTime(isoTime)

        // Then
        assertTrue(result.contains("2025-11-04"))
        assertTrue(result.contains("11:59"))
    }

    @Test
    fun formatSubmittedTime_invalidFormat_returnsOriginal() {
        // Given
        val invalidTime = "not-a-date"

        // When
        val result = formatSubmittedTime(invalidTime)

        // Then
        assertEquals(invalidTime, result)
    }

    @Test
    fun formatDateOnly_validISO8601_returnsDateOnly() {
        // Given
        val isoDate = "2025-11-15T23:59:00+09:00"

        // When
        val result = formatDateOnly(isoDate)

        // Then
        assertEquals("2025-11-15", result)
    }

    @Test
    fun formatDateOnly_invalidFormat_returnsOriginal() {
        // Given
        val invalidDate = "invalid"

        // When
        val result = formatDateOnly(invalidDate)

        // Then
        assertEquals(invalidDate, result)
    }

    @Test
    fun formatTimeOnly_validISO8601_returnsTimeOnly() {
        // Given
        val isoTime = "2025-11-15T23:59:00+09:00"

        // When
        val result = formatTimeOnly(isoTime)

        // Then
        assertEquals("23:59", result)
    }

    @Test
    fun formatTimeOnly_invalidFormat_returnsOriginal() {
        // Given
        val invalidTime = "invalid"

        // When
        val result = formatTimeOnly(invalidTime)

        // Then
        assertEquals(invalidTime, result)
    }

    @Test
    fun formatDateKorean_validISO8601_returnsKoreanFormat() {
        // Given
        val isoDate = "2025-11-15T23:59:00+09:00"

        // When
        val result = formatDateKorean(isoDate)

        // Then
        assertTrue(result.contains("2025년"))
        assertTrue(result.contains("11월"))
        assertTrue(result.contains("15일"))
    }

    @Test
    fun formatDateKorean_invalidFormat_returnsOriginal() {
        // Given
        val invalidDate = "invalid"

        // When
        val result = formatDateKorean(invalidDate)

        // Then
        assertEquals(invalidDate, result)
    }

    @Test
    fun formatDueDate_differentTimeZones_handlesCorrectly() {
        // Given - UTC time
        val utcDate = "2025-11-15T14:59:00Z"

        // When
        val result = formatDueDate(utcDate)

        // Then - Should convert to local timezone
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun formatSubmittedTime_differentTimeZones_handlesCorrectly() {
        // Given - UTC time
        val utcTime = "2025-11-04T02:59:00Z"

        // When
        val result = formatSubmittedTime(utcTime)

        // Then - Should convert to local timezone
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun formatDateOnly_differentTimeZones_handlesCorrectly() {
        // Given - UTC time
        val utcDate = "2025-11-15T14:59:00Z"

        // When
        val result = formatDateOnly(utcDate)

        // Then - Should convert to local date
        assertNotNull(result)
        assertTrue(result.contains("2025"))
    }

    @Test
    fun formatTimeOnly_differentTimeZones_handlesCorrectly() {
        // Given - UTC time
        val utcTime = "2025-11-15T14:59:00Z"

        // When
        val result = formatTimeOnly(utcTime)

        // Then - Should convert to local time
        assertNotNull(result)
        assertTrue(result.matches(Regex("\\d{2}:\\d{2}")))
    }

    @Test
    fun formatDateKorean_differentTimeZones_handlesCorrectly() {
        // Given - UTC time
        val utcDate = "2025-11-15T14:59:00Z"

        // When
        val result = formatDateKorean(utcDate)

        // Then - Should convert to local date in Korean format
        assertNotNull(result)
        assertTrue(result.contains("년"))
        assertTrue(result.contains("월"))
        assertTrue(result.contains("일"))
    }
}

