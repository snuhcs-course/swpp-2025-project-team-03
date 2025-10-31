package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditAssignmentScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun editAssignment_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(
                    assignmentTitle = "테스트 과제"
                )
            }
        }

        composeTestRule.onNodeWithText("과제 편집", substring = true).assertExists()
    }

    @Test
    fun editAssignment_displaysTitleField() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(
                    assignmentTitle = "테스트 과제"
                )
            }
        }

        composeTestRule.onAllNodes(hasText("제목", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun editAssignment_displaysDescriptionField() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(
                    assignmentTitle = "테스트 과제"
                )
            }
        }

        composeTestRule.onAllNodes(hasText("설명", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun editAssignment_displaysSaveButton() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(
                    assignmentTitle = "테스트 과제"
                )
            }
        }

        composeTestRule.onNodeWithText("저장", substring = true).assertExists()
    }
}

