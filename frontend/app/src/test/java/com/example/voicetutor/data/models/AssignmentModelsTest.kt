package com.example.voicetutor.data.models

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import assert

@RunWith(JUnit4::class)
class AssignmentModelsTest {

    @Test
    fun assignmentStatus_enumValues_correct() {
        // Assert
        assert(AssignmentStatus.IN_PROGRESS.name == "IN_PROGRESS")
        assert(AssignmentStatus.COMPLETED.name == "COMPLETED")
        assert(AssignmentStatus.DRAFT.name == "DRAFT")
    }

    @Test
    fun assignmentFilter_enumValues_correct() {
        // Assert
        assert(AssignmentFilter.ALL.name == "ALL")
        assert(AssignmentFilter.IN_PROGRESS.name == "IN_PROGRESS")
        assert(AssignmentFilter.COMPLETED.name == "COMPLETED")
    }

    @Test
    fun personalAssignmentStatus_enumValues_correct() {
        // Assert
        assert(PersonalAssignmentStatus.NOT_STARTED.name == "NOT_STARTED")
        assert(PersonalAssignmentStatus.IN_PROGRESS.name == "IN_PROGRESS")
        assert(PersonalAssignmentStatus.SUBMITTED.name == "SUBMITTED")
        assert(PersonalAssignmentStatus.GRADED.name == "GRADED")
    }

    @Test
    fun personalAssignmentFilter_enumValues_correct() {
        // Assert
        assert(PersonalAssignmentFilter.ALL.name == "ALL")
        assert(PersonalAssignmentFilter.NOT_STARTED.name == "NOT_STARTED")
        assert(PersonalAssignmentFilter.IN_PROGRESS.name == "IN_PROGRESS")
        assert(PersonalAssignmentFilter.SUBMITTED.name == "SUBMITTED")
        assert(PersonalAssignmentFilter.GRADED.name == "GRADED")
    }

    @Test
    fun assignmentData_withAllFields_containsCorrectValues() {
        // Arrange
        val subject = Subject(id = 1, name = "Math", code = "MATH")
        val courseClass = CourseClass(
            id = 1,
            name = "Class1",
            description = "Description",
            subject = subject,
            teacherName = "Teacher1",
            startDate = "2025-01-01",
            endDate = "2025-12-31",
            studentCount = 10,
            createdAt = "2025-01-01"
        )
        val assignment = AssignmentData(
            id = 1,
            title = "Assignment1",
            description = "Description",
            totalQuestions = 5,
            createdAt = "2025-01-01",
            visibleFrom = "2025-01-01",
            dueAt = "2025-12-31",
            courseClass = courseClass,
            materials = null,
            grade = "1",
            personalAssignmentStatus = PersonalAssignmentStatus.IN_PROGRESS,
            solvedNum = 2,
            personalAssignmentId = 10
        )

        // Assert
        assert(assignment.id == 1)
        assert(assignment.title == "Assignment1")
        assert(assignment.totalQuestions == 5)
        assert(assignment.personalAssignmentStatus == PersonalAssignmentStatus.IN_PROGRESS)
        assert(assignment.solvedNum == 2)
        assert(assignment.personalAssignmentId == 10)
    }

    @Test
    fun assignmentData_withNullOptionalFields_handlesNulls() {
        // Arrange
        val subject = Subject(id = 1, name = "Math")
        val courseClass = CourseClass(
            id = 1,
            name = "Class1",
            description = null,
            subject = subject,
            teacherName = "Teacher1",
            startDate = "2025-01-01",
            endDate = "2025-12-31",
            studentCount = 10,
            createdAt = "2025-01-01"
        )
        val assignment = AssignmentData(
            id = 1,
            title = "Assignment1",
            description = null,
            totalQuestions = 0,
            createdAt = null,
            visibleFrom = null,
            dueAt = "2025-12-31",
            courseClass = courseClass,
            materials = null,
            grade = null,
            personalAssignmentStatus = null,
            solvedNum = null,
            personalAssignmentId = null
        )

        // Assert
        assert(assignment.description == null)
        assert(assignment.createdAt == null)
        assert(assignment.visibleFrom == null)
        assert(assignment.materials == null)
        assert(assignment.grade == null)
        assert(assignment.personalAssignmentStatus == null)
    }

    @Test
    fun subject_withCode_containsCode() {
        // Arrange
        val subject = Subject(id = 1, name = "Math", code = "MATH101")

        // Assert
        assert(subject.code == "MATH101")
    }

    @Test
    fun subject_withoutCode_codeIsNull() {
        // Arrange
        val subject = Subject(id = 1, name = "Math")

        // Assert
        assert(subject.code == null)
    }

    @Test
    fun material_withAllFields_containsCorrectValues() {
        // Arrange
        val material = Material(
            id = 1,
            kind = "PDF",
            s3Key = "key123",
            bytes = 1024,
            createdAt = "2025-01-01"
        )

        // Assert
        assert(material.id == 1)
        assert(material.kind == "PDF")
        assert(material.s3Key == "key123")
        assert(material.bytes == 1024)
    }

    @Test
    fun material_withoutBytes_bytesIsNull() {
        // Arrange
        val material = Material(
            id = 1,
            kind = "PDF",
            s3Key = "key123",
            bytes = null,
            createdAt = "2025-01-01"
        )

        // Assert
        assert(material.bytes == null)
    }

    @Test
    fun questionData_withAllFields_containsCorrectValues() {
        // Arrange
        val question = QuestionData(
            id = 1,
            question = "What is 2+2?",
            type = "MULTIPLE_CHOICE",
            options = listOf("3", "4", "5", "6"),
            correctAnswer = "4",
            points = 10,
            explanation = "2+2 equals 4"
        )

        // Assert
        assert(question.id == 1)
        assert(question.question == "What is 2+2?")
        assert(question.type == "MULTIPLE_CHOICE")
        assert(question.options?.size == 4)
        assert(question.correctAnswer == "4")
        assert(question.points == 10)
        assert(question.explanation == "2+2 equals 4")
    }

    @Test
    fun questionData_withoutOptions_handlesNull() {
        // Arrange
        val question = QuestionData(
            id = 1,
            question = "What is 2+2?",
            type = "SHORT_ANSWER",
            options = null,
            correctAnswer = "4",
            points = 5,
            explanation = null
        )

        // Assert
        assert(question.options == null)
        assert(question.explanation == null)
    }

    @Test
    fun questionData_defaultPoints_isOne() {
        // Arrange
        val question = QuestionData(
            id = 1,
            question = "Test",
            type = "SHORT_ANSWER",
            options = null,
            correctAnswer = "Answer"
        )

        // Assert
        assert(question.points == 1)
    }

    @Test
    fun personalAssignmentQuestion_withAllFields_containsCorrectValues() {
        // Arrange
        val question = PersonalAssignmentQuestion(
            id = 1,
            number = "1-2",
            question = "Question",
            answer = "Answer",
            explanation = "Explanation",
            difficulty = "medium"
        )

        // Assert
        assert(question.id == 1)
        assert(question.number == "1-2")
        assert(question.question == "Question")
        assert(question.answer == "Answer")
        assert(question.explanation == "Explanation")
        assert(question.difficulty == "medium")
    }

    @Test
    fun personalAssignmentStatistics_withAllFields_containsCorrectValues() {
        // Arrange
        val statistics = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 8,
            correctAnswers = 6,
            accuracy = 0.75f,
            totalProblem = 5,
            solvedProblem = 4,
            progress = 0.8f
        )

        // Assert
        assert(statistics.totalQuestions == 10)
        assert(statistics.answeredQuestions == 8)
        assert(statistics.correctAnswers == 6)
        assert(statistics.accuracy == 0.75f)
        assert(statistics.totalProblem == 5)
        assert(statistics.solvedProblem == 4)
        assert(statistics.progress == 0.8f)
    }

    @Test
    fun personalAssignmentStatistics_zeroValues_handlesZeros() {
        // Arrange
        val statistics = PersonalAssignmentStatistics(
            totalQuestions = 0,
            answeredQuestions = 0,
            correctAnswers = 0,
            accuracy = 0f,
            totalProblem = 0,
            solvedProblem = 0,
            progress = 0f
        )

        // Assert
        assert(statistics.totalQuestions == 0)
        assert(statistics.accuracy == 0f)
        assert(statistics.progress == 0f)
    }

    @Test
    fun tailQuestion_withAllFields_containsCorrectValues() {
        // Arrange
        val tailQuestion = TailQuestion(
            id = 1,
            number = "2-1",
            question = "Tail Question",
            answer = "Tail Answer",
            explanation = "Tail Explanation",
            difficulty = "hard"
        )

        // Assert
        assert(tailQuestion.id == 1)
        assert(tailQuestion.number == "2-1")
        assert(tailQuestion.question == "Tail Question")
        assert(tailQuestion.answer == "Tail Answer")
    }

    @Test
    fun answerSubmissionResponse_correctAnswer_containsTailQuestion() {
        // Arrange
        val tailQuestion = TailQuestion(
            id = 1,
            number = "2-1",
            question = "Next",
            answer = "Next Answer",
            explanation = "Next Explanation",
            difficulty = "medium"
        )
        val response = AnswerSubmissionResponse(
            isCorrect = true,
            numberStr = "1-1",
            tailQuestion = tailQuestion
        )

        // Assert
        assert(response.isCorrect == true)
        assert(response.numberStr == "1-1")
        assert(response.tailQuestion?.id == 1)
    }

    @Test
    fun answerSubmissionResponse_incorrectAnswer_tailQuestionCanBeNull() {
        // Arrange
        val response = AnswerSubmissionResponse(
            isCorrect = false,
            numberStr = null,
            tailQuestion = null
        )

        // Assert
        assert(response.isCorrect == false)
        assert(response.numberStr == null)
        assert(response.tailQuestion == null)
    }

    @Test
    fun personalAssignmentData_withAllFields_containsCorrectValues() {
        // Arrange
        val studentInfo = StudentInfo(id = 1, displayName = "Student1", email = "s1@test.com")
        val assignmentInfo = PersonalAssignmentInfo(
            id = 1,
            title = "Assignment1",
            description = "Description",
            totalQuestions = 5,
            visibleFrom = "2025-01-01",
            dueAt = "2025-12-31",
            grade = "1"
        )
        val personalAssignment = PersonalAssignmentData(
            id = 1,
            student = studentInfo,
            assignment = assignmentInfo,
            status = PersonalAssignmentStatus.IN_PROGRESS,
            solvedNum = 2,
            startedAt = "2025-01-01",
            submittedAt = null
        )

        // Assert
        assert(personalAssignment.id == 1)
        assert(personalAssignment.student.id == 1)
        assert(personalAssignment.assignment.title == "Assignment1")
        assert(personalAssignment.status == PersonalAssignmentStatus.IN_PROGRESS)
        assert(personalAssignment.solvedNum == 2)
        assert(personalAssignment.startedAt == "2025-01-01")
        assert(personalAssignment.submittedAt == null)
    }

    @Test
    fun studentInfo_withAllFields_containsCorrectValues() {
        // Arrange
        val studentInfo = StudentInfo(
            id = 1,
            displayName = "Student1",
            email = "s1@test.com"
        )

        // Assert
        assert(studentInfo.id == 1)
        assert(studentInfo.displayName == "Student1")
        assert(studentInfo.email == "s1@test.com")
    }

    @Test
    fun personalAssignmentInfo_withAllFields_containsCorrectValues() {
        // Arrange
        val info = PersonalAssignmentInfo(
            id = 1,
            title = "Assignment1",
            description = "Description",
            totalQuestions = 5,
            visibleFrom = "2025-01-01",
            dueAt = "2025-12-31",
            grade = "1"
        )

        // Assert
        assert(info.id == 1)
        assert(info.title == "Assignment1")
        assert(info.totalQuestions == 5)
        assert(info.grade == "1")
    }

    @Test
    fun studentResult_withAllFields_containsCorrectValues() {
        // Arrange
        val detailedAnswers = listOf(
            DetailedAnswer(
                questionNumber = 1,
                question = "Q1",
                studentAnswer = "A1",
                correctAnswer = "A1",
                isCorrect = true,
                confidenceScore = 95,
                responseTime = "30s"
            )
        )
        val result = StudentResult(
            studentId = "1",
            name = "Student1",
            score = 85,
            confidenceScore = 90,
            status = "GRADED",
            submittedAt = "2025-01-01",
            answers = listOf("A1", "A2"),
            detailedAnswers = detailedAnswers
        )

        // Assert
        assert(result.studentId == "1")
        assert(result.name == "Student1")
        assert(result.score == 85)
        assert(result.status == "GRADED")
        assert(result.answers.size == 2)
        assert(result.detailedAnswers.size == 1)
    }

    @Test
    fun detailedAnswer_withAllFields_containsCorrectValues() {
        // Arrange
        val answer = DetailedAnswer(
            questionNumber = 1,
            question = "What is 2+2?",
            studentAnswer = "4",
            correctAnswer = "4",
            isCorrect = true,
            confidenceScore = 95,
            responseTime = "30s"
        )

        // Assert
        assert(answer.questionNumber == 1)
        assert(answer.studentAnswer == "4")
        assert(answer.correctAnswer == "4")
        assert(answer.isCorrect == true)
        assert(answer.confidenceScore == 95)
    }

    @Test
    fun audioRecordingState_initialState_hasDefaultValues() {
        // Arrange
        val state = AudioRecordingState()

        // Assert
        assert(state.isRecording == false)
        assert(state.recordingDuration == 0)
        assert(state.audioFilePath == null)
        assert(state.isProcessing == false)
    }

    @Test
    fun audioRecordingState_recordingState_updatesValues() {
        // Arrange
        val state = AudioRecordingState(
            isRecording = true,
            recordingDuration = 10,
            audioFilePath = "/path/to/audio.wav",
            isProcessing = false
        )

        // Assert
        assert(state.isRecording == true)
        assert(state.recordingDuration == 10)
        assert(state.audioFilePath == "/path/to/audio.wav")
    }
}

