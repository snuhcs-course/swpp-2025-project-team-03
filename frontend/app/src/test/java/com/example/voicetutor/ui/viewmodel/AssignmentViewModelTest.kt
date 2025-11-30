package com.example.voicetutor.ui.viewmodel

import app.cash.turbine.test
import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.network.CreateAssignmentRequest
import com.example.voicetutor.data.network.CreateAssignmentResponse
import com.example.voicetutor.data.network.UpdateAssignmentRequest
import com.example.voicetutor.data.repository.AssignmentRepository
import com.example.voicetutor.testing.MainDispatcherRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.*
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.junit.MockitoJUnitRunner
import java.io.File
import org.mockito.kotlin.any as kotlinAny

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class AssignmentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    lateinit var assignmentRepository: AssignmentRepository

    private fun buildSubject(): Subject = Subject(id = 1, name = "Math")

    private fun buildCourse(): CourseClass = CourseClass(
        id = 1,
        name = "Class A",
        description = null,
        subject = buildSubject(),
        teacherName = "T",

        studentCount = 0,
        createdAt = "",
    )

    private fun buildAssignment(id: Int): AssignmentData = AssignmentData(
        id = id,
        title = "Assignment $id",
        description = "desc",
        totalQuestions = 0,
        createdAt = null,

        dueAt = "",
        courseClass = buildCourse(),
        materials = null,
        grade = null,
        personalAssignmentStatus = null,
        solvedNum = null,
        personalAssignmentId = null,
    )

    @Test
    fun assignments_initialState_emitsEmptyList() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.assignments.test {
            assert(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAllAssignments_success_updatesAssignmentsAndLoading() = runTest {
        val items = listOf(buildAssignment(1), buildAssignment(2))
        Mockito.`when`(assignmentRepository.getAllAssignments(null, null, null))
            .thenReturn(Result.success(items))

        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.assignments.test {
            awaitItem()
            viewModel.loadAllAssignments()

            runCurrent()

            val next = awaitItem()
            assert(next == items)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(assignmentRepository, times(1))
            .getAllAssignments(null, null, null)
    }

    @Test
    @Ignore("Turbine timeout issue")
    fun completeAssignment_success_setsCompletedAndClearsQuestions() = runTest {
        val personalAssignmentId = 123
        Mockito.`when`(assignmentRepository.completePersonalAssignment(personalAssignmentId))
            .thenReturn(Result.success(Unit))

        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.isAssignmentCompleted.test {
            awaitItem()

            viewModel.completeAssignment(personalAssignmentId)

            advanceUntilIdle()

            assert(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(assignmentRepository, times(1))
            .completePersonalAssignment(personalAssignmentId)
    }

    @Test
    fun loadStudentAssignments_success_updatesAssignmentsAndCalculatesStats() = runTest {
        val assignments = listOf(buildAssignment(1), buildAssignment(2))
        Mockito.`when`(assignmentRepository.getAllAssignments())
            .thenReturn(Result.success(assignments))

        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.assignments.test {
            awaitItem()
            viewModel.loadStudentAssignments(studentId = 123)
            runCurrent()

            val next = awaitItem()
            assert(next == assignments)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(assignmentRepository, times(1)).getAllAssignments()
    }

    @Test
    fun loadStudentAssignments_failure_setsError() = runTest {
        Mockito.`when`(assignmentRepository.getAllAssignments())
            .thenReturn(Result.failure(Exception("Network error")))

        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.error.test {
            awaitItem()
            viewModel.loadStudentAssignments(studentId = 123)
            runCurrent()

            val error = awaitItem()
            assert(error?.contains("Network error") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentById_success_updatesCurrentAssignment() = runTest {
        val assignment = buildAssignment(1)
        Mockito.`when`(assignmentRepository.getAssignmentById(1))
            .thenReturn(Result.success(assignment))

        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.currentAssignment.test {
            assert(awaitItem() == null)
            viewModel.loadAssignmentById(1)
            runCurrent()

            assert(awaitItem() == assignment)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(assignmentRepository, times(1)).getAssignmentById(1)
    }

    @Test
    fun loadPersonalAssignmentStatistics_success_updatesStatistics() = runTest {
        val statistics = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 7,
            correctAnswers = 5,
            accuracy = 0.71f,
            totalProblem = 8,
            solvedProblem = 6,
            progress = 0.75f,
            averageScore = 0.8f,
        )
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(26))
            .thenReturn(Result.success(statistics))

        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.personalAssignmentStatistics.test {
            assert(awaitItem() == null)
            viewModel.loadPersonalAssignmentStatistics(26)
            runCurrent()

            assert(awaitItem() == statistics)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(assignmentRepository, times(1)).getPersonalAssignmentStatistics(26)
    }

    @Test
    fun isLoading_loadingOperation_setsTrueThenFalse() = runTest {
        val standardDispatcher = StandardTestDispatcher()
        val originalDispatcher = mainDispatcherRule.testDispatcher
        try {
            Dispatchers.setMain(standardDispatcher)

            Mockito.`when`(assignmentRepository.getAllAssignments(null, null, null))
                .thenReturn(Result.success(emptyList()))

            val viewModel = AssignmentViewModel(assignmentRepository)

            viewModel.isLoading.test {
                assert(!awaitItem())

                viewModel.loadAllAssignments()
                runCurrent()

                val states = mutableListOf<Boolean>()
                states.add(awaitItem())
                states.add(awaitItem())
                assert(states.any { it })
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            Dispatchers.setMain(originalDispatcher)
        }
    }

    @Test
    fun studentStats_creation_withAllParameters_createsCorrectInstance() {
        val stats = StudentStats(
            totalAssignments = 10,
            completedAssignments = 5,
            inProgressAssignments = 3,
            completionRate = 0.5f,
        )

        assert(stats.totalAssignments == 10)
        assert(stats.completedAssignments == 5)
        assert(stats.inProgressAssignments == 3)
        assert(stats.completionRate == 0.5f)
    }

    @Test
    fun studentStats_creation_withZeroValues_createsCorrectInstance() {
        val stats = StudentStats(
            totalAssignments = 0,
            completedAssignments = 0,
            inProgressAssignments = 0,
            completionRate = 0.0f,
        )

        assert(stats.totalAssignments == 0)
        assert(stats.completedAssignments == 0)
        assert(stats.inProgressAssignments == 0)
        assert(stats.completionRate == 0.0f)
    }

    @Test
    fun studentStats_copy_createsNewInstance() {
        val original = StudentStats(10, 5, 3, 0.5f)
        val copy = original.copy(completedAssignments = 7)

        assertEquals(7, copy.completedAssignments)
        assertEquals(original.totalAssignments, copy.totalAssignments)
    }

    @Test
    fun studentStats_equality_worksCorrectly() {
        val stats1 = StudentStats(10, 5, 3, 0.5f)
        val stats2 = StudentStats(10, 5, 3, 0.5f)
        val stats3 = StudentStats(10, 6, 3, 0.5f)

        assertEquals(stats1, stats2)
        assertNotEquals(stats1, stats3)
    }

    @Test
    fun studentStats_hashCode_worksCorrectly() {
        val stats1 = StudentStats(10, 5, 3, 0.5f)
        val stats2 = StudentStats(10, 5, 3, 0.5f)

        assertEquals(stats1.hashCode(), stats2.hashCode())
    }

    @Test
    fun setSelectedAssignmentIds_setsBothIds() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        assertEquals(null, viewModel.selectedAssignmentId.value)
        assertEquals(null, viewModel.selectedPersonalAssignmentId.value)

        viewModel.setSelectedAssignmentIds(1, 2)

        assertEquals(1, viewModel.selectedAssignmentId.value)
        assertEquals(2, viewModel.selectedPersonalAssignmentId.value)
    }

    @Test
    fun setSelectedAssignmentIds_withNullPersonalId_setsNull() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.setSelectedAssignmentIds(1, 2)
        assertEquals(2, viewModel.selectedPersonalAssignmentId.value)

        viewModel.setSelectedAssignmentIds(1, null)
        assertEquals(null, viewModel.selectedPersonalAssignmentId.value)
    }

    @Test
    fun clearError_clearsErrorState() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.error.test {
            awaitItem()
            viewModel.error.test {
                awaitItem()
                Mockito.`when`(assignmentRepository.getAllAssignments())
                    .thenReturn(Result.failure(Exception("Test error")))
                viewModel.loadStudentAssignments(1)
                runCurrent()
                val error = awaitItem()
                assert(error != null)

                viewModel.clearError()
                runCurrent()
                assert(awaitItem() == null)
                cancelAndIgnoreRemainingEvents()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setInitialAssignments_setsAssignments() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignments = listOf(buildAssignment(1), buildAssignment(2))

        viewModel.assignments.test {
            awaitItem()
            viewModel.setInitialAssignments(assignments)
            assertEquals(assignments, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun resetUploadState_resetsUploadStates() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.resetUploadState()

        assertEquals(0f, viewModel.uploadProgress.value)
        assertFalse(viewModel.isUploading.value)
        assertFalse(viewModel.uploadSuccess.value)
        assertFalse(viewModel.isGeneratingQuestions.value)
    }

    @Test
    fun clearQuestionGenerationStatus_clearsStatus() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.clearQuestionGenerationStatus()

        assertFalse(viewModel.questionGenerationSuccess.value)
        assertNull(viewModel.questionGenerationError.value)
    }

    @Test
    fun setAssignmentCompleted_updatesState() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.isAssignmentCompleted.test {
            assert(!awaitItem())
            viewModel.setAssignmentCompleted(true)
            assert(awaitItem())
            viewModel.setAssignmentCompleted(false)
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateRecordingDuration_updatesDuration() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.audioRecordingState.test {
            val initialState = awaitItem()
            assertEquals(0, initialState.recordingTime)

            viewModel.updateRecordingDuration(30)
            runCurrent()
            val updatedState = awaitItem()
            assertEquals(30, updatedState.recordingTime)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setAudioFilePath_updatesFilePath() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.audioRecordingState.test {
            val initialState = awaitItem()
            assertNull(initialState.audioFilePath)

            viewModel.setAudioFilePath("/path/to/audio.wav")
            runCurrent()
            val updatedState = awaitItem()
            assertEquals("/path/to/audio.wav", updatedState.audioFilePath)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun setRecordingComplete_updatesCompleteState() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.audioRecordingState.test {
            val initialState = awaitItem()
            assertFalse(initialState.isRecordingComplete)

            viewModel.setRecordingComplete(true)
            runCurrent()
            val updatedState = awaitItem()
            assertTrue(updatedState.isRecordingComplete)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearAnswerSubmissionResponse_clearsResponse() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.clearAnswerSubmissionResponse()

        assertNull(viewModel.answerSubmissionResponse.value)
    }

    @Test
    fun resetAudioRecording_resetsState() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.setAudioFilePath("/path/to/audio.wav")
        viewModel.updateRecordingDuration(30)
        runCurrent()

        assertEquals(30, viewModel.audioRecordingState.value.recordingTime)
        assertEquals("/path/to/audio.wav", viewModel.audioRecordingState.value.audioFilePath)

        viewModel.resetAudioRecording()

        val resetState = viewModel.audioRecordingState.value
        assertEquals(0, resetState.recordingTime)
        assertNull(resetState.audioFilePath)
        assertFalse(resetState.isRecordingComplete)
    }

    @Test
    fun loadAllAssignments_withSilentFlag_doesNotSetLoading() = runTest {
        val items = listOf(buildAssignment(1))
        Mockito.`when`(assignmentRepository.getAllAssignments(null, null, null))
            .thenReturn(Result.success(items))

        val viewModel = AssignmentViewModel(assignmentRepository)

        assertFalse(viewModel.isLoading.value)

        viewModel.loadAllAssignments(silent = true)

        runCurrent()
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)

        assertEquals(items, viewModel.assignments.value)
    }

    @Test
    fun loadAllAssignments_withStatus_filterByStatus() = runTest {
        val items = listOf(buildAssignment(1))
        Mockito.`when`(assignmentRepository.getAllAssignments(null, null, AssignmentStatus.IN_PROGRESS))
            .thenReturn(Result.success(items))

        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.loadAllAssignments(status = AssignmentStatus.IN_PROGRESS)
        runCurrent()

        Mockito.verify(assignmentRepository, times(1))
            .getAllAssignments(null, null, AssignmentStatus.IN_PROGRESS)
    }

    @Test
    fun loadAllAssignments_withClassId_filterByClass() = runTest {
        val items = listOf(buildAssignment(1))
        Mockito.`when`(assignmentRepository.getAllAssignments(null, "1", null))
            .thenReturn(Result.success(items))

        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.loadAllAssignments(classId = "1")
        runCurrent()

        Mockito.verify(assignmentRepository, times(1))
            .getAllAssignments(null, "1", null)
    }

    @Test
    fun getAssignmentSubmissionStats_success_returnsStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.SUBMITTED,
                solvedNum = 10,
                startedAt = "2025-01-01",
                submittedAt = "2025-01-02",
            ),
        )
        val statistics = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 10,
            correctAnswers = 8,
            accuracy = 0.8f,
            totalProblem = 10,
            solvedProblem = 10,
            progress = 1.0f,
            averageScore = 0.8f,
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.success(personalAssignments))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.success(statistics))

        val result = viewModel.getAssignmentSubmissionStats(assignmentId)

        assertEquals(1, result.submittedStudents)
        assertEquals(1, result.totalStudents)
        assertEquals(0, result.averageScore)
        assertEquals(100, result.completionRate)
    }

    @Test
    fun getAssignmentSubmissionStats_emptyList_returnsZeroStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.success(emptyList()))

        val result = viewModel.getAssignmentSubmissionStats(assignmentId)

        assertEquals(0, result.submittedStudents)
        assertEquals(0, result.totalStudents)
        assertEquals(0, result.averageScore)
        assertEquals(0, result.completionRate)
    }

    @Test
    fun getAssignmentSubmissionStats_failure_returnsZeroStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.failure(Exception("Network error")))

        val result = viewModel.getAssignmentSubmissionStats(assignmentId)

        assertEquals(0, result.submittedStudents)
        assertEquals(0, result.totalStudents)
        assertEquals(0, result.averageScore)
        assertEquals(0, result.completionRate)
    }

    @Test
    fun cancelQuestionGeneration_setsCancellationFlags() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.cancelQuestionGeneration()
        advanceUntilIdle()

        assertTrue(viewModel.questionGenerationCancelled.value)
        assertFalse(viewModel.isGeneratingQuestions.value)
        assertNull(viewModel.generatingAssignmentTitle.value)
        assertFalse(viewModel.questionGenerationSuccess.value)
        assertNull(viewModel.questionGenerationError.value)
    }

    @Test
    fun loadAssignmentCorrectnessFor_success_updatesCorrectness() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val studentId = 1
        val assignmentId = 1
        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.IN_PROGRESS,
                solvedNum = 5,
                startedAt = "2025-01-01",
                submittedAt = null,
            ),
        )
        val correctness = listOf(
            AssignmentCorrectnessItem(
                questionContent = "Question 1",
                questionModelAnswer = "Answer 1",
                studentAnswer = "Answer 1",
                isCorrect = true,
                answeredAt = "2025-01-01",
                questionNum = "1",
                explanation = "Explanation",
            ),
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId = studentId, assignmentId = assignmentId))
            .thenReturn(Result.success(personalAssignments))
        Mockito.`when`(assignmentRepository.getAssignmentCorrectness(1))
            .thenReturn(Result.success(correctness))

        viewModel.loadAssignmentCorrectnessFor(studentId, assignmentId)
        advanceUntilIdle()

        viewModel.assignmentCorrectness.test {
            assertEquals(correctness, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentCorrectnessFor_silent_doesNotSetLoading() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val studentId = 1
        val assignmentId = 1
        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.IN_PROGRESS,
                solvedNum = 5,
                startedAt = "2025-01-01",
                submittedAt = null,
            ),
        )
        val correctness = emptyList<AssignmentCorrectnessItem>()

        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId = studentId, assignmentId = assignmentId))
            .thenReturn(Result.success(personalAssignments))
        Mockito.`when`(assignmentRepository.getAssignmentCorrectness(1))
            .thenReturn(Result.success(correctness))

        viewModel.loadAssignmentCorrectnessFor(studentId, assignmentId, silent = true)
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun loadAssignmentCorrectnessFor_noPersonalAssignment_setsError() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val studentId = 1
        val assignmentId = 1

        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId = studentId, assignmentId = assignmentId))
            .thenReturn(Result.success(emptyList()))

        viewModel.loadAssignmentCorrectnessFor(studentId, assignmentId)
        advanceUntilIdle()

        viewModel.error.test {
            val error = awaitItem()
            assert(error != null)
            assert(error?.contains("Personal assignment not found") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatisticsAndResults_success_updatesStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val fallbackTotalStudents = 10
        val personalAssignments = emptyList<PersonalAssignmentData>()

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.success(personalAssignments))

        viewModel.loadAssignmentStatisticsAndResults(assignmentId, fallbackTotalStudents)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            assert(stats != null)
            if (stats != null) {
                assertEquals(0, stats.submittedStudents)
                assertEquals(0, stats.totalStudents)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatistics_success_updatesStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val totalStudents = 10
        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.SUBMITTED,
                solvedNum = 10,
                startedAt = "2025-01-01",
                submittedAt = "2025-01-02",
            ),
        )
        val statistics = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 10,
            correctAnswers = 8,
            accuracy = 0.8f,
            totalProblem = 10,
            solvedProblem = 10,
            progress = 1.0f,
            averageScore = 0.8f,
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.success(personalAssignments))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.success(statistics))

        viewModel.loadAssignmentStatistics(assignmentId, totalStudents)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            assert(stats != null)
            if (stats != null) {
                assertEquals(1, stats.submittedStudents)
                assertEquals(1, stats.totalStudents)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatistics_failure_setsDefaultStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val totalStudents = 10

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.failure(Exception("Network error")))

        viewModel.loadAssignmentStatistics(assignmentId, totalStudents)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            assert(stats != null)
            if (stats != null) {
                assertEquals(0, stats.submittedStudents)
                assertEquals(totalStudents, stats.totalStudents)
                assertEquals(0, stats.averageScore)
                assertEquals(0, stats.completionRate)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun refreshProcessingStatus_success_updatesProcessingState() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val personalAssignmentId = 1
        val question = PersonalAssignmentQuestion(
            id = 1,
            number = "1",
            question = "Question 1",
            answer = "Answer 1",
            explanation = "Explanation",
            difficulty = "easy",
            isProcessing = false,
        )

        Mockito.`when`(assignmentRepository.getNextQuestion(personalAssignmentId))
            .thenReturn(Result.success(question))

        viewModel.refreshProcessingStatus(personalAssignmentId)
        advanceUntilIdle()

        assertFalse(viewModel.isProcessing.value)
        viewModel.personalAssignmentQuestions.test {
            val questions = awaitItem()
            assertEquals(1, questions.size)
            assertEquals(question, questions[0])
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun refreshProcessingStatus_failureWithCompletionMessage_setsCompleted() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val personalAssignmentId = 1

        Mockito.`when`(assignmentRepository.getNextQuestion(personalAssignmentId))
            .thenReturn(Result.failure(Exception("모든 문제를 완료했습니다")))

        viewModel.refreshProcessingStatus(personalAssignmentId)
        advanceUntilIdle()

        assertFalse(viewModel.isProcessing.value)
        assertTrue(viewModel.isAssignmentCompleted.value)
    }

    @Test
    fun refreshProcessingStatus_failure_setsError() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val personalAssignmentId = 1

        Mockito.`when`(assignmentRepository.getNextQuestion(personalAssignmentId))
            .thenReturn(Result.failure(Exception("Network error")))

        viewModel.refreshProcessingStatus(personalAssignmentId)
        advanceUntilIdle()

        assertFalse(viewModel.isProcessing.value)
        viewModel.error.test {
            val error = awaitItem()
            assertEquals("Network error", error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatistics_withNoSubmittedAssignments_setsZeroStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val totalStudents = 10
        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.NOT_STARTED,
                solvedNum = 0,
                startedAt = null,
                submittedAt = null,
            ),
        )
        val statistics = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 0,
            correctAnswers = 0,
            accuracy = 0.0f,
            totalProblem = 10,
            solvedProblem = 0,
            progress = 0.0f,
            averageScore = 0.0f,
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.success(personalAssignments))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.success(statistics))

        viewModel.loadAssignmentStatistics(assignmentId, totalStudents)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            assert(stats != null)
            if (stats != null) {
                assertEquals(0, stats.submittedStudents)
                assertEquals(1, stats.totalStudents)
                assertEquals(0, stats.averageScore)
                assertEquals(0, stats.completionRate)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatistics_withExceptionInTryBlock_setsDefaultStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val totalStudents = 10

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenThrow(RuntimeException("Unexpected error"))

        viewModel.loadAssignmentStatistics(assignmentId, totalStudents)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            assert(stats != null)
            if (stats != null) {
                assertEquals(0, stats.submittedStudents)
                assertEquals(totalStudents, stats.totalStudents)
                assertEquals(0, stats.averageScore)
                assertEquals(0, stats.completionRate)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatistics_withMultipleCompletedAssignments_calculatesAverage() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val totalStudents = 10
        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.SUBMITTED,
                solvedNum = 10,
                startedAt = "2025-01-01",
                submittedAt = "2025-01-02",
            ),
            PersonalAssignmentData(
                id = 2,
                student = StudentInfo(2, "Student2", "s2@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.SUBMITTED,
                solvedNum = 10,
                startedAt = "2025-01-01",
                submittedAt = "2025-01-02",
            ),
        )
        val statistics1 = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 10,
            correctAnswers = 8,
            accuracy = 0.8f,
            totalProblem = 10,
            solvedProblem = 10,
            progress = 1.0f,
            averageScore = 0.8f,
        )
        val statistics2 = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 10,
            correctAnswers = 9,
            accuracy = 0.9f,
            totalProblem = 10,
            solvedProblem = 10,
            progress = 1.0f,
            averageScore = 0.9f,
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.success(personalAssignments))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.success(statistics1))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(2))
            .thenReturn(Result.success(statistics2))

        viewModel.loadAssignmentStatistics(assignmentId, totalStudents)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            assert(stats != null)
            if (stats != null) {
                assertEquals(2, stats.submittedStudents)
                assertEquals(2, stats.totalStudents)
                assertEquals(0, stats.averageScore)
                assertEquals(100, stats.completionRate)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadPersonalAssignmentStatsAndCorrectness_success_updatesBoth() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val studentId = 1
        val assignmentId = 1
        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.IN_PROGRESS,
                solvedNum = 5,
                startedAt = "2025-01-01",
                submittedAt = null,
            ),
        )
        val statistics = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 5,
            correctAnswers = 4,
            accuracy = 0.8f,
            totalProblem = 10,
            solvedProblem = 5,
            progress = 0.5f,
            averageScore = 0.8f,
        )
        val correctness = listOf(
            AssignmentCorrectnessItem(
                questionContent = "Question 1",
                questionModelAnswer = "Answer 1",
                studentAnswer = "Answer 1",
                isCorrect = true,
                answeredAt = "2025-01-01",
                questionNum = "1",
                explanation = "Explanation",
            ),
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId = studentId, assignmentId = assignmentId))
            .thenReturn(Result.success(personalAssignments))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.success(statistics))
        Mockito.`when`(assignmentRepository.getAssignmentCorrectness(1))
            .thenReturn(Result.success(correctness))

        viewModel.loadPersonalAssignmentStatsAndCorrectness(studentId, assignmentId)
        advanceUntilIdle()

        viewModel.personalAssignmentStatistics.test {
            assertEquals(statistics, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        viewModel.assignmentCorrectness.test {
            assertEquals(correctness, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadPersonalAssignmentStatsAndCorrectness_silent_doesNotSetLoading() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val studentId = 1
        val assignmentId = 1
        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.IN_PROGRESS,
                solvedNum = 5,
                startedAt = "2025-01-01",
                submittedAt = null,
            ),
        )
        val statistics = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 5,
            correctAnswers = 4,
            accuracy = 0.8f,
            totalProblem = 10,
            solvedProblem = 5,
            progress = 0.5f,
            averageScore = 0.8f,
        )
        val correctness = emptyList<AssignmentCorrectnessItem>()

        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId = studentId, assignmentId = assignmentId))
            .thenReturn(Result.success(personalAssignments))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.success(statistics))
        Mockito.`when`(assignmentRepository.getAssignmentCorrectness(1))
            .thenReturn(Result.success(correctness))

        viewModel.loadPersonalAssignmentStatsAndCorrectness(studentId, assignmentId, silent = true)
        advanceUntilIdle()

        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun loadPersonalAssignmentStatsAndCorrectness_noPersonalAssignment_setsError() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val studentId = 1
        val assignmentId = 1

        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId = studentId, assignmentId = assignmentId))
            .thenReturn(Result.success(emptyList()))

        viewModel.loadPersonalAssignmentStatsAndCorrectness(studentId, assignmentId)
        advanceUntilIdle()

        viewModel.error.test {
            val error = awaitItem()
            assert(error != null)
            assert(error?.contains("Personal assignment not found") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadPersonalAssignmentStatsAndCorrectness_statisticsFailure_setsError() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val studentId = 1
        val assignmentId = 1
        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.IN_PROGRESS,
                solvedNum = 5,
                startedAt = "2025-01-01",
                submittedAt = null,
            ),
        )
        val correctness = emptyList<AssignmentCorrectnessItem>()

        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId = studentId, assignmentId = assignmentId))
            .thenReturn(Result.success(personalAssignments))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.failure(Exception("Statistics error")))
        Mockito.`when`(assignmentRepository.getAssignmentCorrectness(1))
            .thenReturn(Result.success(correctness))

        viewModel.loadPersonalAssignmentStatsAndCorrectness(studentId, assignmentId)
        advanceUntilIdle()

        viewModel.error.test {
            val error = awaitItem()
            assert(error != null)
            assert(error?.contains("Statistics error") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadPersonalAssignmentStatsAndCorrectness_correctnessFailure_setsError() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val studentId = 1
        val assignmentId = 1
        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.IN_PROGRESS,
                solvedNum = 5,
                startedAt = "2025-01-01",
                submittedAt = null,
            ),
        )
        val statistics = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 5,
            correctAnswers = 4,
            accuracy = 0.8f,
            totalProblem = 10,
            solvedProblem = 5,
            progress = 0.5f,
            averageScore = 0.8f,
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId = studentId, assignmentId = assignmentId))
            .thenReturn(Result.success(personalAssignments))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.success(statistics))
        Mockito.`when`(assignmentRepository.getAssignmentCorrectness(1))
            .thenReturn(Result.failure(Exception("Correctness error")))

        viewModel.loadPersonalAssignmentStatsAndCorrectness(studentId, assignmentId)
        advanceUntilIdle()

        viewModel.error.test {
            val error = awaitItem()
            assert(error != null)
            assert(error?.contains("Correctness error") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatistics_completedBySubmittedAt_setsStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val totalStudents = 10
        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.IN_PROGRESS,
                solvedNum = 5,
                startedAt = "2025-01-01",
                submittedAt = "2025-01-02",
            ),
        )
        val statistics = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 10,
            correctAnswers = 8,
            accuracy = 0.8f,
            totalProblem = 10,
            solvedProblem = 10,
            progress = 1.0f,
            averageScore = 0.8f,
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.success(personalAssignments))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.success(statistics))

        viewModel.loadAssignmentStatistics(assignmentId, totalStudents)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            assert(stats != null)
            if (stats != null) {
                assertEquals(1, stats.submittedStudents)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatistics_completedBySolvedNum_setsStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val totalStudents = 10
        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.IN_PROGRESS,
                solvedNum = 10,
                startedAt = "2025-01-01",
                submittedAt = null,
            ),
        )
        val statistics = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 10,
            correctAnswers = 8,
            accuracy = 0.8f,
            totalProblem = 10,
            solvedProblem = 10,
            progress = 1.0f,
            averageScore = 0.8f,
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.success(personalAssignments))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.success(statistics))

        viewModel.loadAssignmentStatistics(assignmentId, totalStudents)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            assert(stats != null)
            if (stats != null) {
                assertEquals(1, stats.submittedStudents)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatistics_completedByTotalProblem_setsStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val totalStudents = 10
        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.IN_PROGRESS,
                solvedNum = 5,
                startedAt = "2025-01-01",
                submittedAt = null,
            ),
        )
        val statistics = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 10,
            correctAnswers = 8,
            accuracy = 0.8f,
            totalProblem = 10,
            solvedProblem = 10,
            progress = 1.0f,
            averageScore = 0.8f,
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.success(personalAssignments))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.success(statistics))

        viewModel.loadAssignmentStatistics(assignmentId, totalStudents)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            assert(stats != null)
            if (stats != null) {
                assertEquals(1, stats.submittedStudents)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatistics_completedByAnsweredQuestions_setsStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val totalStudents = 10
        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.IN_PROGRESS,
                solvedNum = 5,
                startedAt = "2025-01-01",
                submittedAt = null,
            ),
        )
        val statistics = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 10,
            correctAnswers = 8,
            accuracy = 0.8f,
            totalProblem = 10,
            solvedProblem = 5,
            progress = 0.5f,
            averageScore = 0.8f,
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.success(personalAssignments))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.success(statistics))

        viewModel.loadAssignmentStatistics(assignmentId, totalStudents)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            assert(stats != null)
            if (stats != null) {
                assertEquals(1, stats.submittedStudents)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatisticsAndResults_withEmptyList_setsZeroStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val fallbackTotalStudents = 10

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.success(emptyList()))

        viewModel.loadAssignmentStatisticsAndResults(assignmentId, fallbackTotalStudents)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            assert(stats != null)
            if (stats != null) {
                assertEquals(0, stats.submittedStudents)
                assertEquals(0, stats.totalStudents)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun createAssignment_success_updatesCurrentAssignmentAndLoadsAll() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val createRequest = CreateAssignmentRequest(
            title = "New Assignment",
            description = "Description",
            total_questions = 5,
            due_at = "2025-01-01",
            class_id = 1,
            grade = "Grade 1",
            subject = "Math",
        )
        val createResponse = CreateAssignmentResponse(
            assignment_id = 100,
            material_id = 200,
            s3_key = "test-key",
            upload_url = "https://example.com",
        )

        Mockito.`when`(assignmentRepository.createAssignment(createRequest))
            .thenReturn(Result.success(createResponse))
        Mockito.`when`(assignmentRepository.getAllAssignments("1", null, null))
            .thenReturn(Result.success(emptyList()))

        viewModel.createAssignment(createRequest, "1")
        advanceUntilIdle()

        viewModel.currentAssignment.test {
            val assignment = awaitItem()
            assert(assignment != null)
            if (assignment != null) {
                assertEquals(100, assignment.id)
                assertEquals("New Assignment", assignment.title)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun createAssignment_failure_setsError() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val createRequest = CreateAssignmentRequest(
            title = "New Assignment",
            description = "Description",
            total_questions = 5,
            due_at = "2025-01-01",
            class_id = 1,
            grade = "Grade 1",
            subject = "Math",
        )

        Mockito.`when`(assignmentRepository.createAssignment(createRequest))
            .thenReturn(Result.failure(Exception("Create failed")))

        viewModel.createAssignment(createRequest, "1")
        advanceUntilIdle()

        viewModel.error.test {
            val error = awaitItem()
            assert(error?.contains("Create failed") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateAssignment_success_updatesCurrentAndList() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val updatedAssignment = buildAssignment(1).copy(title = "Updated Title")
        val updateRequest = UpdateAssignmentRequest.builder()
            .title("Updated Title")
            .build()

        viewModel.setInitialAssignments(listOf(buildAssignment(1)))
        runCurrent()

        Mockito.`when`(assignmentRepository.updateAssignment(1, updateRequest))
            .thenReturn(Result.success(updatedAssignment))

        viewModel.updateAssignment(1, updateRequest)
        advanceUntilIdle()

        viewModel.currentAssignment.test {
            val assignment = awaitItem()
            assertEquals("Updated Title", assignment?.title)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadRecentAssignment_withInProgressAssignments_returnsNull() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.IN_PROGRESS,
                solvedNum = 5,
                startedAt = "2025-01-01",
                submittedAt = null,
            ),
        )

        Mockito.`when`(assignmentRepository.getRecentPersonalAssignment(1))
            .thenReturn(Result.success(2))
        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId = 1))
            .thenReturn(Result.success(personalAssignments))

        viewModel.loadRecentAssignment(1)
        advanceUntilIdle()

        viewModel.recentAssignment.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadRecentAssignment_failure_setsNull() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        Mockito.`when`(assignmentRepository.getRecentPersonalAssignment(1))
            .thenReturn(Result.failure(Exception("Network error")))

        viewModel.loadRecentAssignment(1)
        advanceUntilIdle()

        viewModel.recentAssignment.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun cancelQuestionGeneration_withAssignmentId_updatesAssignment() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        val field = AssignmentViewModel::class.java.getDeclaredField("generatingAssignmentId")
        field.isAccessible = true
        field.set(viewModel, 1)

        val updateRequest = UpdateAssignmentRequest.builder()
            .totalQuestions(0)
            .build()

        Mockito.`when`(assignmentRepository.updateAssignment(1, updateRequest))
            .thenReturn(Result.success(buildAssignment(1)))

        viewModel.cancelQuestionGeneration()
        advanceUntilIdle()

        assertTrue(viewModel.questionGenerationCancelled.value)
    }

    @Test
    fun loadPersonalAssignmentQuestions_alreadyLoaded_returnsEarly() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val questions = listOf(
            PersonalAssignmentQuestion(
                id = 1,
                number = "1",
                question = "Question 1",
                answer = "Answer 1",
                explanation = "Explanation",
                difficulty = "easy",
                isProcessing = false,
            ),
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignmentQuestions(1))
            .thenReturn(Result.success(questions))
        viewModel.loadPersonalAssignmentQuestions(1)
        advanceUntilIdle()

        viewModel.loadPersonalAssignmentQuestions(1)
        advanceUntilIdle()

        Mockito.verify(assignmentRepository, times(1)).getPersonalAssignmentQuestions(1)
    }

    @Test
    fun loadPersonalAssignmentQuestions_whileLoading_returnsEarly() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        Mockito.`when`(assignmentRepository.getPersonalAssignmentQuestions(1))
            .thenReturn(Result.success(emptyList()))

        viewModel.loadPersonalAssignmentQuestions(1)
        viewModel.loadPersonalAssignmentQuestions(2)
        advanceUntilIdle()

        assertTrue(true)
    }

    @Test
    fun loadAllQuestions_whileLoading_returnsEarly() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val baseQuestions = listOf(
            PersonalAssignmentQuestion(
                1,
                "1",
                "Q1",
                "A1",
                "E1",
                "easy",
                false,
            ),
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignmentQuestions(1))
            .thenReturn(Result.success(baseQuestions))
        Mockito.`when`(assignmentRepository.getNextQuestion(1))
            .thenReturn(
                Result.success(
                    PersonalAssignmentQuestion(
                        1,
                        "1",
                        "Q1",
                        "A1",
                        "E1",
                        "easy",
                        false,
                    ),
                ),
            )

        viewModel.loadAllQuestions(1)
        viewModel.loadAllQuestions(1)
        advanceUntilIdle()

        assertTrue(true)
    }

    @Test
    fun loadNextQuestion_whileLoading_returnsEarly() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val question = PersonalAssignmentQuestion(
            1,
            "1",
            "Q1",
            "A1",
            "E1",
            "easy",
            false,
        )

        Mockito.`when`(assignmentRepository.getNextQuestion(1))
            .thenReturn(Result.success(question))

        viewModel.loadNextQuestion(1)
        viewModel.loadNextQuestion(1)
        advanceUntilIdle()

        assertTrue(true)
    }

    @Test
    fun loadNextQuestion_isProcessing_setsProcessingTrue() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val question = PersonalAssignmentQuestion(
            id = 1,
            number = "1",
            question = "Question 1",
            answer = "Answer 1",
            explanation = "Explanation",
            difficulty = "easy",
            isProcessing = true,
        )

        Mockito.`when`(assignmentRepository.getNextQuestion(1))
            .thenReturn(Result.success(question))

        viewModel.loadNextQuestion(1)
        advanceUntilIdle()

        assertTrue(viewModel.isProcessing.value)
    }

    @Test
    fun loadNextQuestion_noMoreQuestions_checksStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val statistics = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 10,
            correctAnswers = 8,
            accuracy = 0.8f,
            totalProblem = 10,
            solvedProblem = 10,
            progress = 1.0f,
            averageScore = 0.8f,
        )

        Mockito.`when`(assignmentRepository.getNextQuestion(1))
            .thenReturn(Result.failure(Exception("모든 문제를 완료했습니다")))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.success(statistics))

        viewModel.loadNextQuestion(1)
        advanceUntilIdle()

        viewModel.personalAssignmentQuestions.test {
            val questions = awaitItem()
            assertTrue(questions.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadNextQuestion_statisticsFailure_setsError() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        Mockito.`when`(assignmentRepository.getNextQuestion(1))
            .thenReturn(Result.failure(Exception("모든 문제를 완료했습니다")))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.failure(Exception("Stats error")))

        viewModel.loadNextQuestion(1)
        advanceUntilIdle()

        viewModel.error.test {
            val error = awaitItem()
            assert(error?.contains("통계를 확인할 수 없습니다") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAssignmentSubmissionStats_exceptionInCatch_returnsZeroStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = 1))
            .thenThrow(RuntimeException("Unexpected error"))

        val result = viewModel.getAssignmentSubmissionStats(1)

        assertEquals(0, result.submittedStudents)
        assertEquals(0, result.totalStudents)
        assertEquals(0, result.averageScore)
        assertEquals(0, result.completionRate)
    }

    @Test
    fun loadAssignmentCorrectnessFor_failureSilent_doesNotSetError() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId = 1, assignmentId = 1))
            .thenReturn(Result.failure(Exception("Network error")))

        viewModel.loadAssignmentCorrectnessFor(1, 1, silent = true)
        advanceUntilIdle()

        assertNull(viewModel.error.value)
    }

    @Test
    fun loadAssignmentCorrectnessFor_correctnessFailureSilent_doesNotSetError() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.IN_PROGRESS,
                solvedNum = 5,
                startedAt = "2025-01-01",
                submittedAt = null,
            ),
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId = 1, assignmentId = 1))
            .thenReturn(Result.success(personalAssignments))
        Mockito.`when`(assignmentRepository.getAssignmentCorrectness(1))
            .thenReturn(Result.failure(Exception("Correctness error")))

        viewModel.loadAssignmentCorrectnessFor(1, 1, silent = true)
        advanceUntilIdle()

        assertNull(viewModel.error.value)
    }

    @Test
    fun loadPersonalAssignmentStatsAndCorrectness_failureSilent_doesNotSetError() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId = 1, assignmentId = 1))
            .thenReturn(Result.failure(Exception("Network error")))

        viewModel.loadPersonalAssignmentStatsAndCorrectness(1, 1, silent = true)
        advanceUntilIdle()

        assertNull(viewModel.error.value)
    }

    @Test
    fun completeAssignment_success_callsLoadAssignmentStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignment = buildAssignment(1)
        val personalAssignment = PersonalAssignmentData(
            id = 1,
            student = StudentInfo(1, "Student1", "s1@test.com"),
            assignment = PersonalAssignmentInfo(
                1,
                "Assignment 1",
                "Description",
                10,
                "2025-01-01",
                "Grade 1",
            ),
            status = PersonalAssignmentStatus.SUBMITTED,
            solvedNum = 10,
            startedAt = "2025-01-01",
            submittedAt = "2025-01-02",
        )

        viewModel.setInitialAssignments(listOf(assignment))
        runCurrent()

        Mockito.`when`(assignmentRepository.getAssignmentById(1))
            .thenReturn(Result.success(assignment))
        viewModel.loadAssignmentById(1)
        runCurrent()

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = null))
            .thenReturn(Result.success(listOf(personalAssignment)))
        Mockito.`when`(assignmentRepository.completePersonalAssignment(1))
            .thenReturn(Result.success(Unit))
        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = 1))
            .thenReturn(Result.success(emptyList()))

        viewModel.completeAssignment(1)
        advanceUntilIdle()

        assertTrue(true)
    }

    @Test
    fun completeAssignment_failure_setsError() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = null))
            .thenReturn(Result.success(emptyList()))
        Mockito.`when`(assignmentRepository.completePersonalAssignment(1))
            .thenReturn(Result.failure(Exception("Complete failed")))

        viewModel.completeAssignment(1)
        advanceUntilIdle()

        viewModel.error.test {
            val error = awaitItem()
            assert(error != null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatistics_emptyAverageScore_setsZero() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.SUBMITTED,
                solvedNum = 10,
                startedAt = "2025-01-01",
                submittedAt = "2025-01-02",
            ),
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = 1))
            .thenReturn(Result.success(personalAssignments))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.failure(Exception("Statistics not found")))

        viewModel.loadAssignmentStatistics(1, 10)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            if (stats != null) {
                assertEquals(0, stats.averageScore)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatistics_zeroTotalStudents_setsCompletionRateZero() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = 1))
            .thenReturn(Result.success(emptyList()))

        viewModel.loadAssignmentStatistics(1, 0)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            if (stats != null) {
                assertEquals(0, stats.completionRate)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getAssignmentSubmissionStats_notStartedStatus_returnsCorrectStatus() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.NOT_STARTED,
                solvedNum = 0,
                startedAt = null,
                submittedAt = null,
            ),
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = 1))
            .thenReturn(Result.success(personalAssignments))

        viewModel.loadAssignmentStudentResults(1)
        advanceUntilIdle()

        viewModel.assignmentResults.test {
            val results = awaitItem()
            if (results.isNotEmpty()) {
                assertEquals("미시작", results[0].status)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadStudentAssignments_submittedFilter_loadsCompletedAssignments() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val allAssignments = listOf(buildAssignment(1), buildAssignment(2))

        Mockito.`when`(assignmentRepository.getAllAssignments())
            .thenReturn(Result.success(allAssignments))

        viewModel.loadStudentAssignments(1)
        advanceUntilIdle()

        viewModel.assignments.test {
            val assignments = awaitItem()
            assertTrue(assignments.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadStudentAssignments_pendingFilter_getAssignmentByIdFailure_addsDefaultCourseClass() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val allAssignments = listOf(buildAssignment(1))

        Mockito.`when`(assignmentRepository.getAllAssignments())
            .thenReturn(Result.success(allAssignments))

        viewModel.loadStudentAssignments(1)
        advanceUntilIdle()

        viewModel.assignments.test {
            val assignments = awaitItem()
            assertTrue(assignments.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadStudentAssignments_completedFilter_getAssignmentByIdFailure_addsDefaultCourseClass() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val allAssignments = listOf(buildAssignment(1))

        Mockito.`when`(assignmentRepository.getAllAssignments())
            .thenReturn(Result.success(allAssignments))

        viewModel.loadStudentAssignments(1)
        advanceUntilIdle()

        viewModel.assignments.test {
            val assignments = awaitItem()
            assertTrue(assignments.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatistics_withSubmittedCount_calculatesCorrectly() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val totalStudents = 10
        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.SUBMITTED,
                solvedNum = 10,
                startedAt = "2025-01-01",
                submittedAt = "2025-01-02",
            ),
            PersonalAssignmentData(
                id = 2,
                student = StudentInfo(2, "Student2", "s2@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.SUBMITTED,
                solvedNum = 10,
                startedAt = "2025-01-01",
                submittedAt = "2025-01-02",
            ),
        )
        val statistics1 = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 10,
            correctAnswers = 9,
            accuracy = 0.9f,
            totalProblem = 10,
            solvedProblem = 10,
            progress = 1.0f,
            averageScore = 0.9f,
        )
        val statistics2 = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 10,
            correctAnswers = 7,
            accuracy = 0.7f,
            totalProblem = 10,
            solvedProblem = 10,
            progress = 1.0f,
            averageScore = 0.7f,
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.success(personalAssignments))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.success(statistics1))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(2))
            .thenReturn(Result.success(statistics2))

        viewModel.loadAssignmentStatistics(assignmentId, totalStudents)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            if (stats != null) {
                assertEquals(2, stats.submittedStudents)
                assertEquals(2, stats.totalStudents)
                assertEquals(0, stats.averageScore)
                assertEquals(100, stats.completionRate)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAllQuestions_allQuestionsCompleted_completesAssignment() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val baseQuestions = listOf(
            PersonalAssignmentQuestion(
                1,
                "1",
                "Q1",
                "A1",
                "E1",
                "easy",
                false,
            ),
        )
        val statistics = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 10,
            correctAnswers = 10,
            accuracy = 1.0f,
            totalProblem = 10,
            solvedProblem = 10,
            progress = 1.0f,
            averageScore = 1.0f,
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignmentQuestions(1))
            .thenReturn(Result.success(baseQuestions))
        Mockito.`when`(assignmentRepository.getNextQuestion(1))
            .thenReturn(Result.failure(Exception("모든 문제를 완료했습니다")))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.success(statistics))
        Mockito.`when`(assignmentRepository.completePersonalAssignment(1))
            .thenReturn(Result.success(Unit))
        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = null))
            .thenReturn(Result.success(emptyList()))

        viewModel.loadAllQuestions(1)
        advanceUntilIdle()

        Mockito.verify(assignmentRepository, times(1)).completePersonalAssignment(1)
    }

    @Test
    fun submitAnswer_statisticsCheckFailure_doesNotSetError() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val audioFile = File("test.wav")
        val response = AnswerSubmissionResponse(
            isCorrect = true,
            numberStr = "Correct",
            tailQuestion = null,
        )

        Mockito.`when`(assignmentRepository.submitAnswer(1, 1, 1, audioFile))
            .thenReturn(Result.success(response))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.failure(Exception("Statistics error")))

        viewModel.submitAnswer(1, 1, 1, audioFile)
        advanceUntilIdle()

        viewModel.error.test {
            val error = awaitItem()
            assertNull(error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun submitAnswer_reloadStatisticsFailure_doesNotThrowException() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val audioFile = File("test.wav")
        val response = AnswerSubmissionResponse(
            isCorrect = true,
            numberStr = "Correct",
            tailQuestion = null,
        )

        Mockito.`when`(assignmentRepository.submitAnswer(1, 1, 1, audioFile))
            .thenReturn(Result.success(response))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.failure(Exception("Reload statistics error")))

        viewModel.submitAnswer(1, 1, 1, audioFile)
        advanceUntilIdle()

        assertTrue(true)
    }

    @Test
    fun loadAssignmentResult_success_updatesStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val resultData = AssignmentResultData(
            submittedStudents = 5,
            totalStudents = 10,
            averageScore = 85.5,
            completionRate = 50.0,
        )

        val updateRequest = UpdateAssignmentRequest.builder()
            .title("Updated Title")
            .build()
        val updatedAssignment = buildAssignment(1).copy(title = "Updated Title")

        Mockito.`when`(assignmentRepository.updateAssignment(1, updateRequest))
            .thenReturn(Result.success(updatedAssignment))
        Mockito.`when`(assignmentRepository.getAssignmentResult(assignmentId))
            .thenReturn(Result.success(resultData))

        viewModel.updateAssignment(1, updateRequest)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            if (stats != null) {
                assertEquals(5, stats.submittedStudents)
                assertEquals(10, stats.totalStudents)
                assertEquals(85, stats.averageScore)
                assertEquals(50, stats.completionRate)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentResult_failure_callsLoadAssignmentStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val updatedAssignment = buildAssignment(1).copy(title = "Updated Title")
        val updateRequest = UpdateAssignmentRequest.builder()
            .title("Updated Title")
            .build()

        Mockito.`when`(assignmentRepository.updateAssignment(1, updateRequest))
            .thenReturn(Result.success(updatedAssignment))
        Mockito.`when`(assignmentRepository.getAssignmentResult(assignmentId))
            .thenReturn(Result.failure(Exception("Result not found")))
        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.success(emptyList()))

        viewModel.updateAssignment(1, updateRequest)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            assert(stats != null)
            if (stats != null) {
                assertEquals(0, stats.submittedStudents)
                assertEquals(0, stats.totalStudents)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun uploadPdfToS3_success_setsSuccessStateAndTriggersQuestionGeneration() = runTest {
        val standardDispatcher = StandardTestDispatcher()
        val originalDispatcher = mainDispatcherRule.testDispatcher
        try {
            Dispatchers.setMain(standardDispatcher)

            val createRequest = CreateAssignmentRequest(
                title = "Test Assignment",
                description = "Description",
                total_questions = 10,
                due_at = "2025-01-01",
                class_id = 1,
                grade = "Grade 1",
                subject = "Math",
            )
            val createResponse = CreateAssignmentResponse(
                assignment_id = 1,
                material_id = 10,
                s3_key = "some-key",
                upload_url = "https://dummy-upload",
            )
            val pdfFile = File.createTempFile("test", ".pdf")
            val viewModel = AssignmentViewModel(assignmentRepository)

            Mockito.`when`(assignmentRepository.createAssignment(createRequest))
                .thenReturn(Result.success(createResponse))
            Mockito.`when`(assignmentRepository.uploadPdfToS3(anyString(), kotlinAny<File>()))
                .thenReturn(Result.success(true))
            Mockito.`when`(assignmentRepository.createQuestionsAfterUpload(anyInt(), anyInt(), anyInt()))
                .thenReturn(Result.success(Unit))
            Mockito.`when`(assignmentRepository.getAllAssignments(anyString(), isNull(), isNull()))
                .thenReturn(Result.success(emptyList()))
            Mockito.`when`(assignmentRepository.getAllAssignments(isNull(), isNull(), isNull()))
                .thenReturn(Result.success(emptyList()))

            viewModel.createAssignmentWithPdf(
                assignment = createRequest,
                pdfFile = pdfFile,
                totalNumber = 10,
                teacherId = "123",
            )

            viewModel.uploadSuccess.test {
                awaitItem()
                runCurrent()
                val success = awaitItem()
                assertTrue(success)
                cancelAndIgnoreRemainingEvents()
            }

            runCurrent()
            advanceUntilIdle()

            assertFalse(viewModel.isUploading.value)
            assertFalse(viewModel.isCreatingAssignment.value)

            viewModel.questionGenerationSuccess.test {
                awaitItem()
                runCurrent()
                advanceUntilIdle()
                val success = awaitItem()
                assertTrue(success)
                cancelAndIgnoreRemainingEvents()
            }

            runCurrent()
            advanceUntilIdle()
        } finally {
            Dispatchers.setMain(originalDispatcher)
        }
    }

    @Test
    fun loadPendingStudentAssignments_getAssignmentByIdFailure_createsAssignmentWithDefaultCourseClass() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val studentId = 1
        val assignmentId = 100

        val pendingPersonalAssignment = PersonalAssignmentData(
            id = 1,
            student = StudentInfo(1, "Student1", "s1@test.com"),
            assignment = PersonalAssignmentInfo(
                assignmentId,
                "Pending Assignment",
                "Description",
                10,
                "2025-01-01",
                "Grade 1",
            ),
            status = PersonalAssignmentStatus.IN_PROGRESS,
            solvedNum = 5,
            startedAt = "2025-01-01",
            submittedAt = null,
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId))
            .thenReturn(Result.success(listOf(pendingPersonalAssignment)))

        Mockito.`when`(assignmentRepository.getAssignmentById(assignmentId))
            .thenReturn(Result.failure(Exception("Assignment not found")))

        viewModel.loadPendingStudentAssignments(studentId)
        advanceUntilIdle()

        viewModel.assignments.test {
            val assignments = awaitItem()
            assertTrue(assignments.isNotEmpty())

            val assignment = assignments.first()
            assertEquals(assignmentId, assignment.id)
            assertEquals("Pending Assignment", assignment.title)
            assertEquals(null, assignment.createdAt)
            assertEquals(
                CourseClass(
                    id = 0,
                    name = "",
                    description = null,
                    subject = Subject(id = 0, name = "", code = null),
                    teacherName = "",
                    studentCount = 0,
                    createdAt = "",
                ),
                assignment.courseClass,
            )
            assertEquals(null, assignment.materials)
            assertEquals(pendingPersonalAssignment.assignment.grade, assignment.grade)
            assertEquals(pendingPersonalAssignment.status, assignment.personalAssignmentStatus)
            assertEquals(pendingPersonalAssignment.solvedNum, assignment.solvedNum)
            assertEquals(pendingPersonalAssignment.id, assignment.personalAssignmentId)

            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(assignmentRepository, times(1)).getAssignmentById(assignmentId)
    }

    @Test
    fun loadCompletedStudentAssignments_getAssignmentByIdFailure_createsAssignmentWithDefaultCourseClass() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val studentId = 1
        val assignmentId = 200

        val completedPersonalAssignment = PersonalAssignmentData(
            id = 2,
            student = StudentInfo(1, "Student1", "s1@test.com"),
            assignment = PersonalAssignmentInfo(
                assignmentId,
                "Completed Assignment",
                "Description",
                10,
                "2025-01-01",
                "Grade 1",
            ),
            status = PersonalAssignmentStatus.SUBMITTED,
            solvedNum = 10,
            startedAt = "2025-01-01",
            submittedAt = "2025-01-02",
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId))
            .thenReturn(Result.success(listOf(completedPersonalAssignment)))

        Mockito.`when`(assignmentRepository.getAssignmentById(assignmentId))
            .thenReturn(Result.failure(Exception("Assignment not found")))

        viewModel.loadCompletedStudentAssignments(studentId)
        advanceUntilIdle()

        viewModel.assignments.test {
            val assignments = awaitItem()
            assertTrue(assignments.isNotEmpty())

            val assignment = assignments.first()
            assertEquals(assignmentId, assignment.id)
            assertEquals("Completed Assignment", assignment.title)
            assertEquals(null, assignment.createdAt)
            assertEquals(
                CourseClass(
                    id = 0,
                    name = "",
                    description = null,
                    subject = Subject(id = 0, name = "", code = null),
                    teacherName = "",
                    studentCount = 0,
                    createdAt = "",
                ),
                assignment.courseClass,
            )
            assertEquals(null, assignment.materials)
            assertEquals(completedPersonalAssignment.assignment.grade, assignment.grade)
            assertEquals(completedPersonalAssignment.status, assignment.personalAssignmentStatus)
            assertEquals(completedPersonalAssignment.solvedNum, assignment.solvedNum)
            assertEquals(completedPersonalAssignment.id, assignment.personalAssignmentId)
            assertEquals(completedPersonalAssignment.submittedAt, assignment.submittedAt)

            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(assignmentRepository, times(1)).getAssignmentById(assignmentId)
    }

    @Test
    fun loadAssignmentStatisticsAndResults_submittedCountGreaterThanZero_calculatesStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val totalStudents = 10

        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.SUBMITTED,
                solvedNum = 10,
                startedAt = "2025-01-01",
                submittedAt = "2025-01-02",
            ),
            PersonalAssignmentData(
                id = 2,
                student = StudentInfo(2, "Student2", "s2@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Assignment 1",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.SUBMITTED,
                solvedNum = 10,
                startedAt = "2025-01-01",
                submittedAt = "2025-01-02",
            ),
        )

        val statistics1 = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 10,
            correctAnswers = 9,
            accuracy = 0.9f,
            totalProblem = 10,
            solvedProblem = 10,
            progress = 1.0f,
            averageScore = 0.9f,
        )
        val statistics2 = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 10,
            correctAnswers = 7,
            accuracy = 0.7f,
            totalProblem = 10,
            solvedProblem = 10,
            progress = 1.0f,
            averageScore = 0.7f,
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.success(personalAssignments))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.success(statistics1))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(2))
            .thenReturn(Result.success(statistics2))

        viewModel.loadAssignmentStatisticsAndResults(assignmentId, totalStudents)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            assertNotNull(stats)
            if (stats != null) {
                assertEquals(2, stats.submittedStudents)
                assertEquals(2, stats.totalStudents)
                assertEquals(0, stats.averageScore)
                assertEquals(100, stats.completionRate)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatisticsAndResults_getPersonalAssignmentsFailure_setsErrorAndDefaultStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val totalStudents = 10

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.failure(Exception("Network error")))

        viewModel.loadAssignmentStatisticsAndResults(assignmentId, totalStudents)
        advanceUntilIdle()

        viewModel.error.test {
            val error = awaitItem()
            assertNotNull(error)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.assignmentResults.test {
            val results = awaitItem()
            assertTrue(results.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            assertNotNull(stats)
            if (stats != null) {
                assertEquals(0, stats.submittedStudents)
                assertEquals(totalStudents, stats.totalStudents)
                assertEquals(0, stats.averageScore)
                assertEquals(0, stats.completionRate)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatisticsAndResults_exceptionInTryBlock_setsErrorAndDefaultStatistics() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val totalStudents = 10

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenThrow(RuntimeException("Unexpected error"))

        viewModel.loadAssignmentStatisticsAndResults(assignmentId, totalStudents)
        advanceUntilIdle()

        // sets error and default statistics
        viewModel.error.test {
            val error = awaitItem()
            assertNotNull(error)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.assignmentResults.test {
            val results = awaitItem()
            assertTrue(results.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            assertNotNull(stats)
            if (stats != null) {
                assertEquals(0, stats.submittedStudents)
                assertEquals(totalStudents, stats.totalStudents)
                assertEquals(0, stats.averageScore)
                assertEquals(0, stats.completionRate)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatisticsAndResults_notStartedStatus_setsStatusToNotStarted() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val totalStudents = 10

        val personalAssignment = PersonalAssignmentData(
            id = 1,
            student = StudentInfo(1, "Student1", "s1@test.com"),
            assignment = PersonalAssignmentInfo(
                1,
                "Assignment 1",
                "Description",
                10,
                "2025-01-01",
                "Grade 1",
            ),
            status = PersonalAssignmentStatus.NOT_STARTED,
            solvedNum = 0,
            startedAt = null,
            submittedAt = null,
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.success(listOf(personalAssignment)))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(
                Result.success(
                    PersonalAssignmentStatistics(
                        totalQuestions = 10,
                        answeredQuestions = 0,
                        correctAnswers = 0,
                        accuracy = 0f,
                        totalProblem = 10,
                        solvedProblem = 0,
                        progress = 0f,
                        averageScore = 0f,
                    ),
                ),
            )

        viewModel.loadAssignmentStatisticsAndResults(assignmentId, totalStudents)
        advanceUntilIdle()

        viewModel.assignmentResults.test {
            val results = awaitItem()
            assertTrue(results.isNotEmpty())
            assertEquals("미시작", results[0].status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatisticsAndResults_completedBySubmittedAt_setsAsCompleted() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val totalStudents = 10

        val personalAssignment = PersonalAssignmentData(
            id = 1,
            student = StudentInfo(1, "Student1", "s1@test.com"),
            assignment = PersonalAssignmentInfo(
                1,
                "Assignment 1",
                "Description",
                10,
                "2025-01-01",
                "Grade 1",
            ),
            status = PersonalAssignmentStatus.IN_PROGRESS,
            solvedNum = 5,
            startedAt = "2025-01-01",
            submittedAt = "2025-01-02",
        )

        val statistics = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 5,
            correctAnswers = 4,
            accuracy = 0.8f,
            totalProblem = 10,
            solvedProblem = 5,
            progress = 0.5f,
            averageScore = 0.8f,
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.success(listOf(personalAssignment)))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.success(statistics))

        viewModel.loadAssignmentStatisticsAndResults(assignmentId, totalStudents)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            assertNotNull(stats)
            if (stats != null) {
                assertEquals(1, stats.submittedStudents)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatisticsAndResults_completedBySolvedNum_setsAsCompleted() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val totalStudents = 10

        val personalAssignment = PersonalAssignmentData(
            id = 1,
            student = StudentInfo(1, "Student1", "s1@test.com"),
            assignment = PersonalAssignmentInfo(
                1,
                "Assignment 1",
                "Description",
                10,
                "2025-01-01",
                "Grade 1",
            ),
            status = PersonalAssignmentStatus.IN_PROGRESS,
            solvedNum = 10,
            startedAt = "2025-01-01",
            submittedAt = null,
        )

        val statistics = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 10,
            correctAnswers = 9,
            accuracy = 0.9f,
            totalProblem = 10,
            solvedProblem = 9,
            progress = 0.9f,
            averageScore = 0.9f,
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.success(listOf(personalAssignment)))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.success(statistics))

        viewModel.loadAssignmentStatisticsAndResults(assignmentId, totalStudents)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            assertNotNull(stats)
            if (stats != null) {
                assertEquals(1, stats.submittedStudents)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatisticsAndResults_completedByTotalProblemEqualsSolvedProblem_setsAsCompleted() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val totalStudents = 10

        val personalAssignment = PersonalAssignmentData(
            id = 1,
            student = StudentInfo(1, "Student1", "s1@test.com"),
            assignment = PersonalAssignmentInfo(
                1,
                "Assignment 1",
                "Description",
                10,
                "2025-01-01",
                "Grade 1",
            ),
            status = PersonalAssignmentStatus.IN_PROGRESS,
            solvedNum = 9,
            startedAt = "2025-01-01",
            submittedAt = null,
        )

        val statistics = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 10,
            correctAnswers = 9,
            accuracy = 0.9f,
            totalProblem = 10,
            solvedProblem = 10,
            progress = 1.0f,
            averageScore = 0.9f,
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.success(listOf(personalAssignment)))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.success(statistics))

        viewModel.loadAssignmentStatisticsAndResults(assignmentId, totalStudents)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            assertNotNull(stats)
            if (stats != null) {
                assertEquals(1, stats.submittedStudents)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentStatisticsAndResults_completedByAnsweredQuestions_setsAsCompleted() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1
        val totalStudents = 10

        val personalAssignment = PersonalAssignmentData(
            id = 1,
            student = StudentInfo(1, "Student1", "s1@test.com"),
            assignment = PersonalAssignmentInfo(
                1,
                "Assignment 1",
                "Description",
                10,
                "2025-01-01",
                "Grade 1",
            ),
            status = PersonalAssignmentStatus.IN_PROGRESS,
            solvedNum = 8,
            startedAt = "2025-01-01",
            submittedAt = null,
        )

        val statistics = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 10,
            correctAnswers = 8,
            accuracy = 0.8f,
            totalProblem = 10,
            solvedProblem = 8,
            progress = 0.8f,
            averageScore = 0.8f,
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = assignmentId))
            .thenReturn(Result.success(listOf(personalAssignment)))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(1))
            .thenReturn(Result.success(statistics))

        viewModel.loadAssignmentStatisticsAndResults(assignmentId, totalStudents)
        advanceUntilIdle()

        viewModel.assignmentStatistics.test {
            val stats = awaitItem()
            assertNotNull(stats)
            if (stats != null) {
                assertEquals(1, stats.submittedStudents)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadStudentAssignmentsWithPersonalFilter_submittedFilter_filtersSubmittedAssignments() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val studentId = 1

        val personalAssignments = listOf(
            PersonalAssignmentData(
                id = 1,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    1,
                    "Submitted Assignment",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.SUBMITTED,
                solvedNum = 10,
                startedAt = "2025-01-01",
                submittedAt = "2025-01-02",
            ),
            PersonalAssignmentData(
                id = 2,
                student = StudentInfo(1, "Student1", "s1@test.com"),
                assignment = PersonalAssignmentInfo(
                    2,
                    "In Progress Assignment",
                    "Description",
                    10,
                    "2025-01-01",
                    "Grade 1",
                ),
                status = PersonalAssignmentStatus.IN_PROGRESS,
                solvedNum = 5,
                startedAt = "2025-01-01",
                submittedAt = null,
            ),
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId))
            .thenReturn(Result.success(personalAssignments))

        viewModel.loadStudentAssignmentsWithPersonalFilter(studentId, PersonalAssignmentFilter.SUBMITTED)
        advanceUntilIdle()

        viewModel.assignments.test {
            val assignments = awaitItem()
            assertEquals(1, assignments.size)
            assertEquals("Submitted Assignment", assignments[0].title)
            assertEquals(PersonalAssignmentStatus.SUBMITTED, assignments[0].personalAssignmentStatus)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun cancelQuestionGeneration_updateAssignmentFailure_setsGeneratingAssignmentIdToNull() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1

        val field = AssignmentViewModel::class.java.getDeclaredField("generatingAssignmentId")
        field.isAccessible = true
        field.set(viewModel, assignmentId)

        Mockito.`when`(assignmentRepository.updateAssignment(eq(assignmentId), kotlinAny()))
            .thenReturn(Result.failure(Exception("Update failed")))

        viewModel.cancelQuestionGeneration()
        advanceUntilIdle()

        viewModel.questionGenerationCancelled.test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun cancelQuestionGeneration_updateAssignmentThrowsException_setsGeneratingAssignmentIdToNull() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignmentId = 1

        val field = AssignmentViewModel::class.java.getDeclaredField("generatingAssignmentId")
        field.isAccessible = true
        field.set(viewModel, assignmentId)

        Mockito.`when`(assignmentRepository.updateAssignment(eq(assignmentId), kotlinAny()))
            .thenThrow(RuntimeException("Update exception"))

        viewModel.cancelQuestionGeneration()
        advanceUntilIdle()

        viewModel.questionGenerationCancelled.test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadRecentAssignment_getPersonalAssignmentsFailure_setsRecentAssignmentToNull() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val studentId = 1
        val personalAssignmentId = 1

        Mockito.`when`(assignmentRepository.getRecentPersonalAssignment(studentId))
            .thenReturn(Result.success(personalAssignmentId))
        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId))
            .thenReturn(Result.failure(Exception("Network error")))

        viewModel.loadRecentAssignment(studentId)
        advanceUntilIdle()

        viewModel.recentAssignment.test {
            assertNull(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAllQuestions_getPersonalAssignmentStatisticsFailure_setsError() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val personalAssignmentId = 1

        val baseQuestions = listOf(
            PersonalAssignmentQuestion(
                id = 1,
                number = "1",
                question = "Question 1",
                answer = "Answer 1",
                explanation = "Explanation 1",
                isProcessing = false,
                difficulty = "Easy",
            ),
        )

        Mockito.`when`(assignmentRepository.getPersonalAssignmentQuestions(personalAssignmentId))
            .thenReturn(Result.success(baseQuestions))
        Mockito.`when`(assignmentRepository.getNextQuestion(personalAssignmentId))
            .thenReturn(Result.failure(Exception("모든 문제를 완료했습니다")))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(personalAssignmentId))
            .thenReturn(Result.failure(Exception("Statistics error")))

        viewModel.loadAllQuestions(personalAssignmentId)
        advanceUntilIdle()

        viewModel.error.test {
            val error = awaitItem()
            assertNotNull(error)
            assertTrue(error?.contains("통계를 확인할 수 없습니다") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadNextQuestion_getNextQuestionFailureWithOtherError_setsError() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val personalAssignmentId = 1

        Mockito.`when`(assignmentRepository.getNextQuestion(personalAssignmentId))
            .thenReturn(Result.failure(Exception("Network error")))

        viewModel.loadNextQuestion(personalAssignmentId)
        advanceUntilIdle()

        viewModel.error.test {
            val error = awaitItem()
            assertNotNull(error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun completeAssignment_exceptionInTryBlock_setsError() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val personalAssignmentId = 1

        Mockito.`when`(assignmentRepository.getPersonalAssignments(assignmentId = null))
            .thenThrow(RuntimeException("Unexpected error"))

        viewModel.completeAssignment(personalAssignmentId)
        advanceUntilIdle()

        // sets error
        viewModel.error.test {
            val error = awaitItem()
            assertNotNull(error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun createAssignmentWithPdf_questionGenerationSuccessButRefreshFails_handlesException() = runTest {
        val standardDispatcher = StandardTestDispatcher()
        val originalDispatcher = mainDispatcherRule.testDispatcher
        try {
            Dispatchers.setMain(standardDispatcher)

            val viewModel = AssignmentViewModel(assignmentRepository)
            val assignment = CreateAssignmentRequest(
                title = "Test Assignment",
                subject = "Math",
                class_id = 1,
                due_at = "2025-12-31T23:59:59Z",
                grade = "1학년",
                description = "Test",
                total_questions = 5,
            )
            val pdfFile = File.createTempFile("test", ".pdf")
            pdfFile.deleteOnExit()

            val createResponse = CreateAssignmentResponse(
                assignment_id = 1,
                material_id = 1,
                s3_key = "test-key",
                upload_url = "https://example.com/upload",
            )

            Mockito.`when`(assignmentRepository.createAssignment(assignment))
                .thenReturn(Result.success(createResponse))

            Mockito.`when`(assignmentRepository.uploadPdfToS3(anyString(), kotlinAny()))
                .thenReturn(Result.success(true))

            Mockito.`when`(assignmentRepository.createQuestionsAfterUpload(anyInt(), anyInt(), anyInt()))
                .thenReturn(Result.success(Unit))

            Mockito.`when`(assignmentRepository.getAllAssignments(nullable(String::class.java), isNull(), isNull()))
                .thenReturn(Result.failure(RuntimeException("Refresh failed")))

            viewModel.createAssignmentWithPdf(assignment, pdfFile, 5)
            runCurrent()
            advanceUntilIdle()

            viewModel.uploadSuccess.test {
                skipItems(1)
                runCurrent()
                val success = awaitItem()
                assertTrue(success)
                cancelAndIgnoreRemainingEvents()
            }

            runCurrent()
            advanceUntilIdle()

            viewModel.error.test {
                val error = awaitItem()
                if (error == null) {
                    runCurrent()
                    advanceUntilIdle()
                    val nextError = awaitItem()
                    assertNotNull(nextError)
                } else {
                    assertNotNull(error)
                }
                cancelAndIgnoreRemainingEvents()
            }
        } finally {
            Dispatchers.setMain(originalDispatcher)
        }
    }

    @Test
    fun createAssignmentWithPdf_questionGenerationFailure_setsError() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignment = CreateAssignmentRequest(
            title = "Test Assignment",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Test",
            total_questions = 5,
        )
        val pdfFile = File.createTempFile("test", ".pdf")
        pdfFile.deleteOnExit()

        val createResponse = CreateAssignmentResponse(
            assignment_id = 1,
            material_id = 1,
            s3_key = "test-key",
            upload_url = "https://example.com/upload",
        )

        Mockito.`when`(assignmentRepository.createAssignment(assignment))
            .thenReturn(Result.success(createResponse))

        Mockito.`when`(assignmentRepository.uploadPdfToS3(anyString(), kotlinAny()))
            .thenReturn(Result.success(true))

        Mockito.`when`(assignmentRepository.createQuestionsAfterUpload(anyInt(), anyInt(), anyInt()))
            .thenReturn(Result.failure(Exception("Question generation failed")))

        Mockito.`when`(assignmentRepository.getAllAssignments(nullable(String::class.java), isNull(), isNull()))
            .thenReturn(Result.success(emptyList()))

        viewModel.createAssignmentWithPdf(assignment, pdfFile, 5)
        advanceUntilIdle()

        viewModel.questionGenerationError.test {
            val error = awaitItem()
            if (error == null) {
                val nextError = awaitItem()
                assertEquals("Question generation failed", nextError)
            } else {
                assertEquals("Question generation failed", error)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun createAssignmentWithPdf_questionGenerationThrowsException_setsError() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignment = CreateAssignmentRequest(
            title = "Test Assignment",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Test",
            total_questions = 5,
        )
        val pdfFile = File.createTempFile("test", ".pdf")
        pdfFile.deleteOnExit()

        val createResponse = CreateAssignmentResponse(
            assignment_id = 1,
            material_id = 1,
            s3_key = "test-key",
            upload_url = "https://example.com/upload",
        )

        Mockito.`when`(assignmentRepository.createAssignment(assignment))
            .thenReturn(Result.success(createResponse))

        Mockito.`when`(assignmentRepository.uploadPdfToS3(anyString(), kotlinAny()))
            .thenReturn(Result.success(true))

        Mockito.`when`(assignmentRepository.createQuestionsAfterUpload(anyInt(), anyInt(), anyInt()))
            .thenThrow(RuntimeException("Question generation exception"))

        Mockito.`when`(assignmentRepository.getAllAssignments(nullable(String::class.java), isNull(), isNull()))
            .thenReturn(Result.success(emptyList()))

        viewModel.createAssignmentWithPdf(assignment, pdfFile, 5)
        advanceUntilIdle()

        viewModel.questionGenerationError.test {
            val error = awaitItem()
            if (error == null) {
                val nextError = awaitItem()
                assertEquals("Question generation exception", nextError)
            } else {
                assertEquals("Question generation exception", error)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun createAssignmentWithPdf_topLevelException_setsError() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)
        val assignment = CreateAssignmentRequest(
            title = "Test Assignment",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Test",
            total_questions = 5,
        )
        val pdfFile = File.createTempFile("test", ".pdf")
        pdfFile.deleteOnExit()

        Mockito.`when`(assignmentRepository.createAssignment(kotlinAny()))
            .thenThrow(RuntimeException("Top level exception"))

        viewModel.createAssignmentWithPdf(assignment, pdfFile, 5)
        advanceUntilIdle()

        viewModel.error.test {
            val error = awaitItem()
            if (error == null) {
                val nextError = awaitItem()
                assertNotNull(nextError)
            } else {
                assertNotNull(error)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }
}
