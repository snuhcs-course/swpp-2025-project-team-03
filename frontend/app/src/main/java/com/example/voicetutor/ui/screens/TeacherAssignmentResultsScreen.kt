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
    assignmentViewModel: AssignmentViewModel? = null,
    assignmentId: Int = 0,
    assignmentTitle: String? = null, // For backward compatibility
    onNavigateToStudentDetail: (String) -> Unit = {}
) {
    val viewModel: AssignmentViewModel = assignmentViewModel ?: hiltViewModel()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    // assignmentResults API가 제거되었으므로 빈 리스트 사용
    val students = remember { emptyList<StudentResult>() }
    val currentAssignment by viewModel.currentAssignment.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    // Find assignment by ID or title from the assignments list
    // "과목 - 제목" 형식도 처리 가능하도록 수정
    val targetAssignment = remember(assignments, assignmentId, assignmentTitle) {
        if (assignmentId > 0) {
            assignments.find { it.id == assignmentId }
        } else if (assignmentTitle != null) {
        assignments.find { 
            it.title == assignmentTitle || 
            "${it.courseClass.subject.name} - ${it.title}" == assignmentTitle ||
            assignmentTitle.contains(it.title)
            }
        } else {
            null
        }
    }
    
    // 동적 과제 제목 가져오기
    val dynamicAssignmentTitle = currentAssignment?.title ?: (targetAssignment?.title ?: assignmentTitle ?: "과제")
    
    // Load assignment data on first composition
    // Note: loadAssignmentResults API가 제거되었으므로 assignmentResults는 빈 리스트일 수 있음
    LaunchedEffect(assignmentId, targetAssignment?.id) {
        if (assignmentId > 0) {
            println("TeacherAssignmentResults - Loading assignment by ID: $assignmentId")
            viewModel.loadAssignmentById(assignmentId)
        } else {
        targetAssignment?.let { target ->
            println("TeacherAssignmentResults - Loading assignment: ${target.title} (ID: ${target.id})")
            viewModel.loadAssignmentById(target.id)
            }
        }
        // loadAssignmentResults API가 제거되었으므로 주석 처리
        // viewModel.loadAssignmentResults(target.id)
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
                title = "평균 등급",
                value = if (students.isNotEmpty() && students.any { it.status == "완료" }) {
                    val avgScore = students.filter { it.status == "완료" }.map { it.score }.average().toInt()
                    scoreToGrade(avgScore)
                } else {
                    "-"
                },
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
                // 학생 결과 표시 (API 데이터가 있을 때만 표시됨)
                students.forEachIndexed { index, student ->
                    TeacherAssignmentResultCard(
                        student = student,
                        onStudentClick = { onNavigateToStudentDetail(student.studentId) }
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
            
            // Grade and time info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "등급",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    val grade = scoreToGrade(student.score)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = getGradeColor(grade).copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = grade,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = getGradeColor(grade),
                            fontSize = 24.sp
                        )
                    }
                }
                
                Column {
                    Text(
                        text = "소요 시간",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = "정보 없음",
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
                        text = formatSubmittedTime(student.submittedAt),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Gray800
                    )
                }
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

// Helper function to format submitted time
private fun formatSubmittedTime(isoTime: String): String {
    return try {
        // ISO 8601 형식 파싱: "2025-10-16T02:10:13.245620Z"
        val parts = isoTime.split("T")
        if (parts.size >= 2) {
            val date = parts[0] // "2025-10-16"
            val timePart = parts[1].split(".")[0] // "02:10:13"
            val time = timePart.substring(0, 5) // "02:10"
            "$date $time"
        } else {
            isoTime
        }
    } catch (e: Exception) {
        isoTime
    }
}

// Helper function to convert score to grade
private fun scoreToGrade(score: Int): String {
    return when {
        score >= 90 -> "A"
        score >= 80 -> "B"
        score >= 70 -> "C"
        score >= 60 -> "D"
        else -> "F"
    }
}

// Helper function to get grade color
private fun getGradeColor(grade: String): Color {
    return when (grade) {
        "A" -> Success
        "B" -> Color(0xFF4CAF50)
        "C" -> Warning
        "D" -> Color(0xFFFF9800)
        "F" -> Error
        else -> Gray600
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherAssignmentResultsScreenPreview() {
    VoiceTutorTheme {
        TeacherAssignmentResultsScreen()
    }
}