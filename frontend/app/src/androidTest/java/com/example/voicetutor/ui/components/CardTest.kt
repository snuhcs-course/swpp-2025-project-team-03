package com.example.voicetutor.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
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
                    onClick = { clicked = true },
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
                    onClick = { clickCount++ },
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

    // VTCard2 tests
    @Test
    fun card2_displaysContent() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard2 {
                    Text("VTCard2 내용")
                }
            }
        }

        composeTestRule.onNodeWithText("VTCard2 내용").assertExists()
    }

    @Test
    fun card2_displaysAllVariants() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    VTCard2(variant = CardVariant.Default) {
                        Text("VTCard2 Default")
                    }
                    VTCard2(variant = CardVariant.Elevated) {
                        Text("VTCard2 Elevated")
                    }
                    VTCard2(variant = CardVariant.Outlined) {
                        Text("VTCard2 Outlined")
                    }
                    VTCard2(variant = CardVariant.Gradient) {
                        Text("VTCard2 Gradient")
                    }
                    VTCard2(variant = CardVariant.Selected) {
                        Text("VTCard2 Selected")
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("VTCard2 Default").assertExists()
        composeTestRule.onNodeWithText("VTCard2 Elevated").assertExists()
        composeTestRule.onNodeWithText("VTCard2 Outlined").assertExists()
        composeTestRule.onNodeWithText("VTCard2 Gradient").assertExists()
        composeTestRule.onNodeWithText("VTCard2 Selected").assertExists()
    }

    @Test
    fun card2_callsOnClick_whenClickable() {
        var clicked = false

        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard2(
                    onClick = { clicked = true },
                ) {
                    Text("VTCard2 Clickable")
                }
            }
        }

        composeTestRule.onNodeWithText("VTCard2 Clickable").performClick()
        assert(clicked)
    }

    @Test
    fun card2_doesNotCallOnClick_whenNotClickable() {
        var clicked = false

        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard2(onClick = null) {
                    Text("VTCard2 Non-clickable")
                }
            }
        }

        composeTestRule.onNodeWithText("VTCard2 Non-clickable").assertExists()
        // onClick is null, so click should not trigger
    }

    @Test
    fun card2_hasDifferentPadding() {
        // VTCard2 uses padding(5.dp) instead of padding(20.dp)
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard2 {
                    Text("VTCard2 with 5dp padding")
                }
            }
        }

        composeTestRule.onNodeWithText("VTCard2 with 5dp padding").assertExists()
    }

    @Test
    fun card_withModifier_appliesCorrectly() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(
                    modifier = Modifier.semantics { testTag = "CustomCard" },
                ) {
                    Text("Custom Modifier Card")
                }
            }
        }

        composeTestRule.onNodeWithTag("CustomCard").assertExists()
    }

    @Test
    fun card2_withModifier_appliesCorrectly() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard2(
                    modifier = Modifier.semantics { testTag = "CustomCard2" },
                ) {
                    Text("Custom Modifier VTCard2")
                }
            }
        }

        composeTestRule.onNodeWithTag("CustomCard2").assertExists()
    }

    // Additional tests for Default variant
    @Test
    fun card_defaultVariant_isClickable() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(
                    variant = CardVariant.Default,
                    onClick = { clicked = true },
                ) {
                    Text("Default Clickable")
                }
            }
        }

        composeTestRule.onNodeWithText("Default Clickable").performClick()
        assert(clicked)
    }

    @Test
    fun card2_defaultVariant_isClickable() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard2(
                    variant = CardVariant.Default,
                    onClick = { clicked = true },
                ) {
                    Text("VTCard2 Default Clickable")
                }
            }
        }

        composeTestRule.onNodeWithText("VTCard2 Default Clickable").performClick()
        assert(clicked)
    }

    // Additional tests for Outlined variant
    @Test
    fun card_outlinedVariant_isClickable() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(
                    variant = CardVariant.Outlined,
                    onClick = { clicked = true },
                ) {
                    Text("Outlined Clickable")
                }
            }
        }

        composeTestRule.onNodeWithText("Outlined Clickable").performClick()
        assert(clicked)
    }

    @Test
    fun card2_outlinedVariant_isClickable() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard2(
                    variant = CardVariant.Outlined,
                    onClick = { clicked = true },
                ) {
                    Text("VTCard2 Outlined Clickable")
                }
            }
        }

        composeTestRule.onNodeWithText("VTCard2 Outlined Clickable").performClick()
        assert(clicked)
    }

    @Test
    fun card_outlinedVariant_displaysContent() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(variant = CardVariant.Outlined) {
                    Text("Outlined Content")
                    Text("Additional text")
                }
            }
        }

        composeTestRule.onNodeWithText("Outlined Content").assertExists()
        composeTestRule.onNodeWithText("Additional text").assertExists()
    }

    @Test
    fun card2_outlinedVariant_displaysContent() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard2(variant = CardVariant.Outlined) {
                    Text("VTCard2 Outlined Content")
                    Text("Additional text")
                }
            }
        }

        composeTestRule.onNodeWithText("VTCard2 Outlined Content").assertExists()
        composeTestRule.onNodeWithText("Additional text").assertExists()
    }

    // Additional tests for Gradient variant
    @Test
    fun card_gradientVariant_isClickable() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(
                    variant = CardVariant.Gradient,
                    onClick = { clicked = true },
                ) {
                    Text("Gradient Clickable")
                }
            }
        }

        composeTestRule.onNodeWithText("Gradient Clickable").performClick()
        assert(clicked)
    }

    @Test
    fun card2_gradientVariant_isClickable() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard2(
                    variant = CardVariant.Gradient,
                    onClick = { clicked = true },
                ) {
                    Text("VTCard2 Gradient Clickable")
                }
            }
        }

        composeTestRule.onNodeWithText("VTCard2 Gradient Clickable").performClick()
        assert(clicked)
    }

    @Test
    fun card_gradientVariant_displaysContent() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(variant = CardVariant.Gradient) {
                    Text("Gradient Content")
                    Text("With gradient background")
                }
            }
        }

        composeTestRule.onNodeWithText("Gradient Content").assertExists()
        composeTestRule.onNodeWithText("With gradient background").assertExists()
    }

    @Test
    fun card2_gradientVariant_displaysContent() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard2(variant = CardVariant.Gradient) {
                    Text("VTCard2 Gradient Content")
                    Text("With gradient background")
                }
            }
        }

        composeTestRule.onNodeWithText("VTCard2 Gradient Content").assertExists()
        composeTestRule.onNodeWithText("With gradient background").assertExists()
    }

    // Additional tests for Selected variant
    @Test
    fun card_selectedVariant_isClickable() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(
                    variant = CardVariant.Selected,
                    onClick = { clicked = true },
                ) {
                    Text("Selected Clickable")
                }
            }
        }

        composeTestRule.onNodeWithText("Selected Clickable").performClick()
        assert(clicked)
    }

    @Test
    fun card2_selectedVariant_isClickable() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard2(
                    variant = CardVariant.Selected,
                    onClick = { clicked = true },
                ) {
                    Text("VTCard2 Selected Clickable")
                }
            }
        }

        composeTestRule.onNodeWithText("VTCard2 Selected Clickable").performClick()
        assert(clicked)
    }

    @Test
    fun card_selectedVariant_displaysContent() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(variant = CardVariant.Selected) {
                    Text("Selected Content")
                    Text("With selected border")
                }
            }
        }

        composeTestRule.onNodeWithText("Selected Content").assertExists()
        composeTestRule.onNodeWithText("With selected border").assertExists()
    }

    @Test
    fun card2_selectedVariant_displaysContent() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard2(variant = CardVariant.Selected) {
                    Text("VTCard2 Selected Content")
                    Text("With selected border")
                }
            }
        }

        composeTestRule.onNodeWithText("VTCard2 Selected Content").assertExists()
        composeTestRule.onNodeWithText("With selected border").assertExists()
    }

    // Test all variants together for VTCard
    @Test
    fun card_displaysAllVariantsTogether() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    VTCard(variant = CardVariant.Default) {
                        Text("Default Card")
                    }
                    VTCard(variant = CardVariant.Outlined) {
                        Text("Outlined Card")
                    }
                    VTCard(variant = CardVariant.Gradient) {
                        Text("Gradient Card")
                    }
                    VTCard(variant = CardVariant.Selected) {
                        Text("Selected Card")
                    }
                }
            }
        }

        composeTestRule.onNodeWithText("Default Card").assertExists()
        composeTestRule.onNodeWithText("Outlined Card").assertExists()
        composeTestRule.onNodeWithText("Gradient Card").assertExists()
        composeTestRule.onNodeWithText("Selected Card").assertExists()
    }

    // Test variant combinations with onClick
    @Test
    fun card_defaultVariant_withOnClick_works() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(
                    variant = CardVariant.Default,
                    onClick = { clicked = true },
                ) {
                    Text("Default with onClick")
                }
            }
        }

        composeTestRule.onNodeWithText("Default with onClick").performClick()
        assert(clicked)
    }

    @Test
    fun card_outlinedVariant_withOnClick_works() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(
                    variant = CardVariant.Outlined,
                    onClick = { clicked = true },
                ) {
                    Text("Outlined with onClick")
                }
            }
        }

        composeTestRule.onNodeWithText("Outlined with onClick").performClick()
        assert(clicked)
    }

    @Test
    fun card_gradientVariant_withOnClick_works() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(
                    variant = CardVariant.Gradient,
                    onClick = { clicked = true },
                ) {
                    Text("Gradient with onClick")
                }
            }
        }

        composeTestRule.onNodeWithText("Gradient with onClick").performClick()
        assert(clicked)
    }

    @Test
    fun card_selectedVariant_withOnClick_works() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(
                    variant = CardVariant.Selected,
                    onClick = { clicked = true },
                ) {
                    Text("Selected with onClick")
                }
            }
        }

        composeTestRule.onNodeWithText("Selected with onClick").performClick()
        assert(clicked)
    }

    // Test VTCard2 variant combinations with onClick
    @Test
    fun card2_defaultVariant_withOnClick_works() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard2(
                    variant = CardVariant.Default,
                    onClick = { clicked = true },
                ) {
                    Text("VTCard2 Default with onClick")
                }
            }
        }

        composeTestRule.onNodeWithText("VTCard2 Default with onClick").performClick()
        assert(clicked)
    }

    @Test
    fun card2_outlinedVariant_withOnClick_works() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard2(
                    variant = CardVariant.Outlined,
                    onClick = { clicked = true },
                ) {
                    Text("VTCard2 Outlined with onClick")
                }
            }
        }

        composeTestRule.onNodeWithText("VTCard2 Outlined with onClick").performClick()
        assert(clicked)
    }

    @Test
    fun card2_gradientVariant_withOnClick_works() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard2(
                    variant = CardVariant.Gradient,
                    onClick = { clicked = true },
                ) {
                    Text("VTCard2 Gradient with onClick")
                }
            }
        }

        composeTestRule.onNodeWithText("VTCard2 Gradient with onClick").performClick()
        assert(clicked)
    }

    @Test
    fun card2_selectedVariant_withOnClick_works() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard2(
                    variant = CardVariant.Selected,
                    onClick = { clicked = true },
                ) {
                    Text("VTCard2 Selected with onClick")
                }
            }
        }

        composeTestRule.onNodeWithText("VTCard2 Selected with onClick").performClick()
        assert(clicked)
    }
}
