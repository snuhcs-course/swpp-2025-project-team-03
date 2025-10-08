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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.viewmodel.AssignmentViewModel

@Composable
fun SubjectDetailScreen(
    subject: String = "과목",
    onBackClick: () -> Unit = {}
) {
    val viewModel: AssignmentViewModel = hiltViewModel()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    
    // Load assignments for this subject on first composition
    LaunchedEffect(subject) {
        viewModel.loadAllAssignments()
    }
    
    // Handle error
    error?.let { errorMessage ->
        LaunchedEffect(errorMessage) {
            // Show error message
            viewModel.clearError()
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray50)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(PrimaryIndigo)
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = Color.White
                    )
                }
                Text(
                    text = subject,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            IconButton(onClick = { /* TODO: Search assignments */ }) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "검색",
                    tint = Color.White
                )
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
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Subject info card
                VTCard(variant = CardVariant.Elevated) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.School,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(32.dp)
                            )
                            Column {
                                Text(
                                    text = subject,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Gray900
                                )
                                Text(
                                    text = "과목 상세 정보",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Gray600
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Stats row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            VTStatsCard(
                                title = "총 과제",
                                value = assignments.filter { it.subject == subject }.size.toString(),
                                icon = Icons.Filled.Assignment,
                                iconColor = PrimaryIndigo,
                                modifier = Modifier.weight(1f),
                                variant = CardVariant.Outlined
                            )
                            
                            VTStatsCard(
                                title = "완료율",
                                value = "0%", // TODO: Calculate completion rate
                                icon = Icons.Filled.Done,
                                iconColor = Success,
                                modifier = Modifier.weight(1f),
                                variant = CardVariant.Outlined
                            )
                        }
                    }
                }
                
                // Assignments list
                Text(
                    text = "과제 목록",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                
                val subjectAssignments = assignments.filter { it.subject == subject }
                
                if (subjectAssignments.isEmpty()) {
                    VTCard(variant = CardVariant.Outlined) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Assignment,
                                contentDescription = null,
                                tint = Gray400,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "아직 과제가 없습니다",
                                style = MaterialTheme.typography.titleMedium,
                                color = Gray600
                            )
                            Text(
                                text = "새로운 과제를 생성해보세요",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Gray500
                            )
                        }
                    }
                } else {
                    subjectAssignments.forEach { assignment ->
                        VTCard(
                            variant = CardVariant.Elevated,
                            onClick = { /* TODO: Navigate to assignment detail */ }
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = assignment.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Gray900
                                        )
                                        Text(
                                            text = assignment.description ?: "설명 없음",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Gray600
                                        )
                                    }
                                    
                                    Icon(
                                        imageVector = Icons.Filled.ChevronRight,
                                        contentDescription = null,
                                        tint = Gray400
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "마감일: ${assignment.dueDate}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Gray500
                                    )
                                    
                                    Text(
                                        text = when (assignment.status) {
                                            AssignmentStatus.DRAFT -> "임시저장"
                                            AssignmentStatus.IN_PROGRESS -> "진행중"
                                            AssignmentStatus.COMPLETED -> "완료"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when (assignment.status) {
                                            AssignmentStatus.DRAFT -> Warning
                                            AssignmentStatus.IN_PROGRESS -> PrimaryIndigo
                                            AssignmentStatus.COMPLETED -> Success
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SubjectDetailScreenPreview() {
    VoiceTutorTheme {
        SubjectDetailScreen(
            subject = "생물학",
            onBackClick = {}
        )
    }
}
