package com.example.voicetutor.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainLayoutViewModel @Inject constructor() : ViewModel() {

    private val _lastTeacherBaseRoute = MutableStateFlow(TEACHER_DASHBOARD)
    val lastTeacherBaseRoute: StateFlow<String> = _lastTeacherBaseRoute.asStateFlow()

    fun updateTeacherBaseRoute(route: String) {
        if (_lastTeacherBaseRoute.value != route) {
            _lastTeacherBaseRoute.value = route
        }
    }

    fun resetTeacherBaseRoute() {
        _lastTeacherBaseRoute.value = TEACHER_DASHBOARD
    }

    companion object {
        const val TEACHER_DASHBOARD = "teacher_dashboard"
    }
}

