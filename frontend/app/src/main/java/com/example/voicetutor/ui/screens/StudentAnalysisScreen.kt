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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.viewmodel.AnalysisViewModel

@Composable
fun StudentAnalysisScreen(
    studentId: Int = 1,
    onNavigateBack: () -> Unit = {}
) {
    val analysisViewModel: AnalysisViewModel = hiltViewModel()
    val studentAnalysis by analysisViewModel.studentAnalysis.collectAsStateWithLifecycle()
    val isLoading by analysisViewModel.isLoading.collectAsStateWithLifecycle()
    val error by analysisViewModel.error.collectAsStateWithLifecycle()
    
    // Load analysis data on first composition
    LaunchedEffect(studentId) {
        analysisViewModel.loadStudentAnalysis(studentId)
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            analysisViewModel.clearError()
        }
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryIndigo)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            VTHeader(
                title = "학습 분석",
                onBackClick = onNavigateBack
            )
            
            studentAnalysis?.let { analysis ->
                // Overall Performance
                VTCard(variant = CardVariant.Elevated) {
                    Column {
                        Text(
                            text = "전체 성과",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            VTStatsCard(
                                title = "평균 점수",
                                value = "${analysis.averageScore.toInt()}점",
                                icon = Icons.Filled.Star,
                                iconColor = Success,
                                variant = CardVariant.Gradient,
                                modifier = Modifier.weight(1f)
                            )
                            
                            VTStatsCard(
                                title = "정확도",
                                value = "${analysis.accuracyRate.toInt()}%",
                                icon = Icons.Filled.GpsFixed,
                                iconColor = PrimaryIndigo,
                                variant = CardVariant.Gradient,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            VTStatsCard(
                                title = "개선율",
                                value = "+${analysis.improvementRate.toInt()}%",
                                icon = Icons.Filled.TrendingUp,
                                iconColor = Warning,
                                variant = CardVariant.Gradient,
                                modifier = Modifier.weight(1f)
                            )
                            
                            VTStatsCard(
                                title = "학습 시간",
                                value = "${analysis.studyTime}분",
                                icon = Icons.Filled.Schedule,
                                iconColor = PrimaryEmerald,
                                variant = CardVariant.Gradient,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                // Strengths and Weaknesses
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Strengths
                    VTCard(
                        variant = CardVariant.Elevated,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ThumbUp,
                                    contentDescription = null,
                                    tint = Success,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "강점",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Gray800
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            analysis.strengths.forEach { strength ->
                                Text(
                                    text = "• $strength",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray600,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    // Weaknesses
                    VTCard(
                        variant = CardVariant.Elevated,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ThumbDown,
                                    contentDescription = null,
                                    tint = Error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "개선점",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Gray800
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            analysis.weaknesses.forEach { weakness ->
                                Text(
                                    text = "• $weakness",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Gray600,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
                
                // Recommendations
                VTCard(variant = CardVariant.Elevated) {
                    Column {
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
                                text = "학습 권장사항",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Gray800
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        analysis.recommendations.forEachIndexed { index, recommendation ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${index + 1}. ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = PrimaryIndigo
                                )
                                Text(
                                    text = recommendation,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Gray700
                                )
                            }
                        }
                    }
                }
                
                // Assignment Progress
                VTCard(variant = CardVariant.Elevated) {
                    Column {
                        Text(
                            text = "과제 진행률",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "완료된 과제",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray600
                            )
                            Text(
                                text = "${analysis.completedAssignments}/${analysis.totalAssignments}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = Gray800
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val progress = if (analysis.totalAssignments > 0) {
                            analysis.completedAssignments.toFloat() / analysis.totalAssignments
                        } else 0f
                        
                        LinearProgressIndicator(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(MaterialTheme.shapes.small),
                            color = PrimaryIndigo,
                            trackColor = Gray200
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "${(progress * 100).toInt()}% 완료",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                    }
                }
            } ?: run {
                // No analysis data
                VTCard(variant = CardVariant.Elevated) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Analytics,
                            contentDescription = null,
                            tint = Gray400,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "분석 데이터가 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudentAnalysisScreenPreview() {
    VoiceTutorTheme {
        StudentAnalysisScreen()
    }
}
