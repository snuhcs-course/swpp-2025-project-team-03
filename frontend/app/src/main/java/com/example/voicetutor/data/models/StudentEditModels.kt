package com.example.voicetutor.data.models

import com.google.gson.annotations.SerializedName

/**
 * 학생 편집 요청 모델
 */
data class StudentEditRequest(
    @SerializedName("studentId")
    val studentId: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String
)

/**
 * 학생 편집 응답 모델
 */
data class StudentEditResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("updatedStudent")
    val updatedStudent: Student? = null
)

/**
 * 학생 삭제 요청 모델
 */
data class StudentDeleteRequest(
    @SerializedName("studentId")
    val studentId: Int
)

/**
 * 학생 삭제 응답 모델
 */
data class StudentDeleteResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)


/**
 * 학생 클래스 변경 요청 모델
 */
data class StudentClassChangeRequest(
    @SerializedName("studentId")
    val studentId: Int,
    @SerializedName("fromClassId")
    val fromClassId: Int,
    @SerializedName("toClassId")
    val toClassId: Int,
    @SerializedName("reason")
    val reason: String? = null
)

/**
 * 학생 클래스 변경 응답 모델
 */
data class StudentClassChangeResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("updatedStudent")
    val updatedStudent: Student? = null
)
