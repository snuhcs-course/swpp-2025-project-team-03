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
                    onNavigateToAllAssignments = { studentId ->
                        // 학생은 PendingAssignments로 이동 (해야 할 과제)
                        navController.navigate(VoiceTutorScreens.PendingAssignments.createRoute(studentId))
                    },
                    onNavigateToCompletedAssignments = { studentId ->
                        navController.navigate(VoiceTutorScreens.CompletedAssignments.createRoute(studentId))
                    },
                    onNavigateToAllStudentAssignments = { studentId ->
                        navController.navigate(VoiceTutorScreens.AllStudentAssignments.createRoute(studentId))
                    },
                    onNavigateToProgressReport = {
                        navController.navigate(VoiceTutorScreens.Progress.route)
                    },
                    onNavigateToAssignmentDetail = { assignmentId ->
                        // assignmentId는 PersonalAssignment ID (String 형태로 전달됨)
                        navController.navigate(VoiceTutorScreens.AssignmentDetail.createRoute(assignmentId, "과제"))
                    }
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
                }
            )
        ) { backStackEntry ->
            val assignmentId = backStackEntry.arguments?.getString("assignmentId") ?: "1"
            val assignmentTitle = backStackEntry.arguments?.getString("title") ?: "과제"
            
            // Use graph-scoped ViewModels to share data between screens
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
            
            MainLayout(
                navController = navController,
                userRole = UserRole.STUDENT
            ) {
                AssignmentScreen(
                    assignmentId = assignmentId.toIntOrNull(),
                    assignmentTitle = assignmentTitle,
                    authViewModel = authViewModel,
                    onNavigateToHome = {
                        // StudentDashboard로 이동하고 모든 백스택 제거 후 재로딩
                        navController.navigate(VoiceTutorScreens.StudentDashboard.route) {
                            popUpTo(VoiceTutorScreens.StudentDashboard.route) { inclusive = true }
                            // StudentDashboard까지 포함하여 제거하고 새로 생성하여 재로딩 보장
                        }
                    }
                )
            }
        }
        
        composable(VoiceTutorScreens.Progress.route) {
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
            
            MainLayout(
                navController = navController,
                userRole = UserRole.STUDENT
            ) {
                ReportScreen(
                    studentId = currentUser?.id,
                    onNavigateToAssignmentReport = { assignmentTitle ->
                        navController.navigate(VoiceTutorScreens.AssignmentDetailedResults.createRoute(assignmentTitle))
                    }
                )
            }
        }
        
        composable(
            route = VoiceTutorScreens.CompletedAssignments.route,
            arguments = listOf(
                navArgument("studentId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getInt("studentId")
            MainLayout(
                navController = navController,
                userRole = UserRole.STUDENT
            ) {
                CompletedAssignmentsScreen(
                    studentId = studentId,
                    onNavigateToAssignmentDetail = { assignmentId ->
                        navController.navigate(VoiceTutorScreens.AssignmentDetail.createRoute(assignmentId.toString(), "과제"))
                    },
                    onNavigateToAssignmentReport = { assignmentTitle ->
                        navController.navigate(VoiceTutorScreens.AssignmentDetailedResults.createRoute(assignmentTitle))
                    }
                )
            }
        }
        
        composable(
            route = VoiceTutorScreens.AllStudentAssignments.route,
            arguments = listOf(
                navArgument("studentId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getInt("studentId")
            if (studentId != null) {
                MainLayout(
                    navController = navController,
                    userRole = UserRole.STUDENT
                ) {
                    AllStudentAssignmentsScreen(
                        studentId = studentId,
                        onNavigateToAssignmentDetail = { assignmentId ->
                            navController.navigate(VoiceTutorScreens.AssignmentDetail.createRoute(assignmentId, "과제"))
                        },
                        onNavigateToAssignment = { assignmentId ->
                            navController.navigate(VoiceTutorScreens.Assignment.createRoute(assignmentId, "과제"))
                        },
                        onNavigateToAssignmentReport = { assignmentTitle ->
                            navController.navigate(VoiceTutorScreens.AssignmentDetailedResults.createRoute(assignmentTitle))
                        }
                    )
                }
            }
        }
        
        // Pending Assignments Screen (해야 할 과제)
        composable(
            route = VoiceTutorScreens.PendingAssignments.route,
            arguments = listOf(
                navArgument("studentId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getInt("studentId")
            if (studentId != null) {
                MainLayout(
                    navController = navController,
                    userRole = UserRole.STUDENT
                ) {
                    val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
                    PendingAssignmentsScreen(
                        studentId = studentId,
                        onNavigateToAssignment = { assignmentId ->
                            // assignmentId는 Assignment ID가 아니라 Personal Assignment ID
                            // AssignmentScreen에서 Personal Assignment ID를 사용해야 함
                            navController.navigate(VoiceTutorScreens.Assignment.createRoute(assignmentId, "과제"))
                        },
                        onNavigateToAssignmentDetail = { assignmentId ->
                            // assignmentId는 Assignment ID
                            navController.navigate(VoiceTutorScreens.AssignmentDetail.createRoute(assignmentId, "과제"))
                        },
                        assignmentViewModel = assignmentViewModel
                    )
                }
            }
        }
        
        composable(
            route = VoiceTutorScreens.AssignmentDetailedResults.route,
            arguments = listOf(
                navArgument("title") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val assignmentTitle = backStackEntry.arguments?.getString("title") ?: "과제 결과"
            MainLayout(
                navController = navController,
                userRole = UserRole.STUDENT
            ) {
                AssignmentDetailedResultsScreen(
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
                    onNavigateToClassDetail = { className, classId ->
                        navController.navigate(VoiceTutorScreens.TeacherClassDetail.createRoute(className, classId))
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
            // Use graph-scoped ViewModels to get current user
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
            
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                AllAssignmentsScreen(
                    teacherId = currentUser?.id?.toString(),
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
            // Use graph-scoped ViewModels to get current user
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
            
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                AllStudentsScreen(
                    teacherId = currentUser?.id?.toString() ?: "1",
                    onNavigateToStudentDetail = { studentId ->
                        // 학생을 클릭하면 Settings 화면으로 이동하여 해당 학생 정보 표시
                        navController.navigate(VoiceTutorScreens.Settings.createRoute(studentId))
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
                        navController.navigate(VoiceTutorScreens.TeacherAssignmentDetail.createRoute(assignmentTitle))
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
                // Use graph-scoped ViewModel so selection persists across screens
                val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
                val selectedPersonalAssignmentId by assignmentViewModel.selectedPersonalAssignmentId.collectAsStateWithLifecycle()
                
                AssignmentDetailScreen(
                    assignmentId = assignmentId.toIntOrNull(),
                    assignmentTitle = assignmentTitle,
                    onStartAssignment = {
                        // Navigate to assignment execution screen with personal_assignment_id
                        val personalId = selectedPersonalAssignmentId?.toString() ?: "1"
                        println("AssignmentDetailScreen - Navigating to Assignment with personal_assignment_id: $personalId")
                        navController.navigate(VoiceTutorScreens.Assignment.createRoute(personalId, assignmentTitle))
                    },
                    assignmentViewModelParam = assignmentViewModel
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
                    onNavigateToStudentDetail = { studentId ->
                        // 특정 학생의 특정 과제 상세 결과 화면으로 이동
                        navController.navigate(VoiceTutorScreens.TeacherStudentAssignmentDetail.createRoute(studentId, assignmentTitle))
                    }
                )
            }
        }
        
        // Teacher student assignment detail screen
        composable(
            route = VoiceTutorScreens.TeacherStudentAssignmentDetail.route,
            arguments = listOf(
                navArgument("studentId") {
                    type = NavType.StringType
                },
                navArgument("assignmentTitle") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: "1"
            val assignmentTitle = backStackEntry.arguments?.getString("assignmentTitle")?.replace("_", "/") ?: "과제"
            
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
            val unusedStudentName = backStackEntry.arguments?.getString("studentName") ?: "학생"
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
                },
                navArgument("classId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val className = backStackEntry.arguments?.getString("className") ?: "반"
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                TeacherClassDetailScreen(
                    classId = classId,
                    className = className,
                    onNavigateToClassMessage = {
                        navController.navigate(VoiceTutorScreens.ClassMessage.createRoute(className))
                    },
                    onNavigateToCreateAssignment = {
                        navController.navigate(VoiceTutorScreens.CreateAssignment.route)
                    },
                    onNavigateToAssignmentDetail = { assignmentTitle ->
                        navController.navigate(VoiceTutorScreens.EditAssignment.createRoute(assignmentTitle))
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
        
        // Settings (accessible from both roles)
        composable(
            route = VoiceTutorScreens.Settings.route,
            arguments = listOf(
                navArgument("studentId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getInt("studentId") ?: -1
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
            val userRole = currentUser?.role ?: (if (studentId != -1) UserRole.STUDENT else UserRole.STUDENT)
            
            MainLayout(
                navController = navController,
                userRole = userRole
            ) {
                SettingsScreen(
                    userRole = userRole,
                    studentId = if (studentId != -1) studentId else null,
                    onLogout = {
                        navController.navigate(VoiceTutorScreens.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    navController = navController
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
    object Settings : VoiceTutorScreens("settings/{studentId}") {
        fun createRoute(studentId: Int? = null) = if (studentId != null) "settings/$studentId" else "settings/-1"
    }
    
    // Student screens
    object Assignment : VoiceTutorScreens("assignment/{assignmentId}/{title}") {
        fun createRoute(assignmentId: String, title: String) = "assignment/$assignmentId/$title"
    }
    object AssignmentDetail : VoiceTutorScreens("assignment_detail/{id}/{title}") {
        fun createRoute(id: String, title: String) = "assignment_detail/$id/$title"
    }
    object AssignmentDetailedResults : VoiceTutorScreens("assignment_detailed_results/{title}") {
        fun createRoute(title: String) = "assignment_detailed_results/$title"
    }
    object Progress : VoiceTutorScreens("progress")
    object CompletedAssignments : VoiceTutorScreens("completed_assignments/{studentId}") {
        fun createRoute(studentId: Int) = "completed_assignments/$studentId"
    }
    object AllStudentAssignments : VoiceTutorScreens("all_student_assignments/{studentId}") {
        fun createRoute(studentId: Int) = "all_student_assignments/$studentId"
    }
    object PendingAssignments : VoiceTutorScreens("pending_assignments/{studentId}") {
        fun createRoute(studentId: Int) = "pending_assignments/$studentId"
    }
    object CreateClass : VoiceTutorScreens("create_class")
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
    object TeacherAssignmentDetail : VoiceTutorScreens("teacher_assignment_detail/{title}") {
        fun createRoute(title: String) = "teacher_assignment_detail/$title"
    }
    object TeacherStudentDetail : VoiceTutorScreens("teacher_student_detail/{name}") {
        fun createRoute(name: String) = "teacher_student_detail/$name"
    }
    object TeacherStudentAssignmentDetail : VoiceTutorScreens("teacher_student_assignment_detail/{studentId}/{assignmentTitle}") {
        fun createRoute(studentId: String, assignmentTitle: String) = "teacher_student_assignment_detail/$studentId/${assignmentTitle.replace("/", "_")}"
    }
    object TeacherMessage : VoiceTutorScreens("teacher_message/{studentName}") {
        fun createRoute(studentName: String) = "teacher_message/$studentName"
    }
    object TeacherClassDetail : VoiceTutorScreens("teacher_class_detail/{className}/{classId}") {
        fun createRoute(className: String, classId: Int) = "teacher_class_detail/$className/$classId"
    }
    object ClassMessage : VoiceTutorScreens("class_message/{className}") {
        fun createRoute(className: String) = "class_message/$className"
    }
    object AttendanceManagement : VoiceTutorScreens("attendance_management/{classId}") {
        fun createRoute(classId: Int) = "attendance_management/$classId"
    }
}
