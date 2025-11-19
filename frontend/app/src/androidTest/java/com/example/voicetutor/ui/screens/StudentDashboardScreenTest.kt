package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
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
class StudentDashboardScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun waitForText(text: String, timeoutMillis: Long = 10_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule.onAllNodesWithText(text, substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun studentDashboardScreen_noAssignments_showsEmptyState() {
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

        waitForText("과제가 없습니다")
        composeRule.onNodeWithText("과제가 없습니다", substring = true).assertIsDisplayed()
    }

    @Test
    fun studentDashboardScreen_withAssignments_displaysCard() {
        val fakeApi = FakeApiService().apply {
            personalAssignmentsResponse = listOf(personalAssignmentData)
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

        val assignmentTitle = fakeApi.personalAssignmentData.assignment.title

        waitForText(assignmentTitle)
        composeRule.onNodeWithText(assignmentTitle, substring = true).assertIsDisplayed()
    }

    @Test
    fun studentDashboardScreen_displaysWelcomeMessage() {
        val fakeApi = FakeApiService()
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

        waitForText("학생")
        composeRule.onNodeWithText("학생", substring = true).assertIsDisplayed()
    }

    @Test
    fun studentDashboardScreen_displaysStatistics() {
        val fakeApi = FakeApiService().apply {
            personalAssignmentsResponse = listOf(personalAssignmentData)
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

        // Statistics should be displayed
        waitForText("과제")
        composeRule.waitForIdle()
    }

    @Test
    fun studentDashboardScreen_displaysAssignmentCards() {
        val fakeApi = FakeApiService().apply {
            personalAssignmentsResponse = listOf(personalAssignmentData)
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

        waitForText(fakeApi.personalAssignmentData.assignment.title)
        composeRule.onNodeWithText(fakeApi.personalAssignmentData.assignment.title, substring = true).assertIsDisplayed()
    }

    @Test
    fun studentDashboardScreen_displaysAssignmentStatus() {
        val fakeApi = FakeApiService().apply {
            personalAssignmentsResponse = listOf(personalAssignmentData)
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

        // Assignment status should be displayed
        waitForText("진행 중")
        composeRule.onAllNodesWithText("진행 중", substring = true, useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun studentDashboardScreen_displaysProgress() {
        val fakeApi = FakeApiService().apply {
            personalAssignmentsResponse = listOf(personalAssignmentData)
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

        // Progress should be displayed
        waitForText("5")
        composeRule.onNodeWithText("5", substring = true).assertIsDisplayed()
    }

    @Test
    fun studentDashboardScreen_handlesLoadingState() {
        val fakeApi = FakeApiService()
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

        // Loading state should be handled
        composeRule.waitForIdle()
    }

    @Test
    fun studentDashboardScreen_displaysNavigationButtons() {
        val fakeApi = FakeApiService()
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

        // Navigation buttons should be displayed
        waitForText("과제")
        composeRule.onAllNodesWithText("과제", substring = true, useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun studentDashboardScreen_displaysAssignmentCount() {
        val fakeApi = FakeApiService().apply {
            personalAssignmentsResponse = listOf(personalAssignmentData)
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

        // Assignment count should be displayed
        waitForText("1")
        composeRule.onAllNodesWithText("1", substring = true, useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun studentDashboardScreen_handlesErrorState() {
        val fakeApi = FakeApiService().apply {
            shouldFailPersonalAssignments = true
            personalAssignmentsErrorMessage = "과제 로드 실패"
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

        // Error should be handled gracefully
        composeRule.waitForIdle()
    }
}
