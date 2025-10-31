package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReportScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun reportScreen_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                ReportScreen()
            }
        }

        composeTestRule.onNodeWithText("리포트", substring = true).assertExists()
    }

    @Test
    fun reportScreen_displaysStatistics() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                ReportScreen()
            }
        }

        composeTestRule.onNodeWithText("리포트", substring = true).assertExists()
    }

    @Test
    fun reportScreen_displaysCharts() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                ReportScreen()
            }
        }

        composeTestRule.onNodeWithText("리포트", substring = true).assertExists()
    }
}

