package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

@Composable
fun TeacherAssignmentDetailScreen(
    assignmentViewModel: AssignmentViewModel? = null,
    assignmentId: Int = 0,
    onNavigateToAssignmentResults: (Int) -> Unit = {},
    onNavigateToEditAssignment: (Int) -> Unit = {}
) {
    val viewModel: AssignmentViewModel = assignmentViewModel ?: hiltViewModel()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val assignment by viewModel.currentAssignment.collectAsStateWithLifecycle()
    val assignmentStatistics by viewModel.assignmentStatistics.collectAsStateWithLifecycle()
    
    // Find assignment by id from the assignments list
    val targetAssignment = remember(assignments, assignmentId) {
        if (assignmentId > 0) {
            assignments.find { it.id == assignmentId }
        } else {
            null
        }
    }
    
    // 동적 과제 제목 가져오기
    val dynamicAssignmentTitle = assignment?.title ?: (targetAssignment?.title ?: "과제")
    // assignmentResults API가 제거되었으므로 빈 리스트 사용
    val assignmentResults = remember { emptyList<StudentResult>() }
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    // Load assignment data on first composition and when screen is entered
    LaunchedEffect(assignmentId) {
        if (assignmentId > 0) {
            println("TeacherAssignmentDetail - Loading assignment ID: $assignmentId")
            viewModel.loadAssignmentById(assignmentId)
        } else {
            targetAssignment?.let { target ->
                println("TeacherAssignmentDetail - Loading assignment: ${target.title} (ID: ${target.id})")
                viewModel.loadAssignmentById(target.id)
                // 통계도 함께 로드 (assignment가 로드되기 전에도 targetAssignment로 통계 로드 가능)
                println("TeacherAssignmentDetail - Loading statistics for target assignment: ${target.title} (ID: ${target.id})")
                viewModel.loadAssignmentStatistics(target.id, target.courseClass.studentCount)
            }
        }
    }
    
    // assignment가 로드되면 통계 새로고침 (화면 진입 시, assignment가 업데이트될 때)
    LaunchedEffect(assignment?.id) {
        assignment?.let { a ->
            println("TeacherAssignmentDetail - Assignment loaded, refreshing statistics: ${a.title} (ID: ${a.id})")
            viewModel.loadAssignmentStatistics(a.id, a.courseClass.studentCount)
        }
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            viewModel.clearError()
        }
    }
    
    // Convert AssignmentData to AssignmentDetail for UI
    val assignmentDetail = assignment?.let { a ->
        AssignmentDetail(
            title = a.title,
            subject = a.courseClass.subject.name,
            className = a.courseClass.name,
            dueDate = a.dueAt,
            createdAt = a.createdAt ?: "",
            status = "IN_PROGRESS", // 기본값으로 설정
            type = "연속형", // type 속성이 없으므로 기본값
            description = a.description ?: "",
            totalStudents = assignmentStatistics?.totalStudents ?: a.courseClass.studentCount,
            submittedStudents = assignmentStatistics?.submittedStudents ?: 0,
            averageScore = assignmentStatistics?.averageScore ?: 0,
            completionRate = assignmentStatistics?.completionRate ?: 0
        )
    }
    
    // Convert StudentResult to StudentSubmission for UI
    val recentSubmissions = assignmentResults.map { result ->
        StudentSubmission(
            name = result.name,
            studentId = result.studentId,
            submittedAt = result.submittedAt,
            score = result.score,
            status = result.status
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = PrimaryIndigo
                )
            }
        } else if (assignmentDetail == null) {
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
                        text = "과제 정보를 찾을 수 없습니다",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Gray600
                    )
                }
            }
        } else {
            // Assignment info card - Welcome section style
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
                            text = assignmentDetail.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${assignmentDetail.subject} • ${assignmentDetail.className}",
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
                        text = "김",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Stats overview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VTStatsCard(
                title = "제출률",
                value = "${assignmentDetail.completionRate}%",
                icon = Icons.Filled.Assignment,
                iconColor = PrimaryIndigo,
                variant = CardVariant.Elevated,
                modifier = Modifier.weight(1f),
                layout = StatsCardLayout.Vertical
            )
            
            VTStatsCard(
                title = "평균 점수",
                value = "${assignmentDetail.averageScore}점",
                icon = Icons.Filled.Grade,
                iconColor = Success,
                variant = CardVariant.Elevated,
                modifier = Modifier.weight(1f),
                layout = StatsCardLayout.Vertical
            )
            
            VTStatsCard(
                title = "제출 학생",
                value = "${assignmentDetail.submittedStudents}/${assignmentDetail.totalStudents}",
                icon = Icons.Filled.People,
                iconColor = Warning,
                variant = CardVariant.Elevated,
                modifier = Modifier.weight(1f),
                layout = StatsCardLayout.Vertical
            )
        }
        
        // Assignment content
        VTCard(variant = CardVariant.Elevated) {
            Column {
                Text(
                    text = "과제 내용",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = assignmentDetail.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
                )
            }
        }
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VTButton(
                text = "과제 결과",
                onClick = { 
                    val assignmentId = assignment?.id ?: targetAssignment?.id ?: 0
                    if (assignmentId > 0) {
                        onNavigateToAssignmentResults(assignmentId)
                    }
                },
                variant = ButtonVariant.Primary,
                modifier = Modifier.weight(1f),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Assessment,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            
            VTButton(
                text = "과제 편집",
                onClick = { 
                    val assignmentId = assignment?.id ?: targetAssignment?.id ?: 0
                    if (assignmentId > 0) {
                        onNavigateToEditAssignment(assignmentId)
                    }
                },
                variant = ButtonVariant.Outline,
                modifier = Modifier.weight(1f),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun StudentSubmissionItem(
    submission: StudentSubmission,
    onClick: () -> Unit
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
                text = submission.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Gray800
            )
            Text(
                text = submission.studentId,
                style = MaterialTheme.typography.bodySmall,
                color = Gray600
            )
        }
        
        Column(
            horizontalAlignment = Alignment.End
        ) {
            if (submission.status == "완료") {
                Text(
                    text = "${submission.score}점",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        submission.score >= 90 -> Success
                        submission.score >= 70 -> Warning
                        else -> Error
                    }
                )
                Text(
                    text = com.example.voicetutor.utils.formatSubmittedTime(submission.submittedAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
            } else {
                Box(
                    modifier = Modifier
                        .background(
                            Error.copy(alpha = 0.1f),
                            androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = submission.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = Error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        }
    }
}

data class AssignmentDetail(
    val title: String,
    val subject: String,
    val className: String,
    val dueDate: String,
    val createdAt: String,
    val status: String,
    val type: String,
    val description: String,
    val totalStudents: Int,
    val submittedStudents: Int,
    val averageScore: Int,
    val completionRate: Int
)

data class StudentSubmission(
    val name: String,
    val studentId: String,
    val submittedAt: String,
    val score: Int,
    val status: String
)

@Preview(showBackground = true)
@Composable
fun TeacherAssignmentDetailScreenPreview() {
    VoiceTutorTheme {
        TeacherAssignmentDetailScreen()
    }
}
