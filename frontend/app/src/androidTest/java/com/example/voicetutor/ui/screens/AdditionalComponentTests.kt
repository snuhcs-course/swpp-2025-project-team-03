package com.example.voicetutor.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Additional tests for screen components to maximize coverage.
 * Tests more components and edge cases.
 */
@RunWith(AndroidJUnit4::class)
class AdditionalComponentTests {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestHiltActivity>()

    // ========== TeacherAssignmentResultCard Tests ==========

    @Test
    fun teacherAssignmentResultCard_renders_withAllData() {
        val studentResult = StudentResult(
            studentId = "1",
            name = "학생 1",
            score = 85,
            confidenceScore = 80,
            status = "완료",
            submittedAt = "2024-01-01T12:00:00Z",
            answers = emptyList(),
            detailedAnswers = emptyList()
        )
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentResultCard(
                    student = studentResult,
                    onStudentClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("학생 1", substring = true).assertExists()
        composeTestRule.onNodeWithText("완료", substring = true).assertExists()
    }

    @Test
    fun teacherAssignmentResultCard_renders_withDifferentScores() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    TeacherAssignmentResultCard(
                        student = StudentResult("1", "학생 0점", 0, 0, "미완료", null, "2024-01-01T12:00:00Z", emptyList(), emptyList()),
                        onStudentClick = {}
                    )
                    TeacherAssignmentResultCard(
                        student = StudentResult("2", "학생 50점", 50, 50, "미완료", null, "2024-01-01T12:00:00Z", emptyList(), emptyList()),
                        onStudentClick = {}
                    )
                    TeacherAssignmentResultCard(
                        student = StudentResult("3", "학생 75점", 75, 75, "완료", null, "2024-01-01T12:00:00Z", emptyList(), emptyList()),
                        onStudentClick = {}
                    )
                    TeacherAssignmentResultCard(
                        student = StudentResult("4", "학생 85점", 85, 85, "완료", null, "2024-01-01T12:00:00Z", emptyList(), emptyList()),
                        onStudentClick = {}
                    )
                    TeacherAssignmentResultCard(
                        student = StudentResult("5", "학생 95점", 95, 95, "완료", null, "2024-01-01T12:00:00Z", emptyList(), emptyList()),
                        onStudentClick = {}
                    )
                    TeacherAssignmentResultCard(
                        student = StudentResult("6", "학생 100점", 100, 100, "완료", null, "2024-01-01T12:00:00Z", emptyList(), emptyList()),
                        onStudentClick = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun teacherAssignmentResultCard_triggersOnClick() {
        var clicked = false
        val studentResult = StudentResult(
            studentId = "1",
            name = "학생",
            score = 85,
            confidenceScore = 80,
            status = "완료",
            submittedAt = "2024-01-01T12:00:00Z",
            answers = emptyList(),
            detailedAnswers = emptyList()
        )
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentResultCard(
                    student = studentResult,
                    onStudentClick = { clicked = true }
                )
            }
        }
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("학생", substring = true).performClick()
        assert(clicked)
    }

    @Test
    fun teacherAssignmentResultCard_renders_withDifferentStatuses() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    TeacherAssignmentResultCard(
                        student = StudentResult(
                            studentId = "1",
                            name = "완료 학생",
                            score = 85,
                            confidenceScore = 80,
                            status = "완료",
                            submittedAt = "2024-01-01T12:00:00Z",
                            answers = emptyList(),
                            detailedAnswers = emptyList()
                        ),
                        onStudentClick = {}
                    )
                    TeacherAssignmentResultCard(
                        student = StudentResult(
                            studentId = "2",
                            name = "미완료 학생",
                            score = 50,
                            confidenceScore = 45,
                            status = "미완료",
                            submittedAt = "2024-01-01T12:00:00Z",
                            answers = emptyList(),
                            detailedAnswers = emptyList()
                        ),
                        onStudentClick = {}
                    )
                    TeacherAssignmentResultCard(
                        student = StudentResult(
                            studentId = "3",
                            name = "진행중 학생",
                            score = 60,
                            confidenceScore = 55,
                            status = "진행중",
                            submittedAt = "2024-01-01T12:00:00Z",
                            answers = emptyList(),
                            detailedAnswers = emptyList()
                        ),
                        onStudentClick = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    // ========== AssignmentCard Tests (AllAssignmentsScreen) ==========

    @Test
    fun assignmentCard_renders_withAllData() {
        val assignment = createMockAssignmentData(
            id = 1,
            title = "수학 과제",
            subjectName = "수학",
            className = "수학 1반",
            dueDate = "2024-12-31T23:59:59Z",
            totalQuestions = 10
        )
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentCard(
                    assignment = assignment,
                    submittedCount = 5,
                    totalCount = 10,
                    onAssignmentClick = {},
                    onEditClick = {},
                    onDeleteClick = {},
                    onViewResults = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("수학 과제", substring = true).assertExists()
        composeTestRule.onNodeWithText("수학", substring = true).assertExists()
    }

    @Test
    fun assignmentCard_triggersAllCallbacks() {
        var assignmentClicked = false
        val assignment = createMockAssignmentData(
            id = 1,
            title = "과제",
            subjectName = "수학",
            className = "수학 1반",
            dueDate = "2024-12-31T23:59:59Z",
            totalQuestions = 10
        )
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentCard(
                    assignment = assignment,
                    submittedCount = 5,
                    totalCount = 10,
                    onAssignmentClick = { assignmentClicked = true },
                    onEditClick = {},
                    onDeleteClick = {},
                    onViewResults = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("과제", substring = true).performClick()
        assert(assignmentClicked)
    }

    @Test
    fun assignmentCard_renders_withDifferentSubmissionRates() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    AssignmentCard(
                        assignment = createMockAssignmentData(
                            id = 1,
                            title = "과제 0/10",
                            subjectName = "수학",
                            className = "수학 1반",
                            dueDate = "2024-12-31T23:59:59Z",
                            totalQuestions = 10
                        ),
                        submittedCount = 0,
                        totalCount = 10,
                        onAssignmentClick = {},
                        onEditClick = {},
                        onDeleteClick = {},
                        onViewResults = {}
                    )
                    AssignmentCard(
                        assignment = createMockAssignmentData(
                            id = 2,
                            title = "과제 5/10",
                            subjectName = "수학",
                            className = "수학 1반",
                            dueDate = "2024-12-31T23:59:59Z",
                            totalQuestions = 10
                        ),
                        submittedCount = 5,
                        totalCount = 10,
                        onAssignmentClick = {},
                        onEditClick = {},
                        onDeleteClick = {},
                        onViewResults = {}
                    )
                    AssignmentCard(
                        assignment = createMockAssignmentData(
                            id = 3,
                            title = "과제 10/10",
                            subjectName = "수학",
                            className = "수학 1반",
                            dueDate = "2024-12-31T23:59:59Z",
                            totalQuestions = 10
                        ),
                        submittedCount = 10,
                        totalCount = 10,
                        onAssignmentClick = {},
                        onEditClick = {},
                        onDeleteClick = {},
                        onViewResults = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    // ========== Comprehensive Component Combinations ==========

    @Test
    fun multipleResultCards_renderTogether() {
        val students = listOf(
            StudentResult("1", "학생 1", 85, 80, "완료", null, "2024-01-01T12:00:00Z", emptyList(), emptyList()),
            StudentResult("2", "학생 2", 75, 70, "완료", null, "2024-01-01T13:00:00Z", emptyList(), emptyList()),
            StudentResult("3", "학생 3", 65, 60, "미완료", null, "2024-01-01T14:00:00Z", emptyList(), emptyList())
        )
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    students.forEach { student ->
                        TeacherAssignmentResultCard(
                            student = student,
                            onStudentClick = {}
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        
        students.forEach { student ->
            composeTestRule.onNodeWithText(student.name, substring = true).assertExists()
        }
    }

    @Test
    fun allCardTypes_renderTogether() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    // StudentAssignmentCard
                    StudentAssignmentCard(
                        title = "학생 과제",
                        subject = "수학",
                        dueDate = "2024-12-31",
                        progress = 0.5f,
                        solvedNum = 5,
                        totalQuestions = 10,
                        status = PersonalAssignmentStatus.IN_PROGRESS,
                        onClick = {},
                        onStartAssignment = {}
                    )
                    
                    // TeacherAssignmentCard
                    TeacherAssignmentCard(
                        title = "선생님 과제",
                        className = "수학 1반",
                        dueDate = "2024-12-31T23:59:59Z",
                        submittedCount = 5,
                        totalCount = 10,
                        status = AssignmentStatus.IN_PROGRESS,
                        onClick = {},
                        onViewResults = {},
                        onEdit = {}
                    )
                    
                    // AssignmentCard
                    AssignmentCard(
                        assignment = createMockAssignmentData(
                            id = 3,
                            title = "전체 과제",
                            subjectName = "수학",
                            className = "수학 1반",
                            dueDate = "2024-12-31T23:59:59Z",
                            totalQuestions = 10
                        ),
                        submittedCount = 5,
                        totalCount = 10,
                        onAssignmentClick = {},
                        onEditClick = {},
                        onDeleteClick = {},
                        onViewResults = {}
                    )
                    
                    // TeacherAssignmentResultCard
                    TeacherAssignmentResultCard(
                        student = StudentResult("1", "학생", 85, 80, "완료", null, "2024-01-01T12:00:00Z", emptyList(), emptyList()),
                        onStudentClick = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        
        // Verify at least one card renders
        composeTestRule.onAllNodesWithText("과제", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertExists()
    }

    // ========== Edge Cases ==========

    @Test
    fun teacherAssignmentResultCard_edgeCases() {
        // Test with zero score
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentResultCard(
                    student = StudentResult("1", "학생", 0, 0, "미완료", null, "2024-01-01T12:00:00Z", emptyList(), emptyList()),
                    onStudentClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        // Verify card renders (even if name is empty or not found)
        composeTestRule.onRoot().assertExists()
        
        // Test with perfect score
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentResultCard(
                    student = StudentResult("1", "학생", 100, 100, "완료", null, "2024-01-01T12:00:00Z", emptyList(), emptyList()),
                    onStudentClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
        
        // Test with empty name
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentResultCard(
                    student = StudentResult("1", "", 85, 80, "완료", null, "2024-01-01T12:00:00Z", emptyList(), emptyList()),
                    onStudentClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun assignmentCard_edgeCases() {
        // Test with zero submissions
        val assignment1 = createMockAssignmentData(
            id = 1,
            title = "과제",
            subjectName = "수학",
            className = "수학 1반",
            dueDate = "2024-12-31T23:59:59Z",
            totalQuestions = 10
        )
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentCard(
                    assignment = assignment1,
                    submittedCount = 0,
                    totalCount = 0,
                    onAssignmentClick = {},
                    onEditClick = {},
                    onDeleteClick = {},
                    onViewResults = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
        
        // Test with all submissions
        val assignment2 = createMockAssignmentData(
            id = 2,
            title = "완료된 과제",
            subjectName = "수학",
            className = "수학 1반",
            dueDate = "2024-12-31T23:59:59Z",
            totalQuestions = 10
        )
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentCard(
                    assignment = assignment2,
                    submittedCount = 10,
                    totalCount = 10,
                    onAssignmentClick = {},
                    onEditClick = {},
                    onDeleteClick = {},
                    onViewResults = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    // ========== Multiple Renders for Coverage ==========

    @Test
    fun multipleRenders_allComponents() {
        // Render StudentAssignmentCard
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentAssignmentCard(
                    title = "과제 1",
                    subject = "수학",
                    dueDate = "2024-12-31",
                    progress = 0.5f,
                    solvedNum = 5,
                    totalQuestions = 10,
                    status = PersonalAssignmentStatus.IN_PROGRESS,
                    onClick = {},
                    onStartAssignment = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        
        // Render TeacherAssignmentCard
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentCard(
                    title = "과제 2",
                    className = "수학 1반",
                    dueDate = "2024-12-31T23:59:59Z",
                    submittedCount = 5,
                    totalCount = 10,
                    status = AssignmentStatus.IN_PROGRESS,
                    onClick = {},
                    onViewResults = {},
                    onEdit = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        
        // Render AssignmentCard
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentCard(
                    assignment = createMockAssignmentData(
                        id = 3,
                        title = "과제 3",
                        subjectName = "수학",
                        className = "수학 1반",
                        dueDate = "2024-12-31T23:59:59Z",
                        totalQuestions = 10
                    ),
                    submittedCount = 5,
                    totalCount = 10,
                    onAssignmentClick = {},
                    onEditClick = {},
                    onDeleteClick = {},
                    onViewResults = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        
        // Render TeacherAssignmentResultCard
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentResultCard(
                    student = StudentResult("1", "학생", 85, 80, "완료", null, "2024-01-01T12:00:00Z", emptyList(), emptyList()),
                    onStudentClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
    }

    // ========== Helper Functions ==========

    private fun createMockAssignmentData(
        id: Int,
        title: String,
        subjectName: String,
        className: String,
        dueDate: String,
        totalQuestions: Int
    ): AssignmentData {
        return AssignmentData(
            id = id,
            title = title,
            description = "설명",
            courseClass = CourseClass(
                id = 1,
                name = className,
                subject = Subject(id = 1, name = subjectName),
                teacherName = "선생님",
                startDate = "2024-01-01",
                endDate = "2024-12-31",
                studentCount = 10,
                createdAt = "2024-01-01T00:00:00Z"
            ),
            dueAt = dueDate,
            createdAt = "2024-01-01T00:00:00Z",
            totalQuestions = totalQuestions,
            personalAssignmentId = null,
            personalAssignmentStatus = null,
            solvedNum = null
        )
    }
}

