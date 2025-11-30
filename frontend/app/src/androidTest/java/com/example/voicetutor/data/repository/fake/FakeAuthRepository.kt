package com.example.voicetutor.data.repository.fake

import com.example.voicetutor.data.models.User
import com.example.voicetutor.data.models.UserRole

class FakeAuthRepository {

    private val users = mutableMapOf<String, User>()

    init {

        users["student@voicetutor.com"] = User(
            id = 1,
            name = "테스트학생",
            email = "student@voicetutor.com",
            role = UserRole.STUDENT,
            isStudent = true,
            totalAssignments = 5,
            completedAssignments = 2,
            inProgressAssignments = 3,
            assignments = emptyList(),
        )

        users["teacher@voicetutor.com"] = User(
            id = 2,
            name = "테스트선생님",
            email = "teacher@voicetutor.com",
            role = UserRole.TEACHER,
            isStudent = false,
            totalStudents = 10,
            totalClasses = 3,
            assignments = emptyList(),
        )
    }

    private val credentials = mutableMapOf(
        "student@voicetutor.com" to "student123",
        "teacher@voicetutor.com" to "teacher123",
    )

    suspend fun login(email: String, password: String): Result<User> {
        return try {
            kotlinx.coroutines.delay(500)

            val storedPassword = credentials[email]
            if (storedPassword == null) {
                return Result.failure(Exception("사용자를 찾을 수 없습니다"))
            }

            if (storedPassword != password) {
                return Result.failure(Exception("비밀번호가 일치하지 않습니다"))
            }

            val user = users[email]
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("사용자 정보를 찾을 수 없습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signup(
        name: String,
        email: String,
        password: String,
        role: UserRole,
    ): Result<User> {
        return try {
            kotlinx.coroutines.delay(500)

            if (users.containsKey(email)) {
                return Result.failure(Exception("이미 존재하는 이메일입니다"))
            }

            val newUser = User(
                id = users.size + 1,
                name = name,
                email = email,
                role = role,
                isStudent = role == UserRole.STUDENT,
                totalAssignments = 0,
                completedAssignments = 0,
                inProgressAssignments = 0,
                totalStudents = if (role == UserRole.TEACHER) 0 else null,
                totalClasses = if (role == UserRole.TEACHER) 0 else null,
                assignments = emptyList(),
            )

            users[email] = newUser
            credentials[email] = password

            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearAll() {
        users.clear()
        credentials.clear()
    }

    fun reset() {
        clearAll()

        users["student@voicetutor.com"] = User(
            id = 1,
            name = "테스트학생",
            email = "student@voicetutor.com",
            role = UserRole.STUDENT,
            isStudent = true,
            totalAssignments = 5,
            completedAssignments = 2,
            inProgressAssignments = 3,
            assignments = emptyList(),
        )

        users["teacher@voicetutor.com"] = User(
            id = 2,
            name = "테스트선생님",
            email = "teacher@voicetutor.com",
            role = UserRole.TEACHER,
            isStudent = false,
            totalStudents = 10,
            totalClasses = 3,
            assignments = emptyList(),
        )

        credentials["student@voicetutor.com"] = "student123"
        credentials["teacher@voicetutor.com"] = "teacher123"
    }
}
