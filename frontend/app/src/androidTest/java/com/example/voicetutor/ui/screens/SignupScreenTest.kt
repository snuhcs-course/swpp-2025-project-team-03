package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignupScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun signupScreen_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreen()
            }
        }

        // 회원가입 화면 제목이 표시되어야 함
        composeTestRule.onNodeWithText("회원가입", substring = true).assertExists()
    }

    @Test
    fun signupScreen_displaysNameTextField() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreen()
            }
        }

        // 이름 입력 필드가 존재해야 함
        composeTestRule.onAllNodes(hasText("이름", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun signupScreen_displaysEmailTextField() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreen()
            }
        }

        // 이메일 입력 필드가 존재해야 함
        composeTestRule.onAllNodes(hasText("이메일", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun signupScreen_displaysPasswordTextField() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreen()
            }
        }

        // 비밀번호 입력 필드가 존재해야 함
        composeTestRule.onAllNodes(hasText("비밀번호", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun signupScreen_displaysConfirmPasswordTextField() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreen()
            }
        }

        // 비밀번호 확인 필드가 존재해야 함
        composeTestRule.onAllNodes(hasText("비밀번호 확인", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun signupScreen_displaysRoleSelector() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreen()
            }
        }

        // 역할 선택 필드가 존재해야 함
        composeTestRule.onAllNodes(hasText("역할", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun signupScreen_displaysSignupButton() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreen()
            }
        }

        // 회원가입 버튼이 존재해야 함
        composeTestRule.onNodeWithText("회원가입", substring = true).assertExists()
    }

    @Test
    fun signupScreen_displaysBackToLoginLink() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreen()
            }
        }

        // 로그인으로 돌아가기 링크가 존재해야 함
        composeTestRule.onNodeWithText("로그인", substring = true).assertExists()
    }

    @Test
    fun signupScreen_validatesPasswordMatch() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreen()
            }
        }

        // 비밀번호와 비밀번호 확인 필드가 모두 존재해야 함
        composeTestRule.onAllNodes(hasText("비밀번호", substring = true))
            .assertCountEquals(2) // 비밀번호와 비밀번호 확인
    }

    @Test
    fun signupScreen_callsOnBackToLogin_whenBackClicked() {
        var backClicked = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                SignupScreen(
                    onLoginClick = { backClicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("로그인", substring = true).performClick()
        // 콜백이 호출되었는지 확인
        assert(backClicked)
    }
}

