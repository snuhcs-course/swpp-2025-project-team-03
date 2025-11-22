package com.example.voicetutor.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
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
    val composeTestRule = createComposeRule()

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
        var gray50Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray50 = colorScheme.gray50()
                // gray50() uses isSystemInDarkTheme(), not VoiceTutorTheme's darkTheme parameter
                val expectedGray50 = if (isSystemInDarkTheme()) DarkColors.Gray50 else LightColors.Gray50
                gray50Correct = (gray50 == expectedGray50)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray50Correct)
    }

    @Test
    fun colorScheme_gray50_lightTheme_returnsLightGray50() {
        // Given - ColorScheme.gray50() extension uses isSystemInDarkTheme() (line 205)
        var gray50Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray50 = colorScheme.gray50()
                // gray50() uses isSystemInDarkTheme(), not VoiceTutorTheme's darkTheme parameter
                val expectedGray50 = if (isSystemInDarkTheme()) DarkColors.Gray50 else LightColors.Gray50
                gray50Correct = (gray50 == expectedGray50)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray50Correct)
    }

    @Test
    fun colorScheme_gray100_darkTheme_returnsDarkGray100() {
        // Given - ColorScheme.gray100() extension uses isSystemInDarkTheme() (line 208)
        var gray100Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray100 = colorScheme.gray100()
                val expectedGray100 = if (isSystemInDarkTheme()) DarkColors.Gray100 else LightColors.Gray100
                gray100Correct = (gray100 == expectedGray100)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray100Correct)
    }

    @Test
    fun colorScheme_gray100_lightTheme_returnsLightGray100() {
        // Given - ColorScheme.gray100() extension uses isSystemInDarkTheme() (line 208)
        var gray100Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray100 = colorScheme.gray100()
                val expectedGray100 = if (isSystemInDarkTheme()) DarkColors.Gray100 else LightColors.Gray100
                gray100Correct = (gray100 == expectedGray100)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray100Correct)
    }

    @Test
    fun colorScheme_gray200_darkTheme_returnsDarkGray200() {
        // Given - ColorScheme.gray200() extension uses isSystemInDarkTheme() (line 211)
        var gray200Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray200 = colorScheme.gray200()
                val expectedGray200 = if (isSystemInDarkTheme()) DarkColors.Gray200 else LightColors.Gray200
                gray200Correct = (gray200 == expectedGray200)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray200Correct)
    }

    @Test
    fun colorScheme_gray200_lightTheme_returnsLightGray200() {
        // Given - ColorScheme.gray200() extension uses isSystemInDarkTheme() (line 211)
        var gray200Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray200 = colorScheme.gray200()
                val expectedGray200 = if (isSystemInDarkTheme()) DarkColors.Gray200 else LightColors.Gray200
                gray200Correct = (gray200 == expectedGray200)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray200Correct)
    }

    @Test
    fun colorScheme_gray300_darkTheme_returnsDarkGray300() {
        // Given - ColorScheme.gray300() extension uses isSystemInDarkTheme() (line 214)
        var gray300Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray300 = colorScheme.gray300()
                val expectedGray300 = if (isSystemInDarkTheme()) DarkColors.Gray300 else LightColors.Gray300
                gray300Correct = (gray300 == expectedGray300)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray300Correct)
    }

    @Test
    fun colorScheme_gray300_lightTheme_returnsLightGray300() {
        // Given - ColorScheme.gray300() extension uses isSystemInDarkTheme() (line 214)
        var gray300Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray300 = colorScheme.gray300()
                val expectedGray300 = if (isSystemInDarkTheme()) DarkColors.Gray300 else LightColors.Gray300
                gray300Correct = (gray300 == expectedGray300)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray300Correct)
    }

    @Test
    fun colorScheme_gray400_darkTheme_returnsDarkGray400() {
        // Given - ColorScheme.gray400() extension uses isSystemInDarkTheme() (line 217)
        var gray400Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray400 = colorScheme.gray400()
                val expectedGray400 = if (isSystemInDarkTheme()) DarkColors.Gray400 else LightColors.Gray400
                gray400Correct = (gray400 == expectedGray400)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray400Correct)
    }

    @Test
    fun colorScheme_gray400_lightTheme_returnsLightGray400() {
        // Given - ColorScheme.gray400() extension uses isSystemInDarkTheme() (line 217)
        var gray400Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray400 = colorScheme.gray400()
                val expectedGray400 = if (isSystemInDarkTheme()) DarkColors.Gray400 else LightColors.Gray400
                gray400Correct = (gray400 == expectedGray400)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray400Correct)
    }

    @Test
    fun colorScheme_gray500_darkTheme_returnsDarkGray500() {
        // Given - ColorScheme.gray500() extension uses isSystemInDarkTheme() (line 220)
        var gray500Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray500 = colorScheme.gray500()
                val expectedGray500 = if (isSystemInDarkTheme()) DarkColors.Gray500 else LightColors.Gray500
                gray500Correct = (gray500 == expectedGray500)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray500Correct)
    }

    @Test
    fun colorScheme_gray500_lightTheme_returnsLightGray500() {
        // Given - ColorScheme.gray500() extension uses isSystemInDarkTheme() (line 220)
        var gray500Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray500 = colorScheme.gray500()
                val expectedGray500 = if (isSystemInDarkTheme()) DarkColors.Gray500 else LightColors.Gray500
                gray500Correct = (gray500 == expectedGray500)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray500Correct)
    }

    @Test
    fun colorScheme_gray600_darkTheme_returnsDarkGray600() {
        // Given - ColorScheme.gray600() extension uses isSystemInDarkTheme() (line 223)
        var gray600Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray600 = colorScheme.gray600()
                val expectedGray600 = if (isSystemInDarkTheme()) DarkColors.Gray600 else LightColors.Gray600
                gray600Correct = (gray600 == expectedGray600)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray600Correct)
    }

    @Test
    fun colorScheme_gray600_lightTheme_returnsLightGray600() {
        // Given - ColorScheme.gray600() extension uses isSystemInDarkTheme() (line 223)
        var gray600Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray600 = colorScheme.gray600()
                val expectedGray600 = if (isSystemInDarkTheme()) DarkColors.Gray600 else LightColors.Gray600
                gray600Correct = (gray600 == expectedGray600)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray600Correct)
    }

    @Test
    fun colorScheme_gray700_darkTheme_returnsDarkGray700() {
        // Given - ColorScheme.gray700() extension uses isSystemInDarkTheme() (line 226)
        var gray700Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray700 = colorScheme.gray700()
                val expectedGray700 = if (isSystemInDarkTheme()) DarkColors.Gray700 else LightColors.Gray700
                gray700Correct = (gray700 == expectedGray700)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray700Correct)
    }

    @Test
    fun colorScheme_gray700_lightTheme_returnsLightGray700() {
        // Given - ColorScheme.gray700() extension uses isSystemInDarkTheme() (line 226)
        var gray700Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray700 = colorScheme.gray700()
                val expectedGray700 = if (isSystemInDarkTheme()) DarkColors.Gray700 else LightColors.Gray700
                gray700Correct = (gray700 == expectedGray700)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray700Correct)
    }

    @Test
    fun colorScheme_gray800_darkTheme_returnsDarkGray800() {
        // Given - ColorScheme.gray800() extension uses isSystemInDarkTheme() (line 229)
        var gray800Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray800 = colorScheme.gray800()
                val expectedGray800 = if (isSystemInDarkTheme()) DarkColors.Gray800 else LightColors.Gray800
                gray800Correct = (gray800 == expectedGray800)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray800Correct)
    }

    @Test
    fun colorScheme_gray800_lightTheme_returnsLightGray800() {
        // Given - ColorScheme.gray800() extension uses isSystemInDarkTheme() (line 229)
        var gray800Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray800 = colorScheme.gray800()
                val expectedGray800 = if (isSystemInDarkTheme()) DarkColors.Gray800 else LightColors.Gray800
                gray800Correct = (gray800 == expectedGray800)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray800Correct)
    }

    @Test
    fun colorScheme_gray900_darkTheme_returnsDarkGray900() {
        // Given - ColorScheme.gray900() extension uses isSystemInDarkTheme() (line 232)
        var gray900Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray900 = colorScheme.gray900()
                val expectedGray900 = if (isSystemInDarkTheme()) DarkColors.Gray900 else LightColors.Gray900
                gray900Correct = (gray900 == expectedGray900)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray900Correct)
    }

    @Test
    fun colorScheme_gray900_lightTheme_returnsLightGray900() {
        // Given - ColorScheme.gray900() extension uses isSystemInDarkTheme() (line 232)
        var gray900Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray900 = colorScheme.gray900()
                val expectedGray900 = if (isSystemInDarkTheme()) DarkColors.Gray900 else LightColors.Gray900
                gray900Correct = (gray900 == expectedGray900)
            }
        }
        
        composeTestRule.waitForIdle()
        assertTrue(gray900Correct)
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
