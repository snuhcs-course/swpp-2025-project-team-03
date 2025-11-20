package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.utils.TutorialPreferences
import com.example.voicetutor.utils.formatDueDate

@Composable
fun StudentDashboardScreen(
    authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel? = null,
    assignmentViewModel: AssignmentViewModel? = null,
    onNavigateToAssignment: (String) -> Unit = {},
    onNavigateToAssignmentDetail: (String) -> Unit = {},
) {
    val viewModelAssignment = assignmentViewModel ?: hiltViewModel()
    val viewModelAuth = authViewModel ?: hiltViewModel()

    val assignments by viewModelAssignment.assignments.collectAsStateWithLifecycle()
    val isLoading by viewModelAssignment.isLoading.collectAsStateWithLifecycle()
    val error by viewModelAssignment.error.collectAsStateWithLifecycle()
    val currentUser by viewModelAuth.currentUser.collectAsStateWithLifecycle()

    val studentName = currentUser?.name ?: "학생"

    val context = LocalContext.current
    val tutorialPrefs = remember { TutorialPreferences(context) }
    var showTutorial by remember { mutableStateOf(false) }

    // 튜토리얼은 회원가입 시 또는 설정에서 초기화 후 로그인 시에만 표시
    // currentUser와 showTutorial을 모두 key로 사용하여 초기화 후에도 재확인
    LaunchedEffect(currentUser, showTutorial) {
        if (currentUser != null && !showTutorial) {
            val isNewUser = tutorialPrefs.isNewUser()
            if (isNewUser) {
                showTutorial = true
            }
        }
    }

    // 화면 재포커스 시 튜토리얼 상태 재확인
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, currentUser) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && currentUser != null && !showTutorial) {
                val isNewUser = tutorialPrefs.isNewUser()
                if (isNewUser) {
                    showTutorial = true
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var selectedFilter by remember { mutableStateOf(PersonalAssignmentFilter.ALL) }

    val validAssignments = remember(assignments, selectedFilter) {
        val filtered = assignments.filter {
            it.totalQuestions > 0 &&
                it.personalAssignmentStatus != null &&
                it.personalAssignmentId != null
        }

        when (selectedFilter) {
            PersonalAssignmentFilter.ALL -> filtered
            PersonalAssignmentFilter.NOT_STARTED -> filtered.filter {
                it.personalAssignmentStatus == PersonalAssignmentStatus.NOT_STARTED
            }
            PersonalAssignmentFilter.IN_PROGRESS -> filtered.filter {
                it.personalAssignmentStatus == PersonalAssignmentStatus.IN_PROGRESS
            }
            else -> filtered
        }
    }

    // 각 상태별 개수 계산
    val allAssignmentsCount = remember(assignments) {
        assignments.count {
            it.totalQuestions > 0 &&
                it.personalAssignmentStatus != null &&
                it.personalAssignmentId != null
        }
    }

    val notStartedCount = remember(assignments) {
        assignments.count {
            it.totalQuestions > 0 &&
                it.personalAssignmentStatus == PersonalAssignmentStatus.NOT_STARTED &&
                it.personalAssignmentId != null
        }
    }

    val inProgressCount = remember(assignments) {
        assignments.count {
            it.totalQuestions > 0 &&
                it.personalAssignmentStatus == PersonalAssignmentStatus.IN_PROGRESS &&
                it.personalAssignmentId != null
        }
    }

    LaunchedEffect(currentUser) {
        val user = currentUser
        if (user != null) {
            viewModelAssignment.loadPendingStudentAssignments(user.id)
        } else {
            kotlinx.coroutines.delay(500)
            val retryUser = viewModelAuth.currentUser.value
            if (retryUser != null) {
                viewModelAssignment.loadPendingStudentAssignments(retryUser.id)
            }
        }
    }

    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            viewModelAssignment.clearError()
        }
    }

    if (showTutorial) {
        OnboardingPager(
            pages = StudentOnboardingData.studentOnboardingPages,
            onComplete = {
                tutorialPrefs.setStudentTutorialCompleted()
                tutorialPrefs.clearNewUserFlag()
                showTutorial = false
            },
            onSkip = {
                tutorialPrefs.setStudentTutorialCompleted()
                tutorialPrefs.clearNewUserFlag()
                showTutorial = false
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Welcome section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = PrimaryIndigo.copy(alpha = 0.08f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                )
                .padding(20.dp),
        ) {
            Column {
                Text(
                    text = currentUser?.welcomeMessage ?: "안녕하세요, ${studentName}님!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentUser?.subMessage ?: "오늘도 VoiceTutor와 함께 학습을 시작해볼까요?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                )
            }
        }

        // My assignments
        Column {
            Column {
                Text(
                    text = "나에게 할당된 과제",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800,
                )
                Text(
                    text = "진행 중인 과제와 새로 배정된 과제를 확인해보세요",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = selectedFilter == PersonalAssignmentFilter.ALL,
                    onClick = { selectedFilter = PersonalAssignmentFilter.ALL },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("전체")
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        color = PrimaryIndigo,
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "$allAssignmentsCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.List,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )

                FilterChip(
                    selected = selectedFilter == PersonalAssignmentFilter.NOT_STARTED,
                    onClick = { selectedFilter = PersonalAssignmentFilter.NOT_STARTED },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("시작 안함")
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        color = PrimaryIndigo,
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "$notStartedCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    },
                )

                FilterChip(
                    selected = selectedFilter == PersonalAssignmentFilter.IN_PROGRESS,
                    onClick = { selectedFilter = PersonalAssignmentFilter.IN_PROGRESS },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("진행 중")
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        color = PrimaryIndigo,
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "$inProgressCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Student assignments from API
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = PrimaryIndigo,
                    )
                }
            } else if (validAssignments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Assignment,
                            contentDescription = null,
                            tint = Gray400,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "과제가 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600,
                        )
                    }
                }
            } else {
                validAssignments.forEachIndexed { index, assignment ->
                    // 진행률 계산: solvedNum은 기본 질문에 답변한 개수 (꼬리 질문 제외)
                    val progress = if (assignment.totalQuestions > 0 && assignment.solvedNum != null) {
                        (assignment.solvedNum.toFloat() / assignment.totalQuestions.toFloat()).coerceIn(0f, 1f)
                    } else {
                        0f
                    }

                    StudentAssignmentCard(
                        title = assignment.title,
                        subject = assignment.courseClass.subject.name,
                        className = assignment.courseClass.name,
                        dueDate = formatDueDate(assignment.dueAt),
                        progress = progress,
                        solvedNum = assignment.solvedNum ?: 0,
                        totalQuestions = assignment.totalQuestions,
                        status = assignment.personalAssignmentStatus,
                        onClick = {
                            // assignment.id와 personalAssignmentId 모두 저장
                            viewModelAssignment.setSelectedAssignmentIds(
                                assignmentId = assignment.id,
                                personalAssignmentId = assignment.personalAssignmentId,
                            )
                            val detailId = assignment.personalAssignmentId ?: assignment.id
                            onNavigateToAssignmentDetail(detailId.toString())
                        },
                        onStartAssignment = {
                            val personalId = assignment.personalAssignmentId ?: assignment.id
                            onNavigateToAssignment(personalId.toString())
                        },
                    )

                    if (index < validAssignments.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StudentAssignmentCard(
    title: String,
    subject: String,
    dueDate: String,
    progress: Float,
    solvedNum: Int = 0,
    totalQuestions: Int = 0,
    status: PersonalAssignmentStatus? = null,
    className: String = "",
    onClick: () -> Unit = {},
    onStartAssignment: () -> Unit = {},
) {
    VTCard(
        variant = CardVariant.Elevated,
        onClick = onClick,
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    // Subject and Status Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        // Subject badge
                        if (subject.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                    .background(PrimaryIndigo.copy(alpha = 0.1f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = subject,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryIndigo,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }

                        // Class name badge
                        if (className.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                    .background(PrimaryEmerald.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = className,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryEmerald,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }

                        // Status badge
                        if (status != null) {
                            Box(
                                modifier = Modifier
                                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                                    .background(
                                        when (status) {
                                            PersonalAssignmentStatus.NOT_STARTED -> Gray400.copy(alpha = 0.1f)
                                            PersonalAssignmentStatus.IN_PROGRESS -> Warning.copy(alpha = 0.15f)
                                            else -> Gray400.copy(alpha = 0.1f)
                                        },
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = when (status) {
                                        PersonalAssignmentStatus.NOT_STARTED -> "시작 안함"
                                        PersonalAssignmentStatus.IN_PROGRESS -> "진행 중"
                                        else -> ""
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when (status) {
                                        PersonalAssignmentStatus.NOT_STARTED -> Gray400
                                        PersonalAssignmentStatus.IN_PROGRESS -> Warning
                                        else -> Gray400
                                    },
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Assignment title
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    // Due date
                    Text(
                        text = "마감: $dueDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray500,
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End,
                ) {
                    // Question count
                    Text(
                        text = "$totalQuestions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryIndigo,
                    )
                    Text(
                        text = "문제",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                    )
                }
            }

            // Progress bar
            if ((status == PersonalAssignmentStatus.IN_PROGRESS || status == PersonalAssignmentStatus.NOT_STARTED) && totalQuestions > 0) {
                Spacer(modifier = Modifier.height(10.dp))

                VTProgressBar(
                    progress = progress,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StudentDashboardScreenPreview() {
    VoiceTutorTheme {
        StudentDashboardScreen()
    }
}
