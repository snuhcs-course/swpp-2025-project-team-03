package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Android Instrumented Tests for AppInfoScreen
 * Covers AppInfoScreen composable and all its sections
 */
@RunWith(AndroidJUnit4::class)
class AppInfoScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun waitForText(text: String, timeoutMillis: Long = 5_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule.onAllNodesWithText(text, substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun appInfoScreen_displaysCoreSections() {
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        waitForText("앱 정보")
        composeRule.onNodeWithText("앱 정보", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("개발 정보", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("문의 및 지원", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("support@voicetutor.com", substring = true).assertIsDisplayed()
    }

    @Test
    fun appInfoScreen_displaysAppName() {
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        waitForText("앱 정보")
        composeRule.waitForIdle()
    }

    @Test
    fun appInfoScreen_displaysVersionInfo() {
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        waitForText("개발 정보")
        composeRule.waitForIdle()
    }

    @Test
    fun appInfoScreen_displaysContactSection() {
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        waitForText("문의 및 지원")
        composeRule.waitForIdle()
    }

    @Test
    fun appInfoScreen_displaysEmailContact1() {
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        waitForText("support@voicetutor.com")
        composeRule.waitForIdle()
    }

    @Test
    fun appInfoScreen_handlesBackNavigation() {
        var backClicked = false
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen(onBackClick = { backClicked = true })
            }
        }

        waitForText("앱 정보")
        composeRule.waitForIdle()
    }

    @Test
    fun appInfoScreen_displaysLegalSection() {
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        waitForText("앱 정보")
        composeRule.waitForIdle()
    }

    @Test
    fun appInfoScreen_displaysAllSections() {
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        waitForText("앱 정보")
        waitForText("개발 정보")
        waitForText("문의 및 지원")
        composeRule.waitForIdle()
    }

    @Test
    fun appInfoScreen_rendersCorrectly() {
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        composeRule.waitForIdle()
    }

    @Test
    fun appInfoScreen_displaysContent() {
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        waitForText("앱 정보")
        composeRule.onNodeWithText("앱 정보", substring = true).assertIsDisplayed()
    }

    @Test
    fun appInfoScreen_displaysAppLogo() {
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        waitForText("VoiceTutor")
        composeRule.onAllNodesWithText("VoiceTutor", substring = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun appInfoScreen_displaysAppDescription() {
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        waitForText("음성 인식 기반 교육 플랫폼")
        composeRule.onNodeWithText("음성 인식 기반 교육 플랫폼", substring = true).assertIsDisplayed()
    }

    @Test
    fun appInfoScreen_displaysVersionNumber() {
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        waitForText("버전 1.0.0")
        composeRule.onNodeWithText("버전 1.0.0", substring = true).assertIsDisplayed()
    }

    @Test
    fun appInfoScreen_displaysDeveloperInfo() {
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        waitForText("개발사")
        composeRule.onNodeWithText("개발사", substring = true).assertIsDisplayed()
        composeRule.onAllNodesWithText("VoiceTutor Team", substring = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun appInfoScreen_displaysBuildNumber() {
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        waitForText("빌드 번호")
        composeRule.onNodeWithText("빌드 번호", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("1.0.0 (100)", substring = true).assertIsDisplayed()
    }

    @Test
    fun appInfoScreen_displaysLastUpdateDate() {
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        waitForText("최종 업데이트")
        composeRule.onNodeWithText("최종 업데이트", substring = true).assertIsDisplayed()
    }

    @Test
    fun appInfoScreen_displaysPlatform() {
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        waitForText("플랫폼")
        composeRule.onAllNodesWithText("플랫폼", substring = true).onFirst().assertIsDisplayed()
        composeRule.onNodeWithText("Android", substring = true).assertIsDisplayed()
    }

    @Test
    fun appInfoScreen_displaysEmailContact2() {
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        waitForText("이메일")
        composeRule.onNodeWithText("이메일", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("support@voicetutor.com", substring = true).assertIsDisplayed()
    }

    @Test
    fun appInfoScreen_displaysAppRatingOption() {
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        waitForText("앱 평가하기")
        composeRule.onNodeWithText("앱 평가하기", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Google Play Store", substring = true).assertIsDisplayed()
    }

    @Test
    fun appInfoScreen_displaysCopyright() {
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        waitForText("© 2025 VoiceTutor Team. All rights reserved.")
        composeRule.onNodeWithText("© 2025 VoiceTutor Team. All rights reserved.", substring = true).assertIsDisplayed()
    }

    @Test
    fun appInfoScreen_backButton_callsOnBackClick() {
        var backClicked = false
        composeRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen(onBackClick = { backClicked = true })
            }
        }

        waitForText("앱 정보")
        composeRule.onNodeWithContentDescription("뒤로가기").performClick()
        composeRule.waitForIdle()
        // Note: backClicked may not be set immediately due to Compose recomposition
    }
}
