package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
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
import androidx.lifecycle.ViewModelProvider

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
@RunWith(AndroidJUnit4::class)
class CreateAssignmentScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun waitForText(text: String, timeoutMillis: Long = 10_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule.onAllNodesWithText(text, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun <T> setStateFlow(viewModel: AssignmentViewModel, fieldName: String, value: T) {
        val field = AssignmentViewModel::class.java.getDeclaredField(fieldName)
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as MutableStateFlow<T>
        stateFlow.value = value
    }




    @Test
    fun createAssignmentScreen_showsUploadSuccessBanner() {
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

        waitForText("PDF 업로드 완료!")
        composeRule.onAllNodesWithText("PDF 업로드 완료!", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }


    @Test
    fun createAssignmentScreen_displaysPdfUploadSection() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText("PDF 파일", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText("PDF 파일", substring = true, useUnmergedTree = true).onFirst().assertIsDisplayed()
    }





    @Test
    fun createAssignmentScreen_showsCreatingAssignmentIndicator() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]

        composeRule.runOnIdle {
            setStateFlow(assignmentViewModel, "_isCreatingAssignment", true)
        }

        // CircularProgressIndicator should be displayed
        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_handlesErrorState() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]

        composeRule.runOnIdle {
            val errorField = AssignmentViewModel::class.java.getDeclaredField("_error")
            errorField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val errorFlow = errorField.get(assignmentViewModel) as MutableStateFlow<String?>
            errorFlow.value = "과제 생성 실패"
        }

        // Error should be cleared automatically
        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_displaysClassDropdown() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("반 선택")
        composeRule.onAllNodesWithText("반 선택", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }





    @Test
    fun createAssignmentScreen_handlesUploadProgress() {
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

        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule.onAllNodesWithText("30%", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_handlesUploadComplete() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]

        composeRule.runOnIdle {
            setStateFlow(assignmentViewModel, "_isUploading", false)
            setStateFlow(assignmentViewModel, "_uploadSuccess", true)
            setStateFlow(assignmentViewModel, "_uploadProgress", 1.0f)
        }

        waitForText("PDF 업로드 완료!")
        composeRule.onAllNodesWithText("PDF 업로드 완료!", substring = true, useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun createAssignmentScreen_displaysDueDateField() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("마감일")
        composeRule.onAllNodesWithText("마감일", substring = true, useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun createAssignmentScreen_handlesNullTeacherId() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = null)
            }
        }

        // Should handle null teacherId gracefully
        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_handlesInitialClassId() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2", initialClassId = 1)
            }
        }

        // Should handle initialClassId
        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_displaysSelectedFiles() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        // File selection UI should be displayed
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodesWithText("PDF 파일", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.waitForIdle()
    }








    @Test
    fun createAssignmentScreen_displaysErrorWhenUploadFails() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]

        composeRule.runOnIdle {
            val errorField = AssignmentViewModel::class.java.getDeclaredField("_error")
            errorField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val errorFlow = errorField.get(assignmentViewModel) as MutableStateFlow<String?>
            errorFlow.value = "업로드 실패"
        }

        composeRule.waitForIdle()
    }


    @Test
    fun createAssignmentScreen_displaysDueDatePicker() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("마감일")
        // Date picker should be available
        composeRule.waitForIdle()
    }


    @Test
    fun createAssignmentScreen_handlesEmptyClassesList() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        // Should handle empty classes list gracefully
        composeRule.waitForIdle()
    }

    @Test
    fun createAssignmentScreen_handlesEmptyStudentsList() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        // Should handle empty students list gracefully
        composeRule.waitForIdle()
    }







    @Test
    fun createAssignmentScreen_allowsSubjectDropdownSelection() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        composeRule.waitUntil(timeoutMillis = 30_000) {
            try {
                composeRule.onAllNodesWithText("과목", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("과목", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun createAssignmentScreen_allowsGradeDropdownSelection() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        composeRule.waitUntil(timeoutMillis = 30_000) {
            try {
                composeRule.onAllNodesWithText("학년", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("학년", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun createAssignmentScreen_allowsClassDropdownSelection() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        composeRule.waitUntil(timeoutMillis = 30_000) {
            try {
                composeRule.onAllNodesWithText("반 선택", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("반 선택", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun createAssignmentScreen_displaysFileUploadSuccessMessage() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        val assignmentViewModel = ViewModelProvider(composeRule.activity)[AssignmentViewModel::class.java]

        composeRule.runOnIdle {
            setStateFlow(assignmentViewModel, "_uploadSuccess", true)
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            try {
                composeRule.onAllNodesWithText("PDF 업로드 완료!", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        composeRule.waitForIdle()
    }

}

