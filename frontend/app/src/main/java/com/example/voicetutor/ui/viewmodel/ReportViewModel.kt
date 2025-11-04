package com.example.voicetutor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicetutor.data.models.CurriculumReportData
import com.example.voicetutor.data.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository
) : ViewModel() {
    
    private val _curriculumReport = MutableStateFlow<CurriculumReportData?>(null)
    val curriculumReport: StateFlow<CurriculumReportData?> = _curriculumReport.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadCurriculumReport(classId: Int, studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            reportRepository.getCurriculumReport(classId, studentId)
                .onSuccess { report ->
                    _curriculumReport.value = report
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun clearError() {
        _error.value = null
    }
    
    fun clearReport() {
        _curriculumReport.value = null
    }
}

