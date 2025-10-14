package com.example.voicetutor.data.models

import com.google.gson.annotations.SerializedName

/**
 * 메시지 전송 요청 모델
 */
data class SendMessageRequest(
    @SerializedName("teacherId")
    val teacherId: Int,
    @SerializedName("studentIds")
    val studentIds: List<Int>,
    @SerializedName("message")
    val message: String,
    @SerializedName("messageType")
    val messageType: String = "TEXT" // TEXT, VOICE, IMAGE
)

/**
 * 메시지 전송 응답 모델
 */
data class SendMessageResponse(
    @SerializedName("messageId")
    val messageId: String,
    @SerializedName("sentCount")
    val sentCount: Int,
    @SerializedName("failedCount")
    val failedCount: Int,
    @SerializedName("timestamp")
    val timestamp: Long
)

/**
 * 메시지 모델
 */
data class Message(
    @SerializedName("id")
    val id: String,
    @SerializedName("senderId")
    val senderId: Int,
    @SerializedName("senderName")
    val senderName: String,
    @SerializedName("receiverId")
    val receiverId: Int,
    @SerializedName("receiverName")
    val receiverName: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("messageType")
    val messageType: String,
    @SerializedName("timestamp")
    val timestamp: Long,
    @SerializedName("isRead")
    val isRead: Boolean = false
)

/**
 * 메시지 목록 응답 모델
 */
data class MessageListResponse(
    @SerializedName("messages")
    val messages: List<Message>,
    @SerializedName("totalCount")
    val totalCount: Int,
    @SerializedName("unreadCount")
    val unreadCount: Int
)
