package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    /**
     * 메시지를 전송합니다
     */
    suspend fun sendMessage(
        teacherId: Int,
        studentIds: List<Int>,
        content: String
    ): Result<SendMessageResponse> {
        return try {
            val request = SendMessageRequest(
                teacherId = teacherId,
                studentIds = studentIds,
                content = content
            )
            
            val response = apiService.sendMessage(request)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val sendResult = response.body()?.data
                if (sendResult != null) {
                    Result.success(sendResult)
                } else {
                    Result.failure(Exception("메시지 전송 결과를 받을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "메시지 전송에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * 메시지 목록을 가져옵니다
     */
    suspend fun getMessages(
        userId: Int,
        messageType: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ): Result<MessageListResponse> {
        return try {
            val response = apiService.getMessages(userId, messageType, limit, offset)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val messageList = response.body()?.data
                if (messageList != null) {
                    Result.success(messageList)
                } else {
                    Result.failure(Exception("메시지 목록을 받을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.message ?: "메시지 목록을 가져오는데 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
}