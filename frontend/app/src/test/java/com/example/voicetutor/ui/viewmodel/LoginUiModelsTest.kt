package com.example.voicetutor.ui.viewmodel

import org.junit.Test
import org.junit.Assert.*

class LoginUiModelsTest {

    @Test
    fun loginField_enumValues_areCorrect() {
        // Then
        assertEquals(LoginField.EMAIL, LoginField.valueOf("EMAIL"))
        assertEquals(LoginField.PASSWORD, LoginField.valueOf("PASSWORD"))
    }

    @Test
    fun loginField_values_containsAllFields() {
        // When
        val values = LoginField.values()
        
        // Then
        assertEquals(2, values.size)
        assertTrue(values.contains(LoginField.EMAIL))
        assertTrue(values.contains(LoginField.PASSWORD))
    }

    @Test
    fun loginErrorInput_hasFieldAndMessage() {
        // Given
        val field = LoginField.EMAIL
        val message = "이메일 형식이 올바르지 않습니다"
        
        // When
        val error = LoginError.Input(field, message)
        
        // Then
        assertEquals(field, error.field)
        assertEquals(message, error.message)
    }

    @Test
    fun loginErrorGeneralInvalidCredentials_hasMessage() {
        // Given
        val message = "이메일 또는 비밀번호가 올바르지 않습니다"
        
        // When
        val error = LoginError.General.InvalidCredentials(message)
        
        // Then
        assertEquals(message, error.message)
        assertTrue(error.canRetry)
    }

    @Test
    fun loginErrorGeneralAccountNotFound_hasMessage() {
        // Given
        val message = "계정을 찾을 수 없습니다"
        
        // When
        val error = LoginError.General.AccountNotFound(message)
        
        // Then
        assertEquals(message, error.message)
        assertFalse(error.canRetry)
    }

    @Test
    fun loginErrorGeneralAccountLocked_hasMessage() {
        // Given
        val message = "계정이 잠겨 있습니다"
        
        // When
        val error = LoginError.General.AccountLocked(message)
        
        // Then
        assertEquals(message, error.message)
        assertFalse(error.canRetry)
    }

    @Test
    fun loginErrorGeneralServer_hasMessage() {
        // Given
        val message = "서버 오류"
        
        // When
        val error = LoginError.General.Server(message)
        
        // Then
        assertEquals(message, error.message)
        assertTrue(error.canRetry)
    }

    @Test
    fun loginErrorGeneralNetwork_hasMessage() {
        // Given
        val message = "네트워크 오류"
        
        // When
        val error = LoginError.General.Network(message)
        
        // Then
        assertEquals(message, error.message)
        assertTrue(error.canRetry)
    }

    @Test
    fun loginErrorGeneralUnknown_hasMessage() {
        // Given
        val message = "알 수 없는 오류"
        
        // When
        val error = LoginError.General.Unknown(message)
        
        // Then
        assertEquals(message, error.message)
        assertTrue(error.canRetry)
    }

    @Test
    fun loginError_isSealedClass() {
        // Given
        val inputError = LoginError.Input(LoginField.EMAIL, "Error")
        val generalError = LoginError.General.InvalidCredentials("Error")
        
        // Then
        assertTrue(inputError is LoginError)
        assertTrue(generalError is LoginError)
    }
}

