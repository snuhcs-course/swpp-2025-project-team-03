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
import javax.inject.Inject
import java.io.File

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
    
    // PDF 업로드 관련 상태
    private val _uploadProgress = MutableStateFlow(0f.coerceIn(0f, 1f))
    val uploadProgress: StateFlow<Float> = _uploadProgress.asStateFlow()
    
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()
    
    private val _uploadSuccess = MutableStateFlow(false)
    val uploadSuccess: StateFlow<Boolean> = _uploadSuccess.asStateFlow()
    
    // S3 업로드 상태
    private val _s3UploadStatus = MutableStateFlow<S3UploadStatus?>(null)
    val s3UploadStatus: StateFlow<S3UploadStatus?> = _s3UploadStatus.asStateFlow()
    
    // 학생별 통계
    private val _studentStats = MutableStateFlow<StudentStats?>(null)
    val studentStats: StateFlow<StudentStats?> = _studentStats.asStateFlow()
    
    // Personal Assignment 관련 상태들
    private val _personalAssignmentQuestions = MutableStateFlow<List<PersonalAssignmentQuestion>>(emptyList())
    val personalAssignmentQuestions: StateFlow<List<PersonalAssignmentQuestion>> = _personalAssignmentQuestions.asStateFlow()
    
    // 전체 기본 문제 개수 (진행률 계산용)
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
    
    // 과제 완료 상태
    private val _isAssignmentCompleted = MutableStateFlow(false)
    val isAssignmentCompleted: StateFlow<Boolean> = _isAssignmentCompleted.asStateFlow()
    
    // 선택된 과제 ID들 (네비게이션용)
    private val _selectedAssignmentId = MutableStateFlow<Int?>(null)
    val selectedAssignmentId: StateFlow<Int?> = _selectedAssignmentId.asStateFlow()
    
    private val _selectedPersonalAssignmentId = MutableStateFlow<Int?>(null)
    val selectedPersonalAssignmentId: StateFlow<Int?> = _selectedPersonalAssignmentId.asStateFlow()
    
    fun setSelectedAssignmentIds(assignmentId: Int, personalAssignmentId: Int?) {
        _selectedAssignmentId.value = assignmentId
        _selectedPersonalAssignmentId.value = personalAssignmentId
    }
    
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
            
            println("AssignmentViewModel - Loading all assignments for student ID: $studentId")
            // 백엔드에 학생별 과제 API가 없으므로 모든 과제를 가져와서 클라이언트에서 필터링
            assignmentRepository.getAllAssignments()
                .onSuccess { allAssignments ->
                    println("AssignmentViewModel - Received ${allAssignments.size} total assignments")
                    // 모든 과제를 표시 (임시 해결책)
                    _assignments.value = allAssignments
                    allAssignments.forEach { assignment ->
                        println("  - ${assignment.title}")
                        println("    courseClass: ${assignment.courseClass.name}")
                        println("    subject: ${assignment.courseClass.subject.name}")
                        println("    dueAt: ${assignment.dueAt}")
                    }
                    
                    // 학생별 통계 계산
                    calculateStudentStats(allAssignments)
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
        
        // 해야 할 과제: 시작 안함 + 진행 중
        val pendingAssignments = personalAssignments.count { personalAssignment: PersonalAssignmentData -> 
            personalAssignment.status == PersonalAssignmentStatus.NOT_STARTED || 
            personalAssignment.status == PersonalAssignmentStatus.IN_PROGRESS
        }
        
        // 완료한 과제: 제출됨 + 완료
        val completedAssignments = personalAssignments.count { personalAssignment: PersonalAssignmentData -> 
            personalAssignment.status == PersonalAssignmentStatus.SUBMITTED || 
            personalAssignment.status == PersonalAssignmentStatus.GRADED
        }
        
        val completionRate = if (totalAssignments > 0) completedAssignments.toFloat() / totalAssignments else 0f
        
        val stats = StudentStats(
            totalAssignments = pendingAssignments, // 해야 할 과제 개수
            completedAssignments = completedAssignments, // 완료한 과제 개수
            inProgressAssignments = personalAssignments.count { it.status == PersonalAssignmentStatus.IN_PROGRESS }, // 진행 중 과제 개수
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
                .onSuccess { createResponse ->
                    // 생성된 과제 정보를 currentAssignment에 설정
                    _currentAssignment.value = AssignmentData(
                        id = createResponse.assignment_id,
                        title = assignment.title,
                        description = assignment.description,
                        totalQuestions = assignment.questions?.size ?: 0,
                        createdAt = "", // 서버에서 받아올 수 있음
                        visibleFrom = "",
                        dueAt = assignment.due_at,
                        courseClass = CourseClass(
                            id = assignment.class_id,
                            name = "", // 서버에서 받아올 수 있음
                            description = "",
                            subject = Subject(id = 0, name = assignment.subject),
                            teacherName = "",
                            startDate = "",
                            endDate = "",
                            studentCount = 0,
                            createdAt = ""
                        ),
                        grade = assignment.grade
                    )
                    
                    // Refresh assignments list
                    loadAllAssignments()
                }
                .onFailure { exception ->
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
                        println("AssignmentViewModel - Converting Filtered PersonalAssignment: ID=${personalAssignment.id}, Assignment ID=${personalAssignment.assignment.id}, Title=${personalAssignment.assignment.title}")
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
                                name = "", // 빈 문자열로 변경
                                description = null,
                                subject = Subject(
                                    id = 0,
                                    name = "", // 빈 문자열로 변경
                                    code = null
                                ),
                                teacherName = "",
                                startDate = "",
                                endDate = "",
                                studentCount = 0,
                                createdAt = ""
                            ),
                            materials = null,
                            grade = personalAssignment.assignment.grade,
                            // Personal Assignment 정보 추가
                            personalAssignmentStatus = personalAssignment.status,
                            solvedNum = personalAssignment.solvedNum,
                            personalAssignmentId = personalAssignment.id  // PersonalAssignment ID 설정
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
                        PersonalAssignmentFilter.GRADED -> {
                            val filtered = personalAssignments.filter { 
                                it.status == PersonalAssignmentStatus.GRADED 
                            }
                            println("AssignmentViewModel - Filter: GRADED - found ${filtered.size} assignments")
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
                                name = "",
                                description = null,
                                subject = Subject(
                                    id = 0,
                                    name = "",
                                    code = null
                                ),
                                teacherName = "",
                                startDate = "",
                                endDate = "",
                                studentCount = 0,
                                createdAt = ""
                            ),
                            materials = null,
                            grade = personalAssignment.assignment.grade,
                            // Personal Assignment 정보 추가
                            personalAssignmentStatus = personalAssignment.status,
                            solvedNum = personalAssignment.solvedNum,
                            personalAssignmentId = personalAssignment.id  // PersonalAssignment ID 설정
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
    
    // 해야 할 과제 (시작 안함 + 진행 중)만 로드하는 메서드
    fun loadPendingStudentAssignments(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            println("AssignmentViewModel - Loading pending assignments for student ID: $studentId")
            assignmentRepository.getPersonalAssignments(studentId)
                .onSuccess { personalAssignments: List<PersonalAssignmentData> ->
                    println("AssignmentViewModel - Received ${personalAssignments.size} personal assignments")
                    
                    // 해야 할 과제만 필터링 (시작 안함 + 진행 중)
                    val pendingAssignments = personalAssignments.filter { 
                        it.status == PersonalAssignmentStatus.NOT_STARTED || 
                        it.status == PersonalAssignmentStatus.IN_PROGRESS 
                    }
                    
                    println("AssignmentViewModel - Found ${pendingAssignments.size} pending assignments")
                    
                    // PersonalAssignmentData를 AssignmentData로 변환
                    val convertedAssignments: List<AssignmentData> = pendingAssignments.map { personalAssignment: PersonalAssignmentData ->
                        println("AssignmentViewModel - Converting Pending PersonalAssignment: ID=${personalAssignment.id}, Assignment ID=${personalAssignment.assignment.id}, Title=${personalAssignment.assignment.title}")
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
                                name = "",
                                description = null,
                                subject = Subject(
                                    id = 0,
                                    name = "",
                                    code = null
                                ),
                                teacherName = "",
                                startDate = "",
                                endDate = "",
                                studentCount = 0,
                                createdAt = ""
                            ),
                            materials = null,
                            grade = personalAssignment.assignment.grade,
                            // Personal Assignment 정보 추가
                            personalAssignmentStatus = personalAssignment.status,
                            solvedNum = personalAssignment.solvedNum,
                            personalAssignmentId = personalAssignment.id  // PersonalAssignment ID 설정
                        )
                    }
                    
                    _assignments.value = convertedAssignments
                    println("AssignmentViewModel - Pending assignments loaded, showing ${convertedAssignments.size} assignments")
                    
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
    
    // 완료된 과제만 로드하는 메서드
    fun loadCompletedStudentAssignments(studentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            println("AssignmentViewModel - Loading completed assignments for student ID: $studentId")
            assignmentRepository.getPersonalAssignments(studentId)
                .onSuccess { personalAssignments: List<PersonalAssignmentData> ->
                    println("AssignmentViewModel - Received ${personalAssignments.size} personal assignments")
                    
                    // 완료된 과제만 필터링 (SUBMITTED 또는 GRADED 상태)
                    val completedAssignments = personalAssignments.filter { 
                        it.status == PersonalAssignmentStatus.SUBMITTED || it.status == PersonalAssignmentStatus.GRADED
                    }
                    
                    println("AssignmentViewModel - Found ${completedAssignments.size} completed assignments")
                    
                    // PersonalAssignmentData를 AssignmentData로 변환
                    val convertedAssignments: List<AssignmentData> = completedAssignments.map { personalAssignment: PersonalAssignmentData ->
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
                                name = "",
                                description = null,
                                subject = Subject(
                                    id = 0,
                                    name = "",
                                    code = null
                                ),
                                teacherName = "",
                                startDate = "",
                                endDate = "",
                                studentCount = 0,
                                createdAt = ""
                            ),
                            materials = null,
                            grade = personalAssignment.assignment.grade,
                            // Personal Assignment 정보 추가
                            personalAssignmentStatus = personalAssignment.status,
                            solvedNum = personalAssignment.solvedNum,
                            personalAssignmentId = personalAssignment.id  // PersonalAssignment ID 설정
                        )
                    }
                    
                    _assignments.value = convertedAssignments
                    println("AssignmentViewModel - Completed assignments loaded, showing ${convertedAssignments.size} assignments")
                    
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
    
    fun createAssignmentWithPdf(assignment: CreateAssignmentRequest, pdfFile: File) {
        println("=== AssignmentViewModel.createAssignmentWithPdf 시작 ===")
        println("PDF 파일: ${pdfFile.name}")
        println("파일 크기: ${pdfFile.length()} bytes")
        println("파일 존재: ${pdfFile.exists()}")
        
        viewModelScope.launch {
            _isLoading.value = true
            _isUploading.value = true
            _error.value = null
            _uploadProgress.value = 0f.coerceIn(0f, 1f)
            println("DEBUG: uploadProgress set to 0f")
            _uploadSuccess.value = false
            
            try {
                println("1단계: 과제 생성 요청 시작")
                // 1. 과제 생성 (S3 업로드 URL 받기)
                        assignmentRepository.createAssignment(assignment)
                    .onSuccess { createResponse ->
                        println("✅ 과제 생성 성공")
                        println("과제 ID: ${createResponse.assignment_id}")
                        println("자료 ID: ${createResponse.material_id}")
                        println("S3 키: ${createResponse.s3_key}")
                        println("업로드 URL: ${createResponse.upload_url}")
                        _uploadProgress.value = 0.3f.coerceIn(0f, 1f)
                        println("DEBUG: uploadProgress set to 0.3f")
                        
                        // 생성된 과제 정보를 currentAssignment에 설정
                        _currentAssignment.value = AssignmentData(
                            id = createResponse.assignment_id,
                            title = assignment.title,
                            description = assignment.description,
                            totalQuestions = assignment.questions?.size ?: 0,
                            createdAt = "", // 서버에서 받아올 수 있음
                            visibleFrom = "",
                            dueAt = assignment.due_at,
                            courseClass = CourseClass(
                                id = assignment.class_id,
                                name = "", // 서버에서 받아올 수 있음
                                description = "",
                                subject = Subject(id = 0, name = assignment.subject),
                                teacherName = "",
                                startDate = "",
                                endDate = "",
                                studentCount = 0,
                                createdAt = ""
                            ),
                            grade = assignment.grade
                        )
                        
                        println("2단계: S3 업로드 시작")
                        // 2. PDF 파일을 S3에 업로드
                        assignmentRepository.uploadPdfToS3(createResponse.upload_url, pdfFile)
                            .onSuccess {
                                println("✅ S3 업로드 성공")
                                _uploadProgress.value = 1f.coerceIn(0f, 1f)
                                println("DEBUG: uploadProgress set to 1f")
                                _uploadSuccess.value = true
                                _isUploading.value = false

                                // 3. 업로드 완료 직후 기본 문제 생성 트리거
                                val totalNumber = assignment.questions?.size ?: 5
                                println("3단계: 기본 문제 생성 트리거 - totalNumber=$totalNumber")
                                viewModelScope.launch {
                                    assignmentRepository.createQuestionsAfterUpload(
                                        assignmentId = createResponse.assignment_id,
                                        materialId = createResponse.material_id,
                                        totalNumber = totalNumber
                                    ).onSuccess {
                                        println("✅ 기본 문제 생성 요청 성공")
                                    }.onFailure { genErr ->
                                        println("❌ 기본 문제 생성 요청 실패: ${genErr.message}")
                                        // 실패해도 과제 생성 자체는 유지. 필요 시 사용자 알림 처리 가능
                                    }
                                }
                                
                                // Refresh assignments list
                                loadAllAssignments()
                            }
                            .onFailure { uploadException ->
                                println("❌ S3 업로드 실패: ${uploadException.message}")
                                _error.value = "PDF 업로드 실패: ${uploadException.message}"
                                _isUploading.value = false
                            }
                    }
                    .onFailure { createException ->
                        println("❌ 과제 생성 실패: ${createException.message}")
                        _error.value = "과제 생성 실패: ${createException.message}"
                        _isUploading.value = false
                    }
            } catch (e: Exception) {
                println("❌ 예상치 못한 오류: ${e.message}")
                _error.value = "예상치 못한 오류: ${e.message}"
                _isUploading.value = false
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
    
    // TODO: Implement if needed
    // fun saveAssignmentDraft(assignmentId: Int, draftContent: String) {
    //     viewModelScope.launch {
    //         _isLoading.value = true
    //         _error.value = null
    //         
    //         assignmentRepository.saveAssignmentDraft(assignmentId, draftContent)
    //             .onSuccess {
    //                 // Draft saved successfully
    //                 println("Draft saved for assignment $assignmentId")
    //             }
    //             .onFailure { exception ->
    //                 _error.value = exception.message
    //             }
    //         _isLoading.value = false
    //     }
    // }
    
    // TODO: Implement if needed
    // fun loadAssignmentResults(id: Int) {
    //     viewModelScope.launch {
    //         _isLoading.value = true
    //         _error.value = null
    //         
    //         assignmentRepository.getAssignmentResults(id)
    //             .onSuccess { results ->
    //                 _assignmentResults.value = results
    //             }
    //             .onFailure { exception ->
    //                 _error.value = exception.message
    //             }
    //         
    //         _isLoading.value = false
    //     }
    // }
    
    // TODO: Implement if needed
    // fun loadAssignmentQuestions(id: Int) {
    //     viewModelScope.launch {
    //         _isLoading.value = true
    //         _error.value = null
    //         
    //         assignmentRepository.getAssignmentQuestions(id)
    //             .onSuccess { questions ->
    //                 _assignmentQuestions.value = questions
    //             }
    //             .onFailure { exception ->
    //                 _error.value = exception.message
    //             }
    //         
    //         _isLoading.value = false
    //     }
    // }
    
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
    
    fun resetUploadState() {
        _uploadProgress.value = 0f.coerceIn(0f, 1f)
        println("DEBUG: uploadProgress reset to 0f")
        _isUploading.value = false
        _uploadSuccess.value = false
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
                    _error.value = "S3 확인 실패: ${exception.message}"
                }
            
            _isLoading.value = false
        }
    }
    
    // Personal Assignment API 메서드들
    private var lastLoadedPersonalAssignmentId: Int? = null
    
    fun loadPersonalAssignmentQuestions(personalAssignmentId: Int) {
        viewModelScope.launch {
            // 이미 같은 ID로 로딩했거나, 로딩 중이면 중복 호출 방지
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
                    _currentQuestionIndex.value = 0 // 첫 번째 문제부터 시작
                    lastLoadedPersonalAssignmentId = personalAssignmentId
                    println("AssignmentViewModel - Successfully loaded ${questions.size} questions")
                }
                .onFailure { exception ->
                    _error.value = exception.message
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
                // Step 1: 모든 기본 문제 로드 (진행률 계산용)
                println("AssignmentViewModel - Loading all base questions for progress calculation")
                assignmentRepository.getPersonalAssignmentQuestions(personalAssignmentId)
                    .onSuccess { baseQuestions ->
                        println("AssignmentViewModel - Loaded ${baseQuestions.size} base questions")
                        _totalBaseQuestions.value = baseQuestions.size
                        
                        // Step 2: 다음 풀어야 할 문제 찾기
                        println("AssignmentViewModel - Finding next question to solve")
                        assignmentRepository.getNextQuestion(personalAssignmentId)
                            .onSuccess { nextQuestion ->
                                println("AssignmentViewModel - Found next question: ${nextQuestion.number}")
                                
                                // 다음 문제를 현재 문제로 설정
                                _personalAssignmentQuestions.value = listOf(nextQuestion)
                                _currentQuestionIndex.value = 0
                                println("AssignmentViewModel - Set current question to next question")
                            }
                            .onFailure { exception ->
                                println("AssignmentViewModel - No next question found: ${exception.message}")
                                
                                // "모든 문제를 완료했습니다" 메시지인지 확인
                                val message = exception.message ?: ""
                                if (message.contains("모든 문제를 완료했습니다") || 
                                    message.contains("No more questions")) {
                                    println("AssignmentViewModel - All questions completed! Message: $message")
                                    
                                    // 통계를 먼저 다시 로드하여 최신 상태 확인
                                    assignmentRepository.getPersonalAssignmentStatistics(personalAssignmentId)
                                        .onSuccess { stats ->
                                            println("AssignmentViewModel - Statistics: totalProblem=${stats.totalProblem}, solvedProblem=${stats.solvedProblem}")
                                            
                                            // totalProblem과 solvedProblem이 같을 때만 완료 처리
                                            if (stats.totalProblem == stats.solvedProblem) {
                                                println("AssignmentViewModel - All problems solved, completing assignment")
                                                _personalAssignmentQuestions.value = emptyList()
                                                _currentQuestionIndex.value = 0
                                                _error.value = null
                                                _personalAssignmentStatistics.value = stats
                                                
                                                // 과제 완료 처리
                                                completeAssignment(personalAssignmentId)
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
                                    println("AssignmentViewModel - Other error: $message")
                                    _error.value = exception.message
                                }
                            }
                    }
                    .onFailure { exception ->
                        println("AssignmentViewModel - Failed to load base questions: ${exception.message}")
                        _error.value = exception.message
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
                        // 단일 질문을 리스트로 변환하여 저장
                        _personalAssignmentQuestions.value = listOf(question)
                        _currentQuestionIndex.value = 0
                        println("AssignmentViewModel - Successfully loaded next question: ${question.question}")
                    }
                    .onFailure { exception ->
                        println("AssignmentViewModel - FAILURE: ${exception.message}")
                        
                        // "No more questions" 에러인지 확인
                        if (exception.message?.contains("No more questions") == true || 
                            exception.message?.contains("모든 문제를 완료했습니다") == true) {
                            println("AssignmentViewModel - All questions completed - checking statistics")
                            
                            // 통계를 확인하여 모든 문제가 해결되었는지 확인
                            assignmentRepository.getPersonalAssignmentStatistics(personalAssignmentId)
                                .onSuccess { stats ->
                                    println("AssignmentViewModel - Statistics: totalProblem=${stats.totalProblem}, solvedProblem=${stats.solvedProblem}")
                                    
                                    // totalProblem과 solvedProblem이 같을 때만 완료 상태로 설정
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
                            _error.value = exception.message
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
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }

    // Helper: find personalAssignment by (studentId, assignmentId) and load its statistics
    fun loadPersonalAssignmentStatisticsFor(studentId: Int, assignmentId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                assignmentRepository.getPersonalAssignments(studentId = studentId, assignmentId = assignmentId)
                    .onSuccess { list ->
                        val pa = list.firstOrNull()
                        if (pa == null) {
                            _error.value = "Personal assignment not found for student $studentId and assignment $assignmentId"
                        } else {
                            // then load statistics
                            assignmentRepository.getPersonalAssignmentStatistics(pa.id)
                                .onSuccess { statistics ->
                                    _personalAssignmentStatistics.value = statistics
                                }
                                .onFailure { e -> _error.value = e.message }
                        }
                    }
                    .onFailure { e -> _error.value = e.message }
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun submitAnswer(personalAssignmentId: Int, studentId: Int, questionId: Int, audioFile: File) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // 오디오 녹음 상태를 처리 중으로 설정 (SimpleRecordingState에는 isProcessing 필드가 없음)
            
            assignmentRepository.submitAnswer(personalAssignmentId, studentId, questionId, audioFile)
                .onSuccess { response ->
                    _answerSubmissionResponse.value = response
                    
                    println("AssignmentViewModel - Answer submitted successfully")
                    println("AssignmentViewModel - isCorrect: ${response.isCorrect}, numberStr: ${response.numberStr}")
                    
                    // 답변 제출 후 통계 갱신 (solved_problem 업데이트 확인용)
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
                    _error.value = exception.message
                    println("AssignmentViewModel - Failed to submit answer: ${exception.message}")
                }
            
            _isLoading.value = false
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
    
    // 서버 응답의 number_str을 기반으로 올바른 질문으로 이동
    fun moveToQuestionByNumber(questionNumber: String, personalAssignmentId: Int) {
        println("AssignmentViewModel - moveToQuestionByNumber called with: $questionNumber")
        
        // 하이픈이 포함된 경우 (꼬리 질문)는 별도 처리
        if (questionNumber.contains("-")) {
            println("AssignmentViewModel - This is a tail question: $questionNumber")
            return
        }
        
        // 기본 질문 번호로 변환
        val targetNumber = questionNumber.toIntOrNull() ?: return
        println("AssignmentViewModel - Target question number: $targetNumber")
        
        // 현재 질문 리스트에서 해당 번호의 질문 찾기
        val questions = _personalAssignmentQuestions.value
        val targetIndex = questions.indexOfFirst { it.number == questionNumber }
        
        if (targetIndex != -1) {
            println("AssignmentViewModel - Found question at index: $targetIndex")
            _currentQuestionIndex.value = targetIndex
        } else {
            println("AssignmentViewModel - Question $questionNumber not found in current list, loading from server")
            // 현재 리스트에 없는 경우 서버에서 다음 질문을 로드
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
                // 백엔드에 과제 완료 API 호출
                assignmentRepository.completePersonalAssignment(personalAssignmentId)
                    .onSuccess {
                        println("AssignmentViewModel - Assignment completed successfully")
                        
                        // 과제 완료 상태 설정
                        _isAssignmentCompleted.value = true
                        _personalAssignmentQuestions.value = emptyList()
                        _currentQuestionIndex.value = 0
                    }
                    .onFailure { exception ->
                        println("AssignmentViewModel - Error completing assignment: ${exception.message}")
                        _error.value = exception.message
                    }
            } catch (e: Exception) {
                println("AssignmentViewModel - Error completing assignment: ${e.message}")
                _error.value = e.message
            }
        }
    }
}
