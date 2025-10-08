package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    /**
     * 출석을 기록합니다
     */
    suspend fun recordAttendance(
        classId: Int,
        date: String,
        records: List<AttendanceRecordUpdate>
    ): Result<AttendanceRecordResponse> {
        return try {
            val request = AttendanceRecordRequest(
                classId = classId,
                date = date,
                records = records
            )
            
            val response = apiService.recordAttendance(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val recordResult = response.body()?.data
                if (recordResult != null) {
                    Result.success(recordResult)
                } else {
                    Result.failure(Exception("출석 기록 결과를 받을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "출석 기록에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 출석 기록을 조회합니다
     */
    suspend fun queryAttendance(
        classId: Int? = null,
        studentId: Int? = null,
        startDate: String,
        endDate: String
    ): Result<AttendanceQueryResponse> {
        return try {
            val request = AttendanceQueryRequest(
                classId = classId,
                studentId = studentId,
                startDate = startDate,
                endDate = endDate
            )
            
            val response = apiService.queryAttendance(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val queryResult = response.body()?.data
                if (queryResult != null) {
                    Result.success(queryResult)
                } else {
                    Result.failure(Exception("출석 조회 결과를 받을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "출석 조회에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 학생별 출석 요약을 가져옵니다
     */
    suspend fun getStudentAttendanceSummary(studentId: Int): Result<AttendanceSummary> {
        return try {
            val response = apiService.getStudentAttendanceSummary(studentId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val summary = response.body()?.data
                if (summary != null) {
                    Result.success(summary)
                } else {
                    Result.failure(Exception("출석 요약을 받을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "출석 요약을 가져오는데 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 클래스 출석을 가져옵니다
     */
    suspend fun getClassAttendance(classId: Int, date: String): Result<ClassAttendanceSummary> {
        return try {
            val response = apiService.getClassAttendance(classId, date)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val classAttendance = response.body()?.data
                if (classAttendance != null) {
                    Result.success(classAttendance)
                } else {
                    Result.failure(Exception("클래스 출석 정보를 받을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "클래스 출석 정보를 가져오는데 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
}
