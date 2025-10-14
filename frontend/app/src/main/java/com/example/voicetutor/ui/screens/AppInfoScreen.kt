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
fun AppInfoScreen(
    onBackClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        VTHeader(
            title = "앱 정보",
            onBackClick = onBackClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // App logo and basic info
        VTCard(variant = CardVariant.Outlined) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // App icon placeholder
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = PrimaryIndigo,
                            shape = MaterialTheme.shapes.large
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.School,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "VoiceTutor",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Gray800
                )
                
                Text(
                    text = "음성 인식 기반 교육 플랫폼",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "버전 1.0.0",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // App features
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "주요 기능",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                val features = listOf(
                    "음성 인식 기반 과제 제출",
                    "실시간 AI 피드백",
                    "학생 진도 추적 및 분석",
                    "클래스 관리 및 메시징",
                    "출석 관리 시스템"
                )
                
                features.forEach { feature ->
                    FeatureItem(feature = feature)
                    if (feature != features.last()) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Development info
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "개발 정보",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                InfoItem(
                    label = "개발사",
                    value = "VoiceTutor Team"
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                InfoItem(
                    label = "빌드 번호",
                    value = "1.0.0 (100)"
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                InfoItem(
                    label = "최종 업데이트",
                    value = "2024년 1월 15일"
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                InfoItem(
                    label = "플랫폼",
                    value = "Android"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Legal information
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "법적 정보",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                LegalItem(
                    title = "개인정보처리방침",
                    onClick = {
                        // TODO: Open privacy policy
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LegalItem(
                    title = "서비스 이용약관",
                    onClick = {
                        // TODO: Open terms of service
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LegalItem(
                    title = "오픈소스 라이선스",
                    onClick = {
                        // TODO: Open open source licenses
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Contact and support
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "문의 및 지원",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                ContactItem(
                    icon = Icons.Filled.Email,
                    title = "이메일",
                    value = "support@voicetutor.com",
                    onClick = {
                        // TODO: Open email client
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ContactItem(
                    icon = Icons.Filled.Language,
                    title = "웹사이트",
                    value = "www.voicetutor.com",
                    onClick = {
                        // TODO: Open website
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ContactItem(
                    icon = Icons.Filled.Star,
                    title = "앱 평가하기",
                    value = "Google Play Store",
                    onClick = {
                        // TODO: Open app store
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // App actions
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Text(
                    text = "앱 관리",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                ActionItem(
                    icon = Icons.Filled.Update,
                    title = "업데이트 확인",
                    description = "최신 버전으로 업데이트하세요",
                    onClick = {
                        // TODO: Check for updates
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ActionItem(
                    icon = Icons.Filled.Share,
                    title = "앱 공유하기",
                    description = "VoiceTutor를 친구들에게 추천하세요",
                    onClick = {
                        // TODO: Share app
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ActionItem(
                    icon = Icons.Filled.Delete,
                    title = "캐시 삭제",
                    description = "앱 데이터를 정리하여 공간을 확보하세요",
                    onClick = {
                        // TODO: Clear cache
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Copyright
        Text(
            text = "© 2024 VoiceTutor Team. All rights reserved.",
            style = MaterialTheme.typography.bodySmall,
            color = Gray500,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun FeatureItem(
    feature: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = Success,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = feature,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray800
        )
    }
}

@Composable
fun InfoItem(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray600
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Gray800
        )
    }
}

@Composable
fun LegalItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryIndigo
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = Gray400,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun ContactItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryIndigo,
            modifier = Modifier.size(20.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Gray800
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = Gray600
            )
        }
        
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = Gray400,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun ActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryIndigo,
            modifier = Modifier.size(20.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
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
        
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = Gray400,
            modifier = Modifier.size(16.dp)
        )
    }
}
