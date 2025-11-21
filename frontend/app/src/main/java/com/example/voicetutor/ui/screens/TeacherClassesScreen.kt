package com.example.voicetutor.ui.screens

import android.widget.Toast
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel
import com.example.voicetutor.ui.viewmodel.ClassViewModel

data class ClassRoom(
    val id: Int,
    val name: String,
    val subject: String,
    val description: String,
    val studentCount: Int,
    val assignmentCount: Int,
    val completionRate: Float,
    val color: Color,
)

@Composable
fun TeacherClassesScreen(
    authViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel? = null,
    assignmentViewModel: AssignmentViewModel? = null,
    teacherId: String? = null,
    showCreatedToast: Boolean = false,
    onNavigateToClassDetail: (String, Int) -> Unit = { _, _ -> },
    onNavigateToCreateClass: () -> Unit = {},
    onNavigateToCreateAssignment: (Int?) -> Unit = { _ -> },
    onNavigateToStudents: (Int) -> Unit = {},
) {
    val classViewModel: ClassViewModel = hiltViewModel()
    val actualAssignmentViewModel: AssignmentViewModel = assignmentViewModel ?: hiltViewModel()
    val actualAuthViewModel: com.example.voicetutor.ui.viewmodel.AuthViewModel = authViewModel ?: hiltViewModel()

    val classes by classViewModel.classes.collectAsStateWithLifecycle()
    val assignments by actualAssignmentViewModel.assignments.collectAsStateWithLifecycle()
    val isLoading by classViewModel.isLoading.collectAsStateWithLifecycle()
    val error by classViewModel.error.collectAsStateWithLifecycle()
    val currentUser by actualAuthViewModel.currentUser.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(currentUser?.id) {
        val actualTeacherId = teacherId ?: currentUser?.id?.toString()

        if (actualTeacherId == null) {
            println("TeacherClassesScreen - Waiting for user to be loaded...")
            return@LaunchedEffect
        }

        if (assignments.isEmpty()) {
            println("TeacherClassesScreen - Loading assignments for teacher ID: $actualTeacherId")
            actualAssignmentViewModel.loadAllAssignments(teacherId = actualTeacherId)
        } else {
            println("TeacherClassesScreen - Already have ${assignments.size} assignments from login")
        }

        if (classes.isEmpty()) {
            println("TeacherClassesScreen - Loading classes for teacher ID: $actualTeacherId")
            classViewModel.loadClasses(actualTeacherId)
        } else {
            println("TeacherClassesScreen - Already have ${classes.size} classes")
        }
    }

    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            classViewModel.clearError()
        }
    }

    LaunchedEffect(showCreatedToast, currentUser?.id) {
        if (showCreatedToast && currentUser?.id != null) {
            val actualTeacherId = teacherId ?: currentUser?.id?.toString()
            if (actualTeacherId != null) {
                Toast.makeText(context, "수업이 생성되었습니다.", Toast.LENGTH_SHORT).show()
                classViewModel.loadClasses(actualTeacherId)
            }
        }
    }

    val classRooms = classes.map { classData ->
        val classAssignments = assignments.filter { it.courseClass.id == classData.id }
        val assignmentCount = classAssignments.size

        ClassRoom(
            id = classData.id,
            name = classData.name,
            subject = classData.subject.name,
            description = classData.description,
            studentCount = classData.actualStudentCount,
            assignmentCount = assignmentCount,
            completionRate = 0f,
            color = when (classData.id % 4) {
                0 -> PrimaryIndigo
                1 -> Success
                2 -> Warning
                else -> Error
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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
                    text = "수업 관리",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray800,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "내 수업을 관리하고 과제를 생성하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "수업 목록",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Gray800,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                VTButton(
                    text = "수업 생성",
                    onClick = onNavigateToCreateClass,
                    variant = ButtonVariant.Outline,
                    size = ButtonSize.Small,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = PrimaryIndigo,
                    )
                }
            } else if (classRooms.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.School,
                            contentDescription = null,
                            tint = Gray400,
                            modifier = Modifier.size(48.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "반이 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600,
                        )
                    }
                }
            } else {
                classRooms.forEach { classRoom ->
                    ClassCard(
                        classRoom = classRoom,
                        onClassClick = { onNavigateToClassDetail(classRoom.name, classRoom.id) },
                        onCreateAssignment = { classId -> onNavigateToCreateAssignment(classId) },
                        onViewStudents = { onNavigateToStudents(classRoom.id) },
                        onDeleteClass = { room, resultCallback ->
                            classViewModel.deleteClass(room.id) { success ->
                                resultCallback(success)
                                val message = if (success) {
                                    "${room.name} 수업이 삭제되었어요"
                                } else {
                                    "수업 삭제에 실패했어요"
                                }
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun ClassCard(
    classRoom: ClassRoom,
    onClassClick: (Int) -> Unit,
    onCreateAssignment: (Int) -> Unit,
    onViewStudents: (Int) -> Unit,
    onDeleteClass: (ClassRoom, (Boolean) -> Unit) -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    VTCard(
        variant = CardVariant.Elevated,
        onClick = { onClassClick(classRoom.id) },
    ) {
        Column(
            modifier = Modifier.padding(3.5.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .background(classRoom.color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.MenuBook,
                        contentDescription = null,
                        tint = classRoom.color,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = classRoom.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800,
                    )
                    Text(
                        text = classRoom.subject,
                        style = MaterialTheme.typography.bodyMedium,
                        color = classRoom.color,
                        fontWeight = FontWeight.Medium,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.offset(y = (-12).dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "수업 삭제",
                        tint = Error,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                ClassStatItem(
                    icon = Icons.Filled.People,
                    value = classRoom.studentCount.toString(),
                    label = "학생",
                    color = Gray600,
                )

                ClassStatItem(
                    icon = Icons.Filled.Assignment,
                    value = classRoom.assignmentCount.toString(),
                    label = "과제",
                    color = Gray600,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                VTButton(
                    text = "과제 생성",
                    onClick = { onCreateAssignment(classRoom.id) },
                    variant = ButtonVariant.Primary,
                    size = ButtonSize.Small,
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    },
                )

                VTButton(
                    text = "학생 상세",
                    onClick = { onViewStudents(classRoom.id) },
                    variant = ButtonVariant.Outline,
                    size = ButtonSize.Small,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isDeleting) {
                    showDeleteDialog = false
                }
            },
            title = {
                Text(
                    text = "수업 삭제",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            },
            text = {
                Text(
                    text = "\"${classRoom.name}\" 수업을 삭제하시겠습니까?\n삭제하면 되돌릴 수 없어요!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray700,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isDeleting = true
                        onDeleteClass(classRoom) { success ->
                            isDeleting = false
                            if (success) {
                                showDeleteDialog = false
                            }
                        }
                    },
                    enabled = !isDeleting,
                ) {
                    Text(if (isDeleting) "삭제 중..." else "삭제하기", color = Error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!isDeleting) {
                            showDeleteDialog = false
                        }
                    },
                ) {
                    Text("취소")
                }
            },
        )
    }
}

@Composable
fun ClassStatItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp),
        )
        Text(
            text = "$label: $value",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = Gray800,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TeacherClassesScreenPreview() {
    VoiceTutorTheme {
        TeacherClassesScreen()
    }
}
