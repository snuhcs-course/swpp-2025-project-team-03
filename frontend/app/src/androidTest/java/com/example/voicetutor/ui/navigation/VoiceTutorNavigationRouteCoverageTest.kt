package com.example.voicetutor.ui.navigation

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
class VoiceTutorNavigationRouteCoverageTest {

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
        loginTeacher()
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

    private fun assignmentViewModel(): AssignmentViewModel {
        var viewModel: AssignmentViewModel? = null
        composeRule.runOnIdle {
            val entry = navController.getBackStackEntry(navController.graph.id)
            viewModel = ViewModelProvider(entry)[AssignmentViewModel::class.java]
        }
        return checkNotNull(viewModel)
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

    private fun navigateAndAssert(route: String, expectedText: String, substring: Boolean = true, timeoutMillis: Long = 30_000) {
        val prefix = route.substringBefore("{")
        val targetRoute = prefix.ifEmpty { route }
        
        // Check if we're already on this route
        var alreadyOnRoute = false
        composeRule.runOnIdle {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            alreadyOnRoute = currentRoute?.startsWith(targetRoute) == true
        }
        
        if (!alreadyOnRoute) {
            composeRule.runOnIdle {
                navController.navigate(route)
            }
            waitForRoutePrefix(targetRoute, timeoutMillis = timeoutMillis)
        }

        // Wait for screen to load and display expected text
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            try {
                composeRule
                    .onAllNodesWithText(
                        expectedText,
                        substring = substring,
                        useUnmergedTree = true,
                    )
                    .fetchSemanticsNodes(atLeastOneRootRequired = false)
                    .isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }

        // Verify the text is displayed
        composeRule
            .onAllNodesWithText(expectedText, substring = substring, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()

        // Wait a bit for screen to fully render
        composeRule.waitForIdle()
    }

    @org.junit.Test
    fun testTeacherDashboardRoute() {
        // Already on TeacherDashboard from setUp, just verify we're on the right route
        waitForRoutePrefix(VoiceTutorScreens.TeacherDashboard.route)
        composeRule.waitForIdle()
        // Verify some text that should be on the dashboard - wait for it to appear
        composeRule.waitUntil(timeoutMillis = 15_000) {
            try {
                composeRule
                    .onAllNodesWithText("환영", substring = true, useUnmergedTree = true)
                    .fetchSemanticsNodes(atLeastOneRootRequired = false)
                    .isNotEmpty()
            } catch (e: Exception) {
                false
            }
        }
        composeRule
            .onAllNodesWithText("환영", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @org.junit.Test
    fun testTeacherClassesRoute() {
        navigateAndAssert(
            route = VoiceTutorScreens.TeacherClasses.route,
            expectedText = "수업",
        )
    }

    @org.junit.Test
    fun testAllAssignmentsRoute() {
        navigateAndAssert(
            route = VoiceTutorScreens.AllAssignments.route,
            expectedText = "과제",
        )
    }

    @org.junit.Test
    fun testAllStudentsRoute() {
        navigateAndAssert(
            route = VoiceTutorScreens.AllStudents.route,
            expectedText = "학생",
        )
    }

    @org.junit.Test
    fun testAppInfoRoute() {
        navigateAndAssert(
            route = VoiceTutorScreens.AppInfo.route,
            expectedText = "정보",
        )
    }
}
