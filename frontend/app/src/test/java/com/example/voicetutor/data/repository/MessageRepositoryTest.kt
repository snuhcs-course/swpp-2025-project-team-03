package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.ApiResponse
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
class MessageRepositoryTest {

    @Mock
    lateinit var apiService: ApiService

    private fun buildSendMessageResponse() = SendMessageResponse(
        messageId = "msg123",
        sentCount = 5,
        failedCount = 0,
        timestamp = 1735689600000L // 2025-01-01 as timestamp
    )

    private fun buildMessage(id: String = "msg1") = Message(
        id = id,
        teacherId = 1,
        teacherName = "Teacher1",
        studentId = 1,
        studentName = "Student1",
        content = "Test message",
        timestamp = 1735689600000L // 2025-01-01 as timestamp
    )

    private fun buildMessageListResponse(messages: List<Message> = emptyList()) = MessageListResponse(
        messages = messages,
        totalCount = messages.size
    )

    @Test
    fun sendMessage_success_returnsResponse() = runTest {
        // Arrange
        val repo = MessageRepository(apiService)
        val response = buildSendMessageResponse()
        val apiResponse = ApiResponse(
            success = true,
            data = response,
            message = "Success",
            error = null
        )
        whenever(apiService.sendMessage(org.mockito.kotlin.any())).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.sendMessage(teacherId = 1, studentIds = listOf(1, 2), content = "Hello")

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == response)
        assert(result.getOrNull()?.sentCount == 5)
    }

    @Test
    fun sendMessage_noData_returnsFailure() = runTest {
        // Arrange
        val repo = MessageRepository(apiService)
        val apiResponse = ApiResponse<SendMessageResponse>(
            success = true,
            data = null,
            message = "Success",
            error = null
        )
        whenever(apiService.sendMessage(org.mockito.kotlin.any())).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.sendMessage(teacherId = 1, studentIds = listOf(1), content = "Hello")

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message?.contains("메시지 전송 결과를 받을 수 없습니다") == true)
    }

    @Test
    fun sendMessage_apiFailure_returnsFailure() = runTest {
        // Arrange
        val repo = MessageRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"message":"Failed"}""")
        whenever(apiService.sendMessage(org.mockito.kotlin.any())).thenReturn(Response.error(500, errorBody))

        // Act
        val result = repo.sendMessage(teacherId = 1, studentIds = listOf(1), content = "Hello")

        // Assert
        assert(result.isFailure)
        // Response.error를 반환하면 response.body()가 null이므로 기본 메시지 반환
        assert(result.exceptionOrNull()?.message?.contains("메시지 전송에 실패했습니다") == true)
    }

    @Test
    fun sendMessage_apiFailure_noMessage_returnsDefaultError() = runTest {
        // Arrange
        val repo = MessageRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{}""")
        whenever(apiService.sendMessage(org.mockito.kotlin.any())).thenReturn(Response.error(500, errorBody))

        // Act
        val result = repo.sendMessage(teacherId = 1, studentIds = listOf(1), content = "Hello")

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message?.contains("메시지 전송에 실패했습니다") == true)
    }

    @Test
    fun sendMessage_networkException_returnsFailure() = runTest {
        // Arrange
        val repo = MessageRepository(apiService)
        whenever(apiService.sendMessage(org.mockito.kotlin.any())).thenThrow(RuntimeException("Network error"))

        // Act
        val result = repo.sendMessage(teacherId = 1, studentIds = listOf(1), content = "Hello")

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun sendMessage_emptyStudentList_returnsResponse() = runTest {
        // Arrange
        val repo = MessageRepository(apiService)
        val response = SendMessageResponse(
            messageId = "msg123",
            sentCount = 0,
            failedCount = 0,
            timestamp = 1735689600000L // 2025-01-01 as timestamp
        )
        val apiResponse = ApiResponse(
            success = true,
            data = response,
            message = "Success",
            error = null
        )
        whenever(apiService.sendMessage(org.mockito.kotlin.any())).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.sendMessage(teacherId = 1, studentIds = emptyList(), content = "Hello")

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull()?.sentCount == 0)
    }

    @Test
    fun getMessages_success_returnsMessageList() = runTest {
        // Arrange
        val repo = MessageRepository(apiService)
        val messages = listOf(buildMessage("msg1"), buildMessage("msg2"))
        val response = buildMessageListResponse(messages)
        val apiResponse = ApiResponse(
            success = true,
            data = response,
            message = "Success",
            error = null
        )
        whenever(apiService.getMessages(1, null, 50, 0)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getMessages(userId = 1)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == response)
        assert(result.getOrNull()?.messages?.size == 2)
        assert(result.getOrNull()?.totalCount == 2)
    }

    @Test
    fun getMessages_withMessageType_success_returnsFilteredList() = runTest {
        // Arrange
        val repo = MessageRepository(apiService)
        val messages = listOf(buildMessage("msg1"))
        val response = buildMessageListResponse(messages)
        val apiResponse = ApiResponse(
            success = true,
            data = response,
            message = "Success",
            error = null
        )
        whenever(apiService.getMessages(1, "sent", 50, 0)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getMessages(userId = 1, messageType = "sent")

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull()?.messages?.size == 1)
    }

    @Test
    fun getMessages_withLimitAndOffset_success_returnsPaginatedList() = runTest {
        // Arrange
        val repo = MessageRepository(apiService)
        val messages = listOf(buildMessage("msg1"))
        val response = buildMessageListResponse(messages)
        val apiResponse = ApiResponse(
            success = true,
            data = response,
            message = "Success",
            error = null
        )
        whenever(apiService.getMessages(1, null, 10, 20)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getMessages(userId = 1, limit = 10, offset = 20)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull()?.messages?.size == 1)
    }

    @Test
    fun getMessages_emptyList_returnsEmptyList() = runTest {
        // Arrange
        val repo = MessageRepository(apiService)
        val response = buildMessageListResponse(emptyList())
        val apiResponse = ApiResponse(
            success = true,
            data = response,
            message = "Success",
            error = null
        )
        whenever(apiService.getMessages(1, null, 50, 0)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getMessages(userId = 1)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull()?.messages?.isEmpty() == true)
        assert(result.getOrNull()?.totalCount == 0)
    }

    @Test
    fun getMessages_noData_returnsFailure() = runTest {
        // Arrange
        val repo = MessageRepository(apiService)
        val apiResponse = ApiResponse<MessageListResponse>(
            success = true,
            data = null,
            message = "Success",
            error = null
        )
        whenever(apiService.getMessages(1, null, 50, 0)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getMessages(userId = 1)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message?.contains("메시지 목록을 받을 수 없습니다") == true)
    }

    @Test
    fun getMessages_apiFailure_returnsFailure() = runTest {
        // Arrange
        val repo = MessageRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"message":"Failed"}""")
        whenever(apiService.getMessages(1, null, 50, 0)).thenReturn(Response.error(500, errorBody))

        // Act
        val result = repo.getMessages(userId = 1)

        // Assert
        assert(result.isFailure)
        // Response.error를 반환하면 response.body()가 null이므로 기본 메시지 반환
        assert(result.exceptionOrNull()?.message?.contains("메시지 목록을 가져오는데 실패했습니다") == true)
    }

    @Test
    fun getMessages_apiFailure_noMessage_returnsDefaultError() = runTest {
        // Arrange
        val repo = MessageRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{}""")
        whenever(apiService.getMessages(1, null, 50, 0)).thenReturn(Response.error(500, errorBody))

        // Act
        val result = repo.getMessages(userId = 1)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message?.contains("메시지 목록을 가져오는데 실패했습니다") == true)
    }

    @Test
    fun getMessages_networkException_returnsFailure() = runTest {
        // Arrange
        val repo = MessageRepository(apiService)
        whenever(apiService.getMessages(1, null, 50, 0)).thenThrow(RuntimeException("Network error"))

        // Act
        val result = repo.getMessages(userId = 1)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "Network error")
    }
}

