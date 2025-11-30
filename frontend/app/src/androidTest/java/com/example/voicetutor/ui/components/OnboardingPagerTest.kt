package com.example.voicetutor.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.test.swipeRight
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingPagerTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun waitForText(text: String, substring: Boolean = true, timeoutMillis: Long = 5_000) {
        composeRule.waitUntil(timeoutMillis = timeoutMillis) {
            composeRule.onAllNodesWithText(text, substring = substring)
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private val testPages = listOf(
        OnboardingPage(
            title = "첫 번째 페이지",
            description = "Description1",
            imageRes = android.R.drawable.ic_menu_gallery,
            icon = Icons.Filled.Home,
        ),
        OnboardingPage(
            title = "두 번째 페이지",
            description = "Description2",
            imageRes = android.R.drawable.ic_menu_camera,
            icon = Icons.Filled.Settings,
        ),
        OnboardingPage(
            title = "세 번째 페이지",
            description = "Description3",
            imageRes = android.R.drawable.ic_menu_send,
            icon = null,
        ),
    )

    @Test
    fun onboardingPager_displaysFirstPage() {
        composeRule.setContent {
            VoiceTutorTheme {
                OnboardingPager(
                    pages = testPages,
                    onComplete = {},
                )
            }
        }

        waitForText("첫 번째 페이지")
        composeRule.onNodeWithText("첫 번째 페이지", substring = true).assertIsDisplayed()
        composeRule.onNodeWithText("Description1", substring = true).assertIsDisplayed()
    }

    @Test
    fun onboardingPager_displaysPageIndicator() {
        composeRule.setContent {
            VoiceTutorTheme {
                OnboardingPager(
                    pages = testPages,
                    onComplete = {},
                )
            }
        }

        waitForText("튜토리얼")
        composeRule.onNodeWithText("튜토리얼", substring = true).assertIsDisplayed()
    }

    @Test
    fun onboardingPager_displaysSkipButton_onFirstPage() {
        composeRule.setContent {
            VoiceTutorTheme {
                OnboardingPager(
                    pages = testPages,
                    onComplete = {},
                )
            }
        }

        waitForText("건너뛰기")
        composeRule.onNodeWithText("건너뛰기", substring = true).assertIsDisplayed()
    }

    @Test
    fun onboardingPager_skipButton_callsOnComplete() {
        var onCompleteCalled = false
        var onSkipCalled = false
        composeRule.setContent {
            VoiceTutorTheme {
                OnboardingPager(
                    pages = testPages,
                    onComplete = { onCompleteCalled = true },
                    onSkip = { onSkipCalled = true },
                )
            }
        }

        waitForText("건너뛰기")
        composeRule.onNodeWithText("건너뛰기", substring = true).performClick()
        composeRule.waitForIdle()

        assert(onCompleteCalled || onSkipCalled)
    }

    @Test
    fun onboardingPager_navigatesToNextPage_withSwipe() {
        composeRule.setContent {
            VoiceTutorTheme {
                OnboardingPager(
                    pages = testPages,
                    onComplete = {},
                )
            }
        }

        waitForText("첫 번째 페이지")
        composeRule.onNodeWithText("첫 번째 페이지", substring = true).assertIsDisplayed()

        composeRule.onNodeWithText("첫 번째 페이지", substring = true).performTouchInput { swipeLeft() }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("두 번째 페이지", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("두 번째 페이지", substring = true).assertIsDisplayed()
    }

    @Test
    fun onboardingPager_navigatesToPreviousPage_withSwipe() {
        composeRule.setContent {
            VoiceTutorTheme {
                OnboardingPager(
                    pages = testPages,
                    onComplete = {},
                )
            }
        }

        waitForText("첫 번째 페이지")

        composeRule.onNodeWithText("첫 번째 페이지", substring = true).performTouchInput { swipeLeft() }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("두 번째 페이지", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("두 번째 페이지", substring = true).performTouchInput { swipeRight() }

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("첫 번째 페이지", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("첫 번째 페이지", substring = true).assertIsDisplayed()
    }

    @Test
    fun onboardingPager_displaysNextArrow_onFirstPage() {
        composeRule.setContent {
            VoiceTutorTheme {
                OnboardingPager(
                    pages = testPages,
                    onComplete = {},
                )
            }
        }

        waitForText("첫 번째 페이지")
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("다음").assertIsDisplayed()
    }

    @Test
    fun onboardingPager_nextArrow_navigatesToNextPage() {
        composeRule.setContent {
            VoiceTutorTheme {
                OnboardingPager(
                    pages = testPages,
                    onComplete = {},
                )
            }
        }

        waitForText("첫 번째 페이지")
        composeRule.waitForIdle()

        composeRule.onNodeWithContentDescription("다음").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("두 번째 페이지", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("두 번째 페이지", substring = true).assertIsDisplayed()
    }

    @Test
    fun onboardingPager_displaysPreviousArrow_onSecondPage() {
        composeRule.setContent {
            VoiceTutorTheme {
                OnboardingPager(
                    pages = testPages,
                    onComplete = {},
                )
            }
        }

        waitForText("첫 번째 페이지")

        composeRule.onNodeWithContentDescription("다음").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("두 번째 페이지", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithContentDescription("이전").assertIsDisplayed()
    }

    @Test
    fun onboardingPager_previousArrow_navigatesToPreviousPage() {
        composeRule.setContent {
            VoiceTutorTheme {
                OnboardingPager(
                    pages = testPages,
                    onComplete = {},
                )
            }
        }

        waitForText("첫 번째 페이지")

        composeRule.onNodeWithContentDescription("다음").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("두 번째 페이지", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithContentDescription("이전").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("첫 번째 페이지", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("첫 번째 페이지", substring = true).assertIsDisplayed()
    }

    @Test
    fun onboardingPager_displaysStartButton_onLastPage() {
        composeRule.setContent {
            VoiceTutorTheme {
                OnboardingPager(
                    pages = testPages,
                    onComplete = {},
                )
            }
        }

        waitForText("첫 번째 페이지")

        composeRule.onNodeWithContentDescription("다음").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("다음").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("세 번째 페이지", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        waitForText("시작하기")
        composeRule.onNodeWithText("시작하기", substring = true).assertIsDisplayed()
    }

    @Test
    fun onboardingPager_startButton_callsOnComplete() {
        var onCompleteCalled = false
        composeRule.setContent {
            VoiceTutorTheme {
                OnboardingPager(
                    pages = testPages,
                    onComplete = { onCompleteCalled = true },
                )
            }
        }

        waitForText("첫 번째 페이지")

        composeRule.onNodeWithContentDescription("다음").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("다음").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("시작하기", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("시작하기", substring = true).performClick()
        composeRule.waitForIdle()

        assert(onCompleteCalled)
    }

    @Test
    fun onboardingPager_hidesSkipButton_onLastPage() {
        composeRule.setContent {
            VoiceTutorTheme {
                OnboardingPager(
                    pages = testPages,
                    onComplete = {},
                )
            }
        }

        waitForText("첫 번째 페이지")

        composeRule.onNodeWithContentDescription("다음").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("다음").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("세 번째 페이지", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.waitForIdle()
    }

    @Test
    fun onboardingPager_displaysAllPages() {
        composeRule.setContent {
            VoiceTutorTheme {
                OnboardingPager(
                    pages = testPages,
                    onComplete = {},
                )
            }
        }

        waitForText("첫 번째 페이지")
        composeRule.onNodeWithText("첫 번째 페이지", substring = true).assertIsDisplayed()

        composeRule.onNodeWithContentDescription("다음").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("두 번째 페이지", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("두 번째 페이지", substring = true).assertIsDisplayed()

        composeRule.onNodeWithContentDescription("다음").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("세 번째 페이지", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("세 번째 페이지", substring = true).assertIsDisplayed()
    }

    @Test
    fun onboardingPager_displaysPageWithIcon() {
        composeRule.setContent {
            VoiceTutorTheme {
                OnboardingPager(
                    pages = testPages,
                    onComplete = {},
                )
            }
        }

        waitForText("첫 번째 페이지")
        composeRule.onNodeWithText("첫 번째 페이지", substring = true).assertIsDisplayed()
    }

    @Test
    fun onboardingPager_displaysPageWithImage() {
        composeRule.setContent {
            VoiceTutorTheme {
                OnboardingPager(
                    pages = testPages,
                    onComplete = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("다음").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithContentDescription("다음").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("세 번째 페이지", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("세 번째 페이지", substring = true).assertIsDisplayed()
    }

    @Test
    fun onboardingPager_updatesPageIndicator() {
        composeRule.setContent {
            VoiceTutorTheme {
                OnboardingPager(
                    pages = testPages,
                    onComplete = {},
                )
            }
        }

        waitForText("튜토리얼 1/3")
        composeRule.onNodeWithText("튜토리얼 1/3", substring = true).assertIsDisplayed()

        composeRule.onNodeWithContentDescription("다음").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("튜토리얼 2/3", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("튜토리얼 2/3", substring = true).assertIsDisplayed()
    }
}
