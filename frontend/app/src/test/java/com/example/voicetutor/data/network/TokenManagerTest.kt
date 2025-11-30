package com.example.voicetutor.data.network

import android.content.Context
import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.doReturn
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
        // Use doReturn().when() for method chaining to ensure proper return values
        doReturn(editorAfterPut).`when`(editor).putString(any(), any())
        doReturn(editorAfterPut).`when`(editor).remove(any())
        // editorAfterPut.remove() is used in clearTokens() for method chaining
        doReturn(editorAfterPut).`when`(editorAfterPut).remove(any())
        // apply() returns void, so we use doNothing
        doNothing().`when`(editorAfterPut).apply()

        tokenManager = TokenManager(context)
    }

    @Test
    fun saveAccessToken_savesToken() {
        val token = "test_access_token"
        tokenManager.saveAccessToken(token)
        verify(editor).putString("access_token", token)
        verify(editorAfterPut).apply()
    }

    @Test
    fun getAccessToken_returnsSavedToken() {
        val token = "test_access_token"
        whenever(sharedPreferences.getString(eq("access_token"), isNull()))
            .thenReturn(token)
        val result = tokenManager.getAccessToken()
        assertEquals(token, result)
        verify(sharedPreferences).getString("access_token", null)
    }

    @Test
    fun getAccessToken_whenNoToken_returnsNull() {
        whenever(sharedPreferences.getString(eq("access_token"), isNull()))
            .thenReturn(null)
        val result = tokenManager.getAccessToken()
        assertNull(result)
    }

    @Test
    fun saveRefreshToken_savesToken() {
        val token = "test_refresh_token"
        tokenManager.saveRefreshToken(token)
        verify(editor).putString("refresh_token", token)
        verify(editorAfterPut).apply()
    }

    @Test
    fun getRefreshToken_returnsSavedToken() {
        val token = "test_refresh_token"
        whenever(sharedPreferences.getString(eq("refresh_token"), isNull()))
            .thenReturn(token)
        val result = tokenManager.getRefreshToken()
        assertEquals(token, result)
        verify(sharedPreferences).getString("refresh_token", null)
    }

    @Test
    fun getRefreshToken_whenNoToken_returnsNull() {
        whenever(sharedPreferences.getString(eq("refresh_token"), isNull()))
            .thenReturn(null)
        val result = tokenManager.getRefreshToken()
        assertNull(result)
    }

    @Test
    fun clearTokens_removesBothTokens() {
        tokenManager.clearTokens()
        verify(editor).remove("access_token")
        verify(editorAfterPut).remove("refresh_token")
        verify(editorAfterPut).apply()
    }

    @Test
    fun hasToken_whenTokenExists_returnsTrue() {
        whenever(sharedPreferences.getString(eq("access_token"), isNull()))
            .thenReturn("test_token")
        val result = tokenManager.hasToken()
        assertTrue(result)
    }

    @Test
    fun hasToken_whenNoToken_returnsFalse() {
        whenever(sharedPreferences.getString(eq("access_token"), isNull()))
            .thenReturn(null)
        val result = tokenManager.hasToken()
        assertFalse(result)
    }

    @Test
    fun hasToken_whenTokenIsEmpty_returnsTrue() {
        // Note: hasToken() checks if token is not null, not if it's empty
        whenever(sharedPreferences.getString(eq("access_token"), isNull()))
            .thenReturn("")
        val result = tokenManager.hasToken()
        // Empty string is not null, so hasToken() returns true
        assertTrue(result)
    }
}
