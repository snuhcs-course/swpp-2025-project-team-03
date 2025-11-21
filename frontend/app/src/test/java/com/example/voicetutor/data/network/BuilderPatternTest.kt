package com.example.voicetutor.data.network

import org.junit.Assert.*
import org.junit.Test

class BuilderPatternTest {

    @Test
    fun createAssignmentRequest_builder_success() {
        // Act
        val request = CreateAssignmentRequest.builder()
            .title("Test Assignment")
            .subject("Math")
            .classId(1)
            .dueAt("2025-12-31T23:59:59Z")
            .grade("1학년")
            .description("Test Description")
            .totalQuestions(10)
            .build()

        // Assert
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
        // Act
        val request = CreateAssignmentRequest.builder()
            .title("Test Assignment")
            .subject("Math")
            .classId(1)
            .dueAt("2025-12-31T23:59:59Z")
            .build()

        // Assert
        assertNull(request.grade)
        assertNull(request.description)
        assertNull(request.total_questions)
    }

    @Test(expected = IllegalArgumentException::class)
    fun createAssignmentRequest_builder_missingTitle_throwsException() {
        // Act
        CreateAssignmentRequest.builder()
            .subject("Math")
            .classId(1)
            .dueAt("2025-12-31T23:59:59Z")
            .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun createAssignmentRequest_builder_missingSubject_throwsException() {
        // Act
        CreateAssignmentRequest.builder()
            .title("Test Assignment")
            .classId(1)
            .dueAt("2025-12-31T23:59:59Z")
            .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun createAssignmentRequest_builder_missingClassId_throwsException() {
        // Act
        CreateAssignmentRequest.builder()
            .title("Test Assignment")
            .subject("Math")
            .dueAt("2025-12-31T23:59:59Z")
            .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun createAssignmentRequest_builder_missingDueAt_throwsException() {
        // Act
        CreateAssignmentRequest.builder()
            .title("Test Assignment")
            .subject("Math")
            .classId(1)
            .build()
    }

    @Test
    fun createClassRequest_builder_success() {
        // Act
        val request = CreateClassRequest.builder()
            .name("Math Class")
            .description("Math Description")
            .subjectName("Math")
            .teacherId(1)
            .build()

        // Assert
        assertEquals("Math Class", request.name)
        assertEquals("Math Description", request.description)
        assertEquals("Math", request.subject_name)
        assertEquals(1, request.teacher_id)
    }

    @Test
    fun createClassRequest_builder_withNullDescription() {
        // Act
        val request = CreateClassRequest.builder()
            .name("Math Class")
            .subjectName("Math")
            .teacherId(1)
            .build()

        // Assert
        assertNull(request.description)
    }

    @Test(expected = IllegalArgumentException::class)
    fun createClassRequest_builder_missingName_throwsException() {
        // Act
        CreateClassRequest.builder()
            .subjectName("Math")
            .teacherId(1)
            .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun createClassRequest_builder_missingSubjectName_throwsException() {
        // Act
        CreateClassRequest.builder()
            .name("Math Class")
            .teacherId(1)
            .build()
    }

    @Test(expected = IllegalArgumentException::class)
    fun createClassRequest_builder_missingTeacherId_throwsException() {
        // Act
        CreateClassRequest.builder()
            .name("Math Class")
            .subjectName("Math")
            .build()
    }

    @Test
    fun updateAssignmentRequest_builder_success() {
        // Act
        val request = UpdateAssignmentRequest.builder()
            .title("Updated Title")
            .description("Updated Description")
            .totalQuestions(20)
            .dueAt("2025-12-31T23:59:59Z")
            .grade("2학년")
            .subject(SubjectUpdateRequest(id = 1, name = "Science", code = "SCI"))
            .build()

        // Assert
        assertEquals("Updated Title", request.title)
        assertEquals("Updated Description", request.description)
        assertEquals(20, request.totalQuestions)
        assertEquals("2025-12-31T23:59:59Z", request.dueAt)
        assertEquals("2학년", request.grade)
        assertNotNull(request.subject)
    }

    @Test
    fun updateAssignmentRequest_builder_withSingleField() {
        // Act
        val request = UpdateAssignmentRequest.builder()
            .title("Updated Title")
            .build()

        // Assert
        assertEquals("Updated Title", request.title)
        assertNull(request.description)
    }

    @Test(expected = IllegalArgumentException::class)
    fun updateAssignmentRequest_builder_allFieldsNull_throwsException() {
        // Act
        UpdateAssignmentRequest.builder()
            .build()
    }

    @Test
    fun updateAssignmentRequest_builder_withSubjectUpdateRequest() {
        // Act
        val subjectUpdate = SubjectUpdateRequest(id = 1, name = "Science", code = "SCI")
        val request = UpdateAssignmentRequest.builder()
            .subject(subjectUpdate)
            .build()

        // Assert
        assertEquals(subjectUpdate, request.subject)
    }

    @Test
    fun subjectUpdateRequest_withAllFields() {
        // Act
        val request = SubjectUpdateRequest(id = 1, name = "Science", code = "SCI")

        // Assert
        assertEquals(1, request.id)
        assertEquals("Science", request.name)
        assertEquals("SCI", request.code)
    }

    @Test
    fun subjectUpdateRequest_withNullFields() {
        // Act
        val request = SubjectUpdateRequest()

        // Assert
        assertNull(request.id)
        assertNull(request.name)
        assertNull(request.code)
    }
}

