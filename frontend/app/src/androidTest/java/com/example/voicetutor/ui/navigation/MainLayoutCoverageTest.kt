package com.example.voicetutor.ui.navigation

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
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
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
@UninstallModules(NetworkModule::class)
class MainLayoutCoverageTest {

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
            shouldFailDashboardStats = false
            shouldFailGetAllAssignments = false
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

    private fun assignmentViewModel(): AssignmentViewModel {
        var viewModel: AssignmentViewModel? = null
        composeRule.runOnIdle {
            val entry = navController.getBackStackEntry(navController.graph.id)
            viewModel = ViewModelProvider(entry)[AssignmentViewModel::class.java]
        }
        return checkNotNull(viewModel)
    }

    @Test
    fun testGetPageTitle_Assignment() {
        val title = getPageTitle(VoiceTutorScreens.Assignment.route, UserRole.STUDENT)
        assert(title == "과제")
    }

    @Test
    fun testGetPageTitle_AssignmentDetail() {
        val title = getPageTitle(VoiceTutorScreens.AssignmentDetail.createRoute("1", "Test"), UserRole.STUDENT)
        assert(title == "과제 상세")
    }

    @Test
    fun testGetPageTitle_Progress() {
        val title = getPageTitle(VoiceTutorScreens.Progress.route, UserRole.STUDENT)
        assert(title == "학습 리포트")
    }

    @Test
    fun testGetPageTitle_TeacherClasses() {
        val title = getPageTitle(VoiceTutorScreens.TeacherClasses.route, UserRole.TEACHER)
        assert(title == "수업 관리")
    }

    @Test
    fun testGetPageTitle_CreateAssignment() {
        val title = getPageTitle(VoiceTutorScreens.CreateAssignment.createRoute(1), UserRole.TEACHER)
        assert(title == "과제 생성")
    }

    @Test
    fun testGetPageTitle_EditAssignment() {
        val title = getPageTitle(VoiceTutorScreens.EditAssignment.createRoute(1), UserRole.TEACHER)
        assert(title == "과제 편집")
    }

    @Test
    fun testGetPageTitle_Settings() {
        val title = getPageTitle(VoiceTutorScreens.Settings.route, UserRole.STUDENT)
        assert(title == "계정")
    }

    @Test
    fun testGetPageTitle_DefaultTeacher() {
        val title = getPageTitle("unknown_route", UserRole.TEACHER)
        assert(title == "선생님 페이지")
    }

    @Test
    fun testGetPageTitle_DefaultStudent() {
        val title = getPageTitle("unknown_route", UserRole.STUDENT)
        assert(title == "학생 페이지")
    }

    @Test
    fun testTeacherBaseRouteUpdate() {
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
            navController.navigate(VoiceTutorScreens.TeacherClasses.route)
        }

        waitForRoutePrefix(VoiceTutorScreens.TeacherClasses.route)
        composeRule.waitForIdle()
    }

    @Test
    fun testIsDashboard_StudentDashboard() {
        setContent()

        val authViewModel = authViewModel()

        composeRule.runOnIdle {
            authViewModel.login("student@voicetutor.com", "student123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null
        }

        waitForRoutePrefix(VoiceTutorScreens.StudentDashboard.route)

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("VoiceTutor", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText("VoiceTutor", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun testIsDashboard_AssignmentDetailIsDashboard() {
        setContent()

        val authViewModel = authViewModel()

        composeRule.runOnIdle {
            authViewModel.login("student@voicetutor.com", "student123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null
        }

        waitForRoutePrefix(VoiceTutorScreens.StudentDashboard.route)

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.AssignmentDetail.createRoute("1", "Test"))
        }

        waitForRoutePrefix(VoiceTutorScreens.AssignmentDetail.route.substringBefore("{"))

        composeRule.onAllNodesWithText("VoiceTutor", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun testIsDashboard_NonDashboardShowsBackButton() {
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
            navController.navigate(VoiceTutorScreens.CreateAssignment.createRoute(null))
        }

        waitForRoutePrefix(VoiceTutorScreens.CreateAssignment.route.substringBefore("{"))

        composeRule.onNodeWithContentDescription("뒤로가기").assertIsDisplayed()
    }

    @Test
    fun testHeaderShowsLogoOnDashboard() {
        setContent()

        val authViewModel = authViewModel()

        composeRule.runOnIdle {
            authViewModel.login("teacher@voicetutor.com", "teacher123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null
        }

        waitForRoutePrefix(VoiceTutorScreens.TeacherDashboard.route)

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("VoiceTutor", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText("VoiceTutor", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun testHeaderShowsBackButtonOnNonDashboard() {
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
            navController.navigate(VoiceTutorScreens.CreateAssignment.createRoute(null))
        }

        waitForRoutePrefix(VoiceTutorScreens.CreateAssignment.route.substringBefore("{"))

        composeRule.onNodeWithContentDescription("뒤로가기").assertIsDisplayed()
        composeRule.onAllNodesWithText("과제 생성", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun testUserInfoDisplay() {
        setContent()

        val authViewModel = authViewModel()

        composeRule.runOnIdle {
            authViewModel.login("teacher@voicetutor.com", "teacher123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null
        }

        waitForRoutePrefix(VoiceTutorScreens.TeacherDashboard.route)

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("선생님", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText("선생님", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun testProfileButtonClick() {
        setContent()

        val authViewModel = authViewModel()

        composeRule.runOnIdle {
            authViewModel.login("student@voicetutor.com", "student123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null
        }

        waitForRoutePrefix(VoiceTutorScreens.StudentDashboard.route)

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.Settings.createRoute())
        }

        waitForRoutePrefix(VoiceTutorScreens.Settings.route.substringBefore("{"))
        composeRule.waitForIdle()
    }

    @Test
    fun testLogoutDialog() {
        setContent()

        val authViewModel = authViewModel()

        composeRule.runOnIdle {
            authViewModel.login("student@voicetutor.com", "student123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null
        }

        waitForRoutePrefix(VoiceTutorScreens.StudentDashboard.route)

        composeRule.onNodeWithContentDescription("로그아웃").performClick()
        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("로그아웃", useUnmergedTree = true)
                .fetchSemanticsNodes().size >= 2
        }
        composeRule.onAllNodesWithText("로그아웃하시겠습니까?", useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()

        composeRule.onAllNodesWithText("취소", useUnmergedTree = true)
            .onFirst()
            .performClick()
        composeRule.waitForIdle()

        waitForRoutePrefix(VoiceTutorScreens.StudentDashboard.route)
    }

    @Test
    fun testLogoutDialogConfirm() {
        setContent()

        val authViewModel = authViewModel()

        composeRule.runOnIdle {
            authViewModel.login("student@voicetutor.com", "student123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null
        }

        waitForRoutePrefix(VoiceTutorScreens.StudentDashboard.route)

        composeRule.onNodeWithContentDescription("로그아웃").performClick()
        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("로그아웃", useUnmergedTree = true)
                .fetchSemanticsNodes().size >= 2
        }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(
                hasText("로그아웃") and hasClickAction(),
            ).fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNode(
            hasText("로그아웃") and hasClickAction(),
        ).performClick()

        composeRule.waitForIdle()

        waitForRoutePrefix(VoiceTutorScreens.Login.route)
    }

    @Test
    fun testFloatingProgressIndicator() {
        setContent()

        val authViewModel = authViewModel()
        val assignmentViewModel = assignmentViewModel()

        composeRule.runOnIdle {
            authViewModel.login("teacher@voicetutor.com", "teacher123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null
        }

        waitForRoutePrefix(VoiceTutorScreens.TeacherDashboard.route)

        composeRule.runOnIdle {
            val isGeneratingField = AssignmentViewModel::class.java.getDeclaredField("_isGeneratingQuestions")
            isGeneratingField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val isGeneratingFlow = isGeneratingField.get(assignmentViewModel) as MutableStateFlow<Boolean>
            isGeneratingFlow.value = true

            val titleField = AssignmentViewModel::class.java.getDeclaredField("_generatingAssignmentTitle")
            titleField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val titleFlow = titleField.get(assignmentViewModel) as MutableStateFlow<String?>
            titleFlow.value = "Test Assignment"
        }

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("질문 생성 중...", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText("질문 생성 중...", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun testAssignmentCreatedToast() {
        setContent()

        val authViewModel = authViewModel()
        val assignmentViewModel = assignmentViewModel()

        composeRule.runOnIdle {
            authViewModel.login("teacher@voicetutor.com", "teacher123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null
        }

        waitForRoutePrefix(VoiceTutorScreens.TeacherDashboard.route)

        composeRule.runOnIdle {
            val successField = AssignmentViewModel::class.java.getDeclaredField("_questionGenerationSuccess")
            successField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val successFlow = successField.get(assignmentViewModel) as MutableStateFlow<Boolean>
            successFlow.value = true

            val isGeneratingField = AssignmentViewModel::class.java.getDeclaredField("_isGeneratingQuestions")
            isGeneratingField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val isGeneratingFlow = isGeneratingField.get(assignmentViewModel) as MutableStateFlow<Boolean>
            isGeneratingFlow.value = false
        }

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("과제가 성공적으로 생성되었습니다!", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText("과제가 성공적으로 생성되었습니다!", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun testCancelledToast() {
        setContent()

        val authViewModel = authViewModel()
        val assignmentViewModel = assignmentViewModel()

        composeRule.runOnIdle {
            authViewModel.login("teacher@voicetutor.com", "teacher123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null
        }

        waitForRoutePrefix(VoiceTutorScreens.TeacherDashboard.route)

        composeRule.runOnIdle {
            val cancelledField = AssignmentViewModel::class.java.getDeclaredField("_questionGenerationCancelled")
            cancelledField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val cancelledFlow = cancelledField.get(assignmentViewModel) as MutableStateFlow<Boolean>
            cancelledFlow.value = true
        }

        composeRule.waitForIdle()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("과제 생성이 취소되었습니다!", substring = true, useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onAllNodesWithText("과제 생성이 취소되었습니다!", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun testBottomNavigationWithRecentAssignment() {
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

        composeRule.runOnIdle {
            val recentAssignmentField = AssignmentViewModel::class.java.getDeclaredField("_recentAssignment")
            recentAssignmentField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val recentAssignmentFlow = recentAssignmentField.get(assignmentViewModel) as MutableStateFlow<RecentAssignment?>
            recentAssignmentFlow.value = RecentAssignment(
                id = "1",
                title = "Test Assignment",
                assignmentId = 1,
            )
        }

        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("이어하기", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        waitForRoutePrefix(VoiceTutorScreens.AssignmentDetail.route.substringBefore("{"))
    }

    @Test
    fun testBottomNavigationWithoutRecentAssignment() {
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

        composeRule.runOnIdle {
            val recentAssignmentField = AssignmentViewModel::class.java.getDeclaredField("_recentAssignment")
            recentAssignmentField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val recentAssignmentFlow = recentAssignmentField.get(assignmentViewModel) as MutableStateFlow<RecentAssignment?>
            recentAssignmentFlow.value = null
        }

        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("이어하기", useUnmergedTree = true)
            .onFirst()
            .performClick()

        composeRule.waitForIdle()

        waitForRoutePrefix(VoiceTutorScreens.NoRecentAssignment.route.substringBefore("{"))
    }

    @Test
    fun testLoadRecentAssignmentForStudents() {
        setContent()

        val authViewModel = authViewModel()

        composeRule.runOnIdle {
            authViewModel.login("student@voicetutor.com", "student123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null
        }

        waitForRoutePrefix(VoiceTutorScreens.StudentDashboard.route)

        composeRule.waitForIdle()
    }
}
