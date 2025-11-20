package com.example.voicetutor.data.network

import org.junit.Assert.*
import org.junit.Test

class ApiServiceModelsTest {

    @Test
    fun recentAnswerData_createsCorrectly() {
        // Given
        val data = RecentAnswerData(personalAssignmentId = 123)

        // Then
        assertEquals(123, data.personalAssignmentId)
    }

    @Test
    fun apiResponse_success_createsCorrectly() {
        // Given
        val data = "test data"
        val response = ApiResponse(
            success = true,
            data = data,
            message = "Success",
            error = null,
        )

        // Then
        assertTrue(response.success)
        assertEquals(data, response.data)
        assertEquals("Success", response.message)
        assertNull(response.error)
    }

    @Test
    fun apiResponse_failure_createsCorrectly() {
        // Given
        val response = ApiResponse<String>(
            success = false,
            data = null,
            message = null,
            error = "Error occurred",
        )

        // Then
        assertFalse(response.success)
        assertNull(response.data)
        assertNull(response.message)
        assertEquals("Error occurred", response.error)
    }

    @Test
    fun createAssignmentRequest_createsCorrectly() {
        // Given
        val request = CreateAssignmentRequest.builder()
            .title("Math Assignment")
            .subject("Mathematics")
            .classId(1)
            .dueAt("2025-12-31T23:59:00Z")
            .grade("A")
            .description("Test assignment")
            .build()

        // Then
        assertEquals("Math Assignment", request.title)
        assertEquals("Mathematics", request.subject)
        assertEquals(1, request.class_id)
        assertEquals("2025-12-31T23:59:00Z", request.due_at)
        assertEquals("A", request.grade)
        assertEquals("Test assignment", request.description)
    }

    @Test
    fun createAssignmentRequest_withNullOptionalFields_createsCorrectly() {
        // Given
        val request = CreateAssignmentRequest.builder()
            .title("Simple Assignment")
            .subject("Math")
            .classId(1)
            .dueAt("2025-12-31T23:59:00Z")
            .grade(null)
            .description(null)
            .build()

        // Then
        assertEquals("Simple Assignment", request.title)
        assertNull(request.grade)
        assertNull(request.description)
    }

    @Test
    fun createAssignmentResponse_createsCorrectly() {
        // Given
        val response = CreateAssignmentResponse(
            assignment_id = 123,
            material_id = 456,
            s3_key = "assignments/123/file.pdf",
            upload_url = "https://s3.example.com/upload",
        )

        // Then
        assertEquals(123, response.assignment_id)
        assertEquals(456, response.material_id)
        assertEquals("assignments/123/file.pdf", response.s3_key)
        assertEquals("https://s3.example.com/upload", response.upload_url)
    }

    @Test
    fun s3UploadStatus_createsCorrectly() {
        // Given
        val status = S3UploadStatus(
            assignment_id = 123,
            material_id = 456,
            s3_key = "assignments/123/file.pdf",
            file_exists = true,
            file_size = 1024L,
            content_type = "application/pdf",
            last_modified = "2025-01-01T00:00:00Z",
            bucket = "my-bucket",
        )

        // Then
        assertEquals(123, status.assignment_id)
        assertEquals(456, status.material_id)
        assertEquals("assignments/123/file.pdf", status.s3_key)
        assertTrue(status.file_exists)
        assertEquals(1024L, status.file_size)
        assertEquals("application/pdf", status.content_type)
        assertEquals("2025-01-01T00:00:00Z", status.last_modified)
        assertEquals("my-bucket", status.bucket)
    }

    @Test
    fun s3UploadStatus_withNullOptionalFields_createsCorrectly() {
        // Given
        val status = S3UploadStatus(
            assignment_id = 123,
            material_id = 456,
            s3_key = "assignments/123/file.pdf",
            file_exists = false,
            file_size = null,
            content_type = null,
            last_modified = null,
            bucket = "my-bucket",
        )

        // Then
        assertFalse(status.file_exists)
        assertNull(status.file_size)
        assertNull(status.content_type)
        assertNull(status.last_modified)
    }

    @Test
    fun questionCreateRequest_createsCorrectly() {
        // Given
        val request = QuestionCreateRequest(
            assignment_id = 123,
            material_id = 456,
            total_number = 10,
        )

        // Then
        assertEquals(123, request.assignment_id)
        assertEquals(456, request.material_id)
        assertEquals(10, request.total_number)
    }

    @Test
    fun createClassRequest_createsCorrectly() {
        // Given
        val request = CreateClassRequest.builder()
            .name("Math Class")
            .description("Advanced Mathematics")
            .subjectName("Mathematics")
            .teacherId(1)
            .build()

        // Then
        assertEquals("Math Class", request.name)
        assertEquals("Advanced Mathematics", request.description)
        assertEquals("Mathematics", request.subject_name)
        assertEquals(1, request.teacher_id)
    }

    @Test
    fun createClassRequest_withNullDescription_createsCorrectly() {
        // Given
        val request = CreateClassRequest.builder()
            .name("Math Class")
            .description(null)
            .subjectName("Mathematics")
            .teacherId(1)
            .build()

        // Then
        assertNull(request.description)
    }

    @Test
    fun updateAssignmentRequest_createsCorrectly() {
        // Given
        val request = UpdateAssignmentRequest.builder()
            .title("Updated Title")
            .description("Updated Description")
            .totalQuestions(20)
            .dueAt("2025-12-31T23:59:00Z")
            .grade("B")
            .subject(SubjectUpdateRequest(id = 1, name = "Math", code = "MATH101"))
            .build()

        // Then
        assertEquals("Updated Title", request.title)
        assertEquals("Updated Description", request.description)
        assertEquals(20, request.totalQuestions)
        assertEquals("2025-12-31T23:59:00Z", request.dueAt)
        assertEquals("B", request.grade)
        assertNotNull(request.subject)
        assertEquals(1, request.subject?.id)
        assertEquals("Math", request.subject?.name)
        assertEquals("MATH101", request.subject?.code)
    }

    @Test
    fun updateAssignmentRequest_withNullFields_createsCorrectly() {
        // Given - Builder requires at least one field, so we use a minimal field
        val request = UpdateAssignmentRequest.builder()
            .title("Minimal")
            .build()

        // Then
        assertEquals("Minimal", request.title)
        assertNull(request.description)
        assertNull(request.totalQuestions)
        assertNull(request.dueAt)
        assertNull(request.grade)
        assertNull(request.subject)
    }

    @Test
    fun subjectUpdateRequest_createsCorrectly() {
        // Given
        val request = SubjectUpdateRequest(
            id = 1,
            name = "Mathematics",
            code = "MATH101",
        )

        // Then
        assertEquals(1, request.id)
        assertEquals("Mathematics", request.name)
        assertEquals("MATH101", request.code)
    }

    @Test
    fun subjectUpdateRequest_withNullFields_createsCorrectly() {
        // Given
        val request = SubjectUpdateRequest()

        // Then
        assertNull(request.id)
        assertNull(request.name)
        assertNull(request.code)
    }
}
