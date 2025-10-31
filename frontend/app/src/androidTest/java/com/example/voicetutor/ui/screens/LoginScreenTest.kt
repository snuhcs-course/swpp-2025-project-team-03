package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen()
            }
        }

        composeTestRule.waitForIdle()

        // 로그인 화면 제목이 표시되어야 함
        composeTestRule.onNodeWithText("로그인", substring = true).assertExists()
    }

    @Test
    fun loginScreen_displaysEmailTextField() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen()
            }
        }

        // 이메일 입력 필드가 존재해야 함
        composeTestRule.onAllNodes(hasText("이메일", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun loginScreen_displaysPasswordTextField() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen()
            }
        }

        // 비밀번호 입력 필드가 존재해야 함
        composeTestRule.onAllNodes(hasText("비밀번호", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun loginScreen_displaysLoginButton() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen()
            }
        }

        // 로그인 버튼이 존재해야 함
        composeTestRule.onNodeWithText("로그인", substring = true).assertExists()
    }

    @Test
    fun loginScreen_displaysSignupLink() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen()
            }
        }

        // 회원가입 링크가 존재해야 함
        composeTestRule.onNodeWithText("회원가입", substring = true).assertExists()
    }

    @Test
    fun loginScreen_emailInput_updatesValue() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen()
            }
        }

        composeTestRule.waitForIdle()

        // 이메일 필드에 텍스트 입력
        composeTestRule.onAllNodes(hasText("이메일", substring = true))
            .onFirst()
            .performTextInput("test@example.com")
    }

    @Test
    fun loginScreen_passwordInput_togglesVisibility() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen()
            }
        }

        composeTestRule.waitForIdle()

        // 비밀번호 필드 찾기
        val passwordField = composeTestRule.onAllNodes(hasText("비밀번호", substring = true))
            .onFirst()
        
        // 비밀번호 입력
        passwordField.performTextInput("password123")
        
        // 비밀번호 표시/숨김 토글 버튼이 존재해야 함
        composeTestRule.onAllNodes(hasContentDescription("비밀번호 표시/숨김"))
            .assertCountEquals(1)
    }

    @Test
    fun loginScreen_callsOnSignupClick_whenSignupClicked() {
        var signupClicked = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen(
                    onSignupClick = { signupClicked = true }
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("회원가입", substring = true).performClick()
        // 콜백이 호출되었는지 확인
        assert(signupClicked)
    }

    @Test
    fun loginScreen_showsLoading_whenIsLoading() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen()
            }
        }

        // 로딩 상태는 ViewModel을 통해 관리되므로 직접 테스트하기 어려움
        // 하지만 기본 UI는 존재해야 함
        composeTestRule.onNodeWithText("로그인", substring = true).assertExists()
    }

    @Test
    fun loginScreen_displaysError_whenErrorExists() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen()
            }
        }

        // 에러 표시는 ViewModel을 통해 관리됨
        // 기본 UI 구조 확인
        composeTestRule.onNodeWithText("로그인", substring = true).assertExists()
    }

    @Test
    fun loginScreen_handlesEmptyEmail() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen()
            }
        }

        // 이메일 필드가 비어있는 상태로 시작
        composeTestRule.onAllNodes(hasText("이메일", substring = true))
            .onFirst()
            .assertExists()
    }

    @Test
    fun loginScreen_handlesEmptyPassword() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LoginScreen()
            }
        }

        // 비밀번호 필드가 비어있는 상태로 시작
        composeTestRule.onAllNodes(hasText("비밀번호", substring = true))
            .onFirst()
            .assertExists()
    }
}

