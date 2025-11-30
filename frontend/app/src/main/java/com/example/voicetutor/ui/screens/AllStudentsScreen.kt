package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.utils.ErrorMessageMapper
import com.example.voicetutor.ui.viewmodel.ClassViewModel
import com.example.voicetutor.ui.viewmodel.StudentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllStudentsScreen(
    teacherId: String,
    onNavigateToStudentDetail: (Int, Int, String) -> Unit = { _, _, _ -> },
) {
    val studentViewModel: StudentViewModel = hiltViewModel()
    val classViewModel: ClassViewModel = hiltViewModel()

    val apiStudents by studentViewModel.students.collectAsStateWithLifecycle()
    val isLoading by studentViewModel.isLoading.collectAsStateWithLifecycle()
    val error by studentViewModel.error.collectAsStateWithLifecycle()

    val classes by classViewModel.classes.collectAsStateWithLifecycle()
    val classError by classViewModel.error.collectAsStateWithLifecycle()
    val classIsLoading by classViewModel.isLoading.collectAsStateWithLifecycle()

    var selectedClassId by rememberSaveable(
        stateSaver = Saver(
            save = { it ?: -1 },
            restore = { if (it == -1) null else it },
        ),
    ) { mutableStateOf<Int?>(null) }
    var expandedClassDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(teacherId) {
        classViewModel.loadClasses(teacherId)
    }

    LaunchedEffect(classes) {
        if (classes.isNotEmpty() && selectedClassId == null) {
            selectedClassId = classes.first().id
        }
    }

    LaunchedEffect(selectedClassId) {
        if (selectedClassId != null) {
            // 수업이 변경되면 이전 학생 목록을 먼저 클리어
            studentViewModel.clearStudents()
            studentViewModel.loadAllStudents(teacherId = teacherId, classId = selectedClassId.toString())
        }
    }

    // 네트워크 에러가 아닌 경우에만 에러를 클리어합니다.
    // 네트워크 에러는 allStudents.isEmpty()일 때 구분하기 위해 유지합니다.
    // isLoading이 false가 되고 selectedClassId가 설정된 후에만 에러를 클리어하여
    // 네트워크 에러를 감지할 수 있도록 합니다.
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage, isLoading, selectedClassId) {
            // 로딩이 완료되고, selectedClassId가 설정되어 있고, 네트워크 에러가 아닌 경우에만 클리어
            if (!isLoading && selectedClassId != null && !ErrorMessageMapper.isNetworkError(errorMessage)) {
                studentViewModel.clearError()
            }
        }
    }

    // classViewModel의 에러도 네트워크 에러가 아닌 경우에만 클리어합니다.
    classError?.let { errorMessage ->
        LaunchedEffect(errorMessage, classIsLoading) {
            // 로딩이 완료되고, 네트워크 에러가 아닌 경우에만 클리어
            if (!classIsLoading && !ErrorMessageMapper.isNetworkError(errorMessage)) {
                classViewModel.clearError()
            }
        }
    }

    val allStudents = apiStudents.map { student ->
        AllStudentsStudent(
            id = student.id,
            name = student.name ?: "이름 없음",
            email = student.email,
            role = student.role,
        )
    }

    val totalStudents = allStudents.size

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = PrimaryIndigo.copy(alpha = 0.08f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    )
                    .padding(20.dp),
            ) {
                Column {
                    Text(
                        text = "성취기준 리포트",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "학생들의 학습 현황을 확인하고 취약 유형을 분석하세요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600,
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(PrimaryIndigo.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.People,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    Text(
                        text = "전체 학생",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Gray800,
                        fontWeight = FontWeight.Medium,
                    )
                }
                Text(
                    text = "${totalStudents}명",
                    style = MaterialTheme.typography.titleLarge,
                    color = Gray800,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        item {
            ExposedDropdownMenuBox(
                expanded = expandedClassDropdown,
                onExpandedChange = { expandedClassDropdown = it },
            ) {
                OutlinedTextField(
                    value = classes.find { it.id == selectedClassId }?.name ?: "수업 선택",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("수업 선택") },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = Gray800),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedClassDropdown)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                        focusedTextColor = Gray800,
                        unfocusedTextColor = Gray800,
                        focusedLabelColor = PrimaryIndigo,
                        unfocusedLabelColor = Gray600,
                    ),
                )

                ExposedDropdownMenu(
                    expanded = expandedClassDropdown,
                    onDismissRequest = { expandedClassDropdown = false },
                ) {
                    classes.forEach { classData ->
                        DropdownMenuItem(
                            text = { Text(classData.name) },
                            onClick = {
                                selectedClassId = classData.id
                                expandedClassDropdown = false
                            },
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "학생 목록",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800,
                )

                Text(
                    text = "${allStudents.size}명",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        // classViewModel 또는 studentViewModel이 로딩 중이면 로딩 인디케이터 표시
        val isAnyLoading = classIsLoading || isLoading
        val shouldShowEmptyState = !isAnyLoading && (allStudents.isEmpty() || (selectedClassId == null && classes.isEmpty()))

        if (isAnyLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = PrimaryIndigo,
                    )
                }
            }
        } else if (shouldShowEmptyState) {
            item {
                // 빈 상태일 때 네트워크 에러인지 확인
                // 1. studentViewModel에서 로딩을 시도했고 네트워크 에러가 발생한 경우
                // 2. classViewModel에서 로딩을 시도했고 네트워크 에러가 발생하여 classes가 비어있는 경우
                val hasAttemptedStudentLoad = selectedClassId != null && !isLoading
                val isStudentNetworkError = hasAttemptedStudentLoad && error != null && ErrorMessageMapper.isNetworkError(error)

                val hasAttemptedClassLoad = !classIsLoading
                val isClassNetworkError = hasAttemptedClassLoad && classes.isEmpty() && classError != null && ErrorMessageMapper.isNetworkError(classError)

                val isNetworkErrorState = isStudentNetworkError || isClassNetworkError
                val emptyStateMessage = if (isNetworkErrorState) {
                    "네트워크가 불안정합니다"
                } else {
                    "학생이 없습니다"
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = Gray400,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = emptyStateMessage,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600,
                        )
                    }
                }
            }
        } else {
            itemsIndexed(
                items = allStudents,
                key = { _, student -> student.id },
            ) { _, student ->

                AllStudentsCard(
                    student = student,
                    onReportClick = {
                        val classId = selectedClassId ?: 0
                        onNavigateToStudentDetail(classId, student.id, student.name)
                    },
                )
            }
        }
    }
}

@Composable
fun AllStudentsCard(
    student: AllStudentsStudent,
    onReportClick: () -> Unit,
) {
    VTCard2(
        modifier = Modifier.fillMaxWidth(),
        variant = CardVariant.Elevated,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(PrimaryIndigo.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = student.name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                        color = PrimaryIndigo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = student.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                    )
                    Text(
                        text = student.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                VTButton(
                    text = "리포트 보기",
                    onClick = onReportClick,
                    variant = ButtonVariant.Primary,
                    size = ButtonSize.Medium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AllStudentsScreenPreview() {
    VoiceTutorTheme {
        AllStudentsScreen("1")
    }
}
