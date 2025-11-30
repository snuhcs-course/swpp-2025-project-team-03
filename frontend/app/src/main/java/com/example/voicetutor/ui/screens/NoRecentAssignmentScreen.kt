package com.example.voicetutor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.VTCard
import com.example.voicetutor.ui.components.CardVariant
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.utils.ErrorMessageMapper
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.ui.viewmodel.AuthViewModel

@Composable
fun NoRecentAssignmentScreen(
    viewModel: AssignmentViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    // 네트워크 에러가 아닌 경우에만 에러를 클리어합니다.
    // 네트워크 에러는 화면에 표시하기 위해 유지합니다.
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            if (!ErrorMessageMapper.isNetworkError(errorMessage)) {
                viewModel.clearError()
            }
        }
    }
    
    // 네트워크 오류 확인 (화면 표시용)
    val isNetworkError = error != null && ErrorMessageMapper.isNetworkError(error)
    
    // 화면이 표시될 때 loadRecentAssignment를 다시 호출하여 최신 상태 확인
    LaunchedEffect(Unit) {
        currentUser?.id?.let { userId ->
            viewModel.loadRecentAssignment(userId)
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = PrimaryIndigo,
            )
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Icon(
                    imageVector = if (isNetworkError) Icons.Filled.WifiOff else Icons.AutoMirrored.Filled.Assignment,
                    contentDescription = if (isNetworkError) "Network Error" else "No Recent Assignments",
                    tint = if (isNetworkError) Error else Gray400,
                    modifier = Modifier.size(80.dp),
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = if (isNetworkError) "네트워크가 불안정합니다" else "이어할 과제가 없습니다",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isNetworkError) Error else Gray800,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        text = if (isNetworkError) "인터넷 연결을 확인하고 다시 시도해주세요" else "홈 화면에서 새로운 과제를 확인해보세요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoRecentAssignmentScreenPreview() {
    VoiceTutorTheme {
        NoRecentAssignmentScreen()
    }
}
