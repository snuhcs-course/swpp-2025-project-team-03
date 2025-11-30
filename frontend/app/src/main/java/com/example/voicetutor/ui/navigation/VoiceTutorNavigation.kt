package com.example.voicetutor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.ui.screens.*

@Composable
fun VoiceTutorNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = VoiceTutorScreens.Login.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    ) {
        composable(VoiceTutorScreens.Login.route) {
            val graphBackStackEntry = remember(navController) {
                navController.getBackStackEntry(navController.graph.id)
            }
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(graphBackStackEntry)
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(graphBackStackEntry)
            val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

            LaunchedEffect(currentUser) {
                currentUser?.assignments?.let { assignments ->
                    if (assignments.isNotEmpty()) {
                        assignmentViewModel.setInitialAssignments(assignments)
                    }
                }
            }

            LaunchedEffect(isLoggedIn, currentUser) {
                val user = currentUser
                if (isLoggedIn && user != null) {
                    when (user.role) {
                        UserRole.TEACHER -> {
                            navController.navigate(VoiceTutorScreens.TeacherDashboard.route) {
                                popUpTo(VoiceTutorScreens.Login.route) { inclusive = true }
                            }
                        }
                        UserRole.STUDENT -> {
                            navController.navigate(VoiceTutorScreens.StudentDashboard.route) {
                                popUpTo(VoiceTutorScreens.Login.route) { inclusive = true }
                            }
                        }
                    }
                }
            }

            LoginScreen(
                authViewModel = authViewModel,
                assignmentViewModel = assignmentViewModel,
                onLoginSuccess = {
                    val userRole = authViewModel.currentUser.value?.role
                    when (userRole) {
                        UserRole.TEACHER -> {
                            navController.navigate(VoiceTutorScreens.TeacherDashboard.route) {
                                popUpTo(VoiceTutorScreens.Login.route) { inclusive = true }
                            }
                        }
                        UserRole.STUDENT -> {
                            navController.navigate(VoiceTutorScreens.StudentDashboard.route) {
                                popUpTo(VoiceTutorScreens.Login.route) { inclusive = true }
                            }
                        }
                        null -> {}
                    }
                },
                onSignupClick = {
                    navController.navigate(VoiceTutorScreens.Signup.route)
                },
            )
        }

        composable(VoiceTutorScreens.Signup.route) {
            val graphBackStackEntry = remember(navController) {
                navController.getBackStackEntry(navController.graph.id)
            }
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(graphBackStackEntry)
            val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

            LaunchedEffect(isLoggedIn, currentUser) {
                val user = currentUser
                if (isLoggedIn && user != null) {
                    when (user.role) {
                        UserRole.TEACHER -> {
                            navController.navigate(VoiceTutorScreens.TeacherDashboard.route) {
                                popUpTo(VoiceTutorScreens.Login.route) { inclusive = true }
                            }
                        }
                        UserRole.STUDENT -> {
                            navController.navigate(VoiceTutorScreens.StudentDashboard.route) {
                                popUpTo(VoiceTutorScreens.Login.route) { inclusive = true }
                            }
                        }
                    }
                }
            }

            SignupScreen(
                authViewModel = authViewModel,
                onLoginClick = {
                    navController.popBackStack()
                },
            )
        }

        composable(VoiceTutorScreens.StudentDashboard.route) {
            val graphBackStackEntry = remember(navController) {
                navController.getBackStackEntry(navController.graph.id)
            }
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(graphBackStackEntry)
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(graphBackStackEntry)

            MainLayout(
                navController = navController,
                userRole = UserRole.STUDENT,
            ) {
                StudentDashboardScreen(
                    authViewModel = authViewModel,
                    assignmentViewModel = assignmentViewModel,
                    onNavigateToAssignmentDetail = { assignmentId ->
                        navController.navigate(VoiceTutorScreens.AssignmentDetail.createRoute(assignmentId, "과제"))
                    },
                )
            }
        }

        composable(
            route = VoiceTutorScreens.Assignment.route,
            arguments = listOf(
                navArgument("assignmentId") {
                    type = NavType.StringType
                },
                navArgument("title") {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val assignmentId = backStackEntry.arguments?.getString("assignmentId") ?: "1"

            val graphBackStackEntry = remember(navController) {
                navController.getBackStackEntry(navController.graph.id)
            }
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(graphBackStackEntry)

            MainLayout(
                navController = navController,
                userRole = UserRole.STUDENT,
            ) {
                AssignmentScreen(
                    assignmentId = assignmentId.toIntOrNull(),
                    authViewModel = authViewModel,
                    onNavigateToHome = {
                        navController.navigate(VoiceTutorScreens.StudentDashboard.route) {
                            popUpTo(VoiceTutorScreens.StudentDashboard.route) { inclusive = true }
                        }
                    },
                )
            }
        }

        composable(VoiceTutorScreens.Progress.route) {
            val graphBackStackEntry = remember(navController) {
                navController.getBackStackEntry(navController.graph.id)
            }
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(graphBackStackEntry)
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

            MainLayout(
                navController = navController,
                userRole = UserRole.STUDENT,
            ) {
                ReportScreen(
                    studentId = currentUser?.id,
                    onNavigateToAssignmentReport = { personalAssignmentId: Int, assignmentTitle: String ->
                        navController.navigate(VoiceTutorScreens.AssignmentDetailedResults.createRoute(personalAssignmentId, assignmentTitle))
                    },
                )
            }
        }

        composable(
            route = VoiceTutorScreens.NoRecentAssignment.route,
            arguments = listOf(
                navArgument("studentId") {
                    type = NavType.IntType
                },
            ),
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getInt("studentId")
            if (studentId != null) {
                MainLayout(
                    navController = navController,
                    userRole = UserRole.STUDENT,
                ) {
                    NoRecentAssignmentScreen()
                }
            }
        }

        composable(
            route = VoiceTutorScreens.AssignmentDetailedResults.route,
            arguments = listOf(
                navArgument("personalAssignmentId") {
                    type = NavType.IntType
                },
                navArgument("title") {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val personalAssignmentId = backStackEntry.arguments?.getInt("personalAssignmentId") ?: 0
            val assignmentTitle = backStackEntry.arguments?.getString("title") ?: "과제 결과"
            MainLayout(
                navController = navController,
                userRole = UserRole.STUDENT,
            ) {
                AssignmentDetailedResultsScreen(
                    personalAssignmentId = personalAssignmentId,
                    assignmentTitle = assignmentTitle,
                )
            }
        }

        composable(
            route = "${VoiceTutorScreens.TeacherDashboard.route}?refresh={refresh}&deleted={deleted}",
            arguments = listOf(
                navArgument("refresh") {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument("deleted") {
                    type = NavType.BoolType
                    defaultValue = false
                },
            ),
        ) { backStackEntry ->
            val refreshTimestamp = backStackEntry.arguments?.getLong("refresh") ?: 0L
            val deleted = backStackEntry.arguments?.getBoolean("deleted") ?: false

            val graphBackStackEntry = remember(navController) {
                navController.getBackStackEntry(navController.graph.id)
            }
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(graphBackStackEntry)
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(graphBackStackEntry)

            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER,
            ) {
                TeacherDashboardScreen(
                    authViewModel = authViewModel,
                    assignmentViewModel = assignmentViewModel,
                    refreshTimestamp = refreshTimestamp,
                    showDeletedToast = deleted,
                    onCreateNewAssignment = {
                        navController.navigate(VoiceTutorScreens.CreateAssignment.createRoute(null))
                    },
                    onNavigateToCreateClass = {
                        navController.navigate(VoiceTutorScreens.CreateClass.route)
                    },
                    onNavigateToAssignmentDetail = { assignmentId ->
                        navController.navigate(VoiceTutorScreens.TeacherAssignmentDetail.createRoute(assignmentId))
                    },
                )
            }
        }

        composable(
            route = VoiceTutorScreens.TeacherClasses.route,
        ) {
            val graphBackStackEntry = remember(navController) {
                navController.getBackStackEntry(navController.graph.id)
            }
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(graphBackStackEntry)
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(graphBackStackEntry)
            val classViewModel: com.example.voicetutor.ui.viewmodel.ClassViewModel = hiltViewModel(graphBackStackEntry)

            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER,
            ) {
                TeacherClassesScreen(
                    authViewModel = authViewModel,
                    assignmentViewModel = assignmentViewModel,
                    classViewModel = classViewModel,
                    onNavigateToClassDetail = { className, classId ->
                        navController.navigate(VoiceTutorScreens.TeacherClassDetail.createRoute(className, classId))
                    },
                    onNavigateToCreateClass = {
                        navController.navigate(VoiceTutorScreens.CreateClass.route)
                    },
                    onNavigateToCreateAssignment = { classId ->
                        navController.navigate(VoiceTutorScreens.CreateAssignment.createRoute(classId))
                    },
                    onNavigateToStudents = { classId ->
                        navController.navigate(VoiceTutorScreens.TeacherStudents.createRoute(classId.toString()))
                    },
                )
            }
        }

        composable(
            route = VoiceTutorScreens.TeacherStudents.route,
            arguments = listOf(
                navArgument("classId") {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId")?.toIntOrNull()
            val graphBackStackEntry = remember(navController) {
                navController.getBackStackEntry(navController.graph.id)
            }
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(graphBackStackEntry)
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER,
            ) {
                TeacherStudentsScreen(
                    classId = classId,
                    teacherId = currentUser?.id?.toString(),
                )
            }
        }

        composable(VoiceTutorScreens.AllAssignments.route) {
            val graphBackStackEntry = remember(navController) {
                navController.getBackStackEntry(navController.graph.id)
            }
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(graphBackStackEntry)
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER,
            ) {
                AllAssignmentsScreen(
                    teacherId = currentUser?.id?.toString(),
                    onNavigateToAssignmentResults = { assignmentId ->
                        navController.navigate(VoiceTutorScreens.TeacherAssignmentResults.createRoute(assignmentId))
                    },
                    onNavigateToEditAssignment = { assignmentId ->
                        navController.navigate(VoiceTutorScreens.EditAssignment.createRoute(assignmentId))
                    },
                    onNavigateToAssignmentDetail = { assignmentId ->
                        navController.navigate(VoiceTutorScreens.TeacherAssignmentDetail.createRoute(assignmentId))
                    },
                )
            }
        }

        composable(VoiceTutorScreens.AllStudents.route) {
            val graphBackStackEntry = remember(navController) {
                navController.getBackStackEntry(navController.graph.id)
            }
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(graphBackStackEntry)
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER,
            ) {
                AllStudentsScreen(
                    teacherId = currentUser?.id?.toString() ?: "1",
                    onNavigateToStudentDetail = { classId, studentId, studentName ->
                        navController.navigate(VoiceTutorScreens.TeacherStudentReport.createRoute(classId, studentId, studentName))
                    },
                )
            }
        }

        composable(
            route = VoiceTutorScreens.AssignmentDetail.route,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                },
                navArgument("title") {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val assignmentId = backStackEntry.arguments?.getString("id") ?: "1"
            val assignmentTitle = backStackEntry.arguments?.getString("title") ?: "과제"
            MainLayout(
                navController = navController,
                userRole = UserRole.STUDENT,
            ) {
                val graphBackStackEntry = remember(navController.graph.id) {
                    navController.getBackStackEntry(navController.graph.id)
                }
                val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(graphBackStackEntry)
                val selectedPersonalAssignmentId by assignmentViewModel.selectedPersonalAssignmentId.collectAsStateWithLifecycle()

                AssignmentDetailScreen(
                    assignmentId = assignmentId.toIntOrNull(),
                    assignmentTitle = assignmentTitle,
                    onStartAssignment = {
                        val personalId = assignmentId.toIntOrNull()?.toString() ?: selectedPersonalAssignmentId?.toString() ?: "1"
                        navController.navigate(VoiceTutorScreens.Assignment.createRoute(personalId, assignmentTitle))
                    },
                    assignmentViewModelParam = assignmentViewModel,
                )
            }
        }

        composable(
            route = VoiceTutorScreens.CreateAssignment.route,
            arguments = listOf(
                navArgument("classId") {
                    type = NavType.IntType
                    defaultValue = 0
                },
            ),
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0
            val graphBackStackEntry = remember(navController) {
                navController.getBackStackEntry(navController.graph.id)
            }
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(graphBackStackEntry)
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(graphBackStackEntry)

            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER,
            ) {
                CreateAssignmentScreen(
                    authViewModel = authViewModel,
                    assignmentViewModel = assignmentViewModel,
                    initialClassId = if (classId > 0) classId else null,
                    onCreateAssignment = {
                        val timestamp = System.currentTimeMillis()
                        val refreshRoute = "${VoiceTutorScreens.TeacherDashboard.route}?refresh=$timestamp"
                        navController.navigate(refreshRoute) {
                            popUpTo(VoiceTutorScreens.CreateAssignment.route) { inclusive = true }
                        }
                    },
                )
            }
        }

        composable(
            route = VoiceTutorScreens.EditAssignment.route,
            arguments = listOf(
                navArgument("assignment_id") {
                    type = NavType.IntType
                },
            ),
        ) { backStackEntry ->
            val assignmentId = backStackEntry.arguments?.getInt("assignment_id") ?: 0
            val graphBackStackEntry = remember(navController) {
                navController.getBackStackEntry(navController.graph.id)
            }
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(graphBackStackEntry)

            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER,
            ) {
                EditAssignmentScreen(
                    assignmentViewModel = assignmentViewModel,
                    assignmentId = assignmentId,
                    onSaveAssignment = {
                        navController.popBackStack()
                    },
                    onDeleteAssignment = {
                        val timestamp = System.currentTimeMillis()
                        navController.navigate("${VoiceTutorScreens.TeacherDashboard.route}?refresh=$timestamp&deleted=true") {
                            popUpTo(VoiceTutorScreens.TeacherDashboard.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }

        composable(
            route = VoiceTutorScreens.TeacherAssignmentResults.route,
            arguments = listOf(
                navArgument("assignment_id") {
                    type = NavType.IntType
                },
            ),
        ) { backStackEntry ->
            val assignmentId = backStackEntry.arguments?.getInt("assignment_id") ?: 0
            val graphBackStackEntry = remember(navController) {
                navController.getBackStackEntry(navController.graph.id)
            }
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(graphBackStackEntry)

            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER,
            ) {
                TeacherAssignmentResultsScreen(
                    assignmentViewModel = assignmentViewModel,
                    assignmentId = assignmentId,
                    onNavigateToStudentDetail = { studentId, targetAssignmentId, assignmentTitle ->
                        navController.navigate(
                            VoiceTutorScreens.TeacherStudentAssignmentDetail.createRoute(
                                studentId,
                                targetAssignmentId,
                                assignmentTitle,
                            ),
                        )
                    },
                )
            }
        }

        composable(
            route = VoiceTutorScreens.TeacherStudentAssignmentDetail.route,
            arguments = listOf(
                navArgument("studentId") {
                    type = NavType.StringType
                },
                navArgument("assignmentId") {
                    type = NavType.IntType
                    defaultValue = 0
                },
                navArgument("assignmentTitle") {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: "1"
            val assignmentId = backStackEntry.arguments?.getInt("assignmentId") ?: 0
            val assignmentTitle = backStackEntry.arguments?.getString("assignmentTitle")?.replace("_", "/") ?: "과제"

            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER,
            ) {
                TeacherStudentAssignmentDetailScreen(
                    studentId = studentId,
                    assignmentId = assignmentId,
                    assignmentTitle = assignmentTitle,
                )
            }
        }

        composable(
            route = VoiceTutorScreens.TeacherAssignmentDetail.route,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.IntType
                },
            ),
        ) { backStackEntry ->
            val assignmentId = backStackEntry.arguments?.getInt("id") ?: 0
            val graphBackStackEntry = remember(navController) {
                navController.getBackStackEntry(navController.graph.id)
            }
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(graphBackStackEntry)

            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER,
            ) {
                TeacherAssignmentDetailScreen(
                    assignmentViewModel = assignmentViewModel,
                    assignmentId = assignmentId,
                    onNavigateToEditAssignment = { assignmentId ->
                        navController.navigate(VoiceTutorScreens.EditAssignment.createRoute(assignmentId))
                    },
                    onNavigateToStudentDetail = { studentId, targetAssignmentId, assignmentTitle ->
                        navController.navigate(
                            VoiceTutorScreens.TeacherStudentAssignmentDetail.createRoute(
                                studentId,
                                targetAssignmentId,
                                assignmentTitle,
                            ),
                        )
                    },
                )
            }
        }

        composable(
            route = VoiceTutorScreens.TeacherClassDetail.route,
            arguments = listOf(
                navArgument("className") {
                    type = NavType.StringType
                },
                navArgument("classId") {
                    type = NavType.IntType
                },
            ),
        ) { backStackEntry ->
            val className = backStackEntry.arguments?.getString("className") ?: "반"
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER,
            ) {
                TeacherClassDetailScreen(
                    classId = classId,
                    className = className,
                    onNavigateToCreateAssignment = { classId ->
                        navController.navigate(VoiceTutorScreens.CreateAssignment.createRoute(classId))
                    },
                    onNavigateToAssignmentDetail = { assignmentId ->
                        navController.navigate(VoiceTutorScreens.TeacherAssignmentDetail.createRoute(assignmentId))
                    },
                )
            }
        }

        composable(
            route = VoiceTutorScreens.Settings.route,
            arguments = listOf(
                navArgument("studentId") {
                    type = NavType.IntType
                    defaultValue = -1
                },
            ),
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getInt("studentId") ?: -1
            val graphBackStackEntry = remember(navController) {
                navController.getBackStackEntry(navController.graph.id)
            }
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(graphBackStackEntry)
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
            val userRole = currentUser?.role ?: (if (studentId != -1) UserRole.STUDENT else UserRole.STUDENT)

            MainLayout(
                navController = navController,
                userRole = userRole,
            ) {
                SettingsScreen(
                    studentId = if (studentId != -1) studentId else null,
                    navController = navController,
                )
            }
        }

        composable(VoiceTutorScreens.CreateClass.route) {
            val graphBackStackEntry = remember(navController) {
                navController.getBackStackEntry(navController.graph.id)
            }
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(graphBackStackEntry)
            val classViewModel: com.example.voicetutor.ui.viewmodel.ClassViewModel = hiltViewModel(graphBackStackEntry)
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER,
            ) {
                CreateClassScreen(
                    teacherId = currentUser?.id?.toString(),
                    classViewModel = classViewModel,
                    onClassCreated = {
                        navController.navigate(VoiceTutorScreens.TeacherClasses.route) {
                            popUpTo(VoiceTutorScreens.TeacherClasses.route) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                )
            }
        }

        composable(
            route = VoiceTutorScreens.TeacherStudentReport.route,
            arguments = listOf(
                navArgument("classId") {
                    type = NavType.IntType
                },
                navArgument("studentId") {
                    type = NavType.IntType
                },
                navArgument("studentName") {
                    type = NavType.StringType
                },
            ),
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0
            val studentId = backStackEntry.arguments?.getInt("studentId") ?: 0
            val studentName = backStackEntry.arguments?.getString("studentName")?.replace("_", " ") ?: "학생"

            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER,
            ) {
                TeacherStudentReportScreen(
                    classId = classId,
                    studentId = studentId,
                    studentName = studentName,
                )
            }
        }

        composable(VoiceTutorScreens.AppInfo.route) {
            AppInfoScreen(
                onBackClick = {
                    navController.popBackStack()
                },
            )
        }
    }
}

sealed class VoiceTutorScreens(val route: String) {
    object Login : VoiceTutorScreens("login")
    object Signup : VoiceTutorScreens("signup")
    object StudentDashboard : VoiceTutorScreens("student_dashboard")
    object TeacherDashboard : VoiceTutorScreens("teacher_dashboard")
    object Settings : VoiceTutorScreens("settings/{studentId}") {
        fun createRoute(studentId: Int? = null) = if (studentId != null) "settings/$studentId" else "settings/-1"
    }

    object Assignment : VoiceTutorScreens("assignment/{assignmentId}/{title}") {
        fun createRoute(assignmentId: String, title: String) = "assignment/$assignmentId/$title"
    }
    object AssignmentDetail : VoiceTutorScreens("assignment_detail/{id}/{title}") {
        fun createRoute(id: String, title: String) = "assignment_detail/$id/$title"
    }
    object AssignmentDetailedResults : VoiceTutorScreens("assignment_detailed_results/{personalAssignmentId}/{title}") {
        fun createRoute(personalAssignmentId: Int, title: String) = "assignment_detailed_results/$personalAssignmentId/$title"
    }
    object Progress : VoiceTutorScreens("progress")
    object NoRecentAssignment : VoiceTutorScreens("no_recent_assignment/{studentId}") {
        fun createRoute(studentId: Int) = "no_recent_assignment/$studentId"
    }
    object CreateClass : VoiceTutorScreens("create_class")
    object AppInfo : VoiceTutorScreens("app_info")

    object TeacherClasses : VoiceTutorScreens("teacher_classes")
    object TeacherStudents : VoiceTutorScreens("teacher_students/{classId}") {
        fun createRoute(classId: String) = "teacher_students/$classId"
    }
    object AllAssignments : VoiceTutorScreens("all_assignments")
    object AllStudents : VoiceTutorScreens("all_students")
    object CreateAssignment : VoiceTutorScreens("create_assignment/{classId}") {
        fun createRoute(classId: Int? = null) = if (classId != null && classId > 0) "create_assignment/$classId" else "create_assignment/0"
    }
    object EditAssignment : VoiceTutorScreens("edit_assignment/{assignment_id}") {
        fun createRoute(assignmentId: Int) = "edit_assignment/$assignmentId"
    }
    object TeacherAssignmentResults : VoiceTutorScreens("teacher_assignment_results/{assignment_id}") {
        fun createRoute(assignmentId: Int) = "teacher_assignment_results/$assignmentId"
    }
    object TeacherAssignmentDetail : VoiceTutorScreens("teacher_assignment_detail/{id}") {
        fun createRoute(id: Int) = "teacher_assignment_detail/$id"
    }
    object TeacherStudentAssignmentDetail : VoiceTutorScreens("teacher_student_assignment_detail/{studentId}/{assignmentId}/{assignmentTitle}") {
        fun createRoute(studentId: String, assignmentId: Int, assignmentTitle: String) = "teacher_student_assignment_detail/$studentId/$assignmentId/${assignmentTitle.replace("/", "_")}"
    }
    object TeacherClassDetail : VoiceTutorScreens("teacher_class_detail/{className}/{classId}") {
        fun createRoute(className: String, classId: Int) = "teacher_class_detail/$className/$classId"
    }
    object AttendanceManagement : VoiceTutorScreens("attendance_management/{classId}") {
        fun createRoute(classId: Int) = "attendance_management/$classId"
    }
    object TeacherStudentReport : VoiceTutorScreens("teacher_student_report/{classId}/{studentId}/{studentName}") {
        fun createRoute(classId: Int, studentId: Int, studentName: String) = "teacher_student_report/$classId/$studentId/${studentName.replace("/", "_")}"
    }
}
