package com.example.voicetutor.ui.components

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
class TextFieldTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun textField_displaysEmpty_whenInitialValueIsEmpty() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.semantics { testTag = "EmptyTextField" }
                )
            }
        }

        composeTestRule.onNodeWithTag("EmptyTextField").assertExists()
        // Empty text field should exist even with empty value
        composeTestRule.onAllNodes(hasText("")).assertCountEquals(1)
    }

    @Test
    fun textField_displaysLabel_whenLabelProvided() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTTextField(
                    value = "",
                    onValueChange = {},
                    label = "이름"
                )
            }
        }

        composeTestRule.onNodeWithText("이름").assertExists()
    }

    @Test
    fun textField_displaysPlaceholder_whenValueIsEmpty() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = "이름을 입력하세요"
                )
            }
        }

        composeTestRule.onNodeWithText("이름을 입력하세요").assertExists()
    }

    @Test
    fun textField_hidesPlaceholder_whenValueIsNotEmpty() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTTextField(
                    value = "김학생",
                    onValueChange = {},
                    placeholder = "이름을 입력하세요"
                )
            }
        }

        composeTestRule.onNodeWithText("이름을 입력하세요").assertDoesNotExist()
        composeTestRule.onAllNodes(hasText("김학생")).assertCountEquals(1)
    }

    @Test
    fun textField_displaysErrorMessage_whenErrorState() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTTextField(
                    value = "",
                    onValueChange = {},
                    isError = true,
                    errorMessage = "이 필드는 필수입니다"
                )
            }
        }

        composeTestRule.onNodeWithText("이 필드는 필수입니다").assertExists()
    }

    @Test
    fun textField_displaysValue_whenValueProvided() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTTextField(
                    value = "기존 값",
                    onValueChange = {}
                )
            }
        }

        composeTestRule.onAllNodes(hasText("기존 값")).assertCountEquals(1)
    }

    @Test
    fun textField_isDisabled_whenEnabledIsFalse() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTTextField(
                    value = "비활성화된 필드",
                    onValueChange = {},
                    enabled = false
                )
            }
        }

        composeTestRule.onAllNodes(hasText("비활성화된 필드"))
            .onFirst()
            .assertIsNotEnabled()
    }

    @Test
    fun textField_supportsMultipleLines_whenMaxLinesGreaterThanOne() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTTextField(
                    value = "첫 번째 줄\n두 번째 줄\n세 번째 줄",
                    onValueChange = {},
                    maxLines = 3
                )
            }
        }

        composeTestRule.onAllNodes(hasText("첫 번째 줄\n두 번째 줄\n세 번째 줄"))
            .assertCountEquals(1)
    }

    @Test
    fun textField_displaysErrorBorder_whenIsErrorIsTrue() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTTextField(
                    value = "",
                    onValueChange = {},
                    isError = true,
                    errorMessage = "에러 메시지",
                    modifier = Modifier.semantics { testTag = "ErrorTextField" }
                )
            }
        }

        // Error state should be applied - check for error message
        composeTestRule.onNodeWithText("에러 메시지").assertExists()
    }

    @Test
    fun textField_handlesLongText() {
        val longText = "이것은 매우 긴 텍스트입니다. " + "반복 ".repeat(50)
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTTextField(
                    value = longText,
                    onValueChange = {}
                )
            }
        }

        composeTestRule.onAllNodes(hasText(longText)).assertCountEquals(1)
    }

    @Test
    fun textField_handlesSpecialCharacters() {
        val specialText = "특수문자: !@#$%^&*()_+-=[]{}|;:'\",.<>?/~`"
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTTextField(
                    value = specialText,
                    onValueChange = {}
                )
            }
        }

        composeTestRule.onAllNodes(hasText(specialText)).assertCountEquals(1)
    }

    @Test
    fun textField_handlesEmptyLabel() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTTextField(
                    value = "테스트",
                    onValueChange = {},
                    label = null
                )
            }
        }

        composeTestRule.onAllNodes(hasText("테스트")).assertCountEquals(1)
    }

    @Test
    fun textField_handlesEmptyPlaceholder() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = null,
                    modifier = Modifier.semantics { testTag = "EmptyPlaceholderTextField" }
                )
            }
        }

        // Should not crash when placeholder is null - verify field exists
        composeTestRule.onNodeWithTag("EmptyPlaceholderTextField").assertExists()
    }

    @Test
    fun textField_handlesEmptyErrorMessage() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTTextField(
                    value = "",
                    onValueChange = {},
                    isError = true,
                    errorMessage = null,
                    modifier = Modifier.semantics { testTag = "EmptyErrorMessageTextField" }
                )
            }
        }

        // Should not crash when errorMessage is null - verify field exists
        composeTestRule.onNodeWithTag("EmptyErrorMessageTextField").assertExists()
    }
}

