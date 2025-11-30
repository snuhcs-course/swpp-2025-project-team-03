package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.AnswerSubmissionResponse
import com.example.voicetutor.data.models.AssignmentCorrectnessItem
import com.example.voicetutor.data.models.AssignmentData
import com.example.voicetutor.data.models.AssignmentResultData
import com.example.voicetutor.data.models.AssignmentStatus
import com.example.voicetutor.data.models.PersonalAssignmentData
import com.example.voicetutor.data.models.PersonalAssignmentQuestion
import com.example.voicetutor.data.models.PersonalAssignmentStatistics
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.QuestionCreateRequest
import com.example.voicetutor.data.network.S3UploadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.emptyList

@Singleton
class AssignmentRepository @Inject constructor(
    private val apiService: ApiService,
) {

    suspend fun getAllAssignments(
        teacherId: String? = null,
        classId: String? = null,
        status: AssignmentStatus? = null,
    ): Result<List<AssignmentData>> {
        return try {
            val response = apiService.getAllAssignments(teacherId, classId, status?.name)

            if (response.isSuccessful && response.body()?.success == true) {
                val assignments = response.body()?.data ?: emptyList()
                Result.success(assignments)
            } else {
                val errorMsg = response.body()?.error ?: "Unknown error"
                Result.failure(Exception(errorMsg))
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
            val response = apiService.getStudentAssignments(studentId)

            if (response.isSuccessful && response.body()?.success == true) {
                val assignments = response.body()?.data ?: emptyList()
                Result.success(assignments)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPersonalAssignments(studentId: Int): Result<List<PersonalAssignmentData>> {
        return try {
            val response = apiService.getPersonalAssignments(studentId = studentId)

            if (response.isSuccessful && response.body()?.success == true) {
                val personalAssignments = response.body()?.data ?: emptyList()
                Result.success(personalAssignments)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPersonalAssignments(
        studentId: Int? = null,
        assignmentId: Int? = null,
    ): Result<List<PersonalAssignmentData>> {
        return try {
            val response = apiService.getPersonalAssignments(studentId = studentId, assignmentId = assignmentId)
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: emptyList())
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createAssignment(assignment: com.example.voicetutor.data.network.CreateAssignmentRequest): Result<com.example.voicetutor.data.network.CreateAssignmentResponse> {
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

    suspend fun getAssignmentResult(id: Int): Result<AssignmentResultData> {
        return try {
            val response = apiService.getAssignmentResult(id)

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

    suspend fun uploadPdfToS3(uploadUrl: String, pdfFile: File): Result<Boolean> {
        return try {
            withContext(Dispatchers.IO) {
                try {
                    val client = OkHttpClient()
                    val mediaType = "application/pdf".toMediaType()
                    val requestBody = pdfFile.asRequestBody(mediaType)

                    val request = Request.Builder()
                        .url(uploadUrl)
                        .put(requestBody)
                        .addHeader("Content-Type", "application/pdf")
                        .build()

                    val response = client.newCall(request).execute()

                    if (response.isSuccessful) {
                        Result.success(true)
                    } else {
                        val errorBody = response.body?.string()
                        Result.failure(Exception("Upload failed with status ${response.code}: $errorBody"))
                    }
                } catch (e: Exception) {
                    Result.failure(e)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkS3Upload(assignmentId: Int): Result<S3UploadStatus> {
        return try {
            val response = apiService.checkS3Upload(assignmentId)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.data ?: throw Exception("No data"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createQuestionsAfterUpload(
        assignmentId: Int,
        materialId: Int,
        totalNumber: Int,
    ): Result<Unit> {
        return try {
            val response = apiService.createQuestions(
                QuestionCreateRequest(
                    assignment_id = assignmentId,
                    material_id = materialId,
                    total_number = totalNumber,
                ),
            )
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorMessage = response.message() ?: "HTTP ${response.code()}"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPersonalAssignmentQuestions(personalAssignmentId: Int): Result<List<PersonalAssignmentQuestion>> {
        return try {
            val response = apiService.getPersonalAssignmentQuestions(personalAssignmentId)

            if (response.isSuccessful && response.body()?.success == true) {
                val questions = response.body()?.data ?: emptyList()
                Result.success(questions)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getNextQuestion(personalAssignmentId: Int): Result<PersonalAssignmentQuestion> {
        return try {
            val response = apiService.getNextQuestion(personalAssignmentId)

            if (response.isSuccessful && response.body()?.success == true) {
                val question = response.body()?.data
                if (question != null) {
                    Result.success(question)
                } else {
                    Result.failure(Exception("No question data"))
                }
            } else {
                val responseBody = response.body()
                val errorBody = response.errorBody()

                val errorMessage = if (responseBody != null) {
                    responseBody.message ?: responseBody.error ?: "Unknown error"
                } else if (errorBody != null) {
                    try {
                        val errorJson = errorBody.string()
                        if (errorJson.contains("\"message\":\"모든 문제를 완료했습니다.\"")) {
                            "모든 문제를 완료했습니다."
                        } else {
                            "Unknown error"
                        }
                    } catch (e: Exception) {
                        "Unknown error"
                    }
                } else {
                    "Unknown error"
                }

                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPersonalAssignmentStatistics(personalAssignmentId: Int): Result<PersonalAssignmentStatistics> {
        return try {
            val response = apiService.getPersonalAssignmentStatistics(personalAssignmentId)

            if (response.isSuccessful && response.body()?.success == true) {
                val statistics = response.body()?.data
                Result.success(statistics ?: throw Exception("No statistics data"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRecentPersonalAssignment(studentId: Int): Result<Int> {
        return try {
            val response = apiService.getRecentPersonalAssignment(studentId)
            if (response.isSuccessful && response.body()?.success == true) {
                val id = response.body()?.data?.personalAssignmentId
                if (id != null) Result.success(id) else Result.failure(Exception("최근 개인 과제 ID를 찾을 수 없습니다"))
            } else {
                Result.failure(Exception(response.body()?.message ?: "최근 개인 과제 조회 실패"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitAnswer(
        personalAssignmentId: Int,
        studentId: Int,
        questionId: Int,
        audioFile: File,
    ): Result<AnswerSubmissionResponse> {
        return try {
            val requestBody = audioFile.asRequestBody("audio/wav".toMediaType())
            val audioPart = MultipartBody.Part.createFormData(
                "audioFile",
                audioFile.name,
                requestBody,
            )

            val studentIdPart = MultipartBody.Part.createFormData(
                "studentId",
                studentId.toString(),
            )

            val questionIdPart = MultipartBody.Part.createFormData(
                "questionId",
                questionId.toString(),
            )

            val response = apiService.submitAnswer(personalAssignmentId, studentIdPart, questionIdPart, audioPart)

            if (response.isSuccessful && response.body()?.success == true) {
                val submissionResponse = response.body()?.data
                Result.success(submissionResponse ?: throw Exception("No submission data"))
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completePersonalAssignment(personalAssignmentId: Int): Result<Unit> {
        return try {
            val response = apiService.completePersonalAssignment(personalAssignmentId)

            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(Unit)
            } else {
                val errorMessage = response.body()?.message ?: response.body()?.error ?: "Unknown error"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getAssignmentCorrectness(personalAssignmentId: Int): Result<List<AssignmentCorrectnessItem>> {
        return try {
            val response = apiService.getAssignmentCorrectness(personalAssignmentId)

            if (response.isSuccessful && response.body()?.success == true) {
                val correctnessData = response.body()?.data ?: emptyList()
                Result.success(correctnessData)
            } else {
                Result.failure(Exception(response.body()?.error ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
