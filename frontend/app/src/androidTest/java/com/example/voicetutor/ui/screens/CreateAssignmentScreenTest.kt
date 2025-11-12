package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
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

    @Test
    fun createAssignmentScreen_displaysFormSections() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("기본 정보")
        composeRule.onNodeWithText("기본 정보", useUnmergedTree = true).assertIsDisplayed()

        waitForText("과제 제목")
        composeRule.onNodeWithText("과제 제목", useUnmergedTree = true).assertIsDisplayed()

        waitForText("반 선택")
        composeRule.onNodeWithText("반 선택", useUnmergedTree = true).assertIsDisplayed()

        waitForText("과제 생성")
        composeRule.onNodeWithText("과제 생성", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun createAssignmentScreen_requiresAllFieldsBeforeEnablingButton() {
        composeRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(teacherId = "2")
            }
        }

        waitForText("과제 생성")
        composeRule.onNodeWithText("과제 생성", useUnmergedTree = true).assertIsNotEnabled()

        waitForText("과제 제목")
        composeRule.onNodeWithText("과제 제목", useUnmergedTree = true).performTextInput("테스트 과제")
        waitForText("과제 설명")
        composeRule.onNodeWithText("과제 설명", useUnmergedTree = true).performTextInput("테스트 설명")

        composeRule.onNodeWithText("과제 생성", useUnmergedTree = true).assertIsNotEnabled()
    }
}

