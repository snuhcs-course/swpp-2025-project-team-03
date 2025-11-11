package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Direct tests for Screen composables that don't require ViewModel.
 * These tests directly call the Screen composables to maximize coverage.
 */
@RunWith(AndroidJUnit4::class)
class DirectScreenTests {

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
        
        // Verify all UI elements
        composeTestRule.onNodeWithText("이어할 과제가 없습니다", substring = true).assertExists()
        composeTestRule.onNodeWithText("홈 화면에서 새로운 과제를 확인해보세요", substring = true).assertExists()
    }

    @Test
    fun assignmentReportCard_renders_withData() {
        val testAssignment = AssignmentData(
            id = 1,
            title = "테스트 과제",
            description = "설명",
            totalQuestions = 5,
            dueAt = "2024-12-31T23:59:59Z",
            visibleFrom = "2024-01-01T00:00:00Z",
            createdAt = "2024-01-01T00:00:00Z",
            courseClass = CourseClass(
                id = 1,
                name = "1반",
                description = null,
                subject = Subject(id = 1, name = "수학", code = "MATH"),
                teacherName = "선생님",
                startDate = "2024-01-01T00:00:00Z",
                endDate = "2024-12-31T23:59:59Z",
                studentCount = 30,
                createdAt = "2024-01-01T00:00:00Z"
            ),
            grade = "1",
            materials = null
        )

        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentReportCard(
                    assignment = testAssignment,
                    onReportClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("테스트 과제", substring = true).assertExists()
    }
}

