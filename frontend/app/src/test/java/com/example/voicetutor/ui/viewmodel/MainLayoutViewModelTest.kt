package com.example.voicetutor.ui.viewmodel

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class MainLayoutViewModelTest {

    @Test
    fun initialRoute_isTeacherDashboard() = runTest {
        // Arrange
        val viewModel = MainLayoutViewModel()

        // Act
        val route = viewModel.lastTeacherBaseRoute.first()

        // Assert
        assertEquals(MainLayoutViewModel.TEACHER_DASHBOARD, route)
    }

    @Test
    fun updateTeacherBaseRoute_changesRoute() = runTest {
        // Arrange
        val viewModel = MainLayoutViewModel()
        val newRoute = "teacher_classes"

        // Act
        viewModel.updateTeacherBaseRoute(newRoute)
        val route = viewModel.lastTeacherBaseRoute.first()

        // Assert
        assertEquals(newRoute, route)
    }

    @Test
    fun updateTeacherBaseRoute_sameRoute_doesNotChange() = runTest {
        // Arrange
        val viewModel = MainLayoutViewModel()
        val initialRoute = viewModel.lastTeacherBaseRoute.first()

        // Act
        viewModel.updateTeacherBaseRoute(initialRoute)
        val route = viewModel.lastTeacherBaseRoute.first()

        // Assert
        assertEquals(initialRoute, route)
    }

    @Test
    fun resetTeacherBaseRoute_resetsToDashboard() = runTest {
        // Arrange
        val viewModel = MainLayoutViewModel()
        viewModel.updateTeacherBaseRoute("teacher_classes")

        // Act
        viewModel.resetTeacherBaseRoute()
        val route = viewModel.lastTeacherBaseRoute.first()

        // Assert
        assertEquals(MainLayoutViewModel.TEACHER_DASHBOARD, route)
    }

    @Test
    fun teacherDashboardConstant_hasCorrectValue() {
        // Assert
        assertEquals("teacher_dashboard", MainLayoutViewModel.TEACHER_DASHBOARD)
    }
}

