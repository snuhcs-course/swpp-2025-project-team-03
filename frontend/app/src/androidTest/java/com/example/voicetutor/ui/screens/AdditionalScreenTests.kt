package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Additional tests to maximize coverage by calling ALL Preview functions
 * and testing more Screen composables directly.
 *
 * Note: Tests that require ViewModel are @Ignore'd as they need Hilt setup.
 */
@RunWith(AndroidJUnit4::class)
class AdditionalScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Test ALL remaining Preview functions
    @Ignore("Requires ViewModel setup")
    @Test
    fun loginScreenPreview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun signupScreenPreview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun createAssignmentScreenPreview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun editAssignmentScreenPreview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun assignmentScreenPreview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun assignmentQuizScreenPreview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentQuizScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun assignmentDetailScreenPreview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun assignmentDetailedResultsScreenPreview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailedResultsScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun teacherAssignmentDetailScreenPreview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentDetailScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun teacherAssignmentResultsScreenPreview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentResultsScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun teacherStudentReportScreenPreview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherStudentReportScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    // Test Screen composables directly (without ViewModel dependencies)
    @Ignore("Requires ViewModel setup")
    @Test
    fun createClassScreen_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CreateClassScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun loginScreen_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun signupScreen_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun allAssignmentsScreen_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllAssignmentsScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun allStudentsScreen_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun teacherClassesScreen_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherClassesScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun teacherStudentsScreen_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun teacherClassDetailScreen_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherClassDetailScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun reportScreen_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                ReportScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun assignmentScreen_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun assignmentDetailScreen_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun createAssignmentScreen_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun editAssignmentScreen_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun teacherAssignmentDetailScreen_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentDetailScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun teacherAssignmentResultsScreen_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentResultsScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun teacherStudentAssignmentDetailScreen_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherStudentAssignmentDetailScreen(
                    studentId = "1",
                    assignmentId = 1,
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun teacherStudentReportScreen_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherStudentReportScreen(
                    classId = 1,
                    studentId = 1,
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    @Ignore("Requires ViewModel setup")
    @Test
    fun assignmentDetailedResultsScreen_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailedResultsScreen(
                    personalAssignmentId = 1,
                )
            }
        }
        composeTestRule.waitForIdle()
    }
}
