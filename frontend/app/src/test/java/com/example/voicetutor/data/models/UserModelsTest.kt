package com.example.voicetutor.data.models

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert.*

@RunWith(JUnit4::class)
class UserModelsTest {

    @Test
    fun user_initial_withName_returnsFirstChar() {
        // Arrange
        val user = User(
            id = 1,
            name = "홍길동",
            email = "test@test.com",
            role = UserRole.TEACHER
        )

        // Act
        val initial = user.initial

        // Assert
        assertEquals("홍", initial)
    }

    @Test
    fun user_initial_withEmptyName_returnsQuestionMark() {
        // Arrange
        val user = User(
            id = 1,
            name = "",
            email = "test@test.com",
            role = UserRole.TEACHER
        )

        // Act
        val initial = user.initial

        // Assert
        assertEquals("?", initial)
    }

    @Test
    fun user_welcomeMessage_teacherRole_returnsTeacherMessage() {
        // Arrange
        val user = User(
            id = 1,
            name = "김선생",
            email = "teacher@test.com",
            role = UserRole.TEACHER
        )

        // Act
        val message = user.welcomeMessage

        // Assert
        assertEquals("환영합니다, 김선생 선생님!", message)
    }

    @Test
    fun user_welcomeMessage_studentRole_returnsStudentMessage() {
        // Arrange
        val user = User(
            id = 1,
            name = "이학생",
            email = "student@test.com",
            role = UserRole.STUDENT
        )

        // Act
        val message = user.welcomeMessage

        // Assert
        assertEquals("안녕하세요, 이학생님!", message)
    }

    @Test
    fun user_subMessage_teacherRole_returnsTeacherSubMessage() {
        // Arrange
        val user = User(
            id = 1,
            name = "김선생",
            email = "teacher@test.com",
            role = UserRole.TEACHER
        )

        // Act
        val message = user.subMessage

        // Assert
        assertEquals("수업을 관리하고 학생들의 진도를 추적하세요", message)
    }

    @Test
    fun user_subMessage_studentRole_returnsStudentSubMessage() {
        // Arrange
        val user = User(
            id = 1,
            name = "이학생",
            email = "student@test.com",
            role = UserRole.STUDENT
        )

        // Act
        val message = user.subMessage

        // Assert
        assertEquals("오늘도 VoiceTutor와 함께 학습을 시작해볼까요?", message)
    }

    @Test
    fun userRole_teachValue_equalsTeacher() {
        // Assert
        assertEquals("TEACHER", UserRole.TEACHER.name)
    }

    @Test
    fun userRole_studentValue_equalsStudent() {
        // Assert
        assertEquals("STUDENT", UserRole.STUDENT.name)
    }

    @Test
    fun user_withAllOptionalFields_initializesCorrectly() {
        // Arrange
        val user = User(
            id = 1,
            name = "홍길동",
            email = "test@test.com",
            role = UserRole.TEACHER,
            isStudent = false,
            lastLoginAt = "2025-01-01T00:00:00Z",
            totalAssignments = 10,
            completedAssignments = 5,
            inProgressAssignments = 3,
            totalStudents = 20,
            totalClasses = 2,
            assignments = emptyList()
        )

        // Assert
        assertEquals(1, user.id)
        assertEquals("홍길동", user.name)
        assertEquals("test@test.com", user.email)
        assertEquals(UserRole.TEACHER, user.role)
        assertEquals(false, user.isStudent)
        assertEquals(10, user.totalAssignments)
        assertEquals(5, user.completedAssignments)
        assertEquals(3, user.inProgressAssignments)
        assertEquals(20, user.totalStudents)
        assertEquals(2, user.totalClasses)
        assertNotNull(user.assignments)
    }

    @Test
    fun user_withNullOptionalFields_initializesCorrectly() {
        // Arrange
        val user = User(
            id = 1,
            name = "홍길동",
            email = "test@test.com",
            role = UserRole.STUDENT
        )

        // Assert
        assertNull(user.isStudent)
        assertNull(user.lastLoginAt)
        assertNull(user.totalAssignments)
        assertNull(user.completedAssignments)
        assertNull(user.inProgressAssignments)
        assertNull(user.totalStudents)
        assertNull(user.totalClasses)
        assertNull(user.assignments)
    }

    @Test
    fun loginRequest_initializesCorrectly() {
        // Arrange
        val request = LoginRequest(
            email = "test@test.com",
            password = "password123"
        )

        // Assert
        assertEquals("test@test.com", request.email)
        assertEquals("password123", request.password)
    }

    @Test
    fun loginResponse_withAllFields_initializesCorrectly() {
        // Arrange
        val user = User(
            id = 1,
            name = "홍길동",
            email = "test@test.com",
            role = UserRole.TEACHER
        )
        val response = LoginResponse(
            success = true,
            user = user,
            token = "token123",
            message = "로그인 성공",
            error = null
        )

        // Assert
        assertTrue(response.success)
        assertEquals(user, response.user)
        assertEquals("token123", response.token)
        assertEquals("로그인 성공", response.message)
        assertNull(response.error)
    }

    @Test
    fun loginResponse_withNullFields_initializesCorrectly() {
        // Arrange
        val response = LoginResponse(
            success = false,
            user = null,
            token = null,
            message = null,
            error = "로그인 실패"
        )

        // Assert
        assertFalse(response.success)
        assertNull(response.user)
        assertNull(response.token)
        assertNull(response.message)
        assertEquals("로그인 실패", response.error)
    }

    @Test
    fun signupRequest_initializesCorrectly() {
        // Arrange
        val request = SignupRequest(
            name = "홍길동",
            email = "test@test.com",
            password = "password123",
            role = "STUDENT"
        )

        // Assert
        assertEquals("홍길동", request.name)
        assertEquals("test@test.com", request.email)
        assertEquals("password123", request.password)
        assertEquals("STUDENT", request.role)
    }

    @Test
    fun signupRequest_withTeacherRole_initializesCorrectly() {
        // Arrange
        val request = SignupRequest(
            name = "김선생",
            email = "teacher@test.com",
            password = "password123",
            role = "TEACHER"
        )

        // Assert
        assertEquals("TEACHER", request.role)
    }

    @Test
    fun dashboardStats_initializesCorrectly() {
        // Arrange
        val stats = DashboardStats(
            totalAssignments = 10,
            totalStudents = 20,
            totalClasses = 2,
            completedAssignments = 5,
            inProgressAssignments = 3
        )

        // Assert
        assertEquals(10, stats.totalAssignments)
        assertEquals(20, stats.totalStudents)
        assertEquals(2, stats.totalClasses)
        assertEquals(5, stats.completedAssignments)
        assertEquals(3, stats.inProgressAssignments)
    }

    @Test
    fun dashboardStats_withDefaultValues_initializesCorrectly() {
        // Arrange
        val stats = DashboardStats(
            totalAssignments = 10,
            totalStudents = 20,
            totalClasses = 2
        )

        // Assert
        assertEquals(0, stats.completedAssignments)
        assertEquals(0, stats.inProgressAssignments)
    }

    @Test
    fun userRole_values_containsBothRoles() {
        // When
        val values = UserRole.values()

        // Then
        assertEquals(2, values.size)
        assertTrue(values.contains(UserRole.TEACHER))
        assertTrue(values.contains(UserRole.STUDENT))
    }

    @Test
    fun user_initial_withSingleChar_returnsChar() {
        // Arrange
        val user = User(
            id = 1,
            name = "A",
            email = "test@test.com",
            role = UserRole.TEACHER
        )

        // Act
        val initial = user.initial

        // Assert
        assertEquals("A", initial)
    }

    @Test
    fun user_initial_withEnglishName_returnsFirstChar() {
        // Arrange
        val user = User(
            id = 1,
            name = "Alice",
            email = "test@test.com",
            role = UserRole.TEACHER
        )

        // Act
        val initial = user.initial

        // Assert
        assertEquals("A", initial)
    }
}

