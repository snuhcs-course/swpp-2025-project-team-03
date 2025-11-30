package com.example.voicetutor.ui.viewmodel

import org.junit.Assert.*
import org.junit.Test

class LoginUiModelsTest {

    @Test
    fun loginField_enumValues_areCorrect() {
        assertEquals(LoginField.EMAIL, LoginField.valueOf("EMAIL"))
        assertEquals(LoginField.PASSWORD, LoginField.valueOf("PASSWORD"))
    }

    @Test
    fun loginField_values_containsAllFields() {
        val values = LoginField.entries

        assertEquals(2, values.size)
        assertTrue(values.contains(LoginField.EMAIL))
        assertTrue(values.contains(LoginField.PASSWORD))
    }

    @Test
    fun loginErrorInput_hasFieldAndMessage() {
        val field = LoginField.EMAIL
        val message = "이메일 형식이 올바르지 않습니다"

        val error = LoginError.Input(field, message)

        assertEquals(field, error.field)
        assertEquals(message, error.message)
    }

    @Test
    fun loginErrorGeneralInvalidCredentials_hasMessage() {
        val message = "이메일 또는 비밀번호가 올바르지 않습니다"

        val error = LoginError.General.InvalidCredentials(message)

        assertEquals(message, error.message)
        assertTrue(error.canRetry)
    }

    @Test
    fun loginErrorGeneralAccountNotFound_hasMessage() {
        val message = "계정을 찾을 수 없습니다"

        val error = LoginError.General.AccountNotFound(message)

        assertEquals(message, error.message)
        assertFalse(error.canRetry)
    }

    @Test
    fun loginErrorGeneralAccountLocked_hasMessage() {
        val message = "계정이 잠겨 있습니다"

        val error = LoginError.General.AccountLocked(message)

        assertEquals(message, error.message)
        assertFalse(error.canRetry)
    }

    @Test
    fun loginErrorGeneralServer_hasMessage() {
        val message = "서버 오류"

        val error = LoginError.General.Server(message)

        assertEquals(message, error.message)
        assertTrue(error.canRetry)
    }

    @Test
    fun loginErrorGeneralNetwork_hasMessage() {
        val message = "네트워크 오류"

        val error = LoginError.General.Network(message)

        assertEquals(message, error.message)
        assertTrue(error.canRetry)
    }

    @Test
    fun loginErrorGeneralUnknown_hasMessage() {
        val message = "알 수 없는 오류"

        val error = LoginError.General.Unknown(message)

        assertEquals(message, error.message)
        assertTrue(error.canRetry)
    }

    @Test
    fun loginError_isSealedClass() {
        val inputError = LoginError.Input(LoginField.EMAIL, "Error")
        val generalError = LoginError.General.InvalidCredentials("Error")

        assertNotNull(inputError)
        assertNotNull(generalError)
    }
}
