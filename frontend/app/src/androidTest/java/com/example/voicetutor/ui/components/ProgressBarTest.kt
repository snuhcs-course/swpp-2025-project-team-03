package com.example.voicetutor.ui.components

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProgressBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // VTProgressBar Tests
    @Test
    fun progressBar_displaysProgressText() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTProgressBar(
                    progress = 0.5f,
                    showPercentage = true
                )
            }
        }

        composeTestRule.onNodeWithText("진행률").assertExists()
        composeTestRule.onNodeWithText("50%", substring = true).assertExists()
    }

    @Test
    fun progressBar_displaysZeroProgress() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTProgressBar(
                    progress = 0f,
                    showPercentage = true
                )
            }
        }

        composeTestRule.onNodeWithText("0%", substring = true).assertExists()
    }

    @Test
    fun progressBar_displaysFullProgress() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTProgressBar(
                    progress = 1f,
                    showPercentage = true
                )
            }
        }

        composeTestRule.onNodeWithText("100%", substring = true).assertExists()
    }

    @Test
    fun progressBar_hidesPercentage_whenShowPercentageIsFalse() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTProgressBar(
                    progress = 0.5f,
                    showPercentage = false
                )
            }
        }

        composeTestRule.onNodeWithText("진행률").assertDoesNotExist()
    }

    @Test
    fun progressBar_handlesNegativeProgress() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTProgressBar(
                    progress = -0.5f,
                    showPercentage = true
                )
            }
        }

        // Progress should be clamped to 0
        composeTestRule.onNodeWithText("0%", substring = true).assertExists()
    }

    @Test
    fun progressBar_handlesProgressGreaterThanOne() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTProgressBar(
                    progress = 1.5f,
                    showPercentage = true,
                    animated = false // Disable animation for test stability
                )
            }
        }

        // Progress should be clamped to 100%
        composeTestRule.onNodeWithText("100%", substring = true).assertExists()
    }

    @Test
    fun progressBar_displaysCustomHeight() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTProgressBar(
                    progress = 0.5f,
                    height = 12,
                    showPercentage = true
                )
            }
        }

        // Verify progress bar exists by checking for percentage text
        composeTestRule.onNodeWithText("50%", substring = true).assertExists()
    }

    // VTCircularProgress Tests
    @Test
    fun circularProgress_displaysProgressText() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCircularProgress(
                    progress = 0.75f,
                    showPercentage = true
                )
            }
        }

        composeTestRule.onNodeWithText("75%", substring = true).assertExists()
    }

    @Test
    fun circularProgress_displaysZeroProgress() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCircularProgress(
                    progress = 0f,
                    showPercentage = true
                )
            }
        }

        composeTestRule.onNodeWithText("0%", substring = true).assertExists()
    }

    @Test
    fun circularProgress_displaysFullProgress() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCircularProgress(
                    progress = 1f,
                    showPercentage = true
                )
            }
        }

        composeTestRule.onNodeWithText("100%", substring = true).assertExists()
    }

    @Test
    fun circularProgress_hidesPercentage_whenShowPercentageIsFalse() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCircularProgress(
                    progress = 0.5f,
                    showPercentage = false
                )
            }
        }

        composeTestRule.onNodeWithText("50%", substring = true).assertDoesNotExist()
    }

    @Test
    fun circularProgress_displaysCustomSize() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCircularProgress(
                    progress = 0.5f,
                    size = 80,
                    showPercentage = true
                )
            }
        }

        // Verify circular progress exists by checking for percentage text
        composeTestRule.onNodeWithText("50%", substring = true).assertExists()
    }

    @Test
    fun circularProgress_handlesNegativeProgress() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCircularProgress(
                    progress = -0.5f,
                    showPercentage = true
                )
            }
        }

        composeTestRule.onNodeWithText("0%", substring = true).assertExists()
    }

    @Test
    fun circularProgress_handlesProgressGreaterThanOne() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCircularProgress(
                    progress = 1.5f,
                    showPercentage = true,
                    animated = false // Disable animation for test stability
                )
            }
        }

        // Progress should be clamped to 100%
        composeTestRule.onNodeWithText("100%", substring = true).assertExists()
    }

    // VTStepProgress Tests
    @Test
    fun stepProgress_displaysSteps() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStepProgress(
                    currentStep = 2,
                    totalSteps = 4
                )
            }
        }

        // Should display step numbers
        composeTestRule.onNodeWithText("1").assertExists()
        composeTestRule.onNodeWithText("2").assertExists()
        composeTestRule.onNodeWithText("3").assertExists()
        composeTestRule.onNodeWithText("4").assertExists()
    }

    @Test
    fun stepProgress_displaysStepLabels() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStepProgress(
                    currentStep = 2,
                    totalSteps = 4,
                    stepLabels = listOf("시작", "진행", "검토", "완료")
                )
            }
        }

        composeTestRule.onNodeWithText("시작").assertExists()
        composeTestRule.onNodeWithText("진행").assertExists()
        composeTestRule.onNodeWithText("검토").assertExists()
        composeTestRule.onNodeWithText("완료").assertExists()
    }

    @Test
    fun stepProgress_handlesSingleStep() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStepProgress(
                    currentStep = 1,
                    totalSteps = 1
                )
            }
        }

        composeTestRule.onNodeWithText("1").assertExists()
    }

    @Test
    fun stepProgress_handlesFirstStep() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStepProgress(
                    currentStep = 1,
                    totalSteps = 5
                )
            }
        }

        composeTestRule.onNodeWithText("1").assertExists()
    }

    @Test
    fun stepProgress_handlesLastStep() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStepProgress(
                    currentStep = 5,
                    totalSteps = 5
                )
            }
        }

        composeTestRule.onNodeWithText("5").assertExists()
    }

    @Test
    fun stepProgress_handlesZeroCurrentStep() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStepProgress(
                    currentStep = 0,
                    totalSteps = 3
                )
            }
        }

        composeTestRule.onNodeWithText("1").assertExists()
    }

    @Test
    fun stepProgress_handlesCurrentStepGreaterThanTotal() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStepProgress(
                    currentStep = 10,
                    totalSteps = 5
                )
            }
        }

        composeTestRule.onNodeWithText("5").assertExists()
    }

    @Test
    fun stepProgress_handlesEmptyStepLabels() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStepProgress(
                    currentStep = 2,
                    totalSteps = 4,
                    stepLabels = emptyList()
                )
            }
        }

        // Should still display step numbers
        composeTestRule.onNodeWithText("1").assertExists()
    }

    @Test
    fun stepProgress_handlesPartialStepLabels() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStepProgress(
                    currentStep = 2,
                    totalSteps = 4,
                    stepLabels = listOf("시작", "진행")
                )
            }
        }

        // Should only display labels for first two steps
        composeTestRule.onNodeWithText("1").assertExists()
        composeTestRule.onNodeWithText("2").assertExists()
    }

    @Test
    fun stepProgress_handlesLargeNumberOfSteps() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStepProgress(
                    currentStep = 5,
                    totalSteps = 10
                )
            }
        }

        composeTestRule.onNodeWithText("10").assertExists()
    }

    @Test
    fun progressBar_displaysCustomColors() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTProgressBar(
                    progress = 0.5f,
                    color = androidx.compose.ui.graphics.Color.Red,
                    backgroundColor = androidx.compose.ui.graphics.Color.Gray,
                    showPercentage = true
                )
            }
        }

        composeTestRule.onNodeWithText("50%", substring = true).assertExists()
    }

    @Test
    fun circularProgress_displaysCustomColors() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTCircularProgress(
                    progress = 0.5f,
                    color = androidx.compose.ui.graphics.Color.Green,
                    backgroundColor = androidx.compose.ui.graphics.Color.LightGray,
                    showPercentage = true
                )
            }
        }

        composeTestRule.onNodeWithText("50%", substring = true).assertExists()
    }

    @Test
    fun stepProgress_displaysCustomColors() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VTStepProgress(
                    currentStep = 2,
                    totalSteps = 4,
                    color = androidx.compose.ui.graphics.Color.Blue,
                    backgroundColor = androidx.compose.ui.graphics.Color.LightGray
                )
            }
        }

        composeTestRule.onNodeWithText("2").assertExists()
    }
}

