package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests that call actual Screen Preview composables.
 * This ensures the actual Screen code is executed, increasing coverage.
 *
 * Note: Most Preview tests are ignored because they require ViewModel setup.
 * Only tests for screens without ViewModel dependencies are enabled.
 */
@RunWith(AndroidJUnit4::class)
class ScreenPreviewTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun noRecentAssignmentScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("이어할 과제가 없습니다", substring = true).assertExists()
    }

    @Test
    fun noRecentAssignmentScreen_direct_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen()
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("이어할 과제가 없습니다", substring = true).assertExists()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun loginScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun signupScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun studentDashboardScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun teacherDashboardScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherDashboardScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun allAssignmentsScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllAssignmentsScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun allStudentsScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun teacherClassesScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherClassesScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun teacherStudentsScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun teacherClassDetailScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherClassDetailScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun teacherStudentReportScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherStudentReportScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun createAssignmentScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun editAssignmentScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                EditAssignmentScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun teacherAssignmentResultsScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentResultsScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun teacherAssignmentDetailScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentDetailScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun assignmentScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun assignmentQuizScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentQuizScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun reportScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                ReportScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun settingsScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SettingsScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun assignmentDetailScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    @Ignore("Requires ViewModel setup")
    fun assignmentDetailedResultsScreen_preview_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailedResultsScreenPreview()
            }
        }
        composeTestRule.waitForIdle()
    }
}
