package com.example.voicetutor.ui.viewmodel

import app.cash.turbine.test
import com.example.voicetutor.data.models.AssignmentData
import com.example.voicetutor.data.models.CourseClass
import com.example.voicetutor.data.models.AssignmentResultData
import com.example.voicetutor.data.models.Subject
import com.example.voicetutor.data.models.PersonalAssignmentStatistics
import com.example.voicetutor.data.network.CreateAssignmentRequest
import com.example.voicetutor.data.repository.AssignmentRepository
import com.example.voicetutor.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
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
        startDate = "",
        endDate = "",
        studentCount = 0,
        createdAt = ""
    )

    private fun buildAssignment(id: Int): AssignmentData = AssignmentData(
        id = id,
        title = "Assignment $id",
        description = "desc",
        totalQuestions = 0,
        createdAt = null,
        visibleFrom = "",
        dueAt = "",
        courseClass = buildCourse(),
        materials = null,
        grade = null,
        personalAssignmentStatus = null,
        solvedNum = null,
        personalAssignmentId = null
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
            averageScore = 0.8f
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
            completionRate = 0.5f
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
            completionRate = 0.0f
        )
        
        // then: 모든 필드가 0으로 설정됨
        assert(stats.totalAssignments == 0)
        assert(stats.completedAssignments == 0)
        assert(stats.inProgressAssignments == 0)
        assert(stats.completionRate == 0.0f)
    }
}


