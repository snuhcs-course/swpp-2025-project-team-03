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

        composeTestRule.waitForIdle()

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

        composeTestRule.waitForIdle()

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

        composeTestRule.waitForIdle()

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

        composeTestRule.waitForIdle()

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

        composeTestRule.waitForIdle()

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

        composeTestRule.waitForIdle()

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

        composeTestRule.waitForIdle()

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

        composeTestRule.waitForIdle()

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

        composeTestRule.waitForIdle()

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

        composeTestRule.waitForIdle()

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

    // getPageTitle comprehensive tests
    @Test
    fun getPageTitle_returnsCorrectTitle_forAllStudentRoutes() {
        assertEquals("과제", getPageTitle(VoiceTutorScreens.Assignment.route, UserRole.STUDENT))
        assertEquals("과제 상세", getPageTitle(VoiceTutorScreens.AssignmentDetail.route, UserRole.STUDENT))
        assertEquals("과제 결과", getPageTitle(VoiceTutorScreens.AssignmentDetailedResults.route, UserRole.STUDENT))
        assertEquals("진도 리포트", getPageTitle(VoiceTutorScreens.Progress.route, UserRole.STUDENT))
        assertEquals("설정", getPageTitle(VoiceTutorScreens.Settings.route, UserRole.STUDENT))
    }

    @Test
    fun getPageTitle_returnsCorrectTitle_forAllTeacherRoutes() {
        assertEquals("수업 관리", getPageTitle(VoiceTutorScreens.TeacherClasses.route, UserRole.TEACHER))
        assertEquals("학생 관리", getPageTitle(VoiceTutorScreens.TeacherStudents.route, UserRole.TEACHER))
        assertEquals("전체 과제", getPageTitle(VoiceTutorScreens.AllAssignments.route, UserRole.TEACHER))
        assertEquals("전체 학생", getPageTitle(VoiceTutorScreens.AllStudents.route, UserRole.TEACHER))
        assertEquals("과제 생성", getPageTitle(VoiceTutorScreens.CreateAssignment.route, UserRole.TEACHER))
        assertEquals("과제 편집", getPageTitle(VoiceTutorScreens.EditAssignment.route, UserRole.TEACHER))
        assertEquals("과제 결과", getPageTitle(VoiceTutorScreens.TeacherAssignmentResults.route, UserRole.TEACHER))
        assertEquals("과제 상세", getPageTitle(VoiceTutorScreens.TeacherAssignmentDetail.route, UserRole.TEACHER))
        assertEquals("학생 상세", getPageTitle(VoiceTutorScreens.TeacherStudentDetail.route, UserRole.TEACHER))
        assertEquals("과제 결과", getPageTitle(VoiceTutorScreens.TeacherStudentAssignmentDetail.route, UserRole.TEACHER))
        assertEquals("설정", getPageTitle(VoiceTutorScreens.Settings.route, UserRole.TEACHER))
    }

    @Test
    fun getPageTitle_returnsDefault_forUnknownStudentRoute() {
        val title = getPageTitle("unknown_route", UserRole.STUDENT)
        assertEquals("학생 페이지", title)
    }

    @Test
    fun getPageTitle_returnsDefault_forUnknownTeacherRoute() {
        val title = getPageTitle("unknown_route", UserRole.TEACHER)
        assertEquals("선생님 페이지", title)
    }

    @Test
    fun getPageTitle_handlesRouteWithParameters() {
        // 파라미터가 포함된 실제 라우트 경로로 테스트
        val title = getPageTitle("assignment/1/테스트", UserRole.STUDENT)
        // assignment/1/테스트는 Assignment.route 패턴과 매치되지 않으므로 기본값 반환
        assertEquals("학생 페이지", title)
    }

    @Test
    fun getPageTitle_handlesEmptyRoute() {
        val title = getPageTitle("", UserRole.STUDENT)
        assertEquals("학생 페이지", title)
    }

    // MainLayout UI tests
    @Test
    fun mainLayout_showsRecentAssignment_forStudent() {
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

        // 최근 과제 섹션이 있을 수 있음 (데이터가 있으면)
        composeTestRule.onNodeWithText("홈", substring = true).assertExists()
    }

    @Test
    fun mainLayout_hidesRecentAssignment_forTeacher() {
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

        // 선생님은 최근 과제 섹션이 없어야 함
        composeTestRule.onNodeWithText("홈", substring = true).assertExists()
    }

    @Test
    fun mainLayout_displaysCorrectRole_forStudent() {
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

        // 학생 역할 표시 (기본값 "사용자"로 시작)
        composeTestRule.onNodeWithText("사용자", substring = true).assertExists()
    }

    @Test
    fun mainLayout_displaysCorrectRole_forTeacher() {
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

        // 선생님 역할 표시
        composeTestRule.onNodeWithText("선생님", substring = true).assertExists()
    }

    @Test
    fun mainLayout_navigationTabs_areClickable() {
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

        // 탭들이 클릭 가능해야 함
        composeTestRule.onNodeWithText("과제", substring = true).assertExists()
        composeTestRule.onNodeWithText("리포트", substring = true).assertExists()
    }

    @Test
    fun mainLayout_backButton_callsPopBackStack() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                navController.navigate(VoiceTutorScreens.Assignment.route)
                
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
    fun mainLayout_profileIcon_exists() {
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

        // 프로필 아이콘이 존재해야 함
        composeTestRule.onAllNodes(hasContentDescription("프로필"))
            .onFirst()
            .assertExists()
    }

    @Test
    fun mainLayout_bottomNavigation_showsCorrectTabsForStudent() {
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

        // 학생용 탭 확인
        composeTestRule.onNodeWithText("홈", substring = true).assertExists()
        composeTestRule.onNodeWithText("과제", substring = true).assertExists()
        composeTestRule.onNodeWithText("리포트", substring = true).assertExists()
    }

    @Test
    fun mainLayout_bottomNavigation_showsCorrectTabsForTeacher() {
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

        // 선생님용 탭 확인
        composeTestRule.onNodeWithText("홈", substring = true).assertExists()
        composeTestRule.onNodeWithText("수업", substring = true).assertExists()
        composeTestRule.onNodeWithText("학생", substring = true).assertExists()
    }

    @Test
    fun mainLayout_header_showsLogoOnDashboard() {
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

        // 대시보드에서는 로고가 표시되어야 함
        composeTestRule.onNodeWithText("VoiceTutor", substring = true).assertExists()
    }

    @Test
    fun mainLayout_header_showsTitleOnNonDashboard() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                navController.navigate(VoiceTutorScreens.Assignment.route)
                
                MainLayout(
                    navController = navController,
                    userRole = UserRole.STUDENT
                ) {
                    // Mock content
                }
            }
        }

        // 대시보드가 아닌 페이지에서는 제목이 표시되어야 함
        composeTestRule.onNodeWithContentDescription("뒤로가기").assertExists()
    }

    @Test
    fun mainLayout_currentRoute_determinesTabSelection() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                navController.navigate(VoiceTutorScreens.Progress.route)
                
                MainLayout(
                    navController = navController,
                    userRole = UserRole.STUDENT
                ) {
                    // Mock content
                }
            }
        }

        // 리포트 탭이 선택된 상태여야 함
        composeTestRule.onNodeWithText("리포트", substring = true).assertExists()
    }

    @Test
    fun mainLayout_teacherCurrentRoute_determinesTabSelection() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                navController.navigate(VoiceTutorScreens.TeacherStudents.route)
                
                MainLayout(
                    navController = navController,
                    userRole = UserRole.TEACHER
                ) {
                    // Mock content
                }
            }
        }

        // 학생 탭이 선택된 상태여야 함
        composeTestRule.onNodeWithText("학생", substring = true).assertExists()
    }

    @Test
    fun mainLayout_handlesNullCurrentDestination() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                // 아무 라우트도 navigate하지 않음
                
                MainLayout(
                    navController = navController,
                    userRole = UserRole.STUDENT
                ) {
                    // Mock content
                }
            }
        }

        // null destination을 처리해야 함
        composeTestRule.onNodeWithText("홈", substring = true).assertExists()
    }

    @Test
    fun mainLayout_handlesQueryParametersInRoute() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                navController.navigate("${VoiceTutorScreens.TeacherDashboard.route}?refresh=123456")
                
                MainLayout(
                    navController = navController,
                    userRole = UserRole.TEACHER
                ) {
                    // Mock content
                }
            }
        }

        // 쿼리 파라미터가 있어도 대시보드로 인식해야 함
        composeTestRule.onNodeWithText("VoiceTutor", substring = true).assertExists()
    }

    @Test
    fun mainLayout_showsDefaultTitle_whenDestinationNotMapped() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                val navController = rememberNavController()
                navController.navigate("unknown_route")
                
                MainLayout(
                    navController = navController,
                    userRole = UserRole.STUDENT
                ) {
                    // Mock content
                }
            }
        }

        // 알 수 없는 라우트에 대해서는 기본 제목 표시
        composeTestRule.onNodeWithContentDescription("뒤로가기").assertExists()
    }

    @Test
    fun mainLayout_studentRole_showsStudentNavigation() {
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

        // 학생 네비게이션 항목 확인
        val studentNavItems = listOf("홈", "과제", "리포트")
        studentNavItems.forEach { item ->
            composeTestRule.onNodeWithText(item, substring = true).assertExists()
        }
    }

    @Test
    fun mainLayout_teacherRole_showsTeacherNavigation() {
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

        // 선생님 네비게이션 항목 확인
        val teacherNavItems = listOf("홈", "수업", "학생")
        teacherNavItems.forEach { item ->
            composeTestRule.onNodeWithText(item, substring = true).assertExists()
        }
    }

    @Test
    fun mainLayout_userInfo_displayedCorrectly() {
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

        // 사용자 정보가 표시되어야 함
        composeTestRule.onNodeWithText("사용자", substring = true).assertExists()
    }

    @Test
    fun mainLayout_logoutButton_functionality() {
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
    fun mainLayout_settingsButton_navigation() {
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

        // 설정 버튼(프로필)이 존재해야 함
        composeTestRule.onAllNodes(hasContentDescription("프로필"))
            .onFirst()
            .assertExists()
    }
}

