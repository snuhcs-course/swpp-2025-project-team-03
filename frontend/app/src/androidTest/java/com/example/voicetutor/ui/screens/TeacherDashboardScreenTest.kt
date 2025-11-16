package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.*
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso/Compose UI tests for TeacherDashboardScreen.
 * 
 * NOTE: Disabled due to MockK incompatibility with Android Instrumentation tests.
 */
@Ignore("MockK incompatible with Android tests - use Hilt-based tests instead")
@RunWith(AndroidJUnit4::class)
class TeacherDashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockAuthViewModel: AuthViewModel
    private lateinit var mockAssignmentViewModel: AssignmentViewModel
    private lateinit var mockDashboardViewModel: DashboardViewModel
    private lateinit var mockStudentViewModel: StudentViewModel

    @Before
    fun setup() {
        mockAuthViewModel = mockk(relaxed = true) {
            every { currentUser } returns MutableStateFlow(
                User(
                    id = 1,
                    name = "선생님",
                    email = "teacher@test.com",
                    role = UserRole.TEACHER,
                    isStudent = false
                )
            )
        }
        mockAssignmentViewModel = mockk(relaxed = true) {
            every { assignments } returns MutableStateFlow(emptyList())
            every { isLoading } returns MutableStateFlow(false)
            every { error } returns MutableStateFlow(null)
        }
        mockDashboardViewModel = mockk(relaxed = true) {
            every { dashboardStats } returns MutableStateFlow(null)
        }
        mockStudentViewModel = mockk(relaxed = true) {
            every { students } returns MutableStateFlow(emptyList())
        }
    }

    @Test
    fun displaysWelcomeMessage() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherDashboardScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel
                )
            }
        }

        composeTestRule.onNodeWithText("선생님", substring = true)
            .assertExists()
    }

    @Test
    fun displaysNavigationButtons() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherDashboardScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel
                )
            }
        }

        // Check for navigation buttons
        composeTestRule.onNodeWithText("과제", substring = true)
            .assertExists()
        composeTestRule.onNodeWithText("학생", substring = true)
            .assertExists()
        composeTestRule.onNodeWithText("반", substring = true)
            .assertExists()
    }

    @Test
    fun displaysEmptyStateWhenNoAssignments() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherDashboardScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel
                )
            }
        }

        // Should show empty state or assignment count
        composeTestRule.onNodeWithText("0", substring = true)
            .assertExists()
    }

    @Test
    fun displaysAssignmentsWhenAvailable() {
        val testSubject = Subject(id = 1, name = "수학", code = "MATH")
        val testClass = CourseClass(
            id = 1,
            name = "수학반",
            description = null,
            subject = testSubject,
            teacherName = "선생님",
            
            
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
                
                dueAt = "2025-01-15",
                courseClass = testClass,
                materials = null,
                grade = "중1"
            )
        )

        every { mockAssignmentViewModel.assignments } returns MutableStateFlow(testAssignments)

        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherDashboardScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel
                )
            }
        }

        composeTestRule.onNodeWithText("1단원 복습")
            .assertExists()
    }

    @Test
    fun displaysLoadingState() {
        every { mockAssignmentViewModel.isLoading } returns MutableStateFlow(true)

        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherDashboardScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel
                )
            }
        }

        // Loading indicator should be visible
        composeTestRule.onAllNodesWithContentDescription("Loading")
            .onFirst()
            .assertExists()
    }

    @Test
    fun navigationButtonsAreClickable() {
        var assignmentsClicked = false
        var studentsClicked = false
        var classesClicked = false

        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherDashboardScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel,
                    onNavigateToAllAssignments = { assignmentsClicked = true },
                    onNavigateToAllStudents = { studentsClicked = true },
                    onNavigateToCreateClass = { classesClicked = true }
                )
            }
        }

        // Test navigation button clicks
        composeTestRule.onNodeWithText("과제", substring = true)
            .performClick()
        assert(assignmentsClicked)
    }
}

