package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Espresso/Compose UI tests for LoginScreen.
 * Tests UI interactions and state without modifying the original file.
 *
 * NOTE: Disabled due to MockK incompatibility with Android Instrumentation tests.
 */
@Ignore("MockK incompatible with Android tests - use Hilt-based tests instead")
@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var mockAuthViewModel: AuthViewModel
    private lateinit var mockAssignmentViewModel: AssignmentViewModel

    @Before
    fun setup() {
        mockAuthViewModel = mockk(relaxed = true) {
            every { currentUser } returns MutableStateFlow(null)
            every { isLoading } returns MutableStateFlow(false)
            every { loginError } returns MutableStateFlow(null)
            every { autoFillCredentials } returns MutableStateFlow(null)
        }
        mockAssignmentViewModel = mockk(relaxed = true)
    }

    @Test
    fun displaysLoginForm() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel,
                )
            }
        }

        // Verify login form elements exist
        composeTestRule.onNodeWithText("이메일", substring = true)
            .assertExists()
        composeTestRule.onNodeWithText("비밀번호", substring = true)
            .assertExists()
        composeTestRule.onNodeWithText("로그인", substring = true)
            .assertExists()
    }

    @Test
    fun displaysSignupLink() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel,
                )
            }
        }

        composeTestRule.onNodeWithText("회원가입", substring = true)
            .assertExists()
    }

    @Test
    fun displaysLoadingState() {
        every { mockAuthViewModel.isLoading } returns MutableStateFlow(true)

        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel,
                )
            }
        }

        // Loading indicator should be visible
        composeTestRule.onAllNodesWithContentDescription("Loading")
            .onFirst()
            .assertExists()
    }

    @Test
    fun emailFieldIsEditable() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel,
                )
            }
        }

        // Find email field and type
        composeTestRule.onNodeWithText("이메일", substring = true)
            .performTextInput("test@example.com")
    }

    @Test
    fun passwordFieldIsEditable() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel,
                )
            }
        }

        // Find password field and type
        composeTestRule.onNodeWithText("비밀번호", substring = true)
            .performTextInput("password123")
    }

    @Test
    fun loginButtonIsClickable() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel,
                )
            }
        }

        composeTestRule.onNodeWithText("로그인", substring = true)
            .assertIsEnabled()
            .performClick()

        // Verify login was called
        verify(exactly = 1) { mockAuthViewModel.login(any(), any()) }
    }

    @Test
    fun signupLinkIsClickable() {
        var signupClicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen(
                    authViewModel = mockAuthViewModel,
                    assignmentViewModel = mockAssignmentViewModel,
                    onSignupClick = { signupClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithText("회원가입", substring = true)
            .performClick()

        assert(signupClicked)
    }
}
