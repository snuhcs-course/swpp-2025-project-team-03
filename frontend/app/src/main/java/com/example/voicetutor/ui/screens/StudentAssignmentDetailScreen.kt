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


@Composable
fun StudentAssignmentDetailScreen(
    assignmentId: Int? = null,
    studentId: Int? = null,
    assignmentTitle: String = "과제",
    onBackClick: () -> Unit = {},
    onNavigateToDetailedResults: () -> Unit = {}
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val assignmentResults by viewModel.assignmentResults.collectAsStateWithLifecycle()
    val currentAssignment by viewModel.currentAssignment.collectAsStateWithLifecycle()
    val personalAssignmentStatistics by viewModel.personalAssignmentStatistics.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    // 동적 과제 제목 가져오기
    val dynamicAssignmentTitle = currentAssignment?.title ?: assignmentTitle
    
    // Load assignment data and personal assignment statistics on first composition
    LaunchedEffect(assignmentId) {
        if (assignmentId != null) {
            println("StudentAssignmentDetail - Loading assignment: $assignmentId")
            viewModel.loadAssignmentById(assignmentId)
            // 개인 과제 통계 로드 (personalAssignmentId 사용)
            viewModel.loadPersonalAssignmentStatistics(assignmentId)
        }
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            viewModel.clearError()
        }
    }
    
    // Use personal assignment statistics
    val stats = personalAssignmentStatistics
    
    // Calculate derived values from statistics
    val totalQuestions = stats?.totalQuestions ?: 0
    val answeredQuestions = stats?.answeredQuestions ?: 0
    val correctAnswers = stats?.correctAnswers ?: 0
    val accuracy = stats?.accuracy ?: 0f
    val progress = stats?.progress ?: 0f
    
    // 정답률 계산: 정답 수 / 응답한 문제 수 * 100
    // API에서 받은 accuracy 값이 이상하면 직접 계산
    val calculatedAccuracy = if (answeredQuestions > 0) {
        (correctAnswers.toFloat() / answeredQuestions.toFloat() * 100f).toInt()
    } else {
        0
    }
    
    // totalScore는 0~100 사이로 제한
    val totalScore = if (calculatedAccuracy > 100) 100 else calculatedAccuracy
    
    // 진행률 계산: 답변한 문제 / 총 문제 수 * 100
    val calculatedProgress = if (totalQuestions > 0) {
        (answeredQuestions.toFloat() / totalQuestions.toFloat() * 100f).toInt()
    } else {
        0
    }
    
    // 진행률을 0~100 사이로 제한
    val displayProgress = if (calculatedProgress > 100) 100 else calculatedProgress
    val averageConfidence = 0f // API에서 제공하지 않으므로 0으로 설정
    
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
        } else if (stats == null) {
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
                        text = "과제 통계를 찾을 수 없습니다",
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
                    title = "정답률",
                    value = "${totalScore}%",
                    icon = Icons.Filled.Grade,
                    iconColor = if (totalScore >= 80) Success else Warning,
                    modifier = Modifier.weight(1f),
                    variant = CardVariant.Gradient
                )
                
                VTStatsCard(
                    title = "진행률",
                    value = "${displayProgress}%",
                    icon = Icons.Filled.CheckCircle,
                    iconColor = if (displayProgress >= 80) Success else Warning,
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
                    title = "총 문제",
                    value = "${totalQuestions}개",
                    icon = Icons.Filled.Quiz,
                    iconColor = PrimaryIndigo,
                    modifier = Modifier.weight(1f),
                    variant = CardVariant.Elevated
                )
                
                VTStatsCard(
                    title = "정답 수",
                    value = "${correctAnswers}개",
                    icon = Icons.Filled.Psychology,
                    iconColor = if (correctAnswers >= totalQuestions * 0.8) Success else Warning,
                    modifier = Modifier.weight(1f),
                    variant = CardVariant.Elevated
                )
            }
            
            // Assignment summary
            Column {
                Text(
                    text = "과제 요약",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                VTCard(
                    variant = CardVariant.Outlined
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "총 문제 수",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray600
                            )
                            Text(
                                text = "${totalQuestions}개",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray800
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "답변한 문제",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray600
                            )
                            Text(
                                text = "${answeredQuestions}개",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray800
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "정답 수",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray600
                            )
                            Text(
                                text = "${correctAnswers}개",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (correctAnswers > 0) Success else Gray800
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "정답률",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray600
                            )
                            Text(
                                text = "${totalScore}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (totalScore >= 80) Success else if (totalScore >= 60) Warning else Error
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Progress bar
                        VTProgressBar(
                            progress = displayProgress / 100f,
                            showPercentage = true,
                            color = if (displayProgress >= 80) Success else if (displayProgress >= 60) Warning else PrimaryIndigo
                        )
                    }
                }
            }
            
            // Detailed results button
            VTButton(
                text = "과제 결과 상세",
                onClick = onNavigateToDetailedResults,
                variant = ButtonVariant.Primary,
                fullWidth = true,
                size = ButtonSize.Large
            )
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