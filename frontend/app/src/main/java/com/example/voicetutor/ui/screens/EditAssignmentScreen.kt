package com.example.voicetutor.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAssignmentScreen(
    assignmentViewModel: AssignmentViewModel? = null,
    teacherId: String? = null, // 실제 선생님 ID 사용
    assignmentId: Int = 0,
    assignmentTitle: String? = null, // For backward compatibility
    onSaveAssignment: () -> Unit = {}
) {
    val classViewModel: ClassViewModel = hiltViewModel()
    val viewModel: AssignmentViewModel = assignmentViewModel ?: hiltViewModel()
    
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val classes by classViewModel.classes.collectAsStateWithLifecycle()
    val currentAssignment by viewModel.currentAssignment.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val assignmentStats by viewModel.assignmentStatistics.collectAsStateWithLifecycle()
    val context = LocalContext.current
    
    // Find assignment by ID or title from the assignments list
    val targetAssignment = remember(assignments, assignmentId, assignmentTitle) {
        if (assignmentId > 0) {
            assignments.find { it.id == assignmentId }
        } else if (assignmentTitle != null) {
        assignments.find { it.title == assignmentTitle }
        } else {
            null
        }
    }
    
    // 동적 과제 제목 가져오기
    val dynamicAssignmentTitle = currentAssignment?.title ?: targetAssignment?.title ?: assignmentTitle ?: "과제"
    var title: String by remember { mutableStateOf(dynamicAssignmentTitle) }
    var description by remember { mutableStateOf("세포분열 과정을 단계별로 설명하고, 각 단계에서 일어나는 주요 변화들을 정리해보세요.") }
    
    // 삭제 확인 다이얼로그 상태
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedClass by remember { mutableStateOf("고등학교 1학년 A반") }
    var dueDate by remember { mutableStateOf("2024-01-20 23:59") }
    var assignmentType by remember { mutableStateOf("연속형") }
    var dueDateError by remember { mutableStateOf<String?>(null) }
    var validationDialogMessage by remember { mutableStateOf<String?>(null) }
    
    // Load data on first composition
    LaunchedEffect(assignmentId, targetAssignment?.id, teacherId) {
        if (assignmentId > 0) {
            println("EditAssignment - Loading assignment by ID: $assignmentId")
            viewModel.loadAssignmentById(assignmentId)
        } else {
        targetAssignment?.let { target ->
            println("EditAssignment - Loading assignment: ${target.title} (ID: ${target.id})")
            viewModel.loadAssignmentById(target.id)
            }
        }
        teacherId?.let { id ->
            classViewModel.loadClasses(id)
        }
    }
    
    // Update form data when assignment is loaded
    LaunchedEffect(currentAssignment) {
        currentAssignment?.let { assignment ->
            title = assignment.title
            description = assignment.description ?: ""
            selectedClass = assignment.courseClass.name
            dueDate = normalizeDateTime(assignment.dueAt) ?: assignment.dueAt
            assignmentType = "연속형" // type 속성이 없으므로 기본값
            dueDateError = null
        }
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            viewModel.clearError()
        }
    }
    
    // Convert API data to UI format
    val classNames = classes.map { it.name }
    
    val assignmentTypes = listOf("연속형", "객관식", "토론형")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header removed - now handled by MainLayout
        
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
        } else {
            // Assignment info card
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
                        text = "과제 정보",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "과제의 기본 정보를 수정할 수 있습니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600
                    )
                }
            }

        // Form
        VTCard(variant = CardVariant.Elevated) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Assignment title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("과제 제목") },
                    placeholder = { Text("예: 생물학 - 세포분열 연속형 과제") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Title,
                            contentDescription = null,
                            tint = PrimaryIndigo
                        )
                    }
                )
                
                // Assignment description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("과제 설명") },
                    placeholder = { Text("과제에 대한 자세한 설명을 입력해주세요...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            tint = PrimaryIndigo
                        )
                    }
                )
                
                // Class selection
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedClass,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("대상 학급") },
                        placeholder = { Text("학급을 선택해주세요") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.School,
                                contentDescription = null,
                                tint = PrimaryIndigo
                            )
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        classNames.forEach { className ->
                            DropdownMenuItem(
                                text = { Text(className) },
                                onClick = {
                                    selectedClass = className
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                // Due date
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = {
                        dueDate = it
                        dueDateError = null
                    },
                    label = { Text("마감일") },
                    placeholder = { Text("예: 2024-01-20 23:59") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Schedule,
                            contentDescription = null,
                            tint = PrimaryIndigo
                        )
                    },
                    supportingText = {
                        Text(
                            text = dueDateError ?: "yyyy-MM-dd HH:mm 형식으로 입력하세요",
                            color = if (dueDateError != null) Error else Gray500,
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    isError = dueDateError != null
                )
            }
        }
        
        // Statistics card
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "과제 진행 현황",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${assignmentStats?.totalStudents ?: 0}명",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo
                        )
                        Text(
                            text = "총 학생",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${assignmentStats?.submittedStudents ?: 0}명",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Success
                        )
                        Text(
                            text = "제출 완료",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${assignmentStats?.completionRate ?: 0}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Warning
                        )
                        Text(
                            text = "완료율",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                }
            }
        }
        
        // Action button
        VTButton(
            text = "저장",
            onClick = {
                dueDateError = validateDateTime(dueDate)
                
                if (title.isNotBlank() && description.isNotBlank() && 
                    selectedClass.isNotBlank() && dueDate.isNotBlank() &&
                    dueDateError == null) {
                    val isoDue = formatToIso8601(dueDate)
                    
                    if (isoDue == null) {
                        validationDialogMessage = "날짜 형식이 올바르지 않습니다."
                        return@VTButton
                    }
                    
                    val assignmentIdToUpdate = if (assignmentId > 0) assignmentId else targetAssignment?.id
                    if (assignmentIdToUpdate == null) {
                        validationDialogMessage = "수정할 과제 ID를 찾을 수 없습니다."
                        return@VTButton
                    }
                    
                    val updateRequest = com.example.voicetutor.data.network.UpdateAssignmentRequest(
                        title = title,
                        description = description,
                        totalQuestions = currentAssignment?.totalQuestions,
                        dueAt = isoDue,
                        grade = currentAssignment?.grade,
                        subject = currentAssignment?.courseClass?.subject?.let {
                            com.example.voicetutor.data.network.SubjectUpdateRequest(
                                id = it.id,
                                name = it.name,
                                code = it.code
                            )
                        }
                    )
                    
                    viewModel.updateAssignment(assignmentIdToUpdate, updateRequest)
                    Toast.makeText(context, "과제가 성공적으로 수정되었습니다.", Toast.LENGTH_SHORT).show()
                    onSaveAssignment()
                } else {
                    val message = dueDateError
                        ?: "필수 항목을 모두 입력하고 올바른 형식인지 확인해주세요."
                    validationDialogMessage = message
                }
            },
            variant = ButtonVariant.Gradient,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Save,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
        
        // Danger zone
        VTCard(
            variant = CardVariant.Outlined,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "경고",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Error
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "과제를 삭제하면 모든 학생의 제출 내용과 점수가 영구적으로 삭제됩니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                VTButton(
                    text = "과제 삭제",
                    onClick = { showDeleteDialog = true },
                    variant = ButtonVariant.Outline,
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
        
        // 삭제 확인 다이얼로그
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        text = "과제 삭제",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = "정말로 이 과제를 삭제하시겠습니까?\n삭제된 과제는 복구할 수 없습니다.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    VTButton(
                        text = "삭제",
                        onClick = {
                            // 실제 삭제 API 호출
                            targetAssignment?.id?.let { id ->
                                viewModel.deleteAssignment(id)
                            }
                            showDeleteDialog = false
                            onSaveAssignment() // 삭제 후 뒤로가기
                        },
                        variant = ButtonVariant.Primary,
                        size = ButtonSize.Small
                    )
                },
                dismissButton = {
                    VTButton(
                        text = "취소",
                        onClick = { showDeleteDialog = false },
                        variant = ButtonVariant.Outline,
                        size = ButtonSize.Small
                    )
                }
            )
        }
        
        validationDialogMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { validationDialogMessage = null },
                title = {
                    Text(
                        text = "입력 오류",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                confirmButton = {
                    VTButton(
                        text = "확인",
                        onClick = { validationDialogMessage = null },
                        variant = ButtonVariant.Primary,
                        size = ButtonSize.Small
                    )
                }
            )
        }
        
        LaunchedEffect(error) {
            error?.let {
                Toast.makeText(context, "과제 수정에 문제가 발생했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun EditAssignmentScreenPreview() {
    VoiceTutorTheme {
        EditAssignmentScreen()
    }
}

private fun validateDateTime(input: String): String? {
    if (input.isBlank()) return "날짜와 시간을 입력해주세요"
    return try {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        formatter.isLenient = false
        formatter.parse(input)
        null
    } catch (e: ParseException) {
        "유효한 날짜가 아닙니다. 형식: yyyy-MM-dd HH:mm"
    }
}

private fun normalizeDateTime(input: String?): String? {
    if (input.isNullOrBlank()) return null
    val targetFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
        isLenient = false
    }
    val patterns = listOf(
        "yyyy-MM-dd HH:mm",
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd"
    )
    for (pattern in patterns) {
        try {
            val parser = SimpleDateFormat(pattern, Locale.getDefault()).apply { isLenient = false }
            val date = parser.parse(input)
            if (date != null) {
                return targetFormat.format(date)
            }
        } catch (_: ParseException) {
        }
    }
    return null
}

private fun formatToIso8601(input: String): String? {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).apply {
            isLenient = false
            timeZone = TimeZone.getTimeZone("Asia/Seoul")
        }
        val date = parser.parse(input) ?: return null
        val adjustedDate = Date(date.time + 9 * 60 * 60 * 1000L)
        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        isoFormatter.format(adjustedDate)
    } catch (e: ParseException) {
        null
    }
}
