package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.ui.navigation.VoiceTutorNavigation
import com.example.voicetutor.ui.navigation.VoiceTutorScreens
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@org.junit.Ignore("Integration tests require real network access")
@MediumTest
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class AuthIntegrationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun studentSignupAndLogin_NavigatesToStudentDashboard() = runTest {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                VoiceTutorNavigation(
                    navController = navController,
                    startDestination = VoiceTutorScreens.Login.route,
                )
            }
        }

        composeTestRule.onNode(
            hasText("계정 만들기") and hasClickAction(),
        ).performClick()

        Thread.sleep(1000)

        composeTestRule.onNodeWithText("VoiceTutor와 함께 시작하세요").assertIsDisplayed()

        composeTestRule.onNodeWithText("학생").performClick()

        val testEmail = "student_test_${System.currentTimeMillis()}@test.com"
        composeTestRule.onAllNodesWithText("이름")[0].performTextInput("테스트학생")
        composeTestRule.onAllNodesWithText("이메일")[0].performTextInput(testEmail)
        composeTestRule.onAllNodesWithText("비밀번호")[0].performTextInput("Password123!")
        composeTestRule.onNodeWithText("비밀번호 확인").performTextInput("Password123!")

        composeTestRule.onNode(
            hasText("계정 만들기") and hasClickAction() and !hasText("VoiceTutor"),
        ).performClick()

        Thread.sleep(3000)

        composeTestRule.onNodeWithText("VoiceTutor").assertIsDisplayed()

        composeTestRule.onAllNodesWithText("이메일")[0].performTextInput(testEmail)
        composeTestRule.onAllNodesWithText("비밀번호")[0].performTextInput("Password123!")
        composeTestRule.onAllNodesWithText("로그인")[0].performClick()

        Thread.sleep(3000)

        composeTestRule.onNodeWithText("안녕하세요, 테스트학생", substring = true, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun teacherSignupAndLogin_NavigatesToTeacherDashboard() = runTest {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                VoiceTutorNavigation(
                    navController = navController,
                    startDestination = VoiceTutorScreens.Login.route,
                )
            }
        }

        composeTestRule.onNode(
            hasText("계정 만들기") and hasClickAction(),
        ).performClick()

        Thread.sleep(1000)

        composeTestRule.onNodeWithText("VoiceTutor와 함께 시작하세요").assertIsDisplayed()

        composeTestRule.onNodeWithText("선생님").performClick()

        val testEmail = "teacher_test_${System.currentTimeMillis()}@test.com"
        composeTestRule.onAllNodesWithText("이름")[0].performTextInput("테스트선생님")
        composeTestRule.onAllNodesWithText("이메일")[0].performTextInput(testEmail)
        composeTestRule.onAllNodesWithText("비밀번호")[0].performTextInput("Password123!")
        composeTestRule.onNodeWithText("비밀번호 확인").performTextInput("Password123!")

        composeTestRule.onNode(
            hasText("계정 만들기") and hasClickAction() and !hasText("VoiceTutor"),
        ).performClick()

        Thread.sleep(3000)

        composeTestRule.onNodeWithText("VoiceTutor").assertIsDisplayed()

        composeTestRule.onAllNodesWithText("이메일")[0].performTextInput(testEmail)
        composeTestRule.onAllNodesWithText("비밀번호")[0].performTextInput("Password123!")
        composeTestRule.onAllNodesWithText("로그인")[0].performClick()

        Thread.sleep(3000)

        composeTestRule.onNodeWithText("환영합니다, 테스트 선생님", substring = true, useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun existingStudentLogin_NavigatesToStudentDashboard() = runTest {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                VoiceTutorNavigation(
                    navController = navController,
                    startDestination = VoiceTutorScreens.Login.route,
                )
            }
        }

        composeTestRule.onNodeWithText("VoiceTutor").assertIsDisplayed()

        Thread.sleep(1000)

        composeTestRule.onAllNodesWithText("이메일")[0].performTextInput("student@voicetutor.com")
        composeTestRule.onAllNodesWithText("비밀번호")[0].performTextInput("student123")

        composeTestRule.onAllNodesWithText("로그인")[0].performClick()

        Thread.sleep(3000)

        composeTestRule.onNodeWithText("안녕하세요", substring = true, useUnmergedTree = true).assertExists()
    }

    @Test
    fun existingTeacherLogin_NavigatesToTeacherDashboard() = runTest {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                VoiceTutorNavigation(
                    navController = navController,
                    startDestination = VoiceTutorScreens.Login.route,
                )
            }
        }

        composeTestRule.onNodeWithText("VoiceTutor").assertIsDisplayed()

        Thread.sleep(1000)

        composeTestRule.onAllNodesWithText("이메일")[0].performTextInput("teacher@voicetutor.com")
        composeTestRule.onAllNodesWithText("비밀번호")[0].performTextInput("teacher123")

        composeTestRule.onAllNodesWithText("로그인")[0].performClick()

        Thread.sleep(3000)

        composeTestRule.onNodeWithText("환영합니다", substring = true, useUnmergedTree = true).assertExists()
    }
}
