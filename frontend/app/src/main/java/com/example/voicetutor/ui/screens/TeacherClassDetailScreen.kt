package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.voicetutor.ui.viewmodel.ClassViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

data class ClassAssignment(
    val id: Int,
    val title: String,
    val subject: String,
    val dueDate: String,
    val completionRate: Float,
    val totalStudents: Int,
    val completedStudents: Int,
    val averageScore: Int,
)

private const val HIGH_SCORE_THRESHOLD = 80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherClassDetailScreen(
    classId: Int? = null,
    className: String? = null,
    subject: String? = null,
    onNavigateToCreateAssignment: (Int?) -> Unit = { _ -> },
    onNavigateToAssignmentDetail: (Int) -> Unit = {},
) {
    val assignmentViewModel: AssignmentViewModel = hiltViewModel()
    val classViewModel: ClassViewModel = hiltViewModel()

    val assignments by assignmentViewModel.assignments.collectAsStateWithLifecycle()
    val classStudents by classViewModel.classStudents.collectAsStateWithLifecycle()
    val currentClass by classViewModel.currentClass.collectAsStateWithLifecycle()
    val isLoading by assignmentViewModel.isLoading.collectAsStateWithLifecycle()
    val error by assignmentViewModel.error.collectAsStateWithLifecycle()

    val dynamicClassName = currentClass?.name ?: className
    val dynamicSubject = currentClass?.subject?.name ?: subject
    var selectedFilter by remember { mutableStateOf(AssignmentFilter.ALL) }

    LaunchedEffect(Unit) {
        classId?.let { id ->
            assignmentViewModel.loadAllAssignments(classId = id.toString())
            classViewModel.loadClassStudents(id)
            classViewModel.loadClassById(id)
        }
    }

    LaunchedEffect(assignments.size) {
        classId?.let { id ->
            assignmentViewModel.loadAllAssignments(classId = id.toString())
        }
    }

    // 네트워크 에러가 아닌 경우에만 에러를 클리어합니다.
    // 네트워크 에러는 classAssignments.isEmpty()일 때 구분하기 위해 유지합니다.
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            if (!ErrorMessageMapper.isNetworkError(errorMessage)) {
                assignmentViewModel.clearError()
            }
        }
    }

    val assignmentStatsMap = remember { mutableStateMapOf<Int, Triple<Int, Int, Int>>() }

    assignments.forEach { assignment ->
        LaunchedEffect(assignment.id) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val stats = assignmentViewModel.getAssignmentSubmissionStats(assignment.id)
                assignmentStatsMap[assignment.id] = Triple(
                    stats.submittedStudents,
                    stats.totalStudents,
                    stats.averageScore,
                )
            }
        }
    }

    val allClassAssignments = assignments.map { assignment ->
        val stats = assignmentStatsMap[assignment.id] ?: Triple(0, classStudents.size, 0)
        ClassAssignment(
            id = assignment.id,
            title = assignment.title,
            subject = assignment.courseClass.subject.name,
            dueDate = assignment.dueAt,
            completionRate = if (stats.second > 0) {
                stats.first.toFloat() / stats.second
            } else {
                0.0f
            },
            totalStudents = stats.second,
            completedStudents = stats.first,
            averageScore = stats.third,
        )
    }

    val classAssignments = remember(allClassAssignments, selectedFilter) {
        filterAssignmentsByStatus(allClassAssignments, selectedFilter)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = PrimaryIndigo.copy(alpha = 0.08f),
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(20.dp),
            ) {
                Column {
                    Text(
                        text = dynamicClassName ?: "수업",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = dynamicSubject ?: "과목",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600,
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                VTStatsCard(
                    title = "학생",
                    value = "${classStudents.size}명",
                    icon = Icons.Filled.People,
                    iconColor = PrimaryIndigo,
                    modifier = Modifier.weight(1f),
                    variant = CardVariant.Gradient,
                    layout = StatsCardLayout.Horizontal,
                )

                VTStatsCard(
                    title = "과제",
                    value = "${classAssignments.size}개",
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    iconColor = Warning,
                    modifier = Modifier.weight(1f),
                    variant = CardVariant.Gradient,
                    layout = StatsCardLayout.Horizontal,
                )
            }
        }

        item {
            VTButton(
                text = "과제 생성",
                onClick = { onNavigateToCreateAssignment(classId) },
                variant = ButtonVariant.Outline,
                size = ButtonSize.Small,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
            )
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = " 과제 목록",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800,
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = selectedFilter == AssignmentFilter.ALL,
                        onClick = { selectedFilter = AssignmentFilter.ALL },
                        label = { Text("전체", style = MaterialTheme.typography.bodySmall) },
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
                        label = { Text("진행중", style = MaterialTheme.typography.bodySmall) },
                    )

                    FilterChip(
                        selected = selectedFilter == AssignmentFilter.COMPLETED,
                        onClick = { selectedFilter = AssignmentFilter.COMPLETED },
                        label = { Text("마감", style = MaterialTheme.typography.bodySmall) },
                    )
                }
            }
        }

        when {
            isLoading -> {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = PrimaryIndigo)
                    }
                }
            }
            classAssignments.isEmpty() -> {
                item {
                    // classAssignments.isEmpty()일 때 네트워크 에러인지 확인
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
                }
            }
            else -> {
                items(classAssignments) { assignment ->
                    ClassAssignmentCard(
                        assignment = assignment,
                        onNavigateToAssignmentDetail = { onNavigateToAssignmentDetail(assignment.id) },
                    )
                }
            }
        }
    }
}

private fun filterAssignmentsByStatus(
    assignments: List<ClassAssignment>,
    filter: AssignmentFilter,
): List<ClassAssignment> {
    val now = Calendar.getInstance()
    return when (filter) {
        AssignmentFilter.ALL -> assignments
        AssignmentFilter.IN_PROGRESS -> assignments.filter { isAssignmentInProgress(it.dueDate, now) }
        AssignmentFilter.COMPLETED -> assignments.filter { isAssignmentCompleted(it.dueDate, now) }
    }
}

private fun isAssignmentInProgress(dueDate: String, now: Calendar): Boolean {
    return try {
        val dueDateCalendar = parseIsoDate(dueDate)
        dueDateCalendar != null && dueDateCalendar.after(now)
    } catch (_: Exception) {
        true
    }
}

private fun isAssignmentCompleted(dueDate: String, now: Calendar): Boolean {
    return try {
        val dueDateCalendar = parseIsoDate(dueDate)
        if (dueDateCalendar == null) {
            false
        } else {
            dueDateCalendar.before(now) || dueDateCalendar.timeInMillis == now.timeInMillis
        }
    } catch (_: Exception) {
        false
    }
}

private fun parseIsoDate(isoDate: String): Calendar? {
    return try {
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss",
        )

        for (pattern in formats) {
            try {
                val sdf = SimpleDateFormat(pattern, Locale.getDefault())
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                val date = sdf.parse(isoDate)
                if (date != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = date
                    return calendar
                }
            } catch (_: Exception) {
                continue
            }
        }
        null
    } catch (_: Exception) {
        null
    }
}

@Composable
fun ClassAssignmentCard(
    assignment: ClassAssignment,
    onNavigateToAssignmentDetail: (Int) -> Unit = {},
) {
    VTCard(
        variant = CardVariant.Elevated,
        onClick = { onNavigateToAssignmentDetail(assignment.id) },
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = assignment.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Gray800,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "제출률",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                    )
                    Text(
                        text = "${(assignment.completionRate * 100).toInt()}%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo,
                    )
                }

                Column {
                    Text(
                        text = "제출 학생",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                    )
                    Text(
                        text = "${assignment.completedStudents}/${assignment.totalStudents}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gray800,
                    )
                }

                Column {
                    Text(
                        text = "평균 점수",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                    )
                    Text(
                        text = "${assignment.averageScore}점",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (assignment.averageScore >= HIGH_SCORE_THRESHOLD) Success else Warning,
                    )
                }
            }

            VTProgressBar(
                progress = assignment.completionRate,
                showPercentage = false,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = Gray500,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "마감일: ${com.example.voicetutor.utils.formatDueDate(assignment.dueDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherClassDetailScreenPreview() {
    VoiceTutorTheme {
        TeacherClassDetailScreen()
    }
}
