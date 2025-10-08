package com.example.voicetutor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.repository.AnalysisRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val analysisRepository: AnalysisRepository
) : ViewModel() {
    
    private val _studentAnalysis = MutableStateFlow<LearningAnalysis?>(null)
    val studentAnalysis: StateFlow<LearningAnalysis?> = _studentAnalysis.asStateFlow()
    
    private val _classAnalysis = MutableStateFlow<ClassAnalysis?>(null)
    val classAnalysis: StateFlow<ClassAnalysis?> = _classAnalysis.asStateFlow()
    
    private val _subjectAnalysis = MutableStateFlow<SubjectAnalysis?>(null)
    val subjectAnalysis: StateFlow<SubjectAnalysis?> = _subjectAnalysis.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * 학생별 학습 분석을 로드합니다
     */
    fun loadStudentAnalysis(
        studentId: Int,
        dateRange: DateRange? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            analysisRepository.getStudentAnalysis(studentId, dateRange)
                .onSuccess { analysis ->
                    _studentAnalysis.value = analysis
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 클래스 전체 분석을 로드합니다
     */
    fun loadClassAnalysis(
        classId: Int,
        dateRange: DateRange? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            analysisRepository.getClassAnalysis(classId, dateRange)
                .onSuccess { analysis ->
                    _classAnalysis.value = analysis
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 과목별 분석을 로드합니다
     */
    fun loadSubjectAnalysis(
        subject: String,
        studentId: Int? = null,
        classId: Int? = null,
        dateRange: DateRange? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            analysisRepository.getSubjectAnalysis(subject, studentId, classId, dateRange)
                .onSuccess { analysis ->
                    _subjectAnalysis.value = analysis
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 학생 분석 초기화
     */
    fun clearStudentAnalysis() {
        _studentAnalysis.value = null
    }
    
    /**
     * 클래스 분석 초기화
     */
    fun clearClassAnalysis() {
        _classAnalysis.value = null
    }
    
    /**
     * 과목 분석 초기화
     */
    fun clearSubjectAnalysis() {
        _subjectAnalysis.value = null
    }
    
    /**
     * 에러 메시지 초기화
     */
    fun clearError() {
        _error.value = null
    }
}
