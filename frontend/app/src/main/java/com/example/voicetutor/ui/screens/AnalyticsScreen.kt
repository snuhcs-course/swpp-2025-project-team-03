package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.viewmodel.StudentViewModel

@Composable
fun AnalyticsScreen(
    classId: Int = 1,
    teacherId: String = "1",
    onBackClick: () -> Unit = {}
) {
    val studentViewModel: StudentViewModel = hiltViewModel()
    val apiStudents by studentViewModel.students.collectAsStateWithLifecycle()
    val isLoading by studentViewModel.isLoading.collectAsStateWithLifecycle()
    
    var selectedPeriod by remember { mutableStateOf("주간") }
    var selectedSubject by remember { mutableStateOf("전체") }
    
    // Load students on first composition
    LaunchedEffect(teacherId) {
        studentViewModel.loadAllStudents(teacherId = teacherId)
    }
    
    val periods = listOf("주간", "월간", "학기")
    val subjects = listOf("전체", "영어", "수학", "과학", "국어")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        VTHeader(
            title = "성과 분석",
            onBackClick = onBackClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Filter options
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "분석 옵션",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Period selection
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "기간",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = selectedPeriod,
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { 
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    
                    // Subject selection
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "과목",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = selectedSubject,
                            onValueChange = { },
                            readOnly = true,
                            trailingIcon = { 
                                Icon(
                                    imageVector = Icons.Filled.ArrowDropDown,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Overall statistics
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VTStatsCard(
                title = "평균 점수",
                value = "85점",
                icon = Icons.Filled.Star,
                iconColor = Warning,
                trend = TrendDirection.Up,
                trendValue = "+5점",
                modifier = Modifier.weight(1f)
            )
            
            VTStatsCard(
                title = "완료율",
                value = "92%",
                icon = Icons.Filled.Done,
                iconColor = Success,
                trend = TrendDirection.Up,
                trendValue = "+3%",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VTStatsCard(
                title = "참여도",
                value = "88%",
                icon = Icons.Filled.People,
                iconColor = PrimaryIndigo,
                trend = TrendDirection.Up,
                trendValue = "+2%",
                modifier = Modifier.weight(1f)
            )
            
            VTStatsCard(
                title = "개선도",
                value = "15%",
                icon = Icons.Filled.TrendingUp,
                iconColor = Success,
                trend = TrendDirection.Up,
                trendValue = "+8%",
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Top performers
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "우수 학생",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = Warning,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // API에서 가져온 학생 데이터를 점수순으로 정렬
                val topStudents = remember(apiStudents) {
                    apiStudents
                        .sortedByDescending { it.averageScore }
                        .take(5)
                        .map { student ->
                            TopStudent(
                                name = student.name,
                                score = student.averageScore,
                                improvement = "+${kotlin.random.Random.nextInt(1, 10)}점" // 실제 개선 데이터는 API에 없음
                            )
                        }
                }
                
                if (topStudents.isEmpty() && isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryIndigo,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else if (topStudents.isEmpty()) {
                    Text(
                        text = "학생 데이터가 없습니다",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                
                topStudents.forEach { student ->
                    TopStudentItem(
                        name = student.name,
                        score = student.score,
                        improvement = student.improvement
                    )
                    if (student != topStudents.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Areas needing attention
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "관심 필요 학생",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = null,
                        tint = Error,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val attentionStudents = listOf(
                    AttentionStudent("한학생", 65, "발음 연습 필요"),
                    AttentionStudent("서학생", 68, "참여도 향상 필요"),
                    AttentionStudent("강학생", 72, "과제 완료율 개선")
                )
                
                attentionStudents.forEach { student ->
                    AttentionStudentItem(
                        name = student.name,
                        score = student.score,
                        issue = student.issue
                    )
                    if (student != attentionStudents.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Subject breakdown
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "과목별 성과",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val subjects = listOf(
                    SubjectPerformance("영어", 88, 95),
                    SubjectPerformance("수학", 82, 90),
                    SubjectPerformance("과학", 85, 88),
                    SubjectPerformance("국어", 90, 92)
                )
                
                subjects.forEach { subject ->
                    SubjectPerformanceItem(
                        subject = subject.subject,
                        averageScore = subject.averageScore,
                        completionRate = subject.completionRate
                    )
                    if (subject != subjects.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TopStudentItem(
    name: String,
    score: Int,
    improvement: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = PrimaryIndigo,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray800
            )
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "${score}점",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Gray800
            )
            Text(
                text = improvement,
                style = MaterialTheme.typography.bodySmall,
                color = Success
            )
        }
    }
}

@Composable
fun AttentionStudentItem(
    name: String,
    score: Int,
    issue: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = Error,
                modifier = Modifier.size(16.dp)
            )
            Column {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray800
                )
                Text(
                    text = issue,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
            }
        }
        
        Text(
            text = "${score}점",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Error
        )
    }
}

@Composable
fun SubjectPerformanceItem(
    subject: String,
    averageScore: Int,
    completionRate: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = subject,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray800
        )
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "평균 ${averageScore}점",
                style = MaterialTheme.typography.bodySmall,
                color = Gray600
            )
            Text(
                text = "완료율 ${completionRate}%",
                style = MaterialTheme.typography.bodySmall,
                color = Gray600
            )
        }
    }
}

data class TopStudent(
    val name: String,
    val score: Int,
    val improvement: String
)

data class AttentionStudent(
    val name: String,
    val score: Int,
    val issue: String
)

data class SubjectPerformance(
    val subject: String,
    val averageScore: Int,
    val completionRate: Int
)
