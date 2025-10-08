package com.example.voicetutor.data.models

import com.google.gson.annotations.SerializedName

data class Student(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("className")
    val className: String,
    @SerializedName("classId")
    val classId: Int,
    @SerializedName("completedAssignments")
    val completedAssignments: Int,
    @SerializedName("totalAssignments")
    val totalAssignments: Int,
    @SerializedName("averageScore")
    val averageScore: Int,
    @SerializedName("lastActive")
    val lastActive: String
)

data class AllStudentsStudent(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("className")
    val className: String,
    @SerializedName("completedAssignments")
    val completedAssignments: Int,
    @SerializedName("totalAssignments")
    val totalAssignments: Int,
    @SerializedName("averageScore")
    val averageScore: Int,
    @SerializedName("lastActive")
    val lastActive: String
)


data class StudentProgress(
    @SerializedName("studentId")
    val studentId: Int,
    @SerializedName("totalAssignments")
    val totalAssignments: Int,
    @SerializedName("completedAssignments")
    val completedAssignments: Int,
    @SerializedName("averageScore")
    val averageScore: Double,
    @SerializedName("weeklyProgress")
    val weeklyProgress: List<WeeklyProgress>,
    @SerializedName("subjectBreakdown")
    val subjectBreakdown: List<SubjectProgress>
)

data class WeeklyProgress(
    @SerializedName("week")
    val week: String,
    @SerializedName("assignmentsCompleted")
    val assignmentsCompleted: Int,
    @SerializedName("averageScore")
    val averageScore: Double
)

data class SubjectProgress(
    @SerializedName("subject")
    val subject: String,
    @SerializedName("completedAssignments")
    val completedAssignments: Int,
    @SerializedName("totalAssignments")
    val totalAssignments: Int,
    @SerializedName("averageScore")
    val averageScore: Double
)

data class ClassMessageStudent(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("studentId")
    val studentId: String,
    @SerializedName("completedAssignments")
    val completedAssignments: Int,
    @SerializedName("totalAssignments")
    val totalAssignments: Int,
    @SerializedName("averageScore")
    val averageScore: Int,
    @SerializedName("lastActive")
    val lastActive: String,
    @SerializedName("isSelected")
    val isSelected: Boolean = false
)
