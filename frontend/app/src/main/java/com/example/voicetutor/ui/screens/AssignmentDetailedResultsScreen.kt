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
    assignmentTitle: String = "과제 결과",
    onBackClick: () -> Unit = {}
) {
    // 더미 데이터 생성 - 함수의 개념 단원
    val detailedResults = remember {
        listOf(
            DetailedQuestionResult(
                questionNumber = 1,
                question = "함수의 정의는 무엇인가요?",
                questionType = QuestionType.VOICE_RESPONSE,
                myAnswer = "그래프",
                correctAnswer = "정의역의 각 원소에 대해 공역의 원소가 단 하나만 대응되는 대응관계",
                isCorrect = false,
                explanation = "함수는 정의역의 각 원소에 대해 공역의 원소가 단 하나만 대응되는 대응관계입니다."
            ),
            DetailedQuestionResult(
                questionNumber = 2,
                question = "f(x) = 2x + 3에서 f(5)의 값은?",
                questionType = QuestionType.SHORT_ANSWER,
                myAnswer = "10",
                correctAnswer = "13",
                isCorrect = false,
                explanation = "f(5) = 2(5) + 3 = 10 + 3 = 13입니다. x에 5를 대입하여 계산해야 합니다."
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
                    text = "과제 결과",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
        
        // Summary stats (위에 크게 표시)
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
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Questions list (아래에 문제별 상세)
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
            
            // Question text
            Text(
                text = question.question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Gray800
            )
            
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
