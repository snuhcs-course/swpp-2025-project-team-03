package com.example.voicetutor.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
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
    assignmentId: Int? = null,
    assignmentTitle: String? = null,
    onStartAssignment: () -> Unit = {},
    assignmentViewModelParam: AssignmentViewModel? = null,
) {
    val context = LocalContext.current
    val assignmentViewModel: AssignmentViewModel = assignmentViewModelParam ?: hiltViewModel()
    val currentAssignment by assignmentViewModel.currentAssignment.collectAsStateWithLifecycle()
    val personalAssignmentStatistics by assignmentViewModel.personalAssignmentStatistics.collectAsStateWithLifecycle()
    val isLoading by assignmentViewModel.isLoading.collectAsStateWithLifecycle()
    val error by assignmentViewModel.error.collectAsStateWithLifecycle()
    val selectedAssignmentId by assignmentViewModel.selectedAssignmentId.collectAsStateWithLifecycle()
    val selectedPersonalAssignmentId by assignmentViewModel.selectedPersonalAssignmentId.collectAsStateWithLifecycle()

    LaunchedEffect(assignmentId, selectedAssignmentId, selectedPersonalAssignmentId) {
        val personalId = selectedPersonalAssignmentId ?: assignmentId
        val assignId = selectedAssignmentId

        if (assignId != null) {
            assignmentViewModel.loadAssignmentById(assignId)
        }

        personalId?.let { pid ->
            assignmentViewModel.loadPersonalAssignmentStatistics(pid)
        }
    }

    // 네트워크 오류 시 Toast 표시
    LaunchedEffect(error) {
        error?.let {
            if (ErrorMessageMapper.isNetworkError(it)) {
                Toast.makeText(context, "네트워크가 불안정합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 네트워크 에러가 아닌 경우에만 에러를 클리어합니다.
    // 네트워크 에러는 currentAssignment == null일 때 구분하기 위해 유지합니다.
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            if (!ErrorMessageMapper.isNetworkError(errorMessage)) {
                assignmentViewModel.clearError()
            }
        }
    }

    val actualTitle = currentAssignment?.title ?: assignmentTitle ?: "과제"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = PrimaryIndigo)
            }
        }

        // 에러가 있고 과제 데이터가 없을 때만 에러를 표시
        // 네트워크 에러는 Toast로만 표시하고 화면에는 표시하지 않음
        if (error != null && currentAssignment == null) {
            val isNetworkError = ErrorMessageMapper.isNetworkError(error)
            // 네트워크 오류가 아닌 경우에만 화면에 표시
            if (!isNetworkError) {
                VTCard(
                    variant = CardVariant.Outlined,
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
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = ErrorMessageMapper.getErrorMessage(error),
                            color = Error,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        )
                    }
                }
            }
        } else if (error != null && currentAssignment != null) {
            // 과제 데이터가 있지만 에러가 있는 경우 (통계 로딩 실패 등)
            // 네트워크 에러가 아닌 경우에만 표시
            val isNetworkError = ErrorMessageMapper.isNetworkError(error)
            if (!isNetworkError) {
                VTCard(
                    variant = CardVariant.Outlined,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = ErrorMessageMapper.getErrorMessage(error),
                        color = Error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = PrimaryIndigo.copy(alpha = 0.08f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                )
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 14.dp),
        ) {
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
                            modifier = Modifier.widthIn(max = 120.dp),
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
                            modifier = Modifier.widthIn(max = 120.dp),
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

            currentAssignment?.dueAt?.let { dueDate ->
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 5.dp, y = (-3).dp),
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
                    text = "• 음성으로 답변을 녹음해주세요\n• 각 단계별로 명확하게 설명해주세요\n• 각 답변은 1분 이내로 완료해주세요\n• 답변에 따라 문제당 1~3개의 꼬리 질문이 생성됩니다",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600,
                )
            }
        }

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
