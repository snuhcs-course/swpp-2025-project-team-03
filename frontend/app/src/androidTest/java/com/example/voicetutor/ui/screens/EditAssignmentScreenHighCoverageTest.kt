package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.models.AssignmentData
import com.example.voicetutor.data.models.ClassData
import com.example.voicetutor.data.models.CourseClass
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
class EditAssignmentScreenHighCoverageTest {

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
            shouldFailCreateAssignment = false
            shouldFailCreateClass = false
            shouldFailClasses = false
            classesResponse = listOf(
                ClassData(
                    id = 1,
                    name = "수학 A반",
                    subject = Subject(id = 1, name = "수학", code = "MATH"),
                    description = "기초 수학 수업",
                    teacherId = 2,
                    teacherName = "김선생님",
                    studentCount = 25,
                    studentCountAlt = 25,
                    createdAt = "2024-01-01T00:00:00Z",
                ),
            )

            assignmentByIdResponse = AssignmentData(
                id = 100,
                title = "기존 과제",
                description = "기존 설명",
                totalQuestions = 5,
                createdAt = "2024-01-01T00:00:00Z",
                dueAt = "2024-12-31 23:59",
                courseClass = CourseClass(
                    id = 1,
                    name = "수학 A반",
                    description = "desc",
                    subject = Subject(id = 1, name = "수학", code = "MATH"),
                    teacherName = "Teacher",
                    studentCount = 20,
                    createdAt = "2024-01-01",
                ),
                materials = emptyList(),
                grade = "중학교 1학년",
            )
        }
    }

    private fun waitForText(text: String, timeoutMillis: Long = 5_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule.onAllNodesWithText(text, substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun testClassSelectionDropdown() {
        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(teacherId = "2", assignmentId = 100)
            }
        }

        waitForText("기존 과제")

        composeRule.onNodeWithText("수업 선택 (변경 불가)").assertIsDisplayed()
        composeRule.onNodeWithText("과목 (변경 불가)").assertIsDisplayed()
        composeRule.onNodeWithText("설명").assertIsDisplayed()
        composeRule.onNodeWithText("마감일").assertIsDisplayed()
        composeRule.onNodeWithText("과제 진행 현황").assertIsDisplayed()
    }

    @Test
    fun testSaveButtonValidationAndSuccess() {
        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(teacherId = "2", assignmentId = 100)
            }
        }

        waitForText("기존 과제")

        val titleField = composeRule.onNodeWithText("기존 과제")
        titleField.performTextReplacement("")

        composeRule.waitForIdle()

        composeRule.onNodeWithText("저장").performClick()
        composeRule.waitForIdle()

        waitForText("입력 오류")
        waitForText("필수 항목을 모두 입력하고")

        composeRule.onNodeWithText("확인").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("AssignmentTitleInput")

        composeRule.onAllNodes(hasSetTextAction())[0].performTextReplacement("수정된 과제")

        composeRule.onNodeWithText("저장").performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun testDeleteFlow() {
        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(teacherId = "2", assignmentId = 100)
            }
        }

        waitForText("기존 과제")

        composeRule.onNodeWithText("과제 삭제").performScrollTo()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("과제 삭제").performClick()
        composeRule.waitForIdle()

        waitForText("과제 삭제")
        waitForText("정말로 이 과제를 삭제하시겠습니까?")

        composeRule.onNodeWithText("취소").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("정말로 이 과제를 삭제하시겠습니까?").assertDoesNotExist()

        composeRule.onNodeWithText("과제 삭제").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("삭제").performClick()
    }

    @Test
    fun testDateTimePickerFlow() {
        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(teacherId = "2", assignmentId = 100)
            }
        }

        waitForText("기존 과제")

        composeRule.onNodeWithText("마감일").performScrollTo()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("마감일").performClick()
        composeRule.waitForIdle()

        waitForText("시간 선택")

        composeRule.onNodeWithText("시간 선택").performClick()
        composeRule.waitForIdle()

        waitForText("시간 선택")

        composeRule.onNodeWithText("확인").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("시간 선택").assertDoesNotExist()
    }

    @Test
    fun testValidationDialog() {
        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(teacherId = "2", assignmentId = 100)
            }
        }

        waitForText("기존 과제")

        val titleField = composeRule.onNodeWithText("기존 과제")
        titleField.performTextReplacement("")
        composeRule.waitForIdle()

        composeRule.onNodeWithText("저장").performClick()
        composeRule.waitForIdle()

        waitForText("입력 오류")
        waitForText("필수 항목을 모두 입력하고")

        composeRule.onNodeWithText("확인").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithText("입력 오류").assertDoesNotExist()
    }
}
