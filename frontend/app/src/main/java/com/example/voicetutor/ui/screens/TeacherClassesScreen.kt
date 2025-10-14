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
import com.example.voicetutor.ui.viewmodel.ClassViewModel
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel

data class ClassRoom(
    val id: Int,
    val name: String,
    val subject: String,
    val description: String,
    val studentCount: Int,
    val assignmentCount: Int,
    val completionRate: Float,
    val color: Color,
    val recentActivity: String = "2시간 전"
)

@Composable
fun TeacherClassesScreen(
    teacherId: String = "1", // 임시로 기본값 설정, 나중에 인증에서 가져오도록 수정
    onNavigateToClassDetail: (String) -> Unit = {},
    onNavigateToCreateClass: () -> Unit = {},
    onNavigateToCreateAssignment: () -> Unit = {},
    onNavigateToStudents: (Int) -> Unit = {}
) {
    val classViewModel: ClassViewModel = hiltViewModel()
    val assignmentViewModel: AssignmentViewModel = hiltViewModel()
    val classes by classViewModel.classes.collectAsStateWithLifecycle()
    val assignments by assignmentViewModel.assignments.collectAsStateWithLifecycle()
    val isLoading by classViewModel.isLoading.collectAsStateWithLifecycle()
    val error by classViewModel.error.collectAsStateWithLifecycle()
    
    // Load classes and assignments on first composition
    LaunchedEffect(teacherId) {
        classViewModel.loadClasses(teacherId)
        assignmentViewModel.loadAllAssignments(teacherId = teacherId)
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            classViewModel.clearError()
        }
    }
    
    // Convert ClassData to ClassRoom for UI
    val classRooms = classes.map { classData ->
        // Calculate assignment count and completion rate from actual data
        val classAssignments = assignments.filter { it.classId == classData.id }
        val assignmentCount = classAssignments.size
        val completedAssignments = classAssignments.count { it.status == AssignmentStatus.COMPLETED }
        val completionRate = if (assignmentCount > 0) completedAssignments.toFloat() / assignmentCount else 0.0f
        
        ClassRoom(
            id = classData.id,
            name = classData.name,
            subject = classData.subject,
            description = classData.description,
            studentCount = classData.studentCount,
            assignmentCount = assignmentCount,
            completionRate = completionRate,
            color = when (classData.id % 4) {
                0 -> PrimaryIndigo
                1 -> Success
                2 -> Warning
                else -> Error
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "내 반 관리",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Gray800
                )
                Text(
                    text = "반을 관리하고 과제를 생성하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
            }
            
            VTButton(
                text = "새 반 만들기",
                onClick = onNavigateToCreateClass,
                variant = ButtonVariant.Primary,
                size = ButtonSize.Small,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }
            )
        }
        
        // Quick stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VTStatsCard(
                title = "총 반",
                value = classRooms.size.toString(),
                icon = Icons.Filled.School,
                iconColor = PrimaryIndigo,
                modifier = Modifier.weight(1f)
            )
            
            VTStatsCard(
                title = "총 학생",
                value = classRooms.sumOf { it.studentCount }.toString(),
                icon = Icons.Filled.People,
                iconColor = Success,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Classes list
        Column {
            Text(
                text = "반 목록",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray800
            )
            
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
            } else if (classRooms.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.School,
                            contentDescription = null,
                            tint = Gray400,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "반이 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600
                        )
                    }
                }
            } else {
                classRooms.forEach { classRoom ->
                    ClassCard(
                        classRoom = classRoom,
                        onClassClick = { onNavigateToClassDetail(classRoom.name) },
                        onCreateAssignment = { onNavigateToCreateAssignment() },
                        onViewStudents = { onNavigateToStudents(classRoom.id) }
                    )
                    
                    if (classRoom != classRooms.last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
        
        // Recent activity
        VTCard(variant = CardVariant.Elevated) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.History,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "최근 활동",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RecentActivityItem(
                        title = "고1 A반에 새 과제 생성",
                        description = "생물학 - 세포분열 리뷰",
                        time = "30분 전",
                        icon = Icons.Filled.Assignment,
                        color = PrimaryIndigo
                    )
                    
                    RecentActivityItem(
                        title = "고2 B반 과제 결과 확인",
                        description = "화학 - 원소주기율표 퀴즈",
                        time = "1시간 전",
                        icon = Icons.Filled.Assessment,
                        color = Success
                    )
                    
                    RecentActivityItem(
                        title = "고3 C반 학생 피드백",
                        description = "물리학 과제에 대한 개별 피드백 제공",
                        time = "2시간 전",
                        icon = Icons.Filled.Feedback,
                        color = Warning
                    )
                }
            }
        }
    }
}

@Composable
fun ClassCard(
    classRoom: ClassRoom,
    onClassClick: (Int) -> Unit,
    onCreateAssignment: (Int) -> Unit,
    onViewStudents: (Int) -> Unit
) {
    VTCard(
        variant = CardVariant.Elevated,
        onClick = { onClassClick(classRoom.id) }
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
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .background(classRoom.color.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MenuBook,
                            contentDescription = null,
                            tint = classRoom.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = classRoom.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                        Text(
                            text = classRoom.subject,
                            style = MaterialTheme.typography.bodyMedium,
                            color = classRoom.color,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = classRoom.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${(classRoom.completionRate * 100).toInt()}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = classRoom.color
                    )
                    Text(
                        text = "완료율",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ClassStatItem(
                    icon = Icons.Filled.People,
                    value = classRoom.studentCount.toString(),
                    label = "학생",
                    color = Gray600
                )
                
                ClassStatItem(
                    icon = Icons.Filled.Assignment,
                    value = classRoom.assignmentCount.toString(),
                    label = "과제",
                    color = Gray600
                )
                
                ClassStatItem(
                    icon = Icons.Filled.Schedule,
                    value = classRoom.recentActivity,
                    label = "최근 활동",
                    color = Gray600
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Progress bar
            VTProgressBar(
                progress = classRoom.completionRate,
                showPercentage = false,
                color = classRoom.color,
                height = 6
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                VTButton(
                    text = "과제 생성",
                    onClick = { onCreateAssignment(classRoom.id) },
                    variant = ButtonVariant.Outline,
                    size = ButtonSize.Small,
                    modifier = Modifier.weight(1f)
                )
                
                VTButton(
                    text = "학생 보기",
                    onClick = { onViewStudents(classRoom.id) },
                    variant = ButtonVariant.Outline,
                    size = ButtonSize.Small,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ClassStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Gray800
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = color
        )
    }
}

@Composable
fun RecentActivityItem(
    title: String,
    description: String,
    time: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Gray800
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Gray600
            )
        }
        
        Text(
            text = time,
            style = MaterialTheme.typography.bodySmall,
            color = Gray500
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherClassesScreenPreview() {
    VoiceTutorTheme {
        TeacherClassesScreen()
    }
}
