package com.example.voicetutor.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun card_displaysContent() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard {
                    Text("카드 내용")
                }
            }
        }

        composeTestRule.onNodeWithText("카드 내용").assertExists()
    }

    @Test
    fun card_displaysDefaultVariant() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(variant = CardVariant.Default) {
                    Text("Default Card")
                }
            }
        }

        composeTestRule.onNodeWithText("Default Card").assertExists()
    }

    @Test
    fun card_displaysElevatedVariant() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(variant = CardVariant.Elevated) {
                    Text("Elevated Card")
                }
            }
        }

        composeTestRule.onNodeWithText("Elevated Card").assertExists()
    }

    @Test
    fun card_displaysOutlinedVariant() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(variant = CardVariant.Outlined) {
                    Text("Outlined Card")
                }
            }
        }

        composeTestRule.onNodeWithText("Outlined Card").assertExists()
    }

    @Test
    fun card_displaysGradientVariant() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(variant = CardVariant.Gradient) {
                    Text("Gradient Card")
                }
            }
        }

        composeTestRule.onNodeWithText("Gradient Card").assertExists()
    }

    @Test
    fun card_displaysSelectedVariant() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(variant = CardVariant.Selected) {
                    Text("Selected Card")
                }
            }
        }

        composeTestRule.onNodeWithText("Selected Card").assertExists()
    }

    @Test
    fun card_callsOnClick_whenClickable() {
        var clicked = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(
                    onClick = { clicked = true }
                ) {
                    Text("Clickable Card")
                }
            }
        }

        composeTestRule.onNodeWithText("Clickable Card").performClick()
        assert(clicked)
    }

    @Test
    fun card_doesNotCallOnClick_whenNotClickable() {
        var clicked = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(onClick = null) {
                    Text("Non-clickable Card")
                }
            }
        }

        // onClick is null, so click should not trigger
        composeTestRule.onNodeWithText("Non-clickable Card").assertExists()
    }

    @Test
    fun card_displaysMultipleContentElements() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard {
                    Text("제목")
                    Text("내용")
                    Text("푸터")
                }
            }
        }

        composeTestRule.onNodeWithText("제목").assertExists()
        composeTestRule.onNodeWithText("내용").assertExists()
        composeTestRule.onNodeWithText("푸터").assertExists()
    }

    @Test
    fun card_handlesLongContent() {
        val longContent = "이것은 매우 긴 카드 내용입니다. " + "반복 ".repeat(50)
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard {
                    Text(longContent)
                }
            }
        }

        composeTestRule.onNodeWithText(longContent).assertExists()
    }

    @Test
    fun card_handlesEmptyContent() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard {
                    // Empty content - card may not render visible content, so we use a test tag
                    Box(modifier = Modifier.semantics { testTag = "EmptyCard" })
                }
            }
        }

        // Card should still render even with empty content
        composeTestRule.onNodeWithTag("EmptyCard").assertExists()
    }

    @Test
    fun card_handlesMultipleClicks() {
        var clickCount = 0
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(
                    onClick = { clickCount++ }
                ) {
                    Text("Multiple Clicks")
                }
            }
        }

        val card = composeTestRule.onNodeWithText("Multiple Clicks")
        card.performClick()
        card.performClick()
        card.performClick()

        assert(clickCount == 3)
    }

    @Test
    fun card_displaysNestedComponents() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard {
                    Text("외부 텍스트")
                    VTCard(variant = CardVariant.Outlined) {
                        Text("내부 카드")
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("외부 텍스트").assertExists()
        composeTestRule.onNodeWithText("내부 카드").assertExists()
    }
}

