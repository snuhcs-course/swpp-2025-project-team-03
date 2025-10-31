package com.example.voicetutor.data.models

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import assert

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
        assert(initial == "홍")
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
        assert(initial == "?")
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
        assert(message == "환영합니다, 김선생선생님!")
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
        assert(message == "안녕하세요, 이학생님!")
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
        assert(message == "수업을 관리하고 학생들의 진도를 추적하세요")
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
        assert(message == "오늘도 VoiceTutor와 함께 학습을 시작해볼까요?")
    }

    @Test
    fun userRole_teachValue_equalsTeacher() {
        // Assert
        assert(UserRole.TEACHER.name == "TEACHER")
    }

    @Test
    fun userRole_studentValue_equalsStudent() {
        // Assert
        assert(UserRole.STUDENT.name == "STUDENT")
    }
}

