package com.example.voicetutor.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.audio.AudioRecorder
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import com.example.voicetutor.utils.PermissionUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AssignmentScreen(
    assignmentId: Int? = null, // PersonalAssignment ID 사용
    authViewModel: AuthViewModel? = null, // 전달받은 AuthViewModel 사용
    onNavigateToHome: () -> Unit = {}, // 홈으로 돌아가기 콜백
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val viewModelAuth = authViewModel ?: hiltViewModel<AuthViewModel>()
    
    val context = LocalContext.current
    val assignmentIdValue = assignmentId ?: 1

    val currentUser by viewModelAuth.currentUser.collectAsStateWithLifecycle()
    val personalAssignmentQuestions by viewModel.personalAssignmentQuestions.collectAsStateWithLifecycle()
    val personalAssignmentStatistics by viewModel.personalAssignmentStatistics.collectAsStateWithLifecycle()
    val audioRecordingState by viewModel.audioRecordingState.collectAsStateWithLifecycle()
    val answerSubmissionResponse by viewModel.answerSubmissionResponse.collectAsStateWithLifecycle()
    val isAssignmentCompleted by viewModel.isAssignmentCompleted.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val audioRecorder = remember { AudioRecorder(context) }

    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackDuration by remember { mutableStateOf(0) }
    var playbackCurrentPosition by remember { mutableStateOf(0) }

    val audioRecorderState by audioRecorder.recordingState.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()

    // 채점 상태 polling
    var pollingJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) {
        viewModel.refreshProcessingStatus(assignmentIdValue)
    }

    LaunchedEffect(isProcessing) {
        if (isProcessing) {
            pollingJob?.cancel()
            pollingJob = scope.launch {
                while (true) {
                    delay(1000)
                    viewModel.refreshProcessingStatus(assignmentIdValue)
                    if (!viewModel.isProcessing.value) {
                        break
                    }
                }
            }
        } else {
            pollingJob?.cancel()
        }
    }

    if (isProcessing) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = PrimaryIndigo)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "채점 중입니다. 잠시 대기하세요.",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                )
            }
        }
        return
    }

    // 재생 진행 시간 업데이트
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(100)
            mediaPlayer?.let { player ->
                try {
                    playbackCurrentPosition = player.currentPosition / 1000
                } catch (e: Exception) {
                    // 재생 위치 가져오기 실패 시 무시
                }
            }
        }
    }

    // 응답 결과 표시를 위한 상태
    var showResult by remember { mutableStateOf(false) }
    var isAnswerCorrect by remember { mutableStateOf(false) }
    var isSkipped by remember { mutableStateOf(false) }
    var currentTailQuestionNumber by remember { mutableStateOf<String?>(null) }
    var savedTailQuestion by remember { mutableStateOf<com.example.voicetutor.data.models.TailQuestion?>(null) }
    var lastProcessedResponseNumberStr by remember { mutableStateOf<String?>(null) }

    // MediaPlayer cleanup
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    // 권한 요청 런처
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { }

    val currentQuestion = viewModel.getCurrentQuestion()

    // 과제 질문 로드 (최초 1회만 실행)
    LaunchedEffect(assignmentIdValue) {
        viewModel.loadAllQuestions(assignmentIdValue)
        viewModel.loadPersonalAssignmentStatistics(assignmentIdValue)
    }

    // 응답 결과 처리
    LaunchedEffect(answerSubmissionResponse) {
        answerSubmissionResponse?.let { response ->
            val responseNumberStr = response.numberStr

            // 동일한 응답 중복 처리 방지
            if (responseNumberStr != null && lastProcessedResponseNumberStr == responseNumberStr) {
                return@let
            }

            lastProcessedResponseNumberStr = responseNumberStr
            isAnswerCorrect = response.isCorrect
            showResult = true

            // numberStr이 null이면 마지막 문제 (결과 화면 표시 후 사용자가 "완료" 버튼 클릭 시 처리)
            if (response.numberStr == null) {
                // 결과 화면을 보여주고, "완료" 버튼을 통해 과제 완료 처리
                return@let
            }

            // numberStr에 하이픈이 포함되면 꼬리 질문, 아니면 다음 기본 질문
            val isTailQuestion = response.numberStr.contains("-")

            if (isTailQuestion) {
                currentTailQuestionNumber = response.numberStr
                savedTailQuestion = response.tailQuestion
            } else {
                // 다음 기본 질문인 경우 상태 초기화 
                currentTailQuestionNumber = null
                savedTailQuestion = null
            }
        }
    }

    // AudioRecorder 상태 변화 감지 및 ViewModel 동기화
    LaunchedEffect(audioRecorderState.isRecordingComplete) {
        if (audioRecorderState.isRecordingComplete && audioRecorderState.audioFilePath != null) {
            viewModel.stopRecordingWithFilePath(audioRecorderState.audioFilePath!!)
        }
    }

    // 녹음 시간 업데이트
    LaunchedEffect(audioRecordingState.isRecording) {
        if (!audioRecordingState.isRecording) return@LaunchedEffect

        while (audioRecordingState.isRecording) {
            delay(1000)
            if (audioRecordingState.isRecording) {
                viewModel.updateRecordingDuration(audioRecordingState.recordingTime + 1)
            } else {
                break
            }
        }
    }

    // 로딩 중이면 로딩 화면 표시
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                color = PrimaryIndigo,
            )
        }
    } else if (isAssignmentCompleted || personalAssignmentQuestions.isEmpty() || currentQuestion == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            VTCard(
                variant = CardVariant.Elevated,
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.AssignmentTurnedIn,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(80.dp),
                    )
                    Text(
                        text = "과제 완료!",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Gray800,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "모든 문제를 완료했습니다.\n수고하셨습니다!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Gray600,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    VTButton(
                        text = "홈으로 돌아가기",
                        onClick = {
                            onNavigateToHome()
                        },
                        variant = ButtonVariant.Gradient,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 진행률 계산 및 표시
            val totalProblems = personalAssignmentStatistics?.totalProblem ?: 0
            val solvedProblems = personalAssignmentStatistics?.solvedProblem ?: 0
            val progress = if (totalProblems > 0) {
                (solvedProblems.toFloat() / totalProblems.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }

            VTProgressBar(
                progress = progress,
                showPercentage = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "$solvedProblems / $totalProblems",
                style = MaterialTheme.typography.bodySmall,
                color = Gray600,
                modifier = Modifier.fillMaxWidth(),
            )

            // 질문 카드
            VTCard(
                variant = CardVariant.Elevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(0.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // 질문 번호 표시
                    if (!showResult) {
                        val questionNumber = currentTailQuestionNumber?.let { tailNumber ->
                            if (tailNumber.contains("-")) "꼬리 질문 $tailNumber" else "질문 $tailNumber"
                        } ?: run {
                            val response = answerSubmissionResponse
                            if (response?.numberStr != null && !response.numberStr.contains("-")) {
                                "질문 ${response.numberStr}"
                            } else if (currentQuestion.number.contains("-")) {
                                "꼬리 질문 ${currentQuestion.number}"
                            } else {
                                "질문 ${currentQuestion.number}"
                            }
                        }

                        Text(
                            text = questionNumber,
                            style = MaterialTheme.typography.labelLarge,
                            color = PrimaryIndigo,
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    // 질문 텍스트 표시
                    val response = answerSubmissionResponse
                    val questionText = when {
                        !showResult -> {
                            if (response != null && response.numberStr?.contains("-") == true) {
                                response.tailQuestion?.question ?: currentQuestion.question
                            } else if (currentTailQuestionNumber != null) {
                                savedTailQuestion?.question ?: currentQuestion.question
                            } else {
                                currentQuestion.question
                            }
                        }
                        else -> ""
                    }
                    if (!showResult && questionText.isNotEmpty()) {
                        Text(
                            text = questionText,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Gray800,
                        )
                    }

                    // 응답 결과 표시
                    if (showResult) {
                        Spacer(modifier = Modifier.height(16.dp))

                        // 정답/오답 표시
                        VTCard(
                            variant = CardVariant.Outlined,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                Icon(
                                    imageVector = when {
                                        isSkipped -> Icons.Filled.SkipNext
                                        isAnswerCorrect -> Icons.Filled.CheckCircle
                                        else -> Icons.Filled.Cancel
                                    },
                                    contentDescription = null,
                                    tint = when {
                                        isSkipped -> Warning
                                        isAnswerCorrect -> Success
                                        else -> Error
                                    },
                                    modifier = Modifier.size(56.dp),
                                )
                                val resultTextStyle = when {
                                    isSkipped -> MaterialTheme.typography.titleLarge
                                    else -> MaterialTheme.typography.headlineSmall
                                }
                                Text(
                                    text = when {
                                        isSkipped -> "문제를 건너뛰었습니다"
                                        isAnswerCorrect -> "정답입니다!"
                                        else -> "틀렸습니다"
                                    },
                                    style = resultTextStyle,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isSkipped -> Warning
                                        isAnswerCorrect -> Success
                                        else -> Error
                                    },
                                    modifier = Modifier.padding(top = 8.dp),
                                    textAlign = TextAlign.Center,
                                )
                            }
                        }
                    }
                }
            }

            // Recording and send area
            VTCard(variant = CardVariant.Outlined) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    // 녹음 관련 UI
                    if (!showResult) {
                        if (audioRecordingState.isRecording) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.RadioButtonChecked,
                                    contentDescription = null,
                                    tint = Error,
                                    modifier = Modifier.size(16.dp),
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "녹음 중... ${String.format("%02d:%02d", audioRecordingState.recordingTime / 60, audioRecordingState.recordingTime % 60)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Error,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        } else if (audioRecordingState.audioFilePath != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // 녹음 완료 표시
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = Success,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "녹음 완료 (${String.format("%02d:%02d", audioRecordingState.recordingTime / 60, audioRecordingState.recordingTime % 60)})",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Success,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }

                                // 음성 다시 듣기 아이콘 버튼
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    if (isPlaying) {
                                        Text(
                                            text = "${String.format("%02d:%02d", playbackCurrentPosition / 60, playbackCurrentPosition % 60)} / ${String.format("%02d:%02d", playbackDuration / 60, playbackDuration % 60)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Error,
                                            fontWeight = FontWeight.Bold,
                                        )
                                    } else {
                                        Text(
                                            text = "다시 듣기",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = PrimaryIndigo,
                                            fontWeight = FontWeight.Medium,
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val audioFilePath = audioRecordingState.audioFilePath
                                            if (audioFilePath != null) {
                                                if (isPlaying) {
                                                    mediaPlayer?.stop()
                                                    mediaPlayer?.release()
                                                    mediaPlayer = null
                                                    isPlaying = false
                                                    playbackCurrentPosition = 0
                                                } else {
                                                    try {
                                                        mediaPlayer?.release()
                                                        mediaPlayer = android.media.MediaPlayer().apply {
                                                            setDataSource(audioFilePath)
                                                            prepare()
                                                            playbackDuration = duration / 1000
                                                            playbackCurrentPosition = 0
                                                            start()
                                                            isPlaying = true

                                                            setOnCompletionListener {
                                                                isPlaying = false
                                                                playbackCurrentPosition = 0
                                                                release()
                                                                mediaPlayer = null
                                                            }

                                                            setOnErrorListener { _, _, _ ->
                                                                isPlaying = false
                                                                playbackCurrentPosition = 0
                                                                release()
                                                                mediaPlayer = null
                                                                true
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        isPlaying = false
                                                        playbackCurrentPosition = 0
                                                        mediaPlayer?.release()
                                                        mediaPlayer = null
                                                    }
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .background(
                                                color = if (isPlaying) Error.copy(alpha = 0.15f) else PrimaryIndigo.copy(alpha = 0.15f),
                                                shape = androidx.compose.foundation.shape.CircleShape,
                                            )
                                            .size(40.dp),
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                            contentDescription = if (isPlaying) "재생 중지" else "음성 재생",
                                            tint = if (isPlaying) Error else PrimaryIndigo,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                }
                            }
                        }

                        // 녹음 버튼
                        if (audioRecordingState.audioFilePath == null && !audioRecordingState.isRecording) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                VTButton(
                                    text = "녹음 시작",
                                    onClick = {
                                        if (!PermissionUtils.hasAudioPermission(context)) {
                                            permissionLauncher.launch(PermissionUtils.getRequiredPermissions())
                                        } else {
                                            val success = audioRecorder.startRecording()
                                            if (success) {
                                                viewModel.startRecording()
                                            }
                                        }
                                    },
                                    variant = ButtonVariant.Gradient,
                                    enabled = true,
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Mic,
                                            contentDescription = null,
                                        )
                                    },
                                )

                                VTButton(
                                    text = "건너뛰기",
                                    onClick = {
                                        isSkipped = true

                                        scope.launch {
                                            try {
                                                val emptyFile = audioRecorder.createEmptyWavFile()
                                                if (emptyFile != null && currentUser != null && currentQuestion != null) {
                                                    val questionIdToSubmit = if (currentTailQuestionNumber != null && savedTailQuestion != null) {
                                                        savedTailQuestion!!.id
                                                    } else {
                                                        currentQuestion.id
                                                    }

                                                    viewModel.submitAnswer(
                                                        personalAssignmentId = assignmentIdValue,
                                                        studentId = currentUser!!.id,
                                                        questionId = questionIdToSubmit,
                                                        audioFile = emptyFile,
                                                    )
                                                    viewModel.resetAudioRecording()
                                                }
                                            } catch (e: Exception) {
                                                // 건너뛰기 실패 시 무시
                                            }
                                        }
                                    },
                                    variant = ButtonVariant.Outline,
                                    enabled = !isSubmitting,
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.SkipNext,
                                            contentDescription = null,
                                        )
                                    },
                                )
                            }
                        } else {
                            // 녹음 중 또는 녹음 완료: 단일 버튼
                            VTButton(
                                text = when {
                                    audioRecordingState.isRecording -> "녹음 중지"
                                    audioRecordingState.audioFilePath != null -> "다시 녹음하기"
                                    else -> "녹음 시작"
                                },
                                onClick = {
                                    if (audioRecordingState.isRecording) {
                                        try {
                                            viewModel.stopRecordingImmediately()
                                            scope.launch {
                                                try {
                                                    audioRecorder.stopRecording()
                                                } catch (e: Exception) {
                                                    // 녹음 중지 실패 시 무시
                                                }
                                            }
                                        } catch (e: Exception) {
                                            viewModel.resetAudioRecording()
                                        }
                                    } else {
                                        viewModel.resetAudioRecording()
                                    }
                                },
                                variant = when {
                                    audioRecordingState.isRecording -> ButtonVariant.Outline
                                    else -> ButtonVariant.Gradient
                                },
                                enabled = true,
                                fullWidth = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = when {
                                            audioRecordingState.isRecording -> Icons.Filled.Stop
                                            audioRecordingState.audioFilePath != null -> Icons.Filled.Refresh
                                            else -> Icons.Filled.Mic
                                        },
                                        contentDescription = null,
                                    )
                                },
                            )
                        }
                    }

                    // 응답 결과에 따른 버튼 표시
                    if (showResult) {
                        // 응답 결과가 있을 때
                        val response = answerSubmissionResponse
                        if (response == null) return@Column

                        when {
                            // Case 1: numberStr이 null이면 마지막 문제 완료
                            response.numberStr == null -> {
                                VTButton(
                                    text = "완료",
                                    onClick = {
                                        viewModel.setAssignmentCompleted(true)
                                    },
                                    variant = ButtonVariant.Gradient,
                                    fullWidth = true,
                                )
                            }
                            // Case 2: numberStr에 하이픈이 있으면 꼬리 질문으로 이동
                            response.numberStr.contains("-") -> {
                                VTButton(
                                    text = "꼬리질문으로 넘어가기",
                                    onClick = {
                                        showResult = false
                                        isSkipped = false
                                        // currentTailQuestionNumber와 savedTailQuestion은 이미 설정됨
                                    },
                                    variant = ButtonVariant.Gradient,
                                    fullWidth = true,
                                )
                            }
                            // Case 3: 다음 기본 질문으로 이동
                            else -> {
                                VTButton(
                                    text = "다음 문제",
                                    onClick = {
                                        viewModel.clearAnswerSubmissionResponse()
                                        showResult = false
                                        isSkipped = false
                                        currentTailQuestionNumber = null
                                        savedTailQuestion = null

                                        // 서버에서 받은 numberStr로 질문 이동
                                        viewModel.moveToQuestionByNumber(response.numberStr, assignmentIdValue)
                                    },
                                    variant = ButtonVariant.Gradient,
                                    fullWidth = true,
                                )
                            }
                        }
                    } else {
                        // 전송 버튼
                        VTButton(
                            text = "전송",
                            onClick = {
                                val user = currentUser
                                val audioFilePath = audioRecordingState.audioFilePath
                                if (audioFilePath != null && user != null && currentQuestion != null) {
                                    val audioFile = File(audioFilePath)

                                    try {
                                        val questionIdToSubmit = if (currentTailQuestionNumber != null && savedTailQuestion != null) {
                                            savedTailQuestion!!.id
                                        } else {
                                            currentQuestion.id
                                        }

                                        viewModel.submitAnswer(
                                            personalAssignmentId = assignmentIdValue,
                                            studentId = user.id,
                                            questionId = questionIdToSubmit,
                                            audioFile = audioFile,
                                        )
                                        viewModel.resetAudioRecording()
                                    } catch (e: Exception) {
                                        // 전송 실패 시 무시
                                    }
                                }
                            },
                            variant = ButtonVariant.Gradient,
                            fullWidth = true,
                            enabled = audioRecordingState.audioFilePath != null && !audioRecordingState.isRecording,
                        )
                    }
                }
            }
        }

        // 채점 중 오버레이
        if (isSubmitting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(48.dp),
                    )
                    Text(
                        text = "채점 중...",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun AssignmentScreenPreview() {
    VoiceTutorTheme {
        AssignmentScreen()
    }
}

