package com.example.voicetutor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicetutor.data.models.SignupRequest
import com.example.voicetutor.data.models.User
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()
    
    // 로그인 시 받은 초기 과제 목록
    private val _initialAssignments = MutableStateFlow<List<com.example.voicetutor.data.models.AssignmentData>>(emptyList())
    val initialAssignments: StateFlow<List<com.example.voicetutor.data.models.AssignmentData>> = _initialAssignments.asStateFlow()
    
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // 실제 API 호출
            authRepository.login(email, password)
                .onSuccess { user ->
                    _currentUser.value = user
                    _isLoggedIn.value = true
                    
                    // 로그인 시 받은 과제 목록 저장
                    user.assignments?.let { assignments ->
                        _initialAssignments.value = assignments
                    }
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun signup(name: String, email: String, password: String, role: UserRole, className: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // 실제 API 호출
            val signupRequest = SignupRequest(name, email, password, role, className)
            authRepository.signup(signupRequest)
                .onSuccess { user ->
                    _currentUser.value = user
                    _isLoggedIn.value = true
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            _isLoading.value = false
        }
    }
    
    fun logout() {
        _currentUser.value = null
        _isLoggedIn.value = false
        _error.value = null
    }
    
    fun clearError() {
        _error.value = null
    }
    
    private fun determineRoleFromEmail(email: String): UserRole {
        return when {
            email.contains("teacher") -> UserRole.TEACHER
            email.contains("student") -> UserRole.STUDENT
            else -> UserRole.STUDENT // 기본값
        }
    }
    
    // 현재 사용자 정보 가져오기
    fun getCurrentUser(): User? {
        return _currentUser.value
    }
    
    // 사용자 이름 가져오기
    fun getUserName(): String {
        return _currentUser.value?.name ?: "사용자"
    }
    
    // 사용자 역할 가져오기
    fun getUserRole(): UserRole? {
        return _currentUser.value?.role
    }
}
