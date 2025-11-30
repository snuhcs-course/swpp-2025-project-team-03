package com.example.voicetutor.ui.viewmodel

import app.cash.turbine.test
import com.example.voicetutor.data.models.DashboardStats
import com.example.voicetutor.data.repository.DashboardRepository
import com.example.voicetutor.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule { StandardTestDispatcher() }

    @Mock
    lateinit var dashboardRepository: DashboardRepository

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        viewModel = DashboardViewModel(dashboardRepository)
    }

    @Test
    fun dashboardStats_initialState_emitsNull() = runTest {
        viewModel.dashboardStats.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadDashboardData_success_updatesDashboardStats() = runTest {
        val stats = DashboardStats(
            totalStudents = 10,
            totalClasses = 3,
            totalAssignments = 15,
            completedAssignments = 5,
        )
        Mockito.`when`(dashboardRepository.getDashboardStats("1"))
            .thenReturn(Result.success(stats))

        viewModel.dashboardStats.test {
            awaitItem()

            viewModel.loadDashboardData("1")
            runCurrent()

            assert(awaitItem() == stats)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(dashboardRepository, times(1)).getDashboardStats("1")
    }

    @Test
    fun loadDashboardData_failure_setsError() = runTest {
        Mockito.`when`(dashboardRepository.getDashboardStats("1"))
            .thenReturn(Result.failure(Exception("Network error")))

        viewModel.error.test {
            awaitItem()

            viewModel.loadDashboardData("1")
            runCurrent()

            val error = awaitItem()
            assert(error?.contains("Network error") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun isLoading_loadingOperation_setsTrueThenFalse() = runTest {
        val stats = DashboardStats(
            totalStudents = 5,
            totalClasses = 2,
            totalAssignments = 10,
            completedAssignments = 3,
        )
        Mockito.`when`(dashboardRepository.getDashboardStats("1"))
            .thenReturn(Result.success(stats))

        viewModel.isLoading.test {
            assert(!awaitItem())

            viewModel.loadDashboardData("1")
            runCurrent()

            val states = mutableListOf<Boolean>()
            states.add(awaitItem())
            states.add(awaitItem())
            assert(states.any { it })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearError_clearsErrorState() = runTest {
        Mockito.`when`(dashboardRepository.getDashboardStats("1"))
            .thenReturn(Result.failure(Exception("Error")))

        viewModel.error.test {
            awaitItem()
            viewModel.loadDashboardData("1")
            runCurrent()
            assert(awaitItem() != null)

            viewModel.clearError()

            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
