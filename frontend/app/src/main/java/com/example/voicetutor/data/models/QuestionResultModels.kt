package com.example.voicetutor.data.models

data class DetailedQuestionResult(
    val questionNumber: String,
    val question: String,
    val myAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val explanation: String? = null,
)

data class QuestionGroup(
    val baseQuestion: DetailedQuestionResult,
    val tailQuestions: List<DetailedQuestionResult> = emptyList(),
)
