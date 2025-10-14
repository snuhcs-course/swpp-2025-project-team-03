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
        email: String,
        phoneNumber: String? = null,
        parentName: String? = null,
        parentPhone: String? = null,
        address: String? = null,
        birthDate: String? = null,
        notes: String? = null,
        isActive: Boolean = true
    ): Result<StudentEditResponse> {
        return try {
            val request = StudentEditRequest(
                studentId = studentId,
                name = name,
                email = email,
                phoneNumber = phoneNumber,
                parentName = parentName,
                parentPhone = parentPhone,
                address = address,
                birthDate = birthDate,
                notes = notes,
                isActive = isActive
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
        studentId: Int,
        reason: String? = null
    ): Result<StudentDeleteResponse> {
        return try {
            val request = StudentDeleteRequest(
                studentId = studentId,
                reason = reason
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
     * 학생 상태를 변경합니다 (활성/비활성)
     */
    suspend fun updateStudentStatus(
        studentId: Int,
        isActive: Boolean,
        reason: String? = null
    ): Result<StudentStatusResponse> {
        return try {
            val request = StudentStatusRequest(
                studentId = studentId,
                isActive = isActive,
                reason = reason
            )
            
            val response = apiService.updateStudentStatus(studentId, request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val statusResult = response.body()?.data
                if (statusResult != null) {
                    Result.success(statusResult)
                } else {
                    Result.failure(Exception("학생 상태 변경 결과를 받을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "학생 상태 변경에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 학생 비밀번호를 재설정합니다
     */
    suspend fun resetStudentPassword(
        studentId: Int,
        newPassword: String? = null,
        temporaryPassword: Boolean = true
    ): Result<StudentPasswordResetResponse> {
        return try {
            val request = StudentPasswordResetRequest(
                studentId = studentId,
                newPassword = newPassword ?: generateTemporaryPassword(),
                temporaryPassword = temporaryPassword
            )
            
            val response = apiService.resetStudentPassword(studentId, request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val passwordResult = response.body()?.data
                if (passwordResult != null) {
                    Result.success(passwordResult)
                } else {
                    Result.failure(Exception("비밀번호 재설정 결과를 받을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "비밀번호 재설정에 실패했습니다"))
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
    
    private fun generateTemporaryPassword(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
        return (1..8)
            .map { chars.random() }
            .joinToString("")
    }
}
