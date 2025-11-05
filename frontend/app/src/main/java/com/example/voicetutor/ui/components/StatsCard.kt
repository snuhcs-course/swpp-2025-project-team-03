package com.example.voicetutor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicetutor.ui.theme.*

enum class TrendDirection {
    Up, Down, None
}

enum class StatsCardLayout {
    Horizontal, // Text left, icon right (default)
    Vertical    // Icon top, text bottom
}

@Composable
fun VTStatsCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconColor: Color = PrimaryIndigo,
    trend: TrendDirection = TrendDirection.None,
    trendValue: String = "",
    onClick: (() -> Unit)? = null,
    variant: CardVariant = CardVariant.Default,
    layout: StatsCardLayout = StatsCardLayout.Horizontal
) {
    val shape = RoundedCornerShape(20.dp)
    
    val cardModifier = modifier
        .clip(shape)
        .then(
            if (variant == CardVariant.Gradient) {
                Modifier.background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            iconColor.copy(alpha = 0.06f),
                            iconColor.copy(alpha = 0.03f)
                        )
                    )
                )
            } else {
                Modifier.background(
                    when (variant) {
                        CardVariant.Default -> Color.White
                        CardVariant.Elevated -> Color.White
                        CardVariant.Outlined -> Color.White
                        CardVariant.Selected -> Color.White
                        CardVariant.Gradient -> Color.White // This case won't be reached
                    }
                )
            }
        )
        .shadow(
            elevation = when (variant) {
                CardVariant.Default -> 4.dp
                CardVariant.Elevated -> 8.dp
                CardVariant.Selected -> 6.dp
                CardVariant.Outlined -> 2.dp
                CardVariant.Gradient -> 6.dp
            },
            shape = shape,
            ambientColor = Color.Black.copy(alpha = 0.03f),
            spotColor = Color.Black.copy(alpha = 0.06f)
        )
        .then(
            if (onClick != null) {
                Modifier.clickable { onClick() }
            } else {
                Modifier
            }
        )

    when (layout) {
        StatsCardLayout.Horizontal -> {
            Column(
                modifier = cardModifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray600,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = value,
                            style = MaterialTheme.typography.titleLarge,
                            color = Gray900,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                        
                        if (trend != TrendDirection.None && trendValue.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = when (trend) {
                                        TrendDirection.Up -> Icons.Filled.KeyboardArrowUp
                                        TrendDirection.Down -> Icons.Filled.KeyboardArrowDown
                                        TrendDirection.None -> Icons.Filled.Remove
                                    },
                                    contentDescription = null,
                                    tint = when (trend) {
                                        TrendDirection.Up -> Success
                                        TrendDirection.Down -> Error
                                        TrendDirection.None -> Gray400
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                
                                Text(
                                    text = trendValue,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when (trend) {
                                        TrendDirection.Up -> Success
                                        TrendDirection.Down -> Error
                                        TrendDirection.None -> Gray400
                                    },
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(iconColor.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
        
        StatsCardLayout.Vertical -> {
            Column(
                modifier = cardModifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon at the top
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconColor.copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // Title
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600,
                    fontWeight = FontWeight.Medium,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 2,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Value
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    color = Gray900,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                // Trend indicator
                if (trend != TrendDirection.None && trendValue.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = when (trend) {
                                TrendDirection.Up -> Icons.Filled.KeyboardArrowUp
                                TrendDirection.Down -> Icons.Filled.KeyboardArrowDown
                                TrendDirection.None -> Icons.Filled.Remove
                            },
                            contentDescription = null,
                            tint = when (trend) {
                                TrendDirection.Up -> Success
                                TrendDirection.Down -> Error
                                TrendDirection.None -> Gray400
                            },
                            modifier = Modifier.size(16.dp)
                        )
                        
                        Text(
                            text = trendValue,
                            style = MaterialTheme.typography.bodySmall,
                            color = when (trend) {
                                TrendDirection.Up -> Success
                                TrendDirection.Down -> Error
                                TrendDirection.None -> Gray400
                            },
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatsCardPreview() {
    VoiceTutorTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VTStatsCard(
                    title = "총 과제",
                    value = "0", // TODO: 실제 총 과제 수로 동적 설정
                    icon = Icons.Filled.List,
                    iconColor = PrimaryIndigo,
                    trend = TrendDirection.Up,
                    trendValue = "+0", // TODO: 실제 증가 수로 동적 설정
                    modifier = Modifier.weight(1f),
                    variant = CardVariant.Gradient
                )
                
                VTStatsCard(
                    title = "완료율",
                    value = "0%", // TODO: 실제 완료율로 동적 설정
                    icon = Icons.Filled.Done,
                    iconColor = Success,
                    trend = TrendDirection.Up,
                    trendValue = "+0%", // TODO: 실제 증가율로 동적 설정
                    modifier = Modifier.weight(1f),
                    variant = CardVariant.Gradient
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                VTStatsCard(
                    title = "학생 수",
                    value = "0", // TODO: 실제 학생 수로 동적 설정
                    icon = Icons.Filled.Person,
                    iconColor = PrimaryEmerald,
                    trend = TrendDirection.Up,
                    trendValue = "+0", // TODO: 실제 증가 수로 동적 설정
                    modifier = Modifier.weight(1f),
                    onClick = { }
                )
                
                VTStatsCard(
                    title = "평균 점수",
                    value = "0점", // TODO: 실제 평균 점수로 동적 설정
                    icon = Icons.Filled.Star,
                    iconColor = Warning,
                    trend = TrendDirection.Down,
                    trendValue = "0점", // TODO: 실제 변화량으로 동적 설정
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
