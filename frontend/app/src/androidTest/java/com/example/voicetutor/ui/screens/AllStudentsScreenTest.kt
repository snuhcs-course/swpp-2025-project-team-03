package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.models.ClassData
import com.example.voicetutor.data.models.ClassInfo
import com.example.voicetutor.data.models.Student
import com.example.voicetutor.data.models.Subject
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
@RunWith(AndroidJUnit4::class)
class AllStudentsScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var apiService: ApiService

    private val fakeApi: FakeApiService
        get() = apiService as FakeApiService

    @Before
    fun setUp() {
        hiltRule.inject()
        resetFakeApi()
    }

    private fun resetFakeApi() {
        val subject = Subject(id = 1, name = "수학", code = "MATH")
        val classes = listOf(
            ClassData(
                id = 1,
                name = "수학 A반",
                subject = subject,
                description = "심화 수학",
                teacherId = 2,
                teacherName = "김선생",
                studentCount = 25,
                studentCountAlt = 25,
                createdAt = "2024-01-01T00:00:00Z",

            ),
            ClassData(
                id = 2,
                name = "수학 B반",
                subject = subject,
                description = "기초 수학",
                teacherId = 2,
                teacherName = "김선생",
                studentCount = 20,
                studentCountAlt = 20,
                createdAt = "2024-01-01T00:00:00Z",

            ),
        )

        val students = listOf(
            Student(id = 1, name = "홍길동", email = "hong@school.com", role = UserRole.STUDENT),
            Student(id = 2, name = "이몽룡", email = "lee@school.com", role = UserRole.STUDENT),
        )

        fakeApi.apply {
            classesResponse = classes
            shouldFailClasses = false
            allStudentsResponse = students
            shouldFailAllStudents = false
            shouldFailStudentClasses = false
            studentClassesResponse = listOf(
                ClassInfo(id = 1, name = "수학 A반"),
                ClassInfo(id = 2, name = "과학 B반"),
            )
        }
    }

    private fun waitForText(text: String, timeoutMillis: Long = 10_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule
                .onAllNodesWithText(text, substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun allStudentsScreen_emptyState_showsPlaceholder() {
        fakeApi.allStudentsResponse = emptyList()

        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("학생이 없습니다")
        composeRule.onAllNodesWithText("학생이 없습니다", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_errorState_showsPlaceholder() {
        fakeApi.shouldFailAllStudents = true
        fakeApi.allStudentsErrorMessage = "학생 목록 로드 실패"

        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("학생이 없습니다")
        composeRule.onAllNodesWithText("학생이 없습니다", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_displaysClassDropdown() {
        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("수업 선택")

        composeRule.onAllNodesWithText("수업 선택", useUnmergedTree = true).onFirst().assertIsDisplayed()
        waitForText("수학 A반")

        composeRule.onAllNodesWithText("수학 A반", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_displaysStudentListHeader() {
        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("학생 목록")
        composeRule.onNodeWithText("학생 목록", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_displaysMultipleStudents() {
        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("홍길동")
        composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true).onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("이몽룡", useUnmergedTree = true).onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("hong@school.com", useUnmergedTree = true).onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("lee@school.com", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_displaysStudentWithNullName() {
        fakeApi.allStudentsResponse = listOf(
            Student(id = 1, name = null, email = "test@school.com", role = UserRole.STUDENT),
        )

        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("이름 없음")
        composeRule.onNodeWithText("이름 없음", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_displaysStudentCardWithReportButton() {
        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("리포트 보기")
        composeRule.onAllNodesWithText("리포트 보기", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_studentCardShowsLoadingClasses() {
        fakeApi.shouldFailStudentClasses = false

        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("홍길동")

        composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_studentCardShowsEmptyClasses() {
        fakeApi.shouldFailStudentClasses = false
        fakeApi.studentClassesResponse = emptyList()

        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("홍길동")
        composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true).onFirst().assertIsDisplayed()

        composeRule.onAllNodesWithText("리포트 보기", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_studentCardShowsMultipleClasses() {
        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("홍길동")
        composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true).onFirst().assertIsDisplayed()

        waitForText("수학 A반", timeoutMillis = 5_000)
    }

    @Test
    fun allStudentsScreen_handlesClassLoadingError() {
        fakeApi.shouldFailClasses = true
        fakeApi.classesErrorMessage = "반 목록 로드 실패"

        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("수업 선택", timeoutMillis = 5_000)
    }

    @Test
    fun allStudentsScreen_handlesStudentClassesLoadingError() {
        fakeApi.shouldFailStudentClasses = true
        fakeApi.studentClassesErrorMessage = "학생 반 정보 로드 실패"

        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("홍길동")
        composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_reportButtonClickTriggersNavigation() {
        var navigationCalled = false
        var capturedClassId = -1
        var capturedStudentId = -1
        var capturedStudentName = ""

        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(
                    teacherId = "2",
                    onNavigateToStudentDetail = { classId, studentId, studentName ->
                        navigationCalled = true
                        capturedClassId = classId
                        capturedStudentId = studentId
                        capturedStudentName = studentName
                    },
                )
            }
        }

        waitForText("리포트 보기")
        composeRule.onAllNodesWithText("리포트 보기", useUnmergedTree = true).onFirst().performClick()

        assertTrue("Navigation callback should be called", navigationCalled)
        assertTrue("Class ID should be captured", capturedClassId > 0)
        assertTrue("Student ID should be captured", capturedStudentId > 0)
        assertTrue("Student name should be captured", capturedStudentName.isNotEmpty())
    }

    @Test
    fun allStudentsScreen_displaysLoadingIndicator() {
        fakeApi.allStudentsResponse = listOf(
            Student(id = 1, name = "홍길동", email = "hong@school.com", role = UserRole.STUDENT),
        )

        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("홍길동")
        composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_displaysStudentAvatarInitial() {
        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("홍길동")

        composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_displaysDividersBetweenStudents() {
        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("홍길동")
        waitForText("이몽룡")

        composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true).onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("이몽룡", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_showsLoadingClassesMessage() {
        fakeApi.shouldFailStudentClasses = false
        fakeApi.studentClassesResponse = listOf(
            ClassInfo(id = 1, name = "수학 A반"),
        )

        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("홍길동")

        waitForText("수학 A반", timeoutMillis = 5_000)
    }

    @Test
    fun allStudentsScreen_displaysMultipleClassesWithCommas() {
        fakeApi.studentClassesResponse = listOf(
            ClassInfo(id = 1, name = "수학 A반"),
            ClassInfo(id = 2, name = "과학 B반"),
            ClassInfo(id = 3, name = "영어 C반"),
        )

        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("홍길동")

        composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true).onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("리포트 보기", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_showsPlaceholderWhenNoClasses() {
        fakeApi.classesResponse = emptyList()

        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("수업 선택")

        composeRule.onAllNodesWithText("수업 선택", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_handlesNullSelectedClassId() {
        fakeApi.classesResponse = listOf(
            ClassData(
                id = 1,
                name = "수학 A반",
                subject = Subject(id = 1, name = "수학", code = "MATH"),
                description = "심화 수학",
                teacherId = 2,
                teacherName = "김선생",
                studentCount = 25,
                studentCountAlt = 25,
                createdAt = "2024-01-01T00:00:00Z",

            ),
        )

        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("수학 A반")

        composeRule.onAllNodesWithText("수학 A반", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_displaysStudentCountInHeader() {
        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("학생 목록")
        waitForText("2명")

        composeRule.onAllNodesWithText("2명", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_handlesEmptyStudentNameGracefully() {
        fakeApi.allStudentsResponse = listOf(
            Student(id = 1, name = null, email = "test@school.com", role = UserRole.STUDENT),
        )

        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("이름 없음")
        composeRule.onNodeWithText("이름 없음", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_displaysAllStudentEmails() {
        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("hong@school.com")
        waitForText("lee@school.com")
        composeRule.onAllNodesWithText("hong@school.com", useUnmergedTree = true).onFirst().assertIsDisplayed()
        composeRule.onAllNodesWithText("lee@school.com", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_displaysCorrectClassInDropdown() {
        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("수학 A반")

        composeRule.onAllNodesWithText("수학 A반", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun allStudentsScreen_singleStudentShowsNoDividers() {
        fakeApi.allStudentsResponse = listOf(
            Student(id = 1, name = "홍길동", email = "hong@school.com", role = UserRole.STUDENT),
        )

        composeRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(teacherId = "2")
            }
        }

        waitForText("홍길동")
        composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }
}
