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
import androidx.compose.ui.unit.dp
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*

@Composable
fun CreateClassScreen(
    onBackClick: () -> Unit = {},
    onCreateClass: (className: String, subject: String, description: String) -> Unit = { _, _, _ -> }
) {
    var className by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedGrade by remember { mutableStateOf("1학년") }
    var selectedClass by remember { mutableStateOf("A반") }
    
    val grades = listOf("1학년", "2학년", "3학년")
    val classes = listOf("A반", "B반", "C반", "D반")
    
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
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Class selection
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "반",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600
                        )
                        Spacer(modifier = Modifier.height(4.dp))
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
                            modifier = Modifier.fillMaxWidth()
                        )
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
            text = "클래스 만들기",
            onClick = {
                val fullClassName = if (className.isNotBlank()) {
                    className
                } else {
                    "$selectedGrade $selectedClass"
                }
                onCreateClass(fullClassName, subject, description)
                onBackClick()
            },
            fullWidth = true,
            enabled = className.isNotBlank() || (selectedGrade.isNotBlank() && selectedClass.isNotBlank()),
            variant = ButtonVariant.Gradient
        )
    }
}
