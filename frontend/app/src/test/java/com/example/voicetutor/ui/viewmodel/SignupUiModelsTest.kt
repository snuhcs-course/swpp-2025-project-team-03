package com.example.voicetutor.ui.viewmodel

import org.junit.Test
import org.junit.Assert.*

class SignupUiModelsTest {

    @Test
    fun signupField_enumValues_areCorrect() {
        // Then
        assertEquals(SignupField.NAME, SignupField.valueOf("NAME"))
        assertEquals(SignupField.EMAIL, SignupField.valueOf("EMAIL"))
        assertEquals(SignupField.PASSWORD, SignupField.valueOf("PASSWORD"))
        assertEquals(SignupField.CONFIRM_PASSWORD, SignupField.valueOf("CONFIRM_PASSWORD"))
    }

    @Test
    fun signupField_values_containsAllFields() {
        // When
        val values = SignupField.values()
        
        // Then
        assertEquals(4, values.size)
        assertTrue(values.contains(SignupField.NAME))
        assertTrue(values.contains(SignupField.EMAIL))
        assertTrue(values.contains(SignupField.PASSWORD))
        assertTrue(values.contains(SignupField.CONFIRM_PASSWORD))
    }

    @Test
    fun signupErrorInput_hasFieldAndMessage() {
        // Given
        val field = SignupField.EMAIL
        val message = "이메일 형식이 올바르지 않습니다"
        
        // When
        val error = SignupError.Input(field, message)
        
        // Then
        assertEquals(field, error.field)
        assertEquals(message, error.message)
    }

    @Test
    fun signupErrorGeneralDuplicateEmail_hasMessage() {
        // Given
        val message = "이미 사용 중인 이메일입니다"
        
        // When
        val error = SignupError.General.DuplicateEmail(message)
        
        // Then
        assertEquals(message, error.message)
        assertFalse(error.canRetry)
    }

    @Test
    fun signupErrorGeneralServer_hasMessage() {
        // Given
        val message = "서버 오류"
        
        // When
        val error = SignupError.General.Server(message)
        
        // Then
        assertEquals(message, error.message)
        assertTrue(error.canRetry)
    }

    @Test
    fun signupErrorGeneralNetwork_hasMessage() {
        // Given
        val message = "네트워크 오류"
        
        // When
        val error = SignupError.General.Network(message)
        
        // Then
        assertEquals(message, error.message)
        assertTrue(error.canRetry)
    }

    @Test
    fun signupErrorGeneralUnknown_hasMessage() {
        // Given
        val message = "알 수 없는 오류"
        
        // When
        val error = SignupError.General.Unknown(message)
        
        // Then
        assertEquals(message, error.message)
        assertTrue(error.canRetry)
    }

    @Test
    fun signupError_isSealedClass() {
        // Given
        val inputError = SignupError.Input(SignupField.EMAIL, "Error")
        val generalError = SignupError.General.DuplicateEmail("Error")
        
        // Then
        assertTrue(inputError is SignupError)
        assertTrue(generalError is SignupError)
    }
}

