package com.example.voicetutor.ui.viewmodel

import org.junit.Assert.*
import org.junit.Test

class SignupUiModelsTest {

    @Test
    fun signupField_enumValues_areCorrect() {
        assertEquals(SignupField.NAME, SignupField.valueOf("NAME"))
        assertEquals(SignupField.EMAIL, SignupField.valueOf("EMAIL"))
        assertEquals(SignupField.PASSWORD, SignupField.valueOf("PASSWORD"))
        assertEquals(SignupField.CONFIRM_PASSWORD, SignupField.valueOf("CONFIRM_PASSWORD"))
    }

    @Test
    fun signupField_values_containsAllFields() {
        val values = SignupField.entries

        assertEquals(4, values.size)
        assertTrue(values.contains(SignupField.NAME))
        assertTrue(values.contains(SignupField.EMAIL))
        assertTrue(values.contains(SignupField.PASSWORD))
        assertTrue(values.contains(SignupField.CONFIRM_PASSWORD))
    }

    @Test
    fun signupErrorInput_hasFieldAndMessage() {
        val field = SignupField.EMAIL
        val message = "이메일 형식이 올바르지 않습니다"

        val error = SignupError.Input(field, message)

        assertEquals(field, error.field)
        assertEquals(message, error.message)
    }

    @Test
    fun signupErrorGeneralDuplicateEmail_hasMessage() {
        val message = "이미 사용 중인 이메일입니다"

        val error = SignupError.General.DuplicateEmail(message)

        assertEquals(message, error.message)
        assertFalse(error.canRetry)
    }

    @Test
    fun signupErrorGeneralServer_hasMessage() {
        val message = "서버 오류"

        val error = SignupError.General.Server(message)

        assertEquals(message, error.message)
        assertTrue(error.canRetry)
    }

    @Test
    fun signupErrorGeneralNetwork_hasMessage() {
        val message = "네트워크 오류"

        val error = SignupError.General.Network(message)

        assertEquals(message, error.message)
        assertTrue(error.canRetry)
    }

    @Test
    fun signupErrorGeneralUnknown_hasMessage() {
        val message = "알 수 없는 오류"

        val error = SignupError.General.Unknown(message)

        assertEquals(message, error.message)
        assertTrue(error.canRetry)
    }

    @Test
    fun signupError_isSealedClass() {
        val inputError = SignupError.Input(SignupField.EMAIL, "Error")
        val generalError = SignupError.General.DuplicateEmail("Error")

        assertNotNull(inputError)
        assertNotNull(generalError)
    }
}
