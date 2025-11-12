package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
        }
    }

    private fun waitForText(text: String, timeoutMillis: Long = 10_000) {
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
                    assignmentTitle = fakeApi.personalAssignmentData.assignment.title
                )
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[com.example.voicetutor.ui.viewmodel.AssignmentViewModel::class.java]

        composeRule.waitUntil(timeoutMillis = 15_000) {
            assignmentViewModel.assignmentResults.value.any { it.name.contains("홍길동") }
        }

        waitForText("홍길동")
        waitForText("문제별 상세 결과")

        composeRule.onNodeWithText("평균 점수", substring = true, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("소요 시간", substring = true, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun teacherStudentAssignmentDetailScreen_withoutResults_showsEmptyState() {
        fakeApi.personalAssignmentsResponse = emptyList()
        fakeApi.personalAssignmentStatisticsResponses.clear()

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = "999",
                    assignmentId = 999,
                    assignmentTitle = "빈 과제"
                )
            }
        }

        waitForText("학생 결과를 찾을 수 없습니다")
        composeRule.onNodeWithText("학생 결과를 찾을 수 없습니다", useUnmergedTree = true).assertIsDisplayed()
    }
}

