package com.example.voicetutor.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.data.repository.AssignmentRepository
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import java.util.concurrent.atomic.AtomicBoolean
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AssignmentDetailScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun waitForText(text: String, timeoutMillis: Long = 10_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule
                .onAllNodesWithText(text, substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun assignmentDetailScreen_displaysAssignmentInformation_andHandlesStart() {
        val fakeApi = FakeApiService()
        val assignmentRepository = AssignmentRepository(fakeApi)
        val assignmentViewModel = AssignmentViewModel(assignmentRepository)
        val personalAssignmentId = fakeApi.personalAssignmentData.id
        val assignmentId = fakeApi.personalAssignmentData.assignment.id
        val assignmentTitle = fakeApi.personalAssignmentData.assignment.title
        val startClicked = AtomicBoolean(false)

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailScreen(
                    assignmentId = personalAssignmentId,
                    assignmentTitle = assignmentTitle,
                    onStartAssignment = { startClicked.set(true) },
                    assignmentViewModelParam = assignmentViewModel
                )
            }
        }

        composeRule.runOnIdle {
            assignmentViewModel.setSelectedAssignmentIds(assignmentId, personalAssignmentId)
        }

        waitForText("진행 현황")
        waitForText("과제 내용")

        composeRule.onNodeWithText(assignmentTitle, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("과제 내용", useUnmergedTree = true).assertIsDisplayed()

        composeRule.onNodeWithText("과제 시작", useUnmergedTree = true).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) { startClicked.get() }
    }

    @Test
    fun assignmentDetailScreen_errorState_displaysErrorMessage() {
        val failingApi = FakeApiService().apply {
            shouldFailGetAssignmentById = true
            getAssignmentByIdErrorMessage = "서버 오류"
        }
        val assignmentRepository = AssignmentRepository(failingApi)
        val assignmentViewModel = AssignmentViewModel(assignmentRepository)

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailScreen(
                    assignmentId = 999,
                    assignmentTitle = "에러 과제",
                    assignmentViewModelParam = assignmentViewModel
                )
            }
        }

        composeRule.runOnIdle {
            assignmentViewModel.loadAssignmentById(999)
        }

        waitForText("서버 오류")
        composeRule.onNodeWithText("서버 오류", useUnmergedTree = true).assertIsDisplayed()
    }
}

