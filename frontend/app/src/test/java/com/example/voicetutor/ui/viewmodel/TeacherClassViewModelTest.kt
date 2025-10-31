package com.example.voicetutor.ui.viewmodel

import app.cash.turbine.test
import com.example.voicetutor.data.models.ClassData
import com.example.voicetutor.data.models.Student
import com.example.voicetutor.data.models.Subject
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.data.repository.ClassRepository
import com.example.voicetutor.testing.MainDispatcherRule
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TeacherClassViewModelTest {

    private val repository: ClassRepository = mock()
    private lateinit var viewModel: ClassViewModel
    @get:Rule
    val mainRule = MainDispatcherRule()

    @Before
    fun setUp() {
        viewModel = ClassViewModel(repository)
    }

    @Test
    fun loadClassById_and_loadClassStudents_updatesStates() {
        runTest(mainRule.testDispatcher) {
        // given: 반 상세/학생 목록을 성공 반환하도록 스텁
        val cd = classData(1, "C1")
        val students = listOf(student(10), student(11))
        whenever(repository.getClassById(1)).thenReturn(Result.success(cd))
        whenever(repository.getClassStudents(1)).thenReturn(Result.success(students))

        viewModel.currentClass.test {
            // when: 상세 호출
            assert(awaitItem() == null)
            viewModel.loadClassById(1)
            runCurrent()
            // then: currentClass 업데이트
            assert(awaitItem() == cd)
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.classStudents.test {
            // when: 학생 목록 호출
            assert(awaitItem().isEmpty())
            viewModel.loadClassStudents(1)
            runCurrent()
            // then: 목록 반영
            assert(awaitItem() == students)
            cancelAndIgnoreRemainingEvents()
        }

        // then: 저장소 각 1회 호출
        verify(repository, times(1)).getClassById(1)
        verify(repository, times(1)).getClassStudents(1)
        }
    }

    @Test
    fun enrollStudentToClass_callsRepo_thenRefreshesStudents() {
        runTest(mainRule.testDispatcher) {
        // given: 등록 성공, 이후 목록은 1명으로 반환
        whenever(repository.enrollStudentToClass(classId = 1, studentId = 10, name = null, email = null))
            .thenReturn(Result.success(
                com.example.voicetutor.data.models.EnrollmentData(
                    student = student(10),
                    courseClass = classData(1, "C1"),
                    status = "ENROLLED"
                )
            ))
        whenever(repository.getClassStudents(1)).thenReturn(Result.success(listOf(student(10))))

        // when: 등록 호출
        viewModel.enrollStudentToClass(classId = 1, studentId = 10)
        runCurrent()

        // then: 등록 1회 + 목록 재로드 1회
        verify(repository, times(1)).enrollStudentToClass(classId = 1, studentId = 10, name = null, email = null)
        verify(repository, times(1)).getClassStudents(1)
        }
    }

    // helpers
    private fun classData(id: Int, name: String) = ClassData(
        id = id,
        name = name,
        subject = Subject(1, "수학", null),
        description = "",
        teacherId = 1,
        studentCount = 0,
        createdAt = null,
        startDate = null,
        endDate = null
    )

    private fun student(id: Int) = Student(
        id = id,
        name = "s$id",
        email = "s$id@test.com",
        role = UserRole.STUDENT
    )
}


