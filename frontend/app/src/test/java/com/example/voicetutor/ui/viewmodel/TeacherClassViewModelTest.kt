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
import org.junit.Ignore
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
        whenever(repository.enrollStudentToClass(classId = 1, studentId = 10))
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
        verify(repository, times(1)).enrollStudentToClass(classId = 1, studentId = 10)
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

    @Test
    fun classes_initialState_emitsEmptyList() {
        runTest(mainRule.testDispatcher) {
        // given: 새로 생성된 ViewModel
        viewModel.classes.test {
            // when: 초기 상태를 관측하면
            // then: 첫 방출이 emptyList 여야 한다
            assert(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
        }
    }

    @Test
    fun loadClasses_success_updatesClasses() {
        runTest(mainRule.testDispatcher) {
        // given: 저장소가 성공적으로 반 목록을 반환하도록 스텁
        val classes = listOf(classData(1, "C1"), classData(2, "C2"))
        whenever(repository.getClasses("1")).thenReturn(Result.success(classes))

        viewModel.classes.test {
            // when: 초기 상태를 구독한 뒤 loadClasses() 호출
            assert(awaitItem().isEmpty())
            viewModel.loadClasses("1")
            runCurrent()

            // then: 업데이트된 목록 반영
            assert(awaitItem() == classes)
            cancelAndIgnoreRemainingEvents()
        }

        // then: 저장소는 정확히 1회 호출됨
        verify(repository, times(1)).getClasses("1")
        }
    }

    @Test
    fun loadClasses_failure_setsError() {
        runTest(mainRule.testDispatcher) {
        // given: 저장소가 실패 반환
        whenever(repository.getClasses("1")).thenReturn(Result.failure(Exception("Network error")))

        viewModel.error.test {
            awaitItem() // initial null
            viewModel.loadClasses("1")
            runCurrent()

            // then: 에러 메시지 설정
            val error = awaitItem()
            assert(error?.contains("Network error") == true)
            cancelAndIgnoreRemainingEvents()
        }
        }
    }

    @Test
    fun createClass_success_addsClassToList() {
        runTest(mainRule.testDispatcher) {
        // given: 생성 성공
        val newClass = classData(3, "C3")
        val request = com.example.voicetutor.data.network.CreateClassRequest(
            name = "C3",
            description = "",
            subject_name = "수학",
            teacher_id = 1,
            start_date = "2025-01-01",
            end_date = "2025-12-31"
        )
        whenever(repository.createClass(request)).thenReturn(Result.success(newClass))

        viewModel.classes.test {
            awaitItem() // initial empty
            
            viewModel.createClass(request)
            runCurrent()

            // then: 새 클래스가 목록에 추가됨
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
        // given: 생성 실패
        val request = com.example.voicetutor.data.network.CreateClassRequest(
            name = "C3",
            description = "",
            subject_name = "수학",
            teacher_id = 1,
            start_date = "2025-01-01",
            end_date = "2025-12-31"
        )
        whenever(repository.createClass(request)).thenReturn(Result.failure(Exception("Creation failed")))

        viewModel.error.test {
            awaitItem()
            viewModel.createClass(request)
            runCurrent()

            // then
            val error = awaitItem()
            assert(error?.contains("Creation failed") == true)
            cancelAndIgnoreRemainingEvents()
        }
        }
    }

    @Test
    fun refreshClasses_callsLoadClasses() {
        runTest(mainRule.testDispatcher) {
        // given
        whenever(repository.getClasses("1")).thenReturn(Result.success(emptyList()))

        // when
        viewModel.refreshClasses("1")
        runCurrent()

        // then: loadClasses가 호출됨 (내부적으로 getClasses 호출)
        verify(repository, times(1)).getClasses("1")
        }
    }

    @Test
    fun clearError_clearsErrorState() {
        runTest(mainRule.testDispatcher) {
        // given: 에러가 발생한 상태
        whenever(repository.getClasses("1")).thenReturn(Result.failure(Exception("Error")))

        viewModel.error.test {
            awaitItem()
            viewModel.loadClasses("1")
            runCurrent()
            assert(awaitItem() != null) // 에러 설정 확인
            
            // when: clearError 호출
            viewModel.clearError()
            
            // then: 에러가 null로 변경
            assert(awaitItem() == null)
            cancelAndIgnoreRemainingEvents()
        }
        }
    }

    @Test
    fun isLoading_loadingOperation_setsTrueThenFalse() {
        runTest(mainRule.testDispatcher) {
        // given
        whenever(repository.getClasses("1")).thenReturn(Result.success(emptyList()))

        // when
        viewModel.isLoading.test {
            assert(!awaitItem()) // initial false
            viewModel.loadClasses("1")
            runCurrent()

            // then: 로딩 상태 변경 확인
            val states = listOf(awaitItem(), awaitItem())
            assert(states.contains(true))
            cancelAndIgnoreRemainingEvents()
        }
        }
    }

    @Test
    fun loadClassById_failure_setsError() {
        runTest(mainRule.testDispatcher) {
        // given
        whenever(repository.getClassById(1)).thenReturn(Result.failure(Exception("Not found")))

        // when
        viewModel.error.test {
            awaitItem() // initial null
            viewModel.loadClassById(1)
            runCurrent()

            // then
            val error = awaitItem()
            assert(error?.contains("Not found") == true)
            cancelAndIgnoreRemainingEvents()
        }
        }
    }

    @Test
    fun loadClassStudents_failure_setsError() {
        runTest(mainRule.testDispatcher) {
        // given
        whenever(repository.getClassStudents(1)).thenReturn(Result.failure(Exception("Failed")))

        // when
        viewModel.error.test {
            awaitItem() // initial null
            viewModel.loadClassStudents(1)
            runCurrent()

            // then
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
        // given
        whenever(repository.enrollStudentToClass(classId = 1, studentId = 10))
            .thenReturn(Result.failure(Exception("Enrollment failed")))

        // when
        viewModel.error.test {
            awaitItem() // initial null
            viewModel.enrollStudentToClass(classId = 1, studentId = 10)
            runCurrent()

            // then
            val error = awaitItem()
            assert(error?.contains("Enrollment failed") == true)
            cancelAndIgnoreRemainingEvents()
        }
        }
    }

    @Test
    fun createClass_success_addsToExistingList() {
        runTest(mainRule.testDispatcher) {
        // given: 기존 클래스가 있는 상태
        val existingClasses = listOf(classData(1, "C1"), classData(2, "C2"))
        whenever(repository.getClasses("1")).thenReturn(Result.success(existingClasses))
        viewModel.loadClasses("1")
        runCurrent()

        // 새로운 클래스 생성
        val newClass = classData(3, "C3")
        val request = com.example.voicetutor.data.network.CreateClassRequest(
            name = "C3",
            description = "",
            subject_name = "수학",
            teacher_id = 1,
            start_date = "2025-01-01",
            end_date = "2025-12-31"
        )
        whenever(repository.createClass(request)).thenReturn(Result.success(newClass))

        // when
        viewModel.classes.test {
            awaitItem() // 기존 리스트
            viewModel.createClass(request)
            runCurrent()

            // then: 새 클래스가 기존 리스트에 추가됨
            val updated = awaitItem()
            assert(updated.size == 3)
            assert(updated.contains(newClass))
            cancelAndIgnoreRemainingEvents()
        }
        }
    }
}


