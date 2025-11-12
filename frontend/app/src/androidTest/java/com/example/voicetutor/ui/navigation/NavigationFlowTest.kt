package com.example.voicetutor.ui.navigation

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
class NavigationFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    @Inject
    lateinit var apiService: ApiService

    private lateinit var navController: NavHostController

    private val fakeApi: FakeApiService
        get() = apiService as FakeApiService

    private fun waitForText(
        text: String,
        substring: Boolean = false
    ) {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule
                .onAllNodesWithText(text, substring = substring, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    private fun waitForRoute(
        routePrefix: String,
        timeoutMillis: Long = 15_000
    ) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            var matches = false
            composeRule.runOnIdle {
                matches = navController.currentBackStackEntry?.destination?.route?.startsWith(routePrefix) == true
            }
            matches
        }
    }

    @Before
    fun setUp() {
        hiltRule.inject()
        resetFakeApi()
    }

    private fun resetFakeApi() {
        fakeApi.apply {
            shouldFailPersonalAssignments = false
            personalAssignmentsDelayMillis = 0
            shouldFailDashboardStats = false
        }
    }

    private fun setContent(startDestination: String = VoiceTutorScreens.Login.route) {
        composeRule.setContent {
            VoiceTutorTheme {
                val controller = rememberNavController()
                SideEffect { navController = controller }
                VoiceTutorNavigation(
                    navController = controller,
                    startDestination = startDestination
                )
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun teacherLoginNavigatesToTeacherDashboard() {
        setContent()

        var authViewModel: AuthViewModel? = null
        composeRule.runOnIdle {
            val entry = navController.getBackStackEntry(navController.graph.id)
            authViewModel = ViewModelProvider(entry)[AuthViewModel::class.java]
        }
        val viewModel = checkNotNull(authViewModel)
        composeRule.runOnIdle {
            viewModel.login(email = "teacher@voicetutor.com", password = "teacher123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            viewModel.currentUser.value?.role == UserRole.TEACHER
        }

        waitForRoute(VoiceTutorScreens.TeacherDashboard.route)
        waitForText(text = "빠른 실행")
    }

    @Test
    fun studentLoginNavigatesToStudentDashboard() {
        setContent()

        var authViewModel: AuthViewModel? = null
        composeRule.runOnIdle {
            val entry = navController.getBackStackEntry(navController.graph.id)
            authViewModel = ViewModelProvider(entry)[AuthViewModel::class.java]
        }
        val viewModel = checkNotNull(authViewModel)
        composeRule.runOnIdle {
            viewModel.login(email = "student@voicetutor.com", password = "student123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            viewModel.currentUser.value?.role == UserRole.STUDENT
        }

        waitForRoute(VoiceTutorScreens.StudentDashboard.route)
        waitForText(text = "나에게 할당된 과제", substring = true)
    }

    @Test
    fun manualNavigationToAppInfoRendersScreen() {
        setContent(startDestination = VoiceTutorScreens.Login.route)

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.AppInfo.route)
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodesWithText("앱 정보", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule
            .onNode(hasText("앱 정보", substring = true), useUnmergedTree = true)
            .assertIsDisplayed()
    }
}

