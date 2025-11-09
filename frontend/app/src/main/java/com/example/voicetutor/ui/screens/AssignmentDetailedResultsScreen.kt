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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.voicetutor.data.models.DetailedQuestionResult
import com.example.voicetutor.data.models.QuestionGroup
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel

@Composable
fun AssignmentDetailedResultsScreen(
    personalAssignmentId: Int,
    assignmentTitle: String = "과제 결과",
    onBackClick: () -> Unit = {},
    viewModel: AssignmentViewModel = hiltViewModel()
) {
    // API에서 정답 여부 데이터 로드
    LaunchedEffect(personalAssignmentId) {
        viewModel.loadAssignmentCorrectness(personalAssignmentId)
        viewModel.loadPersonalAssignmentStatistics(personalAssignmentId)
    }

    val correctnessData by viewModel.assignmentCorrectness.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val statistics by viewModel.personalAssignmentStatistics.collectAsState()

    // API 데이터를 더미 데이터 형식으로 변환
    val detailedResults = remember(correctnessData) {
        correctnessData.map { item ->
            DetailedQuestionResult(
                questionNumber = item.questionNum,
                question = item.questionContent,
                myAnswer = item.studentAnswer,
                correctAnswer = item.questionModelAnswer,
                isCorrect = item.isCorrect,
                explanation = item.explanation
            )
        }
    }
    
    // base question과 tail question으로 그룹화
    val questionGroups = remember(detailedResults) {
        val grouped = mutableMapOf<String, MutableList<DetailedQuestionResult>>()

        detailedResults.forEach { result ->
            val baseNum = if (result.questionNumber.contains("-")) {
                result.questionNumber.substringBefore("-")
            } else {
                result.questionNumber
            }

            if (!grouped.containsKey(baseNum)) {
                grouped[baseNum] = mutableListOf()
            }
            grouped[baseNum]?.add(result)
        }

        // QuestionGroup 리스트로 변환
        grouped.entries.sortedBy { it.key.toIntOrNull() ?: 0 }.map { (baseNum, questions) ->
            val base = questions.find { it.questionNumber == baseNum }
            val tails = questions.filter { it.questionNumber != baseNum }
                .sortedBy { it.questionNumber }

            QuestionGroup(
                baseQuestion = base ?: questions.first(),
                tailQuestions = tails
            )
        }
    }

    // 각 그룹의 토글 상태 관리
    val expandedStates = remember(questionGroups) {
        mutableStateMapOf<String, Boolean>().apply {
            questionGroups.forEach { group ->
                this[group.baseQuestion.questionNumber] = false
            }
        }
    }

    val totalQuestions = detailedResults.size
    // API에서 평균 점수를 가져옴 (0~100 사이의 값)
    val averageScore = statistics?.averageScore?.toInt() ?: 0

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (error != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "오류가 발생했습니다",
                    color = Error,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = error ?: "알 수 없는 오류",
                    color = Gray600,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    } else if (detailedResults.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "결과가 없습니다",
                color = Gray600,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else {
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
                    title = "점수",
                    value = "${averageScore}점",
                    icon = Icons.Filled.Grade,
                    iconColor = if (averageScore >= 80) Success else Warning,
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

                questionGroups.forEachIndexed { index, group ->
                    QuestionGroupCard(
                        group = group,
                        isExpanded = expandedStates[group.baseQuestion.questionNumber] ?: false,
                        onToggle = {
                            val currentState = expandedStates[group.baseQuestion.questionNumber] ?: false
                            expandedStates[group.baseQuestion.questionNumber] = !currentState
                        }
                    )

                    if (index < questionGroups.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionGroupCard(
    group: QuestionGroup,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Base question card with toggle
        VTCard(
            variant = CardVariant.Outlined,
            onClick = if (group.tailQuestions.isNotEmpty()) onToggle else null
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Question header with toggle icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Question number + Result badge
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "문제 ${group.baseQuestion.questionNumber}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Gray800
                        )

                        // Result badge
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (group.baseQuestion.isCorrect) Success.copy(alpha = 0.1f) else Error.copy(alpha = 0.1f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (group.baseQuestion.isCorrect) "정답" else "오답",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (group.baseQuestion.isCorrect) Success else Error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Right: Toggle with tail count (if exists)
                    if (group.tailQuestions.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .background(
                                    color = PrimaryIndigo.copy(alpha = 0.1f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Tail question count
                            Text(
                                text = "꼬리질문 ${group.tailQuestions.size}개",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryIndigo,
                                fontWeight = FontWeight.Bold
                            )

                            // Toggle icon
                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (isExpanded) "접기" else "펼치기",
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Question text
                Text(
                    text = group.baseQuestion.question,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Gray800
                )

                // My answer
                if (group.baseQuestion.myAnswer.isNotEmpty()) {
                    Column {
                        Text(
                            text = "내 답변",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Gray600
                        )
                        Text(
                            text = group.baseQuestion.myAnswer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray800,
                            modifier = Modifier
                                .background(
                                    color = if (group.baseQuestion.isCorrect) Success.copy(alpha = 0.1f) else Error.copy(alpha = 0.1f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        )
                    }
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
                        text = group.baseQuestion.correctAnswer,
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
                group.baseQuestion.explanation?.let { explanation ->
                    if (explanation.isNotEmpty()) {
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

        // Tail questions (shown when expanded)
        if (isExpanded && group.tailQuestions.isNotEmpty()) {
            Column(
                modifier = Modifier.padding(start = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                group.tailQuestions.forEach { tailQuestion ->
                    DetailedQuestionResultCard(question = tailQuestion)
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
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
            if (question.myAnswer.isNotEmpty()) {
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
                if (explanation.isNotEmpty()) {
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
}

@Preview(showBackground = true)
@Composable
fun AssignmentDetailedResultsScreenPreview() {
    VoiceTutorTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Preview - personalAssignmentId 필요")
        }
    }
}
