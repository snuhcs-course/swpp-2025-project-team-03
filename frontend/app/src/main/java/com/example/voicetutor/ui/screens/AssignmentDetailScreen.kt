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
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel

@Composable
fun AssignmentDetailScreen(
    assignmentId: Int? = null, // PersonalAssignment ID 사용
    assignmentTitle: String? = null, // 실제 과제 제목 사용
    onStartAssignment: () -> Unit = {},
    assignmentViewModelParam: com.example.voicetutor.ui.viewmodel.AssignmentViewModel? = null
) {
    val assignmentViewModel: AssignmentViewModel = assignmentViewModelParam ?: hiltViewModel()
    val currentAssignment by assignmentViewModel.currentAssignment.collectAsStateWithLifecycle()
    val personalAssignmentStatistics by assignmentViewModel.personalAssignmentStatistics.collectAsStateWithLifecycle()
    val isLoading by assignmentViewModel.isLoading.collectAsStateWithLifecycle()
    val error by assignmentViewModel.error.collectAsStateWithLifecycle()
    val selectedAssignmentId by assignmentViewModel.selectedAssignmentId.collectAsStateWithLifecycle()
    val selectedPersonalAssignmentId by assignmentViewModel.selectedPersonalAssignmentId.collectAsStateWithLifecycle()
    
    
    
    // Load assignment data and statistics
    LaunchedEffect(assignmentId, selectedAssignmentId, selectedPersonalAssignmentId) {
        // 우선순위: ViewModel에 저장된 선택값 → 네비게이션 파라미터
        val personalId = selectedPersonalAssignmentId ?: assignmentId
        val assignId = selectedAssignmentId
        if (assignId != null) {
            println("AssignmentDetailScreen - Loading assignment meta by assignment.id: $assignId")
            assignmentViewModel.loadAssignmentById(assignId)
        }
        personalId?.let { pid ->
            println("AssignmentDetailScreen - Loading statistics by personal_assignment.id: $pid")
            assignmentViewModel.loadPersonalAssignmentStatistics(pid)
        }
    }
    
    // Debug statistics data
    LaunchedEffect(personalAssignmentStatistics) {
        println("AssignmentDetailScreen - PersonalAssignmentStatistics updated:")
        println("  - progress: ${personalAssignmentStatistics?.progress}")
        println("  - totalProblem: ${personalAssignmentStatistics?.totalProblem}")
        println("  - solvedProblem: ${personalAssignmentStatistics?.solvedProblem}")
        println("  - totalQuestions: ${personalAssignmentStatistics?.totalQuestions}")
        println("  - answeredQuestions: ${personalAssignmentStatistics?.answeredQuestions}")
        println("  - correctAnswers: ${personalAssignmentStatistics?.correctAnswers}")
        println("  - accuracy: ${personalAssignmentStatistics?.accuracy}")
    }
    
    // Use actual assignment title or fallback
    val actualTitle = currentAssignment?.title ?: assignmentTitle ?: "과제"
    // Format subject and due date from API instead of dummy text
    fun formatDueDate(due: String?): String {
        return try {
            if (due == null) return ""
            java.time.ZonedDateTime.parse(due).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
        } catch (e: Exception) { due ?: "" }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header removed - now handled by MainLayout
        
        // Loading state
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryIndigo)
            }
        }
        
        // Error state
        if (error != null) {
            VTCard(
                variant = CardVariant.Outlined,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = error ?: "",
                    color = Error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        // Assignment info card
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
                        text = actualTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val subtitle = buildString {
                        val subject = currentAssignment?.courseClass?.subject?.name
                        val due = formatDueDate(currentAssignment?.dueAt)
                        if (!subject.isNullOrBlank()) append(subject)
                        if (!subject.isNullOrBlank() && due.isNotBlank()) append(" · ")
                        if (due.isNotBlank()) append("마감: ").append(due)
                    }
                    if (subtitle.isNotBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
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
                        text = if (personalAssignmentStatistics?.totalQuestions ?: 0 > 0) {
                            "${((personalAssignmentStatistics?.answeredQuestions ?: 0).toFloat() / (personalAssignmentStatistics?.totalQuestions ?: 1).toFloat() * 100f).toInt()}%"
                        } else {
                            "0%"
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Progress section
        VTCard(variant = CardVariant.Elevated) {
            Column {
                Text(
                    text = "진행 현황",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                VTProgressBar(
                    progress = if (personalAssignmentStatistics?.totalQuestions ?: 0 > 0) {
                        (personalAssignmentStatistics?.answeredQuestions ?: 0).toFloat() / (personalAssignmentStatistics?.totalQuestions ?: 1).toFloat()
                    } else {
                        0f
                    },
                    showPercentage = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${personalAssignmentStatistics?.totalQuestions ?: 0}개 중 ${personalAssignmentStatistics?.answeredQuestions ?: 0}개 완료",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
            }
        }
        
        // Assignment content
        VTCard(variant = CardVariant.Elevated) {
            Column {
                Text(
                    text = "과제 내용",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                val desc = currentAssignment?.description
                if (!desc.isNullOrBlank()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray700,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
                    )
                }
            }
        }
        
        // Instructions
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "과제 안내",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "• 음성으로 답변을 녹음해주세요\n• 각 단계별로 명확하게 설명해주세요\n• 5분 이내로 완료해주세요",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
            }
        }
        
        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            VTButton(
                text = "과제 시작",
                onClick = onStartAssignment,
                variant = ButtonVariant.Gradient,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AssignmentDetailScreenPreview() {
    VoiceTutorTheme {
        AssignmentDetailScreen()
    }
}
