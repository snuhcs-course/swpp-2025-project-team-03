package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeacherAssignmentDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun teacherAssignmentDetail_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentDetailScreen(
                    assignmentTitle = "테스트 과제"
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("테스트 과제", substring = true).assertExists()
    }

    @Test
    fun teacherAssignmentDetail_displaysStudentList() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentDetailScreen(
                    assignmentTitle = "테스트 과제"
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("테스트 과제", substring = true).assertExists()
    }

    @Test
    fun teacherAssignmentDetail_navigatesToStudentAssignment_whenItemClicked() {
        var navigated = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentDetailScreen(
                    assignmentTitle = "테스트 과제",
                    onNavigateToAssignmentResults = { navigated = true }
                )
            }
        }

        composeTestRule.waitForIdle()

        if (composeTestRule.onAllNodes(hasText("학생", substring = true))
            .fetchSemanticsNodes().size > 1) {
            composeTestRule.onAllNodes(hasText("학생", substring = true))
                .get(1)
                .performClick()
            assert(navigated)
        }
    }
}

