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

    @Test
    fun createAssignmentRequest_directConstructor_initializesCorrectly() {
        // Act - Builder 없이 직접 생성자 호출
        val request = CreateAssignmentRequest(
            title = "Direct Assignment",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Direct Description",
            total_questions = 10,
        )

        // Assert
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
        // Act - Builder 없이 직접 생성자 호출, nullable 필드 null
        val request = CreateAssignmentRequest(
            title = "Direct Assignment",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = null,
            description = null,
            total_questions = null,
        )

        // Assert
        assertNull(request.grade)
        assertNull(request.description)
        assertNull(request.total_questions)
    }

    @Test
    fun createAssignmentRequest_companionBuilder_returnsBuilder() {
        // Act
        val builder = CreateAssignmentRequest.builder()

        // Assert
        assertNotNull(builder)
    }

    @Test
    fun createAssignmentRequest_builder_allMethodsChained() {
        // Act - 모든 메서드를 체이닝하여 호출
        val request = CreateAssignmentRequest.builder()
            .title("Test")
            .subject("Math")
            .classId(1)
            .dueAt("2025-12-31T23:59:59Z")
            .grade("1학년")
            .description("Desc")
            .totalQuestions(10)
            .build()

        // Assert
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
        // Act - Builder 없이 직접 생성자 호출
        val request = UpdateAssignmentRequest(
            title = "Updated Title",
            description = "Updated Description",
            totalQuestions = 20,
            dueAt = "2025-12-31T23:59:59Z",
            grade = "2학년",
            subject = SubjectUpdateRequest(id = 1, name = "Science", code = "SCI"),
        )

        // Assert
        assertEquals("Updated Title", request.title)
        assertEquals("Updated Description", request.description)
        assertEquals(20, request.totalQuestions)
        assertEquals("2025-12-31T23:59:59Z", request.dueAt)
        assertEquals("2학년", request.grade)
        assertNotNull(request.subject)
    }

    @Test
    fun updateAssignmentRequest_directConstructor_withAllNullFields() {
        // Act - 모든 필드가 null인 경우
        val request = UpdateAssignmentRequest()

        // Assert
        assertNull(request.title)
        assertNull(request.description)
        assertNull(request.totalQuestions)
        assertNull(request.dueAt)
        assertNull(request.grade)
        assertNull(request.subject)
    }

    @Test
    fun updateAssignmentRequest_builder_withOnlyDescription() {
        // Act
        val request = UpdateAssignmentRequest.builder()
            .description("Only Description")
            .build()

        // Assert
        assertNull(request.title)
        assertEquals("Only Description", request.description)
        assertNull(request.totalQuestions)
    }

    @Test
    fun updateAssignmentRequest_builder_withOnlyTotalQuestions() {
        // Act
        val request = UpdateAssignmentRequest.builder()
            .totalQuestions(15)
            .build()

        // Assert
        assertNull(request.title)
        assertNull(request.description)
        assertEquals(15, request.totalQuestions)
    }

    @Test
    fun updateAssignmentRequest_builder_withOnlyDueAt() {
        // Act
        val request = UpdateAssignmentRequest.builder()
            .dueAt("2025-12-31T23:59:59Z")
            .build()

        // Assert
        assertNull(request.title)
        assertEquals("2025-12-31T23:59:59Z", request.dueAt)
    }

    @Test
    fun updateAssignmentRequest_builder_withOnlyGrade() {
        // Act
        val request = UpdateAssignmentRequest.builder()
            .grade("3학년")
            .build()

        // Assert
        assertNull(request.title)
        assertEquals("3학년", request.grade)
    }

    @Test
    fun updateAssignmentRequest_builder_withOnlySubject() {
        // Act
        val subjectUpdate = SubjectUpdateRequest(id = 2, name = "English", code = "ENG")
        val request = UpdateAssignmentRequest.builder()
            .subject(subjectUpdate)
            .build()

        // Assert
        assertNull(request.title)
        assertEquals(subjectUpdate, request.subject)
    }

    @Test
    fun updateAssignmentRequest_builder_withMultipleFields() {
        // Act - 여러 필드 조합
        val request = UpdateAssignmentRequest.builder()
            .title("Title")
            .description("Description")
            .build()

        // Assert
        assertEquals("Title", request.title)
        assertEquals("Description", request.description)
    }

    @Test
    fun updateAssignmentRequest_companionBuilder_returnsBuilder() {
        // Act
        val builder = UpdateAssignmentRequest.builder()

        // Assert
        assertNotNull(builder)
    }

    @Test
    fun updateAssignmentRequest_builder_allMethodsChained() {
        // Act - 모든 메서드를 체이닝하여 호출
        val subjectUpdate = SubjectUpdateRequest(id = 1, name = "Science", code = "SCI")
        val request = UpdateAssignmentRequest.builder()
            .title("Title")
            .description("Description")
            .totalQuestions(20)
            .dueAt("2025-12-31T23:59:59Z")
            .grade("2학년")
            .subject(subjectUpdate)
            .build()

        // Assert
        assertEquals("Title", request.title)
        assertEquals("Description", request.description)
        assertEquals(20, request.totalQuestions)
        assertEquals("2025-12-31T23:59:59Z", request.dueAt)
        assertEquals("2학년", request.grade)
        assertEquals(subjectUpdate, request.subject)
    }

    @Test
    fun createAssignmentRequest_copy_createsNewInstance() {
        // Arrange
        val original = CreateAssignmentRequest(
            title = "Original",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Original Description",
            total_questions = 10,
        )

        // Act - copy() 메서드로 내부 생성자 호출
        val copied = original.copy(
            title = "Copied",
            description = "Copied Description",
        )

        // Assert
        assertEquals("Copied", copied.title)
        assertEquals("Original", original.title)
        assertEquals("Math", copied.subject)
        assertEquals(1, copied.class_id)
        assertEquals("Copied Description", copied.description)
        assertEquals("Original Description", original.description)
    }

    @Test
    fun createAssignmentRequest_copy_withAllParameters() {
        // Arrange
        val original = CreateAssignmentRequest(
            title = "Original",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Original Description",
            total_questions = 10,
        )

        // Act - copy() 메서드로 모든 파라미터 전달
        val copied = original.copy(
            title = "New Title",
            subject = "Science",
            class_id = 2,
            due_at = "2026-01-01T00:00:00Z",
            grade = "2학년",
            description = "New Description",
            total_questions = 20,
        )

        // Assert
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
        // Arrange
        val original = CreateAssignmentRequest(
            title = "Original",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Original Description",
            total_questions = 10,
        )

        // Act - copy() 메서드로 nullable 필드를 null로 변경
        val copied = original.copy(
            grade = null,
            description = null,
            total_questions = null,
        )

        // Assert
        assertEquals("Original", copied.title)
        assertNull(copied.grade)
        assertNull(copied.description)
        assertNull(copied.total_questions)
    }

    @Test
    fun createAssignmentRequest_destructuring_decomposesCorrectly() {
        // Arrange
        val request = CreateAssignmentRequest(
            title = "Test",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Description",
            total_questions = 10,
        )

        // Act - destructuring으로 내부 생성자 호출
        val (title, subject, classId, dueAt, grade, description, totalQuestions) = request

        // Assert
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
        // Arrange
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

        // Assert
        assertEquals(request1, request2)
        assertNotEquals(request1, request3)
    }

    @Test
    fun createAssignmentRequest_hashCode_worksCorrectly() {
        // Arrange
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

        // Assert
        assertEquals(request1.hashCode(), request2.hashCode())
    }

    @Test
    fun createAssignmentRequest_toString_containsAllFields() {
        // Arrange
        val request = CreateAssignmentRequest(
            title = "Test",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:59Z",
            grade = "1학년",
            description = "Description",
            total_questions = 10,
        )

        // Act
        val toString = request.toString()

        // Assert
        assertTrue(toString.contains("Test"))
        assertTrue(toString.contains("Math"))
        assertTrue(toString.contains("1"))
    }
}

