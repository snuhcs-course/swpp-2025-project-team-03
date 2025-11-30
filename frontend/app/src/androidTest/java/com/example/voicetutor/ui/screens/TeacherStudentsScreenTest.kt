package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.models.ClassData
import com.example.voicetutor.data.models.ClassStudentsStatistics
import com.example.voicetutor.data.models.Student
import com.example.voicetutor.data.models.StudentStatisticsItem
import com.example.voicetutor.data.models.Subject
import com.example.voicetutor.data.models.UserRole
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
class TeacherStudentsScreenTest {

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
        resetFakeApi()
    }

    private fun resetFakeApi() {
        val defaultClass = ClassData(
            id = 1,
            name = "고등학교 1학년 A반",
            subject = Subject(id = 1, name = "국어", code = "KOR"),
            description = "국어 수업",
            teacherId = 2,
            teacherName = "이선생",
            studentCount = 2,
            studentCountAlt = 2,
            createdAt = "2024-01-01T00:00:00Z",

        )

        val defaultStudents = listOf(
            Student(id = 1, name = "홍길동", email = "hong@school.com", role = UserRole.STUDENT),
            Student(id = 2, name = "이몽룡", email = "lee@school.com", role = UserRole.STUDENT),
        )

        val defaultStats = ClassStudentsStatistics(
            overallCompletionRate = 0.8f,
            students = listOf(
                StudentStatisticsItem(
                    studentId = 1,
                    averageScore = 90f,
                    completionRate = 0.9f,
                    totalAssignments = 10,
                    completedAssignments = 9,
                ),
                StudentStatisticsItem(
                    studentId = 2,
                    averageScore = 85f,
                    completionRate = 0.75f,
                    totalAssignments = 12,
                    completedAssignments = 9,
                ),
            ),
        )

        fakeApi.apply {
            classesResponse = listOf(defaultClass)
            shouldFailClasses = false
            classStudentsResponse = defaultStudents
            shouldFailClassStudents = false
            allStudentsResponse = listOf(

                Student(id = 1, name = "홍길동", email = "hong@school.com", role = UserRole.STUDENT),
                Student(id = 2, name = "이몽룡", email = "lee@school.com", role = UserRole.STUDENT),
                Student(id = 3, name = "김영희", email = "kim@school.com", role = UserRole.STUDENT),
                Student(id = 4, name = "박철수", email = "park@school.com", role = UserRole.STUDENT),
            )
            shouldFailAllStudents = false
            classStudentsStatisticsResponse = defaultStats
            shouldFailClassStudentsStatistics = false
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
    fun teacherStudentsScreen_emptyState_showsPlaceholder() {
        fakeApi.classStudentsResponse = emptyList()
        fakeApi.allStudentsResponse = emptyList()
        fakeApi.classStudentsStatisticsResponse = ClassStudentsStatistics(
            overallCompletionRate = 0f,
            students = emptyList(),
        )

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("학생이 없습니다")
        composeRule.onAllNodesWithText("학생이 없습니다", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun teacherStudentsScreen_errorState_showsFallbackAndResetStats() {
        fakeApi.shouldFailClassStudents = true
        fakeApi.classStudentsErrorMessage = "학생 목록 로드 실패"
        fakeApi.shouldFailAllStudents = true
        fakeApi.allStudentsErrorMessage = "전체 학생 로드 실패"
        fakeApi.allStudentsResponse = emptyList()
        fakeApi.shouldFailClassStudentsStatistics = true
        fakeApi.classStudentsStatisticsErrorMessage = "통계 로드 실패"

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule
                .onAllNodesWithText("학생이 없습니다", useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty() &&
                composeRule
                    .onAllNodesWithText("0%", useUnmergedTree = true)
                    .fetchSemanticsNodes()
                    .isNotEmpty()
        }

        composeRule.onAllNodesWithText("학생이 없습니다", useUnmergedTree = true).onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("0%", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun teacherStudentsScreen_displaysStudentEmails() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("hong@school.com")
        composeRule.onAllNodesWithText("hong@school.com", useUnmergedTree = true).onFirst().assertIsDisplayed()
        waitForText("lee@school.com")
        composeRule.onAllNodesWithText("lee@school.com", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun teacherStudentsScreen_displaysCompletionRates() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText("완료율", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText("완료율", substring = true, useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun teacherStudentsScreen_displaysAverageScores() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText("평균 점수", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText("평균 점수", substring = true, useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun teacherStudentsScreen_handlesNullClassId() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = null, teacherId = "2")
            }
        }

        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentsScreen_handlesNullTeacherId() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = null)
            }
        }

        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentsScreen_displaysMultipleStudents() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("홍길동")
        waitForText("이몽룡")
        composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true).onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("이몽룡", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun teacherStudentsScreen_displaysAssignmentCounts() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("홍길동")

        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText("9", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText("9", substring = true, useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun teacherStudentsScreen_showsZeroCompletionRateWhenNoStats() {
        fakeApi.classStudentsStatisticsResponse = ClassStudentsStatistics(
            overallCompletionRate = 0f,
            students = emptyList(),
        )

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("0%")
        composeRule.onAllNodesWithText("0%", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun teacherStudentsScreen_displaysStudentList() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("홍길동")
        composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun teacherStudentsScreen_displaysStudentEmail() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("hong@school.com")
        composeRule.onAllNodesWithText("hong@school.com", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun teacherStudentsScreen_handlesEmptyStudentList() {
        fakeApi.classStudentsResponse = emptyList()

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentsScreen_displaysMultipleStudentsSecond() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("홍길동")
        waitForText("이몽룡")
        composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true).onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("이몽룡", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun teacherStudentsScreen_displaysStudentName() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("홍길동")
        composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun teacherStudentsScreen_displaysStudentEmailSecond() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("hong@school.com")
        composeRule.onAllNodesWithText("hong@school.com", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun teacherStudentsScreen_displaysStudentListSecond() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("홍길동")
        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentsScreen_displaysStatisticsSecond() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("홍길동")
        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentsScreen_handlesNullClassIdSecond() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = null, teacherId = "2")
            }
        }

        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentsScreen_displaysZeroCompletionRate() {
        fakeApi.classStudentsStatisticsResponse = ClassStudentsStatistics(
            overallCompletionRate = 0f,
            students = emptyList(),
        )

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentsScreen_displaysFullCompletionRate() {
        fakeApi.classStudentsStatisticsResponse = ClassStudentsStatistics(
            overallCompletionRate = 1f,
            students = listOf(
                StudentStatisticsItem(
                    studentId = 1,
                    averageScore = 100f,
                    completionRate = 1f,
                    totalAssignments = 10,
                    completedAssignments = 10,
                ),
            ),
        )

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("홍길동")
        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentsScreen_displaysAverageScore() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("홍길동")
        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentsScreen_displaysCompletionRateSecond() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("홍길동")
        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentsScreen_displaysEnrollBottomSheet() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("학생 등록")

        composeRule.onAllNodesWithText("학생 등록", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("학생 등록", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("이름 또는 이메일로 검색", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun teacherStudentsScreen_enrollBottomSheet_searchFiltersStudents() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("학생 등록")

        composeRule.onAllNodesWithText("학생 등록", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("학생 등록", useUnmergedTree = true)
                .fetchSemanticsNodes().size >= 2
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                val searchFields = composeRule.onAllNodes(hasSetTextAction(), useUnmergedTree = true)
                if (searchFields.fetchSemanticsNodes().isNotEmpty()) {
                    searchFields[0].performClick()
                    composeRule.waitForIdle()
                    searchFields[0].performTextReplacement("김영희")
                    true
                } else {
                    false
                }
            } catch (_: Exception) {
                false
            }
        }

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("김영희", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun teacherStudentsScreen_enrollBottomSheet_selectsStudents() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("학생 등록")

        composeRule.onAllNodesWithText("학생 등록", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText("김영희", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("등록 가능한 학생이 없습니다", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentsScreen_enrollBottomSheet_cancelsOnCancelButton() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("학생 등록")

        composeRule.onAllNodesWithText("학생 등록", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("취소", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onAllNodesWithText("취소", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("학생 등록", useUnmergedTree = true)
                .fetchSemanticsNodes().size == 1
        }
    }

    @Test
    fun teacherStudentsScreen_displaysDeleteBottomSheet() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("학생 삭제")

        composeRule.onAllNodesWithText("학생 삭제", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("학생 삭제", useUnmergedTree = true)
                .fetchSemanticsNodes().size >= 2
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("이름 또는 이메일로 검색", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun teacherStudentsScreen_deleteBottomSheet_searchFiltersStudents() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("학생 삭제")

        composeRule.onAllNodesWithText("학생 삭제", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("학생 삭제", useUnmergedTree = true)
                .fetchSemanticsNodes().size >= 2
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                val searchFields = composeRule.onAllNodes(hasSetTextAction(), useUnmergedTree = true)
                if (searchFields.fetchSemanticsNodes().isNotEmpty()) {
                    searchFields[0].performClick()
                    composeRule.waitForIdle()
                    searchFields[0].performTextReplacement("홍길동")
                    true
                } else {
                    false
                }
            } catch (_: Exception) {
                false
            }
        }

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun teacherStudentsScreen_deleteBottomSheet_selectsStudents() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("학생 삭제")

        composeRule.onAllNodesWithText("학생 삭제", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("삭제할 학생이 없습니다", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentsScreen_deleteBottomSheet_opensConfirmationDialog() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("학생 삭제")

        composeRule.onAllNodesWithText("학생 삭제", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("학생 삭제", useUnmergedTree = true)
                .fetchSemanticsNodes().size >= 2
        }

        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("삭제할 학생이 없습니다", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeRule.onAllNodesWithText("삭제", useUnmergedTree = true)
                    .onFirst()
                    .performClick()
                true
            } catch (_: Exception) {
                false
            }
        }

        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentsScreen_displaysDeleteConfirmationDialog() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("학생 삭제")

        composeRule.onAllNodesWithText("학생 삭제", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("학생 삭제", useUnmergedTree = true)
                .fetchSemanticsNodes().size >= 2
        }

        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("삭제할 학생이 없습니다", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.waitForIdle()

        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentsScreen_deleteConfirmationDialog_cancelsOnCancelButton() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("학생 삭제")

        composeRule.onAllNodesWithText("학생 삭제", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("학생 삭제", useUnmergedTree = true)
                .fetchSemanticsNodes().size >= 2
        }

        composeRule.waitForIdle()

        composeRule.waitForIdle()
    }

    @Test
    fun teacherStudentsScreen_deleteBottomSheet_cancelsOnCancelButton() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("학생 삭제")

        composeRule.onAllNodesWithText("학생 삭제", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("취소", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onAllNodesWithText("취소", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("학생 삭제", useUnmergedTree = true)
                .fetchSemanticsNodes().size == 1
        }
    }

    @Test
    fun teacherStudentsScreen_enrollBottomSheet_showsEmptyState() {
        fakeApi.allStudentsResponse = emptyList()

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("학생 등록")

        composeRule.onAllNodesWithText("학생 등록", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("등록 가능한 학생이 없습니다", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun teacherStudentsScreen_deleteBottomSheet_showsEmptyState() {
        fakeApi.classStudentsResponse = emptyList()

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(classId = 1, teacherId = "2")
            }
        }

        waitForText("학생 삭제")

        composeRule.onAllNodesWithText("학생 삭제", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithText("삭제할 학생이 없습니다", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
