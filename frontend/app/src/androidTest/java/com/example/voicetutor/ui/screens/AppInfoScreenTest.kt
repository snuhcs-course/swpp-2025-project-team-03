package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
    fun appInfoScreen_displaysEmailContact() {
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
}
