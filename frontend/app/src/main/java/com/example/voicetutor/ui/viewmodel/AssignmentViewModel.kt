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
import com.example.voicetutor.data.models.PersonalAssignmentQuestion
import com.example.voicetutor.data.models.PersonalAssignmentStatistics
import com.example.voicetutor.data.models.AnswerSubmissionResponse
import com.example.voicetutor.audio.RecordingState
import com.example.voicetutor.ui.navigation.RecentAssignment
import com.example.voicetutor.data.network.AssignmentSubmissionRequest
import com.example.voicetutor.ui.utils.ErrorMessageMapper
import com.example.voicetutor.data.network.AssignmentSubmissionResult
import com.example.voicetutor.data.network.CreateAssignmentRequest
import com.example.voicetutor.data.network.CreateAssignmentResponse
import com.example.voicetutor.data.network.S3UploadStatus
import com.example.voicetutor.data.network.UpdateAssignmentRequest
import com.example.voicetutor.data.repository.AssignmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import javax.inject.Inject
import java.io.File

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
    
    private val _isCreatingAssignment = MutableStateFlow(false)
    val isCreatingAssignment: StateFlow<Boolean> = _isCreatingAssignment.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _uploadProgress = MutableStateFlow(0f.coerceIn(0f, 1f))
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()
    
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()
    
    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess: StateFlow<Boolean> = _uploadSuccess.asStateFlow()
    
    private val _s3UploadStatus = MutableStateFlow<S3UploadStatus?>(null)
    val s3UploadStatus: StateFlow<S3UploadStatus?> = _s3UploadStatus.asStateFlow()
    
    private val _isGeneratingQuestions = MutableStateFlow(false)
    val isGeneratingQuestions: StateFlow<Boolean> = _isGeneratingQuestions.asStateFlow()
    
    private val _questionGenerationSuccess = MutableStateFlow(false)
    val questionGenerationSuccess: StateFlow<Boolean> = _questionGenerationSuccess.asStateFlow()
    
    private val _questionGenerationError = MutableStateFlow<String?>(null)
    val questionGenerationError: StateFlow<String?> = _questionGenerationError.asStateFlow()
    
    private val _generatingAssignmentTitle = MutableStateFlow<String?>(null)
    val generatingAssignmentTitle: StateFlow<String?> = _generatingAssignmentTitle.asStateFlow()
    
    private var generatingAssignmentId: Int? = null
    
    private val _studentStats = MutableStateFlow<StudentStats?>(null)
    val studentStats: StateFlow<StudentStats?> = _studentStats.asStateFlow()
    
    private val _personalAssignmentQuestions = MutableStateFlow<List<PersonalAssignmentQuestion>>(emptyList())
    val personalAssignmentQuestions: StateFlow<List<PersonalAssignmentQuestion>> = _personalAssignmentQuestions.asStateFlow()
    
    private val _totalBaseQuestions = MutableStateFlow(0)
    val totalBaseQuestions: StateFlow<Int> = _totalBaseQuestions.asStateFlow()
    
    private val _personalAssignmentStatistics = MutableStateFlow<PersonalAssignmentStatistics?>(null)
    val personalAssignmentStatistics: StateFlow<PersonalAssignmentStatistics?> = _personalAssignmentStatistics.asStateFlow()
    
    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()
    
    private val _audioRecordingState = MutableStateFlow(RecordingState())
    val audioRecordingState: StateFlow<RecordingState> = _audioRecordingState.asStateFlow()
    
    private val _answerSubmissionResponse = MutableStateFlow<AnswerSubmissionResponse?>(null)
    val answerSubmissionResponse: StateFlow<AnswerSubmissionResponse?> = _answerSubmissionResponse.asStateFlow()
    
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _isAssignmentCompleted = MutableStateFlow(false)
    val isAssignmentCompleted: StateFlow<Boolean> = _isAssignmentCompleted.asStateFlow()
    
    private val _assignmentCorrectness = MutableStateFlow<List<com.example.voicetutor.data.models.AssignmentCorrectnessItem>>(emptyList())
    val assignmentCorrectness: StateFlow<List<com.example.voicetutor.data.models.AssignmentCorrectnessItem>> = _assignmentCorrectness.asStateFlow()

    private val _selectedAssignmentId = MutableStateFlow<Int?>(null)
    val selectedAssignmentId: StateFlow<Int?> = _selectedAssignmentId.asStateFlow()
    
    private val _selectedPersonalAssignmentId = MutableStateFlow<Int?>(null)
    val selectedPersonalAssignmentId: StateFlow<Int?> = _selectedPersonalAssignmentId.asStateFlow()
    
    data class AssignmentStatistics(
        val submittedStudents: Int,
        val totalStudents: Int,
        val averageScore: Int,
        val completionRate: Int
    )
    
    private val _assignmentStatistics = MutableStateFlow<AssignmentStatistics?>(null)
    val assignmentStatistics: StateFlow<AssignmentStatistics?> = _assignmentStatistics.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    fun setSelectedAssignmentIds(assignmentId: Int, personalAssignmentId: Int?) {
        _selectedAssignmentId.value = assignmentId
        _selectedPersonalAssignmentId.value = personalAssignmentId
    }
    
    fun loadAllAssignments(teacherId: String? = null, classId: String? = null, status: AssignmentStatus? = null, silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _isLoading.value = true
            }
            _error.value = null
            
            println("AssignmentViewModel - loadAllAssignments called with teacherId=$teacherId, classId=$classId, status=$status, silent=$silent")
            
            assignmentRepository.getAllAssignments(teacherId, classId, status)
                .onSuccess { assignments ->
                    println("AssignmentViewModel - ✅ Successfully loaded ${assignments.size} assignments")
                    _assignments.value = assignments
                }
                .onFailure { exception ->
                    println("AssignmentViewModel - ❌ Failed to load assignments: ${exception.message}")
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                }
            
            if (!silent) {
                _isLoading.value = false
            }
        }
    }
    
    fun loadStudentAssignments(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            println("AssignmentViewModel - Loading all assignments for student ID: $studentId")
            assignmentRepository.getAllAssignments()
                .onSuccess { allAssignments ->
                    println("AssignmentViewModel - Received ${allAssignments.size} total assignments")
                    _assignments.value = allAssignments
                    allAssignments.forEach { assignment ->
                        println("  - ${assignment.title}")
                        println("    courseClass: ${assignment.courseClass.name}")
                        println("    subject: ${assignment.courseClass.subject.name}")
                        println("    dueAt: ${assignment.dueAt}")
                    }
                    
                    calculateStudentStats(allAssignments)
                }
                .onFailure { exception ->
                    println("AssignmentViewModel - Error: ${exception.message}")
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                }
            
            _isLoading.value = false
        }
    }
    
    private fun calculateStudentStats(assignments: List<AssignmentData>) {
        val totalAssignments = assignments.size
        val inProgressAssignments = assignments.count { 
            true 
        }
        val completedAssignments = 0
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
        
        val pendingAssignments = personalAssignments.count { personalAssignment: PersonalAssignmentData -> 
            personalAssignment.status == PersonalAssignmentStatus.NOT_STARTED || 
            personalAssignment.status == PersonalAssignmentStatus.IN_PROGRESS
        }
        
        val completedAssignments = personalAssignments.count { personalAssignment: PersonalAssignmentData -> 
            personalAssignment.status == PersonalAssignmentStatus.SUBMITTED
        }
        
        val completionRate = if (totalAssignments > 0) completedAssignments.toFloat() / totalAssignments else 0f
        
        val stats = StudentStats(
            totalAssignments = pendingAssignments,
            completedAssignments = completedAssignments,
            inProgressAssignments = personalAssignments.count { it.status == PersonalAssignmentStatus.IN_PROGRESS },
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
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                }
            
            _isLoading.value = false
        }
    }
    
    private fun loadAssignmentResult(assignmentId: Int, fallbackTotalStudents: Int) {
        viewModelScope.launch {
            assignmentRepository.getAssignmentResult(assignmentId)
                .onSuccess { result ->
                    val submitted = result.submittedStudents ?: 0
                    val total = result.totalStudents ?: fallbackTotalStudents
                    val average = result.averageScore?.toInt() ?: 0
                    val completionRate = result.completionRate?.toInt() ?: if (total > 0) (submitted * 100) / total else 0
                    _assignmentStatistics.value = AssignmentStatistics(
                        submittedStudents = submitted,
                        totalStudents = total,
                        averageScore = average,
                        completionRate = completionRate
                    )
                }
                .onFailure {
                    loadAssignmentStatistics(assignmentId, fallbackTotalStudents)
                }
        }
    }
    
    fun loadAssignmentStatistics(assignmentId: Int, totalStudents: Int) {
        viewModelScope.launch {
            try {
                println("AssignmentViewModel - loadAssignmentStatistics called: assignmentId=$assignmentId, totalStudents=$totalStudents")
                
                assignmentRepository.getPersonalAssignments(assignmentId = assignmentId)
                    .onSuccess { personalAssignments ->
                        println("AssignmentViewModel - Loaded ${personalAssignments.size} personal assignments for assignment $assignmentId")
                        personalAssignments.forEach { pa ->
                            println("  - PersonalAssignment ID: ${pa.id}, Student: ${pa.student.displayName}, Status: ${pa.status}")
                        }
                        
                        val actualTotalStudents = personalAssignments.size
                        
                        coroutineScope {
                            val assignmentStatsDeferred = personalAssignments.map { personalAssignment ->
                                async {
                                    val stats = assignmentRepository.getPersonalAssignmentStatistics(personalAssignment.id).getOrNull()
                                    Pair(personalAssignment, stats)
                                }
                            }
                            
                            val assignmentStatsList = assignmentStatsDeferred.awaitAll()
                            
                            val submittedAssignments = assignmentStatsList.filter { (personalAssignment, stats) ->
                                val assignmentTotalQuestions = personalAssignment.assignment.totalQuestions
                                val solvedNum = personalAssignment.solvedNum
                                val hasStarted = !personalAssignment.startedAt.isNullOrEmpty()
                                
                                val isCompleted = when {
                                    personalAssignment.status == PersonalAssignmentStatus.SUBMITTED -> {
                                        println("AssignmentViewModel - PA ${personalAssignment.id} completed by status: SUBMITTED")
                                        true
                                    }
                                    !personalAssignment.submittedAt.isNullOrEmpty() -> {
                                        println("AssignmentViewModel - PA ${personalAssignment.id} completed by submitted_at: ${personalAssignment.submittedAt}")
                                        true
                                    }
                                    hasStarted && assignmentTotalQuestions > 0 && solvedNum >= assignmentTotalQuestions -> {
                                        println("AssignmentViewModel - PA ${personalAssignment.id} completed by solved_num: started=${hasStarted}, solved=${solvedNum}, total=${assignmentTotalQuestions}")
                                        true
                                    }
                                    hasStarted && stats != null && stats.totalProblem > 0 && stats.totalProblem == stats.solvedProblem -> {
                                        println("AssignmentViewModel - PA ${personalAssignment.id} completed by statistics: started=${hasStarted}, total=${stats.totalProblem}, solved=${stats.solvedProblem}")
                                        true
                                    }
                                    hasStarted && stats != null && stats.totalQuestions > 0 && stats.answeredQuestions >= stats.totalQuestions -> {
                                        println("AssignmentViewModel - PA ${personalAssignment.id} completed by answeredQuestions: started=${hasStarted}, answered=${stats.answeredQuestions}, total=${stats.totalQuestions}")
                                        true
                                    }
                                    else -> {
                                        println("AssignmentViewModel - PA ${personalAssignment.id} NOT completed: status=${personalAssignment.status}, submittedAt=${personalAssignment.submittedAt}, startedAt=${personalAssignment.startedAt}, solvedNum=${solvedNum}, totalQuestions=${assignmentTotalQuestions}, statsTotal=${stats?.totalProblem}, statsSolved=${stats?.solvedProblem}, answeredQuestions=${stats?.answeredQuestions}, totalQuestions=${stats?.totalQuestions}")
                                        false
                                    }
                                }
                                isCompleted
                            }
                            
                            val submittedCount = submittedAssignments.size
                            println("AssignmentViewModel - Submitted count: $submittedCount, Total students: $actualTotalStudents")
                            
                            if (submittedCount > 0) {
                                println("AssignmentViewModel - Loading statistics for $submittedCount submitted assignments")
                                
                                val statisticsList = submittedAssignments.mapNotNull { (_, stats) -> stats }
                                
                                println("AssignmentViewModel - Loaded ${statisticsList.size} statistics")
                                
                                val averageScore = if (statisticsList.isNotEmpty()) {
                                    val avg = statisticsList.map { it.accuracy }.average().toInt()
                                    println("AssignmentViewModel - Average score: $avg")
                                    avg
                                } else {
                                    0
                                }
                                
                                val completionRate = if (actualTotalStudents > 0) {
                                    val rate = (submittedCount * 100) / actualTotalStudents
                                    println("AssignmentViewModel - Completion rate: $rate%")
                                    rate
                                } else {
                                    0
                                }
                                
                                val stats = AssignmentStatistics(
                                    submittedStudents = submittedCount,
                                    totalStudents = actualTotalStudents,
                                    averageScore = averageScore,
                                    completionRate = completionRate
                                )
                                
                                println("AssignmentViewModel - Setting statistics: $stats")
                                _assignmentStatistics.value = stats
                            } else {
                                println("AssignmentViewModel - No submitted assignments found")
                                val stats = AssignmentStatistics(
                                    submittedStudents = 0,
                                    totalStudents = actualTotalStudents,
                                    averageScore = 0,
                                    completionRate = 0
                                )
                                println("AssignmentViewModel - Setting statistics: $stats")
                                _assignmentStatistics.value = stats
                            }
                        }
                    }
                    .onFailure { exception ->
                        println("AssignmentViewModel - Failed to load personal assignments: ${exception.message}")
                        exception.printStackTrace()
                        _assignmentStatistics.value = AssignmentStatistics(
                            submittedStudents = 0,
                            totalStudents = totalStudents,
                            averageScore = 0,
                            completionRate = 0
                        )
                    }
            } catch (e: Exception) {
                println("AssignmentViewModel - Exception loading assignment statistics: ${e.message}")
                e.printStackTrace()
                _assignmentStatistics.value = AssignmentStatistics(
                    submittedStudents = 0,
                    totalStudents = totalStudents,
                    averageScore = 0,
                    completionRate = 0
                )
            }
        }
    }

    fun loadAssignmentStatisticsAndResults(assignmentId: Int, totalStudents: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                println("AssignmentViewModel - loadAssignmentStatisticsAndResults called: assignmentId=$assignmentId, totalStudents=$totalStudents")
                
                assignmentRepository.getPersonalAssignments(assignmentId = assignmentId)
                    .onSuccess { personalAssignments ->
                        println("AssignmentViewModel - Loaded ${personalAssignments.size} personal assignments for assignment $assignmentId")
                        personalAssignments.forEach { pa ->
                            println("  - PersonalAssignment ID: ${pa.id}, Student: ${pa.student.displayName}, Status: ${pa.status}")
                        }
                        
                        val actualTotalStudents = personalAssignments.size
                        
                        coroutineScope {
                            val assignmentStatsDeferred = personalAssignments.map { personalAssignment ->
                                async {
                                    val stats = assignmentRepository.getPersonalAssignmentStatistics(personalAssignment.id).getOrNull()
                                    Pair(personalAssignment, stats)
                                }
                            }
                            
                            val assignmentStatsList = assignmentStatsDeferred.awaitAll()
                            
                            val results = assignmentStatsList.map { (personalAssignment, stats) ->
                                val score = stats?.averageScore?.toInt() ?: 0
                                val confidence = stats?.accuracy?.toInt() ?: 0
                                StudentResult(
                                    studentId = personalAssignment.student.id.toString(),
                                    name = personalAssignment.student.displayName,
                                    score = score,
                                    confidenceScore = confidence,
                                    status = when {
                                        personalAssignment.status == PersonalAssignmentStatus.SUBMITTED -> "완료"
                                        !personalAssignment.startedAt.isNullOrEmpty() -> "진행 중"
                                        else -> "미시작"
                                    },
                                    startedAt = personalAssignment.startedAt,
                                    submittedAt = personalAssignment.submittedAt ?: personalAssignment.startedAt ?: "",
                                    answers = emptyList(),
                                    detailedAnswers = emptyList()
                                )
                            }
                            _assignmentResults.value = results
                            
                            val submittedAssignments = assignmentStatsList.filter { (personalAssignment, stats) ->
                                val assignmentTotalQuestions = personalAssignment.assignment.totalQuestions
                                val solvedNum = personalAssignment.solvedNum
                                val hasStarted = !personalAssignment.startedAt.isNullOrEmpty()
                                
                                val isCompleted = when {
                                    personalAssignment.status == PersonalAssignmentStatus.SUBMITTED -> {
                                        println("AssignmentViewModel - PA ${personalAssignment.id} completed by status: SUBMITTED")
                                        true
                                    }
                                    !personalAssignment.submittedAt.isNullOrEmpty() -> {
                                        println("AssignmentViewModel - PA ${personalAssignment.id} completed by submitted_at: ${personalAssignment.submittedAt}")
                                        true
                                    }
                                    hasStarted && assignmentTotalQuestions > 0 && solvedNum >= assignmentTotalQuestions -> {
                                        println("AssignmentViewModel - PA ${personalAssignment.id} completed by solved_num: started=${hasStarted}, solved=${solvedNum}, total=${assignmentTotalQuestions}")
                                        true
                                    }
                                    hasStarted && stats != null && stats.totalProblem > 0 && stats.totalProblem == stats.solvedProblem -> {
                                        println("AssignmentViewModel - PA ${personalAssignment.id} completed by statistics: started=${hasStarted}, total=${stats.totalProblem}, solved=${stats.solvedProblem}")
                                        true
                                    }
                                    hasStarted && stats != null && stats.totalQuestions > 0 && stats.answeredQuestions >= stats.totalQuestions -> {
                                        println("AssignmentViewModel - PA ${personalAssignment.id} completed by answeredQuestions: started=${hasStarted}, answered=${stats.answeredQuestions}, total=${stats.totalQuestions}")
                                        true
                                    }
                                    else -> {
                                        println("AssignmentViewModel - PA ${personalAssignment.id} NOT completed: status=${personalAssignment.status}, submittedAt=${personalAssignment.submittedAt}, startedAt=${personalAssignment.startedAt}, solvedNum=${solvedNum}, totalQuestions=${assignmentTotalQuestions}, statsTotal=${stats?.totalProblem}, statsSolved=${stats?.solvedProblem}, answeredQuestions=${stats?.answeredQuestions}, totalQuestions=${stats?.totalQuestions}")
                                        false
                                    }
                                }
                                isCompleted
                            }
                            
                            val submittedCount = submittedAssignments.size
                            println("AssignmentViewModel - Submitted count: $submittedCount, Total students: $actualTotalStudents")
                            
                            if (submittedCount > 0) {
                                val statisticsList = submittedAssignments.mapNotNull { (_, stats) -> stats }
                                println("AssignmentViewModel - Loaded ${statisticsList.size} statistics")
                                
                                val averageScore = if (statisticsList.isNotEmpty()) {
                                    val avg = statisticsList.map { it.accuracy }.average().toInt()
                                    println("AssignmentViewModel - Average score: $avg")
                                    avg
                                } else {
                                    0
                                }
                                
                                val completionRate = if (actualTotalStudents > 0) {
                                    val rate = (submittedCount * 100) / actualTotalStudents
                                    println("AssignmentViewModel - Completion rate: $rate%")
                                    rate
                                } else {
                                    0
                                }
                                
                                val stats = AssignmentStatistics(
                                    submittedStudents = submittedCount,
                                    totalStudents = actualTotalStudents,
                                    averageScore = averageScore,
                                    completionRate = completionRate
                                )
                                
                                println("AssignmentViewModel - Setting statistics: $stats")
                                _assignmentStatistics.value = stats
                            } else {
                                println("AssignmentViewModel - No submitted assignments found")
                                val stats = AssignmentStatistics(
                                    submittedStudents = 0,
                                    totalStudents = actualTotalStudents,
                                    averageScore = 0,
                                    completionRate = 0
                                )
                                println("AssignmentViewModel - Setting statistics: $stats")
                                _assignmentStatistics.value = stats
                            }
                        }
                    }
                    .onFailure { exception ->
                        println("AssignmentViewModel - Failed to load personal assignments: ${exception.message}")
                        exception.printStackTrace()
                        _error.value = ErrorMessageMapper.getErrorMessage(exception)
                        _assignmentResults.value = emptyList()
                        _assignmentStatistics.value = AssignmentStatistics(
                            submittedStudents = 0,
                            totalStudents = totalStudents,
                            averageScore = 0,
                            completionRate = 0
                        )
                    }
            } catch (e: Exception) {
                println("AssignmentViewModel - Exception loading assignment statistics and results: ${e.message}")
                e.printStackTrace()
                _error.value = ErrorMessageMapper.getErrorMessage(e)
                _assignmentResults.value = emptyList()
                _assignmentStatistics.value = AssignmentStatistics(
                    submittedStudents = 0,
                    totalStudents = totalStudents,
                    averageScore = 0,
                    completionRate = 0
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAssignmentStudentResults(assignmentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                assignmentRepository.getPersonalAssignments(assignmentId = assignmentId)
                    .onSuccess { personalAssignments ->
                        viewModelScope.launch {
                            val results = coroutineScope {
                                personalAssignments.map { personalAssignment ->
                                    async {
                                        val stats = assignmentRepository.getPersonalAssignmentStatistics(personalAssignment.id).getOrNull()
                                        val score = stats?.averageScore?.toInt() ?: 0
                                        val confidence = stats?.accuracy?.toInt() ?: 0
                                        StudentResult(
                                            studentId = personalAssignment.student.id.toString(),
                                            name = personalAssignment.student.displayName,
                                            score = score,
                                            confidenceScore = confidence,
                                            status = when {
                                                personalAssignment.status == PersonalAssignmentStatus.SUBMITTED -> "완료"
                                                !personalAssignment.startedAt.isNullOrEmpty() -> "진행 중"
                                                else -> "미시작"
                                            },
                                            startedAt = personalAssignment.startedAt,
                                            submittedAt = personalAssignment.submittedAt ?: personalAssignment.startedAt ?: "",
                                            answers = emptyList(),
                                            detailedAnswers = emptyList()
                                        )
                                    }
                                }.awaitAll()
                            }
                            _assignmentResults.value = results
                        }
                    }
                    .onFailure { e ->
                        _error.value = ErrorMessageMapper.getErrorMessage(e)
                        _assignmentResults.value = emptyList()
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun createAssignment(assignment: CreateAssignmentRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            assignmentRepository.createAssignment(assignment)
                .onSuccess { createResponse ->
                    _currentAssignment.value = AssignmentData(
                        id = createResponse.assignment_id,
                        title = assignment.title,
                        description = assignment.description,
                        totalQuestions = assignment.questions?.size ?: 0,
                        createdAt = "",
                        dueAt = assignment.due_at,
                        courseClass = CourseClass(
                            id = assignment.class_id,
                            name = "",
                            description = "",
                            subject = Subject(id = 0, name = assignment.subject),
                            teacherName = "",
                            
                            
                            studentCount = 0,
                            createdAt = ""
                        ),
                        grade = assignment.grade
                    )
                    
                    loadAllAssignments()
                }
                .onFailure { exception ->
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                }
            
            _isLoading.value = false
        }
    }
    
    fun loadStudentAssignmentsWithFilter(studentId: Int, filter: AssignmentFilter) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            println("AssignmentViewModel - Loading personal assignments with filter for student ID: $studentId, filter: $filter")
            assignmentRepository.getPersonalAssignments(studentId)
                .onSuccess { personalAssignments: List<PersonalAssignmentData> ->
                    println("AssignmentViewModel - Received ${personalAssignments.size} personal assignments")
                    
                    val filteredAssignments = when (filter) {
                        AssignmentFilter.ALL -> personalAssignments
                        AssignmentFilter.IN_PROGRESS -> personalAssignments.filter { 
                            it.status == PersonalAssignmentStatus.NOT_STARTED || 
                            it.status == PersonalAssignmentStatus.IN_PROGRESS 
                        }
                        AssignmentFilter.COMPLETED -> personalAssignments.filter { 
                            it.status == PersonalAssignmentStatus.SUBMITTED 
                        }
                        else -> personalAssignments
                    }
                    
                    val convertedAssignments: List<AssignmentData> = filteredAssignments.map { personalAssignment: PersonalAssignmentData ->
                        println("AssignmentViewModel - Converting Filtered PersonalAssignment: ID=${personalAssignment.id}, Assignment ID=${personalAssignment.assignment.id}, Title=${personalAssignment.assignment.title}")
                        AssignmentData(
                            id = personalAssignment.assignment.id,
                            title = personalAssignment.assignment.title,
                            description = personalAssignment.assignment.description,
                            totalQuestions = personalAssignment.assignment.totalQuestions,
                            createdAt = null,
                            dueAt = personalAssignment.assignment.dueAt,
                            courseClass = CourseClass(
                                id = 0,
                                name = "", // 빈 문자열로 변경
                                description = null,
                                subject = Subject(
                                    id = 0,
                                    name = "", // 빈 문자열로 변경
                                    code = null
                                ),
                                teacherName = "",
                                
                                
                                studentCount = 0,
                                createdAt = ""
                            ),
                            materials = null,
                            grade = personalAssignment.assignment.grade,
                            personalAssignmentStatus = personalAssignment.status,
                            solvedNum = personalAssignment.solvedNum,
                            personalAssignmentId = personalAssignment.id
                        )
                    }
                    
                    _assignments.value = convertedAssignments
                    println("AssignmentViewModel - Filtered to ${convertedAssignments.size} assignments")
                    
                    calculateStudentStatsFromPersonalAssignments(personalAssignments)
                }
                .onFailure { exception ->
                    println("AssignmentViewModel - Error: ${exception.message}")
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                }
            
            _isLoading.value = false
        }
    }
    
    fun loadStudentAssignmentsWithPersonalFilter(studentId: Int, filter: PersonalAssignmentFilter) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            println("AssignmentViewModel - Loading personal assignments with personal filter for student ID: $studentId, filter: $filter")
            assignmentRepository.getPersonalAssignments(studentId)
                .onSuccess { personalAssignments: List<PersonalAssignmentData> ->
                    println("AssignmentViewModel - Received ${personalAssignments.size} personal assignments")
                    
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
                    
                    val convertedAssignments: List<AssignmentData> = filteredAssignments.map { personalAssignment: PersonalAssignmentData ->
                        AssignmentData(
                            id = personalAssignment.assignment.id,
                            title = personalAssignment.assignment.title,
                            description = personalAssignment.assignment.description,
                            totalQuestions = personalAssignment.assignment.totalQuestions,
                            createdAt = null,
                            dueAt = personalAssignment.assignment.dueAt,
                            courseClass = CourseClass(
                                id = 0,
                                name = "",
                                description = null,
                                subject = Subject(
                                    id = 0,
                                    name = "",
                                    code = null
                                ),
                                teacherName = "",
                                
                                
                                studentCount = 0,
                                createdAt = ""
                            ),
                            materials = null,
                            grade = personalAssignment.assignment.grade,
                            personalAssignmentStatus = personalAssignment.status,
                            solvedNum = personalAssignment.solvedNum,
                            personalAssignmentId = personalAssignment.id
                        )
                    }
                    
                    _assignments.value = convertedAssignments
                    println("AssignmentViewModel - Personal filter applied, showing ${convertedAssignments.size} assignments")
                    
                    calculateStudentStatsFromPersonalAssignments(personalAssignments)
                }
                .onFailure { exception ->
                    println("AssignmentViewModel - Error: ${exception.message}")
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                }
            
            _isLoading.value = false
        }
    }
    
    fun loadPendingStudentAssignments(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            println("AssignmentViewModel - Loading pending assignments for student ID: $studentId")
            assignmentRepository.getPersonalAssignments(studentId)
                .onSuccess { personalAssignments: List<PersonalAssignmentData> ->
                    println("AssignmentViewModel - Received ${personalAssignments.size} personal assignments")
                    
                    val pendingAssignments = personalAssignments.filter { 
                        it.status == PersonalAssignmentStatus.NOT_STARTED || 
                        it.status == PersonalAssignmentStatus.IN_PROGRESS 
                    }
                    
                    println("AssignmentViewModel - Found ${pendingAssignments.size} pending assignments")
                    
                    val convertedAssignments: MutableList<AssignmentData> = mutableListOf()

                    pendingAssignments.forEach { personalAssignment ->
                        println("AssignmentViewModel - Converting Pending PersonalAssignment: ID=${personalAssignment.id}, Assignment ID=${personalAssignment.assignment.id}, Title=${personalAssignment.assignment.title}")

                        assignmentRepository.getAssignmentById(personalAssignment.assignment.id)
                            .onSuccess { fullAssignment ->
                                val assignmentData = AssignmentData(
                                    id = personalAssignment.assignment.id,
                                    title = personalAssignment.assignment.title,
                                    description = personalAssignment.assignment.description,
                                    totalQuestions = personalAssignment.assignment.totalQuestions,
                                    createdAt = fullAssignment.createdAt,
                                    dueAt = personalAssignment.assignment.dueAt,
                                    courseClass = fullAssignment.courseClass,
                                    materials = fullAssignment.materials,
                                    grade = personalAssignment.assignment.grade,
                                    personalAssignmentStatus = personalAssignment.status,
                                    solvedNum = personalAssignment.solvedNum,
                                    personalAssignmentId = personalAssignment.id
                                )
                                convertedAssignments.add(assignmentData)
                                println("AssignmentViewModel - Added assignment with courseClass: ${fullAssignment.courseClass.name}, subject: ${fullAssignment.courseClass.subject.name}")

                                if (convertedAssignments.size == pendingAssignments.size) {
                                    _assignments.value = convertedAssignments.toList()
                                    println("AssignmentViewModel - All pending assignments loaded with courseClass info, showing ${convertedAssignments.size} assignments")
                                }
                            }
                            .onFailure { e ->
                                println("AssignmentViewModel - Failed to load full assignment ${personalAssignment.assignment.id}: ${e.message}")
                                val assignmentData = AssignmentData(
                                    id = personalAssignment.assignment.id,
                                    title = personalAssignment.assignment.title,
                                    description = personalAssignment.assignment.description,
                                    totalQuestions = personalAssignment.assignment.totalQuestions,
                                    createdAt = null,
                                    dueAt = personalAssignment.assignment.dueAt,
                                    courseClass = CourseClass(
                                        id = 0, name = "", description = null,
                                        subject = Subject(id = 0, name = "", code = null),
                                        teacherName = "",  
                                        studentCount = 0, createdAt = ""
                                    ),
                                    materials = null,
                                    grade = personalAssignment.assignment.grade,
                                    personalAssignmentStatus = personalAssignment.status,
                                    solvedNum = personalAssignment.solvedNum,
                                    personalAssignmentId = personalAssignment.id
                                )
                                convertedAssignments.add(assignmentData)

                                if (convertedAssignments.size == pendingAssignments.size) {
                                    _assignments.value = convertedAssignments.toList()
                                }
                            }
                    }
                    
                    if (pendingAssignments.isEmpty()) {
                        _assignments.value = emptyList()
                        println("AssignmentViewModel - No pending assignments found")
                    }

                    calculateStudentStatsFromPersonalAssignments(personalAssignments)
                }
                .onFailure { exception ->
                    println("AssignmentViewModel - Error: ${exception.message}")
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                }
            
            _isLoading.value = false
        }
    }
    
    fun loadCompletedStudentAssignments(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            println("AssignmentViewModel - Loading completed assignments for student ID: $studentId")
            assignmentRepository.getPersonalAssignments(studentId)
                .onSuccess { personalAssignments: List<PersonalAssignmentData> ->
                    println("AssignmentViewModel - Received ${personalAssignments.size} personal assignments")
                    
                    val completedAssignments = personalAssignments.filter { 
                        it.status == PersonalAssignmentStatus.SUBMITTED
                    }
                    
                    println("AssignmentViewModel - Found ${completedAssignments.size} completed assignments")
                    
                    val convertedAssignments: MutableList<AssignmentData> = mutableListOf()

                    completedAssignments.forEach { personalAssignment ->
                        assignmentRepository.getAssignmentById(personalAssignment.assignment.id)
                            .onSuccess { fullAssignment ->
                                val assignmentData = AssignmentData(
                                    id = personalAssignment.assignment.id,
                                    title = personalAssignment.assignment.title,
                                    description = personalAssignment.assignment.description,
                                    totalQuestions = personalAssignment.assignment.totalQuestions,
                                    createdAt = fullAssignment.createdAt,
                                    dueAt = personalAssignment.assignment.dueAt,
                                    courseClass = fullAssignment.courseClass,
                                    materials = fullAssignment.materials,
                                    grade = personalAssignment.assignment.grade,
                                    personalAssignmentStatus = personalAssignment.status,
                                    solvedNum = personalAssignment.solvedNum,
                                    personalAssignmentId = personalAssignment.id,
                                    submittedAt = personalAssignment.submittedAt
                                )
                                convertedAssignments.add(assignmentData)

                                if (convertedAssignments.size == completedAssignments.size) {
                                    _assignments.value = convertedAssignments.toList()
                                    println("AssignmentViewModel - All completed assignments loaded with courseClass info")
                                }
                            }
                            .onFailure { e ->
                                println("AssignmentViewModel - Failed to load full assignment ${personalAssignment.assignment.id}: ${e.message}")
                                val assignmentData = AssignmentData(
                                    id = personalAssignment.assignment.id,
                                    title = personalAssignment.assignment.title,
                                    description = personalAssignment.assignment.description,
                                    totalQuestions = personalAssignment.assignment.totalQuestions,
                                    createdAt = null,
                                    dueAt = personalAssignment.assignment.dueAt,
                                    courseClass = CourseClass(
                                        id = 0, name = "", description = null,
                                        subject = Subject(id = 0, name = "", code = null),
                                        teacherName = "",  
                                        studentCount = 0, createdAt = ""
                                    ),
                                    materials = null,
                                    grade = personalAssignment.assignment.grade,
                                    personalAssignmentStatus = personalAssignment.status,
                                    solvedNum = personalAssignment.solvedNum,
                                    personalAssignmentId = personalAssignment.id,
                                    submittedAt = personalAssignment.submittedAt
                                )
                                convertedAssignments.add(assignmentData)

                                if (convertedAssignments.size == completedAssignments.size) {
                                    _assignments.value = convertedAssignments.toList()
                                }
                            }
                    }
                    
                    if (completedAssignments.isEmpty()) {
                        _assignments.value = emptyList()
                        println("AssignmentViewModel - No completed assignments found")
                    }

                    calculateStudentStatsFromPersonalAssignments(personalAssignments)
                }
                .onFailure { exception ->
                    println("AssignmentViewModel - Error: ${exception.message}")
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                }
            
            _isLoading.value = false
        }
    }
    
    fun createAssignmentWithPdf(assignment: CreateAssignmentRequest, pdfFile: File, totalNumber: Int = 5) {
        println("=== AssignmentViewModel.createAssignmentWithPdf 시작 ===")
        println("PDF 파일: ${pdfFile.name}")
        
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                _isCreatingAssignment.value = true
                _isUploading.value = true
                _uploadProgress.value = 0f
                _uploadSuccess.value = false
            }
            
            try {
                println("1단계: 과제 생성")
                val createResult = assignmentRepository.createAssignment(assignment)
                
                createResult.onSuccess { createResponse ->
                    println("✅ 과제 생성 성공: ${createResponse.assignment_id}")
                    
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        _uploadProgress.value = 0.3f
                        _currentAssignment.value = AssignmentData(
                            id = createResponse.assignment_id,
                            title = assignment.title,
                            description = assignment.description,
                            totalQuestions = totalNumber,
                            createdAt = "",
                            dueAt = assignment.due_at,
                            courseClass = CourseClass(
                                id = assignment.class_id,
                                name = "",
                                description = "",
                                subject = Subject(id = 0, name = assignment.subject),
                                teacherName = "",
                                
                                
                                studentCount = 0,
                                createdAt = ""
                            ),
                            grade = assignment.grade
                        )
                    }
                    
                    println("2단계: PDF 업로드")
                    val uploadResult = assignmentRepository.uploadPdfToS3(createResponse.upload_url, pdfFile)
                    
                    uploadResult.onSuccess {
                        println("✅ PDF 업로드 완료")
                        
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            _uploadProgress.value = 1f
                            _uploadSuccess.value = true
                            _isUploading.value = false
                            _isCreatingAssignment.value = false
                            println("✅ 모든 로딩 상태 해제 완료")
                        }
                        
                        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            try {
                                println("🔄 [별도 스레드] 과제 목록 새로고침")
                                loadAllAssignments(silent = true)
                                println("✅ [별도 스레드] 과제 목록 새로고침 완료")
                            } catch (e: Exception) {
                                println("❌ [별도 스레드] 과제 목록 새로고침 실패: ${e.message}")
                            }
                        }
                        
                        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                _isGeneratingQuestions.value = true
                                _generatingAssignmentTitle.value = assignment.title
                                generatingAssignmentId = createResponse.assignment_id
                            }
                            
                            try {
                                println("🔄 [별도 스레드] 문제 생성 시작")
                                val result = assignmentRepository.createQuestionsAfterUpload(
                                    assignmentId = createResponse.assignment_id,
                                    materialId = createResponse.material_id,
                                    totalNumber = totalNumber
                                )
                                
                                result.onSuccess {
                                    println("✅ [별도 스레드] 문제 생성 완료")
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        _questionGenerationSuccess.value = true
                                        _isGeneratingQuestions.value = false
                                        _generatingAssignmentTitle.value = null
                                        generatingAssignmentId = null
                                    }
                                }.onFailure { e ->
                                    println("❌ [별도 스레드] 문제 생성 실패: ${e.message}")
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        _questionGenerationError.value = e.message
                                        _isGeneratingQuestions.value = false
                                        _generatingAssignmentTitle.value = null
                                        generatingAssignmentId = null
                                    }
                                }
                            } catch (e: Exception) {
                                println("❌ [별도 스레드] 문제 생성 예외: ${e.message}")
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    _questionGenerationError.value = e.message
                                    _isGeneratingQuestions.value = false
                                    _generatingAssignmentTitle.value = null
                                    generatingAssignmentId = null
                                }
                            }
                        }
                    }.onFailure { e ->
                        println("❌ PDF 업로드 실패: ${e.message}")
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            _error.value = "PDF 업로드 실패: ${ErrorMessageMapper.getErrorMessage(e)}"
                            _isUploading.value = false
                            _isCreatingAssignment.value = false
                        }
                    }
                }.onFailure { e ->
                    println("❌ 과제 생성 실패: ${e.message}")
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        _error.value = "과제 생성 실패: ${ErrorMessageMapper.getErrorMessage(e)}"
                        _isUploading.value = false
                        _isCreatingAssignment.value = false
                    }
                }
            } catch (e: Exception) {
                println("❌ 예외 발생: ${e.message}")
                e.printStackTrace()
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    _error.value = ErrorMessageMapper.getErrorMessage(e)
                    _isUploading.value = false
                    _isCreatingAssignment.value = false
                }
            }
        }
    }
    
    fun updateAssignment(id: Int, assignment: UpdateAssignmentRequest) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            assignmentRepository.updateAssignment(id, assignment)
                .onSuccess { updatedAssignment ->
                    _currentAssignment.value = updatedAssignment
                    _assignments.value = _assignments.value.map { 
                        if (it.id == id) updatedAssignment else it 
                    }
                    loadAssignmentResult(id, updatedAssignment.courseClass.studentCount)
                }
                .onFailure { exception ->
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
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
                    _assignments.value = _assignments.value.filter { it.id != id }
                }
                .onFailure { exception ->
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
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
                }
                .onFailure { exception ->
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                }
            
            _isLoading.value = false
        }
    }
    
    fun loadRecentAssignment(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            
            assignmentRepository.getRecentPersonalAssignment(studentId)
                .onSuccess { personalAssignmentId ->
                    val personalAssignmentsResult = assignmentRepository.getPersonalAssignments(studentId)

                    personalAssignmentsResult.onSuccess { personalAssignments ->
                        val personalAssignment = personalAssignments.find { it.id == personalAssignmentId }

                        if (personalAssignment != null) {
                            val recent = RecentAssignment(
                                id = personalAssignment.id.toString(),
                                title = personalAssignment.assignment.title,
                                assignmentId = personalAssignment.assignment.id
                            )
                            _recentAssignment.value = recent
                        } else {
                            _recentAssignment.value = null
                        }
                    }.onFailure {
                        _recentAssignment.value = null
                    }
                }
                .onFailure {
                    _recentAssignment.value = null
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
    
    fun resetUploadState() {
        _uploadProgress.value = 0f.coerceIn(0f, 1f)
        println("DEBUG: uploadProgress reset to 0f")
        _isUploading.value = false
        _uploadSuccess.value = false
        _isGeneratingQuestions.value = false
        _questionGenerationSuccess.value = false
        _questionGenerationError.value = null
        _generatingAssignmentTitle.value = null
        generatingAssignmentId = null // 초기화
    }
    
    fun clearQuestionGenerationStatus() {
        _questionGenerationSuccess.value = false
        _questionGenerationError.value = null
    }
    
    fun cancelQuestionGeneration() {
        println("AssignmentViewModel - Cancelling question generation")
        val assignmentId = generatingAssignmentId
        
        _isGeneratingQuestions.value = false
        _generatingAssignmentTitle.value = null
        _questionGenerationSuccess.value = false
        _questionGenerationError.value = null
        
        if (assignmentId != null) {
            println("AssignmentViewModel - Updating assignment $assignmentId: totalQuestions = 0")
            viewModelScope.launch {
                try {
                    val updateRequest = com.example.voicetutor.data.network.UpdateAssignmentRequest(
                        title = null,
                        description = null,
                        totalQuestions = 0,
                        dueAt = null,
                        grade = null,
                        subject = null
                    )
                    assignmentRepository.updateAssignment(assignmentId, updateRequest)
                        .onSuccess {
                                    println("✅ Assignment $assignmentId: totalQuestions가 0으로 업데이트됨")
                                    generatingAssignmentId = null
                        }
                        .onFailure { e ->
                            println("❌ Assignment $assignmentId 업데이트 실패: ${e.message}")
                            generatingAssignmentId = null
                        }
                } catch (e: Exception) {
                    println("❌ Assignment $assignmentId 업데이트 예외: ${e.message}")
                    generatingAssignmentId = null
                }
            }
        } else {
            println("AssignmentViewModel - No assignment ID to cancel")
            generatingAssignmentId = null
        }
    }
    
    fun checkS3UploadStatus(assignmentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            assignmentRepository.checkS3Upload(assignmentId)
                .onSuccess { status ->
                    _s3UploadStatus.value = status
                    println("=== S3 업로드 상태 확인 ===")
                    println("과제 ID: ${status.assignment_id}")
                    println("자료 ID: ${status.material_id}")
                    println("S3 키: ${status.s3_key}")
                    println("파일 존재: ${status.file_exists}")
                    if (status.file_exists) {
                        println("파일 크기: ${status.file_size} bytes")
                        println("Content-Type: ${status.content_type}")
                        println("마지막 수정: ${status.last_modified}")
                    }
                    println("버킷: ${status.bucket}")
                }
                .onFailure { exception ->
                    _error.value = "S3 확인 실패: ${ErrorMessageMapper.getErrorMessage(exception)}"
                }
            
            _isLoading.value = false
        }
    }
    
    private var lastLoadedPersonalAssignmentId: Int? = null
    
    fun loadPersonalAssignmentQuestions(personalAssignmentId: Int) {
        viewModelScope.launch {
            if (lastLoadedPersonalAssignmentId == personalAssignmentId && _personalAssignmentQuestions.value.isNotEmpty()) {
                println("AssignmentViewModel - Questions already loaded for personal assignment $personalAssignmentId")
                return@launch
            }
            
            if (_isLoading.value) {
                println("AssignmentViewModel - Already loading questions")
                return@launch
            }
            
            println("AssignmentViewModel - Loading questions for personal assignment $personalAssignmentId")
            _isLoading.value = true
            _error.value = null
            
            assignmentRepository.getPersonalAssignmentQuestions(personalAssignmentId)
                .onSuccess { questions ->
                    _personalAssignmentQuestions.value = questions
                    _currentQuestionIndex.value = 0
                    lastLoadedPersonalAssignmentId = personalAssignmentId
                    println("AssignmentViewModel - Successfully loaded ${questions.size} questions")
                }
                .onFailure { exception ->
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                    println("AssignmentViewModel - Failed to load questions: ${exception.message}")
                }
            
            _isLoading.value = false
        }
    }
    
    fun loadAllQuestions(personalAssignmentId: Int) {
        viewModelScope.launch {
            println("AssignmentViewModel - loadAllQuestions CALLED for personalAssignmentId: $personalAssignmentId")
            
            if (_isLoading.value) {
                println("AssignmentViewModel - Already loading questions - RETURNING EARLY")
                return@launch
            }
            
            _isLoading.value = true
            _error.value = null
            
            try {
                println("AssignmentViewModel - Loading all base questions for progress calculation")
                assignmentRepository.getPersonalAssignmentQuestions(personalAssignmentId)
                    .onSuccess { baseQuestions ->
                        println("AssignmentViewModel - Loaded ${baseQuestions.size} base questions")
                        _totalBaseQuestions.value = baseQuestions.size
                        
                        println("AssignmentViewModel - Finding next question to solve")
                        assignmentRepository.getNextQuestion(personalAssignmentId)
                            .onSuccess { nextQuestion ->
                                println("AssignmentViewModel - Found next question: ${nextQuestion.number}")
                                
                                _personalAssignmentQuestions.value = listOf(nextQuestion)
                                _currentQuestionIndex.value = 0
                                println("AssignmentViewModel - Set current question to next question")
                            }
                            .onFailure { exception ->
                                println("AssignmentViewModel - No next question found: ${exception.message}")
                                
                                val message = exception.message ?: ""
                                if (message.contains("모든 문제를 완료했습니다") || 
                                    message.contains("No more questions")) {
                                    println("AssignmentViewModel - All questions completed! Message: $message")
                                    
                                    assignmentRepository.getPersonalAssignmentStatistics(personalAssignmentId)
                                        .onSuccess { stats ->
                                            println("AssignmentViewModel - Statistics: totalProblem=${stats.totalProblem}, solvedProblem=${stats.solvedProblem}")
                                            
                                            if (stats.totalProblem == stats.solvedProblem) {
                                                println("AssignmentViewModel - All problems solved, completing assignment")
                                                _personalAssignmentQuestions.value = emptyList()
                                                _currentQuestionIndex.value = 0
                                                _error.value = null
                                                _personalAssignmentStatistics.value = stats
                                                
                                                completeAssignment(personalAssignmentId)
                                            } else {
                                                println("AssignmentViewModel - Not all problems solved yet (totalProblem=${stats.totalProblem}, solvedProblem=${stats.solvedProblem})")
                                                _personalAssignmentStatistics.value = stats
                                                _error.value = "아직 모든 문제를 완료하지 못했습니다. (${stats.solvedProblem}/${stats.totalProblem})"
                                            }
                                        }
                                        .onFailure { statsException ->
                                            println("AssignmentViewModel - Failed to load statistics: ${statsException.message}")
                                            _error.value = "통계를 확인할 수 없습니다: ${ErrorMessageMapper.getErrorMessage(statsException)}"
                                        }
                                } else {
                                    println("AssignmentViewModel - Other error: $message")
                                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                                }
                            }
                    }
                    .onFailure { exception ->
                        println("AssignmentViewModel - Failed to load base questions: ${exception.message}")
                        _error.value = ErrorMessageMapper.getErrorMessage(exception)
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun loadNextQuestion(personalAssignmentId: Int) {
        viewModelScope.launch {
            println("AssignmentViewModel - loadNextQuestion CALLED for personalAssignmentId: $personalAssignmentId")
            println("AssignmentViewModel - Current isLoading state: ${_isLoading.value}")
            
            if (_isLoading.value) {
                println("AssignmentViewModel - Already loading next question - RETURNING EARLY")
                return@launch
            }
            
            println("AssignmentViewModel - Setting isLoading to true")
            _isLoading.value = true
            _error.value = null
            
            try {
                println("AssignmentViewModel - Calling assignmentRepository.getNextQuestion")
                assignmentRepository.getNextQuestion(personalAssignmentId)
                    .onSuccess { question ->
                        println("AssignmentViewModel - SUCCESS: Received question: ${question.question}")

                        _isProcessing.value = question.isProcessing
                        if (question.isProcessing) {
                            println("AssignmentViewModel - Question is still processing")
                        } else {
                            _personalAssignmentQuestions.value = listOf(question)
                            _currentQuestionIndex.value = 0
                            println("AssignmentViewModel - Successfully loaded next question: ${question.question}")
                        }
                    }
                    .onFailure { exception ->
                        println("AssignmentViewModel - FAILURE: ${exception.message}")
                        
                        if (exception.message?.contains("No more questions") == true || 
                            exception.message?.contains("모든 문제를 완료했습니다") == true) {
                            println("AssignmentViewModel - All questions completed - checking statistics")
                            
                            assignmentRepository.getPersonalAssignmentStatistics(personalAssignmentId)
                                .onSuccess { stats ->
                                    println("AssignmentViewModel - Statistics: totalProblem=${stats.totalProblem}, solvedProblem=${stats.solvedProblem}")
                                    
                                    if (stats.totalProblem == stats.solvedProblem) {
                                        println("AssignmentViewModel - All problems solved - setting empty list")
                                        _personalAssignmentQuestions.value = emptyList()
                                        _error.value = null
                                        _personalAssignmentStatistics.value = stats
                                    } else {
                                        println("AssignmentViewModel - Not all problems solved yet (totalProblem=${stats.totalProblem}, solvedProblem=${stats.solvedProblem})")
                                        _personalAssignmentStatistics.value = stats
                                        _error.value = "아직 모든 문제를 완료하지 못했습니다. (${stats.solvedProblem}/${stats.totalProblem})"
                                    }
                                }
                                .onFailure { statsException ->
                                    println("AssignmentViewModel - Failed to load statistics: ${statsException.message}")
                                    _error.value = "통계를 확인할 수 없습니다: ${statsException.message}"
                                }
                        } else {
                            _error.value = ErrorMessageMapper.getErrorMessage(exception)
                            println("AssignmentViewModel - Failed to load next question: ${exception.message}")
                        }
                    }
            } finally {
                println("AssignmentViewModel - Setting isLoading to false")
                _isLoading.value = false
                println("AssignmentViewModel - loadNextQuestion COMPLETED")
            }
        }
    }
    fun refreshProcessingStatus(personalAssignmentId: Int) {
        viewModelScope.launch {
            println("AssignmentViewModel - refreshProcessingStatus CALLED for personalAssignmentId: $personalAssignmentId")
            assignmentRepository.getNextQuestion(personalAssignmentId)
                .onSuccess { question ->
                    _isProcessing.value = question.isProcessing
                    println("AssignmentViewModel - refreshProcessingStatus: isProcessing=${question.isProcessing}")

                    if (!question.isProcessing) {
                        _personalAssignmentQuestions.value = listOf(question)
                        _currentQuestionIndex.value = 0
                    }
                }
                .onFailure { e ->
                    val msg = e.message ?: ""
                    println("AssignmentViewModel - Failed to refresh processing status: ${e.message}")
                    if (msg.contains("모든 문제를 완료했습니다")) {
                        _isProcessing.value = false
                        _isAssignmentCompleted.value = true
                    } else {
                        _isProcessing.value = false
                        _error.value = msg
                    }
                }
        }
    }
    fun loadPersonalAssignmentStatistics(personalAssignmentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            println("AssignmentViewModel - Loading statistics for PersonalAssignment ID: $personalAssignmentId")
            assignmentRepository.getPersonalAssignmentStatistics(personalAssignmentId)
                .onSuccess { statistics ->
                    println("AssignmentViewModel - Statistics loaded successfully:")
                    println("  - progress: ${statistics.progress}")
                    println("  - totalProblem: ${statistics.totalProblem}")
                    println("  - solvedProblem: ${statistics.solvedProblem}")
                    println("  - totalQuestions: ${statistics.totalQuestions}")
                    println("  - answeredQuestions: ${statistics.answeredQuestions}")
                    println("  - correctAnswers: ${statistics.correctAnswers}")
                    println("  - accuracy: ${statistics.accuracy}")
                    _personalAssignmentStatistics.value = statistics
                }
                .onFailure { exception ->
                    println("AssignmentViewModel - Failed to load statistics: ${exception.message}")
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                }
            
            _isLoading.value = false
        }
    }

    fun loadPersonalAssignmentStatisticsFor(studentId: Int, assignmentId: Int, silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _isLoading.value = true
            }
            _error.value = null
            try {
                assignmentRepository.getPersonalAssignments(studentId = studentId, assignmentId = assignmentId)
                    .onSuccess { list ->
                        val pa = list.firstOrNull()
                        if (pa == null) {
                            if (!silent) {
                                _error.value = "Personal assignment not found for student $studentId and assignment $assignmentId"
                            }
                        } else {
                            assignmentRepository.getPersonalAssignmentStatistics(pa.id)
                                .onSuccess { statistics ->
                                    _personalAssignmentStatistics.value = statistics
                                }
                                .onFailure { e -> 
                                    if (!silent) {
                                        _error.value = e.message
                                    }
                                }
                        }
                    }
                    .onFailure { e -> 
                        if (!silent) {
                            _error.value = e.message
                        }
                    }
            } finally {
                if (!silent) {
                    _isLoading.value = false
                }
            }
        }
    }
    
    fun submitAnswer(personalAssignmentId: Int, studentId: Int, questionId: Int, audioFile: File) {
        viewModelScope.launch {
            _isSubmitting.value = true
            _error.value = null
            
            assignmentRepository.submitAnswer(personalAssignmentId, studentId, questionId, audioFile)
                .onSuccess { response ->
                    _answerSubmissionResponse.value = response
                    
                    println("AssignmentViewModel - Answer submitted successfully")
                    println("AssignmentViewModel - isCorrect: ${response.isCorrect}, numberStr: ${response.numberStr}")
                    
                    assignmentRepository.getPersonalAssignmentStatistics(personalAssignmentId)
                        .onSuccess { stats ->
                            println("AssignmentViewModel - Statistics updated after answer submission:")
                            println("  - solvedProblem: ${stats.solvedProblem}")
                            println("  - totalProblem: ${stats.totalProblem}")
                            _personalAssignmentStatistics.value = stats
                        }
                        .onFailure { statsException ->
                            println("AssignmentViewModel - Failed to reload statistics: ${statsException.message}")
                        }
                }
                .onFailure { exception ->
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                    println("AssignmentViewModel - Failed to submit answer: ${exception.message}")
                }
            
            _isSubmitting.value = false
        }
    }
    
    fun startRecording() {
        _audioRecordingState.value = _audioRecordingState.value.copy(
            isRecording = true,
            recordingTime = 0,
            audioFilePath = null
        )
    }
    
    fun stopRecording(audioFilePath: String) {
        _audioRecordingState.value = _audioRecordingState.value.copy(
            isRecording = false,
            audioFilePath = audioFilePath
        )
    }
    
    fun stopRecordingImmediately() {
        println("AssignmentViewModel - Stopping recording immediately")
        _audioRecordingState.value = _audioRecordingState.value.copy(
            isRecording = false
        )
    }
    
    fun stopRecordingWithFilePath(audioFilePath: String) {
        println("AssignmentViewModel - Stopping recording with file path: $audioFilePath")
        _audioRecordingState.value = _audioRecordingState.value.copy(
            isRecording = false,
            audioFilePath = audioFilePath
        )
    }
    
    fun updateRecordingDuration(duration: Int) {
        _audioRecordingState.value = _audioRecordingState.value.copy(
            recordingTime = duration
        )
    }
    
    fun nextQuestion() {
        val currentIndex = _currentQuestionIndex.value
        val totalQuestions = _personalAssignmentQuestions.value.size
        
        if (currentIndex < totalQuestions - 1) {
            _currentQuestionIndex.value = currentIndex + 1
        }
    }
    
    fun moveToQuestionByNumber(questionNumber: String, personalAssignmentId: Int) {
        println("AssignmentViewModel - moveToQuestionByNumber called with: $questionNumber")
        
        if (questionNumber.contains("-")) {
            println("AssignmentViewModel - This is a tail question: $questionNumber")
            return
        }
        
        val targetNumber = questionNumber.toIntOrNull() ?: return
        println("AssignmentViewModel - Target question number: $targetNumber")
        
        val questions = _personalAssignmentQuestions.value
        val targetIndex = questions.indexOfFirst { it.number == questionNumber }
        
        if (targetIndex != -1) {
            println("AssignmentViewModel - Found question at index: $targetIndex")
            _currentQuestionIndex.value = targetIndex
        } else {
            println("AssignmentViewModel - Question $questionNumber not found in current list, loading from server")
            loadNextQuestion(personalAssignmentId)
        }
    }
    
    fun previousQuestion() {
        val currentIndex = _currentQuestionIndex.value
        
        if (currentIndex > 0) {
            _currentQuestionIndex.value = currentIndex - 1
        }
    }
    
    fun getCurrentQuestion(): PersonalAssignmentQuestion? {
        val currentIndex = _currentQuestionIndex.value
        val questions = _personalAssignmentQuestions.value
        
        return if (currentIndex < questions.size) {
            questions[currentIndex]
        } else {
            null
        }
    }
    
    fun resetAudioRecording() {
        _audioRecordingState.value = RecordingState()
    }
    
    fun setAudioFilePath(filePath: String) {
        _audioRecordingState.value = _audioRecordingState.value.copy(
            audioFilePath = filePath
        )
    }

    fun setRecordingComplete(isComplete: Boolean) {
        _audioRecordingState.value = _audioRecordingState.value.copy(
            isRecordingComplete = isComplete
        )
    }

    fun clearAnswerSubmissionResponse() {
        _answerSubmissionResponse.value = null
    }
    
    fun setAssignmentCompleted(completed: Boolean) {
        _isAssignmentCompleted.value = completed
    }
    
    fun updatePersonalAssignmentQuestions(questions: List<PersonalAssignmentQuestion>) {
        _personalAssignmentQuestions.value = questions
    }
    
    fun completeAssignment(personalAssignmentId: Int) {
        viewModelScope.launch {
            println("AssignmentViewModel - Completing assignment: $personalAssignmentId")
            try {
                var assignmentId: Int? = null
                assignmentRepository.getPersonalAssignments(assignmentId = null)
                    .onSuccess { allPersonalAssignments ->
                        val personalAssignment = allPersonalAssignments.find { it.id == personalAssignmentId }
                        assignmentId = personalAssignment?.assignment?.id
                    }
                
                assignmentRepository.completePersonalAssignment(personalAssignmentId)
                    .onSuccess {
                        println("AssignmentViewModel - Assignment completed successfully")
                        
                        _isAssignmentCompleted.value = true
                        _personalAssignmentQuestions.value = emptyList()
                        _currentQuestionIndex.value = 0
                        
                        assignmentId?.let { id ->
                            val assignment = _currentAssignment.value
                            if (assignment?.id == id) {
                                loadAssignmentStatistics(id, assignment.courseClass.studentCount)
                            }
                            _assignments.value = _assignments.value.map { a ->
                                if (a.id == id) {
                                    a
                                } else {
                                    a
                                }
                            }
                        }
                    }
                    .onFailure { exception ->
                        println("AssignmentViewModel - Error completing assignment: ${exception.message}")
                        _error.value = ErrorMessageMapper.getErrorMessage(exception)
                    }
            } catch (e: Exception) {
                println("AssignmentViewModel - Error completing assignment: ${e.message}")
                _error.value = e.message
            }
        }
    }
    
    suspend fun getAssignmentSubmissionStats(assignmentId: Int): AssignmentStatistics {
        return try {
            val personalAssignments = assignmentRepository.getPersonalAssignments(assignmentId = assignmentId).getOrNull()
            
            if (personalAssignments == null || personalAssignments.isEmpty()) {
                return AssignmentStatistics(0, 0, 0, 0)
            }
            
            val totalStudents = personalAssignments.size
            
            coroutineScope {
                val assignmentStatsDeferred = personalAssignments.map { personalAssignment ->
                    async {
                        val stats = assignmentRepository.getPersonalAssignmentStatistics(personalAssignment.id).getOrNull()
                        Pair(personalAssignment, stats)
                    }
                }
                
                val assignmentStatsList = assignmentStatsDeferred.awaitAll()
                
                val submittedAssignments = assignmentStatsList.filter { (personalAssignment, stats) ->
                    val assignmentTotalQuestions = personalAssignment.assignment.totalQuestions
                    val solvedNum = personalAssignment.solvedNum
                    val hasStarted = !personalAssignment.startedAt.isNullOrEmpty()
                    
                                val isCompleted = when {
                        personalAssignment.status == PersonalAssignmentStatus.SUBMITTED -> true
                        !personalAssignment.submittedAt.isNullOrEmpty() -> true
                        hasStarted && assignmentTotalQuestions > 0 && solvedNum >= assignmentTotalQuestions -> true
                        hasStarted && stats != null && stats.totalProblem > 0 && stats.totalProblem == stats.solvedProblem -> true
                        hasStarted && stats != null && stats.totalQuestions > 0 && stats.answeredQuestions >= stats.totalQuestions -> true
                        else -> false
                    }
                    isCompleted
                }
                
                val submittedCount = submittedAssignments.size
                val completionRate = if (totalStudents > 0) {
                    (submittedCount * 100) / totalStudents
                } else {
                    0
                }
                
                val statisticsList = submittedAssignments.mapNotNull { (_, stats) -> stats }
                val averageScore = if (statisticsList.isNotEmpty()) {
                    statisticsList.map { it.accuracy }.average().toInt()
                } else {
                    0
                }
                
                AssignmentStatistics(
                    submittedStudents = submittedCount,
                    totalStudents = totalStudents,
                    averageScore = averageScore,
                    completionRate = completionRate
                )
            }
        } catch (e: Exception) {
            println("AssignmentViewModel - Error getting assignment stats: ${e.message}")
            e.printStackTrace()
            AssignmentStatistics(0, 0, 0, 0)
        }
    }

    fun loadAssignmentCorrectness(personalAssignmentId: Int) {
        viewModelScope.launch {
            println("AssignmentViewModel - Loading correctness for personal assignment $personalAssignmentId")
            _isLoading.value = true
            _error.value = null

            assignmentRepository.getAssignmentCorrectness(personalAssignmentId)
                .onSuccess { correctnessData ->
                    _assignmentCorrectness.value = correctnessData
                    println("AssignmentViewModel - Successfully loaded ${correctnessData.size} correctness items")
                }
                .onFailure { exception ->
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                    println("AssignmentViewModel - Failed to load correctness: ${exception.message}")
                }

            _isLoading.value = false
        }
    }

    fun loadAssignmentCorrectnessFor(studentId: Int, assignmentId: Int, silent: Boolean = false) {
        viewModelScope.launch {
            if (!silent) {
                _isLoading.value = true
            }
            _error.value = null
            try {
                assignmentRepository.getPersonalAssignments(studentId = studentId, assignmentId = assignmentId)
                    .onSuccess { list ->
                        val pa = list.firstOrNull()
                        if (pa == null) {
                            if (!silent) {
                                _error.value = "Personal assignment not found for student $studentId and assignment $assignmentId"
                            }
                        } else {
                            assignmentRepository.getAssignmentCorrectness(pa.id)
                                .onSuccess { correctnessData ->
                                    _assignmentCorrectness.value = correctnessData
                                }
                                .onFailure { e -> 
                                    if (!silent) {
                                        _error.value = e.message
                                    }
                                }
                        }
                    }
                    .onFailure { e -> 
                        if (!silent) {
                            _error.value = e.message
                        }
                    }
            } finally {
                if (!silent) {
                    _isLoading.value = false
                }
            }
        }
    }
}
