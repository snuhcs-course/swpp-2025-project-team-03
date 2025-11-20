package com.example.voicetutor.data.models

import com.google.gson.annotations.SerializedName

data class ProgressReportData(
    @SerializedName("totalStudents")
    val totalStudents: Int,
    @SerializedName("totalAssignments")
    val totalAssignments: Int,
    @SerializedName("completedAssignments")
    val completedAssignments: Int,
    @SerializedName("averageScore")
    val averageScore: Double,
    @SerializedName("classBreakdown")
    val classBreakdown: List<ClassProgress>,
)

data class ClassProgress(
    @SerializedName("classId")
    val classId: Int,
    @SerializedName("className")
    val className: String,
    @SerializedName("studentCount")
    val studentCount: Int,
    @SerializedName("completedAssignments")
    val completedAssignments: Int,
    @SerializedName("totalAssignments")
    val totalAssignments: Int,
)

data class QuestionResult(
    @SerializedName("questionNumber")
    val questionNumber: Int,
    @SerializedName("question")
    val question: String,
    @SerializedName("myAnswer")
    val myAnswer: String,
    @SerializedName("correctAnswer")
    val correctAnswer: String,
    @SerializedName("isCorrect")
    val isCorrect: Boolean,
    @SerializedName("confidence")
    val confidence: Float,
)

// 성취기준별 통계 데이터
data class AchievementStatistics(
    @SerializedName("total_questions")
    val totalQuestions: Int,
    @SerializedName("correct_questions")
    val correctQuestions: Int,
    @SerializedName("accuracy")
    val accuracy: Double,
    @SerializedName("content")
    val content: String,
)

// 성취기준 분석 리포트 데이터
data class CurriculumReportData(
    @SerializedName("total_questions")
    val totalQuestions: Int,
    @SerializedName("total_correct")
    val totalCorrect: Int,
    @SerializedName("overall_accuracy")
    val overallAccuracy: Double,
    @SerializedName("achievement_statistics")
    val achievementStatistics: Map<String, AchievementStatistics>,
)
