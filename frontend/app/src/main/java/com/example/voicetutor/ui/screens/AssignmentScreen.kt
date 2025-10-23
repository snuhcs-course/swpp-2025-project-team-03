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
    
    // 모든 과제는 음성 답변 + AI 대화형 꼬리 질문 형태로 진행
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = PrimaryIndigo
            )
        }
    } else {
        AssignmentContinuousScreen(assignmentId = assignmentId ?: 1, assignmentTitle = assignmentTitle ?: "과제")
    }
}

// Mock 데이터: 화학 기초 퀴즈
data class QuizQuestionData(
    val questionNumber: Int,
    val question: String,
    val hint: String,
    val modelAnswer: String
)

private val mockChemistryQuestions = listOf(
    QuizQuestionData(1, "원소주기율표에서 같은 족의 원소들의 공통점은?", "전자 배치를 생각해보세요", "최외각 전자 수가 같다"),
    QuizQuestionData(2, "물(H2O)의 분자량은? (H=1, O=16)", "원자량을 더하세요", "18"),
    QuizQuestionData(3, "산소의 원소 기호는?", "Oxygen", "O"),
    QuizQuestionData(4, "공유결합은 무엇을 공유하는가?", "원자들이 함께 사용하는 것", "전자"),
    QuizQuestionData(5, "이온화 에너지가 가장 큰 원소족은?", "가장 안정한 원소들", "18족 (비활성 기체)")
)

@Composable
fun AssignmentContinuousScreen(
    assignmentId: Int = 1,
    assignmentTitle: String
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    
    var timeLeft by remember { mutableStateOf(15 * 60) } // 15분
    var isRecording by remember { mutableStateOf(false) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var recordedAudio by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableStateOf(0) }
    
    val currentQuestion = mockChemistryQuestions.getOrNull(currentQuestionIndex)
    val scope = rememberCoroutineScope()
    
    // Timer countdown effect
    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }
    
    // Recording duration effect
    LaunchedEffect(isRecording) {
        if (isRecording) {
            recordingDuration = 0
            while (isRecording) {
                delay(1000)
                recordingDuration++
            }
        }
    }

    if (currentQuestion == null) {
        // 퀴즈 완료 화면
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            VTCard(
                variant = CardVariant.Elevated,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Success,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "과제 완료!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Gray800
                    )
                    Text(
                        text = "모든 문제를 완료했습니다.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Gray600,
                        textAlign = TextAlign.Center
                    )
                    VTButton(
                        text = "결과 확인",
                        onClick = { /* TODO: Navigate to results */ },
                        variant = ButtonVariant.Gradient
                    )
                }
            }
        }
    } else {
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
                        text = "화학 기초 퀴즈",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "문제 ${currentQuestionIndex + 1} / ${mockChemistryQuestions.size}",
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
                        color = if (timeLeft < 300) Error else Gray800
                    )
                }
                
                // Progress
                VTProgressBar(
                    progress = (currentQuestionIndex + 1).toFloat() / mockChemistryQuestions.size.toFloat(),
                    showPercentage = true,
                    modifier = Modifier.weight(1f).padding(start = 16.dp)
                )
            }
            
            // Question card
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
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Question number
                    Text(
                        text = "질문 ${currentQuestion.questionNumber}",
                        style = MaterialTheme.typography.labelLarge,
                        color = PrimaryIndigo,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Question text
                    Text(
                        text = currentQuestion.question,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gray800
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Hint card
                    VTCard(variant = CardVariant.Outlined) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Lightbulb,
                                contentDescription = null,
                                tint = Warning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "힌트: ${currentQuestion.hint}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray700
                            )
                        }
                    }
                }
            }
            
            // Recording and send area
            VTCard(variant = CardVariant.Outlined) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Recording status
                    if (isRecording) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.RadioButtonChecked,
                                contentDescription = null,
                                tint = Error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "녹음 중... ${String.format("%02d:%02d", recordingDuration / 60, recordingDuration % 60)}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (recordedAudio) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = Success,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "녹음 완료 (${String.format("%02d:%02d", recordingDuration / 60, recordingDuration % 60)})",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Success,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Voice recorder button
                    VTButton(
                        text = if (isRecording) "녹음 중지" else "녹음 시작",
                        onClick = {
                            if (isRecording) {
                                // 녹음 중지
                                isRecording = false
                                recordedAudio = true
                                println("음성 녹음 중지")
                            } else {
                                // 녹음 시작
                                isRecording = true
                                recordedAudio = false
                                println("음성 녹음 시작")
                            }
                        },
                        variant = if (isRecording) ButtonVariant.Outline else ButtonVariant.Gradient,
                        fullWidth = true,
                        leadingIcon = {
                            Icon(
                                imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                                contentDescription = null
                            )
                        }
                    )
                    
                    // Send button
                    VTButton(
                        text = if (currentQuestionIndex < mockChemistryQuestions.size - 1) "전송 및 다음 문제" else "전송 및 완료",
                        onClick = {
                            if (recordedAudio) {
                                // Mock: 답변 전송 및 다음 문제로 이동
                                println("답변 전송: 문제 ${currentQuestionIndex + 1}")
                                
                                // 다음 문제로 이동
                                if (currentQuestionIndex < mockChemistryQuestions.size - 1) {
                                    currentQuestionIndex++
                                    recordedAudio = false
                                    recordingDuration = 0
                                } else {
                                    // 모든 문제 완료
                                    currentQuestionIndex++
                                }
                            }
                        },
                        variant = ButtonVariant.Gradient,
                        fullWidth = true,
                        enabled = recordedAudio && !isRecording
                    )
                }
            }
        }
    }
}

@Composable
fun AssignmentQuizScreen(
    assignmentId: Int = 1,
    assignmentTitle: String
) {
    // 모든 퀴즈는 음성 답변 + AI 대화형 꼬리 질문 형태로 진행
    AssignmentContinuousScreen(
        assignmentId = assignmentId,
        assignmentTitle = assignmentTitle
    )
}

// 이전 객관식 퀴즈 로직 (참고용으로 보관)
@Composable
private fun AssignmentQuizScreenOld(
    assignmentId: Int = 1,
    assignmentTitle: String
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val currentAssignment by viewModel.currentAssignment.collectAsStateWithLifecycle()
    val assignmentQuestions by viewModel.assignmentQuestions.collectAsStateWithLifecycle()
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
    
    // Load assignment and questions on first composition
    LaunchedEffect(assignmentId) {
        viewModel.loadAssignmentById(assignmentId)
        viewModel.loadAssignmentQuestions(assignmentId)
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            viewModel.clearError()
        }
    }
    
    // Convert API data to QuizQuestion format
    val questions = remember(assignmentQuestions, currentAssignment) {
        assignmentQuestions.mapIndexed { index, questionData ->
            // QuestionData의 correctAnswer (String)를 options에서의 index로 변환
            val correctAnswerIndex = questionData.options?.indexOf(questionData.correctAnswer) ?: 0
            
            QuizQuestion(
                id = questionData.id,
                question = questionData.question,
                options = questionData.options ?: emptyList(),
                correctAnswer = correctAnswerIndex,
                explanation = questionData.explanation ?: "",
                subject = currentAssignment?.subject?.name ?: "과목"
            )
        }
    }

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