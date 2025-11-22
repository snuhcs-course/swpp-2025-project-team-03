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

@Preview(showBackground = true)
@Composable
fun VoiceTutorAppPreview() {
    VoiceTutorTheme {
        VoiceTutorApp()
    }
}
