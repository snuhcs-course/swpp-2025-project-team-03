package com.example.voicetutor.data.repository.fake

import com.example.voicetutor.data.models.User
import com.example.voicetutor.data.models.UserRole

/**
 * Fake implementation of AuthRepository for integration testing.
 *
 * This repository NEVER makes real network calls!
 *
 * How it works:
 * - Standalone class with same method signatures as AuthRepository
 * - Uses in-memory data storage instead of network calls
 * - NO ApiService needed - completely independent
 * - All data lives in-memory (users, credentials maps)
 *
 *
 * Used by: FakeAuthRepositoryWrapper for Hilt dependency injection
 */
class FakeAuthRepository {
    // In-memory storage for test users
    private val users = mutableMapOf<String, User>()

    init {
        // Pre-populate with test accounts
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

    // Simulated user credentials (email -> password)
    private val credentials = mutableMapOf(
        "student@voicetutor.com" to "student123",
        "teacher@voicetutor.com" to "teacher123",
    )

    //  Same method signature as real AuthRepository.login()
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            //  NO NETWORK CALL - All data is in-memory
            // Simulate network delay for realistic UI testing
            kotlinx.coroutines.delay(500)

            // Check if user exists and password matches (in-memory check)
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

    //  Same method signature as real AuthRepository.signup()
    suspend fun signup(
        name: String,
        email: String,
        password: String,
        role: UserRole,
    ): Result<User> {
        return try {
            //  NO NETWORK CALL - All data is in-memory
            // Simulate network delay for realistic UI testing
            kotlinx.coroutines.delay(500)

            // Check if user already exists (in-memory check)
            if (users.containsKey(email)) {
                return Result.failure(Exception("이미 존재하는 이메일입니다"))
            }

            // Create new user (stored in-memory, not in database)
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

            // Store user and credentials (in-memory only)
            users[email] = newUser
            credentials[email] = password

            Result.success(newUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Helper method for testing - clear all data
    fun clearAll() {
        users.clear()
        credentials.clear()
    }

    // Helper method for testing - reset to initial state
    fun reset() {
        clearAll()
        // Re-initialize with default test accounts
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
