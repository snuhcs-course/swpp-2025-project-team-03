package com.example.voicetutor.ui.components

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.voicetutor.ui.theme.*

@Composable
fun VTSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    placeholder: String = "검색어를 입력하세요",
    enabled: Boolean = true,
    showClearButton: Boolean = true,
    showSearchButton: Boolean = true
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var isFocused by remember { mutableStateOf(false) }
    
    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> PrimaryIndigo
            query.isNotEmpty() -> Success
            else -> Gray300
        },
        animationSpec = tween(200),
        label = "borderColor"
    )
    
    val backgroundColor by animateColorAsState(
        targetValue = if (isFocused) PrimaryIndigo.copy(alpha = 0.05f) else Color.White,
        animationSpec = tween(200),
        label = "backgroundColor"
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 검색 아이콘
        Icon(
            imageVector = Icons.Filled.Search,
            contentDescription = "검색",
            tint = if (isFocused) PrimaryIndigo else Gray500,
            modifier = Modifier.size(20.dp)
        )
        
        // 검색 입력 필드
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { 
                Text(
                    text = placeholder,
                    color = Gray500
                ) 
            },
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                },
            enabled = enabled,
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = Gray800,
                unfocusedTextColor = Gray800,
                cursorColor = PrimaryIndigo
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearch(query)
                    keyboardController?.hide()
                }
            )
        )
        
        // 지우기 버튼
        if (showClearButton && query.isNotEmpty()) {
            IconButton(
                onClick = { 
                    onQueryChange("")
                    keyboardController?.hide()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = "지우기",
                    tint = Gray500,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        
        // 검색 버튼
        if (showSearchButton) {
            IconButton(
                onClick = { 
                    onSearch(query)
                    keyboardController?.hide()
                },
                enabled = query.isNotEmpty()
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "검색 실행",
                    tint = if (query.isNotEmpty()) PrimaryIndigo else Gray400,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun VTSearchBarWithFilters(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit = {},
    onFilterClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    placeholder: String = "검색어를 입력하세요",
    filterCount: Int = 0
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        VTSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            placeholder = placeholder,
            modifier = Modifier.weight(1f)
        )
        
        // 필터 버튼
        IconButton(
            onClick = onFilterClick,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (filterCount > 0) PrimaryIndigo else Gray100
                )
        ) {
            Box {
                Icon(
                    imageVector = Icons.Filled.FilterList,
                    contentDescription = "필터",
                    tint = if (filterCount > 0) Color.White else Gray600,
                    modifier = Modifier.size(20.dp)
                )
                
                if (filterCount > 0) {
                    Badge(
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Text(
                            text = filterCount.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VTSearchSuggestions(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (suggestions.isNotEmpty()) {
        VTCard(
            variant = CardVariant.Elevated,
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "추천 검색어",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Gray700
                )
                
                suggestions.forEach { suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSuggestionClick(suggestion) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.History,
                            contentDescription = null,
                            tint = Gray500,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray700
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewVTSearchBar() {
    VoiceTutorTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            VTSearchBar(
                query = "",
                onQueryChange = {},
                placeholder = "과제를 검색하세요"
            )
            
            VTSearchBar(
                query = "생물학",
                onQueryChange = {},
                placeholder = "학생을 검색하세요"
            )
            
            VTSearchBarWithFilters(
                query = "세포분열",
                onQueryChange = {},
                filterCount = 2
            )
            
            VTSearchSuggestions(
                suggestions = listOf("세포분열", "DNA 복제", "유전자 발현"),
                onSuggestionClick = {}
            )
        }
    }
}