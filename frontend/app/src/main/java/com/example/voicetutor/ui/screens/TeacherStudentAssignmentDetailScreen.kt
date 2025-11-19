package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel

private fun formatSubmittedTime(isoTime: String): String {
    return com.example.voicetutor.utils.formatSubmittedTime(isoTime)
}

private fun formatDuration(startIso: String?, endIso: String?): String {
    return try {
        if (startIso.isNullOrEmpty() || endIso.isNullOrEmpty()) {
            return "정보 없음"
        }
        val start = parseIsoToMillis(startIso)
        val end = parseIsoToMillis(endIso)
        if (start == null || end == null || end <= start) {
            return "정보 없음"
        }
        val diffMs = end - start
        val totalSeconds = diffMs / 1000
        val hours = (totalSeconds / 3600).toInt()
        val minutes = ((totalSeconds % 3600) / 60).toInt()
        val seconds = (totalSeconds % 60).toInt()
        if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    } catch (e: Exception) {
        "정보 없음"
    }
}

private fun parseIsoToMillis(iso: String): Long? {
    return try {
        val cleaned = iso.replace("Z", "").let { raw ->
            val dotIdx = raw.indexOf('.')
            if (dotIdx != -1) raw.substring(0, dotIdx) else raw
        }
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        sdf.parse(cleaned)?.time
    } catch (e: Exception) {
        null
    }
}

@Composable
fun TeacherStudentAssignmentDetailScreen(
    studentId: String,
    assignmentId: Int = 0,
    assignmentTitle: String = "과제"
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val studentViewModel: com.example.voicetutor.ui.viewmodel.StudentViewModel = hiltViewModel()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val assignmentResults by viewModel.assignmentResults.collectAsStateWithLifecycle()
    val currentAssignment by viewModel.currentAssignment.collectAsStateWithLifecycle()
    val currentStudent by studentViewModel.currentStudent.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val paStats by viewModel.personalAssignmentStatistics.collectAsStateWithLifecycle()
    val correctnessData by viewModel.assignmentCorrectness.collectAsStateWithLifecycle()
    
    val targetAssignment = remember(currentAssignment, assignments, assignmentId, assignmentTitle) {
        if (assignmentId > 0) {
            currentAssignment?.takeIf { it.id == assignmentId } 
                ?: assignments.find { it.id == assignmentId }
        } else {
            currentAssignment ?: assignments.find { 
                it.title == assignmentTitle || 
                it.title.contains(assignmentTitle) ||
                assignmentTitle.contains(it.title)
            }
        }
    }
    
    val studentName = currentStudent?.name ?: "학생"
    val dynamicAssignmentTitle = currentAssignment?.title ?: (targetAssignment?.title ?: assignmentTitle)
    
    var loadedDataForKey by remember { mutableStateOf<String?>(null) }
    var loadedStatsForKey by remember { mutableStateOf<String?>(null) }
    var loadedCorrectnessForKey by remember { mutableStateOf<String?>(null) }
    val dataKey = "$studentId-$assignmentId"
    
    val existingStudentResult = assignmentResults.find { it.studentId == studentId }
    val hasExistingData = existingStudentResult != null
    
    LaunchedEffect(assignmentId, studentId) {
        val resolvedAssignmentId = if (assignmentId > 0) {
            assignmentId
        } else {
            targetAssignment?.id
        }
        
        if (resolvedAssignmentId != null && dataKey != loadedDataForKey) {
            println("TeacherStudentAssignmentDetail - Loading data for student: $studentId, assignment: $resolvedAssignmentId")
            loadedDataForKey = dataKey
            
            val studentIdInt = studentId.toIntOrNull() ?: 1
            
            if (currentAssignment?.id != resolvedAssignmentId) {
                viewModel.loadAssignmentById(resolvedAssignmentId)
            }
            
            if (!hasExistingData) {
                viewModel.loadAssignmentStudentResults(resolvedAssignmentId)
            }
        }
        
        if (resolvedAssignmentId != null && dataKey != loadedStatsForKey && paStats == null) {
            loadedStatsForKey = dataKey
            val studentIdInt = studentId.toIntOrNull() ?: 1
            viewModel.loadPersonalAssignmentStatisticsFor(studentIdInt, resolvedAssignmentId, silent = true)
        }
        
        if (resolvedAssignmentId != null && dataKey != loadedCorrectnessForKey && correctnessData.isEmpty()) {
            loadedCorrectnessForKey = dataKey
            val studentIdInt = studentId.toIntOrNull() ?: 1
            viewModel.loadAssignmentCorrectnessFor(studentIdInt, resolvedAssignmentId, silent = true)
        }
        
        if (currentStudent?.id.toString() != studentId) {
            studentViewModel.loadStudentById(studentId.toIntOrNull() ?: 1)
        }
    }
    
    LaunchedEffect(assignments, assignmentTitle, studentId, assignmentId) {
        if (assignmentId == 0 && targetAssignment == null && assignments.isNotEmpty() && dataKey != loadedDataForKey) {
            val foundAssignment = assignments.find { 
                it.title == assignmentTitle || 
                it.title.contains(assignmentTitle) ||
                assignmentTitle.contains(it.title)
            }
            foundAssignment?.let { assignment ->
                println("TeacherStudentAssignmentDetail - Found assignment after loading: ${assignment.title} (ID: ${assignment.id}) for student: $studentId")
                loadedDataForKey = "$studentId-${assignment.id}"
                
                val studentIdInt = studentId.toIntOrNull() ?: 1
                viewModel.loadAssignmentById(assignment.id)
                
                if (assignmentResults.none { it.studentId == studentId && it.studentId.isNotEmpty() }) {
                    viewModel.loadAssignmentStudentResults(assignment.id)
                }
                
                viewModel.loadPersonalAssignmentStatisticsFor(studentIdInt, assignment.id, silent = true)
                viewModel.loadAssignmentCorrectnessFor(studentIdInt, assignment.id, silent = true)
            }
        }
    }
    
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            viewModel.clearError()
        }
    }
    
    val studentResult = assignmentResults.find { it.studentId == studentId }
    
    val hasAssignmentResults = assignmentResults.isNotEmpty()
    val hasBasicData = studentResult != null || currentAssignment != null || targetAssignment != null || hasAssignmentResults
    val isInitialLoading = isLoading && !hasBasicData && loadedDataForKey == null
    val isWaitingForStudentData = isLoading && hasBasicData && studentResult == null && loadedDataForKey != null
    val isWaitingForInitialData = isLoading && hasBasicData && studentResult == null && loadedDataForKey == null
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (isInitialLoading || isWaitingForInitialData) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = PrimaryIndigo
                )
            }
        } else if (isWaitingForStudentData) {
            assignmentResults.firstOrNull()?.let { tempResult ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = PrimaryIndigo.copy(alpha = 0.08f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = tempResult.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "로딩 중...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600
                        )
                    }
                }
            } ?: run {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = PrimaryIndigo
                    )
                }
            }
        } else if (studentResult == null && !hasBasicData && !isLoading && loadedDataForKey != null) {
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
                        text = "학생 결과를 찾을 수 없습니다",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Gray600
                    )
                }
            }
        } else {
            val displayStudentResult = studentResult ?: assignmentResults.firstOrNull()
            
            if (displayStudentResult != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = PrimaryIndigo.copy(alpha = 0.08f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            text = displayStudentResult.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "평균 점수: ${paStats?.averageScore?.toInt()?.let { "$it 점" } ?: (if (isLoading) "로딩 중..." else "-")}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VTStatsCard(
                        title = "정답률",
                        value = paStats?.let { "${it.accuracy.toInt()}%" } ?: (if (isLoading) "..." else "-"),
                        icon = Icons.Filled.CheckCircle,
                        iconColor = Success,
                        modifier = Modifier.weight(1f),
                        variant = CardVariant.Gradient
                    )
                    
                    VTStatsCard(
                        title = "평균 점수",
                        value = paStats?.averageScore?.toInt()?.toString() ?: (if (isLoading) "..." else "-"),
                        icon = Icons.Filled.Star,
                        iconColor = Warning,
                        modifier = Modifier.weight(1f),
                        variant = CardVariant.Gradient
                    )
                }
                
                VTCard(variant = CardVariant.Elevated) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "소요 시간",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Gray600
                            )
                            Text(
                                text = formatDuration(displayStudentResult.startedAt, displayStudentResult.submittedAt),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray800
                            )
                        }
                        
                        Divider()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "제출 시간",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Gray600
                            )
                            Text(
                                text = formatSubmittedTime(displayStudentResult.submittedAt),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray800
                            )
                        }
                    }
                }
            }
            
            if (correctnessData.isNotEmpty()) {
                Column {
                    Text(
                        text = "문제별 상세 결과",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    Spacer(modifier = Modifier.height(12.dp))
            
                    // Build groups from correctness
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
                    val questionGroups = remember(detailedResults) {
                        val grouped = mutableMapOf<String, MutableList<DetailedQuestionResult>>()
                        detailedResults.forEach { result ->
                            val baseNum = if (result.questionNumber.contains("-")) result.questionNumber.substringBefore("-") else result.questionNumber
                            grouped.getOrPut(baseNum) { mutableListOf() }.add(result)
                        }
                        grouped.entries.sortedBy { it.key.toIntOrNull() ?: 0 }.map { (baseNum, questions) ->
                            val base = questions.find { it.questionNumber == baseNum } ?: questions.first()
                            val tails = questions.filter { it.questionNumber != baseNum }.sortedBy { it.questionNumber }
                            QuestionGroup(baseQuestion = base, tailQuestions = tails)
                        }
                    }
                    val expandedStates = remember(questionGroups) {
                        mutableStateMapOf<String, Boolean>().apply {
                            questionGroups.forEach { group -> this[group.baseQuestion.questionNumber] = false }
                        }
                    }

                    questionGroups.forEachIndexed { index, group ->
                        QuestionGroupCard2(
                            group = group,
                            isExpanded = expandedStates[group.baseQuestion.questionNumber] ?: false,
                            onToggle = {
                                val current = expandedStates[group.baseQuestion.questionNumber] ?: false
                                expandedStates[group.baseQuestion.questionNumber] = !current
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
}

@Composable
private fun QuestionGroupCard2(
    group: QuestionGroup,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val density = LocalDensity.current
    val cardPositions = remember { mutableStateListOf<Float>() }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
                VTCard(
            variant = CardVariant.Outlined,
            onClick = if (group.tailQuestions.isNotEmpty()) onToggle else null
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            Text(
                                text = if (isExpanded) "꼬리질문 접기" else "꼬리질문 펼치기",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryIndigo,
                                fontWeight = FontWeight.Bold
                            )

                            Icon(
                                imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                contentDescription = if (isExpanded) "접기" else "펼치기",
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Text(
                    text = group.baseQuestion.question,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Gray800
                )

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

        if (isExpanded && group.tailQuestions.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(start = 12.dp)
            ) {
                Box(
                    modifier = Modifier.width(20.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(20.dp)
                    ) {
                        if (cardPositions.size == group.tailQuestions.size) {
                            val lineColor = PrimaryIndigo.copy(alpha = 0.4f)
                            val strokeWidth = 3.dp.toPx()
                            val verticalLineX = 0.dp.toPx()
                            val branchLength = 16.dp.toPx()
                            val curveRadius = 8.dp.toPx()

                            group.tailQuestions.forEachIndexed { index, _ ->
                                val path = Path()
                                val yPosition = cardPositions[index]

                                when (index) {
                                    0 -> {
                                        path.moveTo(verticalLineX, 0f)
                                        path.lineTo(verticalLineX, yPosition - curveRadius)
                                        path.quadraticTo(
                                            verticalLineX, yPosition,
                                            verticalLineX + curveRadius, yPosition
                                        )
                                        path.lineTo(branchLength, yPosition)
                                    }
                                    cardPositions.size - 1 -> {
                                        val prevYPosition = cardPositions[index - 1]
                                        path.moveTo(verticalLineX, prevYPosition)
                                        path.lineTo(verticalLineX, yPosition - curveRadius)
                                        path.quadraticTo(
                                            verticalLineX, yPosition,
                                            verticalLineX + curveRadius, yPosition
                                        )
                                        path.lineTo(branchLength, yPosition)
                                    }
                                    else -> {
                                        val prevYPosition = cardPositions[index - 1]
                                        path.moveTo(verticalLineX, prevYPosition)
                                        path.lineTo(verticalLineX, yPosition - curveRadius)
                                        path.quadraticTo(
                                            verticalLineX, yPosition,
                                            verticalLineX + curveRadius, yPosition
                                        )
                                        path.lineTo(branchLength, yPosition)
                                    }
                                }

                                drawPath(
                                    path = path,
                                    color = lineColor,
                                    style = Stroke(width = strokeWidth)
                                )
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                            }
                        ) {
                            DetailedQuestionResultCard2(question = tailQuestion)
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = PrimaryIndigo.copy(alpha = 0.1f),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp)
                            )
                            .clickable { onToggle() }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "꼬리질문 접기",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryIndigo,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.Filled.ExpandLess,
                                contentDescription = "접기",
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        } else {
            cardPositions.clear()
            repeat(group.tailQuestions.size) { cardPositions.add(0f) }
        }
    }
}

@Composable
private fun DetailedQuestionResultCard2(
    question: DetailedQuestionResult
) {
    VTCard(
        variant = CardVariant.Outlined
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
            
            Text(
                text = question.question,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Gray800
            )
            
            if (question.myAnswer.isNotEmpty()) {
                Column {
            Text(
                text = "학생 답변:",
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

            Column {
                Text(
                    text = "정답:",
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
