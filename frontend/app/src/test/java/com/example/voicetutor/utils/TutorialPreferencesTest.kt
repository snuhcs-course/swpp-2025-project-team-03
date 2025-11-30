package com.example.voicetutor.utils

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
class TutorialPreferencesTest {

    @Mock
    lateinit var context: Context

    @Mock
    lateinit var sharedPreferences: SharedPreferences

    @Mock
    lateinit var editor: SharedPreferences.Editor

    @Mock
    lateinit var editorAfterPut: SharedPreferences.Editor

    private lateinit var tutorialPreferences: TutorialPreferences

    @Before
    fun setUp() {
        whenever(context.getSharedPreferences(any(), any())).thenReturn(sharedPreferences)
        whenever(sharedPreferences.edit()).thenReturn(editor)
        whenever(editor.putBoolean(any(), any())).thenReturn(editorAfterPut)
        whenever(editorAfterPut.putBoolean(any(), any())).thenReturn(editorAfterPut)

        tutorialPreferences = TutorialPreferences(context)
    }

    @Test
    fun isTeacherTutorialCompleted_returnsFalseByDefault() {
        whenever(sharedPreferences.getBoolean(eq("teacher_tutorial_completed"), eq(false)))
            .thenReturn(false)
        val result = tutorialPreferences.isTeacherTutorialCompleted()
        assertFalse(result)
        verify(sharedPreferences).getBoolean("teacher_tutorial_completed", false)
    }

    @Test
    fun isTeacherTutorialCompleted_returnsTrueWhenSet() {
        whenever(sharedPreferences.getBoolean(eq("teacher_tutorial_completed"), eq(false)))
            .thenReturn(true)
        val result = tutorialPreferences.isTeacherTutorialCompleted()
        assertTrue(result)
    }

    @Test
    fun isStudentTutorialCompleted_returnsFalseByDefault() {
        whenever(sharedPreferences.getBoolean(eq("student_tutorial_completed"), eq(false)))
            .thenReturn(false)
        val result = tutorialPreferences.isStudentTutorialCompleted()
        assertFalse(result)
        verify(sharedPreferences).getBoolean("student_tutorial_completed", false)
    }

    @Test
    fun isStudentTutorialCompleted_returnsTrueWhenSet() {
        whenever(sharedPreferences.getBoolean(eq("student_tutorial_completed"), eq(false)))
            .thenReturn(true)
        val result = tutorialPreferences.isStudentTutorialCompleted()
        assertTrue(result)
    }

    @Test
    fun setTeacherTutorialCompleted_savesTrue() {
        tutorialPreferences.setTeacherTutorialCompleted()
        verify(editor).putBoolean("teacher_tutorial_completed", true)
        verify(editorAfterPut).apply()
    }

    @Test
    fun setStudentTutorialCompleted_savesTrue() {
        tutorialPreferences.setStudentTutorialCompleted()
        verify(editor).putBoolean("student_tutorial_completed", true)
        verify(editorAfterPut).apply()
    }

    @Test
    fun isNewUser_returnsFalseByDefault() {
        whenever(sharedPreferences.getBoolean(eq("is_new_user"), eq(false)))
            .thenReturn(false)
        val result = tutorialPreferences.isNewUser()
        assertFalse(result)
        verify(sharedPreferences).getBoolean("is_new_user", false)
    }

    @Test
    fun isNewUser_returnsTrueWhenSet() {
        whenever(sharedPreferences.getBoolean(eq("is_new_user"), eq(false)))
            .thenReturn(true)
        val result = tutorialPreferences.isNewUser()
        assertTrue(result)
    }

    @Test
    fun setNewUser_savesTrue() {
        tutorialPreferences.setNewUser()
        verify(editor).putBoolean("is_new_user", true)
        verify(editorAfterPut).apply()
    }

    @Test
    fun clearNewUserFlag_savesFalse() {
        tutorialPreferences.clearNewUserFlag()
        verify(editor).putBoolean("is_new_user", false)
        verify(editorAfterPut).apply()
    }

    @Test
    fun resetAllTutorials_resetsAllFlags() {
        tutorialPreferences.resetAllTutorials()
        verify(editor).putBoolean("teacher_tutorial_completed", false)
        verify(editorAfterPut).putBoolean("student_tutorial_completed", false)
        verify(editorAfterPut).putBoolean("is_new_user", true)
        verify(editorAfterPut).apply()
    }
}
