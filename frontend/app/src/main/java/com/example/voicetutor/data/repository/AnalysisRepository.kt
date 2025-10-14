package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalysisRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    /**
     * 학생별 학습 분석을 가져옵니다
     */
    suspend fun getStudentAnalysis(
        studentId: Int,
        dateRange: DateRange? = null
    ): Result<LearningAnalysis> {
        return try {
            val request = AnalysisRequest(
                studentId = studentId,
                dateRange = dateRange
            )
            
            val response = apiService.getStudentAnalysis(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val analysisData = response.body()?.data?.learningAnalysis
                if (analysisData != null) {
                    Result.success(analysisData)
                } else {
                    Result.failure(Exception("학생 분석 데이터를 받을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "학생 분석을 가져오는데 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 클래스 전체 분석을 가져옵니다
     */
    suspend fun getClassAnalysis(
        classId: Int,
        dateRange: DateRange? = null
    ): Result<ClassAnalysis> {
        return try {
            val request = AnalysisRequest(
                classId = classId,
                dateRange = dateRange
            )
            
            val response = apiService.getClassAnalysis(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val analysisData = response.body()?.data?.classAnalysis
                if (analysisData != null) {
                    Result.success(analysisData)
                } else {
                    Result.failure(Exception("클래스 분석 데이터를 받을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "클래스 분석을 가져오는데 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 과목별 분석을 가져옵니다
     */
    suspend fun getSubjectAnalysis(
        subject: String,
        studentId: Int? = null,
        classId: Int? = null,
        dateRange: DateRange? = null
    ): Result<SubjectAnalysis> {
        return try {
            val request = AnalysisRequest(
                studentId = studentId,
                classId = classId,
                subject = subject,
                dateRange = dateRange
            )
            
            val response = apiService.getSubjectAnalysis(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val analysisData = response.body()?.data?.subjectAnalysis
                if (analysisData != null) {
                    Result.success(analysisData)
                } else {
                    Result.failure(Exception("과목 분석 데이터를 받을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "과목 분석을 가져오는데 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
}
