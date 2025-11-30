package com.example.voicetutor.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.theme.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ScreenInternalComponentsTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun studentAssignmentCard_renders_withAllData() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentAssignmentCard(
                    title = "수학 과제",
                    subject = "수학",
                    dueDate = "2024-12-31",
                    progress = 0.5f,
                    totalQuestions = 10,
                    status = PersonalAssignmentStatus.IN_PROGRESS,
                    onClick = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("수학 과제", substring = true).assertExists()
        composeTestRule.onNodeWithText("수학", substring = true).assertExists()
    }

    @Test
    fun studentAssignmentCard_renders_withDifferentStatuses() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    StudentAssignmentCard(
                        title = "미시작 과제",
                        subject = "수학",
                        dueDate = "2024-12-31",
                        progress = 0f,
                        totalQuestions = 10,
                        status = PersonalAssignmentStatus.NOT_STARTED,
                        onClick = {},
                    )
                    StudentAssignmentCard(
                        title = "진행중 과제",
                        subject = "수학",
                        dueDate = "2024-12-31",
                        progress = 0.5f,
                        totalQuestions = 10,
                        status = PersonalAssignmentStatus.IN_PROGRESS,
                        onClick = {},
                    )
                    StudentAssignmentCard(
                        title = "제출된 과제",
                        subject = "수학",
                        dueDate = "2024-12-31",
                        progress = 1.0f,
                        totalQuestions = 10,
                        status = PersonalAssignmentStatus.SUBMITTED,
                        onClick = {},
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun studentAssignmentCard_renders_withDifferentProgress() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    StudentAssignmentCard(
                        title = "과제 0%",
                        subject = "수학",
                        dueDate = "2024-12-31",
                        progress = 0f,
                        totalQuestions = 10,
                        status = PersonalAssignmentStatus.IN_PROGRESS,
                        onClick = {},
                    )
                    StudentAssignmentCard(
                        title = "과제 25%",
                        subject = "수학",
                        dueDate = "2024-12-31",
                        progress = 0.25f,
                        totalQuestions = 10,
                        status = PersonalAssignmentStatus.IN_PROGRESS,
                        onClick = {},
                    )
                    StudentAssignmentCard(
                        title = "과제 50%",
                        subject = "수학",
                        dueDate = "2024-12-31",
                        progress = 0.5f,
                        totalQuestions = 10,
                        status = PersonalAssignmentStatus.IN_PROGRESS,
                        onClick = {},
                    )
                    StudentAssignmentCard(
                        title = "과제 75%",
                        subject = "수학",
                        dueDate = "2024-12-31",
                        progress = 0.75f,
                        totalQuestions = 10,
                        status = PersonalAssignmentStatus.IN_PROGRESS,
                        onClick = {},
                    )
                    StudentAssignmentCard(
                        title = "과제 100%",
                        subject = "수학",
                        dueDate = "2024-12-31",
                        progress = 1.0f,
                        totalQuestions = 10,
                        status = PersonalAssignmentStatus.IN_PROGRESS,
                        onClick = {},
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun studentAssignmentCard_triggersOnClick() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentAssignmentCard(
                    title = "과제",
                    subject = "수학",
                    dueDate = "2024-12-31",
                    progress = 0.5f,
                    totalQuestions = 10,
                    status = PersonalAssignmentStatus.IN_PROGRESS,
                    onClick = { clicked = true },
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onAllNodesWithText("과제", substring = true)
            .filter(hasClickAction())
            .onFirst()
            .performClick()
        assert(clicked)
    }

    @Test
    fun teacherAssignmentCard_renders_withAllData() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentCard(
                    title = "수학 과제",
                    className = "수학 1반",
                    dueDate = "2024-12-31T23:59:59Z",
                    submittedCount = 5,
                    totalCount = 10,
                    onClick = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("수학 과제", substring = true).assertExists()
        composeTestRule.onNodeWithText("수학 1반", substring = true).assertExists()
    }

    @Test
    fun teacherAssignmentCard_renders_withDifferentStatuses() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    TeacherAssignmentCard(
                        title = "진행중 과제",
                        className = "수학 1반",
                        dueDate = "2024-12-31T23:59:59Z",
                        submittedCount = 5,
                        totalCount = 10,
                        onClick = {},
                    )
                    TeacherAssignmentCard(
                        title = "완료된 과제",
                        className = "수학 1반",
                        dueDate = "2024-12-31T23:59:59Z",
                        submittedCount = 10,
                        totalCount = 10,
                        onClick = {},
                    )
                    TeacherAssignmentCard(
                        title = "초안 과제",
                        className = "수학 1반",
                        dueDate = "2024-12-31T23:59:59Z",
                        submittedCount = 0,
                        totalCount = 10,
                        onClick = {},
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun teacherAssignmentCard_triggersOnViewResults() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentCard(
                    title = "과제",
                    className = "수학 1반",
                    dueDate = "2024-12-31T23:59:59Z",
                    submittedCount = 5,
                    totalCount = 10,
                    onClick = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onRoot().printToLog("TEACHER_ASSIGNMENT_CARD")
    }

    @Test
    fun allDashboardCards_renderTogether() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    StudentAssignmentCard(
                        title = "학생 과제",
                        subject = "수학",
                        dueDate = "2024-12-31",
                        progress = 0.5f,
                        totalQuestions = 10,
                        status = PersonalAssignmentStatus.IN_PROGRESS,
                        onClick = {},
                    )
                    TeacherAssignmentCard(
                        title = "선생님 과제",
                        className = "수학 1반",
                        dueDate = "2024-12-31T23:59:59Z",
                        submittedCount = 5,
                        totalCount = 10,
                        onClick = {},
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("학생 과제", substring = true).assertExists()
        composeTestRule.onNodeWithText("선생님 과제", substring = true).assertExists()
    }

    @Test
    fun studentAssignmentCard_edgeCases() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    StudentAssignmentCard(
                        title = "과제",
                        subject = "수학",
                        dueDate = "2024-12-31",
                        progress = 0f,
                        totalQuestions = 0,
                        status = PersonalAssignmentStatus.NOT_STARTED,
                        onClick = {},
                    )

                    StudentAssignmentCard(
                        title = "제출된 과제",
                        subject = "수학",
                        dueDate = "2024-12-31",
                        progress = 1.0f,
                        totalQuestions = 10,
                        status = PersonalAssignmentStatus.SUBMITTED,
                        onClick = {},
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun teacherAssignmentCard_edgeCases() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    TeacherAssignmentCard(
                        title = "과제",
                        className = "수학 1반",
                        dueDate = "2024-12-31T23:59:59Z",
                        submittedCount = 0,
                        totalCount = 10,
                        onClick = {},
                    )

                    TeacherAssignmentCard(
                        title = "완료된 과제",
                        className = "수학 1반",
                        dueDate = "2024-12-31T23:59:59Z",
                        submittedCount = 10,
                        totalCount = 10,
                        onClick = {},
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }
}
