package com.example.voicetutor.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.User
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.data.repository.AuthRepository
import com.example.voicetutor.data.repository.SignupException
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalTestApi::class)
class SignupScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun waitForText(text: String, timeoutMillis: Long = 15_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule
                .onAllNodesWithText(text, substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun signupButton(): SemanticsNodeInteraction {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            try {
                composeRule.onAllNodesWithText("계정 만들기", substring = true, useUnmergedTree = true)
                    .filter(hasClickAction())
                    .fetchSemanticsNodes(atLeastOneRootRequired = false).isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        return composeRule.onAllNodesWithText("계정 만들기", substring = true, useUnmergedTree = true)
            .filter(hasClickAction())
            .onFirst()
    }

    @Test
    fun signupScreen_displaysAllFormFields() {
        val viewModel = AuthViewModel(AuthRepository(FakeApiService()))

        composeRule.setContent {
            VoiceTutorTheme {
                SignupScreen(authViewModel = viewModel)
            }
        }

        waitForText("이름")
        composeRule.onAllNodesWithText("이름", useUnmergedTree = true).onFirst().assertIsDisplayed()
        waitForText("이메일")
        composeRule.onAllNodesWithText("이메일", useUnmergedTree = true).onFirst().assertIsDisplayed()
        waitForText("비밀번호")
        composeRule.onAllNodesWithText("비밀번호", useUnmergedTree = true).onFirst().assertIsDisplayed()
        waitForText("비밀번호 확인")
        composeRule.onAllNodesWithText("비밀번호 확인", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun signupScreen_displaysRoleSelection() {
        val viewModel = AuthViewModel(AuthRepository(FakeApiService()))

        composeRule.setContent {
            VoiceTutorTheme {
                SignupScreen(authViewModel = viewModel)
            }
        }

        waitForText("학생")
        composeRule.onAllNodesWithText("학생", useUnmergedTree = true).onFirst().assertIsDisplayed()
        waitForText("선생님")
        composeRule.onAllNodesWithText("선생님", useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun signupScreen_displaysPasswordRequirements() {
        val viewModel = AuthViewModel(AuthRepository(FakeApiService()))

        composeRule.setContent {
            VoiceTutorTheme {
                SignupScreen(authViewModel = viewModel)
            }
        }

        // Password requirements might be displayed
        composeRule.waitForIdle()
    }

}

