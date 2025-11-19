package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import com.example.voicetutor.HiltComponentActivity
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
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
class HighCoverageScreensIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
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
        // Reset all error flags to ensure clean state
        fakeApi.apply {
            shouldFailGetAssignmentById = false
            shouldFailPersonalAssignments = false
            shouldFailGetAllAssignments = false
            shouldFailAssignmentResult = false
            shouldFailClasses = false
            shouldFailClassStudents = false
            shouldFailAllStudents = false
            shouldFailClassStudentsStatistics = false
            shouldFailDashboardStats = false
        }
    }

    private fun waitForText(text: String, substring: Boolean = false, timeoutMillis: Long = 15_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule
                .onAllNodesWithText(text, substring = substring, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun assertFirstNodeWithText(text: String, substring: Boolean = false) {
        composeRule
            .onAllNodesWithText(text, substring = substring, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    private fun setScreen(content: @androidx.compose.runtime.Composable () -> Unit) {
        composeRule.setContent {
            VoiceTutorTheme {
                content()
            }
        }
        // Wait for initial composition and any immediate state updates
        composeRule.waitForIdle()
        // Additional small delay to allow LaunchedEffect to start
        Thread.sleep(100)
        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_rendersFakeData() {
        setScreen {
            CreateAssignmentScreen(teacherId = "2")
        }

        waitForText("기본 정보", substring = true, timeoutMillis = 15_000)
        waitForText("과제 제목", substring = true, timeoutMillis = 15_000)
        assertFirstNodeWithText("기본 정보", substring = true)
        assertFirstNodeWithText("과제 제목", substring = true)
    }

    @Test
    fun teacherDashboardScreen_rendersStatistics() {
        setScreen {
            TeacherDashboardScreen(teacherId = "2")
        }

        // Wait for data to load - dashboard loads assignments and stats asynchronously
        waitForText("빠른 실행", timeoutMillis = 20_000)
        waitForText("과제 생성", substring = true, timeoutMillis = 20_000)
        assertFirstNodeWithText("빠른 실행")
        assertFirstNodeWithText("과제 생성", substring = true)
    }

    @Test
    fun teacherAssignmentResultsScreen_displaysStudentResults() {
        setScreen {
            TeacherAssignmentResultsScreen(assignmentId = 1)
        }

        waitForText("학생별 과제 결과를 확인하고 피드백을 제공하세요", substring = true, timeoutMillis = 20_000)
        waitForText("학생별 결과", substring = true, timeoutMillis = 20_000)
        assertFirstNodeWithText("학생별 과제 결과를 확인하고 피드백을 제공하세요", substring = true)
        assertFirstNodeWithText("학생별 결과", substring = true)
    }

    @Test
    fun teacherAssignmentDetailScreen_showsAssignmentHeader() {
        setScreen {
            TeacherAssignmentDetailScreen(assignmentId = 1)
        }

        // Wait for assignment to load - check for assignment title first
        waitForText("1단원 복습 과제", substring = true, timeoutMillis = 15_000)
        
        // Then check for section headers
        waitForText("과제 내용", substring = true, timeoutMillis = 15_000)
        waitForText("학생별 결과", substring = true, timeoutMillis = 15_000)
        assertFirstNodeWithText("과제 내용", substring = true)
        assertFirstNodeWithText("학생별 결과", substring = true)
    }

    @Test
    fun teacherStudentsScreen_rendersClassOverview() {
        setScreen {
            TeacherStudentsScreen(classId = 1, teacherId = "2")
        }

        waitForText("학생 목록", substring = true, timeoutMillis = 20_000)
        waitForText("학생 등록", substring = true, timeoutMillis = 20_000)
        assertFirstNodeWithText("학생 목록", substring = true)
        assertFirstNodeWithText("학생 등록", substring = true)
    }

    @Test
    fun teacherClassDetailScreen_rendersSummary() {
        setScreen {
            TeacherClassDetailScreen(classId = 1, className = "수학 A반")
        }

        waitForText("수학 A반", substring = true, timeoutMillis = 20_000)
        waitForText("과제 목록", substring = true, timeoutMillis = 20_000)
        assertFirstNodeWithText("수학 A반", substring = true)
        assertFirstNodeWithText("과제 목록", substring = true)
    }
}
