package com.example.voicetutor.ui.utils

import org.junit.Assert.*
import org.junit.Test
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

class ErrorMessageMapperTest {

    @Test
    fun getErrorMessage_nullException_returnsDefaultMessage() {
        // Act
        val result = ErrorMessageMapper.getErrorMessage(null as Throwable?)

        // Assert
        assertEquals("알 수 없는 오류가 발생했습니다.", result)
    }

    @Test
    fun getErrorMessage_connectException_returnsNetworkMessage() {
        // Arrange
        val exception = ConnectException("Connection refused")

        // Act
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertTrue(result.contains("네트워크 연결"))
    }

    @Test
    fun getErrorMessage_unknownHostException_returnsNetworkMessage() {
        // Arrange
        val exception = UnknownHostException("Unable to resolve host")

        // Act
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertTrue(result.contains("네트워크 연결"))
    }

    @Test
    fun getErrorMessage_socketTimeoutException_returnsTimeoutMessage() {
        // Arrange
        val exception = SocketTimeoutException("Read timed out")

        // Act
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertTrue(result.contains("요청 시간이 초과"))
    }

    @Test
    fun getErrorMessage_sslException_returnsSecurityMessage() {
        // Arrange
        val exception = SSLException("SSL handshake failed")

        // Act
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertTrue(result.contains("보안 연결"))
    }

    @Test
    fun getErrorMessage_failedToConnectMessage_returnsNetworkMessage() {
        // Arrange
        val exception = Exception("Failed to connect to host")

        // Act
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertTrue(result.contains("네트워크 연결"))
    }

    @Test
    fun getErrorMessage_unableToResolveHostMessage_returnsNetworkMessage() {
        // Arrange
        val exception = Exception("Unable to resolve host example.com")

        // Act
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertTrue(result.contains("네트워크 연결"))
    }

    @Test
    fun getErrorMessage_connectionRefusedMessage_returnsNetworkMessage() {
        // Arrange
        val exception = Exception("Connection refused")

        // Act
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertTrue(result.contains("네트워크 연결"))
    }

    @Test
    fun getErrorMessage_timeoutMessage_returnsTimeoutMessage() {
        // Arrange
        val exception = Exception("Request timeout occurred")

        // Act
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertTrue(result.contains("요청 시간이 초과"))
    }

    @Test
    fun getErrorMessage_timedOutMessage_returnsTimeoutMessage() {
        // Arrange
        val exception = Exception("Operation timed out")

        // Act
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertTrue(result.contains("요청 시간이 초과"))
    }

    @Test
    fun getErrorMessage_http500Message_returnsServerErrorMessage() {
        // Arrange
        val exception = Exception("HTTP 500 Internal Server Error")

        // Act
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertTrue(result.contains("서버에서 오류가 발생"))
    }

    @Test
    fun getErrorMessage_http404Message_returnsNotFoundMessage() {
        // Arrange
        val exception = Exception("HTTP 404 Not Found")

        // Act
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertTrue(result.contains("요청한 정보를 찾을 수 없습니다"))
    }

    @Test
    fun getErrorMessage_http403Message_returnsForbiddenMessage() {
        // Arrange
        val exception = Exception("HTTP 403 Forbidden")

        // Act
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertTrue(result.contains("접근 권한이 없습니다"))
    }

    @Test
    fun getErrorMessage_http401Message_returnsUnauthorizedMessage() {
        // Arrange
        val exception = Exception("HTTP 401 Unauthorized")

        // Act
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertTrue(result.contains("인증이 필요합니다"))
    }

    @Test
    fun getErrorMessage_javaExceptionMessage_returnsGenericMessage() {
        // Arrange
        val exception = Exception("java.lang.NullPointerException")

        // Act
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertTrue(result.contains("오류가 발생했습니다"))
    }

    @Test
    fun getErrorMessage_kotlinExceptionMessage_returnsGenericMessage() {
        // Arrange
        val exception = Exception("kotlin.KotlinNullPointerException")

        // Act
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertTrue(result.contains("오류가 발생했습니다"))
    }

    @Test
    fun getErrorMessage_exceptionWithStackTrace_returnsGenericMessage() {
        // Arrange
        val exception = Exception("Error at com.example.Test.method(Test.kt:10)")

        // Act
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertTrue(result.contains("오류가 발생했습니다"))
    }

    @Test
    fun getErrorMessage_longMessage_returnsGenericMessage() {
        // Arrange
        val longMessage = "A".repeat(150)
        val exception = Exception(longMessage)

        // Act
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertTrue(result.contains("오류가 발생했습니다"))
    }

    @Test
    fun getErrorMessage_shortUserFriendlyMessage_returnsMessageAsIs() {
        // Arrange
        val exception = Exception("사용자 친화적인 메시지")

        // Act
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertEquals("사용자 친화적인 메시지", result)
    }

    @Test
    fun getErrorMessage_nullMessage_returnsDefaultMessage() {
        // Arrange
        val exception = Exception(null as String?)

        // Act
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertEquals("알 수 없는 오류가 발생했습니다.", result)
    }

    // String overload tests
    @Test
    fun getErrorMessage_nullString_returnsDefaultMessage() {
        // Act
        val result = ErrorMessageMapper.getErrorMessage(null as String?)

        // Assert
        assertEquals("알 수 없는 오류가 발생했습니다.", result)
    }

    @Test
    fun getErrorMessage_blankString_returnsDefaultMessage() {
        // Act
        val result = ErrorMessageMapper.getErrorMessage("   ")

        // Assert
        assertEquals("알 수 없는 오류가 발생했습니다.", result)
    }

    @Test
    fun getErrorMessage_failedToConnectString_returnsNetworkMessage() {
        // Act
        val result = ErrorMessageMapper.getErrorMessage("Failed to connect to host")

        // Assert
        assertTrue(result.contains("네트워크 연결"))
    }

    @Test
    fun getErrorMessage_timeoutString_returnsTimeoutMessage() {
        // Act
        val result = ErrorMessageMapper.getErrorMessage("Request timeout occurred")

        // Assert
        assertTrue(result.contains("요청 시간이 초과"))
    }

    @Test
    fun getErrorMessage_http500String_returnsServerErrorMessage() {
        // Act
        val result = ErrorMessageMapper.getErrorMessage("HTTP 500 Internal Server Error")

        // Assert
        assertTrue(result.contains("서버에서 오류가 발생"))
    }

    @Test
    fun getErrorMessage_http404String_returnsNotFoundMessage() {
        // Act
        val result = ErrorMessageMapper.getErrorMessage("HTTP 404 Not Found")

        // Assert
        assertTrue(result.contains("요청한 정보를 찾을 수 없습니다"))
    }

    @Test
    fun getErrorMessage_http403String_returnsForbiddenMessage() {
        // Act
        val result = ErrorMessageMapper.getErrorMessage("HTTP 403 Forbidden")

        // Assert
        assertTrue(result.contains("접근 권한이 없습니다"))
    }

    @Test
    fun getErrorMessage_http401String_returnsUnauthorizedMessage() {
        // Act
        val result = ErrorMessageMapper.getErrorMessage("HTTP 401 Unauthorized")

        // Assert
        assertTrue(result.contains("인증이 필요합니다"))
    }

    @Test
    fun getErrorMessage_javaExceptionString_returnsGenericMessage() {
        // Act
        val result = ErrorMessageMapper.getErrorMessage("java.lang.NullPointerException")

        // Assert
        assertTrue(result.contains("오류가 발생했습니다"))
    }

    @Test
    fun getErrorMessage_longString_returnsGenericMessage() {
        // Arrange
        val longMessage = "A".repeat(150)

        // Act
        val result = ErrorMessageMapper.getErrorMessage(longMessage)

        // Assert
        assertTrue(result.contains("오류가 발생했습니다"))
    }

    @Test
    fun getErrorMessage_stringWithStackTrace_returnsGenericMessage() {
        // Act
        val result = ErrorMessageMapper.getErrorMessage("Error at com.example.Test.method(Test.kt:10)")

        // Assert
        assertTrue(result.contains("오류가 발생했습니다"))
    }

    @Test
    fun getErrorMessage_shortUserFriendlyString_returnsStringAsIs() {
        // Act
        val result = ErrorMessageMapper.getErrorMessage("사용자 친화적인 메시지")

        // Assert
        assertEquals("사용자 친화적인 메시지", result)
    }
}

