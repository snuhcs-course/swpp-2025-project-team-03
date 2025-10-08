package com.example.voicetutor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.repository.AIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIViewModel @Inject constructor(
    private val aiRepository: AIRepository
) : ViewModel() {
    
    private val _aiResponse = MutableStateFlow<AIConversationResponse?>(null)
    val aiResponse: StateFlow<AIConversationResponse?> = _aiResponse.asStateFlow()
    
    private val _voiceRecognitionResult = MutableStateFlow<VoiceRecognitionResponse?>(null)
    val voiceRecognitionResult: StateFlow<VoiceRecognitionResponse?> = _voiceRecognitionResult.asStateFlow()
    
    private val _quizSubmissionResult = MutableStateFlow<QuizSubmissionResponse?>(null)
    val quizSubmissionResult: StateFlow<QuizSubmissionResponse?> = _quizSubmissionResult.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * AI에게 메시지를 전송합니다
     */
    fun sendMessageToAI(
        assignmentId: String,
        studentId: Int,
        message: String,
        conversationHistory: List<ConversationMessage>? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            aiRepository.sendMessageToAI(assignmentId, studentId, message, conversationHistory)
                .onSuccess { response ->
                    _aiResponse.value = response
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 음성을 텍스트로 변환합니다
     */
    fun recognizeVoice(audioData: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            aiRepository.recognizeVoice(audioData)
                .onSuccess { result ->
                    _voiceRecognitionResult.value = result
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 퀴즈 답안을 제출합니다
     */
    fun submitQuiz(
        assignmentId: String,
        studentId: Int,
        answers: List<QuizAnswer>,
        timeSpent: Long
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            aiRepository.submitQuiz(assignmentId, studentId, answers, timeSpent)
                .onSuccess { result ->
                    _quizSubmissionResult.value = result
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * AI 응답 초기화
     */
    fun clearAIResponse() {
        _aiResponse.value = null
    }
    
    /**
     * 음성 인식 결과 초기화
     */
    fun clearVoiceRecognitionResult() {
        _voiceRecognitionResult.value = null
    }
    
    /**
     * 퀴즈 제출 결과 초기화
     */
    fun clearQuizSubmissionResult() {
        _quizSubmissionResult.value = null
    }
    
    /**
     * 에러 메시지 초기화
     */
    fun clearError() {
        _error.value = null
    }
}
