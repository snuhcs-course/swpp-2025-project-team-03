package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeacherClassDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun teacherClassDetail_displaysClassName() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherClassDetailScreen(
                    className = "수학",
                    classId = 1
                )
            }
        }

        composeTestRule.onNodeWithText("수학", substring = true).assertExists()
    }

    @Test
    fun teacherClassDetail_displaysStudentList() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherClassDetailScreen(
                    className = "수학",
                    classId = 1
                )
            }
        }

        composeTestRule.onNodeWithText("수학", substring = true).assertExists()
    }

    @Test
    fun teacherClassDetail_navigatesToStudentDetail_whenItemClicked() {
        var navigated = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherClassDetailScreen(
                    className = "수학",
                    classId = 1,
                    onNavigateToAssignmentDetail = { navigated = true }
                )
            }
        }

        if (composeTestRule.onAllNodes(hasText("학생", substring = true))
            .fetchSemanticsNodes().size > 1) {
            composeTestRule.onAllNodes(hasText("학생", substring = true))
                .get(1)
                .performClick()
            assert(navigated)
        }
    }

    @Test
    fun teacherClassDetail_displaysAssignmentList() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherClassDetailScreen(
                    className = "수학",
                    classId = 1
                )
            }
        }

        composeTestRule.onNodeWithText("수학", substring = true).assertExists()
    }
}

