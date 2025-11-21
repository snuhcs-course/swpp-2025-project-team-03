package com.example.voicetutor.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.ui.components.ButtonSize
import com.example.voicetutor.ui.components.ButtonVariant
import com.example.voicetutor.ui.components.VTButton
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.viewmodel.MainLayoutViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class RecentAssignment(
    val id: String, // personal_assignment_id
    val title: String, // 과제 제목
    val assignmentId: Int, // assignment_id
)

// Helper function to get page title based on current destination
fun getPageTitle(currentDestination: String?, userRole: UserRole): String {
    return when {
        currentDestination == VoiceTutorScreens.Assignment.route -> "과제"
        currentDestination?.startsWith(VoiceTutorScreens.AssignmentDetail.route.split("{").first()) == true -> "과제 상세"
        currentDestination == VoiceTutorScreens.AssignmentDetailedResults.route -> "리포트"
        currentDestination == VoiceTutorScreens.Progress.route -> "학습 리포트"
        currentDestination == VoiceTutorScreens.TeacherClasses.route -> "수업 관리"
        currentDestination == VoiceTutorScreens.CreateClass.route -> "수업 생성"
        currentDestination == VoiceTutorScreens.TeacherStudents.route -> "학생 관리"
        currentDestination == VoiceTutorScreens.AllAssignments.route -> "전체 과제"
        currentDestination == VoiceTutorScreens.AllStudents.route -> "전체 학생"
        currentDestination?.startsWith("create_assignment") == true -> "과제 생성"
        currentDestination?.startsWith(VoiceTutorScreens.EditAssignment.route.split("{").first()) == true -> "과제 편집"
        currentDestination?.startsWith(VoiceTutorScreens.TeacherAssignmentResults.route.split("{").first()) == true -> "과제 결과"
        currentDestination?.startsWith(VoiceTutorScreens.TeacherAssignmentDetail.route.split("{").first()) == true -> "과제 상세"
        currentDestination?.startsWith(VoiceTutorScreens.TeacherStudentAssignmentDetail.route.split("{").first()) == true -> "과제 결과"
        currentDestination?.startsWith(VoiceTutorScreens.TeacherStudentReport.route.split("{").first()) == true -> "리포트"
        currentDestination?.startsWith(VoiceTutorScreens.TeacherClassDetail.route.split("{").first()) == true -> "수업 관리"
        currentDestination == VoiceTutorScreens.Settings.route -> "계정"
        else -> if (userRole == UserRole.TEACHER) "선생님 페이지" else "학생 페이지"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainLayout(
    navController: NavHostController,
    userRole: UserRole,
    content: @Composable () -> Unit,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination?.route
    val baseDestination = currentDestination?.split("?")?.first()

    val graphEntry = remember(navController) {
        navController.getBackStackEntry(navController.graph.id)
    }
    val mainLayoutViewModel: MainLayoutViewModel = hiltViewModel(graphEntry)
    val lastTeacherBaseRoute by mainLayoutViewModel.lastTeacherBaseRoute.collectAsStateWithLifecycle()

    val teacherBaseRoute = when (baseDestination) {
        VoiceTutorScreens.TeacherDashboard.route -> "teacher_dashboard"
        VoiceTutorScreens.TeacherClasses.route -> "teacher_classes"
        VoiceTutorScreens.AllStudents.route -> "teacher_students"
        null -> null
        else -> null
    }

    LaunchedEffect(userRole) {
        if (userRole != UserRole.TEACHER) {
            mainLayoutViewModel.resetTeacherBaseRoute()
        }
    }

    LaunchedEffect(userRole, teacherBaseRoute) {
        if (userRole == UserRole.TEACHER && teacherBaseRoute != null) {
            mainLayoutViewModel.updateTeacherBaseRoute(teacherBaseRoute)
        }
    }

    val studentRoute = when {
        currentDestination == VoiceTutorScreens.StudentDashboard.route -> "student_dashboard"
        currentDestination == VoiceTutorScreens.Assignment.route -> "assignment"
        currentDestination?.startsWith(VoiceTutorScreens.AssignmentDetail.route) == true -> "assignment"
        currentDestination?.startsWith(VoiceTutorScreens.NoRecentAssignment.route.split("{").first()) == true -> "assignment"
        currentDestination == VoiceTutorScreens.Progress.route -> "progress"
        currentDestination == VoiceTutorScreens.AssignmentDetailedResults.route -> "progress"
        else -> "student_dashboard"
    }

    val currentRoute = if (userRole == UserRole.TEACHER) {
        lastTeacherBaseRoute
    } else {
        studentRoute
    }

    val assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel = hiltViewModel(graphEntry)
    val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel(graphEntry)
    val recentAssignmentState = assignmentViewModel.recentAssignment.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val recentAssignment = if (userRole == UserRole.STUDENT) recentAssignmentState.value else null

    // Get question generation status for floating progress indicator
    val isGeneratingQuestions by assignmentViewModel.isGeneratingQuestions.collectAsStateWithLifecycle()
    val generatingAssignmentTitle by assignmentViewModel.generatingAssignmentTitle.collectAsStateWithLifecycle()
    val questionGenerationSuccess by assignmentViewModel.questionGenerationSuccess.collectAsStateWithLifecycle()
    val questionGenerationCancelled by assignmentViewModel.questionGenerationCancelled.collectAsStateWithLifecycle()

    var showAssignmentCreatedToast by remember { mutableStateOf(false) }
    var showCancelledToast by remember { mutableStateOf(false) }
    
    var hasShownSuccessMessage by remember { mutableStateOf(false) }
    var hasShownCancelledMessage by remember { mutableStateOf(false) }
    
    var previousIsGeneratingQuestions by remember { mutableStateOf(false) }
    
    LaunchedEffect(isGeneratingQuestions) {
        if (!isGeneratingQuestions && previousIsGeneratingQuestions) {
            if (questionGenerationSuccess && !hasShownSuccessMessage) {
                hasShownSuccessMessage = true
                showAssignmentCreatedToast = true
                delay(5000)
                showAssignmentCreatedToast = false
                assignmentViewModel.clearQuestionGenerationStatus()
                delay(1000)
                hasShownSuccessMessage = false
            }
        }
        previousIsGeneratingQuestions = isGeneratingQuestions
    }
    
    LaunchedEffect(questionGenerationSuccess) {
        if (questionGenerationSuccess && !hasShownSuccessMessage) {
            hasShownSuccessMessage = true
            showAssignmentCreatedToast = true
            delay(5000)
            showAssignmentCreatedToast = false
            assignmentViewModel.clearQuestionGenerationStatus()
            delay(1000)
            hasShownSuccessMessage = false
        }
    }
    
    LaunchedEffect(questionGenerationCancelled) {
        if (questionGenerationCancelled && !hasShownCancelledMessage) {
            hasShownCancelledMessage = true
            showCancelledToast = true
            delay(5000)
            showCancelledToast = false
            assignmentViewModel.clearQuestionGenerationStatus()
            delay(1000)
            hasShownCancelledMessage = false
        }
    }
    
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    DisposableEffect(lifecycleOwner, questionGenerationSuccess) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && questionGenerationSuccess && !hasShownSuccessMessage) {
                hasShownSuccessMessage = true
                showAssignmentCreatedToast = true
                scope.launch {
                    delay(5000)
                    showAssignmentCreatedToast = false
                    assignmentViewModel.clearQuestionGenerationStatus()
                    delay(1000)
                    hasShownSuccessMessage = false
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

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
    val isDashboard = baseDestination == VoiceTutorScreens.StudentDashboard.route ||
        baseDestination == VoiceTutorScreens.Progress.route ||
        baseDestination == VoiceTutorScreens.TeacherDashboard.route ||
        baseDestination == VoiceTutorScreens.TeacherClasses.route ||
        baseDestination == VoiceTutorScreens.AllStudents.route ||
        baseDestination?.startsWith("assignment_detail") == true ||
        baseDestination?.startsWith("no_recent_assignment") == true

    val userName = currentUser?.name ?: "사용자"
    val userInitial = currentUser?.initial ?: "?"

    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Gray50,
                        Color(0xFFF0F4FF), // blue-50/30
                        Color(0xFFF0F0FF), // indigo-50/50
                    ),
                ),
            ),
    ) {
        // Header
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (isDashboard) {
                        // Logo (for dashboard pages)
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(PrimaryIndigo, PrimaryPurple),
                                    ),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "V",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = "VoiceTutor",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo,
                        )
                    } else {
                        // Back button (for non-dashboard pages)
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            },
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "뒤로가기",
                                tint = PrimaryIndigo,
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = getPageTitle(currentDestination, userRole),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo,
                        )
                    }
                }
            },
            actions = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // User info
                    Column(
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Gray800,
                        )
                        Text(
                            text = when (currentUser?.role) {
                                UserRole.TEACHER -> "선생님"
                                UserRole.STUDENT -> "학생"
                                null -> if (userRole == UserRole.TEACHER) "선생님" else "학생"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500,
                        )
                    }

                    // Profile button
                    IconButton(
                        onClick = {
                            navController.navigate(VoiceTutorScreens.Settings.createRoute())
                        },
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Gray100, Gray200),
                                    ),
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = userInitial,
                                color = Gray700,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    // Logout button
                    IconButton(
                        onClick = {
                            showLogoutDialog = true
                        },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "로그아웃",
                            tint = Gray500,
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White.copy(alpha = 0.95f),
            ),
        )

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White,
                tonalElevation = 0.dp,
                icon = {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(PrimaryIndigo, PrimaryPurple),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            tint = Color.White,
                        )
                    }
                },
                title = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "로그아웃",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryIndigo,
                        )
                    }
                },
                text = {
                    Text(
                        text = "로그아웃하시겠습니까?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                confirmButton = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        VTButton(
                            text = "취소",
                            onClick = { showLogoutDialog = false },
                            modifier = Modifier.weight(1f),
                            variant = ButtonVariant.Outline,
                            size = ButtonSize.Medium,
                        )
                        VTButton(
                            text = "로그아웃",
                            onClick = {
                                showLogoutDialog = false
                                authViewModel.logout()
                                navController.navigate(VoiceTutorScreens.Login.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            variant = ButtonVariant.Primary,
                            size = ButtonSize.Medium,
                        )
                    }
                },
            )
        }

        // Main content
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = 17.dp,
                    top = 17.dp,
                    end = 17.dp,
                    bottom = 4.dp,
                ),
        ) {
            content()
        }

        // Floating progress indicator for background question generation (above bottom navigation)
        if (isGeneratingQuestions && generatingAssignmentTitle != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Surface(
                    modifier = Modifier
                        .shadow(8.dp, RoundedCornerShape(12.dp))
                        .wrapContentWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    tonalElevation = 8.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .heightIn(min = 56.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "$generatingAssignmentTitle: 질문 생성 중...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray800,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        CircularProgressIndicator(
                            color = PrimaryIndigo,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(24.dp),
                        )
                        TextButton(
                            onClick = {
                                assignmentViewModel.cancelQuestionGeneration()
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        ) {
                            Text(
                                text = "생성 취소",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray600,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
                }
            }
        }

        // 과제 생성 성공 메시지 (질문 생성 완료 시)
        if (showAssignmentCreatedToast) {
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
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "과제 생성 완료",
                            tint = PrimaryIndigo
                        )
                        Text(
                            text = "과제가 성공적으로 생성되었습니다!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray800,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 300.dp)
                        )
                    }
                }
            }
        }
        
        // 취소 메시지
        if (showCancelledToast) {
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
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = "과제 생성 취소",
                            tint = Gray600
                        )
                        Text(
                            text = "과제 생성이 취소되었습니다!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray800,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 300.dp)
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
            currentUserId = currentUser?.id,
            assignmentViewModel = assignmentViewModel,
        )
    }
}

@Composable
fun BottomNavigation(
    navController: NavHostController,
    userRole: UserRole,
    currentRoute: String,
    recentAssignment: RecentAssignment? = null,
    currentUserId: Int? = null,
    assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel,
) {
    NavigationBar(
        containerColor = Color.White.copy(alpha = 0.95f),
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp)),
    ) {
        if (userRole == UserRole.TEACHER) {
            // Teacher navigation items
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "홈",
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
                    indicatorColor = LightIndigo,
                ),
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = "수업",
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
                    indicatorColor = LightIndigo,
                ),
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Assessment,
                        contentDescription = "리포트",
                    )
                },
                label = { Text("리포트") },
                selected = currentRoute == "teacher_students",
                onClick = {
                    navController.navigate(VoiceTutorScreens.AllStudents.route)
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryIndigo,
                    selectedTextColor = PrimaryIndigo,
                    indicatorColor = LightIndigo,
                ),
            )
        } else {
            // Student navigation items
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = "홈",
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
                    indicatorColor = LightIndigo,
                ),
            )

            // Recent assignment (always shows "이어하기")
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = "이어하기",
                    )
                },
                label = { Text("이어하기") },
                selected = currentRoute == "assignment",
                onClick = {
                    if (recentAssignment != null) {
                        // ViewModel에 두 ID를 모두 저장 (Dashboard와 동일하게)
                        assignmentViewModel.setSelectedAssignmentIds(
                            assignmentId = recentAssignment.assignmentId,
                            personalAssignmentId = recentAssignment.id.toIntOrNull(),
                        )
                        // 이어하기: 최근 과제 상세 화면으로 이동
                        navController.navigate(VoiceTutorScreens.AssignmentDetail.createRoute(recentAssignment.id, recentAssignment.title))
                    } else {
                        // 진행할 과제가 없는 경우: NoRecentAssignmentScreen으로 이동
                        currentUserId?.let { studentId ->
                            navController.navigate(VoiceTutorScreens.NoRecentAssignment.createRoute(studentId))
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryIndigo,
                    selectedTextColor = PrimaryIndigo,
                    indicatorColor = LightIndigo,
                ),
            )

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = Icons.Filled.Assessment,
                        contentDescription = "리포트",
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
                    indicatorColor = LightIndigo,
                ),
            )
        }
        }
    }
