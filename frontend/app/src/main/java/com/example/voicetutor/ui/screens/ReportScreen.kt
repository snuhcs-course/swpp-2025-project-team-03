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
import com.example.voicetutor.utils.formatDateOnly

@Composable
fun ReportScreen(
    studentId: Int? = null,
    onNavigateToAssignmentReport: (Int, String) -> Unit = { _, _ -> }
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    // Load completed assignments on first composition
    LaunchedEffect(Unit) {
        println("ReportScreen - Loading completed assignments for studentId: $studentId")
        if (studentId != null) {
            // 학생의 완료한 과제만 로드 (SUBMITTED 상태)
            viewModel.loadCompletedStudentAssignments(studentId)
        } else {
            println("ReportScreen - studentId is null, cannot load assignments")
        }
    }
    
    // Debug assignments
    LaunchedEffect(assignments) {
        println("ReportScreen - Assignments loaded: ${assignments.size}")
        assignments.forEach { assignment ->
            println("  - ${assignment.title} (Status: ${assignment.personalAssignmentStatus})")
        }
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            viewModel.clearError()
        }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Loading indicator
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PrimaryIndigo
                    )
                }
            }
        } else if (assignments.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Assessment,
                            contentDescription = null,
                            tint = Gray400,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "완료한 과제가 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600
                        )
                    }
                }
            }
        } else {
            items(assignments) { assignment ->
                AssignmentReportCard(
                    assignment = assignment,
                    onReportClick = { 
                        // personalAssignmentId와 과제 제목을 전달
                        assignment.personalAssignmentId?.let { personalAssignmentId ->
                            onNavigateToAssignmentReport(personalAssignmentId, assignment.title)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun AssignmentReportCard(
    assignment: AssignmentData,
    onReportClick: () -> Unit = {}
) {
    VTCard(
        variant = CardVariant.Elevated,
        onClick = onReportClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 과제 제목
            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray800
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 완료일
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = Success,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "완료일: ${formatDateOnly(assignment.dueAt)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 리포트 보기 버튼
            VTButton(
                text = "리포트 보기",
                onClick = onReportClick,
                variant = ButtonVariant.Primary,
                size = ButtonSize.Medium,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReportScreenPreview() {
    VoiceTutorTheme {
        ReportScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun AssignmentReportCardPreview() {
    VoiceTutorTheme {
        AssignmentReportCard(
            assignment = AssignmentData(
                id = 1,
                title = "화학 기초 퀴즈",
                description = "원소주기율표 기초 문제",
                totalQuestions = 5,
                createdAt = "2024-01-10T09:00:00Z",
                visibleFrom = "2024-01-10T09:00:00Z",
                dueAt = "2024-01-15T10:00:00Z",
                courseClass = CourseClass(
                    id = 1,
                    name = "1학년 1반",
                    description = "화학 기초반",
                    subject = Subject(id = 1, name = "화학"),
                    teacherName = "김선생님",
                    startDate = "2024-01-01",
                    endDate = "2024-12-31",
                    studentCount = 25,
                    createdAt = "2024-01-01T00:00:00Z"
                ),
                materials = emptyList(),
                grade = "1학년"
            )
        )
    }
}
