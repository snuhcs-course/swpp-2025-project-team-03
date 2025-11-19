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

/**
 * Integration test for Sign Up and Login flows.
 * Tests the complete user journey from signup to login and navigation to the appropriate dashboard.
 *
 * This test follows the same pattern as TaskDetailFragmentTest, performing end-to-end testing
 * of the authentication flow without mocking the backend.
 *
 * Uses HiltComponentActivity for Hilt dependency injection support in Compose tests.
 */
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

    /**
     * Test student signup and login flow.
     * GIVEN - User wants to sign up as a student
     * WHEN - User completes signup and then logs in
     * THEN - User is navigated to Student Dashboard
     */
    @Test
    fun studentSignupAndLogin_NavigatesToStudentDashboard() = runTest {
        // GIVEN - Launch the app at login screen
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                VoiceTutorNavigation(
                    navController = navController,
                    startDestination = VoiceTutorScreens.Login.route,
                )
            }
        }
        // WHEN - Navigate to signup screen
        // Click the "계정 만들기" button (not the title text)
        composeTestRule.onNode(
            hasText("계정 만들기") and hasClickAction(),
        ).performClick()

        // (Optional for explanation) Pause to visually confirm signup screen loading
        Thread.sleep(1000)

        // Verify we're on the signup screen by checking for unique signup text
        composeTestRule.onNodeWithText("VoiceTutor와 함께 시작하세요").assertIsDisplayed()

        // Select Student role
        composeTestRule.onNodeWithText("학생").performClick()
        // Fill in signup form - using unique email with timestamp
        val testEmail = "student_test_${System.currentTimeMillis()}@test.com"
        composeTestRule.onAllNodesWithText("이름")[0].performTextInput("테스트학생")
        composeTestRule.onAllNodesWithText("이메일")[0].performTextInput(testEmail)
        composeTestRule.onAllNodesWithText("비밀번호")[0].performTextInput("Password123!")
        composeTestRule.onNodeWithText("비밀번호 확인").performTextInput("Password123!")

        // Click signup button to create account (not the navigation button)
        composeTestRule.onNode(
            hasText("계정 만들기") and hasClickAction() and !hasText("VoiceTutor"),
        ).performClick()

        // Wait for signup to complete and return to login screen
        Thread.sleep(3000) // Allow time for API call and navigation

        // Verify we're back on login screen with VoiceTutor title
        composeTestRule.onNodeWithText("VoiceTutor").assertIsDisplayed()

        composeTestRule.onAllNodesWithText("이메일")[0].performTextInput(testEmail)
        composeTestRule.onAllNodesWithText("비밀번호")[0].performTextInput("Password123!")
        composeTestRule.onAllNodesWithText("로그인")[0].performClick()

        // Wait for login to complete
        Thread.sleep(3000) // Allow time for API call and navigation

        // THEN - Verify we're on Student Dashboard by checking for student-specific UI elements
        composeTestRule.onNodeWithText("안녕하세요, 테스트학생", substring = true, useUnmergedTree = true).assertIsDisplayed()
    }

    /**
     * Test teacher signup and login flow.
     * GIVEN - User wants to sign up as a teacher
     * WHEN - User completes signup and then logs in
     * THEN - User is navigated to Teacher Dashboard
     */
    @Test
    fun teacherSignupAndLogin_NavigatesToTeacherDashboard() = runTest {
        // GIVEN - Launch the app at login screen
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                VoiceTutorNavigation(
                    navController = navController,
                    startDestination = VoiceTutorScreens.Login.route,
                )
            }
        } // WHEN - Navigate to signup screen
        // Click the "계정 만들기" button (not the title text)
        composeTestRule.onNode(
            hasText("계정 만들기") and hasClickAction(),
        ).performClick()

        // (Optional for explanation) Pause to visually confirm signup screen loading
        Thread.sleep(1000)

        // Verify we're on the signup screen
        composeTestRule.onNodeWithText("VoiceTutor와 함께 시작하세요").assertIsDisplayed()

        // Select Teacher role
        composeTestRule.onNodeWithText("선생님").performClick()

        // Fill in signup form - using unique email with timestamp
        val testEmail = "teacher_test_${System.currentTimeMillis()}@test.com"
        composeTestRule.onAllNodesWithText("이름")[0].performTextInput("테스트선생님")
        composeTestRule.onAllNodesWithText("이메일")[0].performTextInput(testEmail)
        composeTestRule.onAllNodesWithText("비밀번호")[0].performTextInput("Password123!")
        composeTestRule.onNodeWithText("비밀번호 확인").performTextInput("Password123!")

        // Click signup button to create account (not the navigation button)
        composeTestRule.onNode(
            hasText("계정 만들기") and hasClickAction() and !hasText("VoiceTutor"),
        ).performClick()

        // Wait for signup to complete and return to login screen
        Thread.sleep(3000) // Allow time for API call and navigation

        // Verify we're back on login screen
        composeTestRule.onNodeWithText("VoiceTutor").assertIsDisplayed()

        composeTestRule.onAllNodesWithText("이메일")[0].performTextInput(testEmail)
        composeTestRule.onAllNodesWithText("비밀번호")[0].performTextInput("Password123!")
        composeTestRule.onAllNodesWithText("로그인")[0].performClick()

        // Wait for login to complete
        Thread.sleep(3000) // Allow time for API call and navigation

        // THEN - Verify we're on Teacher Dashboard by checking for teacher-specific UI elements
        composeTestRule.onNodeWithText("환영합니다, 테스트 선생님", substring = true, useUnmergedTree = true).assertIsDisplayed()
    }

    /**
     * Test direct login flow for existing student.
     * GIVEN - A student user already exists in the system
     * WHEN - User logs in with student credentials
     * THEN - User is navigated to Student Dashboard
     */
    @Test
    fun existingStudentLogin_NavigatesToStudentDashboard() = runTest {
        // GIVEN - Launch the app at login screen
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                VoiceTutorNavigation(
                    navController = navController,
                    startDestination = VoiceTutorScreens.Login.route,
                )
            }
        }

        // Verify we're on login screen
        composeTestRule.onNodeWithText("VoiceTutor").assertIsDisplayed()

        // (Optional for explanation) Pause to visually confirm login screen
        Thread.sleep(1000)

        // WHEN - Fill in login form with existing student credentials
        composeTestRule.onAllNodesWithText("이메일")[0].performTextInput("student@voicetutor.com")
        composeTestRule.onAllNodesWithText("비밀번호")[0].performTextInput("student123")

        // Click login button
        composeTestRule.onAllNodesWithText("로그인")[0].performClick()

        // Wait for login to complete and navigation
        Thread.sleep(3000) // Allow time for API call and navigation

        // THEN - Verify we're on Student Dashboard
        // Check for student-specific dashboard elements
        composeTestRule.onNodeWithText("안녕하세요", substring = true, useUnmergedTree = true).assertExists()
    }

    /**
     * Test direct login flow for existing teacher.
     * GIVEN - A teacher user already exists in the system
     * WHEN - User logs in with teacher credentials
     * THEN - User is navigated to Teacher Dashboard
     */
    @Test
    fun existingTeacherLogin_NavigatesToTeacherDashboard() = runTest {
        // GIVEN - Launch the app at login screen
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                VoiceTutorNavigation(
                    navController = navController,
                    startDestination = VoiceTutorScreens.Login.route,
                )
            }
        }

        // Verify we're on login screen
        composeTestRule.onNodeWithText("VoiceTutor").assertIsDisplayed()

        // (Optional for explanation) Pause to visually confirm login screen
        Thread.sleep(1000)

        // WHEN - Fill in login form with existing teacher credentials
        composeTestRule.onAllNodesWithText("이메일")[0].performTextInput("teacher@voicetutor.com")
        composeTestRule.onAllNodesWithText("비밀번호")[0].performTextInput("teacher123")

        // Click login button
        composeTestRule.onAllNodesWithText("로그인")[0].performClick()

        // Wait for login to complete and navigation
        Thread.sleep(3000) // Allow time for API call and navigation

        // THEN - Verify we're on Teacher Dashboard
        // Check for teacher-specific dashboard elements
        composeTestRule.onNodeWithText("환영합니다", substring = true, useUnmergedTree = true).assertExists()
    }
}
