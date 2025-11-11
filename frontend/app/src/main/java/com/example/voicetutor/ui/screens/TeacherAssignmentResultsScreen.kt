package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAssignmentResultsScreen(
    assignmentViewModel: AssignmentViewModel? = null,
    assignmentId: Int = 0,
    assignmentTitle: String? = null // For backward compatibility
) {
    val viewModel: AssignmentViewModel = assignmentViewModel ?: hiltViewModel()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val students by viewModel.assignmentResults.collectAsStateWithLifecycle()
    val currentAssignment by viewModel.currentAssignment.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val assignmentStats by viewModel.assignmentStatistics.collectAsStateWithLifecycle()
    
    // 모달 상태 관리
    var selectedStudent by remember { mutableStateOf<StudentResult?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Find assignment by ID or title from the assignments list
    // "과목 - 제목" 형식도 처리 가능하도록 수정
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
    
    // 동적 과제 제목 가져오기
    val dynamicAssignmentTitle = currentAssignment?.title ?: (targetAssignment?.title ?: assignmentTitle ?: "과제")
    
    // Load assignment data on first composition
    LaunchedEffect(assignmentId, targetAssignment?.id) {
        if (assignmentId > 0) {
            println("TeacherAssignmentResults - Loading assignment by ID: $assignmentId")
            viewModel.loadAssignmentById(assignmentId)
            viewModel.loadAssignmentStudentResults(assignmentId)
        } else {
        targetAssignment?.let { target ->
            println("TeacherAssignmentResults - Loading assignment: ${target.title} (ID: ${target.id})")
            viewModel.loadAssignmentById(target.id)
            viewModel.loadAssignmentStudentResults(target.id)
            }
        }
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = PrimaryIndigo,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                )
                .shadow(
                    elevation = 8.dp,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    ambientColor = PrimaryIndigo.copy(alpha = 0.3f),
                    spotColor = PrimaryIndigo.copy(alpha = 0.3f)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = dynamicAssignmentTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "학생별 과제 결과를 확인하고 피드백을 제공하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
        
        // Stats cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VTStatsCard(
                title = "제출 학생",
                value = "${students.count { it.status == "완료" }}명",
                icon = Icons.Filled.CheckCircle,
                iconColor = Success,
                modifier = Modifier.weight(1f),
                variant = CardVariant.Gradient
            )
            
            VTStatsCard(
                title = "평균 점수",
                value = if (students.isNotEmpty() && students.any { it.status == "완료" }) {
                    students.filter { it.status == "완료" }.map { it.score }.average().toInt().toString()
                } else "-",
                icon = Icons.Filled.Star,
                iconColor = Warning,
                modifier = Modifier.weight(1f),
                variant = CardVariant.Gradient
            )
        }
        
        // Students list
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
                // 학생 결과 표시 (API 데이터가 있을 때만 표시됨)
                students.forEachIndexed { index, student ->
                    TeacherAssignmentResultCard(
                        student = student,
                        onStudentClick = { 
                            selectedStudent = student
                        }
                    )
                    
                    if (index < students.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
    
    // 학생 상세 결과 모달
    selectedStudent?.let { student ->
        ModalBottomSheet(
            onDismissRequest = { selectedStudent = null },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            StudentResultDetailModal(
                student = student,
                assignmentTitle = dynamicAssignmentTitle,
                onDismiss = { selectedStudent = null }
            )
        }
    }
}

@Composable
fun TeacherAssignmentResultCard(
    student: StudentResult,
    onStudentClick: () -> Unit
) {
    VTCard(
        variant = CardVariant.Elevated,
        onClick = onStudentClick
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Student info header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.first().toString(),
                        color = PrimaryIndigo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                }
                
                // Status badge
                Box(
                    modifier = Modifier
                        .background(
                            color = if (student.status == "완료") Success.copy(alpha = 0.1f) else Warning.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = student.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (student.status == "완료") Success else Warning,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Grade and time info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "등급",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    val grade = scoreToGrade(student.score)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = getGradeColor(grade).copy(alpha = 0.15f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = grade,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = getGradeColor(grade),
                            fontSize = 24.sp
                        )
                    }
                }
                
                Column {
                    Text(
                        text = "소요 시간",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = formatDuration(student.startedAt, student.submittedAt),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Gray800
                    )
                }
                
                Column {
                    Text(
                        text = "제출 시간",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = formatSubmittedTime(student.submittedAt),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Gray800
                    )
                }
            }
            
            // Sample answers preview
            if (student.answers.isNotEmpty()) {
                Column {
                    Text(
                        text = "답변 미리보기",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Gray700
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = student.answers.first(),
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// Helper function to format submitted time - using common utility
private fun formatSubmittedTime(isoTime: String): String {
    return com.example.voicetutor.utils.formatSubmittedTime(isoTime)
}

// Helper function to convert score to grade
private fun scoreToGrade(score: Int): String {
    return when {
        score >= 90 -> "A"
        score >= 80 -> "B"
        score >= 70 -> "C"
        score >= 60 -> "D"
        else -> "F"
    }
}

// Helper function to get grade color
private fun getGradeColor(grade: String): Color {
    return when (grade) {
        "A" -> Success
        "B" -> Color(0xFF4CAF50)
        "C" -> Warning
        "D" -> Color(0xFFFF9800)
        "F" -> Error
        else -> Gray600
    }
}

// Helper function to format duration between startedAt and submittedAt
private fun formatDuration(startIso: String?, endIso: String?): String {
    return try {
        println("formatDuration start!")
        if (startIso.isNullOrEmpty() || endIso.isNullOrEmpty()) {
            println("startIso or endIso is null, $startIso || $endIso")
            return "정보 없음"
        }
        val start = parseIsoToMillis(startIso)
        val end = parseIsoToMillis(endIso)
        if (start == null || end == null || end <= start) {
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

// Parses basic ISO8601 like "yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'" or without fractional seconds
private fun parseIsoToMillis(iso: String): Long? {
    return try {
        // Remove timezone 'Z' and fractional seconds for SimpleDateFormat compatibility
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

@Composable
fun StudentResultDetailModal(
    student: StudentResult,
    assignmentTitle: String,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Gray800
                )
                Text(
                    text = assignmentTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "닫기",
                    tint = Gray600
                )
            }
        }
        
        Divider()
        
        // Student info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(PrimaryIndigo.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.name.first().toString(),
                    color = PrimaryIndigo,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "학생 ID: ${student.studentId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .background(
                            color = if (student.status == "완료") Success.copy(alpha = 0.1f) else Warning.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = student.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (student.status == "완료") Success else Warning,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        // Stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 등급 섹션
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "등급",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                val grade = scoreToGrade(student.score)
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = getGradeColor(grade).copy(alpha = 0.15f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = grade,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = getGradeColor(grade),
                        fontSize = 32.sp
                    )
                }
            }
            
            // 점수 섹션
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "점수",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${student.score}점",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryIndigo
                )
            }
        }
        
        Divider()
        
        // Time info
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "소요 시간",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatDuration(student.startedAt, student.submittedAt),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "제출 시간",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatSubmittedTime(student.submittedAt),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
            }
        }
        
        // Full detail button
        // 상세 페이지 삭제로 버튼 제거
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherAssignmentResultsScreenPreview() {
    VoiceTutorTheme {
        TeacherAssignmentResultsScreen()
    }
}