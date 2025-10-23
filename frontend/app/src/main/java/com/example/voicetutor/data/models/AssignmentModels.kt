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
    @SerializedName("subject")
    val subject: Subject,
    @SerializedName("class")
    val `class`: ClassData,
    @SerializedName("classId")
    val classId: Int,
    @SerializedName("dueAt")
    val dueAt: String,
    @SerializedName("totalQuestions")
    val totalQuestions: Int,
    @SerializedName("status")
    val status: AssignmentStatus,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("visibleAt")
    val visibleAt: String? = null
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
