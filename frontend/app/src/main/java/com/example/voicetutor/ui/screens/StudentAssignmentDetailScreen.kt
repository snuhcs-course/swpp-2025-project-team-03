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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel

data class QuestionResult(
    val questionNumber: Int,
    val question: String,
    val myAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val confidence: Float, // 0.0 ~ 1.0
    val questionType: QuestionType
)

enum class QuestionType {
    MULTIPLE_CHOICE,
    SHORT_ANSWER,
    VOICE_RESPONSE
}

@Composable
fun StudentAssignmentDetailScreen(
    assignmentId: Int = 1, // 임시로 기본값 설정
    studentId: Int = 1, // 임시로 기본값 설정
    assignmentTitle: String = "과제", // TODO: 실제 과제 제목으로 동적 설정
    onBackClick: () -> Unit = {}
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val assignmentResults by viewModel.assignmentResults.collectAsStateWithLifecycle()
    val currentAssignment by viewModel.currentAssignment.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    // 동적 과제 제목 가져오기
    val dynamicAssignmentTitle = currentAssignment?.title ?: assignmentTitle
    
    // Load assignment data and results on first composition
    LaunchedEffect(assignmentId) {
        viewModel.loadAssignmentById(assignmentId)
        viewModel.loadAssignmentResults(assignmentId)
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            viewModel.clearError()
        }
    }
    
    // Find student's result
    val studentResult = assignmentResults.find { it.studentId == studentId.toString() }
    
    // Convert DetailedAnswer to QuestionResult for UI
    val questions = studentResult?.detailedAnswers?.map { answer ->
        QuestionResult(
            questionNumber = answer.questionNumber,
            question = answer.question,
            myAnswer = answer.studentAnswer,
            correctAnswer = answer.correctAnswer,
            isCorrect = answer.isCorrect,
            confidence = answer.confidenceScore / 100f,
            questionType = when {
                answer.question.contains("선택") -> QuestionType.MULTIPLE_CHOICE
                answer.question.contains("음성") -> QuestionType.VOICE_RESPONSE
                else -> QuestionType.SHORT_ANSWER
            }
        )
    } ?: emptyList()
    
    val totalQuestions = questions.size
    val correctAnswers = questions.count { it.isCorrect }
    val totalScore = if (totalQuestions > 0) (correctAnswers * 100) / totalQuestions else 0
    val averageConfidence = if (questions.isNotEmpty()) {
        (questions.sumOf { (it.confidence * 100).toInt() } / questions.size).toFloat()
    } else 0f
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
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
                    text = dynamicAssignmentTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "과제 결과를 확인하고 피드백을 받아보세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
        
        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = PrimaryIndigo
                )
            }
        } else if (studentResult == null) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Filled.Assignment,
                        contentDescription = null,
                        tint = Gray400,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "과제 결과를 찾을 수 없습니다",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Gray600
                    )
                }
            }
        } else {
            // Score summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VTStatsCard(
                    title = "총 점수",
                    value = "${studentResult.score}점",
                    icon = Icons.Filled.Grade,
                    iconColor = if (studentResult.score >= 80) Success else Warning,
                    modifier = Modifier.weight(1f),
                    variant = CardVariant.Gradient
                )
                
                VTStatsCard(
                    title = "정답률",
                    value = "${totalScore}%",
                    icon = Icons.Filled.CheckCircle,
                    iconColor = if (totalScore >= 80) Success else Warning,
                    modifier = Modifier.weight(1f),
                    variant = CardVariant.Gradient
                )
            }
            
            // Additional stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VTStatsCard(
                    title = "완료 시간",
                    value = "정보 없음",
                    icon = Icons.Filled.Timer,
                    iconColor = PrimaryIndigo,
                    modifier = Modifier.weight(1f),
                    variant = CardVariant.Elevated
                )
                
                VTStatsCard(
                    title = "자신감 점수",
                    value = "${studentResult.confidenceScore}%",
                    icon = Icons.Filled.Psychology,
                    iconColor = if (studentResult.confidenceScore >= 70) Success else Warning,
                    modifier = Modifier.weight(1f),
                    variant = CardVariant.Elevated
                )
            }
            
            // Questions list
            Column {
                Text(
                    text = "문제별 결과",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (questions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
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
                                text = "문제 결과가 없습니다",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Gray600
                            )
                        }
                    }
                } else {
                    questions.forEachIndexed { index, question ->
                        QuestionResultCard(question = question)
                        
                        if (index < questions.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionResultCard(
    question: QuestionResult
) {
    VTCard(
        variant = CardVariant.Outlined
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Question header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "문제 ${question.questionNumber}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Gray800
                )
                
                // Result badge
                Box(
                    modifier = Modifier
                        .background(
                            color = if (question.isCorrect) Success.copy(alpha = 0.1f) else Error.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (question.isCorrect) "정답" else "오답",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (question.isCorrect) Success else Error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Question text
            Text(
                text = question.question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Gray800
            )
            
            // My answer
            Column {
                Text(
                    text = "내 답변",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Gray600
                )
                Text(
                    text = question.myAnswer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray800,
                    modifier = Modifier
                        .background(
                            color = if (question.isCorrect) Success.copy(alpha = 0.1f) else Error.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                )
            }
            
            // Correct answer
            Column {
                Text(
                    text = "정답",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Gray600
                )
                Text(
                    text = question.correctAnswer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray800,
                    modifier = Modifier
                        .background(
                            color = Success.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                )
            }
            
            // Confidence score
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "자신감:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                VTProgressBar(
                    progress = question.confidence,
                    showPercentage = false,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "${(question.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Gray800
                )
            }
            
            // Question type
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (question.questionType) {
                        QuestionType.MULTIPLE_CHOICE -> Icons.Filled.RadioButtonChecked
                        QuestionType.SHORT_ANSWER -> Icons.Filled.Edit
                        QuestionType.VOICE_RESPONSE -> Icons.Filled.Mic
                    },
                    contentDescription = null,
                    tint = PrimaryIndigo,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = when (question.questionType) {
                        QuestionType.MULTIPLE_CHOICE -> "객관식"
                        QuestionType.SHORT_ANSWER -> "주관식"
                        QuestionType.VOICE_RESPONSE -> "음성 답변"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudentAssignmentDetailScreenPreview() {
    VoiceTutorTheme {
        StudentAssignmentDetailScreen()
    }
}