package com.example.voicetutor.data.network

import com.example.voicetutor.data.models.QuestionData
import org.junit.Test
import org.junit.Assert.*

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
            error = null
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
            error = "Error occurred"
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
        val questions = listOf(
            QuestionData(
                id = 1,
                question = "What is 2+2?",
                type = "multiple_choice",
                options = listOf("3", "4", "5"),
                correctAnswer = "4",
                points = 1
            )
        )
        val request = CreateAssignmentRequest(
            title = "Math Assignment",
            subject = "Mathematics",
            class_id = 1,
            due_at = "2025-12-31T23:59:00Z",
            grade = "A",
            type = "quiz",
            description = "Test assignment",
            questions = questions
        )

        // Then
        assertEquals("Math Assignment", request.title)
        assertEquals("Mathematics", request.subject)
        assertEquals(1, request.class_id)
        assertEquals("2025-12-31T23:59:00Z", request.due_at)
        assertEquals("A", request.grade)
        assertEquals("quiz", request.type)
        assertEquals("Test assignment", request.description)
        assertEquals(questions, request.questions)
    }

    @Test
    fun createAssignmentRequest_withNullOptionalFields_createsCorrectly() {
        // Given
        val request = CreateAssignmentRequest(
            title = "Simple Assignment",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31T23:59:00Z",
            grade = null,
            type = "quiz",
            description = null,
            questions = null
        )

        // Then
        assertEquals("Simple Assignment", request.title)
        assertNull(request.grade)
        assertNull(request.description)
        assertNull(request.questions)
    }

    @Test
    fun createAssignmentResponse_createsCorrectly() {
        // Given
        val response = CreateAssignmentResponse(
            assignment_id = 123,
            material_id = 456,
            s3_key = "assignments/123/file.pdf",
            upload_url = "https://s3.example.com/upload"
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
            bucket = "my-bucket"
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
            bucket = "my-bucket"
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
            total_number = 10
        )

        // Then
        assertEquals(123, request.assignment_id)
        assertEquals(456, request.material_id)
        assertEquals(10, request.total_number)
    }

    @Test
    fun createClassRequest_createsCorrectly() {
        // Given
        val request = CreateClassRequest(
            name = "Math Class",
            description = "Advanced Mathematics",
            subject_name = "Mathematics",
            teacher_id = 1,
            start_date = "2025-01-01",
            end_date = "2025-12-31"
        )

        // Then
        assertEquals("Math Class", request.name)
        assertEquals("Advanced Mathematics", request.description)
        assertEquals("Mathematics", request.subject_name)
        assertEquals(1, request.teacher_id)
        assertEquals("2025-01-01", request.start_date)
        assertEquals("2025-12-31", request.end_date)
    }

    @Test
    fun createClassRequest_withNullDescription_createsCorrectly() {
        // Given
        val request = CreateClassRequest(
            name = "Math Class",
            description = null,
            subject_name = "Mathematics",
            teacher_id = 1,
            start_date = "2025-01-01",
            end_date = "2025-12-31"
        )

        // Then
        assertNull(request.description)
    }

    @Test
    fun updateAssignmentRequest_createsCorrectly() {
        // Given
        val request = UpdateAssignmentRequest(
            title = "Updated Title",
            description = "Updated Description",
            totalQuestions = 20,
            dueAt = "2025-12-31T23:59:00Z",
            grade = "B",
            subject = SubjectUpdateRequest(id = 1, name = "Math", code = "MATH101")
        )

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
        // Given
        val request = UpdateAssignmentRequest()

        // Then
        assertNull(request.title)
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
            code = "MATH101"
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

    @Test
    fun assignmentSubmissionRequest_createsCorrectly() {
        // Given
        val answers = listOf(
            AnswerSubmission(
                questionId = 1,
                answer = "Answer 1",
                audioFile = "audio1.wav",
                confidence = 0.95f
            ),
            AnswerSubmission(
                questionId = 2,
                answer = "Answer 2",
                audioFile = null,
                confidence = null
            )
        )
        val request = AssignmentSubmissionRequest(
            studentId = 123,
            answers = answers
        )

        // Then
        assertEquals(123, request.studentId)
        assertEquals(2, request.answers.size)
        assertEquals(1, request.answers[0].questionId)
        assertEquals("Answer 1", request.answers[0].answer)
        assertEquals("audio1.wav", request.answers[0].audioFile)
        assertEquals(0.95f, request.answers[0].confidence)
        assertEquals(2, request.answers[1].questionId)
        assertNull(request.answers[1].audioFile)
        assertNull(request.answers[1].confidence)
    }

    @Test
    fun answerSubmission_createsCorrectly() {
        // Given
        val answer = AnswerSubmission(
            questionId = 1,
            answer = "Answer",
            audioFile = "audio.wav",
            confidence = 0.9f
        )

        // Then
        assertEquals(1, answer.questionId)
        assertEquals("Answer", answer.answer)
        assertEquals("audio.wav", answer.audioFile)
        assertEquals(0.9f, answer.confidence)
    }

    @Test
    fun answerSubmission_withNullOptionalFields_createsCorrectly() {
        // Given
        val answer = AnswerSubmission(
            questionId = 1,
            answer = "Answer",
            audioFile = null,
            confidence = null
        )

        // Then
        assertNull(answer.audioFile)
        assertNull(answer.confidence)
    }

    @Test
    fun assignmentSubmissionResult_createsCorrectly() {
        // Given
        val feedback = listOf(
            QuestionFeedback(
                questionId = 1,
                isCorrect = true,
                studentAnswer = "Answer",
                correctAnswer = "Answer",
                explanation = "Correct",
                confidence = 0.95f,
                pronunciationScore = 0.9f
            )
        )
        val result = AssignmentSubmissionResult(
            submissionId = 123,
            score = 85,
            totalQuestions = 100,
            correctAnswers = 85,
            feedback = feedback
        )

        // Then
        assertEquals(123, result.submissionId)
        assertEquals(85, result.score)
        assertEquals(100, result.totalQuestions)
        assertEquals(85, result.correctAnswers)
        assertEquals(1, result.feedback.size)
    }

    @Test
    fun questionFeedback_createsCorrectly() {
        // Given
        val feedback = QuestionFeedback(
            questionId = 1,
            isCorrect = true,
            studentAnswer = "Answer",
            correctAnswer = "Answer",
            explanation = "Correct answer",
            confidence = 0.95f,
            pronunciationScore = 0.9f
        )

        // Then
        assertEquals(1, feedback.questionId)
        assertTrue(feedback.isCorrect)
        assertEquals("Answer", feedback.studentAnswer)
        assertEquals("Answer", feedback.correctAnswer)
        assertEquals("Correct answer", feedback.explanation)
        assertEquals(0.95f, feedback.confidence)
        assertEquals(0.9f, feedback.pronunciationScore)
    }

    @Test
    fun questionFeedback_withNullOptionalFields_createsCorrectly() {
        // Given
        val feedback = QuestionFeedback(
            questionId = 1,
            isCorrect = false,
            studentAnswer = "Wrong",
            correctAnswer = "Correct",
            explanation = null,
            confidence = 0.5f,
            pronunciationScore = null
        )

        // Then
        assertFalse(feedback.isCorrect)
        assertNull(feedback.explanation)
        assertNull(feedback.pronunciationScore)
    }
}

