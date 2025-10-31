package com.example.voicetutor.data.network

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiConfig @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("api_config", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_BASE_URL = "base_url"
        
        // 기본 URL
        const val PROD_URL = "http://147.46.78.61:8003/api/" // 배포 서버
    }
    
    fun getBaseUrl(): String {
        return prefs.getString(KEY_BASE_URL, PROD_URL) ?: PROD_URL
    }
}
