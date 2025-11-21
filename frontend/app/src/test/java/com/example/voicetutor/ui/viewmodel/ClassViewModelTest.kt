package com.example.voicetutor.ui.viewmodel

import app.cash.turbine.test
import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.network.CreateClassRequest
import com.example.voicetutor.data.repository.ClassRepository
import com.example.voicetutor.testing.MainDispatcherRule
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class ClassViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    lateinit var classRepository: ClassRepository

    private fun buildSubject(id: Int = 1, name: String = "Math") = Subject(id = id, name = name)

    private fun buildClassData(id: Int = 1, name: String = "Class1") = ClassData(
        id = id,
        name = name,
        subject = buildSubject(),
        description = "Description",
        teacherId = 1,

        studentCount = 10,
        createdAt = "2025-01-01",
    )

    @Test
    fun initialStates_areCorrect() = runTest {
        val vm = ClassViewModel(classRepository)

        vm.classes.test {
            assert(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
        vm.currentClass.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
        vm.classStudents.test {
            assert(awaitItem().isEmpty())
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
    fun loadClasses_success_updatesClasses() = runTest {
        // Given
        val vm = ClassViewModel(classRepository)
        val classes = listOf(buildClassData(1), buildClassData(2))
        Mockito.`when`(classRepository.getClasses("1")).thenReturn(Result.success(classes))

        // When
        vm.loadClasses("1")
        advanceUntilIdle()

        // Then
        vm.classes.test {
            assertEquals(classes, awaitItem())
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
    fun loadClasses_failure_setsError() = runTest {
        // Given
        val vm = ClassViewModel(classRepository)
        Mockito.`when`(classRepository.getClasses("1")).thenReturn(Result.failure(Exception("Network error")))

        // When
        vm.loadClasses("1")
        advanceUntilIdle()

        // Then
        vm.error.test {
            assertEquals("Network error", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        vm.isLoading.test {
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadClassById_success_updatesCurrentClass() = runTest {
        // Given
        val vm = ClassViewModel(classRepository)
        val classData = buildClassData(1)
        Mockito.`when`(classRepository.getClassById(1)).thenReturn(Result.success(classData))

        // When
        vm.loadClassById(1)
        advanceUntilIdle()

        // Then
        vm.currentClass.test {
            assertEquals(classData, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        vm.isLoading.test {
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadClassById_failure_setsError() = runTest {
        // Given
        val vm = ClassViewModel(classRepository)
        Mockito.`when`(classRepository.getClassById(1)).thenReturn(Result.failure(Exception("Not found")))

        // When
        vm.loadClassById(1)
        advanceUntilIdle()

        // Then
        vm.error.test {
            assertEquals("Not found", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadClassStudents_success_updatesClassStudents() = runTest {
        // Given
        val vm = ClassViewModel(classRepository)
        val students = listOf(
            Student(id = 1, name = "Student1", email = "s1@test.com", role = UserRole.STUDENT),
            Student(id = 2, name = "Student2", email = "s2@test.com", role = UserRole.STUDENT),
        )
        Mockito.`when`(classRepository.getClassStudents(1)).thenReturn(Result.success(students))

        // When
        vm.loadClassStudents(1)
        advanceUntilIdle()

        // Then
        vm.classStudents.test {
            assertEquals(students, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        vm.isLoading.test {
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadClassStudents_failure_setsError() = runTest {
        // Given
        val vm = ClassViewModel(classRepository)
        Mockito.`when`(classRepository.getClassStudents(1)).thenReturn(Result.failure(Exception("Failed")))

        // When
        vm.loadClassStudents(1)
        advanceUntilIdle()

        // Then
        vm.error.test {
            assertEquals("Failed", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun createClass_success_addsToClasses() = runTest {
        // Given
        val vm = ClassViewModel(classRepository)
        val newClass = buildClassData(2, "New Class")
        val request = CreateClassRequest(
            name = "New Class",
            description = "Description",
            subject_name = "Math",
            teacher_id = 1,

        )

        Mockito.`when`(classRepository.createClass(request)).thenReturn(Result.success(newClass))

        // When
        vm.createClass(request)
        advanceUntilIdle()

        // Then
        vm.classes.test {
            val classes = awaitItem()
            assertTrue(classes.contains(newClass))
            cancelAndIgnoreRemainingEvents()
        }
        vm.isLoading.test {
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun createClass_failure_setsError() = runTest {
        // Given
        val vm = ClassViewModel(classRepository)
        val request = CreateClassRequest(
            name = "New Class",
            description = null,
            subject_name = "Math",
            teacher_id = 1,

        )
        Mockito.`when`(classRepository.createClass(request)).thenReturn(Result.failure(Exception("Creation failed")))

        // When
        vm.createClass(request)
        advanceUntilIdle()

        // Then
        vm.error.test {
            assertEquals("Creation failed", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun refreshClasses_callsLoadClasses() = runTest {
        // Given
        val vm = ClassViewModel(classRepository)
        val classes = listOf(buildClassData(1))
        Mockito.`when`(classRepository.getClasses("1")).thenReturn(Result.success(classes))

        // When
        vm.refreshClasses("1")
        advanceUntilIdle()

        // Then
        vm.classes.test {
            assertEquals(classes, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun enrollStudentToClass_success_refreshesClassStudents() = runTest {
        // Given
        val vm = ClassViewModel(classRepository)
        val enrollment = EnrollmentData(
            student = Student(id = 1, name = "Student1", email = "s1@test.com", role = UserRole.STUDENT),
            courseClass = buildClassData(1),
            status = "enrolled",
        )
        val students = listOf(
            Student(id = 1, name = "Student1", email = "s1@test.com", role = UserRole.STUDENT),
        )
        Mockito.`when`(classRepository.enrollStudentToClass(1, 1)).thenReturn(Result.success(enrollment))
        Mockito.`when`(classRepository.getClassStudents(1)).thenReturn(Result.success(students))

        // When
        vm.enrollStudentToClass(1, 1)
        advanceUntilIdle()

        // Then
        vm.classStudents.test {
            assertEquals(students, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        vm.isLoading.test {
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun enrollStudentToClass_failure_setsError() = runTest {
        // Given
        val vm = ClassViewModel(classRepository)
        Mockito.`when`(classRepository.enrollStudentToClass(1, 1)).thenReturn(Result.failure(Exception("Enrollment failed")))

        // When
        vm.enrollStudentToClass(1, 1)
        advanceUntilIdle()

        // Then
        vm.error.test {
            assertEquals("Enrollment failed", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun removeStudentFromClass_success_refreshesClassStudents() = runTest {
        // Given
        val vm = ClassViewModel(classRepository)
        val students = emptyList<Student>()
        Mockito.`when`(classRepository.removeStudentFromClass(1, 1)).thenReturn(Result.success(Unit))
        Mockito.`when`(classRepository.getClassStudents(1)).thenReturn(Result.success(students))

        // When
        vm.removeStudentFromClass(1, 1)
        advanceUntilIdle()

        // Then
        vm.classStudents.test {
            assertEquals(students, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        vm.isLoading.test {
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun removeStudentFromClass_failure_setsError() = runTest {
        // Given
        val vm = ClassViewModel(classRepository)
        Mockito.`when`(classRepository.removeStudentFromClass(1, 1)).thenReturn(Result.failure(Exception("Removal failed")))

        // When
        vm.removeStudentFromClass(1, 1)
        advanceUntilIdle()

        // Then
        vm.error.test {
            assertEquals("Removal failed", awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearError_clearsError() = runTest {
        // Given
        val vm = ClassViewModel(classRepository)
        Mockito.`when`(classRepository.getClasses("1")).thenReturn(Result.failure(Exception("Some error")))
        vm.loadClasses("1")
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
    fun loadClassStudentsStatistics_callsRepository() = runTest {
        // Given
        val vm = ClassViewModel(classRepository)
        val statistics = ClassStudentsStatistics(
            overallCompletionRate = 0.85f,
            students = listOf(
                StudentStatisticsItem(
                    studentId = 1,
                    averageScore = 85.5f,
                    completionRate = 0.9f,
                    totalAssignments = 10,
                    completedAssignments = 9,
                ),
            ),
        )
        Mockito.`when`(classRepository.getClassStudentsStatistics(1)).thenReturn(Result.success(statistics))

        // When
        var callbackResult: Result<ClassStudentsStatistics>? = null
        vm.loadClassStudentsStatistics(1) { result ->
            callbackResult = result
        }
        advanceUntilIdle()

        // Then
        assert(callbackResult != null)
        assert(callbackResult?.isSuccess == true)
        assertEquals(statistics, callbackResult?.getOrNull())
    }

    @Test
    fun deleteClass_success_removesFromClassesAndCallsCallback() = runTest {
        // Given
        val vm = ClassViewModel(classRepository)
        val class1 = buildClassData(1, "Class 1")
        val class2 = buildClassData(2, "Class 2")
        vm.classes.test {
            awaitItem() // initial empty
            // Manually set classes for testing
            // Since _classes is private, we'll test through the actual flow
        }

        Mockito.`when`(classRepository.getClasses("1"))
            .thenReturn(Result.success(listOf(class1, class2)))
        Mockito.`when`(classRepository.removeClassById(1))
            .thenReturn(Result.success(Unit))

        // Load classes first
        vm.loadClasses("1")
        advanceUntilIdle()

        // When
        var callbackCalled = false
        var callbackResult = false
        vm.deleteClass(1) { result ->
            callbackCalled = true
            callbackResult = result
        }
        advanceUntilIdle()

        // Then
        assert(callbackCalled)
        assert(callbackResult)
        vm.classes.test {
            val classes = awaitItem()
            assert(!classes.contains(class1))
            cancelAndIgnoreRemainingEvents()
        }
        vm.isLoading.test {
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteClass_failure_setsErrorAndCallsCallback() = runTest {
        // Given
        val vm = ClassViewModel(classRepository)
        Mockito.`when`(classRepository.removeClassById(1))
            .thenReturn(Result.failure(Exception("Deletion failed")))

        // When
        var callbackCalled = false
        var callbackResult = false
        vm.deleteClass(1) { result ->
            callbackCalled = true
            callbackResult = result
        }
        advanceUntilIdle()

        // Then
        assert(callbackCalled)
        assert(!callbackResult)
        vm.error.test {
            val error = awaitItem()
            assert(error != null)
            assert(error?.contains("Deletion failed") == true || error?.contains("failed") == true)
            cancelAndIgnoreRemainingEvents()
        }
        vm.isLoading.test {
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteClass_withDefaultCallback_handlesCorrectly() = runTest {
        // Given
        val vm = ClassViewModel(classRepository)
        Mockito.`when`(classRepository.removeClassById(1))
            .thenReturn(Result.success(Unit))

        // When - call without explicit callback (uses default empty callback)
        vm.deleteClass(1)
        advanceUntilIdle()

        // Then - should complete without error
        vm.isLoading.test {
            assert(!awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
