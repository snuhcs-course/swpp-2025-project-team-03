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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.theme.Gray800
import com.example.voicetutor.ui.utils.ErrorMessageMapper
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.utils.formatDueDate

@Composable
fun AssignmentDetailScreen(
    assignmentId: Int? = null, // PersonalAssignment ID 사용
    assignmentTitle: String? = null, // 실제 과제 제목 사용
    onStartAssignment: () -> Unit = {},
    assignmentViewModelParam: AssignmentViewModel? = null,
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

        // selectedAssignmentId가 있으면 Assignment 메타데이터 로드
        // 없으면 PersonalAssignment 통계만 로드 (Assignment 메타데이터는 선택사항)
        if (assignId != null) {
            println("AssignmentDetailScreen - Loading assignment meta by assignment.id: $assignId")
            assignmentViewModel.loadAssignmentById(assignId)
        } else {
            println("AssignmentDetailScreen - selectedAssignmentId is null, skipping loadAssignmentById")
            println("AssignmentDetailScreen - PersonalAssignment ID: $personalId")
        }

        // personal_assignment_id로 통계 로드
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header removed - now handled by MainLayout

        // Loading state
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = PrimaryIndigo)
            }
        }

        // Error state
        if (error != null) {
            VTCard(
                variant = CardVariant.Outlined,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = ErrorMessageMapper.getErrorMessage(error),
                    color = Error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
            }
        }

        // Assignment info card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = PrimaryIndigo.copy(alpha = 0.08f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                )
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 14.dp),
        ) {
            // Left side: Title and subject/class info
            Column(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = actualTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800,
                    maxLines = 1,
                    modifier = Modifier.widthIn(max = 180.dp),
                )
                Spacer(modifier = Modifier.height(10.dp))
                
                // Subject and Class chips
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val subject = currentAssignment?.courseClass?.subject?.name
                    val className = currentAssignment?.courseClass?.name
                    
                    if (!subject.isNullOrBlank()) {
                        Surface(
                            color = PrimaryIndigo.copy(alpha = 0.12f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                            modifier = Modifier.widthIn(max = 120.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Book,
                                    contentDescription = null,
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = subject,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryIndigo,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                    
                    if (!className.isNullOrBlank()) {
                        Surface(
                            color = PrimaryEmerald.copy(alpha = 0.12f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(6.dp),
                            modifier = Modifier.widthIn(max = 120.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Group,
                                    contentDescription = null,
                                    tint = PrimaryEmerald,
                                    modifier = Modifier.size(14.dp),
                                )
                                Text(
                                    text = className,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryEmerald,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
            
            // Right side: Due date badge (absolute positioned)
            currentAssignment?.dueAt?.let { dueDate ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 5.dp, y = (-3).dp)
                ) {
                    Surface(
                        color = PrimaryIndigo.copy(alpha = 0.7f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color.White,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = formatDueDate(dueDate),
                                style = MaterialTheme.typography.bodySmall,
                                color = androidx.compose.ui.graphics.Color.White,
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }
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
                    color = Gray800,
                )
                Spacer(modifier = Modifier.height(12.dp))

                VTProgressBar(
                    progress = if ((personalAssignmentStatistics?.totalProblem ?: 0) > 0) {
                        (personalAssignmentStatistics?.solvedProblem ?: 0).toFloat() / (personalAssignmentStatistics?.totalProblem ?: 1).toFloat()
                    } else {
                        0f
                    },
                    showPercentage = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${personalAssignmentStatistics?.totalProblem ?: 0}개 중 ${personalAssignmentStatistics?.solvedProblem ?: 0}개 완료",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
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
                    color = Gray800,
                )

                Spacer(modifier = Modifier.height(12.dp))

                val desc = currentAssignment?.description
                if (!desc.isNullOrBlank()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray700,
                        lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5,
                    )
                }
            }
        }

        // Instructions
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "과제 안내",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "• 음성으로 답변을 녹음해주세요\n• 각 단계별로 명확하게 설명해주세요\n• 5분 이내로 완료해주세요",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600,
                )
            }
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                        modifier = Modifier.size(20.dp),
                    )
                },
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
