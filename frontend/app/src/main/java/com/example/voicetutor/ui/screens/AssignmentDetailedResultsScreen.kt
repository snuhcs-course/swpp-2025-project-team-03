package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.voicetutor.data.models.DetailedQuestionResult
import com.example.voicetutor.data.models.QuestionGroup
import com.example.voicetutor.data.models.QuestionGroupFactory
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.utils.ErrorMessageMapper
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AssignmentDetailedResultsScreen(
    personalAssignmentId: Int,
    assignmentTitle: String = "리포트",
    viewModel: AssignmentViewModel = hiltViewModel(),
) {
    LaunchedEffect(personalAssignmentId) {
        viewModel.loadAssignmentCorrectness(personalAssignmentId)
        viewModel.loadPersonalAssignmentStatistics(personalAssignmentId)
    }

    val correctnessData by viewModel.assignmentCorrectness.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val statistics by viewModel.personalAssignmentStatistics.collectAsState()

    // 네트워크 에러가 아닌 경우에만 에러를 클리어합니다.
    // 네트워크 에러는 detailedResults.isEmpty()일 때 구분하기 위해 유지합니다.
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            if (!ErrorMessageMapper.isNetworkError(errorMessage)) {
                viewModel.clearError()
            }
        }
    }

    val detailedResults = remember(correctnessData) {
        correctnessData.map { item ->
            DetailedQuestionResult(
                questionNumber = item.questionNum,
                question = item.questionContent,
                myAnswer = item.studentAnswer,
                correctAnswer = item.questionModelAnswer,
                isCorrect = item.isCorrect,
                explanation = item.explanation,
            )
        }
    }

    val questionGroups = remember(detailedResults) {
        QuestionGroupFactory.createQuestionGroups(detailedResults)
    }

    val expandedStates = remember(questionGroups) {
        mutableStateMapOf<String, Boolean>().apply {
            questionGroups.forEach { group ->
                this[group.baseQuestion.questionNumber] = false
            }
        }
    }

    val totalQuestions = questionGroups.size
    val averageScore = statistics?.averageScore?.toInt() ?: 0

    // 로딩 중이거나, 아직 데이터가 로드되지 않은 초기 상태(결과가 비어있고 에러도 없는 경우)에는 로딩 표시
    if (isLoading || (detailedResults.isEmpty() && error == null)) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else if (error != null && detailedResults.isNotEmpty()) {
        // 에러가 있지만 결과 데이터가 있는 경우 (통계 로딩 실패 등)
        // 네트워크 에러가 아닌 경우에만 표시
        val isNetworkError = ErrorMessageMapper.isNetworkError(error)
        if (!isNetworkError) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = null,
                        tint = Error,
                        modifier = Modifier.size(48.dp),
                    )
                    Text(
                        text = "오류가 발생했습니다",
                        color = Error,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = ErrorMessageMapper.getErrorMessage(error),
                        color = Gray600,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    } else if (detailedResults.isEmpty() && error != null) {
        // 네트워크 에러로 인해 결과를 불러오지 못한 경우
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.WifiOff,
                    contentDescription = null,
                    tint = Gray400,
                    modifier = Modifier.size(48.dp),
                )
                Text(
                    text = "네트워크가 불안정합니다",
                    color = Gray600,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    } else {
        val scrollState = rememberScrollState()
        val coroutineScope = rememberCoroutineScope()
        val density = LocalDensity.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = PrimaryIndigo.copy(alpha = 0.08f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    )
                    .padding(20.dp),
            ) {
                Column {
                    Text(
                        text = assignmentTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "리포트",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                VTStatsCard(
                    title = "총 문제",
                    value = "${totalQuestions}개",
                    icon = Icons.Filled.Quiz,
                    iconColor = PrimaryIndigo,
                    modifier = Modifier.weight(1f),
                    variant = CardVariant.Elevated,
                )

                VTStatsCard(
                    title = "점수",
                    value = "${averageScore}점",
                    icon = Icons.Filled.Grade,
                    iconColor = if (averageScore >= 80) Success else Warning,
                    modifier = Modifier.weight(1f),
                    variant = CardVariant.Elevated,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Text(
                    text = "문제별 상세 결과",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800,
                )

                Spacer(modifier = Modifier.height(12.dp))

                questionGroups.forEachIndexed { index, group ->
                    QuestionGroupCard(
                        group = group,
                        isExpanded = expandedStates[group.baseQuestion.questionNumber] ?: false,
                        onToggle = {
                            val currentState = expandedStates[group.baseQuestion.questionNumber] ?: false
                            val willExpand = !currentState
                            expandedStates[group.baseQuestion.questionNumber] = willExpand

                            // 확장될 때 스크롤을 조금 내림
                            if (willExpand) {
                                coroutineScope.launch {
                                    // 약간의 딜레이 후 스크롤 (레이아웃이 완료된 후)
                                    delay(100)
                                    val scrollOffset = with(density) { 150.dp.toPx() }.toInt()
                                    val targetScroll = scrollState.value + scrollOffset
                                    scrollState.animateScrollTo(targetScroll)
                                }
                            }
                        },
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
    onToggle: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        VTCard(
            variant = CardVariant.Outlined,
            onClick = if (group.tailQuestions.isNotEmpty()) onToggle else null,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "문제 ${group.baseQuestion.questionNumber}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Gray800,
                        )

                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (group.baseQuestion.isCorrect) Success.copy(alpha = 0.1f) else Error.copy(alpha = 0.1f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = if (group.baseQuestion.isCorrect) "정답" else "오답",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (group.baseQuestion.isCorrect) Success else Error,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }

                    if (group.tailQuestions.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .background(
                                    color = PrimaryIndigo.copy(alpha = 0.1f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (isExpanded) "꼬리질문 접기" else "꼬리질문 펼치기",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryIndigo,
                                fontWeight = FontWeight.Bold,
                            )

                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (isExpanded) "접기" else "펼치기",
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }

                Text(
                    text = group.baseQuestion.question,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Gray800,
                )

                if (group.baseQuestion.myAnswer.isNotEmpty()) {
                    Column {
                        Text(
                            text = "내 답변",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Gray600,
                        )
                        Text(
                            text = group.baseQuestion.myAnswer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray800,
                            modifier = Modifier
                                .background(
                                    color = if (group.baseQuestion.isCorrect) Success.copy(alpha = 0.1f) else Error.copy(alpha = 0.1f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                )
                                .padding(12.dp),
                        )
                    }
                }

                Column {
                    Text(
                        text = "정답",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Gray600,
                    )
                    Text(
                        text = group.baseQuestion.correctAnswer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray800,
                        modifier = Modifier
                            .background(
                                color = Success.copy(alpha = 0.1f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            )
                            .padding(12.dp),
                    )
                }

                group.baseQuestion.explanation?.let { explanation ->
                    if (explanation.isNotEmpty()) {
                        Column {
                            Text(
                                text = "해설",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Gray600,
                            )
                            Text(
                                text = explanation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray700,
                                modifier = Modifier
                                    .background(
                                        color = PrimaryIndigo.copy(alpha = 0.1f),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                    )
                                    .padding(12.dp),
                            )
                        }
                    }
                }
            }
        }

        if (isExpanded && group.tailQuestions.isNotEmpty()) {
            val cardPositions = remember { mutableStateListOf<Float>() }
            val density = androidx.compose.ui.platform.LocalDensity.current

            Row(
                modifier = Modifier.padding(start = 12.dp),
            ) {
                Box(
                    modifier = Modifier.width(20.dp),
                ) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(20.dp),
                    ) {
                        if (cardPositions.size == group.tailQuestions.size) {
                            val lineColor = PrimaryIndigo.copy(alpha = 0.4f)
                            val strokeWidth = 3.dp.toPx()
                            val verticalLineX = 0.dp.toPx()
                            val branchLength = 16.dp.toPx()
                            val curveRadius = 8.dp.toPx()

                            cardPositions.forEachIndexed { index, yPosition ->
                                val path = androidx.compose.ui.graphics.Path()

                                when {
                                    index == 0 -> {
                                        path.moveTo(verticalLineX, 0f)
                                        path.lineTo(verticalLineX, yPosition - curveRadius)

                                        path.quadraticTo(
                                            verticalLineX,
                                            yPosition,
                                            verticalLineX + curveRadius,
                                            yPosition,
                                        )
                                        path.lineTo(branchLength, yPosition)
                                    }
                                    index == cardPositions.size - 1 -> {
                                        val prevYPosition = cardPositions[index - 1]
                                        path.moveTo(verticalLineX, prevYPosition)
                                        path.lineTo(verticalLineX, yPosition - curveRadius)

                                        path.quadraticTo(
                                            verticalLineX,
                                            yPosition,
                                            verticalLineX + curveRadius,
                                            yPosition,
                                        )
                                        path.lineTo(branchLength, yPosition)
                                    }
                                    else -> {
                                        val prevYPosition = cardPositions[index - 1]
                                        path.moveTo(verticalLineX, prevYPosition)
                                        path.lineTo(verticalLineX, yPosition - curveRadius)

                                        path.quadraticTo(
                                            verticalLineX,
                                            yPosition,
                                            verticalLineX + curveRadius,
                                            yPosition,
                                        )
                                        path.lineTo(branchLength, yPosition)
                                    }
                                }

                                drawPath(
                                    path = path,
                                    color = lineColor,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth),
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    group.tailQuestions.forEachIndexed { index, tailQuestion ->
                        Box(
                            modifier = Modifier.onGloballyPositioned { coordinates ->
                                val yPos = coordinates.positionInParent().y + with(density) { 28.dp.toPx() }
                                if (index < cardPositions.size) {
                                    cardPositions[index] = yPos
                                } else {
                                    cardPositions.add(yPos)
                                }
                            },
                        ) {
                            DetailedQuestionResultCard(question = tailQuestion)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = PrimaryIndigo.copy(alpha = 0.1f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                            )
                            .clickable { onToggle() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "꼬리질문 접기",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryIndigo,
                                fontWeight = FontWeight.Bold,
                            )

                            Icon(
                                imageVector = Icons.Filled.ExpandLess,
                                contentDescription = "접기",
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailedQuestionResultCard(
    question: DetailedQuestionResult,
) {
    VTCard(
        variant = CardVariant.Outlined,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "문제 ${question.questionNumber}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Gray800,
                )

                Box(
                    modifier = Modifier
                        .background(
                            color = if (question.isCorrect) Success.copy(alpha = 0.1f) else Error.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = if (question.isCorrect) "정답" else "오답",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (question.isCorrect) Success else Error,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Text(
                text = question.question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Gray800,
            )

            if (question.myAnswer.isNotEmpty()) {
                Column {
                    Text(
                        text = "내 답변",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Gray600,
                    )
                    Text(
                        text = question.myAnswer,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray800,
                        modifier = Modifier
                            .background(
                                color = if (question.isCorrect) Success.copy(alpha = 0.1f) else Error.copy(alpha = 0.1f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                            )
                            .padding(12.dp),
                    )
                }
            }

            Column {
                Text(
                    text = "정답",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Gray600,
                )
                Text(
                    text = question.correctAnswer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray800,
                    modifier = Modifier
                        .background(
                            color = Success.copy(alpha = 0.1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                        )
                        .padding(12.dp),
                )
            }

            question.explanation?.let { explanation ->
                if (explanation.isNotEmpty()) {
                    Column {
                        Text(
                            text = "해설",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Gray600,
                        )
                        Text(
                            text = explanation,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray700,
                            modifier = Modifier
                                .background(
                                    color = PrimaryIndigo.copy(alpha = 0.1f),
                                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                                )
                                .padding(12.dp),
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
            contentAlignment = Alignment.Center,
        ) {
            Text("Preview - personalAssignmentId 필요")
        }
    }
}
