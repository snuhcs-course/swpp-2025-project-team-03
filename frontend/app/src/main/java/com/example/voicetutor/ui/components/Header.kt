package com.example.voicetutor.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.voicetutor.ui.theme.*

@Composable
fun VTHeader(
    title: String,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    showBackButton: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBackButton) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "뒤로가기",
                    tint = Gray700
                )
            }
        } else {
            Spacer(modifier = Modifier.size(24.dp))
        }
        
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Gray800,
            modifier = Modifier.weight(1f)
        )
        
        // Empty space for balance
        Spacer(modifier = Modifier.size(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun HeaderPreview() {
    VoiceTutorTheme {
        VTHeader(
            title = "학생 관리",
            onBackClick = {}
        )
    }
}
