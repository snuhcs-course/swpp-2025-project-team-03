package com.example.voicetutor.data.models

import com.google.gson.annotations.SerializedName

/**
 * 출석 상태 열거형
 */
// enum class AttendanceStatus {
//     @SerializedName("PRESENT")
//     PRESENT,        // 출석
    
//     @SerializedName("ABSENT")
//     ABSENT,         // 결석
    
//     @SerializedName("LATE")
//     LATE,           // 지각
    
//     @SerializedName("EXCUSED")
//     EXCUSED         // 공결
// }

/**
 * 출석 기록 모델
 */
// data class AttendanceRecord(
//     @SerializedName("id")
//     val id: String,
//     @SerializedName("studentId")
//     val studentId: Int,
//     @SerializedName("studentName")
//     val studentName: String,
//     @SerializedName("classId")
//     val classId: Int,
//     @SerializedName("date")
//     val date: String, // YYYY-MM-DD format
//     @SerializedName("status")
//     val status: AttendanceStatus,
//     @SerializedName("checkInTime")
//     val checkInTime: String? = null, // HH:MM format
//     @SerializedName("notes")
//     val notes: String? = null
// )

/**
 * 출석 요약 모델
 */
// data class AttendanceSummary(
//     @SerializedName("studentId")
//     val studentId: Int,
//     @SerializedName("studentName")
//     val studentName: String,
//     @SerializedName("totalDays")
//     val totalDays: Int,
//     @SerializedName("presentDays")
//     val presentDays: Int,
//     @SerializedName("absentDays")
//     val absentDays: Int,
//     @SerializedName("lateDays")
//     val lateDays: Int,
//     @SerializedName("excusedDays")
//     val excusedDays: Int,
//     @SerializedName("attendanceRate")
//     val attendanceRate: Float // percentage
// )

/**
 * 클래스 출석 요약 모델
 */
// data class ClassAttendanceSummary(
//     @SerializedName("classId")
//     val classId: Int,
//     @SerializedName("className")
//     val className: String,
//     @SerializedName("date")
//     val date: String,
//     @SerializedName("totalStudents")
//     val totalStudents: Int,
//     @SerializedName("presentCount")
//     val presentCount: Int,
//     @SerializedName("absentCount")
//     val absentCount: Int,
//     @SerializedName("lateCount")
//     val lateCount: Int,
//     @SerializedName("excusedCount")
//     val excusedCount: Int,
//     @SerializedName("attendanceRate")
//     val attendanceRate: Float,
//     @SerializedName("attendanceRecords")
//     val attendanceRecords: List<AttendanceRecord>
// )

/**
 * 출석 기록 요청 모델
 */
// data class AttendanceRecordRequest(
//     @SerializedName("classId")
//     val classId: Int,
//     @SerializedName("date")
//     val date: String,
//     @SerializedName("records")
//     val records: List<AttendanceRecordUpdate>
// )

/**
 * 출석 기록 업데이트 모델
 */
// data class AttendanceRecordUpdate(
//     @SerializedName("studentId")
//     val studentId: Int,
//     @SerializedName("status")
//     val status: AttendanceStatus,
//     @SerializedName("checkInTime")
//     val checkInTime: String? = null,
//     @SerializedName("notes")
//     val notes: String? = null
// )

/**
 * 출석 기록 응답 모델
 */
// data class AttendanceRecordResponse(
//     @SerializedName("success")
//     val success: Boolean,
//     @SerializedName("updatedCount")
//     val updatedCount: Int,
//     @SerializedName("message")
//     val message: String
// )

/**
 * 출석 조회 요청 모델
 */
// data class AttendanceQueryRequest(
//     @SerializedName("classId")
//     val classId: Int? = null,
//     @SerializedName("studentId")
//     val studentId: Int? = null,
//     @SerializedName("startDate")
//     val startDate: String,
//     @SerializedName("endDate")
//     val endDate: String
// )

/**
 * 출석 조회 응답 모델
 */
// data class AttendanceQueryResponse(
//     @SerializedName("attendanceRecords")
//     val attendanceRecords: List<AttendanceRecord>,
//     @SerializedName("summary")
//     val summary: AttendanceSummary? = null,
//     @SerializedName("classSummary")
//     val classSummary: ClassAttendanceSummary? = null
// )
