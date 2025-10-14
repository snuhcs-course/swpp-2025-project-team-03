package com.example.voicetutor.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicetutor.data.models.DashboardStats
import com.example.voicetutor.data.models.RecentActivity
import com.example.voicetutor.data.repository.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val dashboardRepository: DashboardRepository
) : ViewModel() {
    
    private val _dashboardStats = MutableStateFlow<DashboardStats?>(null)
    val dashboardStats: StateFlow<DashboardStats?> = _dashboardStats.asStateFlow()
    
    private val _recentActivities = MutableStateFlow<List<RecentActivity>>(emptyList())
    val recentActivities: StateFlow<List<RecentActivity>> = _recentActivities.asStateFlow()
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    fun loadDashboardData(teacherId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // 실제 API 호출
            dashboardRepository.getDashboardStats(teacherId)
                .onSuccess { stats ->
                    _dashboardStats.value = stats
                }
                .onFailure { exception ->
                    _error.value = exception.message
                }
            
            dashboardRepository.getRecentActivities(teacherId)
                .onSuccess { activities ->
                    _recentActivities.value = activities
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
}
