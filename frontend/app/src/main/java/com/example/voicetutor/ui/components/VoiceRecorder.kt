package com.example.voicetutor.ui.components

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.voicetutor.audio.AudioRecorder
import com.example.voicetutor.utils.PermissionUtils
import com.example.voicetutor.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun VoiceRecorder(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    recordingDuration: String = "00:00",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // AudioRecorder 인스턴스
    val audioRecorder = remember { AudioRecorder(context) }
    val recordingState by audioRecorder.recordingState.collectAsState()
    
    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            onStartRecording()
        }
    }
    
    // 권한 체크 및 녹음 시작
    val startRecordingWithPermission = {
        if (PermissionUtils.hasAudioPermission(context)) {
            onStartRecording()
        } else {
            permissionLauncher.launch(PermissionUtils.getRequiredPermissions())
        }
    }
    
    // 실제 녹음 시작/중지
    LaunchedEffect(isRecording) {
        if (isRecording) {
            audioRecorder.startRecording()
        } else {
            audioRecorder.stopRecording()
        }
    }
    
    // 컴포넌트 해제 시 정리
    DisposableEffect(Unit) {
        onDispose {
            audioRecorder.cleanup()
        }
    }
    
    VoiceRecorderContent(
        isRecording = isRecording,
        recordingDuration = formatTime(recordingState.recordingTime),
        onStartRecording = startRecordingWithPermission,
        onStopRecording = onStopRecording,
        error = recordingState.error,
        modifier = modifier
    )
}

@Composable
fun VoiceRecorderContent(
    isRecording: Boolean,
    recordingDuration: String,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    error: String? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "recording")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecording) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ), label = "scale"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = if (isRecording) 0.8f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ), label = "pulseAlpha"
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Gray50)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 녹음 상태 표시
        if (isRecording) {
            VoiceWaveform(modifier = Modifier.fillMaxWidth().height(60.dp))
            
            Text(
                text = recordingDuration,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryIndigo
            )
            
            Text(
                text = "음성 녹음 중...",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )
        } else {
            Text(
                text = "음성으로 답변하기",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                color = Gray800
            )
        }
        
        // 에러 메시지
        error?.let { errorMessage ->
            Text(
                text = "❌ $errorMessage",
                style = MaterialTheme.typography.bodySmall,
                color = Error,
                fontWeight = FontWeight.Medium
            )
        }
        
        // 녹음 버튼
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    if (isRecording) Error else PrimaryIndigo
                )
                .clickable {
                    if (isRecording) {
                        onStopRecording()
                    } else {
                        onStartRecording()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // 펄스 효과
            if (isRecording) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(
                            Error.copy(alpha = pulseAlpha)
                        )
                )
            }
            
            Icon(
                imageVector = if (isRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = if (isRecording) "녹음 중지" else "음성 입력",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        // 사용 안내
        Text(
            text = if (isRecording) "녹음을 중지하려면 버튼을 누르세요" else "녹음을 시작하려면 버튼을 누르세요",
            style = MaterialTheme.typography.bodySmall,
            color = Gray600
        )
    }
}

@Composable
fun VoiceWaveform(modifier: Modifier = Modifier) {
    val animatedProgress = rememberInfiniteTransition(label = "waveformAnimation").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "waveformProgress"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        val barWidth = 4.dp.toPx()
        val spacing = 2.dp.toPx()
        val totalBars = ((width + spacing) / (barWidth + spacing)).toInt()

        for (i in 0 until totalBars) {
            val x = i * (barWidth + spacing)
            val animatedHeight = (abs(Random.nextFloat()) * (height * 0.8f) + height * 0.2f) * animatedProgress.value
            val barHeight = animatedHeight.coerceIn(height * 0.1f, height * 0.9f)
            val startY = centerY - barHeight / 2
            val endY = centerY + barHeight / 2

            drawLine(
                color = PrimaryIndigo,
                start = Offset(x, startY),
                end = Offset(x, endY),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}


private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

@Preview(showBackground = true)
@Composable
fun PreviewVoiceRecorder() {
    VoiceTutorTheme {
        var isRecording by remember { mutableStateOf(false) }
        var recordedAudio by remember { mutableStateOf<String?>(null) }
        var isPlaying by remember { mutableStateOf(false) }

        Column(modifier = Modifier.padding(16.dp)) {
            VoiceRecorder(
                isRecording = isRecording,
                onStartRecording = { isRecording = true },
                onStopRecording = { isRecording = false },
                recordingDuration = "01:23"
            )
            Spacer(modifier = Modifier.height(16.dp))
            VTButton(text = "Set Recorded Audio", onClick = { recordedAudio = "path/to/audio.mp3" })
            Spacer(modifier = Modifier.height(8.dp))
            VTButton(text = "Clear Recorded Audio", onClick = { recordedAudio = null })
        }
    }
}