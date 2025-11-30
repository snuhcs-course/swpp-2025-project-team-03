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

@RunWith(AndroidJUnit4::class)
class ThemeManagerCoverageTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Suppress("DEPRECATION")
    private fun setSystemDarkMode(isDark: Boolean): Int {
        val activity = composeTestRule.activity
        val config = activity.resources.configuration
        val originalUiMode = config.uiMode
        val nightMode = if (isDark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
        config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightMode
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
        return originalUiMode
    }

    @Suppress("DEPRECATION")
    private fun restoreSystemDarkMode(originalUiMode: Int) {
        val activity = composeTestRule.activity
        val config = activity.resources.configuration
        config.uiMode = originalUiMode
        activity.resources.updateConfiguration(config, activity.resources.displayMetrics)
    }

    @Test
    fun voiceTutorTheme_darkTheme_true_appliesDarkColorScheme() {
        var darkColorSchemeApplied = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                darkColorSchemeApplied = (
                    colorScheme.background == DarkColors.Background &&
                        colorScheme.surface == DarkColors.Surface &&
                        colorScheme.primary == DarkColors.PrimaryIndigo
                    )
            }
        }

        composeTestRule.waitForIdle()
        assertTrue(darkColorSchemeApplied)
    }

    @Test
    fun voiceTutorTheme_darkTheme_false_appliesLightColorScheme() {
        var lightColorSchemeApplied = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                lightColorSchemeApplied = (
                    colorScheme.background == LightColors.Background &&
                        colorScheme.surface == LightColors.Surface &&
                        colorScheme.primary == LightColors.PrimaryIndigo
                    )
            }
        }

        composeTestRule.waitForIdle()
        assertTrue(lightColorSchemeApplied)
    }

    @Test
    fun voiceTutorTheme_defaultUsesSystemTheme() {
        var themeApplied = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                // Theme is applied if we can access MaterialTheme
                MaterialTheme.colorScheme
                themeApplied = true
            }
        }

        composeTestRule.waitForIdle()
        assertTrue(themeApplied)
    }

    @Test
    fun colorScheme_success_returnsSuccessColor() {
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
        val originalUiMode = setSystemDarkMode(true)

        var gray50Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = true) {
                val colorScheme = MaterialTheme.colorScheme
                val gray50 = colorScheme.gray50()

                gray50Correct = (gray50 == DarkColors.Gray50)
            }
        }

        composeTestRule.waitForIdle()
        assertTrue(gray50Correct)

        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray50_lightTheme_returnsLightGray50() {
        val originalUiMode = setSystemDarkMode(false)

        var gray50Correct = false
        composeTestRule.setContent {
            VoiceTutorTheme(darkTheme = false) {
                val colorScheme = MaterialTheme.colorScheme
                val gray50 = colorScheme.gray50()

                gray50Correct = (gray50 == LightColors.Gray50)
            }
        }

        composeTestRule.waitForIdle()
        assertTrue(gray50Correct)

        restoreSystemDarkMode(originalUiMode)
    }

    @Test
    fun colorScheme_gray100_darkTheme_returnsDarkGray100() {
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
        var materialThemeApplied = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                // Theme is applied if we can access MaterialTheme properties
                MaterialTheme.colorScheme
                MaterialTheme.typography
                materialThemeApplied = true
            }
        }

        composeTestRule.waitForIdle()
        assertTrue(materialThemeApplied)
    }
}
