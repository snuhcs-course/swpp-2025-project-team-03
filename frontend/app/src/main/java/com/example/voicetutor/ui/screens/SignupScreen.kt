package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.utils.TutorialPreferences
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import com.example.voicetutor.ui.viewmodel.SignupError
import com.example.voicetutor.ui.viewmodel.SignupField


@Composable
fun SignupScreen(
    authViewModel: AuthViewModel? = null,
    onSignupSuccess: () -> Unit = {},
    onLoginClick: () -> Unit = {}
) {
    val viewModelAuth = authViewModel ?: hiltViewModel()
    val isLoading by viewModelAuth.isLoading.collectAsStateWithLifecycle()
    val signupError by viewModelAuth.signupError.collectAsStateWithLifecycle()
    val currentUser by viewModelAuth.currentUser.collectAsStateWithLifecycle()
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val inputError = signupError as? SignupError.Input
    val nameErrorMessage = if (inputError?.field == SignupField.NAME) inputError.message else null
    val emailErrorMessage = if (inputError?.field == SignupField.EMAIL) inputError.message else null
    val passwordErrorMessage = if (inputError?.field == SignupField.PASSWORD) inputError.message else null
    val confirmPasswordErrorMessage = if (inputError?.field == SignupField.CONFIRM_PASSWORD) inputError.message else null
    val generalError = signupError as? SignupError.General

    // 회원가입 로직을 함수로 분리
    val performSignup = {
        viewModelAuth.clearSignupError()
        when {
            name.isBlank() -> {
                viewModelAuth.setSignupInputError(SignupField.NAME, "이름을 입력해주세요")
            }
            email.isBlank() -> {
                viewModelAuth.setSignupInputError(SignupField.EMAIL, "이메일을 입력해주세요")
            }
            password.isBlank() -> {
                viewModelAuth.setSignupInputError(SignupField.PASSWORD, "비밀번호를 입력해주세요")
            }
            confirmPassword.isBlank() -> {
                viewModelAuth.setSignupInputError(SignupField.CONFIRM_PASSWORD, "비밀번호 확인을 입력해주세요")
            }
            password != confirmPassword -> {
                viewModelAuth.setSignupInputError(SignupField.CONFIRM_PASSWORD, "비밀번호가 일치하지 않습니다")
            }
            password.length < 6 -> {
                viewModelAuth.setSignupInputError(SignupField.PASSWORD, "비밀번호는 최소 6자 이상이어야 합니다")
            }
            else -> {
                viewModelAuth.signup(name, email, password, selectedRole)
            }
        }
    }

    // Error handling은 signupError를 통해 처리됨
    
    // 회원가입 성공 시 새 사용자 플래그 설정
    val context = LocalContext.current
    val tutorialPrefs = remember { TutorialPreferences(context) }
    val isLoggedIn by viewModelAuth.isLoggedIn.collectAsStateWithLifecycle()
    
    LaunchedEffect(isLoggedIn, currentUser) {
        // 회원가입 화면에서 회원가입 성공 후 로그인 상태가 되고 사용자 정보가 설정된 경우
        // (회원가입 성공 시 자동으로 로그인되므로)
        if (isLoggedIn && currentUser != null) {
            // 새로 가입한 사용자로 표시
            tutorialPrefs.setNewUser()
            println("SignupScreen - New user flag set for: ${currentUser?.email}")
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
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
                        RoleCard(
                            title = "학생",
                            description = "과제를 받고\n학습합니다",
                            icon = Icons.Filled.School,
                            isSelected = selectedRole == UserRole.STUDENT,
                            onClick = { selectedRole = UserRole.STUDENT },
                            modifier = Modifier.weight(1f)
                        )
                        
                        RoleCard(
                            title = "선생님",
                            description = "과제를 생성하고\n관리합니다",
                            icon = Icons.Filled.Person,
                            isSelected = selectedRole == UserRole.TEACHER,
                            onClick = { selectedRole = UserRole.TEACHER },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                viewModelAuth.clearFieldError(SignupField.NAME)
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
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    focusManager.moveFocus(FocusDirection.Down)
                                }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            isError = nameErrorMessage != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                focusedLabelColor = PrimaryIndigo,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black
                            ),
                            supportingText = {
                                if (nameErrorMessage != null) {
                                    Text(
                                        text = nameErrorMessage,
                                        color = Error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        )
                        
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                viewModelAuth.clearFieldError(SignupField.EMAIL)
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
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    focusManager.moveFocus(FocusDirection.Down)
                                }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            isError = emailErrorMessage != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                focusedLabelColor = PrimaryIndigo,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black
                            ),
                            supportingText = {
                                if (emailErrorMessage != null) {
                                    Text(
                                        text = emailErrorMessage,
                                        color = Error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        )
                        
                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                viewModelAuth.clearFieldError(SignupField.PASSWORD)
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
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    focusManager.moveFocus(FocusDirection.Down)
                                }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            isError = passwordErrorMessage != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                focusedLabelColor = PrimaryIndigo,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black
                            ),
                            supportingText = {
                                if (passwordErrorMessage != null) {
                                    Text(
                                        text = passwordErrorMessage,
                                        color = Error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        )
                        
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                viewModelAuth.clearFieldError(SignupField.CONFIRM_PASSWORD)
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
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    performSignup()
                                }
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            isError = confirmPasswordErrorMessage != null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                focusedLabelColor = PrimaryIndigo,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black,
                                cursorColor = Color.Black
                            ),
                            supportingText = {
                                if (confirmPasswordErrorMessage != null) {
                                    Text(
                                        text = confirmPasswordErrorMessage,
                                        color = Error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        )
                    }
                    
                    if (generalError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        VTCard(
                            variant = CardVariant.Outlined,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = generalError.message,
                                    color = Error,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                when (generalError) {
                                    is SignupError.General.DuplicateEmail -> {
                                        VTButton(
                                            text = "로그인하기",
                                            onClick = onLoginClick,
                                            variant = ButtonVariant.Outline,
                                            size = ButtonSize.Medium,
                                            fullWidth = false,
                                            enabled = !isLoading
                                        )
                                    }
                                    is SignupError.General.Network,
                                    is SignupError.General.Server,
                                    is SignupError.General.Unknown -> {
                                        VTButton(
                                            text = "다시 시도",
                                            onClick = {
                                                focusManager.clearFocus()
                                                performSignup()
                                            },
                                            variant = ButtonVariant.Outline,
                                            size = ButtonSize.Medium,
                                            fullWidth = false,
                                            enabled = !isLoading
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    VTButton(
                        text = if (isLoading) "계정 생성 중..." else "계정 만들기",
                        onClick = {
                            focusManager.clearFocus()
                            performSignup()
                        },
                        variant = ButtonVariant.Gradient,
                        size = ButtonSize.Large,
                        fullWidth = true,
                        enabled = !isLoading
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    HorizontalDivider(color = Gray200)
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
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
            modifier = Modifier.fillMaxWidth(),
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
