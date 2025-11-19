package com.example.voicetutor.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HeaderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun header_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTHeader(
                    title = "테스트 제목",
                    onBackClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText("테스트 제목").assertExists()
    }

    @Test
    fun header_callsOnBackClick_whenBackButtonClicked() {
        var backClicked = false

        composeTestRule.setContent {
            VoiceTutorTheme {
                VTHeader(
                    title = "테스트",
                    onBackClick = { backClicked = true },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("뒤로가기").performClick()
        assert(backClicked)
    }

    @Test
    fun header_displaysBackButton() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTHeader(
                    title = "테스트",
                    onBackClick = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("뒤로가기").assertExists()
    }

    @Test
    fun header_handlesLongTitle() {
        val longTitle = "이것은 매우 긴 헤더 제목입니다. " + "반복 ".repeat(20)

        composeTestRule.setContent {
            VoiceTutorTheme {
                VTHeader(
                    title = longTitle,
                    onBackClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText(longTitle, substring = true).assertExists()
    }

    @Test
    fun header_handlesEmptyTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTHeader(
                    title = "",
                    onBackClick = {},
                )
            }
        }

        // Header should still render even with empty title
        composeTestRule.onNodeWithContentDescription("뒤로가기").assertExists()
    }

    @Test
    fun header_handlesSpecialCharacters() {
        val specialTitle = "특수문자: !@#$%^&*()_+-=[]{}|;:'\",.<>?/~`"

        composeTestRule.setContent {
            VoiceTutorTheme {
                VTHeader(
                    title = specialTitle,
                    onBackClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText(specialTitle).assertExists()
    }

    @Test
    fun header_handlesDefaultOnBackClick() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTHeader(
                    title = "기본 콜백",
                    onBackClick = {},
                )
            }
        }

        // Should not crash when clicking with default callback
        composeTestRule.onNodeWithContentDescription("뒤로가기").performClick()
    }

    @Test
    fun header_displaysCorrectLayout() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTHeader(
                    title = "레이아웃 테스트",
                    onBackClick = {},
                )
            }
        }

        // Should have back button, title, and spacer
        composeTestRule.onNodeWithContentDescription("뒤로가기").assertExists()
        composeTestRule.onNodeWithText("레이아웃 테스트").assertExists()
    }

    @Test
    fun header_handlesMultipleBackClicks() {
        var clickCount = 0

        composeTestRule.setContent {
            VoiceTutorTheme {
                VTHeader(
                    title = "여러 번 클릭",
                    onBackClick = { clickCount++ },
                )
            }
        }

        val backButton = composeTestRule.onNodeWithContentDescription("뒤로가기")
        backButton.performClick()
        backButton.performClick()
        backButton.performClick()

        assert(clickCount == 3)
    }

    @Test
    fun header_handlesUnicodeCharacters() {
        val unicodeTitle = "한글 🎉 Emoji 中文 日本語"

        composeTestRule.setContent {
            VoiceTutorTheme {
                VTHeader(
                    title = unicodeTitle,
                    onBackClick = {},
                )
            }
        }

        composeTestRule.onNodeWithText(unicodeTitle).assertExists()
    }
}
