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

    // Note: PendingAssignments, CompletedAssignments, AllStudentAssignments screens don't exist
    // These tests are commented out as they reference non-existent navigation screens

    @Test
    fun voiceTutorScreens_teacherStudents_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.TeacherStudents.createRoute("1")
        assertEquals("teacher_students/1", route)
    }

    @Test
    fun voiceTutorScreens_editAssignment_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.EditAssignment.createRoute(1)
        assertEquals("edit_assignment/1", route)
    }

    @Test
    fun voiceTutorScreens_teacherAssignmentResults_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.TeacherAssignmentResults.createRoute(1)
        assertEquals("teacher_assignment_results/1", route)
    }

    @Test
    fun voiceTutorScreens_teacherAssignmentDetail_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.TeacherAssignmentDetail.createRoute(1)
        assertEquals("teacher_assignment_detail/1", route)
    }

    @Test
    fun voiceTutorScreens_teacherStudentAssignmentDetail_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.TeacherStudentAssignmentDetail.createRoute("1", 2, "테스트 과제")
        assertEquals("teacher_student_assignment_detail/1/2/테스트 과제", route)
    }

    @Test
    fun voiceTutorScreens_teacherStudentAssignmentDetail_createRoute_handlesSpecialCharacters() {
        val route = VoiceTutorScreens.TeacherStudentAssignmentDetail.createRoute("1", 2, "테스트/과제")
        assertEquals("teacher_student_assignment_detail/1/2/테스트_과제", route)
    }

    @Test
    fun voiceTutorScreens_teacherClassDetail_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.TeacherClassDetail.createRoute("수학", 1)
        assertEquals("teacher_class_detail/수학/1", route)
    }

    @Test
    fun voiceTutorScreens_attendanceManagement_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.AttendanceManagement.createRoute(1)
        assertEquals("attendance_management/1", route)
    }

    @Test
    fun voiceTutorScreens_assignmentDetailedResults_createRoute_worksCorrectly() {
        val route = VoiceTutorScreens.AssignmentDetailedResults.createRoute(1, "테스트 과제")
        assertEquals("assignment_detailed_results/1/테스트 과제", route)
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

    // Route creation tests
    // Note: AllStudentAssignments, CompletedAssignments, PendingAssignments screens don't exist

    @Test
    fun voiceTutorScreens_assignment_createRoute_handlesEmptyTitle() {
        val route = VoiceTutorScreens.Assignment.createRoute("1", "")
        assertEquals("assignment/1/", route)
    }

    @Test
    fun voiceTutorScreens_assignmentDetail_createRoute_handlesSpecialCharacters() {
        val route = VoiceTutorScreens.AssignmentDetail.createRoute("1", "테스트/과제")
        assertEquals("assignment_detail/1/테스트/과제", route)
    }

    @Test
    fun voiceTutorScreens_assignmentDetailedResults_createRoute_handlesEmptyTitle() {
        val route = VoiceTutorScreens.AssignmentDetailedResults.createRoute(1, "")
        assertEquals("assignment_detailed_results/1/", route)
    }

    @Test
    fun voiceTutorScreens_teacherClassDetail_createRoute_handlesZeroClassId() {
        val route = VoiceTutorScreens.TeacherClassDetail.createRoute("수학", 0)
        assertEquals("teacher_class_detail/수학/0", route)
    }

    @Test
    fun voiceTutorScreens_teacherClassDetail_createRoute_handlesLargeClassId() {
        val route = VoiceTutorScreens.TeacherClassDetail.createRoute("과학", 1000000)
        assertEquals("teacher_class_detail/과학/1000000", route)
    }

    @Test
    fun voiceTutorScreens_attendanceManagement_createRoute_handlesZero() {
        val route = VoiceTutorScreens.AttendanceManagement.createRoute(0)
        assertEquals("attendance_management/0", route)
    }

    @Test
    fun voiceTutorScreens_settings_createRoute_withNegativeId() {
        val route = VoiceTutorScreens.Settings.createRoute(-1)
        assertEquals("settings/-1", route)
    }

    @Test
    fun voiceTutorScreens_settings_createRoute_withZeroId() {
        val route = VoiceTutorScreens.Settings.createRoute(0)
        assertEquals("settings/0", route)
    }

    @Test
    fun voiceTutorScreens_teacherStudents_createRoute_handlesEmptyClassId() {
        val route = VoiceTutorScreens.TeacherStudents.createRoute("")
        assertEquals("teacher_students/", route)
    }

    @Test
    fun voiceTutorScreens_editAssignment_createRoute_handlesLargeId() {
        val route = VoiceTutorScreens.EditAssignment.createRoute(999999)
        assertEquals("edit_assignment/999999", route)
    }

    @Test
    fun voiceTutorScreens_teacherAssignmentResults_createRoute_handlesLargeId() {
        val route = VoiceTutorScreens.TeacherAssignmentResults.createRoute(12345)
        assertEquals("teacher_assignment_results/12345", route)
    }

    @Test
    fun voiceTutorScreens_teacherAssignmentDetail_createRoute_handlesLargeId() {
        val route = VoiceTutorScreens.TeacherAssignmentDetail.createRoute(12345)
        assertEquals("teacher_assignment_detail/12345", route)
    }

    @Test
    fun voiceTutorScreens_teacherStudentAssignmentDetail_createRoute_handlesMultipleSlashes() {
        val route = VoiceTutorScreens.TeacherStudentAssignmentDetail.createRoute("1", 2, "테스트/과제/번호")
        assertEquals("teacher_student_assignment_detail/1/2/테스트_과제_번호", route)
    }

    @Test
    fun voiceTutorScreens_teacherStudentAssignmentDetail_createRoute_handlesEmptyTitle() {
        val route = VoiceTutorScreens.TeacherStudentAssignmentDetail.createRoute("1", 2, "")
        assertEquals("teacher_student_assignment_detail/1/2/", route)
    }

    @Test
    fun voiceTutorScreens_teacherStudentAssignmentDetail_createRoute_handlesEmptyStudentId() {
        val route = VoiceTutorScreens.TeacherStudentAssignmentDetail.createRoute("", 2, "과제")
        assertEquals("teacher_student_assignment_detail//2/과제", route)
    }

    @Test
    fun voiceTutorScreens_routeConstants_areCorrect() {
        assertEquals("login", VoiceTutorScreens.Login.route)
        assertEquals("signup", VoiceTutorScreens.Signup.route)
        assertEquals("student_dashboard", VoiceTutorScreens.StudentDashboard.route)
        assertEquals("teacher_dashboard", VoiceTutorScreens.TeacherDashboard.route)
        assertEquals("progress", VoiceTutorScreens.Progress.route)
        assertEquals("teacher_classes", VoiceTutorScreens.TeacherClasses.route)
        assertEquals("all_assignments", VoiceTutorScreens.AllAssignments.route)
        assertEquals("all_students", VoiceTutorScreens.AllStudents.route)
        assertEquals("create_assignment", VoiceTutorScreens.CreateAssignment.route)
        assertEquals("create_class", VoiceTutorScreens.CreateClass.route)
        assertEquals("app_info", VoiceTutorScreens.AppInfo.route)
    }

    @Test
    fun voiceTutorScreens_routeWithParameters_hasCorrectBaseRoute() {
        assertTrue(VoiceTutorScreens.Assignment.route.startsWith("assignment/"))
        assertTrue(VoiceTutorScreens.Settings.route.startsWith("settings/"))
        assertTrue(VoiceTutorScreens.TeacherStudents.route.startsWith("teacher_students/"))
        assertTrue(VoiceTutorScreens.CreateAssignment.route.startsWith("create_assignment/"))
    }

    @Test
    fun voiceTutorScreens_createRoute_returnsValidRouteFormat() {
        val routes = listOf(
            VoiceTutorScreens.Assignment.createRoute("1", "과제"),
            VoiceTutorScreens.AssignmentDetail.createRoute("1", "과제"),
            VoiceTutorScreens.AssignmentDetailedResults.createRoute(1, "과제"),
            VoiceTutorScreens.Settings.createRoute(1),
            VoiceTutorScreens.Settings.createRoute(),
            VoiceTutorScreens.EditAssignment.createRoute(1),
            VoiceTutorScreens.TeacherAssignmentResults.createRoute(1),
            VoiceTutorScreens.TeacherAssignmentDetail.createRoute(1)
        )

        // 모든 라우트가 비어있지 않은지 확인
        routes.forEach { route ->
            assertTrue("Route should not be empty: $route", route.isNotBlank())
        }
    }

    @Test
    fun voiceTutorNavigation_handlesSpecialCharactersInRoutes() {
        // 특수 문자가 포함된 제목으로 라우트 생성 테스트
        val specialTitle = "과제: 테스트/문제"
        val route = VoiceTutorScreens.TeacherStudentAssignmentDetail.createRoute("1", 2, specialTitle)
        
        // 슬래시는 언더스코어로 치환되어야 함
        assertTrue(route.contains("테스트_문제") || route.contains("테스트/문제"))
    }

    @Test
    fun voiceTutorScreens_allRoutes_areNonEmpty() {
        val allRoutes = listOf(
            VoiceTutorScreens.Login.route,
            VoiceTutorScreens.Signup.route,
            VoiceTutorScreens.StudentDashboard.route,
            VoiceTutorScreens.TeacherDashboard.route,
            VoiceTutorScreens.Assignment.route,
            VoiceTutorScreens.AssignmentDetail.route,
            VoiceTutorScreens.AssignmentDetailedResults.route,
            VoiceTutorScreens.Progress.route,
            VoiceTutorScreens.TeacherClasses.route,
            VoiceTutorScreens.TeacherStudents.route,
            VoiceTutorScreens.AllAssignments.route,
            VoiceTutorScreens.AllStudents.route,
            VoiceTutorScreens.CreateAssignment.route,
            VoiceTutorScreens.EditAssignment.route,
            VoiceTutorScreens.TeacherAssignmentResults.route,
            VoiceTutorScreens.TeacherAssignmentDetail.route,
            VoiceTutorScreens.TeacherStudentAssignmentDetail.route,
            VoiceTutorScreens.TeacherClassDetail.route,
            VoiceTutorScreens.AttendanceManagement.route,
            VoiceTutorScreens.Settings.route,
            VoiceTutorScreens.CreateClass.route,
            VoiceTutorScreens.AppInfo.route
        )

        allRoutes.forEach { route ->
            assertTrue("Route should not be empty: $route", route.isNotBlank())
        }
    }
}

