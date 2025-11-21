package com.example.voicetutor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.network.CreateClassRequest
import com.example.voicetutor.data.repository.ClassRepository
import com.example.voicetutor.ui.utils.ErrorMessageMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassViewModel @Inject constructor(
    private val classRepository: ClassRepository,
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
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
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
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
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
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
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
                    _classes.value = _classes.value + classData
                }
                .onFailure { exception ->
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                }

            _isLoading.value = false
        }
    }

    fun refreshClasses(teacherId: String) {
        loadClasses(teacherId)
    }

    fun enrollStudentToClass(classId: Int, studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            classRepository.enrollStudentToClass(classId, studentId)
                .onSuccess {
                    loadClassStudents(classId)
                }
                .onFailure { e ->
                    _error.value = ErrorMessageMapper.getErrorMessage(e)
                }
            _isLoading.value = false
        }
    }

    fun removeStudentFromClass(classId: Int, studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            classRepository.removeStudentFromClass(classId, studentId)
                .onSuccess {
                    loadClassStudents(classId)
                }
                .onFailure { e ->
                    _error.value = ErrorMessageMapper.getErrorMessage(e)
                }
            _isLoading.value = false
        }
    }

    fun deleteClass(classId: Int, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            classRepository.removeClassById(classId)
                .onSuccess {
                    _classes.value = _classes.value.filterNot { it.id == classId }
                    onResult(true)
                }
                .onFailure { e ->
                    _error.value = ErrorMessageMapper.getErrorMessage(e)
                    onResult(false)
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
