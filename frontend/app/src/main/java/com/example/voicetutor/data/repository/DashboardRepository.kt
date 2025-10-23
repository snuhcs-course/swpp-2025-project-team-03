package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.DashboardStats
import com.example.voicetutor.data.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    suspend fun getDashboardStats(teacherId: String): Result<DashboardStats> {
        return try {
            val response = apiService.getDashboardStats(teacherId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val stats = response.body()?.data
                if (stats != null) {
                    Result.success(stats)
                } else {
                    Result.failure(Exception("통계 데이터를 찾을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.error ?: "통계 데이터를 가져오는데 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
