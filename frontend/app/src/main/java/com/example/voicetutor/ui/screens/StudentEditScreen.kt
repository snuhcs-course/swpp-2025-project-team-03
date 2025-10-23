package com.example.voicetutor.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.viewmodel.StudentEditViewModel
import com.example.voicetutor.ui.viewmodel.StudentViewModel

@Composable
fun StudentEditScreen(
    studentId: Int = 1,
    onNavigateBack: () -> Unit = {}
) {
    val studentEditViewModel: StudentEditViewModel = hiltViewModel()
    val studentViewModel: StudentViewModel = hiltViewModel()
    
    val currentStudent by studentViewModel.currentStudent.collectAsStateWithLifecycle()
    val editResult by studentEditViewModel.editResult.collectAsStateWithLifecycle()
    val deleteResult by studentEditViewModel.deleteResult.collectAsStateWithLifecycle()
    // Status update and password reset are not supported by current backend API
    val isLoading by studentEditViewModel.isLoading.collectAsStateWithLifecycle()
    val error by studentEditViewModel.error.collectAsStateWithLifecycle()
    
    // Form states
    var name by remember { mutableStateOf(currentStudent?.name ?: "") }
    var email by remember { mutableStateOf(currentStudent?.email ?: "") }
    var phoneNumber by remember { mutableStateOf("") }
    var parentName by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }
    
    // Dialog states
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPasswordResetDialog by remember { mutableStateOf(false) }
    var showStatusChangeDialog by remember { mutableStateOf(false) }
    var showClassChangeDialog by remember { mutableStateOf(false) }
    
    // Load student data on first composition
    LaunchedEffect(studentId) {
        studentViewModel.loadStudentById(studentId)
    }
    
    // Update form when student data is loaded
    LaunchedEffect(currentStudent) {
        currentStudent?.let { student ->
            name = student.name
            email = student.email
            // isActive property not available in Student model
        }
    }
    
    // Handle results
    LaunchedEffect(editResult) {
        editResult?.let { result ->
            if (result.success) {
                onNavigateBack()
            }
            studentEditViewModel.clearEditResult()
        }
    }
    
    LaunchedEffect(deleteResult) {
        deleteResult?.let { result ->
            if (result.success) {
                onNavigateBack()
            }
            studentEditViewModel.clearDeleteResult()
        }
    }
    
    // Status update and password reset are not supported by current backend API
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            studentEditViewModel.clearError()
        }
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryIndigo)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            VTHeader(
                title = "학생 편집",
                onBackClick = onNavigateBack
            )
            
            // Basic Information
            VTCard(variant = CardVariant.Elevated) {
                Column {
                    Text(
                        text = "기본 정보",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    VTTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "이름",
                        placeholder = "학생 이름을 입력하세요"
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    VTTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = "이메일",
                        placeholder = "이메일을 입력하세요"
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    VTTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = "전화번호",
                        placeholder = "전화번호를 입력하세요"
                    )
                }
            }
            
            // Parent Information
            VTCard(variant = CardVariant.Elevated) {
                Column {
                    Text(
                        text = "보호자 정보",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    VTTextField(
                        value = parentName,
                        onValueChange = { parentName = it },
                        label = "보호자 이름",
                        placeholder = "보호자 이름을 입력하세요"
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    VTTextField(
                        value = parentPhone,
                        onValueChange = { parentPhone = it },
                        label = "보호자 전화번호",
                        placeholder = "보호자 전화번호를 입력하세요"
                    )
                }
            }
            
            // Additional Information
            VTCard(variant = CardVariant.Elevated) {
                Column {
                    Text(
                        text = "추가 정보",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    VTTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = "주소",
                        placeholder = "주소를 입력하세요"
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    VTTextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        label = "생년월일",
                        placeholder = "YYYY-MM-DD"
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    VTTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = "비고",
                        placeholder = "추가 정보를 입력하세요",
                        maxLines = 3
                    )
                }
            }
            
            // Status and Actions
            VTCard(variant = CardVariant.Elevated) {
                Column {
                    Text(
                        text = "상태 및 관리",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Active status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "활성 상태",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray700
                        )
                        
                        Switch(
                            checked = isActive,
                            onCheckedChange = { newValue ->
                                showStatusChangeDialog = true
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Success,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Gray300
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        VTButton(
                            text = "비밀번호 재설정",
                            onClick = { showPasswordResetDialog = true },
                            modifier = Modifier.weight(1f),
                            variant = ButtonVariant.Outlined
                        )
                        
                        VTButton(
                            text = "클래스 변경",
                            onClick = { showClassChangeDialog = true },
                            modifier = Modifier.weight(1f),
                            variant = ButtonVariant.Outlined
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    VTButton(
                        text = "학생 삭제",
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        variant = ButtonVariant.Danger
                    )
                }
            }
            
            // Save button
            VTButton(
                text = "저장",
                onClick = {
                    studentEditViewModel.editStudent(
                        studentId = studentId,
                        name = name,
                        email = email,
                        phoneNumber = phoneNumber.ifEmpty { null },
                        parentName = parentName.ifEmpty { null },
                        parentPhone = parentPhone.ifEmpty { null },
                        address = address.ifEmpty { null },
                        birthDate = birthDate.ifEmpty { null },
                        notes = notes.ifEmpty { null },
                        isActive = isActive
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
    
    // Delete confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("학생 삭제") },
            text = { Text("정말로 이 학생을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        studentEditViewModel.deleteStudent(studentId)
                        showDeleteDialog = false
                    }
                ) {
                    Text("삭제", color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
    
    // Password reset dialog
    if (showPasswordResetDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordResetDialog = false },
            title = { Text("비밀번호 재설정") },
            text = { Text("학생의 비밀번호를 재설정하시겠습니까? 임시 비밀번호가 생성됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Password reset is not supported by current backend API
                        showPasswordResetDialog = false
                    }
                ) {
                    Text("재설정")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordResetDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
    
    // Status change dialog
    if (showStatusChangeDialog) {
        AlertDialog(
            onDismissRequest = { showStatusChangeDialog = false },
            title = { Text("상태 변경") },
            text = { Text("학생의 활성 상태를 ${if (isActive) "비활성" else "활성"}으로 변경하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Status update is not supported by current backend API
                        showStatusChangeDialog = false
                    }
                ) {
                    Text("변경")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStatusChangeDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
    
    // Class change dialog
    if (showClassChangeDialog) {
        AlertDialog(
            onDismissRequest = { showClassChangeDialog = false },
            title = { Text("클래스 변경") },
            text = { Text("학생의 클래스를 변경하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        // TODO: Implement class selection
                        showClassChangeDialog = false
                    }
                ) {
                    Text("변경")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClassChangeDialog = false }) {
                    Text("취소")
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun StudentEditScreenPreview() {
    VoiceTutorTheme {
        StudentEditScreen()
    }
}
