package com.example.voicetutor.data.network

import com.example.voicetutor.data.models.*
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Health check API
    @GET("core/health/")
    suspend fun healthCheck(): Response<ApiResponse<String>>

    // Auth APIs
    @POST("auth/login/")
    suspend fun login(@Body request: com.example.voicetutor.data.models.LoginRequest): Response<com.example.voicetutor.data.models.LoginResponse>

    @POST("auth/signup/")
    suspend fun signup(@Body request: com.example.voicetutor.data.models.SignupRequest): Response<com.example.voicetutor.data.models.LoginResponse>

    @POST("auth/logout/")
    suspend fun logout(): Response<ApiResponse<Unit>>

    @DELETE("auth/account/")
    suspend fun deleteAccount(): Response<ApiResponse<Unit>>

    // Assignment APIs
    @GET("assignments/")
    suspend fun getAllAssignments(
        @Query("teacherId") teacherId: String? = null,
        @Query("classId") classId: String? = null,
        @Query("status") status: String? = null,
    ): Response<ApiResponse<List<AssignmentData>>>

    @GET("assignments/{id}/")
    suspend fun getAssignmentById(@Path("id") id: Int): Response<ApiResponse<AssignmentData>>

    @POST("assignments/create/")
    suspend fun createAssignment(@Body assignment: CreateAssignmentRequest): Response<ApiResponse<CreateAssignmentResponse>>

    @PUT("assignments/{id}/")
    suspend fun updateAssignment(@Path("id") id: Int, @Body assignment: UpdateAssignmentRequest): Response<ApiResponse<AssignmentData>>

    @DELETE("assignments/{id}/")
    suspend fun deleteAssignment(@Path("id") id: Int): Response<ApiResponse<Unit>>

    @GET("assignments/{id}/results/")
    suspend fun getAssignmentResult(@Path("id") id: Int): Response<ApiResponse<AssignmentResultData>>

    // removed: saveAssignmentDraft

    // Student APIs (Backend: /api/courses/students/)
    @GET("courses/students/")
    suspend fun getAllStudents(
        @Query("teacherId") teacherId: String? = null,
        @Query("classId") classId: String? = null,
    ): Response<ApiResponse<List<Student>>>

    @GET("courses/students/{id}/")
    suspend fun getStudentById(@Path("id") id: Int): Response<ApiResponse<Student>>

    @GET("courses/students/{id}/assignments/")
    suspend fun getStudentAssignments(@Path("id") id: Int): Response<ApiResponse<List<AssignmentData>>>

    // Personal Assignment APIs (Backend: /api/personal_assignments/)
    @GET("personal_assignments/")
    suspend fun getPersonalAssignments(
        @Query("student_id") studentId: Int? = null,
        @Query("assignment_id") assignmentId: Int? = null,
    ): Response<ApiResponse<List<PersonalAssignmentData>>>

    @GET("personal_assignments/{id}/questions/")
    suspend fun getPersonalAssignmentQuestions(@Path("id") id: Int): Response<ApiResponse<List<PersonalAssignmentQuestion>>>

    @GET("personal_assignments/{id}/statistics/")
    suspend fun getPersonalAssignmentStatistics(@Path("id") id: Int): Response<ApiResponse<PersonalAssignmentStatistics>>

    // Recent personal assignment for a student (used when entering assignment without id)
    @GET("personal_assignments/recentanswer/")
    suspend fun getRecentPersonalAssignment(
        @Query("student_id") studentId: Int,
    ): Response<ApiResponse<RecentAnswerData>>

    @Multipart
    @POST("personal_assignments/answer/")
    suspend fun submitAnswer(
        @Query("personal_assignment_id") personalAssignmentId: Int,
        @Part studentId: MultipartBody.Part,
        @Part questionId: MultipartBody.Part,
        @Part audioFile: MultipartBody.Part,
    ): Response<ApiResponse<AnswerSubmissionResponse>>

    @GET("personal_assignments/answer/")
    suspend fun getNextQuestion(
        @Query("personal_assignment_id") personalAssignmentId: Int,
    ): Response<ApiResponse<PersonalAssignmentQuestion>>

    @POST("personal_assignments/{id}/complete/")
    suspend fun completePersonalAssignment(@Path("id") id: Int): Response<ApiResponse<Unit>>

    @GET("personal_assignments/{id}/correctness/")
    suspend fun getAssignmentCorrectness(@Path("id") id: Int): Response<ApiResponse<List<AssignmentCorrectnessItem>>>

    @GET("courses/students/{id}/progress/")
    suspend fun getStudentProgress(@Path("id") id: Int): Response<ApiResponse<StudentProgress>>

    // Class APIs (Backend: /api/courses/classes/)
    @GET("courses/classes/")
    suspend fun getClasses(@Query("teacherId") teacherId: String): Response<ApiResponse<List<ClassData>>>

    @POST("courses/classes/")
    suspend fun createClass(@Body request: CreateClassRequest): Response<ApiResponse<ClassData>>

    @GET("courses/classes/{id}/")
    suspend fun getClassById(@Path("id") id: Int): Response<ApiResponse<ClassData>>

    @GET("courses/classes/{id}/students/")
    suspend fun getClassStudents(@Path("id") id: Int): Response<ApiResponse<List<Student>>>

    // Student's enrolled classes (Backend: GET /api/courses/students/{id}/classes/)
    @GET("courses/students/{id}/classes/")
    suspend fun getStudentClasses(
        @Path("id") id: Int,
    ): Response<ApiResponse<List<ClassInfo>>>

    // Enroll student to class (Backend: PUT /api/courses/classes/{id}/students/)
    @PUT("courses/classes/{id}/students/")
    suspend fun enrollStudentToClass(
        @Path("id") id: Int,
        @Query("studentId") studentId: Int,
    ): Response<ApiResponse<EnrollmentData>>

    // Remove student from class (Backend: DELETE /api/courses/classes/{id}/students/{student_id}/)
    @DELETE("courses/classes/{id}/students/{student_id}/")
    suspend fun removeStudentFromClass(
        @Path("id") id: Int,
        @Path("student_id") student_id: Int,
    ): Response<ApiResponse<Unit>>

    // Class Students Statistics API
    @GET("courses/classes/{classId}/students-statistics/")
    suspend fun getClassStudentsStatistics(
        @Path("classId") classId: Int,
    ): Response<ApiResponse<ClassStudentsStatistics>>

    // Progress Report APIs
    @GET("reports/progress/")
    suspend fun getProgressReport(
        @Query("teacherId") teacherId: String,
        @Query("classId") classId: String? = null,
        @Query("period") period: String = "week",
    ): Response<ApiResponse<ProgressReportData>>

    // Curriculum Report API (선생님용 학생 리포트)
    @GET("reports/{class_id}/{student_id}/")
    suspend fun getCurriculumReport(
        @Path("class_id") classId: Int,
        @Path("student_id") studentId: Int,
    ): Response<ApiResponse<com.example.voicetutor.data.models.CurriculumReportData>>

    // Quiz/Assignment Submission APIs
    @POST("assignments/{id}/submit/")
    suspend fun submitAssignment(
        @Path("id") id: Int,
        @Body submission: AssignmentSubmissionRequest,
    ): Response<ApiResponse<AssignmentSubmissionResult>>

    // removed: getAssignmentQuestions

    @GET("assignments/{assignment_id}/s3-check/")
    suspend fun checkS3Upload(@Path("assignment_id") assignmentId: Int): Response<ApiResponse<S3UploadStatus>>

    // Questions - generate base questions after PDF upload
    // 백엔드는 ApiResponse 형식이 아닌 직접 JSON 응답을 반환함
    @POST("questions/create/")
    suspend fun createQuestions(@Body request: QuestionCreateRequest): Response<ResponseBody>

    // Dashboard APIs (Backend: /api/assignments/teacher-dashboard-stats/)
    @GET("assignments/teacher-dashboard-stats/")
    suspend fun getDashboardStats(
        @Query("teacherId") teacherId: String,
    ): Response<ApiResponse<com.example.voicetutor.data.models.DashboardStats>>

    // AI/Quiz APIs removed
}

// Minimal response model for recent personal assignment lookup
data class RecentAnswerData(
    @SerializedName("personal_assignment_id") val personalAssignmentId: Int,
)

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val error: String?,
)

data class CreateAssignmentRequest(
    val title: String,
    val subject: String,
    @SerializedName("class_id") val class_id: Int,
    @SerializedName("due_at") val due_at: String,
    val grade: String?,
    val description: String?,
    @SerializedName("total_questions") val total_questions: Int? = null,
)

data class CreateAssignmentResponse(
    @SerializedName("assignment_id") val assignment_id: Int,
    @SerializedName("material_id") val material_id: Int,
    @SerializedName("s3_key") val s3_key: String,
    @SerializedName("upload_url") val upload_url: String,
)

data class S3UploadStatus(
    @SerializedName("assignment_id") val assignment_id: Int,
    @SerializedName("material_id") val material_id: Int,
    @SerializedName("s3_key") val s3_key: String,
    @SerializedName("file_exists") val file_exists: Boolean,
    @SerializedName("file_size") val file_size: Long?,
    @SerializedName("content_type") val content_type: String?,
    @SerializedName("last_modified") val last_modified: String?,
    @SerializedName("bucket") val bucket: String,
)

// Request body to trigger question generation based on uploaded PDF
data class QuestionCreateRequest(
    @SerializedName("assignment_id") val assignment_id: Int,
    @SerializedName("material_id") val material_id: Int,
    @SerializedName("total_number") val total_number: Int,
)

data class CreateClassRequest(
    val name: String,
    val description: String?,
    val subject_name: String,
    val teacher_id: Int,
)

data class UpdateAssignmentRequest(
    val title: String? = null,
    val description: String? = null,
    @SerializedName("total_questions") val totalQuestions: Int? = null,
    @SerializedName("due_at") val dueAt: String? = null,
    val grade: String? = null,
    val subject: SubjectUpdateRequest? = null,
)

data class SubjectUpdateRequest(
    val id: Int? = null,
    val name: String? = null,
    val code: String? = null,
)

data class AssignmentSubmissionRequest(
    val studentId: Int,
    val answers: List<AnswerSubmission>,
)

data class AnswerSubmission(
    val questionId: Int,
    val answer: String,
    val audioFile: String?,
    val confidence: Float?,
)

data class AssignmentSubmissionResult(
    val submissionId: Int,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val feedback: List<QuestionFeedback>,
)

data class QuestionFeedback(
    val questionId: Int,
    val isCorrect: Boolean,
    val studentAnswer: String,
    val correctAnswer: String,
    val explanation: String?,
    val confidence: Float,
    val pronunciationScore: Float?,
)
