package com.example.voicetutor.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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
import kotlinx.coroutines.launch

data class OnboardingPage(
    val title: String,
    val description: String,
    val imageRes: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
)

@Composable
fun OnboardingPager(
    pages: List<OnboardingPage>,
    onComplete: () -> Unit,
    onSkip: () -> Unit = {},
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E1B4B), // Dark indigo
                            Color(0xFF312E81), // Medium indigo
                            Color(0xFF4C1D95), // Dark purple
                            Color(0xFF1E1B4B), // Back to dark indigo
                        ),
                    ),
                ),
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
            ) {
                val scrollState = rememberScrollState()
                val isLastPage = remember { derivedStateOf { pagerState.currentPage == pages.size - 1 } }
                val isFirstPage = remember { derivedStateOf { pagerState.currentPage == 0 } }
                val pageOffset = remember { derivedStateOf { pagerState.currentPageOffsetFraction } }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
                            .height(40.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                )
                                .padding(horizontal = 10.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = "튜토리얼 ${pagerState.currentPage + 1}/${pages.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }

                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .width(80.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (!isLastPage.value) {
                                TextButton(
                                    onClick = {
                                        onSkip()
                                        onComplete()
                                    },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = Color.White.copy(alpha = 0.9f),
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        text = "건너뛰기",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        pages.indices.forEach { index ->
                            val isSelected = index == pagerState.currentPage
                            val shape = RoundedCornerShape(2.5.dp)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(5.dp)
                                    .clip(shape)
                                    .then(
                                        if (isSelected) {
                                            Modifier.background(
                                                brush = Brush.horizontalGradient(
                                                    colors = listOf(
                                                        Color.White,
                                                        Color.White.copy(alpha = 0.8f),
                                                    ),
                                                ),
                                                shape = shape,
                                            )
                                        } else {
                                            Modifier.background(
                                                color = Color.White.copy(alpha = 0.3f),
                                                shape = shape,
                                            )
                                        },
                                    )
                                    .shadow(
                                        elevation = if (isSelected) 4.dp else 0.dp,
                                        shape = shape,
                                        ambientColor = Color.White.copy(alpha = 0.3f),
                                        spotColor = Color.White.copy(alpha = 0.3f),
                                    ),
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(700.dp),
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                        ) { page ->
                            val isCurrentPageLast = page == pages.size - 1
                            OnboardingPageContent(
                                page = pages[page],
                                modifier = Modifier.fillMaxSize(),
                                showStartButton = isCurrentPageLast,
                                onStartClick = onComplete,
                            )
                        }

                        if (!isFirstPage.value) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .offset(x = (-12).dp)
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.25f),
                                                Color.White.copy(alpha = 0.15f),
                                            ),
                                        ),
                                    )
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = CircleShape,
                                        ambientColor = Color.Black.copy(alpha = 0.3f),
                                        spotColor = Color.Black.copy(alpha = 0.2f),
                                    )
                                    .clickable {
                                        if (pagerState.currentPage > 0) {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "이전",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }

                        if (!isLastPage.value) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .offset(x = 12.dp)
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.radialGradient(
                                            colors = listOf(
                                                Color.White.copy(alpha = 0.25f),
                                                Color.White.copy(alpha = 0.15f),
                                            ),
                                        ),
                                    )
                                    .shadow(
                                        elevation = 8.dp,
                                        shape = CircleShape,
                                        ambientColor = Color.Black.copy(alpha = 0.3f),
                                        spotColor = Color.Black.copy(alpha = 0.2f),
                                    )
                                    .clickable {
                                        if (pagerState.currentPage < pages.size - 1) {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                            }
                                        }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "다음",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp),
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }

                val onCompleteState = rememberUpdatedState(onComplete)
                var hasCompleted by remember { mutableStateOf(false) }
                var previousOffset by remember(pagerState.currentPage) { mutableStateOf(0f) }

                LaunchedEffect(pagerState.currentPage, pageOffset.value) {
                    val currentPageIndex = pagerState.currentPage
                    val isCurrentlyLastPage = currentPageIndex == pages.size - 1

                    if (!isCurrentlyLastPage) {
                        previousOffset = 0f
                        hasCompleted = false
                        return@LaunchedEffect
                    }

                    val isFullySettled = kotlin.math.abs(pageOffset.value) < 0.05f
                    val wasFullySettled = kotlin.math.abs(previousOffset) < 0.05f

                    if (wasFullySettled && pageOffset.value < -0.5f && !hasCompleted) {
                        hasCompleted = true
                        onCompleteState.value()
                    }

                    previousOffset = pageOffset.value

                    if (isFullySettled) {
                        hasCompleted = false
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(
    page: OnboardingPage,
    modifier: Modifier = Modifier,
    showStartButton: Boolean = false,
    onStartClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(9f / 16f)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.15f),
                            Color.White.copy(alpha = 0.08f),
                        ),
                    ),
                )
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color.Black.copy(alpha = 0.3f),
                    spotColor = Color.Black.copy(alpha = 0.2f),
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (page.icon != null) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    PrimaryIndigo.copy(alpha = 0.3f),
                                    PrimaryPurple.copy(alpha = 0.2f),
                                    Color.Transparent,
                                ),
                            ),
                            shape = RoundedCornerShape(70.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = page.icon,
                        contentDescription = null,
                        modifier = Modifier.size(120.dp),
                        tint = Color.White,
                    )
                }
            } else {
                Image(
                    painter = painterResource(id = page.imageRes),
                    contentDescription = page.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = page.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 8.dp),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(horizontal = 8.dp),
            )

            if (showStartButton) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    PrimaryIndigo,
                                    PrimaryPurple,
                                    Color(0xFF8B5CF6),
                                ),
                            ),
                        )
                        .shadow(
                            elevation = 12.dp,
                            shape = RoundedCornerShape(16.dp),
                            ambientColor = PrimaryIndigo.copy(alpha = 0.4f),
                            spotColor = PrimaryPurple.copy(alpha = 0.5f),
                        )
                        .clickable { onStartClick() },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "시작하기",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
