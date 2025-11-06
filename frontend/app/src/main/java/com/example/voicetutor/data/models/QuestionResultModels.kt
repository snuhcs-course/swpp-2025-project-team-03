package com.example.voicetutor.data.models

// 상세 결과 표시용 데이터 클래스 (top-level)
data class DetailedQuestionResult(
    val questionNumber: String,
    val question: String,
    val myAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val explanation: String? = null
)

// 기본 문항 + 꼬리 문항 그룹 (top-level)
data class QuestionGroup(
    val baseQuestion: DetailedQuestionResult,
    val tailQuestions: List<DetailedQuestionResult> = emptyList()
)