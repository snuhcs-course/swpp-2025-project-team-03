package com.example.voicetutor.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.voicetutor.ui.theme.*

/**
 * 온보딩 페이지 데이터 모델
 */
data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int, // drawable 리소스 ID (임시로 android.R.drawable 사용 가능)
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null // 아이콘 (이미지 대신 사용 가능)
)

/**
 * 재사용 가능한 온보딩 Pager 컴포넌트
 */
@Composable
fun OnboardingPager(
    pages: List<OnboardingPage>,
    onComplete: () -> Unit,
    onSkip: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    
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
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PrimaryIndigo.copy(alpha = 0.1f),
                            PrimaryPurple.copy(alpha = 0.1f),
                            LightBlue.copy(alpha = 0.1f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 상단 헤더 (건너뛰기 버튼)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "튜토리얼 ${pagerState.currentPage + 1}/${pages.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    
                    TextButton(onClick = {
                        onSkip()
                        onComplete()
                    }) {
                        Text(
                            text = if (pagerState.currentPage == pages.size - 1) "시작하기" else "건너뛰기",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                // 페이지 인디케이터
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pages.indices.forEach { index ->
                        val isSelected = index == pagerState.currentPage
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (isSelected) Color.White
                                    else Gray400.copy(alpha = 0.5f)
                                )
                        )
                    }
                }
                
                // Pager 콘텐츠
                val isLastPage = remember { derivedStateOf { pagerState.currentPage == pages.size - 1 } }
                val pageOffset = remember { derivedStateOf { pagerState.currentPageOffsetFraction } }
                
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { page ->
                    OnboardingPageContent(
                        page = pages[page],
                        modifier = Modifier.fillMaxSize()
                    )
                }
                
                // 마지막 페이지에서 오른쪽으로 스와이프 시 완료 처리
                val onCompleteState = rememberUpdatedState(onComplete)
                var hasCompleted by remember { mutableStateOf(false) }
                var previousOffset by remember(pagerState.currentPage) { mutableStateOf(0f) }
                
                LaunchedEffect(pagerState.currentPage, pageOffset.value) {
                    // 현재 페이지가 마지막 페이지인지 확인 (학생: 인덱스 5, 선생님: 인덱스 7)
                    // 실제로는 학생: 인덱스 4 (5번째), 선생님: 인덱스 6 (7번째)이지만
                    // 체크를 더 엄격하게 하기 위해 pages.size와 비교
                    val currentPageIndex = pagerState.currentPage
                    val isCurrentlyLastPage = currentPageIndex == pages.size - 1
                    
                    // 마지막 페이지가 아니면 리셋하고 종료
                    if (!isCurrentlyLastPage) {
                        previousOffset = 0f
                        hasCompleted = false
                        return@LaunchedEffect
                    }
                    
                    // 마지막 페이지에서만 처리
                    // 페이지가 완전히 안정된 상태(오프셋이 0)에서 시작해서 음수로 변했을 때만 완료
                    val isFullySettled = kotlin.math.abs(pageOffset.value) < 0.05f
                    val wasFullySettled = kotlin.math.abs(previousOffset) < 0.05f
                    
                    // 안정된 상태에서 오른쪽으로 스와이프할 때만 완료
                    if (wasFullySettled && pageOffset.value < -0.5f && !hasCompleted) {
                        hasCompleted = true
                        onCompleteState.value()
                    }
                    
                    // 오프셋 업데이트
                    previousOffset = pageOffset.value
                    
                    // 스와이프가 끝나면 플래그 리셋 (다시 시도할 수 있도록)
                    if (isFullySettled) {
                        hasCompleted = false
                    }
                }
            }
        }
    }
}

/**
 * 개별 온보딩 페이지 콘텐츠
 */
@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // 이미지 또는 아이콘 - 위쪽에 액자 스타일로 배치 (9:16 비율)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.80f)
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (page.icon != null) {
                Icon(
                    imageVector = page.icon,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = PrimaryIndigo
                )
            } else {
                Image(
                    painter = painterResource(id = page.imageRes),
                    contentDescription = page.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // 제목과 설명 - 이미지 아래에 배치
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 제목
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 설명
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                color = Gray200,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

