package com.example.voicetutor.data.network

import com.example.voicetutor.data.models.*
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
    suspend fun createAssignment(@Body assignment: CreateAssignmentRequest): Response<ApiResponse<AssignmentData>>
    
    @PUT("assignments/{id}/")
    suspend fun updateAssignment(@Path("id") id: Int, @Body assignment: UpdateAssignmentRequest): Response<ApiResponse<AssignmentData>>
    
    @DELETE("assignments/{id}/")
    suspend fun deleteAssignment(@Path("id") id: Int): Response<ApiResponse<Unit>>
    
    @POST("assignments/{id}/draft/")
    suspend fun saveAssignmentDraft(@Path("id") id: Int, @Body content: String): Response<ApiResponse<Unit>>
    
    @GET("assignments/{id}/results/")
    suspend fun getAssignmentResults(@Path("id") id: Int): Response<ApiResponse<List<StudentResult>>>
    
    // Student APIs
    @GET("students/")
    suspend fun getAllStudents(
        @Query("teacherId") teacherId: String? = null,
        @Query("classId") classId: String? = null
    ): Response<ApiResponse<List<Student>>>
    
    @GET("students/{id}/")
    suspend fun getStudentById(@Path("id") id: Int): Response<ApiResponse<Student>>
    
    @GET("students/{id}/assignments/")
    suspend fun getStudentAssignments(@Path("id") id: Int): Response<ApiResponse<List<AssignmentData>>>
    
    @GET("students/{id}/progress/")
    suspend fun getStudentProgress(@Path("id") id: Int): Response<ApiResponse<StudentProgress>>
    
    // Class APIs
    @GET("classes/")
    suspend fun getClasses(@Query("teacherId") teacherId: String): Response<ApiResponse<List<ClassData>>>
    
    @GET("classes/{id}/")
    suspend fun getClassById(@Path("id") id: Int): Response<ApiResponse<ClassData>>
    
    @GET("classes/{id}/students/")
    suspend fun getClassStudents(@Path("id") id: Int): Response<ApiResponse<List<Student>>>
    
    // Message APIs
    @POST("messages/send/")
    suspend fun sendMessage(@Body request: SendMessageRequest): Response<ApiResponse<SendMessageResponse>>
    
    @GET("messages/{classId}/")
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
    
    // Dashboard APIs
    @GET("dashboard/stats/")
    suspend fun getDashboardStats(
        @Query("teacherId") teacherId: String
    ): Response<ApiResponse<com.example.voicetutor.data.models.DashboardStats>>
    
    @GET("dashboard/recent-activities/")
    suspend fun getRecentActivities(
        @Query("teacherId") teacherId: String,
        @Query("limit") limit: Int = 5
    ): Response<ApiResponse<List<com.example.voicetutor.data.models.RecentActivity>>>
    
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
    
    // Attendance APIs
    @POST("attendance/record/")
    suspend fun recordAttendance(@Body request: AttendanceRecordRequest): Response<ApiResponse<AttendanceRecordResponse>>
    
    @POST("attendance/query/")
    suspend fun queryAttendance(@Body request: AttendanceQueryRequest): Response<ApiResponse<AttendanceQueryResponse>>
    
    @GET("attendance/summary/{studentId}/")
    suspend fun getStudentAttendanceSummary(@Path("studentId") studentId: Int): Response<ApiResponse<AttendanceSummary>>
    
    @GET("attendance/class/{classId}/")
    suspend fun getClassAttendance(@Path("classId") classId: Int, @Query("date") date: String): Response<ApiResponse<ClassAttendanceSummary>>
    
    // Student Edit APIs
    @PUT("students/{studentId}/")
    suspend fun editStudent(@Path("studentId") studentId: Int, @Body request: StudentEditRequest): Response<ApiResponse<StudentEditResponse>>
    
    @DELETE("students/{studentId}/")
    suspend fun deleteStudent(@Path("studentId") studentId: Int, @Body request: StudentDeleteRequest): Response<ApiResponse<StudentDeleteResponse>>
    
    @PUT("students/{studentId}/status/")
    suspend fun updateStudentStatus(@Path("studentId") studentId: Int, @Body request: StudentStatusRequest): Response<ApiResponse<StudentStatusResponse>>
    
    @PUT("students/{studentId}/password/")
    suspend fun resetStudentPassword(@Path("studentId") studentId: Int, @Body request: StudentPasswordResetRequest): Response<ApiResponse<StudentPasswordResetResponse>>
    
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
    val classId: Int,
    val dueDate: String,
    val type: String,
    val description: String?,
    val questions: List<QuestionData>?
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
