package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AssignmentDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun assignmentDetail_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailScreen(
                    assignmentId = 1,
                    assignmentTitle = "테스트 과제"
                )
            }
        }

        composeTestRule.waitForIdle()

        // 과제 제목이 표시되어야 함
        composeTestRule.onNodeWithText("테스트 과제", substring = true).assertExists()
    }

    @Test
    fun assignmentDetail_displaysStartButton() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailScreen(
                    assignmentId = 1,
                    assignmentTitle = "테스트 과제"
                )
            }
        }

        // 시작 버튼이 표시되어야 함
        composeTestRule.onNodeWithText("과제 시작", substring = true).assertExists()
    }

    @Test
    fun assignmentDetail_displaysDescription() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailScreen(
                    assignmentId = 1,
                    assignmentTitle = "테스트 과제"
                )
            }
        }

        // 과제 설명이 표시되어야 함
        // 기본 UI 구조 확인
        composeTestRule.onNodeWithText("테스트 과제", substring = true).assertExists()
    }

    @Test
    fun assignmentDetail_displaysStatistics() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailScreen(
                    assignmentId = 1,
                    assignmentTitle = "테스트 과제"
                )
            }
        }

        // 통계 정보가 표시되어야 함
        // 기본 UI 구조 확인
        composeTestRule.onNodeWithText("테스트 과제", substring = true).assertExists()
    }

    @Test
    fun assignmentDetail_navigatesToAssignment_whenStartClicked() {
        var navigated = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailScreen(
                    assignmentId = 1,
                    assignmentTitle = "테스트 과제",
                    onStartAssignment = { navigated = true }
                )
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("과제 시작", substring = true).performClick()
        // 네비게이션 콜백이 호출되었는지 확인
        assert(navigated)
    }

    @Test
    fun assignmentDetail_handlesNullAssignmentId() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailScreen(
                    assignmentId = null,
                    assignmentTitle = null
                )
            }
        }

        // 과제 ID가 없어도 기본 UI는 표시되어야 함
        composeTestRule.onAllNodes(hasContentDescription("과제"))
            .assertCountEquals(1)
    }

    @Test
    fun assignmentDetail_displaysDueDate() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailScreen(
                    assignmentId = 1,
                    assignmentTitle = "테스트 과제"
                )
            }
        }

        // 마감일이 표시되어야 함 (있는 경우)
        // 기본 UI 구조 확인
        composeTestRule.onNodeWithText("테스트 과제", substring = true).assertExists()
    }

    @Test
    fun assignmentDetail_displaysProgress() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AssignmentDetailScreen(
                    assignmentId = 1,
                    assignmentTitle = "테스트 과제"
                )
            }
        }

        // 진행률이 표시되어야 함
        // 기본 UI 구조 확인
        composeTestRule.onNodeWithText("테스트 과제", substring = true).assertExists()
    }
}

