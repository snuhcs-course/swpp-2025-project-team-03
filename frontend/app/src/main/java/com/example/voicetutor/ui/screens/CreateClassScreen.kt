package com.example.voicetutor.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.data.network.CreateClassRequest
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.utils.ErrorMessageMapper
import com.example.voicetutor.ui.viewmodel.ClassViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClassScreen(
    onClassCreated: () -> Unit = {},
    teacherId: String? = null,
    classViewModel: ClassViewModel = hiltViewModel(),
) {
    var className by remember { mutableStateOf("") }
    var classNameError by remember { mutableStateOf<String?>(null) }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val isLoading by classViewModel.isLoading.collectAsStateWithLifecycle()
    val error by classViewModel.error.collectAsStateWithLifecycle()
    val classes by classViewModel.classes.collectAsStateWithLifecycle()

    var initialClassesSize by remember { mutableIntStateOf(classes.size) }
    var isCreating by remember { mutableStateOf(false) }

    LaunchedEffect(classes.size, isLoading) {
        if (isCreating && !isLoading && classes.size > initialClassesSize) {
            isCreating = false
            onClassCreated()
            initialClassesSize = classes.size
        }
    }

    LaunchedEffect(isLoading) {
        if (!isLoading && isCreating) {
            initialClassesSize = classes.size
        }
    }

    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            if (isCreating) {
                isCreating = false
            }
        }
    }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
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
                                value = className,
                                onValueChange = {
                                    val sanitized = it.replace("/", "")
                                    classNameError = if (sanitized.length != it.length) {
                                        "'/' 문자는 사용할 수 없어요."
                                    } else {
                                        null
                                    }
                                    className = sanitized
                                },
                                label = { Text("수업 이름") },
                                placeholder = { Text("예: 고등학교 1학년 A반") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Next,
                                ),
                                singleLine = true,
                                isError = classNameError != null,
                                supportingText = classNameError?.let { error ->
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

                            OutlinedTextField(
                                value = subject,
                                onValueChange = { subject = it },
                                label = { Text("과목") },
                                placeholder = { Text("예: 영어, 수학, 과학") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(
                                    capitalization = KeyboardCapitalization.Sentences,
                                    imeAction = ImeAction.Next,
                                ),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryIndigo,
                                    focusedLabelColor = PrimaryIndigo,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black,
                                    cursorColor = Color.Black,
                                ),
                            )

                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("수업 설명") },
                                placeholder = { Text("수업에 대한 간단한 설명을 입력하세요...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 80.dp),
                                minLines = 3,
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
                        }
                    }
                }

                CreateClassButton(
                    isLoading = isLoading,
                    className = className,
                    classNameError = classNameError,
                    subject = subject,
                    description = description,
                    teacherId = teacherId,
                    onClassCreate = { request ->
                        initialClassesSize = classes.size
                        isCreating = true
                        classViewModel.createClass(request)
                    },
                )

                error?.let { errorMessage ->
                    Text(
                        text = if (ErrorMessageMapper.isNetworkError(errorMessage)) {
                            "네트워크가 불안정합니다"
                        } else {
                            ErrorMessageMapper.getErrorMessage(errorMessage)
                        },
                        color = Error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateClassButton(
    isLoading: Boolean,
    className: String,
    classNameError: String?,
    subject: String,
    description: String,
    teacherId: String?,
    onClassCreate: (CreateClassRequest) -> Unit,
) {
    VTButton(
        text = "수업 생성",
        onClick = {
            if (teacherId == null) {
                return@VTButton
            }

            val teacherIdInt = teacherId.toIntOrNull()
            if (teacherIdInt == null) {
                return@VTButton
            }

            val createClassRequest = CreateClassRequest.builder()
                .name(className)
                .description(description)
                .subjectName(subject)
                .teacherId(teacherIdInt)
                .build()

            onClassCreate(createClassRequest)
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = !isLoading && className.isNotBlank() && subject.isNotBlank() && classNameError == null,
        variant = ButtonVariant.Gradient,
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
        },
    )
}
