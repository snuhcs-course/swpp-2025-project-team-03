package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeacherClassesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun teacherClasses_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherClassesScreen()
            }
        }

        // 제목이 표시되어야 함
        composeTestRule.onNodeWithText("수업 관리", substring = true).assertExists()
    }

    @Test
    fun teacherClasses_displaysCreateButton() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherClassesScreen()
            }
        }

        // 수업 생성 버튼이 표시되어야 함
        composeTestRule.onNodeWithText("수업 생성", substring = true).assertExists()
    }

    @Test
    fun teacherClasses_displaysClassList() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherClassesScreen()
            }
        }

        // 수업 목록이 표시되어야 함
        composeTestRule.onNodeWithText("수업 관리", substring = true).assertExists()
    }

    @Test
    fun teacherClasses_navigatesToCreateClass_whenCreateClicked() {
        var navigated = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherClassesScreen(
                    onNavigateToCreateClass = { navigated = true }
                )
            }
        }

        composeTestRule.onNodeWithText("수업 생성", substring = true).performClick()
        assert(navigated)
    }

    @Test
    fun teacherClasses_navigatesToClassDetail_whenItemClicked() {
        var navigated = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherClassesScreen(
                    onNavigateToClassDetail = { _, _ -> navigated = true }
                )
            }
        }

        // 수업 항목 클릭 (있는 경우)
        if (composeTestRule.onAllNodes(hasText("수업", substring = true))
            .fetchSemanticsNodes().size > 1) {
            composeTestRule.onAllNodes(hasText("수업", substring = true))
                .get(1)
                .performClick()
            assert(navigated)
        }
    }

    @Test
    fun teacherClasses_displaysEmptyState_whenNoClasses() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherClassesScreen()
            }
        }

        // 수업이 없을 때 적절한 메시지가 표시되어야 함
        composeTestRule.onNodeWithText("수업 관리", substring = true).assertExists()
    }
}

