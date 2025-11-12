package com.example.voicetutor.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.data.repository.AssignmentRepository
import com.example.voicetutor.data.repository.AuthRepository
import com.example.voicetutor.data.repository.DashboardRepository
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import com.example.voicetutor.ui.viewmodel.DashboardViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
@RunWith(AndroidJUnit4::class)
class ScreenCoverageExpansionTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun loginTeacher() {
        val authViewModel = ViewModelProvider(composeRule.activity)[AuthViewModel::class.java]
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            authViewModel.login("teacher@voicetutor.com", "teacher123")
        }
        composeRule.waitUntil(timeoutMillis = 5_000) { authViewModel.currentUser.value != null }
    }

    private fun waitForText(text: String, substring: Boolean = true, timeoutMillis: Long = 10_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule.onAllNodesWithText(text, substring = substring, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun renderTeacherScreens() {
        loginTeacher()

        composeRule.setContent {
            VoiceTutorTheme {
                Column {
                    TeacherDashboardScreen(teacherId = "2")
                    AllAssignmentsScreen(teacherId = "2")
                    TeacherStudentsScreen(classId = 1, teacherId = "2")
                    TeacherClassDetailScreen(classId = 1, className = "수학 A반")
                    TeacherStudentReportScreen(
                        classId = 1,
                        studentId = 1,
                        studentName = "홍길동"
                    )
                }
            }
        }
        waitForText("모든 과제")
        waitForText("학생 목록")
        waitForText("수학 A반")
        waitForText("성취기준")
    }

    @Test
    fun renderStudentScreens() {
        val apiService = FakeApiService()
        val assignmentViewModel = AssignmentViewModel(AssignmentRepository(apiService))
        val authViewModel = AuthViewModel(AuthRepository(apiService))
        val dashboardViewModel = DashboardViewModel(DashboardRepository(apiService))

        composeRule.setContent {
            VoiceTutorTheme {
                Column {
                    StudentDashboardScreen(
                        authViewModel = authViewModel,
                        assignmentViewModel = assignmentViewModel,
                        dashboardViewModel = dashboardViewModel
                    )
                    AssignmentDetailedResultsScreen(
                        personalAssignmentId = 1,
                        assignmentTitle = "테스트 과제",
                        viewModel = assignmentViewModel
                    )
                    AppInfoScreen()
                }
            }
        }

        composeRule.runOnIdle {
            authViewModel.login("student@voicetutor.com", "student123")
        }
        composeRule.waitUntil(timeoutMillis = 5_000) { authViewModel.currentUser.value != null }
        composeRule.waitForIdle()
        waitForText("나에게 할당된 과제")
        waitForText("테스트 과제")
        waitForText("앱 정보")
    }
}

