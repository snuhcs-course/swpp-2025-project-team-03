package com.example.voicetutor.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.utils.ErrorMessageMapper
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.ui.viewmodel.AuthViewModel

@Composable
fun NoRecentAssignmentScreen() {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val authViewModel: AuthViewModel = hiltViewModel()
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    
    // 네트워크 오류 토스트를 한 번만 표시하기 위한 플래그
    var lastError by remember { mutableStateOf<String?>(null) }
    
    // error가 변경될 때마다 네트워크 오류 체크
    LaunchedEffect(error, isLoading) {
        val errorMessage = error
        // 로딩이 완료되고 에러가 있고, 네트워크 오류인 경우
        if (!isLoading && errorMessage != null && ErrorMessageMapper.isNetworkError(errorMessage)) {
            // 새로운 에러인 경우에만 토스트 표시
            if (errorMessage != lastError) {
                Toast.makeText(context, "네트워크가 불안정합니다.", Toast.LENGTH_SHORT).show()
                lastError = errorMessage
            }
        } else if (errorMessage != null && !ErrorMessageMapper.isNetworkError(errorMessage)) {
            // 네트워크 오류가 아닌 경우에만 에러를 클리어합니다.
            viewModel.clearError()
        }
    }
    
    // 화면이 표시될 때 loadRecentAssignment를 다시 호출하여 최신 상태 확인
    LaunchedEffect(Unit) {
        currentUser?.id?.let { userId ->
            // 플래그 초기화
            lastError = null
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
                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                    contentDescription = "No Recent Assignments",
                    tint = Gray400,
                    modifier = Modifier.size(80.dp),
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "이어할 과제가 없습니다",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        text = "홈 화면에서 새로운 과제를 확인해보세요",
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
