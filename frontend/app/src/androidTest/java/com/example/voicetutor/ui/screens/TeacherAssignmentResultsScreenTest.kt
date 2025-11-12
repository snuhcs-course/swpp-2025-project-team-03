package com.example.voicetutor.ui.screens

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.AssignmentData
import com.example.voicetutor.data.models.AssignmentResultData
import com.example.voicetutor.data.models.CourseClass
import com.example.voicetutor.data.models.Material
import com.example.voicetutor.data.models.PersonalAssignmentData
import com.example.voicetutor.data.models.PersonalAssignmentInfo
import com.example.voicetutor.data.models.PersonalAssignmentStatistics
import com.example.voicetutor.data.models.PersonalAssignmentStatus
import com.example.voicetutor.data.models.StudentInfo
import com.example.voicetutor.data.models.Subject
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.data.repository.AssignmentRepository
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import java.util.Locale
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeacherAssignmentResultsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun teacherAssignmentResultsScreen_displaysStudentListAndBottomSheet() {
        val assignmentId = 77
        val assignmentData = createAssignmentData(
            id = assignmentId,
            title = "듣기 평가 과제"
        )

        val submittedAssignment = createPersonalAssignmentData(
            id = 301,
            assignmentId = assignmentId,
            status = PersonalAssignmentStatus.SUBMITTED,
            studentName = "홍길동",
            startedAt = "2024-01-02T09:00:00Z",
            submittedAt = "2024-01-02T10:00:00Z"
        )

        val fakeApi = FakeApiService().apply {
            assignmentByIdResponse = assignmentData
            assignmentsResponse = listOf(assignmentData)
            personalAssignmentsResponse = listOf(submittedAssignment)
            personalAssignmentStatisticsResponses = mutableMapOf(
                submittedAssignment.id to createStatistics(averageScore = 92f, accuracy = 0.92f)
            )
            assignmentResultResponse = AssignmentResultData(
                submittedStudents = 1,
                totalStudents = 1,
                averageScore = 92.0,
                completionRate = 1.0
            )
        }

        val viewModel = AssignmentViewModel(AssignmentRepository(fakeApi))

        setScreenContent(viewModel, assignmentId)

        waitForText("학생별 결과")
        waitForText("홍길동")

        composeTestRule.onNodeWithText("홍길동", useUnmergedTree = true).performClick()

        waitForText("학생 ID: 301")
        composeTestRule.onNodeWithText("학생 ID: 301", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("점수", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun teacherAssignmentResultsScreen_withEmptyResults_showsEmptyPlaceholder() {
        val assignmentId = 88
        val assignmentData = createAssignmentData(id = assignmentId, title = "빈 결과 과제")

        val fakeApi = FakeApiService().apply {
            assignmentByIdResponse = assignmentData
            assignmentsResponse = listOf(assignmentData)
            personalAssignmentsResponse = emptyList()
            personalAssignmentStatisticsResponses = mutableMapOf()
            assignmentResultResponse = AssignmentResultData(
                submittedStudents = 0,
                totalStudents = 0,
                averageScore = 0.0,
                completionRate = 0.0
            )
        }

        val viewModel = AssignmentViewModel(AssignmentRepository(fakeApi))

        setScreenContent(viewModel, assignmentId)

        waitForText("학생별 결과")
        waitForText("제출된 과제가 없습니다")

        composeTestRule.onNodeWithText("제출된 과제가 없습니다", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("학생별 결과", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun teacherAssignmentResultsScreen_loadingState_showsProgressIndicator() {
        val assignmentId = 55
        val assignmentData = createAssignmentData(id = assignmentId, title = "로딩 상태 과제")

        val fakeApi = FakeApiService().apply {
            assignmentByIdResponse = assignmentData
            assignmentsResponse = listOf(assignmentData)
            personalAssignmentsDelayMillis = 2_000
        }

        val viewModel = AssignmentViewModel(AssignmentRepository(fakeApi))

        composeTestRule.mainClock.autoAdvance = false

        setScreenContent(viewModel, assignmentId)

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodes(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }

        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo.Indeterminate)).assertExists()

        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.mainClock.advanceTimeBy(2_500)

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("학생별 결과", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun teacherAssignmentResultsScreen_errorState_showsEmptyMessage() {
        val assignmentId = 66
        val assignmentData = createAssignmentData(id = assignmentId, title = "에러 상태 과제")

        val fakeApi = FakeApiService().apply {
            assignmentByIdResponse = assignmentData
            assignmentsResponse = listOf(assignmentData)
            shouldFailPersonalAssignments = true
            personalAssignmentsErrorMessage = "네트워크 오류"
        }

        val viewModel = AssignmentViewModel(AssignmentRepository(fakeApi))

        setScreenContent(viewModel, assignmentId)

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasText("제출된 과제가 없습니다", substring = true), useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("제출된 과제가 없습니다", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun teacherAssignmentResultsScreen_withMixedStatuses_showsAllBadges() {
        val assignmentId = 99
        val assignmentData = createAssignmentData(id = assignmentId, title = "상태 혼합 과제")

        val submitted = createPersonalAssignmentData(
            id = 401,
            assignmentId = assignmentId,
            status = PersonalAssignmentStatus.SUBMITTED,
            studentName = "김제출",
            startedAt = "2024-01-03T09:00:00Z",
            submittedAt = "2024-01-03T10:00:00Z"
        )
        val inProgress = createPersonalAssignmentData(
            id = 402,
            assignmentId = assignmentId,
            status = PersonalAssignmentStatus.IN_PROGRESS,
            studentName = "박진행",
            startedAt = "2024-01-04T09:00:00Z",
            submittedAt = null
        )
        val notStarted = createPersonalAssignmentData(
            id = 403,
            assignmentId = assignmentId,
            status = PersonalAssignmentStatus.NOT_STARTED,
            studentName = "최미시작",
            startedAt = null,
            submittedAt = null
        )

        val fakeApi = FakeApiService().apply {
            assignmentByIdResponse = assignmentData
            assignmentsResponse = listOf(assignmentData)
            personalAssignmentsResponse = listOf(submitted, inProgress, notStarted)
            personalAssignmentStatisticsResponses = mutableMapOf(
                submitted.id to createStatistics(averageScore = 88f, accuracy = 0.88f),
                inProgress.id to createStatistics(averageScore = 55f, accuracy = 0.55f)
            )
            assignmentResultResponse = AssignmentResultData(
                submittedStudents = 1,
                totalStudents = 3,
                averageScore = 88.0,
                completionRate = 0.33
            )
        }

        val viewModel = AssignmentViewModel(AssignmentRepository(fakeApi))

        setScreenContent(viewModel, assignmentId)

        waitForText("학생별 결과")
        waitForText("총 3명")

        composeTestRule.onNodeWithText("총 3명", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("완료", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("진행 중", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("미시작", useUnmergedTree = true).assertIsDisplayed()
    }

    private fun setScreenContent(
        viewModel: AssignmentViewModel,
        assignmentId: Int
    ) {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentResultsScreen(
                    assignmentViewModel = viewModel,
                    assignmentId = assignmentId
                )
            }
        }
    }

    private fun waitForText(text: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodes(hasText(text), useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun createAssignmentData(
        id: Int,
        title: String
    ): AssignmentData {
        val subject = Subject(id = 10, name = "국어", code = "KOR")
        val courseClass = CourseClass(
            id = 20,
            name = "국어 A반",
            description = "테스트 반",
            subject = subject,
            teacherName = "이선생",
            startDate = "2024-01-01",
            endDate = "2024-12-31",
            studentCount = 30,
            createdAt = "2024-01-01T00:00:00Z"
        )
        return AssignmentData(
            id = id,
            title = title,
            description = "상세 설명",
            totalQuestions = 10,
            createdAt = "2024-01-01T09:00:00Z",
            visibleFrom = "2024-01-01T09:00:00Z",
            dueAt = "2024-02-01T23:59:59Z",
            courseClass = courseClass,
            materials = listOf(
                Material(
                    id = 1,
                    kind = "PDF",
                    s3Key = "assignments/$id/material.pdf",
                    bytes = 1024,
                    createdAt = "2024-01-01T09:00:00Z"
                )
            ),
            grade = "중학교 2학년"
        )
    }

    private fun createPersonalAssignmentData(
        id: Int,
        assignmentId: Int,
        status: PersonalAssignmentStatus,
        studentName: String,
        startedAt: String?,
        submittedAt: String?
    ): PersonalAssignmentData {
        val displayName = studentName
        val email = "${studentName.lowercase(Locale.getDefault())}@school.com"

        return PersonalAssignmentData(
            id = id,
            student = StudentInfo(
                id = id,
                displayName = displayName,
                email = email
            ),
            assignment = PersonalAssignmentInfo(
                id = assignmentId,
                title = "듣기 평가 과제",
                description = "과제 설명",
                totalQuestions = 10,
                visibleFrom = "2024-01-01T09:00:00Z",
                dueAt = "2024-02-01T23:59:59Z",
                grade = "중학교 2학년"
            ),
            status = status,
            solvedNum = if (status == PersonalAssignmentStatus.SUBMITTED) 10 else 4,
            startedAt = startedAt,
            submittedAt = submittedAt
        )
    }

    private fun createStatistics(
        averageScore: Float,
        accuracy: Float
    ): PersonalAssignmentStatistics =
        PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = (accuracy * 10).toInt(),
            correctAnswers = (accuracy * 10).toInt(),
            accuracy = accuracy,
            totalProblem = 10,
            solvedProblem = (accuracy * 10).toInt(),
            progress = accuracy,
            averageScore = averageScore
        )
}

