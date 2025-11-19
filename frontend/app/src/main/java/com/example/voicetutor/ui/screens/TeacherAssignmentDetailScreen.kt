package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel

@Composable
fun TeacherAssignmentDetailScreen(
    assignmentViewModel: AssignmentViewModel? = null,
    assignmentId: Int = 0,
    assignmentTitle: String? = null,
    onNavigateToEditAssignment: (Int) -> Unit = {},
    onNavigateToStudentDetail: (studentId: String, assignmentId: Int, assignmentTitle: String) -> Unit = { _, _, _ -> }
) {
    val viewModel: AssignmentViewModel = assignmentViewModel ?: hiltViewModel()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val assignment by viewModel.currentAssignment.collectAsStateWithLifecycle()
    val students by viewModel.assignmentResults.collectAsStateWithLifecycle()
    val assignmentStatistics by viewModel.assignmentStatistics.collectAsStateWithLifecycle()

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

    val dynamicAssignmentTitle = assignment?.title ?: (targetAssignment?.title ?: "과제")
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val resolvedAssignmentId = targetAssignment?.id ?: assignment?.id ?: assignmentId

    var loadedStatsForAssignmentId by remember { mutableStateOf<Int?>(null) }
    val hasBasicAssignmentData = assignment != null || targetAssignment != null
    val isInitialLoading = isLoading && !hasBasicAssignmentData
    
    LaunchedEffect(assignmentId) {
        if (assignmentId > 0) {
            println("TeacherAssignmentDetail - Loading assignment ID: $assignmentId")
            viewModel.loadAssignmentById(assignmentId)
        } else {
            targetAssignment?.let { target ->
                println("TeacherAssignmentDetail - Loading assignment: ${target.title} (ID: ${target.id})")
                viewModel.loadAssignmentById(target.id)
            }
        }
    }

    LaunchedEffect(assignment?.id) {
        assignment?.let { a ->
            if (a.id != loadedStatsForAssignmentId) {
                println("TeacherAssignmentDetail - Assignment loaded, loading statistics and student results: ${a.title} (ID: ${a.id})")
                loadedStatsForAssignmentId = a.id
                viewModel.loadAssignmentStatisticsAndResults(a.id, a.courseClass.studentCount)
            }
        }
    }

    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            viewModel.clearError()
        }
    }

    val assignmentDetail = assignment?.let { a ->
        AssignmentDetail(
            title = a.title,
            subject = a.courseClass.subject.name,
            className = a.courseClass.name,
            dueDate = a.dueAt,
            createdAt = a.createdAt ?: "",
            status = "IN_PROGRESS",
            type = "연속형",
            description = a.description ?: "",
            totalStudents = assignmentStatistics?.totalStudents ?: a.courseClass.studentCount,
            submittedStudents = assignmentStatistics?.submittedStudents ?: 0,
            averageScore = assignmentStatistics?.averageScore ?: 0,
            completionRate = assignmentStatistics?.completionRate ?: 0
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isInitialLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = PrimaryIndigo
                )
            }
        } else if (assignmentDetail == null && !hasBasicAssignmentData) {
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
                        text = "과제 정보를 찾을 수 없습니다",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Gray600
                    )
                }
            }
        } else {
            assignmentDetail?.let { detail ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = PrimaryIndigo.copy(alpha = 0.08f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = detail.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${detail.subject} • ${detail.className}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VTStatsCard(
                        title = "제출률",
                        value = "${detail.completionRate}%",
                        icon = Icons.Filled.Assignment,
                        iconColor = PrimaryIndigo,
                        variant = CardVariant.Elevated,
                        modifier = Modifier.weight(1f),
                        layout = StatsCardLayout.Vertical
                    )

                    VTStatsCard(
                        title = "평균 점수",
                        value = "${detail.averageScore}점",
                        icon = Icons.Filled.Grade,
                        iconColor = Success,
                        variant = CardVariant.Elevated,
                        modifier = Modifier.weight(1f),
                        layout = StatsCardLayout.Vertical
                    )

                    VTStatsCard(
                        title = "제출 학생",
                        value = "${detail.submittedStudents}/${detail.totalStudents}",
                        icon = Icons.Filled.People,
                        iconColor = Warning,
                        variant = CardVariant.Elevated,
                        modifier = Modifier.weight(1f),
                        layout = StatsCardLayout.Vertical
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    Text(
                        text = "과제 내용",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VTButton(
                            text = "과제 편집",
                            onClick = {
                                val assignmentId = assignment?.id ?: targetAssignment?.id ?: 0
                                if (assignmentId > 0) {
                                    onNavigateToEditAssignment(assignmentId)
                                }
                            },
                            variant = ButtonVariant.Outline,
                            size = ButtonSize.Small,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Edit,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                }

                VTCard(variant = CardVariant.Elevated) {
                    Column {
                        Text(
                            text = detail.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray700,
                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
                        )
                    }
                }
            }

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
                    students.forEachIndexed { index, student ->
                        TeacherAssignmentResultCard(
                            student = student,
                            onStudentClick = {
                                val destinationAssignmentId = resolvedAssignmentId
                                if (destinationAssignmentId != 0) {
                                    onNavigateToStudentDetail(student.studentId, destinationAssignmentId, dynamicAssignmentTitle)
                                }
                            }
                        )

                        if (index < students.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
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
    val isCompleted = student.status == "완료"
    
    VTCard(
        variant = CardVariant.Elevated,
        onClick = onStudentClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 첫 번째 Row: 이름과 상태
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 이름
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 상태 - 간단한 점과 텍스트
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (isCompleted) Success else Warning
                            )
                    )
                    Text(
                        text = student.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isCompleted) Success else Warning,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // 두 번째 Row: 제출 시간과 평균 점수 - 한 줄에 간결하게
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 제출 시간 - 왼쪽 고정
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Schedule,
                        contentDescription = null,
                        tint = Gray500,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "제출: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = formatSubmittedTime(student.submittedAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }

                // 평균 점수 - 오른쪽 정렬
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Grade,
                        contentDescription = null,
                        tint = Gray500,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "점수: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = "${student.score}점",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Gray800
                    )
                }
            }

            if (student.answers.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "답변 미리보기",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Gray600
                    )
                    Text(
                        text = student.answers.first(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        lineHeight = MaterialTheme.typography.bodySmall.lineHeight * 1.3
                    )
                }
            }
        }
    }
}

private fun formatSubmittedTime(isoTime: String): String {
    return com.example.voicetutor.utils.formatSubmittedTime(isoTime)
}

private fun formatDuration(startIso: String?, endIso: String?): String {
    return try {
        println("formatDuration start!")
        if (startIso.isNullOrEmpty() || endIso.isNullOrEmpty()) {
            println("startIso or endIso is null, $startIso || $endIso")
            return "정보 없음"
        }
        val start = parseIsoToMillis(startIso)
        val end = parseIsoToMillis(endIso)
        if (start == null || end == null || end < start) {
            println("start or end is null $startIso ,$endIso")
            return "정보 없음"
        }
        val diffMs = end - start
        val totalSeconds = diffMs / 1000
        val hours = (totalSeconds / 3600).toInt()
        val minutes = ((totalSeconds % 3600) / 60).toInt()
        val seconds = (totalSeconds % 60).toInt()
        if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    } catch (e: Exception) {
        println("FormatDuration: Exception!! $startIso $endIso")
        "정보 없음"
    }
}

private fun parseIsoToMillis(iso: String): Long? {
    return try {
        val cleaned = iso.replace("Z", "").let { raw ->
            val dotIdx = raw.indexOf('.')
            if (dotIdx != -1) raw.substring(0, dotIdx) else raw
        }
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        sdf.parse(cleaned)?.time
    } catch (e: Exception) {
        null
    }
}

data class AssignmentDetail(
    val title: String,
    val subject: String,
    val className: String,
    val dueDate: String,
    val createdAt: String,
    val status: String,
    val type: String,
    val description: String,
    val totalStudents: Int,
    val submittedStudents: Int,
    val averageScore: Int,
    val completionRate: Int
)

data class StudentSubmission(
    val name: String,
    val studentId: String,
    val submittedAt: String,
    val score: Int,
    val status: String
)

@Preview(showBackground = true)
@Composable
fun TeacherAssignmentDetailScreenPreview() {
    VoiceTutorTheme {
        TeacherAssignmentDetailScreen()
    }
}
