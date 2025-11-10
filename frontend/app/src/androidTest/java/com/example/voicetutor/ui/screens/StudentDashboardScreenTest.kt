package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import com.example.voicetutor.ui.viewmodel.DashboardViewModel
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso/Compose UI tests for StudentDashboardScreen.
 * 
 * These tests use mocked ViewModels to control UI state without modifying
 * the original StudentDashboardScreen.kt file.
 * 
 * NOTE: Disabled due to MockK incompatibility with Android Instrumentation tests.
 */
@Ignore("MockK incompatible with Android tests - use Hilt-based tests instead")
@RunWith(AndroidJUnit4::class)
class StudentDashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockAuthViewModel: AuthViewModel
    private lateinit var mockAssignmentViewModel: AssignmentViewModel
    private lateinit var mockDashboardViewModel: DashboardViewModel

    @Before
    fun setup() {
        // Create mock ViewModels with relaxed mocking for StateFlow properties
        mockAuthViewModel = mockk(relaxed = true) {
            // Default empty StateFlows
            every { currentUser } returns MutableStateFlow(null)
        }
        mockAssignmentViewModel = mockk(relaxed = true) {
            every { assignments } returns MutableStateFlow(emptyList())
            every { isLoading } returns MutableStateFlow(false)
            every { error } returns MutableStateFlow(null)
            every { studentStats } returns MutableStateFlow(null)
        }
        mockDashboardViewModel = mockk(relaxed = true)
    }

    @Test
    fun displaysWelcomeMessage() {
        // Given: A logged-in student
        val testUser = User(
            id = 1,
            name = "홍길동",
            email = "student@test.com",
            role = UserRole.STUDENT,
            isStudent = true
        )
        
        every { mockAuthViewModel.currentUser } returns MutableStateFlow(testUser)
        every { mockAssignmentViewModel.assignments } returns MutableStateFlow(emptyList())
        every { mockAssignmentViewModel.isLoading } returns MutableStateFlow(false)
        every { mockAssignmentViewModel.error } returns MutableStateFlow(null)
        every { mockAssignmentViewModel.studentStats } returns MutableStateFlow(null)

        // When: Screen is displayed
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel,
                    dashboardViewModel = mockDashboardViewModel
                )
            }
        }

        // Then: Welcome message should be visible
        composeTestRule.onNodeWithText("안녕하세요, 홍길동님!", substring = true)
            .assertExists()
        composeTestRule.onNodeWithText("오늘도 VoiceTutor와 함께 학습을 시작해볼까요?")
            .assertExists()
    }

    @Test
    fun displaysEmptyStateWhenNoAssignments() {
        // Given: Student with no assignments
        val testUser = User(
            id = 1,
            name = "홍길동",
            email = "student@test.com",
            role = UserRole.STUDENT,
            isStudent = true
        )
        
        every { mockAuthViewModel.currentUser } returns MutableStateFlow(testUser)
        every { mockAssignmentViewModel.assignments } returns MutableStateFlow(emptyList())
        every { mockAssignmentViewModel.isLoading } returns MutableStateFlow(false)
        every { mockAssignmentViewModel.error } returns MutableStateFlow(null)
        every { mockAssignmentViewModel.studentStats } returns MutableStateFlow(null)

        // When: Screen is displayed
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel,
                    dashboardViewModel = mockDashboardViewModel
                )
            }
        }

        // Then: Empty state message should be visible
        composeTestRule.onNodeWithText("과제가 없습니다")
            .assertExists()
        composeTestRule.onNodeWithText("나에게 할당된 과제 0개")
            .assertExists()
    }

    @Test
    fun displaysLoadingIndicator() {
        // Given: Loading state
        val testUser = User(
            id = 1,
            name = "홍길동",
            email = "student@test.com",
            role = UserRole.STUDENT,
            isStudent = true
        )
        
        every { mockAuthViewModel.currentUser } returns MutableStateFlow(testUser)
        every { mockAssignmentViewModel.assignments } returns MutableStateFlow(emptyList())
        every { mockAssignmentViewModel.isLoading } returns MutableStateFlow(true)
        every { mockAssignmentViewModel.error } returns MutableStateFlow(null)
        every { mockAssignmentViewModel.studentStats } returns MutableStateFlow(null)

        // When: Screen is displayed
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel,
                    dashboardViewModel = mockDashboardViewModel
                )
            }
        }

        // Then: Loading indicator should be visible (CircularProgressIndicator)
        // Note: We can't directly test CircularProgressIndicator, but we can verify
        // that assignment cards are not shown during loading
        composeTestRule.onNodeWithText("과제가 없습니다")
            .assertDoesNotExist()
    }

    @Test
    fun displaysAssignmentCards() {
        // Given: Student with assignments
        val testUser = User(
            id = 1,
            name = "홍길동",
            email = "student@test.com",
            role = UserRole.STUDENT,
            isStudent = true
        )

        val testSubject = Subject(id = 1, name = "수학", code = "MATH")
        val testClass = CourseClass(
            id = 1,
            name = "수학반",
            description = null,
            subject = testSubject,
            teacherName = "선생님",
            startDate = "2025-01-01",
            endDate = "2025-12-31",
            studentCount = 10,
            createdAt = "2025-01-01"
        )

        val testAssignments = listOf(
            AssignmentData(
                id = 1,
                title = "1단원 복습",
                description = "설명",
                totalQuestions = 5,
                createdAt = "2025-01-01",
                visibleFrom = "2025-01-01",
                dueAt = "2025-01-15",
                courseClass = testClass,
                materials = null,
                grade = "중1",
                personalAssignmentStatus = PersonalAssignmentStatus.IN_PROGRESS,
                solvedNum = 2,
                personalAssignmentId = 10,
                submittedAt = null
            ),
            AssignmentData(
                id = 2,
                title = "2단원 예습",
                description = "설명",
                totalQuestions = 3,
                createdAt = "2025-01-01",
                visibleFrom = "2025-01-01",
                dueAt = "2025-01-20",
                courseClass = testClass,
                materials = null,
                grade = "중1",
                personalAssignmentStatus = PersonalAssignmentStatus.NOT_STARTED,
                solvedNum = 0,
                personalAssignmentId = 11,
                submittedAt = null
            )
        )
        
        every { mockAuthViewModel.currentUser } returns MutableStateFlow(testUser)
        every { mockAssignmentViewModel.assignments } returns MutableStateFlow(testAssignments)
        every { mockAssignmentViewModel.isLoading } returns MutableStateFlow(false)
        every { mockAssignmentViewModel.error } returns MutableStateFlow(null)
        every { mockAssignmentViewModel.studentStats } returns MutableStateFlow(null)

        // When: Screen is displayed
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel,
                    dashboardViewModel = mockDashboardViewModel
                )
            }
        }

        // Then: Assignment cards should be visible
        composeTestRule.onNodeWithText("나에게 할당된 과제 2개")
            .assertExists()
        composeTestRule.onNodeWithText("1단원 복습")
            .assertExists()
        composeTestRule.onNodeWithText("2단원 예습")
            .assertExists()
        composeTestRule.onNodeWithText("과제 시작", substring = true)
            .assertExists()
        composeTestRule.onNodeWithText("과제 상세", substring = true)
            .assertExists()
    }

    @Test
    fun displaysFilterChips() {
        // Given: Student with assignments
        val testUser = User(
            id = 1,
            name = "홍길동",
            email = "student@test.com",
            role = UserRole.STUDENT,
            isStudent = true
        )
        
        every { mockAuthViewModel.currentUser } returns MutableStateFlow(testUser)
        every { mockAssignmentViewModel.assignments } returns MutableStateFlow(emptyList())
        every { mockAssignmentViewModel.isLoading } returns MutableStateFlow(false)
        every { mockAssignmentViewModel.error } returns MutableStateFlow(null)
        every { mockAssignmentViewModel.studentStats } returns MutableStateFlow(null)

        // When: Screen is displayed
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel,
                    dashboardViewModel = mockDashboardViewModel
                )
            }
        }

        // Then: Filter chips should be visible
        composeTestRule.onNodeWithText("전체")
            .assertExists()
        composeTestRule.onNodeWithText("시작 안함")
            .assertExists()
        composeTestRule.onNodeWithText("진행 중")
            .assertExists()
    }

    @Test
    fun filterChipsAreClickable() {
        // Given: Student with assignments
        val testUser = User(
            id = 1,
            name = "홍길동",
            email = "student@test.com",
            role = UserRole.STUDENT,
            isStudent = true
        )
        
        every { mockAuthViewModel.currentUser } returns MutableStateFlow(testUser)
        every { mockAssignmentViewModel.assignments } returns MutableStateFlow(emptyList())
        every { mockAssignmentViewModel.isLoading } returns MutableStateFlow(false)
        every { mockAssignmentViewModel.error } returns MutableStateFlow(null)
        every { mockAssignmentViewModel.studentStats } returns MutableStateFlow(null)

        // When: Screen is displayed
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel,
                    dashboardViewModel = mockDashboardViewModel
                )
            }
        }

        // Then: Filter chips should be clickable
        composeTestRule.onNodeWithText("시작 안함")
            .assertIsEnabled()
            .performClick()
        
        composeTestRule.onNodeWithText("진행 중")
            .assertIsEnabled()
            .performClick()
    }

    @Test
    fun displaysAssignmentCount() {
        // Given: Student with 3 assignments
        val testUser = User(
            id = 1,
            name = "홍길동",
            email = "student@test.com",
            role = UserRole.STUDENT,
            isStudent = true
        )

        val testSubject = Subject(id = 1, name = "수학", code = "MATH")
        val testClass = CourseClass(
            id = 1,
            name = "수학반",
            description = null,
            subject = testSubject,
            teacherName = "선생님",
            startDate = "2025-01-01",
            endDate = "2025-12-31",
            studentCount = 10,
            createdAt = "2025-01-01"
        )

        val testAssignments = (1..3).map { i ->
            AssignmentData(
                id = i,
                title = "과제 $i",
                description = "설명",
                totalQuestions = 5,
                createdAt = "2025-01-01",
                visibleFrom = "2025-01-01",
                dueAt = "2025-01-15",
                courseClass = testClass,
                materials = null,
                grade = "중1",
                personalAssignmentStatus = PersonalAssignmentStatus.IN_PROGRESS,
                solvedNum = 2,
                personalAssignmentId = 10 + i,
                submittedAt = null
            )
        }
        
        every { mockAuthViewModel.currentUser } returns MutableStateFlow(testUser)
        every { mockAssignmentViewModel.assignments } returns MutableStateFlow(testAssignments)
        every { mockAssignmentViewModel.isLoading } returns MutableStateFlow(false)
        every { mockAssignmentViewModel.error } returns MutableStateFlow(null)
        every { mockAssignmentViewModel.studentStats } returns MutableStateFlow(null)

        // When: Screen is displayed
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel,
                    dashboardViewModel = mockDashboardViewModel
                )
            }
        }

        // Then: Assignment count should be displayed correctly
        composeTestRule.onNodeWithText("나에게 할당된 과제 3개")
            .assertExists()
    }
}

