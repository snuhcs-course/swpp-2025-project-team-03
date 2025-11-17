package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.data.repository.AssignmentRepository
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
                    viewModel = viewModel
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
                    viewModel = viewModel
                )
            }
        }

        waitForText("결과가 없습니다")
        composeRule.onNodeWithText("결과가 없습니다", substring = true).assertIsDisplayed()
    }
}

