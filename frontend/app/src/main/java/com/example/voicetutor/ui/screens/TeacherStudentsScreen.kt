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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ApiServiceEntryPoint
import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.repository.StudentRepository
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.utils.ErrorMessageMapper
import com.example.voicetutor.ui.viewmodel.ClassViewModel
import com.example.voicetutor.ui.viewmodel.StudentViewModel
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val HEADER_ALPHA = 0.08f
private const val HEADER_CORNER_RADIUS = 16
private const val EMPTY_STATE_ICON_SIZE = 48
private const val AVATAR_SIZE = 32
private const val AVATAR_BACKGROUND_ALPHA = 0.1f
private const val SCORE_BADGE_ALPHA = 0.08f
private const val PROGRESS_BAR_HEIGHT = 6
private const val EMAIL_MAX_LENGTH = 24
private const val DELAY_AFTER_ENROLL = 500L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherStudentsScreen(
    classId: Int? = null,
    teacherId: String? = null,
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
    val classError by classViewModel.error.collectAsStateWithLifecycle()
    val classIsLoading by classViewModel.isLoading.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    val className = currentClass?.name ?: "고등학교 1학년 A반"
    val subjectName = currentClass?.subject?.name ?: "과목"
    val description = currentClass?.description ?: "과목 설명"

    data class StudentStats(
        val averageScore: Float,
        val completionRate: Float,
        val totalAssignments: Int,
        val completedAssignments: Int,
    )

    var studentsStatisticsMap by remember { mutableStateOf<Map<Int, StudentStats>>(emptyMap()) }
    var isLoadingStatistics by remember { mutableStateOf(true) }
    var overallCompletionRate by remember { mutableStateOf(0f) }

    LaunchedEffect(classId, currentUser?.id) {
        val actualTeacherId = teacherId ?: currentUser?.id?.toString()
        if (classId != null && actualTeacherId != null) {
            viewModel.loadAllStudents(teacherId = actualTeacherId, classId = classId.toString())
            classViewModel.loadClassById(classId)
            classViewModel.loadClassStudents(classId)

            isLoadingStatistics = true
            classViewModel.loadClassStudentsStatistics(classId) { result ->
                result.onSuccess { stats ->
                    overallCompletionRate = stats.overallCompletionRate
                    studentsStatisticsMap = stats.students.associate {
                        it.studentId to StudentStats(
                            averageScore = it.averageScore,
                            completionRate = it.completionRate,
                            totalAssignments = it.totalAssignments,
                            completedAssignments = it.completedAssignments,
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

    // 네트워크 에러가 아닌 경우에만 에러를 클리어합니다.
    // 네트워크 에러는 students.isEmpty()일 때 구분하기 위해 유지합니다.
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            if (!ErrorMessageMapper.isNetworkError(errorMessage)) {
                viewModel.clearError()
            }
        }
    }

    var showEnrollSheet by remember { mutableStateOf(false) }
    val selectedToEnroll = remember { mutableStateListOf<Int>() }
    val allStudentsForEnroll = remember { mutableStateListOf<Student>() }
    var isLoadingAllStudents by remember { mutableStateOf(false) }
    var enrollNetworkError by remember { mutableStateOf<String?>(null) }
    var enrollSearchQuery by remember { mutableStateOf("") }

    var showDeleteSheet by remember { mutableStateOf(false) }
    val selectedToDelete = remember { mutableStateListOf<Int>() }
    var deleteSearchQuery by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val studentRepository = remember {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ApiServiceEntryPoint::class.java,
        )
        val apiService = entryPoint.apiService()
        StudentRepository(apiService)
    }
    val classRepository = remember {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ApiServiceEntryPoint::class.java,
        )
        val apiService = entryPoint.apiService()
        com.example.voicetutor.data.repository.ClassRepository(apiService)
    }

    LaunchedEffect(showEnrollSheet) {
        if (showEnrollSheet) {
            isLoadingAllStudents = true
            enrollNetworkError = null
            try {
                val result = studentRepository.getAllStudents(teacherId = null, classId = null)
                result.onSuccess { allStudents ->
                    withContext(Dispatchers.Main) {
                        allStudentsForEnroll.clear()
                        allStudentsForEnroll.addAll(allStudents)
                        isLoadingAllStudents = false
                        enrollNetworkError = null
                    }
                }.onFailure { exception ->
                    withContext(Dispatchers.Main) {
                        isLoadingAllStudents = false
                        val errorMessage = ErrorMessageMapper.getErrorMessage(exception)
                        enrollNetworkError = if (ErrorMessageMapper.isNetworkError(errorMessage)) {
                            errorMessage
                        } else {
                            null
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isLoadingAllStudents = false
                    val errorMessage = ErrorMessageMapper.getErrorMessage(e)
                    enrollNetworkError = if (ErrorMessageMapper.isNetworkError(errorMessage)) {
                        errorMessage
                    } else {
                        null
                    }
                }
            }
        } else {
            allStudentsForEnroll.clear()
            selectedToEnroll.clear()
            enrollSearchQuery = ""
            enrollNetworkError = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = PrimaryIndigo.copy(alpha = HEADER_ALPHA),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(HEADER_CORNER_RADIUS.dp),
                )
                .padding(20.dp),
        ) {
            Column {
                Text(
                    text = className,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$subjectName - $description",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            VTStatsCard(
                title = "과제 제출률",
                value = if (isLoadingStatistics) "-" else "${overallCompletionRate.toInt()}%",
                icon = Icons.Filled.Done,
                iconColor = PrimaryIndigo,
                modifier = Modifier.weight(1f),
            )

            VTStatsCard(
                title = "학생",
                value = students.size.toString(),
                icon = Icons.Filled.Person,
                iconColor = PrimaryIndigo,
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            VTButton(
                text = "학생 등록",
                onClick = {
                    selectedToEnroll.clear()
                    showEnrollSheet = true
                },
                variant = ButtonVariant.Primary,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.PersonAdd,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                },
                modifier = Modifier.weight(1f),
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
                        modifier = Modifier.size(18.dp),
                    )
                },
                modifier = Modifier.weight(1f),
            )
        }

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "학생 목록",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = PrimaryIndigo,
                    )
                }
            } else if (students.isEmpty()) {
                // students.isEmpty()일 때 네트워크 에러인지 확인
                val isNetworkErrorState = error != null && ErrorMessageMapper.isNetworkError(error)
                val emptyStateMessage = if (isNetworkErrorState) {
                    "네트워크가 불안정합니다"
                } else {
                    "학생이 없습니다"
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = Gray400,
                            modifier = Modifier.size(EMPTY_STATE_ICON_SIZE.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = emptyStateMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600,
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
                        isLastItem = index == students.lastIndex,
                    )
                }
            }
        }
    }

    if (showEnrollSheet) {
        ModalBottomSheet(onDismissRequest = {
            showEnrollSheet = false
            enrollSearchQuery = ""
        }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                Text(
                    "학생 등록",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(12.dp))

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
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "검색",
                            tint = Gray600,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    trailingIcon = {
                        if (enrollSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { enrollSearchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "검색어 지우기",
                                    tint = Gray600,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = Gray300,
                    ),
                )
                Spacer(Modifier.height(12.dp))

                if (isLoadingAllStudents) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = PrimaryIndigo)
                    }
                } else {
                    val enrolledIds = classStudents.map { it.id }.toSet()
                    val allCandidates = allStudentsForEnroll.filter { it.id !in enrolledIds }

                    val searchQueryLower = enrollSearchQuery.lowercase()
                    val candidates = if (searchQueryLower.isBlank()) {
                        allCandidates
                    } else {
                        allCandidates.filter { student ->
                            val name = student.name?.lowercase() ?: ""
                            val email = student.email.lowercase()
                            name.contains(searchQueryLower) || email.contains(searchQueryLower)
                        }
                    }

                    if (enrollNetworkError != null) {
                        Text("네트워크가 불안정합니다", color = Gray600)
                    } else if (allCandidates.isEmpty()) {
                        Text("등록 가능한 학생이 없습니다.", color = Gray600)
                    } else if (candidates.isEmpty()) {
                        Text("검색 결과가 없습니다.", color = Gray600)
                    } else {
                        candidates.forEach { student ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
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
                        modifier = Modifier.weight(1f),
                    )
                    VTButton(
                        text = "등록",
                        onClick = {
                            classId?.let { id ->
                                coroutineScope.launch {
                                    selectedToEnroll.forEach { sid ->
                                        classViewModel.enrollStudentToClass(classId = id, studentId = sid)
                                    }
                                    delay(DELAY_AFTER_ENROLL)
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
                        modifier = Modifier.weight(1f),
                        enabled = selectedToEnroll.isNotEmpty(),
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (showDeleteSheet) {
        ModalBottomSheet(onDismissRequest = {
            showDeleteSheet = false
            deleteSearchQuery = ""
        }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                Text(
                    "학생 삭제",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(12.dp))

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
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "검색",
                            tint = Gray600,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                    trailingIcon = {
                        if (deleteSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { deleteSearchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Clear,
                                    contentDescription = "검색어 지우기",
                                    tint = Gray600,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = Gray300,
                    ),
                )
                Spacer(Modifier.height(12.dp))

                val enrolledStudents = classStudents
                val searchQueryLower = deleteSearchQuery.lowercase()
                val filteredStudents = if (searchQueryLower.isBlank()) {
                    enrolledStudents
                } else {
                    enrolledStudents.filter { student ->
                        val name = student.name?.lowercase() ?: ""
                        val email = student.email.lowercase()
                        name.contains(searchQueryLower) || email.contains(searchQueryLower)
                    }
                }

                val isDeleteNetworkError = !classIsLoading && enrolledStudents.isEmpty() && classError != null && ErrorMessageMapper.isNetworkError(classError)
                if (isDeleteNetworkError) {
                    Text("네트워크가 불안정합니다", color = Gray600)
                } else if (enrolledStudents.isEmpty()) {
                    Text("삭제할 학생이 없습니다.", color = Gray600)
                } else if (filteredStudents.isEmpty()) {
                    Text("검색 결과가 없습니다.", color = Gray600)
                } else {
                    filteredStudents.forEach { student ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
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
                        modifier = Modifier.weight(1f),
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
                        enabled = selectedToDelete.isNotEmpty(),
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (showDeleteConfirmDialog && selectedToDelete.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "학생 제거",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text = "선택한 ${selectedToDelete.size}명의 학생을 이 반에서 제거하시겠습니까?\n제거된 학생의 과제, 질문, 답변이 모두 삭제됩니다.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        VTButton(
                            text = "취소",
                            onClick = {
                                showDeleteConfirmDialog = false
                            },
                            variant = ButtonVariant.Outline,
                            size = ButtonSize.Medium,
                            modifier = Modifier.weight(1f),
                        )
                        VTButton(
                            text = "제거",
                            onClick = {
                                classId?.let { id ->
                                    coroutineScope.launch {
                                        try {
                                            withContext(Dispatchers.IO) {
                                                for (studentId in selectedToDelete) {
                                                    classRepository.removeStudentFromClass(id, studentId)
                                                }
                                            }
                                            withContext(Dispatchers.Main) {
                                                classViewModel.loadClassStudents(id)
                                                val actualTeacherId = teacherId ?: currentUser?.id?.toString()
                                                actualTeacherId?.let {
                                                    viewModel.loadAllStudents(teacherId = it, classId = id.toString())
                                                }
                                                showDeleteConfirmDialog = false
                                                showDeleteSheet = false
                                                selectedToDelete.clear()
                                            }
                                        } catch (e: Exception) {
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
                            size = ButtonSize.Medium,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {},
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
    isLastItem: Boolean,
) {
    androidx.compose.material3.Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Gray200.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(AVATAR_SIZE.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(PrimaryIndigo.copy(alpha = AVATAR_BACKGROUND_ALPHA)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = student.name?.takeIf { it.isNotBlank() }?.firstOrNull()?.toString() ?: "?",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo,
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = student.name ?: "이름 없음",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800,
                        )
                        Text(
                            text = run {
                                val email = student.email
                                if (email.length > EMAIL_MAX_LENGTH) email.take(EMAIL_MAX_LENGTH) + "..." else email
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600,
                        )
                    }
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    val scoreLabelText = if (isLoadingStats) "평균 점수: 로딩 중" else "평균 점수: ${averageScore.toInt()}점"

                    val scoreColor = PrimaryIndigo

                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .background(PrimaryIndigo.copy(alpha = SCORE_BADGE_ALPHA))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = scoreLabelText,
                            style = MaterialTheme.typography.bodySmall,
                            color = scoreColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val completionText = if (isLoadingStats) {
                    "과제 완료: 로딩 중"
                } else {
                    if (totalAssignments > 0) "과제 완료: $completedAssignments/$totalAssignments" else "과제 완료: -"
                }
                val completionRateValue = (completionRate).toInt()
                val completionRateText = if (isLoadingStats) "완료율: 로딩 중" else "완료율: $completionRateValue%"

                val completionColor = Gray600
                val progressColor = PrimaryIndigo

                Text(
                    text = completionText,
                    style = MaterialTheme.typography.bodySmall,
                    color = completionColor,
                    fontWeight = FontWeight.Medium,
                )

                Text(
                    text = completionRateText,
                    style = MaterialTheme.typography.bodySmall,
                    color = progressColor,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progressColor = PrimaryIndigo

            VTProgressBar(
                progress = if (totalAssignments > 0) (completionRate / 100) else 0f,
                showPercentage = false,
                color = progressColor,
                height = PROGRESS_BAR_HEIGHT,
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
    ) {
        if (!isLastItem) {
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                color = Gray200,
                thickness = 0.5.dp,
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
