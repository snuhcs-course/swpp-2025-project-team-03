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
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/signup/")
    suspend fun signup(@Body request: SignupRequest): Response<LoginResponse>

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
) {
    class Builder {
        private var title: String? = null
        private var subject: String? = null
        private var class_id: Int? = null
        private var due_at: String? = null
        private var grade: String? = null
        private var description: String? = null
        private var total_questions: Int? = null

        fun title(value: String) = apply { title = value }
        fun subject(value: String) = apply { subject = value }
        fun classId(value: Int) = apply { class_id = value }
        fun dueAt(value: String) = apply { due_at = value }
        fun grade(value: String?) = apply { grade = value }
        fun description(value: String?) = apply { description = value }
        fun totalQuestions(value: Int?) = apply { total_questions = value }

        fun build(): CreateAssignmentRequest {
            require(title != null) { "title은 필수입니다." }
            require(subject != null) { "subject는 필수입니다." }
            require(class_id != null) { "class_id는 필수입니다." }
            require(due_at != null) { "due_at은 필수입니다." }

            return CreateAssignmentRequest(
                title = title!!,
                subject = subject!!,
                class_id = class_id!!,
                due_at = due_at!!,
                grade = grade,
                description = description,
                total_questions = total_questions,
            )
        }
    }

    companion object {
        fun builder(): Builder = Builder()
    }
}

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
) {
    class Builder {
        private var name: String? = null
        private var description: String? = null
        private var subject_name: String? = null
        private var teacher_id: Int? = null

        fun name(value: String) = apply { name = value }
        fun description(value: String?) = apply { description = value }
        fun subjectName(value: String) = apply { subject_name = value }
        fun teacherId(value: Int) = apply { teacher_id = value }

        fun build(): CreateClassRequest {
            require(name != null) { "name은 필수입니다." }
            require(subject_name != null) { "subject_name은 필수입니다." }
            require(teacher_id != null) { "teacher_id는 필수입니다." }

            return CreateClassRequest(
                name = name!!,
                description = description,
                subject_name = subject_name!!,
                teacher_id = teacher_id!!,
            )
        }
    }

    companion object {
        fun builder(): Builder = Builder()
    }
}

data class UpdateAssignmentRequest(
    val title: String? = null,
    val description: String? = null,
    @SerializedName("total_questions") val totalQuestions: Int? = null,
    @SerializedName("due_at") val dueAt: String? = null,
    val grade: String? = null,
    val subject: SubjectUpdateRequest? = null,
) {
    class Builder {
        private var title: String? = null
        private var description: String? = null
        private var totalQuestions: Int? = null
        private var dueAt: String? = null
        private var grade: String? = null
        private var subject: SubjectUpdateRequest? = null

        fun title(value: String?) = apply { title = value }
        fun description(value: String?) = apply { description = value }
        fun totalQuestions(value: Int?) = apply { totalQuestions = value }
        fun dueAt(value: String?) = apply { dueAt = value }
        fun grade(value: String?) = apply { grade = value }
        fun subject(value: SubjectUpdateRequest?) = apply { subject = value }

        fun build(): UpdateAssignmentRequest {
            require(
                title != null ||
                    description != null ||
                    totalQuestions != null ||
                    dueAt != null ||
                    grade != null ||
                    subject != null,
            ) { "수정할 필드를 최소 1개 이상 지정해야 합니다." }

            return UpdateAssignmentRequest(
                title = title,
                description = description,
                totalQuestions = totalQuestions,
                dueAt = dueAt,
                grade = grade,
                subject = subject,
            )
        }
    }

    companion object {
        fun builder(): Builder = Builder()
    }
}

data class SubjectUpdateRequest(
    val id: Int? = null,
    val name: String? = null,
    val code: String? = null,
)
