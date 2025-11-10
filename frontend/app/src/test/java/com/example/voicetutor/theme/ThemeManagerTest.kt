package com.example.voicetutor.theme

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for ThemeManager enums and data classes.
 */
class ThemeManagerTest {

    @Test
    fun appTheme_enumValues_areCorrect() {
        assertEquals(3, AppTheme.values().size)
        assertTrue(AppTheme.values().contains(AppTheme.LIGHT))
        assertTrue(AppTheme.values().contains(AppTheme.DARK))
        assertTrue(AppTheme.values().contains(AppTheme.AUTO))
    }

    @Test
    fun themeState_creation_withAllFields_createsCorrectInstance() {
        val themeState = ThemeState(
            currentTheme = AppTheme.DARK,
            isDarkMode = true
        )
        
        assertEquals(AppTheme.DARK, themeState.currentTheme)
        assertTrue(themeState.isDarkMode)
    }

    @Test
    fun themeState_creation_withDefaults_usesDefaults() {
        val themeState = ThemeState()
        
        assertEquals(AppTheme.LIGHT, themeState.currentTheme)
        assertFalse(themeState.isDarkMode)
    }

    @Test
    fun appTheme_allThemes_haveUniqueNames() {
        val names = AppTheme.values().map { it.name }
        assertEquals(names.size, names.distinct().size)
    }
}

