package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel

@Composable
fun TeacherDashboardScreen(
    authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel? = null,
    assignmentViewModel: AssignmentViewModel? = null,
    teacherId: String? = null, // 실제 사용자 ID 사용
    refreshTimestamp: Long = 0L, // 새로고침 트리거
    onNavigateToAllAssignments: () -> Unit = {},
    onNavigateToAllStudents: () -> Unit = {},
    onCreateNewAssignment: () -> Unit = {},
    onNavigateToAssignmentDetail: (Int) -> Unit = {},
    onNavigateToAssignmentResults: (Int) -> Unit = {},
    onNavigateToEditAssignment: (Int) -> Unit = {},
    onNavigateToStudentDetail: (String) -> Unit = {}
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
    // Recent activities are not supported by current backend API
    
    var selectedFilter by remember { mutableStateOf(AssignmentFilter.ALL) }
    
    // Compute actual teacher ID
    val actualTeacherId = teacherId ?: currentUser?.id?.toString()
    
    // Load assignments and dashboard data on first composition
    LaunchedEffect(Unit) {
        // ViewModel 초기화 완료 대기
        kotlinx.coroutines.delay(100)
    }
    
    LaunchedEffect(assignments.size, selectedFilter) {
        println("TeacherDashboard - Assignments changed: ${assignments.size}, filter: $selectedFilter")
        assignments.forEach { 
            println("  - ${it.title} (${it.courseClass.subject.name})")
        }
    }
    
    // 강제 새로고침 처리 (과제 생성 후 등)
    LaunchedEffect(refreshTimestamp, actualTeacherId) {
        println("TeacherDashboard - LaunchedEffect triggered with refreshTimestamp: $refreshTimestamp")
        println("TeacherDashboard - actualTeacherId: $actualTeacherId")
        
        if (refreshTimestamp > 0L && actualTeacherId != null) {
            println("TeacherDashboard - ✅ Force refreshing data (timestamp: $refreshTimestamp, teacherId: $actualTeacherId)")
            actualAssignmentViewModel.loadAllAssignments(teacherId = actualTeacherId)
            dashboardViewModel.loadDashboardData(actualTeacherId)
            studentViewModel.loadAllStudents(teacherId = actualTeacherId)
        } else {
            println("TeacherDashboard - ❌ Skipping refresh (timestamp: $refreshTimestamp, teacherId: $actualTeacherId)")
        }
    }
    
    LaunchedEffect(actualTeacherId) {
        if (actualTeacherId == null) {
            println("TeacherDashboard - ⚠️ Waiting for user to be loaded...")
            return@LaunchedEffect
        }
        
        println("TeacherDashboard - ✅ Initial loading data for teacher ID: $actualTeacherId")
        println("TeacherDashboard - Current user ID: ${currentUser?.id}, email: ${currentUser?.email}")
        
        // 항상 해당 선생님의 과제만 가져오도록 teacherId 필수로 전달
        // 로그인 시 받은 assignments는 무시하고 항상 API로 최신 데이터 가져오기
        if (selectedFilter == AssignmentFilter.ALL) {
            println("TeacherDashboard - Calling loadAllAssignments with teacherId=$actualTeacherId (required)")
            actualAssignmentViewModel.loadAllAssignments(teacherId = actualTeacherId)
            dashboardViewModel.loadDashboardData(actualTeacherId)
        }
        
        studentViewModel.loadAllStudents(teacherId = actualTeacherId)
    }
    
    LaunchedEffect(selectedFilter, actualTeacherId) {
        if (actualTeacherId == null) {
            println("TeacherDashboard - Filter change skipped: teacherId is null")
            return@LaunchedEffect
        }
        
        println("TeacherDashboard - Loading assignments with filter: $selectedFilter, teacherId: $actualTeacherId")
        
        val status = when (selectedFilter) {
            AssignmentFilter.ALL -> null
            AssignmentFilter.IN_PROGRESS -> AssignmentStatus.IN_PROGRESS
            AssignmentFilter.COMPLETED -> AssignmentStatus.COMPLETED
        }
        
        // teacherId 필수로 전달하여 해당 선생님의 과제만 가져오기
        actualAssignmentViewModel.loadAllAssignments(teacherId = actualTeacherId, status = status)
    }
    
    LaunchedEffect(assignments) {
        val user = currentUser
        println("TeacherDashboard - Assignments state updated: ${assignments.size} assignments")
        println("TeacherDashboard - Current user ID: ${user?.id}, email: ${user?.email}")
        assignments.forEach { 
            println("  - ${it.title} (${it.courseClass.subject.name}) - teacher: ${it.courseClass.teacherName}")
        }
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            actualAssignmentViewModel.clearError()
        }
    }
    
    // 필터링된 과제 목록 (API에서 이미 필터링된 결과 사용)
    val filteredAssignments = assignments
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = PrimaryIndigo,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                )
                .shadow(
                    elevation = 8.dp,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    ambientColor = PrimaryIndigo.copy(alpha = 0.3f),
                    spotColor = PrimaryIndigo.copy(alpha = 0.3f)
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = currentUser?.welcomeMessage ?: "환영합니다!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentUser?.subMessage ?: "수업을 관리하고 학생들의 진도를 추적하세요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .shadow(
                            elevation = 4.dp,
                            shape = androidx.compose.foundation.shape.CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.1f),
                            spotColor = Color.Black.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser?.initial ?: "김",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Quick stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VTStatsCard(
                title = "총 과제",
                value = dashboardStats?.totalAssignments?.toString() ?: (currentUser?.totalAssignments ?: assignments.size).toString(),
                icon = Icons.Filled.List,
                iconColor = PrimaryIndigo,
                variant = CardVariant.Elevated,
                trend = TrendDirection.None,
                trendValue = "",
                onClick = { onNavigateToAllAssignments() },
                modifier = Modifier.weight(1f),
                layout = StatsCardLayout.Horizontal
            )
            
            VTStatsCard(
                title = "총 학생",
                value = students.size.toString().takeIf { students.isNotEmpty() } 
                    ?: dashboardStats?.totalStudents?.toString() 
                    ?: currentUser?.totalStudents?.toString() 
                    ?: "0",
                icon = Icons.Filled.People,
                iconColor = Success,
                variant = CardVariant.Elevated,
                trend = TrendDirection.None,
                trendValue = "${dashboardStats?.totalClasses ?: currentUser?.totalClasses ?: 0}개 클래스",
                onClick = { onNavigateToAllStudents() },
                modifier = Modifier.weight(1f),
                layout = StatsCardLayout.Horizontal
            )
        }
        
        // Assignment management section
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "내가 낸 과제",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    Text(
                        text = "최근 생성한 과제들을 확인하세요",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
                
                VTButton(
                    text = "새 과제",
                    onClick = onCreateNewAssignment,
                    variant = ButtonVariant.Outline,
                    size = ButtonSize.Small,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Filter chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == AssignmentFilter.ALL,
                    onClick = { selectedFilter = AssignmentFilter.ALL },
                    label = { 
                        Text(
                            text = "전체",
                            fontWeight = if (selectedFilter == AssignmentFilter.ALL) FontWeight.SemiBold else FontWeight.Medium
                        ) 
                    },
                    leadingIcon = if (selectedFilter == AssignmentFilter.ALL) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        {
                            Icon(
                                imageVector = Icons.Filled.List,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                
                FilterChip(
                    selected = selectedFilter == AssignmentFilter.IN_PROGRESS,
                    onClick = { selectedFilter = AssignmentFilter.IN_PROGRESS },
                    label = { 
                        Text(
                            text = "진행중",
                            fontWeight = if (selectedFilter == AssignmentFilter.IN_PROGRESS) FontWeight.SemiBold else FontWeight.Medium
                        ) 
                    },
                    leadingIcon = if (selectedFilter == AssignmentFilter.IN_PROGRESS) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                
                FilterChip(
                    selected = selectedFilter == AssignmentFilter.COMPLETED,
                    onClick = { selectedFilter = AssignmentFilter.COMPLETED },
                    label = { 
                        Text(
                            text = "완료",
                            fontWeight = if (selectedFilter == AssignmentFilter.COMPLETED) FontWeight.SemiBold else FontWeight.Medium
                        ) 
                    },
                    leadingIcon = if (selectedFilter == AssignmentFilter.COMPLETED) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        {
                            Icon(
                                imageVector = Icons.Filled.Done,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Assignment cards
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PrimaryIndigo
                    )
                }
            } else if (filteredAssignments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Assignment,
                            contentDescription = null,
                            tint = Gray400,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "과제가 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600
                        )
                    }
                }
            } else {
                // 제출 현황을 저장하는 StateMap
                val assignmentStatsMap = remember { mutableStateMapOf<Int, Pair<Int, Int>>() }
                
                // 각 과제의 제출 현황을 로드
                filteredAssignments.forEach { assignment ->
                    LaunchedEffect(assignment.id) {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            val stats = actualAssignmentViewModel.getAssignmentSubmissionStats(assignment.id)
                            assignmentStatsMap[assignment.id] = stats.submittedStudents to stats.totalStudents
                        }
                    }
                }
                
                filteredAssignments.forEachIndexed { index, assignment ->
                    val stats = assignmentStatsMap[assignment.id] ?: (0 to assignment.courseClass.studentCount)
                    
                    TeacherAssignmentCard(
                        title = assignment.title,
                        className = assignment.courseClass.name,
                        submittedCount = stats.first,
                        totalCount = stats.second,
                        dueDate = assignment.dueAt,
                        status = AssignmentStatus.IN_PROGRESS, // 기본값으로 설정
                        onClick = { onNavigateToAssignmentDetail(assignment.id) },
                        onViewResults = { onNavigateToAssignmentResults(assignment.id) },
                        onEdit = { onNavigateToEditAssignment(assignment.id) }
                    )
                    
                    if (index < filteredAssignments.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
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
    onEdit: () -> Unit = {}
) {
    VTCard(
        variant = CardVariant.Elevated,
        onClick = onClick
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = className,
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryIndigo,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                }
                
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .background(PrimaryIndigo.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = dueDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "제출 현황: $submittedCount/$totalCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600,
                    fontWeight = FontWeight.Medium
                )
                
                Text(
                    text = "${if (totalCount > 0) (submittedCount.toFloat() / totalCount * 100).toInt() else 0}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            VTProgressBar(
                progress = if (totalCount > 0) submittedCount.toFloat() / totalCount else 0f,
                showPercentage = false,
                color = PrimaryIndigo,
                height = 6
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VTButton(
                    text = "결과 보기",
                    onClick = onViewResults,
                    variant = ButtonVariant.Outline,
                    size = ButtonSize.Small,
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Analytics,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
                
                VTButton(
                    text = "편집",
                    onClick = onEdit,
                    variant = ButtonVariant.Outline,
                    size = ButtonSize.Small,
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
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
