package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.voicetutor.ui.viewmodel.MessageViewModel
import com.example.voicetutor.ui.viewmodel.AuthViewModel

@Composable
fun ClassMessageScreen(
    classId: Int = 1, // 임시로 기본값 설정
    className: String = "고등학교 1학년 A반",
    onNavigateToMessage: (String) -> Unit = {}
) {
    val studentViewModel: StudentViewModel = hiltViewModel()
    val messageViewModel: MessageViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    
    val apiStudents by studentViewModel.students.collectAsStateWithLifecycle()
    val sendMessageResult by messageViewModel.sendMessageResult.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val isLoading by studentViewModel.isLoading.collectAsStateWithLifecycle()
    val error by studentViewModel.error.collectAsStateWithLifecycle()
    
    // Load students on first composition
    LaunchedEffect(classId) {
        studentViewModel.loadAllStudents(classId = classId.toString())
    }
    
    // 메시지 전송 결과 처리
    LaunchedEffect(sendMessageResult) {
        sendMessageResult?.let { result ->
            // 메시지 전송 성공 처리
            println("Message sent successfully: ${result.sentCount} students, ${result.failedCount} failed")
            messageViewModel.clearSendMessageResult()
        }
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            studentViewModel.clearError()
        }
    }
    
    // Convert Student to ClassMessageStudent for UI
    val messageStudents = apiStudents.map { student ->
        ClassMessageStudent(
            id = student.id,
            studentId = student.id,
            teacherId = 1, // 임시 값
            content = "" // 임시 값
        )
    }
    
    var selectedStudents by remember { mutableStateOf(setOf<Int>()) }
    var showMessageDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = PrimaryIndigo,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = className,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "학생들을 선택하여 그룹 메시지를 보내거나 개별 메시지를 보내세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
        
        // Selection controls
        if (selectedStudents.isNotEmpty()) {
            VTCard(variant = CardVariant.Elevated) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "선택된 학생",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                        
                        Text(
                            text = "${selectedStudents.size}명 선택됨",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryIndigo,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VTButton(
                            text = "전체 선택",
                            onClick = { 
                                selectedStudents = messageStudents.map { it.id }.toSet()
                            },
                            variant = ButtonVariant.Outline,
                            size = ButtonSize.Small,
                            modifier = Modifier.weight(1f)
                        )
                        
                        VTButton(
                            text = "선택 해제",
                            onClick = { selectedStudents = emptySet() },
                            variant = ButtonVariant.Outline,
                            size = ButtonSize.Small,
                            modifier = Modifier.weight(1f)
                        )
                        
                        VTButton(
                            text = "메시지 보내기",
                            onClick = { showMessageDialog = true },
                            variant = ButtonVariant.Primary,
                            size = ButtonSize.Small,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                    
                    if (selectedStudents.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "선택된 학생들에게 그룹 메시지가 전송됩니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryIndigo,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
        
        // Students list
        Column {
            Text(
                text = "학생 목록",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray800
            )
            
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
            } else if (messageStudents.isEmpty()) {
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
                messageStudents.forEach { student ->
                    ClassMessageStudentCard(
                        student = student,
                        isSelected = selectedStudents.contains(student.id),
                        onStudentClick = { 
                            selectedStudents = if (selectedStudents.contains(student.id)) {
                                selectedStudents - student.id
                            } else {
                                selectedStudents + student.id
                            }
                        },
                        onMessageClick = { onNavigateToMessage("학생") }
                    )
                    
                    if (student != messageStudents.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        // Message dialog
        if (showMessageDialog) {
            MessageDialog(
                selectedStudents = selectedStudents,
                onSendMessage = { messageText ->
                    // API를 통한 메시지 전송
                    val teacherId = currentUser?.id ?: 1
                    val studentIds = selectedStudents.map { studentId ->
                        try {
                            studentId.toInt()
                        } catch (e: NumberFormatException) {
                            1
                        }
                    }
                    
                    messageViewModel.sendMessage(
                        teacherId = teacherId,
                        studentIds = studentIds,
                        message = messageText,
                        messageType = "TEXT"
                    )
                    
                    showMessageDialog = false
                    selectedStudents = emptySet()
                },
                onDismiss = {
                    showMessageDialog = false
                }
            )
        }
    }
}

@Composable
fun ClassMessageStudentCard(
    student: ClassMessageStudent,
    isSelected: Boolean,
    onStudentClick: () -> Unit,
    onMessageClick: () -> Unit
) {
    VTCard(
        variant = CardVariant.Outlined,
        onClick = onStudentClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onStudentClick() },
                colors = CheckboxDefaults.colors(
                    checkedColor = PrimaryIndigo,
                    uncheckedColor = Gray400
                )
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PrimaryIndigo.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "학",
                    color = PrimaryIndigo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "학생",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Text(
                    text = student.studentId.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
                Text(
                    text = "정보 없음",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
            
            IconButton(
                onClick = onMessageClick
            ) {
                Icon(
                    imageVector = Icons.Filled.Message,
                    contentDescription = "개별 메시지",
                    tint = PrimaryIndigo
                )
            }
        }
    }
}

@Composable
fun MessageDialog(
    selectedStudents: Set<Int>,
    onSendMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "그룹 메시지 보내기",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "${selectedStudents.size}명의 학생에게 메시지를 보냅니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    label = { Text("메시지 내용") },
                    placeholder = { Text("메시지를 입력하세요...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )
            }
        },
        confirmButton = {
            VTButton(
                text = "전송",
                onClick = { onSendMessage(messageText) },
                enabled = messageText.isNotBlank() && selectedStudents.isNotEmpty()
            )
        },
        dismissButton = {
            VTButton(
                text = "취소",
                onClick = onDismiss,
                variant = ButtonVariant.Outline
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ClassMessageScreenPreview() {
    VoiceTutorTheme {
        ClassMessageScreen()
    }
}