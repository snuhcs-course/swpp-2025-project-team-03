package com.example.voicetutor.data.network

import com.example.voicetutor.data.models.CourseClass
import com.example.voicetutor.data.models.Subject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class NetworkModelsTest {

    private fun buildSubject() = Subject(id = 1, name = "Math")
    private fun buildCourseClass() = CourseClass(
        id = 1,
        name = "Class1",
        description = null,
        subject = buildSubject(),
        teacherName = "Teacher1",

        studentCount = 10,
        createdAt = "2025-01-01",
    )

    @Test
    fun recentAnswerData_containsCorrectValues() {
        // Arrange
        val data = RecentAnswerData(
            personalAssignmentId = 100,
        )

        // Assert
        assertEquals(100, data.personalAssignmentId)
    }

    @Test
    fun apiResponse_success_containsSuccessFlag() {
        // Arrange
        val response = ApiResponse(
            success = true,
            data = "test data",
            message = "Success",
            error = null,
        )

        // Assert
        assertEquals(true, response.success)
        assertEquals("test data", response.data)
        assertEquals("Success", response.message)
        assertNull(response.error)
    }

    @Test
    fun apiResponse_failure_containsError() {
        // Arrange
        val response = ApiResponse<String>(
            success = false,
            data = null,
            message = null,
            error = "Error occurred",
        )

        // Assert
        assertEquals(false, response.success)
        assertNull(response.data)
        assertEquals("Error occurred", response.error)
    }

    @Test
    fun createAssignmentRequest_withAllFields_containsCorrectValues() {
        // Arrange
        val request = CreateAssignmentRequest(
            title = "Assignment1",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31",
            grade = "1",
            description = "Description",
        )

        // Assert
        assertEquals("Assignment1", request.title)
        assertEquals("Math", request.subject)
        assertEquals(1, request.class_id)
        assertEquals("2025-12-31", request.due_at)
        assertEquals("1", request.grade)
        assertEquals("Description", request.description)
    }

    @Test
    fun createAssignmentRequest_withNullOptionalFields_handlesNulls() {
        // Arrange
        val request = CreateAssignmentRequest(
            title = "Assignment1",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31",
            grade = "1",
            description = null,
        )

        // Assert
        assertNull(request.description)
    }

    @Test
    fun createAssignmentResponse_withAllFields_containsCorrectValues() {
        // Arrange
        val response = CreateAssignmentResponse(
            assignment_id = 50,
            material_id = 20,
            s3_key = "key123",
            upload_url = "https://s3.amazonaws.com/upload",
        )

        // Assert
        assertEquals(50, response.assignment_id)
        assertEquals(20, response.material_id)
        assertEquals("key123", response.s3_key)
        assertEquals("https://s3.amazonaws.com/upload", response.upload_url)
    }

    @Test
    fun s3UploadStatus_withAllFields_containsCorrectValues() {
        // Arrange
        val status = S3UploadStatus(
            assignment_id = 1,
            material_id = 10,
            s3_key = "key123",
            file_exists = true,
            file_size = 1024L,
            content_type = "application/pdf",
            last_modified = "2025-01-01",
            bucket = "my-bucket",
        )

        // Assert
        assertEquals(1, status.assignment_id)
        assertEquals(10, status.material_id)
        assertEquals("key123", status.s3_key)
        assertEquals(true, status.file_exists)
        assertEquals(1024L, status.file_size)
        assertEquals("application/pdf", status.content_type)
        assertEquals("my-bucket", status.bucket)
    }

    @Test
    fun s3UploadStatus_withNullOptionalFields_handlesNulls() {
        // Arrange
        val status = S3UploadStatus(
            assignment_id = 1,
            material_id = 10,
            s3_key = "key123",
            file_exists = false,
            file_size = null,
            content_type = null,
            last_modified = null,
            bucket = "my-bucket",
        )

        // Assert
        assertEquals(false, status.file_exists)
        assertNull(status.file_size)
        assertNull(status.content_type)
        assertNull(status.last_modified)
    }

    @Test
    fun questionCreateRequest_withAllFields_containsCorrectValues() {
        // Arrange
        val request = QuestionCreateRequest(
            assignment_id = 1,
            material_id = 10,
            total_number = 5,
        )

        // Assert
        assertEquals(1, request.assignment_id)
        assertEquals(10, request.material_id)
        assertEquals(5, request.total_number)
    }

    @Test
    fun createClassRequest_withAllFields_containsCorrectValues() {
        // Arrange
        val request = CreateClassRequest(
            name = "Class1",
            description = "Description",
            subject_name = "Math",
            teacher_id = 1,
        )

        // Assert
        assertEquals("Class1", request.name)
        assertEquals("Description", request.description)
        assertEquals("Math", request.subject_name)
        assertEquals(1, request.teacher_id)
    }

    @Test
    fun createClassRequest_withNullDescription_handlesNull() {
        // Arrange
        val request = CreateClassRequest(
            name = "Class1",
            description = null,
            subject_name = "Math",
            teacher_id = 1,
        )

        // Assert
        assertNull(request.description)
    }

    @Test
    fun updateAssignmentRequest_withAllFields_containsCorrectValues() {
        // Arrange
        val request = UpdateAssignmentRequest(
            title = "Updated",
            description = "New description",
            totalQuestions = 10,
            dueAt = "2025-12-31T00:00:00Z",
            grade = "A",
            subject = SubjectUpdateRequest(id = 2, name = "Math", code = "MATH"),
        )

        // Assert
        assertEquals("Updated", request.title)
        assertEquals("New description", request.description)
        assertEquals(10, request.totalQuestions)
        assertEquals("2025-12-31T00:00:00Z", request.dueAt)
        assertEquals("A", request.grade)
        assertEquals(2, request.subject?.id)
    }

    @Test
    fun updateAssignmentRequest_withNullFields_handlesNulls() {
        // Arrange
        val request = UpdateAssignmentRequest(
            title = null,
            description = null,
            totalQuestions = null,
            dueAt = null,
            grade = null,
            subject = null,
        )

        // Assert
        assertNull(request.title)
        assertNull(request.description)
        assertNull(request.totalQuestions)
        assertNull(request.dueAt)
        assertNull(request.grade)
        assertNull(request.subject)
    }

    @Test
    fun assignmentSubmissionRequest_withAllFields_containsCorrectValues() {
        // Arrange
        val answers = listOf(
            AnswerSubmission(questionId = 1, answer = "A1", audioFile = "file1.wav", confidence = 0.9f),
            AnswerSubmission(questionId = 2, answer = "A2", audioFile = null, confidence = null),
        )
        val request = AssignmentSubmissionRequest(
            studentId = 1,
            answers = answers,
        )

        // Assert
        assertEquals(1, request.studentId)
        assertEquals(2, request.answers.size)
        assertEquals(1, request.answers[0].questionId)
        assertNull(request.answers[1].audioFile)
    }

    @Test
    fun answerSubmission_withAllFields_containsCorrectValues() {
        // Arrange
        val submission = AnswerSubmission(
            questionId = 1,
            answer = "Answer",
            audioFile = "audio.wav",
            confidence = 0.95f,
        )

        // Assert
        assertEquals(1, submission.questionId)
        assertEquals("Answer", submission.answer)
        assertEquals("audio.wav", submission.audioFile)
        assertEquals(0.95f, submission.confidence ?: 0f, 0.01f)
    }

    @Test
    fun answerSubmission_withNullOptionalFields_handlesNulls() {
        // Arrange
        val submission = AnswerSubmission(
            questionId = 1,
            answer = "Answer",
            audioFile = null,
            confidence = null,
        )

        // Assert
        assertNull(submission.audioFile)
        assertNull(submission.confidence)
    }

    @Test
    fun assignmentSubmissionResult_withAllFields_containsCorrectValues() {
        // Arrange
        val feedback = listOf(
            QuestionFeedback(
                questionId = 1,
                isCorrect = true,
                studentAnswer = "A1",
                correctAnswer = "A1",
                explanation = "Correct",
                confidence = 0.9f,
                pronunciationScore = 0.8f,
            ),
        )
        val result = AssignmentSubmissionResult(
            submissionId = 1,
            score = 85,
            totalQuestions = 10,
            correctAnswers = 8,
            feedback = feedback,
        )

        // Assert
        assertEquals(1, result.submissionId)
        assertEquals(85, result.score)
        assertEquals(10, result.totalQuestions)
        assertEquals(8, result.correctAnswers)
        assertEquals(1, result.feedback.size)
    }

    @Test
    fun questionFeedback_withAllFields_containsCorrectValues() {
        // Arrange
        val feedback = QuestionFeedback(
            questionId = 1,
            isCorrect = true,
            studentAnswer = "A1",
            correctAnswer = "A1",
            explanation = "Correct answer",
            confidence = 0.95f,
            pronunciationScore = 0.9f,
        )

        // Assert
        assertEquals(1, feedback.questionId)
        assertEquals(true, feedback.isCorrect)
        assertEquals("A1", feedback.studentAnswer)
        assertEquals(0.95f, feedback.confidence, 0.01f)
        assertEquals(0.9f, feedback.pronunciationScore ?: 0f, 0.01f)
    }

    @Test
    fun questionFeedback_withNullOptionalFields_handlesNulls() {
        // Arrange
        val feedback = QuestionFeedback(
            questionId = 1,
            isCorrect = false,
            studentAnswer = "Wrong",
            correctAnswer = "Correct",
            explanation = null,
            confidence = 0.3f,
            pronunciationScore = null,
        )

        // Assert
        assertNull(feedback.explanation)
        assertNull(feedback.pronunciationScore)
    }
}
