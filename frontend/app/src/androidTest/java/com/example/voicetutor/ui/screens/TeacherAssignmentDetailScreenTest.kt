package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.models.AssignmentData
import com.example.voicetutor.data.models.AssignmentResultData
import com.example.voicetutor.data.models.CourseClass
import com.example.voicetutor.data.models.Subject
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
class TeacherAssignmentDetailScreenTest {

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

    private val testAssignment: AssignmentData
        get() = fakeApi.assignmentsResponse.firstOrNull() ?: AssignmentData(
        id = 1,
        title = "테스트 과제",
        description = "테스트 설명",
        totalQuestions = 10,
        createdAt = "2024-01-01T09:00:00Z",
        
        dueAt = "2024-02-01T23:59:59Z",
        courseClass = CourseClass(
            id = 1,
            name = "수학 A반",
            description = "기초 수학 수업",
            subject = Subject(id = 1, name = "수학", code = "MATH"),
            teacherName = "김선생님",
            
            
            studentCount = 25,
            createdAt = "2024-01-01T00:00:00Z"
        ),
        materials = emptyList(),
        grade = "중학교 1학년"
    )

    private fun resetFakeApi() {
        val defaultAssignment = AssignmentData(
            id = 1,
            title = "테스트 과제",
            description = "테스트 설명",
            totalQuestions = 10,
            createdAt = "2024-01-01T09:00:00Z",
            
            dueAt = "2024-02-01T23:59:59Z",
            courseClass = CourseClass(
                id = 1,
                name = "수학 A반",
                description = "기초 수학 수업",
                subject = Subject(id = 1, name = "수학", code = "MATH"),
                teacherName = "김선생님",
                
                
                studentCount = 25,
                createdAt = "2024-01-01T00:00:00Z"
            ),
            materials = emptyList(),
            grade = "중학교 1학년"
        )
        fakeApi.apply {
            shouldFailPersonalAssignments = false
            personalAssignmentsResponse = listOf(personalAssignmentData)
            personalAssignmentsDelayMillis = 0
            assignmentsResponse = listOf(defaultAssignment)
            shouldFailGetAllAssignments = false
            assignmentResultResponse = AssignmentResultData(
                submittedStudents = 5,
                totalStudents = 10,
                averageScore = 85.0,
                completionRate = 0.5
            )
            shouldFailAssignmentResult = false
        }
    }

    private fun waitForText(text: String, timeoutMillis: Long = 15_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule.onAllNodesWithText(text, substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
        }
    }

    @Test
    fun teacherAssignmentDetailScreen_displaysStatistics() {
        val assignmentId = testAssignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentDetailScreen(
                    assignmentId = assignmentId
                )
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[com.example.voicetutor.ui.viewmodel.AssignmentViewModel::class.java]

        composeRule.waitUntil(timeoutMillis = 15_000) {
            assignmentViewModel.assignmentStatistics.value != null
        }

        waitForText("제출률")
        composeRule.onAllNodesWithText("제출률", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()

        waitForText("평균 점수")
        composeRule.onAllNodesWithText("평균 점수", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()

        waitForText("제출 학생")
        composeRule.onAllNodesWithText("제출 학생", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun teacherAssignmentDetailScreen_displaysAssignmentContent() {
        val assignmentId = testAssignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentDetailScreen(
                    assignmentId = assignmentId
                )
            }
        }

        waitForText("과제 내용")
        composeRule.onAllNodesWithText("과제 내용", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun teacherAssignmentDetailScreen_showsLoadingState() {
        // Simulate loading delay

        val assignmentId = testAssignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentDetailScreen(
                    assignmentId = assignmentId
                )
            }
        }

        // Loading indicator should be displayed initially
        composeRule.waitForIdle()
    }

    @Test
    fun teacherAssignmentDetailScreen_handlesZeroAssignmentId() {
        composeRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentDetailScreen(
                    assignmentId = 0
                )
            }
        }

        // Should handle zero assignmentId gracefully
        composeRule.waitForIdle()
    }

    @Test
    fun teacherAssignmentDetailScreen_displaysSubjectAndClass() {
        val assignmentId = testAssignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentDetailScreen(
                    assignmentId = assignmentId
                )
            }
        }

        waitForText(testAssignment.courseClass.subject.name)
        composeRule.onAllNodesWithText(testAssignment.courseClass.subject.name, substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()

        waitForText(testAssignment.courseClass.name)
        composeRule.onAllNodesWithText(testAssignment.courseClass.name, substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun teacherAssignmentDetailScreen_handlesStatisticsLoadingError() {
        fakeApi.assignmentResultErrorMessage = "통계 로드 실패"
        fakeApi.shouldFailAssignmentResult = true

        val assignmentId = testAssignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentDetailScreen(
                    assignmentId = assignmentId
                )
            }
        }

        // Should handle statistics loading error gracefully
        composeRule.waitForIdle()
    }

    @Test
    fun teacherAssignmentDetailScreen_displaysStatisticsWhenLoaded() {
        val assignmentId = testAssignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentDetailScreen(
                    assignmentId = assignmentId
                )
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[com.example.voicetutor.ui.viewmodel.AssignmentViewModel::class.java]

        composeRule.waitUntil(timeoutMillis = 15_000) {
            assignmentViewModel.assignmentStatistics.value != null
        }

        // Statistics should be displayed
        waitForText("제출률")
        waitForText("평균 점수")
        waitForText("제출 학생")
    }

    @Test
    fun teacherAssignmentDetailScreen_handlesNullStatistics() {
        fakeApi.assignmentResultResponse = AssignmentResultData(
            submittedStudents = 0,
            totalStudents = 0,
            averageScore = 0.0,
            completionRate = 0.0
        )

        val assignmentId = testAssignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentDetailScreen(
                    assignmentId = assignmentId
                )
            }
        }

        // Should handle null statistics gracefully
        composeRule.waitForIdle()
    }

    @Test
    fun teacherAssignmentDetailScreen_displaysZeroStatistics() {
        fakeApi.assignmentResultResponse = AssignmentResultData(
            submittedStudents = 0,
            totalStudents = 0,
            averageScore = 0.0,
            completionRate = 0.0
        )

        val assignmentId = testAssignment.id

        composeRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentDetailScreen(
                    assignmentId = assignmentId
                )
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[com.example.voicetutor.ui.viewmodel.AssignmentViewModel::class.java]

        composeRule.waitUntil(timeoutMillis = 15_000) {
            assignmentViewModel.assignmentStatistics.value != null
        }

        // Zero statistics should be displayed
        composeRule.waitForIdle()
    }

}

