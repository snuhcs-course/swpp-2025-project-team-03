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
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
            
            LaunchedEffect(isLoggedIn, currentUser) {
                val user = currentUser
                println("VoiceTutorNavigation - Signup screen: isLoggedIn=$isLoggedIn, currentUser=${user?.email}, role=${user?.role}, id=${user?.id}")
                if (isLoggedIn && user != null) {
                    println("VoiceTutorNavigation - ✅ Navigating to dashboard for role: ${user.role}")
                    when (user.role) {
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
                            println("VoiceTutorNavigation - ❌ User role is null!")
                        }
                    }
                } else {
                    println("VoiceTutorNavigation - ⚠️ Waiting for user data: isLoggedIn=$isLoggedIn, currentUser=${user?.email}")
                }
            }
            
            SignupScreen(
                authViewModel = authViewModel,
                onSignupSuccess = {
                    navController.popBackStack()
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }
        
        // Student screens with layout
        composable(VoiceTutorScreens.StudentDashboard.route) {
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            val dashboardViewModel: com.example.voicetutor.ui.viewmodel.DashboardViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
            LaunchedEffect(currentUser) {
                println("VoiceTutorNavigation - StudentDashboard composable: currentUser=${currentUser?.email}, id=${currentUser?.id}, role=${currentUser?.role}")
            }
            
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
                        navController.navigate(VoiceTutorScreens.Progress.route)
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
                    onNavigateToAssignmentReport = { personalAssignmentId: Int, assignmentTitle: String ->
                        navController.navigate(VoiceTutorScreens.AssignmentDetailedResults.createRoute(personalAssignmentId, assignmentTitle))
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
                        onNavigateToAssignmentReport = { personalAssignmentId: Int, assignmentTitle: String ->
                            navController.navigate(VoiceTutorScreens.AssignmentDetailedResults.createRoute(personalAssignmentId, assignmentTitle))
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
                navArgument("personalAssignmentId") {
                    type = NavType.IntType
                },
                navArgument("title") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val personalAssignmentId = backStackEntry.arguments?.getInt("personalAssignmentId") ?: 0
            val assignmentTitle = backStackEntry.arguments?.getString("title") ?: "과제 결과"
            MainLayout(
                navController = navController,
                userRole = UserRole.STUDENT
            ) {
                AssignmentDetailedResultsScreen(
                    personalAssignmentId = personalAssignmentId,
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
                        navController.navigate(VoiceTutorScreens.CreateAssignment.createRoute(null))
                    },
                    onNavigateToCreateClass = {
                        navController.navigate(VoiceTutorScreens.CreateClass.route)
                    },
                    onNavigateToAssignmentDetail = { assignmentId ->
                        navController.navigate(VoiceTutorScreens.TeacherAssignmentDetail.createRoute(assignmentId))
                    },
                    onNavigateToAssignmentResults = { assignmentId ->
                        navController.navigate(VoiceTutorScreens.TeacherAssignmentResults.createRoute(assignmentId))
                    },
                    onNavigateToEditAssignment = { assignmentId ->
                        navController.navigate(VoiceTutorScreens.EditAssignment.createRoute(assignmentId))
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
                    onNavigateToCreateAssignment = { classId ->
                        navController.navigate(VoiceTutorScreens.CreateAssignment.createRoute(classId))
                    },
                    onNavigateToStudents = { classId ->
                        navController.navigate(VoiceTutorScreens.TeacherStudents.createRoute(classId.toString()))
                    }
                )
            }
        }
        
        composable(
            route = VoiceTutorScreens.TeacherStudents.route,
            arguments = listOf(
                navArgument("classId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getString("classId")?.toIntOrNull()
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
            
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                TeacherStudentsScreen(
                    classId = classId,
                    teacherId = currentUser?.id?.toString(),
                    onNavigateToStudentDetail = { studentId ->
                        navController.navigate(VoiceTutorScreens.TeacherStudentDetail.createRoute(studentId))
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
                    onNavigateToAssignmentResults = { assignmentId ->
                        navController.navigate(VoiceTutorScreens.TeacherAssignmentResults.createRoute(assignmentId))
                    },
                    onNavigateToEditAssignment = { assignmentId ->
                        navController.navigate(VoiceTutorScreens.EditAssignment.createRoute(assignmentId))
                    },
                    onNavigateToAssignmentDetail = { assignmentId ->
                        navController.navigate(VoiceTutorScreens.TeacherAssignmentDetail.createRoute(assignmentId))
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
                    onNavigateToStudentDetail = { classId, studentId, studentName ->
                        // 리포트 버튼 클릭 시 리포트로 이동 (선택된 반의 클래스 ID 전달)
                        navController.navigate(VoiceTutorScreens.TeacherStudentReport.createRoute(classId, studentId, studentName))
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
                        // assignmentId is the personal_assignment_id passed from navigation
                        val personalId = assignmentId.toIntOrNull()?.toString() ?: selectedPersonalAssignmentId?.toString() ?: "1"
                        println("AssignmentDetailScreen - Navigating to Assignment with personal_assignment_id: $personalId")
                        println("AssignmentDetailScreen - assignmentId from navigation: $assignmentId")
                        println("AssignmentDetailScreen - selectedPersonalAssignmentId from ViewModel: $selectedPersonalAssignmentId")
                        navController.navigate(VoiceTutorScreens.Assignment.createRoute(personalId, assignmentTitle))
                    },
                    assignmentViewModelParam = assignmentViewModel
                )
            }
        }
        
        // Create assignment screen
        composable(
            route = VoiceTutorScreens.CreateAssignment.route,
            arguments = listOf(
                navArgument("classId") {
                    type = NavType.IntType
                    defaultValue = 0
                }
            )
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0
            val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                CreateAssignmentScreen(
                    authViewModel = authViewModel,
                    assignmentViewModel = assignmentViewModel,
                    initialClassId = if (classId > 0) classId else null,
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
                navArgument("assignment_id") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val assignmentId = backStackEntry.arguments?.getInt("assignment_id") ?: 0
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                EditAssignmentScreen(
                    assignmentViewModel = assignmentViewModel,
                    assignmentId = assignmentId,
                    onSaveAssignment = {
                        // Save edited assignment and navigate back
                        println("Saving edited assignment: $assignmentId")
                        navController.popBackStack()
                    }
                )
            }
        }
        
        composable(
            route = VoiceTutorScreens.TeacherAssignmentResults.route,
            arguments = listOf(
                navArgument("assignment_id") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val assignmentId = backStackEntry.arguments?.getInt("assignment_id") ?: 0
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                TeacherAssignmentResultsScreen(
                    assignmentViewModel = assignmentViewModel,
                    assignmentId = assignmentId
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
                navArgument("assignmentId") {
                    type = NavType.IntType
                    defaultValue = 0
                },
                navArgument("assignmentTitle") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: "1"
            val assignmentId = backStackEntry.arguments?.getInt("assignmentId") ?: 0
            val assignmentTitle = backStackEntry.arguments?.getString("assignmentTitle")?.replace("_", "/") ?: "과제"
            
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                TeacherStudentAssignmentDetailScreen(
                    studentId = studentId,
                    assignmentId = assignmentId,
                    assignmentTitle = assignmentTitle
                )
            }
        }
        
        composable(
            route = VoiceTutorScreens.TeacherAssignmentDetail.route,
            arguments = listOf(
                navArgument("id") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val assignmentId = backStackEntry.arguments?.getInt("id") ?: 0
            val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
            
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                TeacherAssignmentDetailScreen(
                    assignmentViewModel = assignmentViewModel,
                    assignmentId = assignmentId,
                    onNavigateToAssignmentResults = { assignmentId ->
                        navController.navigate(VoiceTutorScreens.TeacherAssignmentResults.createRoute(assignmentId))
                    },
                    onNavigateToEditAssignment = { assignmentId ->
                        navController.navigate(VoiceTutorScreens.EditAssignment.createRoute(assignmentId))
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
                    onNavigateToCreateAssignment = { classId ->
                        navController.navigate(VoiceTutorScreens.CreateAssignment.createRoute(classId))
                    },
                    onNavigateToAssignmentDetail = { assignmentId ->
                        navController.navigate(VoiceTutorScreens.TeacherAssignmentDetail.createRoute(assignmentId))
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
        
        
        // Teacher student report screen
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
                }
            )
        ) { backStackEntry ->
            val classId = backStackEntry.arguments?.getInt("classId") ?: 0
            val studentId = backStackEntry.arguments?.getInt("studentId") ?: 0
            val studentName = backStackEntry.arguments?.getString("studentName")?.replace("_", " ") ?: "학생"
            
            MainLayout(
                navController = navController,
                userRole = UserRole.TEACHER
            ) {
                TeacherStudentReportScreen(
                    classId = classId,
                    studentId = studentId,
                    studentName = studentName,
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
    object AssignmentDetailedResults : VoiceTutorScreens("assignment_detailed_results/{personalAssignmentId}/{title}") {
        fun createRoute(personalAssignmentId: Int, title: String) = "assignment_detailed_results/$personalAssignmentId/$title"
    }
    object Progress : VoiceTutorScreens("progress")
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
