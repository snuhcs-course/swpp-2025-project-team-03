package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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

// AllStudentsStudent는 StudentModels.kt에서 정의된 것을 사용

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllStudentsScreen(
    teacherId: String = "1", // 임시로 기본값 설정
    onNavigateToStudentDetail: (Int, Int, String) -> Unit = { _, _, _ -> },
    onNavigateToMessage: (String) -> Unit = {}
) {
    val studentViewModel: StudentViewModel = hiltViewModel()
    val classViewModel: ClassViewModel = hiltViewModel()
    
    val apiStudents by studentViewModel.students.collectAsStateWithLifecycle()
    val isLoading by studentViewModel.isLoading.collectAsStateWithLifecycle()
    val error by studentViewModel.error.collectAsStateWithLifecycle()
    
    val classes by classViewModel.classes.collectAsStateWithLifecycle()
    val isLoadingClasses by classViewModel.isLoading.collectAsStateWithLifecycle()
    
    var selectedClassId by remember { mutableStateOf<Int?>(null) }
    var expandedClassDropdown by remember { mutableStateOf(false) }
    
    // Load classes for teacher
    LaunchedEffect(teacherId) {
        println("AllStudentsScreen - Loading classes for teacher ID: $teacherId")
        classViewModel.loadClasses(teacherId)
    }
    
    // Auto-select first class if not already selected
    LaunchedEffect(classes) {
        if (classes.isNotEmpty() && selectedClassId == null) {
            selectedClassId = classes.first().id
            println("AllStudentsScreen - Auto-selecting first class: ${classes.first().id}")
        }
    }
    
    // Load students when class is selected
    LaunchedEffect(selectedClassId) {
        if (selectedClassId != null) {
            println("AllStudentsScreen - Loading students for class ID: $selectedClassId")
            studentViewModel.loadAllStudents(teacherId = teacherId, classId = selectedClassId.toString())
        }
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            studentViewModel.clearError()
        }
    }
    
    // Convert Student to AllStudentsStudent for UI
    val allStudents = apiStudents.map { student ->
        AllStudentsStudent(
            id = student.id,
            name = student.name ?: "이름 없음", // null 체크 추가
            email = student.email,
            role = student.role
        )
    }
    
    // Calculate stats
    val totalStudents = allStudents.size
    val averageScore = 0 // 평균 점수 정보가 없으므로 0으로 설정
    
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
                        text = "전체 학생 관리",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "학생들의 학습 현황을 확인하고 관리하세요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        text = "총 ${totalStudents}명의 학생",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        item {
            // Stats cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VTStatsCard(
                    title = "전체 학생",
                    value = "${totalStudents}명",
                    icon = Icons.Filled.People,
                    iconColor = PrimaryIndigo,
                    modifier = Modifier.weight(1f),
                    variant = CardVariant.Gradient
                )
                
                
                VTStatsCard(
                    title = "평균 점수",
                    value = "${averageScore}점",
                    icon = Icons.Filled.Star,
                    iconColor = Warning,
                    modifier = Modifier.weight(1f),
                    variant = CardVariant.Gradient
                )
            }
        }
        
        item {
            // Class selector dropdown only (no search)
            ExposedDropdownMenuBox(
                expanded = expandedClassDropdown,
                onExpandedChange = { expandedClassDropdown = it }
            ) {
                OutlinedTextField(
                    value = classes.find { it.id == selectedClassId }?.name ?: "반 선택",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("반 선택") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClassDropdown)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                
                ExposedDropdownMenu(
                    expanded = expandedClassDropdown,
                    onDismissRequest = { expandedClassDropdown = false }
                ) {
                    classes.forEach { classData ->
                        DropdownMenuItem(
                            text = { Text(classData.name) },
                            onClick = {
                                selectedClassId = classData.id
                                expandedClassDropdown = false
                            }
                        )
                    }
                }
            }
        }
        
        item {
            // Students list header
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
                
                Text(
                    text = "${allStudents.size}명",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Students list
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
        } else if (allStudents.isEmpty()) {
            item {
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
            }
        } else {
            items(allStudents) { student ->
                AllStudentsCard(
                    student = student,
                    onStudentClick = { 
                        val classId = selectedClassId ?: 0
                        onNavigateToStudentDetail(classId, student.id, student.name)
                    },
                    onMessageClick = { onNavigateToMessage(student.name) }
                )
            }
        }
    }
}

@Composable
fun AllStudentsCard(
    student: com.example.voicetutor.data.models.AllStudentsStudent,
    onStudentClick: () -> Unit,
    onMessageClick: () -> Unit
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
                        .clip(androidx.compose.foundation.shape.CircleShape)
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
                    Text(
                        text = student.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = "학생", // 클래스 정보가 없으므로 기본값
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                }
                
            }
            
            // Progress info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "과제 진행률",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = "정보 없음", // 과제 정보가 없으므로 기본값
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gray500
                    )
                }
                
                Column {
                    Text(
                        text = "평균 점수",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = "정보 없음", // 평균 점수 정보가 없으므로 기본값
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gray500
                    )
                }
            }
            
            // Progress bar
            VTProgressBar(
                progress = 0f, // 과제 정보가 없으므로 0으로 설정
                showPercentage = true
            )
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onMessageClick
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Message,
                        contentDescription = "메시지 보내기",
                        tint = PrimaryIndigo
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AllStudentsScreenPreview() {
    VoiceTutorTheme {
        AllStudentsScreen()
    }
}