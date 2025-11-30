package com.example.voicetutor.ui.navigation

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.voicetutor.HiltComponentActivity
import com.example.voicetutor.data.models.AssignmentData
import com.example.voicetutor.data.models.CourseClass
import com.example.voicetutor.data.models.Subject
import com.example.voicetutor.data.models.User
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
class VoiceTutorNavigationCoverageTest {

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
    fun testLoginSetsInitialAssignments() {
        setContent()

        val authViewModel = authViewModel()
        val assignmentViewModel = assignmentViewModel()

        val courseClass = CourseClass(
            id = 1,
            name = "수학 A반",
            description = "기초 수학 수업",
            subject = Subject(id = 1, name = "수학", code = "MATH"),
            teacherName = "김선생님",
            studentCount = 25,
            createdAt = "2024-01-01",
        )
        val userWithAssignments = User(
            id = 1,
            name = "Test Student",
            email = "student@test.com",
            role = UserRole.STUDENT,
            assignments = listOf(
                AssignmentData(
                    id = 1,
                    title = "Test Assignment",
                    courseClass = courseClass,
                    dueAt = "2024-12-31",
                    createdAt = "2024-01-01",
                    totalQuestions = 10,
                ),
            ),
        )

        composeRule.runOnIdle {
            val field = AuthViewModel::class.java.getDeclaredField("_currentUser")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val stateFlow = field.get(authViewModel) as MutableStateFlow<User?>
            stateFlow.value = userWithAssignments
        }

        composeRule.waitForIdle()

        composeRule.runOnIdle {
            assert(assignmentViewModel.assignments.value.isNotEmpty())
        }
    }

    @Test
    fun testAutoNavigationAfterLogin_Teacher() {
        setContent()

        val authViewModel = authViewModel()

        composeRule.runOnIdle {
            authViewModel.login("teacher@voicetutor.com", "teacher123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null &&
                authViewModel.isLoggedIn.value
        }

        waitForRoutePrefix(VoiceTutorScreens.TeacherDashboard.route)
    }

    @Test
    fun testAutoNavigationAfterLogin_Student() {
        setContent()

        val authViewModel = authViewModel()

        composeRule.runOnIdle {
            authViewModel.login("student@voicetutor.com", "student123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null &&
                authViewModel.isLoggedIn.value
        }

        waitForRoutePrefix(VoiceTutorScreens.StudentDashboard.route)
    }

    @Test
    fun testSignupAutoNavigation() {
        setContent()

        val authViewModel = authViewModel()

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.Signup.route)
        }

        waitForRoutePrefix(VoiceTutorScreens.Signup.route)

        composeRule.runOnIdle {
            val user = User(
                id = 1,
                name = "New User",
                email = "newuser@test.com",
                role = UserRole.STUDENT,
            )
            val field = AuthViewModel::class.java.getDeclaredField("_currentUser")
            field.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val stateFlow = field.get(authViewModel) as MutableStateFlow<User?>
            stateFlow.value = user

            val isLoggedInField = AuthViewModel::class.java.getDeclaredField("_isLoggedIn")
            isLoggedInField.isAccessible = true
            @Suppress("UNCHECKED_CAST")
            val isLoggedInFlow = isLoggedInField.get(authViewModel) as MutableStateFlow<Boolean>
            isLoggedInFlow.value = true
        }

        composeRule.waitForIdle()

        waitForRoutePrefix(VoiceTutorScreens.StudentDashboard.route)
    }

    @Test
    fun testSignupScreenCallbacks() {
        setContent()

        composeRule.runOnIdle {
            navController.navigate(VoiceTutorScreens.Signup.route)
        }

        waitForRoutePrefix(VoiceTutorScreens.Signup.route)

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("로그인", substring = true).fetchSemanticsNodes().isNotEmpty()
        }

        /*
        Row(...) {
            Text("이미 계정이 있으신가요? ")
            Text(
                text = "로그인",
                ...
                modifier = Modifier.clickable { onLoginClick() }
            )
        }
         */

        composeRule.onNodeWithText("로그인", substring = true).performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun testTeacherDashboardWithRefreshParameter() {
        setContent()

        val authViewModel = authViewModel()

        composeRule.runOnIdle {
            authViewModel.login("teacher@voicetutor.com", "teacher123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null
        }

        waitForRoutePrefix(VoiceTutorScreens.TeacherDashboard.route)

        val timestamp = System.currentTimeMillis()
        composeRule.runOnIdle {
            navController.navigate("${VoiceTutorScreens.TeacherDashboard.route}?refresh=$timestamp&deleted=false")
        }

        waitForRoutePrefix(VoiceTutorScreens.TeacherDashboard.route)
        composeRule.waitForIdle()
    }

    @Test
    fun testTeacherDashboardWithDeletedParameter() {
        setContent()

        val authViewModel = authViewModel()

        composeRule.runOnIdle {
            authViewModel.login("teacher@voicetutor.com", "teacher123")
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            authViewModel.currentUser.value != null
        }

        waitForRoutePrefix(VoiceTutorScreens.TeacherDashboard.route)

        val timestamp = System.currentTimeMillis()
        composeRule.runOnIdle {
            navController.navigate("${VoiceTutorScreens.TeacherDashboard.route}?refresh=$timestamp&deleted=true")
        }

        waitForRoutePrefix(VoiceTutorScreens.TeacherDashboard.route)
        composeRule.waitForIdle()
    }

    @Test
    fun testTeacherClassesWithCreatedParameter() {
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
            navController.navigate("${VoiceTutorScreens.TeacherClasses.route}?created=true")
        }

        waitForRoutePrefix(VoiceTutorScreens.TeacherClasses.route)
        composeRule.waitForIdle()
    }

    @Test
    fun testNoRecentAssignmentWithNullStudentId() {
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
            navController.navigate("no_recent_assignment/999")
        }

        waitForRoutePrefix("no_recent_assignment")
        composeRule.waitForIdle()
    }

    @Test
    fun testCreateAssignmentOnCreateCallback() {
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
        composeRule.waitForIdle()
    }

    @Test
    fun testEditAssignmentNavigation() {
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
            navController.navigate(VoiceTutorScreens.EditAssignment.createRoute(1))
        }

        waitForRoutePrefix(VoiceTutorScreens.EditAssignment.route.substringBefore("{"))
        composeRule.waitForIdle()

        composeRule.onAllNodesWithText("과제 편집", substring = true, useUnmergedTree = true)
            .onFirst()
            .assertIsDisplayed()
    }

    @Test
    fun testCreateClassOnClassCreatedCallback() {
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
        composeRule.waitForIdle()
    }

    @Test
    fun testStudentDashboardNavigationCallbacks() {
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
            navController.navigate(VoiceTutorScreens.AssignmentDetail.createRoute("1", "Test Assignment"))
        }

        waitForRoutePrefix(VoiceTutorScreens.AssignmentDetail.route.substringBefore("{"))
        composeRule.waitForIdle()
    }

    @Test
    fun testAssignmentScreenOnNavigateToHome() {
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
            navController.navigate(VoiceTutorScreens.Assignment.createRoute("1", "Test Assignment"))
        }

        waitForRoutePrefix(VoiceTutorScreens.Assignment.route.substringBefore("{"))
        composeRule.waitForIdle()
    }
}
