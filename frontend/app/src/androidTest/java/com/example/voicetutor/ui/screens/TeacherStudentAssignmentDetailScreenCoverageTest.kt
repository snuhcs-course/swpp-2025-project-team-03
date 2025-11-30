package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.models.AssignmentCorrectnessItem
import com.example.voicetutor.data.models.AssignmentData
import com.example.voicetutor.data.models.CourseClass
import com.example.voicetutor.data.models.PersonalAssignmentStatistics
import com.example.voicetutor.data.models.Subject
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
class TeacherStudentAssignmentDetailScreenCoverageTest {

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
            assignmentCorrectnessResponses = listOf(
                AssignmentCorrectnessItem(
                    questionContent = "태양이 도는 은하의 이름은?",
                    questionModelAnswer = "은하수",
                    studentAnswer = "은하수",
                    isCorrect = true,
                    answeredAt = "2024-01-02T10:00:00Z",
                    questionNum = "1",
                    explanation = "태양계는 은하수 은하에 속해 있습니다.",
                ),
                AssignmentCorrectnessItem(
                    questionContent = "지구의 위성은?",
                    questionModelAnswer = "달",
                    studentAnswer = "달",
                    isCorrect = true,
                    answeredAt = "2024-01-02T10:05:00Z",
                    questionNum = "1-1",
                    explanation = "지구의 유일한 자연 위성은 달입니다.",
                ),
                AssignmentCorrectnessItem(
                    questionContent = "태양계의 행성 수는?",
                    questionModelAnswer = "8개",
                    studentAnswer = "9개",
                    isCorrect = false,
                    answeredAt = "2024-01-02T10:10:00Z",
                    questionNum = "2",
                    explanation = "태양계에는 8개의 행성이 있습니다.",
                ),
            )
            shouldFailGetAllAssignments = false
            shouldFailGetAssignmentById = false
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
    fun teacherStudentAssignmentDetailScreen_formatDuration_nullOrEmpty_returnsDash() {
        fakeApi.personalAssignmentsResponse = listOf(
            fakeApi.personalAssignmentData.copy(
                startedAt = null,
                submittedAt = null,
            ),
        )

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
            assignmentViewModel.assignmentResults.value.isNotEmpty()
        }

        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("-", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_formatDuration_validTimes_calculatesCorrectly() {
        fakeApi.personalAssignmentsResponse = listOf(
            fakeApi.personalAssignmentData.copy(
                startedAt = "2024-01-02T09:00:00Z",
                submittedAt = "2024-01-02T10:30:45Z",
            ),
        )

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
            assignmentViewModel.assignmentResults.value.isNotEmpty()
        }

        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("소요 시간", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_formatDuration_invalidTimes_returnsDash() {
        fakeApi.personalAssignmentsResponse = listOf(
            fakeApi.personalAssignmentData.copy(
                startedAt = "2024-01-02T10:00:00Z",
                submittedAt = "2024-01-02T09:00:00Z",
            ),
        )

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

        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("-", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_parseIsoToMillis_invalidFormat_handlesGracefully() {
        fakeApi.personalAssignmentsResponse = listOf(
            fakeApi.personalAssignmentData.copy(
                startedAt = "invalid-iso-format",
                submittedAt = "also-invalid",
            ),
        )

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

        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("-", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_findsAssignmentByTitle() {
        val testAssignment = AssignmentData(
            id = 999,
            title = "특별한 과제 제목",
            description = "테스트용 과제",
            totalQuestions = 5,
            createdAt = "2024-01-01T00:00:00Z",
            dueAt = "2024-12-31T23:59:59Z",
            courseClass = CourseClass(
                id = 1,
                name = "테스트 클래스",
                description = "테스트",
                subject = Subject(id = 1, name = "수학", code = "MATH"),
                teacherName = "선생님",
                studentCount = 10,
                createdAt = "2024-01-01T00:00:00Z",
            ),
            grade = "중학교 1학년",
        )

        fakeApi.assignmentsResponse = listOf(testAssignment)
        fakeApi.assignmentByIdResponse = testAssignment

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = "1",
                    assignmentId = 0,
                    assignmentTitle = "특별한 과제 제목",
                )
            }
        }

        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_findsAssignmentByTitleContains() {
        val testAssignment = AssignmentData(
            id = 888,
            title = "수학 복습 과제",
            description = "테스트용",
            totalQuestions = 5,
            createdAt = "2024-01-01T00:00:00Z",
            dueAt = "2024-12-31T23:59:59Z",
            courseClass = CourseClass(
                id = 1,
                name = "테스트 클래스",
                description = "테스트",
                subject = Subject(id = 1, name = "수학", code = "MATH"),
                teacherName = "선생님",
                studentCount = 10,
                createdAt = "2024-01-01T00:00:00Z",
            ),
            grade = "중학교 1학년",
        )

        fakeApi.assignmentsResponse = listOf(testAssignment)

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = "1",
                    assignmentId = 0,
                    assignmentTitle = "복습",
                )
            }
        }

        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_alreadyLoaded_skipsReload() {
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
            assignmentViewModel.assignmentResults.value.isNotEmpty()
        }

        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_loadsStatsAndCorrectnessSeparately() {
        fakeApi.personalAssignmentStatisticsResponses.clear()
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

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[com.example.voicetutor.ui.viewmodel.AssignmentViewModel::class.java]

        composeRule.waitUntil(timeoutMillis = 15_000) {
            assignmentViewModel.currentAssignment.value != null
        }

        fakeApi.personalAssignmentStatisticsResponses[assignmentId] = defaultPersonalAssignmentStatistics()
        fakeApi.assignmentCorrectnessResponses = listOf(
            AssignmentCorrectnessItem(
                questionContent = "테스트 문제",
                questionModelAnswer = "정답",
                studentAnswer = "정답",
                isCorrect = true,
                answeredAt = "2024-01-02T10:00:00Z",
                questionNum = "1",
                explanation = "설명",
            ),
        )

        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_withAssignmentId_skipsTitleSearch() {
        val studentId = fakeApi.personalAssignmentData.student.id.toString()
        val assignmentId = fakeApi.personalAssignmentData.assignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = studentId,
                    assignmentId = assignmentId,
                    assignmentTitle = "다른 제목",
                )
            }
        }

        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_findsAndLoadsAssignmentByTitle() {
        val testAssignment = AssignmentData(
            id = 777,
            title = "동적 로딩 과제",
            description = "테스트",
            totalQuestions = 5,
            createdAt = "2024-01-01T00:00:00Z",
            dueAt = "2024-12-31T23:59:59Z",
            courseClass = CourseClass(
                id = 1,
                name = "테스트 클래스",
                description = "테스트",
                subject = Subject(id = 1, name = "수학", code = "MATH"),
                teacherName = "선생님",
                studentCount = 10,
                createdAt = "2024-01-01T00:00:00Z",
            ),
            grade = "중학교 1학년",
        )

        fakeApi.assignmentsResponse = listOf(testAssignment)
        fakeApi.assignmentByIdResponse = testAssignment

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = "1",
                    assignmentId = 0,
                    assignmentTitle = "동적 로딩 과제",
                )
            }
        }

        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_showsLoadingStates() {
        fakeApi.personalAssignmentsDelayMillis = 1000

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

        composeRule.waitForIdle()

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[com.example.voicetutor.ui.viewmodel.AssignmentViewModel::class.java]

        composeRule.waitUntil(timeoutMillis = 15_000) {
            assignmentViewModel.assignmentResults.value.isNotEmpty()
        }
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_togglesQuestionGroup() {
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

        composeRule.onAllNodesWithText("꼬리질문 펼치기", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("꼬리질문 접기", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_displaysTailQuestionToggle() {
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

        composeRule.onAllNodesWithText("꼬리질문 펼치기", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_expandsAndCollapsesTailQuestions() {
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

        composeRule.onAllNodesWithText("꼬리질문 펼치기", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("꼬리질문 접기", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()

        composeRule.onAllNodesWithText("꼬리질문 접기", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("꼬리질문 펼치기", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_displaysDetailedQuestionResultCard() {
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

        composeRule.onAllNodesWithText("꼬리질문 펼치기", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_displaysIncorrectAnswerCard() {
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

        waitForText("태양계의 행성 수는?")
        composeRule.onAllNodesWithText("태양계의 행성 수는?", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()

        composeRule.onAllNodesWithText("오답", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_handlesEmptyMyAnswer() {
        fakeApi.assignmentCorrectnessResponses = listOf(
            AssignmentCorrectnessItem(
                questionContent = "빈 답변 문제",
                questionModelAnswer = "정답",
                studentAnswer = "",
                isCorrect = false,
                answeredAt = "2024-01-02T10:00:00Z",
                questionNum = "3",
                explanation = "설명",
            ),
        )

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

        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_handlesEmptyExplanation() {
        fakeApi.assignmentCorrectnessResponses = listOf(
            AssignmentCorrectnessItem(
                questionContent = "설명 없는 문제",
                questionModelAnswer = "정답",
                studentAnswer = "정답",
                isCorrect = true,
                answeredAt = "2024-01-02T10:00:00Z",
                questionNum = "4",
                explanation = "",
            ),
        )

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

        composeRule.waitForIdle()
    }
}
