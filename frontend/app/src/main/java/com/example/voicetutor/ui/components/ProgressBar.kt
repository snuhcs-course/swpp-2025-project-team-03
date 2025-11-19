package com.example.voicetutor.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.voicetutor.ui.theme.*

@Composable
fun VTProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    animated: Boolean = true,
    showPercentage: Boolean = true,
    color: Color = PrimaryIndigo,
    backgroundColor: Color = Gray200,
    height: Int = 8,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (animated) progress else progress,
        animationSpec = tween(durationMillis = if (animated) 1000 else 0),
        label = "progress",
    )

    val shape = RoundedCornerShape(height.dp / 2)

    Column(modifier = modifier) {
        if (showPercentage) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "진행률",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray600,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "${(animatedProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall,
                    color = color,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height.dp)
                .clip(shape)
                .background(backgroundColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                    .clip(shape)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                color,
                                color.copy(alpha = 0.8f),
                            ),
                        ),
                    ),
            )
        }
    }
}

@Composable
fun VTCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Int = 120,
    strokeWidth: Int = 8,
    animated: Boolean = true,
    showPercentage: Boolean = true,
    color: Color = PrimaryIndigo,
    backgroundColor: Color = Gray200,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = if (animated) progress else progress,
        animationSpec = tween(durationMillis = if (animated) 1000 else 0),
        label = "circular_progress",
    )

    Box(
        modifier = modifier.size(size.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = backgroundColor,
            strokeWidth = strokeWidth.dp,
        )

        CircularProgressIndicator(
            progress = { animatedProgress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxSize(),
            color = color,
            strokeWidth = strokeWidth.dp,
        )

        if (showPercentage) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium,
                color = color,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun VTStepProgress(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier,
    stepLabels: List<String> = emptyList(),
    color: Color = PrimaryIndigo,
    backgroundColor: Color = Gray200,
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (step in 1..totalSteps) {
                val isCompleted = step <= currentStep
                val isActive = step == currentStep

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isCompleted) color else backgroundColor,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = step.toString(),
                        color = if (isCompleted) Color.White else Gray500,
                        fontSize = 14.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    )
                }

                if (step < totalSteps) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                if (step < currentStep) color else backgroundColor,
                            ),
                    )
                }
            }
        }

        if (stepLabels.isNotEmpty() && stepLabels.size >= totalSteps) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                stepLabels.take(totalSteps).forEachIndexed { index, label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (index + 1 <= currentStep) color else Gray500,
                        fontWeight = if (index + 1 == currentStep) FontWeight.Bold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressBarPreview() {
    VoiceTutorTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            VTProgressBar(
                progress = 0.7f,
                showPercentage = true,
            )

            VTProgressBar(
                progress = 0.3f,
                height = 12,
                color = Success,
                showPercentage = true,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                VTCircularProgress(
                    progress = 0.8f,
                    size = 80,
                )

                VTCircularProgress(
                    progress = 0.45f,
                    size = 80,
                    color = PrimaryEmerald,
                )
            }

            VTStepProgress(
                currentStep = 2,
                totalSteps = 4,
                stepLabels = listOf("시작", "진행", "검토", "완료"),
            )
        }
    }
}
