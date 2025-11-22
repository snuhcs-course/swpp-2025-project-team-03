package com.example.voicetutor.ui.viewmodel

import app.cash.turbine.test
import com.example.voicetutor.data.models.AssignmentData
import com.example.voicetutor.data.models.ClassInfo
import com.example.voicetutor.data.models.Student
import com.example.voicetutor.data.models.StudentProgress
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.data.repository.StudentRepository
import com.example.voicetutor.testing.MainDispatcherRule
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.times
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class StudentViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    lateinit var studentRepository: StudentRepository

    private lateinit var viewModel: StudentViewModel

    @Before
    fun setUp() {
        viewModel = StudentViewModel(studentRepository)
    }

    @Test
    fun students_initialState_emitsEmptyList() = runTest {
        // Given: 새로 생성된 ViewModel
        // When: 초기 상태를 관측하면
        viewModel.students.test {
            // Then: 첫 방출이 emptyList 여야 한다
            assert(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadAllStudents_success_updatesStudents() = runTest {
        // Given: 저장소가 성공적으로 학생 목록을 반환하도록 스텁
        val students = listOf(
            Student(id = 1, name = "Alice", email = "alice@test.com", role = UserRole.STUDENT),
            Student(id = 2, name = "Bob", email = "bob@test.com", role = UserRole.STUDENT),
        )
        Mockito.`when`(studentRepository.getAllStudents(null, null))
            .thenReturn(Result.success(students))

        // When: loadAllStudents 호출
        viewModel.students.test {
            // 초기 상태 구독
            awaitItem()

            viewModel.loadAllStudents()
            runCurrent()

            // Then: 업데이트된 학생 목록 반영
            assert(awaitItem() == students)
            cancelAndIgnoreRemainingEvents()
        }

        // Then: 저장소는 정확히 1회 호출됨
        Mockito.verify(studentRepository, times(1)).getAllStudents(null, null)
    }

    @Test
    fun loadAllStudents_withFilters_success_updatesStudents() = runTest {
        // Given: teacherId와 classId 필터 적용
        val students = listOf(
            Student(id = 1, name = "Alice", email = "alice@test.com", role = UserRole.STUDENT),
        )
        Mockito.`when`(studentRepository.getAllStudents("1", "10"))
            .thenReturn(Result.success(students))

        // When
        viewModel.students.test {
            awaitItem()

            viewModel.loadAllStudents(teacherId = "1", classId = "10")
            runCurrent()

            // Then
            assert(awaitItem() == students)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(studentRepository, times(1)).getAllStudents("1", "10")
    }

    @Test
    fun loadAllStudents_failure_setsError() = runTest {
        // Given: 저장소가 실패 반환
        Mockito.`when`(studentRepository.getAllStudents(null, null))
            .thenReturn(Result.failure(Exception("Network error")))

        // When
        viewModel.error.test {
            awaitItem() // initial null

            viewModel.loadAllStudents()
            runCurrent()

            // Then: 에러 메시지 설정
            val error = awaitItem()
            assert(error?.contains("Network error") == true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadStudentById_success_updatesCurrentStudent() = runTest {
        // Given
        val student = Student(id = 1, name = "Alice", email = "alice@test.com", role = UserRole.STUDENT)
        Mockito.`when`(studentRepository.getStudentById(1))
            .thenReturn(Result.success(student))

        // When
        viewModel.currentStudent.test {
            assert(awaitItem() == null)

            viewModel.loadStudentById(1)
            runCurrent()

            // Then
            assert(awaitItem() == student)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(studentRepository, times(1)).getStudentById(1)
    }


    @Test
    fun isLoading_loadingOperation_setsTrueThenFalse() = runTest {
        // Given: 느린 네트워크 시뮬레이션
        Mockito.`when`(studentRepository.getAllStudents(null, null))
            .thenReturn(Result.success(emptyList()))

        // When
        viewModel.isLoading.test {
            assert(!awaitItem()) // initial false

            viewModel.loadAllStudents()
            runCurrent()

            // Then: 로딩 상태가 true로 변경된 후 false로 변경
            val loadingStates = listOf(awaitItem(), awaitItem())
            assert(loadingStates.contains(true))
            // 최종적으로 false가 되어야 함
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun clearError_clearsErrorState() = runTest {
        // Given: 에러가 발생한 상태
        Mockito.`when`(studentRepository.getAllStudents(null, null))
            .thenReturn(Result.failure(Exception("Error")))

        viewModel.error.test {
            awaitItem()
            viewModel.loadAllStudents()
            runCurrent()
            assert(awaitItem() != null) // 에러 설정 확인

            // When: clearError 호출
            viewModel.clearError()

            // Then: 에러가 null로 변경
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadStudentById_failure_setsError() = runTest {
        // Given: 저장소가 실패 반환
        Mockito.`when`(studentRepository.getStudentById(1))
            .thenReturn(Result.failure(Exception("Student not found")))

        // When
        viewModel.error.test {
            awaitItem() // initial null

            viewModel.loadStudentById(1)
            runCurrent()

            // Then: 에러 메시지 설정
            val error = awaitItem()
            assert(error?.contains("Student not found") == true)
            cancelAndIgnoreRemainingEvents()
        }

        // Then: currentStudent가 null로 유지됨
        viewModel.currentStudent.test {
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
    }


    @Test
    fun loadAllStudents_withOnlyTeacherId_callsRepoWithTeacherId() = runTest {
        // Given: teacherId만 전달
        val students = listOf(
            Student(id = 1, name = "Alice", email = "alice@test.com", role = UserRole.STUDENT),
        )
        Mockito.`when`(studentRepository.getAllStudents("1", null))
            .thenReturn(Result.success(students))

        // When
        viewModel.students.test {
            awaitItem()

            viewModel.loadAllStudents(teacherId = "1")
            runCurrent()

            // Then
            assert(awaitItem() == students)
            cancelAndIgnoreRemainingEvents()
        }

        Mockito.verify(studentRepository, times(1)).getAllStudents("1", null)
    }

    @Test
    fun loadAllStudents_withOnlyClassId_callsRepoWithClassId() = runTest {
        // Given: classId만 전달
        val students = listOf(
            Student(id = 1, name = "Alice", email = "alice@test.com", role = UserRole.STUDENT),
        )
        Mockito.`when`(studentRepository.getAllStudents(null, "10"))
            .thenReturn(Result.success(students))

        // When
        viewModel.students.test {
            awaitItem()

            viewModel.loadAllStudents(classId = "10")
            runCurrent()

            // Then
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
