package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.viewmodel.AttendanceViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AttendanceManagementScreen(
    classId: Int = 1,
    onNavigateBack: () -> Unit = {}
) {
    val attendanceViewModel: AttendanceViewModel = hiltViewModel()
    val classAttendanceSummary by attendanceViewModel.classAttendanceSummary.collectAsStateWithLifecycle()
    val attendanceRecords by attendanceViewModel.attendanceRecords.collectAsStateWithLifecycle()
    val isLoading by attendanceViewModel.isLoading.collectAsStateWithLifecycle()
    val error by attendanceViewModel.error.collectAsStateWithLifecycle()
    
    var selectedDate by remember { mutableStateOf(getCurrentDate()) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    // Load class attendance on first composition
    LaunchedEffect(classId, selectedDate) {
        attendanceViewModel.loadClassAttendance(classId, selectedDate)
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            attendanceViewModel.clearError()
        }
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryIndigo)
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            VTHeader(
                title = "출석 관리",
                onBackClick = onNavigateBack
            )
            
            // Date Selection
            VTCard(variant = CardVariant.Elevated) {
                Column {
                    Text(
                        text = "출석 날짜",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedDate,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray700
                        )
                        
                        IconButton(
                            onClick = { showDatePicker = true }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = "날짜 선택",
                                tint = PrimaryIndigo
                            )
                        }
                    }
                }
            }
            
            // Attendance Summary
            classAttendanceSummary?.let { summary ->
                VTCard(variant = CardVariant.Elevated) {
                    Column {
                        Text(
                            text = "출석 현황",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            VTStatsCard(
                                title = "출석",
                                value = summary.presentCount.toString(),
                                icon = Icons.Filled.CheckCircle,
                                iconColor = Success,
                                variant = CardVariant.Gradient,
                                modifier = Modifier.weight(1f)
                            )
                            
                            VTStatsCard(
                                title = "지각",
                                value = summary.lateCount.toString(),
                                icon = Icons.Filled.Schedule,
                                iconColor = Warning,
                                variant = CardVariant.Gradient,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            VTStatsCard(
                                title = "결석",
                                value = summary.absentCount.toString(),
                                icon = Icons.Filled.Cancel,
                                iconColor = Error,
                                variant = CardVariant.Gradient,
                                modifier = Modifier.weight(1f)
                            )
                            
                            VTStatsCard(
                                title = "출석률",
                                value = "${summary.attendanceRate.toInt()}%",
                                icon = Icons.Filled.Person,
                                iconColor = PrimaryIndigo,
                                variant = CardVariant.Gradient,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                
                // Student List
                VTCard(variant = CardVariant.Elevated) {
                    Column {
                        Text(
                            text = "학생별 출석",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Gray800
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(summary.attendanceRecords) { record ->
                                AttendanceRecordItem(
                                    record = record,
                                    onStatusChange = { newStatus ->
                                        attendanceViewModel.updateAttendanceStatus(
                                            studentId = record.studentId,
                                            status = newStatus
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            } ?: run {
                // No attendance data
                VTCard(variant = CardVariant.Elevated) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CalendarToday,
                            contentDescription = null,
                            tint = Gray400,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "출석 데이터가 없습니다",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600
                        )
                    }
                }
            }
        }
    }
    
    // Date Picker Dialog
    if (showDatePicker) {
        // TODO: Implement date picker dialog
        // For now, just close the dialog
        LaunchedEffect(showDatePicker) {
            showDatePicker = false
        }
    }
}

@Composable
fun AttendanceRecordItem(
    record: AttendanceRecord,
    onStatusChange: (AttendanceStatus) -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = when (record.status) {
                    AttendanceStatus.PRESENT -> Success.copy(alpha = 0.1f)
                    AttendanceStatus.LATE -> Warning.copy(alpha = 0.1f)
                    AttendanceStatus.ABSENT -> Error.copy(alpha = 0.1f)
                    AttendanceStatus.EXCUSED -> PrimaryIndigo.copy(alpha = 0.1f)
                },
                shape = MaterialTheme.shapes.small
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.studentName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Gray800
            )
            
            if (record.checkInTime != null) {
                Text(
                    text = "출석 시간: ${record.checkInTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
            }
            
            if (record.notes != null) {
                Text(
                    text = "비고: ${record.notes}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600
                )
            }
        }
        
        Box {
            Button(
                onClick = { showStatusMenu = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (record.status) {
                        AttendanceStatus.PRESENT -> Success
                        AttendanceStatus.LATE -> Warning
                        AttendanceStatus.ABSENT -> Error
                        AttendanceStatus.EXCUSED -> PrimaryIndigo
                    }
                ),
                modifier = Modifier.height(32.dp)
            ) {
                Text(
                    text = when (record.status) {
                        AttendanceStatus.PRESENT -> "출석"
                        AttendanceStatus.LATE -> "지각"
                        AttendanceStatus.ABSENT -> "결석"
                        AttendanceStatus.EXCUSED -> "공결"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            }
            
            DropdownMenu(
                expanded = showStatusMenu,
                onDismissRequest = { showStatusMenu = false }
            ) {
                AttendanceStatus.values().forEach { status ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = when (status) {
                                    AttendanceStatus.PRESENT -> "출석"
                                    AttendanceStatus.LATE -> "지각"
                                    AttendanceStatus.ABSENT -> "결석"
                                    AttendanceStatus.EXCUSED -> "공결"
                                }
                            )
                        },
                        onClick = {
                            onStatusChange(status)
                            showStatusMenu = false
                        }
                    )
                }
            }
        }
    }
}

private fun getCurrentDate(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return dateFormat.format(Date())
}

@Preview(showBackground = true)
@Composable
fun AttendanceManagementScreenPreview() {
    VoiceTutorTheme {
        AttendanceManagementScreen()
    }
}
