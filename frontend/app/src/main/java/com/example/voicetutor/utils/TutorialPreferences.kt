package com.example.voicetutor.utils

import android.content.Context
import android.content.SharedPreferences

class TutorialPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    companion object {
        private const val PREFS_NAME = "tutorial_preferences"
        private const val KEY_TEACHER_TUTORIAL_COMPLETED = "teacher_tutorial_completed"
        private const val KEY_STUDENT_TUTORIAL_COMPLETED = "student_tutorial_completed"
        private const val KEY_IS_NEW_USER = "is_new_user"
    }

    fun isTeacherTutorialCompleted(): Boolean {
        return prefs.getBoolean(KEY_TEACHER_TUTORIAL_COMPLETED, false)
    }

    fun isStudentTutorialCompleted(): Boolean {
        return prefs.getBoolean(KEY_STUDENT_TUTORIAL_COMPLETED, false)
    }

    fun setTeacherTutorialCompleted() {
        prefs.edit().putBoolean(KEY_TEACHER_TUTORIAL_COMPLETED, true).apply()
    }

    fun setStudentTutorialCompleted() {
        prefs.edit().putBoolean(KEY_STUDENT_TUTORIAL_COMPLETED, true).apply()
    }

    fun setNewUser() {
        prefs.edit().putBoolean(KEY_IS_NEW_USER, true).apply()
    }

    fun isNewUser(): Boolean {
        return prefs.getBoolean(KEY_IS_NEW_USER, false)
    }

    fun clearNewUserFlag() {
        prefs.edit().putBoolean(KEY_IS_NEW_USER, false).apply()
    }

    fun resetAllTutorials() {
        prefs.edit()
            .putBoolean(KEY_TEACHER_TUTORIAL_COMPLETED, false)
            .putBoolean(KEY_STUDENT_TUTORIAL_COMPLETED, false)
            .putBoolean(KEY_IS_NEW_USER, true) // 초기화 후 다시 로그인 시 튜토리얼 표시
            .apply()
    }
}
