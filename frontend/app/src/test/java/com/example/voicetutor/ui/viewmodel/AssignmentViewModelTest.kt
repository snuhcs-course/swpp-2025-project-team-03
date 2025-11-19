package com.example.voicetutor.ui.viewmodel

import app.cash.turbine.test
import com.example.voicetutor.data.models.AssignmentData
import com.example.voicetutor.data.models.AssignmentResultData
import com.example.voicetutor.data.models.AssignmentStatus
import com.example.voicetutor.data.models.CourseClass
import com.example.voicetutor.data.models.PersonalAssignmentStatistics
import com.example.voicetutor.data.models.Subject
import com.example.voicetutor.data.repository.AssignmentRepository
import com.example.voicetutor.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.junit.MockitoJUnitRunner

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
        // Given
        val viewModel = AssignmentViewModel(assignmentRepository)

        // When / Then
        viewModel.assignments.test {
            // Then
            // Initial emission is emptyList()
            assert(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAllAssignments_success_updatesAssignmentsAndLoading() = runTest {
        // Given
        val items = listOf(buildAssignment(1), buildAssignment(2))
        Mockito.`when`(assignmentRepository.getAllAssignments(null, null, null))
            .thenReturn(Result.success(items))

        val viewModel = AssignmentViewModel(assignmentRepository)

        // When
        viewModel.assignments.test {
            // initial
            awaitItem()
            viewModel.loadAllAssignments()

            // Allow coroutine to run
            runCurrent()

            // Then
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
        // Given
        val personalAssignmentId = 123
        Mockito.`when`(assignmentRepository.completePersonalAssignment(personalAssignmentId))
            .thenReturn(Result.success(Unit))

        val viewModel = AssignmentViewModel(assignmentRepository)

        // When / Then
        viewModel.isAssignmentCompleted.test {
            // initial false
            awaitItem()

            viewModel.completeAssignment(personalAssignmentId)

            // let repository answer and state update propagate
            advanceUntilIdle()

            // Then
            assert(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(assignmentRepository, times(1))
            .completePersonalAssignment(personalAssignmentId)
    }

    @Test
    fun loadStudentAssignments_success_updatesAssignmentsAndCalculatesStats() = runTest {
        // Given
        val assignments = listOf(buildAssignment(1), buildAssignment(2))
        Mockito.`when`(assignmentRepository.getAllAssignments())
            .thenReturn(Result.success(assignments))

        val viewModel = AssignmentViewModel(assignmentRepository)

        // When
        viewModel.assignments.test {
            awaitItem() // initial
            viewModel.loadStudentAssignments(studentId = 123)
            runCurrent()

            // Then
            val next = awaitItem()
            assert(next == assignments)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(assignmentRepository, times(1)).getAllAssignments()
    }

    @Test
    fun loadStudentAssignments_failure_setsError() = runTest {
        // Given
        Mockito.`when`(assignmentRepository.getAllAssignments())
            .thenReturn(Result.failure(Exception("Network error")))

        val viewModel = AssignmentViewModel(assignmentRepository)

        // When
        viewModel.error.test {
            awaitItem() // initial null
            viewModel.loadStudentAssignments(studentId = 123)
            runCurrent()

            // Then
            val error = awaitItem()
            assert(error?.contains("Network error") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentById_success_updatesCurrentAssignment() = runTest {
        // Given
        val assignment = buildAssignment(1)
        Mockito.`when`(assignmentRepository.getAssignmentById(1))
            .thenReturn(Result.success(assignment))
        Mockito.`when`(assignmentRepository.getAssignmentResult(1))
            .thenReturn(Result.success(AssignmentResultData(submittedStudents = 5, totalStudents = 10, averageScore = 80.0, completionRate = 50.0)))

        val viewModel = AssignmentViewModel(assignmentRepository)

        // When
        viewModel.currentAssignment.test {
            assert(awaitItem() == null)
            viewModel.loadAssignmentById(1)
            runCurrent()

            // Then
            assert(awaitItem() == assignment)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(assignmentRepository, times(1)).getAssignmentById(1)
        Mockito.verify(assignmentRepository, times(1)).getAssignmentResult(1)
    }

    @Test
    fun loadPersonalAssignmentStatistics_success_updatesStatistics() = runTest {
        // Given
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

        // When
        viewModel.personalAssignmentStatistics.test {
            assert(awaitItem() == null)
            viewModel.loadPersonalAssignmentStatistics(26)
            runCurrent()

            // Then
            assert(awaitItem() == statistics)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(assignmentRepository, times(1)).getPersonalAssignmentStatistics(26)
    }

    @Test
    fun setAssignmentCompleted_setsCompletedState() = runTest {
        // Given
        val viewModel = AssignmentViewModel(assignmentRepository)

        // When
        viewModel.isAssignmentCompleted.test {
            assert(!awaitItem()) // initial false

            viewModel.setAssignmentCompleted(true)

            // Then
            assert(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun isLoading_loadingOperation_setsTrueThenFalse() = runTest {
        // Given
        Mockito.`when`(assignmentRepository.getAllAssignments(null, null, null))
            .thenReturn(Result.success(emptyList()))

        val viewModel = AssignmentViewModel(assignmentRepository)

        // When
        viewModel.isLoading.test {
            assert(!awaitItem()) // initial false

            viewModel.loadAllAssignments()
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
    fun StudentStats_creation_withAllParameters_createsCorrectInstance() {
        // given: 모든 파라미터가 주어진 경우
        // when: StudentStats 인스턴스 생성
        val stats = com.example.voicetutor.ui.viewmodel.StudentStats(
            totalAssignments = 10,
            completedAssignments = 5,
            inProgressAssignments = 3,
            completionRate = 0.5f,
        )

        // then: 모든 필드가 올바르게 설정됨
        assert(stats.totalAssignments == 10)
        assert(stats.completedAssignments == 5)
        assert(stats.inProgressAssignments == 3)
        assert(stats.completionRate == 0.5f)
    }

    @Test
    fun StudentStats_creation_withZeroValues_createsCorrectInstance() {
        // given: 모든 값이 0인 경우
        // when: StudentStats 인스턴스 생성
        val stats = com.example.voicetutor.ui.viewmodel.StudentStats(
            totalAssignments = 0,
            completedAssignments = 0,
            inProgressAssignments = 0,
            completionRate = 0.0f,
        )

        // then: 모든 필드가 0으로 설정됨
        assert(stats.totalAssignments == 0)
        assert(stats.completedAssignments == 0)
        assert(stats.inProgressAssignments == 0)
        assert(stats.completionRate == 0.0f)
    }

    @Test
    fun StudentStats_copy_createsNewInstance() {
        val original = com.example.voicetutor.ui.viewmodel.StudentStats(10, 5, 3, 0.5f)
        val copy = original.copy(completedAssignments = 7)

        assertEquals(7, copy.completedAssignments)
        assertEquals(original.totalAssignments, copy.totalAssignments)
    }

    @Test
    fun StudentStats_equality_worksCorrectly() {
        val stats1 = com.example.voicetutor.ui.viewmodel.StudentStats(10, 5, 3, 0.5f)
        val stats2 = com.example.voicetutor.ui.viewmodel.StudentStats(10, 5, 3, 0.5f)
        val stats3 = com.example.voicetutor.ui.viewmodel.StudentStats(10, 6, 3, 0.5f)

        assertEquals(stats1, stats2)
        assertNotEquals(stats1, stats3)
    }

    @Test
    fun StudentStats_hashCode_worksCorrectly() {
        val stats1 = com.example.voicetutor.ui.viewmodel.StudentStats(10, 5, 3, 0.5f)
        val stats2 = com.example.voicetutor.ui.viewmodel.StudentStats(10, 5, 3, 0.5f)

        assertEquals(stats1.hashCode(), stats2.hashCode())
    }

    @Test
    fun setSelectedAssignmentIds_setsBothIds() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        // Verify initial state
        assertEquals(null, viewModel.selectedAssignmentId.value)
        assertEquals(null, viewModel.selectedPersonalAssignmentId.value)

        // Set both IDs
        viewModel.setSelectedAssignmentIds(1, 2)

        // Values are set synchronously, no need for runCurrent()
        assertEquals(1, viewModel.selectedAssignmentId.value)
        assertEquals(2, viewModel.selectedPersonalAssignmentId.value)
    }

    @Test
    fun setSelectedAssignmentIds_withNullPersonalId_setsNull() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        // First set a value
        viewModel.setSelectedAssignmentIds(1, 2)
        assertEquals(2, viewModel.selectedPersonalAssignmentId.value)

        // Then set to null
        viewModel.setSelectedAssignmentIds(1, null)
        assertEquals(null, viewModel.selectedPersonalAssignmentId.value)
    }

    @Test
    fun clearError_clearsErrorState() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        // First set an error
        viewModel.error.test {
            awaitItem() // initial null
            // Manually set error for testing
            viewModel.error.test {
                awaitItem()
                // We can't directly set private _error, so we trigger an error first
                Mockito.`when`(assignmentRepository.getAllAssignments())
                    .thenReturn(Result.failure(Exception("Test error")))
                viewModel.loadStudentAssignments(1)
                runCurrent()
                val error = awaitItem()
                assert(error != null)

                // Now clear error
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
            awaitItem() // initial empty
            viewModel.setInitialAssignments(assignments)
            assertEquals(assignments, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun resetUploadState_resetsUploadStates() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        // resetUploadState sets values to initial state synchronously
        // Verify it can be called and sets initial values
        viewModel.resetUploadState()

        // Verify initial states are set
        assertEquals(0f, viewModel.uploadProgress.value)
        assertFalse(viewModel.isUploading.value)
        assertFalse(viewModel.uploadSuccess.value)
        assertFalse(viewModel.isGeneratingQuestions.value)
    }

    @Test
    fun clearQuestionGenerationStatus_clearsStatus() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        // clearQuestionGenerationStatus sets values to initial state synchronously
        // Verify it can be called and sets initial values
        viewModel.clearQuestionGenerationStatus()

        // Verify initial states are set
        assertFalse(viewModel.questionGenerationSuccess.value)
        assertNull(viewModel.questionGenerationError.value)
    }

    @Test
    fun setAssignmentCompleted_updatesState() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        viewModel.isAssignmentCompleted.test {
            assert(!awaitItem()) // initial false
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

        // Set a response first (we can't easily create AnswerSubmissionResponse, so just verify it clears)
        // The function sets value to null synchronously
        viewModel.clearAnswerSubmissionResponse()

        // Value is set synchronously
        assertNull(viewModel.answerSubmissionResponse.value)
    }

    @Test
    fun resetAudioRecording_resetsState() = runTest {
        val viewModel = AssignmentViewModel(assignmentRepository)

        // Set some state first
        viewModel.setAudioFilePath("/path/to/audio.wav")
        viewModel.updateRecordingDuration(30)
        runCurrent()

        // Verify state was set
        assertEquals(30, viewModel.audioRecordingState.value.recordingTime)
        assertEquals("/path/to/audio.wav", viewModel.audioRecordingState.value.audioFilePath)

        // Reset (synchronous)
        viewModel.resetAudioRecording()

        // Verify reset state
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

        // Verify initial state
        assertFalse(viewModel.isLoading.value)

        // Load with silent flag (doesn't set loading state)
        viewModel.loadAllAssignments(silent = true)

        // Wait for coroutine to complete
        runCurrent()
        advanceUntilIdle()

        // Should remain false when silent (no loading state change)
        assertFalse(viewModel.isLoading.value)

        // Verify assignments were loaded
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
}
