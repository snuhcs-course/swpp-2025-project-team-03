package com.example.voicetutor.data.models

import com.google.gson.annotations.SerializedName

/**
 * 학습 분석 데이터 모델
 */
data class LearningAnalysis(
    @SerializedName("studentId")
    val studentId: Int,
    @SerializedName("studentName")
    val studentName: String,
    @SerializedName("totalAssignments")
    val totalAssignments: Int,
    @SerializedName("completedAssignments")
    val completedAssignments: Int,
    @SerializedName("averageScore")
    val averageScore: Float,
    @SerializedName("improvementRate")
    val improvementRate: Float,
    @SerializedName("accuracyRate")
    val accuracyRate: Float,
    @SerializedName("studyTime")
    val studyTime: Long, // minutes
    @SerializedName("strengths")
    val strengths: List<String>,
    @SerializedName("weaknesses")
    val weaknesses: List<String>,
    @SerializedName("recommendations")
    val recommendations: List<String>
)

/**
 * 과목별 분석 데이터 모델
 */
data class SubjectAnalysis(
    @SerializedName("subject")
    val subject: String,
    @SerializedName("totalQuestions")
    val totalQuestions: Int,
    @SerializedName("correctAnswers")
    val correctAnswers: Int,
    @SerializedName("accuracyRate")
    val accuracyRate: Float,
    @SerializedName("averageTime")
    val averageTime: Float, // seconds per question
    @SerializedName("difficultyLevel")
    val difficultyLevel: String,
    @SerializedName("progressTrend")
    val progressTrend: List<ProgressData>
)

/**
 * 진행률 데이터 모델
 */
data class ProgressData(
    @SerializedName("date")
    val date: String,
    @SerializedName("score")
    val score: Float,
    @SerializedName("timeSpent")
    val timeSpent: Long
)

/**
 * 클래스 전체 분석 데이터 모델
 */
data class ClassAnalysis(
    @SerializedName("classId")
    val classId: Int,
    @SerializedName("className")
    val className: String,
    @SerializedName("totalStudents")
    val totalStudents: Int,
    @SerializedName("averageScore")
    val averageScore: Float,
    @SerializedName("completionRate")
    val completionRate: Float,
    @SerializedName("topPerformers")
    val topPerformers: List<StudentPerformance>,
    @SerializedName("needsAttention")
    val needsAttention: List<StudentPerformance>,
    @SerializedName("subjectBreakdown")
    val subjectBreakdown: List<SubjectAnalysis>
)

/**
 * 학생 성과 데이터 모델
 */
data class StudentPerformance(
    @SerializedName("studentId")
    val studentId: Int,
    @SerializedName("studentName")
    val studentName: String,
    @SerializedName("score")
    val score: Float,
    @SerializedName("rank")
    val rank: Int,
    @SerializedName("improvement")
    val improvement: Float
)

/**
 * 분석 요청 모델
 */
data class AnalysisRequest(
    @SerializedName("studentId")
    val studentId: Int? = null,
    @SerializedName("classId")
    val classId: Int? = null,
    @SerializedName("subject")
    val subject: String? = null,
    @SerializedName("dateRange")
    val dateRange: DateRange? = null
)

/**
 * 날짜 범위 모델
 */
data class DateRange(
    @SerializedName("startDate")
    val startDate: String,
    @SerializedName("endDate")
    val endDate: String
)

/**
 * 분석 응답 모델
 */
data class AnalysisResponse(
    @SerializedName("learningAnalysis")
    val learningAnalysis: LearningAnalysis? = null,
    @SerializedName("classAnalysis")
    val classAnalysis: ClassAnalysis? = null,
    @SerializedName("subjectAnalysis")
    val subjectAnalysis: SubjectAnalysis? = null
)
