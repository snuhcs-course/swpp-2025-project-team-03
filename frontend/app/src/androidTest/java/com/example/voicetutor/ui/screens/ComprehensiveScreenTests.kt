package com.example.voicetutor.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ComprehensiveScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun studentAssignmentCard_NOT_STARTED_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentAssignmentCard(
                    title = "테스트 과제",
                    subject = "수학",
                    dueDate = "2024-12-31",
                    progress = 0f,
                    totalQuestions = 10,
                    status = PersonalAssignmentStatus.NOT_STARTED,
                    onClick = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("테스트 과제", substring = true).assertExists()
    }

    @Test
    fun studentAssignmentCard_IN_PROGRESS_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentAssignmentCard(
                    title = "진행 중 과제",
                    subject = "과학",
                    dueDate = "2024-12-31",
                    progress = 0.5f,
                    totalQuestions = 10,
                    status = PersonalAssignmentStatus.IN_PROGRESS,
                    onClick = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("진행 중 과제", substring = true).assertExists()
    }

    @Test
    fun studentAssignmentCard_SUBMITTED_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentAssignmentCard(
                    title = "제출 완료 과제",
                    subject = "영어",
                    dueDate = "2024-12-31",
                    progress = 1f,
                    totalQuestions = 10,
                    status = PersonalAssignmentStatus.SUBMITTED,
                    onClick = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("제출 완료 과제", substring = true).assertExists()
    }

    @Test
    fun teacherAssignmentCard_DRAFT_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentCard(
                    title = "초안 과제",
                    className = "테스트 클래스",
                    submittedCount = 0,
                    totalCount = 10,
                    dueDate = "2024-12-31T23:59:59Z",
                    onClick = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("초안 과제", substring = true).assertExists()
    }

    @Test
    fun teacherAssignmentCard_IN_PROGRESS_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentCard(
                    title = "진행 중 과제",
                    className = "테스트 클래스",
                    submittedCount = 5,
                    totalCount = 10,
                    dueDate = "2024-12-31T23:59:59Z",
                    onClick = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("진행 중 과제", substring = true).assertExists()
    }

    @Test
    fun teacherAssignmentCard_COMPLETED_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentCard(
                    title = "완료된 과제",
                    className = "테스트 클래스",
                    submittedCount = 10,
                    totalCount = 10,
                    dueDate = "2024-12-31T23:59:59Z",
                    onClick = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("완료된 과제", substring = true).assertExists()
    }

    @Test
    fun classCard_renders() {
        val classRoom = ClassRoom(
            id = 1,
            name = "1학년 1반",
            subject = "수학",
            description = "수학 클래스",
            studentCount = 25,
            assignmentCount = 5,
            completionRate = 0.8f,
            color = androidx.compose.ui.graphics.Color(0xFF6200EE),
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                ClassCard(
                    classRoom = classRoom,
                    onClassClick = {},
                    onCreateAssignment = {},
                    onViewStudents = {},
                    onDeleteClass = { _, _ -> },
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("1학년 1반", substring = true).assertExists()
    }

    @Test
    fun allStudentsCard_withClasses_renders() {
        val student = AllStudentsStudent(
            id = 1,
            name = "홍길동",
            email = "hong@test.com",
            role = UserRole.STUDENT,
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllStudentsCard(
                    student = student,
                    onReportClick = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("홍길동", substring = true).assertExists()
    }

    @Test
    fun allStudentsCard_withoutClasses_renders() {
        val student = AllStudentsStudent(
            id = 2,
            name = "김철수",
            email = "kim@test.com",
            role = UserRole.STUDENT,
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllStudentsCard(
                    student = student,
                    onReportClick = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("김철수", substring = true).assertExists()
    }

    @Test
    fun allStudentsCard_loadingClasses_renders() {
        val student = AllStudentsStudent(
            id = 3,
            name = "이영희",
            email = "lee@test.com",
            role = UserRole.STUDENT,
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllStudentsCard(
                    student = student,
                    onReportClick = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("이영희", substring = true).assertExists()
    }

    @Test
    fun assignmentCard_renders() {
        val assignment = AssignmentData(
            id = 1,
            title = "테스트 과제",
            totalQuestions = 10,
            dueAt = "2024-12-31T23:59:59Z",
            courseClass = CourseClass(
                id = 1,
                name = "테스트 클래스",
                teacherName = "선생님",
                subject = Subject(id = 1, name = "수학"),

                studentCount = 10,
                createdAt = "2024-01-01",
            ),
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentCard(
                    assignment = assignment,
                    submittedCount = 5,
                    totalCount = 10,
                    onAssignmentClick = {},
                    onEditClick = {},
                    onViewResults = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("테스트 과제", substring = true).assertExists()
    }

    @Test
    fun classAssignmentCard_renders() {
        val classAssignment = ClassAssignment(
            id = 1,
            title = "클래스 과제",
            subject = "수학",
            dueDate = "2024-12-31",
            completionRate = 0.8f,
            totalStudents = 10,
            completedStudents = 8,
            averageScore = 85,
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                ClassAssignmentCard(
                    assignment = classAssignment,
                    onNavigateToAssignmentDetail = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("클래스 과제", substring = true).assertExists()
    }

    @Test
    fun questionGroupCard_expanded_renders() {
        val baseQuestion = DetailedQuestionResult(
            questionNumber = "1",
            question = "첫 번째 문제",
            myAnswer = "답1",
            correctAnswer = "답1",
            isCorrect = true,
            explanation = "설명",
        )
        val tailQuestion = DetailedQuestionResult(
            questionNumber = "1-1",
            question = "꼬리 문제",
            myAnswer = "답2",
            correctAnswer = "답2",
            isCorrect = true,
            explanation = "설명",
        )
        val group = QuestionGroup(
            baseQuestion = baseQuestion,
            tailQuestions = listOf(tailQuestion),
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                QuestionGroupCard(
                    group = group,
                    isExpanded = true,
                    onToggle = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.waitForIdle()
    }

    @Test
    fun questionGroupCard_collapsed_renders() {
        val baseQuestion = DetailedQuestionResult(
            questionNumber = "2",
            question = "두 번째 문제",
            myAnswer = "답1",
            correctAnswer = "답1",
            isCorrect = true,
            explanation = "설명",
        )
        val group = QuestionGroup(
            baseQuestion = baseQuestion,
            tailQuestions = emptyList(),
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                QuestionGroupCard(
                    group = group,
                    isExpanded = false,
                    onToggle = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("문제 2", substring = true).assertExists()
    }

    @Test
    fun detailedQuestionResultCard_correct_renders() {
        val question = DetailedQuestionResult(
            questionNumber = "1",
            question = "정답 문제",
            myAnswer = "정답",
            correctAnswer = "정답",
            isCorrect = true,
            explanation = "맞습니다!",
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                DetailedQuestionResultCard(question = question)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("문제 1", substring = true).assertExists()
    }

    @Test
    fun detailedQuestionResultCard_incorrect_renders() {
        val question = DetailedQuestionResult(
            questionNumber = "2",
            question = "오답 문제",
            myAnswer = "오답",
            correctAnswer = "정답",
            isCorrect = false,
            explanation = "틀렸습니다",
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                DetailedQuestionResultCard(question = question)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("문제 2", substring = true).assertExists()
    }

    @Test
    fun teacherAssignmentResultCard_highScore_renders() {
        val student = StudentResult(
            studentId = "S001",
            name = "홍길동",
            score = 95,
            confidenceScore = 98,
            status = "SUBMITTED",
            submittedAt = "2024-01-15T10:00:00Z",
            answers = emptyList(),
            detailedAnswers = emptyList(),
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentResultCard(
                    student = student,
                    onStudentClick = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("홍길동", substring = true).assertExists()
    }

    @Test
    fun teacherAssignmentResultCard_lowScore_renders() {
        val student = StudentResult(
            studentId = "S002",
            name = "김철수",
            score = 45,
            confidenceScore = 50,
            status = "SUBMITTED",
            submittedAt = "2024-01-15T10:00:00Z",
            answers = emptyList(),
            detailedAnswers = emptyList(),
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentResultCard(
                    student = student,
                    onStudentClick = {},
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("김철수", substring = true).assertExists()
    }

    @Test
    fun appInfoScreen_allComponents_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.waitForIdle()
    }

    @Test
    fun noRecentAssignmentScreen_multipleTimes() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun assignmentReportCard_multipleAssignments() {
        val assignments = listOf(
            AssignmentData(
                id = 1,
                title = "과제 1",
                totalQuestions = 10,
                dueAt = "2024-12-31T23:59:59Z",
                courseClass = CourseClass(
                    id = 1,
                    name = "클래스 1",
                    teacherName = "선생님",
                    subject = Subject(id = 1, name = "수학"),

                    studentCount = 10,
                    createdAt = "2024-01-01",
                ),
            ),
            AssignmentData(
                id = 2,
                title = "과제 2",
                totalQuestions = 20,
                dueAt = "2024-12-31T23:59:59Z",
                courseClass = CourseClass(
                    id = 2,
                    name = "클래스 2",
                    teacherName = "선생님",
                    subject = Subject(id = 2, name = "과학"),

                    studentCount = 15,
                    createdAt = "2024-01-01",
                ),
            ),
        )

        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    assignments.forEach { assignment ->
                        AssignmentReportCard(
                            assignment = assignment,
                            onReportClick = {},
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun appInfoScreen_rendersAllSections() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen(onBackClick = {})
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("앱 정보", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("VoiceTutor", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("개발 정보", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("문의 및 지원", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("© 2025 VoiceTutor Team. All rights reserved.", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun appInfoScreen_allFeaturesDisplayed() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen(onBackClick = {})
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("음성 인식 기반 교육 플랫폼", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("버전 1.0.0", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("VoiceTutor Team", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun appInfoScreen_contactItemsDisplayed() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen(onBackClick = {})
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("이메일", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("support@voicetutor.com", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("앱 평가하기", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun noRecentAssignmentScreen_rendersCorrectly() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("이어할 과제가 없습니다", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("홈 화면에서 새로운 과제를 확인해보세요", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun appInfoScreen_backButtonWorks() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen(onBackClick = {})
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithContentDescription("뒤로가기", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
    }

    @Test
    fun appInfoScreen_infoItem_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("개발사", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("빌드 번호", useUnmergedTree = true).assertExists()
    }

    @Test
    fun appInfoScreen_contactItem_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("이메일", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("support@voicetutor.com", useUnmergedTree = true).assertExists()
    }

    @Test
    fun appInfoScreen_allHelperComposables() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("개발사", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("이메일", useUnmergedTree = true).assertExists()
    }
}
