package com.example.voicetutor

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class VoiceTutorApplication : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    override fun onCreate() {
        super.onCreate()
        
        // Hilt 초기화 후 health check 호출
        // EntryPoint를 사용하여 안전하게 의존성 주입 받기
        applicationScope.launch {
            try {
                // Application이 완전히 초기화될 때까지 약간 대기
                kotlinx.coroutines.delay(500)
                performHealthCheck()
            } catch (e: Exception) {
                Log.e("VoiceTutorApp", "Health check failed", e)
            }
        }
    }
    
    private suspend fun performHealthCheck() {
        try {
            val apiService = getApiService()
            val response = apiService.healthCheck()
            if (response.isSuccessful) {
                val body = response.body()
                Log.d("VoiceTutorApp", "Health check successful: ${body?.data}")
            } else {
                Log.w("VoiceTutorApp", "Health check failed with code: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("VoiceTutorApp", "Health check error: ${e.message}", e)
        }
    }
    
    private fun getApiService(): com.example.voicetutor.data.network.ApiService {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            ApiServiceEntryPoint::class.java
        )
        return entryPoint.apiService()
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface ApiServiceEntryPoint {
    fun apiService(): com.example.voicetutor.data.network.ApiService
}
