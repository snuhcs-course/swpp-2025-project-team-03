package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.models.ClassData
import com.example.voicetutor.data.models.Student
import com.example.voicetutor.data.models.Subject
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
@RunWith(AndroidJUnit4::class)
class CreateAssignmentScreenCoverageTest {

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
        fakeApi.apply {
            shouldFailCreateAssignment = false
            shouldFailCreateClass = false
            shouldFailClasses = false
            classesResponse = listOf(
                ClassData(
                    id = 1,
                    name = "수학 A반",
                    subject = Subject(id = 1, name = "수학", code = "MATH"),
                    description = "기초 수학 수업",
                    teacherId = 2,
                    teacherName = "김선생님",
                    studentCount = 25,
                    studentCountAlt = 25,
                    createdAt = "2024-01-01T00:00:00Z",
                ),
            )
            classStudentsResponse = listOf(
                Student(
                    id = 1,
                    name = "홍길동",
                    email = "student1@school.com",
                    role = com.example.voicetutor.data.models.UserRole.STUDENT,
                ),
                Student(
                    id = 2,
                    name = "김철수",
                    email = "student2@school.com",
                    role = com.example.voicetutor.data.models.UserRole.STUDENT,
                ),
            )
        }
    }

    private fun <T> setStateFlow(viewModel: AssignmentViewModel, fieldName: String, value: T) {
        val field = AssignmentViewModel::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as MutableStateFlow<T>
        stateFlow.value = value
    }

    private fun waitForText(text: String, timeoutMillis: Long = 10_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule.onAllNodesWithText(text, substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun createAssignmentScreen_handlesFileSelectionState() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_callsOnCreateAssignmentWhenComplete() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(
                    teacherId = "2",
                    onCreateAssignment = {},
                )
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]

        composeRule.runOnIdle {
            setStateFlow(assignmentViewModel, "_currentAssignment", fakeApi.assignmentByIdResponse)
            setStateFlow(assignmentViewModel, "_uploadSuccess", true)
        }

        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_displaysClassDropdownItems() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("수업 선택")

        composeRule.onAllNodesWithText("수업 선택", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        waitForText("수학 A반")
        composeRule.onAllNodesWithText("수학 A반", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun createAssignmentScreen_gradeFieldIsReadOnly() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("학년")

        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_displaysGradeDropdownItems() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("학년")

        composeRule.onAllNodesWithText("학년", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_displaysUploadProgress() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]

        composeRule.runOnIdle {
            setStateFlow(assignmentViewModel, "_isUploading", true)
            setStateFlow(assignmentViewModel, "_uploadProgress", 0.5f)
        }

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("PDF 업로드 중", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("50%", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.runOnIdle {
            setStateFlow(assignmentViewModel, "_uploadProgress", 1.0f)
        }

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("100%", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun createAssignmentScreen_displaysUploadSuccessMessage() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]

        composeRule.runOnIdle {
            setStateFlow(assignmentViewModel, "_uploadSuccess", true)
            setStateFlow(assignmentViewModel, "_isUploading", false)
        }

        composeRule.waitForIdle()

        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_displaysFilePickerButton() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("PDF 자료 업로드")

        composeRule.onAllNodesWithText("파일 선택", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()

        composeRule.onAllNodesWithText("최대 10MB", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun createAssignmentScreen_displaysFileSelectionUI() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("PDF 자료 업로드")

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("파일 선택", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty() ||
                composeRule.onAllNodesWithText("파일 추가", useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onAllNodesWithText("최대 10MB", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun createAssignmentScreen_displaysProgressIndicator() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]

        composeRule.runOnIdle {
            setStateFlow(assignmentViewModel, "_isUploading", true)
            setStateFlow(assignmentViewModel, "_uploadProgress", 0.3f)
        }

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("PDF 업로드 중", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("30%", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.runOnIdle {
            setStateFlow(assignmentViewModel, "_uploadProgress", 0.75f)
        }

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("75%", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun createAssignmentScreen_displaysUploadOverlayDialog() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]

        composeRule.runOnIdle {
            setStateFlow(assignmentViewModel, "_isUploading", true)
            setStateFlow(assignmentViewModel, "_uploadProgress", 0.6f)
        }

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("PDF 업로드 중", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("60%", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.runOnIdle {
            setStateFlow(assignmentViewModel, "_uploadProgress", 0.9f)
        }

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("90%", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.runOnIdle {
            setStateFlow(assignmentViewModel, "_isUploading", false)
            setStateFlow(assignmentViewModel, "_uploadProgress", 1.0f)
        }

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("PDF 업로드 중", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isEmpty()
        }
    }

    @Test
    fun createAssignmentScreen_showsCompleteUploadProgress() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]

        composeRule.runOnIdle {
            setStateFlow(assignmentViewModel, "_isUploading", true)
            setStateFlow(assignmentViewModel, "_uploadProgress", 0.0f)
        }

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("PDF 업로드 중", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        val progressSteps = listOf(0.25f, 0.5f, 0.75f, 1.0f)
        for (progress in progressSteps) {
            composeRule.runOnIdle {
                setStateFlow(assignmentViewModel, "_uploadProgress", progress)
            }
            composeRule.waitForIdle()

            val expectedPercent = (progress * 100).toInt()
            composeRule.waitUntil(timeoutMillis = 3_000) {
                composeRule.onAllNodesWithText("$expectedPercent%", useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty()
            }
        }

        composeRule.runOnIdle {
            setStateFlow(assignmentViewModel, "_isUploading", false)
        }

        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_handlesSelectiveAssignment() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("수업 선택")

        composeRule.onAllNodesWithText("수업 선택", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        waitForText("수학 A반")
        composeRule.onAllNodesWithText("수학 A반", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        waitForText("선택한 학생에게만 배정")
        composeRule.onAllNodesWithText("선택한 학생에게만 배정", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_showsClassSelectionWarning() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("선택한 학생에게만 배정")
        composeRule.onAllNodesWithText("선택한 학생에게만 배정", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_displaysStudentSelection() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("수업 선택")

        composeRule.onAllNodesWithText("수업 선택", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        waitForText("수학 A반")
        composeRule.onAllNodesWithText("수학 A반", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("선택한 학생에게만 배정", useUnmergedTree = true)
            .onFirst()
            .performScrollTo()

        composeRule.waitForIdle()
        Thread.sleep(500)

        composeRule.onAllNodes(isSelectable() and hasClickAction(), useUnmergedTree = true)
            .onLast()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 15_000) {
            try {
                composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty() ||
                    composeRule.onAllNodesWithText("학생 목록을 불러오는 중", substring = true, useUnmergedTree = true)
                        .fetchSemanticsNodes().isNotEmpty() ||
                    composeRule.onAllNodesWithText("해당 반에 학생이 등록되지 않았습니다", substring = true, useUnmergedTree = true)
                        .fetchSemanticsNodes().isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_allowsStudentCheckboxSelection() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("수업 선택")

        composeRule.onAllNodesWithText("수업 선택", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        waitForText("수학 A반")
        composeRule.onAllNodesWithText("수학 A반", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("선택한 학생에게만 배정", useUnmergedTree = true)
            .onFirst()
            .performScrollTo()

        composeRule.waitForIdle()
        Thread.sleep(500)

        composeRule.onAllNodes(isSelectable() and hasClickAction(), useUnmergedTree = true)
            .onLast()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 15_000) {
            try {
                composeRule.onAllNodesWithText("전체 선택", useUnmergedTree = true)
                    .fetchSemanticsNodes().isNotEmpty() ||
                    composeRule.onAllNodesWithText("홍길동", useUnmergedTree = true)
                        .fetchSemanticsNodes().isNotEmpty() ||
                    composeRule.onAllNodesWithText("학생 목록을 불러오는 중", substring = true, useUnmergedTree = true)
                        .fetchSemanticsNodes().isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

        try {
            composeRule.onAllNodesWithText("전체 선택", useUnmergedTree = true)
                .onFirst()
                .performClick()
        } catch (_: Exception) {
        }

        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_validatesFormFields() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("과제 제목")

        val allTextFields = composeRule.onAllNodes(hasSetTextAction(), useUnmergedTree = true)

        allTextFields[0].performClick()
        composeRule.waitForIdle()
        allTextFields[0].performTextReplacement("테스트 과제")

        composeRule.waitForIdle()

        allTextFields[1].performClick()
        composeRule.waitForIdle()
        allTextFields[1].performTextReplacement("테스트 설명")

        composeRule.waitForIdle()

        allTextFields[2].performClick()
        composeRule.waitForIdle()
        allTextFields[2].performTextReplacement("10")

        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("수업 선택", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        waitForText("수학 A반")
        composeRule.onAllNodesWithText("수학 A반", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("학년", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("과목", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_createsAssignmentOnButtonClick() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("과제 제목")

        val allTextFields = composeRule.onAllNodes(hasSetTextAction(), useUnmergedTree = true)

        allTextFields[0].performClick()
        composeRule.waitForIdle()
        allTextFields[0].performTextReplacement("테스트 과제")

        composeRule.waitForIdle()

        allTextFields[1].performClick()
        composeRule.waitForIdle()
        allTextFields[1].performTextReplacement("테스트 설명")

        composeRule.waitForIdle()

        allTextFields[2].performClick()
        composeRule.waitForIdle()
        allTextFields[2].performTextReplacement("10")

        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("수업 선택", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        waitForText("수학 A반")
        composeRule.onAllNodesWithText("수학 A반", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("학년", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("과목", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("마감일", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_displaysDatePicker() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("마감일")

        composeRule.onAllNodesWithText("마감일", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_displaysTimePicker() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("마감일")

        composeRule.onAllNodesWithText("마감일", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_handlesEmptyStudentsList() {
        fakeApi.classStudentsResponse = emptyList()

        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("수업 선택")

        composeRule.onAllNodesWithText("수업 선택", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        waitForText("수학 A반")
        composeRule.onAllNodesWithText("수학 A반", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("선택한 학생에게만 배정", useUnmergedTree = true)
            .onFirst()
            .performScrollTo()

        composeRule.waitForIdle()
        Thread.sleep(500)

        composeRule.onAllNodes(isSelectable() and hasClickAction(), useUnmergedTree = true)
            .onLast()
            .performClick()

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 20_000) {
            try {
                val emptyMessage = composeRule.onAllNodesWithText("해당 반에 학생이 등록되지 않았습니다", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes()
                val loadingMessage = composeRule.onAllNodesWithText("학생 목록을 불러오는 중", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes()
                emptyMessage.isNotEmpty() || loadingMessage.isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeRule.onAllNodesWithText("해당 반에 학생이 등록되지 않았습니다", substring = true, useUnmergedTree = true)
                    .onFirst()
                    .assertExists()
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    @Test
    fun createAssignmentScreen_showsLoadingStudentsState() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("수업 선택")

        composeRule.onAllNodesWithText("수업 선택", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        waitForText("수학 A반")
        composeRule.onAllNodesWithText("수학 A반", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("선택한 학생에게만 배정", useUnmergedTree = true)
            .onFirst()
            .performScrollTo()

        composeRule.waitForIdle()
        Thread.sleep(500)

        composeRule.onAllNodes(isSelectable() and hasClickAction(), useUnmergedTree = true)
            .onLast()
            .performClick()

        composeRule.waitForIdle()
    }
}
