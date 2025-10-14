package com.example.voicetutor.data.models

import com.google.gson.annotations.SerializedName

data class ProgressReportData(
    @SerializedName("period")
    val period: String,
    @SerializedName("totalStudents")
    val totalStudents: Int,
    @SerializedName("activeStudents")
    val activeStudents: Int,
    @SerializedName("totalAssignments")
    val totalAssignments: Int,
    @SerializedName("completedAssignments")
    val completedAssignments: Int,
    @SerializedName("averageScore")
    val averageScore: Double,
    @SerializedName("classBreakdown")
    val classBreakdown: List<ClassProgress>,
    @SerializedName("subjectBreakdown")
    val subjectBreakdown: List<SubjectBreakdown>,
    @SerializedName("weeklyActivity")
    val weeklyActivity: List<WeeklyActivity>
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
    @SerializedName("averageScore")
    val averageScore: Double
)

data class SubjectBreakdown(
    @SerializedName("subject")
    val subject: String,
    @SerializedName("assignments")
    val assignments: Int,
    @SerializedName("averageScore")
    val averageScore: Double,
    @SerializedName("completionRate")
    val completionRate: Double
)

data class WeeklyActivity(
    @SerializedName("week")
    val week: String,
    @SerializedName("assignmentsCompleted")
    val assignmentsCompleted: Int,
    @SerializedName("newStudents")
    val newStudents: Int,
    @SerializedName("averageScore")
    val averageScore: Double
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
    @SerializedName("questionType")
    val questionType: QuestionType
)
