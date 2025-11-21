package com.example.voicetutor.data.network

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*

@RunWith(MockitoJUnitRunner::class)
class TokenManagerTest {

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var sharedPreferences: SharedPreferences

    @Mock
    lateinit var editor: SharedPreferences.Editor

    @Mock
    lateinit var editorAfterPut: SharedPreferences.Editor

    private lateinit var tokenManager: TokenManager

    @Before
    fun setUp() {
        whenever(context.getSharedPreferences(eq("auth_prefs"), any())).thenReturn(sharedPreferences)
        whenever(sharedPreferences.edit()).thenReturn(editor)
        whenever(editor.putString(any(), any())).thenReturn(editorAfterPut)
        whenever(editor.remove(any())).thenReturn(editorAfterPut)
        // editorAfterPut.remove() is used in clearTokens() for method chaining
        whenever(editorAfterPut.remove(any())).thenReturn(editorAfterPut)

        tokenManager = TokenManager(context)
    }

    @Test
    fun saveAccessToken_savesToken() {
        // Arrange
        val token = "test_access_token"

        // Act
        tokenManager.saveAccessToken(token)

        // Assert
        verify(editor).putString("access_token", token)
        verify(editorAfterPut).apply()
    }

    @Test
    fun getAccessToken_returnsSavedToken() {
        // Arrange
        val token = "test_access_token"
        whenever(sharedPreferences.getString(eq("access_token"), isNull()))
            .thenReturn(token)

        // Act
        val result = tokenManager.getAccessToken()

        // Assert
        assertEquals(token, result)
        verify(sharedPreferences).getString("access_token", null)
    }

    @Test
    fun getAccessToken_whenNoToken_returnsNull() {
        // Arrange
        whenever(sharedPreferences.getString(eq("access_token"), isNull()))
            .thenReturn(null)

        // Act
        val result = tokenManager.getAccessToken()

        // Assert
        assertNull(result)
    }

    @Test
    fun saveRefreshToken_savesToken() {
        // Arrange
        val token = "test_refresh_token"

        // Act
        tokenManager.saveRefreshToken(token)

        // Assert
        verify(editor).putString("refresh_token", token)
        verify(editorAfterPut).apply()
    }

    @Test
    fun getRefreshToken_returnsSavedToken() {
        // Arrange
        val token = "test_refresh_token"
        whenever(sharedPreferences.getString(eq("refresh_token"), isNull()))
            .thenReturn(token)

        // Act
        val result = tokenManager.getRefreshToken()

        // Assert
        assertEquals(token, result)
        verify(sharedPreferences).getString("refresh_token", null)
    }

    @Test
    fun getRefreshToken_whenNoToken_returnsNull() {
        // Arrange
        whenever(sharedPreferences.getString(eq("refresh_token"), isNull()))
            .thenReturn(null)

        // Act
        val result = tokenManager.getRefreshToken()

        // Assert
        assertNull(result)
    }

    @Test
    fun clearTokens_removesBothTokens() {
        // Act
        tokenManager.clearTokens()

        // Assert
        verify(editor).remove("access_token")
        verify(editorAfterPut).remove("refresh_token")
        verify(editorAfterPut).apply()
    }

    @Test
    fun hasToken_whenTokenExists_returnsTrue() {
        // Arrange
        whenever(sharedPreferences.getString(eq("access_token"), isNull()))
            .thenReturn("test_token")

        // Act
        val result = tokenManager.hasToken()

        // Assert
        assertTrue(result)
    }

    @Test
    fun hasToken_whenNoToken_returnsFalse() {
        // Arrange
        whenever(sharedPreferences.getString(eq("access_token"), isNull()))
            .thenReturn(null)

        // Act
        val result = tokenManager.hasToken()

        // Assert
        assertFalse(result)
    }

    @Test
    fun hasToken_whenTokenIsEmpty_returnsTrue() {
        // Arrange
        // Note: hasToken() checks if token is not null, not if it's empty
        whenever(sharedPreferences.getString(eq("access_token"), isNull()))
            .thenReturn("")

        // Act
        val result = tokenManager.hasToken()

        // Assert
        // Empty string is not null, so hasToken() returns true
        assertTrue(result)
    }
}

