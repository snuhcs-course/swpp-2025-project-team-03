package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*

@Composable
fun LanguageSettingsScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedLanguage by remember { mutableStateOf("한국어") }
    var selectedVoice by remember { mutableStateOf("여성 음성") }
    var speechSpeed by remember { mutableStateOf(1.0f) }
    var speechPitch by remember { mutableStateOf(1.0f) }
    
    val languages = listOf("한국어", "English", "日本語", "中文")
    val voices = listOf("여성 음성", "남성 음성", "아동 음성")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        VTHeader(
            title = "언어 설정",
            onBackClick = onBackClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Language selection
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "앱 언어",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "VoiceTutor 앱의 표시 언어를 선택하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                languages.forEach { language ->
                    LanguageOption(
                        language = language,
                        isSelected = selectedLanguage == language,
                        onClick = { selectedLanguage = language }
                    )
                    if (language != languages.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Voice settings
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "음성 설정",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "AI 음성의 종류와 속도를 조정하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                // Voice type selection
                Text(
                    text = "음성 종류",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                voices.forEach { voice ->
                    VoiceOption(
                        voice = voice,
                        isSelected = selectedVoice == voice,
                        onClick = { selectedVoice = voice }
                    )
                    if (voice != voices.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Speech speed
                Text(
                    text = "말하기 속도",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "느림",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    
                    Slider(
                        value = speechSpeed,
                        onValueChange = { speechSpeed = it },
                        valueRange = 0.5f..2.0f,
                        steps = 6,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = "빠름",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
                
                Text(
                    text = "현재 속도: ${String.format("%.1f", speechSpeed)}x",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryIndigo,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Speech pitch
                Text(
                    text = "음성 높이",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "낮음",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                    
                    Slider(
                        value = speechPitch,
                        onValueChange = { speechPitch = it },
                        valueRange = 0.5f..2.0f,
                        steps = 6,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = "높음",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
                
                Text(
                    text = "현재 높이: ${String.format("%.1f", speechPitch)}x",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryIndigo,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Test voice button
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "음성 테스트",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "설정한 음성으로 테스트 문장을 들어보세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                VTButton(
                    text = "음성 테스트 재생",
                    onClick = {
                        // TODO: Play test voice
                    },
                    fullWidth = true,
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
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Save button
        VTButton(
            text = "설정 저장",
            onClick = {
                // TODO: Save language settings
                onBackClick()
            },
            fullWidth = true,
            variant = ButtonVariant.Gradient
        )
    }
}

@Composable
fun LanguageOption(
    language: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) PrimaryIndigo.copy(alpha = 0.1f) else Color.Transparent
    val textColor = if (isSelected) PrimaryIndigo else Gray800
    val borderColor = if (isSelected) PrimaryIndigo else Gray300
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = language,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
        
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = PrimaryIndigo,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun VoiceOption(
    voice: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) PrimaryIndigo.copy(alpha = 0.1f) else Color.Transparent
    val textColor = if (isSelected) PrimaryIndigo else Gray800
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = voice,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
        
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = PrimaryIndigo,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
