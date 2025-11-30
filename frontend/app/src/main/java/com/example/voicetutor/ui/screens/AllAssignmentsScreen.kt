package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.List
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
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.utils.ErrorMessageMapper
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.utils.formatDueDate

@Composable
fun AllAssignmentsScreen(
    teacherId: String? = null,
    onNavigateToAssignmentResults: (Int) -> Unit = {},
    onNavigateToEditAssignment: (Int) -> Unit = {},
    onNavigateToAssignmentDetail: (Int) -> Unit = {},
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

    LaunchedEffect(actualTeacherId) {
        if (actualTeacherId != null) {
            viewModel.loadAllAssignments(teacherId = actualTeacherId)
        } else {
            viewModel.loadAllAssignments()
        }
    }

    LaunchedEffect(Unit) {
        if (actualTeacherId != null) {
            viewModel.loadAllAssignments(teacherId = actualTeacherId)
        }
    }

    LaunchedEffect(selectedFilter, actualTeacherId) {
        if (actualTeacherId != null) {
            val status = when (selectedFilter) {
                AssignmentFilter.ALL -> null
                AssignmentFilter.IN_PROGRESS -> AssignmentStatus.IN_PROGRESS
                AssignmentFilter.COMPLETED -> AssignmentStatus.COMPLETED
            }
            viewModel.loadAllAssignments(teacherId = actualTeacherId, status = status)
        }
    }

    // 네트워크 에러가 아닌 경우에만 에러를 클리어합니다.
    // 네트워크 에러는 assignments.isEmpty()일 때 구분하기 위해 유지합니다.
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            if (!ErrorMessageMapper.isNetworkError(errorMessage)) {
                viewModel.clearError()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = PrimaryIndigo.copy(alpha = 0.08f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                )
                .padding(20.dp),
        ) {
            Column {
                Text(
                    text = "모든 과제",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "총 ${assignments.size}개의 과제",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = selectedFilter == AssignmentFilter.ALL,
                onClick = { selectedFilter = AssignmentFilter.ALL },
                label = { Text("전체") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )

            FilterChip(
                selected = selectedFilter == AssignmentFilter.IN_PROGRESS,
                onClick = { selectedFilter = AssignmentFilter.IN_PROGRESS },
                label = { Text("진행중") },
            )

            FilterChip(
                selected = selectedFilter == AssignmentFilter.COMPLETED,
                onClick = { selectedFilter = AssignmentFilter.COMPLETED },
                label = { Text("마감") },
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = PrimaryIndigo,
                )
            }
        } else {
            if (assignments.isEmpty()) {
                // assignments.isEmpty()일 때 네트워크 에러인지 확인
                val isNetworkErrorState = error != null && ErrorMessageMapper.isNetworkError(error)
                val emptyStateMessage = if (isNetworkErrorState) {
                    "네트워크가 불안정합니다"
                } else {
                    "과제가 없습니다"
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Assignment,
                            contentDescription = null,
                            tint = Gray400,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = emptyStateMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600,
                        )
                    }
                }
            } else {
                val assignmentStatsMap = remember { mutableStateMapOf<Int, Pair<Int, Int>>() }

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
                        onViewResults = { onNavigateToAssignmentResults(assignment.id) },
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
    onViewResults: () -> Unit,
) {
    VTCard(
        variant = CardVariant.Elevated,
        onClick = { onAssignmentClick(assignment.id) },
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    // Subject badge
                    Box(
                        modifier = Modifier
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .background(PrimaryIndigo.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = assignment.courseClass.subject.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryIndigo,
                            fontWeight = FontWeight.Medium,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = assignment.courseClass.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                    )

                    Text(
                        text = "마감: ${formatDueDate(assignment.dueAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = "${assignment.totalQuestions}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo,
                    )
                    Text(
                        text = "문제",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val progress = if (totalCount > 0) {
                submittedCount.toFloat() / totalCount
            } else {
                0f
            }

            VTProgressBar(
                progress = progress,
                showPercentage = false,
                color = PrimaryIndigo,
                height = 6,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                VTButton(
                    text = "과제 결과",
                    onClick = onViewResults,
                    variant = ButtonVariant.Primary,
                    size = ButtonSize.Small,
                    modifier = Modifier.weight(1f),
                )

                VTButton(
                    text = "과제 편집",
                    onClick = { onEditClick(assignment.id) },
                    variant = ButtonVariant.Outline,
                    size = ButtonSize.Small,
                    modifier = Modifier.weight(1f),
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
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontWeight = FontWeight.Medium,
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
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = color,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun TypeBadge(type: String) {
    val (text, color, icon) = when (type) {
        "Quiz" -> Triple("퀴즈", Warning, Icons.Filled.Quiz)
        "Continuous" -> Triple("연속", Success, Icons.Filled.Schedule)
        "Discussion" -> Triple("토론", PrimaryPurple, Icons.AutoMirrored.Filled.Chat)
        else -> Triple("알 수 없음", MaterialTheme.colorScheme.onSurface, Icons.AutoMirrored.Filled.Help)
    }

    Box(
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = color,
                fontWeight = FontWeight.Medium,
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
