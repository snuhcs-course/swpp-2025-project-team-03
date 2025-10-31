package com.example.voicetutor.data.repository

import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.ApiResponse
import com.example.voicetutor.data.network.S3UploadStatus
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import org.mockito.kotlin.eq
import org.mockito.kotlin.any
import retrofit2.Response
import kotlinx.coroutines.test.runTest
import com.example.voicetutor.data.models.AssignmentStatus
import java.io.File
import com.example.voicetutor.data.models.AssignmentData
import com.example.voicetutor.data.models.CourseClass
import com.example.voicetutor.data.models.Subject
import com.example.voicetutor.data.models.PersonalAssignmentQuestion
import com.example.voicetutor.data.models.PersonalAssignmentStatistics

@RunWith(MockitoJUnitRunner::class)
class AssignmentRepositoryTest {

    @Mock
    lateinit var apiService: ApiService

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
        id = 1, name = "C", description = null, subject = subject(),
        teacherName = "T", startDate = "", endDate = "", studentCount = 0, createdAt = ""
    )

    @Test
    fun getAllAssignments_success_returnsList() = runTest {
        val repo = AssignmentRepository(apiService)
        val items = listOf(
            AssignmentData(1, "A1", "d", 0, null, "", "", course(), null, null),
            AssignmentData(2, "A2", "d", 0, null, "", "", course(), null, null)
        )
        whenever(apiService.getAllAssignments(null, null, null)).thenReturn(
            Response.success(ApiResponse(success = true, data = items, message = null, error = null))
        )
        val r = repo.getAllAssignments()
        assert(r.isSuccess)
        assert(r.getOrNull()?.size == 2)
    }

    @Test
    fun getAssignmentById_success_returnsItem() = runTest {
        val repo = AssignmentRepository(apiService)
        val item = AssignmentData(3, "A3", "d", 0, null, "", "", course(), null, null)
        whenever(apiService.getAssignmentById(3)).thenReturn(
            Response.success(ApiResponse(success = true, data = item, message = null, error = null))
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
            Response.success(ApiResponse(success = true, data = list, message = null, error = null))
        )
        val r = repo.getPersonalAssignmentQuestions(9)
        assert(r.isSuccess)
        assert(r.getOrNull()?.size == 1)
    }

    @Test
    fun getPersonalAssignmentStatistics_success_returnsStats() = runTest {
        val repo = AssignmentRepository(apiService)
        val stats = PersonalAssignmentStatistics(
            totalQuestions = 3, answeredQuestions = 1, correctAnswers = 1,
            accuracy = 0.5f, totalProblem = 3, solvedProblem = 1, progress = 0.5f
        )
        whenever(apiService.getPersonalAssignmentStatistics(11)).thenReturn(
            Response.success(ApiResponse(success = true, data = stats, message = null, error = null))
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
        val tmp = File.createTempFile("sample", ".pdf").apply { writeBytes(byteArrayOf(1,2,3)) }
        val r = repo.uploadPdfToS3("http://invalid-host-${System.currentTimeMillis()}.local/upload", tmp)
        assert(r.isFailure)
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
                classId = null,
                dueDate = null,
                type = null,
                description = null,
                questions = null
            )
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
        val audio = File.createTempFile("ans", ".wav").apply { writeBytes(byteArrayOf(0,1)) }
        whenever(apiService.submitAnswer(any(), any(), any())).thenReturn(Response.error(400, errorBody))
        val r = repo.submitAnswer(7, 21, audio)
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
}


