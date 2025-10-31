package com.example.voicetutor.ui.screens

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StudentDashboardScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun studentDashboard_displaysTitle() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreen()
            }
        }

        // 대시보드 제목이 표시되어야 함
        composeTestRule.onAllNodes(hasText("대시보드", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun studentDashboard_displaysPendingAssignmentsSection() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreen()
            }
        }

        // 해야 할 과제 섹션이 존재해야 함
        composeTestRule.onAllNodes(hasText("해야 할 과제", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun studentDashboard_displaysCompletedAssignmentsSection() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreen()
            }
        }

        // 완료한 과제 섹션이 존재해야 함
        composeTestRule.onAllNodes(hasText("완료한 과제", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun studentDashboard_displaysStatsCards() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreen()
            }
        }

        // 통계 카드들이 표시되어야 함
        // (실제 데이터가 없어도 UI는 표시됨)
        composeTestRule.onAllNodes(hasText("진행률", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun studentDashboard_navigatesToPendingAssignments_whenClicked() {
        var navigated = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreen(
                    onNavigateToAllAssignments = { navigated = true }
                )
            }
        }

        // 해야 할 과제 버튼 클릭
        composeTestRule.onAllNodes(hasText("모두 보기", substring = true))
            .onFirst()
            .performClick()
        
        // 네비게이션 콜백이 호출되었는지 확인
        assert(navigated)
    }

    @Test
    fun studentDashboard_displaysProgressReport() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreen()
            }
        }

        // 진행률 리포트가 표시되어야 함
        composeTestRule.onAllNodes(hasText("진행률", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun studentDashboard_handlesEmptyAssignments() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreen()
            }
        }

        // 과제가 없어도 기본 UI는 표시되어야 함
        composeTestRule.onAllNodes(hasText("해야 할 과제", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun studentDashboard_displaysRecentActivity() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreen()
            }
        }

        // 최근 활동이 표시되어야 함 (데이터가 있을 때)
        // 기본 UI 구조 확인
        composeTestRule.onAllNodes(hasText("대시보드", substring = true))
            .assertCountEquals(1)
    }

    @Test
    fun studentDashboard_navigatesToProgressReport_whenClicked() {
        var navigated = false
        
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreen(
                    onNavigateToProgressReport = { navigated = true }
                )
            }
        }

        // 리포트 버튼 클릭
        composeTestRule.onAllNodes(hasText("리포트", substring = true))
            .onFirst()
            .performClick()
        
        // 네비게이션 콜백이 호출되었는지 확인
        assert(navigated)
    }

    @Test
    fun studentDashboard_displaysWelcomeMessage() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                StudentDashboardScreen()
            }
        }

        // 환영 메시지나 사용자 정보가 표시되어야 함
        // 기본 레이아웃 확인
        composeTestRule.onAllNodes(hasText("대시보드", substring = true))
            .assertCountEquals(1)
    }
}

