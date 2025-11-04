package com.example.voicetutor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.repository.StudentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StudentViewModel @Inject constructor(
    private val studentRepository: StudentRepository
) : ViewModel() {
    
    private val _students = MutableStateFlow<List<Student>>(emptyList())
    val students: StateFlow<List<Student>> = _students.asStateFlow()
    
    private val _currentStudent = MutableStateFlow<Student?>(null)
    val currentStudent: StateFlow<Student?> = _currentStudent.asStateFlow()
    
    private val _studentAssignments = MutableStateFlow<List<AssignmentData>>(emptyList())
    val studentAssignments: StateFlow<List<AssignmentData>> = _studentAssignments.asStateFlow()
    
    private val _studentProgress = MutableStateFlow<StudentProgress?>(null)
    val studentProgress: StateFlow<StudentProgress?> = _studentProgress.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadAllStudents(teacherId: String? = null, classId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            studentRepository.getAllStudents(teacherId, classId)
                .onSuccess { students ->
                    _students.value = students
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun loadStudentById(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            studentRepository.getStudentById(id)
                .onSuccess { student ->
                    _currentStudent.value = student
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun loadStudentAssignments(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            studentRepository.getStudentAssignments(id)
                .onSuccess { assignments ->
                    _studentAssignments.value = assignments
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun loadStudentProgress(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            studentRepository.getStudentProgress(id)
                .onSuccess { progress ->
                    _studentProgress.value = progress
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
}
