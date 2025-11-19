package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoRecentAssignmentScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun noRecentAssignmentScreen_displaysMessage() {
        composeRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen()
            }
        }

        composeRule.onNodeWithText("이어할 과제가 없습니다", substring = true).assertIsDisplayed()
    }

    @Test
    fun noRecentAssignmentScreen_displaysEmptyState() {
        composeRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen()
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("이어할 과제가 없습니다", substring = true).assertIsDisplayed()
    }

    @Test
    fun noRecentAssignmentScreen_rendersCorrectly() {
        composeRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen()
            }
        }

        composeRule.waitForIdle()
    }

    @Test
    fun noRecentAssignmentScreen_displaysContent() {
        composeRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen()
            }
        }

        composeRule.onNodeWithText("이어할 과제가 없습니다", substring = true).assertIsDisplayed()
    }

    @Test
    fun noRecentAssignmentScreen_displaysCorrectly() {
        composeRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen()
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("이어할 과제가 없습니다", substring = true).assertIsDisplayed()
    }
}
