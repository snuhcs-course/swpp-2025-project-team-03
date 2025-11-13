package com.example.voicetutor.ui.theme

import androidx.compose.ui.text.font.FontWeight
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for Typography.
 */
class TypeTest {

    @Test
    fun typography_isDefined() {
        assertNotNull(Typography)
    }

    @Test
    fun typography_bodyLarge_isDefined() {
        assertNotNull(Typography.bodyLarge)
        assertEquals(16, Typography.bodyLarge.fontSize.value.toInt())
    }

    @Test
    fun typography_titleLarge_isDefined() {
        assertNotNull(Typography.titleLarge)
        assertEquals(22, Typography.titleLarge.fontSize.value.toInt())
    }

    @Test
    fun typography_titleMedium_isDefined() {
        assertNotNull(Typography.titleMedium)
        assertEquals(18, Typography.titleMedium.fontSize.value.toInt())
    }

    @Test
    fun typography_titleSmall_isDefined() {
        assertNotNull(Typography.titleSmall)
        assertEquals(16, Typography.titleSmall.fontSize.value.toInt())
    }

    @Test
    fun typography_bodyMedium_isDefined() {
        assertNotNull(Typography.bodyMedium)
        assertEquals(14, Typography.bodyMedium.fontSize.value.toInt())
    }

    @Test
    fun typography_bodySmall_isDefined() {
        assertNotNull(Typography.bodySmall)
        assertEquals(12, Typography.bodySmall.fontSize.value.toInt())
    }

    @Test
    fun typography_labelLarge_isDefined() {
        assertNotNull(Typography.labelLarge)
        assertEquals(14, Typography.labelLarge.fontSize.value.toInt())
    }

    @Test
    fun typography_labelMedium_isDefined() {
        assertNotNull(Typography.labelMedium)
        assertEquals(12, Typography.labelMedium.fontSize.value.toInt())
    }

    @Test
    fun typography_labelSmall_isDefined() {
        assertNotNull(Typography.labelSmall)
        assertEquals(11, Typography.labelSmall.fontSize.value.toInt())
    }

    @Test
    fun typography_titleLarge_isBold() {
        assertNotNull(Typography.titleLarge.fontWeight)
        assertEquals(FontWeight.Bold, Typography.titleLarge.fontWeight)
    }

    @Test
    fun typography_titleMedium_isSemiBold() {
        assertNotNull(Typography.titleMedium.fontWeight)
        assertEquals(FontWeight.SemiBold, Typography.titleMedium.fontWeight)
    }

    @Test
    fun typography_bodyStyles_haveNormalWeight() {
        assertNotNull(Typography.bodyLarge.fontWeight)
        assertEquals(FontWeight.Normal, Typography.bodyLarge.fontWeight)
        assertNotNull(Typography.bodyMedium.fontWeight)
        assertEquals(FontWeight.Normal, Typography.bodyMedium.fontWeight)
        assertNotNull(Typography.bodySmall.fontWeight)
        assertEquals(FontWeight.Normal, Typography.bodySmall.fontWeight)
    }

    @Test
    fun typography_allStyles_haveFontSize() {
        assertTrue(Typography.bodyLarge.fontSize.value > 0)
        assertTrue(Typography.titleLarge.fontSize.value > 0)
        assertTrue(Typography.titleMedium.fontSize.value > 0)
        assertTrue(Typography.titleSmall.fontSize.value > 0)
        assertTrue(Typography.bodyMedium.fontSize.value > 0)
        assertTrue(Typography.bodySmall.fontSize.value > 0)
        assertTrue(Typography.labelLarge.fontSize.value > 0)
        assertTrue(Typography.labelMedium.fontSize.value > 0)
        assertTrue(Typography.labelSmall.fontSize.value > 0)
    }

    @Test
    fun typography_allStyles_haveLineHeight() {
        assertTrue(Typography.bodyLarge.lineHeight.value > 0)
        assertTrue(Typography.titleLarge.lineHeight.value > 0)
        assertTrue(Typography.titleMedium.lineHeight.value > 0)
        assertTrue(Typography.titleSmall.lineHeight.value > 0)
        assertTrue(Typography.bodyMedium.lineHeight.value > 0)
        assertTrue(Typography.bodySmall.lineHeight.value > 0)
        assertTrue(Typography.labelLarge.lineHeight.value > 0)
        assertTrue(Typography.labelMedium.lineHeight.value > 0)
        assertTrue(Typography.labelSmall.lineHeight.value > 0)
    }
}

