package com.example.voicetutor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.*

@Composable
fun HelpScreen(
    onBackClick: () -> Unit = {}
) {
    var selectedCategory by remember { mutableStateOf("전체") }
    var searchQuery by remember { mutableStateOf("") }
    
    val categories = listOf("전체", "기본 사용법", "과제 관리", "음성 기능", "문제 해결", "계정 관리")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        VTHeader(
            title = "도움말",
            onBackClick = onBackClick
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Search bar
        VTCard(variant = CardVariant.Outlined) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("궁금한 내용을 검색하세요...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = Gray600
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Category filter
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                CategoryChip(
                    category = category,
                    isSelected = selectedCategory == category,
                    onClick = { selectedCategory = category }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Help content
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(getHelpItems(selectedCategory, searchQuery)) { item ->
                HelpItem(
                    title = item.title,
                    description = item.description,
                    category = item.category,
                    icon = item.icon
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Contact support
        VTCard(variant = CardVariant.Outlined) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Support,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "추가 도움이 필요하신가요?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray800
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "문제가 해결되지 않으면 고객지원팀에 문의하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VTButton(
                        text = "이메일 문의",
                        onClick = {
                            // TODO: Open email client
                        },
                        variant = ButtonVariant.Outlined,
                        modifier = Modifier.weight(1f)
                    )
                    
                    VTButton(
                        text = "전화 문의",
                        onClick = {
                            // TODO: Open phone dialer
                        },
                        variant = ButtonVariant.Outlined,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) PrimaryIndigo else Color.Transparent
    val textColor = if (isSelected) Color.White else Gray800
    val borderColor = if (isSelected) PrimaryIndigo else Gray300
    
    FilterChip(
        onClick = onClick,
        label = {
            Text(
                text = category,
                color = textColor,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
            )
        },
        selected = isSelected,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = backgroundColor,
            selectedLabelColor = textColor,
            containerColor = Color.Transparent,
            labelColor = textColor
        )
    )
}

@Composable
fun HelpItem(
    title: String,
    description: String,
    category: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    VTCard(variant = CardVariant.Outlined) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryIndigo,
                    modifier = Modifier.size(20.dp)
                )
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = Gray800
                    )
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodySmall,
                        color = Gray600
                    )
                }
                
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = Gray400,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Gray600
            )
        }
    }
}

data class HelpItemData(
    val title: String,
    val description: String,
    val category: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

fun getHelpItems(category: String, searchQuery: String): List<HelpItemData> {
    val allItems = listOf(
        HelpItemData(
            title = "VoiceTutor 시작하기",
            description = "VoiceTutor 앱의 기본 사용법과 첫 설정 방법을 알아보세요.",
            category = "기본 사용법",
            icon = Icons.Filled.PlayArrow
        ),
        HelpItemData(
            title = "계정 만들기 및 로그인",
            description = "새 계정을 만들고 로그인하는 방법을 설명합니다.",
            category = "계정 관리",
            icon = Icons.Filled.Person
        ),
        HelpItemData(
            title = "과제 생성 및 배정",
            description = "새로운 과제를 만들고 학생들에게 배정하는 방법입니다.",
            category = "과제 관리",
            icon = Icons.Filled.Assignment
        ),
        HelpItemData(
            title = "음성 녹음 기능 사용법",
            description = "음성으로 답변을 녹음하고 제출하는 방법을 알아보세요.",
            category = "음성 기능",
            icon = Icons.Filled.Mic
        ),
        HelpItemData(
            title = "학생 진도 확인하기",
            description = "학생들의 과제 완료 현황과 성과를 확인하는 방법입니다.",
            category = "과제 관리",
            icon = Icons.Filled.Analytics
        ),
        HelpItemData(
            title = "알림 설정 변경하기",
            description = "알림 유형과 시간을 설정하는 방법을 설명합니다.",
            category = "기본 사용법",
            icon = Icons.Filled.Notifications
        ),
        HelpItemData(
            title = "음성 인식이 안 될 때",
            description = "음성 인식 문제를 해결하는 방법과 팁을 제공합니다.",
            category = "문제 해결",
            icon = Icons.Filled.Error
        ),
        HelpItemData(
            title = "비밀번호 재설정",
            description = "비밀번호를 잊어버렸을 때 재설정하는 방법입니다.",
            category = "계정 관리",
            icon = Icons.Filled.Lock
        ),
        HelpItemData(
            title = "앱이 느리게 작동할 때",
            description = "앱 성능 문제를 해결하는 방법을 알아보세요.",
            category = "문제 해결",
            icon = Icons.Filled.Speed
        ),
        HelpItemData(
            title = "클래스 관리하기",
            description = "클래스를 생성하고 학생들을 관리하는 방법입니다.",
            category = "과제 관리",
            icon = Icons.Filled.Class
        )
    )
    
    return allItems.filter { item ->
        val matchesCategory = category == "전체" || item.category == category
        val matchesSearch = searchQuery.isEmpty() || 
            item.title.contains(searchQuery, ignoreCase = true) ||
            item.description.contains(searchQuery, ignoreCase = true)
        
        matchesCategory && matchesSearch
    }
}
