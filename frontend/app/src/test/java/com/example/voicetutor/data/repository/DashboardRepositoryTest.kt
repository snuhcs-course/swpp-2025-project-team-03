package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.DashboardStats
import com.example.voicetutor.data.network.ApiResponse
import com.example.voicetutor.data.network.ApiService
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import retrofit2.Response

@RunWith(MockitoJUnitRunner::class)
class DashboardRepositoryTest {

    @Mock
    lateinit var apiService: ApiService

    private fun buildDashboardStats() = DashboardStats(
        totalAssignments = 20,
        totalStudents = 50,
        totalClasses = 5,
        completedAssignments = 15,
        inProgressAssignments = 5,
    )

    @Test
    fun getDashboardStats_success_returnsStats() = runTest {
        // Arrange
        val repo = DashboardRepository(apiService)
        val stats = buildDashboardStats()
        val apiResponse = ApiResponse(
            success = true,
            data = stats,
            message = "Success",
            error = null,
        )
        whenever(apiService.getDashboardStats("1")).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getDashboardStats("1")

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == stats)
        assert(result.getOrNull()?.totalAssignments == 20)
        assert(result.getOrNull()?.totalStudents == 50)
    }

    @Test
    fun getDashboardStats_noData_returnsFailure() = runTest {
        // Arrange
        val repo = DashboardRepository(apiService)
        val apiResponse = ApiResponse<DashboardStats>(
            success = true,
            data = null,
            message = "Success",
            error = null,
        )
        whenever(apiService.getDashboardStats("1")).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getDashboardStats("1")

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message?.contains("통계 데이터를 찾을 수 없습니다") == true)
    }

    @Test
    fun getDashboardStats_apiFailure_returnsFailure() = runTest {
        // Arrange
        val repo = DashboardRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"error":"Failed"}""")
        whenever(apiService.getDashboardStats("1")).thenReturn(Response.error(500, errorBody))

        // Act
        val result = repo.getDashboardStats("1")

        // Assert
        assert(result.isFailure)
        // Response.error를 반환하면 response.body()가 null이므로 기본 메시지 반환
        assert(result.exceptionOrNull()?.message?.contains("통계 데이터를 가져오는데 실패했습니다") == true)
    }

    @Test
    fun getDashboardStats_apiFailure_noErrorBody_returnsDefaultError() = runTest {
        // Arrange
        val repo = DashboardRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{}""")
        whenever(apiService.getDashboardStats("1")).thenReturn(Response.error(500, errorBody))

        // Act
        val result = repo.getDashboardStats("1")

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message?.contains("통계 데이터를 가져오는데 실패했습니다") == true)
    }

    @Test
    fun getDashboardStats_networkException_returnsFailure() = runTest {
        // Arrange
        val repo = DashboardRepository(apiService)
        whenever(apiService.getDashboardStats("1")).thenThrow(RuntimeException("Network error"))

        // Act
        val result = repo.getDashboardStats("1")

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun getDashboardStats_zeroValues_returnsStats() = runTest {
        // Arrange
        val repo = DashboardRepository(apiService)
        val stats = DashboardStats(
            totalAssignments = 0,
            totalStudents = 0,
            totalClasses = 0,
            completedAssignments = 0,
            inProgressAssignments = 0,
        )
        val apiResponse = ApiResponse(
            success = true,
            data = stats,
            message = "Success",
            error = null,
        )
        whenever(apiService.getDashboardStats("1")).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getDashboardStats("1")

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull()?.totalAssignments == 0)
        assert(result.getOrNull()?.totalStudents == 0)
    }

    @Test
    fun getDashboardStats_successFalse_returnsFailure() = runTest {
        // Arrange
        val repo = DashboardRepository(apiService)
        val apiResponse = ApiResponse<DashboardStats>(
            success = false,
            data = null,
            message = null,
            error = "Failed to load stats",
        )
        whenever(apiService.getDashboardStats("1")).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getDashboardStats("1")

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message?.contains("Failed to load stats") == true)
    }

    @Test
    fun getDashboardStats_successFalse_noError_returnsDefaultMessage() = runTest {
        // Arrange
        val repo = DashboardRepository(apiService)
        val apiResponse = ApiResponse<DashboardStats>(
            success = false,
            data = null,
            message = null,
            error = null,
        )
        whenever(apiService.getDashboardStats("1")).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getDashboardStats("1")

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message?.contains("통계 데이터를 가져오는데 실패했습니다") == true)
    }
}
