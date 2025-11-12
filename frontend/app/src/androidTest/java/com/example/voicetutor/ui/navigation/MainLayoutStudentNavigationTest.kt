package com.example.voicetutor.ui.navigation

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.di.NetworkModule
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import com.example.voicetutor.ui.navigation.VoiceTutorScreens
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
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
class MainLayoutStudentNavigationTest {

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
            shouldFailPersonalAssignments = false
            personalAssignmentsErrorMessage = "네트워크 오류"
            personalAssignmentsResponse = listOf(personalAssignmentData)
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

    private fun waitForRecentAssignment(assignmentViewModel: AssignmentViewModel, timeoutMillis: Long = 10_000) {
        composeRule.waitUntil(timeoutMillis) {
            var hasRecent = false
            composeRule.runOnIdle {
                hasRecent = assignmentViewModel.recentAssignment.value != null
            }
            hasRecent
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

    private fun assignmentViewModel(): AssignmentViewModel {
        var viewModel: AssignmentViewModel? = null
        composeRule.runOnIdle {
            val entry = navController.getBackStackEntry(navController.graph.id)
            viewModel = ViewModelProvider(entry)[AssignmentViewModel::class.java]
        }
        return checkNotNull(viewModel)
    }

    @Test
    fun studentMainLayout_fullNavigationFlow() {
        setContent()

        val authViewModel = authViewModel()
        val assignmentViewModel = assignmentViewModel()

        composeRule.runOnIdle {
            authViewModel.login("student@voicetutor.com", "student123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null
        }

        waitForRoutePrefix(VoiceTutorScreens.StudentDashboard.route)

        composeRule.onNodeWithText("VoiceTutor", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("테스트학생", useUnmergedTree = true).assertIsDisplayed()

        waitForRecentAssignment(assignmentViewModel)

        composeRule.onNodeWithText("리포트", useUnmergedTree = true).performClick()
        waitForRoutePrefix(VoiceTutorScreens.Progress.route)
        composeRule.onNodeWithText("VoiceTutor", useUnmergedTree = true).assertIsDisplayed()

        composeRule.onNodeWithText("홈", useUnmergedTree = true).performClick()
        waitForRoutePrefix(VoiceTutorScreens.StudentDashboard.route)

        val studentNodes = composeRule.onAllNodesWithText("테스트학생", useUnmergedTree = true)
            .fetchSemanticsNodes()
        if (studentNodes.isNotEmpty()) {
            composeRule.onNodeWithText("테스트학생", useUnmergedTree = true)
                .performClick()
        }
        waitForRoutePrefix(VoiceTutorScreens.Settings.route.substringBefore("{"))
        composeRule.onNodeWithText("계정", useUnmergedTree = true).assertIsDisplayed()

        composeRule.onNodeWithContentDescription("뒤로가기").performClick()
        waitForRoutePrefix(VoiceTutorScreens.StudentDashboard.route)

        composeRule.onNodeWithText("이어하기", useUnmergedTree = true).performClick()
        waitForRoutePrefix(VoiceTutorScreens.AssignmentDetail.route.substringBefore("{"))
        composeRule.onNodeWithText("과제 상세", useUnmergedTree = true).assertIsDisplayed()

        composeRule.onNodeWithContentDescription("뒤로가기").performClick()
        waitForRoutePrefix(VoiceTutorScreens.StudentDashboard.route)

        composeRule.onNodeWithContentDescription("로그아웃").performClick()
        composeRule.onNodeWithText("로그아웃하시겠습니까?", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithText("취소", useUnmergedTree = true).performClick()

        composeRule.onNodeWithContentDescription("로그아웃").performClick()
        composeRule.onNodeWithText("로그아웃", useUnmergedTree = true).performClick()

        waitForRoutePrefix(VoiceTutorScreens.Login.route)
        composeRule.onNodeWithText("로그인", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun studentMainLayout_resumeButtonWithoutRecentNavigatesToEmptyState() {
        fakeApi.personalAssignmentsResponse = emptyList()

        setContent()

        val authViewModel = authViewModel()
        val assignmentViewModel = assignmentViewModel()

        composeRule.runOnIdle {
            authViewModel.login("student@voicetutor.com", "student123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null
        }

        waitForRoutePrefix(VoiceTutorScreens.StudentDashboard.route)

        composeRule.waitUntil(timeoutMillis = 5_000) {
            var hasRecent = true
            composeRule.runOnIdle {
                hasRecent = assignmentViewModel.recentAssignment.value != null
            }
            !hasRecent
        }

        composeRule.onNodeWithText("이어하기", useUnmergedTree = true).performClick()

        waitForRoutePrefix(VoiceTutorScreens.NoRecentAssignment.route.substringBefore("{"))

        composeRule.onNodeWithText("이어할 과제가 없습니다", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithContentDescription("뒤로가기").performClick()

        waitForRoutePrefix(VoiceTutorScreens.StudentDashboard.route)
        composeRule.onNodeWithText("VoiceTutor", useUnmergedTree = true).assertIsDisplayed()
    }
}


