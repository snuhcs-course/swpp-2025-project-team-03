package com.example.voicetutor.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.viewmodel.ClassViewModel
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    var description by remember { mutableStateOf("") }
    
    // 삭제 확인 다이얼로그 상태
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedClass by remember { mutableStateOf("") }
    var selectedClassId by remember { mutableStateOf<Int?>(null) }
    var selectedGrade by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("") }
    var dueDateText by remember { mutableStateOf("") }
    var dueDateRequest by remember { mutableStateOf("") }
    var dueDateTime by remember { mutableStateOf<LocalDateTime?>(null) }
    var dueShowDatePicker by remember { mutableStateOf(false) }
    var dueShowTimePicker by remember { mutableStateOf(false) }
    var duePendingDate by remember { mutableStateOf<LocalDate?>(null) }
    var validationDialogMessage by remember { mutableStateOf<String?>(null) }
    
    val displayDateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm") }
    val zoneId = remember { ZoneId.systemDefault() }
    
    // 학년 리스트
    val grades = listOf(
        "초등학교 1학년", "초등학교 2학년", "초등학교 3학년", 
        "초등학교 4학년", "초등학교 5학년", "초등학교 6학년",
        "중학교 1학년", "중학교 2학년", "중학교 3학년",
        "고등학교 1학년", "고등학교 2학년", "고등학교 3학년"
    )
    
    // 과목 리스트
    val subjects = listOf("국어", "영어", "수학", "과학", "사회")
    
    var classSelectionExpanded by remember { mutableStateOf(false) }
    var gradeSelectionExpanded by remember { mutableStateOf(false) }
    var subjectSelectionExpanded by remember { mutableStateOf(false) }
    
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
            selectedClassId = assignment.courseClass.id
            selectedClass = assignment.courseClass.name
            
            // 학년과 과목 설정
            // assignment.grade가 있으면 사용, 없으면 빈 문자열
            selectedGrade = assignment.grade ?: ""
            selectedSubject = assignment.courseClass.subject.name
            
            // 마감일 파싱
            val normalizedDate = normalizeDateTime(assignment.dueAt)
            if (normalizedDate != null) {
                try {
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                    dueDateTime = LocalDateTime.parse(normalizedDate, formatter)
                    dueDateText = normalizedDate
                    dueDateRequest = dueDateTime
                        ?.atZone(zoneId)
                        ?.toOffsetDateTime()
                        ?.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME) ?: ""
                } catch (e: Exception) {
                    println("EditAssignment - Error parsing due date: ${e.message}")
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
            // Basic info section
            VTCard(variant = CardVariant.Elevated) {
                Column {
                    Text(
                        text = "기본 정보",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Assignment title
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("과제 제목") },
                            placeholder = { Text("예: 세포 구조와 기능 복습") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                focusedLabelColor = PrimaryIndigo,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black
                            )
                        )
                        
                        // Class selection
                        ExposedDropdownMenuBox(
                            expanded = classSelectionExpanded,
                            onExpandedChange = { classSelectionExpanded = !classSelectionExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedClass,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("반 선택") },
                                placeholder = { Text("과제를 배정할 반을 선택하세요") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = classSelectionExpanded)
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Class,
                                        contentDescription = null,
                                        tint = PrimaryIndigo
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryIndigo,
                                    focusedLabelColor = PrimaryIndigo,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    cursorColor = Color.Black
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = classSelectionExpanded,
                                onDismissRequest = { classSelectionExpanded = false }
                            ) {
                                classes.forEachIndexed { index, classData ->
                                    val className = classData.name
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                text = className,
                                                fontWeight = FontWeight.Medium
                                            ) 
                                        },
                                        onClick = {
                                            selectedClass = className
                                            selectedClassId = classData.id
                                            classSelectionExpanded = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Filled.School,
                                                contentDescription = null,
                                                tint = PrimaryIndigo
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Grade selection
                        ExposedDropdownMenuBox(
                            expanded = gradeSelectionExpanded,
                            onExpandedChange = { gradeSelectionExpanded = !gradeSelectionExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedGrade,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("학년") },
                                placeholder = { Text("학년을 선택하세요") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeSelectionExpanded)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryIndigo,
                                    focusedLabelColor = PrimaryIndigo,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    cursorColor = Color.Black
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = gradeSelectionExpanded,
                                onDismissRequest = { gradeSelectionExpanded = false }
                            ) {
                                grades.forEach { grade ->
                                    DropdownMenuItem(
                                        text = { Text(grade) },
                                        onClick = {
                                            selectedGrade = grade
                                            gradeSelectionExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Subject selection
                        ExposedDropdownMenuBox(
                            expanded = subjectSelectionExpanded,
                            onExpandedChange = { subjectSelectionExpanded = !subjectSelectionExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedSubject,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("과목") },
                                placeholder = { Text("과목을 선택하세요") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectSelectionExpanded)
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryIndigo,
                                    focusedLabelColor = PrimaryIndigo,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    cursorColor = Color.Black
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = subjectSelectionExpanded,
                                onDismissRequest = { subjectSelectionExpanded = false }
                            ) {
                                subjects.forEach { subject ->
                                    DropdownMenuItem(
                                        text = { Text(subject) },
                                        onClick = {
                                            selectedSubject = subject
                                            subjectSelectionExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        // Assignment description
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("설명") },
                            placeholder = { Text("과제에 대한 상세 설명을 입력하세요") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp),
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Done
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                focusedLabelColor = PrimaryIndigo,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black
                            )
                        )
                        
                        // Due date
                        val dueDateInteractionSource = remember { MutableInteractionSource() }
                        LaunchedEffect(dueDateInteractionSource) {
                            dueDateInteractionSource.interactions.collect { interaction ->
                                if (interaction is PressInteraction.Release) {
                                    dueShowDatePicker = true
                                }
                            }
                        }
                        OutlinedTextField(
                            value = dueDateText,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("마감일") },
                            placeholder = { Text("날짜와 시간을 선택하세요") },
                            modifier = Modifier
                                .fillMaxWidth(),
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Event,
                                    contentDescription = null,
                                    tint = PrimaryIndigo
                                )
                            },
                            singleLine = true,
                            interactionSource = dueDateInteractionSource,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                focusedLabelColor = PrimaryIndigo,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black
                            )
                        )

                    }
                }
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
                if (title.isNotBlank() && description.isNotBlank() && 
                    selectedClass.isNotBlank() && dueDateText.isNotBlank() &&
                    dueDateRequest.isNotBlank()) {
                    
                    val assignmentIdToUpdate = if (assignmentId > 0) assignmentId else targetAssignment?.id
                    if (assignmentIdToUpdate == null) {
                        validationDialogMessage = "수정할 과제 ID를 찾을 수 없습니다."
                        return@VTButton
                    }
                    
                    val updateRequest = com.example.voicetutor.data.network.UpdateAssignmentRequest(
                        title = title,
                        description = description,
                        totalQuestions = currentAssignment?.totalQuestions,
                        dueAt = dueDateRequest,
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
                    validationDialogMessage = "필수 항목을 모두 입력하고 올바른 형식인지 확인해주세요."
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
        
        // DatePicker Dialog
        if (dueShowDatePicker) {
            val initialDateMillis = dueDateTime
                ?.atZone(zoneId)
                ?.toInstant()
                ?.toEpochMilli()
                ?: Instant.now().toEpochMilli()
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

            DatePickerDialog(
                onDismissRequest = {
                    dueShowDatePicker = false
                    duePendingDate = null
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedMillis = datePickerState.selectedDateMillis
                            if (selectedMillis != null) {
                                duePendingDate = Instant.ofEpochMilli(selectedMillis)
                                    .atZone(zoneId)
                                    .toLocalDate()
                                dueShowDatePicker = false
                                dueShowTimePicker = true
                            }
                        },
                        enabled = datePickerState.selectedDateMillis != null,
                        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryIndigo)
                    ) {
                        Text("시간 선택")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            dueShowDatePicker = false
                            duePendingDate = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Gray600)
                    ) {
                        Text("취소")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    colors = DatePickerDefaults.colors(
                        containerColor = Color.White,
                        titleContentColor = Gray800,
                        headlineContentColor = Gray800,
                        weekdayContentColor = Gray600,
                        dayContentColor = Gray800,
                        selectedDayContainerColor = PrimaryIndigo,
                        selectedDayContentColor = Color.White,
                        todayDateBorderColor = PrimaryIndigo
                    )
                )
            }
        }

        // TimePicker Dialog
        if (dueShowTimePicker) {
            val initialHour = dueDateTime?.hour ?: LocalTime.now().hour
            val initialMinute = dueDateTime?.minute ?: LocalTime.now().minute
            val timePickerState = rememberTimePickerState(
                initialHour = initialHour,
                initialMinute = initialMinute,
                is24Hour = true
            )

            AlertDialog(
                onDismissRequest = {
                    dueShowTimePicker = false
                    duePendingDate = null
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedDate = duePendingDate ?: dueDateTime?.toLocalDate() ?: LocalDate.now()
                            val selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                            val finalDateTime = LocalDateTime.of(selectedDate, selectedTime)
                            dueDateTime = finalDateTime
                            dueDateText = finalDateTime.format(displayDateFormatter)
                            dueDateRequest = finalDateTime
                                .atZone(zoneId)
                                .toOffsetDateTime()
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                            dueShowTimePicker = false
                            duePendingDate = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryIndigo)
                    ) {
                        Text("확인")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            dueShowTimePicker = false
                            duePendingDate = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Gray600)
                    ) {
                        Text("취소")
                    }
                },
                title = {
                    Text(
                        text = "시간 선택",
                        style = MaterialTheme.typography.titleMedium,
                        color = Gray800
                    )
                },
                text = {
                    TimePicker(state = timePickerState)
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

@Preview(showBackground = true)
@Composable
fun EditAssignmentScreenPreview() {
    VoiceTutorTheme {
        EditAssignmentScreen()
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
