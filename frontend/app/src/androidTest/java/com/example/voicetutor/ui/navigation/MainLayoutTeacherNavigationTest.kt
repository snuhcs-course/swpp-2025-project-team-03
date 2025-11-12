package com.example.voicetutor.ui.navigation

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.navigation.VoiceTutorScreens
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

    private fun waitForRoutePrefix(prefix: String, timeoutMillis: Long = 10_000) {
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
    fun teacherMainLayout_bottomNavigation_andLogoutFlow() {
        setContent()

        val authViewModel = authViewModel()

        composeRule.runOnIdle {
            authViewModel.login("teacher@voicetutor.com", "teacher123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null
        }

        waitForRoutePrefix(VoiceTutorScreens.TeacherDashboard.route)

        composeRule.onNodeWithText("VoiceTutor", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("테스트선생님", useUnmergedTree = true).assertIsDisplayed()

        composeRule.onNodeWithText("수업", useUnmergedTree = true).performClick()
        waitForRoutePrefix(VoiceTutorScreens.TeacherClasses.route)
        composeRule.onNodeWithText("수업 관리", useUnmergedTree = true).assertIsDisplayed()

        composeRule.onNodeWithText("학생", useUnmergedTree = true).performClick()
        waitForRoutePrefix(VoiceTutorScreens.AllStudents.route)
        composeRule.onNodeWithText("전체 학생 관리", useUnmergedTree = true).assertIsDisplayed()

        composeRule.onNodeWithText("홈", useUnmergedTree = true).performClick()
        waitForRoutePrefix(VoiceTutorScreens.TeacherDashboard.route)

        val teacherNodes = composeRule.onAllNodesWithText("테스트선생님", useUnmergedTree = true)
            .fetchSemanticsNodes()
        if (teacherNodes.isNotEmpty()) {
            composeRule.onNodeWithText("테스트선생님", useUnmergedTree = true).performClick()
        }
        waitForRoutePrefix(VoiceTutorScreens.Settings.route.substringBefore("{"))
        composeRule.onNodeWithText("계정", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("뒤로가기").performClick()

        waitForRoutePrefix(VoiceTutorScreens.TeacherDashboard.route)

        composeRule.onNodeWithContentDescription("로그아웃").performClick()
        composeRule.onNodeWithText("로그아웃하시겠습니까?", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("로그아웃", useUnmergedTree = true).performClick()

        waitForRoutePrefix(VoiceTutorScreens.Login.route)
        composeRule.onNodeWithText("로그인", useUnmergedTree = true).assertIsDisplayed()
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
        composeRule.onNodeWithText("모든 과제", substring = true, useUnmergedTree = true).assertIsDisplayed()

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.TeacherAssignmentDetail.createRoute(1))
        }
        waitForRoutePrefix(VoiceTutorScreens.TeacherAssignmentDetail.route.substringBefore("{"))
        composeRule.onNodeWithText("과제 내용", substring = true, useUnmergedTree = true).assertIsDisplayed()

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.TeacherAssignmentResults.createRoute(1))
        }
        waitForRoutePrefix(VoiceTutorScreens.TeacherAssignmentResults.route.substringBefore("{"))
        composeRule.onNodeWithText("학생별 결과", substring = true, useUnmergedTree = true).assertIsDisplayed()

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.TeacherStudentReport.createRoute(1, 1, "홍길동"))
        }
        waitForRoutePrefix(VoiceTutorScreens.TeacherStudentReport.route.substringBefore("{"))
        composeRule.onNodeWithText("홍길동", substring = true, useUnmergedTree = true).assertIsDisplayed()

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.CreateAssignment.createRoute(null))
        }
        waitForRoutePrefix(VoiceTutorScreens.CreateAssignment.route.substringBefore("{"))
        composeRule.onNodeWithText("기본 정보", substring = true, useUnmergedTree = true).assertIsDisplayed()

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.EditAssignment.createRoute(1))
        }
        waitForRoutePrefix(VoiceTutorScreens.EditAssignment.route.substringBefore("{"))
        composeRule.onNodeWithText("과제 편집", substring = true, useUnmergedTree = true).assertIsDisplayed()
    }
}


