package com.example.voicetutor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicetutor.audio.RecordingState
import com.example.voicetutor.data.models.AnswerSubmissionResponse
import com.example.voicetutor.data.models.AssignmentData
import com.example.voicetutor.data.models.AssignmentFilter
import com.example.voicetutor.data.models.AssignmentStatus
import com.example.voicetutor.data.models.CourseClass
import com.example.voicetutor.data.models.PersonalAssignmentData
import com.example.voicetutor.data.models.PersonalAssignmentFilter
import com.example.voicetutor.data.models.PersonalAssignmentQuestion
import com.example.voicetutor.data.models.PersonalAssignmentStatistics
import com.example.voicetutor.data.models.PersonalAssignmentStatus
import com.example.voicetutor.data.models.QuestionGroupFactory
import com.example.voicetutor.data.models.StudentResult
import com.example.voicetutor.data.models.Subject
import com.example.voicetutor.data.network.CreateAssignmentRequest
import com.example.voicetutor.data.network.S3UploadStatus
import com.example.voicetutor.data.network.UpdateAssignmentRequest
import com.example.voicetutor.data.repository.AssignmentRepository
import com.example.voicetutor.ui.navigation.RecentAssignment
import com.example.voicetutor.ui.utils.ErrorMessageMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import kotlin.OptIn

data class StudentStats(
    val totalAssignments: Int,
    val completedAssignments: Int,
    val inProgressAssignments: Int,
    val completionRate: Float,
)

@HiltViewModel
class AssignmentViewModel @Inject constructor(
    private val assignmentRepository: AssignmentRepository,
) : ViewModel() {

    private val _assignments = MutableStateFlow<List<AssignmentData>>(emptyList())
    val assignments: StateFlow<List<AssignmentData>> = _assignments.asStateFlow()

    private val _currentAssignment = MutableStateFlow<AssignmentData?>(null)
    val currentAssignment: StateFlow<AssignmentData?> = _currentAssignment.asStateFlow()

    private val _recentAssignment = MutableStateFlow<RecentAssignment?>(null)
    val recentAssignment: StateFlow<RecentAssignment?> = _recentAssignment.asStateFlow()

    private val _assignmentResults = MutableStateFlow<List<StudentResult>>(emptyList())
    val assignmentResults: StateFlow<List<StudentResult>> = _assignmentResults.asStateFlow()

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

    private val _questionGenerationCancelled = MutableStateFlow(false)
    val questionGenerationCancelled: StateFlow<Boolean> = _questionGenerationCancelled.asStateFlow()

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
        val completionRate: Int,
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

            assignmentRepository.getAllAssignments(teacherId, classId, status)
                .onSuccess { assignments ->
                    _assignments.value = assignments
                }
                .onFailure { exception ->
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

            assignmentRepository.getAllAssignments()
                .onSuccess { allAssignments ->
                    _assignments.value = allAssignments
                    calculateStudentStats(allAssignments)
                }
                .onFailure { exception ->
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
            completionRate = completionRate,
        )

        _studentStats.value = stats
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
            completionRate = completionRate,
        )

        _studentStats.value = stats
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
            @Suppress("UNNECESSARY_SAFE_CALL")
            assignmentRepository.getAssignmentResult(assignmentId)
                .onSuccess { result ->
                    val submitted = result?.submittedStudents ?: 0
                    val total = result?.totalStudents ?: fallbackTotalStudents
                    val average = result?.averageScore?.toInt() ?: 0
                    val completionRate = result?.completionRate?.toInt() ?: if (total > 0) (submitted * 100) / total else 0
                    _assignmentStatistics.value = AssignmentStatistics(
                        submittedStudents = submitted,
                        totalStudents = total,
                        averageScore = average,
                        completionRate = completionRate,
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
                assignmentRepository.getPersonalAssignments(assignmentId = assignmentId)
                    .onSuccess { personalAssignments ->

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

                                val isCompleted = when {
                                    personalAssignment.status == PersonalAssignmentStatus.SUBMITTED -> true
                                    !personalAssignment.submittedAt.isNullOrEmpty() -> true
                                    !personalAssignment.startedAt.isNullOrEmpty() && assignmentTotalQuestions > 0 && solvedNum >= assignmentTotalQuestions -> true
                                    !personalAssignment.startedAt.isNullOrEmpty() && stats != null && stats.totalProblem > 0 && stats.totalProblem == stats.solvedProblem -> true
                                    !personalAssignment.startedAt.isNullOrEmpty() && stats != null && stats.totalQuestions > 0 && stats.answeredQuestions >= stats.totalQuestions -> true
                                    else -> false
                                }
                                isCompleted
                            }

                            val submittedCount = submittedAssignments.size

                            if (submittedCount > 0) {
                                val statisticsList = submittedAssignments.mapNotNull { (_, stats) -> stats }

                                val averageScore = if (statisticsList.isNotEmpty()) {
                                    statisticsList.map { it.accuracy }.average().toInt()
                                } else {
                                    0
                                }

                                val completionRate = if (actualTotalStudents > 0) {
                                    (submittedCount * 100) / actualTotalStudents
                                } else {
                                    0
                                }

                                val stats = AssignmentStatistics(
                                    submittedStudents = submittedCount,
                                    totalStudents = actualTotalStudents,
                                    averageScore = averageScore,
                                    completionRate = completionRate,
                                )

                                _assignmentStatistics.value = stats
                            } else {
                                val stats = AssignmentStatistics(
                                    submittedStudents = 0,
                                    totalStudents = actualTotalStudents,
                                    averageScore = 0,
                                    completionRate = 0,
                                )
                                _assignmentStatistics.value = stats
                            }
                        }
                    }
                    .onFailure { exception ->
                        _assignmentStatistics.value = AssignmentStatistics(
                            submittedStudents = 0,
                            totalStudents = totalStudents,
                            averageScore = 0,
                            completionRate = 0,
                        )
                    }
            } catch (e: Exception) {
                _assignmentStatistics.value = AssignmentStatistics(
                    submittedStudents = 0,
                    totalStudents = totalStudents,
                    averageScore = 0,
                    completionRate = 0,
                )
            }
        }
    }

    fun loadAssignmentStatisticsAndResults(assignmentId: Int, totalStudents: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                assignmentRepository.getPersonalAssignments(assignmentId = assignmentId)
                    .onSuccess { personalAssignments ->

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
                                    detailedAnswers = emptyList(),
                                )
                            }
                            _assignmentResults.value = results

                            val submittedAssignments = assignmentStatsList.filter { (personalAssignment, stats) ->
                                val assignmentTotalQuestions = personalAssignment.assignment.totalQuestions
                                val solvedNum = personalAssignment.solvedNum

                                val isCompleted = when {
                                    personalAssignment.status == PersonalAssignmentStatus.SUBMITTED -> true
                                    !personalAssignment.submittedAt.isNullOrEmpty() -> true
                                    !personalAssignment.startedAt.isNullOrEmpty() && assignmentTotalQuestions > 0 && solvedNum >= assignmentTotalQuestions -> true
                                    !personalAssignment.startedAt.isNullOrEmpty() && stats != null && stats.totalProblem > 0 && stats.totalProblem == stats.solvedProblem -> true
                                    !personalAssignment.startedAt.isNullOrEmpty() && stats != null && stats.totalQuestions > 0 && stats.answeredQuestions >= stats.totalQuestions -> true
                                    else -> false
                                }
                                isCompleted
                            }

                            val submittedCount = submittedAssignments.size

                            if (submittedCount > 0) {
                                val statisticsList = submittedAssignments.mapNotNull { (_, stats) -> stats }

                                val averageScore = if (statisticsList.isNotEmpty()) {
                                    statisticsList.map { it.accuracy }.average().toInt()
                                } else {
                                    0
                                }

                                val completionRate = if (actualTotalStudents > 0) {
                                    (submittedCount * 100) / actualTotalStudents
                                } else {
                                    0
                                }

                                val stats = AssignmentStatistics(
                                    submittedStudents = submittedCount,
                                    totalStudents = actualTotalStudents,
                                    averageScore = averageScore,
                                    completionRate = completionRate,
                                )

                                _assignmentStatistics.value = stats
                            } else {
                                val stats = AssignmentStatistics(
                                    submittedStudents = 0,
                                    totalStudents = actualTotalStudents,
                                    averageScore = 0,
                                    completionRate = 0,
                                )
                                _assignmentStatistics.value = stats
                            }
                        }
                    }
                    .onFailure { exception ->
                        _error.value = ErrorMessageMapper.getErrorMessage(exception)
                        _assignmentResults.value = emptyList()
                        _assignmentStatistics.value = AssignmentStatistics(
                            submittedStudents = 0,
                            totalStudents = totalStudents,
                            averageScore = 0,
                            completionRate = 0,
                        )
                    }
            } catch (e: Exception) {
                _error.value = ErrorMessageMapper.getErrorMessage(e)
                _assignmentResults.value = emptyList()
                _assignmentStatistics.value = AssignmentStatistics(
                    submittedStudents = 0,
                    totalStudents = totalStudents,
                    averageScore = 0,
                    completionRate = 0,
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
                                            detailedAnswers = emptyList(),
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

    fun createAssignment(assignment: CreateAssignmentRequest, teacherId: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            assignmentRepository.createAssignment(assignment)
                .onSuccess { createResponse ->
                    _currentAssignment.value = AssignmentData(
                        id = createResponse.assignment_id,
                        title = assignment.title,
                        description = assignment.description,
                        totalQuestions = assignment.total_questions ?: 0,
                        createdAt = "",
                        dueAt = assignment.due_at,
                        courseClass = CourseClass(
                            id = assignment.class_id,
                            name = "",
                            description = "",
                            subject = Subject(id = 0, name = assignment.subject),
                            teacherName = "",

                            studentCount = 0,
                            createdAt = "",
                        ),
                        grade = assignment.grade,
                    )

                    loadAllAssignments(teacherId = teacherId)
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

            assignmentRepository.getPersonalAssignments(studentId)
                .onSuccess { personalAssignments: List<PersonalAssignmentData> ->
                    val filteredAssignments = when (filter) {
                        AssignmentFilter.ALL -> personalAssignments
                        AssignmentFilter.IN_PROGRESS -> personalAssignments.filter {
                            it.status == PersonalAssignmentStatus.NOT_STARTED ||
                                it.status == PersonalAssignmentStatus.IN_PROGRESS
                        }
                        AssignmentFilter.COMPLETED -> personalAssignments.filter {
                            it.status == PersonalAssignmentStatus.SUBMITTED
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
                                    code = null,
                                ),
                                teacherName = "",

                                studentCount = 0,
                                createdAt = "",
                            ),
                            materials = null,
                            grade = personalAssignment.assignment.grade,
                            personalAssignmentStatus = personalAssignment.status,
                            solvedNum = personalAssignment.solvedNum,
                            personalAssignmentId = personalAssignment.id,
                        )
                    }

                    _assignments.value = convertedAssignments
                    calculateStudentStatsFromPersonalAssignments(personalAssignments)
                }
                .onFailure { exception ->
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                }

            _isLoading.value = false
        }
    }

    fun loadStudentAssignmentsWithPersonalFilter(studentId: Int, filter: PersonalAssignmentFilter) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            assignmentRepository.getPersonalAssignments(studentId)
                .onSuccess { personalAssignments: List<PersonalAssignmentData> ->
                    val filteredAssignments = when (filter) {
                        PersonalAssignmentFilter.ALL -> personalAssignments
                        PersonalAssignmentFilter.NOT_STARTED -> {
                            personalAssignments.filter {
                                it.status == PersonalAssignmentStatus.NOT_STARTED
                            }
                        }
                        PersonalAssignmentFilter.IN_PROGRESS -> {
                            personalAssignments.filter {
                                it.status == PersonalAssignmentStatus.IN_PROGRESS
                            }
                        }
                        PersonalAssignmentFilter.SUBMITTED -> {
                            personalAssignments.filter {
                                it.status == PersonalAssignmentStatus.SUBMITTED
                            }
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
                                    code = null,
                                ),
                                teacherName = "",

                                studentCount = 0,
                                createdAt = "",
                            ),
                            materials = null,
                            grade = personalAssignment.assignment.grade,
                            personalAssignmentStatus = personalAssignment.status,
                            solvedNum = personalAssignment.solvedNum,
                            personalAssignmentId = personalAssignment.id,
                        )
                    }

                    _assignments.value = convertedAssignments
                    calculateStudentStatsFromPersonalAssignments(personalAssignments)
                }
                .onFailure { exception ->
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                }

            _isLoading.value = false
        }
    }

    fun loadPendingStudentAssignments(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            assignmentRepository.getPersonalAssignments(studentId)
                .onSuccess { personalAssignments: List<PersonalAssignmentData> ->
                    val pendingAssignments = personalAssignments.filter {
                        it.status == PersonalAssignmentStatus.NOT_STARTED ||
                            it.status == PersonalAssignmentStatus.IN_PROGRESS
                    }

                    val convertedAssignments: MutableList<AssignmentData> = mutableListOf()

                    pendingAssignments.forEach { personalAssignment ->
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
                                )
                                convertedAssignments.add(assignmentData)

                                if (convertedAssignments.size == pendingAssignments.size) {
                                    _assignments.value = convertedAssignments.toList()
                                }
                            }
                            .onFailure { e ->
                                val assignmentData = AssignmentData(
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
                                        subject = Subject(id = 0, name = "", code = null),
                                        teacherName = "",
                                        studentCount = 0,
                                        createdAt = "",
                                    ),
                                    materials = null,
                                    grade = personalAssignment.assignment.grade,
                                    personalAssignmentStatus = personalAssignment.status,
                                    solvedNum = personalAssignment.solvedNum,
                                    personalAssignmentId = personalAssignment.id,
                                )
                                convertedAssignments.add(assignmentData)

                                if (convertedAssignments.size == pendingAssignments.size) {
                                    _assignments.value = convertedAssignments.toList()
                                }
                            }
                    }

                    if (pendingAssignments.isEmpty()) {
                        _assignments.value = emptyList()
                    }

                    calculateStudentStatsFromPersonalAssignments(personalAssignments)
                }
                .onFailure { exception ->
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                }

            _isLoading.value = false
        }
    }

    fun loadCompletedStudentAssignments(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            assignmentRepository.getPersonalAssignments(studentId)
                .onSuccess { personalAssignments: List<PersonalAssignmentData> ->
                    val completedAssignments = personalAssignments.filter {
                        it.status == PersonalAssignmentStatus.SUBMITTED
                    }

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
                                    submittedAt = personalAssignment.submittedAt,
                                )
                                convertedAssignments.add(assignmentData)

                                if (convertedAssignments.size == completedAssignments.size) {
                                    _assignments.value = convertedAssignments.toList()
                                }
                            }
                            .onFailure { e ->
                                val assignmentData = AssignmentData(
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
                                        subject = Subject(id = 0, name = "", code = null),
                                        teacherName = "",
                                        studentCount = 0,
                                        createdAt = "",
                                    ),
                                    materials = null,
                                    grade = personalAssignment.assignment.grade,
                                    personalAssignmentStatus = personalAssignment.status,
                                    solvedNum = personalAssignment.solvedNum,
                                    personalAssignmentId = personalAssignment.id,
                                    submittedAt = personalAssignment.submittedAt,
                                )
                                convertedAssignments.add(assignmentData)

                                if (convertedAssignments.size == completedAssignments.size) {
                                    _assignments.value = convertedAssignments.toList()
                                }
                            }
                    }

                    if (completedAssignments.isEmpty()) {
                        _assignments.value = emptyList()
                    }

                    calculateStudentStatsFromPersonalAssignments(personalAssignments)
                }
                .onFailure { exception ->
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                }

            _isLoading.value = false
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun createAssignmentWithPdf(assignment: CreateAssignmentRequest, pdfFile: File, totalNumber: Int = 5, teacherId: String? = null) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                _isCreatingAssignment.value = true
                _isUploading.value = true
                _uploadProgress.value = 0f
                _uploadSuccess.value = false
            }

            try {
                val createResult = assignmentRepository.createAssignment(assignment)

                createResult.onSuccess { createResponse ->
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
                                createdAt = "",
                            ),
                            grade = assignment.grade,
                        )
                    }

                    val uploadResult = assignmentRepository.uploadPdfToS3(createResponse.upload_url, pdfFile)

                    uploadResult.onSuccess {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            _uploadProgress.value = 1f
                            _uploadSuccess.value = true
                            _isUploading.value = false
                            _isCreatingAssignment.value = false
                        }

                        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            try {
                                loadAllAssignments(teacherId = teacherId, silent = true)
                            } catch (e: Exception) {
                            }
                        }

                        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                _isGeneratingQuestions.value = true
                                _generatingAssignmentTitle.value = assignment.title
                                generatingAssignmentId = createResponse.assignment_id
                            }

                            try {
                                val result = assignmentRepository.createQuestionsAfterUpload(
                                    assignmentId = createResponse.assignment_id,
                                    materialId = createResponse.material_id,
                                    totalNumber = totalNumber,
                                )

                                result.onSuccess {
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        _questionGenerationSuccess.value = true
                                        _isGeneratingQuestions.value = false
                                        _generatingAssignmentTitle.value = null
                                        generatingAssignmentId = null
                                    }

                                    try {
                                        loadAllAssignments(silent = true)
                                    } catch (e: Exception) {
                                    }
                                }.onFailure { e ->
                                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                        _questionGenerationError.value = e.message
                                        _isGeneratingQuestions.value = false
                                        _generatingAssignmentTitle.value = null
                                        generatingAssignmentId = null
                                    }
                                }
                            } catch (e: Exception) {
                                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                                    _questionGenerationError.value = e.message
                                    _isGeneratingQuestions.value = false
                                    _generatingAssignmentTitle.value = null
                                    generatingAssignmentId = null
                                }
                            }
                        }
                    }.onFailure { e ->
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            _error.value = "PDF 업로드 실패: ${ErrorMessageMapper.getErrorMessage(e)}"
                            _isUploading.value = false
                            _isCreatingAssignment.value = false
                        }
                    }
                }.onFailure { e ->
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        _error.value = "과제 생성 실패: ${ErrorMessageMapper.getErrorMessage(e)}"
                        _isUploading.value = false
                        _isCreatingAssignment.value = false
                    }
                }
            } catch (e: Exception) {
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

    fun loadRecentAssignment(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            assignmentRepository.getRecentPersonalAssignment(studentId)
                .onSuccess { personalAssignmentId ->
                    val personalAssignmentsResult = assignmentRepository.getPersonalAssignments(studentId)

                    personalAssignmentsResult.onSuccess { personalAssignments ->
                        val personalAssignment = personalAssignments.find { it.id == personalAssignmentId }

                        if (personalAssignment != null) {
                            val recent = RecentAssignment(
                                id = personalAssignment.id.toString(),
                                title = personalAssignment.assignment.title,
                                assignmentId = personalAssignment.assignment.id,
                            )
                            _recentAssignment.value = recent
                        } else {
                            _recentAssignment.value = null
                        }
                    }.onFailure { exception ->
                        _recentAssignment.value = null
                        _error.value = ErrorMessageMapper.getErrorMessage(exception)
                    }
                }
                .onFailure { exception ->
                    _recentAssignment.value = null
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
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
        _isUploading.value = false
        _uploadSuccess.value = false
        _isGeneratingQuestions.value = false
        _questionGenerationSuccess.value = false
        _questionGenerationError.value = null
        _questionGenerationCancelled.value = false
        _generatingAssignmentTitle.value = null
        generatingAssignmentId = null
    }

    fun clearQuestionGenerationStatus() {
        _questionGenerationSuccess.value = false
        _questionGenerationError.value = null
        _questionGenerationCancelled.value = false
    }

    fun cancelQuestionGeneration() {
        val assignmentId = generatingAssignmentId

        _isGeneratingQuestions.value = false
        _generatingAssignmentTitle.value = null
        _questionGenerationSuccess.value = false
        _questionGenerationError.value = null
        _questionGenerationCancelled.value = true

        if (assignmentId != null) {
            viewModelScope.launch {
                try {
                    val updateRequest = UpdateAssignmentRequest.builder()
                        .totalQuestions(0)
                        .build()
                    assignmentRepository.updateAssignment(assignmentId, updateRequest)
                        .onSuccess {
                            generatingAssignmentId = null
                        }
                        .onFailure { e ->
                            generatingAssignmentId = null
                        }
                } catch (e: Exception) {
                    generatingAssignmentId = null
                }
            }
        } else {
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
                return@launch
            }

            if (_isLoading.value) {
                return@launch
            }

            _isLoading.value = true
            _error.value = null

            assignmentRepository.getPersonalAssignmentQuestions(personalAssignmentId)
                .onSuccess { questions ->
                    val safeQuestions = questions ?: emptyList()
                    _personalAssignmentQuestions.value = safeQuestions
                    _currentQuestionIndex.value = 0
                    lastLoadedPersonalAssignmentId = personalAssignmentId
                }
                .onFailure { exception ->
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                }

            _isLoading.value = false
        }
    }

    fun loadAllQuestions(personalAssignmentId: Int) {
        viewModelScope.launch {
            if (_isLoading.value) {
                return@launch
            }

            _isLoading.value = true
            _error.value = null

            try {
                assignmentRepository.getPersonalAssignmentQuestions(personalAssignmentId)
                    .onSuccess { baseQuestions ->
                        _totalBaseQuestions.value = baseQuestions.size

                        assignmentRepository.getNextQuestion(personalAssignmentId)
                            .onSuccess { nextQuestion ->
                                _personalAssignmentQuestions.value = listOf(nextQuestion)
                                _currentQuestionIndex.value = 0
                            }
                            .onFailure { exception ->
                                val message = exception.message ?: ""
                                if (message.contains("모든 문제를 완료했습니다") ||
                                    message.contains("No more questions")
                                ) {
                                    assignmentRepository.getPersonalAssignmentStatistics(personalAssignmentId)
                                        .onSuccess { stats ->
                                            if (stats.totalProblem == stats.solvedProblem) {
                                                _personalAssignmentQuestions.value = emptyList()
                                                _currentQuestionIndex.value = 0
                                                _error.value = null
                                                _personalAssignmentStatistics.value = stats

                                                completeAssignment(personalAssignmentId)
                                            } else {
                                                _personalAssignmentStatistics.value = stats
                                                _error.value = "아직 모든 문제를 완료하지 못했습니다. (${stats.solvedProblem}/${stats.totalProblem})"
                                            }
                                        }
                                        .onFailure { statsException ->
                                            _error.value = "통계를 확인할 수 없습니다: ${ErrorMessageMapper.getErrorMessage(statsException)}"
                                        }
                                } else {
                                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                                }
                            }
                    }
                    .onFailure { exception ->
                        _error.value = ErrorMessageMapper.getErrorMessage(exception)
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadNextQuestion(personalAssignmentId: Int) {
        viewModelScope.launch {
            if (_isLoading.value) {
                return@launch
            }

            _isLoading.value = true
            _error.value = null

            try {
                assignmentRepository.getNextQuestion(personalAssignmentId)
                    .onSuccess { question ->
                        _isProcessing.value = question.isProcessing
                        if (!question.isProcessing) {
                            _personalAssignmentQuestions.value = listOf(question)
                            _currentQuestionIndex.value = 0
                        }
                    }
                    .onFailure { exception ->
                        if (exception.message?.contains("No more questions") == true ||
                            exception.message?.contains("모든 문제를 완료했습니다") == true
                        ) {
                            assignmentRepository.getPersonalAssignmentStatistics(personalAssignmentId)
                                .onSuccess { stats ->
                                    if (stats.totalProblem == stats.solvedProblem) {
                                        _personalAssignmentQuestions.value = emptyList()
                                        _error.value = null
                                        _personalAssignmentStatistics.value = stats
                                    } else {
                                        _personalAssignmentStatistics.value = stats
                                        _error.value = "아직 모든 문제를 완료하지 못했습니다. (${stats.solvedProblem}/${stats.totalProblem})"
                                    }
                                }
                                .onFailure { statsException ->
                                    _error.value = "통계를 확인할 수 없습니다: ${statsException.message}"
                                }
                        } else {
                            _error.value = ErrorMessageMapper.getErrorMessage(exception)
                        }
                    }
            } finally {
                _isLoading.value = false
            }
        }
    }
    fun refreshProcessingStatus(personalAssignmentId: Int) {
        viewModelScope.launch {
            assignmentRepository.getNextQuestion(personalAssignmentId)
                .onSuccess { question ->
                    _isProcessing.value = question.isProcessing

                    if (!question.isProcessing) {
                        _personalAssignmentQuestions.value = listOf(question)
                        _currentQuestionIndex.value = 0
                    }
                }
                .onFailure { e ->
                    val msg = e.message ?: ""
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

            assignmentRepository.getPersonalAssignmentStatistics(personalAssignmentId)
                .onSuccess { statistics ->
                    _personalAssignmentStatistics.value = statistics
                }
                .onFailure { exception ->
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

                    assignmentRepository.getPersonalAssignmentStatistics(personalAssignmentId)
                        .onSuccess { stats ->
                            _personalAssignmentStatistics.value = stats
                        }
                        .onFailure { statsException ->
                        }
                }
                .onFailure { exception ->
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
                }

            _isSubmitting.value = false
        }
    }

    fun startRecording() {
        _audioRecordingState.value = _audioRecordingState.value.copy(
            isRecording = true,
            recordingTime = 0,
            audioFilePath = null,
        )
    }

    fun stopRecording(audioFilePath: String) {
        _audioRecordingState.value = _audioRecordingState.value.copy(
            isRecording = false,
            audioFilePath = audioFilePath,
        )
    }

    fun stopRecordingImmediately() {
        _audioRecordingState.value = _audioRecordingState.value.copy(
            isRecording = false,
        )
    }

    fun stopRecordingWithFilePath(audioFilePath: String) {
        _audioRecordingState.value = _audioRecordingState.value.copy(
            isRecording = false,
            audioFilePath = audioFilePath,
        )
    }

    fun updateRecordingDuration(duration: Int) {
        _audioRecordingState.value = _audioRecordingState.value.copy(
            recordingTime = duration,
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
        if (!QuestionGroupFactory.isBaseQuestion(questionNumber)) {
            return
        }

        val targetNumber = questionNumber.toIntOrNull() ?: return

        val questions = _personalAssignmentQuestions.value
        val targetIndex = questions.indexOfFirst { it.number == questionNumber }

        if (targetIndex != -1) {
            _currentQuestionIndex.value = targetIndex
        } else {
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
            audioFilePath = filePath,
        )
    }

    fun setRecordingComplete(isComplete: Boolean) {
        _audioRecordingState.value = _audioRecordingState.value.copy(
            isRecordingComplete = isComplete,
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
            try {
                var assignmentId: Int? = null
                assignmentRepository.getPersonalAssignments(assignmentId = null)
                    .onSuccess { allPersonalAssignments ->
                        val personalAssignment = allPersonalAssignments.find { it.id == personalAssignmentId }
                        assignmentId = personalAssignment?.assignment?.id
                    }

                assignmentRepository.completePersonalAssignment(personalAssignmentId)
                    .onSuccess {
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
                        _error.value = ErrorMessageMapper.getErrorMessage(exception)
                    }
            } catch (e: Exception) {
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

                    val isCompleted = when {
                        personalAssignment.status == PersonalAssignmentStatus.SUBMITTED -> true
                        !personalAssignment.submittedAt.isNullOrEmpty() -> true
                        !personalAssignment.startedAt.isNullOrEmpty() && assignmentTotalQuestions > 0 && solvedNum >= assignmentTotalQuestions -> true
                        !personalAssignment.startedAt.isNullOrEmpty() && stats != null && stats.totalProblem > 0 && stats.totalProblem == stats.solvedProblem -> true
                        !personalAssignment.startedAt.isNullOrEmpty() && stats != null && stats.totalQuestions > 0 && stats.answeredQuestions >= stats.totalQuestions -> true
                        else -> false
                    }
                    isCompleted
                }

                val submittedCount = submittedAssignments.size
                val completionRate = (submittedCount * 100) / totalStudents

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
                    completionRate = completionRate,
                )
            }
        } catch (e: Exception) {
            AssignmentStatistics(0, 0, 0, 0)
        }
    }

    fun loadAssignmentCorrectness(personalAssignmentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            assignmentRepository.getAssignmentCorrectness(personalAssignmentId)
                .onSuccess { correctnessData ->
                    _assignmentCorrectness.value = correctnessData
                }
                .onFailure { exception ->
                    _error.value = ErrorMessageMapper.getErrorMessage(exception)
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

    fun loadPersonalAssignmentStatsAndCorrectness(studentId: Int, assignmentId: Int, silent: Boolean = false) {
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
                            coroutineScope {
                                val statsDeferred = async {
                                    assignmentRepository.getPersonalAssignmentStatistics(pa.id)
                                }
                                val correctnessDeferred = async {
                                    assignmentRepository.getAssignmentCorrectness(pa.id)
                                }

                                statsDeferred.await()
                                    .onSuccess { statistics ->
                                        _personalAssignmentStatistics.value = statistics
                                    }
                                    .onFailure { e ->
                                        if (!silent) {
                                            _error.value = e.message
                                        }
                                    }

                                correctnessDeferred.await()
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
