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
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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

        // when: PDF 포함 과제 생성을 호출하면
        viewModel.isUploading.test {
            assert(awaitItem() == false)
            viewModel.createAssignmentWithPdf(request, pdf)
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


