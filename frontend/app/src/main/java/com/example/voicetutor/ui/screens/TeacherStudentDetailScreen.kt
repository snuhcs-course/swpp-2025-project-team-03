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
import com.example.voicetutor.ui.viewmodel.StudentViewModel

@Composable
fun TeacherStudentDetailScreen(
    studentId: Int = 1, // 임시로 기본값 설정
    onNavigateToAllAssignments: () -> Unit = {},
    onNavigateToAssignmentDetail: (String) -> Unit = {},
    onNavigateToMessage: (Int) -> Unit = {},
    onNavigateToEdit: (Int) -> Unit = {}
) {
    val viewModel: StudentViewModel = hiltViewModel()
    val student by viewModel.currentStudent.collectAsStateWithLifecycle()
    val studentAssignments by viewModel.studentAssignments.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    // 동적 학생 이름 가져오기
    val studentName = student?.name ?: "학생"
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    // Load student data on first composition
    LaunchedEffect(studentId) {
        viewModel.loadStudentById(studentId)
        viewModel.loadStudentAssignments(studentId)
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            viewModel.clearError()
        }
    }
    
    // Convert Student to StudentDetail for UI
    val studentDetail = student?.let { s ->
        StudentDetail(
            name = s.name,
            email = s.email,
            studentId = s.id.toString(),
            className = s.className,
            joinDate = "", // Student 모델에 joinDate 없음
            lastActive = s.lastActive,
            status = "활성", // 상태 필드 제거됨
            totalAssignments = s.totalAssignments,
            completedAssignments = s.completedAssignments,
            averageScore = s.averageScore,
            totalStudyTime = "0시간" // Student 모델에 totalStudyTime 없음
        )
    }
    
    // Convert AssignmentData to StudentAssignment for UI
    val recentAssignments = studentAssignments.map { assignment ->
        StudentAssignment(
            title = assignment.title,
            subject = assignment.subject,
            dueDate = assignment.dueDate,
            submittedDate = "", // AssignmentData 모델에 submittedDate 없음
            score = 0, // AssignmentData 모델에 score 없음
            status = "미제출", // 임시로 미제출로 설정
            completionTime = "0분" // AssignmentData 모델에 completionTime 없음
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header removed - now handled by MainLayout
        
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
        } else if (studentDetail == null) {
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
                        text = "학생 정보를 찾을 수 없습니다",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Gray600
                    )
                }
            }
        } else {
            // Student info card - Welcome section style
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = studentDetail?.name ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${studentDetail?.className ?: ""} • ${studentDetail?.studentId ?: ""}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    
                    Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .shadow(
                            elevation = 4.dp,
                            shape = androidx.compose.foundation.shape.CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.1f),
                            spotColor = Color.Black.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = studentDetail?.name?.first()?.toString() ?: "?",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                }
            }
        }
        
        // Student details
        VTCard(variant = CardVariant.Elevated) {
            Column {
                Text(
                    text = "학생 정보",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                StudentInfoRow(
                    label = "학생 ID",
                    value = studentDetail?.studentId ?: ""
                )
                StudentInfoRow(
                    label = "학급",
                    value = studentDetail?.className ?: ""
                )
                StudentInfoRow(
                    label = "가입일",
                    value = studentDetail?.joinDate ?: ""
                )
                StudentInfoRow(
                    label = "최근 활동",
                    value = studentDetail?.lastActive ?: ""
                )
            }
        }
        
        
        // Recent assignments
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "최근 과제",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                
                VTButton(
                    text = "전체 보기",
                    onClick = onNavigateToAllAssignments,
                    variant = ButtonVariant.Ghost,
                    size = ButtonSize.Small
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            recentAssignments.forEach { assignment ->
                StudentAssignmentCard(
                    title = assignment.title,
                    subject = assignment.subject,
                    dueDate = assignment.dueDate,
                    progress = if (assignment.status == "완료") 1.0f else 0.3f,
                    isUrgent = assignment.dueDate.contains("오늘") || assignment.dueDate.contains("내일"),
                    onClick = { onNavigateToAssignmentDetail(assignment.title) }
                )
                
                if (assignment != recentAssignments.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VTButton(
                text = "메시지 보내기",
                onClick = { onNavigateToMessage(studentId) },
                variant = ButtonVariant.Outline,
                modifier = Modifier.weight(1f),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Message,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
            
            VTButton(
                text = "학생 편집",
                onClick = { onNavigateToEdit(studentId) },
                variant = ButtonVariant.Primary,
                modifier = Modifier.weight(1f),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun StudentInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray600
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Gray800
        )
    }
}


data class StudentDetail(
    val name: String,
    val email: String,
    val studentId: String,
    val className: String,
    val joinDate: String,
    val lastActive: String,
    val status: String,
    val totalAssignments: Int,
    val completedAssignments: Int,
    val averageScore: Int,
    val totalStudyTime: String
)

data class StudentAssignment(
    val title: String,
    val subject: String,
    val dueDate: String,
    val submittedDate: String,
    val score: Int,
    val status: String,
    val completionTime: String
)

@Preview(showBackground = true)
@Composable
fun TeacherStudentDetailScreenPreview() {
    VoiceTutorTheme {
        TeacherStudentDetailScreen()
    }
}
