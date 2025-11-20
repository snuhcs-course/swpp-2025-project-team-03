package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.CreateClassRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassRepository @Inject constructor(
    private val apiService: ApiService,
) {

    suspend fun getClasses(teacherId: String): Result<List<ClassData>> {
        return try {
            val response = apiService.getClasses(teacherId)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClassById(id: Int): Result<ClassData> {
        return try {
            val response = apiService.getClassById(id)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: throw Exception("No data"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClassStudents(id: Int): Result<List<Student>> {
        return try {
            val response = apiService.getClassStudents(id)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createClass(createClassRequest: CreateClassRequest): Result<ClassData> {
        return try {
            val response = apiService.createClass(createClassRequest)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: throw Exception("No data"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun enrollStudentToClass(
        classId: Int,
        studentId: Int,
    ): Result<EnrollmentData> {
        return try {
            val response = apiService.enrollStudentToClass(
                id = classId,
                studentId = studentId,
            )
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: throw Exception("No data"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClassStudentsStatistics(classId: Int): Result<ClassStudentsStatistics> {
        return try {
            val response = apiService.getClassStudentsStatistics(classId)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: throw Exception("No data"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun removeStudentFromClass(classId: Int, studentId: Int): Result<Unit> {
        return try {
            println("[ClassRepository] Calling API: DELETE /courses/classes/$classId/students/$studentId/")
            val response = apiService.removeStudentFromClass(classId, studentId)
            println("[ClassRepository] Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")

            if (response.isSuccessful && response.body()?.success == true) {
                println("[ClassRepository] Student removed successfully")
                Result.success(Unit)
            } else {
                val error = response.body()?.error ?: "Unknown error (code: ${response.code()})"
                println("[ClassRepository] Failed to remove student: $error")
                Result.failure(Exception(error))
            }
        } catch (e: Exception) {
            println("[ClassRepository] Exception during API call: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun removeClassById(classId: Int): Result<Unit> {
        return try {
            val response = apiService.removeClassById(classId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
