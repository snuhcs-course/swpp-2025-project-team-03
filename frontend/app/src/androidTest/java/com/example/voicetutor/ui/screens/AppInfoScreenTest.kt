package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppInfoScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun appInfo_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        composeTestRule.onNodeWithText("앱 정보", substring = true).assertExists()
    }

    @Test
    fun appInfo_displaysVersionInfo() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        composeTestRule.onNodeWithText("버전", substring = true).assertExists()
    }

    @Test
    fun appInfo_displaysAppName() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }

        composeTestRule.onNodeWithText("VoiceTutor", substring = true).assertExists()
    }

    @Test
    fun appInfo_callsOnBack_whenBackClicked() {
        var backClicked = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen(
                    onBackClick = { backClicked = true }
                )
            }
        }

        composeTestRule.onAllNodes(hasContentDescription("뒤로가기"))
            .onFirst()
            .performClick()
        assert(backClicked)
    }
}

