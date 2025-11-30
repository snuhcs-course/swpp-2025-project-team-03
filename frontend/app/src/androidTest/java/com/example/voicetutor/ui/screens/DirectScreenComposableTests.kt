package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DirectScreenComposableTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun noRecentAssignmentScreen_renders_completeUI() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("이어할 과제가 없습니다", substring = true).assertExists()
        composeTestRule.onNodeWithText("홈 화면에서 새로운 과제를 확인해보세요", substring = true).assertExists()
    }

    @Test
    fun appInfoScreen_renders_completeUI() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithText("앱", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertExists()
    }

    @Test
    fun appInfoScreen_onBackClick_triggersCallback() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen(onBackClick = {})
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("앱 정보", substring = true).assertExists()
    }
}
