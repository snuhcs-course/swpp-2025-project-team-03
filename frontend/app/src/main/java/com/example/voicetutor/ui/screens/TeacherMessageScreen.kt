package com.example.voicetutor.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.data.models.*
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromTeacher: Boolean,
    val timestamp: String
)

@Composable
fun TeacherMessageScreen(
    studentId: Int = 1, // 선택된 학생 ID
    onBackClick: () -> Unit = {}
) {
    var messageText by remember { mutableStateOf("") }
    
    val studentViewModel: com.example.voicetutor.ui.viewmodel.StudentViewModel = hiltViewModel()
    val currentStudent by studentViewModel.currentStudent.collectAsStateWithLifecycle()
    
    // 동적 학생 이름 가져오기
    val studentName = currentStudent?.name ?: "학생"
    
    // Load student data on first composition
    LaunchedEffect(studentId) {
        studentViewModel.loadStudentById(studentId)
    }
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                id = "1",
                text = "안녕하세요! 과제에 대해 궁금한 점이 있어서 연락드렸습니다.",
                isFromTeacher = false,
                timestamp = "오후 2:30"
            ),
            ChatMessage(
                id = "2",
                text = "안녕하세요! 어떤 부분이 궁금하신가요?",
                isFromTeacher = true,
                timestamp = "오후 2:32"
            ),
            ChatMessage(
                id = "3",
                text = "세포분열 과제에서 DNA 복제 과정이 잘 이해가 안 됩니다.",
                isFromTeacher = false,
                timestamp = "오후 2:35"
            )
        )
    }
    
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    // Auto scroll to bottom when new message is added
    LaunchedEffect(messages.size) {
        scope.launch {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Student info header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryIndigo)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Student avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = studentName.first().toString(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = studentName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = "온라인",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
        
        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                ChatBubble(
                    message = message,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        // Message input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("메시지를 입력하세요...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryIndigo,
                    unfocusedBorderColor = Gray300,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            FloatingActionButton(
                onClick = {
                    if (messageText.isNotBlank()) {
                        messages.add(
                            ChatMessage(
                                id = System.currentTimeMillis().toString(),
                                text = messageText,
                                isFromTeacher = true,
                                timestamp = "지금"
                            )
                        )
                        messageText = ""
                    }
                },
                modifier = Modifier.size(48.dp),
                containerColor = PrimaryIndigo
            ) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "전송",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = if (message.isFromTeacher) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (message.isFromTeacher) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .shadow(
                        elevation = 4.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = if (message.isFromTeacher) PrimaryIndigo.copy(alpha = 0.3f) else Gray300.copy(alpha = 0.3f),
                        spotColor = if (message.isFromTeacher) PrimaryIndigo.copy(alpha = 0.3f) else Gray300.copy(alpha = 0.3f)
                    )
                    .background(
                        color = if (message.isFromTeacher) {
                            PrimaryIndigo
                        } else {
                            Color.White.copy(alpha = 0.8f)
                        }
                    )
                    .padding(16.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (message.isFromTeacher) Color.White else Gray800,
                    lineHeight = 20.sp,
                    fontWeight = if (message.isFromTeacher) FontWeight.Medium else FontWeight.Normal
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherMessageScreenPreview() {
    VoiceTutorTheme {
        TeacherMessageScreen()
    }
}
