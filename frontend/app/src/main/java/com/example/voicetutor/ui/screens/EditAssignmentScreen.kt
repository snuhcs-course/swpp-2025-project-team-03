package com.example.voicetutor.ui.screens

import android.widget.Toast
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAssignmentScreen(
    assignmentViewModel: AssignmentViewModel? = null,
    teacherId: String? = null,
    assignmentId: Int = 0,
    assignmentTitle: String? = null,
    onSaveAssignment: () -> Unit = {},
    onDeleteAssignment: () -> Unit = {},
) {
    val viewModel: AssignmentViewModel = assignmentViewModel ?: hiltViewModel()

    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val currentAssignment by viewModel.currentAssignment.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val assignmentStats by viewModel.assignmentStatistics.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val targetAssignment = remember(assignments, assignmentId, assignmentTitle) {
        if (assignmentId > 0) {
            assignments.find { it.id == assignmentId }
        } else if (assignmentTitle != null) {
            assignments.find { it.title == assignmentTitle }
        } else {
            null
        }
    }

    val dynamicAssignmentTitle = currentAssignment?.title ?: targetAssignment?.title ?: assignmentTitle ?: "과제"
    var title: String by remember { mutableStateOf(dynamicAssignmentTitle) }
    var titleError by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedClass by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("") }
    var dueDateText by remember { mutableStateOf("") }
    var dueDateRequest by remember { mutableStateOf("") }
    var dueDateTime by remember { mutableStateOf<Calendar?>(null) }
    var dueShowDatePicker by remember { mutableStateOf(false) }
    var dueShowTimePicker by remember { mutableStateOf(false) }
    var duePendingDate by remember { mutableStateOf<Calendar?>(null) }
    var validationDialogMessage by remember { mutableStateOf<String?>(null) }
    var isUpdatingAssignment by remember { mutableStateOf(false) }
    var isDeletingAssignment by remember { mutableStateOf(false) }

    val displayDateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val isoDateFormatter = remember { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") } }

    LaunchedEffect(assignmentId, targetAssignment?.id, teacherId) {
        if (assignmentId > 0) {
            viewModel.loadAssignmentById(assignmentId)
        } else {
            targetAssignment?.let { target ->
                viewModel.loadAssignmentById(target.id)
            }
        }
    }

    LaunchedEffect(currentAssignment) {
        currentAssignment?.let { assignment ->
            title = assignment.title
            description = assignment.description ?: ""
            selectedClass = assignment.courseClass.name

            selectedGrade = assignment.grade ?: ""
            selectedSubject = assignment.courseClass.subject.name

            val normalizedDate = normalizeDateTime(assignment.dueAt)
            if (normalizedDate != null) {
                try {
                    val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val date = formatter.parse(normalizedDate)
                    if (date != null) {
                        dueDateTime = Calendar.getInstance().apply {
                            time = date
                        }
                        dueDateText = normalizedDate
                        val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                            time = date
                        }
                        dueDateRequest = isoDateFormatter.format(utcCalendar.time)
                    }
                } catch (_: Exception) {
                }
            }
        }
    }

    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            viewModel.clearError()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = PrimaryIndigo,
                )
            }
        } else {
            VTCard(variant = CardVariant.Elevated) {
                Column {
                    Text(
                        text = "기본 정보",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                val sanitized = it.replace("/", "")
                                titleError = if (sanitized.length != it.length) {
                                    "'/' 문자는 사용할 수 없어요."
                                } else {
                                    null
                                }
                                title = sanitized
                            },
                            label = { Text("과제 제목") },
                            placeholder = { Text("예: 세포 구조와 기능 복습") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Next,
                            ),
                            singleLine = true,
                            isError = titleError != null,
                            supportingText = titleError?.let { error ->
                                {
                                    Text(text = error, color = Error)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                focusedLabelColor = PrimaryIndigo,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black,
                            ),
                        )

                        ExposedDropdownMenuBox(
                            expanded = false,
                            onExpandedChange = { },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedTextField(
                                value = selectedClass,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                label = { Text("수업 선택 (변경 불가)") },
                                placeholder = { Text("과제를 배정할 수업을 선택하세요") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Gray400,
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.Class,
                                        contentDescription = null,
                                        tint = Gray400,
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Gray300,
                                    unfocusedBorderColor = Gray300,
                                    focusedLabelColor = Gray500,
                                    unfocusedLabelColor = Gray500,
                                    focusedTextColor = Gray600,
                                    unfocusedTextColor = Gray600,
                                    disabledTextColor = Gray600,
                                    disabledBorderColor = Gray300,
                                    disabledLabelColor = Gray500,
                                    disabledPlaceholderColor = Gray400,
                                    cursorColor = Color.Black,
                                ),
                            )
                        }

                        ExposedDropdownMenuBox(
                            expanded = false,
                            onExpandedChange = { },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedTextField(
                                value = selectedGrade,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                label = { Text("학년 (변경 불가)") },
                                placeholder = { Text("학년을 선택하세요") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Gray400,
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Gray300,
                                    unfocusedBorderColor = Gray300,
                                    focusedLabelColor = Gray500,
                                    unfocusedLabelColor = Gray500,
                                    focusedTextColor = Gray600,
                                    unfocusedTextColor = Gray600,
                                    disabledTextColor = Gray600,
                                    disabledBorderColor = Gray300,
                                    disabledLabelColor = Gray500,
                                    disabledPlaceholderColor = Gray400,
                                    cursorColor = Color.Black,
                                ),
                            )
                        }

                        ExposedDropdownMenuBox(
                            expanded = false,
                            onExpandedChange = { },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            OutlinedTextField(
                                value = selectedSubject,
                                onValueChange = {},
                                readOnly = true,
                                enabled = false,
                                label = { Text("과목 (변경 불가)") },
                                placeholder = { Text("과목을 선택하세요") },
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowDropDown,
                                        contentDescription = null,
                                        tint = Gray400,
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Gray300,
                                    unfocusedBorderColor = Gray300,
                                    focusedLabelColor = Gray500,
                                    unfocusedLabelColor = Gray500,
                                    focusedTextColor = Gray600,
                                    unfocusedTextColor = Gray600,
                                    disabledTextColor = Gray600,
                                    disabledBorderColor = Gray300,
                                    disabledLabelColor = Gray500,
                                    disabledPlaceholderColor = Gray400,
                                    cursorColor = Color.Black,
                                ),
                            )
                        }

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
                                imeAction = ImeAction.Done,
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                focusedLabelColor = PrimaryIndigo,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black,
                            ),
                        )

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
                                    tint = PrimaryIndigo,
                                )
                            },
                            singleLine = true,
                            interactionSource = dueDateInteractionSource,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                focusedLabelColor = PrimaryIndigo,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black,
                            ),
                        )
                    }
                }
            }
        }

        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "과제 진행 현황",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800,
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "${assignmentStats?.totalStudents ?: 0}명",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryIndigo,
                        )
                        Text(
                            text = "총 학생",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600,
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "${assignmentStats?.submittedStudents ?: 0}명",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Success,
                        )
                        Text(
                            text = "제출 완료",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600,
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "${assignmentStats?.completionRate ?: 0}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Warning,
                        )
                        Text(
                            text = "완료율",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600,
                        )
                    }
                }
            }
        }

        VTButton(
            text = "저장",
            onClick = {
                if (title.isNotBlank() && description.isNotBlank() &&
                    selectedClass.isNotBlank() && dueDateText.isNotBlank() &&
                    dueDateRequest.isNotBlank()
                ) {
                    val assignmentIdToUpdate = if (assignmentId > 0) assignmentId else targetAssignment?.id
                    if (assignmentIdToUpdate == null) {
                        validationDialogMessage = "수정할 과제 ID를 찾을 수 없습니다."
                        return@VTButton
                    }

                    val updateRequest = com.example.voicetutor.data.network.UpdateAssignmentRequest.builder()
                        .title(title)
                        .description(description)
                        .totalQuestions(currentAssignment?.totalQuestions)
                        .dueAt(dueDateRequest)
                        .grade(currentAssignment?.grade)
                        .subject(
                            currentAssignment?.courseClass?.subject?.let {
                                com.example.voicetutor.data.network.SubjectUpdateRequest(
                                    id = it.id,
                                    name = it.name,
                                    code = it.code,
                                )
                            },
                        )
                        .build()

                    isUpdatingAssignment = true
                    viewModel.updateAssignment(assignmentIdToUpdate, updateRequest)
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
                    modifier = Modifier.size(20.dp),
                )
            },
        )

        VTCard(
            variant = CardVariant.Outlined,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Error,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "경고",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Error,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "과제를 삭제하면 모든 학생의 제출 내용과 점수가 영구적으로 삭제됩니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600,
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
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        text = "과제 삭제",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                text = {
                    Text(
                        text = "정말로 이 과제를 삭제하시겠습니까?\n삭제된 과제는 복구할 수 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                confirmButton = {
                    VTButton(
                        text = "삭제",
                        onClick = {
                            targetAssignment?.id?.let { id ->
                                isDeletingAssignment = true
                                viewModel.deleteAssignment(id)
                                showDeleteDialog = false
                            }
                        },
                        variant = ButtonVariant.Primary,
                        size = ButtonSize.Small,
                    )
                },
                dismissButton = {
                    VTButton(
                        text = "취소",
                        onClick = { showDeleteDialog = false },
                        variant = ButtonVariant.Outline,
                        size = ButtonSize.Small,
                    )
                },
            )
        }

        if (dueShowDatePicker) {
            val initialDateMillis = dueDateTime?.timeInMillis
                ?: Calendar.getInstance().timeInMillis
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
                                duePendingDate = Calendar.getInstance().apply {
                                    timeInMillis = selectedMillis
                                }
                                dueShowDatePicker = false
                                dueShowTimePicker = true
                            }
                        },
                        enabled = datePickerState.selectedDateMillis != null,
                        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryIndigo),
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
                        colors = ButtonDefaults.textButtonColors(contentColor = Gray600),
                    ) {
                        Text("취소")
                    }
                },
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
                        todayDateBorderColor = PrimaryIndigo,
                    ),
                )
            }
        }

        if (dueShowTimePicker) {
            val now = Calendar.getInstance()
            val initialHour = dueDateTime?.get(Calendar.HOUR_OF_DAY) ?: now.get(Calendar.HOUR_OF_DAY)
            val initialMinute = dueDateTime?.get(Calendar.MINUTE) ?: now.get(Calendar.MINUTE)
            val timePickerState = rememberTimePickerState(
                initialHour = initialHour,
                initialMinute = initialMinute,
                is24Hour = true,
            )

            AlertDialog(
                onDismissRequest = {
                    dueShowTimePicker = false
                    duePendingDate = null
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedDateCalendar = duePendingDate ?: dueDateTime ?: Calendar.getInstance()
                            val finalDateTime = Calendar.getInstance().apply {
                                timeInMillis = selectedDateCalendar.timeInMillis
                                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                set(Calendar.MINUTE, timePickerState.minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            dueDateTime = finalDateTime
                            dueDateText = displayDateFormatter.format(finalDateTime.time)
                            val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                timeInMillis = finalDateTime.timeInMillis
                            }
                            dueDateRequest = isoDateFormatter.format(utcCalendar.time)
                            dueShowTimePicker = false
                            duePendingDate = null
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = PrimaryIndigo),
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
                        colors = ButtonDefaults.textButtonColors(contentColor = Gray600),
                    ) {
                        Text("취소")
                    }
                },
                title = {
                    Text(
                        text = "시간 선택",
                        style = MaterialTheme.typography.titleMedium,
                        color = Gray800,
                    )
                },
                text = {
                    TimePicker(state = timePickerState)
                },
            )
        }

        validationDialogMessage?.let { message ->
            AlertDialog(
                onDismissRequest = { validationDialogMessage = null },
                title = {
                    Text(
                        text = "입력 오류",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                text = {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                confirmButton = {
                    VTButton(
                        text = "확인",
                        onClick = { validationDialogMessage = null },
                        variant = ButtonVariant.Primary,
                        size = ButtonSize.Small,
                    )
                },
            )
        }

    LaunchedEffect(isUpdatingAssignment, isLoading, error) {
        if (isUpdatingAssignment && !isLoading) {
            if (error == null) {
                Toast.makeText(context, "과제가 성공적으로 수정되었습니다.", Toast.LENGTH_SHORT).show()
                onSaveAssignment()
            } else {
                Toast.makeText(context, "과제 수정에 실패했습니다. 인터넷 연결을 확인하세요.", Toast.LENGTH_SHORT).show()
            }
            isUpdatingAssignment = false
            viewModel.clearError()
        }
    }

    LaunchedEffect(isDeletingAssignment, isLoading, error) {
        if (isDeletingAssignment && !isLoading) {
            if (error == null) {
                Toast.makeText(context, "과제가 성공적으로 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                onDeleteAssignment()
            } else {
                Toast.makeText(context, "과제 삭제에 실패했습니다. 인터넷 연결을 확인하세요.", Toast.LENGTH_SHORT).show()
            }
            isDeletingAssignment = false
            viewModel.clearError()
        }
    }

    LaunchedEffect(error) {
        if (!isUpdatingAssignment && !isDeletingAssignment) {
            error?.let {
                Toast.makeText(context, "네트워크가 불안정합니다.", Toast.LENGTH_SHORT).show()
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
        "yyyy-MM-dd",
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
