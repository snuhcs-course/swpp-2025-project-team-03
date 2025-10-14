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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*

@Composable
fun NotificationSettingsScreen(
    onBackClick: () -> Unit = {}
) {
    var assignmentNotifications by remember { mutableStateOf(true) }
    var messageNotifications by remember { mutableStateOf(true) }
    var deadlineReminders by remember { mutableStateOf(true) }
    var achievementNotifications by remember { mutableStateOf(false) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(true) }
    var quietHoursEnabled by remember { mutableStateOf(false) }
    var quietStartTime by remember { mutableStateOf("22:00") }
    var quietEndTime by remember { mutableStateOf("08:00") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        VTHeader(
            title = "알림 설정",
            onBackClick = onBackClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Notification types
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "알림 유형",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                NotificationSettingItem(
                    title = "과제 알림",
                    description = "새로운 과제가 배정되면 알림을 받습니다",
                    icon = Icons.Filled.Assignment,
                    isEnabled = assignmentNotifications,
                    onToggle = { assignmentNotifications = it }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                NotificationSettingItem(
                    title = "메시지 알림",
                    description = "새로운 메시지를 받으면 알림을 받습니다",
                    icon = Icons.Filled.Message,
                    isEnabled = messageNotifications,
                    onToggle = { messageNotifications = it }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                NotificationSettingItem(
                    title = "마감일 알림",
                    description = "과제 마감일이 다가오면 알림을 받습니다",
                    icon = Icons.Filled.Schedule,
                    isEnabled = deadlineReminders,
                    onToggle = { deadlineReminders = it }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                NotificationSettingItem(
                    title = "성과 알림",
                    description = "성과나 점수 업데이트 시 알림을 받습니다",
                    icon = Icons.Filled.EmojiEvents,
                    isEnabled = achievementNotifications,
                    onToggle = { achievementNotifications = it }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Notification preferences
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "알림 설정",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                NotificationSettingItem(
                    title = "소리",
                    description = "알림 소리를 재생합니다",
                    icon = Icons.Filled.VolumeUp,
                    isEnabled = soundEnabled,
                    onToggle = { soundEnabled = it }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                NotificationSettingItem(
                    title = "진동",
                    description = "알림 시 진동을 울립니다",
                    icon = Icons.Filled.Vibration,
                    isEnabled = vibrationEnabled,
                    onToggle = { vibrationEnabled = it }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quiet hours
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "방해 금지 시간",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                NotificationSettingItem(
                    title = "방해 금지 모드",
                    description = "설정한 시간 동안 알림을 받지 않습니다",
                    icon = Icons.Filled.DoNotDisturb,
                    isEnabled = quietHoursEnabled,
                    onToggle = { quietHoursEnabled = it }
                )
                
                if (quietHoursEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "시작 시간",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray600
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = quietStartTime,
                                onValueChange = { quietStartTime = it },
                                placeholder = { Text("22:00") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "종료 시간",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray600
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = quietEndTime,
                                onValueChange = { quietEndTime = it },
                                placeholder = { Text("08:00") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Save button
        VTButton(
            text = "설정 저장",
            onClick = {
                // TODO: Save notification settings
                onBackClick()
            },
            fullWidth = true,
            variant = ButtonVariant.Gradient
        )
    }
}

@Composable
fun NotificationSettingItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PrimaryIndigo,
                modifier = Modifier.size(20.dp)
            )
            
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Gray800
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
            }
        }
        
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = PrimaryIndigo,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Gray300
            )
        )
    }
}
