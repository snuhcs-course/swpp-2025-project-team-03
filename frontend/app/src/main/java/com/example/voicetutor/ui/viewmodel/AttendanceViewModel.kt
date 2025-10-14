package com.example.voicetutor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository
) : ViewModel() {
    
    private val _attendanceRecords = MutableStateFlow<List<AttendanceRecord>>(emptyList())
    val attendanceRecords: StateFlow<List<AttendanceRecord>> = _attendanceRecords.asStateFlow()
    
    private val _attendanceSummary = MutableStateFlow<AttendanceSummary?>(null)
    val attendanceSummary: StateFlow<AttendanceSummary?> = _attendanceSummary.asStateFlow()
    
    private val _classAttendanceSummary = MutableStateFlow<ClassAttendanceSummary?>(null)
    val classAttendanceSummary: StateFlow<ClassAttendanceSummary?> = _classAttendanceSummary.asStateFlow()
    
    private val _recordResult = MutableStateFlow<AttendanceRecordResponse?>(null)
    val recordResult: StateFlow<AttendanceRecordResponse?> = _recordResult.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * 출석을 기록합니다
     */
    fun recordAttendance(
        classId: Int,
        date: String,
        records: List<AttendanceRecordUpdate>
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            attendanceRepository.recordAttendance(classId, date, records)
                .onSuccess { result ->
                    _recordResult.value = result
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 출석 기록을 조회합니다
     */
    fun queryAttendance(
        classId: Int? = null,
        studentId: Int? = null,
        startDate: String,
        endDate: String
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            attendanceRepository.queryAttendance(classId, studentId, startDate, endDate)
                .onSuccess { response ->
                    _attendanceRecords.value = response.attendanceRecords
                    response.summary?.let { _attendanceSummary.value = it }
                    response.classSummary?.let { _classAttendanceSummary.value = it }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 학생별 출석 요약을 로드합니다
     */
    fun loadStudentAttendanceSummary(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            attendanceRepository.getStudentAttendanceSummary(studentId)
                .onSuccess { summary ->
                    _attendanceSummary.value = summary
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 클래스 출석을 로드합니다
     */
    fun loadClassAttendance(classId: Int, date: String = getCurrentDate()) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            attendanceRepository.getClassAttendance(classId, date)
                .onSuccess { summary ->
                    _classAttendanceSummary.value = summary
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 출석 상태를 업데이트합니다
     */
    fun updateAttendanceStatus(
        studentId: Int,
        status: AttendanceStatus,
        checkInTime: String? = null,
        notes: String? = null
    ) {
        val currentRecords = _attendanceRecords.value.toMutableList()
        val recordIndex = currentRecords.indexOfFirst { it.studentId == studentId }
        
        if (recordIndex != -1) {
            val updatedRecord = currentRecords[recordIndex].copy(
                status = status,
                checkInTime = checkInTime,
                notes = notes
            )
            currentRecords[recordIndex] = updatedRecord
            _attendanceRecords.value = currentRecords
        }
    }
    
    /**
     * 현재 날짜를 YYYY-MM-DD 형식으로 반환합니다
     */
    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }
    
    /**
     * 출석 기록 초기화
     */
    fun clearAttendanceRecords() {
        _attendanceRecords.value = emptyList()
    }
    
    /**
     * 출석 요약 초기화
     */
    fun clearAttendanceSummary() {
        _attendanceSummary.value = null
    }
    
    /**
     * 클래스 출석 요약 초기화
     */
    fun clearClassAttendanceSummary() {
        _classAttendanceSummary.value = null
    }
    
    /**
     * 기록 결과 초기화
     */
    fun clearRecordResult() {
        _recordResult.value = null
    }
    
    /**
     * 에러 메시지 초기화
     */
    fun clearError() {
        _error.value = null
    }
}
