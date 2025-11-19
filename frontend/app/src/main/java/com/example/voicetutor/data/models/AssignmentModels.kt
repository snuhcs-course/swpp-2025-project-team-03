package com.example.voicetutor.data.models

import com.google.gson.annotations.SerializedName

enum class AssignmentStatus {
    @SerializedName("IN_PROGRESS")
    IN_PROGRESS,

    @SerializedName("COMPLETED")
    COMPLETED,

    @SerializedName("DRAFT")
    DRAFT,
}

enum class AssignmentFilter {
    @SerializedName("ALL")
    ALL,

    @SerializedName("IN_PROGRESS")
    IN_PROGRESS,

    @SerializedName("COMPLETED")
    COMPLETED,
}

// Personal Assignment 상태
enum class PersonalAssignmentStatus {
    @SerializedName("NOT_STARTED")
    NOT_STARTED,

    @SerializedName("IN_PROGRESS")
    IN_PROGRESS,

    @SerializedName("SUBMITTED")
    SUBMITTED,
}

// Personal Assignment용 필터
enum class PersonalAssignmentFilter {
    ALL, // 모든 과제
    NOT_STARTED, // 시작 안함
    IN_PROGRESS, // 진행 중
    SUBMITTED, // 제출 완료
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
    @SerializedName("due_at")
    val dueAt: String,
    @SerializedName("course_class")
    val courseClass: CourseClass,
    @SerializedName("materials")
    val materials: List<Material>? = null,
    @SerializedName("grade")
    val grade: String? = null,
    // Personal Assignment 관련 정보 (변환 시 추가)
    val personalAssignmentStatus: PersonalAssignmentStatus? = null,
    val solvedNum: Int? = null,
    val personalAssignmentId: Int? = null, // PersonalAssignment ID 추가
    val submittedAt: String? = null, // 제출 일시
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
    @SerializedName("student_count")
    val studentCount: Int,
    @SerializedName("created_at")
    val createdAt: String,
)

data class Subject(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("code")
    val code: String? = null,
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
    val createdAt: String,
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
    val explanation: String? = null,
)

// Personal Assignment 데이터 모델
data class PersonalAssignmentData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("student")
    val student: StudentInfo,
    @SerializedName("assignment")
    val assignment: PersonalAssignmentInfo,
    @SerializedName("status")
    val status: PersonalAssignmentStatus,
    @SerializedName("solved_num")
    val solvedNum: Int,
    @SerializedName("started_at")
    val startedAt: String? = null,
    @SerializedName("submitted_at")
    val submittedAt: String? = null,
)

data class StudentInfo(
    @SerializedName("id")
    val id: Int,
    @SerializedName("display_name")
    val displayName: String,
    @SerializedName("email")
    val email: String,
)

data class PersonalAssignmentInfo(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("total_questions")
    val totalQuestions: Int,
    @SerializedName("due_at")
    val dueAt: String,
    @SerializedName("grade")
    val grade: String,
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
    @SerializedName("startedAt")
    val startedAt: String? = null,
    @SerializedName("submittedAt")
    val submittedAt: String,
    @SerializedName("answers")
    val answers: List<String>,
    @SerializedName("detailedAnswers")
    val detailedAnswers: List<DetailedAnswer>,
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
    val responseTime: String,
)

data class AssignmentResultData(
    @SerializedName("submitted_students")
    val submittedStudents: Int? = null,
    @SerializedName("total_students")
    val totalStudents: Int? = null,
    @SerializedName("average_score")
    val averageScore: Double? = null,
    @SerializedName("completion_rate")
    val completionRate: Double? = null,
)

// Personal Assignment API용 데이터 모델들
data class PersonalAssignmentQuestion(
    @SerializedName("id")
    val id: Int,
    @SerializedName("number")
    val number: String, // Int에서 String으로 변경 (예: "2-2")
    @SerializedName("question")
    val question: String,
    @SerializedName("answer")
    val answer: String,
    @SerializedName("explanation")
    val explanation: String,
    @SerializedName("difficulty")
    val difficulty: String,
    @SerializedName("is_processing")
    val isProcessing: Boolean = false,
)

data class PersonalAssignmentStatistics(
    @SerializedName("total_questions")
    val totalQuestions: Int,
    @SerializedName("answered_questions")
    val answeredQuestions: Int,
    @SerializedName("correct_answers")
    val correctAnswers: Int,
    @SerializedName("accuracy")
    val accuracy: Float,
    @SerializedName("total_problem")
    val totalProblem: Int,
    @SerializedName("solved_problem")
    val solvedProblem: Int,
    @SerializedName("progress")
    val progress: Float,
    @SerializedName("average_score")
    val averageScore: Float,
)

data class TailQuestion(
    @SerializedName("id")
    val id: Int,
    @SerializedName("number")
    val number: String,
    @SerializedName("question")
    val question: String,
    @SerializedName("answer")
    val answer: String,
    @SerializedName("explanation")
    val explanation: String,
    @SerializedName("difficulty")
    val difficulty: String,
)

data class AnswerSubmissionResponse(
    @SerializedName("is_correct")
    val isCorrect: Boolean,
    @SerializedName("number_str")
    val numberStr: String? = null,
    @SerializedName("tail_question")
    val tailQuestion: TailQuestion?,
)

// 음성 녹음을 위한 데이터 클래스
data class AudioRecordingState(
    val isRecording: Boolean = false,
    val recordingDuration: Int = 0,
    val audioFilePath: String? = null,
    val isProcessing: Boolean = false,
)

data class AssignmentCorrectnessItem(
    @SerializedName("question_content")
    val questionContent: String,
    @SerializedName("question_model_answer")
    val questionModelAnswer: String,
    @SerializedName("student_answer")
    val studentAnswer: String,
    @SerializedName("is_correct")
    val isCorrect: Boolean,
    @SerializedName("answered_at")
    val answeredAt: String,
    @SerializedName("question_number")
    val questionNum: String,
    @SerializedName("explanation")
    val explanation: String,
)
