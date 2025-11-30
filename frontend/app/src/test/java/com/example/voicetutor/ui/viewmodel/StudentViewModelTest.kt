package com.example.voicetutor.ui.viewmodel

import app.cash.turbine.test
import com.example.voicetutor.data.models.Student
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.data.repository.StudentRepository
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
class StudentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule { StandardTestDispatcher() }

    @Mock
    lateinit var studentRepository: StudentRepository

    private lateinit var viewModel: StudentViewModel

    @Before
    fun setUp() {
        viewModel = StudentViewModel(studentRepository)
    }

    @Test
    fun students_initialState_emitsEmptyList() = runTest {
        viewModel.students.test {
            assert(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAllStudents_success_updatesStudents() = runTest {
        val students = listOf(
            Student(id = 1, name = "Alice", email = "alice@test.com", role = UserRole.STUDENT),
            Student(id = 2, name = "Bob", email = "bob@test.com", role = UserRole.STUDENT),
        )
        Mockito.`when`(studentRepository.getAllStudents(null, null))
            .thenReturn(Result.success(students))

        viewModel.students.test {
            awaitItem()

            viewModel.loadAllStudents()
            runCurrent()

            assert(awaitItem() == students)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(studentRepository, times(1)).getAllStudents(null, null)
    }

    @Test
    fun loadAllStudents_withFilters_success_updatesStudents() = runTest {
        val students = listOf(
            Student(id = 1, name = "Alice", email = "alice@test.com", role = UserRole.STUDENT),
        )
        Mockito.`when`(studentRepository.getAllStudents("1", "10"))
            .thenReturn(Result.success(students))

        viewModel.students.test {
            awaitItem()

            viewModel.loadAllStudents(teacherId = "1", classId = "10")
            runCurrent()

            assert(awaitItem() == students)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(studentRepository, times(1)).getAllStudents("1", "10")
    }

    @Test
    fun loadAllStudents_failure_setsError() = runTest {
        Mockito.`when`(studentRepository.getAllStudents(null, null))
            .thenReturn(Result.failure(Exception("Network error")))

        viewModel.error.test {
            awaitItem()

            viewModel.loadAllStudents()
            runCurrent()

            val error = awaitItem()
            assert(error?.contains("Network error") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadStudentById_success_updatesCurrentStudent() = runTest {
        val student = Student(id = 1, name = "Alice", email = "alice@test.com", role = UserRole.STUDENT)
        Mockito.`when`(studentRepository.getStudentById(1))
            .thenReturn(Result.success(student))

        viewModel.currentStudent.test {
            assert(awaitItem() == null)

            viewModel.loadStudentById(1)
            runCurrent()

            assert(awaitItem() == student)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(studentRepository, times(1)).getStudentById(1)
    }

    @Test
    fun isLoading_loadingOperation_setsTrueThenFalse() = runTest {
        Mockito.`when`(studentRepository.getAllStudents(null, null))
            .thenReturn(Result.success(emptyList()))

        viewModel.isLoading.test {
            assert(!awaitItem())

            viewModel.loadAllStudents()
            runCurrent()

            val loadingStates = listOf(awaitItem(), awaitItem())
            assert(loadingStates.contains(true))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearError_clearsErrorState() = runTest {
        Mockito.`when`(studentRepository.getAllStudents(null, null))
            .thenReturn(Result.failure(Exception("Error")))

        viewModel.error.test {
            awaitItem()
            viewModel.loadAllStudents()
            runCurrent()
            assert(awaitItem() != null)

            viewModel.clearError()

            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadStudentById_failure_setsError() = runTest {
        Mockito.`when`(studentRepository.getStudentById(1))
            .thenReturn(Result.failure(Exception("Student not found")))

        viewModel.error.test {
            awaitItem()

            viewModel.loadStudentById(1)
            runCurrent()

            val error = awaitItem()
            assert(error?.contains("Student not found") == true)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.currentStudent.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAllStudents_withOnlyTeacherId_callsRepoWithTeacherId() = runTest {
        val students = listOf(
            Student(id = 1, name = "Alice", email = "alice@test.com", role = UserRole.STUDENT),
        )
        Mockito.`when`(studentRepository.getAllStudents("1", null))
            .thenReturn(Result.success(students))

        viewModel.students.test {
            awaitItem()

            viewModel.loadAllStudents(teacherId = "1")
            runCurrent()

            assert(awaitItem() == students)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(studentRepository, times(1)).getAllStudents("1", null)
    }

    @Test
    fun loadAllStudents_withOnlyClassId_callsRepoWithClassId() = runTest {
        val students = listOf(
            Student(id = 1, name = "Alice", email = "alice@test.com", role = UserRole.STUDENT),
        )
        Mockito.`when`(studentRepository.getAllStudents(null, "10"))
            .thenReturn(Result.success(students))

        viewModel.students.test {
            awaitItem()

            viewModel.loadAllStudents(classId = "10")
            runCurrent()

            assert(awaitItem() == students)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(studentRepository, times(1)).getAllStudents(null, "10")
    }

    @Test
    fun students_initialState_isEmpty() = runTest {
        viewModel.students.test {
            assert(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun currentStudent_initialState_isNull() = runTest {
        viewModel.currentStudent.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun error_initialState_isNull() = runTest {
        viewModel.error.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun isLoading_initialState_isFalse() = runTest {
        viewModel.isLoading.test {
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
