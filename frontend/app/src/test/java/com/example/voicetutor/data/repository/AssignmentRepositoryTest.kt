package com.example.voicetutor.data.repository

import com.example.voicetutor.data.network.ApiService
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import retrofit2.Response
import kotlinx.coroutines.test.runTest

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
}


