package com.example.voicetutor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicetutor.data.models.AssignmentData
import com.example.voicetutor.data.models.AssignmentStatus
import com.example.voicetutor.data.models.QuestionData
import com.example.voicetutor.data.models.StudentResult
import com.example.voicetutor.ui.navigation.RecentAssignment
import com.example.voicetutor.data.network.AssignmentSubmissionRequest
import com.example.voicetutor.data.network.AssignmentSubmissionResult
import com.example.voicetutor.data.network.CreateAssignmentRequest
import com.example.voicetutor.data.network.UpdateAssignmentRequest
import com.example.voicetutor.data.repository.AssignmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssignmentViewModel @Inject constructor(
    private val assignmentRepository: AssignmentRepository
) : ViewModel() {
    
    private val _assignments = MutableStateFlow<List<AssignmentData>>(emptyList())
    val assignments: StateFlow<List<AssignmentData>> = _assignments.asStateFlow()
    
    private val _currentAssignment = MutableStateFlow<AssignmentData?>(null)
    val currentAssignment: StateFlow<AssignmentData?> = _currentAssignment.asStateFlow()
    
    private val _recentAssignment = MutableStateFlow<RecentAssignment?>(null)
    val recentAssignment: StateFlow<RecentAssignment?> = _recentAssignment.asStateFlow()
    
    private val _assignmentResults = MutableStateFlow<List<StudentResult>>(emptyList())
    val assignmentResults: StateFlow<List<StudentResult>> = _assignmentResults.asStateFlow()
    
    private val _assignmentQuestions = MutableStateFlow<List<QuestionData>>(emptyList())
    val assignmentQuestions: StateFlow<List<QuestionData>> = _assignmentQuestions.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadAllAssignments(teacherId: String? = null, classId: String? = null, status: AssignmentStatus? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            assignmentRepository.getAllAssignments(teacherId, classId, status)
                .onSuccess { assignments ->
                    _assignments.value = assignments
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun loadStudentAssignments(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            println("AssignmentViewModel - Loading assignments for student ID: $studentId")
            assignmentRepository.getStudentAssignments(studentId)
                .onSuccess { assignments ->
                    println("AssignmentViewModel - Received ${assignments.size} assignments")
                    assignments.forEach { 
                        println("  - ${it.title} (${it.subject})")
                    }
                    _assignments.value = assignments
                }
                .onFailure { exception ->
                    println("AssignmentViewModel - Error: ${exception.message}")
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun loadAssignmentById(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            assignmentRepository.getAssignmentById(id)
                .onSuccess { assignment ->
                    _currentAssignment.value = assignment
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun createAssignment(assignment: CreateAssignmentRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            assignmentRepository.createAssignment(assignment)
                .onSuccess { newAssignment ->
                    _currentAssignment.value = newAssignment
                    // Refresh assignments list
                    loadAllAssignments()
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun updateAssignment(id: Int, assignment: UpdateAssignmentRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            assignmentRepository.updateAssignment(id, assignment)
                .onSuccess { updatedAssignment ->
                    _currentAssignment.value = updatedAssignment
                    // Update in assignments list
                    _assignments.value = _assignments.value.map { 
                        if (it.id == id) updatedAssignment else it 
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun deleteAssignment(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            assignmentRepository.deleteAssignment(id)
                .onSuccess {
                    // Remove from assignments list
                    _assignments.value = _assignments.value.filter { it.id != id }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun saveAssignmentDraft(assignmentId: Int, draftContent: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            assignmentRepository.saveAssignmentDraft(assignmentId, draftContent)
                .onSuccess {
                    // Draft saved successfully
                    println("Draft saved for assignment $assignmentId")
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            _isLoading.value = false
        }
    }
    
    fun loadAssignmentResults(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            assignmentRepository.getAssignmentResults(id)
                .onSuccess { results ->
                    _assignmentResults.value = results
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun loadAssignmentQuestions(id: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            assignmentRepository.getAssignmentQuestions(id)
                .onSuccess { questions ->
                    _assignmentQuestions.value = questions
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun submitAssignment(id: Int, submission: AssignmentSubmissionRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            assignmentRepository.submitAssignment(id, submission)
                .onSuccess { result ->
                    // Handle submission result
                    // You might want to navigate to results screen or show success message
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun loadRecentAssignment(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            
            assignmentRepository.getAllAssignments()
                .onSuccess { assignments ->
                    // 가장 최근 과제를 찾아서 RecentAssignment로 변환
                    val recent = assignments.firstOrNull()?.let { assignment ->
                        RecentAssignment(
                            id = assignment.id.toString(),
                            title = assignment.title,
                            subject = assignment.subject.name,
                            progress = if (assignment.status == AssignmentStatus.COMPLETED) 1.0f else 0.3f,
                            lastActivity = "방금 전", // TODO: 실제 마지막 활동 시간으로 변경
                            isUrgent = assignment.dueAt.contains("오늘") || assignment.dueAt.contains("내일")
                        )
                    }
                    _recentAssignment.value = recent
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
    
    fun setInitialAssignments(assignments: List<AssignmentData>) {
        _assignments.value = assignments
    }
}
