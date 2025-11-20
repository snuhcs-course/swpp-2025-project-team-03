package com.example.voicetutor.data.network

import com.example.voicetutor.data.models.*
import kotlinx.coroutines.delay
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response

/**
 * Simple in-memory ApiService implementation used in instrumentation tests.
 *
 * The goal is to return deterministic data without performing any network calls so that
 * Compose screens depending on ViewModels/Repositories can render normally.
 */
class FakeApiService : ApiService {

    private val subject = Subject(id = 1, name = "수학", code = "MATH")

    private val courseClass = CourseClass(
        id = 1,
        name = "수학 A반",
        description = "기초 수학 수업",
        subject = subject,
        teacherName = "김선생님",

        studentCount = 25,
        createdAt = "2024-01-01T00:00:00Z",
    )

    private val classData = ClassData(
        id = 1,
        name = "수학 A반",
        subject = subject,
        description = "기초 수학 수업",
        teacherId = 2,
        teacherName = "김선생님",
        studentCount = 25,
        studentCountAlt = 25,
        createdAt = "2024-01-01T00:00:00Z",

    )

    private val assignmentData = AssignmentData(
        id = 1,
        title = "1단원 복습 과제",
        description = "기초 개념을 복습하는 과제입니다.",
        totalQuestions = 10,
        createdAt = "2024-01-01T09:00:00Z",

        dueAt = "2024-02-01T23:59:59Z",
        courseClass = courseClass,
        materials = listOf(
            Material(
                id = 1,
                kind = "PDF",
                s3Key = "assignments/1/material.pdf",
                bytes = 1024,
                createdAt = "2024-01-01T09:00:00Z",
            ),
        ),
        grade = "중학교 1학년",
    )

    private val studentResult = StudentResult(
        studentId = "S1",
        name = "홍길동",
        score = 85,
        confidenceScore = 80,
        status = "완료",
        startedAt = "2024-01-02T09:00:00Z",
        submittedAt = "2024-01-02T10:00:00Z",
        answers = listOf("A", "B", "C"),
        detailedAnswers = listOf(
            DetailedAnswer(
                questionNumber = 1,
                question = "1번 문제",
                studentAnswer = "A",
                correctAnswer = "A",
                isCorrect = true,
                confidenceScore = 90,
                responseTime = "15s",
            ),
        ),
    )

    private val personalAssignmentQuestion = PersonalAssignmentQuestion(
        id = 1,
        number = "1",
        question = "지구는 몇 개의 위성을 가지고 있나요?",
        answer = "1개",
        explanation = "지구의 유일한 자연 위성은 달입니다.",
        difficulty = "EASY",
    )

    var personalAssignmentQuestionsResponses: List<PersonalAssignmentQuestion> = listOf(personalAssignmentQuestion)
    var nextQuestionQueue: MutableList<PersonalAssignmentQuestion> = mutableListOf(personalAssignmentQuestion)
    var shouldReturnNoMoreQuestions: Boolean = false
    var nextQuestionErrorMessage: String = "No more questions"
    var answerSubmissionResponse: AnswerSubmissionResponse =
        AnswerSubmissionResponse(isCorrect = true, numberStr = "1", tailQuestion = null)
    var answerSubmissionResponseQueue: MutableList<AnswerSubmissionResponse>? = null

    private val personalAssignmentStatistics = PersonalAssignmentStatistics(
        totalQuestions = 10,
        answeredQuestions = 5,
        correctAnswers = 4,
        accuracy = 0.8f,
        totalProblem = 10,
        solvedProblem = 5,
        progress = 0.5f,
        averageScore = 85f,
    )

    val personalAssignmentData = PersonalAssignmentData(
        id = 1,
        student = StudentInfo(
            id = 1,
            displayName = "홍길동",
            email = "student1@school.com",
        ),
        assignment = PersonalAssignmentInfo(
            id = assignmentData.id,
            title = assignmentData.title,
            description = assignmentData.description ?: "",
            totalQuestions = assignmentData.totalQuestions,
            dueAt = assignmentData.dueAt,
            grade = assignmentData.grade ?: "중학교 1학년",
        ),
        status = PersonalAssignmentStatus.IN_PROGRESS,
        solvedNum = 5,
        startedAt = "2024-01-02T09:00:00Z",
        submittedAt = null,
    )

    private val student = Student(
        id = 1,
        name = "홍길동",
        email = "student1@school.com",
        role = UserRole.STUDENT,
    )

    private val allStudentsStudent = AllStudentsStudent(
        id = 1,
        name = "홍길동",
        email = "student1@school.com",
        role = UserRole.STUDENT,
    )

    private val studentProgress = StudentProgress(
        studentId = 1,
        totalAssignments = 10,
        completedAssignments = 7,
        averageScore = 88.5,
        weeklyProgress = listOf(
            WeeklyProgress(week = "2024-W01", assignmentsCompleted = 2, averageScore = 90.0),
            WeeklyProgress(week = "2024-W02", assignmentsCompleted = 3, averageScore = 85.0),
        ),
        subjectBreakdown = listOf(
            SubjectProgress(subject = "수학", completedAssignments = 4, totalAssignments = 5, averageScore = 90.0),
            SubjectProgress(subject = "과학", completedAssignments = 3, totalAssignments = 5, averageScore = 86.0),
        ),
    )

    private val assignmentStatistics = AssignmentResultData(
        submittedStudents = 18,
        totalStudents = 25,
        averageScore = 82.0,
        completionRate = 0.72,
    )

    private val dashboardStats = DashboardStats(
        totalAssignments = 8,
        totalStudents = 25,
        totalClasses = 3,
        completedAssignments = 5,
        inProgressAssignments = 3,
    )

    private val assignmentCorrectness = listOf(
        AssignmentCorrectnessItem(
            questionContent = "태양이 도는 은하의 이름은?",
            questionModelAnswer = "은하수",
            studentAnswer = "은하수",
            isCorrect = true,
            answeredAt = "2024-01-02T10:00:00Z",
            questionNum = "1",
            explanation = "태양계는 은하수 은하에 속해 있습니다.",
        ),
    )

    var studentClassesResponse: List<ClassInfo> = listOf(
        ClassInfo(id = 1, name = "수학 A반"),
        ClassInfo(id = 2, name = "과학 B반"),
    )

    var assignmentsResponse: List<AssignmentData> = listOf(assignmentData)
    var assignmentByIdResponse: AssignmentData = assignmentData
    var assignmentResultResponse: AssignmentResultData = assignmentStatistics
    var personalAssignmentsResponse: List<PersonalAssignmentData> = listOf(personalAssignmentData)
    var personalAssignmentStatisticsResponses: MutableMap<Int, PersonalAssignmentStatistics> =
        mutableMapOf(personalAssignmentData.id to personalAssignmentStatistics)
    var shouldFailGetAssignmentById: Boolean = false
    var getAssignmentByIdErrorMessage: String = "Failed to load assignment"
    var shouldFailPersonalAssignments: Boolean = false
    var personalAssignmentsErrorMessage: String = "Failed to load personal assignments"
    var shouldFailGetAllAssignments: Boolean = false
    var getAllAssignmentsErrorMessage: String = "Failed to load assignments"
    var shouldFailAssignmentResult: Boolean = false
    var assignmentResultErrorMessage: String = "Failed to load assignment result"
    var shouldFailAssignmentCorrectness: Boolean = false
    var assignmentCorrectnessErrorMessage: String = "Failed to load assignment correctness"
    var assignmentCorrectnessResponses: List<AssignmentCorrectnessItem> = assignmentCorrectness
    var personalAssignmentsDelayMillis: Long = 0L
    var shouldFailCreateAssignment: Boolean = false
    var createAssignmentErrorMessage: String = "Failed to create assignment"
    var shouldFailPersonalAssignmentStatistics: Boolean = false
    var personalAssignmentStatisticsErrorMessage: String = "Failed to load statistics"
    var shouldFailDashboardStats: Boolean = false
    var dashboardStatsErrorMessage: String = "Failed to load dashboard"
    var shouldFailCurriculumReport: Boolean = false
    var curriculumReportErrorMessage: String = "Failed to load curriculum report"
    var shouldFailStudentProgress: Boolean = false
    var studentProgressErrorMessage: String = "Failed to load student progress"
    var shouldFailStudentClasses: Boolean = false
    var studentClassesErrorMessage: String = "Failed to load student classes"
    var shouldFailCreateClass: Boolean = false
    var createClassErrorMessage: String = "Failed to create class"

    private fun <T> success(data: T): Response<ApiResponse<T>> =
        Response.success(ApiResponse(success = true, data = data, message = null, error = null))

    private fun <T> failure(message: String): Response<ApiResponse<T>> =
        Response.success(ApiResponse(success = false, data = null, message = message, error = message))

    // region Auth APIs
    override suspend fun login(request: LoginRequest): Response<LoginResponse> {
        val user = if (request.email.contains("teacher")) {
            User(
                id = 2,
                name = "테스트선생님",
                email = request.email,
                role = UserRole.TEACHER,
                isStudent = false,
                totalStudents = 25,
                totalClasses = 3,
                assignments = listOf(assignmentData),
            )
        } else {
            User(
                id = 1,
                name = "테스트학생",
                email = request.email,
                role = UserRole.STUDENT,
                isStudent = true,
                totalAssignments = 10,
                completedAssignments = 7,
                inProgressAssignments = 3,
                assignments = listOf(assignmentData),
            )
        }
        return Response.success(LoginResponse(success = true, user = user, token = "fake-token", message = null, error = null))
    }

    override suspend fun signup(request: SignupRequest): Response<LoginResponse> =
        Response.success(LoginResponse(success = true, user = null, token = "fake-token", message = "가입 완료", error = null))

    override suspend fun logout(): Response<ApiResponse<Unit>> = success(Unit)
    override suspend fun deleteAccount(): Response<ApiResponse<Unit>> = success(Unit)
    // endregion

    // region Assignment APIs
    override suspend fun getAllAssignments(
        teacherId: String?,
        classId: String?,
        status: String?,
    ): Response<ApiResponse<List<AssignmentData>>> =
        if (shouldFailGetAllAssignments) {
            failure(getAllAssignmentsErrorMessage)
        } else {
            success(assignmentsResponse)
        }

    override suspend fun getAssignmentById(id: Int): Response<ApiResponse<AssignmentData>> =
        if (shouldFailGetAssignmentById) {
            failure(getAssignmentByIdErrorMessage)
        } else {
            success(assignmentByIdResponse.copy(id = id))
        }

    override suspend fun createAssignment(assignment: CreateAssignmentRequest): Response<ApiResponse<CreateAssignmentResponse>> =
        if (shouldFailCreateAssignment) {
            failure(createAssignmentErrorMessage)
        } else {
            success(
                CreateAssignmentResponse(
                    assignment_id = 99,
                    material_id = 501,
                    s3_key = "assignments/99/material.pdf",
                    upload_url = "https://example.com/upload",
                ),
            )
        }

    override suspend fun updateAssignment(id: Int, assignment: UpdateAssignmentRequest): Response<ApiResponse<AssignmentData>> =
        success(assignmentData.copy(id = id, title = assignment.title ?: assignmentData.title))

    override suspend fun deleteAssignment(id: Int): Response<ApiResponse<Unit>> = success(Unit)

    override suspend fun getAssignmentResult(id: Int): Response<ApiResponse<AssignmentResultData>> =
        if (shouldFailAssignmentResult) {
            failure(assignmentResultErrorMessage)
        } else {
            success(assignmentResultResponse)
        }

    override suspend fun checkS3Upload(assignmentId: Int): Response<ApiResponse<S3UploadStatus>> =
        success(
            S3UploadStatus(
                assignment_id = assignmentId,
                material_id = 1,
                s3_key = "assignments/$assignmentId/material.pdf",
                file_exists = true,
                file_size = 1024,
                content_type = "application/pdf",
                last_modified = "2024-01-01T09:00:00Z",
                bucket = "fake-bucket",
            ),
        )

    override suspend fun createQuestions(request: QuestionCreateRequest): Response<ResponseBody> {
        // 백엔드는 ApiResponse 형식이 아닌 직접 JSON 응답을 반환함
        val responseBody = ResponseBody.create(
            "application/json".toMediaType(),
            """{"assignment_id":${request.assignment_id},"material_id":${request.material_id},"summary_preview":"","questions":[]}"""
        )
        return Response.success(200, responseBody)
    }
    // endregion

    // region Student APIs
    var allStudentsResponse: List<Student> = listOf(student)
    var shouldFailAllStudents: Boolean = false
    var allStudentsErrorMessage: String = "Failed to load students"

    override suspend fun getAllStudents(teacherId: String?, classId: String?): Response<ApiResponse<List<Student>>> =
        if (shouldFailAllStudents) {
            failure(allStudentsErrorMessage)
        } else {
            success(allStudentsResponse)
        }

    override suspend fun getStudentById(id: Int): Response<ApiResponse<Student>> = success(student.copy(id = id))

    override suspend fun getStudentAssignments(id: Int): Response<ApiResponse<List<AssignmentData>>> =
        success(assignmentsResponse)
    // endregion

    // region Personal Assignment APIs
    override suspend fun getPersonalAssignments(
        studentId: Int?,
        assignmentId: Int?,
    ): Response<ApiResponse<List<PersonalAssignmentData>>> {
        if (shouldFailPersonalAssignments) {
            return failure(personalAssignmentsErrorMessage)
        }
        if (personalAssignmentsDelayMillis > 0) {
            delay(personalAssignmentsDelayMillis)
        }
        val data = personalAssignmentsResponse.filter { assignmentId == null || it.assignment.id == assignmentId }
        return success(data)
    }

    override suspend fun getPersonalAssignmentQuestions(id: Int): Response<ApiResponse<List<PersonalAssignmentQuestion>>> =
        success(personalAssignmentQuestionsResponses)

    override suspend fun getPersonalAssignmentStatistics(id: Int): Response<ApiResponse<PersonalAssignmentStatistics>> =
        if (shouldFailPersonalAssignmentStatistics) {
            failure(personalAssignmentStatisticsErrorMessage)
        } else {
            success(personalAssignmentStatisticsResponses[id] ?: personalAssignmentStatistics)
        }

    override suspend fun getRecentPersonalAssignment(studentId: Int): Response<ApiResponse<RecentAnswerData>> =
        success(RecentAnswerData(personalAssignmentId = personalAssignmentData.id))

    override suspend fun submitAnswer(
        personalAssignmentId: Int,
        studentId: MultipartBody.Part,
        questionId: MultipartBody.Part,
        audioFile: MultipartBody.Part,
    ): Response<ApiResponse<AnswerSubmissionResponse>> {
        val response = answerSubmissionResponseQueue?.let { queue ->
            if (queue.isNotEmpty()) queue.removeAt(0) else null
        } ?: answerSubmissionResponse
        return success(response)
    }

    override suspend fun getNextQuestion(personalAssignmentId: Int): Response<ApiResponse<PersonalAssignmentQuestion>> {
        if (nextQuestionQueue.isNotEmpty()) {
            val next = nextQuestionQueue.removeAt(0)
            return success(next)
        }
        return if (shouldReturnNoMoreQuestions) {
            failure(nextQuestionErrorMessage)
        } else {
            success(personalAssignmentQuestion)
        }
    }

    override suspend fun completePersonalAssignment(id: Int): Response<ApiResponse<Unit>> = success(Unit)

    override suspend fun getAssignmentCorrectness(id: Int): Response<ApiResponse<List<AssignmentCorrectnessItem>>> =
        if (shouldFailAssignmentCorrectness) {
            failure(assignmentCorrectnessErrorMessage)
        } else {
            success(assignmentCorrectnessResponses)
        }
    // endregion

    override suspend fun getStudentProgress(id: Int): Response<ApiResponse<StudentProgress>> =
        if (shouldFailStudentProgress) {
            failure(studentProgressErrorMessage)
        } else {
            success(studentProgress)
        }

    // region Class APIs
    var classesResponse: List<ClassData> = listOf(classData)
    var shouldFailClasses: Boolean = false
    var classesErrorMessage: String = "Failed to load classes"

    override suspend fun getClasses(teacherId: String): Response<ApiResponse<List<ClassData>>> =
        if (shouldFailClasses) {
            failure(classesErrorMessage)
        } else {
            success(classesResponse)
        }

    override suspend fun createClass(request: CreateClassRequest): Response<ApiResponse<ClassData>> =
        if (shouldFailCreateClass) {
            failure(createClassErrorMessage)
        } else {
            success(classData.copy(id = 10, name = request.name))
        }

    override suspend fun getClassById(id: Int): Response<ApiResponse<ClassData>> =
        success(classData.copy(id = id))

    var classStudentsResponse: List<Student> = listOf(student)
    var shouldFailClassStudents: Boolean = false
    var classStudentsErrorMessage: String = "Failed to load class students"

    override suspend fun getClassStudents(id: Int): Response<ApiResponse<List<Student>>> =
        if (shouldFailClassStudents) {
            failure(classStudentsErrorMessage)
        } else {
            success(classStudentsResponse)
        }

    override suspend fun getStudentClasses(id: Int): Response<ApiResponse<List<ClassInfo>>> =
        if (shouldFailStudentClasses) {
            failure(studentClassesErrorMessage)
        } else {
            success(studentClassesResponse)
        }

    override suspend fun enrollStudentToClass(id: Int, studentId: Int): Response<ApiResponse<EnrollmentData>> =
        success(
            EnrollmentData(
                student = student.copy(id = studentId),
                courseClass = classData.copy(id = id),
                status = "ENROLLED",
            ),
        )

    override suspend fun removeStudentFromClass(id: Int, student_id: Int): Response<ApiResponse<Unit>> =
        success(Unit)

    var classStudentsStatisticsResponse: ClassStudentsStatistics = ClassStudentsStatistics(
        overallCompletionRate = 0.75f,
        students = listOf(
            StudentStatisticsItem(
                studentId = 1,
                averageScore = 88.0f,
                completionRate = 0.9f,
                totalAssignments = 10,
                completedAssignments = 9,
            ),
        ),
    )
    var shouldFailClassStudentsStatistics: Boolean = false
    var classStudentsStatisticsErrorMessage: String = "Failed to load class statistics"

    override suspend fun getClassStudentsStatistics(classId: Int): Response<ApiResponse<ClassStudentsStatistics>> =
        if (shouldFailClassStudentsStatistics) {
            failure(classStudentsStatisticsErrorMessage)
        } else {
            success(classStudentsStatisticsResponse)
        }
    // endregion

    // region Reports & Dashboard
    override suspend fun getProgressReport(
        teacherId: String,
        classId: String?,
        period: String,
    ): Response<ApiResponse<ProgressReportData>> =
        if (shouldFailCurriculumReport) {
            failure(curriculumReportErrorMessage)
        } else {
            success(
                ProgressReportData(
                    totalStudents = 25,
                    totalAssignments = 12,
                    completedAssignments = 9,
                    averageScore = 85.0,
                    classBreakdown = listOf(
                        ClassProgress(
                            classId = classData.id,
                            className = classData.name,
                            studentCount = 25,
                            completedAssignments = 9,
                            totalAssignments = 12,
                        ),
                    ),
                ),
            )
        }

    override suspend fun getCurriculumReport(
        classId: Int,
        studentId: Int,
    ): Response<ApiResponse<CurriculumReportData>> =
        if (shouldFailCurriculumReport) {
            failure(curriculumReportErrorMessage)
        } else {
            success(
                CurriculumReportData(
                    totalQuestions = 40,
                    totalCorrect = 32,
                    overallAccuracy = 0.8,
                    achievementStatistics = mapOf(
                        "수학" to AchievementStatistics(
                            totalQuestions = 20,
                            correctQuestions = 16,
                            accuracy = 0.8,
                            content = "수와 연산",
                        ),
                    ),
                ),
            )
        }

    override suspend fun getDashboardStats(teacherId: String): Response<ApiResponse<DashboardStats>> =
        if (shouldFailDashboardStats) {
            failure(dashboardStatsErrorMessage)
        } else {
            success(dashboardStats)
        }
    // endregion

    override suspend fun healthCheck(): Response<ApiResponse<String>> =
        success("ok")
}
