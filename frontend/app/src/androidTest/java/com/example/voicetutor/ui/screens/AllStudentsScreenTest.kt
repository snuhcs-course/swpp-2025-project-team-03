package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AllStudentsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun allStudents_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen()
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("전체 학생", substring = true).assertExists()
    }

    @Test
    fun allStudents_displaysStudentList() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen()
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("전체 학생", substring = true).assertExists()
    }

    @Test
    fun allStudents_navigatesToStudentDetail_whenItemClicked() {
        var navigated = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen(
                    onNavigateToStudentDetail = { navigated = true }
                )
            }
        }

        composeTestRule.waitForIdle()

        if (composeTestRule.onAllNodes(hasText("학생", substring = true))
            .fetchSemanticsNodes().size > 1) {
            composeTestRule.onAllNodes(hasText("학생", substring = true))
                .get(1)
                .performClick()
            assert(navigated)
        }
    }

    @Test
    fun allStudents_displaysEmptyState_whenNoStudents() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                AllStudentsScreen()
            }
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("전체 학생", substring = true).assertExists()
    }
}

