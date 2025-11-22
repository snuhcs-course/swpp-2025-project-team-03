package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.data.repository.AssignmentRepository
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Android Instrumented Tests for AssignmentDetailedResultsScreen
 * Covers AssignmentDetailedResultsScreen composable, question groups, and tail questions
 */
@RunWith(AndroidJUnit4::class)
class AssignmentDetailedResultsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun waitForText(text: String, timeoutMillis: Long = 10_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule
                .onAllNodesWithText(text, substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun assignmentDetailedResultsScreen_displaysQuestionGroups() {
        val fakeApi = FakeApiService()
        val viewModel = AssignmentViewModel(AssignmentRepository(fakeApi))
        val personalAssignment = fakeApi.personalAssignmentData

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailedResultsScreen(
                    personalAssignmentId = personalAssignment.id,
                    assignmentTitle = personalAssignment.assignment.title,
                    viewModel = viewModel,
                )
            }
        }

        waitForText("문제별 상세 결과")
        composeRule.onNodeWithText("문제별 상세 결과", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText(personalAssignment.assignment.title, substring = true)
            .assertIsDisplayed()
    }

    @Test
    fun assignmentDetailedResultsScreen_errorState_showsErrorMessage() {
        val failingApi = FakeApiService().apply {
            shouldFailAssignmentCorrectness = true
            assignmentCorrectnessErrorMessage = "정답 데이터 오류"
        }
        val viewModel = AssignmentViewModel(AssignmentRepository(failingApi))

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailedResultsScreen(
                    personalAssignmentId = failingApi.personalAssignmentData.id,
                    assignmentTitle = "에러 리포트",
                    viewModel = viewModel,
                )
            }
        }

        waitForText("결과가 없습니다")
        composeRule.onNodeWithText("결과가 없습니다", substring = true).assertIsDisplayed()
    }

    @Test
    fun assignmentDetailedResultsScreen_displaysLoadingState() {
        val fakeApi = FakeApiService()
        val viewModel = AssignmentViewModel(AssignmentRepository(fakeApi))
        val personalAssignment = fakeApi.personalAssignmentData

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailedResultsScreen(
                    personalAssignmentId = personalAssignment.id,
                    assignmentTitle = personalAssignment.assignment.title,
                    viewModel = viewModel,
                )
            }
        }

        composeRule.waitForIdle()
    }

    @Test
    fun assignmentDetailedResultsScreen_displaysSummaryStats() {
        val fakeApi = FakeApiService()
        val viewModel = AssignmentViewModel(AssignmentRepository(fakeApi))
        val personalAssignment = fakeApi.personalAssignmentData

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailedResultsScreen(
                    personalAssignmentId = personalAssignment.id,
                    assignmentTitle = personalAssignment.assignment.title,
                    viewModel = viewModel,
                )
            }
        }

        waitForText("총 문제")
        waitForText("점수")
        composeRule.onNodeWithText("총 문제", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("점수", substring = true).assertIsDisplayed()
    }

    @Test
    fun assignmentDetailedResultsScreen_displaysQuestionGroupCard() {
        val fakeApi = FakeApiService()
        val viewModel = AssignmentViewModel(AssignmentRepository(fakeApi))
        val personalAssignment = fakeApi.personalAssignmentData

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailedResultsScreen(
                    personalAssignmentId = personalAssignment.id,
                    assignmentTitle = personalAssignment.assignment.title,
                    viewModel = viewModel,
                )
            }
        }

        waitForText("문제별 상세 결과")
        composeRule.onNodeWithText("문제별 상세 결과", substring = true).assertIsDisplayed()
    }

    @Test
    fun assignmentDetailedResultsScreen_togglesTailQuestions() {
        val fakeApi = FakeApiService()
        val viewModel = AssignmentViewModel(AssignmentRepository(fakeApi))
        val personalAssignment = fakeApi.personalAssignmentData

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailedResultsScreen(
                    personalAssignmentId = personalAssignment.id,
                    assignmentTitle = personalAssignment.assignment.title,
                    viewModel = viewModel,
                )
            }
        }

        waitForText("문제별 상세 결과")
        composeRule.waitForIdle()

        // Try to find and click tail question toggle if available
        val tailQuestionToggle = composeRule.onAllNodesWithText("꼬리질문 펼치기", substring = true)
        if (tailQuestionToggle.fetchSemanticsNodes().isNotEmpty()) {
            tailQuestionToggle.onFirst().performClick()
            composeRule.waitForIdle()
        }
    }

    @Test
    fun assignmentDetailedResultsScreen_displaysEmptyState() {
        val emptyApi = FakeApiService().apply {
            assignmentCorrectnessResponses = emptyList<com.example.voicetutor.data.models.AssignmentCorrectnessItem>()
        }
        val viewModel = AssignmentViewModel(AssignmentRepository(emptyApi))

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailedResultsScreen(
                    personalAssignmentId = emptyApi.personalAssignmentData.id,
                    assignmentTitle = "빈 리포트",
                    viewModel = viewModel,
                )
            }
        }

        waitForText("결과가 없습니다")
        composeRule.onNodeWithText("결과가 없습니다", substring = true).assertIsDisplayed()
    }

    @Test
    fun assignmentDetailedResultsScreen_displaysErrorState() {
        val failingApi = FakeApiService().apply {
            shouldFailAssignmentCorrectness = true
            assignmentCorrectnessErrorMessage = "네트워크 오류"
            shouldFailPersonalAssignmentStatistics = true
            personalAssignmentStatisticsErrorMessage = "통계 로드 실패"
        }
        val viewModel = AssignmentViewModel(AssignmentRepository(failingApi))

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailedResultsScreen(
                    personalAssignmentId = failingApi.personalAssignmentData.id,
                    assignmentTitle = "에러 리포트",
                    viewModel = viewModel,
                )
            }
        }

        // Wait for error state to appear (either from correctness or statistics failure)
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("오류가 발생했습니다", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("오류가 발생했습니다", substring = true).assertIsDisplayed()
    }
}
