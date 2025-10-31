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
    onNavigateToAllAssignments: (Int) -> Unit = {},
    onNavigateToCompletedAssignments: (Int) -> Unit = {},
    onNavigateToAllStudentAssignments: (Int) -> Unit = {},
    onNavigateToProgressReport: () -> Unit = {},
    onNavigateToAssignmentDetail: (String) -> Unit = {}
) {
    val viewModelAssignment = assignmentViewModel ?: hiltViewModel()
    val viewModelAuth = authViewModel ?: hiltViewModel()
    val viewModelDashboard = dashboardViewModel ?: hiltViewModel()
    
    val assignments by viewModelAssignment.assignments.collectAsStateWithLifecycle()
    val isLoading by viewModelAssignment.isLoading.collectAsStateWithLifecycle()
    val error by viewModelAssignment.error.collectAsStateWithLifecycle()
    val currentUser by viewModelAuth.currentUser.collectAsStateWithLifecycle()
    val studentStats by viewModelAssignment.studentStats.collectAsStateWithLifecycle()
    
    // 동적 사용자 이름 가져오기
    val studentName = currentUser?.name ?: "학생"
    
    // 디버깅 로그
    LaunchedEffect(assignments) {
        println("StudentDashboard - currentUser: ${currentUser?.email}")
        println("StudentDashboard - assignments from ViewModel: ${assignments.size}")
        assignments.forEach { 
            println("  - ${it.title}")
            println("    - solvedNum: ${it.solvedNum}")
            println("    - totalQuestions: ${it.totalQuestions}")
            println("    - progress: ${if (it.totalQuestions > 0 && it.solvedNum != null) (it.solvedNum.toFloat() / it.totalQuestions.toFloat()) else 0f}")
        }
    }
    
    // Load student assignments and dashboard data on first composition or when returning to screen
    // 화면이 컴포즈될 때마다 재로딩
    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            // 학생별 해야 할 과제만 조회 (시작 안함 + 진행 중)
            println("StudentDashboard - Loading/Reloading pending assignments for student ID: ${user.id}")
            viewModelAssignment.loadPendingStudentAssignments(user.id)
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
                Column {
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
                        onClick = { 
                            currentUser?.id?.let { studentId ->
                                // 전체 과제 보기 (AllStudentAssignments)
                                onNavigateToAllStudentAssignments(studentId)
                            }
                        },
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
                        title = "해야\u00A0할\u00A0과제",
                        value = studentStats?.totalAssignments?.toString() ?: "0",
                        icon = Icons.Filled.List,
                        iconColor = PrimaryIndigo,
                        onClick = { 
                            currentUser?.id?.let { studentId ->
                                // PendingAssignmentsScreen으로 네비게이트
                                onNavigateToAllAssignments(studentId)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    VTStatsCard(
                        title = "완료한 과제",
                        value = studentStats?.completedAssignments?.toString() ?: "0",
                        icon = Icons.Filled.Done,
                        iconColor = Success,
                        onClick = { 
                            currentUser?.id?.let { studentId ->
                                onNavigateToCompletedAssignments(studentId)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
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
                        onClick = { 
                            currentUser?.id?.let { studentId ->
                                // 전체 과제 보기 (PendingAssignments - 해야 할 과제)
                                onNavigateToAllAssignments(studentId)
                            }
                        },
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
                        // Personal assignment의 진행률 계산: solvedNum / totalQuestions
                        // solvedNum은 기본 질문에 답변한 개수 (꼬리 질문 제외)
                        val progress = if (assignment.totalQuestions > 0 && assignment.solvedNum != null) {
                            (assignment.solvedNum.toFloat() / assignment.totalQuestions.toFloat()).coerceIn(0f, 1f)
                        } else {
                            0f
                        }
                        
                        // 진행률이 0이어도 표시되도록 함
                        StudentAssignmentCard(
                            title = assignment.title,
                            subject = assignment.courseClass.subject.name,
                            dueDate = formatDueDate(assignment.dueAt),
                            progress = progress,
                            solvedNum = assignment.solvedNum ?: 0,
                            totalQuestions = assignment.totalQuestions,
                            onClick = { 
                                // 두 ID를 모두 저장: assignment.id (6) 와 personalAssignmentId (16)
                                viewModelAssignment.setSelectedAssignmentIds(
                                    assignmentId = assignment.id,
                                    personalAssignmentId = assignment.personalAssignmentId
                                )
                                val detailId = assignment.personalAssignmentId ?: assignment.id
                                onNavigateToAssignmentDetail(detailId.toString())
                            }
                        )
                        
                        if (index < assignments.size - 1) {
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
    solvedNum: Int = 0,
    totalQuestions: Int = 0,
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
            
            // 진행률 표시 (0%일 때도 표시)
            if (totalQuestions > 0) {
                VTProgressBar(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${solvedNum} / ${totalQuestions} 완료 (${(progress * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
            } else {
                Text(
                    text = "진행률 정보 없음",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
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