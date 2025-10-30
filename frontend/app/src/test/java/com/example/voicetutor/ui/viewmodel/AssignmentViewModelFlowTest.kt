package com.example.voicetutor.ui.viewmodel

import app.cash.turbine.test
import com.example.voicetutor.data.models.*
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
import org.mockito.Mockito.never
import org.mockito.Mockito.inOrder
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class AssignmentViewModelFlowTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    lateinit var assignmentRepository: AssignmentRepository

    private fun subject(name: String = "S") = Subject(id = 1, name = name)
    private fun course(name: String = "C") = CourseClass(
        id = 1, name = name, description = null, subject = subject(),
        teacherName = "T", startDate = "", endDate = "", studentCount = 0, createdAt = ""
    )
    private fun studentInfo(id: Int = 1) = StudentInfo(id = id, displayName = "S$id", email = "s$id@ex.com")
    private fun paInfo(id: Int, total: Int = 4) = PersonalAssignmentInfo(
        id = 100 + id,
        title = "A$id",
        description = "d",
        totalQuestions = total,
        visibleFrom = "",
        dueAt = "",
        grade = "1"
    )
    private fun pa(id: Int, status: PersonalAssignmentStatus, solved: Int = 0, total: Int = 4) = PersonalAssignmentData(
        id = id,
        student = studentInfo(id),
        assignment = paInfo(id, total),
        status = status,
        solvedNum = solved
    )

    // Basic init and simple flows
    @Test
    fun assignments_initialState_emitsEmptyList() = runTest {
        val vm = AssignmentViewModel(assignmentRepository)
        vm.assignments.test {
            assert(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAllAssignments_success_updatesAssignments() = runTest {
        val items = listOf(
            AssignmentData(1, "A1", "d", 0, null, "", "", course(), null, null),
            AssignmentData(2, "A2", "d", 0, null, "", "", course(), null, null)
        )
        Mockito.`when`(assignmentRepository.getAllAssignments(null, null, null))
            .thenReturn(Result.success(items))
        val vm = AssignmentViewModel(assignmentRepository)
        vm.assignments.test {
            awaitItem()
            vm.loadAllAssignments()
            runCurrent()
            val next = awaitItem()
            assert(next == items)
            cancelAndIgnoreRemainingEvents()
        }
        Mockito.verify(assignmentRepository, times(1)).getAllAssignments(null, null, null)
    }

    @Test
    fun completeAssignment_success_setsCompleted() = runTest {
        val pid = 123
        Mockito.`when`(assignmentRepository.completePersonalAssignment(pid))
            .thenReturn(Result.success(Unit))
        val vm = AssignmentViewModel(assignmentRepository)
        vm.isAssignmentCompleted.test {
            awaitItem()
            vm.completeAssignment(pid)
            advanceUntilIdle()
            assert(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        Mockito.verify(assignmentRepository, times(1)).completePersonalAssignment(pid)
    }

    // Filters and lists
    @Test
    fun loadPendingStudentAssignments_filtersNotStartedAndInProgressOnly() = runTest {
        val studentId = 1
        val list = listOf(
            pa(1, PersonalAssignmentStatus.NOT_STARTED),
            pa(2, PersonalAssignmentStatus.IN_PROGRESS, solved = 1),
            pa(3, PersonalAssignmentStatus.SUBMITTED),
            pa(4, PersonalAssignmentStatus.GRADED)
        )
        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId))
            .thenReturn(Result.success(list))
        val vm = AssignmentViewModel(assignmentRepository)
        vm.assignments.test {
            awaitItem()
            vm.loadPendingStudentAssignments(studentId)
            runCurrent()
            val next = awaitItem()
            assert(next.size == 2)
            assert(next.all { it.personalAssignmentStatus == PersonalAssignmentStatus.NOT_STARTED || it.personalAssignmentStatus == PersonalAssignmentStatus.IN_PROGRESS })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadCompletedStudentAssignments_filtersSubmittedAndGraded() = runTest {
        val studentId = 2
        val list = listOf(
            pa(1, PersonalAssignmentStatus.NOT_STARTED),
            pa(2, PersonalAssignmentStatus.IN_PROGRESS),
            pa(3, PersonalAssignmentStatus.SUBMITTED),
            pa(4, PersonalAssignmentStatus.GRADED)
        )
        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId))
            .thenReturn(Result.success(list))
        val vm = AssignmentViewModel(assignmentRepository)
        vm.assignments.test {
            awaitItem()
            vm.loadCompletedStudentAssignments(studentId)
            runCurrent()
            val next = awaitItem()
            assert(next.size == 2)
            assert(next.all { it.personalAssignmentStatus == PersonalAssignmentStatus.SUBMITTED || it.personalAssignmentStatus == PersonalAssignmentStatus.GRADED })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadStudentAssignmentsWithPersonalFilter_appliesEachFilterCorrectly() = runTest {
        val studentId = 3
        val list = listOf(
            pa(1, PersonalAssignmentStatus.NOT_STARTED),
            pa(2, PersonalAssignmentStatus.IN_PROGRESS),
            pa(3, PersonalAssignmentStatus.SUBMITTED),
            pa(4, PersonalAssignmentStatus.GRADED)
        )
        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId))
            .thenReturn(Result.success(list))
        val vm = AssignmentViewModel(assignmentRepository)

        vm.assignments.test { awaitItem(); vm.loadStudentAssignmentsWithPersonalFilter(studentId, PersonalAssignmentFilter.ALL); runCurrent(); assert(awaitItem().size == 4); cancelAndIgnoreRemainingEvents() }
        vm.assignments.test { awaitItem(); vm.loadStudentAssignmentsWithPersonalFilter(studentId, PersonalAssignmentFilter.NOT_STARTED); runCurrent(); val ns = awaitItem(); assert(ns.size == 1 && ns.first().personalAssignmentStatus == PersonalAssignmentStatus.NOT_STARTED); cancelAndIgnoreRemainingEvents() }
        vm.assignments.test { awaitItem(); vm.loadStudentAssignmentsWithPersonalFilter(studentId, PersonalAssignmentFilter.IN_PROGRESS); runCurrent(); val ip = awaitItem(); assert(ip.size == 1 && ip.first().personalAssignmentStatus == PersonalAssignmentStatus.IN_PROGRESS); cancelAndIgnoreRemainingEvents() }
        vm.assignments.test { awaitItem(); vm.loadStudentAssignmentsWithPersonalFilter(studentId, PersonalAssignmentFilter.SUBMITTED); runCurrent(); val sub = awaitItem(); assert(sub.size == 1 && sub.first().personalAssignmentStatus == PersonalAssignmentStatus.SUBMITTED); cancelAndIgnoreRemainingEvents() }
        vm.assignments.test { awaitItem(); vm.loadStudentAssignmentsWithPersonalFilter(studentId, PersonalAssignmentFilter.GRADED); runCurrent(); val gr = awaitItem(); assert(gr.size == 1 && gr.first().personalAssignmentStatus == PersonalAssignmentStatus.GRADED); cancelAndIgnoreRemainingEvents() }
    }

    // Questions and navigation
    @Test
    fun moveToQuestionByNumber_notFound_triggersLoadNextQuestion() = runTest {
        val vm = AssignmentViewModel(assignmentRepository)
        val personalId = 55
        Mockito.`when`(assignmentRepository.getNextQuestion(personalId))
            .thenReturn(Result.success(
                PersonalAssignmentQuestion(id = 9, number = "3", question = "Q3", answer = "A", explanation = "E", difficulty = "M")
            ))
        vm.moveToQuestionByNumber("3", personalId)
        advanceUntilIdle()
        Mockito.verify(assignmentRepository, times(1)).getNextQuestion(personalId)
    }

    @Test
    fun loadNextQuestion_noMoreQuestions_clearsListAndError() = runTest {
        val vm = AssignmentViewModel(assignmentRepository)
        val personalId = 88
        // Pre-populate with a question so that clearing to empty emits a new value
        vm.updatePersonalAssignmentQuestions(
            listOf(PersonalAssignmentQuestion(1, "1", "Q1", "A1", "E1", "M"))
        )
        Mockito.`when`(assignmentRepository.getNextQuestion(personalId))
            .thenReturn(Result.failure(Exception("No more questions")))

        vm.personalAssignmentQuestions.test {
            // first emission: pre-populated list
            val initial = awaitItem()
            assert(initial.isNotEmpty())
            vm.loadNextQuestion(personalId)
            runCurrent()
            val next = awaitItem()
            assert(next.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun moveToQuestionByNumber_found_updatesIndex() = runTest {
        val vm = AssignmentViewModel(assignmentRepository)
        vm.updatePersonalAssignmentQuestions(
            listOf(
                PersonalAssignmentQuestion(1, "1", "Q1", "A1", "E1", "M"),
                PersonalAssignmentQuestion(2, "2", "Q2", "A2", "E2", "M")
            )
        )
        vm.moveToQuestionByNumber("2", personalAssignmentId = 1)
        vm.currentQuestionIndex.test {
            val idx = awaitItem()
            assert(idx == 1)
            cancelAndIgnoreRemainingEvents()
        }
        // ensure repository is not called when item is found locally
        Mockito.verify(assignmentRepository, never()).getNextQuestion(Mockito.anyInt())
    }

    @Test
    fun loadAllQuestions_happyPath_setsTotalsAndFirstQuestion() = runTest {
        val vm = AssignmentViewModel(assignmentRepository)
        val personalId = 77
        val base = listOf(
            PersonalAssignmentQuestion(1, "1", "Q1", "A1", "E1", "M"),
            PersonalAssignmentQuestion(2, "2", "Q2", "A2", "E2", "M"),
            PersonalAssignmentQuestion(3, "3", "Q3", "A3", "E3", "M")
        )
        Mockito.`when`(assignmentRepository.getPersonalAssignmentQuestions(personalId))
            .thenReturn(Result.success(base))
        Mockito.`when`(assignmentRepository.getNextQuestion(personalId))
            .thenReturn(Result.success(base[1]))

        vm.loadAllQuestions(personalId)
        advanceUntilIdle()

        vm.totalBaseQuestions.test { val total = awaitItem(); assert(total == base.size); cancelAndIgnoreRemainingEvents() }
        vm.personalAssignmentQuestions.test { val list = awaitItem(); assert(list.size == 1 && list.first().number == "2"); cancelAndIgnoreRemainingEvents() }
    }

    // Misc flows
    @Test
    fun errorFlows_propagateToErrorState() = runTest {
        val vm = AssignmentViewModel(assignmentRepository)
        val studentId = 999
        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId))
            .thenReturn(Result.failure(Exception("boom")))
        vm.error.test {
            awaitItem()
            vm.loadPendingStudentAssignments(studentId)
            runCurrent()
            val err = awaitItem()
            assert(err?.contains("boom") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadStudentAssignmentsWithPersonalFilter_ALL_updatesStudentStats() = runTest {
        val studentId = 7
        val list = listOf(
            pa(1, PersonalAssignmentStatus.NOT_STARTED),
            pa(2, PersonalAssignmentStatus.IN_PROGRESS),
            pa(3, PersonalAssignmentStatus.SUBMITTED)
        )
        Mockito.`when`(assignmentRepository.getPersonalAssignments(studentId))
            .thenReturn(Result.success(list))
        val vm = AssignmentViewModel(assignmentRepository)
        vm.studentStats.test {
            awaitItem()
            vm.loadStudentAssignmentsWithPersonalFilter(studentId, PersonalAssignmentFilter.ALL)
            runCurrent()
            val stats = awaitItem()
            assert(stats != null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun startStopRecordingAndUpdateDuration_updatesState() = runTest {
        val vm = AssignmentViewModel(assignmentRepository)
        vm.audioRecordingState.test {
            awaitItem()
            vm.startRecording()
            val started = awaitItem(); assert(started.isRecording)
            vm.updateRecordingDuration(5)
            val updated = awaitItem(); assert(updated.recordingTime == 5)
            vm.stopRecordingWithFilePath("/tmp/sample.3gp")
            val stopped = awaitItem(); assert(!stopped.isRecording && stopped.audioFilePath != null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadRecentPersonalAssignment_success_setsId() = runTest {
        val vm = AssignmentViewModel(assignmentRepository)
        Mockito.`when`(assignmentRepository.getRecentPersonalAssignment(9))
            .thenReturn(Result.success(123))
        vm.recentPersonalAssignmentId.test {
            awaitItem()
            vm.loadRecentPersonalAssignment(9)
            advanceUntilIdle()
            val next = awaitItem(); assert(next == 123)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAssignmentById_success_setsCurrentAssignment() = runTest {
        val vm = AssignmentViewModel(assignmentRepository)
        val a = AssignmentData(6, "Title", "d", 1, null, "", "", course(), null, null)
        Mockito.`when`(assignmentRepository.getAssignmentById(6)).thenReturn(Result.success(a))
        vm.currentAssignment.test {
            awaitItem()
            vm.loadAssignmentById(6)
            advanceUntilIdle()
            val next = awaitItem(); assert(next == a)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAllAssignments_withStatusCompleted_callsRepoWithStatus() = runTest {
        val vm = AssignmentViewModel(assignmentRepository)
        Mockito.`when`(assignmentRepository.getAllAssignments(null, null, AssignmentStatus.COMPLETED))
            .thenReturn(Result.success(emptyList()))
        vm.loadAllAssignments(status = AssignmentStatus.COMPLETED)
        advanceUntilIdle()
        Mockito.verify(assignmentRepository, times(1)).getAllAssignments(null, null, AssignmentStatus.COMPLETED)
    }

    @Test
    fun setSelectedAssignmentIds_callsMetaThenStats_inOrder() = runTest {
        val vm = AssignmentViewModel(assignmentRepository)
        val assignmentId = 6
        val personalId = 16
        Mockito.`when`(assignmentRepository.getAssignmentById(assignmentId))
            .thenReturn(Result.success(
                AssignmentData(assignmentId, "t", "d", 1, null, "", "", course(), null, null)
            ))
        Mockito.`when`(assignmentRepository.getPersonalAssignmentStatistics(personalId))
            .thenReturn(Result.success(
                PersonalAssignmentStatistics(
                    totalQuestions = 3,
                    answeredQuestions = 1,
                    correctAnswers = 1,
                    accuracy = 0.5f,
                    totalProblem = 3,
                    solvedProblem = 1,
                    progress = 0.5f
                )
            ))

        vm.setSelectedAssignmentIds(assignmentId, personalId)
        advanceUntilIdle()

        val order = inOrder(assignmentRepository)
        order.verify(assignmentRepository).getAssignmentById(assignmentId)
        order.verify(assignmentRepository).getPersonalAssignmentStatistics(personalId)
    }

    @Test
    fun submitAnswer_success_updatesResponse_andVerifiesRepoCall() = runTest {
        val vm = AssignmentViewModel(assignmentRepository)
        val studentId = 7
        val questionId = 21
        val tmpFile = File.createTempFile("answer", ".3gp")
        val response = AnswerSubmissionResponse(isCorrect = true, numberStr = "2", tailQuestion = null)
        whenever(assignmentRepository.submitAnswer(eq(studentId), eq(questionId), any()))
            .thenReturn(Result.success(response))

        vm.answerSubmissionResponse.test {
            awaitItem() // initial null
            vm.submitAnswer(studentId, questionId, tmpFile)
            advanceUntilIdle()
            val next = awaitItem()
            assert(next == response)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(assignmentRepository, times(1)).submitAnswer(eq(studentId), eq(questionId), any())
    }
}


