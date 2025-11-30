package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.utils.ErrorMessageMapper
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import com.example.voicetutor.ui.viewmodel.ClassViewModel
import com.example.voicetutor.ui.viewmodel.ReportViewModel

private const val HEADER_ALPHA = 0.08f
private const val HEADER_CORNER_RADIUS = 16
private const val EMPTY_STATE_ICON_SIZE = 48
private const val HIGH_ACCURACY_THRESHOLD = 80.0
private const val MEDIUM_ACCURACY_THRESHOLD = 50.0
private const val PROGRESS_BAR_HEIGHT = 12
private const val PROGRESS_BAR_CORNER_RADIUS = 6
private const val GRADIENT_ALPHA = 0.8f

@Composable
fun TeacherStudentReportScreen(
    classId: Int,
    studentId: Int,
    studentName: String = "학생",
) {
    val reportViewModel: ReportViewModel = hiltViewModel()
    val classViewModel: ClassViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()

    val report by reportViewModel.curriculumReport.collectAsStateWithLifecycle()
    val isLoading by reportViewModel.isLoading.collectAsStateWithLifecycle()
    val error by reportViewModel.error.collectAsStateWithLifecycle()

    val classes by classViewModel.classes.collectAsStateWithLifecycle()
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()

    var selectedClassId by remember { mutableStateOf(classId) }
    var completionRate by remember { mutableStateOf(0f) }
    var isLoadingCompletion by remember { mutableStateOf(true) }

    LaunchedEffect(classId, currentUser) {
        if (classId == 0) {
            currentUser?.let { user ->
                classViewModel.loadClasses(user.id.toString())
            }
        } else {
            selectedClassId = classId
        }
    }

    LaunchedEffect(classes, classId) {
        if (classId == 0 && classes.isNotEmpty() && selectedClassId == 0) {
            selectedClassId = classes.first().id
        }
    }

    LaunchedEffect(selectedClassId, studentId) {
        if (selectedClassId > 0 && studentId > 0) {
            reportViewModel.loadCurriculumReport(selectedClassId, studentId)

            isLoadingCompletion = true
            classViewModel.loadClassStudentsStatistics(selectedClassId) { result ->
                result.onSuccess { stats ->
                    val studentStat = stats.students.find { it.studentId == studentId }
                    completionRate = studentStat?.completionRate ?: 0f
                    isLoadingCompletion = false
                }.onFailure {
                    completionRate = 0f
                    isLoadingCompletion = false
                }
            }
        }
    }

    // 네트워크 에러가 아닌 경우에만 에러를 클리어합니다.
    // 네트워크 에러는 report == null일 때 구분하기 위해 유지합니다.
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            if (!ErrorMessageMapper.isNetworkError(errorMessage)) {
                reportViewModel.clearError()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Gray50,
                        Color(0xFFF0F4FF),
                        Color(0xFFF0F0FF),
                    ),
                ),
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = PrimaryIndigo.copy(alpha = HEADER_ALPHA),
                        shape = RoundedCornerShape(HEADER_CORNER_RADIUS.dp),
                    )
                    .padding(20.dp),
            ) {
                Column {
                    Text(
                        text = "${studentName}님의 성취기준 리포트",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "성취기준별 학습 현황을 확인하세요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600,
                    )
                }
            }
        }

        if (classId == 0 && classes.isNotEmpty()) {
            item {
                VTCard(
                    variant = CardVariant.Elevated,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = "수업 선택",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Gray800,
                        )
                        classes.forEach { classData ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(
                                    text = classData.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Gray700,
                                )
                                RadioButton(
                                    selected = selectedClassId == classData.id,
                                    onClick = {
                                        selectedClassId = classData.id
                                        if (selectedClassId > 0 && studentId > 0) {
                                            reportViewModel.loadCurriculumReport(selectedClassId, studentId)
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = PrimaryIndigo)
                }
            }
        } else if (error != null) {
            item {
                VTCard(
                    variant = CardVariant.Elevated,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = null,
                            tint = Error,
                            modifier = Modifier.size(EMPTY_STATE_ICON_SIZE.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "리포트를 불러올 수 없습니다",
                            style = MaterialTheme.typography.titleMedium,
                            color = Gray800,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = ErrorMessageMapper.getErrorMessage(error),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600,
                        )
                    }
                }
            }
        } else if (report != null) {
            val reportData = report!!

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    VTStatsCard(
                        title = "전체 문제",
                        value = "${reportData.totalQuestions}개",
                        icon = Icons.Filled.Quiz,
                        iconColor = PrimaryIndigo,
                        modifier = Modifier.weight(1f),
                        variant = CardVariant.Gradient,
                        layout = StatsCardLayout.Vertical,
                    )

                    VTStatsCard(
                        title = "정답 수",
                        value = "${reportData.totalCorrect}개",
                        icon = Icons.Filled.CheckCircle,
                        iconColor = Success,
                        modifier = Modifier.weight(1f),
                        variant = CardVariant.Gradient,
                        layout = StatsCardLayout.Vertical,
                    )

                    VTStatsCard(
                        title = "과제 완료율",
                        value = if (isLoadingCompletion) "..." else "${completionRate.toInt()}%",
                        icon = Icons.Filled.Done,
                        iconColor = Warning,
                        modifier = Modifier.weight(1f),
                        variant = CardVariant.Gradient,
                        layout = StatsCardLayout.Vertical,
                    )
                }
            }

            item {
                VTCard(
                    variant = CardVariant.Elevated,
                    modifier = Modifier.fillMaxWidth(),
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
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Assessment,
                                    contentDescription = null,
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(20.dp),
                                )
                                Text(
                                    text = "전체 정답률",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Gray800,
                                )
                            }
                            Text(
                                text = "${String.format("%.1f", reportData.overallAccuracy)}%",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryIndigo,
                            )
                        }
                        VTProgressBar(
                            progress = ((reportData.overallAccuracy / 100.0).coerceIn(0.0, 1.0)).toFloat(),
                            showPercentage = false,
                            color = PrimaryIndigo,
                            height = PROGRESS_BAR_HEIGHT,
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "성취기준별 분석",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                    )

                    Text(
                        text = "${reportData.achievementStatistics.size}개",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PrimaryIndigo,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            if (reportData.achievementStatistics.isEmpty()) {
                item {
                    VTCard(
                        variant = CardVariant.Elevated,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Info,
                                    contentDescription = null,
                                    tint = Gray400,
                                    modifier = Modifier.size(48.dp),
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "제출한 과제가 없습니다",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Gray600,
                                )
                            }
                        }
                    }
                }
            } else {
                items(
                    items = reportData.achievementStatistics.entries.toList(),
                    key = { it.key },
                ) { (achievementCode, statistics) ->
                    AchievementStatisticCard(
                        achievementCode = achievementCode,
                        statistics = statistics,
                    )
                }
            }
        } else {
            // report == null일 때 네트워크 에러인지 확인
            val isNetworkErrorState = error != null && ErrorMessageMapper.isNetworkError(error)
            val emptyStateMessage = if (isNetworkErrorState) {
                "네트워크가 불안정합니다"
            } else {
                "리포트 데이터가 없습니다"
            }

            item {
                VTCard(
                    variant = CardVariant.Elevated,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Assessment,
                                contentDescription = null,
                                tint = Gray400,
                                modifier = Modifier.size(48.dp),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = emptyStateMessage,
                                style = MaterialTheme.typography.bodyLarge,
                                color = Gray600,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementStatisticCard(
    achievementCode: String,
    statistics: AchievementStatistics,
) {
    val accuracy = statistics.accuracy
    val progress = ((accuracy / 100.0).coerceIn(0.0, 1.0)).toFloat()
    val isHighAccuracy = accuracy >= HIGH_ACCURACY_THRESHOLD
    val isMediumAccuracy = accuracy >= MEDIUM_ACCURACY_THRESHOLD && accuracy < HIGH_ACCURACY_THRESHOLD

    val progressColor = when {
        isHighAccuracy -> Success
        isMediumAccuracy -> Warning
        else -> Error
    }

    val statusText = when {
        isHighAccuracy -> "✅"
        isMediumAccuracy -> "⚠️"
        else -> "❌"
    }

    VTCard(
        variant = CardVariant.Elevated,
        modifier = Modifier.fillMaxWidth(),
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
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Label,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = "성취기준: $achievementCode",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo,
                    )
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Description,
                    contentDescription = null,
                    tint = Gray600,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = statistics.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "정답률",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                    )
                    Text(
                        text = "${String.format("%.1f", accuracy)}% (${statistics.correctQuestions}/${statistics.totalQuestions})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = progressColor,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                ) {
                    Text(
                        text = "문제 수",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                    )
                    Text(
                        text = "${statistics.totalQuestions}개",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = Gray800,
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(PROGRESS_BAR_HEIGHT.dp)
                    .clip(RoundedCornerShape(PROGRESS_BAR_CORNER_RADIUS.dp))
                    .background(Gray200),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .clip(RoundedCornerShape(PROGRESS_BAR_CORNER_RADIUS.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    progressColor,
                                    progressColor.copy(alpha = GRADIENT_ALPHA),
                                ),
                            ),
                        ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherStudentReportScreenPreview() {
    VoiceTutorTheme {
        TeacherStudentReportScreen(
            classId = 1,
            studentId = 1,
            studentName = "홍길동",
        )
    }
}
