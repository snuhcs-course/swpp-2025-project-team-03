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
fun CompletedAssignmentsScreen(
    studentId: Int? = null,
    onNavigateToAssignmentDetail: (Int) -> Unit = {},
    onNavigateToAssignmentReport: (Int, String) -> Unit = { _, _ -> }
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    // Load completed assignments on first composition
    LaunchedEffect(Unit) {
        if (studentId != null) {
            // 학생의 완료한 과제만 로드
            viewModel.loadCompletedStudentAssignments(studentId)
        } else {
            // 교사용: 완료된 과제 로드
            viewModel.loadAllAssignments(status = AssignmentStatus.COMPLETED)
        }
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            viewModel.clearError()
        }
    }
    
    // Use assignments directly as they are already filtered by status
    val completedAssignments = assignments
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "완료한 과제",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Gray800
            )
            Text(
                text = "총 ${assignments.size}개의 완료한 과제",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )
        }
        
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
        } else {
            // Assignment list (scrollable)
            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (completedAssignments.isEmpty()) {
                    item {
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
                                    text = "완료한 과제가 없습니다",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Gray600
                                )
                            }
                        }
                    }
                } else {
                    items(completedAssignments.size) { index ->
                        val assignment = completedAssignments[index]
                    CompletedAssignmentCard(
                        assignment = assignment,
                        onClick = { 
                            // 리포트 보기 버튼은 리포트 화면으로 이동
                            assignment.personalAssignmentId?.let { personalAssignmentId ->
                                onNavigateToAssignmentReport(personalAssignmentId, assignment.title)
                            }
                        }
                    )
                    }
                }
            }
        }
    }
}

@Composable
fun CompletedAssignmentCard(
    assignment: AssignmentData,
    onClick: () -> Unit = {}
) {
    VTCard(
        variant = CardVariant.Elevated,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Subject badge - 빈 문자열이면 표시하지 않음
                    if (assignment.courseClass.subject.name.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                .background(PrimaryIndigo.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = assignment.courseClass.subject.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryIndigo,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Assignment title
                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    
                    // Class info - 빈 문자열이면 표시하지 않음
                    if (assignment.courseClass.name.isNotEmpty()) {
                        Text(
                            text = assignment.courseClass.name,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600
                        )
                    }
                }
                
                // Due date
                Text(
                    text = formatDueDate(assignment.dueAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 리포트 보기 버튼
            VTButton(
                text = "리포트 보기",
                onClick = onClick,
                variant = ButtonVariant.Primary,
                size = ButtonSize.Medium,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CompletedAssignmentsScreenPreview() {
    VoiceTutorTheme {
        CompletedAssignmentsScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun CompletedAssignmentCardPreview() {
    VoiceTutorTheme {
        CompletedAssignmentCard(
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
