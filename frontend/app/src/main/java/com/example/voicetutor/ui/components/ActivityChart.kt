package com.example.voicetutor.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicetutor.ui.theme.*
import kotlinx.coroutines.delay

enum class ChartColor {
    Blue, Purple, Green, Orange
}

@Composable
fun VTActivityChart(
    data: List<Int>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    chartColor: ChartColor = ChartColor.Blue,
    animated: Boolean = true,
    showValues: Boolean = true
) {
    val maxValue = data.maxOrNull() ?: 1
    var animationPlayed by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (animated) {
            delay(100)
            animationPlayed = true
        } else {
            animationPlayed = true
        }
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEachIndexed { index, value ->
            ChartBar(
                value = value,
                maxValue = maxValue,
                label = labels.getOrNull(index) ?: "",
                chartColor = chartColor,
                animationDelay = if (animated) index * 100 else 0,
                animationPlayed = animationPlayed,
                showValue = showValues,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ChartBar(
    value: Int,
    maxValue: Int,
    label: String,
    chartColor: ChartColor,
    animationDelay: Int,
    animationPlayed: Boolean,
    showValue: Boolean,
    modifier: Modifier = Modifier
) {
    val heightFraction = value.toFloat() / maxValue
    
    val animatedHeight by animateFloatAsState(
        targetValue = if (animationPlayed) heightFraction else 0f,
        animationSpec = tween(
            durationMillis = 800,
            delayMillis = animationDelay,
            easing = FastOutSlowInEasing
        ),
        label = "bar_height"
    )
    
    val barColors = getBarColors(chartColor, heightFraction)
    
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Value display
        if (showValue) {
            Box(
                modifier = Modifier
                    .height(24.dp),
                contentAlignment = Alignment.Center
            ) {
                if (animatedHeight > 0.1f) {
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Gray700,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // Bar container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Glow effect
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .fillMaxHeight(animatedHeight.coerceAtLeast(0.05f))
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = barColors.map { it.copy(alpha = 0.3f) }
                        )
                    )
                    .blur(4.dp)
            )
            
            // Main bar
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .fillMaxHeight(animatedHeight.coerceAtLeast(0.05f))
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(
                        brush = Brush.verticalGradient(colors = barColors)
                    )
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Label
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Gray600,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 2.dp)
        )
    }
}

private fun getBarColors(chartColor: ChartColor, intensity: Float): List<Color> {
    return when (chartColor) {
        ChartColor.Blue -> when {
            intensity > 0.8f -> listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6)) // blue-700 to blue-500
            intensity > 0.6f -> listOf(Color(0xFF3B82F6), Color(0xFF60A5FA)) // blue-500 to blue-400
            intensity > 0.4f -> listOf(Color(0xFF60A5FA), Color(0xFF93C5FD)) // blue-400 to blue-300
            else -> listOf(Color(0xFF93C5FD), Color(0xFFDBEAFE)) // blue-300 to blue-100
        }
        ChartColor.Purple -> when {
            intensity > 0.8f -> listOf(Color(0xFF7C3AED), Color(0xFF8B5CF6)) // purple-600 to purple-500
            intensity > 0.6f -> listOf(Color(0xFF8B5CF6), Color(0xFFA78BFA)) // purple-500 to purple-400
            intensity > 0.4f -> listOf(Color(0xFFA78BFA), Color(0xFFC4B5FD)) // purple-400 to purple-300
            else -> listOf(Color(0xFFC4B5FD), Color(0xFFE9D5FF)) // purple-300 to purple-100
        }
        ChartColor.Green -> when {
            intensity > 0.8f -> listOf(Color(0xFF059669), Color(0xFF10B981)) // emerald-600 to emerald-500
            intensity > 0.6f -> listOf(Color(0xFF10B981), Color(0xFF34D399)) // emerald-500 to emerald-400
            intensity > 0.4f -> listOf(Color(0xFF34D399), Color(0xFF6EE7B7)) // emerald-400 to emerald-300
            else -> listOf(Color(0xFF6EE7B7), Color(0xFFA7F3D0)) // emerald-300 to emerald-200
        }
        ChartColor.Orange -> when {
            intensity > 0.8f -> listOf(Color(0xFFEA580C), Color(0xFFF97316)) // orange-600 to orange-500
            intensity > 0.6f -> listOf(Color(0xFFF97316), Color(0xFFFB923C)) // orange-500 to orange-400
            intensity > 0.4f -> listOf(Color(0xFFFB923C), Color(0xFFFBBF24)) // orange-400 to orange-300
            else -> listOf(Color(0xFFFBBF24), Color(0xFFFEF3C7)) // orange-300 to orange-100
        }
    }
}

@Composable
fun VTLineChart(
    data: List<Float>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    lineColor: Color = PrimaryIndigo,
    animated: Boolean = true
) {
    val maxValue = data.maxOrNull() ?: 1f
    val minValue = data.minOrNull() ?: 0f
    val range = maxValue - minValue
    
    var animationPlayed by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        if (animated) {
            delay(200)
            animationPlayed = true
        } else {
            animationPlayed = true
        }
    }
    
    Column(modifier = modifier) {
        // Chart area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .padding(8.dp)
        ) {
            // Grid lines
            repeat(5) { i ->
                val yPosition = (i / 4f)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .align(Alignment.TopStart)
                        .offset(y = (yPosition * 104).dp)
                        .background(Gray200)
                )
            }
            
            // Data points
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                data.forEachIndexed { index, value ->
                    val normalizedValue = if (range > 0) (value - minValue) / range else 0f
                    val animatedValue by animateFloatAsState(
                        targetValue = if (animationPlayed) normalizedValue else 0f,
                        animationSpec = tween(
                            durationMillis = 800,
                            delayMillis = index * 100
                        ),
                        label = "point_value"
                    )
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Data point
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .offset(y = (-(animatedValue * 104)).dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(lineColor)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
        
        // Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = Gray600,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ActivityChartPreview() {
    VoiceTutorTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            VTCard {
                Column {
                    Text(
                        text = "주간 활동 (막대 차트)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    VTActivityChart(
                        data = listOf(65, 45, 70, 60, 80, 75, 90),
                        labels = listOf("월", "화", "수", "목", "금", "토", "일"),
                        chartColor = ChartColor.Blue
                    )
                }
            }
            
            VTCard {
                Column {
                    Text(
                        text = "점수 추이 (선 차트)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    VTLineChart(
                        data = listOf(78f, 82f, 75f, 88f, 92f, 85f, 90f),
                        labels = listOf("1주", "2주", "3주", "4주", "5주", "6주", "7주"),
                        lineColor = Success
                    )
                }
            }
        }
    }
}
