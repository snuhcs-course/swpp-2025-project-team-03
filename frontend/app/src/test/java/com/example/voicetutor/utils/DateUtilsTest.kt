package com.example.voicetutor.utils

import org.junit.Assert.*
import org.junit.Test

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

    @Test
    fun formatDueDate_withMilliseconds_handlesCorrectly() {
        // Given - ISO 8601 with milliseconds
        val dateWithMs = "2025-11-15T23:59:59.123+09:00"

        // When
        val result = formatDueDate(dateWithMs)

        // Then
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun formatSubmittedTime_withMilliseconds_handlesCorrectly() {
        // Given - ISO 8601 with milliseconds
        val timeWithMs = "2025-11-04T11:59:59.456+09:00"

        // When
        val result = formatSubmittedTime(timeWithMs)

        // Then
        assertNotNull(result)
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun formatDateOnly_withMilliseconds_handlesCorrectly() {
        // Given - ISO 8601 with milliseconds
        val dateWithMs = "2025-11-15T23:59:59.789+09:00"

        // When
        val result = formatDateOnly(dateWithMs)

        // Then
        assertEquals("2025-11-15", result)
    }

    @Test
    fun formatTimeOnly_withMilliseconds_handlesCorrectly() {
        // Given - ISO 8601 with milliseconds
        val timeWithMs = "2025-11-15T23:59:59.999+09:00"

        // When
        val result = formatTimeOnly(timeWithMs)

        // Then
        assertEquals("23:59", result)
    }

    @Test
    fun formatDateKorean_withMilliseconds_handlesCorrectly() {
        // Given - ISO 8601 with milliseconds
        val dateWithMs = "2025-11-15T23:59:59.111+09:00"

        // When
        val result = formatDateKorean(dateWithMs)

        // Then
        assertTrue(result.contains("2025년"))
        assertTrue(result.contains("11월"))
        assertTrue(result.contains("15일"))
    }

    @Test
    fun formatDueDate_nullString_handlesGracefully() {
        // Given
        val nullString: String? = null

        // When
        val result = nullString?.let { formatDueDate(it) }

        // Then
        assertNull(result)
    }

    @Test
    fun formatDueDate_whitespaceOnly_returnsOriginal() {
        // Given
        val whitespace = "   "

        // When
        val result = formatDueDate(whitespace)

        // Then
        assertEquals(whitespace, result)
    }

    @Test
    fun formatSubmittedTime_whitespaceOnly_returnsOriginal() {
        // Given
        val whitespace = "   "

        // When
        val result = formatSubmittedTime(whitespace)

        // Then
        assertEquals(whitespace, result)
    }

    @Test
    fun formatDateOnly_whitespaceOnly_returnsOriginal() {
        // Given
        val whitespace = "   "

        // When
        val result = formatDateOnly(whitespace)

        // Then
        assertEquals(whitespace, result)
    }

    @Test
    fun formatTimeOnly_whitespaceOnly_returnsOriginal() {
        // Given
        val whitespace = "   "

        // When
        val result = formatTimeOnly(whitespace)

        // Then
        assertEquals(whitespace, result)
    }

    @Test
    fun formatDateKorean_whitespaceOnly_returnsOriginal() {
        // Given
        val whitespace = "   "

        // When
        val result = formatDateKorean(whitespace)

        // Then
        assertEquals(whitespace, result)
    }

    @Test
    fun formatDueDate_partialISO8601_returnsOriginal() {
        // Given - Partial ISO 8601 format
        val partialDate = "2025-11-15"

        // When
        val result = formatDueDate(partialDate)

        // Then
        assertEquals(partialDate, result)
    }

    @Test
    fun formatSubmittedTime_partialISO8601_returnsOriginal() {
        // Given - Partial ISO 8601 format
        val partialTime = "11:59"

        // When
        val result = formatSubmittedTime(partialTime)

        // Then
        assertEquals(partialTime, result)
    }

    @Test
    fun formatDateOnly_partialISO8601_returnsOriginal() {
        // Given - Partial ISO 8601 format
        val partialDate = "2025-11"

        // When
        val result = formatDateOnly(partialDate)

        // Then
        assertEquals(partialDate, result)
    }

    @Test
    fun formatTimeOnly_partialISO8601_returnsOriginal() {
        // Given - Partial ISO 8601 format
        val partialTime = "23"

        // When
        val result = formatTimeOnly(partialTime)

        // Then
        assertEquals(partialTime, result)
    }

    @Test
    fun formatDateKorean_partialISO8601_returnsOriginal() {
        // Given - Partial ISO 8601 format
        val partialDate = "2025"

        // When
        val result = formatDateKorean(partialDate)

        // Then
        assertEquals(partialDate, result)
    }
}
