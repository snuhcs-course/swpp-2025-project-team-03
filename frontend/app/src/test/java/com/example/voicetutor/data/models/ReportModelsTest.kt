package com.example.voicetutor.data.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class ReportModelsTest {

    @Test
    fun progressReportData_withAllFields_containsCorrectValues() {
        // Arrange
        val classBreakdown = listOf(
            ClassProgress(classId = 1, className = "Class1", studentCount = 10, completedAssignments = 5, totalAssignments = 8),
        )
        val report = ProgressReportData(
            totalStudents = 50,
            totalAssignments = 20,
            completedAssignments = 15,
            averageScore = 85.5,
            classBreakdown = classBreakdown,
        )

        // Assert
        assertEquals(50, report.totalStudents)
        assertEquals(20, report.totalAssignments)
        assertEquals(15, report.completedAssignments)
        assertEquals(85.5, report.averageScore, 0.01)
        assertEquals(1, report.classBreakdown.size)
    }

    @Test
    fun progressReportData_withEmptyBreakdown_handlesEmptyList() {
        // Arrange
        val report = ProgressReportData(
            totalStudents = 0,
            totalAssignments = 0,
            completedAssignments = 0,
            averageScore = 0.0,
            classBreakdown = emptyList(),
        )

        // Assert
        assertTrue(report.classBreakdown.isEmpty())
    }

    @Test
    fun classProgress_withAllFields_containsCorrectValues() {
        // Arrange
        val classProgress = ClassProgress(
            classId = 1,
            className = "Class1",
            studentCount = 10,
            completedAssignments = 5,
            totalAssignments = 8,
        )

        // Assert
        assertEquals(1, classProgress.classId)
        assertEquals("Class1", classProgress.className)
        assertEquals(10, classProgress.studentCount)
        assertEquals(5, classProgress.completedAssignments)
        assertEquals(8, classProgress.totalAssignments)
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
            confidence = 0.95f,
        )

        // Assert
        assertEquals(1, result.questionNumber)
        assertEquals("What is 2+2?", result.question)
        assertEquals("4", result.myAnswer)
        assertEquals("4", result.correctAnswer)
        assertEquals(true, result.isCorrect)
        assertEquals(0.95f, result.confidence, 0.01f)
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
            confidence = 0.3f,
        )

        // Assert
        assertEquals(false, result.isCorrect)
        assertTrue(result.myAnswer != result.correctAnswer)
    }
}
