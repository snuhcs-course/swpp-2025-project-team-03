package com.example.voicetutor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.network.CreateClassRequest
import com.example.voicetutor.data.repository.ClassRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassViewModel @Inject constructor(
    private val classRepository: ClassRepository
) : ViewModel() {
    
    private val _classes = MutableStateFlow<List<ClassData>>(emptyList())
    val classes: StateFlow<List<ClassData>> = _classes.asStateFlow()
    
    private val _currentClass = MutableStateFlow<ClassData?>(null)
    val currentClass: StateFlow<ClassData?> = _currentClass.asStateFlow()
    
    private val _classStudents = MutableStateFlow<List<Student>>(emptyList())
    val classStudents: StateFlow<List<Student>> = _classStudents.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadClasses(teacherId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            classRepository.getClasses(teacherId)
                .onSuccess { classes ->
                    _classes.value = classes
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun loadClassById(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            classRepository.getClassById(id)
                .onSuccess { classData ->
                    _currentClass.value = classData
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun loadClassStudents(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            classRepository.getClassStudents(id)
                .onSuccess { students ->
                    _classStudents.value = students
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun createClass(createClassRequest: CreateClassRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            classRepository.createClass(createClassRequest)
                .onSuccess { classData ->
                    // 새로 생성된 클래스를 목록에 추가
                    _classes.value = _classes.value + classData
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun refreshClasses(teacherId: String) {
        loadClasses(teacherId)
    }
    
    fun enrollStudentToClass(classId: Int, studentId: Int? = null, name: String? = null, email: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            classRepository.enrollStudentToClass(classId, studentId, name, email)
                .onSuccess {
                    // 등록 성공 후 해당 반 학생 목록 갱신
                    loadClassStudents(classId)
                }
                .onFailure { e ->
                    _error.value = e.message
                }
            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }
    
    fun loadClassStudentsStatistics(classId: Int, callback: (Result<ClassStudentsStatistics>) -> Unit) {
        viewModelScope.launch {
            val result = classRepository.getClassStudentsStatistics(classId)
            callback(result)
        }
    }
}
