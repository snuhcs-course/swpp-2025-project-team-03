package com.example.voicetutor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.repository.StudentEditRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentEditViewModel @Inject constructor(
    private val studentEditRepository: StudentEditRepository
) : ViewModel() {
    
    private val _editResult = MutableStateFlow<StudentEditResponse?>(null)
    val editResult: StateFlow<StudentEditResponse?> = _editResult.asStateFlow()
    
    private val _deleteResult = MutableStateFlow<StudentDeleteResponse?>(null)
    val deleteResult: StateFlow<StudentDeleteResponse?> = _deleteResult.asStateFlow()
    
    // Status update and password reset are not supported by current backend API
    
    private val _classChangeResult = MutableStateFlow<StudentClassChangeResponse?>(null)
    val classChangeResult: StateFlow<StudentClassChangeResponse?> = _classChangeResult.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * 학생 정보를 편집합니다
     */
    fun editStudent(
        studentId: Int,
        name: String,
        email: String,
        phoneNumber: String? = null,
        parentName: String? = null,
        parentPhone: String? = null,
        address: String? = null,
        birthDate: String? = null,
        notes: String? = null,
        isActive: Boolean = true
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            studentEditRepository.editStudent(
                studentId = studentId,
                name = name,
                email = email
            ).onSuccess { result ->
                _editResult.value = result
            }.onFailure { exception ->
                _error.value = exception.message
            }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 학생을 삭제합니다
     */
    fun deleteStudent(
        studentId: Int,
        reason: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            studentEditRepository.deleteStudent(studentId)
                .onSuccess { result ->
                    _deleteResult.value = result
                }.onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    // Status update and password reset functions are not supported by current backend API
    
    /**
     * 학생의 클래스를 변경합니다
     */
    fun changeStudentClass(
        studentId: Int,
        fromClassId: Int,
        toClassId: Int,
        reason: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            studentEditRepository.changeStudentClass(studentId, fromClassId, toClassId, reason)
                .onSuccess { result ->
                    _classChangeResult.value = result
                }.onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 편집 결과 초기화
     */
    fun clearEditResult() {
        _editResult.value = null
    }
    
    /**
     * 삭제 결과 초기화
     */
    fun clearDeleteResult() {
        _deleteResult.value = null
    }
    
    // Status update and password reset functions are not supported by current backend API
    
    /**
     * 클래스 변경 결과 초기화
     */
    fun clearClassChangeResult() {
        _classChangeResult.value = null
    }
    
    /**
     * 에러 메시지 초기화
     */
    fun clearError() {
        _error.value = null
    }
}
