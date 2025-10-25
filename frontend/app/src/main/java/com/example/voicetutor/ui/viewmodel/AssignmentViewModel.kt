package com.example.voicetutor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicetutor.data.models.AssignmentData
import com.example.voicetutor.data.models.AssignmentStatus
import com.example.voicetutor.data.models.AssignmentFilter
import com.example.voicetutor.data.models.PersonalAssignmentFilter
import com.example.voicetutor.data.models.QuestionData
import com.example.voicetutor.data.models.StudentResult
import com.example.voicetutor.data.models.PersonalAssignmentData
import com.example.voicetutor.data.models.PersonalAssignmentStatus
import com.example.voicetutor.data.models.CourseClass
import com.example.voicetutor.data.models.Subject
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

// 학생별 통계 데이터 클래스
data class StudentStats(
    val totalAssignments: Int,
    val completedAssignments: Int,
    val inProgressAssignments: Int,
    val completionRate: Float
)

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
    
    // 학생별 통계
    private val _studentStats = MutableStateFlow<StudentStats?>(null)
    val studentStats: StateFlow<StudentStats?> = _studentStats.asStateFlow()
    
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
            
            println("AssignmentViewModel - Loading personal assignments for student ID: $studentId")
            // 새로운 personal assignment API 사용
            assignmentRepository.getPersonalAssignments(studentId)
                .onSuccess { personalAssignments: List<PersonalAssignmentData> ->
                    println("AssignmentViewModel - Received ${personalAssignments.size} personal assignments")
                    
                    // PersonalAssignmentData를 AssignmentData로 변환
                    val convertedAssignments: List<AssignmentData> = personalAssignments.map { personalAssignment: PersonalAssignmentData ->
                        AssignmentData(
                            id = personalAssignment.assignment.id,
                            title = personalAssignment.assignment.title,
                            description = personalAssignment.assignment.description,
                            totalQuestions = personalAssignment.assignment.totalQuestions,
                            createdAt = null, // PersonalAssignment에는 createdAt이 없음
                            visibleFrom = personalAssignment.assignment.visibleFrom,
                            dueAt = personalAssignment.assignment.dueAt,
                            courseClass = CourseClass(
                                id = 0, // PersonalAssignment에는 courseClass 정보가 없으므로 임시값
                                name = "개인 과제",
                                description = null,
                                subject = Subject(
                                    id = 0,
                                    name = "개인 과제",
                                    code = null
                                ),
                                teacherName = "시스템",
                                startDate = "",
                                endDate = "",
                                studentCount = 1,
                                createdAt = ""
                            ),
                            materials = null,
                            grade = personalAssignment.assignment.grade,
                            // Personal Assignment 정보 추가
                            personalAssignmentStatus = personalAssignment.status,
                            solvedNum = personalAssignment.solvedNum
                        )
                    }
                    
                    _assignments.value = convertedAssignments
                    convertedAssignments.forEach { assignment: AssignmentData ->
                        println("  - ${assignment.title}")
                        println("    subject: ${assignment.courseClass.subject.name}")
                        println("    dueAt: ${assignment.dueAt}")
                    }
                    
                    // 학생별 통계 계산 (personal assignment 기반)
                    calculateStudentStatsFromPersonalAssignments(personalAssignments)
                }
                .onFailure { exception ->
                    println("AssignmentViewModel - Error: ${exception.message}")
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    // Personal assignment 필터링을 위한 메서드 (기존 AssignmentFilter 사용)
    fun loadStudentAssignmentsWithFilter(studentId: Int, filter: AssignmentFilter) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            println("AssignmentViewModel - Loading personal assignments with filter for student ID: $studentId, filter: $filter")
            assignmentRepository.getPersonalAssignments(studentId)
                .onSuccess { personalAssignments: List<PersonalAssignmentData> ->
                    println("AssignmentViewModel - Received ${personalAssignments.size} personal assignments")
                    
                    // 필터링 적용
                    val filteredAssignments = when (filter) {
                        AssignmentFilter.ALL -> personalAssignments
                        AssignmentFilter.IN_PROGRESS -> personalAssignments.filter { 
                            it.status == PersonalAssignmentStatus.NOT_STARTED || 
                            it.status == PersonalAssignmentStatus.IN_PROGRESS 
                        }
                        AssignmentFilter.COMPLETED -> personalAssignments.filter { 
                            it.status == PersonalAssignmentStatus.SUBMITTED 
                        }
                        else -> personalAssignments // 기본값으로 모든 과제 반환
                    }
                    
                    // PersonalAssignmentData를 AssignmentData로 변환
                    val convertedAssignments: List<AssignmentData> = filteredAssignments.map { personalAssignment: PersonalAssignmentData ->
                        AssignmentData(
                            id = personalAssignment.assignment.id,
                            title = personalAssignment.assignment.title,
                            description = personalAssignment.assignment.description,
                            totalQuestions = personalAssignment.assignment.totalQuestions,
                            createdAt = null,
                            visibleFrom = personalAssignment.assignment.visibleFrom,
                            dueAt = personalAssignment.assignment.dueAt,
                            courseClass = CourseClass(
                                id = 0,
                                name = "개인 과제",
                                description = null,
                                subject = Subject(
                                    id = 0,
                                    name = "개인 과제",
                                    code = null
                                ),
                                teacherName = "시스템",
                                startDate = "",
                                endDate = "",
                                studentCount = 1,
                                createdAt = ""
                            ),
                            materials = null,
                            grade = personalAssignment.assignment.grade,
                            // Personal Assignment 정보 추가
                            personalAssignmentStatus = personalAssignment.status,
                            solvedNum = personalAssignment.solvedNum
                        )
                    }
                    
                    _assignments.value = convertedAssignments
                    println("AssignmentViewModel - Filtered to ${convertedAssignments.size} assignments")
                    
                    // 학생별 통계 계산 (전체 personal assignment 기반)
                    calculateStudentStatsFromPersonalAssignments(personalAssignments)
                }
                .onFailure { exception ->
                    println("AssignmentViewModel - Error: ${exception.message}")
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    // Personal Assignment 전용 필터링 메서드
    fun loadStudentAssignmentsWithPersonalFilter(studentId: Int, filter: PersonalAssignmentFilter) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            println("AssignmentViewModel - Loading personal assignments with personal filter for student ID: $studentId, filter: $filter")
            assignmentRepository.getPersonalAssignments(studentId)
                .onSuccess { personalAssignments: List<PersonalAssignmentData> ->
                    println("AssignmentViewModel - Received ${personalAssignments.size} personal assignments")
                    
                    // Personal Assignment 상태에 따른 필터링
                    println("AssignmentViewModel - Filtering with filter: $filter")
                    personalAssignments.forEach { assignment ->
                        println("  - Assignment: ${assignment.assignment.title}, Status: ${assignment.status}")
                    }
                    
                    val filteredAssignments = when (filter) {
                        PersonalAssignmentFilter.ALL -> {
                            println("AssignmentViewModel - Filter: ALL - returning all ${personalAssignments.size} assignments")
                            personalAssignments
                        }
                        PersonalAssignmentFilter.NOT_STARTED -> {
                            val filtered = personalAssignments.filter { 
                                it.status == PersonalAssignmentStatus.NOT_STARTED 
                            }
                            println("AssignmentViewModel - Filter: NOT_STARTED - found ${filtered.size} assignments")
                            filtered
                        }
                        PersonalAssignmentFilter.IN_PROGRESS -> {
                            val filtered = personalAssignments.filter { 
                                it.status == PersonalAssignmentStatus.IN_PROGRESS 
                            }
                            println("AssignmentViewModel - Filter: IN_PROGRESS - found ${filtered.size} assignments")
                            filtered
                        }
                        PersonalAssignmentFilter.SUBMITTED -> {
                            val filtered = personalAssignments.filter { 
                                it.status == PersonalAssignmentStatus.SUBMITTED 
                            }
                            println("AssignmentViewModel - Filter: SUBMITTED - found ${filtered.size} assignments")
                            filtered
                        }
                    }
                    
                    // PersonalAssignmentData를 AssignmentData로 변환
                    val convertedAssignments: List<AssignmentData> = filteredAssignments.map { personalAssignment: PersonalAssignmentData ->
                        AssignmentData(
                            id = personalAssignment.assignment.id,
                            title = personalAssignment.assignment.title,
                            description = personalAssignment.assignment.description,
                            totalQuestions = personalAssignment.assignment.totalQuestions,
                            createdAt = null,
                            visibleFrom = personalAssignment.assignment.visibleFrom,
                            dueAt = personalAssignment.assignment.dueAt,
                            courseClass = CourseClass(
                                id = 0,
                                name = "개인 과제",
                                description = null,
                                subject = Subject(
                                    id = 0,
                                    name = "개인 과제",
                                    code = null
                                ),
                                teacherName = "시스템",
                                startDate = "",
                                endDate = "",
                                studentCount = 1,
 
                                createdAt = ""
                            ),
                            materials = null,
                            grade = personalAssignment.assignment.grade
                        )
                    }
                    
                    _assignments.value = convertedAssignments
                    println("AssignmentViewModel - Personal filter applied, showing ${convertedAssignments.size} assignments")
                    
                    // 학생별 통계 계산 (전체 personal assignment 기반)
                    calculateStudentStatsFromPersonalAssignments(personalAssignments)
                }
                .onFailure { exception ->
                    println("AssignmentViewModel - Error: ${exception.message}")
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    private fun calculateStudentStats(assignments: List<AssignmentData>) {
        val totalAssignments = assignments.size
        // TODO: 실제로는 PersonalAssignment의 status를 확인해야 하지만, 
        // 현재는 임시로 진행 중인 과제와 완료된 과제를 구분
        val inProgressAssignments = assignments.count { 
            // 임시로 모든 과제를 진행 중으로 처리
            true 
        }
        val completedAssignments = 0 // TODO: 실제 완료된 과제 수 계산
        val completionRate = if (totalAssignments > 0) completedAssignments.toFloat() / totalAssignments else 0f
        
        val stats = StudentStats(
            totalAssignments = totalAssignments,
            completedAssignments = completedAssignments,
            inProgressAssignments = inProgressAssignments,
            completionRate = completionRate
        )
        
        _studentStats.value = stats
        println("StudentStats - Total: $totalAssignments, Completed: $completedAssignments, InProgress: $inProgressAssignments")
    }
    
    private fun calculateStudentStatsFromPersonalAssignments(personalAssignments: List<PersonalAssignmentData>) {
        val totalAssignments = personalAssignments.size
        val completedAssignments = personalAssignments.count { personalAssignment: PersonalAssignmentData -> 
            personalAssignment.status == PersonalAssignmentStatus.SUBMITTED 
        }
        val inProgressAssignments = personalAssignments.count { personalAssignment: PersonalAssignmentData -> 
            personalAssignment.status == PersonalAssignmentStatus.IN_PROGRESS 
        }
        val completionRate = if (totalAssignments > 0) completedAssignments.toFloat() / totalAssignments else 0f
        
        val stats = StudentStats(
            totalAssignments = totalAssignments,
            completedAssignments = completedAssignments,
            inProgressAssignments = inProgressAssignments,
            completionRate = completionRate
        )
        
        _studentStats.value = stats
        println("AssignmentViewModel - Personal assignment stats calculated: $stats")
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
                            subject = assignment.courseClass.subject.name,
                            progress = 0.3f, // 임시로 진행률 설정
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
