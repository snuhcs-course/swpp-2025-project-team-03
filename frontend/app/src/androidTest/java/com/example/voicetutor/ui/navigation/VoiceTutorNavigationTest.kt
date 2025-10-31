package com.example.voicetutor.ui.navigation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.ui.theme.VoiceTutorTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoiceTutorNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun voiceTutorScreens_login_hasCorrectRoute() {
        assertEquals("login", VoiceTutorScreens.Login.route)
    }

    @Test
    fun voiceTutorScreens_signup_hasCorrectRoute() {
        assertEquals("signup", VoiceTutorScreens.Signup.route)
    }

    @Test
    fun voiceTutorScreens_studentDashboard_hasCorrectRoute() {
        assertEquals("student_dashboard", VoiceTutorScreens.StudentDashboard.route)
    }

    @Test
    fun voiceTutorScreens_teacherDashboard_hasCorrectRoute() {
        assertEquals("teacher_dashboard", VoiceTutorScreens.TeacherDashboard.route)
    }

    @Test
    fun voiceTutorScreens_assignment_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.Assignment.createRoute("1", "테스트 과제")
        assertEquals("assignment/1/테스트 과제", route)
    }

    @Test
    fun voiceTutorScreens_assignmentDetail_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.AssignmentDetail.createRoute("1", "테스트 과제")
        assertEquals("assignment_detail/1/테스트 과제", route)
    }

    @Test
    fun voiceTutorScreens_settings_createRoute_withStudentId() {
        val route = VoiceTutorScreens.Settings.createRoute(1)
        assertEquals("settings/1", route)
    }

    @Test
    fun voiceTutorScreens_settings_createRoute_withoutStudentId() {
        val route = VoiceTutorScreens.Settings.createRoute()
        assertEquals("settings/-1", route)
    }

    @Test
    fun voiceTutorScreens_pendingAssignments_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.PendingAssignments.createRoute(1)
        assertEquals("pending_assignments/1", route)
    }

    @Test
    fun voiceTutorScreens_completedAssignments_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.CompletedAssignments.createRoute(1)
        assertEquals("completed_assignments/1", route)
    }

    @Test
    fun voiceTutorScreens_allStudentAssignments_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.AllStudentAssignments.createRoute(1)
        assertEquals("all_student_assignments/1", route)
    }

    @Test
    fun voiceTutorScreens_teacherStudents_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.TeacherStudents.createRoute("1")
        assertEquals("teacher_students/1", route)
    }

    @Test
    fun voiceTutorScreens_editAssignment_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.EditAssignment.createRoute("테스트 과제")
        assertEquals("edit_assignment/테스트 과제", route)
    }

    @Test
    fun voiceTutorScreens_teacherAssignmentResults_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.TeacherAssignmentResults.createRoute("테스트 과제")
        assertEquals("teacher_assignment_results/테스트 과제", route)
    }

    @Test
    fun voiceTutorScreens_teacherAssignmentDetail_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.TeacherAssignmentDetail.createRoute("테스트 과제")
        assertEquals("teacher_assignment_detail/테스트 과제", route)
    }

    @Test
    fun voiceTutorScreens_teacherStudentDetail_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.TeacherStudentDetail.createRoute("홍길동")
        assertEquals("teacher_student_detail/홍길동", route)
    }

    @Test
    fun voiceTutorScreens_teacherStudentAssignmentDetail_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.TeacherStudentAssignmentDetail.createRoute("1", "테스트 과제")
        assertEquals("teacher_student_assignment_detail/1/테스트 과제", route)
    }

    @Test
    fun voiceTutorScreens_teacherStudentAssignmentDetail_createRoute_handlesSpecialCharacters() {
        val route = VoiceTutorScreens.TeacherStudentAssignmentDetail.createRoute("1", "테스트/과제")
        assertEquals("teacher_student_assignment_detail/1/테스트_과제", route)
    }

    @Test
    fun voiceTutorScreens_teacherMessage_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.TeacherMessage.createRoute("홍길동")
        assertEquals("teacher_message/홍길동", route)
    }

    @Test
    fun voiceTutorScreens_teacherClassDetail_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.TeacherClassDetail.createRoute("수학", 1)
        assertEquals("teacher_class_detail/수학/1", route)
    }

    @Test
    fun voiceTutorScreens_classMessage_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.ClassMessage.createRoute("수학")
        assertEquals("class_message/수학", route)
    }

    @Test
    fun voiceTutorScreens_attendanceManagement_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.AttendanceManagement.createRoute(1)
        assertEquals("attendance_management/1", route)
    }

    @Test
    fun voiceTutorScreens_assignmentDetailedResults_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.AssignmentDetailedResults.createRoute("테스트 과제")
        assertEquals("assignment_detailed_results/테스트 과제", route)
    }

    @Test
    fun voiceTutorNavigation_startsAtLogin() {
        composeTestRule.setContent {
            VoiceTutorTheme {
                VoiceTutorNavigation(
                    startDestination = VoiceTutorScreens.Login.route
                )
            }
        }

        // 로그인 화면이 표시되어야 함
        composeTestRule.onNodeWithText("로그인", substring = true).assertExists()
    }

    @Test
    fun voiceTutorNavigation_allRoutes_haveUniqueRoutes() {
        val routes = listOf(
            VoiceTutorScreens.Login.route,
            VoiceTutorScreens.Signup.route,
            VoiceTutorScreens.StudentDashboard.route,
            VoiceTutorScreens.TeacherDashboard.route,
            VoiceTutorScreens.Progress.route,
            VoiceTutorScreens.TeacherClasses.route,
            VoiceTutorScreens.AllAssignments.route,
            VoiceTutorScreens.AllStudents.route,
            VoiceTutorScreens.CreateAssignment.route,
            VoiceTutorScreens.CreateClass.route,
            VoiceTutorScreens.AppInfo.route
        )

        // 모든 기본 라우트가 고유한지 확인
        assertEquals(routes.size, routes.distinct().size)
    }
}

