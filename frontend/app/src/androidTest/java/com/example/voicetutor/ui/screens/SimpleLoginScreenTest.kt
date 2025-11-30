package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@Ignore("LoginScreen requires ViewModel setup")
@RunWith(AndroidJUnit4::class)
class SimpleLoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_displaysLoginButton() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen()
            }
        }

        composeTestRule.onNodeWithText("로그인", useUnmergedTree = true).assertExists()
    }

    @Test
    fun loginScreen_displaysEmailField() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen()
            }
        }

        composeTestRule.onNodeWithText("이메일", useUnmergedTree = true).assertExists()
    }

    @Test
    fun loginScreen_displaysPasswordField() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen()
            }
        }

        composeTestRule.onNodeWithText("비밀번호", useUnmergedTree = true).assertExists()
    }
}
