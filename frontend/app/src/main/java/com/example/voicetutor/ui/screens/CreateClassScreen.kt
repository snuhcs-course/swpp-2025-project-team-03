package com.example.voicetutor.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
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
import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.network.CreateClassRequest
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.utils.ErrorMessageMapper
import com.example.voicetutor.ui.viewmodel.ClassViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClassScreen(
    onBackClick: () -> Unit = {},
    onClassCreated: () -> Unit = {},
    teacherId: String? = null,
    classViewModel: ClassViewModel = hiltViewModel(),
) {
    var className by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val isLoading by classViewModel.isLoading.collectAsStateWithLifecycle()
    val error by classViewModel.error.collectAsStateWithLifecycle()
    val classes by classViewModel.classes.collectAsStateWithLifecycle()
    
    var previousClassesSize by remember { mutableStateOf(classes.size) }
    var isCreating by remember { mutableStateOf(false) }
    
    LaunchedEffect(classes.size, isLoading, error) {
        if (isCreating) {
            if (!isLoading) {
                if (error == null && classes.size > previousClassesSize) {
                    isCreating = false
                    onClassCreated()
                } else if (error != null) {
                    isCreating = false
                }
                previousClassesSize = classes.size
            }
        } else if (!isLoading) {
            previousClassesSize = classes.size
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
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            OutlinedTextField(
                                value = className,
                                onValueChange = { className = it },
                                label = { Text("수업 이름") },
                                placeholder = { Text("예: 고등학교 1학년 A반") },
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
                        }
                    }
                }

                VTButton(
                    text = if (isLoading) "생성 중..." else "수업 생성",
                    onClick = {
                        println("CreateClassScreen - teacherId: $teacherId")

                        if (teacherId != null) {
                            try {
                                val teacherIdInt = teacherId.toInt()

                                val createClassRequest = CreateClassRequest.builder()
                                    .name(className)
                                    .description(description)
                                    .subjectName(subject)
                                    .teacherId(teacherIdInt)
                                    .build()

                                println("CreateClassScreen - createClassRequest: $createClassRequest")
                                println("CreateClassScreen - teacher_id: $teacherIdInt")
                                previousClassesSize = classes.size
                                isCreating = true
                                classViewModel.createClass(createClassRequest)
                            } catch (e: NumberFormatException) {
                                println("CreateClassScreen - ERROR: Invalid teacherId format: $teacherId")
                            }
                        } else {
                            println("CreateClassScreen - ERROR: teacherId is null!")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && className.isNotBlank(),
                    variant = ButtonVariant.Gradient,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                        )
                    },
                )

                error?.let { errorMessage ->
                    Text(
                        text = ErrorMessageMapper.getErrorMessage(errorMessage),
                        color = Error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
