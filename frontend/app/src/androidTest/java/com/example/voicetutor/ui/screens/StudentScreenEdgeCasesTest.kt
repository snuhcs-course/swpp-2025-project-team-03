package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.data.repository.AssignmentRepository
import com.example.voicetutor.data.repository.AuthRepository
import com.example.voicetutor.data.repository.DashboardRepository
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import com.example.voicetutor.ui.viewmodel.DashboardViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudentScreenEdgeCasesTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun studentDashboard_emptyAssignments_showsPlaceholder() {
        val fakeApi = FakeApiService().apply {
            personalAssignmentsResponse = emptyList()
        }
        val assignmentViewModel = AssignmentViewModel(AssignmentRepository(fakeApi))
        val authViewModel = AuthViewModel(AuthRepository(fakeApi))
        val dashboardViewModel = DashboardViewModel(DashboardRepository(fakeApi))

        composeRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreen(
                    authViewModel = authViewModel,
                    assignmentViewModel = assignmentViewModel,
                    dashboardViewModel = dashboardViewModel,
                )
            }
        }

        composeRule.runOnIdle {
            authViewModel.login("student@voicetutor.com", "student123")
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(hasText("과제가 없습니다", substring = true), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onAllNodesWithText("과제가 없습니다", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun studentDashboard_errorState_recoversAfterRetry() {
        val fakeApi = FakeApiService().apply {
            shouldFailPersonalAssignments = true
            personalAssignmentsErrorMessage = "네트워크 오류"
        }

        val assignmentViewModel = AssignmentViewModel(AssignmentRepository(fakeApi))
        val authViewModel = AuthViewModel(AuthRepository(fakeApi))
        val dashboardViewModel = DashboardViewModel(DashboardRepository(fakeApi))

        composeRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreen(
                    authViewModel = authViewModel,
                    assignmentViewModel = assignmentViewModel,
                    dashboardViewModel = dashboardViewModel,
                )
            }
        }

        composeRule.runOnIdle {
            authViewModel.login("student@voicetutor.com", "student123")
        }

        // Initially fails and shows empty state placeholder
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(hasText("과제가 없습니다", substring = true), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        // Recover by turning off failure and re-triggering load
        composeRule.runOnIdle {
            fakeApi.shouldFailPersonalAssignments = false
            assignmentViewModel.loadPendingStudentAssignments(authViewModel.currentUser.value?.id ?: 1)
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(hasText("1단원 복습 과제", substring = true), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onAllNodesWithText("1단원 복습 과제", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun assignmentDetailedResultsScreen_rendersTailQuestions() {
        val fakeApi = FakeApiService()
        val assignmentViewModel = AssignmentViewModel(AssignmentRepository(fakeApi))

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailedResultsScreen(
                    personalAssignmentId = 1,
                    assignmentTitle = "상세 결과 테스트",
                    viewModel = assignmentViewModel,
                )
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            var hasData = false
            composeRule.runOnIdle {
                hasData = assignmentViewModel.assignmentCorrectness.value.isNotEmpty()
            }
            hasData
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(hasText("태양이 도는 은하의 이름은?", substring = true), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onAllNodesWithText("태양이 도는 은하의 이름은?", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("은하수", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }
}
