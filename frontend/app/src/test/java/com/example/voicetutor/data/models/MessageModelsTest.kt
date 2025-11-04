package com.example.voicetutor.data.models

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

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
        assertEquals(1, request.teacherId)
        assertEquals(3, request.studentIds.size)
        assertEquals("Test message", request.content)
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
        assertTrue(request.studentIds.isEmpty())
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
        assertEquals("msg123", response.messageId)
        assertEquals(5, response.sentCount)
        assertEquals(0, response.failedCount)
        assertEquals(1735689600000L, response.timestamp)
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
        assertEquals(3, response.sentCount)
        assertEquals(2, response.failedCount)
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
        assertEquals("msg1", message.id)
        assertEquals(1, message.teacherId)
        assertEquals("Teacher1", message.teacherName)
        assertEquals(1, message.studentId)
        assertEquals("Hello", message.content)
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
        assertEquals(2, response.messages.size)
        assertEquals(2, response.totalCount)
    }

    @Test
    fun messageListResponse_withEmptyList_containsZeroCount() {
        // Arrange
        val response = MessageListResponse(
            messages = emptyList(),
            totalCount = 0
        )

        // Assert
        assertTrue(response.messages.isEmpty())
        assertEquals(0, response.totalCount)
    }
}

