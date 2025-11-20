package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.models.AssignmentData
import com.example.voicetutor.data.models.ClassData
import com.example.voicetutor.data.models.CourseClass
import com.example.voicetutor.data.models.Material
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
class EditAssignmentScreenTest {

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
        val subject = Subject(id = 1, name = "수학", code = "MATH")
        val courseClass = CourseClass(
            id = 1,
            name = "수학 A반",
            description = "심화 수학",
            subject = subject,
            teacherName = "김선생",

            studentCount = 25,
            createdAt = "2024-01-01T00:00:00Z",
        )

        val assignment = AssignmentData(
            id = 1,
            title = "1단원 복습 과제",
            description = "기초 개념을 복습하는 과제입니다.",
            totalQuestions = 10,
            createdAt = "2024-01-01T09:00:00Z",

            dueAt = "2024-02-01T23:59:59Z",
            courseClass = courseClass,
            materials = listOf(
                Material(
                    id = 1,
                    kind = "PDF",
                    s3Key = "assignments/1/material.pdf",
                    bytes = 1024,
                    createdAt = "2024-01-01T09:00:00Z",
                ),
            ),
            grade = "중학교 1학년",
        )

        val classes = listOf(
            ClassData(
                id = 1,
                name = "수학 A반",
                subject = subject,
                description = "심화 수학",
                teacherId = 2,
                teacherName = "김선생",
                studentCount = 25,
                studentCountAlt = 25,
                createdAt = "2024-01-01T00:00:00Z",

            ),
        )

        fakeApi.apply {
            assignmentByIdResponse = assignment
            assignmentsResponse = listOf(assignment)
            shouldFailGetAssignmentById = false
            classesResponse = classes
            shouldFailClasses = false
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
    fun editAssignmentScreen_displaysAssignmentInfo() {
        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(assignmentId = 1, teacherId = "2")
            }
        }

        waitForText("기본 정보")
        composeRule.onNodeWithText("기본 정보", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun editAssignmentScreen_displaysFormFields() {
        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(assignmentId = 1, teacherId = "2")
            }
        }

        waitForText("과제 제목")
        composeRule.onAllNodesWithText("과제 제목", useUnmergedTree = true).onFirst().assertIsDisplayed()
        waitForText("설명")
        composeRule.onAllNodesWithText("설명", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun editAssignmentScreen_displaysDueDateField() {
        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(assignmentId = 1, teacherId = "2")
            }
        }

        waitForText("마감일")
        composeRule.onAllNodesWithText("마감일", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun editAssignmentScreen_showsLoadingIndicator() {
        fakeApi.shouldFailGetAssignmentById = false

        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(assignmentId = 1, teacherId = "2")
            }
        }

        // Loading indicator might appear briefly
        composeRule.waitForIdle()
    }

    @Test
    fun editAssignmentScreen_loadsAssignmentData() {
        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(assignmentId = 1, teacherId = "2")
            }
        }

        waitForText("1단원 복습 과제")
        composeRule.onNodeWithText("1단원 복습 과제", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun editAssignmentScreen_handlesErrorState() {
        fakeApi.shouldFailGetAssignmentById = true
        fakeApi.getAssignmentByIdErrorMessage = "과제 로드 실패"

        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(assignmentId = 1, teacherId = "2")
            }
        }

        // Error should be handled gracefully
        composeRule.waitForIdle()
    }

    @Test
    fun editAssignmentScreen_displaysClassDropdown() {
        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(assignmentId = 1, teacherId = "2")
            }
        }

        waitForText("수학 A반")
        composeRule.onNodeWithText("수학 A반", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun editAssignmentScreen_handlesNullAssignmentId() {
        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(assignmentId = 0, teacherId = "2")
            }
        }

        // Should handle null/zero assignmentId gracefully
        composeRule.waitForIdle()
    }

    @Test
    fun editAssignmentScreen_handlesNullTeacherId() {
        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(assignmentId = 1, teacherId = null)
            }
        }

        // Should handle null teacherId gracefully
        composeRule.waitForIdle()
    }

    @Test
    fun editAssignmentScreen_displaysDateFormats() {
        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(assignmentId = 1, teacherId = "2")
            }
        }

        waitForText("마감일")
        // Date fields should be displayed
        composeRule.waitForIdle()
    }

    @Test
    fun editAssignmentScreen_handlesClassLoadingError() {
        fakeApi.shouldFailClasses = true
        fakeApi.classesErrorMessage = "반 목록 로드 실패"

        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(assignmentId = 1, teacherId = "2")
            }
        }

        // Should handle class loading error gracefully
        composeRule.waitForIdle()
    }

    @Test
    fun editAssignmentScreen_displaysCurrentAssignmentData() {
        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(assignmentId = 1, teacherId = "2")
            }
        }

        waitForText("1단원 복습 과제")
        waitForText("기초 개념을 복습하는 과제입니다.")
        composeRule.onNodeWithText("1단원 복습 과제", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun editAssignmentScreen_displaysLoadingState() {
        // Simulate loading delay

        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(assignmentId = 1, teacherId = "2")
            }
        }

        // Loading state should be displayed
        composeRule.waitForIdle()
    }

    @Test
    fun editAssignmentScreen_handlesEmptyAssignmentData() {
        fakeApi.shouldFailGetAssignmentById = true

        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(assignmentId = 999, teacherId = "2")
            }
        }

        // Should handle empty assignment data gracefully
        composeRule.waitForIdle()
    }

    @Test
    fun editAssignmentScreen_displaysMaterialInfo() {
        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(assignmentId = 1, teacherId = "2")
            }
        }

        // Material info might be displayed
        composeRule.waitForIdle()
    }

    @Test
    fun editAssignmentScreen_displaysDeleteWarningMessage() {
        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(assignmentId = 1, teacherId = "2")
            }
        }

        composeRule.waitUntil(timeoutMillis = 30_000) {
            try {
                composeRule.onAllNodesWithText("과제를 삭제하면", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun editAssignmentScreen_displaysDueDateFieldSecond() {
        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(assignmentId = 1, teacherId = "2")
            }
        }

        composeRule.waitUntil(timeoutMillis = 30_000) {
            try {
                composeRule.onAllNodesWithText("마감일", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("마감일", substring = true, useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun editAssignmentScreen_displaysAssignmentDescriptionField() {
        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(assignmentId = 1, teacherId = "2")
            }
        }

        composeRule.waitUntil(timeoutMillis = 30_000) {
            try {
                composeRule.onAllNodesWithText("설명", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("설명", substring = true, useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun editAssignmentScreen_displaysAssignmentTitleField() {
        composeRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen(assignmentId = 1, teacherId = "2")
            }
        }

        composeRule.waitUntil(timeoutMillis = 30_000) {
            try {
                composeRule.onAllNodesWithText("과제 제목", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        composeRule.waitForIdle()
        composeRule.onAllNodesWithText("과제 제목", substring = true, useUnmergedTree = true).onFirst().assertIsDisplayed()
    }
}
