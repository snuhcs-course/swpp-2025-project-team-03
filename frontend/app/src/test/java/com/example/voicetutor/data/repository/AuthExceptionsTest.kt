package com.example.voicetutor.data.repository

import org.junit.Assert.*
import org.junit.Test

class AuthExceptionsTest {

    @Test
    fun loginExceptionInvalidCredentials_hasMessage() {
        // Given
        val message = "이메일 또는 비밀번호가 올바르지 않습니다"

        // When
        val exception = LoginException.InvalidCredentials(message)

        // Then
        assertEquals(message, exception.message)
    }

    @Test
    fun loginExceptionAccountNotFound_hasMessage() {
        // Given
        val message = "계정을 찾을 수 없습니다"

        // When
        val exception = LoginException.AccountNotFound(message)

        // Then
        assertEquals(message, exception.message)
    }

    @Test
    fun loginExceptionAccountLocked_hasMessage() {
        // Given
        val message = "계정이 잠겨 있습니다"

        // When
        val exception = LoginException.AccountLocked(message)

        // Then
        assertEquals(message, exception.message)
    }

    @Test
    fun loginExceptionServer_hasMessage() {
        // Given
        val message = "서버 오류가 발생했습니다"

        // When
        val exception = LoginException.Server(message)

        // Then
        assertEquals(message, exception.message)
    }

    @Test
    fun loginExceptionNetwork_hasMessageAndCause() {
        // Given
        val message = "네트워크 오류"
        val cause = java.io.IOException("Connection failed")

        // When
        val exception = LoginException.Network(message, cause)

        // Then
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun loginExceptionNetwork_withoutCause_hasMessage() {
        // Given
        val message = "네트워크 오류"

        // When
        val exception = LoginException.Network(message)

        // Then
        assertEquals(message, exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun loginExceptionUnknown_hasMessage() {
        // Given
        val message = "알 수 없는 오류"

        // When
        val exception = LoginException.Unknown(message)

        // Then
        assertEquals(message, exception.message)
    }

    @Test
    fun signupExceptionDuplicateEmail_hasMessage() {
        // Given
        val message = "이미 사용 중인 이메일입니다"

        // When
        val exception = SignupException.DuplicateEmail(message)

        // Then
        assertEquals(message, exception.message)
    }

    @Test
    fun signupExceptionServer_hasMessage() {
        // Given
        val message = "서버 오류가 발생했습니다"

        // When
        val exception = SignupException.Server(message)

        // Then
        assertEquals(message, exception.message)
    }

    @Test
    fun signupExceptionNetwork_hasMessageAndCause() {
        // Given
        val message = "네트워크 오류"
        val cause = java.io.IOException("Connection failed")

        // When
        val exception = SignupException.Network(message, cause)

        // Then
        assertEquals(message, exception.message)
        assertEquals(cause, exception.cause)
    }

    @Test
    fun signupExceptionNetwork_withoutCause_hasMessage() {
        // Given
        val message = "네트워크 오류"

        // When
        val exception = SignupException.Network(message)

        // Then
        assertEquals(message, exception.message)
        assertNull(exception.cause)
    }

    @Test
    fun signupExceptionUnknown_hasMessage() {
        // Given
        val message = "알 수 없는 오류"

        // When
        val exception = SignupException.Unknown(message)

        // Then
        assertEquals(message, exception.message)
    }

    @Test
    fun loginException_isException() {
        // Given
        val exception = LoginException.InvalidCredentials("Test")

        // Then
        assertTrue(exception is Exception)
    }

    @Test
    fun signupException_isException() {
        // Given
        val exception = SignupException.DuplicateEmail("Test")

        // Then
        assertTrue(exception is Exception)
    }
}
