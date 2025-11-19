package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.lifecycle.ViewModelProvider
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
class TeacherStudentAssignmentDetailScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var apiService: ApiService

    private val fakeApi: FakeApiService
        get() = apiService as FakeApiService

    private fun defaultPersonalAssignmentStatistics() = PersonalAssignmentStatistics(
        totalQuestions = 10,
        answeredQuestions = 5,
        correctAnswers = 4,
        accuracy = 0.8f,
        totalProblem = 10,
        solvedProblem = 5,
        progress = 0.5f,
        averageScore = 85f,
    )

    private fun defaultAssignmentCorrectness(): List<AssignmentCorrectnessItem> = listOf(
        AssignmentCorrectnessItem(
            questionContent = "태양이 도는 은하의 이름은?",
            questionModelAnswer = "은하수",
            studentAnswer = "은하수",
            isCorrect = true,
            answeredAt = "2024-01-02T10:00:00Z",
            questionNum = "1",
            explanation = "태양계는 은하수 은하에 속해 있습니다.",
        ),
    )

    @Before
    fun setUp() {
        hiltRule.inject()
        resetFakeApi()
    }

    private fun resetFakeApi() {
        fakeApi.apply {
            shouldFailPersonalAssignments = false
            personalAssignmentsResponse = listOf(personalAssignmentData)
            personalAssignmentsDelayMillis = 0
            shouldFailPersonalAssignmentStatistics = false
            personalAssignmentStatisticsResponses = mutableMapOf(
                personalAssignmentData.id to defaultPersonalAssignmentStatistics(),
            )
            shouldFailAssignmentCorrectness = false
            assignmentCorrectnessErrorMessage = "Failed to load assignment correctness"
            assignmentCorrectnessResponses = defaultAssignmentCorrectness()
        }
    }

    private fun waitForText(text: String, timeoutMillis: Long = 15_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule
                .onAllNodesWithText(text, substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_displaysStudentSummary() {
        val studentId = fakeApi.personalAssignmentData.student.id.toString()
        val assignmentId = fakeApi.personalAssignmentData.assignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = studentId,
                    assignmentId = assignmentId,
                    assignmentTitle = fakeApi.personalAssignmentData.assignment.title,
                )
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[com.example.voicetutor.ui.viewmodel.AssignmentViewModel::class.java]

        composeRule.waitUntil(timeoutMillis = 15_000) {
            assignmentViewModel.assignmentResults.value.any { it.name.contains("홍길동") }
        }

        waitForText("홍길동")
        waitForText("문제별 상세 결과")

        composeRule.onAllNodesWithText("평균 점수", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("소요 시간", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_noResults_showsEmptyPlaceholder() {
        fakeApi.personalAssignmentsResponse = emptyList()
        fakeApi.personalAssignmentStatisticsResponses.clear()

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = "1",
                    assignmentId = 1,
                    assignmentTitle = "빈 결과",
                )
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[com.example.voicetutor.ui.viewmodel.AssignmentViewModel::class.java]

        composeRule.waitUntil(timeoutMillis = 10_000) {
            assignmentViewModel.assignmentResults.value.isEmpty()
        }

        waitForText("학생 결과를 찾을 수 없습니다")
        composeRule.onAllNodesWithText("학생 결과를 찾을 수 없습니다", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_statsUnavailable_showsPlaceholderValues() {
        fakeApi.shouldFailPersonalAssignmentStatistics = true
        fakeApi.personalAssignmentStatisticsResponses.clear()

        val studentId = fakeApi.personalAssignmentData.student.id.toString()
        val assignmentId = fakeApi.personalAssignmentData.assignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = studentId,
                    assignmentId = assignmentId,
                    assignmentTitle = fakeApi.personalAssignmentData.assignment.title,
                )
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[com.example.voicetutor.ui.viewmodel.AssignmentViewModel::class.java]

        composeRule.waitUntil(timeoutMillis = 15_000) {
            assignmentViewModel.assignmentResults.value.any { it.studentId == studentId }
        }
        composeRule.waitUntil(timeoutMillis = 5_000) {
            assignmentViewModel.personalAssignmentStatistics.value == null
        }

        composeRule.onAllNodesWithText("정답률", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("평균 점수", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("-", useUnmergedTree = true).assertCountEquals(2)
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_errorLoadingResults_showsEmptyState() {
        fakeApi.shouldFailPersonalAssignments = true
        fakeApi.personalAssignmentsResponse = emptyList()

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = "1",
                    assignmentId = 1,
                    assignmentTitle = "에러 과제",
                )
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[com.example.voicetutor.ui.viewmodel.AssignmentViewModel::class.java]

        composeRule.waitUntil(timeoutMillis = 10_000) {
            assignmentViewModel.assignmentResults.value.isEmpty()
        }

        waitForText("학생 결과를 찾을 수 없습니다")
        composeRule.onAllNodesWithText("학생 결과를 찾을 수 없습니다", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
        composeRule.onAllNodesWithText("문제별 상세 결과", useUnmergedTree = true).assertCountEquals(0)
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_displaysStudentName() {
        val studentId = fakeApi.personalAssignmentData.student.id.toString()
        val assignmentId = fakeApi.personalAssignmentData.assignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = studentId,
                    assignmentId = assignmentId,
                    assignmentTitle = fakeApi.personalAssignmentData.assignment.title,
                )
            }
        }

        val studentViewModel = ViewModelProvider(composeRule.activity)[com.example.voicetutor.ui.viewmodel.StudentViewModel::class.java]

        composeRule.waitUntil(timeoutMillis = 15_000) {
            studentViewModel.currentStudent.value != null
        }

        waitForText("홍길동")
        composeRule.onAllNodesWithText("홍길동", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_displaysStatisticsCards() {
        val studentId = fakeApi.personalAssignmentData.student.id.toString()
        val assignmentId = fakeApi.personalAssignmentData.assignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = studentId,
                    assignmentId = assignmentId,
                    assignmentTitle = fakeApi.personalAssignmentData.assignment.title,
                )
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[com.example.voicetutor.ui.viewmodel.AssignmentViewModel::class.java]

        composeRule.waitUntil(timeoutMillis = 15_000) {
            assignmentViewModel.personalAssignmentStatistics.value != null
        }

        waitForText("정답률")
        composeRule.onAllNodesWithText("정답률", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()

        waitForText("평균 점수")
        composeRule.onAllNodesWithText("평균 점수", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()

        waitForText("소요 시간")
        composeRule.onAllNodesWithText("소요 시간", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_displaysCorrectnessItems() {
        val studentId = fakeApi.personalAssignmentData.student.id.toString()
        val assignmentId = fakeApi.personalAssignmentData.assignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = studentId,
                    assignmentId = assignmentId,
                    assignmentTitle = fakeApi.personalAssignmentData.assignment.title,
                )
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[com.example.voicetutor.ui.viewmodel.AssignmentViewModel::class.java]

        composeRule.waitUntil(timeoutMillis = 15_000) {
            assignmentViewModel.assignmentCorrectness.value.isNotEmpty()
        }

        waitForText("문제별 상세 결과")
        composeRule.onAllNodesWithText("문제별 상세 결과", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_handlesZeroAssignmentId() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = "1",
                    assignmentId = 0,
                    assignmentTitle = "테스트 과제",
                )
            }
        }

        // Should handle zero assignmentId gracefully
        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_handlesNullStatistics() {
        fakeApi.personalAssignmentStatisticsResponses.clear()

        val studentId = fakeApi.personalAssignmentData.student.id.toString()
        val assignmentId = fakeApi.personalAssignmentData.assignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = studentId,
                    assignmentId = assignmentId,
                    assignmentTitle = fakeApi.personalAssignmentData.assignment.title,
                )
            }
        }

        // Should handle null statistics gracefully
        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_displaysCorrectAnswerItems() {
        val studentId = fakeApi.personalAssignmentData.student.id.toString()
        val assignmentId = fakeApi.personalAssignmentData.assignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = studentId,
                    assignmentId = assignmentId,
                    assignmentTitle = fakeApi.personalAssignmentData.assignment.title,
                )
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[com.example.voicetutor.ui.viewmodel.AssignmentViewModel::class.java]

        composeRule.waitUntil(timeoutMillis = 15_000) {
            assignmentViewModel.assignmentCorrectness.value.isNotEmpty()
        }

        waitForText("태양이 도는 은하의 이름은?")
        composeRule.onAllNodesWithText("태양이 도는 은하의 이름은?", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_displaysStudentAnswer() {
        val studentId = fakeApi.personalAssignmentData.student.id.toString()
        val assignmentId = fakeApi.personalAssignmentData.assignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = studentId,
                    assignmentId = assignmentId,
                    assignmentTitle = fakeApi.personalAssignmentData.assignment.title,
                )
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[com.example.voicetutor.ui.viewmodel.AssignmentViewModel::class.java]

        composeRule.waitUntil(timeoutMillis = 15_000) {
            assignmentViewModel.assignmentCorrectness.value.isNotEmpty()
        }

        waitForText("은하수")
        composeRule.onAllNodesWithText("은하수", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_displaysTimeSpent() {
        val studentId = fakeApi.personalAssignmentData.student.id.toString()
        val assignmentId = fakeApi.personalAssignmentData.assignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = studentId,
                    assignmentId = assignmentId,
                    assignmentTitle = fakeApi.personalAssignmentData.assignment.title,
                )
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[com.example.voicetutor.ui.viewmodel.AssignmentViewModel::class.java]

        composeRule.waitUntil(timeoutMillis = 15_000) {
            assignmentViewModel.personalAssignmentStatistics.value != null
        }

        waitForText("소요 시간")
        composeRule.onAllNodesWithText("소요 시간", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_handlesEmptyCorrectnessItems() {
        fakeApi.assignmentCorrectnessResponses = emptyList()

        val studentId = fakeApi.personalAssignmentData.student.id.toString()
        val assignmentId = fakeApi.personalAssignmentData.assignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = studentId,
                    assignmentId = assignmentId,
                    assignmentTitle = fakeApi.personalAssignmentData.assignment.title,
                )
            }
        }

        // Should handle empty correctness items gracefully
        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_displaysExplanation() {
        val studentId = fakeApi.personalAssignmentData.student.id.toString()
        val assignmentId = fakeApi.personalAssignmentData.assignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = studentId,
                    assignmentId = assignmentId,
                    assignmentTitle = fakeApi.personalAssignmentData.assignment.title,
                )
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[com.example.voicetutor.ui.viewmodel.AssignmentViewModel::class.java]

        composeRule.waitUntil(timeoutMillis = 15_000) {
            assignmentViewModel.assignmentCorrectness.value.isNotEmpty()
        }

        waitForText("태양계는 은하수 은하에 속해 있습니다")
        composeRule.onAllNodesWithText("태양계는 은하수 은하에 속해 있습니다", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }
}
