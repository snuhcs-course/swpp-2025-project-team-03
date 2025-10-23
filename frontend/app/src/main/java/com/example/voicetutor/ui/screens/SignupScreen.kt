package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.ui.viewmodel.AuthViewModel


@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
    val error by authViewModel.error.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    // Handle signup success - 회원가입 성공 시 로그인 화면으로 이동
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            // 회원가입 성공 시 로그인 화면으로 이동
            onSignupSuccess()
            // currentUser를 초기화하여 로그인 화면에서 자동 로그인되지 않도록 함
            authViewModel.logout()
        }
    }
    
    // Handle error
    LaunchedEffect(error) {
        error?.let {
            // Error is already handled by AuthViewModel
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        LightIndigo,
                        LightPurple,
                        LightBlue
                    )
                )
            )
    ) {
        // Background decorative elements
        Box(
            modifier = Modifier
                .size(288.dp)
                .offset(x = (-100).dp, y = 80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryIndigo.copy(alpha = 0.2f),
                            PrimaryPurple.copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(50)
                )
                .blur(60.dp)
        )

        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            VTCard(
                variant = CardVariant.Elevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Logo
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(PrimaryIndigo, PrimaryPurple)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "V",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Title
                    Text(
                        text = "계정 만들기",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "VoiceTutor와 함께 시작하세요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Role selection
                    Text(
                        text = "역할을 선택하세요",
                        style = MaterialTheme.typography.titleSmall,
                        color = Gray800,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Student role card
                        RoleCard(
                            title = "학생",
                            description = "과제를 받고 학습합니다",
                            icon = Icons.Filled.School,
                            isSelected = selectedRole == UserRole.STUDENT,
                            onClick = { selectedRole = UserRole.STUDENT },
                            modifier = Modifier.weight(1f)
                        )
                        
                        // Teacher role card
                        RoleCard(
                            title = "선생님",
                            description = "과제를 생성하고 관리합니다",
                            icon = Icons.Filled.Person,
                            isSelected = selectedRole == UserRole.TEACHER,
                            onClick = { selectedRole = UserRole.TEACHER },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Name field
                    OutlinedTextField(
                        value = name,
                        onValueChange = { 
                            name = it
                            authViewModel.clearError()
                        },
                        label = { Text("이름") },
                        placeholder = { Text("홍길동") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = PrimaryIndigo
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryIndigo,
                            focusedLabelColor = PrimaryIndigo
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Email field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { 
                            email = it
                            authViewModel.clearError()
                        },
                        label = { Text("이메일") },
                        placeholder = { Text("이메일을 입력하세요") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Email,
                                contentDescription = null,
                                tint = PrimaryIndigo
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryIndigo,
                            focusedLabelColor = PrimaryIndigo
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Password field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { 
                            password = it
                            authViewModel.clearError()
                        },
                        label = { Text("비밀번호") },
                        placeholder = { Text("••••••••") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = PrimaryIndigo
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) "비밀번호 숨기기" else "비밀번호 보기",
                                    tint = Gray500
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryIndigo,
                            focusedLabelColor = PrimaryIndigo
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Confirm Password field
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { 
                            confirmPassword = it
                            authViewModel.clearError()
                        },
                        label = { Text("비밀번호 확인") },
                        placeholder = { Text("••••••••") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = null,
                                tint = PrimaryIndigo
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (confirmPasswordVisible) "비밀번호 숨기기" else "비밀번호 보기",
                                    tint = Gray500
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryIndigo,
                            focusedLabelColor = PrimaryIndigo
                        )
                    )
                    
                    if (error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        VTCard(
                            variant = CardVariant.Outlined,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = error ?: "",
                                color = Error,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Signup button
                    VTButton(
                        text = if (isLoading) "계정 생성 중..." else "계정 만들기",
                        onClick = {
                            when {
                                name.isBlank() -> return@VTButton
                                email.isBlank() -> return@VTButton
                                password.isBlank() -> return@VTButton
                                confirmPassword.isBlank() -> return@VTButton
                                password != confirmPassword -> return@VTButton
                                password.length < 6 -> return@VTButton
                                else -> {
                                    // Call actual signup API
                                    authViewModel.signup(name, email, password, selectedRole)
                                }
                            }
                        },
                        variant = ButtonVariant.Gradient,
                        size = ButtonSize.Large,
                        fullWidth = true,
                        enabled = !isLoading
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Divider
                    HorizontalDivider(color = Gray200)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Login section
                    Text(
                        text = "이미 계정이 있으신가요?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    VTButton(
                        text = "로그인",
                        onClick = onLoginClick,
                        variant = ButtonVariant.Outline,
                        size = ButtonSize.Large,
                        fullWidth = true
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun RoleCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    VTCard(
        variant = if (isSelected) CardVariant.Selected else CardVariant.Default,
        onClick = onClick,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) PrimaryIndigo else Gray600,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) PrimaryIndigo else Gray800
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) PrimaryIndigo else Gray600,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    VoiceTutorTheme {
        SignupScreen()
    }
}
