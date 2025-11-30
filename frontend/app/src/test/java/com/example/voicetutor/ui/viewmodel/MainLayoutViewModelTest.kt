package com.example.voicetutor.ui.viewmodel

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class MainLayoutViewModelTest {

    @Test
    fun initialRoute_isTeacherDashboard() = runTest {
        val viewModel = MainLayoutViewModel()
        val route = viewModel.lastTeacherBaseRoute.first()
        assertEquals(MainLayoutViewModel.TEACHER_DASHBOARD, route)
    }

    @Test
    fun updateTeacherBaseRoute_changesRoute() = runTest {
        val viewModel = MainLayoutViewModel()
        val newRoute = "teacher_classes"
        viewModel.updateTeacherBaseRoute(newRoute)
        val route = viewModel.lastTeacherBaseRoute.first()
        assertEquals(newRoute, route)
    }

    @Test
    fun updateTeacherBaseRoute_sameRoute_doesNotChange() = runTest {
        val viewModel = MainLayoutViewModel()
        val initialRoute = viewModel.lastTeacherBaseRoute.first()
        viewModel.updateTeacherBaseRoute(initialRoute)
        val route = viewModel.lastTeacherBaseRoute.first()
        assertEquals(initialRoute, route)
    }

    @Test
    fun resetTeacherBaseRoute_resetsToDashboard() = runTest {
        val viewModel = MainLayoutViewModel()
        viewModel.updateTeacherBaseRoute("teacher_classes")
        viewModel.resetTeacherBaseRoute()
        val route = viewModel.lastTeacherBaseRoute.first()
        assertEquals(MainLayoutViewModel.TEACHER_DASHBOARD, route)
    }

    @Test
    fun teacherDashboardConstant_hasCorrectValue() {
        assertEquals("teacher_dashboard", MainLayoutViewModel.TEACHER_DASHBOARD)
    }
}
