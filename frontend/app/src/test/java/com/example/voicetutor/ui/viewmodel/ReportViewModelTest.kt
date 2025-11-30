package com.example.voicetutor.ui.viewmodel

import app.cash.turbine.test
import com.example.voicetutor.data.models.AchievementStatistics
import com.example.voicetutor.data.models.CurriculumReportData
import com.example.voicetutor.data.repository.ReportRepository
import com.example.voicetutor.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ReportViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule { StandardTestDispatcher() }

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
        val vm = ReportViewModel(reportRepository)
        val reportData = buildCurriculumReportData()
        Mockito.`when`(reportRepository.getCurriculumReport(1, 1)).thenReturn(Result.success(reportData))

        vm.loadCurriculumReport(1, 1)
        advanceUntilIdle()

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
        val vm = ReportViewModel(reportRepository)
        Mockito.`when`(reportRepository.getCurriculumReport(1, 1)).thenReturn(Result.failure(Exception("Report not found")))

        vm.loadCurriculumReport(1, 1)
        advanceUntilIdle()

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
        val vm = ReportViewModel(reportRepository)
        Mockito.`when`(reportRepository.getCurriculumReport(1, 1)).thenReturn(Result.failure(Exception("Some error")))
        vm.loadCurriculumReport(1, 1)
        advanceUntilIdle()

        vm.clearError()

        vm.error.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearReport_clearsReport() = runTest {
        val vm = ReportViewModel(reportRepository)
        val reportData = buildCurriculumReportData()
        Mockito.`when`(reportRepository.getCurriculumReport(1, 1)).thenReturn(Result.success(reportData))
        vm.loadCurriculumReport(1, 1)
        advanceUntilIdle()

        vm.clearError()

        vm.error.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadCurriculumReport_setsLoadingDuringLoad() = runTest {
        val vm = ReportViewModel(reportRepository)
        val reportData = buildCurriculumReportData()
        Mockito.`when`(reportRepository.getCurriculumReport(1, 1)).thenReturn(Result.success(reportData))

        vm.loadCurriculumReport(1, 1)

        vm.isLoading.test {
            skipItems(1)
            assert(awaitItem())
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        advanceUntilIdle()
    }
}
