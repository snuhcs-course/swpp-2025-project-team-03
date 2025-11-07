package com.example.voicetutor.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * 튜토리얼 완료 상태를 관리하는 클래스
 */
class TutorialPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREFS_NAME = "tutorial_preferences"
        private const val KEY_TEACHER_TUTORIAL_COMPLETED = "teacher_tutorial_completed"
        private const val KEY_STUDENT_TUTORIAL_COMPLETED = "student_tutorial_completed"
    }
    
    /**
     * 선생님 튜토리얼 완료 여부 확인
     */
    fun isTeacherTutorialCompleted(): Boolean {
        return prefs.getBoolean(KEY_TEACHER_TUTORIAL_COMPLETED, false)
    }
    
    /**
     * 학생 튜토리얼 완료 여부 확인
     */
    fun isStudentTutorialCompleted(): Boolean {
        return prefs.getBoolean(KEY_STUDENT_TUTORIAL_COMPLETED, false)
    }
    
    /**
     * 선생님 튜토리얼 완료 상태 저장
     */
    fun setTeacherTutorialCompleted() {
        prefs.edit().putBoolean(KEY_TEACHER_TUTORIAL_COMPLETED, true).apply()
    }
    
    /**
     * 학생 튜토리얼 완료 상태 저장
     */
    fun setStudentTutorialCompleted() {
        prefs.edit().putBoolean(KEY_STUDENT_TUTORIAL_COMPLETED, true).apply()
    }
    
    /**
     * 모든 튜토리얼 초기화 (개발/테스트용)
     */
    fun resetAllTutorials() {
        prefs.edit()
            .putBoolean(KEY_TEACHER_TUTORIAL_COMPLETED, false)
            .putBoolean(KEY_STUDENT_TUTORIAL_COMPLETED, false)
            .apply()
    }
}

