package com.example.voicetutor.data.models

import com.google.gson.annotations.SerializedName

/**
 * AI 대화 요청 모델
 */
data class AIConversationRequest(
    @SerializedName("assignmentId")
    val assignmentId: String,
    @SerializedName("studentId")
    val studentId: Int,
    @SerializedName("message")
    val message: String,
    @SerializedName("conversationHistory")
    val conversationHistory: List<ConversationMessage>? = null
)

/**
 * AI 대화 응답 모델
 */
data class AIConversationResponse(
    @SerializedName("response")
    val response: String,
    @SerializedName("feedback")
    val feedback: String? = null,
    @SerializedName("score")
    val score: Int? = null,
    @SerializedName("isComplete")
    val isComplete: Boolean = false
)

/**
 * 대화 메시지 모델
 */
data class ConversationMessage(
    @SerializedName("speaker")
    val speaker: String, // "user" or "ai"
    @SerializedName("text")
    val text: String,
    @SerializedName("timestamp")
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 음성 인식 요청 모델
 */
data class VoiceRecognitionRequest(
    @SerializedName("audioData")
    val audioData: String, // Base64 encoded audio
    @SerializedName("language")
    val language: String = "ko-KR"
)

/**
 * 음성 인식 응답 모델
 */
data class VoiceRecognitionResponse(
    @SerializedName("text")
    val text: String,
    @SerializedName("confidence")
    val confidence: Float
)

/**
 * 퀴즈 제출 요청 모델
 */
data class QuizSubmissionRequest(
    @SerializedName("assignmentId")
    val assignmentId: String,
    @SerializedName("studentId")
    val studentId: Int,
    @SerializedName("answers")
    val answers: List<QuizAnswer>,
    @SerializedName("timeSpent")
    val timeSpent: Long // milliseconds
)

/**
 * 퀴즈 답변 모델
 */
data class QuizAnswer(
    @SerializedName("questionId")
    val questionId: String,
    @SerializedName("selectedAnswer")
    val selectedAnswer: String,
    @SerializedName("isCorrect")
    val isCorrect: Boolean
)

/**
 * 퀴즈 제출 응답 모델
 */
data class QuizSubmissionResponse(
    @SerializedName("score")
    val score: Int,
    @SerializedName("totalQuestions")
    val totalQuestions: Int,
    @SerializedName("correctAnswers")
    val correctAnswers: Int,
    @SerializedName("feedback")
    val feedback: String? = null,
    @SerializedName("submissionId")
    val submissionId: String
)
