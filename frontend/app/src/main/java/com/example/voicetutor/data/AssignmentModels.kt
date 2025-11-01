package com.example.voicetutor.data

enum class AssignmentStatus {
    IN_PROGRESS,  // 진행중
    COMPLETED,    // 완료
    DRAFT         // 임시저장
}


enum class AssignmentFilter {
    ALL,          // 전체
    IN_PROGRESS,  // 진행중
    COMPLETED     // 완료
}

data class AssignmentData(
    val id: Int,
    val title: String,
    val subject: String,
    val className: String,
    val dueDate: String,
    val submittedCount: Int,
    val totalCount: Int,
    val status: AssignmentStatus,
    // removed: type (AssignmentType)
)
