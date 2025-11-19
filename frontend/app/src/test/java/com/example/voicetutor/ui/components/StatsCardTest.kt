package com.example.voicetutor.ui.components

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for StatsCard enums.
 */
class StatsCardTest {

    @Test
    fun trendDirection_enumValues_areCorrect() {
        assertEquals(3, TrendDirection.values().size)
        assertTrue(TrendDirection.values().contains(TrendDirection.Up))
        assertTrue(TrendDirection.values().contains(TrendDirection.Down))
        assertTrue(TrendDirection.values().contains(TrendDirection.None))
    }

    @Test
    fun statsCardLayout_enumValues_areCorrect() {
        assertEquals(2, StatsCardLayout.values().size)
        assertTrue(StatsCardLayout.values().contains(StatsCardLayout.Horizontal))
        assertTrue(StatsCardLayout.values().contains(StatsCardLayout.Vertical))
    }

    @Test
    fun trendDirection_none_isDefault() {
        assertEquals(TrendDirection.None, TrendDirection.values()[2])
    }

    @Test
    fun statsCardLayout_horizontal_isDefault() {
        assertEquals(StatsCardLayout.Horizontal, StatsCardLayout.values()[0])
    }
}
