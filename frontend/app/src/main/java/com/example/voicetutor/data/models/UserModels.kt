package com.example.voicetutor.data.models

import com.google.gson.annotations.SerializedName

enum class UserRole {
    @SerializedName("TEACHER")
    TEACHER,

    @SerializedName("STUDENT")
    STUDENT,
}

data class User(
    @SerializedName("id")
    val id: Int,
    @SerializedName("display_name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("role")
    val role: UserRole,
    @SerializedName("is_student")
    val isStudent: Boolean? = null,
    @SerializedName("lastLoginAt")
    val lastLoginAt: String? = null,
    @SerializedName("totalAssignments")
    val totalAssignments: Int? = null,
    @SerializedName("completedAssignments")
    val completedAssignments: Int? = null,
    @SerializedName("inProgressAssignments")
    val inProgressAssignments: Int? = null,
    @SerializedName("totalStudents")
    val totalStudents: Int? = null,
    @SerializedName("totalClasses")
    val totalClasses: Int? = null,
    @SerializedName("assignments")
    val assignments: List<AssignmentData>? = null,
) {
    val initial: String
        get() = name.firstOrNull()?.toString() ?: "?"

    val welcomeMessage: String
        get() = when (role) {
            UserRole.TEACHER -> "환영합니다, $name 선생님!"
            UserRole.STUDENT -> "안녕하세요, ${name}님!"
        }

    val subMessage: String
        get() = when (role) {
            UserRole.TEACHER -> "수업을 관리하고 학생들의 진도를 추적하세요"
            UserRole.STUDENT -> "오늘도 VoiceTutor와 함께 학습을 시작해볼까요?"
        }
}

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
)

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("data")
    val user: User?,
    @SerializedName("token")
    val token: String?,
    @SerializedName("message")
    val message: String?,
    @SerializedName("error")
    val error: String?,
)

data class SignupRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("role")
    val role: String,
)

data class DashboardStats(
    @SerializedName("total_assignments")
    val totalAssignments: Int,
    @SerializedName("total_students")
    val totalStudents: Int,
    @SerializedName("total_classes")
    val totalClasses: Int,
    @SerializedName("completedAssignments")
    val completedAssignments: Int = 0,
    @SerializedName("inProgressAssignments")
    val inProgressAssignments: Int = 0,
)
