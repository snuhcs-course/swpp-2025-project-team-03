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
    val color: Color
)

@Composable
fun TeacherClassesScreen(
    authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel? = null,
    assignmentViewModel: AssignmentViewModel? = null,
    teacherId: String? = null, // 파라미터로 받거나 현재 로그인한 사용자 ID 사용
    onNavigateToClassDetail: (String, Int) -> Unit = { _, _ -> },
    onNavigateToCreateClass: () -> Unit = {},
    onNavigateToCreateAssignment: (Int?) -> Unit = { _ -> },
    onNavigateToStudents: (Int) -> Unit = {}
) {
    val classViewModel: ClassViewModel = hiltViewModel()
    val actualAssignmentViewModel: AssignmentViewModel = assignmentViewModel ?: hiltViewModel()
    val actualAuthViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = authViewModel ?: hiltViewModel()
    
    val classes by classViewModel.classes.collectAsStateWithLifecycle()
    val assignments by actualAssignmentViewModel.assignments.collectAsStateWithLifecycle()
    val isLoading by classViewModel.isLoading.collectAsStateWithLifecycle()
    val error by classViewModel.error.collectAsStateWithLifecycle()
    val currentUser by actualAuthViewModel.currentUser.collectAsStateWithLifecycle()
    
    // Load classes and assignments on first composition
    LaunchedEffect(currentUser?.id, assignments.isEmpty(), classes.isEmpty()) {
        val actualTeacherId = teacherId ?: currentUser?.id?.toString()
        
        // 이미 assignments가 있으면 재호출하지 않음
        if (assignments.isNotEmpty()) {
            println("TeacherClassesScreen - Already have ${assignments.size} assignments from login")
        } else if (actualTeacherId != null) {
            println("TeacherClassesScreen - Loading assignments for teacher ID: $actualTeacherId")
            actualAssignmentViewModel.loadAllAssignments(teacherId = actualTeacherId)
        }
        
        // Classes는 항상 로드 필요
        if (actualTeacherId == null) {
            println("TeacherClassesScreen - Waiting for user to be loaded...")
            return@LaunchedEffect
        }
        
        if (classes.isEmpty()) {
            println("TeacherClassesScreen - Loading classes for teacher ID: $actualTeacherId")
            classViewModel.loadClasses(actualTeacherId)
        } else {
            println("TeacherClassesScreen - Already have ${classes.size} classes")
        }
    }
    
    // 클래스 목록이 변경될 때마다 새로고침
    LaunchedEffect(classes.size) {
        val actualTeacherId = teacherId ?: currentUser?.id?.toString()
        if (actualTeacherId != null && classes.isNotEmpty()) {
            println("TeacherClassesScreen - Classes updated, refreshing...")
            classViewModel.refreshClasses(actualTeacherId)
        }
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            classViewModel.clearError()
        }
    }
    
    // 각 클래스의 완료율을 저장하는 상태
    var classCompletionRates by remember { mutableStateOf<Map<Int, Float>>(emptyMap()) }
    
    // 각 클래스의 완료율 로드
    LaunchedEffect(classes.map { it.id }) {
        if (classes.isNotEmpty()) {
            val ratesMap = mutableMapOf<Int, Float>()
            
            classes.forEach { classData ->
                classViewModel.loadClassCompletionRate(classData.id) { result ->
                    result.onSuccess { completionRate ->
                        ratesMap[classData.id] = completionRate.completionRate
                        classCompletionRates = ratesMap.toMap() // 상태 업데이트
                    }.onFailure {
                        ratesMap[classData.id] = 0f
                        classCompletionRates = ratesMap.toMap() // 상태 업데이트
                    }
                }
            }
        }
    }
    
    // Convert ClassData to ClassRoom for UI
    val classRooms = classes.map { classData ->
        // Calculate assignment count and completion rate from actual data
        val classAssignments = assignments.filter { it.courseClass.id == classData.id }
        val assignmentCount = classAssignments.size
        val completionRate = classCompletionRates[classData.id] ?: 0f
        
        ClassRoom(
            id = classData.id,
            name = classData.name,
            subject = classData.subject.name,
            description = classData.description,
            studentCount = classData.actualStudentCount,
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
                    text = "수업 관리",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Gray800
                )
                Text(
                    text = "내 수업을 관리하고 과제를 생성하세요",
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
                        onClassClick = { onNavigateToClassDetail(classRoom.name, classRoom.id) },
                        onCreateAssignment = { classId -> onNavigateToCreateAssignment(classId) },
                        onViewStudents = { onNavigateToStudents(classRoom.id) }
                    )
                    
                    if (classRoom != classRooms.last()) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
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
                    }
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
            }
            
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
                    text = "학생 상세",
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

@Preview(showBackground = true)
@Composable
fun TeacherClassesScreenPreview() {
    VoiceTutorTheme {
        TeacherClassesScreen()
    }
}
