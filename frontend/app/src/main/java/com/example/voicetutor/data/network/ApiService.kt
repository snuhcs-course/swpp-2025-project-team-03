package com.example.voicetutor.data.network

import com.example.voicetutor.data.models.*
import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // Auth APIs
    @POST("auth/login/")
    suspend fun login(@Body request: com.example.voicetutor.data.models.LoginRequest): Response<com.example.voicetutor.data.models.LoginResponse>
    
    @POST("auth/signup/")
    suspend fun signup(@Body request: com.example.voicetutor.data.models.SignupRequest): Response<com.example.voicetutor.data.models.LoginResponse>
    
    @POST("auth/logout/")
    suspend fun logout(): Response<ApiResponse<Unit>>
    
    // Assignment APIs
    @GET("assignments/")
    suspend fun getAllAssignments(
        @Query("teacherId") teacherId: String? = null,
        @Query("classId") classId: String? = null,
        @Query("status") status: String? = null
    ): Response<ApiResponse<List<AssignmentData>>>
    
    @GET("assignments/{id}/")
    suspend fun getAssignmentById(@Path("id") id: Int): Response<ApiResponse<AssignmentData>>
  
    @POST("assignments/create/")
    suspend fun createAssignment(@Body assignment: CreateAssignmentRequest): Response<ApiResponse<CreateAssignmentResponse>>
    
    @PUT("assignments/{id}/")
    suspend fun updateAssignment(@Path("id") id: Int, @Body assignment: UpdateAssignmentRequest): Response<ApiResponse<AssignmentData>>
    
    @DELETE("assignments/{id}/")
    suspend fun deleteAssignment(@Path("id") id: Int): Response<ApiResponse<Unit>>
    
    @POST("assignments/{id}/draft/")
    suspend fun saveAssignmentDraft(@Path("id") id: Int, @Body content: String): Response<ApiResponse<Unit>>
    
    @GET("assignments/{id}/results/")
    suspend fun getAssignmentResults(@Path("id") id: Int): Response<ApiResponse<List<StudentResult>>>
    
    // Student APIs (Backend: /api/courses/students/)
    @GET("courses/students/")
    suspend fun getAllStudents(
        @Query("teacherId") teacherId: String? = null,
        @Query("classId") classId: String? = null
    ): Response<ApiResponse<List<Student>>>
    
    @GET("courses/students/{id}/")
    suspend fun getStudentById(@Path("id") id: Int): Response<ApiResponse<Student>>
    
    @GET("courses/students/{id}/assignments/")
    suspend fun getStudentAssignments(@Path("id") id: Int): Response<ApiResponse<List<AssignmentData>>>
    
    // Personal Assignment APIs (Backend: /api/personal_assignments/)
    @GET("personal_assignments/")
    suspend fun getPersonalAssignments(
        @Query("student_id") studentId: Int? = null,
        @Query("assignment_id") assignmentId: Int? = null
    ): Response<ApiResponse<List<PersonalAssignmentData>>>
    
    @GET("personal_assignments/{id}/questions/")
    suspend fun getPersonalAssignmentQuestions(@Path("id") id: Int): Response<ApiResponse<List<PersonalAssignmentQuestion>>>
    
    @GET("personal_assignments/{id}/statistics/")
    suspend fun getPersonalAssignmentStatistics(@Path("id") id: Int): Response<ApiResponse<PersonalAssignmentStatistics>>
    
    @Multipart
    @POST("personal_assignments/answer/")
    suspend fun submitAnswer(
        @Part studentId: MultipartBody.Part,
        @Part questionId: MultipartBody.Part,
        @Part audioFile: MultipartBody.Part
    ): Response<ApiResponse<AnswerSubmissionResponse>>
    
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
    
    // Message APIs (Backend: /api/feedbacks/messages/)
    @POST("feedbacks/messages/send/")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<ApiResponse<SendMessageResponse>>
    
    @GET("feedbacks/messages/{classId}/")
    suspend fun getClassMessages(@Path("classId") classId: Int): Response<ApiResponse<List<MessageData>>>
    
    // Progress Report APIs
    @GET("reports/progress/")
    suspend fun getProgressReport(
        @Query("teacherId") teacherId: String,
        @Query("classId") classId: String? = null,
        @Query("period") period: String = "week"
    ): Response<ApiResponse<ProgressReportData>>
    
    // Quiz/Assignment Submission APIs
    @POST("assignments/{id}/submit/")
    suspend fun submitAssignment(
        @Path("id") id: Int,
        @Body submission: AssignmentSubmissionRequest
    ): Response<ApiResponse<AssignmentSubmissionResult>>
    
    @GET("assignments/{id}/questions/")
    suspend fun getAssignmentQuestions(@Path("id") id: Int): Response<ApiResponse<List<QuestionData>>>
    
    @GET("assignments/{assignment_id}/s3-check/")
    suspend fun checkS3Upload(@Path("assignment_id") assignmentId: Int): Response<ApiResponse<S3UploadStatus>>
    
    // Dashboard APIs (Backend: /api/feedbacks/dashboard/stats/)
    @GET("feedbacks/dashboard/stats/")
    suspend fun getDashboardStats(
        @Query("teacherId") teacherId: String
    ): Response<ApiResponse<com.example.voicetutor.data.models.DashboardStats>>
    
    // AI APIs
    @POST("ai/conversation/")
    suspend fun sendAIMessage(@Body request: AIConversationRequest): Response<ApiResponse<AIConversationResponse>>
    
    @POST("ai/voice-recognition/")
    suspend fun recognizeVoice(@Body request: VoiceRecognitionRequest): Response<ApiResponse<VoiceRecognitionResponse>>
    
    // Quiz APIs
    @POST("quiz/submit/")
    suspend fun submitQuiz(@Body request: QuizSubmissionRequest): Response<ApiResponse<QuizSubmissionResponse>>
    
    @GET("messages/")
    suspend fun getMessages(
        @Query("userId") userId: Int,
        @Query("messageType") messageType: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<ApiResponse<MessageListResponse>>
    
    // Analysis APIs
    @POST("analysis/student/")
    suspend fun getStudentAnalysis(@Body request: AnalysisRequest): Response<ApiResponse<AnalysisResponse>>
    
    @POST("analysis/class/")
    suspend fun getClassAnalysis(@Body request: AnalysisRequest): Response<ApiResponse<AnalysisResponse>>
    
    @POST("analysis/subject/")
    suspend fun getSubjectAnalysis(@Body request: AnalysisRequest): Response<ApiResponse<AnalysisResponse>>
    
    // Student Edit APIs
    @PUT("students/{studentId}/")
    suspend fun editStudent(@Path("studentId") studentId: Int, @Body request: StudentEditRequest): Response<ApiResponse<StudentEditResponse>>
    
    @DELETE("students/{studentId}/")
    suspend fun deleteStudent(@Path("studentId") studentId: Int, @Body request: StudentDeleteRequest): Response<ApiResponse<StudentDeleteResponse>>
    
    @PUT("students/{studentId}/class/")
    suspend fun changeStudentClass(@Path("studentId") studentId: Int, @Body request: StudentClassChangeRequest): Response<ApiResponse<StudentClassChangeResponse>>
}

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?,
    val error: String?
)

data class CreateAssignmentRequest(
    val title: String,
    val subject: String,
    @SerializedName("class_id") val class_id: Int,
    @SerializedName("due_at") val due_at: String,
    val grade: String?,
    val type: String,
    val description: String?,
    val questions: List<QuestionData>?
)

data class CreateAssignmentResponse(
    @SerializedName("assignment_id") val assignment_id: Int,
    @SerializedName("material_id") val material_id: Int,
    @SerializedName("s3_key") val s3_key: String,
    @SerializedName("upload_url") val upload_url: String
)

data class S3UploadStatus(
    @SerializedName("assignment_id") val assignment_id: Int,
    @SerializedName("material_id") val material_id: Int,
    @SerializedName("s3_key") val s3_key: String,
    @SerializedName("file_exists") val file_exists: Boolean,
    @SerializedName("file_size") val file_size: Long?,
    @SerializedName("content_type") val content_type: String?,
    @SerializedName("last_modified") val last_modified: String?,
    @SerializedName("bucket") val bucket: String
)

data class CreateClassRequest(
    val name: String,
    val description: String?,
    val subject_name: String,
    val teacher_id: Int,
    val start_date: String,
    val end_date: String
)

data class UpdateAssignmentRequest(
    val title: String?,
    val subject: String?,
    val classId: Int?,
    val dueDate: String?,
    val type: String?,
    val description: String?,
    val questions: List<QuestionData>?
)


data class AssignmentSubmissionRequest(
    val studentId: Int,
    val answers: List<AnswerSubmission>
)

data class AnswerSubmission(
    val questionId: Int,
    val answer: String,
    val audioFile: String?,
    val confidence: Float?
)

data class AssignmentSubmissionResult(
    val submissionId: Int,
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val feedback: List<QuestionFeedback>
)

data class QuestionFeedback(
    val questionId: Int,
    val isCorrect: Boolean,
    val studentAnswer: String,
    val correctAnswer: String,
    val explanation: String?,
    val confidence: Float,
    val pronunciationScore: Float?
)
