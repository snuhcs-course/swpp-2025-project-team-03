package com.example.voicetutor.ui.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
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
class SignupScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun waitForText(text: String, timeoutMillis: Long = 5_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule
                .onAllNodesWithText(text, substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun signupScreen_showsValidationErrors_whenSubmittingEmptyForm() {
        val viewModel = AuthViewModel(AuthRepository(FakeApiService()))

        composeRule.setContent {
            VoiceTutorTheme {
                SignupScreen(authViewModel = viewModel)
            }
        }

        composeRule.onNodeWithText("계정 만들기", useUnmergedTree = true).performClick()

        waitForText("이름을 입력해주세요")
        composeRule.onNodeWithText("이름을 입력해주세요", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun signupScreen_withValidInput_updatesCurrentUser() {
        val successfulRepository = object : AuthRepository(FakeApiService()) {
            override suspend fun signup(name: String, email: String, password: String, role: UserRole): Result<User> {
                delay(100) // simulate network call
                val user = User(
                    id = 100,
                    name = name,
                    email = email,
                    role = role,
                    isStudent = role == UserRole.STUDENT,
                    assignments = emptyList()
                )
                return Result.success(user)
            }
        }
        val authViewModel = AuthViewModel(successfulRepository)
        val lastUserEmail = AtomicReference<String?>(null)

        composeRule.setContent {
            VoiceTutorTheme {
                SignupScreen(authViewModel = authViewModel)
            }
        }

        composeRule.onNodeWithText("이름", useUnmergedTree = true).performTextInput("테스트 학생")
        composeRule.onNodeWithText("이메일", useUnmergedTree = true).performTextInput("student@example.com")
        composeRule.onNodeWithText("비밀번호", useUnmergedTree = true).performTextInput("password123")
        composeRule.onNodeWithText("비밀번호 확인", useUnmergedTree = true).performTextInput("password123")

        composeRule.onNodeWithText("계정 만들기", useUnmergedTree = true).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            authViewModel.currentUser.value?.also { lastUserEmail.set(it.email) } != null
        }

        composeRule.waitUntil(timeoutMillis = 5_000) { lastUserEmail.get() == "student@example.com" }
    }

    @Test
    fun signupScreen_duplicateEmail_showsErrorMessage() {
        val errorRepository = object : AuthRepository(FakeApiService()) {
            override suspend fun signup(name: String, email: String, password: String, role: UserRole): Result<User> {
                return Result.failure(SignupException.DuplicateEmail("이미 사용 중인 이메일입니다"))
            }
        }
        val authViewModel = AuthViewModel(errorRepository)

        composeRule.setContent {
            VoiceTutorTheme {
                SignupScreen(authViewModel = authViewModel)
            }
        }

        composeRule.onNodeWithText("이름", useUnmergedTree = true).performTextInput("테스트 학생")
        composeRule.onNodeWithText("이메일", useUnmergedTree = true).performTextInput("student@example.com")
        composeRule.onNodeWithText("비밀번호", useUnmergedTree = true).performTextInput("password123")
        composeRule.onNodeWithText("비밀번호 확인", useUnmergedTree = true).performTextInput("password123")

        composeRule.onNodeWithText("계정 만들기", useUnmergedTree = true).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            authViewModel.signupError.value != null
        }

        composeRule.onNodeWithText("이미 사용 중인 이메일입니다", substring = true, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("로그인하기", useUnmergedTree = true).assertIsDisplayed()
    }
}

