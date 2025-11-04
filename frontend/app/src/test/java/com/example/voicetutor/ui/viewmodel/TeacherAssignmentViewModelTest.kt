package com.example.voicetutor.ui.viewmodel

import app.cash.turbine.test
import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.network.CreateAssignmentRequest
import com.example.voicetutor.data.network.CreateAssignmentResponse
import com.example.voicetutor.data.network.S3UploadStatus
import com.example.voicetutor.data.repository.AssignmentRepository
import com.example.voicetutor.testing.MainDispatcherRule
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import java.io.File

@RunWith(MockitoJUnitRunner::class)
class TeacherAssignmentViewModelTest {

    @Mock
    lateinit var repository: AssignmentRepository
    private lateinit var viewModel: AssignmentViewModel

    @get:Rule
    val mainRule = MainDispatcherRule()

    @Before
    fun setUp() {
        viewModel = AssignmentViewModel(repository)
    }

    // 1) 초기 상태 검증
    @Test
    fun assignments_initialState_emitsEmptyList() {
        runTest(mainRule.testDispatcher) {
        // given: 새로 생성된 ViewModel
        viewModel.assignments.test {
            // when: 아무 동작도 하지 않은 초기 상태를 관측하면
            // then: 첫 방출이 emptyList 여야 한다
            assert(awaitItem().isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
        }
    }

    // 2) 전체 과제 로드 성공 → 리스트 반영
    @Test
    fun loadAllAssignments_success_updatesAssignments() {
        runTest(mainRule.testDispatcher) {
        // given: 저장소가 성공적으로 목록을 반환하도록 스텁
        val returned = listOf(
            sampleAssignment(1, "A"),
            sampleAssignment(2, "B"),
        )

        whenever(repository.getAllAssignments(null, null, null))
            .thenReturn(Result.success(returned))

        viewModel.assignments.test {
            // when: 초기 상태를 구독한 뒤 loadAllAssignments() 호출
            // then: [] -> list 순서로 방출된다
            assert(awaitItem().isEmpty())

            viewModel.loadAllAssignments()
            runCurrent()

            // 업데이트된 리스트
            assert(awaitItem() == returned)
            cancelAndIgnoreRemainingEvents()
        }

        // then: 저장소는 정확히 1회 호출됨
        verify(repository, times(1)).getAllAssignments(null, null, null)
        }
    }

    // 3) PDF 포함 생성 성공 경로: 업로드/질문생성 트리거/리프레시 호출
    @Test
    fun createAssignmentWithPdf_success_triggersUploadAndQuestionGeneration() {
        runTest(mainRule.testDispatcher) {
        // given: 생성/업로드/질문생성이 모두 성공하도록 저장소 스텁
        val request = CreateAssignmentRequest(
            title = "t",
            subject = "s",
            class_id = 1,
            due_at = "2025-12-31T23:59:00Z",
            grade = "초6",
            type = "Quiz",
            description = "d",
            questions = listOf(
                QuestionData(
                    id = 1,
                    question = "q1",
                    type = "VOICE_RESPONSE",
                    options = null,
                    correctAnswer = "a",
                    points = 1,
                    explanation = "e"
                )
            )
        )
        val createResp = CreateAssignmentResponse(
            assignment_id = 10, material_id = 20, s3_key = "k", upload_url = "http://upload"
        )
        val pdf = File.createTempFile("test", ".pdf")

        whenever(repository.createAssignment(request)).thenReturn(Result.success(createResp))
        whenever(repository.uploadPdfToS3(eq(createResp.upload_url), eq(pdf))).thenReturn(Result.success(true))
        whenever(repository.createQuestionsAfterUpload(10, 20, 1)).thenReturn(Result.success(Unit))
        // 목록 새로고침 시 빈 리스트로 가정
        whenever(repository.getAllAssignments(null, null, null)).thenReturn(Result.success(emptyList()))

        // when: PDF 포함 과제 생성을 호출하면 (totalNumber를 명시적으로 전달)
        viewModel.isUploading.test {
            assert(awaitItem() == false)
            viewModel.createAssignmentWithPdf(request, pdf, totalNumber = 1)
            runCurrent()
            // then: 업로드 상태 전이는 별도 테스트에서 상세 검증(여기서는 호출 여부 위주)
            cancelAndIgnoreRemainingEvents()
        }

        // then: 생성 → 업로드 → 질문생성 → 목록리프레시 호출 순서로 각 1회 호출
        verify(repository, times(1)).createAssignment(request)
        verify(repository, times(1)).uploadPdfToS3(createResp.upload_url, pdf)
        verify(repository, times(1)).createQuestionsAfterUpload(10, 20, 1)
        verify(repository, times(1)).getAllAssignments(null, null, null)
        }
    }

    // 4) S3 업로드 상태 확인
    @Test
    fun checkS3Upload_setsStatus() {
        runTest(mainRule.testDispatcher) {
        // given: 저장소가 S3 상태를 성공적으로 반환
        val status = S3UploadStatus(
            assignment_id = 10,
            material_id = 20,
            s3_key = "k",
            file_exists = true,
            file_size = 1234,
            content_type = "application/pdf",
            last_modified = "2025-01-01T00:00:00Z",
            bucket = "b"
        )
        whenever(repository.checkS3Upload(10)).thenReturn(Result.success(status))

        viewModel.s3UploadStatus.test {
            // when: 체크 호출
            assert(awaitItem() == null)
            viewModel.checkS3UploadStatus(10)
            runCurrent()
            // then: 상태 값이 반영된다
            assert(awaitItem() == status)
            cancelAndIgnoreRemainingEvents()
        }

        // then: 저장소 1회 호출
        verify(repository, times(1)).checkS3Upload(10)
        }
    }

    // 5) S3 업로드 상태 확인 실패
    @Test
    fun checkS3Upload_failure_setsError() {
        runTest(mainRule.testDispatcher) {
        // given: 저장소가 실패를 반환
        whenever(repository.checkS3Upload(10))
            .thenReturn(Result.failure(Exception("S3 error")))

        viewModel.error.test {
            // when: 체크 호출
            assert(awaitItem() == null)
            viewModel.checkS3UploadStatus(10)
            runCurrent()
            
            // then: 에러 메시지가 설정됨
            val error = awaitItem()
            assert(error?.contains("S3 error") == true)
            cancelAndIgnoreRemainingEvents()
        }
        }
    }

    // 6) PDF 업로드 실패 (과제 생성 단계)
    @Test
    fun createAssignmentWithPdf_failureAtCreate_setsError() {
        runTest(mainRule.testDispatcher) {
        // given: 과제 생성이 실패하도록 스텁
        val request = CreateAssignmentRequest(
            title = "t",
            subject = "s",
            class_id = 1,
            due_at = "2025-12-31T23:59:00Z",
            grade = "초6",
            type = "Quiz",
            description = "d",
            questions = null
        )
        val pdf = File.createTempFile("test", ".pdf")

        whenever(repository.createAssignment(request))
            .thenReturn(Result.failure(Exception("Creation failed")))

        viewModel.error.test {
            // when: PDF 포함 과제 생성을 호출
            assert(awaitItem() == null)
            viewModel.createAssignmentWithPdf(request, pdf, totalNumber = 5)
            runCurrent()
            
            // then: 에러 메시지가 설정됨
            val error = awaitItem()
            assert(error?.contains("Creation failed") == true)
            cancelAndIgnoreRemainingEvents()
        }

        // then: 업로드나 질문 생성은 호출되지 않음
        verify(repository, never()).uploadPdfToS3(any(), any())
        verify(repository, never()).createQuestionsAfterUpload(any(), any(), any())
        }
    }

    // 7) PDF 업로드 실패 (S3 업로드 단계)
    @Test
    fun createAssignmentWithPdf_failureAtUpload_setsError() {
        runTest(mainRule.testDispatcher) {
        // given: 과제 생성은 성공하지만 S3 업로드가 실패
        val request = CreateAssignmentRequest(
            title = "t",
            subject = "s",
            class_id = 1,
            due_at = "2025-12-31T23:59:00Z",
            grade = "초6",
            type = "Quiz",
            description = "d",
            questions = null
        )
        val createResp = CreateAssignmentResponse(
            assignment_id = 10, material_id = 20, s3_key = "k", upload_url = "http://upload"
        )
        val pdf = File.createTempFile("test", ".pdf")

        whenever(repository.createAssignment(request)).thenReturn(Result.success(createResp))
        whenever(repository.uploadPdfToS3(eq(createResp.upload_url), eq(pdf)))
            .thenReturn(Result.failure(Exception("Upload failed")))

        viewModel.error.test {
            // when: PDF 포함 과제 생성을 호출
            assert(awaitItem() == null)
            viewModel.createAssignmentWithPdf(request, pdf, totalNumber = 5)
            runCurrent()
            
            // then: 에러 메시지가 설정됨
            val error = awaitItem()
            assert(error?.contains("Upload failed") == true)
            cancelAndIgnoreRemainingEvents()
        }

        // then: 질문 생성은 호출되지 않음
        verify(repository, times(1)).createAssignment(request)
        verify(repository, times(1)).uploadPdfToS3(createResp.upload_url, pdf)
        verify(repository, never()).createQuestionsAfterUpload(any(), any(), any())
        }
    }

    // 8) 필터링 테스트 - teacherId
    @Test
    fun loadAllAssignments_withTeacherId_callsRepoWithFilter() {
        runTest(mainRule.testDispatcher) {
        // given: teacherId 필터 적용
        val returned = listOf(sampleAssignment(1, "A"))
        whenever(repository.getAllAssignments("1", null, null))
            .thenReturn(Result.success(returned))

        viewModel.assignments.test {
            assert(awaitItem().isEmpty())
            
            viewModel.loadAllAssignments(teacherId = "1")
            runCurrent()

            assert(awaitItem() == returned)
            cancelAndIgnoreRemainingEvents()
        }

        verify(repository, times(1)).getAllAssignments("1", null, null)
        }
    }

    // 9) 필터링 테스트 - classId
    @Test
    fun loadAllAssignments_withClassId_callsRepoWithFilter() {
        runTest(mainRule.testDispatcher) {
        // given: classId 필터 적용
        val returned = listOf(sampleAssignment(1, "A"))
        whenever(repository.getAllAssignments(null, "10", null))
            .thenReturn(Result.success(returned))

        viewModel.assignments.test {
            assert(awaitItem().isEmpty())
            
            viewModel.loadAllAssignments(classId = "10")
            runCurrent()

            assert(awaitItem() == returned)
            cancelAndIgnoreRemainingEvents()
        }

        verify(repository, times(1)).getAllAssignments(null, "10", null)
        }
    }

    // 10) 필터링 테스트 - status
    @Test
    fun loadAllAssignments_withStatus_callsRepoWithFilter() {
        runTest(mainRule.testDispatcher) {
        // given: status 필터 적용
        val returned = listOf(sampleAssignment(1, "A"))
        whenever(repository.getAllAssignments(null, null, AssignmentStatus.IN_PROGRESS))
            .thenReturn(Result.success(returned))

        viewModel.assignments.test {
            assert(awaitItem().isEmpty())
            
            viewModel.loadAllAssignments(status = AssignmentStatus.IN_PROGRESS)
            runCurrent()

            assert(awaitItem() == returned)
            cancelAndIgnoreRemainingEvents()
        }

        verify(repository, times(1)).getAllAssignments(null, null, AssignmentStatus.IN_PROGRESS)
        }
    }

    // 7) 과제 결과 로드 성공 (주석 처리된 API용)
    // @Test
    // fun loadAssignmentResults_success_updatesResults() {
    //     runTest(mainRule.testDispatcher) {
    //     // given: 저장소가 성공적으로 결과를 반환
    //     val results = listOf(
    //         StudentResult(
    //             studentId = "1",
    //             name = "Student 1",
    //             submittedAt = "2025-01-01",
    //             score = 85,
    //             status = "SUBMITTED"
    //         ),
    //         StudentResult(
    //             studentId = "2",
    //             name = "Student 2",
    //             submittedAt = "2025-01-02",
    //             score = 90,
    //             status = "SUBMITTED"
    //         )
    //     )
    //     whenever(repository.getAssignmentResults(10)).thenReturn(Result.success(results))
    //
    //     viewModel.assignmentResults.test {
    //         // when: 초기 상태를 구독한 뒤 loadAssignmentResults() 호출
    //         assert(awaitItem().isEmpty())
    //         
    //         viewModel.loadAssignmentResults(10)
    //         runCurrent()
    //         
    //         // then: 결과 목록이 업데이트됨
    //         assert(awaitItem() == results)
    //         cancelAndIgnoreRemainingEvents()
    //     }
    //
    //     // then: 저장소는 정확히 1회 호출됨
    //     verify(repository, times(1)).getAssignmentResults(10)
    //     }
    // }

    // 8) 과제 결과 로드 실패 (주석 처리된 API용)
    // @Test
    // fun loadAssignmentResults_failure_setsError() {
    //     runTest(mainRule.testDispatcher) {
    //     // given: 저장소가 실패를 반환
    //     whenever(repository.getAssignmentResults(10))
    //         .thenReturn(Result.failure(Exception("Network error")))
    //
    //     viewModel.error.test {
    //         // when: 초기 상태를 구독한 뒤 loadAssignmentResults() 호출
    //         assert(awaitItem() == null)
    //         
    //         viewModel.loadAssignmentResults(10)
    //         runCurrent()
    //         
    //         // then: 에러 메시지가 설정됨
    //         val error = awaitItem()
    //         assert(error?.contains("Network error") == true)
    //         cancelAndIgnoreRemainingEvents()
    //     }
    //     }
    // }

    // 9) 기본 문제 로드 성공 (주석 처리된 API용)
    // @Test
    // fun loadAssignmentQuestions_success_updatesQuestions() {
    //     runTest(mainRule.testDispatcher) {
    //     // given: 저장소가 성공적으로 문제 목록을 반환
    //     val questions = listOf(
    //         QuestionData(
    //             id = 1,
    //             question = "Q1",
    //             type = "VOICE_RESPONSE",
    //             options = null,
    //             correctAnswer = "A1",
    //             points = 10,
    //             explanation = "E1"
    //         ),
    //         QuestionData(
    //             id = 2,
    //             question = "Q2",
    //             type = "VOICE_RESPONSE",
    //             options = null,
    //             correctAnswer = "A2",
    //             points = 10,
    //             explanation = "E2"
    //         )
    //     )
    //     whenever(repository.getAssignmentQuestions(10)).thenReturn(Result.success(questions))
    //
    //     viewModel.assignmentQuestions.test {
    //         // when: 초기 상태를 구독한 뒤 loadAssignmentQuestions() 호출
    //         assert(awaitItem().isEmpty())
    //         
    //         viewModel.loadAssignmentQuestions(10)
    //         runCurrent()
    //         
    //         // then: 문제 목록이 업데이트됨
    //         assert(awaitItem() == questions)
    //         cancelAndIgnoreRemainingEvents()
    //     }
    //
    //     // then: 저장소는 정확히 1회 호출됨
    //     verify(repository, times(1)).getAssignmentQuestions(10)
    //     }
    // }

    // 10) 기본 문제 로드 실패 (주석 처리된 API용)
    // @Test
    // fun loadAssignmentQuestions_failure_setsError() {
    //     runTest(mainRule.testDispatcher) {
    //     // given: 저장소가 실패를 반환
    //     whenever(repository.getAssignmentQuestions(10))
    //         .thenReturn(Result.failure(Exception("Not found")))
    //
    //     viewModel.error.test {
    //         // when: 초기 상태를 구독한 뒤 loadAssignmentQuestions() 호출
    //         assert(awaitItem() == null)
    //         
    //         viewModel.loadAssignmentQuestions(10)
    //         runCurrent()
    //         
    //         // then: 에러 메시지가 설정됨
    //         val error = awaitItem()
    //         assert(error?.contains("Not found") == true)
    //         cancelAndIgnoreRemainingEvents()
    //     }
    //     }
    // }

    // ----------------------
    // helpers
    private fun sampleAssignment(id: Int, title: String) = AssignmentData(
        id = id,
        title = title,
        description = "",
        totalQuestions = 3,
        createdAt = null,
        visibleFrom = null,
        dueAt = "2025-12-31",
        courseClass = CourseClass(
            id = 1,
            name = "class",
            description = "",
            subject = Subject(1, "수학", null),
            teacherName = "teacher",
            startDate = "",
            endDate = "",
            studentCount = 0,
            createdAt = ""
        ),
        materials = emptyList(),
        grade = "초6"
    )
}
