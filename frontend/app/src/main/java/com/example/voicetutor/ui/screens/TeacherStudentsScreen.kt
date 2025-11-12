package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.viewmodel.StudentViewModel
import com.example.voicetutor.ui.viewmodel.ClassViewModel
import com.example.voicetutor.data.repository.StudentRepository
import com.example.voicetutor.ApiServiceEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherStudentsScreen(
    classId: Int? = null,
    teacherId: String? = null,
    onNavigateToStudentDetail: (Int) -> Unit = {},
    navController: androidx.navigation.NavHostController? = null
) {
    val viewModel: StudentViewModel = hiltViewModel()
    val classViewModel: ClassViewModel = hiltViewModel()
    val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()
    
    val students by viewModel.students.collectAsStateWithLifecycle()
    val classStudents by classViewModel.classStudents.collectAsStateWithLifecycle()
    val currentClass by classViewModel.currentClass.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    
    // 동적 클래스 정보 가져오기
    val className = currentClass?.name ?: "고등학교 1학년 A반"
    val subjectName = currentClass?.subject?.name ?: "과목"
    val description = currentClass?.description ?: "과목 설명"
    val teacherName = currentClass?.teacherName ?: currentUser?.name ?: "선생님"
    
    // 학생 통계 데이터
    data class StudentStats(
        val averageScore: Float,
        val completionRate: Float,
        val totalAssignments: Int,
        val completedAssignments: Int
    )
    
    var studentsStatisticsMap by remember { mutableStateOf<Map<Int, StudentStats>>(emptyMap()) }
    var isLoadingStatistics by remember { mutableStateOf(true) }
    var overallCompletionRate by remember { mutableStateOf(0f) }
    
    // Load students and class data on first composition
    LaunchedEffect(classId, currentUser?.id) {
        val actualTeacherId = teacherId ?: currentUser?.id?.toString()
        if (classId != null && actualTeacherId != null) {
            println("TeacherStudentsScreen - Loading students for class ID: $classId, teacher ID: $actualTeacherId")
            viewModel.loadAllStudents(teacherId = actualTeacherId, classId = classId.toString())
            classViewModel.loadClassById(classId)
            classViewModel.loadClassStudents(classId)
            
            // 학생 통계 로드
            isLoadingStatistics = true
            classViewModel.loadClassStudentsStatistics(classId) { result ->
                result.onSuccess { stats ->
                    overallCompletionRate = stats.overallCompletionRate
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
                    overallCompletionRate = 0f
                    studentsStatisticsMap = emptyMap()
                    isLoadingStatistics = false
                }
            }
        }
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            viewModel.clearError()
        }
    }
    
    // 학생 등록 바텀시트 상태
    var showEnrollSheet by remember { mutableStateOf(false) }
    val selectedToEnroll = remember { mutableStateListOf<Int>() }
    val allStudentsForEnroll = remember { mutableStateListOf<Student>() }
    var isLoadingAllStudents by remember { mutableStateOf(false) }
    var enrollSearchQuery by remember { mutableStateOf("") }
    
    // 학생 삭제 바텀시트 상태
    var showDeleteSheet by remember { mutableStateOf(false) }
    val selectedToDelete = remember { mutableStateListOf<Int>() }
    var deleteSearchQuery by remember { mutableStateOf("") }
    
    // 학생 삭제 재확인 다이얼로그 상태
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    
    // EntryPoint를 통해 ApiService 주입받기 (바텀시트 독립적 데이터 로딩용)
    val context = LocalContext.current
    val studentRepository = remember {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ApiServiceEntryPoint::class.java
        )
        val apiService = entryPoint.apiService()
        StudentRepository(apiService)
    }
    val classRepository = remember {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ApiServiceEntryPoint::class.java
        )
        val apiService = entryPoint.apiService()
        com.example.voicetutor.data.repository.ClassRepository(apiService)
    }
    
    // 바텀시트 열 때 전체 학생 목록을 별도로 로드 (viewModel.students와 완전히 독립)
    LaunchedEffect(showEnrollSheet) {
        if (showEnrollSheet) {
            isLoadingAllStudents = true
            try {
                // teacherId = null로 전체 학생 계정 가져오기
                val result = studentRepository.getAllStudents(teacherId = null, classId = null)
                result.onSuccess { allStudents ->
                    withContext(Dispatchers.Main) {
                        allStudentsForEnroll.clear()
                        allStudentsForEnroll.addAll(allStudents)
                        isLoadingAllStudents = false
                    }
                }.onFailure {
                    withContext(Dispatchers.Main) {
                        isLoadingAllStudents = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoadingAllStudents = false
                    println("Error loading all students for enrollment: ${e.message}")
                }
            }
        } else {
            // 바텀시트가 닫히면 바텀시트용 목록만 클리어 (viewModel.students는 건드리지 않음)
            allStudentsForEnroll.clear()
            selectedToEnroll.clear()
            enrollSearchQuery = ""
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
                        text = "$subjectName - $description",
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
                        text = teacherName.firstOrNull()?.toString() ?: "T",
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
                title = "과제 제출률",
                value = if (isLoadingStatistics) "-" else "${overallCompletionRate.toInt()}%",
                icon = Icons.Filled.Done,
                iconColor = PrimaryIndigo,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VTButton(
                text = "학생 등록",
                onClick = {
                    selectedToEnroll.clear()
                    showEnrollSheet = true
                },
                variant = ButtonVariant.Outline,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )
            VTButton(
                text = "학생 삭제",
                onClick = {
                    selectedToDelete.clear()
                    showDeleteSheet = true
                },
                variant = ButtonVariant.Outline,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }
        
        // Students list
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "학생 목록",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
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
                students.forEachIndexed { index, student ->
                    val stats = studentsStatisticsMap[student.id]
                    StudentListItem(
                        student = student,
                        averageScore = stats?.averageScore ?: 0f,
                        completionRate = stats?.completionRate ?: 0f,
                        totalAssignments = stats?.totalAssignments ?: 0,
                        completedAssignments = stats?.completedAssignments ?: 0,
                        isLoadingStats = isLoadingStatistics,
                        isLastItem = index == students.lastIndex
                    )
                }
            }
        }
    }
    
    // 학생 등록 바텀시트
    if (showEnrollSheet) {
        ModalBottomSheet(onDismissRequest = { 
            showEnrollSheet = false
            enrollSearchQuery = ""
        }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    "학생 등록", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                
                // 검색 입력 필드
                OutlinedTextField(
                    value = enrollSearchQuery,
                    onValueChange = { enrollSearchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    placeholder = { 
                        Text(
                            "이름 또는 이메일로 검색", 
                            color = Gray500,
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "검색",
                            tint = Gray600,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (enrollSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { enrollSearchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "검색어 지우기",
                                    tint = Gray600,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = Gray300
                    )
                )
                Spacer(Modifier.height(12.dp))

                // 로딩 중일 때
                if (isLoadingAllStudents) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryIndigo)
                    }
                } else {
                    // 이미 등록된 학생 제외 목록
                    val enrolledIds = classStudents.map { it.id }.toSet()
                    val allCandidates = allStudentsForEnroll.filter { it.id !in enrolledIds }
                    
                    // 검색어로 필터링 (이름 또는 이메일)
                    val searchQueryLower = enrollSearchQuery.lowercase()
                    val candidates = if (searchQueryLower.isBlank()) {
                        allCandidates
                    } else {
                        allCandidates.filter { student ->
                            val name = student.name?.lowercase() ?: ""
                            val email = student.email?.lowercase() ?: ""
                            name.contains(searchQueryLower) || email.contains(searchQueryLower)
                        }
                    }

                    if (allCandidates.isEmpty()) {
                        Text("등록 가능한 학생이 없습니다.", color = Gray600)
                    } else if (candidates.isEmpty()) {
                        Text("검색 결과가 없습니다.", color = Gray600)
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
                                coroutineScope.launch {
                                    // 모든 학생 등록 API 호출
                                    selectedToEnroll.forEach { sid ->
                                        classViewModel.enrollStudentToClass(classId = id, studentId = sid)
                                    }
                                    // 등록 완료를 위해 잠시 대기 (API 처리 시간)
                                    delay(500)
                                    // 완료 후 갱신
                                    classViewModel.loadClassStudents(id)
                                    val actualTeacherId = teacherId ?: currentUser?.id?.toString()
                                    actualTeacherId?.let {
                                        viewModel.loadAllStudents(teacherId = it, classId = id.toString())
                                    }
                                }
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
    
    // 학생 삭제 바텀시트
    if (showDeleteSheet) {
        ModalBottomSheet(onDismissRequest = { 
            showDeleteSheet = false
            deleteSearchQuery = ""
        }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    "학생 삭제", 
                    style = MaterialTheme.typography.titleLarge, 
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(12.dp))
                
                // 검색 입력 필드
                OutlinedTextField(
                    value = deleteSearchQuery,
                    onValueChange = { deleteSearchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    placeholder = { 
                        Text(
                            "이름 또는 이메일로 검색", 
                            color = Gray500,
                            style = MaterialTheme.typography.bodyMedium
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "검색",
                            tint = Gray600,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        if (deleteSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { deleteSearchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "검색어 지우기",
                                    tint = Gray600,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = Gray300
                    )
                )
                Spacer(Modifier.height(12.dp))

                // 이미 등록된 학생 목록
                val enrolledStudents = classStudents
                
                // 검색어로 필터링 (이름 또는 이메일)
                val searchQueryLower = deleteSearchQuery.lowercase()
                val filteredStudents = if (searchQueryLower.isBlank()) {
                    enrolledStudents
                } else {
                    enrolledStudents.filter { student ->
                        val name = student.name?.lowercase() ?: ""
                        val email = student.email?.lowercase() ?: ""
                        name.contains(searchQueryLower) || email.contains(searchQueryLower)
                    }
                }

                if (enrolledStudents.isEmpty()) {
                    Text("삭제할 학생이 없습니다.", color = Gray600)
                } else if (filteredStudents.isEmpty()) {
                    Text("검색 결과가 없습니다.", color = Gray600)
                } else {
                    filteredStudents.forEach { student ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(student.name ?: "학생", fontWeight = FontWeight.Medium)
                                Text(student.email, style = MaterialTheme.typography.bodySmall, color = Gray600)
                            }
                            val checked = selectedToDelete.contains(student.id)
                            Checkbox(checked = checked, onCheckedChange = { isChecked ->
                                if (isChecked) selectedToDelete.add(student.id) else selectedToDelete.remove(student.id)
                            })
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    VTButton(
                        text = "취소",
                        onClick = { showDeleteSheet = false },
                        variant = ButtonVariant.Outline,
                        modifier = Modifier.weight(1f)
                    )
                    VTButton(
                        text = "삭제",
                        onClick = {
                            if (selectedToDelete.isNotEmpty()) {
                                showDeleteConfirmDialog = true
                            }
                        },
                        variant = ButtonVariant.Primary,
                        modifier = Modifier.weight(1f),
                        enabled = selectedToDelete.isNotEmpty()
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
    
    // 학생 삭제 재확인 다이얼로그
    if (showDeleteConfirmDialog && selectedToDelete.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "학생 제거",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "선택한 ${selectedToDelete.size}명의 학생을 이 반에서 제거하시겠습니까?\n제거된 학생의 과제, 질문, 답변이 모두 삭제됩니다.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                VTButton(
                    text = "제거",
                    onClick = {
                        classId?.let { id ->
                            coroutineScope.launch {
                                try {
                                    println("[DELETE] Starting to remove ${selectedToDelete.size} students from class $id")
                                    // 각 학생을 순차적으로 삭제
                                    withContext(Dispatchers.IO) {
                                        for (studentId in selectedToDelete) {
                                            println("[DELETE] Removing student $studentId from class $id")
                                            val result = classRepository.removeStudentFromClass(id, studentId)
                                            result.onSuccess {
                                                println("[DELETE] Successfully removed student $studentId")
                                            }.onFailure { e ->
                                                println("[DELETE] Failed to remove student $studentId: ${e.message}")
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                    println("[DELETE] All deletions completed. Refreshing lists...")
                                    // 완료 후 갱신
                                    withContext(Dispatchers.Main) {
                                        classViewModel.loadClassStudents(id)
                                        val actualTeacherId = teacherId ?: currentUser?.id?.toString()
                                        actualTeacherId?.let {
                                            viewModel.loadAllStudents(teacherId = it, classId = id.toString())
                                        }
                                        // 다이얼로그와 시트 닫기
                                        showDeleteConfirmDialog = false
                                        showDeleteSheet = false
                                        selectedToDelete.clear()
                                    }
                                } catch (e: Exception) {
                                    println("[DELETE] Error removing students: ${e.message}")
                                    e.printStackTrace()
                                    withContext(Dispatchers.Main) {
                                        showDeleteConfirmDialog = false
                                        showDeleteSheet = false
                                        selectedToDelete.clear()
                                    }
                                }
                            }
                        }
                    },
                    variant = ButtonVariant.Primary,
                    size = ButtonSize.Small
                )
            },
            dismissButton = {
                VTButton(
                    text = "취소",
                    onClick = {
                        showDeleteConfirmDialog = false
                    },
                    variant = ButtonVariant.Outline,
                    size = ButtonSize.Small
                )
            }
        )
    }
}

@Composable
fun StudentListItem(
    student: Student,
    averageScore: Float,
    completionRate: Float,
    totalAssignments: Int,
    completedAssignments: Int,
    isLoadingStats: Boolean,
    isLastItem: Boolean
) {
    androidx.compose.material3.Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Gray200.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(PrimaryIndigo.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.name?.takeIf { it.isNotBlank() }?.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                            Text(
                                text = student.name ?: "이름 없음",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray800
                            )
                        Text(
                            text = student.email ?: "이메일 없음",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {

                    val scoreLabelText = if (isLoadingStats) "평균 점수: 로딩 중" else "평균 점수: ${averageScore.toInt()}점"

                    val scoreColor = PrimaryIndigo

                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .background(PrimaryIndigo.copy(alpha = 0.08f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = scoreLabelText,
                            style = MaterialTheme.typography.bodySmall,
                            color = scoreColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val completionText = if (isLoadingStats) "과제 완료: 로딩 중" else {
                if (totalAssignments > 0) "과제 완료: $completedAssignments/$totalAssignments" else "과제 완료: -"
            }
            val completionRateValue = (completionRate).toInt()
            val completionRateText = if (isLoadingStats) "완료율: 로딩 중" else "완료율: ${completionRateValue}%"

            val completionColor = Gray600
            val progressColor = PrimaryIndigo

            Text(
                text = completionText,
                style = MaterialTheme.typography.bodySmall,
                color = completionColor,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = completionRateText,
                style = MaterialTheme.typography.bodySmall,
                color = progressColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        val progressColor = PrimaryIndigo

        VTProgressBar(
            progress = if (totalAssignments > 0) (completionRate/100) else 0f,
            showPercentage = false,
            color = progressColor,
            height = 6
        )

        Spacer(modifier = Modifier.height(8.dp))

    }
}

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        if (!isLastItem) {
        Spacer(modifier = Modifier.height(4.dp))
            Divider(
                modifier = Modifier.fillMaxWidth(),
                color = Gray200,
                thickness = 0.5.dp
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
