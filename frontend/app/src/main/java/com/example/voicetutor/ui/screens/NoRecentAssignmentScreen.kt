package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*

@Composable
fun NoRecentAssignmentScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Icon
            Icon(
                imageVector = Icons.Filled.Assignment,
                contentDescription = null,
                tint = Gray400,
                modifier = Modifier.size(80.dp)
            )

            // Message
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "이어할 과제가 없습니다",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "홈 화면에서 새로운 과제를 확인해보세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                    textAlign = TextAlign.Center
                )
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

