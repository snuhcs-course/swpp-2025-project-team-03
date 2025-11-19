package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.models.AssignmentFilter
import com.example.voicetutor.data.models.AssignmentStatus
import com.example.voicetutor.data.models.TeacherOnboardingData
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.utils.TutorialPreferences

@Composable
fun TeacherDashboardScreen(
    authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel? = null,
    assignmentViewModel: AssignmentViewModel? = null,
    teacherId: String? = null, // 실제 사용자 ID 사용
    refreshTimestamp: Long = 0L, // 새로고침 트리거
    onNavigateToAllAssignments: () -> Unit = {},
    onNavigateToAllStudents: () -> Unit = {},
    onNavigateToClasses: () -> Unit = {},
    onCreateNewAssignment: () -> Unit = {},
    onNavigateToCreateClass: () -> Unit = {},
    onNavigateToAssignmentDetail: (Int) -> Unit = {},
    onNavigateToAssignmentResults: (Int) -> Unit = {},
    onNavigateToEditAssignment: (Int) -> Unit = {},
) {
    val actualAssignmentViewModel: AssignmentViewModel = assignmentViewModel ?: hiltViewModel()
    val actualAuthViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = authViewModel ?: hiltViewModel()
    val dashboardViewModel: com.example.voicetutor.ui.viewmodel.DashboardViewModel = hiltViewModel()
    val studentViewModel: com.example.voicetutor.ui.viewmodel.StudentViewModel = hiltViewModel()

    val assignments by actualAssignmentViewModel.assignments.collectAsStateWithLifecycle()
    val isLoading by actualAssignmentViewModel.isLoading.collectAsStateWithLifecycle()
    val error by actualAssignmentViewModel.error.collectAsStateWithLifecycle()
    val currentUser by actualAuthViewModel.currentUser.collectAsStateWithLifecycle()
    val dashboardStats by dashboardViewModel.dashboardStats.collectAsStateWithLifecycle()
    val students by studentViewModel.students.collectAsStateWithLifecycle()
    // 질문 생성 완료 상태 확인
    val questionGenerationSuccess by actualAssignmentViewModel.questionGenerationSuccess.collectAsStateWithLifecycle()
    // Recent activities are not supported by current backend API

    var selectedFilter by remember { mutableStateOf(AssignmentFilter.ALL) }

    // 튜토리얼 상태 관리
    val context = LocalContext.current
    val tutorialPrefs = remember { TutorialPreferences(context) }
    var showTutorial by remember { mutableStateOf(false) }

    // 회원가입 시 또는 설정에서 초기화 후 로그인 시에만 표시
    // currentUser와 showTutorial을 모두 key로 사용하여 초기화 후에도 재확인
    LaunchedEffect(currentUser, showTutorial) {
        if (currentUser != null && !showTutorial) {
            val isNewUser = tutorialPrefs.isNewUser()

            // 회원가입 시 또는 설정에서 초기화 후 로그인 시에만 표시
            if (isNewUser) {
                showTutorial = true
            }
        }
    }

    // 화면이 다시 포커스될 때 튜토리얼 상태 재확인 (설정에서 초기화 후 돌아올 때)
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, currentUser) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && currentUser != null && !showTutorial) {
                val isNewUser = tutorialPrefs.isNewUser()
                if (isNewUser) {
                    showTutorial = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Compute actual teacher ID
    val actualTeacherId = teacherId ?: currentUser?.id?.toString()

    // Load assignments and dashboard data on first composition
    LaunchedEffect(Unit) {
        // ViewModel 초기화 완료 대기
        kotlinx.coroutines.delay(100)
    }

    // 강제 새로고침 처리 (과제 생성 후 등)
    LaunchedEffect(refreshTimestamp, actualTeacherId) {
        if (refreshTimestamp > 0L && actualTeacherId != null) {
            actualAssignmentViewModel.loadAllAssignments(teacherId = actualTeacherId)
            dashboardViewModel.loadDashboardData(actualTeacherId)
            studentViewModel.loadAllStudents(teacherId = actualTeacherId)
        }
    }

    LaunchedEffect(actualTeacherId) {
        if (actualTeacherId == null) {
            return@LaunchedEffect
        }

        // 항상 해당 선생님의 과제만 가져오도록 teacherId 필수로 전달
        // 로그인 시 받은 assignments는 무시하고 항상 API로 최신 데이터 가져오기
        actualAssignmentViewModel.loadAllAssignments(teacherId = actualTeacherId)
        dashboardViewModel.loadDashboardData(actualTeacherId)
        studentViewModel.loadAllStudents(teacherId = actualTeacherId)
    }

    LaunchedEffect(selectedFilter, actualTeacherId) {
        if (actualTeacherId != null) {
            val status = when (selectedFilter) {
                AssignmentFilter.ALL -> null
                AssignmentFilter.IN_PROGRESS -> AssignmentStatus.IN_PROGRESS
                AssignmentFilter.COMPLETED -> AssignmentStatus.COMPLETED
            }
            actualAssignmentViewModel.loadAllAssignments(teacherId = actualTeacherId, status = status)
        }
    }
    
    // 질문 생성 완료 시 홈 리스트 새로고침
    LaunchedEffect(questionGenerationSuccess) {
        if (questionGenerationSuccess && actualTeacherId != null) {
            println("TeacherDashboardScreen - 질문 생성 완료 감지, 리스트 새로고침")
            val status = when (selectedFilter) {
                AssignmentFilter.ALL -> null
                AssignmentFilter.IN_PROGRESS -> AssignmentStatus.IN_PROGRESS
                AssignmentFilter.COMPLETED -> AssignmentStatus.COMPLETED
            }
            actualAssignmentViewModel.loadAllAssignments(teacherId = actualTeacherId, status = status)
            dashboardViewModel.loadDashboardData(actualTeacherId)
            // 메시지가 표시된 후 상태 초기화는 MainLayout에서 처리
        }
    }

    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            actualAssignmentViewModel.clearError()
        }
    }

    // 온보딩 튜토리얼 (7단계)
    if (showTutorial) {
        OnboardingPager(
            pages = TeacherOnboardingData.teacherOnboardingPages,
            onComplete = {
                tutorialPrefs.setTeacherTutorialCompleted()
                tutorialPrefs.clearNewUserFlag()
                showTutorial = false
            },
            onSkip = {
                tutorialPrefs.setTeacherTutorialCompleted()
                tutorialPrefs.clearNewUserFlag()
                showTutorial = false
            },
        )
    }

    // 오늘 마감인 과제 개수 계산 (API 24 호환)
    val dueTodayCount = remember(assignments) {
        // API 26 미만에서는 java.time 일부 기능이 제한되므로 SimpleDateFormat 사용
        val todayStr = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            java.time.LocalDate.now().toString() // yyyy-MM-dd
        } else {
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        }
        assignments.count { a ->
            val due = a.dueAt
            // dueAt이 비어있지 않고 앞 10자리가 yyyy-MM-dd 형태로 오늘과 일치하면 카운트
            due.isNotBlank() && due.length >= 10 && due.substring(0, 10) == todayStr
        }
    }

    // 필터링된 과제 목록 (API에서 이미 필터링된 결과 사용)
    val filteredAssignments = assignments

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Welcome section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = PrimaryIndigo.copy(alpha = 0.08f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                )
                .padding(20.dp),
        ) {
            Column {
                Text(
                    text = currentUser?.welcomeMessage ?: "환영합니다!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentUser?.subMessage ?: "수업을 관리하고 학생들의 진도를 추적하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                )
            }
        }

        // Quick stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DashboardSummaryCard(
                label = "수업",
                value = dashboardStats?.totalClasses?.toString()
                    ?: assignments.map { it.courseClass.id }.distinct().size.toString(),
                icon = Icons.Filled.List,
                tint = PrimaryIndigo,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToClasses,
            )

            DashboardSummaryCard(
                label = "학생",
                value = dashboardStats?.totalStudents?.toString()
                    ?: (
                        if (students.isNotEmpty()) {
                            students.size.toString()
                        } else {
                            currentUser?.totalStudents?.toString() ?: "0"
                        }
                        ),
                icon = Icons.Filled.People,
                tint = Success,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToAllStudents,
            )
        }

        // Quick actions
        Column {
            Text(
                text = "빠른 실행",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray800,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                VTButton(
                    text = "+ 수업 생성하기",
                    onClick = onNavigateToCreateClass,
                    variant = ButtonVariant.Primary,
                    size = ButtonSize.Medium,
                    modifier = Modifier.weight(1f),
                )

                VTButton(
                    text = "+ 과제 생성하기",
                    onClick = onCreateNewAssignment,
                    variant = ButtonVariant.Outline,
                    size = ButtonSize.Medium,
                    modifier = Modifier.weight(1f),
                )
            }

            // Spacer(modifier = Modifier.height(12.dp))

            // VTButton(
            //     text = "전체 과제 보기",
            //     onClick = onNavigateToAllAssignments,
            //     variant = ButtonVariant.Primary,
            //     size = ButtonSize.Large,
            //     modifier = Modifier
            //         .fillMaxWidth()
            //         .height(56.dp),
            //     leadingIcon = {
            //         Icon(
            //             imageVector = Icons.Filled.Assignment,
            //             contentDescription = null,
            //             modifier = Modifier.size(20.dp)
            //         )
            //     }
            // )
        }

        // Assignment management section
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "모든 과제",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = selectedFilter == AssignmentFilter.ALL,
                    onClick = { selectedFilter = AssignmentFilter.ALL },
                    label = { Text("전체") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.List,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )

                FilterChip(
                    selected = selectedFilter == AssignmentFilter.IN_PROGRESS,
                    onClick = { selectedFilter = AssignmentFilter.IN_PROGRESS },
                    label = { Text("진행중") },
                )

                FilterChip(
                    selected = selectedFilter == AssignmentFilter.COMPLETED,
                    onClick = { selectedFilter = AssignmentFilter.COMPLETED },
                    label = { Text("마감") },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = PrimaryIndigo,
                    )
                }
            } else if (assignments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Assignment,
                            contentDescription = null,
                            tint = Gray400,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "과제가 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600,
                        )
                    }
                }
            } else {
                val assignmentStatsMap = remember { mutableStateMapOf<Int, Pair<Int, Int>>() }

                assignments.forEach { assignment ->
                    LaunchedEffect(assignment.id) {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            val stats = actualAssignmentViewModel.getAssignmentSubmissionStats(assignment.id)
                            assignmentStatsMap[assignment.id] = stats.submittedStudents to stats.totalStudents
                        }
                    }
                }

                assignments.forEachIndexed { index, assignment ->
                    val stats = assignmentStatsMap[assignment.id] ?: (0 to assignment.courseClass.studentCount)

                    TeacherAssignmentCard(
                        title = assignment.title,
                        className = assignment.courseClass.name,
                        submittedCount = stats.first,
                        totalCount = stats.second,
                        dueDate = assignment.dueAt,
                        status = AssignmentStatus.IN_PROGRESS,
                        onClick = { onNavigateToAssignmentDetail(assignment.id) },
                        onViewResults = { onNavigateToAssignmentResults(assignment.id) },
                        onEdit = { onNavigateToEditAssignment(assignment.id) },
                    )

                    if (index < assignments.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherDashboardScreenPreview() {
    VoiceTutorTheme {
        TeacherDashboardScreen()
    }
}

@Composable
private fun DashboardSummaryCard(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    VTCard2(
        modifier = modifier.height(60.dp),
        variant = CardVariant.Elevated,
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 25.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp),
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = tint,
                )
            }
        }
    }
}

@Composable
fun TeacherAssignmentCard(
    title: String,
    className: String,
    submittedCount: Int,
    totalCount: Int,
    dueDate: String,
    status: AssignmentStatus,
    onClick: () -> Unit = {},
    onViewResults: () -> Unit = {},
    onEdit: () -> Unit = {},
) {
    VTCard(
        variant = CardVariant.Elevated,
        onClick = onClick,
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = className,
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryIndigo,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .background(PrimaryIndigo.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = com.example.voicetutor.utils.formatDueDate(dueDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "제출 학생: $submittedCount/$totalCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600,
                    fontWeight = FontWeight.Medium,
                )

                Text(
                    text = "${if (totalCount > 0) (submittedCount.toFloat() / totalCount * 100).toInt() else 0}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            VTProgressBar(
                progress = if (totalCount > 0) submittedCount.toFloat() / totalCount else 0f,
                showPercentage = false,
                color = PrimaryIndigo,
                height = 6,
            )

//            Spacer(modifier = Modifier.height(12.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                VTButton(
//                    text = "과제 결과",
//                    onClick = onViewResults,
//                    variant = ButtonVariant.Primary,
//                    size = ButtonSize.Small,
//                    modifier = Modifier.weight(1f)
//                )
//
//                VTButton(
//                    text = "과제 편집",
//                    onClick = onEdit,
//                    variant = ButtonVariant.Outline,
//                    size = ButtonSize.Small,
//                    modifier = Modifier.weight(1f)
//                )
//            }
        }
    }
}
