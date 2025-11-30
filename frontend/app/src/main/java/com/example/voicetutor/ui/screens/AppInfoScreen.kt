package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*

private const val APP_NAME = "VoiceTutor"
private const val APP_DESCRIPTION = "음성 인식 기반 교육 플랫폼"
private const val APP_VERSION = "1.0.0"
private const val BUILD_NUMBER = "1.0.0 (100)"
private const val DEVELOPER_NAME = "VoiceTutor Team"
private const val LAST_UPDATE_DATE = "2025년 12월 7일"
private const val PLATFORM = "Android"
private const val SUPPORT_EMAIL = "support@voicetutor.com"
private const val COPYRIGHT_YEAR = "2025"

@Composable
fun AppInfoScreen(
    onBackClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars),
    ) {
        AppInfoHeader(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            AppInfoCard()
            Spacer(modifier = Modifier.height(4.dp))
            DevelopmentInfoCard()
            Spacer(modifier = Modifier.height(8.dp))
            ContactSupportCard()
        }

        AppInfoCopyright()
    }
}

@Composable
private fun AppInfoHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = Color.Black,
            )
        }
        Text(
            text = "앱 정보",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
        )
    }
}

@Composable
private fun AppInfoCard() {
    VTCard(variant = CardVariant.Outlined) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(PrimaryIndigo, PrimaryPurple),
                        ),
                        shape = MaterialTheme.shapes.large,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "V",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = APP_NAME,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Gray800,
            )

            Text(
                text = APP_DESCRIPTION,
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600,
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "버전 $APP_VERSION",
                style = MaterialTheme.typography.bodySmall,
                color = Gray500,
            )
        }
    }
}

@Composable
private fun DevelopmentInfoCard() {
    VTCard(variant = CardVariant.Outlined) {
        Column {
            Text(
                text = "개발 정보",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray800,
            )
            Spacer(modifier = Modifier.height(12.dp))

            InfoItem(label = "개발사", value = DEVELOPER_NAME)
            Spacer(modifier = Modifier.height(8.dp))
            InfoItem(label = "빌드 번호", value = BUILD_NUMBER)
            Spacer(modifier = Modifier.height(8.dp))
            InfoItem(label = "최종 업데이트", value = LAST_UPDATE_DATE)
            Spacer(modifier = Modifier.height(8.dp))
            InfoItem(label = "플랫폼", value = PLATFORM)
        }
    }
}

@Composable
private fun ContactSupportCard() {
    VTCard(variant = CardVariant.Outlined) {
        Column {
            Text(
                text = "문의 및 지원",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray800,
            )
            Spacer(modifier = Modifier.height(12.dp))

            ContactItem(
                icon = Icons.Filled.Email,
                title = "이메일",
                value = SUPPORT_EMAIL,
                onClick = {},
            )

            Spacer(modifier = Modifier.height(8.dp))

            ContactItem(
                icon = Icons.Filled.Star,
                title = "앱 평가하기",
                value = "Google Play Store",
                onClick = {},
            )
        }
    }
}

@Composable
private fun AppInfoCopyright() {
    Text(
        text = "© $COPYRIGHT_YEAR $DEVELOPER_NAME. All rights reserved.",
        style = MaterialTheme.typography.bodySmall,
        color = Gray500,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .padding(bottom = 16.dp)
            .windowInsetsPadding(WindowInsets.navigationBars),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun InfoItem(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Gray600,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Gray800,
        )
    }
}

@Composable
private fun ContactItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryIndigo,
            modifier = Modifier.size(20.dp),
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Gray800,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = Gray600,
            )
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = Gray400,
            modifier = Modifier.size(16.dp),
        )
    }
}
