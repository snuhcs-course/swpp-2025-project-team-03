package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import com.example.voicetutor.ui.viewmodel.LoginError
import com.example.voicetutor.ui.viewmodel.LoginField

@Composable
private fun loginTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = PrimaryIndigo,
    focusedLabelColor = PrimaryIndigo,
    focusedTextColor = Color.Black,
    unfocusedTextColor = Color.Black,
    cursorColor = Color.Black,
)

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel? = null,
    assignmentViewModel: com.example.voicetutor.ui.viewmodel.AssignmentViewModel? = null,
    onLoginSuccess: () -> Unit = {},
    onSignupClick: () -> Unit = {},
) {
    val viewModelAuth = authViewModel ?: hiltViewModel()
    val viewModelAssignment = assignmentViewModel ?: hiltViewModel()
    val isLoading by viewModelAuth.isLoading.collectAsStateWithLifecycle()
    val loginError by viewModelAuth.loginError.collectAsStateWithLifecycle()
    val currentUser by viewModelAuth.currentUser.collectAsStateWithLifecycle()
    val autoFillCredentials by viewModelAuth.autoFillCredentials.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val inputError = loginError as? LoginError.Input
    val emailErrorMessage = if (inputError?.field == LoginField.EMAIL) inputError.message else null
    val passwordErrorMessage = if (inputError?.field == LoginField.PASSWORD) inputError.message else null
    val generalError = loginError as? LoginError.General

    val performLogin = {
        viewModelAuth.clearLoginError()
        when {
            email.isBlank() -> {
                viewModelAuth.setLoginInputError(LoginField.EMAIL, "이메일을 입력해주세요")
            }
            password.isBlank() -> {
                viewModelAuth.setLoginInputError(LoginField.PASSWORD, "비밀번호를 입력해주세요")
            }
            else -> {
                viewModelAuth.login(email, password)
            }
        }
    }

    LaunchedEffect(autoFillCredentials) {
        autoFillCredentials?.let { (autoEmail, autoPassword) ->
            email = autoEmail
            password = autoPassword
            viewModelAuth.clearAutoFillCredentials()
        }
    }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            currentUser?.assignments?.let { assignments ->
                if (assignments.isNotEmpty()) {
                    viewModelAssignment.setInitialAssignments(assignments)
                }
            }

            onLoginSuccess()
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
                        LightBlue,
                    ),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .size(288.dp)
                .offset(x = (-100).dp, y = 80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PrimaryIndigo.copy(alpha = 0.2f),
                            PrimaryPurple.copy(alpha = 0.2f),
                        ),
                    ),
                    shape = RoundedCornerShape(50),
                )
                .blur(60.dp),
        )

        Box(
            modifier = Modifier
                .size(384.dp)
                .offset(x = 100.dp, y = 400.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Info.copy(alpha = 0.2f),
                            PrimaryIndigo.copy(alpha = 0.2f),
                        ),
                    ),
                    shape = RoundedCornerShape(50),
                )
                .blur(60.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            VTCard(
                variant = CardVariant.Elevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(PrimaryIndigo, PrimaryPurple),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = "V",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "VoiceTutor",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo,
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "말하는 순간, 나만의 AI 튜터가 시작됩니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                viewModelAuth.clearLoginFieldError(LoginField.EMAIL)
                            },
                            label = { Text("이메일") },
                            placeholder = { Text("이메일을 입력하세요") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Email,
                                    contentDescription = null,
                                    tint = PrimaryIndigo,
                                )
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = {
                                    focusManager.moveFocus(FocusDirection.Down)
                                },
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            isError = emailErrorMessage != null,
                            colors = loginTextFieldColors(),
                            supportingText = {
                                if (emailErrorMessage != null) {
                                    Text(
                                        text = emailErrorMessage,
                                        color = Error,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            },
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                viewModelAuth.clearLoginFieldError(LoginField.PASSWORD)
                            },
                            label = { Text("비밀번호") },
                            placeholder = { Text("비밀번호를 입력하세요") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = PrimaryIndigo,
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = if (passwordVisible) "비밀번호 숨기기" else "비밀번호 보기",
                                        tint = Gray500,
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    performLogin()
                                },
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            isError = passwordErrorMessage != null,
                            colors = loginTextFieldColors(),
                            supportingText = {
                                if (passwordErrorMessage != null) {
                                    Text(
                                        text = passwordErrorMessage,
                                        color = Error,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            },
                        )
                    }

                    if (generalError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        VTCard(
                            variant = CardVariant.Outlined,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = generalError.message,
                                    color = Error,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    VTButton(
                        text = if (isLoading) "로그인 중..." else "로그인",
                        onClick = {
                            focusManager.clearFocus()
                            performLogin()
                        },
                        variant = ButtonVariant.Gradient,
                        size = ButtonSize.Large,
                        fullWidth = true,
                        enabled = !isLoading,
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    HorizontalDivider(color = Gray200)

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "계정이 없으신가요?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600,
                        fontWeight = FontWeight.Medium,
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    VTButton(
                        text = "계정 만들기",
                        onClick = onSignupClick,
                        variant = ButtonVariant.Outline,
                        size = ButtonSize.Large,
                        fullWidth = true,
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    VoiceTutorTheme {
        LoginScreen()
    }
}
