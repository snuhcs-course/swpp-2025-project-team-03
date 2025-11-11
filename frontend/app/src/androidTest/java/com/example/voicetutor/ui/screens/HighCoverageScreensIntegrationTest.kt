package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
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
    val composeRule = createAndroidComposeRule<TestHiltActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
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

        composeRule.onNodeWithText("기본 정보", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("과제 제목", substring = true).assertIsDisplayed()
    }

    @Test
    fun teacherDashboardScreen_rendersStatistics() {
        setScreen {
            TeacherDashboardScreen(teacherId = "2")
        }

        composeRule.onNodeWithText("환영합니다", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("과제", substring = true).assertIsDisplayed()
    }

    @Test
    fun teacherAssignmentResultsScreen_displaysStudentResults() {
        setScreen {
            TeacherAssignmentResultsScreen(assignmentId = 1)
        }

        composeRule.onNodeWithText("학생별 과제 결과를 확인하고 피드백을 제공하세요", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("학생별 결과", substring = true).assertIsDisplayed()
    }

    @Test
    fun teacherAssignmentDetailScreen_showsAssignmentHeader() {
        setScreen {
            TeacherAssignmentDetailScreen(assignmentId = 1)
        }

        composeRule.onNodeWithText("1단원 복습 과제", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("과목", substring = true).assertIsDisplayed()
    }

    @Test
    fun teacherStudentsScreen_rendersClassOverview() {
        setScreen {
            TeacherStudentsScreen(classId = 1, teacherId = "2")
        }

        composeRule.onNodeWithText("학생 관리", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("학생 목록", substring = true).assertIsDisplayed()
    }

    @Test
    fun teacherClassDetailScreen_rendersSummary() {
        setScreen {
            TeacherClassDetailScreen(classId = 1, className = "수학 A반")
        }

        composeRule.onNodeWithText("수학 A반", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("학생 수", substring = true).assertIsDisplayed()
    }
}

