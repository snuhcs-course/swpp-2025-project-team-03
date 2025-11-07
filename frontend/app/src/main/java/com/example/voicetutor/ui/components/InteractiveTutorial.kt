package com.example.voicetutor.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.voicetutor.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * 튜토리얼 스팟 - 강조할 화면 영역과 설명
 */
data class TutorialSpot(
    val title: String,
    val description: String,
    val imageRes: Int, // drawable 리소스 ID
    val highlightArea: HighlightArea,
    val tapInstruction: String = "여기를 확인하세요!",
    val arrowDirection: ArrowDirection = ArrowDirection.TOP
)

/**
 * 강조 영역 정의
 */
data class HighlightArea(
    val x: Float, // 화면 비율 (0.0 ~ 1.0)
    val y: Float, // 화면 비율 (0.0 ~ 1.0)
    val width: Float, // 화면 비율
    val height: Float, // 화면 비율
    val shape: HighlightShape = HighlightShape.RECTANGLE
)

enum class HighlightShape {
    RECTANGLE,
    CIRCLE
}

enum class ArrowDirection {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT
}

/**
 * 인터랙티브 튜토리얼 오버레이
 */
@Composable
fun InteractiveTutorialOverlay(
    spots: List<TutorialSpot>,
    onComplete: () -> Unit,
    onSkip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(0) }
    val isLastStep = currentStep == spots.size - 1
    
    Dialog(
        onDismissRequest = { /* 배경 클릭으로 닫히지 않도록 */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
        ) {
            // 현재 스팟
            val currentSpot = spots[currentStep]
            
            // 스크린샷 이미지
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                // 스크린샷 (실제로는 앱 스크린샷 이미지)
                Image(
                    painter = painterResource(id = currentSpot.imageRes),
                    contentDescription = "Tutorial screenshot",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.FillWidth
                )
                
                // 강조 영역과 반짝이는 효과
                HighlightOverlay(
                    highlightArea = currentSpot.highlightArea,
                    arrowDirection = currentSpot.arrowDirection,
                    tapInstruction = currentSpot.tapInstruction
                )
            }
            
            // 상단 헤더
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "튜토리얼 ${currentStep + 1}/${spots.size}",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    TextButton(onClick = {
                        onSkip()
                        onComplete()
                    }) {
                        Text(
                            text = "건너뛰기",
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                
                // 진행 표시기
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    spots.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(1.5.dp))
                                .background(
                                    if (index <= currentStep) Color.White
                                    else Color.White.copy(alpha = 0.3f)
                                )
                        )
                    }
                }
            }
            
            // 하단 설명 카드
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            text = currentSpot.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Gray800
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = currentSpot.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600,
                            lineHeight = 24.sp
                        )
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // 버튼들
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (currentStep > 0) {
                                VTButton(
                                    text = "이전",
                                    onClick = { currentStep-- },
                                    variant = ButtonVariant.Outline,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            
                            VTButton(
                                text = if (isLastStep) "완료" else "다음",
                                onClick = {
                                    if (isLastStep) {
                                        onComplete()
                                    } else {
                                        currentStep++
                                    }
                                },
                                variant = ButtonVariant.Primary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 강조 오버레이 - 특정 영역을 강조하고 반짝이는 효과
 */
@Composable
fun HighlightOverlay(
    highlightArea: HighlightArea,
    arrowDirection: ArrowDirection,
    tapInstruction: String
) {
    // 펄스 애니메이션
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth.value
        val screenHeight = maxHeight.value
        
        val highlightX = highlightArea.x * screenWidth
        val highlightY = highlightArea.y * screenHeight
        val highlightWidth = highlightArea.width * screenWidth
        val highlightHeight = highlightArea.height * screenHeight
        
        // 반짝이는 강조 테두리
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            when (highlightArea.shape) {
                HighlightShape.RECTANGLE -> {
                    drawRoundRect(
                        color = Color.White.copy(alpha = pulseAlpha * 0.8f),
                        topLeft = Offset(highlightX, highlightY),
                        size = Size(highlightWidth, highlightHeight),
                        style = Stroke(width = 4.dp.toPx() * pulseScale)
                    )
                }
                HighlightShape.CIRCLE -> {
                    drawCircle(
                        color = Color.White.copy(alpha = pulseAlpha * 0.8f),
                        radius = (highlightWidth / 2) * pulseScale,
                        center = Offset(
                            highlightX + highlightWidth / 2,
                            highlightY + highlightHeight / 2
                        ),
                        style = Stroke(width = 4.dp.toPx() * pulseScale)
                    )
                }
            }
        }
        
        // 화살표와 안내 텍스트
        val arrowOffset = when (arrowDirection) {
            ArrowDirection.TOP -> Offset(
                highlightX + highlightWidth / 2,
                highlightY - 80.dp.value
            )
            ArrowDirection.BOTTOM -> Offset(
                highlightX + highlightWidth / 2,
                highlightY + highlightHeight + 80.dp.value
            )
            ArrowDirection.LEFT -> Offset(
                highlightX - 120.dp.value,
                highlightY + highlightHeight / 2
            )
            ArrowDirection.RIGHT -> Offset(
                highlightX + highlightWidth + 120.dp.value,
                highlightY + highlightHeight / 2
            )
        }
        
        // 포인터와 텍스트
        Box(
            modifier = Modifier
                .offset(x = arrowOffset.x.dp, y = arrowOffset.y.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (arrowDirection == ArrowDirection.TOP || arrowDirection == ArrowDirection.LEFT) {
                    // 텍스트가 위에
                    AnimatedTapInstruction(tapInstruction)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // 반짝이는 포인터 아이콘
                Icon(
                    imageVector = when (arrowDirection) {
                        ArrowDirection.TOP -> Icons.Filled.ArrowDownward
                        ArrowDirection.BOTTOM -> Icons.Filled.ArrowUpward
                        ArrowDirection.LEFT -> Icons.Filled.ArrowForward
                        ArrowDirection.RIGHT -> Icons.Filled.ArrowBack
                    },
                    contentDescription = null,
                    tint = Color.White.copy(alpha = pulseAlpha),
                    modifier = Modifier
                        .size((32 * pulseScale).dp)
                )
                
                if (arrowDirection == ArrowDirection.BOTTOM || arrowDirection == ArrowDirection.RIGHT) {
                    // 텍스트가 아래에
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedTapInstruction(tapInstruction)
                }
            }
        }
    }
}

@Composable
fun AnimatedTapInstruction(text: String) {
    val infiniteTransition = rememberInfiniteTransition(label = "text_pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_alpha"
    )
    
    Surface(
        color = PrimaryIndigo.copy(alpha = alpha),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 8.dp
    ) {
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

