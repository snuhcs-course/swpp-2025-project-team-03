package com.example.voicetutor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.voicetutor.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DatePickerDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
    initialDate: String = ""
) {
    if (isVisible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "날짜 선택",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Gray800
                )
            },
            text = {
                DatePickerContent(
                    initialDate = initialDate,
                    onDateSelected = onDateSelected
                )
            },
            confirmButton = {
                VTButton(
                    text = "확인",
                    onClick = onDismiss
                )
            },
            dismissButton = {
                VTButton(
                    text = "취소",
                    onClick = onDismiss,
                    variant = ButtonVariant.Outlined
                )
            }
        )
    }
}

@Composable
fun DatePickerContent(
    initialDate: String = "",
    onDateSelected: (String) -> Unit
) {
    var selectedDate by remember { mutableStateOf(initialDate) }
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val today = dateFormat.format(Date())
    
    Column {
        // Quick date selection buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickDateButton(
                text = "오늘",
                onClick = {
                    selectedDate = today
                    onDateSelected(selectedDate)
                },
                isSelected = selectedDate == today,
                modifier = Modifier.weight(1f)
            )
            
            QuickDateButton(
                text = "어제",
                onClick = {
                    val yesterday = Calendar.getInstance().apply {
                        add(Calendar.DAY_OF_MONTH, -1)
                    }
                    selectedDate = dateFormat.format(yesterday.time)
                    onDateSelected(selectedDate)
                },
                isSelected = false,
                modifier = Modifier.weight(1f)
            )
            
            QuickDateButton(
                text = "이번 주",
                onClick = {
                    val thisWeek = Calendar.getInstance().apply {
                        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                    }
                    selectedDate = dateFormat.format(thisWeek.time)
                    onDateSelected(selectedDate)
                },
                isSelected = false,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Manual date input
        OutlinedTextField(
            value = selectedDate,
            onValueChange = { selectedDate = it },
            label = { Text("날짜") },
            placeholder = { Text("YYYY-MM-DD 형식으로 입력") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.DateRange,
                    contentDescription = null,
                    tint = PrimaryIndigo
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "예: 2024-01-15",
            style = MaterialTheme.typography.bodySmall,
            color = Gray600
        )
    }
}

@Composable
fun QuickDateButton(
    text: String,
    onClick: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) PrimaryIndigo else Color.White
    val textColor = if (isSelected) Color.White else Gray800
    val borderColor = if (isSelected) PrimaryIndigo else Gray300
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
fun DateRangePicker(
    startDate: String,
    endDate: String,
    onStartDateClick: () -> Unit,
    onEndDateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Start date
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "시작 날짜",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )
            Spacer(modifier = Modifier.height(4.dp))
            DatePickerField(
                date = startDate,
                onClick = onStartDateClick,
                placeholder = "시작 날짜 선택"
            )
        }
        
        // End date
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "종료 날짜",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )
            Spacer(modifier = Modifier.height(4.dp))
            DatePickerField(
                date = endDate,
                onClick = onEndDateClick,
                placeholder = "종료 날짜 선택"
            )
        }
    }
}

@Composable
fun DatePickerField(
    date: String,
    onClick: () -> Unit,
    placeholder: String = "날짜 선택",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = date,
        onValueChange = { },
        readOnly = true,
        placeholder = { Text(placeholder) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = null,
                tint = PrimaryIndigo
            )
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    )
}

@Composable
fun MonthYearPicker(
    selectedMonth: Int,
    selectedYear: Int,
    onMonthSelected: (Int) -> Unit,
    onYearSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Month selection
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "월",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = "${selectedMonth}월",
                onValueChange = { },
                readOnly = true,
                trailingIcon = { 
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Year selection
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "년",
                style = MaterialTheme.typography.bodyMedium,
                color = Gray600
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = "${selectedYear}년",
                onValueChange = { },
                readOnly = true,
                trailingIcon = { 
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}