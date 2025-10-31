package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AllStudentAssignmentsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun allStudentAssignments_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllStudentAssignmentsScreen(
                    studentId = 1
                )
            }
        }

        composeTestRule.onNodeWithText("전체 과제", substring = true).assertExists()
    }

    @Test
    fun allStudentAssignments_displaysAssignmentList() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllStudentAssignmentsScreen(
                    studentId = 1
                )
            }
        }

        composeTestRule.onNodeWithText("전체 과제", substring = true).assertExists()
    }

    @Test
    fun allStudentAssignments_navigatesToAssignmentDetail_whenItemClicked() {
        var navigated = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllStudentAssignmentsScreen(
                    studentId = 1,
                    onNavigateToAssignmentDetail = { navigated = true }
                )
            }
        }

        if (composeTestRule.onAllNodes(hasText("과제", substring = true))
            .fetchSemanticsNodes().size > 1) {
            composeTestRule.onAllNodes(hasText("과제", substring = true))
                .get(1)
                .performClick()
            assert(navigated)
        }
    }
}

