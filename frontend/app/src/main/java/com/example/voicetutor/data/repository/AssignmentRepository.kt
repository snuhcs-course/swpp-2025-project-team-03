package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.AssignmentData
import com.example.voicetutor.data.models.AssignmentStatus
import com.example.voicetutor.data.models.QuestionData
import com.example.voicetutor.data.models.StudentResult
import com.example.voicetutor.data.models.PersonalAssignmentData
import com.example.voicetutor.data.models.PersonalAssignmentQuestion
import com.example.voicetutor.data.models.PersonalAssignmentStatistics
import com.example.voicetutor.data.models.AnswerSubmissionResponse
import com.example.voicetutor.data.network.ApiService
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
import java.io.File
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
    
    suspend fun getPersonalAssignments(studentId: Int): Result<List<PersonalAssignmentData>> {
        return try {
            println("AssignmentRepository - Calling personal assignments API for student $studentId")
            val response = apiService.getPersonalAssignments(studentId = studentId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val personalAssignments = response.body()?.data ?: emptyList()
                println("AssignmentRepository - Personal assignments API returned ${personalAssignments.size} assignments")
                Result.success(personalAssignments)
            } else {
                println("AssignmentRepository - Personal assignments API error: ${response.body()?.error}")
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            println("AssignmentRepository - Personal assignments Exception: ${e.message}")
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
    
    // Personal Assignment API 메서드들
    suspend fun getPersonalAssignmentQuestions(personalAssignmentId: Int): Result<List<PersonalAssignmentQuestion>> {
        return try {
            println("AssignmentRepository - Getting questions for personal assignment $personalAssignmentId")
            val response = apiService.getPersonalAssignmentQuestions(personalAssignmentId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val questions = response.body()?.data ?: emptyList()
                println("AssignmentRepository - Personal assignment questions API returned ${questions.size} questions")
                Result.success(questions)
            } else {
                println("AssignmentRepository - Personal assignment questions API error: ${response.body()?.error}")
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            println("AssignmentRepository - Personal assignment questions Exception: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun getPersonalAssignmentStatistics(personalAssignmentId: Int): Result<PersonalAssignmentStatistics> {
        return try {
            println("AssignmentRepository - Getting statistics for personal assignment $personalAssignmentId")
            val response = apiService.getPersonalAssignmentStatistics(personalAssignmentId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val statistics = response.body()?.data
                println("AssignmentRepository - Personal assignment statistics API returned: $statistics")
                Result.success(statistics ?: throw Exception("No statistics data"))
            } else {
                println("AssignmentRepository - Personal assignment statistics API error: ${response.body()?.error}")
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            println("AssignmentRepository - Personal assignment statistics Exception: ${e.message}")
            Result.failure(e)
        }
    }
    
    suspend fun submitAnswer(
        studentId: Int,
        questionId: Int,
        audioFile: File
    ): Result<AnswerSubmissionResponse> {
        return try {
            println("AssignmentRepository - Submitting answer for student $studentId, question $questionId")
            
            // Create multipart request body
            val requestBody = RequestBody.create(
                "audio/wav".toMediaType(),
                audioFile
            )
            val audioPart = MultipartBody.Part.createFormData(
                "audioFile",
                audioFile.name,
                requestBody
            )
            
            val studentIdPart = MultipartBody.Part.createFormData(
                "studentId",
                studentId.toString()
            )
            
            val questionIdPart = MultipartBody.Part.createFormData(
                "questionId",
                questionId.toString()
            )
            
            val response = apiService.submitAnswer(studentIdPart, questionIdPart, audioPart)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val submissionResponse = response.body()?.data
                println("AssignmentRepository - Answer submission successful: $submissionResponse")
                Result.success(submissionResponse ?: throw Exception("No submission data"))
            } else {
                println("AssignmentRepository - Answer submission API error: ${response.body()?.error}")
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            println("AssignmentRepository - Answer submission Exception: ${e.message}")
            Result.failure(e)
        }
    }
}
