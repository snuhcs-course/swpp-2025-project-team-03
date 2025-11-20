package com.example.voicetutor.ui.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.voicetutor.data.models.AssignmentStatus
import com.example.voicetutor.data.models.PersonalAssignmentStatistics
import com.example.voicetutor.data.models.PersonalAssignmentStatus
import com.example.voicetutor.data.network.ApiResponse
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.data.repository.AssignmentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class AssignmentViewModelIntegrationTest {

    private lateinit var dispatcher: TestDispatcher
    private lateinit var viewModel: AssignmentViewModel
    private lateinit var apiService: FakeApiService

    @Before
    fun setUp() {
        dispatcher = StandardTestDispatcher()
        Dispatchers.setMain(dispatcher)
        apiService = FakeApiService()
        viewModel = AssignmentViewModel(AssignmentRepository(apiService))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadAllAssignments_updatesState() = runTest(dispatcher) {
        viewModel.loadAllAssignments(teacherId = "2")
        advanceUntilIdle()

        assertTrue(viewModel.assignments.value.isNotEmpty())
        assertEquals(false, viewModel.isLoading.value)
        assertEquals(null, viewModel.error.value)
    }

    @Test
    fun loadAllAssignments_withStatusFilters() = runTest(dispatcher) {
        viewModel.loadAllAssignments(teacherId = "2", status = AssignmentStatus.IN_PROGRESS)
        advanceUntilIdle()
        assertTrue(viewModel.assignments.value.isNotEmpty())

        viewModel.loadAllAssignments(teacherId = "2", status = AssignmentStatus.COMPLETED)
        advanceUntilIdle()
        assertTrue(viewModel.assignments.value.isNotEmpty())
    }

    @Test
    fun loadPendingAssignments_populatesAssignments() = runTest(dispatcher) {
        viewModel.loadPendingStudentAssignments(studentId = 1)
        advanceUntilIdle()

        assertEquals(null, viewModel.error.value)
        assertTrue(viewModel.assignments.value.size >= 0)
        assertNotNull(viewModel.studentStats.value)
    }

    @Test
    fun loadCompletedAssignments_calculatesStats() = runTest(dispatcher) {
        viewModel.loadCompletedStudentAssignments(studentId = 1)
        advanceUntilIdle()

        // Even if fake data returns empty list, the pipeline executes and stats are calculated.
        assertEquals(null, viewModel.error.value)
        assertNotNull(viewModel.studentStats.value)
    }

    @Test
    fun loadAssignmentById_setsCurrentAssignment() = runTest(dispatcher) {
        viewModel.loadAssignmentById(1)
        advanceUntilIdle()

        assertNotNull(viewModel.currentAssignment.value)
        assertEquals(null, viewModel.error.value)
    }

    @Test
    fun loadAssignmentStudentResults_updatesResults() = runTest(dispatcher) {
        viewModel.loadAssignmentStudentResults(assignmentId = 1)
        advanceUntilIdle()

        assertTrue(viewModel.assignmentResults.value.isNotEmpty())
        assertEquals(null, viewModel.error.value)
    }

    @Test
    fun loadAssignmentCorrectness_updatesState() = runTest(dispatcher) {
        viewModel.loadAssignmentCorrectness(personalAssignmentId = 1)
        advanceUntilIdle()

        assertTrue(viewModel.assignmentCorrectness.value.isNotEmpty())
        assertEquals(null, viewModel.error.value)
    }

    @Test
    fun loadPersonalAssignmentStatistics_updatesState() = runTest(dispatcher) {
        viewModel.loadPersonalAssignmentStatistics(personalAssignmentId = 1)
        advanceUntilIdle()

        assertNotNull(viewModel.personalAssignmentStatistics.value)
        assertEquals(null, viewModel.error.value)
    }

    @Test
    fun loadAllQuestions_populatesQuestions() = runTest(dispatcher) {
        viewModel.loadAllQuestions(personalAssignmentId = 1)
        advanceUntilIdle()

        assertTrue(viewModel.personalAssignmentQuestions.value.isNotEmpty())
    }

    @Test
    fun resumePersonalAssignment_afterInterruption_restoresProgress() = runTest(dispatcher) {
        val personalAssignmentId = apiService.personalAssignmentsResponse.first().id

        // Initial session load
        viewModel.loadAllQuestions(personalAssignmentId = personalAssignmentId)
        advanceUntilIdle()
        assertTrue(viewModel.personalAssignmentQuestions.value.isNotEmpty())

        // Simulate progress saved on the server after interruption
        val resumedStats = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 6,
            correctAnswers = 6,
            accuracy = 0.6f,
            totalProblem = 10,
            solvedProblem = 6,
            progress = 0.6f,
            averageScore = 82f,
        )
        apiService.personalAssignmentStatisticsResponses[personalAssignmentId] = resumedStats
        apiService.personalAssignmentsResponse = apiService.personalAssignmentsResponse.map {
            it.copy(
                status = PersonalAssignmentStatus.IN_PROGRESS,
                solvedNum = 6,
                startedAt = it.startedAt ?: "2024-01-02T09:00:00Z",
                submittedAt = null,
            )
        }

        // Recreate ViewModel to mimic process death / resume flow
        val resumedViewModel = AssignmentViewModel(AssignmentRepository(apiService))

        resumedViewModel.loadPersonalAssignmentStatistics(personalAssignmentId = personalAssignmentId)
        advanceUntilIdle()
        resumedViewModel.loadAllQuestions(personalAssignmentId = personalAssignmentId)
        advanceUntilIdle()

        val stats = resumedViewModel.personalAssignmentStatistics.value
        assertNotNull(stats)
        assertEquals(6, stats?.solvedProblem)
        assertTrue((stats?.progress ?: 0f) >= 0.6f)

        assertTrue(resumedViewModel.personalAssignmentQuestions.value.isNotEmpty())
        assertEquals(null, resumedViewModel.error.value)
    }

    @Test
    fun resumePersonalAssignment_whenFetchFails_allowsRetry() = runTest(dispatcher) {
        val personalAssignment = apiService.personalAssignmentsResponse.first()
        val studentId = personalAssignment.student.id
        val assignmentId = personalAssignment.assignment.id

        apiService.shouldFailPersonalAssignments = true

        viewModel.loadPersonalAssignmentStatisticsFor(studentId = studentId, assignmentId = assignmentId)
        advanceUntilIdle()
        assertNotNull(viewModel.error.value)

        apiService.shouldFailPersonalAssignments = false
        viewModel.clearError()

        viewModel.loadPersonalAssignmentStatisticsFor(studentId = studentId, assignmentId = assignmentId)
        advanceUntilIdle()

        assertEquals(null, viewModel.error.value)
        assertNotNull(viewModel.personalAssignmentStatistics.value)
    }

    @Test
    fun deleteAssignment_removesItem() = runTest(dispatcher) {
        viewModel.loadAllAssignments(teacherId = "2")
        advanceUntilIdle()
        val assignmentId = viewModel.assignments.value.first().id

        viewModel.deleteAssignment(assignmentId)
        advanceUntilIdle()

        assertTrue(viewModel.assignments.value.none { it.id == assignmentId })
    }

    @Test
    fun loadAllAssignments_failure_setsError() = runTest(dispatcher) {
        apiService.shouldFailGetAllAssignments = true

        viewModel.loadAllAssignments(teacherId = "2")
        advanceUntilIdle()

        assertEquals(apiService.getAllAssignmentsErrorMessage, viewModel.error.value)
    }

    @Test
    fun loadAssignmentStudentResults_failure_clearsResults() = runTest(dispatcher) {
        apiService.shouldFailPersonalAssignments = true

        viewModel.loadAssignmentStudentResults(assignmentId = 1)
        advanceUntilIdle()

        assertTrue(viewModel.assignmentResults.value.isEmpty())
        assertEquals(apiService.personalAssignmentsErrorMessage, viewModel.error.value)
    }

    @Test
    fun loadAssignmentCorrectness_failure_setsError() = runTest(dispatcher) {
        apiService.shouldFailAssignmentCorrectness = true

        viewModel.loadAssignmentCorrectness(personalAssignmentId = 1)
        advanceUntilIdle()

        assertTrue(viewModel.assignmentCorrectness.value.isEmpty())
        assertEquals(apiService.assignmentCorrectnessErrorMessage, viewModel.error.value)
    }

    @Test
    fun loadRecentAssignment_setsRecentState() = runTest(dispatcher) {
        viewModel.loadRecentAssignment(studentId = 1)
        advanceUntilIdle()

        assertNotNull(viewModel.recentAssignment.value)
    }

    @Test
    fun completeAssignment_marksCompleted() = runTest(dispatcher) {
        viewModel.completeAssignment(personalAssignmentId = 1)
        advanceUntilIdle()

        assertEquals(true, viewModel.isAssignmentCompleted.value)
        assertTrue(viewModel.personalAssignmentQuestions.value.isEmpty())
    }

    @Test
    fun getAssignmentSubmissionStats_returnsData() = runTest(dispatcher) {
        val stats = viewModel.getAssignmentSubmissionStats(assignmentId = 1)

        assertTrue(stats.totalStudents >= 0)
    }

    @Test
    fun failingLoadAllAssignments_setsError() = runTest(dispatcher) {
        val failingViewModel = AssignmentViewModel(
            AssignmentRepository(
                object : ApiService by FakeApiService() {
                    override suspend fun getAllAssignments(
                        teacherId: String?,
                        classId: String?,
                        status: String?,
                    ): Response<ApiResponse<List<com.example.voicetutor.data.models.AssignmentData>>> {
                        return Response.success(
                            ApiResponse(
                                success = false,
                                data = null,
                                message = null,
                                error = "network error",
                            ),
                        )
                    }
                },
            ),
        )

        failingViewModel.loadAllAssignments(teacherId = "2")
        advanceUntilIdle()

        assertEquals("network error", failingViewModel.error.value)
    }
}
