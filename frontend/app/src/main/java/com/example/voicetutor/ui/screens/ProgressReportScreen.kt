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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.ui.viewmodel.DashboardViewModel
import com.example.voicetutor.ui.viewmodel.AnalysisViewModel


@Composable
fun ProgressReportScreen(
    studentId: Int = 1, // 임시로 기본값 설정
    onNavigateToAssignmentDetail: (String) -> Unit = {}
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val dashboardViewModel: DashboardViewModel = hiltViewModel()
    val analysisViewModel: AnalysisViewModel = hiltViewModel()
    
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val dashboardStats by dashboardViewModel.dashboardStats.collectAsStateWithLifecycle()
    val studentAnalysis by analysisViewModel.studentAnalysis.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    // Load student assignments and dashboard data on first composition
    LaunchedEffect(studentId) {
        viewModel.loadAllAssignments()
        dashboardViewModel.loadDashboardData(studentId.toString())
        analysisViewModel.loadStudentAnalysis(studentId)
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            viewModel.clearError()
        }
    }
    
    // Generate weekly activity data (임시로 기본값 사용)
    val weeklyActivity = listOf(65, 45, 70, 60, 80, 75, 90)
    val days = listOf("월", "화", "수", "목", "금", "토", "일")
    
    // Get completed assignments from API (임시로 모든 과제를 완료된 것으로 처리)
    val completedAssignments = assignments.map { assignment ->
        assignment.title
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
        } else {
            // Header
            Column {
            Text(
                text = "과제 리포트",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = Gray800
            )
            Text(
                text = "과목별 과제 완료 현황을 확인해보세요",
                style = MaterialTheme.typography.bodySmall,
                color = Gray600
            )
        }
        
        // Stats overview
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VTStatsCard(
                title = "이번 달",
                value = dashboardStats?.totalAssignments?.toString() ?: "0",
                icon = Icons.Filled.DateRange,
                iconColor = PrimaryIndigo,
                variant = CardVariant.Gradient,
                trend = TrendDirection.Up,
                trendValue = studentAnalysis?.let { "+${it.completedAssignments}" } ?: "+0",
                modifier = Modifier.weight(1f)
            )
            
            VTStatsCard(
                title = "평균 정확도",
                value = studentAnalysis?.let { "${it.accuracyRate.toInt()}%" } ?: "0%",
                icon = Icons.Filled.Star,
                iconColor = Success,
                variant = CardVariant.Gradient,
                trend = TrendDirection.Up,
                trendValue = studentAnalysis?.let { "+${it.improvementRate.toInt()}%" } ?: "+0%",
                modifier = Modifier.weight(1f)
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VTStatsCard(
                title = "이번 주",
                value = dashboardStats?.completedAssignments?.toString() ?: "0",
                icon = Icons.Filled.EmojiEvents,
                iconColor = Warning,
                variant = CardVariant.Gradient,
                trend = TrendDirection.Up,
                trendValue = studentAnalysis?.let { "+${(it.completedAssignments * 0.3).toInt()}" } ?: "+0",
                modifier = Modifier.weight(1f)
            )
            
            VTStatsCard(
                title = "완료율",
                value = dashboardStats?.let { stats ->
                    if (stats.totalAssignments > 0) {
                        "${(stats.completedAssignments * 100 / stats.totalAssignments)}%"
                    } else "0%"
                } ?: "0%",
                icon = Icons.Filled.Done,
                iconColor = PrimaryEmerald,
                variant = CardVariant.Gradient,
                trend = TrendDirection.Up,
                trendValue = studentAnalysis?.let { "+${(it.improvementRate * 0.5).toInt()}%" } ?: "+0%",
                modifier = Modifier.weight(1f)
            )
        }
        
        // Weekly activity chart
        VTCard(variant = CardVariant.Elevated) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "주간 활동",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                        Text(
                            text = "이번 주 과제 완료 시간 (분)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray600
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Filled.TrendingUp,
                        contentDescription = null,
                        tint = Success,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Enhanced activity chart
                VTActivityChart(
                    data = weeklyActivity,
                    labels = days,
                    chartColor = ChartColor.Blue,
                    animated = true
                )
            }
        }
        
        
        // Completed assignments
        VTCard(variant = CardVariant.Elevated) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Assignment,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "완료된 과제",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    completedAssignments.forEach { assignment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 2.dp
                            ),
                            onClick = { onNavigateToAssignmentDetail(assignment) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Assignment,
                                    contentDescription = null,
                                    tint = Success,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = assignment,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Gray800
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(
                                    imageVector = Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    tint = Gray400,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Recent achievements
        VTCard(variant = CardVariant.Elevated) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = Warning,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "최근 성취",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AchievementItem(
                        title = "생물학 과제 연속 완료",
                        description = "5개 과제를 연속으로 완료했습니다",
                        icon = Icons.Filled.Biotech,
                        color = PrimaryIndigo
                    )
                    
                    AchievementItem(
                        title = "화학 고득점 달성",
                        description = "화학 과제에서 95점을 획득했습니다",
                        icon = Icons.Filled.Science,
                        color = Success
                    )
                    
                    AchievementItem(
                        title = "이번 주 목표 달성",
                        description = "주간 과제 완료 목표를 달성했습니다",
                        icon = Icons.Filled.CheckCircle,
                        color = PrimaryEmerald
                    )
                }
            }
        }
    }
}



@Composable
fun CompletedAssignmentItem(
    title: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Success.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Success,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Gray800
                    )
                    Text(
                        text = "완료됨",
                        style = MaterialTheme.typography.bodySmall,
                        color = Success
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Gray400,
                modifier = Modifier.size(20.dp)
            )
        }
        }
    }
}

@Composable
fun AchievementItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Gray800
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Gray600
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressReportScreenPreview() {
    VoiceTutorTheme {
        ProgressReportScreen()
    }
}
