package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    /**
     * AI에게 메시지를 전송하고 응답을 받습니다
     */
    suspend fun sendMessageToAI(
        assignmentId: String,
        studentId: Int,
        message: String,
        conversationHistory: List<ConversationMessage>? = null
    ): Result<AIConversationResponse> {
        return try {
            val request = AIConversationRequest(
                assignmentId = assignmentId,
                studentId = studentId,
                message = message,
                conversationHistory = conversationHistory
            )
            
            val response = apiService.sendAIMessage(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val aiResponse = response.body()?.data
                if (aiResponse != null) {
                    Result.success(aiResponse)
                } else {
                    Result.failure(Exception("AI 응답을 받을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "AI 통신에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 음성을 텍스트로 변환합니다
     */
    suspend fun recognizeVoice(audioData: String): Result<VoiceRecognitionResponse> {
        return try {
            val request = VoiceRecognitionRequest(audioData = audioData)
            val response = apiService.recognizeVoice(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val recognitionResult = response.body()?.data
                if (recognitionResult != null) {
                    Result.success(recognitionResult)
                } else {
                    Result.failure(Exception("음성 인식 결과를 받을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "음성 인식에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 퀴즈 답안을 제출합니다
     */
    suspend fun submitQuiz(
        assignmentId: String,
        studentId: Int,
        answers: List<QuizAnswer>,
        timeSpent: Long
    ): Result<QuizSubmissionResponse> {
        return try {
            val request = QuizSubmissionRequest(
                assignmentId = assignmentId,
                studentId = studentId,
                answers = answers,
                timeSpent = timeSpent
            )
            
            val response = apiService.submitQuiz(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val submissionResult = response.body()?.data
                if (submissionResult != null) {
                    Result.success(submissionResult)
                } else {
                    Result.failure(Exception("퀴즈 제출 결과를 받을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "퀴즈 제출에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
}
