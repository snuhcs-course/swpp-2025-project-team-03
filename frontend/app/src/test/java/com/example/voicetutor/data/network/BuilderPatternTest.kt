package com.example.voicetutor.data.network

import org.junit.Assert.*
import org.junit.Test

class BuilderPatternTest {

    @Test
    fun createAssignmentRequest_builder_success() {
        val request = CreateAssignmentRequest.builder()
            .title("Test Assignment")
            .subject("Math")
            .classId(1)
            .dueAt("2025-12-31T23:59:59Z")
            .grade("1학년")
            .description("Test Description")
            .totalQuestions(10)
            .build()
        assertEquals("Test Assignment", request.title)
        assertEquals("Math", request.subject)
        assertEquals(1, request.class_id)
        assertEquals("2025-12-31T23:59:59Z", request.due_at)
        assertEquals("1학년", request.grade)
        assertEquals("Test Description", request.description)
        assertEquals(10, request.total_questions)
    }

    @Test
    fun createAssignmentRequest_builder_withNullOptionalFields() {
        val request = CreateAssignmentRequest.builder()
            .title("Test Assignment")
            .subject("Math")
            .classId(1)
            .dueAt("2025-12-31T23:59:59Z")
            .build()
        assertNull(request.grade)
        assertNull(request.description)
        assertNull(request.total_questions)
    }

    @Test(expected = IllegalArgumentException::class)
    fun createAssignmentRequest_builder_missingTitle_throwsException() {
        CreateAssignmentRequest.builder()
            .subject("Math")
            .classId(1)
            .dueAt("2025-12-31T23:59:59Z")
            .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun createAssignmentRequest_builder_missingSubject_throwsException() {
        CreateAssignmentRequest.builder()
            .title("Test Assignment")
            .classId(1)
            .dueAt("2025-12-31T23:59:59Z")
            .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun createAssignmentRequest_builder_missingClassId_throwsException() {
        CreateAssignmentRequest.builder()
            .title("Test Assignment")
            .subject("Math")
            .dueAt("2025-12-31T23:59:59Z")
            .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun createAssignmentRequest_builder_missingDueAt_throwsException() {
        CreateAssignmentRequest.builder()
            .title("Test Assignment")
            .subject("Math")
            .classId(1)
            .build()
    }

    @Test
    fun createClassRequest_builder_success() {
        val request = CreateClassRequest.builder()
            .name("Math Class")
            .description("Math Description")
            .subjectName("Math")
            .teacherId(1)
            .build()
        assertEquals("Math Class", request.name)
        assertEquals("Math Description", request.description)
        assertEquals("Math", request.subject_name)
        assertEquals(1, request.teacher_id)
    }

    @Test
    fun createClassRequest_builder_withNullDescription() {
        val request = CreateClassRequest.builder()
            .name("Math Class")
            .subjectName("Math")
            .teacherId(1)
            .build()
        assertNull(request.description)
    }

    @Test(expected = IllegalArgumentException::class)
    fun createClassRequest_builder_missingName_throwsException() {
        CreateClassRequest.builder()
            .subjectName("Math")
            .teacherId(1)
            .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun createClassRequest_builder_missingSubjectName_throwsException() {
        CreateClassRequest.builder()
            .name("Math Class")
            .teacherId(1)
            .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun createClassRequest_builder_missingTeacherId_throwsException() {
        CreateClassRequest.builder()
            .name("Math Class")
            .subjectName("Math")
            .build()
    }

    @Test
    fun updateAssignmentRequest_builder_success() {
        val request = UpdateAssignmentRequest.builder()
            .title("Updated Title")
            .description("Updated Description")
            .totalQuestions(20)
            .dueAt("2025-12-31T23:59:59Z")
            .grade("2학년")
            .subject(SubjectUpdateRequest(id = 1, name = "Science", code = "SCI"))
            .build()
        assertEquals("Updated Title", request.title)
        assertEquals("Updated Description", request.description)
        assertEquals(20, request.totalQuestions)
        assertEquals("2025-12-31T23:59:59Z", request.dueAt)
        assertEquals("2학년", request.grade)
        assertNotNull(request.subject)
    }

    @Test
    fun updateAssignmentRequest_builder_withSingleField() {
        val request = UpdateAssignmentRequest.builder()
            .title("Updated Title")
            .build()
        assertEquals("Updated Title", request.title)
        assertNull(request.description)
    }

    @Test(expected = IllegalArgumentException::class)
    fun updateAssignmentRequest_builder_allFieldsNull_throwsException() {
        UpdateAssignmentRequest.builder()
            .build()
    }

    @Test
    fun updateAssignmentRequest_builder_withSubjectUpdateRequest() {
        val subjectUpdate = SubjectUpdateRequest(id = 1, name = "Science", code = "SCI")
        val request = UpdateAssignmentRequest.builder()
            .subject(subjectUpdate)
            .build()
        assertEquals(subjectUpdate, request.subject)
    }

    @Test
    fun subjectUpdateRequest_withAllFields() {
        val request = SubjectUpdateRequest(id = 1, name = "Science", code = "SCI")
        assertEquals(1, request.id)
        assertEquals("Science", request.name)
        assertEquals("SCI", request.code)
    }

    @Test
    fun subjectUpdateRequest_withNullFields() {
        val request = SubjectUpdateRequest()
        assertNull(request.id)
        assertNull(request.name)
        assertNull(request.code)
    }

    @Test
    fun createAssignmentRequest_directConstructor_initializesCorrectly() {
        val request = CreateAssignmentRequest(
            title = "Direct Assignment",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Direct Description",
            total_questions = 10,
        )
        assertEquals("Direct Assignment", request.title)
        assertEquals("Math", request.subject)
        assertEquals(1, request.class_id)
        assertEquals("2025-12-31T23:59:59Z", request.due_at)
        assertEquals("1학년", request.grade)
        assertEquals("Direct Description", request.description)
        assertEquals(10, request.total_questions)
    }

    @Test
    fun createAssignmentRequest_directConstructor_withNullOptionalFields() {
        val request = CreateAssignmentRequest(
            title = "Direct Assignment",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = null,
            description = null,
            total_questions = null,
        )
        assertNull(request.grade)
        assertNull(request.description)
        assertNull(request.total_questions)
    }

    @Test
    fun createAssignmentRequest_companionBuilder_returnsBuilder() {
        val builder = CreateAssignmentRequest.builder()
        assertNotNull(builder)
    }

    @Test
    fun createAssignmentRequest_builder_allMethodsChained() {
        val request = CreateAssignmentRequest.builder()
            .title("Test")
            .subject("Math")
            .classId(1)
            .dueAt("2025-12-31T23:59:59Z")
            .grade("1학년")
            .description("Desc")
            .totalQuestions(10)
            .build()
        assertEquals("Test", request.title)
        assertEquals("Math", request.subject)
        assertEquals(1, request.class_id)
        assertEquals("2025-12-31T23:59:59Z", request.due_at)
        assertEquals("1학년", request.grade)
        assertEquals("Desc", request.description)
        assertEquals(10, request.total_questions)
    }

    @Test
    fun updateAssignmentRequest_directConstructor_initializesCorrectly() {
        val request = UpdateAssignmentRequest(
            title = "Updated Title",
            description = "Updated Description",
            totalQuestions = 20,
            dueAt = "2025-12-31T23:59:59Z",
            grade = "2학년",
            subject = SubjectUpdateRequest(id = 1, name = "Science", code = "SCI"),
        )
        assertEquals("Updated Title", request.title)
        assertEquals("Updated Description", request.description)
        assertEquals(20, request.totalQuestions)
        assertEquals("2025-12-31T23:59:59Z", request.dueAt)
        assertEquals("2학년", request.grade)
        assertNotNull(request.subject)
    }

    @Test
    fun updateAssignmentRequest_directConstructor_withAllNullFields() {
        val request = UpdateAssignmentRequest()
        assertNull(request.title)
        assertNull(request.description)
        assertNull(request.totalQuestions)
        assertNull(request.dueAt)
        assertNull(request.grade)
        assertNull(request.subject)
    }

    @Test
    fun updateAssignmentRequest_builder_withOnlyDescription() {
        val request = UpdateAssignmentRequest.builder()
            .description("Only Description")
            .build()
        assertNull(request.title)
        assertEquals("Only Description", request.description)
        assertNull(request.totalQuestions)
    }

    @Test
    fun updateAssignmentRequest_builder_withOnlyTotalQuestions() {
        val request = UpdateAssignmentRequest.builder()
            .totalQuestions(15)
            .build()
        assertNull(request.title)
        assertNull(request.description)
        assertEquals(15, request.totalQuestions)
    }

    @Test
    fun updateAssignmentRequest_builder_withOnlyDueAt() {
        val request = UpdateAssignmentRequest.builder()
            .dueAt("2025-12-31T23:59:59Z")
            .build()
        assertNull(request.title)
        assertEquals("2025-12-31T23:59:59Z", request.dueAt)
    }

    @Test
    fun updateAssignmentRequest_builder_withOnlyGrade() {
        val request = UpdateAssignmentRequest.builder()
            .grade("3학년")
            .build()
        assertNull(request.title)
        assertEquals("3학년", request.grade)
    }

    @Test
    fun updateAssignmentRequest_builder_withOnlySubject() {
        val subjectUpdate = SubjectUpdateRequest(id = 2, name = "English", code = "ENG")
        val request = UpdateAssignmentRequest.builder()
            .subject(subjectUpdate)
            .build()
        assertNull(request.title)
        assertEquals(subjectUpdate, request.subject)
    }

    @Test
    fun updateAssignmentRequest_builder_withMultipleFields() {
        val request = UpdateAssignmentRequest.builder()
            .title("Title")
            .description("Description")
            .build()
        assertEquals("Title", request.title)
        assertEquals("Description", request.description)
    }

    @Test
    fun updateAssignmentRequest_companionBuilder_returnsBuilder() {
        val builder = UpdateAssignmentRequest.builder()
        assertNotNull(builder)
    }

    @Test
    fun updateAssignmentRequest_builder_allMethodsChained() {
        val subjectUpdate = SubjectUpdateRequest(id = 1, name = "Science", code = "SCI")
        val request = UpdateAssignmentRequest.builder()
            .title("Title")
            .description("Description")
            .totalQuestions(20)
            .dueAt("2025-12-31T23:59:59Z")
            .grade("2학년")
            .subject(subjectUpdate)
            .build()
        assertEquals("Title", request.title)
        assertEquals("Description", request.description)
        assertEquals(20, request.totalQuestions)
        assertEquals("2025-12-31T23:59:59Z", request.dueAt)
        assertEquals("2학년", request.grade)
        assertEquals(subjectUpdate, request.subject)
    }

    @Test
    fun createAssignmentRequest_copy_createsNewInstance() {
        val original = CreateAssignmentRequest(
            title = "Original",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Original Description",
            total_questions = 10,
        )
        val copied = original.copy(
            title = "Copied",
            description = "Copied Description",
        )
        assertEquals("Copied", copied.title)
        assertEquals("Original", original.title)
        assertEquals("Math", copied.subject)
        assertEquals(1, copied.class_id)
        assertEquals("Copied Description", copied.description)
        assertEquals("Original Description", original.description)
    }

    @Test
    fun createAssignmentRequest_copy_withAllParameters() {
        val original = CreateAssignmentRequest(
            title = "Original",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Original Description",
            total_questions = 10,
        )
        val copied = original.copy(
            title = "New Title",
            subject = "Science",
            class_id = 2,
            due_at = "2026-01-01T00:00:00Z",
            grade = "2학년",
            description = "New Description",
            total_questions = 20,
        )
        assertEquals("New Title", copied.title)
        assertEquals("Science", copied.subject)
        assertEquals(2, copied.class_id)
        assertEquals("2026-01-01T00:00:00Z", copied.due_at)
        assertEquals("2학년", copied.grade)
        assertEquals("New Description", copied.description)
        assertEquals(20, copied.total_questions)
    }

    @Test
    fun createAssignmentRequest_copy_withNullParameters() {
        val original = CreateAssignmentRequest(
            title = "Original",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Original Description",
            total_questions = 10,
        )
        val copied = original.copy(
            grade = null,
            description = null,
            total_questions = null,
        )
        assertEquals("Original", copied.title)
        assertNull(copied.grade)
        assertNull(copied.description)
        assertNull(copied.total_questions)
    }

    @Test
    fun createAssignmentRequest_destructuring_decomposesCorrectly() {
        val request = CreateAssignmentRequest(
            title = "Test",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Description",
            total_questions = 10,
        )
        val (title, subject, classId, dueAt, grade, description, totalQuestions) = request
        assertEquals("Test", title)
        assertEquals("Math", subject)
        assertEquals(1, classId)
        assertEquals("2025-12-31T23:59:59Z", dueAt)
        assertEquals("1학년", grade)
        assertEquals("Description", description)
        assertEquals(10, totalQuestions)
    }

    @Test
    fun createAssignmentRequest_equals_worksCorrectly() {
        val request1 = CreateAssignmentRequest(
            title = "Test",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Description",
            total_questions = 10,
        )
        val request2 = CreateAssignmentRequest(
            title = "Test",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Description",
            total_questions = 10,
        )
        val request3 = CreateAssignmentRequest(
            title = "Different",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Description",
            total_questions = 10,
        )
        assertEquals(request1, request2)
        assertNotEquals(request1, request3)
    }

    @Test
    fun createAssignmentRequest_hashCode_worksCorrectly() {
        val request1 = CreateAssignmentRequest(
            title = "Test",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Description",
            total_questions = 10,
        )
        val request2 = CreateAssignmentRequest(
            title = "Test",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Description",
            total_questions = 10,
        )
        assertEquals(request1.hashCode(), request2.hashCode())
    }

    @Test
    fun createAssignmentRequest_toString_containsAllFields() {
        val request = CreateAssignmentRequest(
            title = "Test",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Description",
            total_questions = 10,
        )
        val toString = request.toString()
        assertTrue(toString.contains("Test"))
        assertTrue(toString.contains("Math"))
        assertTrue(toString.contains("1"))
    }
}
