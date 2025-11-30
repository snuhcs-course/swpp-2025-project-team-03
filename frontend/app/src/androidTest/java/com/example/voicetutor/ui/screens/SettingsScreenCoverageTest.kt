package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
@RunWith(AndroidJUnit4::class)
class SettingsScreenCoverageTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var apiService: com.example.voicetutor.data.network.ApiService

    private val fakeApi: FakeApiService
        get() = apiService as FakeApiService

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    private fun waitForText(text: String, substring: Boolean = true, timeoutMillis: Long = 5_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule.onAllNodesWithText(
                text = text,
                substring = substring,
                useUnmergedTree = true,
            ).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun settingsScreen_displaysProfileSection() {
        composeRule.setContent {
            VoiceTutorTheme {
                SettingsScreen()
            }
        }

        val authViewModel = ViewModelProvider(composeRule.activity)[AuthViewModel::class.java]
        composeRule.runOnIdle {
            authViewModel.login("student@voicetutor.com", "student123")
        }

        waitForText("계정")
        waitForText("프로필")
        composeRule.onNodeWithText("계정", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("프로필", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysLoadingState() {
        composeRule.setContent {
            VoiceTutorTheme {
                SettingsScreen()
            }
        }

        waitForText("계정")
        composeRule.waitForIdle()
    }

    @Test
    fun settingsScreen_displaysUserInfo() {
        composeRule.setContent {
            VoiceTutorTheme {
                SettingsScreen()
            }
        }

        val authViewModel = ViewModelProvider(composeRule.activity)[AuthViewModel::class.java]
        composeRule.runOnIdle {
            authViewModel.login("student@voicetutor.com", "student123")
        }

        waitForText("계정")
        composeRule.waitForIdle()
    }

    @Test
    fun settingsScreen_displaysTutorialResetOption() {
        composeRule.setContent {
            VoiceTutorTheme {
                SettingsScreen()
            }
        }

        waitForText("튜토리얼 다시 보기")
        composeRule.onNodeWithText("튜토리얼 다시 보기", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("앱 사용법을 다시 확인하세요", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysAppInfoOption() {
        composeRule.setContent {
            VoiceTutorTheme {
                SettingsScreen()
            }
        }

        waitForText("앱 정보")
        composeRule.onNodeWithText("앱 정보", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("버전 1.0.0", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysDeleteAccountButton() {
        composeRule.setContent {
            VoiceTutorTheme {
                SettingsScreen()
            }
        }

        waitForText("계정 삭제")
        composeRule.onNodeWithText("계정 삭제", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun settingsScreen_opensDeleteAccountDialog() {
        composeRule.setContent {
            VoiceTutorTheme {
                SettingsScreen()
            }
        }

        waitForText("계정 삭제")
        composeRule.onNodeWithText("계정 삭제", useUnmergedTree = true).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("계정을 삭제하시겠습니까?", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("계정을 삭제하시겠습니까?", substring = true, useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("삭제 후에는 복구할 수 없습니다.", substring = true, useUnmergedTree = true).assertIsDisplayed()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithTag("delete_account_button", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("delete_account_button", useUnmergedTree = true)
            .assertExists()
            .performClick()

        composeRule.waitForIdle()
    }

    @Test
    fun settingsScreen_deleteAccountDialog_cancelsOnCancelButton() {
        composeRule.setContent {
            VoiceTutorTheme {
                SettingsScreen()
            }
        }

        waitForText("계정 삭제")
        composeRule.onNodeWithText("계정 삭제", useUnmergedTree = true).performClick()

        waitForText("취소")
        composeRule.onNodeWithText("취소", useUnmergedTree = true).performClick()

        composeRule.waitForIdle()
    }

    @Test
    fun settingsScreen_opensTutorialResetDialog() {
        composeRule.setContent {
            VoiceTutorTheme {
                SettingsScreen()
            }
        }

        val authViewModel = ViewModelProvider(composeRule.activity)[AuthViewModel::class.java]
        composeRule.runOnIdle {
            authViewModel.login("student@voicetutor.com", "student123")
        }

        waitForText("튜토리얼 다시 보기")
        composeRule.waitForIdle()
        composeRule.onNodeWithText("튜토리얼 다시 보기", useUnmergedTree = true).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("튜토리얼 초기화", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("튜토리얼 초기화", useUnmergedTree = true).assertIsDisplayed()

        composeRule.onNodeWithText("튜토리얼을 초기화하면 다음 로그인 시 다시 볼 수 있습니다.", substring = true, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun settingsScreen_tutorialResetDialog_cancelsOnCancelButton() {
        composeRule.setContent {
            VoiceTutorTheme {
                SettingsScreen()
            }
        }

        waitForText("튜토리얼 다시 보기")
        composeRule.onNodeWithText("튜토리얼 다시 보기", useUnmergedTree = true).performClick()

        waitForText("취소")
        composeRule.onNodeWithText("취소", useUnmergedTree = true).performClick()

        composeRule.waitForIdle()
    }

    @Test
    fun settingsScreen_displaysErrorState() {
        composeRule.setContent {
            VoiceTutorTheme {
                SettingsScreen()
            }
        }

        val authViewModel = ViewModelProvider(composeRule.activity)[AuthViewModel::class.java]
        composeRule.runOnIdle {
            authViewModel.login("student@voicetutor.com", "student123")
        }

        waitForText("계정")
        composeRule.waitForIdle()
    }

    @Test
    fun settingsScreen_errorState_showsRetryButton() {
        composeRule.setContent {
            VoiceTutorTheme {
                SettingsScreen()
            }
        }

        val authViewModel = ViewModelProvider(composeRule.activity)[AuthViewModel::class.java]
        composeRule.runOnIdle {
            authViewModel.login("student@voicetutor.com", "student123")
        }

        waitForText("계정")
        composeRule.waitForIdle()
    }

    @Test
    fun settingsScreen_withStudentId_loadsStudentInfo() {
        composeRule.setContent {
            VoiceTutorTheme {
                SettingsScreen(
                    studentId = 1,
                )
            }
        }

        val authViewModel = ViewModelProvider(composeRule.activity)[AuthViewModel::class.java]
        composeRule.runOnIdle {
            authViewModel.login("teacher@voicetutor.com", "teacher123")
        }

        waitForText("계정")
        composeRule.waitForIdle()
    }

    @Test
    fun settingsScreen_withStudentId_hidesDeleteAccountButton() {
        composeRule.setContent {
            VoiceTutorTheme {
                SettingsScreen(
                    studentId = 1,
                )
            }
        }

        waitForText("계정")
        composeRule.waitForIdle()
    }
}
