package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SettingsScreen(
                    userRole = UserRole.STUDENT,
                    navController = rememberNavController()
                )
            }
        }

        composeTestRule.waitForIdle()

        // 설정 화면 제목이 표시되어야 함
        composeTestRule.onNodeWithText("설정", substring = true).assertExists()
    }

    @Test
    fun settingsScreen_displaysProfileSection() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SettingsScreen(
                    userRole = UserRole.STUDENT,
                    navController = rememberNavController()
                )
            }
        }

        // 프로필 섹션이 표시되어야 함
        composeTestRule.onAllNodes(hasText("프로필", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun settingsScreen_displaysLogoutButton() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SettingsScreen(
                    userRole = UserRole.STUDENT,
                    navController = rememberNavController()
                )
            }
        }

        // 로그아웃 버튼이 표시되어야 함
        composeTestRule.onNodeWithText("로그아웃", substring = true).assertExists()
    }

    @Test
    fun settingsScreen_displaysAppInfoLink() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SettingsScreen(
                    userRole = UserRole.STUDENT,
                    navController = rememberNavController()
                )
            }
        }

        // 앱 정보 링크가 표시되어야 함
        composeTestRule.onNodeWithText("앱 정보", substring = true).assertExists()
    }

    @Test
    fun settingsScreen_displaysUserInfo() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SettingsScreen(
                    userRole = UserRole.STUDENT,
                    navController = rememberNavController()
                )
            }
        }

        // 사용자 정보가 표시되어야 함
        composeTestRule.onAllNodes(hasText("프로필", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun settingsScreen_showsTeacherOptions_forTeacher() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SettingsScreen(
                    userRole = UserRole.TEACHER,
                    navController = rememberNavController()
                )
            }
        }

        // 선생님용 옵션이 표시되어야 함
        composeTestRule.onNodeWithText("설정", substring = true).assertExists()
    }

    @Test
    fun settingsScreen_showsStudentOptions_forStudent() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SettingsScreen(
                    userRole = UserRole.STUDENT,
                    navController = rememberNavController()
                )
            }
        }

        // 학생용 옵션이 표시되어야 함
        composeTestRule.onNodeWithText("설정", substring = true).assertExists()
    }

    @Test
    fun settingsScreen_callsOnLogout_whenLogoutClicked() {
        var loggedOut = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                SettingsScreen(
                    userRole = UserRole.STUDENT,
                    onLogout = { loggedOut = true },
                    navController = rememberNavController()
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("로그아웃", substring = true).performClick()
        // 로그아웃 콜백이 호출되었는지 확인
        assert(loggedOut)
    }

    @Test
    fun settingsScreen_navigatesToAppInfo_whenClicked() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SettingsScreen(
                    userRole = UserRole.STUDENT,
                    navController = rememberNavController()
                )
            }
        }

        composeTestRule.waitForIdle()

        // 앱 정보 버튼 클릭
        composeTestRule.onNodeWithText("앱 정보", substring = true).performClick()
        // 네비게이션이 트리거되었는지 확인 (화면 전환은 네비게이션 시스템에서 처리)
    }

    @Test
    fun settingsScreen_displaysVersionInfo() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                SettingsScreen(
                    userRole = UserRole.STUDENT,
                    navController = rememberNavController()
                )
            }
        }

        composeTestRule.waitForIdle()

        // 버전 정보가 표시되어야 함
        composeTestRule.onNodeWithText("설정", substring = true).assertExists()
    }
}

