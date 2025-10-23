package com.example.voicetutor.data.models

import com.google.gson.annotations.SerializedName

enum class AssignmentStatus {
    @SerializedName("IN_PROGRESS")
    IN_PROGRESS,  // 진행중
    @SerializedName("COMPLETED")
    COMPLETED,    // 완료
    @SerializedName("DRAFT")
    DRAFT         // 임시저장
}

// enum class AssignmentType {
//     @SerializedName("Quiz")
//     Quiz,         // 퀴즈
//     @SerializedName("Continuous")
//     Continuous,   // 연속형
//     @SerializedName("Discussion")
//     Discussion    // 토론
// }

enum class AssignmentFilter {
    @SerializedName("ALL")
    ALL,          // 전체
    @SerializedName("IN_PROGRESS")
    IN_PROGRESS,  // 진행중
    @SerializedName("COMPLETED")
    COMPLETED     // 완료
}

data class AssignmentData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("subject")
    val subject: Subject,
    @SerializedName("class")
    val class: ClassData,
    @SerializedName("classId")
    val classId: Int,
    @SerializedName("dueAt")
    val dueAt: String,
    // @SerializedName("submittedCount")
    // val submittedCount: Int,
    @SerializedName("totalQuestions")
    val totalQuestions: Int,
    @SerializedName("status")
    val status: AssignmentStatus,
    // @SerializedName("type")
    // val type: AssignmentType,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("visibleAt")
    val visibleAt: String? = null,
    @SerializedName("dueAt")
    val dueAt: String? = null
)

data class QuestionData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("question")
    val question: String,
    @SerializedName("type")
    val type: QuestionType,
    @SerializedName("options")
    val options: List<String>? = null,
    @SerializedName("correctAnswer")
    val correctAnswer: String,
    @SerializedName("points")
    val points: Int = 1,
    @SerializedName("explanation")
    val explanation: String? = null
)

// enum class QuestionType {
//     @SerializedName("MULTIPLE_CHOICE")
//     MULTIPLE_CHOICE,
//     @SerializedName("SHORT_ANSWER")
//     SHORT_ANSWER,
//     @SerializedName("VOICE_RESPONSE")
//     VOICE_RESPONSE
// }

data class StudentResult(
    @SerializedName("studentId")
    val studentId: String,
    @SerializedName("name")
    val name: String,
    // @SerializedName("completionTime")
    // val completionTime: String,
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
    // @SerializedName("pronunciationScore")
    // val pronunciationScore: Int?,
    @SerializedName("responseTime")
    val responseTime: String
)
