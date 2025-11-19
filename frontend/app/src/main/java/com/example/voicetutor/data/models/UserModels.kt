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
    // @SerializedName("avatar")
    // val avatar: String? = null,
    // @SerializedName("className")
    // val className: String? = null,
    // @SerializedName("classId")
    // val classId: Int? = null,
    @SerializedName("lastLoginAt")
    val lastLoginAt: String? = null,

    // 로그인 시 받은 추가 데이터
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
    // 사용자 이름의 첫 글자를 반환 (프로필 이니셜용)
    val initial: String
        get() = name.firstOrNull()?.toString() ?: "?"

    // 환영 메시지 생성
    val welcomeMessage: String
        get() = when (role) {
            UserRole.TEACHER -> "환영합니다, $name 선생님!"
            UserRole.STUDENT -> "안녕하세요, ${name}님!"
        }

    // 서브 메시지 생성
    val subMessage: String
        get() = when (role) {
            UserRole.TEACHER -> "수업을 관리하고 학생들의 진도를 추적하세요"
            UserRole.STUDENT -> "오늘도 VoiceTutor와 함께 학습을 시작해볼까요?"
        }
}

// 로그인 요청 데이터
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
)

// 로그인 응답 데이터
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

// 회원가입 요청 데이터
data class SignupRequest(
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String,
    @SerializedName("role")
    val role: String, // 백엔드에서는 String으로 받음
    // @SerializedName("className")
    // val className: String? = null
)

// 대시보드 통계 데이터
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

// 최근 활동 데이터
// data class RecentActivity(
//     @SerializedName("id")
//     val id: String,
//     @SerializedName("studentName")
//     val studentName: String,
//     @SerializedName("action")
//     val action: String,
//     @SerializedName("time")
//     val time: String,
//     @SerializedName("iconType")
//     val iconType: String,
//     @SerializedName("assignmentTitle")
//     val assignmentTitle: String? = null
// )
