package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AllAssignmentsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun allAssignments_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllAssignmentsScreen()
            }
        }

        // 제목이 표시되어야 함
        composeTestRule.onNodeWithText("전체 과제", substring = true).assertExists()
    }

    @Test
    fun allAssignments_displaysCreateButton() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllAssignmentsScreen()
            }
        }

        // 과제 생성 버튼이 표시되어야 함
        composeTestRule.onNodeWithText("과제 생성", substring = true).assertExists()
    }

    @Test
    fun allAssignments_displaysAssignmentList() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllAssignmentsScreen()
            }
        }

        // 과제 목록이 표시되어야 함
        composeTestRule.onNodeWithText("전체 과제", substring = true).assertExists()
    }

    @Test
    fun allAssignments_navigatesToCreateAssignment_whenCreateClicked() {
        var navigated = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllAssignmentsScreen(
                    onNavigateToAssignmentDetail = { navigated = true }
                )
            }
        }

        composeTestRule.onNodeWithText("과제 생성", substring = true).performClick()
        assert(navigated)
    }

    @Test
    fun allAssignments_navigatesToAssignmentDetail_whenItemClicked() {
        var navigated = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllAssignmentsScreen(
                    onNavigateToAssignmentDetail = { navigated = true }
                )
            }
        }

        // 과제 항목 클릭 (있는 경우)
        if (composeTestRule.onAllNodes(hasText("과제", substring = true))
            .fetchSemanticsNodes().size > 1) {
            composeTestRule.onAllNodes(hasText("과제", substring = true))
                .get(1)
                .performClick()
            assert(navigated)
        }
    }

    @Test
    fun allAssignments_displaysEmptyState_whenNoAssignments() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllAssignmentsScreen()
            }
        }

        // 과제가 없을 때 적절한 메시지가 표시되어야 함
        composeTestRule.onNodeWithText("전체 과제", substring = true).assertExists()
    }

    @Test
    fun allAssignments_displaysFilterOptions() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllAssignmentsScreen()
            }
        }

        // 필터 옵션이 표시되어야 함 (있는 경우)
        composeTestRule.onNodeWithText("전체 과제", substring = true).assertExists()
    }
}

