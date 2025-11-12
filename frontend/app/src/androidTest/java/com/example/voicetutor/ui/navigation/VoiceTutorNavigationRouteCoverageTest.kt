package com.example.voicetutor.ui.navigation

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
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
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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

    private fun navigateAndAssert(route: String, expectedText: String, substring: Boolean = true) {
        val prefix = route.substringBefore("{")
        composeRule.runOnIdle {
            navController.navigate(route)
        }
        waitForRoutePrefix(prefix.ifEmpty { route })
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule
                .onAllNodesWithText(
                    expectedText,
                    substring = substring,
                    useUnmergedTree = true
                )
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeRule
            .onNodeWithText(expectedText, substring = substring, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun navigateThroughKeyTeacherRoutes() {
        loginTeacher()

        navigateAndAssert(
            VoiceTutorScreens.TeacherClasses.route,
            expectedText = "수업 관리"
        )

        navigateAndAssert(
            VoiceTutorScreens.TeacherClassDetail.createRoute("수학 A반", 1),
            expectedText = "수학 A반"
        )

        navigateAndAssert(
            VoiceTutorScreens.TeacherStudents.createRoute("1"),
            expectedText = "학생 목록"
        )

        navigateAndAssert(
            VoiceTutorScreens.AllAssignments.route,
            expectedText = "모든 과제"
        )

        navigateAndAssert(
            VoiceTutorScreens.TeacherAssignmentDetail.createRoute(1),
            expectedText = "과제 내용"
        )

        navigateAndAssert(
            VoiceTutorScreens.TeacherAssignmentResults.createRoute(1),
            expectedText = "학생별 결과"
        )

        navigateAndAssert(
            VoiceTutorScreens.TeacherStudentReport.createRoute(1, 1, "홍길동"),
            expectedText = "홍길동"
        )

        navigateAndAssert(
            VoiceTutorScreens.TeacherStudentAssignmentDetail.createRoute("1", 1, "듣기 평가 과제"),
            expectedText = "홍길동"
        )

        navigateAndAssert(
            VoiceTutorScreens.CreateAssignment.createRoute(null),
            expectedText = "기본 정보"
        )

        navigateAndAssert(
            VoiceTutorScreens.EditAssignment.createRoute(1),
            expectedText = "과제 편집"
        )
    }

    @Test
    fun navigateThroughExtendedTeacherRoutes() {
        loginTeacher()

        navigateAndAssert(
            VoiceTutorScreens.TeacherStudents.createRoute("1"),
            expectedText = "학생 목록"
        )

        navigateAndAssert(
            VoiceTutorScreens.TeacherStudentAssignmentDetail.createRoute("1", 1, "듣기 평가 과제"),
            expectedText = "홍길동"
        )

        navigateAndAssert(
            VoiceTutorScreens.TeacherClassDetail.createRoute("수학 A반", 1),
            expectedText = "수학 A반"
        )

        navigateAndAssert(
            VoiceTutorScreens.TeacherAssignmentResults.createRoute(1),
            expectedText = "학생별 결과"
        )

        navigateAndAssert(
            VoiceTutorScreens.AppInfo.route,
            expectedText = "앱 정보"
        )
    }

    @Test
    fun navigateThroughKeyStudentRoutes() {
        loginStudent()

        navigateAndAssert(
            VoiceTutorScreens.Progress.route,
            expectedText = "학습 리포트"
        )

        val personalAssignment = fakeApi.personalAssignmentData
        navigateAndAssert(
            VoiceTutorScreens.AssignmentDetail.createRoute(personalAssignment.id.toString(), personalAssignment.assignment.title),
            expectedText = personalAssignment.assignment.title
        )

        navigateAndAssert(
            VoiceTutorScreens.AssignmentDetailedResults.createRoute(personalAssignment.id, personalAssignment.assignment.title),
            expectedText = personalAssignment.assignment.title
        )

        navigateAndAssert(
            VoiceTutorScreens.NoRecentAssignment.createRoute(personalAssignment.student.id),
            expectedText = "이어할 과제가 없습니다"
        )

        navigateAndAssert(
            VoiceTutorScreens.AppInfo.route,
            expectedText = "앱 정보"
        )
    }
}


