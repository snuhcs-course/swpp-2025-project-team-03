package com.example.voicetutor.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.viewmodel.ClassViewModel
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.ui.viewmodel.StudentViewModel
import com.example.voicetutor.file.FileManager
import com.example.voicetutor.file.FileType
import com.example.voicetutor.file.FileInfo
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAssignmentScreen(
    authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel? = null,
    assignmentViewModel: AssignmentViewModel? = null,
    teacherId: String? = null,
    onCreateAssignment: (String) -> Unit = {} // Pass assignment title on success
) {
    val actualAuthViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = authViewModel ?: hiltViewModel()
    val actualAssignmentViewModel: AssignmentViewModel = assignmentViewModel ?: hiltViewModel()
    val classViewModel: ClassViewModel = hiltViewModel()
    val studentViewModel: StudentViewModel = hiltViewModel()
    
    val currentUser by actualAuthViewModel.currentUser.collectAsStateWithLifecycle()
    val actualTeacherId = teacherId ?: currentUser?.id?.toString() ?: "1"
    
    val classes by classViewModel.classes.collectAsStateWithLifecycle()
    val students by studentViewModel.students.collectAsStateWithLifecycle()
    val isLoading by actualAssignmentViewModel.isLoading.collectAsStateWithLifecycle()
    val error by actualAssignmentViewModel.error.collectAsStateWithLifecycle()
    val currentAssignment by actualAssignmentViewModel.currentAssignment.collectAsStateWithLifecycle()
    val isUploading by actualAssignmentViewModel.isUploading.collectAsStateWithLifecycle()
    val uploadProgress by actualAssignmentViewModel.uploadProgress.collectAsStateWithLifecycle()
    val uploadSuccess by actualAssignmentViewModel.uploadSuccess.collectAsStateWithLifecycle()
    
    val context = LocalContext.current
    val fileManager = remember { FileManager(context) }
    val coroutineScope = rememberCoroutineScope()
    
    // 과제 생성 성공 플래그
    var assignmentCreated by remember { mutableStateOf(false) }
    
    // 파일 선택 상태
    var selectedFiles by remember { mutableStateOf<List<FileInfo>>(emptyList()) }
    var selectedPdfFile by remember { mutableStateOf<File?>(null) }
    
    // PDF 파일 피커
    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            println("=== PDF 파일 선택 디버그 ===")
            println("선택된 URI: $uri")
            println("URI 스키마: ${uri.scheme}")
            println("URI 호스트: ${uri.host}")
            println("URI 경로: ${uri.path}")
            println("URI 쿼리: ${uri.query}")
            
            // URI에서 파일명 추출 시도
            try {
                val fileName = uri.lastPathSegment
                println("URI에서 추출한 파일명: $fileName")
            } catch (e: Exception) {
                println("URI에서 파일명 추출 실패: ${e.message}")
            }
            
            coroutineScope.launch {
                fileManager.saveFile(uri, fileType = FileType.DOCUMENT)
                    .onSuccess { fileInfo ->
                        println("✅ 파일 저장 성공")
                        println("원본 파일명: ${fileInfo.name}")
                        println("파일 경로: ${fileInfo.path}")
                        println("파일 크기: ${fileInfo.size} bytes")
                        println("파일 타입: ${fileInfo.type}")
                        println("파일 확장자: ${fileInfo.name.substringAfterLast('.', "")}")
                        
                        selectedFiles = listOf(fileInfo)
                        selectedPdfFile = File(fileInfo.path)
                        println("selectedPdfFile 설정됨: ${selectedPdfFile?.name}")
                        println("selectedPdfFile 절대 경로: ${selectedPdfFile?.absolutePath}")
                    }
                    .onFailure { exception ->
                        println("❌ 파일 저장 실패: ${exception.message}")
                    }
            }
        }
    }
    
    // 학생 선택 상태
    var selectedStudents by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    var assignmentTitle by remember { mutableStateOf("") }
    var assignmentDescription by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("") }
    var selectedClassId by remember { mutableStateOf<Int?>(null) }
    var selectedGrade by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var timeLimit by remember { mutableStateOf("15") }
    var questionCount by remember { mutableStateOf("5") }
    var assignToAll by remember { mutableStateOf(true) }
    var classSelectionExpanded by remember { mutableStateOf(false) }
    var gradeSelectionExpanded by remember { mutableStateOf(false) }
    var subjectSelectionExpanded by remember { mutableStateOf(false) }
    
    // Load data on first composition
    LaunchedEffect(actualTeacherId) {
        classViewModel.loadClasses(actualTeacherId)
        studentViewModel.loadAllStudents(teacherId = actualTeacherId)
    }
    
    // Navigate to assignment detail when creation succeeds
    LaunchedEffect(currentAssignment, assignmentCreated, uploadSuccess) {
        if (assignmentCreated && (currentAssignment != null || uploadSuccess)) {
            println("Assignment created successfully: ${currentAssignment?.title}")
            currentAssignment?.let { assignment ->
                onCreateAssignment(assignment.title)
            }
            // 업로드 성공 시 상태 리셋
            if (uploadSuccess) {
                actualAssignmentViewModel.resetUploadState()
            }
        }
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            actualAssignmentViewModel.clearError()
        }
    }
    
    // Convert API data to UI format
    val classNames = classes.map { "${it.name} - ${it.subject}" }
    
    // 학년 리스트
    val grades = listOf(
        "초등학교 1학년", "초등학교 2학년", "초등학교 3학년", 
        "초등학교 4학년", "초등학교 5학년", "초등학교 6학년",
        "중학교 1학년", "중학교 2학년", "중학교 3학년",
        "고등학교 1학년", "고등학교 2학년", "고등학교 3학년"
    )
    
    // 과목 리스트
    val subjects = listOf("국어", "영어", "수학", "과학", "사회")
    
    val studentNames = students.map { it.name }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
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
                        value = assignmentTitle,
                        onValueChange = { assignmentTitle = it },
                        label = { Text("과제 제목") },
                        placeholder = { Text("예: 세포 구조와 기능 복습") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true
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
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = classSelectionExpanded,
                            onDismissRequest = { classSelectionExpanded = false }
                        ) {
                            classes.forEachIndexed { index, classData ->
                                val className = "${classData.name} - ${classData.subject.name}"
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
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeSelectionExpanded)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.School,
                                    contentDescription = null,
                                    tint = PrimaryIndigo
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                unfocusedBorderColor = Gray300
                            ),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = gradeSelectionExpanded,
                            onDismissRequest = { gradeSelectionExpanded = false }
                        ) {
                            grades.forEach { grade ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = grade,
                                            fontWeight = FontWeight.Medium
                                        ) 
                                    },
                                    onClick = {
                                        selectedGrade = grade
                                        gradeSelectionExpanded = false
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
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectSelectionExpanded)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = PrimaryIndigo
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                unfocusedBorderColor = Gray300
                            ),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = subjectSelectionExpanded,
                            onDismissRequest = { subjectSelectionExpanded = false }
                        ) {
                            subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = subject,
                                            fontWeight = FontWeight.Medium
                                        ) 
                                    },
                                    onClick = {
                                        selectedSubject = subject
                                        subjectSelectionExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.MenuBook,
                                            contentDescription = null,
                                            tint = PrimaryIndigo
                                        )
                                    }
                                )
                            }
                        }
                    }
                    
                    // Assignment description
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
                            imeAction = ImeAction.Done
                        )
                    )
                    
                    // Due date and time limit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            label = { Text("마감일") },
                            placeholder = { Text("2024-01-15") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = timeLimit,
                            onValueChange = { timeLimit = it },
                            label = { Text("제한시간 (분)") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done
                            ),
                            singleLine = true
                        )
                    }
                }
            }
        }
        
        // PDF upload section (mandatory)
        VTCard(variant = CardVariant.Elevated) {
            Column {
                Text(
                    text = "PDF 자료 업로드 (필수)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    VTButton(
                        text = if (selectedFiles.isEmpty()) "파일 선택" else "파일 추가",
                        onClick = { 
                            // PDF 파일만 선택 가능하도록 MIME 타입 지정
                            pdfPickerLauncher.launch("application/pdf")
                        },
                        variant = ButtonVariant.Outline,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Upload,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "최대 10MB, PDF 파일만 지원",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    
                    // PDF 업로드 진행률 표시
                    if (isUploading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Column {
                            Text(
                                text = "PDF 업로드 중...",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray800
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = uploadProgress.coerceIn(0f, 1f),
                                modifier = Modifier.fillMaxWidth(),
                                color = PrimaryIndigo,
                                trackColor = Gray300
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${(uploadProgress.coerceIn(0f, 1f) * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray600
                            )
                        }
                    }
                    
                    // 업로드 성공 표시
                    if (uploadSuccess) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Success.copy(alpha = 0.1f),
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Success,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PDF 업로드 완료!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Success,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    // 선택된 파일 목록 표시
                    if (selectedFiles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "선택된 파일 (${selectedFiles.size}개)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        selectedFiles.forEach { file ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.PictureAsPdf,
                                        contentDescription = null,
                                        tint = Error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = file.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Gray800
                                        )
                                        Text(
                                            text = fileManager.formatFileSize(file.size),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Gray600
                                        )
                                    }
                                }
                                
                                IconButton(
                                    onClick = {
                                        selectedFiles = selectedFiles.filter { it.path != file.path }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "파일 제거",
                                        tint = Gray500,
                                        modifier = Modifier.size(20.dp)
                                    )
                            }
                        }
                    }
                }
            }
        }
        
        // Question settings
        VTCard(variant = CardVariant.Elevated) {
            Column {
                Text(
                    text = "문제 설정",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                    OutlinedTextField(
                        value = questionCount,
                        onValueChange = { questionCount = it },
                        label = { Text("문제 개수") },
                    placeholder = { Text("5") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )
            }
        }
        
        // Student assignment
        VTCard(variant = CardVariant.Elevated) {
            Column {
                Text(
                    text = "과제 배정",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = assignToAll,
                            onClick = { assignToAll = true }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "전체 학생에게 배정",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Gray800
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = !assignToAll,
                            onClick = { assignToAll = false }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "선택한 학생에게만 배정",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Gray800
                        )
                    }
                    
                    // Student selection (when not assigning to all)
                    if (!assignToAll) {
                        Column(
                            modifier = Modifier.padding(start = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 전체 선택 체크박스
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Checkbox(
                                    checked = selectedStudents.size == studentNames.take(5).size && studentNames.take(5).isNotEmpty(),
                                    onCheckedChange = { isChecked ->
                                        selectedStudents = if (isChecked) {
                                            studentNames.take(5).filterNotNull().toSet()
                                        } else {
                                            emptySet()
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "전체 선택",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = PrimaryIndigo
                                )
                            }
                            studentNames.take(5).forEach { student ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = student in selectedStudents,
                                        onCheckedChange = { isChecked ->
                                            if (student != null) {
                                                selectedStudents = if (isChecked) {
                                                    selectedStudents + student
                                                } else {
                                                    selectedStudents - student
                                                }
                                            }
                                        }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                PrimaryIndigo.copy(alpha = 0.1f),
                                                androidx.compose.foundation.shape.CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = student?.first()?.toString() ?: "?",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = PrimaryIndigo
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = student ?: "이름 없음",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Gray800
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Action buttons
        val isFormValid = assignmentTitle.isNotBlank() && assignmentDescription.isNotBlank() && 
            selectedClass.isNotBlank() && selectedClassId != null && 
            selectedGrade.isNotBlank() && selectedSubject.isNotBlank() &&
            dueDate.isNotBlank() && timeLimit.isNotBlank() && 
            questionCount.isNotBlank() && selectedFiles.isNotEmpty()
        
        VTButton(
            text = "과제 생성",
            onClick = {
                if (isFormValid && selectedClassId != null) {
                    // 데모용 샘플 서술형 질문 생성 (음성 답변 + AI 꼬리 질문 형태)
                    val sampleQuestions = when (selectedSubject) {
                        "과학" -> listOf(
                            com.example.voicetutor.data.models.QuestionData(
                                id = 1,
                                question = "광합성이 무엇인지 설명해주세요.",
                                type = "VOICE_RESPONSE",
                                options = null,
                                correctAnswer = "광합성은 식물이 빛 에너지를 이용해 이산화탄소와 물로 포도당을 만드는 과정입니다.",
                                explanation = "광합성은 식물이 빛 에너지를 이용해 이산화탄소와 물로 포도당을 만드는 과정입니다."
                            ),
                            com.example.voicetutor.data.models.QuestionData(
                                id = 2,
                                question = "그렇다면 광합성이 일어나기 위해 필요한 조건은 무엇인가요?",
                                type = "VOICE_RESPONSE",
                                options = null,
                                correctAnswer = "빛, 이산화탄소, 물, 엽록체가 필요합니다.",
                                explanation = "빛, 이산화탄소, 물, 엽록체가 필요합니다."
                            ),
                            com.example.voicetutor.data.models.QuestionData(
                                id = 3,
                                question = "광합성의 결과로 만들어지는 산소는 어디에서 나오나요?",
                                type = "VOICE_RESPONSE",
                                options = null,
                                correctAnswer = "물이 분해되면서 산소가 발생합니다.",
                                explanation = "물이 분해되면서 산소가 발생합니다."
                            )
                        )
                        "수학" -> listOf(
                            com.example.voicetutor.data.models.QuestionData(
                                id = 1,
                                question = "이차방정식이 무엇인지 설명해주세요.",
                                type = "VOICE_RESPONSE",
                                options = null,
                                correctAnswer = "이차방정식은 미지수의 최고차항이 2인 방정식입니다.",
                                explanation = "이차방정식은 미지수의 최고차항이 2인 방정식입니다."
                            ),
                            com.example.voicetutor.data.models.QuestionData(
                                id = 2,
                                question = "그렇다면 이차방정식을 푸는 방법에는 어떤 것들이 있나요?",
                                type = "VOICE_RESPONSE",
                                options = null,
                                correctAnswer = "인수분해, 완전제곱식, 근의 공식 등이 있습니다.",
                                explanation = "인수분해, 완전제곱식, 근의 공식 등이 있습니다."
                            ),
                            com.example.voicetutor.data.models.QuestionData(
                                id = 3,
                                question = "근의 공식은 언제 사용하는 것이 좋을까요?",
                                type = "VOICE_RESPONSE",
                                options = null,
                                correctAnswer = "인수분해가 어려울 때 근의 공식을 사용하면 편리합니다.",
                                explanation = "인수분해가 어려울 때 근의 공식을 사용하면 편리합니다."
                            )
                        )
                        "국어" -> listOf(
                            com.example.voicetutor.data.models.QuestionData(
                                id = 1,
                                question = "비유적 표현이란 무엇인지 설명해주세요.",
                                type = "VOICE_RESPONSE",
                                options = null,
                                correctAnswer = "다른 대상에 빗대어 표현하는 방법입니다.",
                                explanation = "다른 대상에 빗대어 표현하는 방법입니다."
                            ),
                            com.example.voicetutor.data.models.QuestionData(
                                id = 2,
                                question = "은유와 직유의 차이점은 무엇인가요?",
                                type = "VOICE_RESPONSE",
                                options = null,
                                correctAnswer = "직유는 '~처럼'을 사용하고, 은유는 직접 다른 것이라고 표현합니다.",
                                explanation = "직유는 '~처럼'을 사용하고, 은유는 직접 다른 것이라고 표현합니다."
                            ),
                            com.example.voicetutor.data.models.QuestionData(
                                id = 3,
                                question = "의인법의 예를 하나 들어보세요.",
                                type = "VOICE_RESPONSE",
                                options = null,
                                correctAnswer = "'꽃이 웃는다', '바람이 노래한다' 등이 있습니다.",
                                explanation = "'꽃이 웃는다', '바람이 노래한다' 등이 있습니다."
                            )
                        )
                        "영어" -> listOf(
                            com.example.voicetutor.data.models.QuestionData(
                                id = 1,
                                question = "현재완료 시제가 무엇인지 설명해주세요.",
                                type = "VOICE_RESPONSE",
                                options = null,
                                correctAnswer = "과거에 시작된 일이 현재까지 영향을 미치는 것을 나타냅니다.",
                                explanation = "과거에 시작된 일이 현재까지 영향을 미치는 것을 나타냅니다."
                            ),
                            com.example.voicetutor.data.models.QuestionData(
                                id = 2,
                                question = "현재완료는 어떻게 만드나요?",
                                type = "VOICE_RESPONSE",
                                options = null,
                                correctAnswer = "have/has + 과거분사 형태로 만듭니다.",
                                explanation = "have/has + 과거분사 형태로 만듭니다."
                            ),
                            com.example.voicetutor.data.models.QuestionData(
                                id = 3,
                                question = "현재완료와 과거시제의 차이는 무엇인가요?",
                                type = "VOICE_RESPONSE",
                                options = null,
                                correctAnswer = "과거시제는 과거의 특정 시점을, 현재완료는 과거부터 현재까지를 나타냅니다.",
                                explanation = "과거시제는 과거의 특정 시점을, 현재완료는 과거부터 현재까지를 나타냅니다."
                            )
                        )
                        "사회" -> listOf(
                            com.example.voicetutor.data.models.QuestionData(
                                id = 1,
                                question = "민주주의가 무엇인지 설명해주세요.",
                                type = "VOICE_RESPONSE",
                                options = null,
                                correctAnswer = "국민이 주권을 가지고 정치에 참여하는 제도입니다.",
                                explanation = "국민이 주권을 가지고 정치에 참여하는 제도입니다."
                            ),
                            com.example.voicetutor.data.models.QuestionData(
                                id = 2,
                                question = "민주주의의 기본 원리에는 어떤 것들이 있나요?",
                                type = "VOICE_RESPONSE",
                                options = null,
                                correctAnswer = "국민주권, 권력분립, 기본권 보장 등이 있습니다.",
                                explanation = "국민주권, 권력분립, 기본권 보장 등이 있습니다."
                            ),
                            com.example.voicetutor.data.models.QuestionData(
                                id = 3,
                                question = "권력분립이 왜 중요한가요?",
                                type = "VOICE_RESPONSE",
                                options = null,
                                correctAnswer = "권력의 집중을 막고 상호 견제를 통해 국민의 자유를 보장하기 위함입니다.",
                                explanation = "권력의 집중을 막고 상호 견제를 통해 국민의 자유를 보장하기 위함입니다."
                            )
                        )
                        else -> listOf(
                            com.example.voicetutor.data.models.QuestionData(
                                id = 1,
                                question = "이 주제에 대해 설명해주세요.",
                                type = "VOICE_RESPONSE",
                                options = null,
                                correctAnswer = "자세히 설명해주시면 더 깊이 있게 이야기 나눠볼 수 있습니다.",
                                explanation = "자세히 설명해주시면 더 깊이 있게 이야기 나눠볼 수 있습니다."
                            ),
                            com.example.voicetutor.data.models.QuestionData(
                                id = 2,
                                question = "조금 더 구체적으로 설명해주실 수 있나요?",
                                type = "VOICE_RESPONSE",
                                options = null,
                                correctAnswer = "예시를 들어 설명해주시면 좋습니다.",
                                explanation = "예시를 들어 설명해주시면 좋습니다."
                            ),
                            com.example.voicetutor.data.models.QuestionData(
                                id = 3,
                                question = "그렇다면 이것이 왜 중요한가요?",
                                type = "VOICE_RESPONSE",
                                options = null,
                                correctAnswer = "실생활에서의 적용 사례를 생각해보세요.",
                                explanation = "실생활에서의 적용 사례를 생각해보세요."
                            )
                        )
                    }
                    
                    val createRequest = com.example.voicetutor.data.network.CreateAssignmentRequest(
                        title = assignmentTitle,
                        subject = selectedSubject,
                        class_id = selectedClassId!!,
                        due_at = dueDate,
                        grade = selectedGrade,
                        type = "Quiz",  // PDF 과제는 항상 Quiz 타입
                        description = assignmentDescription,
                        questions = sampleQuestions
                    )
                    
                    println("=== 과제 생성 디버그 ===")
                    println("Creating assignment: $createRequest")
                    println("Grade: $selectedGrade, Subject: $selectedSubject")
                    println("PDF files: ${selectedFiles.map { it.name }}")
                    println("Sample questions: ${sampleQuestions.size}개 생성")
                    println("selectedPdfFile: ${selectedPdfFile?.name}")
                    println("selectedPdfFile != null: ${selectedPdfFile != null}")
                    println("selectedFiles.size: ${selectedFiles.size}")
                    
                    // PDF 파일이 선택된 경우 PDF 업로드와 함께 과제 생성
                    val pdfFile = selectedPdfFile
                    if (pdfFile != null) {
                        println("✅ PDF 업로드와 함께 과제 생성")
                        println("PDF 파일: ${pdfFile.name}")
                        println("파일 크기: ${pdfFile.length()} bytes")
                        actualAssignmentViewModel.createAssignmentWithPdf(createRequest, pdfFile)
                    } else {
                        // PDF 파일이 없는 경우 일반 과제 생성
                        println("❌ PDF 파일이 없음 - 일반 과제 생성")
                        actualAssignmentViewModel.createAssignment(createRequest)
                    }
                    
                    assignmentCreated = true  // 플래그 설정
                }
            },
            variant = ButtonVariant.Gradient,
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid && !isLoading,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
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
