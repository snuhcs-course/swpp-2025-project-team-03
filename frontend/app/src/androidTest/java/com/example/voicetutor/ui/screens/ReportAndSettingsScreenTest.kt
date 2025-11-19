package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.models.PersonalAssignmentStatus
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AuthViewModel
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
class ReportAndSettingsScreenTest {

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
    }

    private fun waitForText(text: String, substring: Boolean = true, timeoutMillis: Long = 5_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule.onAllNodesWithText(
                text = text,
                substring = substring,
                useUnmergedTree = true,
            ).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun reportScreen_withCompletedAssignments_displaysReportCard() {
        fakeApi.personalAssignmentsResponse = listOf(
            fakeApi.personalAssignmentData.copy(
                status = PersonalAssignmentStatus.SUBMITTED,
                submittedAt = "2024-01-02T10:00:00Z",
            ),
        )

        composeRule.setContent {
            VoiceTutorTheme {
                ReportScreen(studentId = 1)
            }
        }

        waitForText("완료한 과제 목록")
        composeRule.onNodeWithText("완료한 과제 목록", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("1단원 복습 과제", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun reportScreen_noCompletedAssignments_showsPlaceholder() {
        fakeApi.personalAssignmentsResponse = emptyList()

        composeRule.setContent {
            VoiceTutorTheme {
                ReportScreen(studentId = 1)
            }
        }

        waitForText("완료한 과제가 없습니다")
        composeRule.onNodeWithText("완료한 과제가 없습니다", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun settingsScreen_teacherRole_showsTeacherBadge() {
        composeRule.setContent {
            VoiceTutorTheme {
                SettingsScreen(userRole = UserRole.TEACHER)
            }
        }

        val authViewModel = ViewModelProvider(composeRule.activity)[AuthViewModel::class.java]
        composeRule.runOnIdle {
            authViewModel.login("teacher@voicetutor.com", "teacher123")
        }

        waitForText("선생님")
        composeRule.onNodeWithText("선생님", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun settingsScreen_studentDetailMode_loadsStudentInfo() {
        composeRule.setContent {
            VoiceTutorTheme {
                SettingsScreen(
                    userRole = UserRole.TEACHER,
                    studentId = 1,
                )
            }
        }

        val authViewModel = ViewModelProvider(composeRule.activity)[AuthViewModel::class.java]
        composeRule.runOnIdle {
            authViewModel.login("teacher@voicetutor.com", "teacher123")
        }

        waitForText("홍길동")
        composeRule.onNodeWithText("홍길동", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("학생", useUnmergedTree = true).assertIsDisplayed()
    }
}
