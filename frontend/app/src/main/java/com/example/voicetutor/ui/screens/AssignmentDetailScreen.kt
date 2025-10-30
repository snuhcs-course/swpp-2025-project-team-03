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
    onStartAssignment: () -> Unit = {}
) {
    val assignmentViewModel: AssignmentViewModel = hiltViewModel()
    val currentAssignment by assignmentViewModel.currentAssignment.collectAsStateWithLifecycle()
    val personalAssignmentStatistics by assignmentViewModel.personalAssignmentStatistics.collectAsStateWithLifecycle()
    val isLoading by assignmentViewModel.isLoading.collectAsStateWithLifecycle()
    val error by assignmentViewModel.error.collectAsStateWithLifecycle()
    
    // 초안 저장 상태
    var showSaveDraftDialog by remember { mutableStateOf(false) }
    var draftContent by remember { mutableStateOf("") }
    
    // Load assignment data and statistics
    LaunchedEffect(assignmentId) {
        assignmentId?.let { id ->
            println("AssignmentDetailScreen - Loading assignment details for PersonalAssignment ID: $id")
            assignmentViewModel.loadAssignmentById(id)
            assignmentViewModel.loadPersonalAssignmentStatistics(id)
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
                    Text(
                        text = "생물학 - 오늘 23:59 마감",
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
                
                Text(
                    text = "세포분열 과정을 단계별로 설명하고, 각 단계에서 일어나는 주요 변화들을 정리해보세요. 또한 세포분열의 의의와 생물학적 중요성에 대해서도 서술해주세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700,
                    lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.5
                )
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            VTButton(
                text = "과제 시작",
                onClick = onStartAssignment,
                variant = ButtonVariant.Gradient,
                modifier = Modifier.weight(1f),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
            
            VTButton(
                text = "임시저장",
                onClick = { showSaveDraftDialog = true },
                variant = ButtonVariant.Outline,
                modifier = Modifier.weight(1f),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            )
        }
        
        // 초안 저장 다이얼로그
        if (showSaveDraftDialog) {
            AlertDialog(
                onDismissRequest = { showSaveDraftDialog = false },
                title = {
                    Text(
                        text = "초안 저장",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "현재까지 작성한 내용을 초안으로 저장하시겠습니까?",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = draftContent,
                            onValueChange = { draftContent = it },
                            label = { Text("초안 메모 (선택사항)") },
                            placeholder = { Text("초안에 대한 간단한 메모를 입력하세요") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                    }
                },
                confirmButton = {
                    VTButton(
                        text = "저장",
                        onClick = {
                            // 실제 초안 저장 API 호출
                            assignmentViewModel.saveAssignmentDraft(
                                assignmentId = assignmentId ?: 1,
                                draftContent = draftContent
                            )
                            showSaveDraftDialog = false
                        },
                        variant = ButtonVariant.Primary,
                        size = ButtonSize.Small
                    )
                },
                dismissButton = {
                    VTButton(
                        text = "취소",
                        onClick = { showSaveDraftDialog = false },
                        variant = ButtonVariant.Outline,
                        size = ButtonSize.Small
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
