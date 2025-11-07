package com.example.voicetutor.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.voicetutor.ui.theme.*

/**
 * 튜토리얼 단계 데이터
 */
data class TutorialStep(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color = PrimaryIndigo
)

/**
 * 튜토리얼 오버레이 컴포넌트
 * 앱 첫 사용자를 위한 튜토리얼 UI를 제공합니다.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun TutorialOverlay(
    steps: List<TutorialStep>,
    onComplete: () -> Unit,
    onSkip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentStep by remember { mutableStateOf(0) }
    val isLastStep = currentStep == steps.size - 1
    
    // 애니메이션 설정
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
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
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = modifier
                    .fillMaxWidth(0.9f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 건너뛰기 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            onSkip()
                            onComplete()
                        }
                    ) {
                        Text(
                            text = "건너뛰기",
                            color = Gray600,
                            fontSize = 14.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 현재 스텝 콘텐츠
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            // 다음으로 이동
                            slideInHorizontally { width -> width } + fadeIn() with
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            // 이전으로 이동
                            slideInHorizontally { width -> -width } + fadeIn() with
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }.using(SizeTransform(clip = false))
                    },
                    label = "step_content"
                ) { step ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // 아이콘
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(steps[step].iconColor.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = steps[step].icon,
                                contentDescription = null,
                                tint = steps[step].iconColor,
                                modifier = Modifier.size(56.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // 제목
                        Text(
                            text = steps[step].title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Gray800,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 설명
                        Text(
                            text = steps[step].description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Gray600,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // 진행 표시기 (점)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    steps.indices.forEach { index ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentStep) 12.dp else 8.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(
                                    if (index == currentStep) PrimaryIndigo
                                    else Gray300
                                )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 버튼들
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 이전 버튼
                    if (currentStep > 0) {
                        VTButton(
                            text = "이전",
                            onClick = { currentStep-- },
                            variant = ButtonVariant.Outline,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    
                    // 다음/시작하기 버튼
                    VTButton(
                        text = if (isLastStep) "시작하기" else "다음",
                        onClick = {
                            if (isLastStep) {
                                onComplete()
                            } else {
                                currentStep++
                            }
                        },
                        variant = ButtonVariant.Primary,
                        modifier = Modifier.weight(if (currentStep > 0) 1f else 1f)
                    )
                }
            }
        }
    }
}

/**
 * 튜토리얼 미리보기
 */
@Composable
fun TutorialOverlayPreview(
    steps: List<TutorialStep>,
    onDismiss: () -> Unit
) {
    TutorialOverlay(
        steps = steps,
        onComplete = onDismiss,
        onSkip = onDismiss
    )
}

