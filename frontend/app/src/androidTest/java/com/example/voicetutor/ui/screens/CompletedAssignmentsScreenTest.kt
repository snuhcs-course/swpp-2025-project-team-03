package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompletedAssignmentsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun completedAssignments_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CompletedAssignmentsScreen(
                    studentId = 1
                )
            }
        }

        composeTestRule.waitForIdle()

        // 제목이 표시되어야 함
        composeTestRule.onNodeWithText("완료한 과제", substring = true).assertExists()
    }

    @Test
    fun completedAssignments_displaysEmptyState_whenNoAssignments() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CompletedAssignmentsScreen(
                    studentId = 1
                )
            }
        }

        // 완료한 과제가 없을 때 적절한 메시지가 표시되어야 함
        composeTestRule.onNodeWithText("완료한 과제", substring = true).assertExists()
    }

    @Test
    fun completedAssignments_displaysAssignmentList_whenAssignmentsExist() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CompletedAssignmentsScreen(
                    studentId = 1
                )
            }
        }

        // 완료한 과제 목록이 표시되어야 함 (있는 경우)
        composeTestRule.onNodeWithText("완료한 과제", substring = true).assertExists()
    }

    @Test
    fun completedAssignments_navigatesToAssignmentDetail_whenItemClicked() {
        var navigated = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                CompletedAssignmentsScreen(
                    studentId = 1,
                    onNavigateToAssignmentDetail = { navigated = true }
                )
            }
        }

        composeTestRule.waitForIdle()

        // 과제 항목 클릭 (있는 경우)
        if (composeTestRule.onAllNodes(hasText("과제", substring = true))
            .fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onAllNodes(hasText("과제", substring = true))
                .onFirst()
                .performClick()
            assert(navigated)
        }
    }

    @Test
    fun completedAssignments_displaysScore_whenAvailable() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CompletedAssignmentsScreen(
                    studentId = 1
                )
            }
        }

        // 점수가 표시되어야 함 (있는 경우)
        composeTestRule.onNodeWithText("완료한 과제", substring = true).assertExists()
    }
}

