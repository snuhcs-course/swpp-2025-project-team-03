package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
    onNavigateToAssignment: (String) -> Unit = {},
    onNavigateToAssignmentReport: (String) -> Unit = {}
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    var selectedFilter by remember { mutableStateOf(PersonalAssignmentFilter.ALL) }
    var isInitialized by remember { mutableStateOf(false) }
    
    // Load all assignments on first composition
    LaunchedEffect(Unit) {
        viewModel.loadStudentAssignmentsWithPersonalFilter(studentId, PersonalAssignmentFilter.ALL)
        isInitialized = true
    }
    
    // Load assignments based on filter changes (skip initial load)
    LaunchedEffect(selectedFilter) {
        if (isInitialized) {
            when (selectedFilter) {
                PersonalAssignmentFilter.ALL -> viewModel.loadStudentAssignmentsWithPersonalFilter(studentId, PersonalAssignmentFilter.ALL)
                PersonalAssignmentFilter.NOT_STARTED -> viewModel.loadStudentAssignmentsWithPersonalFilter(studentId, PersonalAssignmentFilter.NOT_STARTED)
                PersonalAssignmentFilter.IN_PROGRESS -> viewModel.loadStudentAssignmentsWithPersonalFilter(studentId, PersonalAssignmentFilter.IN_PROGRESS)
                PersonalAssignmentFilter.SUBMITTED -> viewModel.loadStudentAssignmentsWithPersonalFilter(studentId, PersonalAssignmentFilter.SUBMITTED)
                PersonalAssignmentFilter.GRADED -> {
                    // GRADED 필터는 UI에서 제거되었지만 enum에 존재하므로 처리 (사용되지 않음)
                    // 실제로는 SUBMITTED와 동일하게 처리할 수 있지만 필터에서 선택 불가
                }
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
        
        // Filter tabs - 스크롤 가능한 LazyRow 사용
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            items(listOf(
                PersonalAssignmentFilter.ALL to "전체",
                PersonalAssignmentFilter.NOT_STARTED to "시작 안함", 
                PersonalAssignmentFilter.IN_PROGRESS to "진행 중",
                PersonalAssignmentFilter.SUBMITTED to "제출됨"
            )) { (filter, label) ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(label) },
                    leadingIcon = if (filter == PersonalAssignmentFilter.ALL) {
                        {
                            Icon(
                                imageVector = Icons.Filled.List,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null
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
                                    text = "과제가 없습니다",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Gray600
                                )
                            }
                        }
                    }
                } else {
                    items(assignments.size) { index ->
                        val assignment = assignments[index]
                        StudentAssignmentCard(
                            assignment = assignment,
                            onClick = { 
                                // Store both ids then navigate using personalAssignmentId if available
                                viewModel.setSelectedAssignmentIds(
                                    assignmentId = assignment.id,
                                    personalAssignmentId = assignment.personalAssignmentId
                                )
                                val detailId = assignment.personalAssignmentId ?: assignment.id
                                onNavigateToAssignmentDetail(detailId.toString()) 
                            },
                            onNavigateToAssignmentDetail = onNavigateToAssignmentDetail,
                            onNavigateToAssignment = { assignmentId ->
                                val personalId = assignment.personalAssignmentId ?: assignment.id
                                onNavigateToAssignment(personalId.toString())
                            },
                            onNavigateToAssignmentReport = onNavigateToAssignmentReport
                        )
                    }
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
    onNavigateToAssignment: (String) -> Unit = {},
    onNavigateToAssignmentReport: (String) -> Unit = {}
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
                                                PersonalAssignmentStatus.SUBMITTED -> Warning.copy(alpha = 0.1f)
                                                PersonalAssignmentStatus.GRADED -> Success.copy(alpha = 0.1f)
                                                else -> Gray400.copy(alpha = 0.1f)
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = when (assignment.personalAssignmentStatus) {
                                            PersonalAssignmentStatus.NOT_STARTED -> "시작 안함"
                                            PersonalAssignmentStatus.IN_PROGRESS -> "진행 중"
                                            PersonalAssignmentStatus.SUBMITTED -> "제출됨"
                                            PersonalAssignmentStatus.GRADED -> "완료"
                                            else -> ""
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when (assignment.personalAssignmentStatus) {
                                            PersonalAssignmentStatus.NOT_STARTED -> Gray400
                                            PersonalAssignmentStatus.IN_PROGRESS -> Warning
                                            PersonalAssignmentStatus.SUBMITTED -> Warning
                                            PersonalAssignmentStatus.GRADED -> Success
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
                                            PersonalAssignmentStatus.SUBMITTED -> Warning.copy(alpha = 0.1f)
                                            PersonalAssignmentStatus.GRADED -> Success.copy(alpha = 0.1f)
                                            else -> Gray400.copy(alpha = 0.1f)
                                        }
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = when (assignment.personalAssignmentStatus) {
                                        PersonalAssignmentStatus.NOT_STARTED -> "시작 안함"
                                        PersonalAssignmentStatus.IN_PROGRESS -> "진행 중"
                                        PersonalAssignmentStatus.SUBMITTED -> "제출됨"
                                        PersonalAssignmentStatus.GRADED -> "완료"
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when (assignment.personalAssignmentStatus) {
                                        PersonalAssignmentStatus.NOT_STARTED -> Gray400
                                        PersonalAssignmentStatus.IN_PROGRESS -> Warning
                                        PersonalAssignmentStatus.SUBMITTED -> Warning
                                        PersonalAssignmentStatus.GRADED -> Success
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
                    
                    // Class info - 빈 문자열이면 표시하지 않음
                    if (assignment.courseClass.name.isNotEmpty()) {
                        Text(
                            text = assignment.courseClass.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                    
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
            if (assignment.personalAssignmentStatus != null) {
                // 시작 안함이나 진행 중인 경우 과제 시작과 과제 상세 버튼 표시
                if (assignment.personalAssignmentStatus == PersonalAssignmentStatus.NOT_STARTED || 
                    assignment.personalAssignmentStatus == PersonalAssignmentStatus.IN_PROGRESS) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VTButton(
                            text = "과제 시작",
                            onClick = { 
                                // personalAssignmentId를 사용하여 과제 시작
                                val personalId = assignment.personalAssignmentId ?: assignment.id
                                onNavigateToAssignment(personalId.toString()) 
                            },
                            variant = ButtonVariant.Primary,
                            size = ButtonSize.Small,
                            modifier = Modifier.weight(1f)
                        )
                        
                        VTButton(
                            text = "과제 상세",
                            onClick = { 
                                // personalAssignmentId가 있으면 그것을 사용해 상세 화면으로 이동
                                val detailId = assignment.personalAssignmentId ?: assignment.id
                                onNavigateToAssignmentDetail(detailId.toString()) 
                            },
                            variant = ButtonVariant.Outline,
                            size = ButtonSize.Small,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else if (assignment.personalAssignmentStatus == PersonalAssignmentStatus.GRADED || 
                           assignment.personalAssignmentStatus == PersonalAssignmentStatus.SUBMITTED) {
                    // 완료된 과제 또는 제출된 과제의 경우 리포트 보기 버튼 표시 (CompletedAssignmentsScreen 스타일)
                    VTButton(
                        text = "리포트 보기",
                        onClick = { 
                            // 리포트 화면으로 이동
                            onNavigateToAssignmentReport(assignment.title)
                        },
                        variant = ButtonVariant.Primary,
                        size = ButtonSize.Medium,
                        modifier = Modifier.fillMaxWidth()
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
