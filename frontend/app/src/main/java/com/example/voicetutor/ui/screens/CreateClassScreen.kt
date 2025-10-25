package com.example.voicetutor.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.network.CreateClassRequest
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.viewmodel.ClassViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateClassScreen(
    onBackClick: () -> Unit = {},
    teacherId: String? = null,
    classViewModel: ClassViewModel = hiltViewModel()
) {
    var className by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf("1학년") }
    var selectedClass by remember { mutableStateOf("A반") }
    var showGradeDropdown by remember { mutableStateOf(false) }
    var showClassDropdown by remember { mutableStateOf(false) }
    
    val grades = listOf("1학년", "2학년", "3학년")
    val classOptions = listOf("A반", "B반", "C반", "D반")
    
    // ViewModel 상태 관찰
    val isLoading by classViewModel.isLoading.collectAsStateWithLifecycle()
    val error by classViewModel.error.collectAsStateWithLifecycle()
    val classes by classViewModel.classes.collectAsStateWithLifecycle()
    
    // 클래스 생성 성공 시 백으로 이동
    LaunchedEffect(classes.size) {
        if (classes.isNotEmpty()) {
            onBackClick()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .clickable { 
                showGradeDropdown = false
                showClassDropdown = false
            }
    ) {
        VTHeader(
            title = "새 클래스 만들기",
            onBackClick = onBackClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Class name input
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "클래스 이름",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                VTTextField(
                    value = className,
                    onValueChange = { className = it },
                    placeholder = "예: 고등학교 1학년 A반",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Grade and Class selection
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "학년 및 반",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Grade selection
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "학년",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box {
                            OutlinedTextField(
                                value = selectedGrade,
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = { 
                                    Icon(
                                        imageVector = Icons.Filled.ArrowDropDown,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showGradeDropdown = true }
                            )
                            
                            // Grade dropdown
                            if (showGradeDropdown) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Column {
                                        grades.forEach { grade ->
                                            Text(
                                                text = grade,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedGrade = grade
                                                        showGradeDropdown = false
                                                    }
                                                    .padding(12.dp),
                                                color = if (grade == selectedGrade) PrimaryIndigo else Gray800
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Class selection
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "반",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box {
                            OutlinedTextField(
                                value = selectedClass,
                                onValueChange = { },
                                readOnly = true,
                                trailingIcon = { 
                                    Icon(
                                        imageVector = Icons.Filled.ArrowDropDown,
                                        contentDescription = null
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showClassDropdown = true }
                            )
                            
                            // Class dropdown
                            if (showClassDropdown) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Column {
                                        classOptions.forEach { classOption ->
                                            Text(
                                                text = classOption,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedClass = classOption
                                                        showClassDropdown = false
                                                    }
                                                    .padding(12.dp),
                                                color = if (classOption == selectedClass) PrimaryIndigo else Gray800
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Subject input
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "과목",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                VTTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    placeholder = "예: 영어, 수학, 과학",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Description input
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "클래스 설명",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                VTTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = "클래스에 대한 간단한 설명을 입력하세요...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Create button
        VTButton(
            text = if (isLoading) "생성 중..." else "클래스 만들기",
            onClick = {
                val fullClassName = if (className.isNotBlank()) {
                    className
                } else {
                    "$selectedGrade $selectedClass"
                }
                
                // 현재 시간을 ISO 형식으로 변환
                val now = LocalDateTime.now()
                val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                val startDate = now.format(formatter)
                val endDate = now.plusMonths(6).format(formatter) // 6개월 후
                
                // teacherId 사용 (파라미터로 받은 값 사용)
                println("CreateClassScreen - teacherId: $teacherId")
                
                if (teacherId != null) {
                    try {
                        val teacherIdInt = teacherId.toInt()
                        
                        // 클래스 생성 요청
                        val createClassRequest = CreateClassRequest(
                            name = fullClassName,
                            description = description,
                            subject_name = subject,
                            teacher_id = teacherIdInt,
                            start_date = startDate,
                            end_date = endDate
                        )
                        
                        println("CreateClassScreen - createClassRequest: $createClassRequest")
                        println("CreateClassScreen - teacher_id: $teacherIdInt")
                        classViewModel.createClass(createClassRequest)
                    } catch (e: NumberFormatException) {
                        println("CreateClassScreen - ERROR: Invalid teacherId format: $teacherId")
                        // 에러 처리 - 사용자에게 알림
                    }
                } else {
                    println("CreateClassScreen - ERROR: teacherId is null!")
                }
            },
            fullWidth = true,
            enabled = !isLoading && (className.isNotBlank() || (selectedGrade.isNotBlank() && selectedClass.isNotBlank())),
            variant = ButtonVariant.Gradient
        )
        
        // 에러 메시지 표시
        error?.let { errorMessage ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = Error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
