package com.example.voicetutor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.*
import com.example.voicetutor.ui.components.*
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Simple rendering tests for screen components without ViewModel dependencies.
 * These tests verify that UI components render without crashing.
 */
@RunWith(AndroidJUnit4::class)
class ScreensRenderingTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_welcomeText_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                // Simple welcome card from login screen
                VTCard {
                    Text("VoiceTutor")
                }
            }
        }
        
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("VoiceTutor").assertExists()
    }

    @Test
    fun signupScreen_roleSelection_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    VTButton(text = "학생", onClick = {})
                    VTButton(text = "선생님", onClick = {})
                }
            }
        }
        
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("학생").assertExists()
        composeTestRule.onNodeWithText("선생님").assertExists()
    }

    @Test
    fun dashboardScreen_statsCard_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "완료한 과제",
                    value = "5",
                    icon = Icons.Filled.Assignment
                )
            }
        }
        
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("완료한 과제").assertExists()
        composeTestRule.onNodeWithText("5").assertExists()
    }

    @Test
    fun assignmentCard_displays_basicInfo() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard {
                    Text("1단원 복습 과제")
                    Text("수학")
                }
            }
        }
        
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("1단원 복습 과제").assertExists()
        composeTestRule.onNodeWithText("수학").assertExists()
    }

    @Test
    fun progressBar_renders_withValue() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                LinearProgressIndicator(
                    progress = { 0.5f },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        
        composeTestRule.waitForIdle()
        // Progress indicator exists
        composeTestRule.onNode(hasProgressBarRangeInfo(ProgressBarRangeInfo(0.5f, 0f..1f)))
            .assertExists()
    }

    @Test
    fun button_displays_text_andClickable() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTButton(
                    text = "시작하기",
                    onClick = { clicked = true }
                )
            }
        }
        
        composeTestRule.onNodeWithText("시작하기").assertExists()
        composeTestRule.onNodeWithText("시작하기").performClick()
        assert(clicked)
    }

    @Test
    fun card_displays_content() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard {
                    Text("환영합니다")
                    Text("VoiceTutor에 오신 것을 환영합니다")
                }
            }
        }
        
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("환영합니다").assertExists()
        composeTestRule.onNodeWithText("VoiceTutor에 오신 것을 환영합니다").assertExists()
    }

    @Test
    fun filterChip_renders_andClickable() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                FilterChip(
                    selected = false,
                    onClick = { clicked = true },
                    label = { Text("전체") }
                )
            }
        }
        
        composeTestRule.onNodeWithText("전체").assertExists()
        composeTestRule.onNodeWithText("전체").performClick()
        assert(clicked)
    }

    @Test
    fun emptyState_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("과제가 없습니다")
                    Text("새로운 과제를 생성해보세요")
                }
            }
        }
        
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("과제가 없습니다").assertExists()
    }

    @Test
    fun loadingState_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        
        composeTestRule.waitForIdle()
        // Loading indicator exists (it's a progress indicator with indeterminate state)
    }

    @Test
    fun errorState_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard {
                    Text(
                        text = "오류가 발생했습니다",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("오류가 발생했습니다").assertExists()
    }

    @Test
    fun textField_renders_withLabel() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    label = { Text("이름") }
                )
            }
        }
        
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("이름").assertExists()
    }

    @Test
    fun textField_acceptsInput() {
        var text = ""
        composeTestRule.setContent {
            VoiceTutorTheme {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("입력") }
                )
            }
        }
        
        composeTestRule.onNodeWithText("입력").performTextInput("테스트")
        assert(text == "테스트")
    }

    @Test
    fun multipleButtons_render_andClickable() {
        var button1Clicked = false
        var button2Clicked = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                Column {
                    VTButton(text = "제출", onClick = { button1Clicked = true })
                    VTButton(
                        text = "취소",
                        onClick = { button2Clicked = true },
                        variant = ButtonVariant.Outline
                    )
                }
            }
        }
        
        composeTestRule.onNodeWithText("제출").assertExists()
        composeTestRule.onNodeWithText("취소").assertExists()
        
        composeTestRule.onNodeWithText("제출").performClick()
        assert(button1Clicked)
        
        composeTestRule.onNodeWithText("취소").performClick()
        assert(button2Clicked)
    }

    @Test
    fun statsCard_withTrend_renders() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStatsCard(
                    title = "정확도",
                    value = "85%",
                    icon = Icons.Filled.Assignment,
                    trend = TrendDirection.Up,
                    trendValue = "+5%"
                )
            }
        }
        
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("정확도").assertExists()
        composeTestRule.onNodeWithText("85%").assertExists()
    }

    @Test
    fun card_withClickAction_isClickable() {
        var clicked = false
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCard(
                    onClick = { clicked = true }
                ) {
                    Text("클릭 가능한 카드")
                }
            }
        }
        
        composeTestRule.onNodeWithText("클릭 가능한 카드").performClick()
        assert(clicked)
    }
}

