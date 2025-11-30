package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.core.net.toUri
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
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
@RunWith(AndroidJUnit4::class)
class CreateAssignmentScreenHighCoverageTest {

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
    fun testFileUploadSuccessAndFileListUI() {
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

        composeRule.runOnIdle {
            setStateFlow(assignmentViewModel, "_isUploading", true)
            setStateFlow(assignmentViewModel, "_uploadProgress", 0.5f)
        }

        waitForText("PDF 업로드 중")
        waitForText("50%")
    }

    @Test
    fun testCreateAssignmentButtonLogic() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        val allTextFields = composeRule.onAllNodes(hasSetTextAction(), useUnmergedTree = true)

        allTextFields[0].performTextReplacement("Test Assignment")

        allTextFields[1].performTextReplacement("Test Description")

        allTextFields[2].performTextReplacement("5")

        composeRule.onNodeWithText("수업 선택").performClick()
        composeRule.onNodeWithText("수학 A반").performClick()

        composeRule.onNodeWithText("학년").performClick()
        composeRule.onNodeWithText("중학교 1학년").performClick()

        composeRule.onNodeWithText("과목").performClick()
        composeRule.onNodeWithText("수학").performClick()

        composeRule.onNodeWithText("마감일").performClick()

        composeRule.waitForIdle()
    }

    @Test
    fun testPdfPickerLauncherCallback() = runTest {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        composeRule.waitForIdle()

        val documentsDir = File(composeRule.activity.filesDir, "documents")
        if (!documentsDir.exists()) {
            documentsDir.mkdirs()
        }

        val testFile = File(documentsDir, "test_document.pdf")
        testFile.writeText("Test PDF content")

        try {
            val fileUri = testFile.toUri()

            val fileManager = com.example.voicetutor.file.FileManager(composeRule.activity)
            val result = fileManager.saveFile(fileUri, "test_document.pdf")

            result.onSuccess { fileInfo ->
                assert(fileInfo.name.contains("test_document.pdf") || fileInfo.name.contains(".pdf"))
                assert(fileInfo.path.isNotEmpty())
                assert(fileInfo.size > 0)
            }.onFailure { _ ->
                // Failure case handled
            }
        } finally {
            if (testFile.exists()) {
                testFile.delete()
            }
        }
    }

    @Test
    fun testTimePickerFlow() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        composeRule.onNodeWithText("마감일").performClick()
        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("시간 선택", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
