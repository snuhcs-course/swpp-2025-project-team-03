package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso/Compose UI tests for SignupScreen.
 * 
 * NOTE: Disabled due to MockK incompatibility with Android Instrumentation tests.
 */
@Ignore("MockK incompatible with Android tests - use Hilt-based tests instead")
@RunWith(AndroidJUnit4::class)
class SignupScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockAuthViewModel: AuthViewModel

    @Before
    fun setup() {
        mockAuthViewModel = mockk(relaxed = true) {
            every { currentUser } returns MutableStateFlow(null)
            every { isLoading } returns MutableStateFlow(false)
            every { signupError } returns MutableStateFlow(null)
        }
    }

    @Test
    fun displaysSignupForm() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreen(authViewModel = mockAuthViewModel)
            }
        }

        // Verify signup form elements exist
        composeTestRule.onNodeWithText("이름", substring = true)
            .assertExists()
        composeTestRule.onNodeWithText("이메일", substring = true)
            .assertExists()
        composeTestRule.onNodeWithText("비밀번호", substring = true)
            .assertExists()
        composeTestRule.onNodeWithText("비밀번호 확인", substring = true)
            .assertExists()
    }

    @Test
    fun displaysRoleSelection() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreen(authViewModel = mockAuthViewModel)
            }
        }

        // Role selection should be visible
        composeTestRule.onNodeWithText("학생", substring = true)
            .assertExists()
        composeTestRule.onNodeWithText("선생님", substring = true)
            .assertExists()
    }

    @Test
    fun displaysSignupButton() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreen(authViewModel = mockAuthViewModel)
            }
        }

        composeTestRule.onNodeWithText("회원가입", substring = true)
            .assertExists()
    }

    @Test
    fun displaysLoginLink() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreen(authViewModel = mockAuthViewModel)
            }
        }

        composeTestRule.onNodeWithText("로그인", substring = true)
            .assertExists()
    }

    @Test
    fun allFieldsAreEditable() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreen(authViewModel = mockAuthViewModel)
            }
        }

        // Test name field
        composeTestRule.onNodeWithText("이름", substring = true)
            .performTextInput("홍길동")

        // Test email field
        composeTestRule.onNodeWithText("이메일", substring = true)
            .performTextInput("test@example.com")

        // Test password field
        composeTestRule.onNodeWithText("비밀번호", substring = true)
            .performTextInput("password123")

        // Test confirm password field
        composeTestRule.onNodeWithText("비밀번호 확인", substring = true)
            .performTextInput("password123")
    }

    @Test
    fun signupButtonIsClickable() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreen(authViewModel = mockAuthViewModel)
            }
        }

        composeTestRule.onNodeWithText("회원가입", substring = true)
            .assertIsEnabled()
            .performClick()
    }

    @Test
    fun displaysLoadingState() {
        every { mockAuthViewModel.isLoading } returns MutableStateFlow(true)

        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreen(authViewModel = mockAuthViewModel)
            }
        }

        // Loading indicator should be visible
        composeTestRule.onAllNodesWithContentDescription("Loading")
            .onFirst()
            .assertExists()
    }
}

