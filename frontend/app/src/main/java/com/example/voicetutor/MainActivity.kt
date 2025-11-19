package com.example.voicetutor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.navigation.*
import com.example.voicetutor.ui.screens.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.theme.Gray50
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VoiceTutorTheme {
                VoiceTutorApp()
            }
        }
    }
}

@Composable
fun VoiceTutorApp() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Gray50,
    ) {
        VoiceTutorNavigation()
    }
}

@Composable
fun ComponentShowcase() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "VoiceTutor 컴포넌트 쇼케이스",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )

        // Button 예시
        VTButton(
            text = "로그인",
            onClick = { },
            variant = ButtonVariant.Primary,
            fullWidth = true,
        )

        // Card 예시
        VTCard(variant = CardVariant.Elevated) {
            Text(
                text = "환영합니다!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "VoiceTutor에 오신 것을 환영합니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600,
            )
        }

        // ProgressBar 예시
        VTProgressBar(
            progress = 0.75f,
            showPercentage = true,
        )

        // StatsCard 예시
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            VTStatsCard(
                title = "완료한 과제",
                value = "0", // TODO: 실제 완료한 과제 수로 동적 설정
                icon = Icons.Filled.Done,
                modifier = Modifier.weight(1f),
                variant = CardVariant.Gradient,
                trend = TrendDirection.Up,
                trendValue = "+0", // TODO: 실제 증가 수로 동적 설정
            )

            VTStatsCard(
                title = "정확도",
                value = "0%", // TODO: 실제 정확도로 동적 설정
                icon = Icons.Filled.Favorite,
                iconColor = Success,
                modifier = Modifier.weight(1f),
                variant = CardVariant.Gradient,
                trend = TrendDirection.Up,
                trendValue = "+0%", // TODO: 실제 증가율로 동적 설정
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VoiceTutorAppPreview() {
    VoiceTutorTheme {
        VoiceTutorApp()
    }
}
