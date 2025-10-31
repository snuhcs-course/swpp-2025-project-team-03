package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeacherMessageScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun teacherMessage_displaysStudentName() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherMessageScreen(
                    studentId = 1
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("홍길동", substring = true).assertExists()
    }

    @Test
    fun teacherMessage_displaysMessageList() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherMessageScreen(
                    studentId = 1
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("홍길동", substring = true).assertExists()
    }

    @Test
    fun teacherMessage_displaysSendButton() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherMessageScreen(
                    studentId = 1
                )
            }
        }

        composeTestRule.onNodeWithText("전송", substring = true).assertExists()
    }

    @Test
    fun teacherMessage_displaysMessageInput() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherMessageScreen(
                    studentId = 1
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("홍길동", substring = true).assertExists()
    }
}

