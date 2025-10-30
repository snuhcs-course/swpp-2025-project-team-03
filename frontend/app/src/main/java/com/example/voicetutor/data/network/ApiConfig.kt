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
        private const val KEY_SERVER_TYPE = "server_type"
        
        // 기본 URL들
        const val LOCALHOST_URL = "http://10.0.2.2:8000/api/" // Android 에뮬레이터용 localhost
        const val LOCALHOST_URL_DEVICE = "http://192.168.35.202:8000/api/" // 실제 디바이스용 (PC의 Wi-Fi IP)
        const val PROD_URL = "http://147.46.78.61:8003/api/" // 배포 서버
        
        // 서버 타입
        const val SERVER_TYPE_LOCALHOST = "localhost"
        const val SERVER_TYPE_PROD = "prod"
    }
    
    fun getBaseUrl(): String {
        return prefs.getString(KEY_BASE_URL, PROD_URL) ?: PROD_URL
    }
    
    fun setBaseUrl(url: String) {
        prefs.edit().putString(KEY_BASE_URL, url).apply()
    }
    
    fun getServerType(): String {
        return prefs.getString(KEY_SERVER_TYPE, SERVER_TYPE_LOCALHOST) ?: SERVER_TYPE_LOCALHOST
    }
    
    fun setServerType(type: String) {
        prefs.edit().putString(KEY_SERVER_TYPE, type).apply()
    }
    
    fun setLocalhostServer() {
        setServerType(SERVER_TYPE_LOCALHOST)
        setBaseUrl(LOCALHOST_URL)
    }
    
    fun setProdServer(url: String) {
        setServerType(SERVER_TYPE_PROD)
        setBaseUrl(url)
    }
    
    fun getAvailableServers(): List<ServerOption> {
        return listOf(
            ServerOption("Production", PROD_URL, SERVER_TYPE_PROD),
            ServerOption("Localhost (에뮬레이터)", LOCALHOST_URL, SERVER_TYPE_LOCALHOST),
            ServerOption("Localhost (디바이스)", LOCALHOST_URL_DEVICE, SERVER_TYPE_LOCALHOST),
        )
    }
}

data class ServerOption(
    val name: String,
    val url: String,
    val type: String
)
