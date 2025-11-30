package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.models.AssignmentCorrectnessItem
import com.example.voicetutor.data.models.PersonalAssignmentStatistics
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
@RunWith(AndroidJUnit4::class)
class AssignmentDetailedResultsScreenHighCoverageTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var apiService: ApiService

    private val fakeApi: FakeApiService
        get() = apiService as FakeApiService

    @Before
    fun setUp() {
        hiltRule.inject()

        fakeApi.apply {
            personalAssignmentStatisticsResponses[1] = PersonalAssignmentStatistics(
                totalQuestions = 2,
                answeredQuestions = 2,
                correctAnswers = 1,
                accuracy = 50f,
                totalProblem = 2,
                solvedProblem = 2,
                progress = 100f,
                averageScore = 75f,
            )

            assignmentCorrectnessResponses = listOf(

                AssignmentCorrectnessItem(
                    questionNum = "1",
                    questionContent = "메인 질문 1",
                    studentAnswer = "정답",
                    questionModelAnswer = "정답",
                    isCorrect = true,
                    explanation = "해설 1",
                    answeredAt = "2024-01-01",
                ),

                AssignmentCorrectnessItem(
                    questionNum = "2",
                    questionContent = "메인 질문 2",
                    studentAnswer = "오답",
                    questionModelAnswer = "정답",
                    isCorrect = false,
                    explanation = "해설 2",
                    answeredAt = "2024-01-01",
                ),

                AssignmentCorrectnessItem(
                    questionNum = "2-1",
                    questionContent = "꼬리 질문 2-1",
                    studentAnswer = "꼬리 정답",
                    questionModelAnswer = "꼬리 정답",
                    isCorrect = true,
                    explanation = "꼬리 해설 2-1",
                    answeredAt = "2024-01-01",
                ),

                AssignmentCorrectnessItem(
                    questionNum = "2-2",
                    questionContent = "꼬리 질문 2-2",
                    studentAnswer = "꼬리 오답",
                    questionModelAnswer = "꼬리 정답",
                    isCorrect = false,
                    explanation = "꼬리 해설 2-2",
                    answeredAt = "2024-01-01",
                ),
            )
        }
    }

    private fun waitForText(text: String, timeoutMillis: Long = 5_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule.onAllNodesWithText(text, substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun testTailQuestionToggleAndContent() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailedResultsScreen(personalAssignmentId = 1)
            }
        }

        waitForText("메인 질문 1")

        composeRule.onNodeWithText("메인 질문 1")
            .assertIsDisplayed()

        composeRule.onNodeWithText("메인 질문 2").performScrollTo()
        composeRule.waitForIdle()

        val toggleButton = composeRule.onNodeWithText("꼬리질문 펼치기")
        toggleButton.assertIsDisplayed()

        toggleButton.performClick()
        composeRule.waitForIdle()

        waitForText("꼬리 질문 2-1")

        composeRule.onNodeWithText("꼬리 질문 2-1").performScrollTo()
        composeRule.onNodeWithText("꼬리 질문 2-1").assertIsDisplayed()

        waitForText("꼬리 정답")

        composeRule.onAllNodesWithText("꼬리 정답", useUnmergedTree = true).onFirst().performScrollTo()
        composeRule.onAllNodesWithText("꼬리 정답", useUnmergedTree = true).onFirst().assertIsDisplayed()

        composeRule.onNodeWithText("꼬리 질문 2-2").performScrollTo()
        composeRule.onNodeWithText("꼬리 질문 2-2").assertIsDisplayed()
        waitForText("꼬리 오답")
        composeRule.onNodeWithText("꼬리 오답").performScrollTo()
        composeRule.onNodeWithText("꼬리 오답").assertIsDisplayed()

        composeRule.onAllNodesWithText("꼬리질문 접기")
            .onLast()
            .performClick()

        composeRule.waitForIdle()
    }

    @Test
    fun testDetailedQuestionResultCardContent() {
        composeRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailedResultsScreen(personalAssignmentId = 1)
            }
        }

        waitForText("메인 질문 1")

        composeRule.onNodeWithText("메인 질문 1").assertIsDisplayed()

        composeRule.onAllNodesWithText("내 답변").onFirst().assertIsDisplayed()

        composeRule.onAllNodesWithText("해설").onFirst().assertIsDisplayed()
        composeRule.onNodeWithText("해설 1").assertIsDisplayed()

        composeRule.onNodeWithText("메인 질문 2").performScrollTo()

        composeRule.onNodeWithText("오답").assertIsDisplayed()

        composeRule.onAllNodesWithText("내 답변").onLast().assertIsDisplayed()
        composeRule.onNodeWithText("오답").assertIsDisplayed()

        composeRule.onNodeWithText("해설 2").assertIsDisplayed()
    }
}
