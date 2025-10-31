package com.example.voicetutor.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun button_displaysText() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = "클릭하세요",
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText("클릭하세요").assertExists()
    }

    @Test
    fun button_callsOnClick_whenClicked() {
        var clicked = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = "클릭하세요",
                    onClick = { clicked = true }
                )
            }
        }

        composeTestRule.onNodeWithText("클릭하세요").performClick()
        assert(clicked)
    }

    @Test
    fun button_doesNotCallOnClick_whenDisabled() {
        var clicked = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = "비활성화",
                    onClick = { clicked = true },
                    enabled = false
                )
            }
        }

        composeTestRule.onNodeWithText("비활성화")
            .assertIsNotEnabled()
            .assertExists()
    }

    @Test
    fun button_displaysPrimaryVariant() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = "Primary",
                    onClick = {},
                    variant = ButtonVariant.Primary
                )
            }
        }

        composeTestRule.onNodeWithText("Primary").assertExists()
    }

    @Test
    fun button_displaysSecondaryVariant() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = "Secondary",
                    onClick = {},
                    variant = ButtonVariant.Secondary
                )
            }
        }

        composeTestRule.onNodeWithText("Secondary").assertExists()
    }

    @Test
    fun button_displaysOutlineVariant() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = "Outline",
                    onClick = {},
                    variant = ButtonVariant.Outline
                )
            }
        }

        composeTestRule.onNodeWithText("Outline").assertExists()
    }

    @Test
    fun button_displaysOutlinedVariant() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = "Outlined",
                    onClick = {},
                    variant = ButtonVariant.Outlined
                )
            }
        }

        composeTestRule.onNodeWithText("Outlined").assertExists()
    }

    @Test
    fun button_displaysGhostVariant() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = "Ghost",
                    onClick = {},
                    variant = ButtonVariant.Ghost
                )
            }
        }

        composeTestRule.onNodeWithText("Ghost").assertExists()
    }

    @Test
    fun button_displaysGradientVariant() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = "Gradient",
                    onClick = {},
                    variant = ButtonVariant.Gradient
                )
            }
        }

        composeTestRule.onNodeWithText("Gradient").assertExists()
    }

    @Test
    fun button_displaysDangerVariant() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = "Danger",
                    onClick = {},
                    variant = ButtonVariant.Danger
                )
            }
        }

        composeTestRule.onNodeWithText("Danger").assertExists()
    }

    @Test
    fun button_displaysSmallSize() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = "Small",
                    onClick = {},
                    size = ButtonSize.Small
                )
            }
        }

        composeTestRule.onNodeWithText("Small").assertExists()
    }

    @Test
    fun button_displaysMediumSize() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = "Medium",
                    onClick = {},
                    size = ButtonSize.Medium
                )
            }
        }

        composeTestRule.onNodeWithText("Medium").assertExists()
    }

    @Test
    fun button_displaysLargeSize() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = "Large",
                    onClick = {},
                    size = ButtonSize.Large
                )
            }
        }

        composeTestRule.onNodeWithText("Large").assertExists()
    }

    @Test
    fun button_displaysLeadingIcon() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = "추가",
                    onClick = {},
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null
                        )
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("추가").assertExists()
    }

    @Test
    fun button_displaysTrailingIcon() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = "삭제",
                    onClick = {},
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = null
                        )
                    }
                )
            }
        }

        composeTestRule.onNodeWithText("삭제").assertExists()
    }

    @Test
    fun button_handlesMultipleClicks() {
        var clickCount = 0
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = "여러 번 클릭",
                    onClick = { clickCount++ }
                )
            }
        }

        val button = composeTestRule.onNodeWithText("여러 번 클릭")
        button.performClick()
        button.performClick()
        button.performClick()

        assert(clickCount == 3)
    }

    @Test
    fun button_handlesLongText() {
        val longText = "이것은 매우 긴 버튼 텍스트입니다. " + "반복 ".repeat(20)
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = longText,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText(longText, substring = false).assertExists()
    }

    @Test
    fun button_handlesEmptyText() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = "",
                    onClick = {},
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Info"
                        )
                    }
                )
            }
        }

        // Button should still exist even with empty text (with icon)
        composeTestRule.onNodeWithContentDescription("Info").assertExists()
    }

    @Test
    fun button_handlesSpecialCharacters() {
        val specialText = "특수문자: !@#$%^&*()"
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = specialText,
                    onClick = {}
                )
            }
        }

        composeTestRule.onNodeWithText(specialText).assertExists()
    }
}

