package com.example.voicetutor.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.ui.theme.*

data class RecentAssignment(
    val id: String, // personal_assignment_id
    val title: String // 과제 제목
)

// Helper function to get page title based on current destination
fun getPageTitle(currentDestination: String?, userRole: UserRole): String {
    return when {
        currentDestination == VoiceTutorScreens.Assignment.route -> "과제"
        currentDestination?.startsWith(VoiceTutorScreens.AssignmentDetail.route.split("{").first()) == true -> "과제 상세"
        currentDestination == VoiceTutorScreens.AssignmentDetailedResults.route -> "과제 결과"
        currentDestination == VoiceTutorScreens.Progress.route -> "학습 리포트"
        currentDestination == VoiceTutorScreens.TeacherClasses.route -> "수업 관리"
        currentDestination == VoiceTutorScreens.CreateClass.route -> "수업 관리"
        currentDestination == VoiceTutorScreens.TeacherStudents.route -> "학생 관리"
        currentDestination == VoiceTutorScreens.AllAssignments.route -> "전체 과제"
        currentDestination == VoiceTutorScreens.AllStudents.route -> "전체 학생"
        currentDestination?.startsWith("create_assignment") == true -> "과제 생성"
        currentDestination?.startsWith(VoiceTutorScreens.EditAssignment.route.split("{").first()) == true -> "과제 편집"
        currentDestination?.startsWith(VoiceTutorScreens.TeacherAssignmentResults.route.split("{").first()) == true -> "과제 결과"
        currentDestination?.startsWith(VoiceTutorScreens.TeacherAssignmentDetail.route.split("{").first()) == true -> "과제 상세"
        currentDestination?.startsWith(VoiceTutorScreens.TeacherStudentDetail.route.split("{").first()) == true -> "학생 상세"
        currentDestination?.startsWith(VoiceTutorScreens.TeacherStudentAssignmentDetail.route.split("{").first()) == true -> "과제 결과"
        currentDestination?.startsWith(VoiceTutorScreens.TeacherStudentReport.route.split("{").first()) == true -> "리포트"
        currentDestination?.startsWith(VoiceTutorScreens.TeacherClassDetail.route.split("{").first()) == true -> "수업 관리"
        currentDestination == VoiceTutorScreens.Settings.route -> "설정"
        else -> if (userRole == UserRole.TEACHER) "선생님 페이지" else "학생 페이지"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    navController: NavHostController,
    userRole: UserRole,
    content: @Composable () -> Unit
) {
    val currentDestination = navController.currentDestination?.route
    val currentRoute = when {
        currentDestination == VoiceTutorScreens.StudentDashboard.route -> "student_dashboard"
        currentDestination == VoiceTutorScreens.TeacherDashboard.route -> "teacher_dashboard"
        currentDestination == VoiceTutorScreens.Assignment.route -> "assignment"
        currentDestination?.startsWith(VoiceTutorScreens.AssignmentDetail.route) == true -> "assignment" // 과제 상세 화면에서 이어하기/과제 탭 선택
        currentDestination?.startsWith(VoiceTutorScreens.PendingAssignments.route.split("{").first()) == true -> "assignment" // 해야 할 과제 목록에서 과제 탭 선택
        currentDestination == VoiceTutorScreens.Progress.route -> "progress"
        currentDestination == VoiceTutorScreens.AssignmentDetailedResults.route -> "progress" // 리포트 탭 유지
        currentDestination == VoiceTutorScreens.TeacherClasses.route -> "teacher_classes"
        currentDestination?.startsWith(VoiceTutorScreens.TeacherClassDetail.route.split("{").first()) == true -> "teacher_classes" // 수업 탭 선택
        currentDestination == VoiceTutorScreens.TeacherStudents.route -> "teacher_students"
        currentDestination == VoiceTutorScreens.AllStudents.route -> "teacher_students" // 전체 학생 페이지에서 학생 탭 선택
        currentDestination == VoiceTutorScreens.TeacherStudentDetail.route -> "teacher_students" // 학생 상세 페이지에서 학생 탭 선택
        else -> if (userRole == UserRole.TEACHER) "teacher_dashboard" else "student_dashboard"
    }
    
    // Get recent assignment data from API for students
    // Use graph-scoped ViewModels to share data between screens
    val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
    val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(navController.getBackStackEntry(navController.graph.id))
    val recentAssignmentState = assignmentViewModel.recentAssignment.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val recentAssignment = if (userRole == UserRole.STUDENT) recentAssignmentState.value else null
    
    // Get question generation status for floating progress indicator
    val isGeneratingQuestions by assignmentViewModel.isGeneratingQuestions.collectAsStateWithLifecycle()
    val generatingAssignmentTitle by assignmentViewModel.generatingAssignmentTitle.collectAsStateWithLifecycle()
    
    // Load recent assignment for students
    LaunchedEffect(userRole, currentUser?.id) {
        if (userRole == UserRole.STUDENT) {
            val userId = currentUser?.id
            if (userId != null) {
                assignmentViewModel.loadRecentAssignment(userId)
            }
        }
    }
    
    // Check if current page is a dashboard (should show logo) or other page (should show back button)
    // Remove query parameters for comparison (e.g., "teacher_dashboard?refresh=123" -> "teacher_dashboard")
    val baseDestination = currentDestination?.split("?")?.first()
    val isDashboard = baseDestination == VoiceTutorScreens.StudentDashboard.route ||
                     baseDestination == VoiceTutorScreens.Progress.route ||
                     baseDestination == VoiceTutorScreens.TeacherDashboard.route ||
                     baseDestination == VoiceTutorScreens.TeacherClasses.route ||
                     baseDestination == VoiceTutorScreens.AllStudents.route
    
    val userName = currentUser?.name ?: "사용자"
    val userInitial = currentUser?.initial ?: "?"
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Gray50,
                        Color(0xFFF0F4FF), // blue-50/30
                        Color(0xFFF0F0FF)  // indigo-50/50
                    )
                )
            )
    ) {
        // Header
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isDashboard) {
                        // Logo (for dashboard pages)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(PrimaryIndigo, PrimaryPurple)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "V",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = "VoiceTutor",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo
                        )
                    } else {
                        // Back button (for non-dashboard pages)
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로가기",
                                tint = PrimaryIndigo
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            text = getPageTitle(currentDestination, userRole),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo
                        )
                    }
                }
            },
            actions = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // User info
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Gray800
                        )
                        Text(
                            text = when (currentUser?.role) {
                                com.example.voicetutor.data.models.UserRole.TEACHER -> "선생님"
                                com.example.voicetutor.data.models.UserRole.STUDENT -> "학생"
                                null -> if (userRole == UserRole.TEACHER) "선생님" else "학생"
                                else -> if (userRole == UserRole.TEACHER) "선생님" else "학생"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                    }
                    
                    // Profile button
                    IconButton(
                        onClick = {
                            navController.navigate(VoiceTutorScreens.Settings.createRoute())
                        }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Gray100, Gray200)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = userInitial,
                                color = Gray700,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    // Logout button
                    IconButton(
                        onClick = {
                            authViewModel.logout()
                            navController.navigate(VoiceTutorScreens.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "로그아웃",
                            tint = Gray500
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            )
        )
        
        // Main content
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
        ) {
            content()
        }
        
        // Floating progress indicator for background question generation (above bottom navigation)
        if (isGeneratingQuestions && generatingAssignmentTitle != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Surface(
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .wrapContentWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "$generatingAssignmentTitle: 질문 생성 중...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray800,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 200.dp)
                        )
                        CircularProgressIndicator(
                            color = PrimaryIndigo,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
        
        // Bottom navigation
        BottomNavigation(
            navController = navController,
            userRole = userRole,
            currentRoute = currentRoute,
            recentAssignment = recentAssignment,
            currentUserId = currentUser?.id
        )
    }
}

@Composable
fun BottomNavigation(
    navController: NavHostController,
    userRole: UserRole,
    currentRoute: String,
    recentAssignment: RecentAssignment? = null,
    currentUserId: Int? = null
) {
    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.95f),
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        if (userRole == UserRole.TEACHER) {
            // Teacher navigation items
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "홈"
                    )
                },
                label = { Text("홈") },
                selected = currentRoute == "teacher_dashboard",
                onClick = {
                    navController.navigate(VoiceTutorScreens.TeacherDashboard.route) {
                        popUpTo(VoiceTutorScreens.TeacherDashboard.route) { inclusive = true }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryIndigo,
                    selectedTextColor = PrimaryIndigo,
                    indicatorColor = LightIndigo
                )
            )
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = "수업"
                    )
                },
                label = { Text("수업") },
                selected = currentRoute == "teacher_classes",
                onClick = {
                    navController.navigate(VoiceTutorScreens.TeacherClasses.route)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryIndigo,
                    selectedTextColor = PrimaryIndigo,
                    indicatorColor = LightIndigo
                )
            )
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.People,
                        contentDescription = "학생"
                    )
                },
                label = { Text("학생") },
                selected = currentRoute == "teacher_students",
                onClick = {
                    navController.navigate(VoiceTutorScreens.AllStudents.route)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryIndigo,
                    selectedTextColor = PrimaryIndigo,
                    indicatorColor = LightIndigo
                )
            )
        } else {
            // Student navigation items
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "홈"
                    )
                },
                label = { Text("홈") },
                selected = currentRoute == "student_dashboard",
                onClick = {
                    navController.navigate(VoiceTutorScreens.StudentDashboard.route) {
                        popUpTo(VoiceTutorScreens.StudentDashboard.route) { inclusive = true }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryIndigo,
                    selectedTextColor = PrimaryIndigo,
                    indicatorColor = LightIndigo
                )
            )
            
            // Recent assignment or regular assignment
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (recentAssignment != null) Icons.Filled.PlayArrow else Icons.Filled.Book,
                        contentDescription = if (recentAssignment != null) "이어하기" else "과제"
                    )
                },
                label = { Text(if (recentAssignment != null) "이어하기" else "과제") },
                selected = currentRoute == "assignment",
                onClick = {
                    if (recentAssignment != null) {
                        // 이어하기: 최근 과제 상세 화면으로 이동
                        navController.navigate(VoiceTutorScreens.AssignmentDetail.createRoute(recentAssignment.id, recentAssignment.title))
                    } else {
                        // 과제: 해야 할 과제 목록 화면으로 이동
                        currentUserId?.let { studentId ->
                            navController.navigate(VoiceTutorScreens.PendingAssignments.createRoute(studentId))
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryIndigo,
                    selectedTextColor = PrimaryIndigo,
                    indicatorColor = LightIndigo
                )
            )
            
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Assessment,
                        contentDescription = "리포트"
                    )
                },
                label = { Text("리포트") },
                selected = currentRoute == "progress",
                onClick = {
                    navController.navigate(VoiceTutorScreens.Progress.route)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryIndigo,
                    selectedTextColor = PrimaryIndigo,
                    indicatorColor = LightIndigo
                )
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MainLayoutPreview() {
    VoiceTutorTheme {
        MainLayout(
            navController = rememberNavController(),
            userRole = UserRole.STUDENT
        ) {
            Text("Preview Content")
        }
    }
}
