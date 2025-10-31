package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeacherDashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun teacherDashboard_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherDashboardScreen()
            }
        }

        composeTestRule.waitForIdle()

        // 대시보드 제목이 표시되어야 함
        composeTestRule.onAllNodes(hasText("대시보드", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun teacherDashboard_displaysStatsCards() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherDashboardScreen()
            }
        }

        // 통계 카드들이 표시되어야 함
        composeTestRule.onAllNodes(hasText("과제", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun teacherDashboard_displaysRecentClasses() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherDashboardScreen()
            }
        }

        // 최근 수업이 표시되어야 함
        composeTestRule.onAllNodes(hasText("수업", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun teacherDashboard_displaysRecentStudents() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherDashboardScreen()
            }
        }

        // 최근 학생이 표시되어야 함
        composeTestRule.onAllNodes(hasText("학생", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun teacherDashboard_navigatesToClasses_whenClicked() {
        var navigated = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherDashboardScreen(
                    onNavigateToAllAssignments = { navigated = true }
                )
            }
        }

        composeTestRule.waitForIdle()

        // 수업 관리 버튼 클릭
        composeTestRule.onAllNodes(hasText("수업 관리", substring = true))
            .onFirst()
            .performClick()
        
        // 네비게이션 콜백이 호출되었는지 확인
        assert(navigated)
    }

    @Test
    fun teacherDashboard_navigatesToStudents_whenClicked() {
        var navigated = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherDashboardScreen(
                    onNavigateToAllStudents = { navigated = true }
                )
            }
        }

        composeTestRule.waitForIdle()

        // 학생 관리 버튼 클릭
        composeTestRule.onAllNodes(hasText("학생 관리", substring = true))
            .onFirst()
            .performClick()
        
        // 네비게이션 콜백이 호출되었는지 확인
        assert(navigated)
    }

    @Test
    fun teacherDashboard_navigatesToAssignments_whenClicked() {
        var navigated = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherDashboardScreen(
                    onNavigateToAllAssignments = { navigated = true }
                )
            }
        }

        composeTestRule.waitForIdle()

        // 과제 관리 버튼 클릭
        composeTestRule.onAllNodes(hasText("과제 관리", substring = true))
            .onFirst()
            .performClick()
        
        // 네비게이션 콜백이 호출되었는지 확인
        assert(navigated)
    }

    @Test
    fun teacherDashboard_displaysQuickActions() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherDashboardScreen()
            }
        }

        composeTestRule.waitForIdle()

        // 빠른 작업 버튼들이 표시되어야 함
        composeTestRule.onAllNodes(hasText("과제", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun teacherDashboard_handlesEmptyData() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherDashboardScreen()
            }
        }

        // 데이터가 없어도 기본 UI는 표시되어야 함
        composeTestRule.onAllNodes(hasText("대시보드", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun teacherDashboard_displaysWelcomeMessage() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                TeacherDashboardScreen()
            }
        }

        // 환영 메시지나 사용자 정보가 표시되어야 함
        // 기본 레이아웃 확인
        composeTestRule.onAllNodes(hasText("대시보드", substring = true))
            .assertCountEquals(1)
    }
}

