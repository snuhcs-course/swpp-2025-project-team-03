package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso/Compose UI tests for AssignmentDetailScreen.
 * 
 * NOTE: Disabled due to MockK incompatibility with Android Instrumentation tests.
 */
@Ignore("MockK incompatible with Android tests - use Hilt-based tests instead")
@RunWith(AndroidJUnit4::class)
class AssignmentDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockAssignmentViewModel: AssignmentViewModel

    @Before
    fun setup() {
        mockAssignmentViewModel = mockk(relaxed = true) {
            every { currentAssignment } returns MutableStateFlow(null)
            every { isLoading } returns MutableStateFlow(false)
            every { error } returns MutableStateFlow(null)
        }
    }

    @Test
    fun displaysAssignmentDetails() {
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

        val testAssignment = AssignmentData(
            id = 1,
            title = "1단원 복습",
            description = "설명",
            totalQuestions = 5,
            createdAt = "2025-01-01",
            visibleFrom = "2025-01-01",
            dueAt = "2025-01-15",
            courseClass = testClass,
            materials = null,
            grade = "중1"
        )

        every { mockAssignmentViewModel.currentAssignment } returns MutableStateFlow(testAssignment)

        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailScreen(
                    assignmentId = 1,
                    assignmentViewModelParam = mockAssignmentViewModel
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
                AssignmentDetailScreen(
                    assignmentId = 1,
                    assignmentViewModelParam = mockAssignmentViewModel
                )
            }
        }

        composeTestRule.onAllNodesWithContentDescription("Loading")
            .onFirst()
            .assertExists()
    }
}

