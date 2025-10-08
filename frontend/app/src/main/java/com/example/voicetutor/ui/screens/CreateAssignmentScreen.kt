package com.example.voicetutor.ui.screens

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
import com.example.voicetutor.ui.viewmodel.StudentViewModel

enum class AssignmentCreationType {
    PDF, MIXED, CURRICULUM
}

enum class DifficultyLevel {
    EASY, MEDIUM, HARD, MIXED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAssignmentScreen(
    teacherId: String = "1", // 임시로 기본값 설정
    onCreateAssignment: () -> Unit = {}
) {
    val classViewModel: ClassViewModel = hiltViewModel()
    val assignmentViewModel: AssignmentViewModel = hiltViewModel()
    val studentViewModel: StudentViewModel = hiltViewModel()
    
    val classes by classViewModel.classes.collectAsStateWithLifecycle()
    val students by studentViewModel.students.collectAsStateWithLifecycle()
    val isLoading by assignmentViewModel.isLoading.collectAsStateWithLifecycle()
    val error by assignmentViewModel.error.collectAsStateWithLifecycle()
    
    // 파일 선택 상태
    var selectedFiles by remember { mutableStateOf<List<String>>(emptyList()) }
    var showFileSelectionDialog by remember { mutableStateOf(false) }
    
    // 학생 선택 상태
    var selectedStudents by remember { mutableStateOf<Set<String>>(emptySet()) }
    
    var assignmentTitle by remember { mutableStateOf("") }
    var assignmentDescription by remember { mutableStateOf("") }
    var selectedClass by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var timeLimit by remember { mutableStateOf("15") }
    var questionCount by remember { mutableStateOf("5") }
    var selectedType by remember { mutableStateOf(AssignmentCreationType.PDF) }
    var selectedDifficulty by remember { mutableStateOf(DifficultyLevel.MIXED) }
    var selectedTopics by remember { mutableStateOf(setOf<String>()) }
    var assignToAll by remember { mutableStateOf(true) }
    var classSelectionExpanded by remember { mutableStateOf(false) }
    
    // Load data on first composition
    LaunchedEffect(teacherId) {
        classViewModel.loadClasses(teacherId)
        studentViewModel.loadAllStudents(teacherId = teacherId)
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            assignmentViewModel.clearError()
        }
    }
    
    // Convert API data to UI format
    val classNames = classes.map { "${it.name} - ${it.subject}" }
    
    val curriculumTopics = listOf(
        "세포 구조와 기능",
        "세포분열",
        "유전과 진화",
        "생태계",
        "생물 다양성"
    )
    
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
                        modifier = Modifier.fillMaxWidth()
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
                            classNames.forEach { className ->
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = className,
                                            fontWeight = FontWeight.Medium
                                        ) 
                                    },
                                    onClick = {
                                        selectedClass = className
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
                    
                    // Assignment description
                    OutlinedTextField(
                        value = assignmentDescription,
                        onValueChange = { assignmentDescription = it },
                        label = { Text("설명") },
                        placeholder = { Text("과제에 대한 상세 설명을 입력하세요") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        maxLines = 3
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
                            modifier = Modifier.weight(1f)
                        )
                        
                        OutlinedTextField(
                            value = timeLimit,
                            onValueChange = { timeLimit = it },
                            label = { Text("제한시간 (분)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        // Assignment type selection
        VTCard(variant = CardVariant.Elevated) {
            Column {
                Text(
                    text = "과제 유형",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // PDF Assignment Type
                    VTCard(
                        variant = if (selectedType == AssignmentCreationType.PDF) CardVariant.Selected else CardVariant.Outlined,
                        onClick = { selectedType = AssignmentCreationType.PDF },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Description,
                                contentDescription = null,
                                tint = if (selectedType == AssignmentCreationType.PDF) PrimaryIndigo else Gray500,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "PDF 자료",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedType == AssignmentCreationType.PDF) PrimaryIndigo else Gray800
                            )
                            Text(
                                text = "업로드 자료 사용",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray600
                            )
                        }
                    }
                    
                    // Mixed Assignment Type
                    VTCard(
                        variant = if (selectedType == AssignmentCreationType.MIXED) CardVariant.Selected else CardVariant.Outlined,
                        onClick = { selectedType = AssignmentCreationType.MIXED },
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = if (selectedType == AssignmentCreationType.MIXED) PrimaryIndigo else Gray500,
                                    modifier = Modifier.size(20.dp)
                                )
                                Icon(
                                    imageVector = Icons.Filled.Description,
                                    contentDescription = null,
                                    tint = if (selectedType == AssignmentCreationType.MIXED) PrimaryIndigo else Gray500,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "혼합",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedType == AssignmentCreationType.MIXED) PrimaryIndigo else Gray800
                            )
                            Text(
                                text = "교과과정 + PDF 함께",
                                style = MaterialTheme.typography.bodySmall,
                                color = Gray600
                            )
                        }
                    }
                }
            }
        }
        
        // Curriculum selection (for mixed type)
        if (selectedType == AssignmentCreationType.MIXED) {
            VTCard(variant = CardVariant.Elevated) {
                Column {
                    Text(
                        text = "교과과정 선택",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        curriculumTopics.forEach { topic ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedTopics.contains(topic),
                                    onCheckedChange = { isChecked ->
                                        selectedTopics = if (isChecked) {
                                            selectedTopics + topic
                                        } else {
                                            selectedTopics - topic
                                        }
                                    }
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = topic,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = Gray800
                                    )
                                    Text(
                                        text = "난이도: 보통",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Gray600
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // PDF upload section
        if (selectedType == AssignmentCreationType.PDF || selectedType == AssignmentCreationType.MIXED) {
            VTCard(variant = CardVariant.Elevated) {
                Column {
                    Text(
                        text = "PDF 자료 업로드",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    VTButton(
                        text = "파일 선택",
                        onClick = { showFileSelectionDialog = true },
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
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = questionCount,
                        onValueChange = { questionCount = it },
                        label = { Text("문제 개수") },
                        modifier = Modifier.weight(1f)
                    )
                    
                    var difficultyExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = difficultyExpanded,
                        onExpandedChange = { difficultyExpanded = !difficultyExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = when (selectedDifficulty) {
                                DifficultyLevel.EASY -> "쉬움"
                                DifficultyLevel.MEDIUM -> "보통"
                                DifficultyLevel.HARD -> "어려움"
                                DifficultyLevel.MIXED -> "혼합"
                            },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("난이도") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = difficultyExpanded)
                            },
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = difficultyExpanded,
                            onDismissRequest = { difficultyExpanded = false }
                        ) {
                            listOf(
                                DifficultyLevel.MIXED to "혼합",
                                DifficultyLevel.EASY to "쉬움",
                                DifficultyLevel.MEDIUM to "보통",
                                DifficultyLevel.HARD to "어려움"
                            ).forEach { (level, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        selectedDifficulty = level
                                        difficultyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
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
                                            studentNames.take(5).toSet()
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
                                            selectedStudents = if (isChecked) {
                                                selectedStudents + student
                                            } else {
                                                selectedStudents - student
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
                                            text = student.first().toString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium,
                                            color = PrimaryIndigo
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = student,
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
            selectedClass.isNotBlank() && dueDate.isNotBlank() && timeLimit.isNotBlank() && 
            questionCount.isNotBlank() &&
            (selectedType == AssignmentCreationType.PDF || selectedType == AssignmentCreationType.MIXED || selectedTopics.isNotEmpty())
        
        VTButton(
            text = "과제 생성",
            onClick = {
                if (isFormValid) {
                    onCreateAssignment()
                }
            },
            variant = ButtonVariant.Gradient,
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid,
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
    
    // 파일 선택 다이얼로그
    if (showFileSelectionDialog) {
        FileSelectionDialog(
            selectedFiles = selectedFiles,
            onFilesSelected = { files ->
                selectedFiles = files
                showFileSelectionDialog = false
            },
            onDismiss = { showFileSelectionDialog = false }
        )
    }
}

@Composable
fun FileSelectionDialog(
    selectedFiles: List<String>,
    onFilesSelected: (List<String>) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelectedFiles by remember { mutableStateOf(selectedFiles) }
    
    // 가상의 파일 목록
    val availableFiles = remember {
        listOf(
            "생물학_교과서_1장.pdf",
            "세포분열_이미지.jpg",
            "DNA_구조_애니메이션.mp4",
            "실험_가이드라인.docx",
            "퀴즈_문제_모음.xlsx",
            "참고자료_링크.txt"
        )
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "파일 선택",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "과제에 첨부할 파일을 선택하세요:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 파일 목록
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableFiles) { fileName ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    tempSelectedFiles = if (fileName in tempSelectedFiles) {
                                        tempSelectedFiles - fileName
                                    } else {
                                        tempSelectedFiles + fileName
                                    }
                                }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = fileName in tempSelectedFiles,
                                onCheckedChange = { isChecked ->
                                    tempSelectedFiles = if (isChecked) {
                                        tempSelectedFiles + fileName
                                    } else {
                                        tempSelectedFiles - fileName
                                    }
                                }
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Icon(
                                imageVector = when (fileName.substringAfterLast('.')) {
                                    "pdf" -> Icons.Filled.PictureAsPdf
                                    "jpg", "png" -> Icons.Filled.Image
                                    "mp4" -> Icons.Filled.VideoFile
                                    "docx" -> Icons.Filled.Description
                                    "xlsx" -> Icons.Filled.TableChart
                                    else -> Icons.Filled.InsertDriveFile
                                },
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            Text(
                                text = fileName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                if (tempSelectedFiles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "선택된 파일: ${tempSelectedFiles.size}개",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryIndigo
                    )
                }
            }
        },
        confirmButton = {
            VTButton(
                text = "선택",
                onClick = { onFilesSelected(tempSelectedFiles) },
                variant = ButtonVariant.Primary,
                size = ButtonSize.Small
            )
        },
        dismissButton = {
            VTButton(
                text = "취소",
                onClick = onDismiss,
                variant = ButtonVariant.Outline,
                size = ButtonSize.Small
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun CreateAssignmentScreenPreview() {
    VoiceTutorTheme {
        CreateAssignmentScreen()
    }
}
