package com.example.voicetutor.ui.screens

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel

@Composable
fun TeacherStudentAssignmentDetailScreen(
    assignmentId: Int = 1, // 임시로 기본값 설정
    studentId: String = "2024001", // 임시로 기본값 설정
    assignmentTitle: String = "과제" // TODO: 실제 과제 제목으로 동적 설정
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val studentViewModel: com.example.voicetutor.ui.viewmodel.StudentViewModel = hiltViewModel()
    val assignmentResults by viewModel.assignmentResults.collectAsStateWithLifecycle()
    val currentAssignment by viewModel.currentAssignment.collectAsStateWithLifecycle()
    val currentStudent by studentViewModel.currentStudent.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    // 동적 학생 이름 가져오기
    val studentName = currentStudent?.name ?: "학생"
    
    // 동적 과제 제목 가져오기
    val dynamicAssignmentTitle = currentAssignment?.title ?: assignmentTitle
    
    // Load assignment data, student data and results on first composition
    LaunchedEffect(assignmentId, studentId) {
        viewModel.loadAssignmentById(assignmentId)
        viewModel.loadAssignmentResults(assignmentId)
        studentViewModel.loadStudentById(studentId.toIntOrNull() ?: 1)
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            viewModel.clearError()
        }
    }
    
    // Find student's result
    val studentResult = assignmentResults.find { it.studentId == studentId }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header removed - now handled by MainLayout
        
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
                        text = "학생 결과를 찾을 수 없습니다",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Gray600
                    )
                }
            }
        } else {
            // Student info card - Welcome section style
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = studentResult.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${studentResult.studentId} • ${studentResult.score}점",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                        .shadow(
                            elevation = 4.dp,
                            shape = androidx.compose.foundation.shape.CircleShape,
                            ambientColor = Color.Black.copy(alpha = 0.1f),
                            spotColor = Color.Black.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = studentResult.name.first().toString(),
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Overall stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            VTStatsCard(
                title = "정답률",
                value = "${(studentResult.detailedAnswers.count { it.isCorrect }.toFloat() / studentResult.detailedAnswers.size * 100).toInt()}%",
                icon = Icons.Filled.CheckCircle,
                iconColor = Success,
                variant = CardVariant.Elevated,
                modifier = Modifier.weight(1f),
                layout = StatsCardLayout.Vertical
            )
            
            VTStatsCard(
                title = "평균 확실성",
                value = "${studentResult.detailedAnswers.map { it.confidenceScore }.average().toInt()}점",
                icon = Icons.Filled.Speed,
                iconColor = PrimaryIndigo,
                variant = CardVariant.Elevated,
                modifier = Modifier.weight(1f),
                layout = StatsCardLayout.Vertical
            )
        }
        
        // Detailed answers
        Column {
            Text(
                text = "상세 답변 분석",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray800
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            studentResult.detailedAnswers.forEach { answer ->
                VTCard(
                    variant = CardVariant.Elevated
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "질문: ${answer.question}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "답변: ${answer.studentAnswer}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Pronunciation Score
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "발음 점수",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray600
                                )
                                Text(
                                    text = "${answer.pronunciationScore ?: 0}점",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        (answer.pronunciationScore ?: 0) >= 80 -> Success
                                        (answer.pronunciationScore ?: 0) >= 60 -> Warning
                                        else -> Error
                                    }
                                )
                            }
                        }
                    }
                }
                if (answer != studentResult.detailedAnswers.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun DetailedAnswerCard(
    answer: DetailedAnswer
) {
    VTCard(
        variant = CardVariant.Elevated
    ) {
        Column {
            // Question header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "문제 ${answer.questionNumber}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryIndigo
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Correctness badge
                    Box(
                        modifier = Modifier
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                            .background(
                                if (answer.isCorrect) Success.copy(alpha = 0.1f) 
                                else Error.copy(alpha = 0.1f)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (answer.isCorrect) "정답" else "오답",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (answer.isCorrect) Success else Error,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Confidence score
                    Text(
                        text = "확실성: ${answer.confidenceScore}점",
                        style = MaterialTheme.typography.bodySmall,
                        color = when {
                            answer.confidenceScore >= 80 -> Success
                            answer.confidenceScore >= 60 -> Warning
                            else -> Error
                        },
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Question
            Text(
                text = "질문:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Gray600
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = answer.question,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray800,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Student answer
            Text(
                text = "학생 답변:",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = Gray600
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = answer.studentAnswer,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray800,
                lineHeight = 20.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Correct answer (if wrong)
            if (!answer.isCorrect) {
                Text(
                    text = "정답:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Gray600
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = answer.correctAnswer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Success,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Performance metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "발음 점수",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = "${answer.pronunciationScore ?: 0}점",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            (answer.pronunciationScore ?: 0) >= 80 -> Success
                            (answer.pronunciationScore ?: 0) >= 60 -> Warning
                            else -> Error
                        }
                    )
                }
                
                Column {
                    Text(
                        text = "응답 시간",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    Text(
                        text = answer.responseTime,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gray800
                    )
                }
            }
        }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherStudentAssignmentDetailScreenPreview() {
    VoiceTutorTheme {
        TeacherStudentAssignmentDetailScreen()
    }
}
