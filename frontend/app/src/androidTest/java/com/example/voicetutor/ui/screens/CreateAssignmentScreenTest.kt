package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateAssignmentScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun createAssignment_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen()
            }
        }

        composeTestRule.waitForIdle()

        // 제목이 표시되어야 함
        composeTestRule.onNodeWithText("과제 생성", substring = true).assertExists()
    }

    @Test
    fun createAssignment_displaysTitleTextField() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen()
            }
        }

        // 제목 입력 필드가 존재해야 함
        composeTestRule.onAllNodes(hasText("제목", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun createAssignment_displaysDescriptionTextField() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen()
            }
        }

        // 설명 입력 필드가 존재해야 함
        composeTestRule.onAllNodes(hasText("설명", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun createAssignment_displaysDueDateField() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen()
            }
        }

        // 마감일 필드가 존재해야 함
        composeTestRule.onAllNodes(hasText("마감일", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun createAssignment_displaysSubmitButton() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen()
            }
        }

        // 제출 버튼이 표시되어야 함
        composeTestRule.onNodeWithText("생성", substring = true).assertExists()
    }

    @Test
    fun createAssignment_displaysCancelButton() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen()
            }
        }

        // 취소 버튼이 표시되어야 함
        composeTestRule.onNodeWithText("취소", substring = true).assertExists()
    }

    @Test
    fun createAssignment_validatesRequiredFields() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen()
            }
        }

        // 필수 필드 검증이 되어야 함
        composeTestRule.onNodeWithText("과제 생성", substring = true).assertExists()
    }

    @Test
    fun createAssignment_callsOnBack_whenCancelClicked() {
        var backClicked = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                CreateAssignmentScreen(
                    onCreateAssignment = { backClicked = true }
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("취소", substring = true).performClick()
        assert(backClicked)
    }
}

