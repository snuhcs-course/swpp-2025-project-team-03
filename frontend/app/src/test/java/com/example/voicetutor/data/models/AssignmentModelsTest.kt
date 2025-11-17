package com.example.voicetutor.data.models

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull

@RunWith(JUnit4::class)
class AssignmentModelsTest {

    @Test
    fun assignmentStatus_enumValues_correct() {
        // Assert
        assertEquals("IN_PROGRESS", AssignmentStatus.IN_PROGRESS.name)
        assertEquals("COMPLETED", AssignmentStatus.COMPLETED.name)
        assertEquals("DRAFT", AssignmentStatus.DRAFT.name)
    }

    @Test
    fun assignmentFilter_enumValues_correct() {
        // Assert
        assertEquals("ALL", AssignmentFilter.ALL.name)
        assertEquals("IN_PROGRESS", AssignmentFilter.IN_PROGRESS.name)
        assertEquals("COMPLETED", AssignmentFilter.COMPLETED.name)
    }

    @Test
    fun personalAssignmentStatus_enumValues_correct() {
        // Assert
        assertEquals("NOT_STARTED", PersonalAssignmentStatus.NOT_STARTED.name)
        assertEquals("IN_PROGRESS", PersonalAssignmentStatus.IN_PROGRESS.name)
        assertEquals("SUBMITTED", PersonalAssignmentStatus.SUBMITTED.name)
    }

    @Test
    fun personalAssignmentFilter_enumValues_correct() {
        // Assert
        assertEquals("ALL", PersonalAssignmentFilter.ALL.name)
        assertEquals("NOT_STARTED", PersonalAssignmentFilter.NOT_STARTED.name)
        assertEquals("IN_PROGRESS", PersonalAssignmentFilter.IN_PROGRESS.name)
        assertEquals("SUBMITTED", PersonalAssignmentFilter.SUBMITTED.name)
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
            
            
            studentCount = 10,
            createdAt = "2025-01-01"
        )
        val assignment = AssignmentData(
            id = 1,
            title = "Assignment1",
            description = "Description",
            totalQuestions = 5,
            createdAt = "2025-01-01",
            
            dueAt = "2025-12-31",
            courseClass = courseClass,
            materials = null,
            grade = "1",
            personalAssignmentStatus = PersonalAssignmentStatus.IN_PROGRESS,
            solvedNum = 2,
            personalAssignmentId = 10
        )

        // Assert
        assertEquals(1, assignment.id)
        assertEquals("Assignment1", assignment.title)
        assertEquals(5, assignment.totalQuestions)
        assertEquals(PersonalAssignmentStatus.IN_PROGRESS, assignment.personalAssignmentStatus)
        assertEquals(2, assignment.solvedNum)
        assertEquals(10, assignment.personalAssignmentId)
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
            
            
            studentCount = 10,
            createdAt = "2025-01-01"
        )
        val assignment = AssignmentData(
            id = 1,
            title = "Assignment1",
            description = null,
            totalQuestions = 0,
            createdAt = null,
            dueAt = "2025-12-31",
            courseClass = courseClass,
            materials = null,
            grade = null,
            personalAssignmentStatus = null,
            solvedNum = null,
            personalAssignmentId = null
        )

        // Assert
        assertNull(assignment.description)
        assertNull(assignment.createdAt)
        assertNull(assignment.materials)
        assertNull(assignment.grade)
        assertNull(assignment.personalAssignmentStatus)
    }

    @Test
    fun subject_withCode_containsCode() {
        // Arrange
        val subject = Subject(id = 1, name = "Math", code = "MATH101")

        // Assert
        assertEquals("MATH101", subject.code)
    }

    @Test
    fun subject_withoutCode_codeIsNull() {
        // Arrange
        val subject = Subject(id = 1, name = "Math")

        // Assert
        assertNull(subject.code)
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
        assertEquals(1, material.id)
        assertEquals("PDF", material.kind)
        assertEquals("key123", material.s3Key)
        assertEquals(1024, material.bytes)
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
        assertNull(material.bytes)
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
        assertEquals(1, question.id)
        assertEquals("What is 2+2?", question.question)
        assertEquals("MULTIPLE_CHOICE", question.type)
        assertEquals(4, question.options?.size)
        assertEquals("4", question.correctAnswer)
        assertEquals(10, question.points)
        assertEquals("2+2 equals 4", question.explanation)
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
        assertNull(question.options)
        assertNull(question.explanation)
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
        assertEquals(1, question.points)
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
        assertEquals(1, question.id)
        assertEquals("1-2", question.number)
        assertEquals("Question", question.question)
        assertEquals("Answer", question.answer)
        assertEquals("Explanation", question.explanation)
        assertEquals("medium", question.difficulty)
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
            progress = 0.8f,
            averageScore = 85.5f
        )

        // Assert
        assertEquals(10, statistics.totalQuestions)
        assertEquals(8, statistics.answeredQuestions)
        assertEquals(6, statistics.correctAnswers)
        assertEquals(0.75f, statistics.accuracy)
        assertEquals(5, statistics.totalProblem)
        assertEquals(4, statistics.solvedProblem)
        assertEquals(0.8f, statistics.progress)
        assertEquals(85.5f, statistics.averageScore)
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
            progress = 0f,
            averageScore = 0f
        )

        // Assert
        assertEquals(0, statistics.totalQuestions)
        assertEquals(0f, statistics.accuracy)
        assertEquals(0f, statistics.progress)
        assertEquals(0f, statistics.averageScore)
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
        assertEquals(1, tailQuestion.id)
        assertEquals("2-1", tailQuestion.number)
        assertEquals("Tail Question", tailQuestion.question)
        assertEquals("Tail Answer", tailQuestion.answer)
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
        assertEquals(true, response.isCorrect)
        assertEquals("1-1", response.numberStr)
        assertEquals(1, response.tailQuestion?.id)
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
        assertEquals(false, response.isCorrect)
        assertNull(response.numberStr)
        assertNull(response.tailQuestion)
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
        assertEquals(1, personalAssignment.id)
        assertEquals(1, personalAssignment.student.id)
        assertEquals("Assignment1", personalAssignment.assignment.title)
        assertEquals(PersonalAssignmentStatus.IN_PROGRESS, personalAssignment.status)
        assertEquals(2, personalAssignment.solvedNum)
        assertEquals("2025-01-01", personalAssignment.startedAt)
        assertNull(personalAssignment.submittedAt)
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
        assertEquals(1, studentInfo.id)
        assertEquals("Student1", studentInfo.displayName)
        assertEquals("s1@test.com", studentInfo.email)
    }

    @Test
    fun personalAssignmentInfo_withAllFields_containsCorrectValues() {
        // Arrange
        val info = PersonalAssignmentInfo(
            id = 1,
            title = "Assignment1",
            description = "Description",
            totalQuestions = 5,
            
            dueAt = "2025-12-31",
            grade = "1"
        )

        // Assert
        assertEquals(1, info.id)
        assertEquals("Assignment1", info.title)
        assertEquals(5, info.totalQuestions)
        assertEquals("1", info.grade)
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
            status = "SUBMITTED",
            submittedAt = "2025-01-01",
            answers = listOf("A1", "A2"),
            detailedAnswers = detailedAnswers
        )

        // Assert
        assertEquals("1", result.studentId)
        assertEquals("Student1", result.name)
        assertEquals(85, result.score)
        assertEquals("SUBMITTED", result.status)
        assertEquals(2, result.answers.size)
        assertEquals(1, result.detailedAnswers.size)
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
        assertEquals(1, answer.questionNumber)
        assertEquals("4", answer.studentAnswer)
        assertEquals("4", answer.correctAnswer)
        assertEquals(true, answer.isCorrect)
        assertEquals(95, answer.confidenceScore)
    }

    @Test
    fun audioRecordingState_initialState_hasDefaultValues() {
        // Arrange
        val state = AudioRecordingState()

        // Assert
        assertEquals(false, state.isRecording)
        assertEquals(0, state.recordingDuration)
        assertNull(state.audioFilePath)
        assertEquals(false, state.isProcessing)
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
        assertEquals(true, state.isRecording)
        assertEquals(10, state.recordingDuration)
        assertEquals("/path/to/audio.wav", state.audioFilePath)
    }
}

