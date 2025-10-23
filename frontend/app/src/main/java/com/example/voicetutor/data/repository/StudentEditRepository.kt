package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StudentEditRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    /**
     * 학생 정보를 편집합니다
     */
    suspend fun editStudent(
        studentId: Int,
        name: String,
        email: String
    ): Result<StudentEditResponse> {
        return try {
            val request = StudentEditRequest(
                studentId = studentId,
                name = name,
                email = email
            )
            
            val response = apiService.editStudent(studentId, request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val editResult = response.body()?.data
                if (editResult != null) {
                    Result.success(editResult)
                } else {
                    Result.failure(Exception("학생 편집 결과를 받을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "학생 편집에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 학생을 삭제합니다
     */
    suspend fun deleteStudent(
        studentId: Int
    ): Result<StudentDeleteResponse> {
        return try {
            val request = StudentDeleteRequest(
                studentId = studentId
            )
            
            val response = apiService.deleteStudent(studentId, request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val deleteResult = response.body()?.data
                if (deleteResult != null) {
                    Result.success(deleteResult)
                } else {
                    Result.failure(Exception("학생 삭제 결과를 받을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "학생 삭제에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 학생의 클래스를 변경합니다
     */
    suspend fun changeStudentClass(
        studentId: Int,
        fromClassId: Int,
        toClassId: Int,
        reason: String? = null
    ): Result<StudentClassChangeResponse> {
        return try {
            val request = StudentClassChangeRequest(
                studentId = studentId,
                fromClassId = fromClassId,
                toClassId = toClassId,
                reason = reason
            )
            
            val response = apiService.changeStudentClass(studentId, request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val classChangeResult = response.body()?.data
                if (classChangeResult != null) {
                    Result.success(classChangeResult)
                } else {
                    Result.failure(Exception("클래스 변경 결과를 받을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "클래스 변경에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
