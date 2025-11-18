package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
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
    teacherId: String,
    onNavigateToStudentDetail: (Int, Int, String) -> Unit = { _, _, _ -> }  // 리포트용
) {
    val studentViewModel: StudentViewModel = hiltViewModel()
    val classViewModel: ClassViewModel = hiltViewModel()
    
    val apiStudents by studentViewModel.students.collectAsStateWithLifecycle()
    val isLoading by studentViewModel.isLoading.collectAsStateWithLifecycle()
    val error by studentViewModel.error.collectAsStateWithLifecycle()
    
    val classes by classViewModel.classes.collectAsStateWithLifecycle()
    val studentClasses by studentViewModel.studentClasses.collectAsStateWithLifecycle()
    val loadingStudentClasses by studentViewModel.loadingStudentClasses.collectAsStateWithLifecycle()
    
    var selectedClassId by rememberSaveable(stateSaver = Saver(
        save = { it ?: -1 },
        restore = { if (it == -1) null else it }
    )) { mutableStateOf<Int?>(null) }
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
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Header
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
                        text = "성취기준 리포트",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "학생들의 학습 현황을 확인하고 취약 유형을 분석하세요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600
                    )
                }
            }
        }
        
        item {
            // Stats card
            VTStatsCard(
                title = "전체 학생",
                value = "${totalStudents}명",
                icon = Icons.Filled.People,
                iconColor = PrimaryIndigo,
                modifier = Modifier.fillMaxWidth(),
                variant = CardVariant.Gradient
            )
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
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Gray800),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClassDropdown)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedTextColor = Gray800,
                        unfocusedTextColor = Gray800,
                        focusedLabelColor = PrimaryIndigo,
                        unfocusedLabelColor = Gray600
                    )
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
            itemsIndexed(
                items = allStudents,
                key = { _, student -> student.id }  // 각 학생의 고유 ID를 키로 사용
            ) { index, student ->
                val classesForStudent = studentClasses[student.id]
                val isClassesLoading = loadingStudentClasses.contains(student.id)
                LaunchedEffect(student.id) {
                    if (classesForStudent == null && !isClassesLoading) {
                        studentViewModel.loadStudentClasses(student.id)
                    }
                }

                AllStudentsCard(
                    student = student,
                    classNames = classesForStudent?.map { it.name } ?: emptyList(),
                    isLoadingClasses = classesForStudent == null || isClassesLoading,
                    onReportClick = {
                        // 리포트 페이지로 이동
                        val classId = selectedClassId ?: 0
                        onNavigateToStudentDetail(classId, student.id, student.name)
                    }
                )
            }
        }
    }
}

@Composable
fun AllStudentsCard(
    student: AllStudentsStudent,
    classNames: List<String>,
    isLoadingClasses: Boolean,
    onReportClick: () -> Unit
) {
    VTCard2(
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.Elevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color = PrimaryIndigo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
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
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "반: ",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Gray600
                )
                when {
                    isLoadingClasses -> Text(
                        text = "불러오는 중...",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                    classNames.isEmpty() -> Text(
                        text = "배정된 반이 없습니다",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
                    )
                    else -> classNames.forEachIndexed { index, name ->
                        if (index > 0) {
                            Text(
                                text = ", ",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray500
                            )
                        }
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray700
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                VTButton(
                    text = "리포트 보기",
                    onClick = onReportClick,
                    variant = ButtonVariant.Primary,
                    size = ButtonSize.Medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AllStudentsScreenPreview() {
    VoiceTutorTheme {
        AllStudentsScreen("1")
    }
}