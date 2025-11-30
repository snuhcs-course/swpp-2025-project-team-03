package com.example.voicetutor.ui.viewmodel

import app.cash.turbine.test
import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.network.CreateAssignmentRequest
import com.example.voicetutor.data.network.CreateAssignmentResponse
import com.example.voicetutor.data.network.S3UploadStatus
import com.example.voicetutor.data.repository.AssignmentRepository
import com.example.voicetutor.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.*
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
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

    @Test
    fun assignments_initialState_emitsEmptyList() {
        runTest(mainRule.testDispatcher) {
            viewModel.assignments.test {
                assert(awaitItem().isEmpty())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun loadAllAssignments_success_updatesAssignments() {
        runTest(mainRule.testDispatcher) {
            val returned = listOf(
                sampleAssignment(1, "A"),
                sampleAssignment(2, "B"),
            )

            whenever(repository.getAllAssignments(null, null, null))
                .thenReturn(Result.success(returned))

            viewModel.assignments.test {
                assert(awaitItem().isEmpty())

                viewModel.loadAllAssignments()
                runCurrent()

                assert(awaitItem() == returned)
                cancelAndIgnoreRemainingEvents()
            }

            verify(repository, times(1)).getAllAssignments(null, null, null)
        }
    }

    @Test
    @Ignore("Verification issue")
    fun createAssignmentWithPdf_success_triggersUploadAndQuestionGeneration() {
        runTest(mainRule.testDispatcher) {
            val request = CreateAssignmentRequest.builder()
                .title("t")
                .subject("s")
                .classId(1)
                .dueAt("2025-12-31T23:59:00Z")
                .grade("초6")
                .description("d")
                .build()
            val createResp = CreateAssignmentResponse(
                assignment_id = 10,
                material_id = 20,
                s3_key = "k",
                upload_url = "http://upload",
            )
            val pdf = File.createTempFile("test", ".pdf")

            whenever(repository.createAssignment(request)).thenReturn(Result.success(createResp))
            whenever(repository.uploadPdfToS3(eq(createResp.upload_url), eq(pdf))).thenReturn(Result.success(true))
            whenever(repository.createQuestionsAfterUpload(10, 20, 1)).thenReturn(Result.success(Unit))
            whenever(repository.getAllAssignments(null, null, null)).thenReturn(Result.success(emptyList()))

            viewModel.isUploading.test {
                assert(!awaitItem())
                viewModel.createAssignmentWithPdf(request, pdf, totalNumber = 1)
                runCurrent()
                cancelAndIgnoreRemainingEvents()
            }

            verify(repository, times(1)).createAssignment(request)
            verify(repository, times(1)).uploadPdfToS3(createResp.upload_url, pdf)
            verify(repository, times(1)).createQuestionsAfterUpload(10, 20, 1)
            verify(repository, times(1)).getAllAssignments(null, null, null)
        }
    }

    @Test
    fun checkS3Upload_setsStatus() {
        runTest(mainRule.testDispatcher) {
            val status = S3UploadStatus(
                assignment_id = 10,
                material_id = 20,
                s3_key = "k",
                file_exists = true,
                file_size = 1234,
                content_type = "application/pdf",
                last_modified = "2025-01-01T00:00:00Z",
                bucket = "b",
            )
            whenever(repository.checkS3Upload(10)).thenReturn(Result.success(status))

            viewModel.s3UploadStatus.test {
                assert(awaitItem() == null)
                viewModel.checkS3UploadStatus(10)
                runCurrent()
                assert(awaitItem() == status)
                cancelAndIgnoreRemainingEvents()
            }

            verify(repository, times(1)).checkS3Upload(10)
        }
    }

    @Test
    fun checkS3Upload_failure_setsError() {
        runTest(mainRule.testDispatcher) {
            whenever(repository.checkS3Upload(10))
                .thenReturn(Result.failure(Exception("S3 error")))

            viewModel.error.test {
                assert(awaitItem() == null)
                viewModel.checkS3UploadStatus(10)
                runCurrent()

                val error = awaitItem()
                assert(error?.contains("S3 error") == true)
                cancelAndIgnoreRemainingEvents()
            }
        }
    }

    @Test
    fun createAssignmentWithPdf_failureAtCreate_setsError() {
        runTest(mainRule.testDispatcher) {
            val request = CreateAssignmentRequest.builder()
                .title("t")
                .subject("s")
                .classId(1)
                .dueAt("2025-12-31T23:59:00Z")
                .grade("초6")
                .description("d")
                .build()
            val pdf = File.createTempFile("test", ".pdf")

            whenever(repository.createAssignment(request))
                .thenReturn(Result.failure(Exception("Creation failed")))

            viewModel.error.test {
                assert(awaitItem() == null)
                viewModel.createAssignmentWithPdf(request, pdf, totalNumber = 5)
                runCurrent()

                val error = awaitItem()
                assert(error?.contains("Creation failed") == true)
                cancelAndIgnoreRemainingEvents()
            }

            verify(repository, never()).uploadPdfToS3(any(), any())
            verify(repository, never()).createQuestionsAfterUpload(any(), any(), any())
        }
    }

    @Test
    fun createAssignmentWithPdf_failureAtUpload_setsError() {
        runTest(mainRule.testDispatcher) {
            val request = CreateAssignmentRequest.builder()
                .title("t")
                .subject("s")
                .classId(1)
                .dueAt("2025-12-31T23:59:00Z")
                .grade("초6")
                .description("d")
                .build()
            val createResp = CreateAssignmentResponse(
                assignment_id = 10,
                material_id = 20,
                s3_key = "k",
                upload_url = "http://upload",
            )
            val pdf = File.createTempFile("test", ".pdf")

            whenever(repository.createAssignment(request)).thenReturn(Result.success(createResp))
            whenever(repository.uploadPdfToS3(eq(createResp.upload_url), eq(pdf)))
                .thenReturn(Result.failure(Exception("Upload failed")))

            viewModel.error.test {
                assert(awaitItem() == null)
                viewModel.createAssignmentWithPdf(request, pdf, totalNumber = 5)
                runCurrent()

                val error = awaitItem()
                assert(error?.contains("Upload failed") == true)
                cancelAndIgnoreRemainingEvents()
            }

            verify(repository, times(1)).createAssignment(request)
            verify(repository, times(1)).uploadPdfToS3(createResp.upload_url, pdf)
            verify(repository, never()).createQuestionsAfterUpload(any(), any(), any())
        }
    }

    @Test
    fun loadAllAssignments_withTeacherId_callsRepoWithFilter() {
        runTest(mainRule.testDispatcher) {
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

    @Test
    fun loadAllAssignments_withClassId_callsRepoWithFilter() {
        runTest(mainRule.testDispatcher) {
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

    @Test
    fun loadAllAssignments_withStatus_callsRepoWithFilter() {
        runTest(mainRule.testDispatcher) {
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
    private fun sampleAssignment(id: Int, title: String) = AssignmentData(
        id = id,
        title = title,
        description = "",
        totalQuestions = 3,
        createdAt = null,
        dueAt = "2025-12-31",
        courseClass = CourseClass(
            id = 1,
            name = "class",
            description = "",
            subject = Subject(1, "수학", null),
            teacherName = "teacher",

            studentCount = 0,
            createdAt = "",
        ),
        materials = emptyList(),
        grade = "초6",
    )
}
