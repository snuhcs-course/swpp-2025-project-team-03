package com.example.voicetutor.data.models

import com.google.gson.annotations.SerializedName

enum class AssignmentStatus {
    @SerializedName("IN_PROGRESS")
    IN_PROGRESS,
    @SerializedName("COMPLETED")
    COMPLETED,
    @SerializedName("DRAFT")
    DRAFT
}

enum class AssignmentFilter {
    @SerializedName("ALL")
    ALL,
    @SerializedName("IN_PROGRESS")
    IN_PROGRESS,
    @SerializedName("COMPLETED")
    COMPLETED
}

data class AssignmentData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("total_questions")
    val totalQuestions: Int,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("visible_from")
    val visibleFrom: String? = null,
    @SerializedName("due_at")
    val dueAt: String,
    @SerializedName("course_class")
    val courseClass: CourseClass,
    @SerializedName("materials")
    val materials: List<Material>? = null,
    @SerializedName("grade")
    val grade: String? = null
)

data class CourseClass(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("subject")
    val subject: Subject,
    @SerializedName("teacher_name")
    val teacherName: String,
    @SerializedName("start_date")
    val startDate: String,
    @SerializedName("end_date")
    val endDate: String,
    @SerializedName("student_count")
    val studentCount: Int,
    @SerializedName("created_at")
    val createdAt: String
)

data class Subject(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("code")
    val code: String? = null
)

data class Material(
    @SerializedName("id")
    val id: Int,
    @SerializedName("kind")
    val kind: String,
    @SerializedName("s3_key")
    val s3Key: String,
    @SerializedName("bytes")
    val bytes: Int? = null,
    @SerializedName("created_at")
    val createdAt: String
)

data class QuestionData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("question")
    val question: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("options")
    val options: List<String>? = null,
    @SerializedName("correctAnswer")
    val correctAnswer: String,
    @SerializedName("points")
    val points: Int = 1,
    @SerializedName("explanation")
    val explanation: String? = null
)

data class StudentResult(
    @SerializedName("studentId")
    val studentId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("score")
    val score: Int,
    @SerializedName("confidenceScore")
    val confidenceScore: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("submittedAt")
    val submittedAt: String,
    @SerializedName("answers")
    val answers: List<String>,
    @SerializedName("detailedAnswers")
    val detailedAnswers: List<DetailedAnswer>
)

data class DetailedAnswer(
    @SerializedName("questionNumber")
    val questionNumber: Int,
    @SerializedName("question")
    val question: String,
    @SerializedName("studentAnswer")
    val studentAnswer: String,
    @SerializedName("correctAnswer")
    val correctAnswer: String,
    @SerializedName("isCorrect")
    val isCorrect: Boolean,
    @SerializedName("confidenceScore")
    val confidenceScore: Int,
    @SerializedName("responseTime")
    val responseTime: String
)
