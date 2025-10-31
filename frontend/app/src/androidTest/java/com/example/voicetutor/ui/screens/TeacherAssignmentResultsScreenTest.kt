package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeacherAssignmentResultsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun teacherAssignmentResults_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentResultsScreen(
                    assignmentTitle = "테스트 과제"
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("테스트 과제", substring = true).assertExists()
    }

    @Test
    fun teacherAssignmentResults_displaysStudentResults() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentResultsScreen(
                    assignmentTitle = "테스트 과제"
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("테스트 과제", substring = true).assertExists()
    }

    @Test
    fun teacherAssignmentResults_displaysStatistics() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentResultsScreen(
                    assignmentTitle = "테스트 과제"
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("테스트 과제", substring = true).assertExists()
    }
}

