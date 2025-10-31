package com.example.voicetutor.data.models

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import assert

@RunWith(JUnit4::class)
class MessageModelsTest {

    @Test
    fun sendMessageRequest_withAllFields_containsCorrectValues() {
        // Arrange
        val request = SendMessageRequest(
            teacherId = 1,
            studentIds = listOf(1, 2, 3),
            content = "Test message"
        )

        // Assert
        assert(request.teacherId == 1)
        assert(request.studentIds.size == 3)
        assert(request.content == "Test message")
    }

    @Test
    fun sendMessageRequest_withEmptyStudentList_handlesEmptyList() {
        // Arrange
        val request = SendMessageRequest(
            teacherId = 1,
            studentIds = emptyList(),
            content = "Test"
        )

        // Assert
        assert(request.studentIds.isEmpty())
    }

    @Test
    fun sendMessageResponse_withAllFields_containsCorrectValues() {
        // Arrange
        val response = SendMessageResponse(
            messageId = "msg123",
            sentCount = 5,
            failedCount = 0,
            timestamp = 1735689600000L
        )

        // Assert
        assert(response.messageId == "msg123")
        assert(response.sentCount == 5)
        assert(response.failedCount == 0)
        assert(response.timestamp == 1735689600000L)
    }

    @Test
    fun sendMessageResponse_withFailures_containsFailedCount() {
        // Arrange
        val response = SendMessageResponse(
            messageId = "msg123",
            sentCount = 3,
            failedCount = 2,
            timestamp = 1735689600000L
        )

        // Assert
        assert(response.sentCount == 3)
        assert(response.failedCount == 2)
    }

    @Test
    fun message_withAllFields_containsCorrectValues() {
        // Arrange
        val message = Message(
            id = "msg1",
            teacherId = 1,
            teacherName = "Teacher1",
            studentId = 1,
            studentName = "Student1",
            content = "Hello",
            timestamp = 1735689600000L
        )

        // Assert
        assert(message.id == "msg1")
        assert(message.teacherId == 1)
        assert(message.teacherName == "Teacher1")
        assert(message.studentId == 1)
        assert(message.content == "Hello")
    }

    @Test
    fun messageListResponse_withMessages_containsCorrectCount() {
        // Arrange
        val messages = listOf(
            Message(id = "msg1", teacherId = 1, teacherName = "T1", studentId = 1, studentName = "S1", content = "Hi", timestamp = 1735689600000L),
            Message(id = "msg2", teacherId = 1, teacherName = "T1", studentId = 2, studentName = "S2", content = "Hello", timestamp = 1735689600000L)
        )
        val response = MessageListResponse(
            messages = messages,
            totalCount = 2
        )

        // Assert
        assert(response.messages.size == 2)
        assert(response.totalCount == 2)
    }

    @Test
    fun messageListResponse_withEmptyList_containsZeroCount() {
        // Arrange
        val response = MessageListResponse(
            messages = emptyList(),
            totalCount = 0
        )

        // Assert
        assert(response.messages.isEmpty())
        assert(response.totalCount == 0)
    }
}

