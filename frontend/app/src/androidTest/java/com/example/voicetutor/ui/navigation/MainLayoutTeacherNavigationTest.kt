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
class MainLayoutTeacherNavigationTest {

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
    }

    private fun resetFakeApi() {
        fakeApi.apply {
            shouldFailGetAllAssignments = false
            shouldFailDashboardStats = false
            shouldFailPersonalAssignments = false
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

    private fun authViewModel(): AuthViewModel {
        var viewModel: AuthViewModel? = null
        composeRule.runOnIdle {
            val entry = navController.getBackStackEntry(navController.graph.id)
            viewModel = ViewModelProvider(entry)[AuthViewModel::class.java]
        }
        return checkNotNull(viewModel)
    }

    @Test
    fun teacherMainLayout_accessesAssignmentManagementRoutes() {
        setContent()

        val authViewModel = authViewModel()

        composeRule.runOnIdle {
            authViewModel.login("teacher@voicetutor.com", "teacher123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null
        }

        waitForRoutePrefix(VoiceTutorScreens.TeacherDashboard.route)

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.AllAssignments.route)
        }
        waitForRoutePrefix(VoiceTutorScreens.AllAssignments.route)
        composeRule.onAllNodesWithText("모든 과제", substring = true, useUnmergedTree = true).onFirst().assertIsDisplayed()

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.TeacherAssignmentDetail.createRoute(1))
        }
        waitForRoutePrefix(VoiceTutorScreens.TeacherAssignmentDetail.route.substringBefore("{"))
        composeRule.onAllNodesWithText("과제 내용", substring = true, useUnmergedTree = true).onFirst().assertIsDisplayed()

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.TeacherAssignmentResults.createRoute(1))
        }
        waitForRoutePrefix(VoiceTutorScreens.TeacherAssignmentResults.route.substringBefore("{"))
        composeRule.onAllNodesWithText("학생별 결과", substring = true, useUnmergedTree = true).onFirst().assertIsDisplayed()

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.TeacherStudentReport.createRoute(1, 1, "홍길동"))
        }
        waitForRoutePrefix(VoiceTutorScreens.TeacherStudentReport.route.substringBefore("{"))
        composeRule.onAllNodesWithText("홍길동", substring = true, useUnmergedTree = true).onFirst().assertIsDisplayed()

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.TeacherStudentAssignmentDetail.createRoute("1", 1, "듣기 평가 과제"))
        }
        waitForRoutePrefix(VoiceTutorScreens.TeacherStudentAssignmentDetail.route.substringBefore("{"))
        composeRule.onAllNodesWithText("홍길동", substring = true, useUnmergedTree = true).onFirst().assertIsDisplayed()

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.CreateAssignment.createRoute(null))
        }
        waitForRoutePrefix(VoiceTutorScreens.CreateAssignment.route.substringBefore("{"))
        composeRule.onAllNodesWithText("기본 정보", substring = true, useUnmergedTree = true).onFirst().assertIsDisplayed()

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.EditAssignment.createRoute(1))
        }
        waitForRoutePrefix(VoiceTutorScreens.EditAssignment.route.substringBefore("{"))
        composeRule.onAllNodesWithText("과제 편집", substring = true, useUnmergedTree = true).onFirst().assertIsDisplayed()
    }

    @Test
    fun teacherMainLayout_navigateToCreateClassScreen() {
        setContent()

        val authViewModel = authViewModel()

        composeRule.runOnIdle {
            authViewModel.login("teacher@voicetutor.com", "teacher123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null
        }

        waitForRoutePrefix(VoiceTutorScreens.TeacherDashboard.route)

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.CreateClass.route)
        }
        waitForRoutePrefix(VoiceTutorScreens.CreateClass.route)
        composeRule.onAllNodesWithText("수업 생성", substring = false, useUnmergedTree = true)[0].assertIsDisplayed()
    }
}
