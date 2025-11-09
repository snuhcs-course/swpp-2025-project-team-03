package com.example.voicetutor.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
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
    LaunchedEffect(currentUser?.id) {
        val actualTeacherId = teacherId ?: currentUser?.id?.toString()
        
        if (actualTeacherId == null) {
            println("TeacherClassesScreen - Waiting for user to be loaded...")
            return@LaunchedEffect
        }
        
        // 이미 assignments가 있으면 재호출하지 않음
        if (assignments.isEmpty()) {
            println("TeacherClassesScreen - Loading assignments for teacher ID: $actualTeacherId")
            actualAssignmentViewModel.loadAllAssignments(teacherId = actualTeacherId)
        } else {
            println("TeacherClassesScreen - Already have ${assignments.size} assignments from login")
        }
        
        // 이미 classes가 있으면 재호출하지 않음
        if (classes.isEmpty()) {
            println("TeacherClassesScreen - Loading classes for teacher ID: $actualTeacherId")
            classViewModel.loadClasses(actualTeacherId)
        } else {
            println("TeacherClassesScreen - Already have ${classes.size} classes")
        }
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
        // Calculate assignment count from actual data
        val classAssignments = assignments.filter { it.courseClass.id == classData.id }
        val assignmentCount = classAssignments.size
        
        ClassRoom(
            id = classData.id,
            name = classData.name,
            subject = classData.subject.name,
            description = classData.description,
            studentCount = classData.actualStudentCount,
            assignmentCount = assignmentCount,
            completionRate = 0f, // Not used in UI, kept for data model compatibility
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
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 6.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header Banner
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
            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "수업 관리",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "내 수업을 관리하고 과제를 생성하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
        
        // Action section with button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "수업 목록",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray800
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VTButton(
                    text = "새 수업 만들기",
                    onClick = onNavigateToCreateClass,
                    variant = ButtonVariant.Outline,
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
        }
        
        // Classes list
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            
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
        Column(
            modifier = Modifier.padding(3.5.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
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
                
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
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
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Gray800
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
