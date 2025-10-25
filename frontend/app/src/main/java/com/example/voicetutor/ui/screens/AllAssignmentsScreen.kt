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
fun AllAssignmentsScreen(
    studentId: Int? = null,
    onNavigateToAssignmentResults: (String) -> Unit = {},
    onNavigateToEditAssignment: (String) -> Unit = {},
    onNavigateToAssignmentDetail: (String) -> Unit = {}
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    var selectedFilter by remember { mutableStateOf(AssignmentFilter.ALL) }
    var selectedPersonalFilter by remember { mutableStateOf(PersonalAssignmentFilter.ALL) }
    
    // Load assignments on first composition
    LaunchedEffect(Unit) {
        if (studentId != null) {
            // 학생의 개인 과제를 필터링 없이 로드 (ALL 필터와 동일)
            viewModel.loadStudentAssignments(studentId)
        } else {
            // 모든 과제 로드 (교사용)
            viewModel.loadAllAssignments()
        }
    }
    
    // Handle filter changes for students
    LaunchedEffect(selectedPersonalFilter) {
        if (studentId != null) {
            println("AllAssignmentsScreen - Filter changed to: $selectedPersonalFilter for student: $studentId")
            // 학생의 개인 과제는 personal assignment 상태로 필터링
            viewModel.loadStudentAssignmentsWithPersonalFilter(studentId, selectedPersonalFilter)
        }
    }
    
    // Handle filter changes for teachers
    LaunchedEffect(selectedFilter) {
        if (studentId == null) {
            // 교사용: 상태별 필터링
            val status = when (selectedFilter) {
                AssignmentFilter.ALL -> null
                AssignmentFilter.IN_PROGRESS -> AssignmentStatus.IN_PROGRESS
                AssignmentFilter.COMPLETED -> AssignmentStatus.COMPLETED
            }
            viewModel.loadAllAssignments(status = status)
        }
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message (you can implement a snackbar or dialog)
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
        Column {
            Text(
                text = "모든 과제",
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
            if (studentId != null) {
                // 학생용 필터링 버튼 (Personal Assignment 상태 기반)
                FilterChip(
                    selected = selectedPersonalFilter == PersonalAssignmentFilter.ALL,
                    onClick = { selectedPersonalFilter = PersonalAssignmentFilter.ALL },
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
                    selected = selectedPersonalFilter == PersonalAssignmentFilter.NOT_STARTED,
                    onClick = { selectedPersonalFilter = PersonalAssignmentFilter.NOT_STARTED },
                    label = { Text("시작 안함") }
                )
                
                FilterChip(
                    selected = selectedPersonalFilter == PersonalAssignmentFilter.IN_PROGRESS,
                    onClick = { selectedPersonalFilter = PersonalAssignmentFilter.IN_PROGRESS },
                    label = { Text("진행 중") }
                )
                
                FilterChip(
                    selected = selectedPersonalFilter == PersonalAssignmentFilter.SUBMITTED,
                    onClick = { selectedPersonalFilter = PersonalAssignmentFilter.SUBMITTED },
                    label = { Text("제출 완료") }
                )
            } else {
                // 교사용 필터링 버튼 (기존 AssignmentFilter 사용)
                FilterChip(
                    selected = selectedFilter == AssignmentFilter.ALL,
                    onClick = { selectedFilter = AssignmentFilter.ALL },
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
                    selected = selectedFilter == AssignmentFilter.IN_PROGRESS,
                    onClick = { selectedFilter = AssignmentFilter.IN_PROGRESS },
                    label = { Text("진행중") }
                )
                
                FilterChip(
                    selected = selectedFilter == AssignmentFilter.COMPLETED,
                    onClick = { selectedFilter = AssignmentFilter.COMPLETED },
                    label = { Text("완료") }
                )
            }
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
                    AssignmentCard(
                        assignment = assignment,
                        onAssignmentClick = { onNavigateToAssignmentDetail("${assignment.courseClass.subject.name} - ${assignment.title}") },
                        onEditClick = { onNavigateToEditAssignment("${assignment.courseClass.subject.name} - ${assignment.title}") },
                        onDeleteClick = { viewModel.deleteAssignment(assignment.id) },
                        onViewResults = { onNavigateToAssignmentResults("${assignment.courseClass.subject.name} - ${assignment.title}") }
                    )
                }
            }
        }
    }
}

@Composable
fun AssignmentCard(
    assignment: AssignmentData,
    onAssignmentClick: (String) -> Unit,
    onEditClick: (String) -> Unit,
    onDeleteClick: (Int) -> Unit,
    onViewResults: () -> Unit
) {
    VTCard(
        variant = CardVariant.Elevated,
        onClick = { onAssignmentClick("${assignment.courseClass.subject.name} - ${assignment.title}") }
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Personal Assignment 상태에 따른 StatusBadge 표시
                        val statusToShow = assignment.personalAssignmentStatus?.let { personalStatus ->
                            when (personalStatus) {
                                PersonalAssignmentStatus.NOT_STARTED -> AssignmentStatus.IN_PROGRESS // "시작 안함"을 "진행중"으로 표시
                                PersonalAssignmentStatus.IN_PROGRESS -> AssignmentStatus.IN_PROGRESS
                                PersonalAssignmentStatus.SUBMITTED -> AssignmentStatus.COMPLETED
                            }
                        } ?: AssignmentStatus.IN_PROGRESS // 기본값
                        
                        // 커스텀 상태 표시
                        val statusText = assignment.personalAssignmentStatus?.let { personalStatus ->
                            when (personalStatus) {
                                PersonalAssignmentStatus.NOT_STARTED -> "시작 안함"
                                PersonalAssignmentStatus.IN_PROGRESS -> "진행 중"
                                PersonalAssignmentStatus.SUBMITTED -> "완료"
                            }
                        } ?: "진행 중"
                        
                        CustomStatusBadge(text = statusText)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    
                    Text(
                        text = "마감: ${formatDueDate(assignment.dueAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
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
                color = PrimaryIndigo, // 기본 색상으로 설정
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
                    modifier = Modifier.weight(1f)
                )
                
                VTButton(
                    text = "편집",
                    onClick = { onEditClick("${assignment.courseClass.subject.name} - ${assignment.title}") },
                    variant = ButtonVariant.Outline,
                    size = ButtonSize.Small,
                    modifier = Modifier.weight(1f)
                )
                
                IconButton(
                    onClick = { onDeleteClick(assignment.id) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "삭제",
                        tint = Error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CustomStatusBadge(text: String) {
    val (color) = when (text) {
        "시작 안함" -> PrimaryIndigo
        "진행 중" -> Warning
        "완료" -> Success
        else -> Gray500
    }
    
    Box(
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun StatusBadge(status: AssignmentStatus) {
    val (text, color) = when (status) {
        AssignmentStatus.IN_PROGRESS -> "진행중" to PrimaryIndigo
        AssignmentStatus.COMPLETED -> "완료" to Success
        AssignmentStatus.DRAFT -> "임시저장" to Gray500
    }
    
    Box(
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun TypeBadge(type: String) {
    val (text, color, icon) = when (type) {
        "Quiz" -> Triple("퀴즈", Warning, Icons.Filled.Quiz)
        "Continuous" -> Triple("연속", Success, Icons.Filled.Schedule)
        "Discussion" -> Triple("토론", PrimaryPurple, Icons.Filled.Chat)
        else -> Triple("알 수 없음", MaterialTheme.colorScheme.onSurface, Icons.Filled.Help)
    }
    
    Box(
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AllAssignmentsScreenPreview() {
    VoiceTutorTheme {
        AllAssignmentsScreen()
    }
}
