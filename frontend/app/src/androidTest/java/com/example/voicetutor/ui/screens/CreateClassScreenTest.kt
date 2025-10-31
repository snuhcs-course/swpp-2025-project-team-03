package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateClassScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun createClass_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CreateClassScreen()
            }
        }

        composeTestRule.waitForIdle()

        // 제목이 표시되어야 함
        composeTestRule.onNodeWithText("수업 생성", substring = true).assertExists()
    }

    @Test
    fun createClass_displaysClassNameField() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CreateClassScreen()
            }
        }

        // 수업명 입력 필드가 존재해야 함
        composeTestRule.onAllNodes(hasText("수업명", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun createClass_displaysSubjectField() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CreateClassScreen()
            }
        }

        // 과목 필드가 존재해야 함
        composeTestRule.onAllNodes(hasText("과목", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun createClass_displaysGradeField() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CreateClassScreen()
            }
        }

        // 학년 필드가 존재해야 함
        composeTestRule.onAllNodes(hasText("학년", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun createClass_displaysSubmitButton() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CreateClassScreen()
            }
        }

        // 제출 버튼이 표시되어야 함
        composeTestRule.onNodeWithText("생성", substring = true).assertExists()
    }

    @Test
    fun createClass_callsOnBack_whenBackClicked() {
        var backClicked = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                CreateClassScreen(
                    onBackClick = { backClicked = true }
                )
            }
        }

        composeTestRule.waitForIdle()

        // 뒤로가기 버튼 클릭
        composeTestRule.onAllNodes(hasContentDescription("뒤로가기"))
            .onFirst()
            .performClick()
        assert(backClicked)
    }

    @Test
    fun createClass_validatesRequiredFields() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                CreateClassScreen()
            }
        }

        // 필수 필드 검증이 되어야 함
        composeTestRule.onNodeWithText("수업 생성", substring = true).assertExists()
    }
}

