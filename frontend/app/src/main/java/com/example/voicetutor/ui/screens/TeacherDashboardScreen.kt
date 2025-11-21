package com.example.voicetutor.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.voicetutor.data.models.AssignmentFilter
import com.example.voicetutor.data.models.AssignmentStatus
import com.example.voicetutor.data.models.TeacherOnboardingData
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.utils.TutorialPreferences

@Composable
fun TeacherDashboardScreen(
    authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel? = null,
    assignmentViewModel: AssignmentViewModel? = null,
    teacherId: String? = null,
    refreshTimestamp: Long = 0L,
    showDeletedToast: Boolean = false,
    onNavigateToAllAssignments: () -> Unit = {},
    onNavigateToAllStudents: () -> Unit = {},
    onNavigateToClasses: () -> Unit = {},
    onCreateNewAssignment: () -> Unit = {},
    onNavigateToCreateClass: () -> Unit = {},
    onNavigateToAssignmentDetail: (Int) -> Unit = {},
    onNavigateToAssignmentResults: (Int) -> Unit = {},
    onNavigateToEditAssignment: (Int) -> Unit = {},
) {
    val actualAssignmentViewModel: AssignmentViewModel = assignmentViewModel ?: hiltViewModel()
    val actualAuthViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = authViewModel ?: hiltViewModel()
    val dashboardViewModel: com.example.voicetutor.ui.viewmodel.DashboardViewModel = hiltViewModel()
    val studentViewModel: com.example.voicetutor.ui.viewmodel.StudentViewModel = hiltViewModel()

    val assignments by actualAssignmentViewModel.assignments.collectAsStateWithLifecycle()
    val isLoading by actualAssignmentViewModel.isLoading.collectAsStateWithLifecycle()
    val error by actualAssignmentViewModel.error.collectAsStateWithLifecycle()
    val currentUser by actualAuthViewModel.currentUser.collectAsStateWithLifecycle()
    val dashboardStats by dashboardViewModel.dashboardStats.collectAsStateWithLifecycle()
    val students by studentViewModel.students.collectAsStateWithLifecycle()
    val questionGenerationSuccess by actualAssignmentViewModel.questionGenerationSuccess.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf(AssignmentFilter.ALL) }
    
    val filteredAssignments = remember(assignments, selectedFilter) {
        val now = System.currentTimeMillis()
        when (selectedFilter) {
            AssignmentFilter.ALL -> assignments
            AssignmentFilter.IN_PROGRESS -> assignments.filter { 
                val dueTime = try {
                    java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).parse(it.dueAt)?.time ?: Long.MAX_VALUE
                } catch (e: Exception) {
                    Long.MAX_VALUE
                }
                dueTime > now
            }
            AssignmentFilter.COMPLETED -> assignments.filter { 
                val dueTime = try {
                    java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).parse(it.dueAt)?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
                dueTime <= now
            }
        }
    }
    
    val allCount = remember(assignments) {
        assignments.size
    }
    
    val inProgressCount = remember(assignments) {
        val now = System.currentTimeMillis()
        assignments.count { 
            val dueTime = try {
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).parse(it.dueAt)?.time ?: Long.MAX_VALUE
            } catch (e: Exception) {
                Long.MAX_VALUE
            }
            dueTime > now
        }
    }
    
    val completedCount = remember(assignments) {
        val now = System.currentTimeMillis()
        assignments.count { 
            val dueTime = try {
                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault()).parse(it.dueAt)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
            dueTime <= now
        }
    }

    val context = LocalContext.current
    val tutorialPrefs = remember { TutorialPreferences(context) }
    var showTutorial by remember { mutableStateOf(false) }

    LaunchedEffect(currentUser, showTutorial) {
        if (currentUser != null && !showTutorial) {
            val isNewUser = tutorialPrefs.isNewUser()

            if (isNewUser) {
                showTutorial = true
            }
        }
    }

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

    val actualTeacherId = teacherId ?: currentUser?.id?.toString()

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
    }

    LaunchedEffect(refreshTimestamp, actualTeacherId) {
        if (refreshTimestamp > 0L && actualTeacherId != null) {
            actualAssignmentViewModel.loadAllAssignments(teacherId = actualTeacherId)
            dashboardViewModel.loadDashboardData(actualTeacherId)
            studentViewModel.loadAllStudents(teacherId = actualTeacherId)
        }
    }

    LaunchedEffect(showDeletedToast) {
        if (showDeletedToast && actualTeacherId != null) {
            Toast.makeText(context, "과제가 삭제되었습니다.", Toast.LENGTH_SHORT).show()
            actualAssignmentViewModel.loadAllAssignments(teacherId = actualTeacherId)
            dashboardViewModel.loadDashboardData(actualTeacherId)
        }
    }

    LaunchedEffect(actualTeacherId) {
        if (actualTeacherId == null) {
            return@LaunchedEffect
        }

        actualAssignmentViewModel.loadAllAssignments(teacherId = actualTeacherId)
        dashboardViewModel.loadDashboardData(actualTeacherId)
        studentViewModel.loadAllStudents(teacherId = actualTeacherId)
    }

    LaunchedEffect(questionGenerationSuccess) {
        if (questionGenerationSuccess && actualTeacherId != null) {
            println("TeacherDashboardScreen - 질문 생성 완료 감지, 리스트 새로고침")
            actualAssignmentViewModel.loadAllAssignments(teacherId = actualTeacherId, status = null)
            dashboardViewModel.loadDashboardData(actualTeacherId)
        }
    }

    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            actualAssignmentViewModel.clearError()
        }
    }

    if (showTutorial) {
        OnboardingPager(
            pages = TeacherOnboardingData.teacherOnboardingPages,
            onComplete = {
                tutorialPrefs.setTeacherTutorialCompleted()
                tutorialPrefs.clearNewUserFlag()
                showTutorial = false
            },
            onSkip = {
                tutorialPrefs.setTeacherTutorialCompleted()
                tutorialPrefs.clearNewUserFlag()
                showTutorial = false
            },
        )
    }

    val dueTodayCount = remember(assignments) {
        val todayStr = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            java.time.LocalDate.now().toString() // yyyy-MM-dd
        } else {
            java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
        }
        assignments.count { a ->
            val due = a.dueAt
            due.isNotBlank() && due.length >= 10 && due.substring(0, 10) == todayStr
        }
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
                    text = currentUser?.welcomeMessage ?: "환영합니다!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = currentUser?.subMessage ?: "수업을 관리하고 학생들의 진도를 추적하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                )
            }
        }

        // Quick stats
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = PrimaryIndigo.copy(alpha = 0.08f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                )
                .padding(horizontal = 38.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                DashboardSummaryItem(
                    label = "수업",
                    value = dashboardStats?.totalClasses?.toString()
                        ?: assignments.map { it.courseClass.id }.distinct().size.toString(),
                    icon = Icons.Filled.School,
                    tint = PrimaryIndigo,
                )

                DashboardSummaryItem(
                    label = "학생",
                    value = dashboardStats?.totalStudents?.toString()
                        ?: (
                            if (students.isNotEmpty()) {
                                students.size.toString()
                            } else {
                                currentUser?.totalStudents?.toString() ?: "0"
                            }
                            ),
                    icon = Icons.Filled.People,
                    tint = Success,
                )
            }
        }

        // Quick actions
        Column {
            Text(
                text = "빠른 실행",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray800,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                VTButton(
                    text = "+ 수업 생성하기",
                    onClick = onNavigateToCreateClass,
                    variant = ButtonVariant.Primary,
                    size = ButtonSize.Medium,
                    modifier = Modifier.weight(1f),
                )

                VTButton(
                    text = "+ 과제 생성하기",
                    onClick = onCreateNewAssignment,
                    variant = ButtonVariant.Outline,
                    size = ButtonSize.Medium,
                    modifier = Modifier.weight(1f),
                )
            }

            // Spacer(modifier = Modifier.height(12.dp))

            // VTButton(
            //     text = "전체 과제 보기",
            //     onClick = onNavigateToAllAssignments,
            //     variant = ButtonVariant.Primary,
            //     size = ButtonSize.Large,
            //     modifier = Modifier
            //         .fillMaxWidth()
            //         .height(56.dp),
            //     leadingIcon = {
            //         Icon(
            //             imageVector = Icons.Filled.Assignment,
            //             contentDescription = null,
            //             modifier = Modifier.size(20.dp)
            //         )
            //     }
            // )
        }

        // Assignment management section
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "모든 과제",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = selectedFilter == AssignmentFilter.ALL,
                    onClick = { selectedFilter = AssignmentFilter.ALL },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("전체")
                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .widthIn(min = 20.dp)
                                    .background(
                                        color = PrimaryIndigo.copy(alpha = 0.7f),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                                    )
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "$allCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontWeight = FontWeight.SemiBold,
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
                    selected = selectedFilter == AssignmentFilter.IN_PROGRESS,
                    onClick = { selectedFilter = AssignmentFilter.IN_PROGRESS },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("진행중")
                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .widthIn(min = 20.dp)
                                    .background(
                                        color = PrimaryIndigo.copy(alpha = 0.7f),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                                    )
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "$inProgressCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    },
                )

                FilterChip(
                    selected = selectedFilter == AssignmentFilter.COMPLETED,
                    onClick = { selectedFilter = AssignmentFilter.COMPLETED },
                    label = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text("마감")
                            Box(
                                modifier = Modifier
                                    .height(20.dp)
                                    .widthIn(min = 20.dp)
                                    .background(
                                        color = PrimaryIndigo.copy(alpha = 0.7f),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                                    )
                                    .padding(horizontal = 6.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "$completedCount",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    },
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = PrimaryIndigo,
                    )
                }
            } else if (filteredAssignments.isEmpty()) {
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
                val assignmentStatsMap = remember { mutableStateMapOf<Int, Pair<Int, Int>>() }

                filteredAssignments.forEach { assignment ->
                    LaunchedEffect(assignment.id) {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                            val stats = actualAssignmentViewModel.getAssignmentSubmissionStats(assignment.id)
                            assignmentStatsMap[assignment.id] = stats.submittedStudents to stats.totalStudents
                        }
                    }
                }

                filteredAssignments.forEachIndexed { index, assignment ->
                    val stats = assignmentStatsMap[assignment.id] ?: (0 to assignment.courseClass.studentCount)

                    TeacherAssignmentCard(
                        title = assignment.title,
                        className = assignment.courseClass.name,
                        submittedCount = stats.first,
                        totalCount = stats.second,
                        dueDate = assignment.dueAt,
                        status = AssignmentStatus.IN_PROGRESS,
                        onClick = { onNavigateToAssignmentDetail(assignment.id) },
                        onViewResults = { onNavigateToAssignmentResults(assignment.id) },
                        onEdit = { onNavigateToEditAssignment(assignment.id) },
                    )

                    if (index < filteredAssignments.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherDashboardScreenPreview() {
    VoiceTutorTheme {
        TeacherDashboardScreen()
    }
}

@Composable
private fun DashboardSummaryItem(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = Gray700,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Gray900,
            )
        }
    }
}

@Composable
fun TeacherAssignmentCard(
    title: String,
    className: String,
    submittedCount: Int,
    totalCount: Int,
    dueDate: String,
    status: AssignmentStatus,
    onClick: () -> Unit = {},
    onViewResults: () -> Unit = {},
    onEdit: () -> Unit = {},
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
                    Text(
                        text = className,
                        style = MaterialTheme.typography.bodySmall,
                        color = PrimaryIndigo,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .background(PrimaryIndigo.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = com.example.voicetutor.utils.formatDueDate(dueDate),
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "제출 학생: $submittedCount/$totalCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600,
                    fontWeight = FontWeight.Medium,
                )

                Text(
                    text = "${if (totalCount > 0) (submittedCount.toFloat() / totalCount * 100).toInt() else 0}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryIndigo,
                    fontWeight = FontWeight.Bold,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            VTProgressBar(
                progress = if (totalCount > 0) submittedCount.toFloat() / totalCount else 0f,
                showPercentage = false,
                color = PrimaryIndigo,
                height = 6,
            )

//            Spacer(modifier = Modifier.height(12.dp))
//
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.spacedBy(8.dp)
//            ) {
//                VTButton(
//                    text = "과제 결과",
//                    onClick = onViewResults,
//                    variant = ButtonVariant.Primary,
//                    size = ButtonSize.Small,
//                    modifier = Modifier.weight(1f)
//                )
//
//                VTButton(
//                    text = "과제 편집",
//                    onClick = onEdit,
//                    variant = ButtonVariant.Outline,
//                    size = ButtonSize.Small,
//                    modifier = Modifier.weight(1f)
//                )
//            }
        }
    }
}
