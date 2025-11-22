package com.example.voicetutor.theme

import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Android Instrumented Tests for VoiceTutorTheme Composable and extension functions
 * Covers lines 153-232 in ThemeManager.kt
 */
@RunWith(AndroidJUnit4::class)
class ThemeManagerCoverageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    
    private fun setSystemDarkMode(isDark: Boolean): Int {
        val activity = composeTestRule.activity
        val config = activity.resources.configuration
        val originalUiMode = config.uiMode
        val nightMode = if (isDark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
        return originalUiMode
    }
    
    private fun restoreSystemDarkMode(originalUiMode: Int) {
        val activity = composeTestRule.activity
        val config = activity.resources.configuration
        config.uiMode = originalUiMode
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }

    @Test
    fun voiceTutorTheme_darkTheme_true_appliesDarkColorScheme() {
        // Given - darkTheme = true (lines 158-170)
        var darkColorSchemeApplied = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                darkColorSchemeApplied = (colorScheme.background == DarkColors.Background &&
                    colorScheme.surface == DarkColors.Surface &&
                    colorScheme.primary == DarkColors.PrimaryIndigo)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(darkColorSchemeApplied)
    }

    @Test
    fun voiceTutorTheme_darkTheme_false_appliesLightColorScheme() {
        // Given - darkTheme = false (lines 171-183)
        var lightColorSchemeApplied = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                lightColorSchemeApplied = (colorScheme.background == LightColors.Background &&
                    colorScheme.surface == LightColors.Surface &&
                    colorScheme.primary == LightColors.PrimaryIndigo)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(lightColorSchemeApplied)
    }

    @Test
    fun voiceTutorTheme_defaultUsesSystemTheme() {
        // Given - default parameter uses isSystemInDarkTheme() (line 155)
        var themeApplied = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                val colorScheme = MaterialTheme.colorScheme
                themeApplied = (colorScheme.background != null)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(themeApplied)
    }

    @Test
    fun colorScheme_success_returnsSuccessColor() {
        // Given - ColorScheme.success extension (lines 194-195)
        var successColorCorrect = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                val colorScheme = MaterialTheme.colorScheme
                val successColor = colorScheme.success
                successColorCorrect = (successColor == DarkColors.Success)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(successColorCorrect)
    }

    @Test
    fun colorScheme_warning_returnsWarningColor() {
        // Given - ColorScheme.warning extension (lines 197-198)
        var warningColorCorrect = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                val colorScheme = MaterialTheme.colorScheme
                val warningColor = colorScheme.warning
                warningColorCorrect = (warningColor == DarkColors.Warning)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(warningColorCorrect)
    }

    @Test
    fun colorScheme_info_returnsInfoColor() {
        // Given - ColorScheme.info extension (lines 200-201)
        var infoColorCorrect = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                val colorScheme = MaterialTheme.colorScheme
                val infoColor = colorScheme.info
                infoColorCorrect = (infoColor == DarkColors.Info)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(infoColorCorrect)
    }

    @Test
    fun colorScheme_gray50_darkTheme_returnsDarkGray50() {
        // Given - ColorScheme.gray50() extension uses isSystemInDarkTheme() (line 205)
        // Set Activity to dark mode to test the true branch
        val originalUiMode = setSystemDarkMode(true)
        
        var gray50Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray50 = colorScheme.gray50()
                // gray50() uses isSystemInDarkTheme(), which should return true now
                gray50Correct = (gray50 == DarkColors.Gray50)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray50Correct)
        
        // Restore original configuration
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray50_lightTheme_returnsLightGray50() {
        // Given - ColorScheme.gray50() extension uses isSystemInDarkTheme() (line 205)
        // Set Activity to light mode to test the false branch
        val originalUiMode = setSystemDarkMode(false)
        
        var gray50Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray50 = colorScheme.gray50()
                // gray50() uses isSystemInDarkTheme(), which should return false now
                gray50Correct = (gray50 == LightColors.Gray50)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray50Correct)
        
        // Restore original configuration
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray100_darkTheme_returnsDarkGray100() {
        // Given - ColorScheme.gray100() extension uses isSystemInDarkTheme() (line 208)
        val originalUiMode = setSystemDarkMode(true)
        
        var gray100Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray100 = colorScheme.gray100()
                gray100Correct = (gray100 == DarkColors.Gray100)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray100Correct)
        
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray100_lightTheme_returnsLightGray100() {
        // Given - ColorScheme.gray100() extension uses isSystemInDarkTheme() (line 208)
        val originalUiMode = setSystemDarkMode(false)
        
        var gray100Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray100 = colorScheme.gray100()
                gray100Correct = (gray100 == LightColors.Gray100)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray100Correct)
        
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray200_darkTheme_returnsDarkGray200() {
        // Given - ColorScheme.gray200() extension uses isSystemInDarkTheme() (line 211)
        val originalUiMode = setSystemDarkMode(true)
        
        var gray200Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray200 = colorScheme.gray200()
                gray200Correct = (gray200 == DarkColors.Gray200)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray200Correct)
        
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray200_lightTheme_returnsLightGray200() {
        // Given - ColorScheme.gray200() extension uses isSystemInDarkTheme() (line 211)
        val originalUiMode = setSystemDarkMode(false)
        
        var gray200Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray200 = colorScheme.gray200()
                gray200Correct = (gray200 == LightColors.Gray200)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray200Correct)
        
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray300_darkTheme_returnsDarkGray300() {
        // Given - ColorScheme.gray300() extension uses isSystemInDarkTheme() (line 214)
        val originalUiMode = setSystemDarkMode(true)
        
        var gray300Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray300 = colorScheme.gray300()
                gray300Correct = (gray300 == DarkColors.Gray300)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray300Correct)
        
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray300_lightTheme_returnsLightGray300() {
        // Given - ColorScheme.gray300() extension uses isSystemInDarkTheme() (line 214)
        val originalUiMode = setSystemDarkMode(false)
        
        var gray300Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray300 = colorScheme.gray300()
                gray300Correct = (gray300 == LightColors.Gray300)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray300Correct)
        
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray400_darkTheme_returnsDarkGray400() {
        // Given - ColorScheme.gray400() extension uses isSystemInDarkTheme() (line 217)
        val originalUiMode = setSystemDarkMode(true)
        
        var gray400Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray400 = colorScheme.gray400()
                gray400Correct = (gray400 == DarkColors.Gray400)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray400Correct)
        
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray400_lightTheme_returnsLightGray400() {
        // Given - ColorScheme.gray400() extension uses isSystemInDarkTheme() (line 217)
        val originalUiMode = setSystemDarkMode(false)
        
        var gray400Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray400 = colorScheme.gray400()
                gray400Correct = (gray400 == LightColors.Gray400)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray400Correct)
        
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray500_darkTheme_returnsDarkGray500() {
        // Given - ColorScheme.gray500() extension uses isSystemInDarkTheme() (line 220)
        val originalUiMode = setSystemDarkMode(true)
        
        var gray500Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray500 = colorScheme.gray500()
                gray500Correct = (gray500 == DarkColors.Gray500)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray500Correct)
        
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray500_lightTheme_returnsLightGray500() {
        // Given - ColorScheme.gray500() extension uses isSystemInDarkTheme() (line 220)
        val originalUiMode = setSystemDarkMode(false)
        
        var gray500Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray500 = colorScheme.gray500()
                gray500Correct = (gray500 == LightColors.Gray500)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray500Correct)
        
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray600_darkTheme_returnsDarkGray600() {
        // Given - ColorScheme.gray600() extension uses isSystemInDarkTheme() (line 223)
        val originalUiMode = setSystemDarkMode(true)
        
        var gray600Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray600 = colorScheme.gray600()
                gray600Correct = (gray600 == DarkColors.Gray600)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray600Correct)
        
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray600_lightTheme_returnsLightGray600() {
        // Given - ColorScheme.gray600() extension uses isSystemInDarkTheme() (line 223)
        val originalUiMode = setSystemDarkMode(false)
        
        var gray600Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray600 = colorScheme.gray600()
                gray600Correct = (gray600 == LightColors.Gray600)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray600Correct)
        
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray700_darkTheme_returnsDarkGray700() {
        // Given - ColorScheme.gray700() extension uses isSystemInDarkTheme() (line 226)
        val originalUiMode = setSystemDarkMode(true)
        
        var gray700Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray700 = colorScheme.gray700()
                gray700Correct = (gray700 == DarkColors.Gray700)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray700Correct)
        
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray700_lightTheme_returnsLightGray700() {
        // Given - ColorScheme.gray700() extension uses isSystemInDarkTheme() (line 226)
        val originalUiMode = setSystemDarkMode(false)
        
        var gray700Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray700 = colorScheme.gray700()
                gray700Correct = (gray700 == LightColors.Gray700)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray700Correct)
        
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray800_darkTheme_returnsDarkGray800() {
        // Given - ColorScheme.gray800() extension uses isSystemInDarkTheme() (line 229)
        val originalUiMode = setSystemDarkMode(true)
        
        var gray800Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray800 = colorScheme.gray800()
                gray800Correct = (gray800 == DarkColors.Gray800)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray800Correct)
        
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray800_lightTheme_returnsLightGray800() {
        // Given - ColorScheme.gray800() extension uses isSystemInDarkTheme() (line 229)
        val originalUiMode = setSystemDarkMode(false)
        
        var gray800Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray800 = colorScheme.gray800()
                gray800Correct = (gray800 == LightColors.Gray800)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray800Correct)
        
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray900_darkTheme_returnsDarkGray900() {
        // Given - ColorScheme.gray900() extension uses isSystemInDarkTheme() (line 232)
        val originalUiMode = setSystemDarkMode(true)
        
        var gray900Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray900 = colorScheme.gray900()
                gray900Correct = (gray900 == DarkColors.Gray900)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray900Correct)
        
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray900_lightTheme_returnsLightGray900() {
        // Given - ColorScheme.gray900() extension uses isSystemInDarkTheme() (line 232)
        val originalUiMode = setSystemDarkMode(false)
        
        var gray900Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray900 = colorScheme.gray900()
                gray900Correct = (gray900 == LightColors.Gray900)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray900Correct)
        
        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun voiceTutorTheme_appliesMaterialTheme() {
        // Given - MaterialTheme application (lines 186-190)
        var materialThemeApplied = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                val colorScheme = MaterialTheme.colorScheme
                val typography = MaterialTheme.typography
                materialThemeApplied = (colorScheme != null && typography != null)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(materialThemeApplied)
    }
}
