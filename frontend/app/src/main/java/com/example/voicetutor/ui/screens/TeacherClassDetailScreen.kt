package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.voicetutor.ui.viewmodel.StudentViewModel
import com.example.voicetutor.ui.viewmodel.ClassViewModel

data class ClassAssignment(
    val id: Int,
    val title: String,
    val subject: String,
    val dueDate: String,
    val completionRate: Float,
    val totalStudents: Int,
    val completedStudents: Int,
    val averageScore: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherClassDetailScreen(
    classId: Int? = null, // 실제 클래스 ID 사용
    className: String? = null, // 실제 클래스 이름 사용
    subject: String? = null, // 실제 과목명 사용
    onNavigateToClassMessage: () -> Unit = {},
    onNavigateToCreateAssignment: () -> Unit = {},
    onNavigateToAssignmentDetail: (String) -> Unit = {}
) {
    val assignmentViewModel: AssignmentViewModel = hiltViewModel()
    val studentViewModel: StudentViewModel = hiltViewModel()
    val classViewModel: ClassViewModel = hiltViewModel()
    
    val assignments by assignmentViewModel.assignments.collectAsStateWithLifecycle()
    val classStudents by classViewModel.classStudents.collectAsStateWithLifecycle()
    val allStudents by studentViewModel.students.collectAsStateWithLifecycle()
    val currentClass by classViewModel.currentClass.collectAsStateWithLifecycle()
    val isLoading by assignmentViewModel.isLoading.collectAsStateWithLifecycle()
    
    // 동적 클래스 정보 가져오기
    val dynamicClassName = currentClass?.name ?: className
    val dynamicSubject = currentClass?.subject?.name ?: subject
    val error by assignmentViewModel.error.collectAsStateWithLifecycle()
    
    // Load data on first composition and when screen becomes visible
    LaunchedEffect(Unit) {
        classId?.let { id ->
            // Load assignments for this class
            println("TeacherClassDetail - Loading assignments for class ID: $id")
            assignmentViewModel.loadAllAssignments(classId = id.toString())
            // Load students for this class
            classViewModel.loadClassStudents(id)
            // Load class data
            classViewModel.loadClassById(id)
        }
    }
    
    // Refresh assignments when assignments list changes (e.g., after creating new assignment)
    LaunchedEffect(assignments.size) {
        classId?.let { id ->
            println("TeacherClassDetail - Refreshing assignments due to size change: ${assignments.size}")
            assignmentViewModel.loadAllAssignments(classId = id.toString())
        }
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            assignmentViewModel.clearError()
        }
    }
    
    // 학생 등록 바텀시트 상태
    var showEnrollSheet by remember { mutableStateOf(false) }
    val selectedToEnroll = remember { mutableStateListOf<Int>() }
    
    // Convert API data to ClassAssignment format
    val classAssignments = assignments.map { assignment ->
        ClassAssignment(
            id = assignment.id,
            title = assignment.title,
                        subject = assignment.courseClass.subject.name,
            dueDate = assignment.dueAt,
            completionRate = 0.0f, // 임시로 0% 설정
            totalStudents = classStudents.size,
            completedStudents = classStudents.count { student ->
                // 임시로 완료된 학생 수 계산
                0 > 0
            },
            averageScore = 85 // 임시로 기본값 사용
        )
    }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
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
                        text = dynamicClassName ?: "클래스",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = dynamicSubject ?: "과목",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
        
        item {
            // Stats overview
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VTStatsCard(
                    title = "총 학생",
                    value = "${classStudents.size}명",
                    icon = Icons.Filled.People,
                    iconColor = PrimaryIndigo,
                    modifier = Modifier.weight(1f),
                    variant = CardVariant.Gradient
                )
                
                VTStatsCard(
                    title = "진행 중인 과제",
                    value = "${classAssignments.count { it.completionRate < 1.0f }}개",
                    icon = Icons.Filled.Assignment,
                    iconColor = Warning,
                    modifier = Modifier.weight(1f),
                    variant = CardVariant.Gradient
                )
                
                VTStatsCard(
                    title = "평균 점수",
                    value = "${if (classAssignments.isNotEmpty()) classAssignments.map { it.averageScore }.average().toInt() else 0}점",
                    icon = Icons.Filled.Star,
                    iconColor = Success,
                    modifier = Modifier.weight(1f),
                    variant = CardVariant.Gradient
                )
            }
        }
        
        item {
            // Quick actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VTButton(
                    text = "클래스 메시지",
                    onClick = onNavigateToClassMessage,
                    variant = ButtonVariant.Gradient,
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Message,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
                
                VTButton(
                    text = "과제 생성",
                    onClick = onNavigateToCreateAssignment,
                    variant = ButtonVariant.Outline,
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )

                VTButton(
                    text = "학생 등록하기",
                    onClick = {
                        // 전체 학생 목록 로드 후 시트 오픈
                        studentViewModel.loadAllStudents()
                        selectedToEnroll.clear()
                        showEnrollSheet = true
                    },
                    variant = ButtonVariant.Primary,
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }
        }
        
        item {
            // Assignments section header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "과제 목록",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                
                Text(
                    text = "${classAssignments.size}개",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Loading indicator
        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PrimaryIndigo
                    )
                }
            }
        } else if (classAssignments.isEmpty()) {
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
            // Assignments list
            items(classAssignments) { assignment ->
                ClassAssignmentCard(
                    assignment = assignment,
                    onNavigateToAssignmentDetail = onNavigateToAssignmentDetail
                )
            }
        }
    }

    // 학생 등록 바텀시트
    if (showEnrollSheet) {
        ModalBottomSheet(onDismissRequest = { showEnrollSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("학생 등록", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))

                // 이미 등록된 학생 제외 목록
                val enrolledIds = classStudents.map { it.id }.toSet()
                val candidates = allStudents.filter { it.id !in enrolledIds }

                if (candidates.isEmpty()) {
                    Text("등록 가능한 학생이 없습니다.", color = Gray600)
                } else {
                    candidates.forEach { student ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(student.name ?: "학생", fontWeight = FontWeight.Medium)
                                Text(student.email, style = MaterialTheme.typography.bodySmall, color = Gray600)
                            }
                            val checked = selectedToEnroll.contains(student.id)
                            Checkbox(checked = checked, onCheckedChange = { isChecked ->
                                if (isChecked) selectedToEnroll.add(student.id) else selectedToEnroll.remove(student.id)
                            })
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    VTButton(
                        text = "취소",
                        onClick = { showEnrollSheet = false },
                        variant = ButtonVariant.Outline,
                        modifier = Modifier.weight(1f)
                    )
                    VTButton(
                        text = "등록",
                        onClick = {
                            classId?.let { id ->
                                selectedToEnroll.forEach { sid ->
                                    classViewModel.enrollStudentToClass(classId = id, studentId = sid)
                                }
                                // 완료 후 갱신 및 닫기
                                classViewModel.loadClassStudents(id)
                            }
                            showEnrollSheet = false
                        },
                        variant = ButtonVariant.Primary,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ClassAssignmentCard(
    assignment: ClassAssignment,
    onNavigateToAssignmentDetail: (String) -> Unit = {}
) {
    VTCard(
        variant = CardVariant.Elevated,
        onClick = { onNavigateToAssignmentDetail(assignment.title) }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Assignment header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = assignment.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    Text(
                        text = assignment.subject,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
                
                // Status badge
                Box(
                    modifier = Modifier
                        .background(
                            color = when {
                                assignment.completionRate >= 0.8f -> Success.copy(alpha = 0.1f)
                                assignment.completionRate >= 0.5f -> Warning.copy(alpha = 0.1f)
                                else -> Error.copy(alpha = 0.1f)
                            },
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            assignment.completionRate >= 0.8f -> "완료"
                            assignment.completionRate >= 0.5f -> "진행중"
                            else -> "시작전"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            assignment.completionRate >= 0.8f -> Success
                            assignment.completionRate >= 0.5f -> Warning
                            else -> Error
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Progress info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "완료율",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = "${(assignment.completionRate * 100).toInt()}%",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo
                    )
                }
                
                Column {
                    Text(
                        text = "완료 학생",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = "${assignment.completedStudents}/${assignment.totalStudents}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gray800
                    )
                }
                
                Column {
                    Text(
                        text = "평균 점수",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = "${assignment.averageScore}점",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (assignment.averageScore >= 80) Success else Warning
                    )
                }
            }
            
            // Progress bar
            VTProgressBar(
                progress = assignment.completionRate,
                showPercentage = false
            )
            
            // Due date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Schedule,
                    contentDescription = null,
                    tint = Gray500,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "마감일: ${assignment.dueDate}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
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