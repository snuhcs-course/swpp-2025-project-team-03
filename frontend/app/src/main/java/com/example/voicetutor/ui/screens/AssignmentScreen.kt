package com.example.voicetutor.ui.screens

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.ui.viewmodel.AuthViewModel
import com.example.voicetutor.audio.AudioRecorder
import com.example.voicetutor.utils.PermissionUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AssignmentScreen(
    assignmentId: Int? = null, // PersonalAssignment ID 사용
    assignmentTitle: String? = null, // 실제 과제 제목 사용
    authViewModel: AuthViewModel? = null, // 전달받은 AuthViewModel 사용
    onNavigateToHome: () -> Unit = {} // 홈으로 돌아가기 콜백
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val viewModelAuth = authViewModel ?: hiltViewModel<AuthViewModel>()
    
    val currentAssignment by viewModel.currentAssignment.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isAssignmentCompleted by viewModel.isAssignmentCompleted.collectAsStateWithLifecycle()
    val currentUser by viewModelAuth.currentUser.collectAsStateWithLifecycle()
    
    val scope = rememberCoroutineScope()
    
    // 동적 과제 제목 가져오기
    val dynamicAssignmentTitle = currentAssignment?.title ?: assignmentTitle ?: "과제"
    
    // Note: assignmentId는 PersonalAssignment ID이므로 loadAssignmentById를 호출하지 않음
    // Assignment 정보가 필요한 경우 PersonalAssignment API 응답에서 assignment.id를 사용해야 함
    
    // 모든 과제는 음성 답변 + AI 대화형 꼬리 질문 형태로 진행
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = PrimaryIndigo
            )
        }
    } else {
        AssignmentContinuousScreen(assignmentId = assignmentId ?: 1, assignmentTitle = assignmentTitle ?: "과제", authViewModel = viewModelAuth, onNavigateToHome = onNavigateToHome)
    }
}

// Mock 데이터: 화학 기초 퀴즈
data class QuizQuestionData(
    val questionNumber: Int,
    val question: String,
    val hint: String,
    val modelAnswer: String
)

private val mockChemistryQuestions = listOf(
    QuizQuestionData(1, "원소주기율표에서 같은 족의 원소들의 공통점은?", "전자 배치를 생각해보세요", "최외각 전자 수가 같다"),
    QuizQuestionData(2, "물(H2O)의 분자량은? (H=1, O=16)", "원자량을 더하세요", "18"),
    QuizQuestionData(3, "산소의 원소 기호는?", "Oxygen", "O"),
    QuizQuestionData(4, "공유결합은 무엇을 공유하는가?", "원자들이 함께 사용하는 것", "전자"),
    QuizQuestionData(5, "이온화 에너지가 가장 큰 원소족은?", "가장 안정한 원소들", "18족 (비활성 기체)")
)

@Composable
fun AssignmentContinuousScreen(
    assignmentId: Int = 1, // PersonalAssignment ID
    assignmentTitle: String,
    authViewModel: AuthViewModel? = null,
    onNavigateToHome: () -> Unit = {}
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val viewModelAuth = authViewModel ?: hiltViewModel<AuthViewModel>()
    val context = LocalContext.current
    
    val currentUser by viewModelAuth.currentUser.collectAsStateWithLifecycle()
    val personalAssignmentQuestions by viewModel.personalAssignmentQuestions.collectAsStateWithLifecycle()
    val totalBaseQuestions by viewModel.totalBaseQuestions.collectAsStateWithLifecycle()
    val personalAssignmentStatistics by viewModel.personalAssignmentStatistics.collectAsStateWithLifecycle()
    val currentQuestionIndex by viewModel.currentQuestionIndex.collectAsStateWithLifecycle()
    val audioRecordingState by viewModel.audioRecordingState.collectAsStateWithLifecycle()
    val answerSubmissionResponse by viewModel.answerSubmissionResponse.collectAsStateWithLifecycle()
    val isAssignmentCompleted by viewModel.isAssignmentCompleted.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isSubmitting by viewModel.isSubmitting.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    val scope = rememberCoroutineScope()
    val audioRecorder = remember { AudioRecorder(context) }
    
    // MediaPlayer 추가
    var mediaPlayer by remember { mutableStateOf<android.media.MediaPlayer?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var playbackDuration by remember { mutableStateOf(0) } // 총 재생 시간 (초)
    var playbackCurrentPosition by remember { mutableStateOf(0) } // 현재 재생 위치 (초)

    // AudioRecorder 상태 관찰
    val audioRecorderState by audioRecorder.recordingState.collectAsStateWithLifecycle()
    
    // 재생 진행 시간 업데이트
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(100) // 0.1초마다 업데이트
            mediaPlayer?.let { player ->
                try {
                    playbackCurrentPosition = player.currentPosition / 1000
                } catch (e: Exception) {
                    println("AssignmentScreen - Error getting playback position: ${e.message}")
                }
            }
        }
    }

    // 응답 결과 표시를 위한 상태
    var showResult by remember { mutableStateOf(false) }
    var isAnswerCorrect by remember { mutableStateOf(false) }
    var currentQuestionAnswer by remember { mutableStateOf("") }
    var currentTailQuestionNumber by remember { mutableStateOf<String?>(null) }
    var lastProcessedQuestionIndex by remember { mutableStateOf(-1) }
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
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            println("AssignmentScreen - All permissions granted")
        } else {
            println("AssignmentScreen - Some permissions denied")
        }
    }
    
    // 현재 문제 가져오기
    val currentQuestion = viewModel.getCurrentQuestion()
    
    // Personal Assignment ID를 사용하여 모든 기본 문제 로드
    // 완료 상태를 추적하여 무한 루프 방지
    var hasAttemptedLoad by remember { mutableStateOf(false) }
    
    // 과제 완료 상태 감지
    if (isAssignmentCompleted) {
        println("AssignmentScreen - Assignment completed, showing completion screen")
    } else if (personalAssignmentQuestions.isEmpty() && !hasAttemptedLoad && !isLoading) {
        LaunchedEffect(Unit) {
            println("AssignmentScreen - Loading all questions for personalAssignmentId: $assignmentId")
            println("AssignmentScreen - Assignment title: $assignmentTitle")
            hasAttemptedLoad = true
            viewModel.loadAllQuestions(assignmentId)
            // 통계도 함께 로드 (진행률 계산용)
            viewModel.loadPersonalAssignmentStatistics(assignmentId)
        }
    }
    
    // 무한 로딩 방지: 이미 시도했고 로딩 중이 아니면 다시 시도하지 않음
    LaunchedEffect(hasAttemptedLoad, isLoading) {
        if (hasAttemptedLoad && !isLoading && personalAssignmentQuestions.isEmpty()) {
            println("AssignmentScreen - Prevented infinite loading: hasAttemptedLoad=$hasAttemptedLoad, isLoading=$isLoading, questionsEmpty=${personalAssignmentQuestions.isEmpty()}")
        }
    }
    
    // 초기 질문의 정답 설정
    LaunchedEffect(currentQuestion) {
        currentQuestion?.let {
            if (currentTailQuestionNumber == null) {
                currentQuestionAnswer = it.answer
            }
        }
    }
    
    
    // 응답 결과 처리 - 새로운 응답이 올 때마다 처리
    LaunchedEffect(answerSubmissionResponse) {
        answerSubmissionResponse?.let { response ->
            val responseNumberStr = response.numberStr
            
            // 동일한 응답을 이미 처리했는지 확인
            if (responseNumberStr != null && lastProcessedResponseNumberStr == responseNumberStr) {
                println("AssignmentScreen - Already processed this response: $responseNumberStr, skipping")
                return@let
            }
            
            println("AssignmentScreen - Processing new response: $responseNumberStr")
            println("AssignmentScreen - Current tail question number: $currentTailQuestionNumber")
            println("AssignmentScreen - Current saved tail question: ${savedTailQuestion?.question}")
            
            // 응답 처리 표시
            lastProcessedResponseNumberStr = responseNumberStr
            isAnswerCorrect = response.isCorrect
            showResult = true
            
            // tailQuestion이 null이면 완료 가능한 상태 (사용자가 완료 버튼을 눌러야 함)
            if (response.tailQuestion == null) {
                println("AssignmentScreen - No tail question, completion available")
                // 완료 가능한 상태로 설정 (자동 완료하지 않음)
                return@let
            }
            
            // numberStr이 null이면 과제 완료
            if (response.numberStr == null) {
                println("AssignmentScreen - Assignment completed (numberStr is null)")
                // 과제 완료 상태로 설정
                viewModel.setAssignmentCompleted(true)
                return@let
            }
            
            // numberStr이 하이픈을 포함하면 꼬리 질문, 아니면 다음 기본 질문
            val isTailQuestion = response.numberStr?.contains("-") == true
            
            println("AssignmentScreen - Processing response: numberStr=${response.numberStr}, isTailQuestion=$isTailQuestion, tailQuestion=${response.tailQuestion?.question}")
            
            if (isTailQuestion) {
                // 꼬리 질문인 경우
                currentTailQuestionNumber = response.numberStr
                savedTailQuestion = response.tailQuestion
                println("AssignmentScreen - This is a tail question: ${response.numberStr}")
            } else {
                // 다음 기본 질문인 경우 (꼬리 질문에서 정답을 맞춘 경우)
                currentTailQuestionNumber = null
                savedTailQuestion = null
                println("AssignmentScreen - Next base question available: ${response.numberStr}")
                
                // 서버에서 받은 numberStr이 현재 질문과 다르면 서버에서 해당 질문을 로드
                val currentQuestionNumber = currentQuestion?.number
                val serverQuestionNumber = response.numberStr
                
                if (currentQuestionNumber != serverQuestionNumber && serverQuestionNumber != null) {
                    println("AssignmentScreen - Question number mismatch: current=$currentQuestionNumber, server=$serverQuestionNumber")
                    println("AssignmentScreen - Loading question $serverQuestionNumber from server")
                    
                    // 통계는 submitAnswer 후에 이미 갱신되었으므로, 바로 moveToQuestionByNumber 호출
                    // moveToQuestionByNumber 내부에서 isLoading 체크를 하므로 여기서는 바로 호출
                    assignmentId?.let { id ->
                        scope.launch {
                            // submitAnswer 및 통계 갱신이 완료될 때까지 짧게 대기
                            delay(300)
                            
                            println("AssignmentScreen - Calling moveToQuestionByNumber: $serverQuestionNumber, personalAssignmentId: $id")
                            viewModel.moveToQuestionByNumber(serverQuestionNumber, id)
                        }
                    }
                }
            }
            
            println("AssignmentScreen - Answer result: isCorrect=${response.isCorrect}, numberStr=${response.numberStr}")
            println("AssignmentScreen - isTailQuestion: $isTailQuestion")
            println("AssignmentScreen - Saved tail question: ${response.tailQuestion?.question}")
        }
    }
    
    // AudioRecorder 상태 변화 감지 및 ViewModel 동기화
    LaunchedEffect(audioRecorderState.isRecordingComplete) {
        if (audioRecorderState.isRecordingComplete && audioRecorderState.audioFilePath != null) {
            println("AssignmentScreen - AudioRecorder completed, updating ViewModel with file path: ${audioRecorderState.audioFilePath}")
            viewModel.stopRecordingWithFilePath(audioRecorderState.audioFilePath!!)
        }
    }
    
    // 녹음 시간 업데이트 - 개선된 로직
    LaunchedEffect(audioRecordingState.isRecording) {
        if (!audioRecordingState.isRecording) return@LaunchedEffect
        
        println("AssignmentScreen - Starting recording timer")
        // 녹음이 시작되면 타이머 시작
        while (audioRecordingState.isRecording) {
            delay(1000)
            // 상태를 다시 확인하여 녹음이 여전히 진행 중인지 체크
            if (audioRecordingState.isRecording) {
                viewModel.updateRecordingDuration(audioRecordingState.recordingTime + 1)
            } else {
                println("AssignmentScreen - Recording stopped, timer exiting")
                break
            }
        }
        println("AssignmentScreen - Recording timer ended")
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = PrimaryIndigo
            )
        }
    } else if (isAssignmentCompleted || currentQuestion == null || personalAssignmentQuestions.isEmpty()) {
        // 퀴즈 완료 화면
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            VTCard(
                variant = CardVariant.Elevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AssignmentTurnedIn,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(80.dp)
                    )
                    Text(
                        text = "과제 완료!",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = Gray800,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "모든 문제를 완료했습니다.\n수고하셨습니다!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Gray600,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    VTButton(
                        text = "홈으로 돌아가기",
                        onClick = {
                            println("AssignmentScreen - Navigating to home")
                            onNavigateToHome()
                        },
                        variant = ButtonVariant.Gradient,
                        modifier = Modifier.fillMaxWidth()
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress - API에서 받은 통계를 사용하여 진행률 계산
            // total_problem과 solved_problem을 사용하여 진행률 계산
            val totalProblems = personalAssignmentStatistics?.totalProblem ?: 0
            val solvedProblems = personalAssignmentStatistics?.solvedProblem ?: 0
            val progress = if (totalProblems > 0) {
                (solvedProblems.toFloat() / totalProblems.toFloat()).coerceIn(0f, 1f)
            } else 0f
            
            VTProgressBar(
                progress = progress,
                showPercentage = true,
                modifier = Modifier.fillMaxWidth()
            )
            
            // 진행률 텍스트 표시 (선택사항)
            Text(
                text = "${solvedProblems} / ${totalProblems}",
                style = MaterialTheme.typography.bodySmall,
                color = Gray600,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Question card
            VTCard(
                variant = CardVariant.Elevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Question number - showResult가 false일 때만 질문 번호 표시
                    if (!showResult) {
                        val questionNumber = currentTailQuestionNumber?.let { tailNumber ->
                            if (tailNumber.contains("-")) "꼬리 질문 $tailNumber" else "질문 $tailNumber"
                        } ?: run {
                            // 서버 응답이 있으면 서버 응답의 numberStr을 우선 사용
                            val response = answerSubmissionResponse
                            if (response?.numberStr != null && !response.numberStr.contains("-")) {
                                "질문 ${response.numberStr}"
                            } else if (currentQuestion.number.contains("-")) {
                                "꼬리 질문 ${currentQuestion.number}"
                            } else {
                                "질문 ${currentQuestion.number}"
                            }
                        }
                        
                        println("AssignmentScreen - Displaying question number: $questionNumber")
                        println("AssignmentScreen - currentTailQuestionNumber: $currentTailQuestionNumber")
                        println("AssignmentScreen - currentQuestion.number: ${currentQuestion.number}")
                        println("AssignmentScreen - answerSubmissionResponse.numberStr: ${answerSubmissionResponse?.numberStr}")
                        
                        Text(
                            text = questionNumber,
                            style = MaterialTheme.typography.labelLarge,
                            color = PrimaryIndigo,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Question text - showResult가 false일 때만 질문 표시
                    val response = answerSubmissionResponse
                    val questionText = when {
                        // 결과 화면이 아니라면
                        !showResult -> {
                            // 새로운 응답이 있고 꼬리 질문인 경우
                            if (response != null && response.numberStr?.contains("-") == true) {
                                response.tailQuestion?.question ?: currentQuestion.question
                            }
                            // 꼬리 질문 번호가 설정되어 있는 경우 (꼬리 질문으로 넘어간 상태)
                            else if (currentTailQuestionNumber != null) {
                                // 저장된 꼬리 질문 표시 (새로운 응답이 오면 savedTailQuestion이 업데이트됨)
                                savedTailQuestion?.question ?: currentQuestion.question
                            }
                            // 기본 질문 표시
                            else {
                                currentQuestion.question
                            }
                        }
                        // 결과 화면에서는 질문 표시하지 않음
                        else -> ""
                    }
                    
                    println("AssignmentScreen - Displaying question text: $questionText")
                    println("AssignmentScreen - showResult: $showResult")
                    println("AssignmentScreen - currentTailQuestionNumber: $currentTailQuestionNumber")
                    println("AssignmentScreen - tailQuestion exists: ${response?.tailQuestion != null}")
                    println("AssignmentScreen - savedTailQuestion exists: ${savedTailQuestion != null}")
                    println("AssignmentScreen - savedTailQuestion text: ${savedTailQuestion?.question}")
                    
                    // 질문 텍스트는 showResult가 false일 때만 표시
                    if (!showResult && questionText.isNotEmpty()) {
                        Text(
                            text = questionText,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Gray800
                        )
                    }
                    
                    // 응답 결과 표시
                    if (showResult) {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 정답/오답 표시
                        VTCard(
                            variant = CardVariant.Outlined,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isAnswerCorrect) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                        contentDescription = null,
                                        tint = if (isAnswerCorrect) Success else Error,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Text(
                                        text = if (isAnswerCorrect) "정답입니다!" else "틀렸습니다",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAnswerCorrect) Success else Error
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Recording and send area
            VTCard(variant = CardVariant.Outlined) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 응답 결과가 없을 때만 녹음 관련 UI 표시
                    if (!showResult) {
                        // Recording status
                        if (audioRecordingState.isRecording) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.RadioButtonChecked,
                                    contentDescription = null,
                                    tint = Error,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "녹음 중... ${String.format("%02d:%02d", audioRecordingState.recordingTime / 60, audioRecordingState.recordingTime % 60)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (audioRecordingState.audioFilePath != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // 녹음 완료 표시
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CheckCircle,
                                        contentDescription = null,
                                        tint = Success,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "녹음 완료 (${String.format("%02d:%02d", audioRecordingState.recordingTime / 60, audioRecordingState.recordingTime % 60)})",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = Success,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                // 음성 다시 듣기 아이콘 버튼
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (isPlaying) {
                                        Text(
                                            text = "${String.format("%02d:%02d", playbackCurrentPosition / 60, playbackCurrentPosition % 60)} / ${String.format("%02d:%02d", playbackDuration / 60, playbackDuration % 60)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Error,
                                            fontWeight = FontWeight.Bold
                                        )
                                    } else {
                                        Text(
                                            text = "다시 듣기",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = PrimaryIndigo,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val audioFilePath = audioRecordingState.audioFilePath
                                            if (audioFilePath != null) {
                                                if (isPlaying) {
                                                    // 재생 중지
                                                    println("AssignmentScreen - Stopping audio playback")
                                                    mediaPlayer?.stop()
                                                    mediaPlayer?.release()
                                                    mediaPlayer = null
                                                    isPlaying = false
                                                    playbackCurrentPosition = 0
                                                } else {
                                                    // 재생 시작
                                                    println("AssignmentScreen - Starting audio playback: $audioFilePath")
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
                                                                println("AssignmentScreen - Audio playback completed")
                                                                isPlaying = false
                                                                playbackCurrentPosition = 0
                                                                release()
                                                                mediaPlayer = null
                                                            }

                                                            setOnErrorListener { _, what, extra ->
                                                                println("AssignmentScreen - Audio playback error: what=$what, extra=$extra")
                                                                isPlaying = false
                                                                playbackCurrentPosition = 0
                                                                release()
                                                                mediaPlayer = null
                                                                true
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        println("AssignmentScreen - Error starting audio playback: ${e.message}")
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
                                                shape = androidx.compose.foundation.shape.CircleShape
                                            )
                                            .size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isPlaying) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                            contentDescription = if (isPlaying) "재생 중지" else "음성 재생",
                                            tint = if (isPlaying) Error else PrimaryIndigo,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // 녹음 버튼 - 녹음 시작과 건너뛰기를 한 줄에 배치
                        if (audioRecordingState.audioFilePath == null && !audioRecordingState.isRecording) {
                            // 녹음 전: 녹음 시작 + 건너뛰기
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                VTButton(
                                    text = "녹음 시작",
                                    onClick = {
                                        // 녹음 시작 전 권한 체크
                                        if (!PermissionUtils.hasAudioPermission(context)) {
                                            println("AssignmentScreen - Requesting audio permission")
                                            permissionLauncher.launch(PermissionUtils.getRequiredPermissions())
                                        } else {
                                            println("AssignmentScreen - Starting recording")
                                            val success = audioRecorder.startRecording()
                                            if (success) {
                                                viewModel.startRecording()
                                            } else {
                                                println("AssignmentScreen - Failed to start recording")
                                            }
                                        }
                                    },
                                    variant = ButtonVariant.Gradient,
                                    enabled = true,
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Mic,
                                            contentDescription = null
                                        )
                                    }
                                )

                                VTButton(
                                    text = "건너뛰기",
                                    onClick = {
                                        println("AssignmentScreen - Skip button clicked")
                                        scope.launch {
                                            try {
                                                // 빈 WAV 파일 생성
                                                val emptyFile = audioRecorder.createEmptyWavFile()
                                                if (emptyFile != null && currentUser != null && currentQuestion != null) {
                                                    println("AssignmentScreen - Empty WAV file created: ${emptyFile.absolutePath}")

                                                    // 꼬리 질문이면 꼬리 질문의 ID를, 아니면 현재 질문의 ID를 사용
                                                    val questionIdToSubmit = if (currentTailQuestionNumber != null && savedTailQuestion != null) {
                                                        savedTailQuestion!!.id
                                                    } else {
                                                        currentQuestion.id
                                                    }

                                                    println("AssignmentScreen - Submitting skip answer with questionId: $questionIdToSubmit")

                                                    // 바로 전송
                                                    val personalAssignmentId = assignmentId
                                                    if (personalAssignmentId != null) {
                                                        viewModel.submitAnswer(
                                                            personalAssignmentId = personalAssignmentId,
                                                            studentId = currentUser!!.id,
                                                            questionId = questionIdToSubmit,
                                                            audioFile = emptyFile
                                                        )

                                                        // 녹음 상태 초기화
                                                        viewModel.resetAudioRecording()
                                                        println("AssignmentScreen - Skip answer submitted successfully")
                                                    } else {
                                                        println("AssignmentScreen - Cannot submit: personalAssignmentId is null")
                                                    }
                                                } else {
                                                    println("AssignmentScreen - Failed to create empty WAV file or missing required data")
                                                }
                                            } catch (e: Exception) {
                                                println("AssignmentScreen - Error in skip: ${e.message}")
                                            }
                                        }
                                    },
                                    variant = ButtonVariant.Outline,
                                    enabled = !isSubmitting,
                                    modifier = Modifier.weight(1f),
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.SkipNext,
                                            contentDescription = null
                                        )
                                    }
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
                                        // 녹음 중지
                                        println("AssignmentScreen - Stopping recording")
                                        try {
                                            viewModel.stopRecordingImmediately()
                                            scope.launch {
                                                try {
                                                    audioRecorder.stopRecording()
                                                    println("AssignmentScreen - AudioRecorder stopped successfully")
                                                } catch (e: Exception) {
                                                    println("AssignmentScreen - Error in AudioRecorder.stopRecording(): ${e.message}")
                                                }
                                            }
                                        } catch (e: Exception) {
                                            println("AssignmentScreen - Error stopping recording: ${e.message}")
                                            viewModel.resetAudioRecording()
                                        }
                                    } else {
                                        // 다시 녹음하기
                                        println("AssignmentScreen - Clearing existing recording and starting new one")
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
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }

                    // Send button - 응답 결과에 따라 다른 버튼 표시
                    if (showResult) {
                        // 응답 결과가 있을 때
                        // numberStr이 하이픈을 포함하는지 확인
                        val response = answerSubmissionResponse
                        val isTailQuestionNum = response?.numberStr?.contains("-") == true
                        
                        if (isTailQuestionNum) {
                            // 꼬리 질문이 있는 경우 - 무조건 꼬리질문으로 넘어가기 버튼 표시
                            VTButton(
                                text = "꼬리질문으로 넘어가기",
                                onClick = {
                                    // 꼬리 질문 상태로 전환
                                    // clearAnswerSubmissionResponse는 호출하지 않음 (tailQuestion 정보 유지)
                                    showResult = false
                                    // currentTailQuestionNumber와 savedTailQuestion은 유지 (이미 설정됨)
                                    
                                    println("AssignmentScreen - Moving to tail question: $currentTailQuestionNumber")
                                    println("AssignmentScreen - Saved tail question: ${savedTailQuestion?.question}")
                                },
                                variant = ButtonVariant.Gradient,
                                fullWidth = true
                            )
                        } else {
                            // 꼬리 질문이 없는 경우
                            val response = answerSubmissionResponse
                            val isTailQuestionNum = response?.numberStr?.contains("-") == true
                            
                            if (response?.tailQuestion == null) {
                                // tailQuestion이 null인 경우 - 완료 버튼 표시
                                VTButton(
                                    text = "완료",
                                    onClick = {
                                        // println("AssignmentScreen - Completion button pressed")
                                        // 과제 완료 API 호출
//                                        assignmentId?.let { id ->
//                                            viewModel.completeAssignment(id)
//                                        }
                                        // 과제 완료 상태로 설정하여 완료 화면 표시
                                        viewModel.setAssignmentCompleted(true)
                                    },
                                    variant = ButtonVariant.Gradient,
                                    fullWidth = true
                                )
                            } else if (response?.numberStr == null) {
                                // 과제 완료인 경우 - 홈으로 돌아가기 버튼 표시
                                VTButton(
                                    text = "홈으로 돌아가기",
                                    onClick = {
                                        println("AssignmentScreen - Assignment completed, navigating to home")
                                        onNavigateToHome()
                                    },
                                    variant = ButtonVariant.Gradient,
                                    fullWidth = true
                                )
                            } else if (isTailQuestionNum) {
                                // 꼬리 질문인 경우 - 꼬리질문으로 넘어가기 버튼 표시
                                VTButton(
                                    text = "꼬리질문으로 넘어가기",
                                    onClick = {
                                        // 꼬리 질문 상태로 전환
                                        showResult = false
                                        currentTailQuestionNumber = response.numberStr
                                        savedTailQuestion = response.tailQuestion
                                        
                                        println("AssignmentScreen - Moving to tail question: ${response.numberStr}")
                                        println("AssignmentScreen - Saved tail question: ${response.tailQuestion?.question}")
                                    },
                                    variant = ButtonVariant.Gradient,
                                    fullWidth = true
                                )
                            } else {
                                // 다음 기본 질문인 경우 - 다음 문제로 넘어가기 버튼 표시
                                VTButton(
                                    text = "다음 문제",
                                    onClick = {
                                        println("AssignmentScreen - Moving to next question")
                                        viewModel.clearAnswerSubmissionResponse()
                                        showResult = false
                                        currentTailQuestionNumber = null
                                        savedTailQuestion = null
                                        
                                        // 서버에서 받은 number_str을 기반으로 올바른 질문으로 이동
                                        val numberStr = response?.numberStr
                                        if (numberStr != null) {
                                            println("AssignmentScreen - Moving to question number: $numberStr")
                                            viewModel.moveToQuestionByNumber(numberStr, assignmentId)
                                        } else {
                                            // numberStr이 없는 경우 기존 로직 사용
                                            val tailQuestion = response?.tailQuestion
                                            if (tailQuestion != null) {
                                                println("AssignmentScreen - Using tailQuestion as next question: ${tailQuestion.question}")
                                                // tailQuestion을 PersonalAssignmentQuestion으로 변환하여 리스트에 추가
                                                val nextQuestion = PersonalAssignmentQuestion(
                                                    id = tailQuestion.id,
                                                    number = tailQuestion.number.toString(),
                                                    question = tailQuestion.question,
                                                    answer = tailQuestion.answer,
                                                    explanation = tailQuestion.explanation,
                                                    difficulty = tailQuestion.difficulty
                                                )
                                                
                                                // 현재 질문 리스트에 다음 질문 추가
                                                val currentQuestions = viewModel.personalAssignmentQuestions.value.toMutableList()
                                                currentQuestions.add(nextQuestion)
                                                viewModel.updatePersonalAssignmentQuestions(currentQuestions)
                                                
                                                // 다음 질문으로 이동
                                                scope.launch {
                                                    delay(100)
                                                    viewModel.nextQuestion()
                                                }
                                            } else {
                                                // 로컬 리스트에서 다음 문제로 이동
                                                scope.launch {
                                                    delay(100)
                                                    viewModel.nextQuestion()
                                                }
                                            }
                                        }
                                    },
                                    variant = ButtonVariant.Gradient,
                                    fullWidth = true
                                )
                            }
                        }
                    } else {
                        // 전송 버튼
                        VTButton(
                            text = "전송",
                            onClick = {
                                println("AssignmentScreen - Send button clicked")
                                println("AssignmentScreen - audioFilePath: ${audioRecordingState.audioFilePath}")
                                println("AssignmentScreen - isRecording: ${audioRecordingState.isRecording}")
                                println("AssignmentScreen - isRecordingComplete: ${audioRecordingState.isRecordingComplete}")
                                
                                // 코루틴 스코프를 안전하게 처리
                                val user = currentUser
                                val audioFilePath = audioRecordingState.audioFilePath
                                if (audioFilePath != null && user != null && currentQuestion != null) {
                                    println("AssignmentScreen - Sending answer for question ${currentQuestionIndex + 1}")
                                    
                                    // API로 답변 전송
                                    val audioFile = File(audioFilePath)
                                    println("AssignmentScreen - Audio file exists: ${audioFile.exists()}")
                                    println("AssignmentScreen - Audio file size: ${audioFile.length()} bytes")
                                    println("AssignmentScreen - Recording duration: ${audioRecordingState.recordingTime} seconds")
                                    
                                    try {
                                        // SimpleAudioRecorder는 이미 3GP 형식으로 녹음하므로 변환 불필요
                                        val finalAudioFile = audioFile
                                        
                                        println("AssignmentScreen - Using 3GP audio file: ${finalAudioFile.absolutePath}")
                                        println("AssignmentScreen - File size: ${finalAudioFile.length()} bytes")
                                        // 꼬리 질문이면 꼬리 질문의 ID를, 아니면 현재 질문의 ID를 사용
                                        val questionIdToSubmit = if (currentTailQuestionNumber != null && savedTailQuestion != null) {
                                            savedTailQuestion!!.id
                                        } else {
                                            currentQuestion.id
                                        }
                                        
                                        println("AssignmentScreen - Submitting answer with questionId: $questionIdToSubmit")
                                        if (currentTailQuestionNumber != null) {
                                            println("AssignmentScreen - This is a tail question submission: $currentTailQuestionNumber")
                                        }
                                        
                                        // assignmentId는 PersonalAssignment ID
                                        val personalAssignmentId = assignmentId
                                        
                                        println("AssignmentScreen - Submitting answer with personal_assignment_id: $personalAssignmentId, questionId: $questionIdToSubmit")
                                        
                                        if (personalAssignmentId != null) {
                                            viewModel.submitAnswer(
                                                personalAssignmentId = personalAssignmentId,
                                                studentId = user.id,
                                                questionId = questionIdToSubmit,
                                                audioFile = finalAudioFile
                                            )
                                        } else {
                                            println("AssignmentScreen - Cannot submit: personalAssignmentId is null")
                                        }
                                        
                                        println("AssignmentScreen - submitAnswer called successfully")
                                        
                                        // 녹음 상태 초기화
                                        viewModel.resetAudioRecording()
                                        println("AssignmentScreen - Recording state reset")
                                    } catch (e: Exception) {
                                        println("AssignmentScreen - Error in submitAnswer: ${e.message}")
                                    }
                                } else {
                                    println("AssignmentScreen - Cannot send: conditions not met")
                                    println("  - audioFilePath null: ${audioRecordingState.audioFilePath == null}")
                                    println("  - user null: ${user == null}")
                                    println("  - currentQuestion null: ${currentQuestion == null}")
                                }
                            },
                            variant = ButtonVariant.Gradient,
                            fullWidth = true,
                            enabled = audioRecordingState.audioFilePath != null && !audioRecordingState.isRecording
                        )
                    }
                }
            }
        }

        // 채점 중 오버레이 로딩
        if (isSubmitting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "채점 중...",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun AssignmentQuizScreen(
    assignmentId: Int = 1,
    assignmentTitle: String,
    onNavigateToHome: () -> Unit = {}
) {
    // 모든 퀴즈는 음성 답변 + AI 대화형 꼬리 질문 형태로 진행
    AssignmentContinuousScreen(
        assignmentId = assignmentId,
        assignmentTitle = assignmentTitle,
        onNavigateToHome = onNavigateToHome
    )
}


@Composable
fun ConversationBubble(
    message: String
) {
    val isUser = true
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isUser) PrimaryIndigo else Gray200,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                )
                .padding(12.dp)
                .widthIn(max = 280.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) Color.White else Gray800
            )
        }
    }
}

@Composable
fun OptionButton(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isWrong: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isCorrect -> Success.copy(alpha = 0.1f)
        isWrong -> Error.copy(alpha = 0.1f)
        isSelected -> PrimaryIndigo.copy(alpha = 0.1f)
        else -> Color.Transparent
    }
    
    val borderColor = when {
        isCorrect -> Success
        isWrong -> Error
        isSelected -> PrimaryIndigo
        else -> Gray300
    }
    
    val textColor = when {
        isCorrect -> Success
        isWrong -> Error
        isSelected -> PrimaryIndigo
        else -> Gray800
    }
    
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = textColor,
            containerColor = backgroundColor
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = borderColor
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AssignmentScreenPreview() {
    VoiceTutorTheme {
        AssignmentScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun AssignmentQuizScreenPreview() {
    VoiceTutorTheme {
        AssignmentQuizScreen(assignmentTitle = "화학 - 원소주기율표 퀴즈")
    }
}