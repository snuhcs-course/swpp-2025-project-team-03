package com.example.voicetutor.ui.viewmodel

import app.cash.turbine.test
import com.example.voicetutor.data.models.ClassData
import com.example.voicetutor.data.models.Student
import com.example.voicetutor.data.models.Subject
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.data.repository.ClassRepository
import com.example.voicetutor.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class TeacherClassViewModelTest {

    private val repository: ClassRepository = mock()
    private lateinit var viewModel: ClassViewModel

    @get:Rule
    val mainRule = MainDispatcherRule { StandardTestDispatcher() }

    @Before
    fun setUp() {
        viewModel = ClassViewModel(repository)
    }

    @Test
    fun loadClassById_and_loadClassStudents_updatesStates() {
        runTest(mainRule.testDispatcher) {
            val cd = classData(1, "C1")
            val students = listOf(student(10), student(11))
            whenever(repository.getClassById(1)).thenReturn(Result.success(cd))
            whenever(repository.getClassStudents(1)).thenReturn(Result.success(students))

            viewModel.currentClass.test {
                assert(awaitItem() == null)
                viewModel.loadClassById(1)
                runCurrent()
                assert(awaitItem() == cd)
                cancelAndIgnoreRemainingEvents()
            }

            viewModel.classStudents.test {
                assert(awaitItem().isEmpty())
                viewModel.loadClassStudents(1)
                runCurrent()
                assert(awaitItem() == students)
                cancelAndIgnoreRemainingEvents()
            }

            verify(repository, times(1)).getClassById(1)
            verify(repository, times(1)).getClassStudents(1)
        }
    }

    @Test
    fun enrollStudentToClass_callsRepo_thenRefreshesStudents() {
        runTest(mainRule.testDispatcher) {
            whenever(repository.enrollStudentToClass(classId = 1, studentId = 10))
                .thenReturn(
                    Result.success(
                        com.example.voicetutor.data.models.EnrollmentData(
                            student = student(10),
                            courseClass = classData(1, "C1"),
                            status = "ENROLLED",
                        ),
                    ),
                )
            whenever(repository.getClassStudents(1)).thenReturn(Result.success(listOf(student(10))))

            viewModel.enrollStudentToClass(classId = 1, studentId = 10)
            runCurrent()

            verify(repository, times(1)).enrollStudentToClass(classId = 1, studentId = 10)
            verify(repository, times(1)).getClassStudents(1)
        }
    }
    private fun classData(id: Int, name: String) = ClassData(
        id = id,
        name = name,
        subject = Subject(1, "수학", null),
        description = "",
        teacherId = 1,
        studentCount = 0,
        createdAt = null,
    )

    private fun student(id: Int) = Student(
        id = id,
        name = "s$id",
        email = "s$id@test.com",
        role = UserRole.STUDENT,
    )

    @Test
    fun classes_initialState_emitsEmptyList() {
        runTest(mainRule.testDispatcher) {
            viewModel.classes.test {
                assert(awaitItem().isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun loadClasses_success_updatesClasses() {
        runTest(mainRule.testDispatcher) {
            val classes = listOf(classData(1, "C1"), classData(2, "C2"))
            whenever(repository.getClasses("1")).thenReturn(Result.success(classes))

            viewModel.classes.test {
                assert(awaitItem().isEmpty())
                viewModel.loadClasses("1")
                runCurrent()

                assert(awaitItem() == classes)
                cancelAndIgnoreRemainingEvents()
            }

            verify(repository, times(1)).getClasses("1")
        }
    }

    @Test
    fun loadClasses_failure_setsError() {
        runTest(mainRule.testDispatcher) {
            whenever(repository.getClasses("1")).thenReturn(Result.failure(Exception("Network error")))

            viewModel.error.test {
                awaitItem()
                viewModel.loadClasses("1")
                runCurrent()

                val error = awaitItem()
                assert(error?.contains("Network error") == true)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun createClass_success_addsClassToList() {
        runTest(mainRule.testDispatcher) {
            val newClass = classData(3, "C3")
            val request = com.example.voicetutor.data.network.CreateClassRequest(
                name = "C3",
                description = "",
                subject_name = "수학",
                teacher_id = 1,

            )
            whenever(repository.createClass(request)).thenReturn(Result.success(newClass))

            viewModel.classes.test {
                awaitItem()

                viewModel.createClass(request)
                runCurrent()

                val updated = awaitItem()
                assert(updated.contains(newClass))
                cancelAndIgnoreRemainingEvents()
            }

            verify(repository, times(1)).createClass(request)
        }
    }

    @Test
    fun createClass_failure_setsError() {
        runTest(mainRule.testDispatcher) {
            val request = com.example.voicetutor.data.network.CreateClassRequest(
                name = "C3",
                description = "",
                subject_name = "수학",
                teacher_id = 1,

            )
            whenever(repository.createClass(request)).thenReturn(Result.failure(Exception("Creation failed")))

            viewModel.error.test {
                awaitItem()
                viewModel.createClass(request)
                runCurrent()

                val error = awaitItem()
                assert(error?.contains("Creation failed") == true)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun refreshClasses_callsLoadClasses() {
        runTest(mainRule.testDispatcher) {
            whenever(repository.getClasses("1")).thenReturn(Result.success(emptyList()))

            viewModel.loadClasses("1")
            runCurrent()

            verify(repository, times(1)).getClasses("1")
        }
    }

    @Test
    fun clearError_clearsErrorState() {
        runTest(mainRule.testDispatcher) {
            whenever(repository.getClasses("1")).thenReturn(Result.failure(Exception("Error")))

            viewModel.error.test {
                awaitItem()
                viewModel.loadClasses("1")
                runCurrent()
                assert(awaitItem() != null)

                viewModel.clearError()

                assert(awaitItem() == null)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun isLoading_loadingOperation_setsTrueThenFalse() {
        runTest(mainRule.testDispatcher) {
            whenever(repository.getClasses("1")).thenReturn(Result.success(emptyList()))

            viewModel.isLoading.test {
                assert(!awaitItem())
                viewModel.loadClasses("1")
                runCurrent()

                val states = listOf(awaitItem(), awaitItem())
                assert(states.contains(true))
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun loadClassById_failure_setsError() {
        runTest(mainRule.testDispatcher) {
            whenever(repository.getClassById(1)).thenReturn(Result.failure(Exception("Not found")))

            viewModel.error.test {
                awaitItem()
                viewModel.loadClassById(1)
                runCurrent()

                val error = awaitItem()
                assert(error?.contains("Not found") == true)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun loadClassStudents_failure_setsError() {
        runTest(mainRule.testDispatcher) {
            whenever(repository.getClassStudents(1)).thenReturn(Result.failure(Exception("Failed")))

            viewModel.error.test {
                awaitItem()
                viewModel.loadClassStudents(1)
                runCurrent()

                val error = awaitItem()
                assert(error?.contains("Failed") == true)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    @Ignore("Uncaught exceptions issue")
    fun enrollStudentToClass_failure_setsError() {
        runTest(mainRule.testDispatcher) {
            whenever(repository.enrollStudentToClass(classId = 1, studentId = 10))
                .thenReturn(Result.failure(Exception("Enrollment failed")))

            viewModel.error.test {
                awaitItem()
                viewModel.enrollStudentToClass(classId = 1, studentId = 10)
                runCurrent()

                val error = awaitItem()
                assert(error?.contains("Enrollment failed") == true)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun createClass_success_addsToExistingList() {
        runTest(mainRule.testDispatcher) {
            val existingClasses = listOf(classData(1, "C1"), classData(2, "C2"))
            whenever(repository.getClasses("1")).thenReturn(Result.success(existingClasses))
            viewModel.loadClasses("1")
            runCurrent()

            val newClass = classData(3, "C3")
            val request = com.example.voicetutor.data.network.CreateClassRequest(
                name = "C3",
                description = "",
                subject_name = "수학",
                teacher_id = 1,
            )
            whenever(repository.createClass(request)).thenReturn(Result.success(newClass))

            viewModel.classes.test {
                awaitItem()
                viewModel.createClass(request)
                runCurrent()

                val updated = awaitItem()
                assert(updated.size == 3)
                assert(updated.contains(newClass))
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}
