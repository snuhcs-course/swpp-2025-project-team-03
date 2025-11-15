package com.example.voicetutor.ui.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class VoiceTutorScreensTest {

    @Test
    fun login_route_isCorrect() {
        assertEquals("login", VoiceTutorScreens.Login.route)
    }

    @Test
    fun signup_route_isCorrect() {
        assertEquals("signup", VoiceTutorScreens.Signup.route)
    }

    @Test
    fun studentDashboard_route_isCorrect() {
        assertEquals("student_dashboard", VoiceTutorScreens.StudentDashboard.route)
    }

    @Test
    fun teacherDashboard_route_isCorrect() {
        assertEquals("teacher_dashboard", VoiceTutorScreens.TeacherDashboard.route)
    }

    @Test
    fun settings_createRoute_withStudentId_createsCorrectRoute() {
        val route = VoiceTutorScreens.Settings.createRoute(123)
        assertEquals("settings/123", route)
    }

    @Test
    fun settings_createRoute_withoutStudentId_createsDefaultRoute() {
        val route = VoiceTutorScreens.Settings.createRoute()
        assertEquals("settings/-1", route)
    }

    @Test
    fun assignment_createRoute_createsCorrectRoute() {
        val route = VoiceTutorScreens.Assignment.createRoute("123", "과제명")
        assertEquals("assignment/123/과제명", route)
    }

    @Test
    fun assignmentDetail_createRoute_createsCorrectRoute() {
        val route = VoiceTutorScreens.AssignmentDetail.createRoute("123", "과제명")
        assertEquals("assignment_detail/123/과제명", route)
    }

    @Test
    fun assignmentDetailedResults_createRoute_createsCorrectRoute() {
        val route = VoiceTutorScreens.AssignmentDetailedResults.createRoute(123, "과제명")
        assertEquals("assignment_detailed_results/123/과제명", route)
    }

    @Test
    fun noRecentAssignment_createRoute_createsCorrectRoute() {
        val route = VoiceTutorScreens.NoRecentAssignment.createRoute(123)
        assertEquals("no_recent_assignment/123", route)
    }

    @Test
    fun teacherStudents_createRoute_createsCorrectRoute() {
        val route = VoiceTutorScreens.TeacherStudents.createRoute("123")
        assertEquals("teacher_students/123", route)
    }

    @Test
    fun createAssignment_createRoute_withClassId_createsCorrectRoute() {
        val route = VoiceTutorScreens.CreateAssignment.createRoute(123)
        assertEquals("create_assignment/123", route)
    }

    @Test
    fun createAssignment_createRoute_withoutClassId_createsDefaultRoute() {
        val route = VoiceTutorScreens.CreateAssignment.createRoute()
        assertEquals("create_assignment/0", route)
    }

    @Test
    fun createAssignment_createRoute_withZeroClassId_createsDefaultRoute() {
        val route = VoiceTutorScreens.CreateAssignment.createRoute(0)
        assertEquals("create_assignment/0", route)
    }

    @Test
    fun editAssignment_createRoute_createsCorrectRoute() {
        val route = VoiceTutorScreens.EditAssignment.createRoute(123)
        assertEquals("edit_assignment/123", route)
    }

    @Test
    fun teacherAssignmentResults_createRoute_createsCorrectRoute() {
        val route = VoiceTutorScreens.TeacherAssignmentResults.createRoute(123)
        assertEquals("teacher_assignment_results/123", route)
    }

    @Test
    fun teacherAssignmentDetail_createRoute_createsCorrectRoute() {
        val route = VoiceTutorScreens.TeacherAssignmentDetail.createRoute(123)
        assertEquals("teacher_assignment_detail/123", route)
    }

    @Test
    fun teacherStudentAssignmentDetail_createRoute_createsCorrectRoute() {
        val route = VoiceTutorScreens.TeacherStudentAssignmentDetail.createRoute("123", 456, "과제명")
        assertEquals("teacher_student_assignment_detail/123/456/과제명", route)
    }

    @Test
    fun teacherStudentAssignmentDetail_createRoute_withSlashInTitle_replacesSlash() {
        val route = VoiceTutorScreens.TeacherStudentAssignmentDetail.createRoute("123", 456, "과제/명")
        assertEquals("teacher_student_assignment_detail/123/456/과제_명", route)
    }

    @Test
    fun teacherClassDetail_createRoute_createsCorrectRoute() {
        val route = VoiceTutorScreens.TeacherClassDetail.createRoute("수업명", 123)
        assertEquals("teacher_class_detail/수업명/123", route)
    }

    @Test
    fun teacherStudentReport_createRoute_createsCorrectRoute() {
        val route = VoiceTutorScreens.TeacherStudentReport.createRoute(1, 2, "학생명")
        assertEquals("teacher_student_report/1/2/학생명", route)
    }

    @Test
    fun teacherStudentReport_createRoute_withSlashInName_replacesSlash() {
        val route = VoiceTutorScreens.TeacherStudentReport.createRoute(1, 2, "학생/명")
        assertEquals("teacher_student_report/1/2/학생_명", route)
    }

    @Test
    fun attendanceManagement_createRoute_createsCorrectRoute() {
        val route = VoiceTutorScreens.AttendanceManagement.createRoute(123)
        assertEquals("attendance_management/123", route)
    }
}

