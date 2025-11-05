package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import com.example.voicetutor.utils.formatDueDate

@Composable
fun PendingAssignmentsScreen(
    studentId: Int,
    onNavigateToAssignment: (String) -> Unit = {},
    onNavigateToAssignmentDetail: (String) -> Unit = {},
    assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel? = null
) {
    val viewModel: AssignmentViewModel = assignmentViewModel ?: hiltViewModel()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    var selectedFilter by remember { mutableStateOf(PersonalAssignmentFilter.ALL) }
    
    // Load pending assignments on first composition
    LaunchedEffect(Unit) {
        viewModel.loadPendingStudentAssignments(studentId)
    }
    
    // Handle filter changes
    LaunchedEffect(selectedFilter) {
        when (selectedFilter) {
            PersonalAssignmentFilter.ALL -> viewModel.loadPendingStudentAssignments(studentId)
            PersonalAssignmentFilter.NOT_STARTED -> viewModel.loadStudentAssignmentsWithPersonalFilter(studentId, PersonalAssignmentFilter.NOT_STARTED)
            PersonalAssignmentFilter.IN_PROGRESS -> viewModel.loadStudentAssignmentsWithPersonalFilter(studentId, PersonalAssignmentFilter.IN_PROGRESS)
            else -> {} // SUBMITTED is not shown in pending
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
                text = "해야 할 과제",
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
                if (assignments.isEmpty()) {
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
                                    text = "할 과제가 없습니다",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Gray600
                                )
                            }
                        }
                    }
                } else {
                    items(assignments.size) { index ->
                        val assignment = assignments[index]
                        PendingAssignmentCard(
                            assignment = assignment,
                            onNavigateToAssignment = { assignmentId ->
                                val personalId = assignment.personalAssignmentId ?: assignment.id
                                println("PendingAssignmentsScreen - Navigating to assignment")
                                println("PendingAssignmentsScreen - Assignment title: ${assignment.title}")
                                println("PendingAssignmentsScreen - Assignment ID: ${assignment.id}")
                                println("PendingAssignmentsScreen - Personal Assignment ID: ${assignment.personalAssignmentId}")
                                println("PendingAssignmentsScreen - Using personalId: $personalId")
                                onNavigateToAssignment(personalId.toString())
                            },
                            onNavigateToAssignmentDetail = { _ ->
                                // 상세 진입 전 assignment.id(메타)와 personalAssignmentId(통계) 모두 저장
                                viewModel.setSelectedAssignmentIds(
                                    assignmentId = assignment.id,
                                    personalAssignmentId = assignment.personalAssignmentId
                                )
                                val detailId = assignment.personalAssignmentId ?: assignment.id
                                onNavigateToAssignmentDetail(detailId.toString())
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PendingAssignmentCard(
    assignment: AssignmentData,
    onNavigateToAssignment: (String) -> Unit = {},
    onNavigateToAssignmentDetail: (String) -> Unit = {}
) {
    VTCard(
        variant = CardVariant.Elevated,
        onClick = { onNavigateToAssignmentDetail(assignment.id.toString()) }
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
                    // Subject and Status Row
                    if (assignment.courseClass.subject.name.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                            
                            // Status badge
                            if (assignment.personalAssignmentStatus != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                        .background(
                                            when (assignment.personalAssignmentStatus) {
                                                PersonalAssignmentStatus.NOT_STARTED -> Gray400.copy(alpha = 0.1f)
                                                PersonalAssignmentStatus.IN_PROGRESS -> Warning.copy(alpha = 0.15f)
                                                else -> Gray400.copy(alpha = 0.1f)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = when (assignment.personalAssignmentStatus) {
                                            PersonalAssignmentStatus.NOT_STARTED -> "시작 안함"
                                            PersonalAssignmentStatus.IN_PROGRESS -> "진행 중"
                                            else -> ""
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when (assignment.personalAssignmentStatus) {
                                            PersonalAssignmentStatus.NOT_STARTED -> Gray400
                                            PersonalAssignmentStatus.IN_PROGRESS -> Warning
                                            else -> Gray400
                                        },
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    } else if (assignment.personalAssignmentStatus != null) {
                        // Subject가 없을 때는 Status만 표시
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Status badge
                            Box(
                                modifier = Modifier
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                    .background(
                                        when (assignment.personalAssignmentStatus) {
                                            PersonalAssignmentStatus.NOT_STARTED -> Gray400.copy(alpha = 0.1f)
                                            PersonalAssignmentStatus.IN_PROGRESS -> Warning.copy(alpha = 0.15f)
                                            else -> Gray400.copy(alpha = 0.1f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = when (assignment.personalAssignmentStatus) {
                                        PersonalAssignmentStatus.NOT_STARTED -> "시작 안함"
                                        PersonalAssignmentStatus.IN_PROGRESS -> "진행 중"
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when (assignment.personalAssignmentStatus) {
                                        PersonalAssignmentStatus.NOT_STARTED -> Gray400
                                        PersonalAssignmentStatus.IN_PROGRESS -> Warning
                                        else -> Gray400
                                    },
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Assignment title
                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Class info and due date
                    if (assignment.courseClass.name.isNotEmpty()) {
                        Text(
                            text = assignment.courseClass.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                    
                    Text(
                        text = "마감: ${formatDueDate(assignment.dueAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
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
            
            // Progress bar (not shown for NOT_STARTED, shown for IN_PROGRESS)
            if (assignment.personalAssignmentStatus == PersonalAssignmentStatus.IN_PROGRESS) {
                VTProgressBar(
                    progress = 0f,
                    showPercentage = false,
                    color = Warning,
                    height = 6
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VTButton(
                    text = "과제 시작",
                    onClick = { 
                        // personalAssignmentId를 사용하여 과제 시작
                        val personalId = assignment.personalAssignmentId ?: assignment.id
                        println("PendingAssignmentCard - Starting assignment")
                        println("PendingAssignmentCard - Assignment title: ${assignment.title}")
                        println("PendingAssignmentCard - Assignment ID: ${assignment.id}")
                        println("PendingAssignmentCard - Personal Assignment ID: ${assignment.personalAssignmentId}")
                        println("PendingAssignmentCard - Using personalId: $personalId")
                        onNavigateToAssignment(personalId.toString()) 
                    },
                    variant = ButtonVariant.Primary,
                    size = ButtonSize.Small,
                    modifier = Modifier.weight(1f)
                )
                
                VTButton(
                    text = "과제 상세",
                    onClick = { 
                        // Assignment ID를 사용하여 과제 상세
                        onNavigateToAssignmentDetail(assignment.id.toString()) 
                    },
                    variant = ButtonVariant.Outline,
                    size = ButtonSize.Small,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PendingAssignmentsScreenPreview() {
    VoiceTutorTheme {
        PendingAssignmentsScreen(studentId = 1)
    }
}
