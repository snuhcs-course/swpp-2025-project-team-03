package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.AssignmentData
import com.example.voicetutor.data.models.AssignmentStatus
import com.example.voicetutor.data.models.QuestionData
import com.example.voicetutor.data.models.StudentResult
import com.example.voicetutor.data.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssignmentRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    suspend fun getAllAssignments(
        teacherId: String? = null,
        classId: String? = null,
        status: AssignmentStatus? = null
    ): Result<List<AssignmentData>> {
        return try {
            val response = apiService.getAllAssignments(teacherId, classId, status?.name)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAssignmentById(id: Int): Result<AssignmentData> {
        return try {
            val response = apiService.getAssignmentById(id)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: throw Exception("No data"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getStudentAssignments(studentId: Int): Result<List<AssignmentData>> {
        return try {
            println("AssignmentRepository - Calling API for student $studentId")
            val response = apiService.getStudentAssignments(studentId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val assignments = response.body()?.data ?: emptyList()
                println("AssignmentRepository - API returned ${assignments.size} assignments")
                Result.success(assignments)
            } else {
                println("AssignmentRepository - API error: ${response.body()?.error}")
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            println("AssignmentRepository - Exception: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun createAssignment(assignment: com.example.voicetutor.data.network.CreateAssignmentRequest): Result<AssignmentData> {
        return try {
            val response = apiService.createAssignment(assignment)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: throw Exception("No data"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateAssignment(id: Int, assignment: com.example.voicetutor.data.network.UpdateAssignmentRequest): Result<AssignmentData> {
        return try {
            val response = apiService.updateAssignment(id, assignment)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: throw Exception("No data"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteAssignment(id: Int): Result<Unit> {
        return try {
            val response = apiService.deleteAssignment(id)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAssignmentResults(id: Int): Result<List<StudentResult>> {
        return try {
            val response = apiService.getAssignmentResults(id)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAssignmentQuestions(id: Int): Result<List<QuestionData>> {
        return try {
            val response = apiService.getAssignmentQuestions(id)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun submitAssignment(
        id: Int,
        submission: com.example.voicetutor.data.network.AssignmentSubmissionRequest
    ): Result<com.example.voicetutor.data.network.AssignmentSubmissionResult> {
        return try {
            val response = apiService.submitAssignment(id, submission)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: throw Exception("No data"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun saveAssignmentDraft(assignmentId: Int, draftContent: String): Result<Unit> {
        return try {
            val response = apiService.saveAssignmentDraft(assignmentId, draftContent)
            
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Failed to save draft"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
