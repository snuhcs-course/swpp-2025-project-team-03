package com.example.voicetutor.data.network

import com.example.voicetutor.data.models.AssignmentData
import com.example.voicetutor.data.models.CourseClass
import com.example.voicetutor.data.models.QuestionData
import com.example.voicetutor.data.models.Subject
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import assert

@RunWith(JUnit4::class)
class NetworkModelsTest {

    private fun buildSubject() = Subject(id = 1, name = "Math")
    private fun buildCourseClass() = CourseClass(
        id = 1,
        name = "Class1",
        description = null,
        subject = buildSubject(),
        teacherName = "Teacher1",
        startDate = "2025-01-01",
        endDate = "2025-12-31",
        studentCount = 10,
        createdAt = "2025-01-01"
    )

    @Test
    fun recentAnswerData_withAllFields_containsCorrectValues() {
        // Arrange
        val data = RecentAnswerData(
            personalAssignmentId = 100,
            nextQuestionId = 50
        )

        // Assert
        assert(data.personalAssignmentId == 100)
        assert(data.nextQuestionId == 50)
    }

    @Test
    fun recentAnswerData_withNullNextQuestionId_handlesNull() {
        // Arrange
        val data = RecentAnswerData(
            personalAssignmentId = 100,
            nextQuestionId = null
        )

        // Assert
        assert(data.personalAssignmentId == 100)
        assert(data.nextQuestionId == null)
    }

    @Test
    fun apiResponse_success_containsSuccessFlag() {
        // Arrange
        val response = ApiResponse(
            success = true,
            data = "test data",
            message = "Success",
            error = null
        )

        // Assert
        assert(response.success == true)
        assert(response.data == "test data")
        assert(response.message == "Success")
        assert(response.error == null)
    }

    @Test
    fun apiResponse_failure_containsError() {
        // Arrange
        val response = ApiResponse<String>(
            success = false,
            data = null,
            message = null,
            error = "Error occurred"
        )

        // Assert
        assert(response.success == false)
        assert(response.data == null)
        assert(response.error == "Error occurred")
    }

    @Test
    fun createAssignmentRequest_withAllFields_containsCorrectValues() {
        // Arrange
        val questions = listOf(
            QuestionData(id = 1, question = "Q1", type = "SHORT_ANSWER", options = null, correctAnswer = "A1")
        )
        val request = CreateAssignmentRequest(
            title = "Assignment1",
            subject = "Math",
            class_id = 1,
            due_at = "2025-12-31",
            grade = "1",
            type = "QUIZ",
            description = "Description",
            questions = questions
        )

        // Assert
        assert(request.title == "Assignment1")
        assert(request.subject == "Math")
        assert(request.class_id == 1)
        assert(request.due_at == "2025-12-31")
        assert(request.grade == "1")
        assert(request.type == "QUIZ")
        assert(request.description == "Description")
        assert(request.questions?.size == 1)
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
            type = "QUIZ",
            description = null,
            questions = null
        )

        // Assert
        assert(request.description == null)
        assert(request.questions == null)
    }

    @Test
    fun createAssignmentResponse_withAllFields_containsCorrectValues() {
        // Arrange
        val response = CreateAssignmentResponse(
            assignment_id = 50,
            material_id = 20,
            s3_key = "key123",
            upload_url = "https://s3.amazonaws.com/upload"
        )

        // Assert
        assert(response.assignment_id == 50)
        assert(response.material_id == 20)
        assert(response.s3_key == "key123")
        assert(response.upload_url == "https://s3.amazonaws.com/upload")
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
            bucket = "my-bucket"
        )

        // Assert
        assert(status.assignment_id == 1)
        assert(status.material_id == 10)
        assert(status.s3_key == "key123")
        assert(status.file_exists == true)
        assert(status.file_size == 1024L)
        assert(status.content_type == "application/pdf")
        assert(status.bucket == "my-bucket")
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
            bucket = "my-bucket"
        )

        // Assert
        assert(status.file_exists == false)
        assert(status.file_size == null)
        assert(status.content_type == null)
        assert(status.last_modified == null)
    }

    @Test
    fun questionCreateRequest_withAllFields_containsCorrectValues() {
        // Arrange
        val request = QuestionCreateRequest(
            assignment_id = 1,
            material_id = 10,
            total_number = 5
        )

        // Assert
        assert(request.assignment_id == 1)
        assert(request.material_id == 10)
        assert(request.total_number == 5)
    }

    @Test
    fun createClassRequest_withAllFields_containsCorrectValues() {
        // Arrange
        val request = CreateClassRequest(
            name = "Class1",
            description = "Description",
            subject_name = "Math",
            teacher_id = 1,
            start_date = "2025-01-01",
            end_date = "2025-12-31"
        )

        // Assert
        assert(request.name == "Class1")
        assert(request.description == "Description")
        assert(request.subject_name == "Math")
        assert(request.teacher_id == 1)
    }

    @Test
    fun createClassRequest_withNullDescription_handlesNull() {
        // Arrange
        val request = CreateClassRequest(
            name = "Class1",
            description = null,
            subject_name = "Math",
            teacher_id = 1,
            start_date = "2025-01-01",
            end_date = "2025-12-31"
        )

        // Assert
        assert(request.description == null)
    }

    @Test
    fun updateAssignmentRequest_withAllFields_containsCorrectValues() {
        // Arrange
        val questions = listOf(
            QuestionData(id = 1, question = "Q1", type = "SHORT_ANSWER", options = null, correctAnswer = "A1")
        )
        val request = UpdateAssignmentRequest(
            title = "Updated",
            subject = "Math",
            classId = 1,
            dueDate = "2025-12-31",
            type = "QUIZ",
            description = "New description",
            questions = questions
        )

        // Assert
        assert(request.title == "Updated")
        assert(request.subject == "Math")
        assert(request.classId == 1)
        assert(request.dueDate == "2025-12-31")
        assert(request.type == "QUIZ")
        assert(request.description == "New description")
        assert(request.questions?.size == 1)
    }

    @Test
    fun updateAssignmentRequest_withNullFields_handlesNulls() {
        // Arrange
        val request = UpdateAssignmentRequest(
            title = null,
            subject = null,
            classId = null,
            dueDate = null,
            type = null,
            description = null,
            questions = null
        )

        // Assert
        assert(request.title == null)
        assert(request.subject == null)
        assert(request.classId == null)
        assert(request.questions == null)
    }

    @Test
    fun assignmentSubmissionRequest_withAllFields_containsCorrectValues() {
        // Arrange
        val answers = listOf(
            AnswerSubmission(questionId = 1, answer = "A1", audioFile = "file1.wav", confidence = 0.9f),
            AnswerSubmission(questionId = 2, answer = "A2", audioFile = null, confidence = null)
        )
        val request = AssignmentSubmissionRequest(
            studentId = 1,
            answers = answers
        )

        // Assert
        assert(request.studentId == 1)
        assert(request.answers.size == 2)
        assert(request.answers[0].questionId == 1)
        assert(request.answers[1].audioFile == null)
    }

    @Test
    fun answerSubmission_withAllFields_containsCorrectValues() {
        // Arrange
        val submission = AnswerSubmission(
            questionId = 1,
            answer = "Answer",
            audioFile = "audio.wav",
            confidence = 0.95f
        )

        // Assert
        assert(submission.questionId == 1)
        assert(submission.answer == "Answer")
        assert(submission.audioFile == "audio.wav")
        assert(submission.confidence == 0.95f)
    }

    @Test
    fun answerSubmission_withNullOptionalFields_handlesNulls() {
        // Arrange
        val submission = AnswerSubmission(
            questionId = 1,
            answer = "Answer",
            audioFile = null,
            confidence = null
        )

        // Assert
        assert(submission.audioFile == null)
        assert(submission.confidence == null)
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
                pronunciationScore = 0.8f
            )
        )
        val result = AssignmentSubmissionResult(
            submissionId = 1,
            score = 85,
            totalQuestions = 10,
            correctAnswers = 8,
            feedback = feedback
        )

        // Assert
        assert(result.submissionId == 1)
        assert(result.score == 85)
        assert(result.totalQuestions == 10)
        assert(result.correctAnswers == 8)
        assert(result.feedback.size == 1)
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
            pronunciationScore = 0.9f
        )

        // Assert
        assert(feedback.questionId == 1)
        assert(feedback.isCorrect == true)
        assert(feedback.studentAnswer == "A1")
        assert(feedback.confidence == 0.95f)
        assert(feedback.pronunciationScore == 0.9f)
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
            pronunciationScore = null
        )

        // Assert
        assert(feedback.explanation == null)
        assert(feedback.pronunciationScore == null)
    }
}

