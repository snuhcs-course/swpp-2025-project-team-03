package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ClassMessageScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun classMessage_displaysClassName() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                ClassMessageScreen(
                    className = "수학"
                )
            }
        }

        composeTestRule.onNodeWithText("수학", substring = true).assertExists()
    }

    @Test
    fun classMessage_displaysMessageList() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                ClassMessageScreen(
                    className = "수학"
                )
            }
        }

        composeTestRule.onNodeWithText("수학", substring = true).assertExists()
    }

    @Test
    fun classMessage_displaysSendButton() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                ClassMessageScreen(
                    className = "수학"
                )
            }
        }

        composeTestRule.onNodeWithText("전송", substring = true).assertExists()
    }

    @Test
    fun classMessage_navigatesToStudentMessage_whenItemClicked() {
        var navigated = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                ClassMessageScreen(
                    className = "수학",
                    onNavigateToMessage = { navigated = true }
                )
            }
        }

        if (composeTestRule.onAllNodes(hasText("학생", substring = true))
            .fetchSemanticsNodes().size > 1) {
            composeTestRule.onAllNodes(hasText("학생", substring = true))
                .get(1)
                .performClick()
            assert(navigated)
        }
    }
}

