package com.example.voicetutor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MessageViewModel @Inject constructor(
    private val messageRepository: MessageRepository
) : ViewModel() {
    
    private val _sendMessageResult = MutableStateFlow<SendMessageResponse?>(null)
    val sendMessageResult: StateFlow<SendMessageResponse?> = _sendMessageResult.asStateFlow()
    
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    /**
     * 메시지를 전송합니다
     */
    fun sendMessage(
        teacherId: Int,
        studentIds: List<Int>,
        message: String,
        messageType: String = "TEXT"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            messageRepository.sendMessage(teacherId, studentIds, message)
                .onSuccess { result ->
                    _sendMessageResult.value = result
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 메시지 목록을 가져옵니다
     */
    fun loadMessages(
        userId: Int,
        messageType: String? = null,
        limit: Int = 50,
        offset: Int = 0
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            messageRepository.getMessages(userId, messageType, limit, offset)
                .onSuccess { result ->
                    _messages.value = result.messages
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    /**
     * 메시지 전송 결과 초기화
     */
    fun clearSendMessageResult() {
        _sendMessageResult.value = null
    }
    
    /**
     * 에러 메시지 초기화
     */
    fun clearError() {
        _error.value = null
    }
}