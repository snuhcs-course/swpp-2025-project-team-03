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
    assignmentId: Int? = null, // PersonalAssignment ID 사용 (PendingAssignmentsScreen에서 전달)
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
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    val scope = rememberCoroutineScope()
    
    // 동적 과제 제목 가져오기
    val dynamicAssignmentTitle = currentAssignment?.title ?: assignmentTitle ?: "과제"
    
    // Resolve personal assignment id: if null, fetch recent by student id
    val resolvedAssignmentIdState = remember { mutableStateOf<Int?>(assignmentId) }
    val recentId by viewModel.recentPersonalAssignmentId.collectAsStateWithLifecycle()
    var hasAttemptedLoadRecent by remember { mutableStateOf(false) }

    LaunchedEffect(assignmentId, currentUser?.id, recentId) {
        if (resolvedAssignmentIdState.value == null && !hasAttemptedLoadRecent) {
            // fetch recent when no id provided
            currentUser?.id?.let { sid ->
                hasAttemptedLoadRecent = true
                viewModel.loadRecentPersonalAssignment(sid)
            }
            if (recentId != null) {
                resolvedAssignmentIdState.value = recentId
            }
        }
        resolvedAssignmentIdState.value?.let { id ->
            viewModel.loadAssignmentById(id)
        }
    }
    
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
        val effectiveId = resolvedAssignmentIdState.value
        // 과제가 없을 때 처리: assignmentId가 null이고 recentId도 null이고 로딩이 완료된 경우
        if (effectiveId == null && assignmentId == null && hasAttemptedLoadRecent && !isLoading) {
            // 과제가 없습니다 메시지 표시
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Assignment,
                        contentDescription = null,
                        tint = Gray400,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "과제가 없습니다",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Gray800
                    )
                    Text(
                        text = "진행 중인 과제가 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Gray600,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    VTButton(
                        text = "홈으로 돌아가기",
                        onClick = {
                            onNavigateToHome()
                        },
                        variant = ButtonVariant.Gradient,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else if (effectiveId != null) {
            AssignmentContinuousScreen(assignmentId = effectiveId, assignmentTitle = assignmentTitle ?: "과제", authViewModel = viewModelAuth, onNavigateToHome = onNavigateToHome)
        } else {
            // Still resolving recent assignment id (초기 상태)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryIndigo)
            }
        }
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
    assignmentId: Int = 1, // PersonalAssignment ID (PendingAssignmentsScreen에서 전달)
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
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    val scope = rememberCoroutineScope()
    val audioRecorder = remember { AudioRecorder(context) }
    
    // AudioRecorder 상태 관찰
    val audioRecorderState by audioRecorder.recordingState.collectAsStateWithLifecycle()
    
    // 응답 결과 표시를 위한 상태
    var showResult by remember { mutableStateOf(false) }
    var isAnswerCorrect by remember { mutableStateOf(false) }
    var currentQuestionAnswer by remember { mutableStateOf("") }
    var currentTailQuestionNumber by remember { mutableStateOf<String?>(null) }
    var lastProcessedQuestionIndex by remember { mutableStateOf(-1) }
    var savedTailQuestion by remember { mutableStateOf<com.example.voicetutor.data.models.TailQuestion?>(null) }
    
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
    } else {
        // totalProblem이 0이면 과제에 문제가 없으므로 로드하지 않음
        val totalProblems = personalAssignmentStatistics?.totalProblem ?: -1
        if (totalProblems == 0) {
            // 통계만 로드해서 확인 (이미 완료 상태일 수 있음)
            LaunchedEffect(Unit) {
                assignmentId?.let { id ->
                    viewModel.loadPersonalAssignmentStatistics(id)
                }
            }
        } else if (assignmentId != null && personalAssignmentQuestions.isEmpty() && !hasAttemptedLoad && !isLoading && totalProblems > 0) {
            // 한 번만 로드 시도 (무한 반복 방지)
            LaunchedEffect(assignmentId) {
                // assignmentId가 변경될 때만 실행 (초기 로드 시)
                println("AssignmentScreen - Loading all questions for personalAssignmentId: $assignmentId")
                println("AssignmentScreen - Assignment title: $assignmentTitle")
                hasAttemptedLoad = true
                assignmentId?.let { id ->
                    viewModel.loadAllQuestions(id)
                    // 통계도 함께 로드 (진행률 계산용)
                    viewModel.loadPersonalAssignmentStatistics(id)
                }
            }
        }
    }
    
    // 무한 로딩 방지: 이미 시도했고 로딩 중이 아니면 다시 시도하지 않음
    LaunchedEffect(hasAttemptedLoad, isLoading, personalAssignmentQuestions.size) {
        if (hasAttemptedLoad && !isLoading && personalAssignmentQuestions.isEmpty()) {
            println("AssignmentScreen - Prevented infinite loading: hasAttemptedLoad=$hasAttemptedLoad, isLoading=$isLoading, questionsEmpty=${personalAssignmentQuestions.isEmpty()}")
            println("AssignmentScreen - Total problems: ${personalAssignmentStatistics?.totalProblem}, Solved: ${personalAssignmentStatistics?.solvedProblem}")
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
            println("AssignmentScreen - Processing new response: ${response.numberStr}")
            println("AssignmentScreen - Current tail question number: $currentTailQuestionNumber")
            println("AssignmentScreen - Current saved tail question: ${savedTailQuestion?.question}")
            
            // 새로운 응답이면 항상 처리
            println("AssignmentScreen - Setting isAnswerCorrect from response.isCorrect: ${response.isCorrect}")
            println("AssignmentScreen - Raw response data - is_correct field: ${response.isCorrect}")
            isAnswerCorrect = response.isCorrect
            println("AssignmentScreen - isAnswerCorrect set to: $isAnswerCorrect")
            showResult = true
            
            // tailQuestion이 null이면 완료 가능한 상태 (사용자가 완료 버튼을 눌러야 함)
            if (response.tailQuestion == null) {
                println("AssignmentScreen - No tail question, completion available")
                // 완료 가능한 상태로 설정 (자동 완료하지 않음)
                return@let
            }
            
            // numberStr이 null이면 통계를 확인하여 실제로 모든 문제를 풀었는지 검증
            if (response.numberStr == null) {
                println("AssignmentScreen - numberStr is null, checking if all problems are solved")
                // 통계를 먼저 갱신
                assignmentId?.let { id ->
                    viewModel.loadPersonalAssignmentStatistics(id)
                    // 통계 갱신 후 완료 여부 확인 (LaunchedEffect에서 처리)
                }
                // 여기서는 완료 상태로 설정하지 않음 (통계 확인 후 결정)
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
                
                if (currentQuestionNumber != serverQuestionNumber) {
                    println("AssignmentScreen - Question number mismatch: current=$currentQuestionNumber, server=$serverQuestionNumber")
                    println("AssignmentScreen - Loading question $serverQuestionNumber from server")
                    // 서버에서 해당 질문을 로드
                    assignmentId?.let { id ->
                        viewModel.moveToQuestionByNumber(serverQuestionNumber, id)
                        // 통계는 백그라운드에서 갱신 (질문 로딩과 별도로 처리)
                        println("AssignmentScreen - Base question answered, refreshing statistics in background")
                        viewModel.loadPersonalAssignmentStatistics(id)
                    }
                } else {
                    // 질문 번호가 같으면 통계만 갱신
                    assignmentId?.let { id ->
                        println("AssignmentScreen - Base question answered, refreshing statistics in background")
                        viewModel.loadPersonalAssignmentStatistics(id)
                    }
                }
                // 자동 이동하지 않고 사용자가 버튼을 눌러야 함
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
    
    // 통계 갱신 후 완료 여부 자동 확인 - 통계 기반으로만 완료 처리
    LaunchedEffect(personalAssignmentStatistics, answerSubmissionResponse) {
        val totalProblems = personalAssignmentStatistics?.totalProblem ?: 0
        val solvedProblems = personalAssignmentStatistics?.solvedProblem ?: 0
        
        println("AssignmentScreen - Statistics updated: Total=$totalProblems, Solved=$solvedProblems")
        
        // 절대 자동으로 완료 처리하지 않음 - totalProblem과 solvedProblem이 동일할 때만
        // numberStr이 null인 응답이 있고, 모든 문제를 풀었을 때만 완료 처리
        val response = answerSubmissionResponse
        if (response != null && response.numberStr == null) {
            println("AssignmentScreen - Response with null numberStr received, checking if all problems solved")
            if (totalProblems > 0 && totalProblems == solvedProblems) {
                println("AssignmentScreen - Verified: All problems solved (Total: $totalProblems, Solved: $solvedProblems). Setting completed.")
                viewModel.setAssignmentCompleted(true)
            } else {
                println("AssignmentScreen - NOT completed: Total=$totalProblems, Solved=$solvedProblems - waiting for all problems to be solved")
                // 완료 상태를 false로 유지
                viewModel.setAssignmentCompleted(false)
            }
        } else {
            // 응답이 없거나 numberStr이 있으면 통계만 확인하여 완료 여부 결정
            if (totalProblems > 0 && totalProblems == solvedProblems) {
                println("AssignmentScreen - All problems solved according to statistics (Total: $totalProblems, Solved: $solvedProblems)")
                // 하지만 numberStr이 null인 응답이 없으면 완료 처리하지 않음 (사용자가 완료 버튼을 눌러야 함)
            } else {
                println("AssignmentScreen - Statistics check: Total=$totalProblems, Solved=$solvedProblems - NOT all solved")
                // 완료 상태를 false로 유지
                viewModel.setAssignmentCompleted(false)
            }
        }
    }

    // 완료 화면 표시 조건: 
    // 1. isAssignmentCompleted가 true이고
    // 2. totalProblem과 solvedProblem이 동일해야 함
    val totalProblems = personalAssignmentStatistics?.totalProblem ?: 0
    val solvedProblems = personalAssignmentStatistics?.solvedProblem ?: 0
    val allProblemsSolved = totalProblems > 0 && totalProblems == solvedProblems
    val shouldShowCompletionScreen = (isAssignmentCompleted && allProblemsSolved) || 
                                     (currentQuestion == null && allProblemsSolved && personalAssignmentQuestions.isNotEmpty()) || 
                                     (personalAssignmentQuestions.isEmpty() && allProblemsSolved && totalProblems > 0)
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = PrimaryIndigo
            )
        }
    } else if (shouldShowCompletionScreen) {
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
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Success,
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
            
            // 진행률 텍스트 표시
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
                    if (!showResult && currentQuestion != null) {
                        val questionNumber = currentTailQuestionNumber?.let { tailNumber ->
                            if (tailNumber.contains("-")) "꼬리 질문 $tailNumber" else "질문 $tailNumber"
                        } ?: run {
                            // 서버 응답이 있으면 서버 응답의 numberStr을 우선 사용
                            val response = answerSubmissionResponse
                            if (response?.numberStr != null && !response.numberStr.contains("-")) {
                                "질문 ${response.numberStr}"
                            } else if (currentQuestion?.number?.contains("-") == true) {
                                "꼬리 질문 ${currentQuestion.number}"
                            } else {
                                "질문 ${currentQuestion?.number ?: ""}"
                            }
                        }
                        
                        println("AssignmentScreen - Displaying question number: $questionNumber")
                        println("AssignmentScreen - currentTailQuestionNumber: $currentTailQuestionNumber")
                        println("AssignmentScreen - currentQuestion.number: ${currentQuestion?.number}")
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
                        !showResult && currentQuestion != null -> {
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
                        // 결과 화면이거나 currentQuestion이 null인 경우 질문 표시하지 않음
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
                                    // 디버깅: 실제 표시되는 값 확인
                                    LaunchedEffect(isAnswerCorrect) {
                                        println("AssignmentScreen - Displaying result: isAnswerCorrect=$isAnswerCorrect")
                                    }
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
                                horizontalArrangement = Arrangement.Center,
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
                        }
                        
                        // Voice recorder button
                        VTButton(
                            text = when {
                                audioRecordingState.isRecording -> "녹음 중지"
                                audioRecordingState.audioFilePath != null -> "다시 녹음하기"
                                else -> "녹음 시작"
                            },
                            onClick = {
                                if (audioRecordingState.isRecording) {
                                    // 녹음 중지 - 간단하게 처리
                                    println("AssignmentScreen - Stopping recording")
                                    try {
                                        // 먼저 ViewModel 상태를 즉시 중지로 변경
                                        viewModel.stopRecordingImmediately()
                                        
                                        // 그 다음 AudioRecorder 중지 (비동기로 처리)
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
                                        // 에러가 발생해도 강제로 상태 초기화
                                        viewModel.resetAudioRecording()
                                    }
                                } else {
                                    // 녹음 시작 또는 다시 녹음하기
                                    if (audioRecordingState.audioFilePath != null) {
                                        // 기존 녹음이 있으면 초기화 후 새로 녹음
                                        println("AssignmentScreen - Clearing existing recording and starting new one")
                                        viewModel.resetAudioRecording()
                                    }
                                    
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
                                // tailQuestion이 null인 경우 - 실제로 모든 문제를 풀었는지 확인
                                val totalProblems = personalAssignmentStatistics?.totalProblem ?: 0
                                val solvedProblems = personalAssignmentStatistics?.solvedProblem ?: 0
                                val allProblemsSolved = totalProblems > 0 && totalProblems == solvedProblems
                                
                                if (allProblemsSolved) {
                                    // 모든 문제를 풀었으면 완료 버튼 표시
                                    VTButton(
                                        text = "완료",
                                        onClick = {
                                            println("AssignmentScreen - Completion button pressed")
                                            println("AssignmentScreen - Total: $totalProblems, Solved: $solvedProblems")
                                            // 통계를 다시 한번 확인 (최신 상태 확인)
                                            assignmentId?.let { id ->
                                                viewModel.loadPersonalAssignmentStatistics(id)
                                                // 통계 갱신 후 완료 처리 (LaunchedEffect에서 처리)
                                                // 여기서는 통계 갱신만 하고, LaunchedEffect에서 완료 여부 확인
                                                scope.launch {
                                                    delay(500) // 통계 갱신 대기
                                                    val updatedStats = viewModel.personalAssignmentStatistics.value
                                                    val updatedTotal = updatedStats?.totalProblem ?: 0
                                                    val updatedSolved = updatedStats?.solvedProblem ?: 0
                                                    if (updatedTotal > 0 && updatedTotal == updatedSolved) {
                                                        println("AssignmentScreen - Verified: All problems solved. Completing assignment.")
                                                        viewModel.completeAssignment(id)
                                                        viewModel.setAssignmentCompleted(true)
                                                    } else {
                                                        println("AssignmentScreen - Verification failed: Total=$updatedTotal, Solved=$updatedSolved")
                                                    }
                                                }
                                            }
                                        },
                                        variant = ButtonVariant.Gradient,
                                        fullWidth = true
                                    )
                                } else {
                                    // 아직 모든 문제를 풀지 않았으면 통계 갱신 후 다음 문제로 넘어가기
                                    VTButton(
                                        text = "다음 문제",
                                        onClick = {
                                            println("AssignmentScreen - Not all problems solved yet")
                                            println("AssignmentScreen - Total: $totalProblems, Solved: $solvedProblems")
                                            // 통계 갱신
                                            assignmentId?.let { id ->
                                                viewModel.loadPersonalAssignmentStatistics(id)
                                            }
                                            viewModel.clearAnswerSubmissionResponse()
                                            showResult = false
                                            currentTailQuestionNumber = null
                                            savedTailQuestion = null
                                        },
                                        variant = ButtonVariant.Gradient,
                                        fullWidth = true
                                    )
                                }
                            } else if (response?.numberStr == null) {
                                // numberStr이 null인 경우 - totalProblem과 solvedProblem을 확인하여 완료 여부 결정
                                val totalProblems = personalAssignmentStatistics?.totalProblem ?: 0
                                val solvedProblems = personalAssignmentStatistics?.solvedProblem ?: 0
                                val allProblemsSolved = totalProblems > 0 && totalProblems == solvedProblems
                                
                                if (allProblemsSolved) {
                                    // 모든 문제를 풀었으면 홈으로 돌아가기 버튼 표시
                                    VTButton(
                                        text = "홈으로 돌아가기",
                                        onClick = {
                                            println("AssignmentScreen - Assignment completed, navigating to home")
                                            onNavigateToHome()
                                        },
                                        variant = ButtonVariant.Gradient,
                                        fullWidth = true
                                    )
                                } else {
                                    // 아직 모든 문제를 풀지 않았으면 통계 갱신 후 다음 문제 찾기
                                    VTButton(
                                        text = "다음 문제 찾기",
                                        onClick = {
                                            println("AssignmentScreen - Not all problems solved yet")
                                            println("AssignmentScreen - Total: $totalProblems, Solved: $solvedProblems")
                                            // 통계 갱신
                                            assignmentId?.let { id ->
                                                viewModel.loadPersonalAssignmentStatistics(id)
                                            }
                                            viewModel.clearAnswerSubmissionResponse()
                                            showResult = false
                                            currentTailQuestionNumber = null
                                            savedTailQuestion = null
                                        },
                                        variant = ButtonVariant.Gradient,
                                        fullWidth = true
                                    )
                                }
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
                                if (audioRecordingState.audioFilePath != null && user != null && currentQuestion != null) {
                                    println("AssignmentScreen - Sending answer for question ${currentQuestionIndex + 1}")
                                    
                                    // API로 답변 전송
                                    val audioFile = File(audioRecordingState.audioFilePath)
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
                                        
                                        // assignmentId는 PersonalAssignment ID
                                        val personalAssignmentId = assignmentId
                                        
                                        println("AssignmentScreen - Submitting answer with personal_assignment_id: $personalAssignmentId, questionId: $questionIdToSubmit")
                                        if (currentTailQuestionNumber != null) {
                                            println("AssignmentScreen - This is a tail question submission: $currentTailQuestionNumber")
                                        }
                                        
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