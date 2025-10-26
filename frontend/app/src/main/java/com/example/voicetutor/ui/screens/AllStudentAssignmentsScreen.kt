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
import com.example.voicetutor.ui.screens.CustomStatusBadge
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// 날짜 포맷 유틸 함수
private fun formatDueDate(dueDate: String): String {
    return try {
        val zonedDateTime = ZonedDateTime.parse(dueDate)
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        zonedDateTime.format(formatter)
    } catch (e: Exception) {
        dueDate
    }
}

@Composable
fun AllStudentAssignmentsScreen(
    studentId: Int,
    onNavigateToAssignmentDetail: (String) -> Unit = {},
    onNavigateToAssignment: (String) -> Unit = {}
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    var selectedFilter by remember { mutableStateOf(PersonalAssignmentFilter.ALL) }
    var isInitialized by remember { mutableStateOf(false) }
    
    // Load all assignments on first composition
    LaunchedEffect(Unit) {
        viewModel.loadStudentAssignments(studentId)
        isInitialized = true
    }
    
    // Load assignments based on filter changes (skip initial load)
    LaunchedEffect(selectedFilter) {
        if (isInitialized) {
            when (selectedFilter) {
                PersonalAssignmentFilter.ALL -> viewModel.loadStudentAssignments(studentId)
                PersonalAssignmentFilter.NOT_STARTED -> viewModel.loadStudentAssignmentsWithPersonalFilter(studentId, PersonalAssignmentFilter.NOT_STARTED)
                PersonalAssignmentFilter.IN_PROGRESS -> viewModel.loadStudentAssignmentsWithPersonalFilter(studentId, PersonalAssignmentFilter.IN_PROGRESS)
                PersonalAssignmentFilter.SUBMITTED -> viewModel.loadStudentAssignmentsWithPersonalFilter(studentId, PersonalAssignmentFilter.SUBMITTED)
                PersonalAssignmentFilter.GRADED -> viewModel.loadStudentAssignmentsWithPersonalFilter(studentId, PersonalAssignmentFilter.GRADED)
            }
        }
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            viewModel.clearError()
        }
    }
    
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
                text = "전체 과제",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Gray800
            )
            Text(
                text = "총 ${assignments.size}개의 과제",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )
        }
        
        // Filter tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == PersonalAssignmentFilter.ALL,
                onClick = { selectedFilter = PersonalAssignmentFilter.ALL },
                label = { Text("전체") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.List,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            
            FilterChip(
                selected = selectedFilter == PersonalAssignmentFilter.NOT_STARTED,
                onClick = { selectedFilter = PersonalAssignmentFilter.NOT_STARTED },
                label = { Text("시작 안함") }
            )
            
            FilterChip(
                selected = selectedFilter == PersonalAssignmentFilter.IN_PROGRESS,
                onClick = { selectedFilter = PersonalAssignmentFilter.IN_PROGRESS },
                label = { Text("진행 중") }
            )
            
            FilterChip(
                selected = selectedFilter == PersonalAssignmentFilter.SUBMITTED,
                onClick = { selectedFilter = PersonalAssignmentFilter.SUBMITTED },
                label = { Text("제출됨") }
            )
            
            FilterChip(
                selected = selectedFilter == PersonalAssignmentFilter.GRADED,
                onClick = { selectedFilter = PersonalAssignmentFilter.GRADED },
                label = { Text("완료") }
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
            // Assignment list
            if (assignments.isEmpty()) {
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
                assignments.forEach { assignment ->
                    StudentAssignmentCard(
                        assignment = assignment,
                        onClick = { onNavigateToAssignmentDetail(assignment.id.toString()) },
                        onNavigateToAssignmentDetail = onNavigateToAssignmentDetail,
                        onNavigateToAssignment = onNavigateToAssignment
                    )
                }
            }
        }
    }
}

@Composable
fun StudentAssignmentCard(
    assignment: AssignmentData,
    onClick: () -> Unit = {},
    onNavigateToAssignmentDetail: (String) -> Unit = {},
    onNavigateToAssignment: (String) -> Unit = {}
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
                    // Subject badge
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
                    
                    // Assignment title
                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    
                    // Class info
                    Text(
                        text = assignment.courseClass.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Due date
                    Text(
                        text = "마감: ${formatDueDate(assignment.dueAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // Status badge
                    Box(
                        modifier = Modifier
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .background(
                                when (assignment.personalAssignmentStatus) {
                                    PersonalAssignmentStatus.NOT_STARTED -> Gray400.copy(alpha = 0.1f)
                                    PersonalAssignmentStatus.IN_PROGRESS -> PrimaryIndigo.copy(alpha = 0.1f)
                                    PersonalAssignmentStatus.SUBMITTED -> Warning.copy(alpha = 0.1f)
                                    PersonalAssignmentStatus.GRADED -> Success.copy(alpha = 0.1f)
                                    null -> Gray400.copy(alpha = 0.1f)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (assignment.personalAssignmentStatus) {
                                    PersonalAssignmentStatus.NOT_STARTED -> Icons.Filled.Schedule
                                    PersonalAssignmentStatus.IN_PROGRESS -> Icons.Filled.PlayArrow
                                    PersonalAssignmentStatus.SUBMITTED -> Icons.Filled.Upload
                                    PersonalAssignmentStatus.GRADED -> Icons.Filled.CheckCircle
                                    null -> Icons.Filled.Help
                                },
                                contentDescription = null,
                                tint = when (assignment.personalAssignmentStatus) {
                                    PersonalAssignmentStatus.NOT_STARTED -> Gray400
                                    PersonalAssignmentStatus.IN_PROGRESS -> PrimaryIndigo
                                    PersonalAssignmentStatus.SUBMITTED -> Warning
                                    PersonalAssignmentStatus.GRADED -> Success
                                    null -> Gray400
                                },
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = when (assignment.personalAssignmentStatus) {
                                    PersonalAssignmentStatus.NOT_STARTED -> "시작 안함"
                                    PersonalAssignmentStatus.IN_PROGRESS -> "진행 중"
                                    PersonalAssignmentStatus.SUBMITTED -> "제출됨"
                                    PersonalAssignmentStatus.GRADED -> "완료"
                                    null -> "알 수 없음"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = when (assignment.personalAssignmentStatus) {
                                    PersonalAssignmentStatus.NOT_STARTED -> Gray400
                                    PersonalAssignmentStatus.IN_PROGRESS -> PrimaryIndigo
                                    PersonalAssignmentStatus.SUBMITTED -> Warning
                                    PersonalAssignmentStatus.GRADED -> Success
                                    null -> Gray400
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Question count
                    Text(
                        text = "${assignment.totalQuestions}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo
                    )
                    Text(
                        text = "문제",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Progress bar
            VTProgressBar(
                progress = 0f, // 제출 정보가 없으므로 0으로 설정
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
                // 시작 안함이나 진행 중인 경우 과제 시작과 과제 상세 버튼 표시
                if (assignment.personalAssignmentStatus == PersonalAssignmentStatus.NOT_STARTED || 
                    assignment.personalAssignmentStatus == PersonalAssignmentStatus.IN_PROGRESS) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VTButton(
                            text = "과제 시작",
                            onClick = { onNavigateToAssignment(assignment.id.toString()) },
                            variant = ButtonVariant.Primary,
                            size = ButtonSize.Small,
                            modifier = Modifier.weight(1f)
                        )
                        
                        VTButton(
                            text = "과제 상세",
                            onClick = { onNavigateToAssignmentDetail(assignment.id.toString()) },
                            variant = ButtonVariant.Outline,
                            size = ButtonSize.Small,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else if (assignment.personalAssignmentStatus == PersonalAssignmentStatus.GRADED) {
                    // 완료된 과제의 경우 결과 보기 버튼 표시
                    VTButton(
                        text = "결과 보기",
                        onClick = { },
                        variant = ButtonVariant.Outline,
                        size = ButtonSize.Small,
                        modifier = Modifier.weight(1f)
                    )
                } else if (assignment.personalAssignmentStatus == PersonalAssignmentStatus.SUBMITTED) {
                    // 제출된 과제의 경우 대기 상태 표시
                    VTButton(
                        text = "채점 대기",
                        onClick = { },
                        variant = ButtonVariant.Outline,
                        size = ButtonSize.Small,
                        modifier = Modifier.weight(1f),
                        enabled = false
                    )
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun AllStudentAssignmentsScreenPreview() {
    VoiceTutorTheme {
        AllStudentAssignmentsScreen(studentId = 1)
    }
}
