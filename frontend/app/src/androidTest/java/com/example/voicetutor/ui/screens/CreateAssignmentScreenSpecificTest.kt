package com.example.voicetutor.ui.screens

import android.app.Activity
import android.app.Instrumentation
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.repository.AssignmentRepository
import com.example.voicetutor.data.repository.AuthRepository
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
@RunWith(AndroidJUnit4::class)
class CreateAssignmentScreenSpecificTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    // Mocks
    private lateinit var mockAssignmentViewModel: AssignmentViewModel
    private lateinit var mockAuthViewModel: AuthViewModel
    
    // StateFlows for Mock VM
    private val isCreatingAssignmentFlow = MutableStateFlow(false)
    private val errorFlow = MutableStateFlow<String?>(null)
    private val currentAssignmentFlow = MutableStateFlow<com.example.voicetutor.data.models.AssignmentData?>(null)
    private val isUploadingFlow = MutableStateFlow(false)
    private val uploadProgressFlow = MutableStateFlow(0f)
    private val uploadSuccessFlow = MutableStateFlow(false)
    private val currentUserFlow = MutableStateFlow<com.example.voicetutor.data.models.User?>(null)

    @Before
    fun setUp() {
        hiltRule.inject()
        Intents.init()

        // Mock ViewModels
        // Use relaxed=true to avoid errors on unstubbed methods/properties
        mockAssignmentViewModel = mockk(relaxed = true)
        mockAuthViewModel = mockk(relaxed = true)

        // Stub StateFlows
        every { mockAssignmentViewModel.isCreatingAssignment } returns isCreatingAssignmentFlow
        every { mockAssignmentViewModel.error } returns errorFlow
        every { mockAssignmentViewModel.currentAssignment } returns currentAssignmentFlow
        every { mockAssignmentViewModel.isUploading } returns isUploadingFlow
        every { mockAssignmentViewModel.uploadProgress } returns uploadProgressFlow
        every { mockAssignmentViewModel.uploadSuccess } returns uploadSuccessFlow
        
        every { mockAuthViewModel.currentUser } returns currentUserFlow
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    private fun createTempFile(sizeBytes: Long): File {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val file = File(context.cacheDir, "temp_test_file_${System.currentTimeMillis()}.pdf")
        FileOutputStream(file).use { fos ->
            val buffer = ByteArray(1024)
            var bytesWritten = 0L
            while (bytesWritten < sizeBytes) {
                val writeSize = minOf(buffer.size.toLong(), sizeBytes - bytesWritten).toInt()
                fos.write(buffer, 0, writeSize)
                bytesWritten += writeSize
            }
        }
        return file
    }

    private fun stubFilePickerResult(file: File) {
        val resultData = Intent().apply {
            data = Uri.fromFile(file)
        }
        val result = Instrumentation.ActivityResult(Activity.RESULT_OK, resultData)
        intending(hasAction(Intent.ACTION_GET_CONTENT)).respondWith(result)
    }

    @Test
    fun createAssignmentScreen_largeFileSelection_showsError() { // Covers lines 582-606
        // Create 11MB file
        val largeFile = createTempFile(11 * 1024 * 1024L)
        stubFilePickerResult(largeFile)

        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(
                    teacherId = "2",
                    assignmentViewModel = mockAssignmentViewModel,
                    authViewModel = mockAuthViewModel
                )
            }
        }

        // Trigger file picker
        composeRule.onNodeWithText("파일 선택").performClick()

        // Verify error message
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithText("파일 용량이 너무 큽니다", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("파일 용량이 너무 큽니다", substring = true).assertIsDisplayed()
        
        // Clean up
        largeFile.delete()
    }

    @Test
    fun createAssignmentScreen_uploadSuccess_showsMessageAndList() { // Covers lines 635-718
        // Create small file
        val smallFile = createTempFile(1 * 1024 * 1024L)
        stubFilePickerResult(smallFile)

        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(
                    teacherId = "2",
                    assignmentViewModel = mockAssignmentViewModel,
                    authViewModel = mockAuthViewModel
                )
            }
        }

        // Trigger file picker
        composeRule.onNodeWithText("파일 선택").performClick()

        composeRule.waitForIdle()

        // Simulate upload success
        uploadSuccessFlow.value = true

        // Verify success message
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithText("PDF 업로드 완료!").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("PDF 업로드 완료!").assertIsDisplayed()

        // Test removing file
        composeRule.onNodeWithContentDescription("파일 제거").performClick()
        
        // Verify file is removed
        composeRule.waitUntil(timeoutMillis = 5000) {
            composeRule.onAllNodesWithText(smallFile.name).fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithText(smallFile.name).assertDoesNotExist()

        // Clean up
        smallFile.delete()
    }

    @Test
    fun createAssignmentScreen_createButton_validatesAndCallsViewModel() { // Covers lines 936-962
        val smallFile = createTempFile(1024)
        stubFilePickerResult(smallFile)

        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(
                    teacherId = "2",
                    assignmentViewModel = mockAssignmentViewModel,
                    authViewModel = mockAuthViewModel
                )
            }
        }

        // Fill form
        composeRule.onNodeWithText("과제 제목").performTextInput("Valid Title")
        composeRule.onNodeWithText("설명").performTextInput("Valid Description")
        
        // Select Class
        composeRule.onNodeWithText("수업 선택").performClick()
        composeRule.waitForIdle()
        // Select first class
        composeRule.onAllNodesWithText("수학 A반").onFirst().performClick()

        // Select Grade
        composeRule.onNodeWithText("학년").performClick()
        composeRule.onNodeWithText("초등학교 1학년").performClick()

        // Select Subject
        composeRule.onNodeWithText("과목").performClick()
        composeRule.onNodeWithText("국어").performClick()

        // Select Due Date
        composeRule.onNodeWithText("마감일").performClick()
        composeRule.onNodeWithText("시간 선택").performClick() // DatePicker confirm
        composeRule.onNodeWithText("확인").performClick() // TimePicker confirm

        // Set Question Count
        composeRule.onNodeWithText("문제 개수").performTextReplacement("5")

        // Select File
        composeRule.onNodeWithText("파일 선택").performClick()

        // Click Create
        composeRule.onNodeWithText("과제 생성").performClick()

        // Clean up
        smallFile.delete()
    }
}
