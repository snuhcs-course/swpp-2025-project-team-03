package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import com.example.voicetutor.ui.viewmodel.SignupError
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
@RunWith(AndroidJUnit4::class)
class SignupScreenCoverageTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun <T> setSignupError(viewModel: AuthViewModel, value: T) {
        val field = AuthViewModel::class.java.getDeclaredField("_signupError")
        field.isAccessible = true
        @Suppress("UNCHECKED_CAST")
        val stateFlow = field.get(viewModel) as MutableStateFlow<T>
        stateFlow.value = value
    }

    @Test
    fun testGeneralErrorHandling() {
        composeRule.setContent {
            VoiceTutorTheme {
                SignupScreen()
            }
        }

        val viewModel = ViewModelProvider(composeRule.activity)[AuthViewModel::class.java]

        composeRule.runOnIdle {
            setSignupError(viewModel, SignupError.General.DuplicateEmail("이미 가입된 이메일입니다."))
        }
        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("이미 가입된 이메일입니다.", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.runOnIdle {
            setSignupError(viewModel, SignupError.General.Network("네트워크 연결을 확인해주세요."))
        }
        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("네트워크 연결을 확인해주세요.", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.runOnIdle {
            setSignupError(viewModel, SignupError.General.Server("서버 오류가 발생했습니다."))
        }
        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("서버 오류가 발생했습니다.", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.runOnIdle {
            setSignupError(viewModel, SignupError.General.Unknown("알 수 없는 오류가 발생했습니다."))
        }
        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("알 수 없는 오류가 발생했습니다.", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
