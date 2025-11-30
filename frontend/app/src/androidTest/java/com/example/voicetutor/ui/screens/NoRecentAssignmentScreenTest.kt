package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.data.repository.AssignmentRepository
import com.example.voicetutor.data.repository.AuthRepository
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NoRecentAssignmentScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val fakeApiService = FakeApiService()
    private val assignmentRepository = AssignmentRepository(fakeApiService)
    private val authRepository = AuthRepository(fakeApiService)
    private val assignmentViewModel = AssignmentViewModel(assignmentRepository)
    private val authViewModel = AuthViewModel(authRepository)

    @Test
    fun noRecentAssignmentScreen_displaysMessage() {
        composeRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen(
                    viewModel = assignmentViewModel,
                    authViewModel = authViewModel,
                )
            }
        }

        composeRule.onNodeWithText("이어할 과제가 없습니다", substring = true).assertIsDisplayed()
    }

    @Test
    fun noRecentAssignmentScreen_displaysEmptyState() {
        composeRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen(
                    viewModel = assignmentViewModel,
                    authViewModel = authViewModel,
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("이어할 과제가 없습니다", substring = true).assertIsDisplayed()
    }

    @Test
    fun noRecentAssignmentScreen_rendersCorrectly() {
        composeRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen(
                    viewModel = assignmentViewModel,
                    authViewModel = authViewModel,
                )
            }
        }

        composeRule.waitForIdle()
    }

    @Test
    fun noRecentAssignmentScreen_displaysContent() {
        composeRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen(
                    viewModel = assignmentViewModel,
                    authViewModel = authViewModel,
                )
            }
        }

        composeRule.onNodeWithText("이어할 과제가 없습니다", substring = true).assertIsDisplayed()
    }

    @Test
    fun noRecentAssignmentScreen_displaysCorrectly() {
        composeRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen(
                    viewModel = assignmentViewModel,
                    authViewModel = authViewModel,
                )
            }
        }

        composeRule.waitForIdle()
        composeRule.onNodeWithText("이어할 과제가 없습니다", substring = true).assertIsDisplayed()
    }
}
