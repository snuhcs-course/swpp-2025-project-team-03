package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.navigation.VoiceTutorScreens
import com.example.voicetutor.ui.theme.*

@Composable
fun SettingsScreen(
    userRole: UserRole = UserRole.STUDENT,
    studentId: Int? = null,
    navController: androidx.navigation.NavHostController? = null
) {
    // Use graph-scoped ViewModels if navController is available
    val authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = if (navController != null) {
        hiltViewModel(navController.getBackStackEntry(navController.graph.id))
    } else {
        hiltViewModel()
    }
    val studentViewModel: com.example.voicetutor.ui.viewmodel.StudentViewModel = hiltViewModel()
    val currentUserState = authViewModel.currentUser.collectAsStateWithLifecycle()
    val isLoadingState = authViewModel.isLoading.collectAsStateWithLifecycle()
    val errorState = authViewModel.error.collectAsStateWithLifecycle()
    val accountDeletedState = authViewModel.accountDeleted.collectAsStateWithLifecycle()
    val currentUser = currentUserState.value
    
    // 학생 정보를 로드하는 경우
    val studentState = studentViewModel.currentStudent.collectAsStateWithLifecycle()
    val studentLoadingState = studentViewModel.isLoading.collectAsStateWithLifecycle()
    val studentErrorState = studentViewModel.error.collectAsStateWithLifecycle()
    
    // studentId가 있으면 해당 학생 정보 로드
    LaunchedEffect(studentId) {
        studentId?.let { id ->
            println("SettingsScreen - Loading student info for ID: $id")
            studentViewModel.loadStudentById(id)
        }
    }
    
    val displayedStudent = studentState.value
    val isLoading = if (studentId != null) studentLoadingState.value else isLoadingState.value
    val error = if (studentId != null) studentErrorState.value else errorState.value
    val accountDeleted = accountDeletedState.value
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(accountDeleted) {
        if (accountDeleted) {
            navController?.let { controller ->
                controller.navigate(VoiceTutorScreens.Login.route) {
                    popUpTo(controller.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
            authViewModel.clearAccountDeletedFlag()
        }
    }
    
    // 표시할 사용자 정보 결정 (학생 정보가 있으면 학생 정보, 없으면 현재 사용자)
    val displayUser = if (studentId != null && displayedStudent != null) {
        // Student 모델을 User 모델로 변환
        com.example.voicetutor.data.models.User(
            id = displayedStudent.id,
            email = displayedStudent.email,
            name = displayedStudent.name ?: "이름 없음",
            role = displayedStudent.role
        )
    } else {
        currentUser
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "계정",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Gray800
        )
        
        VTCard(variant = CardVariant.Elevated) {
            Column {
                Text(
                    text = "프로필",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isLoading) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Gray200),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = PrimaryIndigo,
                                strokeWidth = 2.dp
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = "사용자 정보 로딩 중...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray600
                            )
                            Text(
                                text = "잠시만 기다려주세요",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray500
                            )
                        }
                    }
                } else if (error != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Error.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = null,
                                tint = Error,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = "사용자 정보 로드 실패",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Error
                            )
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray600
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            VTButton(
                                text = "다시 시도",
                                onClick = { 
                                    if (studentId != null) {
                                        studentViewModel.clearError()
                                        studentViewModel.loadStudentById(studentId)
                                    } else {
                                        authViewModel.clearError()
                                    }
                                },
                                variant = ButtonVariant.Outline,
                                size = ButtonSize.Small
                            )
                        }
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(PrimaryIndigo.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = displayUser?.initial ?: "?",
                                style = MaterialTheme.typography.titleLarge,
                                color = PrimaryIndigo,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Text(
                                text = displayUser?.name ?: "사용자",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray800
                            )
                            Text(
                                text = displayUser?.email ?: "이메일 없음",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray600
                            )
                            Text(
                                text = when (displayUser?.role) {
                                    com.example.voicetutor.data.models.UserRole.TEACHER -> "선생님"
                                    com.example.voicetutor.data.models.UserRole.STUDENT -> "학생"
                                    null -> "역할 없음"
                                    else -> "역할 없음"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryIndigo,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
        
        VTCard(variant = CardVariant.Default) {
            Column {                
                SettingsItem(
                    icon = Icons.AutoMirrored.Filled.Help,
                    title = "도움말",
                    subtitle = "사용법 및 자주 묻는 질문",
                    onClick = { 
//                        navController?.navigate("help")
                        println("도움말 화면으로 이동")
                    }
                )
                
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = Gray200
                )
                
                SettingsItem(
                    icon = Icons.Filled.Info,
                    title = "앱 정보",
                    subtitle = "버전 1.0.0",
                    onClick = { 
                        navController?.navigate("app_info")
                        println("앱 정보 화면으로 이동")
                    }
                )
            }
        }
        
        if (error != null && studentId == null && !isLoading) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = Error,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        if (studentId == null) {
            VTButton(
                text = "계정 삭제",
                onClick = { showDeleteAccountDialog = true },
                variant = ButtonVariant.Danger,
                fullWidth = true,
                enabled = !isLoading,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.DeleteForever,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
        }
    }
    
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            tonalElevation = 0.dp,
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PrimaryIndigo, PrimaryPurple)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteForever,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            },
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "계정 삭제",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryIndigo
                    )
                }
            },
            text = {
                Text(
                    text = "계정을 삭제하시겠습니까?\n삭제 후에는 복구할 수 없습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    VTButton(
                        text = "취소",
                        onClick = { showDeleteAccountDialog = false },
                        modifier = Modifier.weight(1f),
                        variant = ButtonVariant.Outline,
                        size = ButtonSize.Medium
                    )
                    VTButton(
                        text = "계정 삭제",
                        onClick = {
                            showDeleteAccountDialog = false
                            authViewModel.deleteAccount()
                        },
                        modifier = Modifier.weight(1f),
                        variant = ButtonVariant.Danger,
                        size = ButtonSize.Medium,
                        enabled = !isLoading
                    )
                }
            }
        )
    }
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Gray600,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = Gray800
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Gray600
            )
        }
        
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = Gray400,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    VoiceTutorTheme {
        SettingsScreen()
    }
}
