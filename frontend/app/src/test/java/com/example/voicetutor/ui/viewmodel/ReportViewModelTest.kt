package com.example.voicetutor.ui.viewmodel

import app.cash.turbine.test
import com.example.voicetutor.data.models.AchievementStatistics
import com.example.voicetutor.data.models.CurriculumReportData
import com.example.voicetutor.data.repository.ReportRepository
import com.example.voicetutor.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ReportViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    lateinit var reportRepository: ReportRepository

    private fun buildCurriculumReportData() = CurriculumReportData(
        totalQuestions = 20,
        totalCorrect = 17,
        overallAccuracy = 0.85,
        achievementStatistics = mapOf(
            "achievement1" to AchievementStatistics(
                totalQuestions = 10,
                correctQuestions = 9,
                accuracy = 0.9,
                content = "Content 1",
            ),
        ),
    )

    @Test
    fun initialStates_areCorrect() = runTest {
        val vm = ReportViewModel(reportRepository)

        vm.curriculumReport.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
        vm.isLoading.test {
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        vm.error.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadCurriculumReport_success_updatesReport() = runTest {
        // Given
        val vm = ReportViewModel(reportRepository)
        val reportData = buildCurriculumReportData()
        Mockito.`when`(reportRepository.getCurriculumReport(1, 1)).thenReturn(Result.success(reportData))

        // When
        vm.loadCurriculumReport(1, 1)
        advanceUntilIdle()

        // Then
        vm.curriculumReport.test {
            assertEquals(reportData, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        vm.isLoading.test {
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        vm.error.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadCurriculumReport_failure_setsError() = runTest {
        // Given
        val vm = ReportViewModel(reportRepository)
        Mockito.`when`(reportRepository.getCurriculumReport(1, 1)).thenReturn(Result.failure(Exception("Report not found")))

        // When
        vm.loadCurriculumReport(1, 1)
        advanceUntilIdle()

        // Then
        vm.error.test {
            assertEquals("Report not found", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        vm.isLoading.test {
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        vm.curriculumReport.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearError_clearsError() = runTest {
        // Given
        val vm = ReportViewModel(reportRepository)
        Mockito.`when`(reportRepository.getCurriculumReport(1, 1)).thenReturn(Result.failure(Exception("Some error")))
        vm.loadCurriculumReport(1, 1)
        advanceUntilIdle()

        // When
        vm.clearError()

        // Then
        vm.error.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearReport_clearsReport() = runTest {
        // Given
        val vm = ReportViewModel(reportRepository)
        val reportData = buildCurriculumReportData()
        Mockito.`when`(reportRepository.getCurriculumReport(1, 1)).thenReturn(Result.success(reportData))
        vm.loadCurriculumReport(1, 1)
        advanceUntilIdle()

        // When
        vm.clearReport()

        // Then
        vm.curriculumReport.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadCurriculumReport_setsLoadingDuringLoad() = runTest {
        // Given
        val vm = ReportViewModel(reportRepository)
        val reportData = buildCurriculumReportData()
        Mockito.`when`(reportRepository.getCurriculumReport(1, 1)).thenReturn(Result.success(reportData))

        // When
        vm.loadCurriculumReport(1, 1)

        // Then - verify loading state changes
        vm.isLoading.test {
            // Skip initial false state
            skipItems(1)
            // Should become true during loading
            assert(awaitItem())
            // Should become false after completion
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        advanceUntilIdle()
    }
}
