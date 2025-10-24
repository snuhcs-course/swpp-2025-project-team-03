package com.example.voicetutor.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String,
    val subject: String = "과목",
    var selectedAnswer: Int? = null,
    var isAnswered: Boolean = false,
    var showExplanation: Boolean = false,
    var hasExplained: Boolean = false,
    var isListening: Boolean = false
)

@Composable
fun QuizScreen(
    assignmentId: Int = 1,
    quizTitle: String = "퀴즈"
) {
    // 모든 퀴즈는 음성 답변 + AI 대화형 꼬리 질문 형태로 진행
    AssignmentContinuousScreen(
        assignmentId = assignmentId,
        assignmentTitle = quizTitle
    )
}

// 이전 객관식 퀴즈 로직 (참고용으로 보관)
@Composable
private fun QuizScreenOld(
    assignmentId: Int = 1,
    quizTitle: String = "퀴즈"
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val aiViewModel: AIViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    
    val currentAssignment by viewModel.currentAssignment.collectAsStateWithLifecycle()
    val assignmentQuestions by viewModel.assignmentQuestions.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val quizSubmissionResult by aiViewModel.quizSubmissionResult.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    
    // 동적 퀴즈 제목 가져오기
    val dynamicQuizTitle = currentAssignment?.title ?: quizTitle
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    // 퀴즈 완료 상태
    var isQuizCompleted by remember { mutableStateOf(false) }
    var quizScore by remember { mutableStateOf(0) }
    var totalQuestions by remember { mutableStateOf(0) }
    var quizStartTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // 퀴즈 제출 결과 처리
    LaunchedEffect(quizSubmissionResult) {
        quizSubmissionResult?.let { result ->
            isQuizCompleted = true
            quizScore = result.correctAnswers
            totalQuestions = result.totalQuestions
            aiViewModel.clearQuizSubmissionResult()
        }
    }
    
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
                subject = currentAssignment?.courseClass?.subject?.name ?: "과목"
            )
        }
    }
    
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var isAnswered by remember { mutableStateOf(false) }
    var showExplanation by remember { mutableStateOf(false) }
    var hasExplained by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(10 * 60) } // 10분
    var score by remember { mutableStateOf(0) }
    
    // Show loading state
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = PrimaryIndigo
            )
        }
        return
    }
    
    // Show empty state
    if (questions.isEmpty()) {
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
        return
    }
    
    val currentQuestion = questions[currentQuestionIndex]
    
    // Timer effect
    LaunchedEffect(timeLeft) {
        if (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        Gray50,
                        LightIndigo.copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        // Header with timer and progress
        VTCard(
            variant = CardVariant.Elevated,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = dynamicQuizTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Gray800
                        )
                        Text(
                            text = "고등학교 1학년 A반",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                    
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${timeLeft / 60}:${String.format("%02d", timeLeft % 60)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (timeLeft < 120) Error else PrimaryIndigo
                        )
                        Text(
                            text = "남은 시간",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "문제 ${currentQuestionIndex + 1} / ${questions.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "점수: ${score}점",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryIndigo,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                VTProgressBar(
                    progress = (currentQuestionIndex + 1) / questions.size.toFloat(),
                    showPercentage = false,
                    color = PrimaryIndigo,
                    height = 8
                )
            }
        }
        
        // Question content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Question card
            VTCard(
                variant = CardVariant.Elevated,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = currentQuestion.question,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                        lineHeight = 24.sp
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Answer options
                    currentQuestion.options.forEachIndexed { index, option ->
                        AnswerOption(
                            text = option,
                            isSelected = selectedAnswer == index,
                            isCorrect = if (isAnswered) index == currentQuestion.correctAnswer else null,
                            isWrong = if (isAnswered && selectedAnswer == index) index != currentQuestion.correctAnswer else false,
                            onClick = {
                                if (!isAnswered) {
                                    selectedAnswer = index
                                    isAnswered = true
                                    showExplanation = true
                                    if (index == currentQuestion.correctAnswer) {
                                        score += 25
                                    }
                                }
                            }
                        )
                        
                        if (index < currentQuestion.options.size - 1) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Explanation section
            if (showExplanation) {
                VTCard(
                    variant = CardVariant.Default,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (selectedAnswer == currentQuestion.correctAnswer) 
                                    Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                contentDescription = null,
                                tint = if (selectedAnswer == currentQuestion.correctAnswer) Success else Error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedAnswer == currentQuestion.correctAnswer) "정답입니다!" else "틀렸습니다",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedAnswer == currentQuestion.correctAnswer) Success else Error
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = currentQuestion.explanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray700,
                            lineHeight = 20.sp
                        )
                        
                        if (!hasExplained) {
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lightbulb,
                                    contentDescription = null,
                                    tint = Warning,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "답을 선택한 이유를 설명해보세요",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray600,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Voice recording for explanation
                            VoiceRecorder(
                                isRecording = isListening,
                                onStartRecording = {
                                    isListening = true
                                    println("퀴즈 설명 녹음 시작")
                                },
                                onStopRecording = {
                                    isListening = false
                                    hasExplained = true
                                    println("퀴즈 설명 녹음 중지")
                                },
                                recordingDuration = "00:00"
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Listening indicator is now handled by VoiceRecorder component
        }
        
        // Bottom actions
        VTCard(
            variant = CardVariant.Elevated,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (currentQuestionIndex > 0) {
                    VTButton(
                        text = "이전",
                        onClick = {
                            if (currentQuestionIndex > 0) {
                                currentQuestionIndex--
                                selectedAnswer = null
                                isAnswered = false
                                showExplanation = false
                                hasExplained = false
                                isListening = false
                            }
                        },
                        variant = ButtonVariant.Outline,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                VTButton(
                    text = if (currentQuestionIndex == questions.size - 1) "완료" else "다음",
                    onClick = {
                        if (currentQuestionIndex < questions.size - 1) {
                            currentQuestionIndex++
                            selectedAnswer = null
                            isAnswered = false
                            showExplanation = false
                            hasExplained = false
                            isListening = false
                        } else {
                            // 퀴즈 완료 - 서버에 제출
                            val timeSpent = System.currentTimeMillis() - quizStartTime
                            val studentId = currentUser?.id ?: 1
                            
                            val quizAnswers = questions.map { question ->
                                QuizAnswer(
                                    questionId = question.id.toString(),
                                    selectedAnswer = question.selectedAnswer?.toString() ?: "",
                                    isCorrect = question.selectedAnswer == question.correctAnswer
                                )
                            }
                            
                            // AI ViewModel을 통해 퀴즈 제출
                            aiViewModel.submitQuiz(
                                assignmentId = assignmentId.toString(),
                                studentId = studentId,
                                answers = quizAnswers,
                                timeSpent = timeSpent
                            )
                        }
                    },
                    variant = ButtonVariant.Primary,
                    enabled = isAnswered,
                    modifier = Modifier.weight(2f)
                )
            }
        }
        
        // 퀴즈 완료 화면
        if (isQuizCompleted) {
            val percentage = if (totalQuestions > 0) (quizScore * 100 / totalQuestions) else 0
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                VTCard(
                    variant = CardVariant.Elevated,
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // 결과 아이콘
                        Icon(
                            imageVector = if (percentage >= 80) Icons.Filled.EmojiEvents else Icons.Filled.School,
                            contentDescription = null,
                            tint = if (percentage >= 80) Color(0xFFFFD700) else PrimaryIndigo,
                            modifier = Modifier.size(64.dp)
                        )
                        
                        // 결과 텍스트
                        Text(
                            text = "퀴즈 완료!",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Gray800
                        )
                        
                        Text(
                            text = "점수: $quizScore/$totalQuestions ($percentage%)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = if (percentage >= 80) Success else if (percentage >= 60) Color(0xFFFF9800) else Error
                        )
                        
                        // 피드백 메시지
                        Text(
                            text = when {
                                percentage >= 90 -> "훌륭합니다! 완벽하게 이해하셨네요."
                                percentage >= 80 -> "잘 하셨습니다! 조금만 더 복습하면 완벽해요."
                                percentage >= 60 -> "괜찮습니다. 틀린 문제를 다시 확인해보세요."
                                else -> "조금 더 공부가 필요합니다. 다시 한번 도전해보세요!"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600,
                            textAlign = TextAlign.Center
                        )
                        
                        // 버튼들
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            VTButton(
                                text = "다시 풀기",
                                onClick = {
                                    isQuizCompleted = false
                                    quizScore = 0
                                    totalQuestions = 0
                                    quizStartTime = System.currentTimeMillis()
                                    // 질문 상태 초기화
                                    questions.forEach { question ->
                                        question.selectedAnswer = null
                                        question.isAnswered = false
                                        question.showExplanation = false
                                        question.hasExplained = false
                                        question.isListening = false
                                    }
                                },
                                variant = ButtonVariant.Secondary,
                                modifier = Modifier.weight(1f)
                            )
                            
                            VTButton(
                                text = "완료",
                                onClick = {
                                    // 퀴즈 결과가 이미 서버에 저장되었으므로 메인 화면으로 이동
                                    // TODO: Navigation으로 메인 화면으로 이동
                                    println("Quiz completed with score: $quizScore/$totalQuestions")
                                },
                                variant = ButtonVariant.Primary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnswerOption(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean?,
    isWrong: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isCorrect == true -> Success.copy(alpha = 0.1f)
            isWrong -> Error.copy(alpha = 0.1f)
            isSelected -> PrimaryIndigo.copy(alpha = 0.1f)
            else -> Color.Transparent
        },
        animationSpec = tween(300),
        label = "background_color"
    )
    
    val borderColor by animateColorAsState(
        targetValue = when {
            isCorrect == true -> Success
            isWrong -> Error
            isSelected -> PrimaryIndigo
            else -> Gray300
        },
        animationSpec = tween(300),
        label = "border_color"
    )
    
    val textColor by animateColorAsState(
        targetValue = when {
            isCorrect == true -> Success
            isWrong -> Error
            isSelected -> PrimaryIndigo
            else -> Gray800
        },
        animationSpec = tween(300),
        label = "text_color"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = textColor,
                fontWeight = if (isSelected || isCorrect == true || isWrong) FontWeight.Medium else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            
            if (isCorrect == true) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Success,
                    modifier = Modifier.size(24.dp)
                )
            } else if (isWrong) {
                Icon(
                    imageVector = Icons.Filled.Cancel,
                    contentDescription = null,
                    tint = Error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun QuizScreenPreview() {
    VoiceTutorTheme {
        QuizScreen()
    }
}
