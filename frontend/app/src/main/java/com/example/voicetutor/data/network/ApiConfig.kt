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
        
        // 로컬 개발 서버 URL
        // 에뮬레이터 사용 시: 10.0.2.2 (localhost를 가리킴)
        // 실제 기기 사용 시: PC의 로컬 IP 주소로 변경 (예: 192.168.1.100)
        const val LOCAL_URL = "http://10.0.2.2:8000/api/" // 로컬 서버 (에뮬레이터용)
        // const val LOCAL_URL = "http://192.168.1.100:8000/api/" // 로컬 서버 (실제 기기용)
    }
    
    fun getBaseUrl(): String {
        return prefs.getString(KEY_BASE_URL, LOCAL_URL) ?: LOCAL_URL // 기본값을 LOCAL_URL로 변경
    }
}
