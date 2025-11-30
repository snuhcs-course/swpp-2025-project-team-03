package com.example.voicetutor.ui.navigation

import com.example.voicetutor.data.models.UserRole
import org.junit.Assert.assertEquals
import org.junit.Test

class NavigationHelperTest {

    @Test
    fun getPageTitle_withAssignmentRouteWithParams_returnsCorrectTitle() {
        val title = getPageTitle("assignment/123/과제명", UserRole.STUDENT)
        assertEquals("학생 페이지", title) // Falls back to default since exact match fails
    }

    @Test
    fun getPageTitle_withAssignmentDetailRouteWithParams_returnsCorrectTitle() {
        val title = getPageTitle("assignment_detail/123/과제명", UserRole.STUDENT)
        assertEquals("과제 상세", title)
    }

    @Test
    fun getPageTitle_withCreateAssignmentRouteWithParams_returnsCorrectTitle() {
        val title = getPageTitle("create_assignment/1", UserRole.TEACHER)
        assertEquals("과제 생성", title)
    }

    @Test
    fun getPageTitle_withEditAssignmentRouteWithParams_returnsCorrectTitle() {
        val title = getPageTitle("edit_assignment/123", UserRole.TEACHER)
        assertEquals("과제 편집", title)
    }

    @Test
    fun getPageTitle_withTeacherAssignmentResultsRouteWithParams_returnsCorrectTitle() {
        val title = getPageTitle("teacher_assignment_results/123", UserRole.TEACHER)
        assertEquals("과제 결과", title)
    }

    @Test
    fun getPageTitle_withTeacherAssignmentDetailRouteWithParams_returnsCorrectTitle() {
        val title = getPageTitle("teacher_assignment_detail/123", UserRole.TEACHER)
        assertEquals("과제 상세", title)
    }

    @Test
    fun getPageTitle_withTeacherStudentAssignmentDetailRouteWithParams_returnsCorrectTitle() {
        val title = getPageTitle("teacher_student_assignment_detail/123/456", UserRole.TEACHER)
        assertEquals("과제 결과", title)
    }

    @Test
    fun getPageTitle_withTeacherStudentReportRouteWithParams_returnsCorrectTitle() {
        val title = getPageTitle("teacher_student_report/123/456", UserRole.TEACHER)
        assertEquals("리포트", title)
    }

    @Test
    fun getPageTitle_withTeacherClassDetailRouteWithParams_returnsCorrectTitle() {
        val title = getPageTitle("teacher_class_detail/수업명/123", UserRole.TEACHER)
        assertEquals("수업 관리", title)
    }

    @Test
    fun getPageTitle_withSettingsRouteWithParams_returnsCorrectTitle() {
        val title = getPageTitle("settings/123", UserRole.STUDENT)
        assertEquals("학생 페이지", title) // Falls back to default since exact match fails
    }

    @Test
    fun getPageTitle_withNoRecentAssignmentRouteWithParams_returnsCorrectTitle() {
        val title = getPageTitle("no_recent_assignment/123", UserRole.STUDENT)
        assertEquals("학생 페이지", title) // Default fallback
    }

    @Test
    fun getPageTitle_withCreateClassRoute_returnsCorrectTitle() {
        val title = getPageTitle(VoiceTutorScreens.CreateClass.route, UserRole.TEACHER)
        assertEquals("수업 생성", title)
    }

    @Test
    fun getPageTitle_withAppInfoRoute_returnsCorrectTitle() {
        val title = getPageTitle(VoiceTutorScreens.AppInfo.route, UserRole.STUDENT)
        assertEquals("학생 페이지", title) // Default fallback
    }

    @Test
    fun getPageTitle_withLoginRoute_returnsDefault() {
        val title = getPageTitle(VoiceTutorScreens.Login.route, UserRole.STUDENT)
        assertEquals("학생 페이지", title)
    }

    @Test
    fun getPageTitle_withSignupRoute_returnsDefault() {
        val title = getPageTitle(VoiceTutorScreens.Signup.route, UserRole.STUDENT)
        assertEquals("학생 페이지", title)
    }

    @Test
    fun getPageTitle_withStudentDashboardRoute_returnsDefault() {
        val title = getPageTitle(VoiceTutorScreens.StudentDashboard.route, UserRole.STUDENT)
        assertEquals("학생 페이지", title)
    }

    @Test
    fun getPageTitle_withTeacherDashboardRoute_returnsDefault() {
        val title = getPageTitle(VoiceTutorScreens.TeacherDashboard.route, UserRole.TEACHER)
        assertEquals("선생님 페이지", title)
    }

    @Test
    fun getPageTitle_withRouteContainingQueryParams_handlesCorrectly() {
        val title = getPageTitle("teacher_dashboard?refresh=123", UserRole.TEACHER)
        assertEquals("선생님 페이지", title)
    }

    @Test
    fun getPageTitle_withPartialRouteMatch_handlesCorrectly() {
        val title = getPageTitle("assignment_detail", UserRole.STUDENT)
        assertEquals("학생 페이지", title)
    }

    @Test
    fun getPageTitle_withAssignmentDetailRouteStartsWith_returnsCorrectTitle() {
        val title = getPageTitle("assignment_detail/123/과제명", UserRole.STUDENT)
        assertEquals("과제 상세", title)
    }

    @Test
    fun getPageTitle_withAssignmentRouteExactMatch_returnsCorrectTitle() {
        val title = getPageTitle(VoiceTutorScreens.Assignment.route, UserRole.STUDENT)
        assertEquals("과제", title)
    }

    @Test
    fun getPageTitle_withSettingsRouteExactMatch_returnsCorrectTitle() {
        val title = getPageTitle(VoiceTutorScreens.Settings.route, UserRole.STUDENT)
        assertEquals("계정", title)
    }
}
