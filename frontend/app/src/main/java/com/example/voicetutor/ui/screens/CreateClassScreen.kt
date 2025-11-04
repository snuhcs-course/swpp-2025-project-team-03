package com.example.voicetutor.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateClassScreen(
    onBackClick: () -> Unit = {},
    teacherId: String? = null,
    classViewModel: ClassViewModel = hiltViewModel()
) {
    var className by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
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
                
                OutlinedTextField(
                    value = className,
                    onValueChange = { className = it },
                    placeholder = { Text("예: 고등학교 1학년 A반") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(color = Color.Black),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        focusedLabelColor = PrimaryIndigo,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black
                    )
                )
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
                
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    placeholder = { Text("예: 영어, 수학, 과학") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(color = Color.Black),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        focusedLabelColor = PrimaryIndigo,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black
                    )
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
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("클래스에 대한 간단한 설명을 입력하세요...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(color = Color.Black),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        focusedLabelColor = PrimaryIndigo,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        cursorColor = Color.Black
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Create button
        VTButton(
            text = if (isLoading) "생성 중..." else "클래스 만들기",
            onClick = {
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
                            name = className,
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
            enabled = !isLoading && className.isNotBlank(),
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
