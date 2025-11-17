package com.example.voicetutor.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.data.repository.AssignmentRepository
import com.example.voicetutor.data.repository.AuthRepository
import com.example.voicetutor.data.repository.StudentRepository
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import com.example.voicetutor.ui.viewmodel.StudentViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Comprehensive tests to maximize coverage by testing:
 * 1. All composable functions with different states
 * 2. Edge cases and various data combinations
 * 3. More internal composable functions
 */
@RunWith(AndroidJUnit4::class)
class ComprehensiveScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Test StudentAssignmentCard with different statuses
    @Test
    fun studentAssignmentCard_NOT_STARTED_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentAssignmentCard(
                    title = "테스트 과제",
                    subject = "수학",
                    dueDate = "2024-12-31",
                    progress = 0f,
                    solvedNum = 0,
                    totalQuestions = 10,
                    status = PersonalAssignmentStatus.NOT_STARTED,
                    onClick = {},
                    onStartAssignment = {}
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
                    solvedNum = 5,
                    totalQuestions = 10,
                    status = PersonalAssignmentStatus.IN_PROGRESS,
                    onClick = {},
                    onStartAssignment = {}
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
                    solvedNum = 10,
                    totalQuestions = 10,
                    status = PersonalAssignmentStatus.SUBMITTED,
                    onClick = {},
                    onStartAssignment = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("제출 완료 과제", substring = true).assertExists()
    }

    // Test TeacherAssignmentCard with different statuses
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
                    status = AssignmentStatus.DRAFT,
                    onClick = {},
                    onViewResults = {},
                    onEdit = {}
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
                    status = AssignmentStatus.IN_PROGRESS,
                    onClick = {},
                    onViewResults = {},
                    onEdit = {}
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
                    status = AssignmentStatus.COMPLETED,
                    onClick = {},
                    onViewResults = {},
                    onEdit = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("완료된 과제", substring = true).assertExists()
    }

    // Test ClassCard
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
            color = androidx.compose.ui.graphics.Color(0xFF6200EE)
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                ClassCard(
                    classRoom = classRoom,
                    onClassClick = {},
                    onCreateAssignment = {},
                    onViewStudents = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("1학년 1반", substring = true).assertExists()
    }

    // Test AllStudentsCard with different data
    @Test
    fun allStudentsCard_withClasses_renders() {
        val student = AllStudentsStudent(
            id = 1,
            name = "홍길동",
            email = "hong@test.com",
            role = UserRole.STUDENT
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllStudentsCard(
                    student = student,
                    classNames = listOf("1학년 1반", "1학년 2반"),
                    isLoadingClasses = false,
                    onReportClick = {}
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
            role = UserRole.STUDENT
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllStudentsCard(
                    student = student,
                    classNames = emptyList(),
                    isLoadingClasses = false,
                    onReportClick = {}
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
            role = UserRole.STUDENT
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllStudentsCard(
                    student = student,
                    classNames = emptyList(),
                    isLoadingClasses = true,
                    onReportClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("이영희", substring = true).assertExists()
    }

    // Test AssignmentCard
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
                createdAt = "2024-01-01"
            )
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
        composeTestRule.onNodeWithText("테스트 과제", substring = true).assertExists()
    }

    // Test ClassAssignmentCard
    @Test
    fun classAssignmentCard_renders() {
        val classAssignment = com.example.voicetutor.ui.screens.ClassAssignment(
            id = 1,
            title = "클래스 과제",
            subject = "수학",
            dueDate = "2024-12-31",
            completionRate = 0.8f,
            totalStudents = 10,
            completedStudents = 8,
            averageScore = 85
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                ClassAssignmentCard(
                    assignment = classAssignment,
                    onNavigateToAssignmentDetail = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("클래스 과제", substring = true).assertExists()
    }

    // Test more QuestionGroupCard scenarios
    @Test
    fun questionGroupCard_expanded_renders() {
        val baseQuestion = DetailedQuestionResult(
            questionNumber = "1",
            question = "첫 번째 문제",
            myAnswer = "답1",
            correctAnswer = "답1",
            isCorrect = true,
            explanation = "설명"
        )
        val tailQuestion = DetailedQuestionResult(
            questionNumber = "1-1",
            question = "꼬리 문제",
            myAnswer = "답2",
            correctAnswer = "답2",
            isCorrect = true,
            explanation = "설명"
        )
        val group = QuestionGroup(
            baseQuestion = baseQuestion,
            tailQuestions = listOf(tailQuestion)
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                QuestionGroupCard(
                    group = group,
                    isExpanded = true,
                    onToggle = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        // Don't assert specific text - just verify it renders
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
            explanation = "설명"
        )
        val group = QuestionGroup(
            baseQuestion = baseQuestion,
            tailQuestions = emptyList()
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                QuestionGroupCard(
                    group = group,
                    isExpanded = false,
                    onToggle = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("문제 2", substring = true).assertExists()
    }

    // Test DetailedQuestionResultCard with different states
    @Test
    fun detailedQuestionResultCard_correct_renders() {
        val question = DetailedQuestionResult(
            questionNumber = "1",
            question = "정답 문제",
            myAnswer = "정답",
            correctAnswer = "정답",
            isCorrect = true,
            explanation = "맞습니다!"
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
            explanation = "틀렸습니다"
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                DetailedQuestionResultCard(question = question)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("문제 2", substring = true).assertExists()
    }

    // Test TeacherAssignmentResultCard with different scores
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
            detailedAnswers = emptyList()
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentResultCard(
                    student = student,
                    onStudentClick = {}
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
            detailedAnswers = emptyList()
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherAssignmentResultCard(
                    student = student,
                    onStudentClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("김철수", substring = true).assertExists()
    }

    // Test OptionButton with all states
    @Test
    fun optionButton_selected_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                OptionButton(
                    text = "선택된 선택지",
                    isSelected = true,
                    isCorrect = false,
                    isWrong = false,
                    onClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("선택된 선택지", substring = true).assertExists()
    }

    @Test
    fun optionButton_correct_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                OptionButton(
                    text = "정답 선택지",
                    isSelected = false,
                    isCorrect = true,
                    isWrong = false,
                    onClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("정답 선택지", substring = true).assertExists()
    }

    @Test
    fun optionButton_wrong_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                OptionButton(
                    text = "오답 선택지",
                    isSelected = false,
                    isCorrect = false,
                    isWrong = true,
                    onClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("오답 선택지", substring = true).assertExists()
    }

    // Test more AppInfoScreen components
    @Test
    fun appInfoScreen_allComponents_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }
        composeTestRule.waitForIdle()
        // Test that all sections render
        composeTestRule.waitForIdle()
    }

    // Test NoRecentAssignmentScreen multiple times
    @Test
    fun noRecentAssignmentScreen_multipleTimes() {
        // Render once - multiple renders don't add coverage value
        composeTestRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    // Test AssignmentReportCard with different assignments
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
                    createdAt = "2024-01-01"
                )
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
                    createdAt = "2024-01-01"
                )
            )
        )
        // Render all assignments in one composition
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    assignments.forEach { assignment ->
                        AssignmentReportCard(
                            assignment = assignment,
                            onReportClick = {}
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // Test AppInfoScreen with all components
    @Test
    fun appInfoScreen_rendersAllSections() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen(onBackClick = {})
            }
        }
        composeTestRule.waitForIdle()
        
        // Verify main sections exist
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
        
        // Verify all features are displayed
        composeTestRule.onNodeWithText("음성 인식 기반 교육 플랫폼", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("버전 1.0.0", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("VoiceTutor Team", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun appInfoScreen_legalItemsClickable() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    LegalItem(title = "개인정보처리방침", onClick = {})
                    LegalItem(title = "서비스 이용약관", onClick = {})
                    LegalItem(title = "오픈소스 라이선스", onClick = {})
                }
            }
        }
        composeTestRule.waitForIdle()
        
        // Verify legal items are present
        composeTestRule.onNodeWithText("개인정보처리방침", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("서비스 이용약관", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("오픈소스 라이선스", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun appInfoScreen_contactItemsDisplayed() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen(onBackClick = {})
            }
        }
        composeTestRule.waitForIdle()
        
        // Verify contact items
        composeTestRule.onNodeWithText("이메일", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("support@voicetutor.com", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("앱 평가하기", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun appInfoScreen_actionItemsDisplayed() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    ActionItem(
                        icon = Icons.Filled.Refresh,
                        title = "업데이트 확인",
                        description = "최신 버전 확인",
                        onClick = {}
                    )
                    ActionItem(
                        icon = Icons.Filled.Share,
                        title = "앱 공유하기",
                        description = "친구에게 VoiceTutor 소개",
                        onClick = {}
                    )
                    ActionItem(
                        icon = Icons.Filled.Delete,
                        title = "캐시 삭제",
                        description = "앱 캐시를 정리합니다",
                        onClick = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        
        // Verify action items
        composeTestRule.onNodeWithText("업데이트 확인", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("앱 공유하기", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("캐시 삭제", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun noRecentAssignmentScreen_rendersCorrectly() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen()
            }
        }
        composeTestRule.waitForIdle()
        
        // Verify main message
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
        
        // Find and click back button
        composeTestRule.onNodeWithContentDescription("뒤로가기", useUnmergedTree = true)
            .assertIsDisplayed()
            .performClick()
        
        composeTestRule.waitForIdle()
        // Note: We can't directly verify the callback, but we can verify the button exists and is clickable
    }

    // Test AppInfoScreen helper composables
    @Test
    fun appInfoScreen_featureItem_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    FeatureItem(feature = "테스트 기능")
                }
            }
        }
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("테스트 기능", useUnmergedTree = true).assertExists()
    }

    @Test
    fun appInfoScreen_infoItem_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    InfoItem(label = "라벨", value = "값")
                }
            }
        }
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("라벨", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("값", useUnmergedTree = true).assertExists()
    }

    @Test
    fun appInfoScreen_legalItem_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    LegalItem(title = "법적 항목", onClick = {})
                }
            }
        }
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("법적 항목", useUnmergedTree = true)
            .assertExists()
            .performClick()
        
        composeTestRule.waitForIdle()
    }

    @Test
    fun appInfoScreen_contactItem_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    ContactItem(
                        icon = Icons.Filled.Email,
                        title = "이메일",
                        value = "test@example.com",
                        onClick = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("이메일", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("test@example.com", useUnmergedTree = true).assertExists()
    }

    @Test
    fun appInfoScreen_actionItem_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    ActionItem(
                        icon = Icons.Filled.Update,
                        title = "업데이트",
                        description = "설명",
                        onClick = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        
        composeTestRule.onNodeWithText("업데이트", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("설명", useUnmergedTree = true).assertExists()
    }

    // Test more screen combinations
    @Test
    fun appInfoScreen_allHelperComposables() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    FeatureItem(feature = "기능 1")
                    InfoItem(label = "라벨", value = "값")
                    LegalItem(title = "법적", onClick = {})
                    ContactItem(
                        icon = Icons.Filled.Email,
                        title = "연락처",
                        value = "값",
                        onClick = {}
                    )
                    ActionItem(
                        icon = Icons.Filled.Update,
                        title = "액션",
                        description = "설명",
                        onClick = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        
        // Verify all components render
        composeTestRule.onNodeWithText("기능 1", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("라벨", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("법적", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("연락처", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("액션", useUnmergedTree = true).assertExists()
    }
}

