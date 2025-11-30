package com.example.voicetutor.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.models.*
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
@RunWith(AndroidJUnit4::class)
class ExtendedScreenTests {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun appInfoScreen_multipleRenders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun noRecentAssignmentScreen_multipleRenders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen()
            }
        }
        composeTestRule.waitForIdle()
    }

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
    fun studentAssignmentCard_variousProgress() {
        val progressValues = listOf(0f, 0.25f, 0.5f, 0.75f, 1f)

        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    progressValues.forEach { progress ->
                        StudentAssignmentCard(
                            title = "진행률 테스트 ${(progress * 100).toInt()}%",
                            subject = "수학",
                            dueDate = "2024-12-31",
                            progress = progress,
                            totalQuestions = 10,
                            status = PersonalAssignmentStatus.IN_PROGRESS,
                            onClick = {},
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun teacherAssignmentCard_allStatuses() {
        val statuses = listOf(
            AssignmentStatus.DRAFT,
            AssignmentStatus.IN_PROGRESS,
            AssignmentStatus.COMPLETED,
        )

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
                            onClick = {},
                        )
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
    }

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

        composeTestRule.onNodeWithText("문제 1", useUnmergedTree = true).assertExists()
    }

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

    @Test
    fun statusBadge_allStatuses_multipleTimes() {
        val statuses = listOf(
            AssignmentStatus.IN_PROGRESS,
            AssignmentStatus.COMPLETED,
            AssignmentStatus.DRAFT,
        )

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

    @Test
    fun typeBadge_allTypes_multipleTimes() {
        val types = listOf("Quiz", "Continuous", "Discussion", "Unknown")

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

    @Test
    fun customStatusBadge_allStatuses_multipleTimes() {
        val statuses = listOf("시작 안함", "진행 중", "완료", "알 수 없음")

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

    @Test
    fun roleCard_bothRoles_multipleTimes() {
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

    @Test
    fun classStatItem_variousStats() {
        val stats = listOf(
            Triple("25", "학생", Icons.Filled.Person),
            Triple("10", "과제", Icons.AutoMirrored.Filled.Assignment),
            Triple("85", "평균", Icons.AutoMirrored.Filled.TrendingUp),
        )

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

    @Test
    fun settingsScreen_settingsItem_variousItems() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SettingsScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                composeTestRule.onAllNodesWithText("튜토리얼 다시 보기", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 10000) {
            try {
                composeTestRule.onAllNodesWithText("앱 정보", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }

        composeTestRule.onNodeWithText("튜토리얼 다시 보기", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("앱 정보", useUnmergedTree = true).assertExists()
    }

    @Test
    fun appInfoScreen_displaysAllInfoItems() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("개발사", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("빌드 번호", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("최종 업데이트", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("플랫폼", useUnmergedTree = true).assertExists()
    }

    @Test
    fun appInfoScreen_displaysAllContactItems() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AppInfoScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("이메일", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("앱 평가하기", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("support@voicetutor.com", useUnmergedTree = true).assertExists()
    }

    @Test
    fun noRecentAssignmentScreen_rendersAllElements() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                NoRecentAssignmentScreen()
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("이어할 과제가 없습니다", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("홈 화면에서 새로운 과제를 확인해보세요", useUnmergedTree = true).assertExists()
    }
}
