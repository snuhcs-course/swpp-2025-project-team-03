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
        val result = ErrorMessageMapper.getErrorMessage(null as Throwable?)
        assertEquals("알 수 없는 오류가 발생했습니다.", result)
    }

    @Test
    fun getErrorMessage_connectException_returnsNetworkMessage() {
        val exception = ConnectException("Connection refused")
        val result = ErrorMessageMapper.getErrorMessage(exception)
        assertTrue(result.contains("네트워크 연결"))
    }

    @Test
    fun getErrorMessage_unknownHostException_returnsNetworkMessage() {
        val exception = UnknownHostException("Unable to resolve host")
        val result = ErrorMessageMapper.getErrorMessage(exception)

        // Assert
        assertTrue(result.contains("네트워크 연결"))
    }

    @Test
    fun getErrorMessage_socketTimeoutException_returnsTimeoutMessage() {
        val exception = SocketTimeoutException("Read timed out")
        val result = ErrorMessageMapper.getErrorMessage(exception)
        assertTrue(result.contains("요청 시간이 초과"))
    }

    @Test
    fun getErrorMessage_sslException_returnsSecurityMessage() {
        val exception = SSLException("SSL handshake failed")
        val result = ErrorMessageMapper.getErrorMessage(exception)
        assertTrue(result.contains("보안 연결"))
    }

    @Test
    fun getErrorMessage_failedToConnectMessage_returnsNetworkMessage() {
        val exception = Exception("Failed to connect to host")
        val result = ErrorMessageMapper.getErrorMessage(exception)
        assertTrue(result.contains("네트워크 연결"))
    }

    @Test
    fun getErrorMessage_unableToResolveHostMessage_returnsNetworkMessage() {
        val exception = Exception("Unable to resolve host example.com")
        val result = ErrorMessageMapper.getErrorMessage(exception)
        assertTrue(result.contains("네트워크 연결"))
    }

    @Test
    fun getErrorMessage_connectionRefusedMessage_returnsNetworkMessage() {
        val exception = Exception("Connection refused")
        val result = ErrorMessageMapper.getErrorMessage(exception)
        assertTrue(result.contains("네트워크 연결"))
    }

    @Test
    fun getErrorMessage_timeoutMessage_returnsTimeoutMessage() {
        val exception = Exception("Request timeout occurred")
        val result = ErrorMessageMapper.getErrorMessage(exception)
        assertTrue(result.contains("요청 시간이 초과"))
    }

    @Test
    fun getErrorMessage_timedOutMessage_returnsTimeoutMessage() {
        val exception = Exception("Operation timed out")
        val result = ErrorMessageMapper.getErrorMessage(exception)
        assertTrue(result.contains("요청 시간이 초과"))
    }

    @Test
    fun getErrorMessage_http500Message_returnsServerErrorMessage() {
        val exception = Exception("HTTP 500 Internal Server Error")
        val result = ErrorMessageMapper.getErrorMessage(exception)
        assertTrue(result.contains("서버에서 오류가 발생"))
    }

    @Test
    fun getErrorMessage_http404Message_returnsNotFoundMessage() {
        val exception = Exception("HTTP 404 Not Found")
        val result = ErrorMessageMapper.getErrorMessage(exception)
        assertTrue(result.contains("요청한 정보를 찾을 수 없습니다"))
    }

    @Test
    fun getErrorMessage_http403Message_returnsForbiddenMessage() {
        val exception = Exception("HTTP 403 Forbidden")
        val result = ErrorMessageMapper.getErrorMessage(exception)
        assertTrue(result.contains("접근 권한이 없습니다"))
    }

    @Test
    fun getErrorMessage_http401Message_returnsUnauthorizedMessage() {
        val exception = Exception("HTTP 401 Unauthorized")
        val result = ErrorMessageMapper.getErrorMessage(exception)
        assertTrue(result.contains("인증이 필요합니다"))
    }

    @Test
    fun getErrorMessage_javaExceptionMessage_returnsGenericMessage() {
        val exception = Exception("java.lang.NullPointerException")
        val result = ErrorMessageMapper.getErrorMessage(exception)
        assertTrue(result.contains("오류가 발생했습니다"))
    }

    @Test
    fun getErrorMessage_kotlinExceptionMessage_returnsGenericMessage() {
        val exception = Exception("kotlin.KotlinNullPointerException")
        val result = ErrorMessageMapper.getErrorMessage(exception)
        assertTrue(result.contains("오류가 발생했습니다"))
    }

    @Test
    fun getErrorMessage_exceptionWithStackTrace_returnsGenericMessage() {
        val exception = Exception("Error at com.example.Test.method(Test.kt:10)")
        val result = ErrorMessageMapper.getErrorMessage(exception)
        assertTrue(result.contains("오류가 발생했습니다"))
    }

    @Test
    fun getErrorMessage_longMessage_returnsGenericMessage() {
        val longMessage = "A".repeat(150)
        val exception = Exception(longMessage)
        val result = ErrorMessageMapper.getErrorMessage(exception)
        assertTrue(result.contains("오류가 발생했습니다"))
    }

    @Test
    fun getErrorMessage_shortUserFriendlyMessage_returnsMessageAsIs() {
        val exception = Exception("사용자 친화적인 메시지")
        val result = ErrorMessageMapper.getErrorMessage(exception)
        assertEquals("사용자 친화적인 메시지", result)
    }

    @Test
    fun getErrorMessage_nullMessage_returnsDefaultMessage() {
        val exception = Exception(null as String?)
        val result = ErrorMessageMapper.getErrorMessage(exception)
        assertEquals("알 수 없는 오류가 발생했습니다.", result)
    }

    @Test
    fun getErrorMessage_nullString_returnsDefaultMessage() {
        val result = ErrorMessageMapper.getErrorMessage(null as String?)
        assertEquals("알 수 없는 오류가 발생했습니다.", result)
    }

    @Test
    fun getErrorMessage_blankString_returnsDefaultMessage() {
        val result = ErrorMessageMapper.getErrorMessage("   ")
        assertEquals("알 수 없는 오류가 발생했습니다.", result)
    }

    @Test
    fun getErrorMessage_failedToConnectString_returnsNetworkMessage() {
        val result = ErrorMessageMapper.getErrorMessage("Failed to connect to host")
        assertTrue(result.contains("네트워크 연결"))
    }

    @Test
    fun getErrorMessage_timeoutString_returnsTimeoutMessage() {
        val result = ErrorMessageMapper.getErrorMessage("Request timeout occurred")
        assertTrue(result.contains("요청 시간이 초과"))
    }

    @Test
    fun getErrorMessage_http500String_returnsServerErrorMessage() {
        val result = ErrorMessageMapper.getErrorMessage("HTTP 500 Internal Server Error")
        assertTrue(result.contains("서버에서 오류가 발생"))
    }

    @Test
    fun getErrorMessage_http404String_returnsNotFoundMessage() {
        val result = ErrorMessageMapper.getErrorMessage("HTTP 404 Not Found")
        assertTrue(result.contains("요청한 정보를 찾을 수 없습니다"))
    }

    @Test
    fun getErrorMessage_http403String_returnsForbiddenMessage() {
        val result = ErrorMessageMapper.getErrorMessage("HTTP 403 Forbidden")
        assertTrue(result.contains("접근 권한이 없습니다"))
    }

    @Test
    fun getErrorMessage_http401String_returnsUnauthorizedMessage() {
        val result = ErrorMessageMapper.getErrorMessage("HTTP 401 Unauthorized")
        assertTrue(result.contains("인증이 필요합니다"))
    }

    @Test
    fun getErrorMessage_javaExceptionString_returnsGenericMessage() {
        val result = ErrorMessageMapper.getErrorMessage("java.lang.NullPointerException")
        assertTrue(result.contains("오류가 발생했습니다"))
    }

    @Test
    fun getErrorMessage_longString_returnsGenericMessage() {
        val longMessage = "A".repeat(150)
        val result = ErrorMessageMapper.getErrorMessage(longMessage)
        assertTrue(result.contains("오류가 발생했습니다"))
    }

    @Test
    fun getErrorMessage_stringWithStackTrace_returnsGenericMessage() {
        val result = ErrorMessageMapper.getErrorMessage("Error at com.example.Test.method(Test.kt:10)")
        assertTrue(result.contains("오류가 발생했습니다"))
    }

    @Test
    fun getErrorMessage_shortUserFriendlyString_returnsStringAsIs() {
        val result = ErrorMessageMapper.getErrorMessage("사용자 친화적인 메시지")
        assertEquals("사용자 친화적인 메시지", result)
    }
}
