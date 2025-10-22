package com.example.voicetutor.data.models

import com.google.gson.annotations.SerializedName

data class ClassData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("subject")
    val subject: Subject,
    @SerializedName("description")
    val description: String,
    @SerializedName("teacherId")
    val teacherId: Int,
    @SerializedName("studentCount")
    val studentCount: Int,
    @SerializedName("createdAt")
    val createdAt: String? = null,
    @SerializedName("startedAt")
    val startedAt: String? = null,
    @SerializedName("endedAt")
    val endedAt: String? = null
)

data class MessageData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("content")
    val content: String,
    @SerializedName("teacherId")
    val teacherId: Int,
    @SerializedName("teacherName")
    val teacherName: String,
    @SerializedName("classId")
    val classId: Int,
    // @SerializedName("recipientIds")
    // val recipientIds: List<Int>? = null,
    @SerializedName("sentAt")
    val sentAt: String,
    // @SerializedName("isRead")
    // val isRead: Boolean = false
)

data class Subject(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("code")
    val code: String
)