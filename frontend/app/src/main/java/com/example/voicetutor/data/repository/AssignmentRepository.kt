package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.AssignmentData
import com.example.voicetutor.data.models.AssignmentStatus
import com.example.voicetutor.data.models.QuestionData
import com.example.voicetutor.data.models.StudentResult
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.CreateAssignmentResponse
import com.example.voicetutor.data.network.S3UploadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
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
}
