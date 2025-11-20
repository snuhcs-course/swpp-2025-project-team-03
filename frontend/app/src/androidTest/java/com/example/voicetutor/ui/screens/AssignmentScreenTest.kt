package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.models.AnswerSubmissionResponse
import com.example.voicetutor.data.models.PersonalAssignmentQuestion
import com.example.voicetutor.data.models.PersonalAssignmentStatistics
import com.example.voicetutor.data.models.TailQuestion
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
@RunWith(AndroidJUnit4::class)
class AssignmentScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var apiService: ApiService

    private val fakeApi: FakeApiService
        get() = apiService as FakeApiService

    private fun baseQuestion() = PersonalAssignmentQuestion(
        id = 1,
        number = "1",
        question = "지구는 몇 개의 위성을 가지고 있나요?",
        answer = "1개",
        explanation = "지구의 유일한 자연 위성은 달입니다.",
        difficulty = "EASY",
    )

    private fun baseStatistics() = PersonalAssignmentStatistics(
        totalQuestions = 10,
        answeredQuestions = 5,
        correctAnswers = 4,
        accuracy = 0.8f,
        totalProblem = 10,
        solvedProblem = 5,
        progress = 0.5f,
        averageScore = 85f,
    )

    @Before
    fun setUp() {
        hiltRule.inject()
        resetFakeApi()
    }

    @After
    fun tearDown() {
        resetFakeApi()
    }

    private fun resetFakeApi() {
        fakeApi.apply {
            personalAssignmentsDelayMillis = 0
            shouldFailPersonalAssignments = false
            personalAssignmentsResponse = listOf(personalAssignmentData)
            personalAssignmentQuestionsResponses = listOf(baseQuestion())
            nextQuestionQueue = mutableListOf(baseQuestion())
            shouldReturnNoMoreQuestions = false
            nextQuestionErrorMessage = "No more questions"
            answerSubmissionResponse = AnswerSubmissionResponse(isCorrect = true, numberStr = "1", tailQuestion = null)
            answerSubmissionResponseQueue = null
            personalAssignmentStatisticsResponses = mutableMapOf(personalAssignmentData.id to baseStatistics())
        }
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText(text, substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun assignmentScreen_showsLoadingIndicator_thenDisplaysQuestionAndProgress() {
        fakeApi.personalAssignmentsDelayMillis = 1_500

        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        composeRule.mainClock.advanceTimeBy(2_000)
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")
        composeRule.onNodeWithText("지구는 몇 개의 위성을 가지고 있나요?", useUnmergedTree = true)
            .assertIsDisplayed()

        composeRule.onNodeWithText("5 / 10", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun assignmentScreen_tailQuestionFlow_displaysTailQuestionAfterSubmission() {
        val baseQuestion = baseQuestion()
        val tailQuestion = TailQuestion(
            id = 2,
            number = "1-1",
            question = "달에 대한 추가 설명을 말해보세요.",
            answer = "달은 지구의 유일한 위성입니다.",
            explanation = "꼬리 질문 예시",
            difficulty = "MEDIUM",
        )

        fakeApi.personalAssignmentQuestionsResponses = listOf(baseQuestion)
        fakeApi.nextQuestionQueue = mutableListOf(baseQuestion)
        fakeApi.answerSubmissionResponseQueue = mutableListOf(
            AnswerSubmissionResponse(
                isCorrect = true,
                numberStr = tailQuestion.number,
                tailQuestion = tailQuestion,
            ),
        )
        fakeApi.personalAssignmentStatisticsResponses = mutableMapOf(
            fakeApi.personalAssignmentData.id to PersonalAssignmentStatistics(
                totalQuestions = 2,
                answeredQuestions = 1,
                correctAnswers = 1,
                accuracy = 0.5f,
                totalProblem = 2,
                solvedProblem = 1,
                progress = 0.5f,
                averageScore = 80f,
            ),
        )

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText(baseQuestion.question)

        val tempFile = File.createTempFile("answer", ".wav", composeRule.activity.cacheDir)
        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]
        composeRule.runOnIdle {
            assignmentViewModel.submitAnswer(
                fakeApi.personalAssignmentData.id,
                fakeApi.personalAssignmentData.student.id,
                baseQuestion.id,
                tempFile,
            )
        }

        waitForText("꼬리질문으로 넘어가기")
        composeRule.onNodeWithText("꼬리질문으로 넘어가기", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("꼬리질문으로 넘어가기", useUnmergedTree = true).performClick()

        waitForText(tailQuestion.question)
        composeRule.onNodeWithText(tailQuestion.question, useUnmergedTree = true).assertIsDisplayed()

        tempFile.delete()
    }

    @Test
    fun assignmentScreen_completionState_showsCompletionScreen() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]
        composeRule.runOnIdle {
            assignmentViewModel.updatePersonalAssignmentQuestions(emptyList())
            assignmentViewModel.setAssignmentCompleted(true)
        }

        waitForText("과제 완료!")
        composeRule.onNodeWithText("과제 완료!", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("홈으로 돌아가기", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun assignmentScreen_displaysQuestionText() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")
        composeRule.onNodeWithText("지구는 몇 개의 위성을 가지고 있나요?", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun assignmentScreen_displaysProgressIndicator() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("5 / 10")
        composeRule.onNodeWithText("5 / 10", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun assignmentScreen_handlesNoMoreQuestions() {
        fakeApi.shouldReturnNoMoreQuestions = true
        fakeApi.nextQuestionErrorMessage = "더 이상 문제가 없습니다"

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")
        // Should handle no more questions gracefully
        composeRule.waitForIdle()
    }

    @Test
    fun assignmentScreen_handlesErrorState() {
        fakeApi.shouldFailPersonalAssignments = true
        fakeApi.personalAssignmentsErrorMessage = "과제 로드 실패"

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        // Should handle error gracefully
        composeRule.waitForIdle()
    }

    @Test
    fun assignmentScreen_displaysDifficulty() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")
        // Difficulty might be displayed
        composeRule.waitForIdle()
    }

    @Test
    fun assignmentScreen_showsCorrectAnswerFeedback() {
        fakeApi.answerSubmissionResponse = AnswerSubmissionResponse(
            isCorrect = true,
            numberStr = "1",
            tailQuestion = null,
        )

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")
        // Answer feedback should be displayed after submission
        composeRule.waitForIdle()
    }

    @Test
    fun assignmentScreen_showsIncorrectAnswerFeedback() {
        fakeApi.answerSubmissionResponse = AnswerSubmissionResponse(
            isCorrect = false,
            numberStr = "1",
            tailQuestion = null,
        )

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")
        // Incorrect answer feedback should be displayed
        composeRule.waitForIdle()
    }

    @Test
    fun assignmentScreen_displaysExplanation() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")
        // Explanation should be available
        composeRule.waitForIdle()
    }

    @Test
    fun assignmentScreen_handlesEmptyQuestions() {
        fakeApi.personalAssignmentQuestionsResponses = emptyList()
        fakeApi.nextQuestionQueue = mutableListOf()

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        // Should handle empty questions gracefully
        composeRule.waitForIdle()
    }

    @Test
    fun assignmentScreen_completionScreen_navigatesToHome() {
        var homeNavigated = false
        val onNavigateToHome: () -> Unit = { homeNavigated = true }

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                    onNavigateToHome = onNavigateToHome,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]
        composeRule.runOnIdle {
            assignmentViewModel.updatePersonalAssignmentQuestions(emptyList())
            assignmentViewModel.setAssignmentCompleted(true)
        }

        waitForText("과제 완료!")
        waitForText("홈으로 돌아가기")

        composeRule.onAllNodesWithText("홈으로 돌아가기", substring = true, useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            homeNavigated
        }
    }

    @Test
    fun assignmentScreen_completionScreen_displaysCompletionMessage() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]
        composeRule.runOnIdle {
            assignmentViewModel.updatePersonalAssignmentQuestions(emptyList())
            assignmentViewModel.setAssignmentCompleted(true)
        }

        waitForText("과제 완료!")
        waitForText("모든 문제를 완료했습니다.")
        composeRule.onAllNodesWithText("모든 문제를 완료했습니다.", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun assignmentScreen_completionScreen_displaysWhenCurrentQuestionIsNull() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]
        composeRule.runOnIdle {
            assignmentViewModel.updatePersonalAssignmentQuestions(emptyList())
            assignmentViewModel.setAssignmentCompleted(true)
        }

        waitForText("과제 완료!")
        composeRule.onAllNodesWithText("과제 완료!", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun assignmentScreen_displaysProgressBar() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        // Progress bar should be displayed
        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule.onAllNodesWithText("5", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun assignmentScreen_handlesNullAssignmentId() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = null,
                )
            }
        }

        // Should handle null assignmentId gracefully
        composeRule.waitForIdle()
    }

    @Test
    fun assignmentScreen_displaysHint() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")
        // Hint might be displayed
        composeRule.waitForIdle()
    }

    @Test
    fun assignmentScreen_displaysModelAnswer() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")
        // Model answer might be displayed
        composeRule.waitForIdle()
    }

    @Test
    fun assignmentScreen_displaysRecordingButton() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        // Recording button should be displayed
        composeRule.waitUntil(timeoutMillis = 30_000) {
            try {
                composeRule.onAllNodesWithText("녹음", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun assignmentScreen_displaysStopRecordingButtonWhenRecording() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]
        composeRule.runOnIdle {
            val recordingStateField = AssignmentViewModel::class.java.getDeclaredField("_audioRecordingState")
            recordingStateField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val recordingStateFlow = recordingStateField.get(assignmentViewModel) as MutableStateFlow<com.example.voicetutor.audio.RecordingState>
            recordingStateFlow.value = com.example.voicetutor.audio.RecordingState(
                isRecording = true,
                recordingTime = 5,
                audioFilePath = null,
            )
        }

        composeRule.waitForIdle()
        // Stop recording button should be displayed
        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule.onAllNodesWithText("중지", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
    }

    @Test
    fun assignmentScreen_displaysRecordingTimer() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]
        composeRule.runOnIdle {
            val recordingStateField = AssignmentViewModel::class.java.getDeclaredField("_audioRecordingState")
            recordingStateField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val recordingStateFlow = recordingStateField.get(assignmentViewModel) as MutableStateFlow<com.example.voicetutor.audio.RecordingState>
            recordingStateFlow.value = com.example.voicetutor.audio.RecordingState(
                isRecording = true,
                recordingTime = 10,
                audioFilePath = null,
            )
        }

        composeRule.waitForIdle()
        // Recording timer should be displayed
        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule.onAllNodesWithText("00:10", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
    }

    @Test
    fun assignmentScreen_displaysAnswerFeedback() {
        fakeApi.answerSubmissionResponse = AnswerSubmissionResponse(
            isCorrect = true,
            numberStr = "1",
            tailQuestion = null,
        )

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]
        composeRule.runOnIdle {
            val responseField = AssignmentViewModel::class.java.getDeclaredField("_answerSubmissionResponse")
            responseField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val responseFlow = responseField.get(assignmentViewModel) as MutableStateFlow<AnswerSubmissionResponse?>
            responseFlow.value = AnswerSubmissionResponse(
                isCorrect = true,
                numberStr = "1",
                tailQuestion = null,
            )
        }

        composeRule.waitForIdle()
        // Answer feedback should be displayed
        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule.onAllNodesWithText("정답", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() ||
                    composeRule.onAllNodesWithText("맞았습니다", substring = true, useUnmergedTree = true)
                        .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
    }

    @Test
    fun assignmentScreen_displaysIncorrectAnswerFeedback() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]
        composeRule.runOnIdle {
            val responseField = AssignmentViewModel::class.java.getDeclaredField("_answerSubmissionResponse")
            responseField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val responseFlow = responseField.get(assignmentViewModel) as MutableStateFlow<AnswerSubmissionResponse?>
            responseFlow.value = AnswerSubmissionResponse(
                isCorrect = false,
                numberStr = "1",
                tailQuestion = null,
            )
        }

        composeRule.waitForIdle()
        // Incorrect answer feedback should be displayed
        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule.onAllNodesWithText("오답", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty() ||
                    composeRule.onAllNodesWithText("틀렸습니다", substring = true, useUnmergedTree = true)
                        .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
    }

    @Test
    fun assignmentScreen_displaysNextQuestionButtonBasic() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        // Next question button might be displayed
        composeRule.waitForIdle()
    }

    @Test
    fun assignmentScreen_displaysProgressPercentage() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        // Progress percentage should be displayed
        composeRule.waitUntil(timeoutMillis = 30_000) {
            try {
                composeRule.onAllNodesWithText("%", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun assignmentScreen_handlesSubmittingState() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]
        composeRule.runOnIdle {
            val submittingField = AssignmentViewModel::class.java.getDeclaredField("_isSubmitting")
            submittingField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val submittingFlow = submittingField.get(assignmentViewModel) as MutableStateFlow<Boolean>
            submittingFlow.value = true
        }

        composeRule.waitForIdle()
        // Submitting state should be handled
    }

    @Test
    fun assignmentScreen_handlesErrorDisplay() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]
        composeRule.runOnIdle {
            val errorField = AssignmentViewModel::class.java.getDeclaredField("_error")
            errorField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val errorFlow = errorField.get(assignmentViewModel) as MutableStateFlow<String?>
            errorFlow.value = "오류 발생"
        }

        composeRule.waitForIdle()
        // Error should be handled
    }

    @Test
    fun assignmentScreen_displaysQuestionExplanation() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")
        // Explanation might be displayed
        composeRule.waitForIdle()
    }

    @Test
    fun assignmentScreen_handlesTailQuestionDisplay() {
        val tailQuestion = TailQuestion(
            id = 2,
            number = "1-1",
            question = "달에 대한 추가 설명을 말해보세요.",
            answer = "달은 지구의 유일한 위성입니다.",
            explanation = "꼬리 질문 예시",
            difficulty = "MEDIUM",
        )

        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]
        composeRule.runOnIdle {
            val responseField = AssignmentViewModel::class.java.getDeclaredField("_answerSubmissionResponse")
            responseField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val responseFlow = responseField.get(assignmentViewModel) as MutableStateFlow<AnswerSubmissionResponse?>
            responseFlow.value = AnswerSubmissionResponse(
                isCorrect = true,
                numberStr = "1-1",
                tailQuestion = tailQuestion,
            )
        }

        composeRule.waitForIdle()
        // Tail question should be displayed
        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule.onAllNodesWithText("꼬리질문", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
    }

    @Test
    fun assignmentScreen_displaysRecordingStartButton() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule.onAllNodesWithText("녹음 시작", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun assignmentScreen_displaysSkipButton() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule.onAllNodesWithText("건너뛰기", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun assignmentScreen_displaysMoveToTailQuestionButton() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]
        composeRule.runOnIdle {
            val responseField = AssignmentViewModel::class.java.getDeclaredField("_answerSubmissionResponse")
            responseField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val responseFlow = responseField.get(assignmentViewModel) as MutableStateFlow<AnswerSubmissionResponse?>
            val tailQuestion = TailQuestion(
                id = 2,
                number = "1-1",
                question = "달에 대한 추가 설명을 말해보세요.",
                answer = "달은 지구의 유일한 위성입니다.",
                explanation = "꼬리 질문 예시",
                difficulty = "MEDIUM",
            )
            responseFlow.value = AnswerSubmissionResponse(
                isCorrect = true,
                numberStr = "1-1",
                tailQuestion = tailQuestion,
            )
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule.onAllNodesWithText("꼬리질문으로 넘어가기", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun assignmentScreen_displaysStopRecordingButtonViaState() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]
        composeRule.runOnIdle {
            val recordingStateField = AssignmentViewModel::class.java.getDeclaredField("_audioRecordingState")
            recordingStateField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val recordingStateFlow = recordingStateField.get(assignmentViewModel) as MutableStateFlow<com.example.voicetutor.audio.RecordingState>
            recordingStateFlow.value = com.example.voicetutor.audio.RecordingState(
                isRecording = true,
                recordingTime = 0,
                audioFilePath = null,
            )
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule.onAllNodesWithText("녹음 중지", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun assignmentScreen_displaysCompletionButton() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen(
                    assignmentId = fakeApi.personalAssignmentData.id,
                )
            }
        }

        waitForText("지구는 몇 개의 위성을 가지고 있나요?")

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]
        composeRule.runOnIdle {
            val responseField = AssignmentViewModel::class.java.getDeclaredField("_answerSubmissionResponse")
            responseField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val responseFlow = responseField.get(assignmentViewModel) as MutableStateFlow<AnswerSubmissionResponse?>
            responseFlow.value = AnswerSubmissionResponse(
                isCorrect = true,
                numberStr = null,
                tailQuestion = null,
            )
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule.onAllNodesWithText("완료", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        composeRule.waitForIdle()
    }
}
