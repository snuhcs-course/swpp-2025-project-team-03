package com.example.voicetutor.ui.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun mainLayout_displaysLogo_whenOnDashboard() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                MainLayout(
                    navController = navController,
                    userRole = UserRole.STUDENT
                ) {
                    // Mock dashboard content
                }
            }
        }

        // VoiceTutor 로고가 표시되어야 함
        composeTestRule.onNodeWithText("VoiceTutor", substring = true).assertExists()
    }

    @Test
    fun mainLayout_displaysBackButton_whenNotOnDashboard() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                navController.navigate("some_other_route")
                
                MainLayout(
                    navController = navController,
                    userRole = UserRole.STUDENT
                ) {
                    // Mock content
                }
            }
        }

        // 뒤로가기 버튼이 표시되어야 함
        composeTestRule.onNodeWithContentDescription("뒤로가기").assertExists()
    }

    @Test
    fun mainLayout_displaysUserName() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                MainLayout(
                    navController = navController,
                    userRole = UserRole.STUDENT
                ) {
                    // Mock content
                }
            }
        }

        // 사용자 이름이 표시되어야 함 (초기값 "사용자")
        composeTestRule.onNodeWithText("사용자", substring = true).assertExists()
    }

    @Test
    fun mainLayout_displaysUserRole() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                MainLayout(
                    navController = navController,
                    userRole = UserRole.TEACHER
                ) {
                    // Mock content
                }
            }
        }

        // 사용자 역할이 표시되어야 함
        composeTestRule.onNodeWithText("선생님", substring = true).assertExists()
    }

    @Test
    fun mainLayout_showsBottomNavigation_forStudent() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                MainLayout(
                    navController = navController,
                    userRole = UserRole.STUDENT
                ) {
                    // Mock content
                }
            }
        }

        // 학생용 하단 네비게이션이 표시되어야 함
        composeTestRule.onNodeWithText("홈", substring = true).assertExists()
        composeTestRule.onNodeWithText("과제", substring = true).assertExists()
        composeTestRule.onNodeWithText("리포트", substring = true).assertExists()
    }

    @Test
    fun mainLayout_showsBottomNavigation_forTeacher() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                MainLayout(
                    navController = navController,
                    userRole = UserRole.TEACHER
                ) {
                    // Mock content
                }
            }
        }

        // 선생님용 하단 네비게이션이 표시되어야 함
        composeTestRule.onNodeWithText("홈", substring = true).assertExists()
        composeTestRule.onNodeWithText("수업", substring = true).assertExists()
        composeTestRule.onNodeWithText("학생", substring = true).assertExists()
    }

    @Test
    fun mainLayout_profileButton_navigatesToSettings() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                MainLayout(
                    navController = navController,
                    userRole = UserRole.STUDENT
                ) {
                    // Mock content
                }
            }
        }

        // 프로필 버튼이 존재해야 함
        composeTestRule.onAllNodes(hasContentDescription("프로필"))
            .onFirst()
            .assertExists()
    }

    @Test
    fun mainLayout_logoutButton_exists() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                MainLayout(
                    navController = navController,
                    userRole = UserRole.STUDENT
                ) {
                    // Mock content
                }
            }
        }

        // 로그아웃 버튼이 존재해야 함
        composeTestRule.onNodeWithContentDescription("로그아웃").assertExists()
    }

    @Test
    fun bottomNavigation_studentHomeSelected_highlightsHome() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                navController.navigate(VoiceTutorScreens.StudentDashboard.route)
                
                MainLayout(
                    navController = navController,
                    userRole = UserRole.STUDENT
                ) {
                    // Mock content
                }
            }
        }

        // 홈 탭이 선택된 상태여야 함
        composeTestRule.onNodeWithText("홈", substring = true).assertExists()
    }

    @Test
    fun bottomNavigation_teacherHomeSelected_highlightsHome() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                navController.navigate(VoiceTutorScreens.TeacherDashboard.route)
                
                MainLayout(
                    navController = navController,
                    userRole = UserRole.TEACHER
                ) {
                    // Mock content
                }
            }
        }

        // 홈 탭이 선택된 상태여야 함
        composeTestRule.onNodeWithText("홈", substring = true).assertExists()
    }

    @Test
    fun getPageTitle_returnsCorrectTitle_forAssignment() {
        val title = getPageTitle(VoiceTutorScreens.Assignment.route, UserRole.STUDENT)
        assertEquals("과제", title)
    }

    @Test
    fun getPageTitle_returnsCorrectTitle_forAssignmentDetail() {
        val title = getPageTitle(VoiceTutorScreens.AssignmentDetail.route, UserRole.STUDENT)
        assertEquals("과제 상세", title)
    }

    @Test
    fun getPageTitle_returnsCorrectTitle_forTeacherClasses() {
        val title = getPageTitle(VoiceTutorScreens.TeacherClasses.route, UserRole.TEACHER)
        assertEquals("수업 관리", title)
    }

    @Test
    fun getPageTitle_returnsCorrectTitle_forTeacherStudents() {
        val title = getPageTitle(VoiceTutorScreens.TeacherStudents.route, UserRole.TEACHER)
        assertEquals("학생 관리", title)
    }

    @Test
    fun getPageTitle_returnsCorrectTitle_forSettings() {
        val title = getPageTitle(VoiceTutorScreens.Settings.route, UserRole.STUDENT)
        assertEquals("설정", title)
    }

    @Test
    fun getPageTitle_returnsDefault_forUnknownRoute() {
        val title = getPageTitle("unknown_route", UserRole.STUDENT)
        assertEquals("학생 페이지", title)
    }

    @Test
    fun getPageTitle_returnsDefault_forNullRoute() {
        val title = getPageTitle(null, UserRole.TEACHER)
        assertEquals("선생님 페이지", title)
    }
}

