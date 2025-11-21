package com.example.voicetutor.data.models

import org.junit.Assert.*
import org.junit.Test

class AssignmentDataModelsTest {

    private fun buildSubject() = Subject(id = 1, name = "Math", code = "MATH")
    private fun buildCourseClass() = CourseClass(
        id = 1,
        name = "Class1",
        description = "Description",
        subject = buildSubject(),
        teacherName = "Teacher1",
        studentCount = 10,
        createdAt = "2025-01-01",
    )

    @Test
    fun assignmentData_withAllFields_initializesCorrectly() {
        // Arrange
        val materials = listOf(
            Material(id = 1, kind = "PDF", s3Key = "key1", bytes = 1024, createdAt = "2025-01-01"),
        )

        // Act
        val assignment = AssignmentData(
            id = 1,
            title = "Test Assignment",
            description = "Test Description",
            totalQuestions = 10,
            createdAt = "2025-01-01",
            dueAt = "2025-12-31",
            courseClass = buildCourseClass(),
            materials = materials,
            grade = "1학년",
            personalAssignmentStatus = PersonalAssignmentStatus.IN_PROGRESS,
            solvedNum = 5,
            personalAssignmentId = 100,
            submittedAt = "2025-01-02",
        )

        // Assert
        assertEquals(1, assignment.id)
        assertEquals("Test Assignment", assignment.title)
        assertEquals("Test Description", assignment.description)
        assertEquals(10, assignment.totalQuestions)
        assertEquals("2025-01-01", assignment.createdAt)
        assertEquals("2025-12-31", assignment.dueAt)
        assertNotNull(assignment.materials)
        assertEquals(1, assignment.materials?.size)
        assertEquals("1학년", assignment.grade)
        assertEquals(PersonalAssignmentStatus.IN_PROGRESS, assignment.personalAssignmentStatus)
        assertEquals(5, assignment.solvedNum)
        assertEquals(100, assignment.personalAssignmentId)
        assertEquals("2025-01-02", assignment.submittedAt)
    }

    @Test
    fun assignmentData_withNullOptionalFields_initializesCorrectly() {
        // Act
        val assignment = AssignmentData(
            id = 1,
            title = "Test Assignment",
            totalQuestions = 10,
            dueAt = "2025-12-31",
            courseClass = buildCourseClass(),
        )

        // Assert
        assertNull(assignment.description)
        assertNull(assignment.createdAt)
        assertNull(assignment.materials)
        assertNull(assignment.grade)
        assertNull(assignment.personalAssignmentStatus)
        assertNull(assignment.solvedNum)
        assertNull(assignment.personalAssignmentId)
        assertNull(assignment.submittedAt)
    }

    @Test
    fun material_withAllFields_initializesCorrectly() {
        // Act
        val material = Material(
            id = 1,
            kind = "PDF",
            s3Key = "assignments/1/material.pdf",
            bytes = 2048,
            createdAt = "2025-01-01T00:00:00Z",
        )

        // Assert
        assertEquals(1, material.id)
        assertEquals("PDF", material.kind)
        assertEquals("assignments/1/material.pdf", material.s3Key)
        assertEquals(2048, material.bytes)
        assertEquals("2025-01-01T00:00:00Z", material.createdAt)
    }

    @Test
    fun material_withNullBytes_initializesCorrectly() {
        // Act
        val material = Material(
            id = 1,
            kind = "PDF",
            s3Key = "key",
            createdAt = "2025-01-01",
        )

        // Assert
        assertNull(material.bytes)
    }

    @Test
    fun questionData_withAllFields_initializesCorrectly() {
        // Arrange
        val options = listOf("A", "B", "C", "D")

        // Act
        val question = QuestionData(
            id = 1,
            question = "What is 2+2?",
            type = "multiple_choice",
            options = options,
            correctAnswer = "4",
            points = 5,
            explanation = "Basic addition",
        )

        // Assert
        assertEquals(1, question.id)
        assertEquals("What is 2+2?", question.question)
        assertEquals("multiple_choice", question.type)
        assertEquals(options, question.options)
        assertEquals("4", question.correctAnswer)
        assertEquals(5, question.points)
        assertEquals("Basic addition", question.explanation)
    }

    @Test
    fun questionData_withDefaultValues_initializesCorrectly() {
        // Act
        val question = QuestionData(
            id = 1,
            question = "Test?",
            type = "short_answer",
            correctAnswer = "Answer",
        )

        // Assert
        assertNull(question.options)
        assertEquals(1, question.points) // Default value
        assertNull(question.explanation)
    }

    @Test
    fun personalAssignmentData_withAllFields_initializesCorrectly() {
        // Arrange
        val student = StudentInfo(id = 1, displayName = "Student1", email = "s1@test.com")
        val assignmentInfo = PersonalAssignmentInfo(
            id = 1,
            title = "Assignment",
            description = "Desc",
            totalQuestions = 10,
            dueAt = "2025-12-31",
            grade = "1학년",
        )

        // Act
        val personalAssignment = PersonalAssignmentData(
            id = 1,
            student = student,
            assignment = assignmentInfo,
            status = PersonalAssignmentStatus.IN_PROGRESS,
            solvedNum = 5,
            startedAt = "2025-01-01",
            submittedAt = "2025-01-02",
        )

        // Assert
        assertEquals(1, personalAssignment.id)
        assertEquals(student, personalAssignment.student)
        assertEquals(assignmentInfo, personalAssignment.assignment)
        assertEquals(PersonalAssignmentStatus.IN_PROGRESS, personalAssignment.status)
        assertEquals(5, personalAssignment.solvedNum)
        assertEquals("2025-01-01", personalAssignment.startedAt)
        assertEquals("2025-01-02", personalAssignment.submittedAt)
    }

    @Test
    fun personalAssignmentData_withNullOptionalFields_initializesCorrectly() {
        // Arrange
        val student = StudentInfo(id = 1, displayName = "Student1", email = "s1@test.com")
        val assignmentInfo = PersonalAssignmentInfo(
            id = 1,
            title = "Assignment",
            description = "Desc",
            totalQuestions = 10,
            dueAt = "2025-12-31",
            grade = "1학년",
        )

        // Act
        val personalAssignment = PersonalAssignmentData(
            id = 1,
            student = student,
            assignment = assignmentInfo,
            status = PersonalAssignmentStatus.NOT_STARTED,
            solvedNum = 0,
        )

        // Assert
        assertNull(personalAssignment.startedAt)
        assertNull(personalAssignment.submittedAt)
    }

    @Test
    fun personalAssignmentQuestion_withAllFields_initializesCorrectly() {
        // Act
        val question = PersonalAssignmentQuestion(
            id = 1,
            number = "2-2",
            question = "Test question?",
            answer = "Answer",
            explanation = "Explanation",
            difficulty = "EASY",
            isProcessing = true,
        )

        // Assert
        assertEquals(1, question.id)
        assertEquals("2-2", question.number)
        assertEquals("Test question?", question.question)
        assertEquals("Answer", question.answer)
        assertEquals("Explanation", question.explanation)
        assertEquals("EASY", question.difficulty)
        assertTrue(question.isProcessing)
    }

    @Test
    fun personalAssignmentQuestion_withDefaultIsProcessing_initializesCorrectly() {
        // Act
        val question = PersonalAssignmentQuestion(
            id = 1,
            number = "1",
            question = "Test?",
            answer = "Answer",
            explanation = "Exp",
            difficulty = "MEDIUM",
        )

        // Assert
        assertFalse(question.isProcessing) // Default value
    }

    @Test
    fun answerSubmissionResponse_withAllFields_initializesCorrectly() {
        // Arrange
        val tailQuestion = TailQuestion(
            id = 2,
            number = "2-1",
            question = "Follow-up?",
            answer = "Answer",
            explanation = "Exp",
            difficulty = "EASY",
        )

        // Act
        val response = AnswerSubmissionResponse(
            isCorrect = true,
            numberStr = "1-1",
            tailQuestion = tailQuestion,
        )

        // Assert
        assertTrue(response.isCorrect)
        assertEquals("1-1", response.numberStr)
        assertEquals(tailQuestion, response.tailQuestion)
    }

    @Test
    fun answerSubmissionResponse_withNullOptionalFields_initializesCorrectly() {
        // Act
        val response = AnswerSubmissionResponse(
            isCorrect = false,
            numberStr = null,
            tailQuestion = null,
        )

        // Assert
        assertFalse(response.isCorrect)
        assertNull(response.numberStr)
        assertNull(response.tailQuestion)
    }

    @Test
    fun tailQuestion_withAllFields_initializesCorrectly() {
        // Act
        val tailQuestion = TailQuestion(
            id = 1,
            number = "2-1",
            question = "Question?",
            answer = "Answer",
            explanation = "Explanation",
            difficulty = "HARD",
        )

        // Assert
        assertEquals(1, tailQuestion.id)
        assertEquals("2-1", tailQuestion.number)
        assertEquals("Question?", tailQuestion.question)
        assertEquals("Answer", tailQuestion.answer)
        assertEquals("Explanation", tailQuestion.explanation)
        assertEquals("HARD", tailQuestion.difficulty)
    }

    @Test
    fun assignmentResultData_withAllFields_initializesCorrectly() {
        // Act
        val result = AssignmentResultData(
            submittedStudents = 18,
            totalStudents = 25,
            averageScore = 85.5,
            completionRate = 0.72,
        )

        // Assert
        assertEquals(18, result.submittedStudents)
        assertEquals(25, result.totalStudents)
        assertEquals(85.5, result.averageScore!!, 0.01)
        assertEquals(0.72, result.completionRate!!, 0.01)
    }

    @Test
    fun assignmentResultData_withNullFields_initializesCorrectly() {
        // Act
        val result = AssignmentResultData()

        // Assert
        assertNull(result.submittedStudents)
        assertNull(result.totalStudents)
        assertNull(result.averageScore)
        assertNull(result.completionRate)
    }

    @Test
    fun personalAssignmentStatistics_withAllFields_initializesCorrectly() {
        // Act
        val stats = PersonalAssignmentStatistics(
            totalQuestions = 10,
            answeredQuestions = 8,
            correctAnswers = 7,
            accuracy = 0.875f,
            totalProblem = 10,
            solvedProblem = 8,
            progress = 0.8f,
            averageScore = 87.5f,
        )

        // Assert
        assertEquals(10, stats.totalQuestions)
        assertEquals(8, stats.answeredQuestions)
        assertEquals(7, stats.correctAnswers)
        assertEquals(0.875f, stats.accuracy)
        assertEquals(10, stats.totalProblem)
        assertEquals(8, stats.solvedProblem)
        assertEquals(0.8f, stats.progress)
        assertEquals(87.5f, stats.averageScore)
    }

    @Test
    fun assignmentCorrectnessItem_withAllFields_initializesCorrectly() {
        // Act
        val item = AssignmentCorrectnessItem(
            questionContent = "What is 2+2?",
            questionModelAnswer = "4",
            studentAnswer = "4",
            isCorrect = true,
            answeredAt = "2025-01-01T10:00:00Z",
            questionNum = "1",
            explanation = "Basic addition",
        )

        // Assert
        assertEquals("What is 2+2?", item.questionContent)
        assertEquals("4", item.questionModelAnswer)
        assertEquals("4", item.studentAnswer)
        assertTrue(item.isCorrect)
        assertEquals("2025-01-01T10:00:00Z", item.answeredAt)
        assertEquals("1", item.questionNum)
        assertEquals("Basic addition", item.explanation)
    }

    @Test
    fun assignmentCorrectnessItem_withIncorrectAnswer_initializesCorrectly() {
        // Act
        val item = AssignmentCorrectnessItem(
            questionContent = "What is 2+2?",
            questionModelAnswer = "4",
            studentAnswer = "5",
            isCorrect = false,
            answeredAt = "2025-01-01T10:00:00Z",
            questionNum = "1",
            explanation = "Basic addition",
        )

        // Assert
        assertFalse(item.isCorrect)
        assertEquals("5", item.studentAnswer)
    }

    @Test
    fun courseClass_withAllFields_initializesCorrectly() {
        // Act
        val courseClass = CourseClass(
            id = 1,
            name = "Math Class",
            description = "Math Description",
            subject = buildSubject(),
            teacherName = "Teacher1",
            studentCount = 25,
            createdAt = "2025-01-01",
        )

        // Assert
        assertEquals(1, courseClass.id)
        assertEquals("Math Class", courseClass.name)
        assertEquals("Math Description", courseClass.description)
        assertEquals("Teacher1", courseClass.teacherName)
        assertEquals(25, courseClass.studentCount)
        assertEquals("2025-01-01", courseClass.createdAt)
    }

    @Test
    fun courseClass_withNullDescription_initializesCorrectly() {
        // Act
        val courseClass = CourseClass(
            id = 1,
            name = "Math Class",
            description = null,
            subject = buildSubject(),
            teacherName = "Teacher1",
            studentCount = 25,
            createdAt = "2025-01-01",
        )

        // Assert
        assertNull(courseClass.description)
    }

    @Test
    fun subject_withAllFields_initializesCorrectly() {
        // Act
        val subject = Subject(id = 1, name = "Mathematics", code = "MATH")

        // Assert
        assertEquals(1, subject.id)
        assertEquals("Mathematics", subject.name)
        assertEquals("MATH", subject.code)
    }

    @Test
    fun subject_withNullCode_initializesCorrectly() {
        // Act
        val subject = Subject(id = 1, name = "Mathematics")

        // Assert
        assertNull(subject.code)
    }

    @Test
    fun studentInfo_withAllFields_initializesCorrectly() {
        // Act
        val student = StudentInfo(id = 1, displayName = "John Doe", email = "john@test.com")

        // Assert
        assertEquals(1, student.id)
        assertEquals("John Doe", student.displayName)
        assertEquals("john@test.com", student.email)
    }

    @Test
    fun personalAssignmentInfo_withAllFields_initializesCorrectly() {
        // Act
        val info = PersonalAssignmentInfo(
            id = 1,
            title = "Assignment",
            description = "Description",
            totalQuestions = 10,
            dueAt = "2025-12-31",
            grade = "1학년",
        )

        // Assert
        assertEquals(1, info.id)
        assertEquals("Assignment", info.title)
        assertEquals("Description", info.description)
        assertEquals(10, info.totalQuestions)
        assertEquals("2025-12-31", info.dueAt)
        assertEquals("1학년", info.grade)
    }
}

