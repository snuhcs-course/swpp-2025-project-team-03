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
import com.example.voicetutor.ui.viewmodel.StudentViewModel
import com.example.voicetutor.ui.viewmodel.ClassViewModel

@Composable
fun TeacherStudentsScreen(
    classId: Int? = null,
    onNavigateToSendMessage: () -> Unit = {},
    onNavigateToStudentDetail: (Int) -> Unit = {},
    onNavigateToMessage: (Int) -> Unit = {},
    onNavigateToAttendance: () -> Unit = {},
    navController: androidx.navigation.NavHostController? = null
) {
    val viewModel: StudentViewModel = hiltViewModel()
    val classViewModel: ClassViewModel = hiltViewModel()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val currentClass by classViewModel.currentClass.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    // 동적 클래스 정보 가져오기
    val className = currentClass?.name ?: "고등학교 1학년 A반"
    val subject = currentClass?.subject ?: "과목"
    val description = currentClass?.description ?: "과목 설명"
    
    // Load students and class data on first composition
    LaunchedEffect(classId) {
        viewModel.loadAllStudents(classId = classId?.toString())
        classId?.let { classViewModel.loadClassById(it) }
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
        // Class info header
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
                        text = className,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$subject - $description",
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
                        text = "김",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Class statistics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VTStatsCard(
                title = "평균 완료율",
                value = "정보 없음",
                icon = Icons.Filled.Done,
                iconColor = Success,
                modifier = Modifier.weight(1f)
            )
            
            VTStatsCard(
                title = "학생",
                value = students.size.toString(),
                icon = Icons.Filled.Person,
                iconColor = PrimaryIndigo,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Action buttons
        VTButton(
            text = "메시지 보내기",
            onClick = onNavigateToSendMessage,
            variant = ButtonVariant.Primary,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Message,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        // Students list
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "학생 목록",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.FilterList,
                        contentDescription = null,
                        tint = Gray500,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "정렬",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
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
                            text = "학생이 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600
                        )
                    }
                }
            } else {
                students.forEach { student ->
                    StudentCard(
                        student = student,
                        onViewStudent = { onNavigateToStudentDetail(student.id) },
                        onSendMessage = { onNavigateToMessage(student.id) }
                    )
                    
                    if (student != students.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        // Quick actions
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "빠른 작업",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionCard(
                        title = "성과 분석",
                        description = "반 전체 성과 보기",
                        icon = Icons.Filled.Analytics,
                        color = PrimaryIndigo,
                        onClick = { navController?.navigate("analytics") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    QuickActionCard(
                        title = "출석 관리",
                        description = "출석 현황 확인",
                        icon = Icons.Filled.EventAvailable,
                        color = Success,
                        onClick = onNavigateToAttendance,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun StudentCard(
    student: Student,
    onViewStudent: (Int) -> Unit,
    onSendMessage: (Int) -> Unit
) {
    VTCard(
        variant = CardVariant.Elevated,
        onClick = { onViewStudent(student.id) }
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(
                                PrimaryIndigo.copy(alpha = 0.1f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.name.first().toString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = student.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray800
                            )
                        }
                        Text(
                            text = student.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                        Text(
                            text = "최근 활동: ${"활동 없음"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                    }
                }
                
                IconButton(
                    onClick = { onSendMessage(student.id) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Message,
                        contentDescription = "메시지 보내기",
                        tint = PrimaryIndigo
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StudentStatItem(
                    label = "과제 완료",
                    value = "${0}/${0}",
                    progress = 0.toFloat() / 0,
                    color = PrimaryIndigo
                )
                
                StudentStatItem(
                    label = "평균 점수",
                    value = "${0}점",
                    progress = 0 / 100f,
                    color = when {
                        0 >= 90 -> Success
                        0 >= 80 -> Warning
                        else -> Error
                    }
                )
            }
        }
    }
}


@Composable
fun StudentStatItem(
    label: String,
    value: String,
    progress: Float,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Gray600
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(4.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                .background(Gray200)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    VTCard(
        variant = CardVariant.Outlined,
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Gray800
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Gray600
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherStudentsScreenPreview() {
    VoiceTutorTheme {
        TeacherStudentsScreen()
    }
}
