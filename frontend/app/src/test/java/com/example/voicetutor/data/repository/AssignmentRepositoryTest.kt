package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.AnswerSubmissionResponse
import com.example.voicetutor.data.models.AssignmentData
import com.example.voicetutor.data.models.AssignmentResultData
import com.example.voicetutor.data.models.AssignmentStatus
import com.example.voicetutor.data.models.CourseClass
import com.example.voicetutor.data.models.PersonalAssignmentQuestion
import com.example.voicetutor.data.models.PersonalAssignmentStatistics
import com.example.voicetutor.data.models.Subject
import com.example.voicetutor.data.network.ApiResponse
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.S3UploadStatus
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.whenever
import retrofit2.Response
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class AssignmentRepositoryTest {

    @Mock
    lateinit var apiService: ApiService

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun getNextQuestion_404_withMessage_parsesKnownMessage() = runTest {
        // Given
        val repo = AssignmentRepository(apiService)
        val json = """{"success":false,"message":"모든 문제를 완료했습니다."}"""
        val errorBody = ResponseBody.create("application/json".toMediaType(), json)
        whenever(apiService.getNextQuestion(77)).thenReturn(Response.error(404, errorBody))

        // When
        val r = repo.getNextQuestion(77)

        // Then
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message?.contains("모든 문제를 완료했습니다.") == true)
    }

    @Test
    fun getNextQuestion_500_unknown_returnsUnknownError() = runTest {
        // Given
        val repo = AssignmentRepository(apiService)
        val json = """{"success":false,"error":"Server error"}"""
        val errorBody = ResponseBody.create("application/json".toMediaType(), json)
        whenever(apiService.getNextQuestion(88)).thenReturn(Response.error(500, errorBody))

        // When
        val r = repo.getNextQuestion(88)

        // Then
        assert(r.isFailure)
        // Repository maps unrecognized to "Unknown error"
        assert(r.exceptionOrNull()?.message?.contains("Unknown error") == true)
    }

    // ===== Success paths =====
    private fun subject() = Subject(id = 1, name = "S")
    private fun course() = CourseClass(
        id = 1,
        name = "C",
        description = null,
        subject = subject(),
        teacherName = "T",
        studentCount = 0,
        createdAt = "",
    )

    @Test
    fun getAllAssignments_success_returnsList() = runTest {
        val repo = AssignmentRepository(apiService)
        val items = listOf(
            AssignmentData(1, "A1", "d", 0, null, "", course(), null, null),
            AssignmentData(2, "A2", "d", 0, null, "", course(), null, null),
        )
        whenever(apiService.getAllAssignments(null, null, null)).thenReturn(
            Response.success(ApiResponse(success = true, data = items, message = null, error = null)),
        )
        val r = repo.getAllAssignments()
        assert(r.isSuccess)
        assert(r.getOrNull()?.size == 2)
    }

    @Test
    fun getAssignmentById_success_returnsItem() = runTest {
        val repo = AssignmentRepository(apiService)
        val item = AssignmentData(3, "A3", "d", 0, null, "", course(), null, null)
        whenever(apiService.getAssignmentById(3)).thenReturn(
            Response.success(ApiResponse(success = true, data = item, message = null, error = null)),
        )
        val r = repo.getAssignmentById(3)
        assert(r.isSuccess)
        assert(r.getOrNull()?.id == 3)
    }

    @Test
    fun getPersonalAssignmentQuestions_success_returnsQuestions() = runTest {
        val repo = AssignmentRepository(apiService)
        val list = listOf(PersonalAssignmentQuestion(1, "1", "Q1", "A", "E", "M"))
        whenever(apiService.getPersonalAssignmentQuestions(9)).thenReturn(
            Response.success(ApiResponse(success = true, data = list, message = null, error = null)),
        )
        val r = repo.getPersonalAssignmentQuestions(9)
        assert(r.isSuccess)
        assert(r.getOrNull()?.size == 1)
    }

    @Test
    fun getPersonalAssignmentStatistics_success_returnsStats() = runTest {
        val repo = AssignmentRepository(apiService)
        val stats = PersonalAssignmentStatistics(
            totalQuestions = 3,
            answeredQuestions = 1,
            correctAnswers = 1,
            accuracy = 0.5f,
            totalProblem = 3,
            solvedProblem = 1,
            progress = 0.5f,
            averageScore = 0.4f,
        )
        whenever(apiService.getPersonalAssignmentStatistics(11)).thenReturn(
            Response.success(ApiResponse(success = true, data = stats, message = null, error = null)),
        )
        val r = repo.getPersonalAssignmentStatistics(11)
        assert(r.isSuccess)
        assert(r.getOrNull()?.totalQuestions == 3)
    }

    @Test
    fun getAllAssignments_failure_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"success":false}""")
        whenever(apiService.getAllAssignments(null, null, AssignmentStatus.COMPLETED.name)).thenReturn(Response.error(500, errorBody))
        val r = repo.getAllAssignments(status = AssignmentStatus.COMPLETED)
        assert(r.isFailure)
    }

    @Test
    fun getAssignmentById_failure_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"success":false}""")
        whenever(apiService.getAssignmentById(999)).thenReturn(Response.error(404, errorBody))
        val r = repo.getAssignmentById(999)
        assert(r.isFailure)
    }

    @Test
    fun getPersonalAssignmentStatistics_failure_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"success":false}""")
        whenever(apiService.getPersonalAssignmentStatistics(5)).thenReturn(Response.error(500, errorBody))
        val r = repo.getPersonalAssignmentStatistics(5)
        assert(r.isFailure)
    }

    @Test
    fun uploadPdfToS3_nonexistentFile_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        val nonExisting = File("/path/does/not/exist-${System.currentTimeMillis()}.pdf")
        val r = repo.uploadPdfToS3("http://invalid-host/upload", nonExisting)
        assert(r.isFailure)
    }

    @Test
    fun uploadPdfToS3_networkFailure_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        // Create an actual small temp file to pass file read, then fail on network
        val tmp = File.createTempFile("sample", ".pdf").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val r = repo.uploadPdfToS3("http://invalid-host-${System.currentTimeMillis()}.local/upload", tmp)
        assert(r.isFailure)
        tmp.delete()
    }

    @Test
    fun uploadPdfToS3_success_returnsTrue() = runTest {
        // Arrange
        val repo = AssignmentRepository(apiService)
        val tmp = File.createTempFile("test", ".pdf").apply { writeBytes(byteArrayOf(1, 2, 3, 4, 5)) }
        val uploadUrl = mockWebServer.url("/upload").toString()

        // Mock successful response
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("Success"))

        // Act
        val result = repo.uploadPdfToS3(uploadUrl, tmp)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == true)
        tmp.delete()
    }

    @Test
    fun uploadPdfToS3_httpError_returnsFailure() = runTest {
        // Arrange
        val repo = AssignmentRepository(apiService)
        val tmp = File.createTempFile("test", ".pdf").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val uploadUrl = mockWebServer.url("/upload").toString()

        // Mock error response
        mockWebServer.enqueue(MockResponse().setResponseCode(500).setBody("Internal Server Error"))

        // Act
        val result = repo.uploadPdfToS3(uploadUrl, tmp)

        // Assert
        assert(result.isFailure)
        val exceptionMessage = result.exceptionOrNull()?.message ?: ""
        assert(exceptionMessage.contains("500") || exceptionMessage.contains("Upload failed"))
        tmp.delete()
    }

    @Test
    fun uploadPdfToS3_http404_returnsFailure() = runTest {
        // Arrange
        val repo = AssignmentRepository(apiService)
        val tmp = File.createTempFile("test", ".pdf").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val uploadUrl = mockWebServer.url("/upload").toString()

        // Mock 404 response
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody("Not Found"))

        // Act
        val result = repo.uploadPdfToS3(uploadUrl, tmp)

        // Assert
        assert(result.isFailure)
        val exceptionMessage = result.exceptionOrNull()?.message ?: ""
        assert(exceptionMessage.contains("404") || exceptionMessage.contains("Upload failed"))
        tmp.delete()
    }

    @Test
    fun uploadPdfToS3_fileReadException_returnsFailure() = runTest {
        // Arrange
        val repo = AssignmentRepository(apiService)
        // Create a file that exists but cannot be read
        val tmp = File.createTempFile("test", ".pdf")
        tmp.writeBytes(byteArrayOf(1, 2, 3))
        // Make file unreadable (if possible on the platform)
        try {
            tmp.setReadable(false)
        } catch (e: Exception) {
            // If we can't make it unreadable, create a directory instead
            tmp.delete()
            val dir = File.createTempFile("test", ".pdf")
            dir.delete()
            dir.mkdirs()
            val result = repo.uploadPdfToS3("http://test.com/upload", dir)
            assert(result.isFailure)
            dir.delete()
            return@runTest
        }

        // Act
        val result = repo.uploadPdfToS3("http://test.com/upload", tmp)

        // Assert
        assert(result.isFailure)
        tmp.setReadable(true)
        tmp.delete()
    }

    @Test
    fun uploadPdfToS3_ioException_returnsFailure() = runTest {
        // Arrange
        val repo = AssignmentRepository(apiService)
        val tmp = File.createTempFile("test", ".pdf").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        // Use an invalid URL that will cause IOException
        val invalidUrl = "not-a-valid-url"

        // Act
        val result = repo.uploadPdfToS3(invalidUrl, tmp)

        // Assert
        assert(result.isFailure)
        tmp.delete()
    }

    @Test
    fun uploadPdfToS3_emptyFile_handlesCorrectly() = runTest {
        // Arrange
        val repo = AssignmentRepository(apiService)
        val tmp = File.createTempFile("empty", ".pdf")
        // Empty file
        val uploadUrl = mockWebServer.url("/upload").toString()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("Success"))

        // Act
        val result = repo.uploadPdfToS3(uploadUrl, tmp)

        // Assert
        assert(result.isSuccess)
        tmp.delete()
    }

    @Test
    fun uploadPdfToS3_largeFile_handlesCorrectly() = runTest {
        // Arrange
        val repo = AssignmentRepository(apiService)
        val tmp = File.createTempFile("large", ".pdf")
        // Create a larger file (10KB)
        val largeData = ByteArray(10 * 1024) { it.toByte() }
        tmp.writeBytes(largeData)
        val uploadUrl = mockWebServer.url("/upload").toString()
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody("Success"))

        // Act
        val result = repo.uploadPdfToS3(uploadUrl, tmp)

        // Assert
        assert(result.isSuccess)
        tmp.delete()
    }

    @Test
    fun updateAssignment_failure_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"success":false}""")
        whenever(apiService.updateAssignment(eq(1), any())).thenReturn(Response.error(500, errorBody))
        val r = repo.updateAssignment(
            1,
            com.example.voicetutor.data.network.UpdateAssignmentRequest(
                title = "t",
                subject = null,
                dueAt = null,
                grade = null,
                description = null,
                totalQuestions = null,
            ),
        )
        assert(r.isFailure)
    }

    @Test
    fun deleteAssignment_failure_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"success":false}""")
        whenever(apiService.deleteAssignment(2)).thenReturn(Response.error(404, errorBody))
        val r = repo.deleteAssignment(2)
        assert(r.isFailure)
    }

    // removed: getAssignmentQuestions_failure_returnsFailure (API removed)

    @Test
    fun submitAssignment_failure_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"success":false}""")
        val audio = File.createTempFile("ans", ".wav").apply { writeBytes(byteArrayOf(0, 1)) }
        whenever(apiService.submitAnswer(any(), any(), any(), any())).thenReturn(Response.error(400, errorBody))
        val r = repo.submitAnswer(26, 7, 21, audio)
        assert(r.isFailure)
        audio.delete()
    }

    // removed: saveAssignmentDraft_failure_returnsFailure (API removed)

    @Test
    fun completePersonalAssignment_failure_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"success":false}""")
        whenever(apiService.completePersonalAssignment(42)).thenReturn(Response.error(500, errorBody))
        val r = repo.completePersonalAssignment(42)
        assert(r.isFailure)
    }

    @Test
    fun getPersonalAssignments_success_returnsList() = runTest {
        val repo = AssignmentRepository(apiService)
        val personalAssignments = listOf(
            com.example.voicetutor.data.models.PersonalAssignmentData(
                id = 1,
                student = com.example.voicetutor.data.models.StudentInfo(1, "S1", "s1@test.com"),
                assignment = com.example.voicetutor.data.models.PersonalAssignmentInfo(1, "A1", "d", 5, "", "1"),
                status = com.example.voicetutor.data.models.PersonalAssignmentStatus.IN_PROGRESS,
                solvedNum = 2,
            ),
        )
        whenever(apiService.getPersonalAssignments(studentId = 1, assignmentId = null)).thenReturn(
            Response.success(ApiResponse(success = true, data = personalAssignments, message = null, error = null)),
        )
        val r = repo.getPersonalAssignments(studentId = 1, assignmentId = null)
        assert(r.isSuccess)
        assert(r.getOrNull()?.size == 1)
    }

    @Test
    fun getPersonalAssignments_withAssignmentId_success_returnsFilteredList() = runTest {
        val repo = AssignmentRepository(apiService)
        val personalAssignments = listOf(
            com.example.voicetutor.data.models.PersonalAssignmentData(
                id = 1,
                student = com.example.voicetutor.data.models.StudentInfo(1, "S1", "s1@test.com"),
                assignment = com.example.voicetutor.data.models.PersonalAssignmentInfo(1, "A1", "d", 5, "", "1"),
                status = com.example.voicetutor.data.models.PersonalAssignmentStatus.IN_PROGRESS,
                solvedNum = 2,
            ),
        )
        whenever(apiService.getPersonalAssignments(studentId = null, assignmentId = 10)).thenReturn(
            Response.success(ApiResponse(success = true, data = personalAssignments, message = null, error = null)),
        )
        val r = repo.getPersonalAssignments(studentId = null, assignmentId = 10)
        assert(r.isSuccess)
        assert(r.getOrNull()?.size == 1)
    }

    @Test
    fun getPersonalAssignments_failure_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"success":false,"error":"Failed"}""")
        whenever(apiService.getPersonalAssignments(studentId = 1, assignmentId = null)).thenReturn(Response.error(500, errorBody))
        val r = repo.getPersonalAssignments(studentId = 1)
        assert(r.isFailure)
    }

    @Test
    fun createAssignment_success_returnsResponse() = runTest {
        val repo = AssignmentRepository(apiService)
        val request = com.example.voicetutor.data.network.CreateAssignmentRequest.builder()
            .title("New Assignment")
            .subject("Math")
            .classId(1)
            .dueAt("2025-12-31")
            .grade("1")
            .description("Description")
            .build()
        val response = com.example.voicetutor.data.network.CreateAssignmentResponse(
            assignment_id = 50,
            material_id = 20,
            s3_key = "key",
            upload_url = "url",
        )
        whenever(apiService.createAssignment(request)).thenReturn(
            Response.success(ApiResponse(success = true, data = response, message = null, error = null)),
        )
        val r = repo.createAssignment(request)
        assert(r.isSuccess)
        assert(r.getOrNull()?.assignment_id == 50)
    }

    @Test
    fun createAssignment_failure_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        val request = com.example.voicetutor.data.network.CreateAssignmentRequest.builder()
            .title("New Assignment")
            .subject("Math")
            .classId(1)
            .dueAt("2025-12-31")
            .grade("1")
            .description(null)
            .build()
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"success":false,"error":"Failed"}""")
        whenever(apiService.createAssignment(request)).thenReturn(Response.error(400, errorBody))
        val r = repo.createAssignment(request)
        assert(r.isFailure)
    }

    @Test
    fun updateAssignment_success_returnsUpdatedAssignment() = runTest {
        val repo = AssignmentRepository(apiService)
        val request = com.example.voicetutor.data.network.UpdateAssignmentRequest(
            title = "Updated",
            description = "New desc",
            totalQuestions = 5,
            dueAt = "2025-12-31T00:00:00Z",
            grade = "A",
            subject = com.example.voicetutor.data.network.SubjectUpdateRequest(id = 1, name = "Math"),
        )
        val updated = AssignmentData(1, "Updated", "New desc", 0, null, "", course(), null, null)
        whenever(apiService.updateAssignment(eq(1), any())).thenReturn(
            Response.success(ApiResponse(success = true, data = updated, message = null, error = null)),
        )
        val r = repo.updateAssignment(1, request)
        assert(r.isSuccess)
        assert(r.getOrNull()?.title == "Updated")
    }

    @Test
    fun getAssignmentResult_success_returnsData() = runTest {
        val repo = AssignmentRepository(apiService)
        val resultData = AssignmentResultData(
            submittedStudents = 5,
            totalStudents = 10,
            averageScore = 85.0,
            completionRate = 50.0,
        )
        whenever(apiService.getAssignmentResult(1)).thenReturn(
            Response.success(ApiResponse(success = true, data = resultData, message = null, error = null)),
        )
        val r = repo.getAssignmentResult(1)
        assert(r.isSuccess)
        assert(r.getOrNull()?.totalStudents == 10)
    }

    @Test
    fun deleteAssignment_success_returnsSuccess() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.deleteAssignment(1)).thenReturn(
            Response.success(ApiResponse(success = true, data = Unit, message = null, error = null)),
        )
        val r = repo.deleteAssignment(1)
        assert(r.isSuccess)
    }

    @Test
    fun submitAnswer_success_returnsResponse() = runTest {
        val repo = AssignmentRepository(apiService)
        val audio = File.createTempFile("test", ".wav").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        val response = AnswerSubmissionResponse(
            isCorrect = true,
            numberStr = "1-2",
            tailQuestion = com.example.voicetutor.data.models.TailQuestion(
                id = 2,
                number = "2",
                question = "Q2",
                answer = "A2",
                explanation = "E2",
                difficulty = "M",
            ),
        )
        whenever(apiService.submitAnswer(any(), any(), any(), any())).thenReturn(
            Response.success(ApiResponse(success = true, data = response, message = null, error = null)),
        )
        val r = repo.submitAnswer(1, 1, 1, audio)
        assert(r.isSuccess)
        assert(r.getOrNull()?.isCorrect == true)
        audio.delete()
    }

    @Test
    fun getRecentPersonalAssignment_success_returnsId() = runTest {
        val repo = AssignmentRepository(apiService)
        val recentData = com.example.voicetutor.data.network.RecentAnswerData(
            personalAssignmentId = 100,
        )
        whenever(apiService.getRecentPersonalAssignment(1)).thenReturn(
            Response.success(ApiResponse(success = true, data = recentData, message = null, error = null)),
        )
        val r = repo.getRecentPersonalAssignment(1)
        assert(r.isSuccess)
        assert(r.getOrNull() == 100)
    }

    @Test
    fun getRecentPersonalAssignment_noData_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getRecentPersonalAssignment(1)).thenReturn(
            Response.success(ApiResponse<com.example.voicetutor.data.network.RecentAnswerData>(success = true, data = null, message = null, error = null)),
        )
        val r = repo.getRecentPersonalAssignment(1)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message?.contains("최근 개인 과제 ID를 찾을 수 없습니다") == true)
    }

    @Test
    fun getRecentPersonalAssignment_apiFailure_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"success":false,"message":"No assignment"}""")
        whenever(apiService.getRecentPersonalAssignment(1)).thenReturn(Response.error(404, errorBody))
        val r = repo.getRecentPersonalAssignment(1)
        assert(r.isFailure)
        // API 실패 시 Response.error를 반환하면 response.body()가 null이므로 기본 메시지 반환
        assert(r.exceptionOrNull()?.message?.contains("최근 개인 과제 조회 실패") == true)
    }

    @Test
    fun checkS3Upload_success_returnsStatus() = runTest {
        val repo = AssignmentRepository(apiService)
        val status = S3UploadStatus(
            assignment_id = 1,
            material_id = 10,
            s3_key = "key",
            file_exists = true,
            file_size = 1024L,
            content_type = "application/pdf",
            last_modified = "2025-01-01",
            bucket = "bucket",
        )
        whenever(apiService.checkS3Upload(1)).thenReturn(
            Response.success(ApiResponse(success = true, data = status, message = null, error = null)),
        )
        val r = repo.checkS3Upload(1)
        assert(r.isSuccess)
        assert(r.getOrNull()?.file_exists == true)
    }

    @Test
    fun checkS3Upload_failure_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"success":false,"error":"Failed"}""")
        whenever(apiService.checkS3Upload(1)).thenReturn(Response.error(500, errorBody))
        val r = repo.checkS3Upload(1)
        assert(r.isFailure)
    }

    @Test
    fun createQuestionsAfterUpload_success_returnsSuccess() = runTest {
        val repo = AssignmentRepository(apiService)
        val request = com.example.voicetutor.data.network.QuestionCreateRequest(
            assignment_id = 1,
            material_id = 10,
            total_number = 5,
        )
        // 백엔드는 ApiResponse 형식이 아닌 직접 JSON 응답을 반환함
        val responseBody = ResponseBody.create("application/json".toMediaType(), """{"assignment_id":1,"material_id":10}""")
        whenever(apiService.createQuestions(request)).thenReturn(
            Response.success(200, responseBody),
        )
        val r = repo.createQuestionsAfterUpload(1, 10, 5)
        assert(r.isSuccess)
    }

    @Test
    fun createQuestionsAfterUpload_failure_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"success":false,"error":"Failed"}""")
        whenever(apiService.createQuestions(any())).thenReturn(Response.error(500, errorBody))
        val r = repo.createQuestionsAfterUpload(1, 10, 5)
        assert(r.isFailure)
    }

    @Test
    fun getNextQuestion_success_returnsQuestion() = runTest {
        val repo = AssignmentRepository(apiService)
        val question = PersonalAssignmentQuestion(id = 1, number = "1", question = "Q1", answer = "A1", explanation = "E1", difficulty = "M")
        whenever(apiService.getNextQuestion(1)).thenReturn(
            Response.success(ApiResponse(success = true, data = question, message = null, error = null)),
        )
        val r = repo.getNextQuestion(1)
        assert(r.isSuccess)
        assert(r.getOrNull()?.id == 1)
    }

    @Test
    fun getPersonalAssignmentQuestions_emptyList_returnsEmptyList() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getPersonalAssignmentQuestions(1)).thenReturn(
            Response.success(ApiResponse(success = true, data = emptyList<PersonalAssignmentQuestion>(), message = null, error = null)),
        )
        val r = repo.getPersonalAssignmentQuestions(1)
        assert(r.isSuccess)
        assert(r.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun getPersonalAssignmentStatistics_noData_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getPersonalAssignmentStatistics(1)).thenReturn(
            Response.success(ApiResponse<PersonalAssignmentStatistics>(success = true, data = null, message = null, error = null)),
        )
        val r = repo.getPersonalAssignmentStatistics(1)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "No statistics data")
    }

    @Test
    fun getAllAssignments_withFilters_success_returnsFilteredList() = runTest {
        val repo = AssignmentRepository(apiService)
        val items = listOf(AssignmentData(1, "A1", "d", 0, null, "", course(), null, null))
        whenever(apiService.getAllAssignments("1", "10", AssignmentStatus.IN_PROGRESS.name)).thenReturn(
            Response.success(ApiResponse(success = true, data = items, message = null, error = null)),
        )
        val r = repo.getAllAssignments(teacherId = "1", classId = "10", status = AssignmentStatus.IN_PROGRESS)
        assert(r.isSuccess)
        assert(r.getOrNull()?.size == 1)
    }

    @Test
    fun getStudentAssignments_emptyList_returnsEmptyList() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getStudentAssignments(1)).thenReturn(
            Response.success(ApiResponse(success = true, data = emptyList<AssignmentData>(), message = null, error = null)),
        )
        val r = repo.getStudentAssignments(1)
        assert(r.isSuccess)
        assert(r.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun getStudentAssignments_failure_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"success":false,"error":"Failed"}""")
        whenever(apiService.getStudentAssignments(1)).thenReturn(Response.error(500, errorBody))
        val r = repo.getStudentAssignments(1)
        assert(r.isFailure)
    }

    @Test
    fun completePersonalAssignment_success_returnsSuccess() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.completePersonalAssignment(1)).thenReturn(
            Response.success(ApiResponse(success = true, data = Unit, message = null, error = null)),
        )
        val r = repo.completePersonalAssignment(1)
        assert(r.isSuccess)
    }

    // ===== Exception handling tests =====

    @Test
    fun getAllAssignments_networkException_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getAllAssignments(isNull(), isNull(), isNull())).thenThrow(RuntimeException("Network error"))
        val r = repo.getAllAssignments()
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun getAssignmentById_noData_throwsException() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getAssignmentById(1)).thenReturn(
            Response.success(ApiResponse<AssignmentData>(success = true, data = null, message = null, error = null)),
        )
        val r = repo.getAssignmentById(1)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "No data")
    }

    @Test
    fun getAssignmentById_networkException_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getAssignmentById(1)).thenThrow(RuntimeException("Network error"))
        val r = repo.getAssignmentById(1)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun getStudentAssignments_networkException_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getStudentAssignments(1)).thenThrow(RuntimeException("Network error"))
        val r = repo.getStudentAssignments(1)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun getPersonalAssignments_byStudentId_networkException_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getPersonalAssignments(studentId = 1)).thenThrow(RuntimeException("Network error"))
        val r = repo.getPersonalAssignments(1)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun getPersonalAssignments_byStudentAndAssignment_networkException_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getPersonalAssignments(studentId = 1, assignmentId = 2)).thenThrow(RuntimeException("Network error"))
        val r = repo.getPersonalAssignments(1, 2)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun createAssignment_noData_throwsException() = runTest {
        val repo = AssignmentRepository(apiService)
        val request = com.example.voicetutor.data.network.CreateAssignmentRequest.builder()
            .title("Test")
            .subject("Math")
            .classId(1)
            .dueAt("2025-12-31T23:59:59Z")
            .build()
        whenever(apiService.createAssignment(any())).thenReturn(
            Response.success(
                ApiResponse<com.example.voicetutor.data.network.CreateAssignmentResponse>(
                    success = true,
                    data = null,
                    message = null,
                    error = null,
                ),
            ),
        )
        val r = repo.createAssignment(request)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "No data")
    }

    @Test
    fun createAssignment_networkException_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        val request = com.example.voicetutor.data.network.CreateAssignmentRequest.builder()
            .title("Test")
            .subject("Math")
            .classId(1)
            .dueAt("2025-12-31T23:59:59Z")
            .build()
        whenever(apiService.createAssignment(any())).thenThrow(RuntimeException("Network error"))
        val r = repo.createAssignment(request)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun updateAssignment_noData_throwsException() = runTest {
        val repo = AssignmentRepository(apiService)
        val request = com.example.voicetutor.data.network.UpdateAssignmentRequest.builder()
            .title("Updated")
            .build()
        whenever(apiService.updateAssignment(eq(1), any())).thenReturn(
            Response.success(ApiResponse<AssignmentData>(success = true, data = null, message = null, error = null)),
        )
        val r = repo.updateAssignment(1, request)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "No data")
    }

    @Test
    fun updateAssignment_networkException_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        val request = com.example.voicetutor.data.network.UpdateAssignmentRequest.builder()
            .title("Updated")
            .build()
        whenever(apiService.updateAssignment(eq(1), any())).thenThrow(RuntimeException("Network error"))
        val r = repo.updateAssignment(1, request)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun getAssignmentResult_noData_throwsException() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getAssignmentResult(1)).thenReturn(
            Response.success(ApiResponse<AssignmentResultData>(success = true, data = null, message = null, error = null)),
        )
        val r = repo.getAssignmentResult(1)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "No data")
    }

    @Test
    fun getAssignmentResult_networkException_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getAssignmentResult(1)).thenThrow(RuntimeException("Network error"))
        val r = repo.getAssignmentResult(1)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun deleteAssignment_networkException_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.deleteAssignment(1)).thenThrow(RuntimeException("Network error"))
        val r = repo.deleteAssignment(1)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun checkS3Upload_noData_throwsException() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.checkS3Upload(1)).thenReturn(
            Response.success(ApiResponse<S3UploadStatus>(success = true, data = null, message = null, error = null)),
        )
        val r = repo.checkS3Upload(1)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "No data")
    }

    @Test
    fun checkS3Upload_networkException_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.checkS3Upload(1)).thenThrow(RuntimeException("Network error"))
        val r = repo.checkS3Upload(1)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun createQuestionsAfterUpload_networkException_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.createQuestions(any())).thenThrow(RuntimeException("Network error"))
        val r = repo.createQuestionsAfterUpload(1, 10, 5)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun getPersonalAssignmentQuestions_networkException_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getPersonalAssignmentQuestions(1)).thenThrow(RuntimeException("Network error"))
        val r = repo.getPersonalAssignmentQuestions(1)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun getNextQuestion_networkException_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getNextQuestion(1)).thenThrow(RuntimeException("Network error"))
        val r = repo.getNextQuestion(1)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun getNextQuestion_errorBodyParseException_returnsUnknownError() = runTest {
        val repo = AssignmentRepository(apiService)
        // Create an error body that will cause parsing exception
        val errorBody = ResponseBody.create("application/json".toMediaType(), "invalid json")
        whenever(apiService.getNextQuestion(1)).thenReturn(Response.error(404, errorBody))
        val r = repo.getNextQuestion(1)
        assert(r.isFailure)
        // Should return "Unknown error" when parsing fails
        assert(r.exceptionOrNull()?.message?.contains("Unknown error") == true)
    }

    @Test
    fun getPersonalAssignmentStatistics_noData_throwsException() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getPersonalAssignmentStatistics(1)).thenReturn(
            Response.success(
                ApiResponse<PersonalAssignmentStatistics>(
                    success = true,
                    data = null,
                    message = null,
                    error = null,
                ),
            ),
        )
        val r = repo.getPersonalAssignmentStatistics(1)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "No statistics data")
    }

    @Test
    fun getPersonalAssignmentStatistics_networkException_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getPersonalAssignmentStatistics(1)).thenThrow(RuntimeException("Network error"))
        val r = repo.getPersonalAssignmentStatistics(1)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun getRecentPersonalAssignment_networkException_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getRecentPersonalAssignment(1)).thenThrow(RuntimeException("Network error"))
        val r = repo.getRecentPersonalAssignment(1)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun submitAnswer_noData_throwsException() = runTest {
        val repo = AssignmentRepository(apiService)
        val tmp = File.createTempFile("audio", ".wav").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        whenever(apiService.submitAnswer(any(), any(), any(), any())).thenReturn(
            Response.success(
                ApiResponse<AnswerSubmissionResponse>(
                    success = true,
                    data = null,
                    message = null,
                    error = null,
                ),
            ),
        )
        val r = repo.submitAnswer(1, 2, 3, tmp)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "No submission data")
        tmp.delete()
    }

    @Test
    fun submitAnswer_networkException_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        val tmp = File.createTempFile("audio", ".wav").apply { writeBytes(byteArrayOf(1, 2, 3)) }
        whenever(apiService.submitAnswer(any(), any(), any(), any())).thenThrow(RuntimeException("Network error"))
        val r = repo.submitAnswer(1, 2, 3, tmp)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "Network error")
        tmp.delete()
    }

    @Test
    fun completePersonalAssignment_networkException_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.completePersonalAssignment(1)).thenThrow(RuntimeException("Network error"))
        val r = repo.completePersonalAssignment(1)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun getAssignmentCorrectness_networkException_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getAssignmentCorrectness(1)).thenThrow(RuntimeException("Network error"))
        val r = repo.getAssignmentCorrectness(1)
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun getAllAssignments_responseNotSuccessful_returnsFailure() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getAllAssignments(isNull(), isNull(), isNull())).thenReturn(
            Response.success(ApiResponse<List<AssignmentData>>(success = false, data = null, message = null, error = "API Error")),
        )
        val r = repo.getAllAssignments()
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "API Error")
    }

    @Test
    fun getAllAssignments_responseNotSuccessful_noError_returnsUnknownError() = runTest {
        val repo = AssignmentRepository(apiService)
        whenever(apiService.getAllAssignments(isNull(), isNull(), isNull())).thenReturn(
            Response.success(ApiResponse<List<AssignmentData>>(success = false, data = null, message = null, error = null)),
        )
        val r = repo.getAllAssignments()
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message == "Unknown error")
    }
}
