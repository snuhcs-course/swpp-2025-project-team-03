package com.example.voicetutor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.voicetutor.ui.screens.*
import com.example.voicetutor.data.models.UserRole

@Composable
fun VoiceTutorNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = VoiceTutorScreens.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth screens
        composable(VoiceTutorScreens.Login.route) {
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
            
            // 로그인 성공 시 과제 저장
            LaunchedEffect(currentUser) {
                currentUser?.assignments?.let { assignments ->
                    if (assignments.isNotEmpty()) {
                        println("Navigation/Login - Setting ${assignments.size} assignments to ViewModel")
                        assignmentViewModel.setInitialAssignments(assignments)
                    }
                }
            }
            
            // 로그인 성공 시 자동으로 대시보드로 이동
            LaunchedEffect(isLoggedIn, currentUser) {
                if (isLoggedIn && currentUser != null) {
                    when (currentUser?.role) {
                        com.example.voicetutor.data.models.UserRole.TEACHER -> {
                            navController.navigate(VoiceTutorScreens.TeacherDashboard.route) {
                                popUpTo(VoiceTutorScreens.Login.route) { inclusive = true }
                            }
                        }
                        com.example.voicetutor.data.models.UserRole.STUDENT -> {
                            navController.navigate(VoiceTutorScreens.StudentDashboard.route) {
                                popUpTo(VoiceTutorScreens.Login.route) { inclusive = true }
                            }
                        }
                        null -> { /* 로그인되지 않음 */ }
                    }
                }
            }
            
            LoginScreen(
                authViewModel = authViewModel,
                assignmentViewModel = assignmentViewModel,
                onLoginSuccess = {
                    // Navigate to appropriate dashboard based on user role
                    val userRole = authViewModel.currentUser.value?.role
                    when (userRole) {
                        com.example.voicetutor.data.models.UserRole.TEACHER -> {
                            navController.navigate(VoiceTutorScreens.TeacherDashboard.route) {
                                popUpTo(VoiceTutorScreens.Login.route) { inclusive = true }
                            }
                        }
                        com.example.voicetutor.data.models.UserRole.STUDENT -> {
                            navController.navigate(VoiceTutorScreens.StudentDashboard.route) {
                                popUpTo(VoiceTutorScreens.Login.route) { inclusive = true }
                            }
                        }
                        null -> {
                            // Stay on login screen if no user
                        }
                    }
                },
                onSignupClick = {
                    navController.navigate(VoiceTutorScreens.Signup.route)
                }
            )
        }
        
        composable(VoiceTutorScreens.Signup.route) {
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel()
            val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
            
            // 회원가입 성공 시 자동으로 대시보드로 이동
            LaunchedEffect(isLoggedIn, currentUser) {
                if (isLoggedIn && currentUser != null) {
                    when (currentUser?.role) {
                        com.example.voicetutor.data.models.UserRole.TEACHER -> {
                            navController.navigate(VoiceTutorScreens.TeacherDashboard.route) {
                                popUpTo(VoiceTutorScreens.Login.route) { inclusive = true }
                            }
                        }
                        com.example.voicetutor.data.models.UserRole.STUDENT -> {
                            navController.navigate(VoiceTutorScreens.StudentDashboard.route) {
                                popUpTo(VoiceTutorScreens.Login.route) { inclusive = true }
                            }
                        }
                        null -> { /* 회원가입되지 않음 */ }
                    }
                }
            }
            
            SignupScreen(
                onSignupSuccess = {
                    // 회원가입 성공 시 로그인 화면으로 이동
                    navController.popBackStack()
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Student screens with layout
        composable(VoiceTutorScreens.StudentDashboard.route) {
            // Navigation Graph 레벨의 ViewModel 사용 (모든 화면에서 공유)
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            val dashboardViewModel: com.example.voicetutor.ui.viewmodel.DashboardViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            
            MainLayout(
                navController = navController,
                userRole = UserRole.STUDENT
            ) {
                StudentDashboardScreen(
                    authViewModel = authViewModel,
                    assignmentViewModel = assignmentViewModel,
                    dashboardViewModel = dashboardViewModel,
                    onNavigateToAllAssignments = {
                        navController.navigate(VoiceTutorScreens.AllAssignments.route)
                    },
                    onNavigateToProgressReport = {
                        navController.navigate(VoiceTutorScreens.Progress.route)
                    },
                    onNavigateToAssignmentDetail = { assignmentTitle ->
                        navController.navigate(VoiceTutorScreens.AssignmentDetail.createRoute("1", assignmentTitle))
                    }
                )
            }
        }
        
        composable(
            route = VoiceTutorScreens.Assignment.route,
            arguments = listOf(
                navArgument("title") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val assignmentTitle = backStackEntry.arguments?.getString("title") ?: "과제"
            MainLayout(
                navController = navController,
                userRole = UserRole.STUDENT
            ) {
                AssignmentScreen(assignmentTitle = assignmentTitle)
            }
        }
        
        composable(VoiceTutorScreens.Quiz.route) {
            MainLayout(
                navController = navController,
                userRole = UserRole.STUDENT
            ) {
                QuizScreen()
            }
        }
        
        composable(VoiceTutorScreens.Progress.route) {
            MainLayout(
                navController = navController,
                userRole = UserRole.STUDENT
            ) {
                CompletedAssignmentsScreen(
                    onNavigateToAssignmentDetail = { assignmentTitle ->
                        navController.navigate(VoiceTutorScreens.StudentAssignmentDetail.createRoute(assignmentTitle))
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        composable(
            route = VoiceTutorScreens.StudentAssignmentDetail.route,
            arguments = listOf(
                navArgument("title") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val assignmentTitle = backStackEntry.arguments?.getString("title") ?: "과제"
            MainLayout(
                navController = navController,
                userRole = UserRole.STUDENT
            ) {
                StudentAssignmentDetailScreen(
                    assignmentTitle = assignmentTitle,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        // Teacher screens with layout
        composable(
            route = "${VoiceTutorScreens.TeacherDashboard.route}?refresh={refresh}",
            arguments = listOf(
                navArgument("refresh") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val refreshTimestamp = backStackEntry.arguments?.getLong("refresh") ?: 0L
            println("TeacherDashboard composable - Received refresh timestamp: $refreshTimestamp")
            
            // Use graph-scoped ViewModels to share data between screens
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                TeacherDashboardScreen(
                    authViewModel = authViewModel,
                    assignmentViewModel = assignmentViewModel,
                    refreshTimestamp = refreshTimestamp,
                    onNavigateToAllAssignments = {
                        navController.navigate(VoiceTutorScreens.AllAssignments.route)
                    },
                    onNavigateToAllStudents = {
                        navController.navigate(VoiceTutorScreens.AllStudents.route)
                    },
                    onCreateNewAssignment = {
                        navController.navigate(VoiceTutorScreens.CreateAssignment.route)
                    },
                    onNavigateToAssignmentDetail = { assignmentTitle ->
                        navController.navigate(VoiceTutorScreens.TeacherAssignmentDetail.createRoute(assignmentTitle))
                    },
                    onNavigateToAssignmentResults = { assignmentTitle ->
                        navController.navigate(VoiceTutorScreens.TeacherAssignmentResults.createRoute(assignmentTitle))
                    },
                    onNavigateToEditAssignment = { assignmentTitle ->
                        navController.navigate(VoiceTutorScreens.EditAssignment.createRoute(assignmentTitle))
                    },
                    onNavigateToStudentDetail = { studentName ->
                        navController.navigate(VoiceTutorScreens.TeacherStudentDetail.createRoute(studentName))
                    }
                )
            }
        }
        
        composable(VoiceTutorScreens.TeacherClasses.route) {
            // Use graph-scoped ViewModels to share data between screens
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                TeacherClassesScreen(
                    authViewModel = authViewModel,
                    assignmentViewModel = assignmentViewModel,
                    onNavigateToClassDetail = { className ->
                        navController.navigate(VoiceTutorScreens.TeacherClassDetail.createRoute(className))
                    },
                    onNavigateToCreateClass = {
                        navController.navigate(VoiceTutorScreens.CreateClass.route)
                        println("Navigate to create class")
                    },
                    onNavigateToCreateAssignment = {
                        navController.navigate(VoiceTutorScreens.CreateAssignment.route)
                    },
                    onNavigateToStudents = { classId ->
                        navController.navigate(VoiceTutorScreens.TeacherStudents.createRoute(classId.toString()))
                    }
                )
            }
        }
        
        composable(VoiceTutorScreens.TeacherStudents.route) {
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                TeacherStudentsScreen(
                    onNavigateToStudentDetail = { studentId ->
                        navController.navigate(VoiceTutorScreens.TeacherStudentDetail.createRoute(studentId.toString()))
                    },
                    onNavigateToMessage = { studentId ->
                        navController.navigate(VoiceTutorScreens.TeacherMessage.createRoute(studentId.toString()))
                    },
                    navController = navController
                )
            }
        }
        
        composable(VoiceTutorScreens.AllAssignments.route) {
            MainLayout(
                navController = navController,
                userRole = UserRole.STUDENT
            ) {
                AllAssignmentsScreen(
                    onNavigateToAssignmentResults = { assignmentTitle ->
                        navController.navigate(VoiceTutorScreens.TeacherAssignmentResults.createRoute(assignmentTitle))
                    },
                    onNavigateToEditAssignment = { assignmentTitle ->
                        navController.navigate(VoiceTutorScreens.EditAssignment.createRoute(assignmentTitle))
                    },
                    onNavigateToAssignmentDetail = { assignmentTitle ->
                        navController.navigate(VoiceTutorScreens.TeacherAssignmentDetail.createRoute(assignmentTitle))
                    }
                )
            }
        }
        
        composable(VoiceTutorScreens.AllStudents.route) {
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                AllStudentsScreen(
                    onNavigateToStudentDetail = { studentName ->
                        navController.navigate(VoiceTutorScreens.TeacherStudentDetail.createRoute(studentName))
                    },
                    onNavigateToMessage = { studentName ->
                        navController.navigate(VoiceTutorScreens.TeacherMessage.createRoute(studentName))
                    }
                )
            }
        }
        
        // Teacher student detail screen
        composable(
            route = VoiceTutorScreens.TeacherStudentDetail.route,
            arguments = listOf(
                navArgument("name") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val studentName = backStackEntry.arguments?.getString("name") ?: "학생"
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                TeacherStudentDetailScreen(
                    onNavigateToAllAssignments = {
                        navController.navigate(VoiceTutorScreens.AllAssignments.route)
                    },
                    onNavigateToAssignmentDetail = { assignmentTitle ->
                        navController.navigate(VoiceTutorScreens.TeacherStudentAssignmentDetail.createRoute(studentName, assignmentTitle))
                    },
                    onNavigateToMessage = { studentId ->
                        navController.navigate(VoiceTutorScreens.TeacherMessage.createRoute(studentId.toString()))
                    }
                )
            }
        }
        
        // Assignment detail screen
        composable(
            route = VoiceTutorScreens.AssignmentDetail.route,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.StringType
                },
                navArgument("title") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val assignmentId = backStackEntry.arguments?.getString("id") ?: "1"
            val assignmentTitle = backStackEntry.arguments?.getString("title") ?: "과제"
            MainLayout(
                navController = navController,
                userRole = UserRole.STUDENT
            ) {
                AssignmentDetailScreen(
                    assignmentId = assignmentId.toIntOrNull(),
                    assignmentTitle = assignmentTitle,
                    onStartAssignment = {
                        // Navigate to assignment execution screen
                        navController.navigate(VoiceTutorScreens.Assignment.createRoute(assignmentTitle))
                    }
                )
            }
        }
        
        // Create assignment screen
        composable(VoiceTutorScreens.CreateAssignment.route) {
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                CreateAssignmentScreen(
                    authViewModel = authViewModel,
                    assignmentViewModel = assignmentViewModel,
                    onCreateAssignment = { assignmentTitle ->
                        // Navigate back to teacher dashboard with refresh flag
                        val timestamp = System.currentTimeMillis()
                        println("Assignment created: $assignmentTitle")
                        println("Navigating to teacher dashboard with refresh timestamp: $timestamp")
                        
                        // Add timestamp to force refresh
                        val refreshRoute = "${VoiceTutorScreens.TeacherDashboard.route}?refresh=$timestamp"
                        println("Navigation route: $refreshRoute")
                        
                        navController.navigate(refreshRoute) {
                            popUpTo(VoiceTutorScreens.CreateAssignment.route) { inclusive = true }
                        }
                    }
                )
            }
        }
        
        // Edit assignment screen
        composable(
            route = VoiceTutorScreens.EditAssignment.route,
            arguments = listOf(
                navArgument("title") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val assignmentTitle = backStackEntry.arguments?.getString("title") ?: "과제"
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                EditAssignmentScreen(
                    assignmentViewModel = assignmentViewModel,
                    assignmentTitle = assignmentTitle,
                    onSaveAssignment = {
                        // Save edited assignment and navigate back
                        println("Saving edited assignment: $assignmentTitle")
                        navController.popBackStack()
                    }
                )
            }
        }
        
        // Teacher assignment results screen
        composable(
            route = VoiceTutorScreens.TeacherAssignmentResults.route,
            arguments = listOf(
                navArgument("title") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val assignmentTitle = backStackEntry.arguments?.getString("title") ?: "과제"
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                TeacherAssignmentResultsScreen(
                    assignmentViewModel = assignmentViewModel,
                    assignmentTitle = assignmentTitle,
                    onNavigateToStudentDetail = { studentName ->
                        navController.navigate(VoiceTutorScreens.TeacherStudentAssignmentDetail.createRoute(studentName, assignmentTitle))
                    }
                )
            }
        }
        
        // Teacher student assignment detail screen
        composable(
            route = VoiceTutorScreens.TeacherStudentAssignmentDetail.route,
            arguments = listOf(
                navArgument("studentName") {
                    type = NavType.StringType
                },
                navArgument("assignmentTitle") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentName") ?: "1" // studentName 파라미터가 실제로는 studentId
            val assignmentTitle = backStackEntry.arguments?.getString("assignmentTitle") ?: "과제"
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                TeacherStudentAssignmentDetailScreen(
                    studentId = studentId,
                    assignmentTitle = assignmentTitle
                )
            }
        }
        
        // Teacher assignment detail screen
        composable(
            route = VoiceTutorScreens.TeacherAssignmentDetail.route,
            arguments = listOf(
                navArgument("title") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val assignmentTitle = backStackEntry.arguments?.getString("title") ?: "과제"
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                TeacherAssignmentDetailScreen(
                    assignmentViewModel = assignmentViewModel,
                    assignmentTitle = assignmentTitle,
                    onNavigateToAssignmentResults = { title ->
                        navController.navigate(VoiceTutorScreens.TeacherAssignmentResults.createRoute(title))
                    },
                    onNavigateToEditAssignment = { title ->
                        navController.navigate(VoiceTutorScreens.EditAssignment.createRoute(title))
                    }
                )
            }
        }
        
        // Teacher message screen
        composable(
            route = VoiceTutorScreens.TeacherMessage.route,
            arguments = listOf(
                navArgument("studentName") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val studentName = backStackEntry.arguments?.getString("studentName") ?: "학생"
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                TeacherMessageScreen(
                    studentId = 1, // TODO: studentName으로부터 studentId를 조회
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        // Teacher class detail screen
        composable(
            route = VoiceTutorScreens.TeacherClassDetail.route,
            arguments = listOf(
                navArgument("className") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val className = backStackEntry.arguments?.getString("className") ?: "반"
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                TeacherClassDetailScreen(
                    className = className,
                    onNavigateToClassMessage = {
                        navController.navigate(VoiceTutorScreens.ClassMessage.createRoute(className))
                    },
                    onNavigateToCreateAssignment = {
                        navController.navigate(VoiceTutorScreens.CreateAssignment.route)
                    },
                    onNavigateToAssignmentDetail = { assignmentTitle ->
                        navController.navigate(VoiceTutorScreens.TeacherAssignmentDetail.createRoute(assignmentTitle))
                    }
                )
            }
        }
        
        // Class message screen
        composable(
            route = VoiceTutorScreens.ClassMessage.route,
            arguments = listOf(
                navArgument("className") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val className = backStackEntry.arguments?.getString("className") ?: "반"
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                ClassMessageScreen(
                    className = className,
                    onNavigateToMessage = { studentName ->
                        navController.navigate(VoiceTutorScreens.TeacherMessage.createRoute(studentName))
                    }
                )
            }
        }
        
        // Subject detail screen
        composable(
            route = VoiceTutorScreens.SubjectDetail.route,
            arguments = listOf(
                navArgument("subject") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val subject = backStackEntry.arguments?.getString("subject") ?: "과목"
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                SubjectDetailScreen(
                    subject = subject,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        // Settings (accessible from both roles)
        composable(VoiceTutorScreens.Settings.route) {
            // In a real app, you would get the user role from a ViewModel or shared state
            val userRole = UserRole.STUDENT // TODO: Get actual user role from auth state
            MainLayout(
                navController = navController,
                userRole = userRole
            ) {
                SettingsScreen(
                    userRole = userRole,
                    onLogout = {
                        navController.navigate(VoiceTutorScreens.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToServerSettings = {
                        navController.navigate(VoiceTutorScreens.ServerSettings.route)
                    },
                    navController = navController
                )
            }
        }
        
        // Server Settings
        composable(VoiceTutorScreens.ServerSettings.route) {
            ServerSettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        
        // Student edit screen
        composable(
            route = VoiceTutorScreens.StudentEdit.route,
            arguments = listOf(
                navArgument("studentId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getInt("studentId") ?: 1
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                StudentEditScreen(
                    studentId = studentId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
        
        // Create class screen
        composable(VoiceTutorScreens.CreateClass.route) {
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
            
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                CreateClassScreen(
                    teacherId = currentUser?.id?.toString(),
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        // Analytics screen
        composable(VoiceTutorScreens.Analytics.route) {
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                AnalyticsScreen(
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        // Notification settings screen
        composable(VoiceTutorScreens.NotificationSettings.route) {
            NotificationSettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Language settings screen
        composable(VoiceTutorScreens.LanguageSettings.route) {
            LanguageSettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Help screen
        composable(VoiceTutorScreens.Help.route) {
            HelpScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // App info screen
        composable(VoiceTutorScreens.AppInfo.route) {
            AppInfoScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

// Navigation routes
sealed class VoiceTutorScreens(val route: String) {
    object Login : VoiceTutorScreens("login")
    object Signup : VoiceTutorScreens("signup")
    object StudentDashboard : VoiceTutorScreens("student_dashboard")
    object TeacherDashboard : VoiceTutorScreens("teacher_dashboard")
    object Settings : VoiceTutorScreens("settings")
    
    // Student screens
    object Assignment : VoiceTutorScreens("assignment/{title}") {
        fun createRoute(title: String) = "assignment/$title"
    }
    object AssignmentDetail : VoiceTutorScreens("assignment_detail/{id}/{title}") {
        fun createRoute(id: String, title: String) = "assignment_detail/$id/$title"
    }
    object StudentAssignmentDetail : VoiceTutorScreens("student_assignment_detail/{title}") {
        fun createRoute(title: String) = "student_assignment_detail/$title"
    }
    object Quiz : VoiceTutorScreens("quiz")
    object Progress : VoiceTutorScreens("progress")
    object ServerSettings : VoiceTutorScreens("server_settings")
    object CreateClass : VoiceTutorScreens("create_class")
    object Analytics : VoiceTutorScreens("analytics")
    object NotificationSettings : VoiceTutorScreens("notification_settings")
    object LanguageSettings : VoiceTutorScreens("language_settings")
    object Help : VoiceTutorScreens("help")
    object AppInfo : VoiceTutorScreens("app_info")
    
    // Teacher screens
    object TeacherClasses : VoiceTutorScreens("teacher_classes")
    object TeacherStudents : VoiceTutorScreens("teacher_students/{classId}") {
        fun createRoute(classId: String) = "teacher_students/$classId"
    }
    object AllAssignments : VoiceTutorScreens("all_assignments")
    object AllStudents : VoiceTutorScreens("all_students")
    object CreateAssignment : VoiceTutorScreens("create_assignment")
    object EditAssignment : VoiceTutorScreens("edit_assignment/{title}") {
        fun createRoute(title: String) = "edit_assignment/$title"
    }
    object TeacherAssignmentResults : VoiceTutorScreens("teacher_assignment_results/{title}") {
        fun createRoute(title: String) = "teacher_assignment_results/$title"
    }
    object TeacherStudentDetail : VoiceTutorScreens("teacher_student_detail/{name}") {
        fun createRoute(name: String) = "teacher_student_detail/$name"
    }
    object TeacherStudentAssignmentDetail : VoiceTutorScreens("teacher_student_assignment_detail/{studentName}/{assignmentTitle}") {
        fun createRoute(studentName: String, assignmentTitle: String) = "teacher_student_assignment_detail/$studentName/$assignmentTitle"
    }
    object TeacherAssignmentDetail : VoiceTutorScreens("teacher_assignment_detail/{title}") {
        fun createRoute(title: String) = "teacher_assignment_detail/$title"
    }
    object TeacherMessage : VoiceTutorScreens("teacher_message/{studentName}") {
        fun createRoute(studentName: String) = "teacher_message/$studentName"
    }
    object TeacherClassDetail : VoiceTutorScreens("teacher_class_detail/{className}") {
        fun createRoute(className: String) = "teacher_class_detail/$className"
    }
    object ClassMessage : VoiceTutorScreens("class_message/{className}") {
        fun createRoute(className: String) = "class_message/$className"
    }
    object SubjectDetail : VoiceTutorScreens("subject_detail/{subject}") {
        fun createRoute(subject: String) = "subject_detail/$subject"
    }
    object AttendanceManagement : VoiceTutorScreens("attendance_management/{classId}") {
        fun createRoute(classId: Int) = "attendance_management/$classId"
    }
    object StudentEdit : VoiceTutorScreens("student_edit/{studentId}") {
        fun createRoute(studentId: Int) = "student_edit/$studentId"
    }
}
