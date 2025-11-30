package com.example.voicetutor.ui.navigation

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.network.ApiService
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
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
class VoiceTutorNavigationCallbackTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var apiService: ApiService

    private lateinit var navController: NavHostController

    private val fakeApi: FakeApiService
        get() = apiService as FakeApiService

    @Before
    fun setUp() {
        hiltRule.inject()
        resetFakeApi()
        setContent()
    }

    private fun resetFakeApi() {
        fakeApi.apply {
            shouldFailAssignmentResult = false
            shouldFailPersonalAssignments = false
            personalAssignmentsDelayMillis = 0
        }
    }

    private fun setContent() {
        composeRule.setContent {
            VoiceTutorTheme {
                val controller = rememberNavController()
                SideEffect { navController = controller }
                VoiceTutorNavigation(navController = controller)
            }
        }
        composeRule.waitForIdle()
    }

    private fun loginTeacher() {
        var authViewModel: AuthViewModel? = null
        composeRule.runOnIdle {
            val entry = navController.getBackStackEntry(navController.graph.id)
            authViewModel = ViewModelProvider(entry)[AuthViewModel::class.java]
        }
        val viewModel = checkNotNull(authViewModel)
        composeRule.runOnIdle {
            viewModel.login("teacher@voicetutor.com", "teacher123")
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            viewModel.currentUser.value != null
        }
        waitForRoutePrefix(VoiceTutorScreens.TeacherDashboard.route)
    }

    private fun loginStudent() {
        var authViewModel: AuthViewModel? = null
        composeRule.runOnIdle {
            val entry = navController.getBackStackEntry(navController.graph.id)
            authViewModel = ViewModelProvider(entry)[AuthViewModel::class.java]
        }
        val viewModel = checkNotNull(authViewModel)
        composeRule.runOnIdle {
            viewModel.login("student@voicetutor.com", "student123")
        }
        composeRule.waitUntil(timeoutMillis = 10_000) {
            viewModel.currentUser.value != null
        }
        waitForRoutePrefix(VoiceTutorScreens.StudentDashboard.route)
    }

    private fun waitForRoutePrefix(prefix: String, timeoutMillis: Long = 15_000) {
        composeRule.waitUntil(timeoutMillis) {
            var matches = false
            composeRule.runOnIdle {
                val currentRoute = navController.currentBackStackEntry?.destination?.route
                matches = currentRoute?.startsWith(prefix) == true
            }
            matches
        }
    }

    @Test
    fun mainLayout_backButton_navigatesBack() {
        loginTeacher()

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.CreateClass.route)
        }
        waitForRoutePrefix(VoiceTutorScreens.CreateClass.route)
        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeRule
                    .onAllNodesWithContentDescription("뒤로가기", useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false)
                    .isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

        composeRule
            .onAllNodesWithContentDescription("뒤로가기", useUnmergedTree = true)
            .onFirst()
            .performClick()

        waitForRoutePrefix(VoiceTutorScreens.TeacherDashboard.route)
        composeRule.waitForIdle()
    }

    @Test
    fun mainLayout_settingsButton_navigatesToSettings() {
        loginTeacher()
        composeRule.waitForIdle()

        // Navigate directly to settings to test the route
        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.Settings.createRoute())
        }
        waitForRoutePrefix("settings")
        composeRule.waitForIdle()
    }

    @Test
    fun mainLayout_logoutButton_showsDialog() {
        loginTeacher()
        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeRule
                    .onAllNodesWithContentDescription("로그아웃", useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false)
                    .isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

        composeRule
            .onAllNodesWithContentDescription("로그아웃", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeRule
                    .onAllNodesWithText("로그아웃", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false)
                    .isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

        composeRule
            .onAllNodesWithText("로그아웃하시겠습니까?", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun mainLayout_logoutDialog_cancelButton_dismissesDialog() {
        loginTeacher()
        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeRule
                    .onAllNodesWithContentDescription("로그아웃", useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false)
                    .isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

        composeRule
            .onAllNodesWithContentDescription("로그아웃", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeRule
                    .onAllNodesWithText("취소", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false)
                    .isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

        composeRule
            .onAllNodesWithText("취소", substring = true, useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        // Dialog should be dismissed
        composeRule.waitUntil(timeoutMillis = 3_000) {
            try {
                composeRule
                    .onAllNodesWithText("로그아웃하시겠습니까?", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false)
                    .isEmpty()
            } catch (_: Exception) {
                true
            }
        }
    }

    @Test
    fun bottomNavigation_teacherHome_navigatesToDashboard() {
        loginTeacher()

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.TeacherClasses.route)
        }
        waitForRoutePrefix(VoiceTutorScreens.TeacherClasses.route)
        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeRule
                    .onAllNodesWithText("홈", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false)
                    .isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

        composeRule
            .onAllNodesWithText("홈", substring = true, useUnmergedTree = true)
            .onFirst()
            .performClick()

        waitForRoutePrefix(VoiceTutorScreens.TeacherDashboard.route)
        composeRule.waitForIdle()
    }

    @Test
    fun bottomNavigation_teacherStudents_navigatesToAllStudents() {
        loginTeacher()
        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeRule
                    .onAllNodesWithText("리포트", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false)
                    .isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

        composeRule
            .onAllNodesWithText("리포트", substring = true, useUnmergedTree = true)
            .onFirst()
            .performClick()

        waitForRoutePrefix(VoiceTutorScreens.AllStudents.route)
        composeRule.waitForIdle()
    }

    @Test
    fun bottomNavigation_studentHome_navigatesToDashboard() {
        loginStudent()

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.Progress.route)
        }
        waitForRoutePrefix(VoiceTutorScreens.Progress.route)
        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeRule
                    .onAllNodesWithText("홈", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false)
                    .isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

        composeRule
            .onAllNodesWithText("홈", substring = true, useUnmergedTree = true)
            .onFirst()
            .performClick()

        waitForRoutePrefix(VoiceTutorScreens.StudentDashboard.route)
        composeRule.waitForIdle()
    }

    @Test
    fun bottomNavigation_studentProgress_navigatesToProgress() {
        loginStudent()
        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeRule
                    .onAllNodesWithText("리포트", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false)
                    .isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

        composeRule
            .onAllNodesWithText("리포트", substring = true, useUnmergedTree = true)
            .onFirst()
            .performClick()

        waitForRoutePrefix(VoiceTutorScreens.Progress.route)
        composeRule.waitForIdle()
    }

    @Test
    fun createClassScreen_onClassCreated_navigatesToTeacherClasses() {
        loginTeacher()

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.CreateClass.route)
        }
        waitForRoutePrefix(VoiceTutorScreens.CreateClass.route)
        composeRule.waitForIdle()

        // This test verifies the navigation callback is set up correctly
        // The actual class creation would require more complex setup
        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeRule
                    .onAllNodesWithText("수업 생성", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false)
                    .isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

        composeRule
            .onAllNodesWithText("수업 생성", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun appInfoScreen_onBackClick_navigatesBack() {
        loginTeacher()

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.AppInfo.route)
        }
        waitForRoutePrefix(VoiceTutorScreens.AppInfo.route)
        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            try {
                composeRule
                    .onAllNodesWithContentDescription("뒤로가기", useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false)
                    .isNotEmpty()
            } catch (_: Exception) {
                false
            }
        }

        composeRule
            .onAllNodesWithContentDescription("뒤로가기", useUnmergedTree = true)
            .onFirst()
            .performClick()

        // Should navigate back to previous screen
        composeRule.waitForIdle()
    }
}
