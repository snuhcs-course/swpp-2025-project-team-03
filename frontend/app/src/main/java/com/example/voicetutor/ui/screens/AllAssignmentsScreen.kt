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
import com.example.voicetutor.utils.formatDueDate

@Composable
fun AllAssignmentsScreen(
    teacherId: String? = null,
    onNavigateToAssignmentResults: (Int) -> Unit = {},
    onNavigateToEditAssignment: (Int) -> Unit = {},
    onNavigateToAssignmentDetail: (Int) -> Unit = {}
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    
    var selectedFilter by remember { mutableStateOf(AssignmentFilter.ALL) }
    
    // Compute actual teacher ID
    val actualTeacherId = teacherId ?: currentUser?.id?.toString()
    
    // Load assignments for the specific teacher
    LaunchedEffect(actualTeacherId) {
        if (actualTeacherId != null) {
            println("AllAssignmentsScreen - Loading assignments for teacher ID: $actualTeacherId")
            viewModel.loadAllAssignments(teacherId = actualTeacherId)
        } else {
            println("AllAssignmentsScreen - No teacher ID available, loading all assignments")
            viewModel.loadAllAssignments()
        }
    }
    
    // Reload when screen becomes visible (for refresh after creating assignment)
    LaunchedEffect(Unit) {
        // This will run when the composable is first created
        // Additional reload can be triggered by navigation lifecycle
        if (actualTeacherId != null) {
            println("AllAssignmentsScreen - Screen visible, ensuring assignments are loaded for teacher ID: $actualTeacherId")
            viewModel.loadAllAssignments(teacherId = actualTeacherId)
        }
    }
    
    // Handle filter changes for teachers
    LaunchedEffect(selectedFilter, actualTeacherId) {
        if (actualTeacherId != null) {
            // 교사용: 상태별 필터링
            val status = when (selectedFilter) {
                AssignmentFilter.ALL -> null
                AssignmentFilter.IN_PROGRESS -> AssignmentStatus.IN_PROGRESS
                AssignmentFilter.COMPLETED -> AssignmentStatus.COMPLETED
            }
            println("AllAssignmentsScreen - Filter changed: $selectedFilter, loading assignments for teacher ID: $actualTeacherId")
            viewModel.loadAllAssignments(teacherId = actualTeacherId, status = status)
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
        
        // Filter tabs (Teacher only)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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
                // 제출 현황을 저장하는 StateMap
                val assignmentStatsMap = remember { mutableStateMapOf<Int, Pair<Int, Int>>() }
                
                // 각 과제의 제출 현황을 로드
                assignments.forEach { assignment ->
                    LaunchedEffect(assignment.id) {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            val stats = viewModel.getAssignmentSubmissionStats(assignment.id)
                            assignmentStatsMap[assignment.id] = stats.submittedStudents to stats.totalStudents
                        }
                    }
                }
                
                assignments.forEach { assignment ->
                    val stats = assignmentStatsMap[assignment.id] ?: (0 to assignment.courseClass.studentCount)
                    
                    AssignmentCard(
                        assignment = assignment,
                        submittedCount = stats.first,
                        totalCount = stats.second,
                        onAssignmentClick = { onNavigateToAssignmentDetail(assignment.id) },
                        onEditClick = { onNavigateToEditAssignment(assignment.id) },
                        onDeleteClick = { viewModel.deleteAssignment(assignment.id) },
                        onViewResults = { onNavigateToAssignmentResults(assignment.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AssignmentCard(
    assignment: AssignmentData,
    submittedCount: Int = 0,
    totalCount: Int = 0,
    onAssignmentClick: (Int) -> Unit,
    onEditClick: (Int) -> Unit,
    onDeleteClick: (Int) -> Unit,
    onViewResults: () -> Unit
) {
    VTCard(
        variant = CardVariant.Elevated,
        onClick = { onAssignmentClick(assignment.id) }
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
                    
                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = assignment.courseClass.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
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
            val progress = if (totalCount > 0) {
                submittedCount.toFloat() / totalCount
            } else {
                0f
            }
            
            VTProgressBar(
                progress = progress,
                showPercentage = false,
                color = PrimaryIndigo,
                height = 6
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons (Teacher only)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VTButton(
                    text = "결과 보기",
                    onClick = onViewResults,
                    variant = ButtonVariant.Primary,
                    size = ButtonSize.Small,
                    modifier = Modifier.weight(1f)
                )
                
                VTButton(
                    text = "편집",
                    onClick = { onEditClick(assignment.id) },
                    variant = ButtonVariant.Outline,
                    size = ButtonSize.Small,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun CustomStatusBadge(text: String) {
    val (textColor, backgroundColor) = when (text) {
        "시작 안함" -> PrimaryIndigo to Color(0xFFE3F2FD) // PrimaryIndigo의 연한 버전
        "진행 중" -> Warning to Color(0xFFFFF3E0) // Warning의 연한 버전
        "완료" -> Success to Color(0xFFE8F5E8) // Success의 연한 버전
        else -> Gray500 to Color(0xFFF5F5F5) // Gray500의 연한 버전
    }
    
    Box(
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
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
