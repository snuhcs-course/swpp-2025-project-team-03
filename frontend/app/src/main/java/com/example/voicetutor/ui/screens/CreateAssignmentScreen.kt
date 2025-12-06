package com.example.voicetutor.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.data.network.CreateAssignmentRequest
import com.example.voicetutor.file.FileInfo
import com.example.voicetutor.file.FileManager
import com.example.voicetutor.file.FileType
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.utils.ErrorMessageMapper
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.ui.viewmodel.ClassViewModel
import com.example.voicetutor.ui.viewmodel.StudentViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAssignmentScreen(
    authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel? = null,
    assignmentViewModel: AssignmentViewModel? = null,
    teacherId: String? = null,
    initialClassId: Int? = null,
    onCreateAssignment: (String) -> Unit = {},
) {
    val actualAuthViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = authViewModel ?: hiltViewModel()
    val actualAssignmentViewModel: AssignmentViewModel = assignmentViewModel ?: hiltViewModel()
    val classViewModel: ClassViewModel = hiltViewModel()
    val studentViewModel: StudentViewModel = hiltViewModel()

    val currentUser by actualAuthViewModel.currentUser.collectAsStateWithLifecycle()
    val actualTeacherId = teacherId ?: currentUser?.id?.toString() ?: "1"

    val classes by classViewModel.classes.collectAsStateWithLifecycle()
    val classError by classViewModel.error.collectAsStateWithLifecycle()
    val classIsLoading by classViewModel.isLoading.collectAsStateWithLifecycle()
    val students by studentViewModel.students.collectAsStateWithLifecycle()
    val isCreatingAssignment by actualAssignmentViewModel.isCreatingAssignment.collectAsStateWithLifecycle()
    val error by actualAssignmentViewModel.error.collectAsStateWithLifecycle()
    val currentAssignment by actualAssignmentViewModel.currentAssignment.collectAsStateWithLifecycle()
    val isUploading by actualAssignmentViewModel.isUploading.collectAsStateWithLifecycle()
    val uploadProgress by actualAssignmentViewModel.uploadProgress.collectAsStateWithLifecycle()
    val uploadSuccess by actualAssignmentViewModel.uploadSuccess.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val fileManager = remember { FileManager(context) }
    val coroutineScope = rememberCoroutineScope()

    // PDF 파일 크기 제한: 10MB
    val maxPdfSizeBytes = 10 * 1024 * 1024L // 10MB in bytes

    var assignmentCreated by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    var classSelectionFieldPosition by remember { mutableStateOf(0) }

    var selectedFiles by remember { mutableStateOf<List<FileInfo>>(emptyList()) }
    var selectedPdfFile by remember { mutableStateOf<File?>(null) }
    var pdfFileSizeError by remember { mutableStateOf<String?>(null) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                // 먼저 파일 크기 확인 시도
                val fileSize = withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                            if (sizeIndex >= 0 && cursor.moveToFirst()) {
                                val size = cursor.getLong(sizeIndex)
                                if (size > 0) size else null
                            } else {
                                null
                            }
                        }
                    } catch (e: Exception) {
                        null
                    }
                }

                // 파일 크기를 가져올 수 있는 경우 미리 검증
                if (fileSize != null && fileSize > maxPdfSizeBytes) {
                    // 파일이 너무 크면 선택하지 않고 에러 메시지 표시
                    pdfFileSizeError = "파일 용량이 너무 큽니다. 최대 10MB까지 업로드 가능합니다."
                } else {
                    // 파일 크기를 미리 알 수 없거나 적절한 경우 저장 후 검증
                    fileManager.saveFile(uri, fileType = FileType.DOCUMENT)
                        .onSuccess { fileInfo ->
                            // 저장 후 실제 파일 크기로 다시 검증
                            if (fileInfo.size > maxPdfSizeBytes) {
                                // 파일이 너무 크면 선택하지 않고 에러 메시지 표시
                                pdfFileSizeError = "파일 용량이 너무 큽니다. 최대 10MB까지 업로드 가능합니다."
                                // 임시로 저장된 파일 삭제
                                fileManager.deleteFile(fileInfo.path)
                            } else {
                                // 파일 크기가 적절하면 정상적으로 선택
                                pdfFileSizeError = null // 에러 클리어
                                selectedFiles = listOf(fileInfo)
                                selectedPdfFile = File(fileInfo.path)
                            }
                        }
                        .onFailure { }
                }
            }
        }
    }

    var selectedStudents by remember { mutableStateOf<Set<Int>>(emptySet()) }

    var assignmentTitle by remember { mutableStateOf("") }
    var assignmentTitleError by remember { mutableStateOf<String?>(null) }
    var assignmentDescription by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("") }
    var selectedClassId by remember { mutableStateOf<Int?>(null) }
    var selectedGrade by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("") }
    var dueDateText by remember { mutableStateOf("") }
    var dueDateRequest by remember { mutableStateOf("") }
    var dueDateTime by remember { mutableStateOf<Calendar?>(null) }
    var questionCount by remember { mutableStateOf("5") }
    var questionCountError by remember { mutableStateOf<String?>(null) }
    var assignToAll by remember { mutableStateOf(true) }
    var showClassSelectionWarning by remember { mutableStateOf(false) }
    var classSelectionExpanded by remember { mutableStateOf(false) }
    var hasRetriedLoadClasses by remember { mutableStateOf(false) }
    var gradeSelectionExpanded by remember { mutableStateOf(false) }
    var subjectSelectionExpanded by remember { mutableStateOf(false) }
    var dueShowDatePicker by remember { mutableStateOf(false) }
    var dueShowTimePicker by remember { mutableStateOf(false) }
    var duePendingDate by remember { mutableStateOf<Calendar?>(null) }

    val displayDateFormatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val isoDateFormatter = remember { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") } }

    val classStudents by classViewModel.classStudents.collectAsStateWithLifecycle()
    val isLoadingClassStudents by classViewModel.isLoading.collectAsStateWithLifecycle()

    val displayStudents = remember(selectedClassId, classStudents, students) {
        if (selectedClassId != null && classStudents.isNotEmpty()) {
            classStudents
        } else {
            emptyList()
        }
    }

    LaunchedEffect(actualTeacherId) {
        classViewModel.loadClasses(actualTeacherId)
        studentViewModel.loadAllStudents(teacherId = actualTeacherId)
        actualAssignmentViewModel.resetUploadState()
        hasRetriedLoadClasses = false // 화면 진입 시 재시도 플래그 리셋
    }

    LaunchedEffect(selectedClassId) {
        val classId = selectedClassId
        if (classId != null) {
            classViewModel.loadClassStudents(classId)
            selectedStudents = emptySet()
            assignToAll = true
            showClassSelectionWarning = false
        }
    }

    var hasSetInitialClass by remember { mutableStateOf(false) }
    LaunchedEffect(classes.size, initialClassId) {
        if (initialClassId != null && classes.isNotEmpty() && !hasSetInitialClass) {
            val targetClass = classes.find { it.id == initialClassId }
            targetClass?.let { classData ->
                selectedClassId = classData.id
                selectedClass = classData.name
                hasSetInitialClass = true
            }
        }
    }

    LaunchedEffect(currentAssignment, assignmentCreated, uploadSuccess) {
        if (assignmentCreated && uploadSuccess) {
            currentAssignment?.let { assignment ->
                onCreateAssignment(assignment.title)
            }
        }
    }

    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            actualAssignmentViewModel.clearError()
        }
    }

    // classViewModel의 에러도 네트워크 에러가 아닌 경우에만 클리어합니다.
    classError?.let { errorMessage ->
        LaunchedEffect(errorMessage, classIsLoading) {
            // 로딩이 완료되고, 네트워크 에러가 아닌 경우에만 클리어
            if (!classIsLoading && !ErrorMessageMapper.isNetworkError(errorMessage)) {
                classViewModel.clearError()
            }
        }
    }

    val grades = listOf(
        "초등학교 1학년", "초등학교 2학년", "초등학교 3학년",
        "초등학교 4학년", "초등학교 5학년", "초등학교 6학년",
        "중학교 1학년", "중학교 2학년", "중학교 3학년",
        "고등학교 1학년", "고등학교 2학년", "고등학교 3학년",
    )

    val subjects = listOf("국어", "영어", "수학", "과학", "사회")

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (isCreatingAssignment) {
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
                                value = assignmentTitle,
                                onValueChange = {
                                    val sanitized = it.replace("/", "")
                                    if (sanitized.length != it.length) {
                                        assignmentTitleError = "'/' 문자는 사용할 수 없어요."
                                    } else {
                                        assignmentTitleError = null
                                    }
                                    assignmentTitle = sanitized
                                },
                                label = { Text("과제 제목") },
                                placeholder = { Text("예: 세포 구조와 기능 복습") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Next,
                                ),
                                singleLine = true,
                                isError = assignmentTitleError != null,
                                supportingText = assignmentTitleError?.let { error ->
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

                            val isNetworkErrorState = remember(classIsLoading, classes.size, classError) {
                                !classIsLoading && classes.isEmpty() && classError != null && ErrorMessageMapper.isNetworkError(classError)
                            }

                            // 네트워크 에러가 해결되면 재시도 플래그 리셋
                            LaunchedEffect(isNetworkErrorState) {
                                if (!isNetworkErrorState) {
                                    hasRetriedLoadClasses = false
                                }
                            }

                            ExposedDropdownMenuBox(
                                expanded = classSelectionExpanded,
                                onExpandedChange = { expanded ->
                                    val wasExpanded = classSelectionExpanded
                                    classSelectionExpanded = expanded
                                    // 드롭다운이 닫혔다가 다시 열릴 때만 재시도 (false -> true 전환)
                                    if (expanded && !wasExpanded) {
                                        val currentIsNetworkError = !classIsLoading && classes.isEmpty() && classError != null && ErrorMessageMapper.isNetworkError(classError)
                                        if (currentIsNetworkError && !hasRetriedLoadClasses) {
                                            hasRetriedLoadClasses = true
                                            classViewModel.loadClasses(actualTeacherId)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .onGloballyPositioned { coordinates ->
                                        classSelectionFieldPosition = coordinates.positionInParent().y.toInt()
                                    },
                            ) {
                                OutlinedTextField(
                                    value = selectedClass,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("수업 선택") },
                                    placeholder = {
                                        Text(
                                            if (isNetworkErrorState) {
                                                "네트워크가 불안정합니다"
                                            } else {
                                                "과제를 배정할 수업을 선택하세요"
                                            },
                                        )
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = classSelectionExpanded)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Class,
                                            contentDescription = null,
                                            tint = PrimaryIndigo,
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
                                        cursorColor = Color.Black,
                                    ),
                                )
                                ExposedDropdownMenu(
                                    expanded = classSelectionExpanded,
                                    onDismissRequest = { classSelectionExpanded = false },
                                ) {
                                    classes.forEachIndexed { _, classData ->
                                        val className = classData.name
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = className,
                                                    fontWeight = FontWeight.Medium,
                                                )
                                            },
                                            onClick = {
                                                selectedClass = className
                                                selectedClassId = classData.id
                                                classSelectionExpanded = false
                                                classViewModel.loadClassStudents(classData.id)
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Filled.School,
                                                    contentDescription = null,
                                                    tint = PrimaryIndigo,
                                                )
                                            },
                                        )
                                    }
                                }
                            }

                            ExposedDropdownMenuBox(
                                expanded = gradeSelectionExpanded,
                                onExpandedChange = { gradeSelectionExpanded = !gradeSelectionExpanded },
                                modifier = Modifier.fillMaxWidth(),
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
                                        cursorColor = Color.Black,
                                    ),
                                )
                                ExposedDropdownMenu(
                                    expanded = gradeSelectionExpanded,
                                    onDismissRequest = { gradeSelectionExpanded = false },
                                ) {
                                    grades.forEach { grade ->
                                        DropdownMenuItem(
                                            text = { Text(grade) },
                                            onClick = {
                                                selectedGrade = grade
                                                gradeSelectionExpanded = false
                                            },
                                        )
                                    }
                                }
                            }

                            ExposedDropdownMenuBox(
                                expanded = subjectSelectionExpanded,
                                onExpandedChange = { subjectSelectionExpanded = !subjectSelectionExpanded },
                                modifier = Modifier.fillMaxWidth(),
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
                                        cursorColor = Color.Black,
                                    ),
                                )
                                ExposedDropdownMenu(
                                    expanded = subjectSelectionExpanded,
                                    onDismissRequest = { subjectSelectionExpanded = false },
                                ) {
                                    subjects.forEach { subject ->
                                        DropdownMenuItem(
                                            text = { Text(subject) },
                                            onClick = {
                                                selectedSubject = subject
                                                subjectSelectionExpanded = false
                                            },
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = assignmentDescription,
                                onValueChange = { assignmentDescription = it },
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

                VTCard(variant = CardVariant.Elevated) {
                    Column {
                        Text(
                            text = "PDF 자료 업로드 (필수)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        VTButton(
                            text = if (selectedFiles.isEmpty()) "파일 선택" else "파일 추가",
                            onClick = {
                                pdfFileSizeError = null // 에러 클리어
                                pdfPickerLauncher.launch("application/pdf")
                            },
                            variant = ButtonVariant.Outline,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Upload,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                )
                            },
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "최대 10MB, PDF 파일만 지원",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600,
                        )

                        // 파일 크기 에러 메시지 표시
                        if (pdfFileSizeError != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Error.copy(alpha = 0.1f),
                                        shape = MaterialTheme.shapes.small,
                                    )
                                    .padding(12.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = Error,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = pdfFileSizeError!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Error,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }

                        if (isUploading) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Column {
                                Text(
                                    text = "PDF 업로드 중...",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Gray800,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { uploadProgress.coerceIn(0f, 1f) },
                                    modifier = Modifier.fillMaxWidth(),
                                    color = PrimaryIndigo,
                                    trackColor = Gray300,
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${(uploadProgress.coerceIn(0f, 1f) * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray600,
                                )
                            }
                        }

                        if (uploadSuccess && selectedFiles.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Success.copy(alpha = 0.1f),
                                        shape = MaterialTheme.shapes.small,
                                    )
                                    .padding(12.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Success,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "PDF 업로드 완료!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Success,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }

                        if (selectedFiles.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "선택된 파일 (${selectedFiles.size}개)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray800,
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            selectedFiles.forEach { file ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.PictureAsPdf,
                                            contentDescription = null,
                                            tint = Error,
                                            modifier = Modifier.size(20.dp),
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = file.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Gray800,
                                            )
                                            Text(
                                                text = fileManager.formatFileSize(file.size),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Gray600,
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            selectedFiles = selectedFiles.filter { it.path != file.path }
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "파일 제거",
                                            tint = Gray500,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                VTCard(variant = CardVariant.Elevated) {
                    Column {
                        Text(
                            text = "문제 설정",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = questionCount,
                            onValueChange = { newValue ->
                                // 숫자만 허용
                                val filtered = newValue.filter { it.isDigit() }

                                if (filtered != newValue) {
                                    questionCount = filtered
                                } else {
                                    questionCount = newValue
                                }

                                // 검증 로직
                                if (filtered.isBlank()) {
                                    questionCountError = null // 빈 값일 때는 에러 표시 안 함 (사용자가 입력 중일 수 있음)
                                } else {
                                    val count = filtered.toIntOrNull()
                                    if (count == null || count <= 0) {
                                        questionCountError = "문제 개수는 1 이상이어야 합니다"
                                    } else if (count > 10) {
                                        questionCountError = "문제 개수는 10개를 초과할 수 없습니다"
                                    } else {
                                        questionCountError = null
                                    }
                                }
                            },
                            label = { Text("문제 개수") },
                            placeholder = { Text("5") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                            ),
                            singleLine = true,
                            isError = questionCountError != null,
                            supportingText = questionCountError?.let { error ->
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
                    }
                }

                VTCard(variant = CardVariant.Elevated) {
                    Column {
                        Text(
                            text = "과제 배정",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800,
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = assignToAll,
                                    onClick = { assignToAll = true },
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "전체 학생에게 배정",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Gray800,
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = !assignToAll,
                                    onClick = {
                                        if (selectedClassId != null) {
                                            assignToAll = false
                                            showClassSelectionWarning = false
                                        } else {
                                            showClassSelectionWarning = true
                                            coroutineScope.launch {
                                                scrollState.animateScrollTo(classSelectionFieldPosition)
                                            }
                                        }
                                    },
                                    enabled = selectedClassId != null,
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "선택한 학생에게만 배정",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = if (selectedClassId != null) Gray800 else Gray400,
                                )
                            }

                            if (showClassSelectionWarning && selectedClassId == null) {
                                Text(
                                    text = "⚠️ 반을 먼저 선택해주세요",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Warning,
                                    modifier = Modifier.padding(start = 24.dp),
                                )
                            }

                            if (!assignToAll && selectedClassId != null) {
                                Column(
                                    modifier = Modifier.padding(start = 24.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    if (isLoadingClassStudents) {
                                        Text(
                                            text = "학생 목록을 불러오는 중...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Gray600,
                                        )
                                    } else if (displayStudents.isEmpty()) {
                                        Text(
                                            text = "해당 반에 학생이 등록되지 않았습니다.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Gray600,
                                        )
                                    } else {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 8.dp),
                                        ) {
                                            Checkbox(
                                                checked = selectedStudents.size == displayStudents.size && displayStudents.isNotEmpty(),
                                                onCheckedChange = { isChecked ->
                                                    selectedStudents = if (isChecked) {
                                                        displayStudents.map { it.id }.toSet()
                                                    } else {
                                                        emptySet()
                                                    }
                                                },
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "전체 선택",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Medium,
                                                color = PrimaryIndigo,
                                            )
                                        }
                                        displayStudents.forEach { student ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Checkbox(
                                                    checked = student.id in selectedStudents,
                                                    onCheckedChange = { isChecked ->
                                                        selectedStudents = if (isChecked) {
                                                            selectedStudents + student.id
                                                        } else {
                                                            selectedStudents - student.id
                                                        }
                                                    },
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = student.name ?: "이름 없음",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = Gray800,
                                                    )
                                                    Text(
                                                        text = student.email,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Gray600,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                val isFormValid = assignmentTitle.isNotBlank() && assignmentDescription.isNotBlank() &&
                    selectedClass.isNotBlank() && selectedClassId != null &&
                    selectedGrade.isNotBlank() && selectedSubject.isNotBlank() &&
                    dueDateRequest.isNotBlank() &&
                    questionCount.isNotBlank() && selectedFiles.isNotEmpty() &&
                    assignmentTitleError == null && questionCountError == null

                VTButton(
                    text = "과제 생성",
                    onClick = {
                        if (isFormValid && selectedClassId != null) {
                            // 최종 검증 (빈 값이거나 0 이하인 경우 방지)
                            val questionCountInt = questionCount.toIntOrNull()
                            if (questionCountInt == null || questionCountInt <= 0) {
                                questionCountError = "문제 개수는 1 이상이어야 합니다"
                            } else {
                                val createRequest = CreateAssignmentRequest.builder()
                                    .title(assignmentTitle)
                                    .subject(selectedSubject)
                                    .classId(selectedClassId!!)
                                    .dueAt(dueDateRequest)
                                    .grade(selectedGrade)
                                    .description(assignmentDescription)
                                    .totalQuestions(questionCountInt)
                                    .build()

                                val pdfFile = selectedPdfFile
                                if (pdfFile != null) {
                                    actualAssignmentViewModel.createAssignmentWithPdf(createRequest, pdfFile, totalNumber = questionCountInt, teacherId = actualTeacherId)
                                } else {
                                    actualAssignmentViewModel.createAssignment(createRequest, teacherId = actualTeacherId)
                                }

                                assignmentCreated = true
                            }
                        }
                    },
                    variant = ButtonVariant.Gradient,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isFormValid && !isCreatingAssignment,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                )
            }
        }

        if (dueShowDatePicker) {
            val initialDateMillis = dueDateTime?.timeInMillis
                ?: Calendar.getInstance().timeInMillis

            val todayCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val todayMillis = todayCalendar.timeInMillis

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
                                if (selectedMillis >= todayMillis) {
                                    duePendingDate = Calendar.getInstance().apply {
                                        timeInMillis = selectedMillis
                                    }
                                    dueShowDatePicker = false
                                    dueShowTimePicker = true
                                }
                            }
                        },
                        enabled = datePickerState.selectedDateMillis != null &&
                            (datePickerState.selectedDateMillis ?: 0) >= todayMillis,
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
            val selectedDateCalendar = duePendingDate ?: dueDateTime ?: Calendar.getInstance()
            val todayCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val selectedDateOnly = Calendar.getInstance().apply {
                timeInMillis = selectedDateCalendar.timeInMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val isToday = selectedDateOnly.timeInMillis == todayCalendar.timeInMillis

            val initialHour = if (isToday && dueDateTime == null) {
                now.get(Calendar.HOUR_OF_DAY)
            } else {
                dueDateTime?.get(Calendar.HOUR_OF_DAY) ?: now.get(Calendar.HOUR_OF_DAY)
            }
            val initialMinute = if (isToday && dueDateTime == null) {
                now.get(Calendar.MINUTE)
            } else {
                dueDateTime?.get(Calendar.MINUTE) ?: now.get(Calendar.MINUTE)
            }

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
                            val finalDateTime = Calendar.getInstance().apply {
                                timeInMillis = selectedDateCalendar.timeInMillis
                                set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                                set(Calendar.MINUTE, timePickerState.minute)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }

                            val isValid = if (isToday) {
                                finalDateTime.after(Calendar.getInstance())
                            } else {
                                true
                            }

                            if (isValid) {
                                dueDateTime = finalDateTime
                                dueDateText = displayDateFormatter.format(finalDateTime.time)

                                val utcCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                                    timeInMillis = finalDateTime.timeInMillis
                                }
                                dueDateRequest = isoDateFormatter.format(utcCalendar.time)

                                dueShowTimePicker = false
                                duePendingDate = null
                            }
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

        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(enabled = false) { },
                contentAlignment = Alignment.Center,
            ) {
                VTCard(
                    variant = CardVariant.Elevated,
                    modifier = Modifier
                        .padding(32.dp)
                        .wrapContentSize(),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(24.dp),
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryIndigo,
                            modifier = Modifier.size(48.dp),
                        )

                        Text(
                            text = "PDF 업로드 중...",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800,
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            LinearProgressIndicator(
                                progress = { uploadProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp),
                                color = PrimaryIndigo,
                                trackColor = Gray200,
                            )
                            Text(
                                text = "${(uploadProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray600,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreateAssignmentScreenPreview() {
    VoiceTutorTheme {
        CreateAssignmentScreen()
    }
}
