package com.example.voicetutor.data.models

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import assert

@RunWith(JUnit4::class)
class ReportModelsTest {

    @Test
    fun progressReportData_withAllFields_containsCorrectValues() {
        // Arrange
        val classBreakdown = listOf(
            ClassProgress(classId = 1, className = "Class1", studentCount = 10, completedAssignments = 5, totalAssignments = 8)
        )
        val report = ProgressReportData(
            totalStudents = 50,
            totalAssignments = 20,
            completedAssignments = 15,
            averageScore = 85.5,
            classBreakdown = classBreakdown
        )

        // Assert
        assert(report.totalStudents == 50)
        assert(report.totalAssignments == 20)
        assert(report.completedAssignments == 15)
        assert(report.averageScore == 85.5)
        assert(report.classBreakdown.size == 1)
    }

    @Test
    fun progressReportData_withEmptyBreakdown_handlesEmptyList() {
        // Arrange
        val report = ProgressReportData(
            totalStudents = 0,
            totalAssignments = 0,
            completedAssignments = 0,
            averageScore = 0.0,
            classBreakdown = emptyList()
        )

        // Assert
        assert(report.classBreakdown.isEmpty())
    }

    @Test
    fun classProgress_withAllFields_containsCorrectValues() {
        // Arrange
        val classProgress = ClassProgress(
            classId = 1,
            className = "Class1",
            studentCount = 10,
            completedAssignments = 5,
            totalAssignments = 8
        )

        // Assert
        assert(classProgress.classId == 1)
        assert(classProgress.className == "Class1")
        assert(classProgress.studentCount == 10)
        assert(classProgress.completedAssignments == 5)
        assert(classProgress.totalAssignments == 8)
    }

    @Test
    fun questionResult_withAllFields_containsCorrectValues() {
        // Arrange
        val result = QuestionResult(
            questionNumber = 1,
            question = "What is 2+2?",
            myAnswer = "4",
            correctAnswer = "4",
            isCorrect = true,
            confidence = 0.95f
        )

        // Assert
        assert(result.questionNumber == 1)
        assert(result.question == "What is 2+2?")
        assert(result.myAnswer == "4")
        assert(result.correctAnswer == "4")
        assert(result.isCorrect == true)
        assert(result.confidence == 0.95f)
    }

    @Test
    fun questionResult_incorrectAnswer_showsIncorrect() {
        // Arrange
        val result = QuestionResult(
            questionNumber = 1,
            question = "What is 2+2?",
            myAnswer = "5",
            correctAnswer = "4",
            isCorrect = false,
            confidence = 0.3f
        )

        // Assert
        assert(result.isCorrect == false)
        assert(result.myAnswer != result.correctAnswer)
    }
}

