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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*

// 더미 데이터 클래스
data class DetailedQuestionResult(
    val questionNumber: Int,
    val question: String,
    val questionType: QuestionType,
    val myAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val explanation: String? = null
)

enum class QuestionType {
    MULTIPLE_CHOICE,
    SHORT_ANSWER,
    VOICE_RESPONSE
}

@Composable
fun AssignmentDetailedResultsScreen(
    assignmentTitle: String = "과제 상세 결과",
    onBackClick: () -> Unit = {}
) {
    // 더미 데이터 생성
    val detailedResults = remember {
        listOf(
            DetailedQuestionResult(
                questionNumber = 1,
                question = "다음 중 한국의 수도는 어디인가요?",
                questionType = QuestionType.MULTIPLE_CHOICE,
                myAnswer = "서울",
                correctAnswer = "서울",
                isCorrect = true,
                explanation = "서울은 대한민국의 수도이자 최대 도시입니다."
            ),
            DetailedQuestionResult(
                questionNumber = 2,
                question = "1+1은 얼마인가요?",
                questionType = QuestionType.SHORT_ANSWER,
                myAnswer = "3",
                correctAnswer = "2",
                isCorrect = false,
                explanation = "1+1은 2입니다. 기본적인 덧셈 연산입니다."
            ),
            DetailedQuestionResult(
                questionNumber = 3,
                question = "오늘 날씨에 대해 음성으로 설명해주세요.",
                questionType = QuestionType.VOICE_RESPONSE,
                myAnswer = "맑음",
                correctAnswer = "맑음",
                isCorrect = true,
                explanation = "정확한 음성 인식으로 답변하셨습니다."
            ),
            DetailedQuestionResult(
                questionNumber = 4,
                question = "지구는 태양계의 몇 번째 행성인가요?",
                questionType = QuestionType.MULTIPLE_CHOICE,
                myAnswer = "2번째",
                correctAnswer = "3번째",
                isCorrect = false,
                explanation = "지구는 태양에서 세 번째로 가까운 행성입니다."
            ),
            DetailedQuestionResult(
                questionNumber = 5,
                question = "한국의 전통 음식 중 하나를 말해주세요.",
                questionType = QuestionType.SHORT_ANSWER,
                myAnswer = "김치",
                correctAnswer = "김치",
                isCorrect = true,
                explanation = "김치는 한국의 대표적인 전통 음식입니다."
            )
        )
    }
    
    val totalQuestions = detailedResults.size
    val correctAnswers = detailedResults.count { it.isCorrect }
    val totalScore = if (totalQuestions > 0) (correctAnswers * 100) / totalQuestions else 0
    
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
                    text = "문제별 상세 결과를 확인해보세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
        
        // Summary stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VTStatsCard(
                title = "총 문제",
                value = "${totalQuestions}개",
                icon = Icons.Filled.Quiz,
                iconColor = PrimaryIndigo,
                modifier = Modifier.weight(1f),
                variant = CardVariant.Elevated
            )
            
            VTStatsCard(
                title = "정답률",
                value = "${totalScore}%",
                icon = Icons.Filled.Grade,
                iconColor = if (totalScore >= 80) Success else Warning,
                modifier = Modifier.weight(1f),
                variant = CardVariant.Elevated
            )
        }
        
        // Questions list
        Column {
            Text(
                text = "문제별 상세 결과",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray800
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            detailedResults.forEachIndexed { index, question ->
                DetailedQuestionResultCard(question = question)
                
                if (index < detailedResults.size - 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun DetailedQuestionResultCard(
    question: DetailedQuestionResult
) {
    VTCard(
        variant = CardVariant.Outlined
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
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
            
            // Question type and text
            Row(
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
            
            
            // Explanation
            question.explanation?.let { explanation ->
                Column {
                    Text(
                        text = "해설",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Gray600
                    )
                    Text(
                        text = explanation,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray700,
                        modifier = Modifier
                            .background(
                                color = PrimaryIndigo.copy(alpha = 0.1f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AssignmentDetailedResultsScreenPreview() {
    VoiceTutorTheme {
        AssignmentDetailedResultsScreen()
    }
}
