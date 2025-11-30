package com.example.voicetutor.ui.components

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for StatsCard enums.
 */
class StatsCardTest {

    @Test
    fun trendDirection_enumValues_areCorrect() {
        assertEquals(3, TrendDirection.entries.size)
        assertTrue(TrendDirection.entries.contains(TrendDirection.Up))
        assertTrue(TrendDirection.entries.contains(TrendDirection.Down))
        assertTrue(TrendDirection.entries.contains(TrendDirection.None))
    }

    @Test
    fun statsCardLayout_enumValues_areCorrect() {
        assertEquals(2, StatsCardLayout.entries.size)
        assertTrue(StatsCardLayout.entries.contains(StatsCardLayout.Horizontal))
        assertTrue(StatsCardLayout.entries.contains(StatsCardLayout.Vertical))
    }

    @Test
    fun trendDirection_none_isDefault() {
        assertEquals(TrendDirection.None, TrendDirection.entries[2])
    }

    @Test
    fun statsCardLayout_horizontal_isDefault() {
        assertEquals(StatsCardLayout.Horizontal, StatsCardLayout.entries[0])
    }
}
