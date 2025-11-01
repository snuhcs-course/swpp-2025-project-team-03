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
import com.example.voicetutor.data.network.QuestionCreateRequest
import com.example.voicetutor.data.network.S3UploadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MultipartBody
import okhttp3.RequestBody
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
    
    // Filtered by student and assignment (student-side precise lookup)
    suspend fun getPersonalAssignments(
        studentId: Int? = null,
        assignmentId: Int? = null
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
    
    // removed: getAssignmentResults, getAssignmentQuestions
    
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
    
    // removed: saveAssignmentDraft
    
    suspend fun uploadPdfToS3(uploadUrl: String, pdfFile: File): Result<Boolean> {
        return try {
            println("=== AssignmentRepository.uploadPdfToS3 시작 ===")
            println("업로드 URL: $uploadUrl")
            println("PDF 파일: ${pdfFile.name}")
            println("파일 크기: ${pdfFile.length()} bytes")
            println("파일 존재: ${pdfFile.exists()}")
            println("파일 읽기 가능: ${pdfFile.canRead()}")
            println("파일 절대 경로: ${pdfFile.absolutePath}")
            
            // 파일 읽기 테스트
            try {
                val fileBytes = pdfFile.readBytes()
                println("파일 읽기 성공: ${fileBytes.size} bytes")
            } catch (e: Exception) {
                println("❌ 파일 읽기 실패: ${e.message}")
                throw e
            }
            
            // 네트워크 요청을 백그라운드 스레드에서 실행
            withContext(Dispatchers.IO) {
                try {
                    val client = OkHttpClient()
                    val mediaType = "application/pdf".toMediaType()
                    val requestBody = pdfFile.asRequestBody(mediaType)
                    
                    println("HTTP 요청 생성 중...")
                    val request = Request.Builder()
                        .url(uploadUrl)
                        .put(requestBody)
                        .addHeader("Content-Type", "application/pdf")
                        .build()
                    
                    println("S3 업로드 요청 전송 중...")
                    println("업로드 URL: $uploadUrl")
                    
                    val response = client.newCall(request).execute()
                    
                    println("응답 코드: ${response.code}")
                    println("응답 메시지: ${response.message}")
                    
                    if (response.isSuccessful) {
                        println("✅ S3 업로드 성공")
                        Result.success(true)
                    } else {
                        val errorBody = response.body?.string()
                        println("❌ S3 업로드 실패: ${response.code} - $errorBody")
                        Result.failure(Exception("Upload failed with status ${response.code}: $errorBody"))
                    }
                } catch (e: java.net.UnknownHostException) {
                    println("❌ 네트워크 연결 실패: ${e.message}")
                    println("에뮬레이터에서 S3에 접근할 수 없습니다.")
                    println("해결 방법:")
                    println("1. 에뮬레이터 재시작")
                    println("2. 실제 디바이스에서 테스트")
                    println("3. 네트워크 설정 확인")
                    Result.failure(e)
                } catch (e: Exception) {
                    println("❌ S3 업로드 예외: ${e.message}")
                    Result.failure(e)
                }
            }
        } catch (e: Exception) {
            println("❌ S3 업로드 예외: ${e.message}")
            println("예외 타입: ${e.javaClass.simpleName}")
            println("예외 스택: ${e.stackTrace.joinToString("\n")}")
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
        totalNumber: Int
    ): Result<Unit> {
        return try {
            println("AssignmentRepository - Trigger question generation: assignment=$assignmentId material=$materialId total=$totalNumber")
            val response = apiService.createQuestions(
                QuestionCreateRequest(
                    assignment_id = assignmentId,
                    material_id = materialId,
                    total_number = totalNumber
                )
            )
            if (response.isSuccessful && response.body()?.success == true) {
                println("AssignmentRepository - Question generation request accepted")
                Result.success(Unit)
            } else {
                val err = response.body()?.error ?: "Unknown error"
                println("AssignmentRepository - Question generation failed: $err")
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            println("AssignmentRepository - Question generation exception: ${e.message}")
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
    
    suspend fun getNextQuestion(personalAssignmentId: Int): Result<PersonalAssignmentQuestion> {
        return try {
            println("AssignmentRepository - getNextQuestion CALLED for personalAssignmentId: $personalAssignmentId")
            val response = apiService.getNextQuestion(personalAssignmentId)
            println("AssignmentRepository - API response received: isSuccessful=${response.isSuccessful}")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val question = response.body()?.data
                if (question != null) {
                    println("AssignmentRepository - Next question API returned question: ${question.question}")
                    Result.success(question)
                } else {
                    println("AssignmentRepository - Next question API returned null data")
                    Result.failure(Exception("No question data"))
                }
            } else {
                val responseBody = response.body()
                val errorBody = response.errorBody()
                
                // 404 응답에서는 response.body()가 null이므로 errorBody()를 사용
                val errorMessage = if (responseBody != null) {
                    responseBody.message ?: responseBody.error ?: "Unknown error"
                } else if (errorBody != null) {
                    // errorBody에서 JSON 파싱 시도
                    try {
                        val errorJson = errorBody.string()
                        println("AssignmentRepository - Error body JSON: $errorJson")
                        // JSON에서 message 필드 추출
                        if (errorJson.contains("\"message\":\"모든 문제를 완료했습니다.\"")) {
                            "모든 문제를 완료했습니다."
                        } else {
                            "Unknown error"
                        }
                    } catch (e: Exception) {
                        println("AssignmentRepository - Failed to parse error body: ${e.message}")
                        "Unknown error"
                    }
                } else {
                    "Unknown error"
                }
                
                println("AssignmentRepository - Next question API error: $errorMessage")
                println("AssignmentRepository - Response body: $responseBody")
                println("AssignmentRepository - Error body: $errorBody")
                println("AssignmentRepository - Response code: ${response.code()}")
                println("AssignmentRepository - Response message: ${response.message()}")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            println("AssignmentRepository - Next question Exception: ${e.message}")
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

        // Recent personal assignment for a student
        suspend fun getRecentPersonalAssignment(studentId: Int): Result<Int> {
            return try {
                println("AssignmentRepository - Getting recent personal assignment for student $studentId")
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
        audioFile: File
    ): Result<AnswerSubmissionResponse> {
        return try {
            println("AssignmentRepository - Submitting answer for personal_assignment_id $personalAssignmentId, student $studentId, question $questionId")
            
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
            
            val response = apiService.submitAnswer(personalAssignmentId, studentIdPart, questionIdPart, audioPart)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val submissionResponse = response.body()?.data
                println("AssignmentRepository - Answer submission successful: $submissionResponse")
                println("AssignmentRepository - Parsed isCorrect value: ${submissionResponse?.isCorrect}")
                println("AssignmentRepository - Parsed numberStr: ${submissionResponse?.numberStr}")
                println("AssignmentRepository - Raw response body: ${response.body()}")
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
    
    suspend fun completePersonalAssignment(personalAssignmentId: Int): Result<Unit> {
        return try {
            println("AssignmentRepository - Completing personal assignment: $personalAssignmentId")
            val response = apiService.completePersonalAssignment(personalAssignmentId)
            
            if (response.isSuccessful && response.body()?.success == true) {
                println("AssignmentRepository - Personal assignment completed successfully")
                Result.success(Unit)
            } else {
                val errorMessage = response.body()?.message ?: response.body()?.error ?: "Unknown error"
                println("AssignmentRepository - Personal assignment completion error: $errorMessage")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            println("AssignmentRepository - Personal assignment completion Exception: ${e.message}")
            Result.failure(e)
        }
    }
}
