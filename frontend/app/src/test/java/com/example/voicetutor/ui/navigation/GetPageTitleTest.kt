package com.example.voicetutor.ui.navigation

import com.example.voicetutor.data.models.UserRole
import org.junit.Assert.*
import org.junit.Test

class GetPageTitleTest {

    @Test
    fun getPageTitle_assignment_returnsCorrectTitle() {
        val title = getPageTitle(VoiceTutorScreens.Assignment.route, UserRole.STUDENT)
        assertEquals("과제", title)
    }

    @Test
    fun getPageTitle_assignmentDetail_returnsCorrectTitle() {
        val title = getPageTitle(VoiceTutorScreens.AssignmentDetail.route, UserRole.STUDENT)
        assertEquals("과제 상세", title)
    }

    @Test
    fun getPageTitle_assignmentDetailedResults_returnsCorrectTitle() {
        val title = getPageTitle(VoiceTutorScreens.AssignmentDetailedResults.route, UserRole.STUDENT)
        assertEquals("과제 결과", title)
    }

    @Test
    fun getPageTitle_progress_returnsCorrectTitle() {
        val title = getPageTitle(VoiceTutorScreens.Progress.route, UserRole.STUDENT)
        assertEquals("학습 리포트", title)
    }

    @Test
    fun getPageTitle_teacherClasses_returnsCorrectTitle() {
        val title = getPageTitle(VoiceTutorScreens.TeacherClasses.route, UserRole.TEACHER)
        assertEquals("수업 관리", title)
    }

    @Test
    fun getPageTitle_teacherStudents_returnsCorrectTitle() {
        val title = getPageTitle(VoiceTutorScreens.TeacherStudents.route, UserRole.TEACHER)
        assertEquals("학생 관리", title)
    }

    @Test
    fun getPageTitle_allAssignments_returnsCorrectTitle() {
        val title = getPageTitle(VoiceTutorScreens.AllAssignments.route, UserRole.TEACHER)
        assertEquals("전체 과제", title)
    }

    @Test
    fun getPageTitle_allStudents_returnsCorrectTitle() {
        val title = getPageTitle(VoiceTutorScreens.AllStudents.route, UserRole.TEACHER)
        assertEquals("전체 학생", title)
    }

    @Test
    fun getPageTitle_createAssignment_returnsCorrectTitle() {
        val title = getPageTitle(VoiceTutorScreens.CreateAssignment.route, UserRole.TEACHER)
        assertEquals("과제 생성", title)
    }

    @Test
    fun getPageTitle_editAssignment_returnsCorrectTitle() {
        val title = getPageTitle(VoiceTutorScreens.EditAssignment.route, UserRole.TEACHER)
        assertEquals("과제 편집", title)
    }

    @Test
    fun getPageTitle_teacherAssignmentResults_returnsCorrectTitle() {
        val title = getPageTitle(VoiceTutorScreens.TeacherAssignmentResults.route, UserRole.TEACHER)
        assertEquals("과제 결과", title)
    }

    @Test
    fun getPageTitle_teacherAssignmentDetail_returnsCorrectTitle() {
        val title = getPageTitle(VoiceTutorScreens.TeacherAssignmentDetail.route, UserRole.TEACHER)
        assertEquals("과제 상세", title)
    }

    @Test
    fun getPageTitle_teacherStudentAssignmentDetail_returnsCorrectTitle() {
        val title = getPageTitle(VoiceTutorScreens.TeacherStudentAssignmentDetail.route, UserRole.TEACHER)
        assertEquals("과제 결과", title)
    }

    @Test
    fun getPageTitle_settings_returnsCorrectTitle() {
        val title = getPageTitle(VoiceTutorScreens.Settings.route, UserRole.STUDENT)
        assertEquals("설정", title)
    }

    @Test
    fun getPageTitle_nullRoute_returnsDefaultForStudent() {
        val title = getPageTitle(null, UserRole.STUDENT)
        assertEquals("학생 페이지", title)
    }

    @Test
    fun getPageTitle_nullRoute_returnsDefaultForTeacher() {
        val title = getPageTitle(null, UserRole.TEACHER)
        assertEquals("선생님 페이지", title)
    }

    @Test
    fun getPageTitle_unknownRoute_returnsDefaultForStudent() {
        val title = getPageTitle("unknown_route", UserRole.STUDENT)
        assertEquals("학생 페이지", title)
    }

    @Test
    fun getPageTitle_unknownRoute_returnsDefaultForTeacher() {
        val title = getPageTitle("unknown_route", UserRole.TEACHER)
        assertEquals("선생님 페이지", title)
    }

    @Test
    fun getPageTitle_emptyRoute_returnsDefault() {
        val title = getPageTitle("", UserRole.STUDENT)
        assertEquals("학생 페이지", title)
    }
}

