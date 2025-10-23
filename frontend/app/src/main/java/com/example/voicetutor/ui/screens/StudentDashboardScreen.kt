package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// 날짜 포맷 유틸 함수
private fun formatDueDate(dueDate: String): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(dueDate)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        zonedDateTime.format(formatter)
    } catch (e: Exception) {
        dueDate
    }
}

@Composable
fun StudentDashboardScreen(
    authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel? = null,
    assignmentViewModel: AssignmentViewModel? = null,
    dashboardViewModel: com.example.voicetutor.ui.viewmodel.DashboardViewModel? = null,
    studentId: Int? = null, // 실제 사용자 ID 사용
    onNavigateToQuiz: () -> Unit = {},
    onNavigateToAllAssignments: () -> Unit = {},
    onNavigateToProgressReport: () -> Unit = {},
    onNavigateToAssignmentDetail: (String) -> Unit = {},
    onNavigateToSubjectDetail: (String) -> Unit = {},
    onNavigateToAllDeadlines: () -> Unit = {}
) {
    val viewModelAssignment = assignmentViewModel ?: hiltViewModel()
    val viewModelAuth = authViewModel ?: hiltViewModel()
    val viewModelDashboard = dashboardViewModel ?: hiltViewModel()
    
    val assignments by viewModelAssignment.assignments.collectAsStateWithLifecycle()
    val isLoading by viewModelAssignment.isLoading.collectAsStateWithLifecycle()
    val error by viewModelAssignment.error.collectAsStateWithLifecycle()
    val currentUser by viewModelAuth.currentUser.collectAsStateWithLifecycle()
    val dashboardStats by viewModelDashboard.dashboardStats.collectAsStateWithLifecycle()
    
    // 동적 사용자 이름 가져오기
    val studentName = currentUser?.name ?: "학생"
    
    // 디버깅 로그
    LaunchedEffect(assignments) {
        println("StudentDashboard - currentUser: ${currentUser?.email}")
        println("StudentDashboard - assignments from ViewModel: ${assignments.size}")
        assignments.forEach { 
            println("  - ${it.title}")
        }
    }
    
    // Load student assignments and dashboard data on first composition
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            // 학생 ID를 사용하여 학생 전용 과제 API 호출
            println("StudentDashboard - Loading assignments for student ID: ${user.id}")
            viewModelAssignment.loadStudentAssignments(user.id)
        }
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            viewModelAssignment.clearError()
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
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
                            text = currentUser?.welcomeMessage ?: "안녕하세요, ${studentName}님!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentUser?.subMessage ?: "오늘도 VoiceTutor와 함께 학습을 시작해볼까요?",
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
                            text = currentUser?.initial ?: "장",
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        item {
            // My assignments
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "나에게 할당된 과제",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                        Text(
                            text = "진행 중인 과제와 새로 배정된 과제를 확인해보세요",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                    
                    VTButton(
                        text = "전체 보기",
                        onClick = onNavigateToAllAssignments,
                        variant = ButtonVariant.Ghost,
                        size = ButtonSize.Small
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Student assignments from API
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryIndigo
                        )
                    }
                } else if (assignments.isEmpty()) {
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
                    assignments.forEachIndexed { index, assignment ->
                        StudentAssignmentCard(
                            title = assignment.title,
                            subject = assignment.subject,
                            dueDate = formatDueDate(assignment.dueDate),
                            progress = 0.3f, // 임시로 진행률 설정
                            isUrgent = assignment.dueDate.contains("오늘") || assignment.dueDate.contains("내일"),
                            onClick = { onNavigateToAssignmentDetail(assignment.title) }
                        )
                        
                        if (index < assignments.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
        
        item {
            // Quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VTCard(
                    variant = CardVariant.Elevated,
                    onClick = onNavigateToQuiz,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Quiz,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "퀴즈 풀기",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                        Text(
                            text = "복습 퀴즈",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                }
                
                VTCard(
                    variant = CardVariant.Elevated,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lightbulb,
                            contentDescription = null,
                            tint = Warning,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "학습 도우미",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                        Text(
                            text = "AI 학습 지원",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                }
            }
        }
        
        item {
            // Progress overview
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "완료 현황",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                        Text(
                            text = "이번 주 과제 완료 현황을 확인해보세요",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                    
                    VTButton(
                        text = "전체 보기",
                        onClick = onNavigateToProgressReport,
                        variant = ButtonVariant.Ghost,
                        size = ButtonSize.Small
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VTStatsCard(
                        title = "총 과제",
                        value = dashboardStats?.totalAssignments?.toString() ?: "0",
                        icon = Icons.Filled.List,
                        iconColor = PrimaryIndigo,
                        trend = TrendDirection.Up,
                        trendValue = "+${dashboardStats?.inProgressAssignments ?: 0}",
                        modifier = Modifier.weight(1f)
                    )
                    
                    VTStatsCard(
                        title = "완료율",
                        value = dashboardStats?.let { stats ->
                            if (stats.totalAssignments > 0) {
                                "${(stats.completedAssignments * 100 / stats.totalAssignments)}%"
                            } else "0%"
                        } ?: "0%",
                        icon = Icons.Filled.Done,
                        iconColor = Success,
                        trend = TrendDirection.Up,
                        trendValue = "+${dashboardStats?.completedAssignments ?: 0}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        item {
            // Upcoming deadlines
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "다가오는 마감일",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                        Text(
                            text = "놓치지 마세요!",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                    
                    VTButton(
                        text = "전체 보기",
                        onClick = onNavigateToAllDeadlines,
                        variant = ButtonVariant.Ghost,
                        size = ButtonSize.Small
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 동적 과제 목록 표시 (실제 데이터에서 가져오기)
                assignments.take(2).forEachIndexed { index, assignment ->
                    StudentDeadlineItem(
                        subject = assignment.subject,
                        assignmentName = assignment.title,
                        dueDate = formatDueDate(assignment.dueDate ?: "마감일 없음"),
                        progress = if (assignment.totalCount > 0) assignment.submittedCount.toFloat() / assignment.totalCount else 0f,
                        onClick = { onNavigateToSubjectDetail(assignment.subject) }
                    )
                    if (index < assignments.take(2).size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                // 과제가 없을 때 기본 표시
                if (assignments.isEmpty()) {
                    repeat(2) { index ->
                        val subject = when (index) {
                            0 -> "수학"
                            else -> "영어"
                        }
                        StudentDeadlineItem(
                            subject = subject,
                            assignmentName = when (index) {
                                0 -> "미적분 개념 정리"
                                else -> "영문법 퀴즈"
                            },
                            dueDate = when (index) {
                                0 -> "내일 18:00"
                                else -> "모레 12:00"
                            },
                            progress = when (index) {
                                0 -> 0.7f
                                else -> 0.4f
                            },
                            onClick = { onNavigateToSubjectDetail(subject) }
                        )
                        if (index < 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentAssignmentCard(
    title: String,
    subject: String,
    dueDate: String,
    progress: Float,
    isUrgent: Boolean = false,
    onClick: () -> Unit = {}
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
                        text = subject,
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
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .background(Warning.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = dueDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Warning,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            VTProgressBar(
                progress = progress,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "${(progress * 100).toInt()}% 완료",
                style = MaterialTheme.typography.bodySmall,
                color = Gray600
            )
        }
    }
}

@Composable
fun StudentDeadlineItem(
    subject: String,
    assignmentName: String,
    dueDate: String,
    progress: Float,
    onClick: () -> Unit
) {
    VTCard(
        variant = CardVariant.Elevated,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                        .background(PrimaryIndigo.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subject.first().toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = assignmentName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    Text(
                        text = dueDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = Warning,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Column(
                horizontalAlignment = Alignment.End
            ) {
                VTProgressBar(
                    progress = progress,
                    modifier = Modifier.width(60.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudentDashboardScreenPreview() {
    VoiceTutorTheme {
        StudentDashboardScreen()
    }
}