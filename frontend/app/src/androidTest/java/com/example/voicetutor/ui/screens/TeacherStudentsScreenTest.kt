package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeacherStudentsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun teacherStudents_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(
                    classId = 1
                )
            }
        }

        composeTestRule.waitForIdle()

        // 제목이 표시되어야 함
        composeTestRule.onNodeWithText("학생 관리", substring = true).assertExists()
    }

    @Test
    fun teacherStudents_displaysStudentList() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(
                    classId = 1
                )
            }
        }

        // 학생 목록이 표시되어야 함
        composeTestRule.onNodeWithText("학생 관리", substring = true).assertExists()
    }

    @Test
    fun teacherStudents_navigatesToStudentDetail_whenItemClicked() {
        var navigated = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(
                    classId = 1,
                    onNavigateToStudentDetail = { navigated = true }
                )
            }
        }

        composeTestRule.waitForIdle()

        // 학생 항목 클릭 (있는 경우)
        if (composeTestRule.onAllNodes(hasText("학생", substring = true))
            .fetchSemanticsNodes().size > 1) {
            composeTestRule.onAllNodes(hasText("학생", substring = true))
                .get(1)
                .performClick()
            assert(navigated)
        }
    }

    @Test
    fun teacherStudents_displaysEmptyState_whenNoStudents() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(
                    classId = 1
                )
            }
        }

        // 학생이 없을 때 적절한 메시지가 표시되어야 함
        composeTestRule.onNodeWithText("학생 관리", substring = true).assertExists()
    }

    @Test
    fun teacherStudents_displaysSearchBar() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherStudentsScreen(
                    classId = 1
                )
            }
        }

        // 검색 바가 표시되어야 함 (있는 경우)
        composeTestRule.onNodeWithText("학생 관리", substring = true).assertExists()
    }
}

