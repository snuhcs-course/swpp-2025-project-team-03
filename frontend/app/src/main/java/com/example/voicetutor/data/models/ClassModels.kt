package com.example.voicetutor.data.models

import com.google.gson.annotations.SerializedName

data class ClassData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("subject")
    val subject: Subject,
    @SerializedName("description")
    val description: String,
    @SerializedName("teacherId")
    val teacherId: Int,
    @SerializedName("teacher_name")
    val teacherName: String? = null,
    @SerializedName("studentCount")
    val studentCount: Int,
    @SerializedName("student_count")
    val studentCountAlt: Int? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("startDate")
    val startDate: String? = null,
    @SerializedName("endDate")
    val endDate: String? = null
) {
    // Helper property to get student count from either field
    val actualStudentCount: Int
        get() = studentCountAlt ?: studentCount
}

data class EnrollmentData(
    @SerializedName("student")
    val student: Student,
    @SerializedName("course_class")
    val courseClass: ClassData?,
    @SerializedName("status")
    val status: String
)

data class MessageData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("teacherId")
    val teacherId: Int,
    @SerializedName("teacherName")
    val teacherName: String,
    @SerializedName("classId")
    val classId: Int,
    // @SerializedName("recipientIds")
    // val recipientIds: List<Int>? = null,
    @SerializedName("sentAt")
    val sentAt: String,
    // @SerializedName("isRead")
    // val isRead: Boolean = false
)
