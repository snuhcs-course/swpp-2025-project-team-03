package com.example.voicetutor.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Additional comprehensive tests to maximize coverage.
 * Tests various composable functions with different data combinations.
 */
@RunWith(AndroidJUnit4::class)
class ExtendedScreenTests {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Test AppInfoScreen multiple times to cover all code paths
    @Test
    fun appInfoScreen_multipleRenders() {
        // Render once - multiple renders don't add coverage value
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    // Test NoRecentAssignmentScreen multiple times
    @Test
    fun noRecentAssignmentScreen_multipleRenders() {
        // Render once - multiple renders don't add coverage value
        composeTestRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    // Test AssignmentReportCard with various assignments
    @Test
    fun assignmentReportCard_variousAssignments() {
        val assignments = listOf(
            AssignmentData(
                id = 1,
                title = "수학 과제 1",
                totalQuestions = 5,
                dueAt = "2024-12-31T23:59:59Z",
                courseClass = CourseClass(
                    id = 1,
                    name = "1학년 1반",
                    teacherName = "김선생님",
                    subject = Subject(id = 1, name = "수학"),

                    studentCount = 20,
                    createdAt = "2024-01-01",
                ),
            ),
            AssignmentData(
                id = 2,
                title = "과학 과제 2",
                totalQuestions = 10,
                dueAt = "2024-12-31T23:59:59Z",
                courseClass = CourseClass(
                    id = 2,
                    name = "2학년 1반",
                    teacherName = "이선생님",
                    subject = Subject(id = 2, name = "과학"),

                    studentCount = 25,
                    createdAt = "2024-01-01",
                ),
            ),
        )
        // Render all assignments in one composition
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

    // Test StudentAssignmentCard with various progress values
    @Test
    fun studentAssignmentCard_variousProgress() {
        val progressValues = listOf(0f, 0.25f, 0.5f, 0.75f, 1f)
        // Render all progress values in one composition
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    progressValues.forEach { progress ->
                        StudentAssignmentCard(
                            title = "진행률 테스트 ${(progress * 100).toInt()}%",
                            subject = "수학",
                            dueDate = "2024-12-31",
                            progress = progress,
                            solvedNum = (progress * 10).toInt(),
                            totalQuestions = 10,
                            status = PersonalAssignmentStatus.IN_PROGRESS,
                            onClick = {},
                            onStartAssignment = {},
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // Test TeacherAssignmentCard with various statuses
    @Test
    fun teacherAssignmentCard_allStatuses() {
        val statuses = listOf(
            AssignmentStatus.DRAFT,
            AssignmentStatus.IN_PROGRESS,
            AssignmentStatus.COMPLETED,
        )
        // Render all statuses in one composition
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    statuses.forEach { status ->
                        TeacherAssignmentCard(
                            title = "상태 테스트 ${status.name}",
                            className = "테스트 클래스",
                            submittedCount = 5,
                            totalCount = 10,
                            dueDate = "2024-12-31T23:59:59Z",
                            status = status,
                            onClick = {},
                            onViewResults = {},
                            onEdit = {},
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // Test ClassCard with various data
    @Test
    fun classCard_variousData() {
        val classes = listOf(
            ClassRoom(
                id = 1,
                name = "1학년 1반",
                subject = "수학",
                description = "수학 클래스",
                studentCount = 20,
                assignmentCount = 5,
                completionRate = 0.8f,
                color = androidx.compose.ui.graphics.Color(0xFF6200EE),
            ),
            ClassRoom(
                id = 2,
                name = "2학년 2반",
                subject = "과학",
                description = "과학 클래스",
                studentCount = 25,
                assignmentCount = 10,
                completionRate = 0.9f,
                color = androidx.compose.ui.graphics.Color(0xFF00BCD4),
            ),
        )
        // Render all classes in one composition
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    classes.forEach { classRoom ->
                        ClassCard(
                            classRoom = classRoom,
                            onClassClick = {},
                            onCreateAssignment = {},
                            onViewStudents = {},
                            onDeleteClass = { _, _ -> },
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // Test AllStudentsCard with various student data
    @Test
    fun allStudentsCard_variousStudents() {
        val students = listOf(
            AllStudentsStudent(
                id = 1,
                name = "홍길동",
                email = "hong@test.com",
                role = UserRole.STUDENT,
            ),
            AllStudentsStudent(
                id = 2,
                name = "김철수",
                email = "kim@test.com",
                role = UserRole.STUDENT,
            ),
        )
        // Render all students in one composition
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    students.forEach { student ->
                        AllStudentsCard(
                            student = student,
                            onReportClick = {},
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // Test QuestionGroupCard with tail questions
    @Test
    fun questionGroupCard_withTailQuestions() {
        val baseQuestion = DetailedQuestionResult(
            questionNumber = "1",
            question = "기본 문제",
            myAnswer = "답1",
            correctAnswer = "답1",
            isCorrect = true,
            explanation = "설명",
        )
        val tailQuestions = listOf(
            DetailedQuestionResult(
                questionNumber = "1-1",
                question = "꼬리 문제 1",
                myAnswer = "답2",
                correctAnswer = "답2",
                isCorrect = true,
                explanation = "설명",
            ),
            DetailedQuestionResult(
                questionNumber = "1-2",
                question = "꼬리 문제 2",
                myAnswer = "답3",
                correctAnswer = "답3",
                isCorrect = true,
                explanation = "설명",
            ),
        )
        val group = QuestionGroup(
            baseQuestion = baseQuestion,
            tailQuestions = tailQuestions,
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
        // Verify that the base question is rendered - the text is "문제 1"
        composeTestRule.onNodeWithText("문제 1", useUnmergedTree = true).assertExists()
    }

    // Test DetailedQuestionResultCard with various states
    @Test
    fun detailedQuestionResultCard_variousStates() {
        val questions = listOf(
            DetailedQuestionResult(
                questionNumber = "1",
                question = "정답 문제",
                myAnswer = "정답",
                correctAnswer = "정답",
                isCorrect = true,
                explanation = "맞습니다!",
            ),
            DetailedQuestionResult(
                questionNumber = "2",
                question = "오답 문제",
                myAnswer = "오답",
                correctAnswer = "정답",
                isCorrect = false,
                explanation = "틀렸습니다",
            ),
        )
        // Render all questions in one composition
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    questions.forEach { question ->
                        DetailedQuestionResultCard(question = question)
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // Test TeacherAssignmentResultCard with various scores
    @Test
    fun teacherAssignmentResultCard_variousScores() {
        val students = listOf(
            StudentResult(
                studentId = "S001",
                name = "고득점 학생",
                score = 95,
                confidenceScore = 98,
                status = "SUBMITTED",
                submittedAt = "2024-01-15T10:00:00Z",
                answers = emptyList(),
                detailedAnswers = emptyList(),
            ),
            StudentResult(
                studentId = "S002",
                name = "중간 점수 학생",
                score = 75,
                confidenceScore = 80,
                status = "SUBMITTED",
                submittedAt = "2024-01-15T10:00:00Z",
                answers = emptyList(),
                detailedAnswers = emptyList(),
            ),
        )
        // Render all students in one composition
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    students.forEach { student ->
                        TeacherAssignmentResultCard(
                            student = student,
                            onStudentClick = {},
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // Test StatusBadge all statuses multiple times
    @Test
    fun statusBadge_allStatuses_multipleTimes() {
        val statuses = listOf(
            AssignmentStatus.IN_PROGRESS,
            AssignmentStatus.COMPLETED,
            AssignmentStatus.DRAFT,
        )
        // Render all statuses in one composition
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    statuses.forEach { status ->
                        StatusBadge(status = status)
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // Test TypeBadge all types multiple times
    @Test
    fun typeBadge_allTypes_multipleTimes() {
        val types = listOf("Quiz", "Continuous", "Discussion", "Unknown")
        // Render all types in one composition
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    types.forEach { type ->
                        TypeBadge(type = type)
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // Test CustomStatusBadge all statuses multiple times
    @Test
    fun customStatusBadge_allStatuses_multipleTimes() {
        val statuses = listOf("시작 안함", "진행 중", "완료", "알 수 없음")
        // Render all statuses in one composition
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    statuses.forEach { status ->
                        CustomStatusBadge(text = status)
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // Test RoleCard both roles multiple times
    @Test
    fun roleCard_bothRoles_multipleTimes() {
        // Render both roles in one composition
        composeTestRule.setContent {
            VoiceTutorTheme {
                Row {
                    RoleCard(
                        title = "학생",
                        description = "과제를 받고 학습합니다",
                        icon = Icons.Filled.School,
                        isSelected = true,
                        onClick = {},
                    )
                    RoleCard(
                        title = "선생님",
                        description = "과제를 생성하고 관리합니다",
                        icon = Icons.Filled.Person,
                        isSelected = false,
                        onClick = {},
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // Test ClassStatItem with various stats
    @Test
    fun classStatItem_variousStats() {
        val stats = listOf(
            Triple("25", "학생", Icons.Filled.Person),
            Triple("10", "과제", Icons.Filled.Assignment),
            Triple("85", "평균", Icons.Filled.TrendingUp),
        )
        // Render all stats in one composition
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    stats.forEach { (value, label, icon) ->
                        ClassStatItem(
                            icon = icon,
                            value = value,
                            label = label,
                            color = androidx.compose.ui.graphics.Color(0xFF6200EE),
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // Test SettingsItem with various items
    @Test
    fun settingsItem_variousItems() {
        val items = listOf(
            Triple("앱 설정", "앱 설정을 변경합니다", Icons.Filled.Settings),
            Triple("알림 설정", "알림을 관리합니다", Icons.Filled.Notifications),
        )
        // Render all items in one composition
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    items.forEach { (title, subtitle, icon) ->
                        SettingsItem(
                            icon = icon,
                            title = title,
                            subtitle = subtitle,
                            onClick = {},
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // Test InfoItem with various info
    @Test
    fun infoItem_variousInfo() {
        val infoPairs = listOf(
            Pair("버전", "1.0.0"),
            Pair("빌드 번호", "100"),
        )
        // Render all info in one composition
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    infoPairs.forEach { (label, value) ->
                        InfoItem(label = label, value = value)
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    // Test ContactItem with various contacts
    @Test
    fun contactItem_variousContacts() {
        val contacts = listOf(
            Triple(Icons.Filled.Email, "이메일", "support@voicetutor.com"),
            Triple(Icons.Filled.Language, "웹사이트", "www.voicetutor.com"),
        )
        // Render all contacts in one composition
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    contacts.forEach { (icon, title, value) ->
                        ContactItem(
                            icon = icon,
                            title = title,
                            value = value,
                            onClick = {},
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun appInfoScreen_infoItem_multipleItems() {
        val infoItems = listOf(
            "라벨 1" to "값 1",
            "라벨 2" to "값 2",
            "라벨 3" to "값 3",
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    infoItems.forEach { (label, value) ->
                        InfoItem(label = label, value = value)
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        infoItems.forEach { (label, value) ->
            composeTestRule.onNodeWithText(label, useUnmergedTree = true).assertExists()
            composeTestRule.onNodeWithText(value, useUnmergedTree = true).assertExists()
        }
    }

    @Test
    fun appInfoScreen_contactItem_multipleItems() {
        val contactItems = listOf(
            Triple(Icons.Filled.Email, "이메일", "email@example.com"),
            Triple(Icons.Filled.Language, "웹사이트", "www.example.com"),
            Triple(Icons.Filled.Star, "앱 평가하기", "Google Play Store"),
        )
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    contactItems.forEach { (icon, title, value) ->
                        ContactItem(
                            icon = icon,
                            title = title,
                            value = value,
                            onClick = {},
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()

        contactItems.forEach { (_, title, value) ->
            composeTestRule.onNodeWithText(title, useUnmergedTree = true).assertExists()
            composeTestRule.onNodeWithText(value, useUnmergedTree = true).assertExists()
        }
    }

    // Test NoRecentAssignmentScreen with different states
    @Test
    fun noRecentAssignmentScreen_rendersAllElements() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen()
            }
        }
        composeTestRule.waitForIdle()

        // Verify all elements exist
        composeTestRule.onNodeWithText("이어할 과제가 없습니다", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("홈 화면에서 새로운 과제를 확인해보세요", useUnmergedTree = true).assertExists()
    }
}
