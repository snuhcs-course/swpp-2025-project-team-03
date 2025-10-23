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

data class AllStudentsStudent(
    val id: Int,
    val name: String,
    val email: String,
    val className: String,
    val completedAssignments: Int,
    val totalAssignments: Int,
    val averageScore: Int,
    val lastActive: String?
)

@Composable
fun AllStudentsScreen(
    teacherId: String = "1", // 임시로 기본값 설정
    onNavigateToStudentDetail: (String) -> Unit = {},
    onNavigateToMessage: (String) -> Unit = {}
) {
    val viewModel: StudentViewModel = hiltViewModel()
    val apiStudents by viewModel.students.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    var searchQuery by remember { mutableStateOf("") }
    
    // Load students on first composition
    LaunchedEffect(teacherId) {
        viewModel.loadAllStudents(teacherId = teacherId)
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            viewModel.clearError()
        }
    }
    
    // Convert Student to AllStudentsStudent for UI
    val allStudents = apiStudents.map { student ->
        AllStudentsStudent(
            id = student.id,
            name = student.name,
            email = student.email,
            className = student.className,
            completedAssignments = student.completedAssignments,
            totalAssignments = student.totalAssignments,
            averageScore = student.averageScore,
            lastActive = student.lastActive
        )
    }
    
    // Filter and search students
    val filteredStudents = remember(allStudents, searchQuery) {
        allStudents.filter { student ->
            val matchesSearch = searchQuery.isEmpty() || 
                student.name.contains(searchQuery, ignoreCase = true) ||
                student.email.contains(searchQuery, ignoreCase = true) ||
                student.className.contains(searchQuery, ignoreCase = true)
            
            val matchesFilter = true // 모든 학생 표시
            
            matchesSearch && matchesFilter
        }
    }
    
    // Calculate stats
    val totalStudents = allStudents.size
    val averageScore = if (allStudents.isNotEmpty()) {
        allStudents.map { it.averageScore }.average().toInt()
    } else 0
    
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
            // Search and filter
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("학생 검색") },
                    placeholder = { Text("이름, 이메일, 학급으로 검색") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
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
                    text = "${filteredStudents.size}명",
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
        } else if (filteredStudents.isEmpty()) {
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
                            text = if (searchQuery.isNotEmpty()) {
                                "검색 결과가 없습니다"
                            } else {
                                "학생이 없습니다"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600
                        )
                    }
                }
            }
        } else {
            items(filteredStudents) { student ->
                AllStudentsCard(
                    student = student,
                    onStudentClick = { onNavigateToStudentDetail(student.name) },
                    onMessageClick = { onNavigateToMessage(student.name) }
                )
            }
        }
    }
}

@Composable
fun AllStudentsCard(
    student: AllStudentsStudent,
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
                        text = student.className,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500
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
                        text = "과제 진행률",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = "${student.completedAssignments}/${student.totalAssignments}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo
                    )
                }
                
                Column {
                    Text(
                        text = "평균 점수",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = "${student.averageScore}점",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (student.averageScore >= 80) Success else Warning
                    )
                }
                
                Column {
                    Text(
                        text = "마지막 활동",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = student.lastActive ?: "활동 없음",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = if (student.lastActive != null) Gray800 else Gray500
                    )
                }
            }
            
            // Progress bar
            VTProgressBar(
                progress = student.totalAssignments.toFloat().let { 
                    if (it > 0) student.completedAssignments / it else 0f 
                },
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