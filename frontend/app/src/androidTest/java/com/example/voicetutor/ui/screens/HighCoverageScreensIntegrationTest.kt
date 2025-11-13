package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
class HighCoverageScreensIntegrationTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun waitForText(text: String, substring: Boolean = false, timeoutMillis: Long = 10_000) {
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
        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_rendersFakeData() {
        setScreen {
            CreateAssignmentScreen(teacherId = "2")
        }

        waitForText("기본 정보", substring = true)
        waitForText("과제 제목", substring = true)
        assertFirstNodeWithText("기본 정보", substring = true)
        assertFirstNodeWithText("과제 제목", substring = true)
    }

    @Test
    fun teacherDashboardScreen_rendersStatistics() {
        setScreen {
            TeacherDashboardScreen(teacherId = "2")
        }

        waitForText("빠른 실행")
        waitForText("과제 생성", substring = true)
        assertFirstNodeWithText("빠른 실행")
        assertFirstNodeWithText("과제 생성", substring = true)
    }

    @Test
    fun teacherAssignmentResultsScreen_displaysStudentResults() {
        setScreen {
            TeacherAssignmentResultsScreen(assignmentId = 1)
        }

        waitForText("학생별 과제 결과를 확인하고 피드백을 제공하세요", substring = true)
        waitForText("학생별 결과", substring = true)
        assertFirstNodeWithText("학생별 과제 결과를 확인하고 피드백을 제공하세요", substring = true)
        assertFirstNodeWithText("학생별 결과", substring = true)
    }

    @Test
    fun teacherAssignmentDetailScreen_showsAssignmentHeader() {
        setScreen {
            TeacherAssignmentDetailScreen(assignmentId = 1)
        }

        waitForText("과제 내용", substring = true)
        waitForText("과제 결과", substring = true)
        assertFirstNodeWithText("과제 내용", substring = true)
        assertFirstNodeWithText("과제 결과", substring = true)
    }

    @Test
    fun teacherStudentsScreen_rendersClassOverview() {
        setScreen {
            TeacherStudentsScreen(classId = 1, teacherId = "2")
        }

        waitForText("학생 목록", substring = true)
        waitForText("학생 등록", substring = true)
        assertFirstNodeWithText("학생 목록", substring = true)
        assertFirstNodeWithText("학생 등록", substring = true)
    }

    @Test
    fun teacherClassDetailScreen_rendersSummary() {
        setScreen {
            TeacherClassDetailScreen(classId = 1, className = "수학 A반")
        }

        waitForText("수학 A반", substring = true)
        waitForText("과제 목록", substring = true)
        assertFirstNodeWithText("수학 A반", substring = true)
        assertFirstNodeWithText("과제 목록", substring = true)
    }
}

