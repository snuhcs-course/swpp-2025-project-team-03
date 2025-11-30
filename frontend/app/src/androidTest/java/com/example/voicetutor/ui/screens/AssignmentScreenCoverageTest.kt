package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.models.PersonalAssignmentQuestion
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
@RunWith(AndroidJUnit4::class)
class AssignmentScreenCoverageTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var apiService: ApiService

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun <T> setStateFlow(viewModel: AssignmentViewModel, fieldName: String, value: T) {
        val field = AssignmentViewModel::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as MutableStateFlow<T>
        stateFlow.value = value
    }

    @Test
    fun testProcessingStateUI() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(assignmentId = 1)
            }
        }

        val viewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]

        composeRule.runOnIdle {
            setStateFlow(viewModel, "_isProcessing", true)
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("채점 중입니다. 잠시 대기하세요.").assertIsDisplayed()
    }

    @Test
    fun testRecordingCompletedUI() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(assignmentId = 1)
            }
        }

        val viewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]

        val completedState = viewModel.audioRecordingState.value.copy(
            isRecording = false,
            isRecordingComplete = true,
            audioFilePath = "/dummy/path/audio.wav",
            recordingTime = 10,
        )

        composeRule.runOnIdle {
            setStateFlow(viewModel, "_audioRecordingState", completedState)
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("녹음 완료 (00:10)").assertIsDisplayed()

        composeRule.onNodeWithText("다시 듣기").assertIsDisplayed()

        composeRule.onNodeWithContentDescription("음성 재생").assertIsDisplayed()
    }

    @Test
    fun testRecordingButtonsLogic() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(assignmentId = 1)
            }
        }

        val viewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]

        val recordingState = viewModel.audioRecordingState.value.copy(
            isRecording = true,
            audioFilePath = null,
        )
        composeRule.runOnIdle {
            setStateFlow(viewModel, "_audioRecordingState", recordingState)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("녹음 중지").assertIsDisplayed()

        val completedState = viewModel.audioRecordingState.value.copy(
            isRecording = false,
            audioFilePath = "/dummy/path.wav",
        )
        composeRule.runOnIdle {
            setStateFlow(viewModel, "_audioRecordingState", completedState)
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithText("다시 녹음하기").assertIsDisplayed()

        composeRule.onNodeWithText("다시 녹음하기").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("녹음 시작").assertIsDisplayed()
    }

    @Test
    fun testSubmitAnswerButton() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(assignmentId = 1)
            }
        }

        val viewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]

        val readyToSubmitState = viewModel.audioRecordingState.value.copy(
            isRecording = false,
            audioFilePath = "/dummy/path.wav",
        )

        val questions = listOf(
            PersonalAssignmentQuestion(
                id = 1,
                number = "1",
                question = "Q1",
                answer = "A1",
                explanation = "Exp1",
                difficulty = "Easy",
            ),
        )
        composeRule.runOnIdle {
            setStateFlow(viewModel, "_audioRecordingState", readyToSubmitState)
            setStateFlow(viewModel, "_personalAssignmentQuestions", questions)
        }

        composeRule.waitForIdle()

        composeRule.onNodeWithText("음성 답안 제출하기").assertIsDisplayed()
        composeRule.onNodeWithText("음성 답안 제출하기").assertIsEnabled()

        composeRule.onNodeWithText("음성 답안 제출하기").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun testSkipButton() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(assignmentId = 1)
            }
        }

        composeRule.onNodeWithText("건너뛰기").assertIsDisplayed()

        composeRule.onNodeWithText("건너뛰기").performClick()
        composeRule.waitForIdle()
    }
}
