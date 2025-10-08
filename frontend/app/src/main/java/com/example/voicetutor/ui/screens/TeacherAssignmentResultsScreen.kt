package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel

@Composable
fun TeacherAssignmentResultsScreen(
    assignmentId: Int = 1, // 임시로 기본값 설정
    assignmentTitle: String = "과제", // TODO: 실제 과제 제목으로 동적 설정
    onNavigateToStudentDetail: (String) -> Unit = {}
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val students by viewModel.assignmentResults.collectAsStateWithLifecycle()
    val currentAssignment by viewModel.currentAssignment.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    // 동적 과제 제목 가져오기
    val dynamicAssignmentTitle = currentAssignment?.title ?: assignmentTitle
    
    // Load assignment data and results on first composition
    LaunchedEffect(assignmentId) {
        viewModel.loadAssignmentById(assignmentId)
        viewModel.loadAssignmentResults(assignmentId)
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            viewModel.clearError()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
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
                    text = dynamicAssignmentTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "학생별 과제 결과를 확인하고 피드백을 제공하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
        
        // Stats cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VTStatsCard(
                title = "제출 완료",
                value = "${students.count { it.status == "완료" }}명",
                icon = Icons.Filled.CheckCircle,
                iconColor = Success,
                modifier = Modifier.weight(1f),
                variant = CardVariant.Gradient,
                trend = TrendDirection.Up,
                trendValue = "+${students.count { it.status == "완료" }}"
            )
            
            VTStatsCard(
                title = "평균 점수",
                value = "${if (students.isNotEmpty()) students.filter { it.status == "완료" }.map { it.score }.average().toInt() else 0}점",
                icon = Icons.Filled.Star,
                iconColor = Warning,
                modifier = Modifier.weight(1f),
                variant = CardVariant.Gradient
            )
        }
        
        // Students list
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "학생별 결과",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                
                Text(
                    text = "총 ${students.size}명",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
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
            } else if (students.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = Gray400,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "제출된 과제가 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600
                        )
                    }
                }
            } else {
                students.forEachIndexed { index, student ->
                    TeacherAssignmentResultCard(
                        student = student,
                        onStudentClick = { onNavigateToStudentDetail(student.name) }
                    )
                    
                    if (index < students.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherAssignmentResultCard(
    student: StudentResult,
    onStudentClick: () -> Unit
) {
    VTCard(
        variant = CardVariant.Elevated,
        onClick = onStudentClick
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Student info header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.first().toString(),
                        color = PrimaryIndigo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    Text(
                        text = student.studentId,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
                
                // Status badge
                Box(
                    modifier = Modifier
                        .background(
                            color = if (student.status == "완료") Success.copy(alpha = 0.1f) else Warning.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = student.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (student.status == "완료") Success else Warning,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Score and time info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "점수",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = "${student.score}점",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo
                    )
                }
                
                Column {
                    Text(
                        text = "소요 시간",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = student.completionTime,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Gray800
                    )
                }
                
                Column {
                    Text(
                        text = "제출 시간",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = student.submittedAt,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Gray800
                    )
                }
            }
            
            // Confidence score
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "자신감 점수",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                VTProgressBar(
                    progress = student.confidenceScore / 100f,
                    showPercentage = false,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "${student.confidenceScore}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Gray800
                )
            }
            
            // Sample answers preview
            if (student.answers.isNotEmpty()) {
                Column {
                    Text(
                        text = "답변 미리보기",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Gray700
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = student.answers.first(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherAssignmentResultsScreenPreview() {
    VoiceTutorTheme {
        TeacherAssignmentResultsScreen()
    }
}