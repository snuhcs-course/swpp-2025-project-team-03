package com.example.voicetutor.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.ui.viewmodel.AIViewModel
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AssignmentScreen(
    assignmentId: Int? = null, // 실제 과제 ID 사용
    assignmentTitle: String? = null // 실제 과제 제목 사용
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val aiViewModel: AIViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    
    val currentAssignment by viewModel.currentAssignment.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val aiResponse by aiViewModel.aiResponse.collectAsStateWithLifecycle()
    val voiceRecognitionResult by aiViewModel.voiceRecognitionResult.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    
    val scope = rememberCoroutineScope()
    
    // 동적 과제 제목 가져오기
    val dynamicAssignmentTitle = currentAssignment?.title ?: assignmentTitle ?: "과제"
    
    // Load assignment on first composition
    LaunchedEffect(assignmentId) {
        assignmentId?.let { id ->
            viewModel.loadAssignmentById(id)
        }
    }
    
    // 과제 유형 결정: 과제 제목에 따라 자동으로 결정
    // "연속형", "대화형", "토론" 등이 포함된 과제는 연속형
    // 과제 타입을 동적으로 판단 (실제 구현에서는 assignment 데이터에서 가져오기)
    val isContinuousType = dynamicAssignmentTitle.contains("연속형") || 
                          dynamicAssignmentTitle.contains("대화형") || 
                          dynamicAssignmentTitle.contains("토론") ||
                          dynamicAssignmentTitle.contains("녹음") ||
                          dynamicAssignmentTitle.contains("발음")
    
    val isQuizType = dynamicAssignmentTitle.contains("퀴즈") || 
                    dynamicAssignmentTitle.contains("객관식") || 
                    dynamicAssignmentTitle.contains("선택형") ||
                    dynamicAssignmentTitle.contains("문제") ||
                    dynamicAssignmentTitle.contains("테스트")
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = PrimaryIndigo
            )
        }
    } else if (isContinuousType) {
        AssignmentContinuousScreen(assignmentId = assignmentId ?: 1, assignmentTitle = assignmentTitle ?: "과제")
    } else if (isQuizType) {
        AssignmentQuizScreen(assignmentId = assignmentId ?: 1, assignmentTitle = assignmentTitle ?: "과제")
    } else {
        // 기본값으로 연속형 화면 표시
        AssignmentContinuousScreen(assignmentId = assignmentId ?: 1, assignmentTitle = assignmentTitle ?: "과제")
    }
}

@Composable
fun AssignmentContinuousScreen(
    assignmentId: Int = 1,
    assignmentTitle: String
) {
    val aiViewModel: AIViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    
    val aiResponse by aiViewModel.aiResponse.collectAsStateWithLifecycle()
    val voiceRecognitionResult by aiViewModel.voiceRecognitionResult.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    
    var timeLeft by remember { mutableStateOf(15 * 60) } // 15분
    var isListening by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var conversation by remember { mutableStateOf(
        listOf(
            ConversationMessage(
                speaker = "ai",
                text = "안녕하세요! 오늘은 ${assignmentTitle.split(" - ").getOrElse(1) { assignmentTitle }}에 대해 이야기해보겠습니다. 먼저 간단히 설명해주시겠어요?"
            )
        )
    ) }
    var currentInput by remember { mutableStateOf("") }
    var remainingQuestions by remember { mutableStateOf(3) }
    
    // AI 응답 처리
    LaunchedEffect(aiResponse) {
        aiResponse?.let { response ->
            conversation = conversation + ConversationMessage(
                speaker = "ai",
                text = response.response
            )
            if (response.isComplete) {
                remainingQuestions = 0
            } else {
                remainingQuestions = maxOf(0, remainingQuestions - 1)
            }
            aiViewModel.clearAIResponse()
        }
    }
    
    // 음성 인식 결과 처리
    LaunchedEffect(voiceRecognitionResult) {
        voiceRecognitionResult?.let { result ->
            currentInput = result.text
            aiViewModel.clearVoiceRecognitionResult()
        }
    }

    // Timer countdown effect
    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = PrimaryIndigo,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                )
                .shadow(
                    elevation = 8.dp,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    ambientColor = PrimaryIndigo.copy(alpha = 0.3f),
                    spotColor = PrimaryIndigo.copy(alpha = 0.3f)
                )
                .padding(24.dp)
        ) {
            Column {
                        Text(
                            text = assignmentTitle,
                    style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                    color = Color.White
                        )
                        Text(
                    text = "AI와 함께 대화하며 학습해보세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
        
        // Timer and progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
            // Timer
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Timer,
                    contentDescription = null,
                    tint = PrimaryIndigo,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                    Text(
                    text = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (timeLeft < 300) Error else Gray800 // 5분 이하일 때 빨간색
                )
            }
            
            // Progress
            Text(
                text = "남은 질문: ${remainingQuestions}개",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )
        }
        
        // Conversation area
        VTCard(
            variant = CardVariant.Elevated,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                conversation.forEach { message ->
                    ConversationBubble(message = message)
                }
            }
        }
        
        // Input area
        VTCard(variant = CardVariant.Outlined) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Text input
                OutlinedTextField(
                    value = currentInput,
                    onValueChange = { currentInput = it },
                    label = { Text("답변을 입력하세요") },
                    placeholder = { Text("여기에 답변을 작성해주세요...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                // Voice input using VoiceRecorder component
                VoiceRecorder(
                    isRecording = isListening,
                    onStartRecording = {
                        isListening = true
                        isRecording = true
                        println("음성 녹음 시작")
                    },
                    onStopRecording = {
                        isListening = false
                        isRecording = false
                        // TODO: 실제 녹음 파일을 Base64로 인코딩하여 전송
                        // 실제 음성 인식 API 호출
                        val audioData = "" // 실제 녹음된 오디오 데이터로 교체 필요
                        aiViewModel.recognizeVoice(audioData)
                        println("음성 녹음 중지 및 인식 시작")
                    },
                    recordingDuration = "00:00" // 실제 녹음 시간으로 업데이트 필요
                )
                
                // Send button
                VTButton(
                    text = "전송",
                    onClick = {
                        if (currentInput.isNotBlank()) {
                            // 사용자 메시지 추가
                            conversation = conversation + ConversationMessage(
                                speaker = "user",
                                text = currentInput
                            )
                            
                            // AI에게 메시지 전송
                            val studentId = currentUser?.id ?: 1
                            aiViewModel.sendMessageToAI(
                                assignmentId = assignmentId.toString(),
                                studentId = studentId,
                                message = currentInput,
                                conversationHistory = conversation
                            )
                            
                            currentInput = ""
                        }
                    },
                    variant = ButtonVariant.Gradient,
                    fullWidth = true
                )
            }
        }
    }
}

@Composable
fun AssignmentQuizScreen(
    assignmentId: Int = 1, // 임시로 기본값 설정
    assignmentTitle: String
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val currentAssignment by viewModel.currentAssignment.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    var timeLeft by remember { mutableStateOf(10 * 60) } // 10분
    var currentQuestion by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var showExplanationPrompt by remember { mutableStateOf(false) }
    var explanation by remember { mutableStateOf("") }
    var hasExplained by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    
    // Load assignment on first composition
    LaunchedEffect(assignmentId) {
        viewModel.loadAssignmentById(assignmentId)
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            viewModel.clearError()
        }
    }
    
    // Convert API data to QuizQuestion format
    val questions = currentAssignment?.let { assignment ->
        // 임시로 기본 퀴즈 문제 생성 (실제로는 API에서 퀴즈 문제를 가져와야 함)
        listOf(
        QuizQuestion(
            id = 1,
            question = "다음 중 원소주기율표에서 같은 족(group)에 속하는 원소들의 공통점은?",
            options = listOf(
                "원자 반지름이 같다",
                "최외각 전자 수가 같다", 
                "원자량이 비슷하다",
                "이온화 에너지가 같다"
            ),
            correctAnswer = 1,
            explanation = "같은 족의 원소들은 최외각 전자 수가 같아서 비슷한 화학적 성질을 가집니다.",
            subject = "화학"
        ),
        QuizQuestion(
            id = 2,
            question = "주기율표에서 왼쪽에서 오른쪽으로 갈수록 변화하는 성질은?",
            options = listOf(
                "원자 반지름이 증가한다",
                "이온화 에너지가 증가한다",
                "전기음성도가 감소한다", 
                "금속성이 증가한다"
            ),
            correctAnswer = 1,
            explanation = "주기에서 왼쪽에서 오른쪽으로 갈수록 이온화 에너지와 전기음성도가 증가합니다.",
            subject = "화학"
        )
    )
    } ?: emptyList()

    // Timer countdown effect
    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = PrimaryIndigo
            )
        }
    } else if (questions.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.Quiz,
                    contentDescription = null,
                    tint = Gray400,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "퀴즈 문제가 없습니다",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Gray600
                )
            }
        }
    } else {
        val currentQuizQuestion = questions[currentQuestion]

    Column(
        modifier = Modifier
            .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            .background(
                        color = PrimaryIndigo,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                    )
                    .shadow(
                        elevation = 8.dp,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                        ambientColor = PrimaryIndigo.copy(alpha = 0.3f),
                        spotColor = PrimaryIndigo.copy(alpha = 0.3f)
                    )
                    .padding(24.dp)
                ) {
                    Column {
                        Text(
                            text = assignmentTitle,
                        style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        color = Color.White
                        )
                        Text(
                        text = "문제 ${currentQuestion + 1} / ${questions.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            
            // Timer and progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timer
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Timer,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (timeLeft < 300) Error else Gray800 // 5분 이하일 때 빨간색
                    )
                }
                
                // Progress bar
                VTProgressBar(
                    progress = (currentQuestion + 1).toFloat() / questions.size.toFloat(),
                    showPercentage = false,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Question card
            VTCard(
                variant = CardVariant.Elevated,
                modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                    // Question text
                        Text(
                        text = currentQuizQuestion.question,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    
                    // Options
                    currentQuizQuestion.options.forEachIndexed { index, option ->
                        OptionButton(
                            text = option,
                            isSelected = selectedAnswer == index,
                            isCorrect = isAnswered && index == currentQuizQuestion.correctAnswer,
                            isWrong = isAnswered && selectedAnswer == index && index != currentQuizQuestion.correctAnswer,
                        onClick = {
                            if (!isAnswered) {
                                selectedAnswer = index
                                isAnswered = true
                                showExplanationPrompt = true
                            }
                        }
                        )
                    }
                }
            }
            
            // Explanation prompt
            if (showExplanationPrompt && !hasExplained) {
                VTCard(variant = CardVariant.Outlined) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "정답에 대한 설명을 작성해주세요",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                        
                        OutlinedTextField(
                            value = explanation,
                            onValueChange = { explanation = it },
                            label = { Text("설명") },
                            placeholder = { Text("왜 이 답이 정답인지 설명해주세요...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            VTButton(
                                text = "설명 완료",
                                onClick = {
                                    hasExplained = true
                                    showExplanationPrompt = false
                                },
                                variant = ButtonVariant.Gradient,
                                modifier = Modifier.width(100.dp)
                            )
                        }
                    }
                }
            }
            
            // Navigation buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                    VTButton(
                    text = "이전",
                                        onClick = {
                        if (currentQuestion > 0) {
                            currentQuestion--
                                                    selectedAnswer = null
                                                    isAnswered = false
                                                    showExplanationPrompt = false
                                                    explanation = ""
                                                    hasExplained = false
                                                }
                                            },
                                            variant = ButtonVariant.Outline,
                    enabled = currentQuestion > 0
                                        )
                
                                    VTButton(
                    text = if (currentQuestion < questions.size - 1) "다음" else "완료",
                                        onClick = {
                                            if (currentQuestion < questions.size - 1) {
                                                currentQuestion++
                                                selectedAnswer = null
                                                isAnswered = false
                                                showExplanationPrompt = false
                                                explanation = ""
                                                hasExplained = false
                                            } else {
                            // TODO: Submit quiz and show results
                        }
                    },
                    variant = ButtonVariant.Gradient,
                    enabled = isAnswered && hasExplained
                )
            }
        }
    }
}

@Composable
fun ConversationBubble(
    message: ConversationMessage
) {
    val isUser = message.speaker == "user"
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isUser) PrimaryIndigo else Gray200,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) Color.White else Gray800
            )
        }
    }
}

@Composable
fun OptionButton(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCorrect -> Success.copy(alpha = 0.1f)
        isWrong -> Error.copy(alpha = 0.1f)
        isSelected -> PrimaryIndigo.copy(alpha = 0.1f)
        else -> Color.Transparent
    }
    
    val borderColor = when {
        isCorrect -> Success
        isWrong -> Error
        isSelected -> PrimaryIndigo
        else -> Gray300
    }
    
    val textColor = when {
        isCorrect -> Success
        isWrong -> Error
        isSelected -> PrimaryIndigo
        else -> Gray800
    }
    
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = textColor,
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = borderColor
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AssignmentScreenPreview() {
    VoiceTutorTheme {
        AssignmentScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun AssignmentQuizScreenPreview() {
    VoiceTutorTheme {
        AssignmentQuizScreen(assignmentTitle = "화학 - 원소주기율표 퀴즈")
    }
}