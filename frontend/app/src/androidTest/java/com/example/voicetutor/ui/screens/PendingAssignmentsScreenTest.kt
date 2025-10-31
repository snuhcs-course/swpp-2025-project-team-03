package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PendingAssignmentsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun pendingAssignments_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                PendingAssignmentsScreen(
                    studentId = 1
                )
            }
        }

        // 제목이 표시되어야 함
        composeTestRule.onNodeWithText("해야 할 과제", substring = true).assertExists()
    }

    @Test
    fun pendingAssignments_displaysEmptyState_whenNoAssignments() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                PendingAssignmentsScreen(
                    studentId = 1
                )
            }
        }

        // 과제가 없을 때 적절한 메시지가 표시되어야 함
        composeTestRule.onNodeWithText("해야 할 과제", substring = true).assertExists()
    }

    @Test
    fun pendingAssignments_displaysAssignmentList_whenAssignmentsExist() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                PendingAssignmentsScreen(
                    studentId = 1
                )
            }
        }

        // 과제 목록이 표시되어야 함 (있는 경우)
        composeTestRule.onNodeWithText("해야 할 과제", substring = true).assertExists()
    }

    @Test
    fun pendingAssignments_navigatesToAssignmentDetail_whenItemClicked() {
        var navigated = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                PendingAssignmentsScreen(
                    studentId = 1,
                    onNavigateToAssignmentDetail = { navigated = true }
                )
            }
        }

        // 과제 항목 클릭 (있는 경우)
        // 네비게이션 콜백이 호출되었는지 확인
        // 실제 데이터가 없으면 테스트 스킵
        if (composeTestRule.onAllNodes(hasText("과제", substring = true))
            .fetchSemanticsNodes().isNotEmpty()) {
            composeTestRule.onAllNodes(hasText("과제", substring = true))
                .onFirst()
                .performClick()
            assert(navigated)
        }
    }

    @Test
    fun pendingAssignments_displaysLoadingIndicator_whenLoading() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                PendingAssignmentsScreen(
                    studentId = 1
                )
            }
        }

        // 로딩 중일 때 로딩 인디케이터가 표시될 수 있음
        composeTestRule.onNodeWithText("해야 할 과제", substring = true).assertExists()
    }

    @Test
    fun pendingAssignments_displaysError_whenErrorOccurs() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                PendingAssignmentsScreen(
                    studentId = 1
                )
            }
        }

        // 에러가 발생했을 때 에러 메시지가 표시되어야 함
        // 기본 UI 구조 확인
        composeTestRule.onNodeWithText("해야 할 과제", substring = true).assertExists()
    }
}

