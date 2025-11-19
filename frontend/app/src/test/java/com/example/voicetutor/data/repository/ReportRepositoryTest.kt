package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.AchievementStatistics
import com.example.voicetutor.data.models.CurriculumReportData
import com.example.voicetutor.data.network.ApiResponse
import com.example.voicetutor.data.network.ApiService
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import retrofit2.Response

@RunWith(MockitoJUnitRunner::class)
class ReportRepositoryTest {

    @Mock
    lateinit var apiService: ApiService

    private fun buildCurriculumReportData() = CurriculumReportData(
        totalQuestions = 20,
        totalCorrect = 17,
        overallAccuracy = 0.85,
        achievementStatistics = mapOf(
            "achievement1" to AchievementStatistics(
                totalQuestions = 10,
                correctQuestions = 9,
                accuracy = 0.9,
                content = "Content 1",
            ),
        ),
    )

    @Test
    fun getCurriculumReport_success_returnsReport() = runTest {
        // Arrange
        val repo = ReportRepository(apiService)
        val reportData = buildCurriculumReportData()
        val apiResponse = ApiResponse(
            success = true,
            data = reportData,
            message = "Success",
            error = null,
        )
        whenever(apiService.getCurriculumReport(1, 1)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getCurriculumReport(1, 1)

        // Assert
        assert(result.isSuccess)
        assertEquals(reportData, result.getOrNull())
    }

    @Test
    fun getCurriculumReport_noData_returnsFailure() = runTest {
        // Arrange
        val repo = ReportRepository(apiService)
        val apiResponse = ApiResponse<CurriculumReportData>(
            success = true,
            data = null,
            message = "Success",
            error = null,
        )
        whenever(apiService.getCurriculumReport(1, 1)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getCurriculumReport(1, 1)

        // Assert
        assert(result.isFailure)
        assertEquals("No data", result.exceptionOrNull()?.message)
    }

    @Test
    fun getCurriculumReport_apiFailure_returnsFailure() = runTest {
        // Arrange
        val repo = ReportRepository(apiService)
        val apiResponse = ApiResponse<CurriculumReportData>(
            success = false,
            data = null,
            message = null,
            error = "Report not found",
        )
        whenever(apiService.getCurriculumReport(1, 1)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getCurriculumReport(1, 1)

        // Assert
        assert(result.isFailure)
        assertEquals("Report not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun getCurriculumReport_responseError_returnsFailure() = runTest {
        // Arrange
        val repo = ReportRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"error":"Server error"}""")
        whenever(apiService.getCurriculumReport(1, 1)).thenReturn(Response.error(500, errorBody))

        // Act
        val result = repo.getCurriculumReport(1, 1)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message?.contains("Unknown error") == true)
    }

    @Test
    fun getCurriculumReport_networkException_returnsFailure() = runTest {
        // Arrange
        val repo = ReportRepository(apiService)
        whenever(apiService.getCurriculumReport(1, 1)).thenThrow(RuntimeException("Network error"))

        // Act
        val result = repo.getCurriculumReport(1, 1)

        // Assert
        assert(result.isFailure)
        assertEquals("Network error", result.exceptionOrNull()?.message)
    }

    @Test
    fun getCurriculumReport_apiFailure_noError_returnsDefaultMessage() = runTest {
        // Arrange
        val repo = ReportRepository(apiService)
        val apiResponse = ApiResponse<CurriculumReportData>(
            success = false,
            data = null,
            message = null,
            error = null,
        )
        whenever(apiService.getCurriculumReport(1, 1)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getCurriculumReport(1, 1)

        // Assert
        assert(result.isFailure)
        assertEquals("Unknown error", result.exceptionOrNull()?.message)
    }
}
