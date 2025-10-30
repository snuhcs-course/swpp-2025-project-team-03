package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.voicetutor.data.network.ApiConfig
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import javax.inject.Inject

@Composable
fun ServerSettingsScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedServerType by remember { mutableStateOf("") }
    var customUrl by remember { mutableStateOf("") }
    var showCustomDialog by remember { mutableStateOf(false) }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val apiConfig = remember { ApiConfig(context) }
    val availableServers = remember { apiConfig.getAvailableServers() }
    
    // 현재 설정 로드
    LaunchedEffect(Unit) {
        selectedServerType = apiConfig.getServerType()
        customUrl = apiConfig.getBaseUrl()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Gray800
                )
            }
            Text(
                text = "서버 설정",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Gray800
            )
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 현재 서버 정보
        VTCard(
            variant = CardVariant.Outlined,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "현재 서버",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray800
                )
                Text(
                    text = "타입: ${apiConfig.getServerType()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
                Text(
                    text = "URL: ${apiConfig.getBaseUrl()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
            }
        }
        
        // 서버 옵션들
        Text(
            text = "서버 선택",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Gray800
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(availableServers) { server ->
                ServerOptionCard(
                    server = server,
                    isSelected = selectedServerType == server.type,
                    onClick = {
                        selectedServerType = server.type
                        when (server.type) {
                            ApiConfig.SERVER_TYPE_LOCALHOST -> {
                                apiConfig.setLocalhostServer()
                            }
                            ApiConfig.SERVER_TYPE_PROD -> {
                                showCustomDialog = true
                            }
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // 연결 테스트 버튼
        VTButton(
            text = "연결 테스트",
            onClick = {
                // TODO: 서버 연결 테스트 구현
                println("서버 연결 테스트: ${apiConfig.getBaseUrl()}")
            },
            variant = ButtonVariant.Outline,
            fullWidth = true
        )
    }
    
    // 커스텀 URL 입력 다이얼로그
    if (showCustomDialog) {
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            title = { Text("Prod 서버 URL 입력") },
            text = {
                OutlinedTextField(
                    value = customUrl,
                    onValueChange = { customUrl = it },
                    label = { Text("서버 URL") },
                    placeholder = { Text("예: http://192.168.1.100:8080/api/") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                VTButton(
                    text = "확인",
                    onClick = {
                        if (customUrl.isNotBlank()) {
                            apiConfig.setProdServer(customUrl)
                            showCustomDialog = false
                        }
                    },
                    variant = ButtonVariant.Primary,
                    size = ButtonSize.Small
                )
            },
            dismissButton = {
                VTButton(
                    text = "취소",
                    onClick = { showCustomDialog = false },
                    variant = ButtonVariant.Outline,
                    size = ButtonSize.Small
                )
            }
        )
    }
}

@Composable
fun ServerOptionCard(
    server: com.example.voicetutor.data.network.ServerOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    VTCard(
        variant = if (isSelected) CardVariant.Selected else CardVariant.Outlined,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = server.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = if (isSelected) PrimaryIndigo else Gray800
                )
                if (server.url.isNotBlank()) {
                    Text(
                        text = server.url,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
            }
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "선택됨",
                    tint = PrimaryIndigo
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ServerSettingsScreenPreview() {
    VoiceTutorTheme {
        ServerSettingsScreen()
    }
}
