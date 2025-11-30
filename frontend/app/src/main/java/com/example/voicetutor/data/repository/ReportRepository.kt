package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.CurriculumReportData
import com.example.voicetutor.data.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val apiService: ApiService,
) {

    suspend fun getCurriculumReport(classId: Int, studentId: Int): Result<CurriculumReportData> {
        return try {
            val response = apiService.getCurriculumReport(classId, studentId)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: throw Exception("No data"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
