package com.example.voicetutor.ui.viewmodel

import app.cash.turbine.test
import com.example.voicetutor.data.models.DashboardStats
import com.example.voicetutor.data.repository.DashboardRepository
import com.example.voicetutor.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
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

@RunWith(MockitoJUnitRunner::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    lateinit var dashboardRepository: DashboardRepository

    private lateinit var viewModel: DashboardViewModel

    @Before
    fun setUp() {
        viewModel = DashboardViewModel(dashboardRepository)
    }

    @Test
    fun dashboardStats_initialState_emitsNull() = runTest {
        // Given: 새로 생성된 ViewModel
        // When: 초기 상태를 관측하면
        viewModel.dashboardStats.test {
            // Then: 첫 방출이 null 이어야 한다
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadDashboardData_success_updatesDashboardStats() = runTest {
        // Given: 저장소가 성공적으로 통계를 반환하도록 스텁
        val stats = DashboardStats(
            totalStudents = 10,
            totalClasses = 3,
            totalAssignments = 15,
            completedAssignments = 5
        )
        Mockito.`when`(dashboardRepository.getDashboardStats("1"))
            .thenReturn(Result.success(stats))

        // When
        viewModel.dashboardStats.test {
            awaitItem() // initial null
            
            viewModel.loadDashboardData("1")
            runCurrent()

            // Then: 업데이트된 통계 반영
            assert(awaitItem() == stats)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(dashboardRepository, times(1)).getDashboardStats("1")
    }

    @Test
    fun loadDashboardData_failure_setsError() = runTest {
        // Given: 저장소가 실패 반환
        Mockito.`when`(dashboardRepository.getDashboardStats("1"))
            .thenReturn(Result.failure(Exception("Network error")))

        // When
        viewModel.error.test {
            awaitItem() // initial null
            
            viewModel.loadDashboardData("1")
            runCurrent()

            // Then: 에러 메시지 설정
            val error = awaitItem()
            assert(error?.contains("Network error") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun isLoading_loadingOperation_setsTrueThenFalse() = runTest {
        // Given
        val stats = DashboardStats(
            totalStudents = 5,
            totalClasses = 2,
            totalAssignments = 10,
            completedAssignments = 3
        )
        Mockito.`when`(dashboardRepository.getDashboardStats("1"))
            .thenReturn(Result.success(stats))

        // When
        viewModel.isLoading.test {
            assert(!awaitItem()) // initial false
            
            viewModel.loadDashboardData("1")
            runCurrent()

            // Then: 로딩 상태 변경 확인
            val states = mutableListOf<Boolean>()
            states.add(awaitItem())
            states.add(awaitItem())
            // 최소 한 번은 true여야 함
            assert(states.any { it })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearError_clearsErrorState() = runTest {
        // Given: 에러가 발생한 상태
        Mockito.`when`(dashboardRepository.getDashboardStats("1"))
            .thenReturn(Result.failure(Exception("Error")))

        viewModel.error.test {
            awaitItem()
            viewModel.loadDashboardData("1")
            runCurrent()
            assert(awaitItem() != null) // 에러 설정 확인
            
            // When: clearError 호출
            viewModel.clearError()
            
            // Then: 에러가 null로 변경
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }
}

