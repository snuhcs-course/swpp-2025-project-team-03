package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
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
    onNavigateToStudentDetail: (Int, Int, String) -> Unit = { _, _, _ -> }  // 리포트용
) {
    val studentViewModel: StudentViewModel = hiltViewModel()
    val classViewModel: ClassViewModel = hiltViewModel()
    
    val apiStudents by studentViewModel.students.collectAsStateWithLifecycle()
    val isLoading by studentViewModel.isLoading.collectAsStateWithLifecycle()
    val error by studentViewModel.error.collectAsStateWithLifecycle()
    
    val classes by classViewModel.classes.collectAsStateWithLifecycle()
    val isLoadingClasses by classViewModel.isLoading.collectAsStateWithLifecycle()
    
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
    
    // 학생 통계 데이터 클래스
    data class StudentStats(
        val averageScore: Float,
        val completionRate: Float,
        val totalAssignments: Int,
        val completedAssignments: Int
    )
    
    var studentsStatisticsMap by remember { mutableStateOf<Map<Int, StudentStats>>(emptyMap()) }
    var isLoadingStatistics by remember { mutableStateOf(true) }
    
    // Load statistics for selected class
    LaunchedEffect(selectedClassId) {
        selectedClassId?.let { classId ->
            isLoadingStatistics = true
            classViewModel.loadClassStudentsStatistics(classId) { result ->
                result.onSuccess { stats ->
                    studentsStatisticsMap = stats.students.associate { 
                        it.studentId to StudentStats(
                            averageScore = it.averageScore,
                            completionRate = it.completionRate,
                            totalAssignments = it.totalAssignments,
                            completedAssignments = it.completedAssignments
                        )
                    }
                    isLoadingStatistics = false
                }.onFailure {
                    studentsStatisticsMap = emptyMap()
                    isLoadingStatistics = false
                }
            }
        } ?: run {
            isLoadingStatistics = false
        }
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
            itemsIndexed(
                items = allStudents,
                key = { _, student -> student.id }  // 각 학생의 고유 ID를 키로 사용
            ) { index, student ->
                val stats = studentsStatisticsMap[student.id]

                if (index > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Gray200.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(12.dp))
                }

                AllStudentsCard(
                    student = student,
                    averageScore = stats?.averageScore ?: 0f,
                    completionRate = stats?.completionRate ?: 0f,
                    totalAssignments = stats?.totalAssignments ?: 0,
                    completedAssignments = stats?.completedAssignments ?: 0,
                    isLoadingStats = isLoadingStatistics,
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
    student: com.example.voicetutor.data.models.AllStudentsStudent,
    averageScore: Float,  // 사용하지 않지만 호환성을 위해 유지
    completionRate: Float,
    totalAssignments: Int,  // 사용하지 않지만 호환성을 위해 유지
    completedAssignments: Int,  // 사용하지 않지만 호환성을 위해 유지
    isLoadingStats: Boolean,
    onReportClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp
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
                    modifier = Modifier.weight(1f),
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

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBadge(
                        label = "이행률",
                        value = if (isLoadingStats) "로딩 중..." else "${completionRate.toInt()}%",
                        valueColor = PrimaryIndigo
                    )
                    StatBadge(
                        label = "평균 점수",
                        value = if (isLoadingStats) "로딩 중..." else "${averageScore.toInt()}점",
                        valueColor = when {
                            averageScore >= 90 -> Success
                            averageScore >= 80 -> Warning
                            else -> Gray600
                        }
                    )
                }
            }

            if (isLoadingStats) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = PrimaryIndigo
                )
            } else {
                LinearProgressIndicator(
                    progress = (completionRate / 100f).coerceIn(0f, 1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = PrimaryIndigo,
                    trackColor = Gray200
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                VTButton(
                    text = "리포트 보기",
                    onClick = onReportClick,
                    variant = ButtonVariant.Outline,
                    size = ButtonSize.Small,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Assessment,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun StatBadge(
    label: String,
    value: String,
    valueColor: Color
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Gray500
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AllStudentsScreenPreview() {
    VoiceTutorTheme {
        AllStudentsScreen()
    }
}