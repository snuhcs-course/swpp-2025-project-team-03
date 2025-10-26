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
import com.example.voicetutor.data.models.AudioRecordingState
import com.example.voicetutor.ui.navigation.RecentAssignment
import com.example.voicetutor.data.network.AssignmentSubmissionRequest
import com.example.voicetutor.data.network.AssignmentSubmissionResult
import com.example.voicetutor.data.network.CreateAssignmentRequest
import com.example.voicetutor.data.network.CreateAssignmentResponse
import com.example.voicetutor.data.network.S3UploadStatus
import com.example.voicetutor.data.network.UpdateAssignmentRequest
import com.example.voicetutor.data.repository.AssignmentRepository
import java.io.File
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
    
    // PDF 업로드 관련 상태
    private val _uploadProgress = MutableStateFlow(0f)
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
    
    private val _personalAssignmentStatistics = MutableStateFlow<PersonalAssignmentStatistics?>(null)
    val personalAssignmentStatistics: StateFlow<PersonalAssignmentStatistics?> = _personalAssignmentStatistics.asStateFlow()
    
    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()
    
    private val _audioRecordingState = MutableStateFlow(AudioRecordingState())
    val audioRecordingState: StateFlow<AudioRecordingState> = _audioRecordingState.asStateFlow()
    
    private val _answerSubmissionResponse = MutableStateFlow<AnswerSubmissionResponse?>(null)
    val answerSubmissionResponse: StateFlow<AnswerSubmissionResponse?> = _answerSubmissionResponse.asStateFlow()
    
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
                    // CreateAssignmentResponse를 받았으므로 AssignmentData로 변환 필요
                    // TODO: 실제로는 createResponse에서 AssignmentData를 생성해야 함
                    // Refresh assignments list
                    loadAllAssignments()
                }
                .onFailure { exception ->
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
            _uploadProgress.value = 0f
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
                        _uploadProgress.value = 0.3f
                        
                        println("2단계: S3 업로드 시작")
                        // 2. PDF 파일을 S3에 업로드
                        assignmentRepository.uploadPdfToS3(createResponse.upload_url, pdfFile)
                            .onSuccess {
                                println("✅ S3 업로드 성공")
                                _uploadProgress.value = 1f
                                _uploadSuccess.value = true
                                _isUploading.value = false
                                
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
    
    fun resetUploadState() {
        _uploadProgress.value = 0f
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
    
    fun submitAnswer(studentId: Int, questionId: Int, audioFile: File) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // 오디오 녹음 상태를 처리 중으로 설정
            _audioRecordingState.value = _audioRecordingState.value.copy(isProcessing = true)
            
            assignmentRepository.submitAnswer(studentId, questionId, audioFile)
                .onSuccess { response ->
                    _answerSubmissionResponse.value = response
                    
                    println("AssignmentViewModel - Answer submitted successfully")
                    
                    // 답변 전송 성공 후 항상 다음 문제로 이동
                    val totalQuestions = _personalAssignmentQuestions.value.size
                    val currentIndex = _currentQuestionIndex.value
                    
                    if (currentIndex < totalQuestions - 1) {
                        // 아직 풀어야 할 문제가 있으면 다음 문제로 이동
                        _currentQuestionIndex.value = currentIndex + 1
                        println("AssignmentViewModel - Moved to next question: ${_currentQuestionIndex.value}/${totalQuestions}")
                    } else {
                        // 모든 문제를 완료한 경우
                        println("AssignmentViewModel - All questions completed")
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                    println("AssignmentViewModel - Failed to submit answer: ${exception.message}")
                }
            
            _isLoading.value = false
            _audioRecordingState.value = _audioRecordingState.value.copy(isProcessing = false)
        }
    }
    
    fun startRecording() {
        _audioRecordingState.value = _audioRecordingState.value.copy(
            isRecording = true,
            recordingDuration = 0,
            audioFilePath = null
        )
    }
    
    fun stopRecording(audioFilePath: String) {
        _audioRecordingState.value = _audioRecordingState.value.copy(
            isRecording = false,
            audioFilePath = audioFilePath
        )
    }
    
    fun updateRecordingDuration(duration: Int) {
        _audioRecordingState.value = _audioRecordingState.value.copy(
            recordingDuration = duration
        )
    }
    
    fun nextQuestion() {
        val currentIndex = _currentQuestionIndex.value
        val totalQuestions = _personalAssignmentQuestions.value.size
        
        if (currentIndex < totalQuestions - 1) {
            _currentQuestionIndex.value = currentIndex + 1
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
        _audioRecordingState.value = AudioRecordingState()
    }
    
    fun clearAnswerSubmissionResponse() {
        _answerSubmissionResponse.value = null
    }
}
