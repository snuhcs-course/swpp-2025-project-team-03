package com.example.voicetutor.data.models

import org.junit.Assert.*
import org.junit.Test

class ReportModelsTest {

    @Test
    fun progressReportData_withAllFields_initializesCorrectly() {
        val classBreakdown = listOf(
            ClassProgress(
                classId = 1,
                className = "Math Class",
                studentCount = 25,
                completedAssignments = 18,
                totalAssignments = 20,
            ),
        )
        val report = ProgressReportData(
            totalStudents = 50,
            totalAssignments = 40,
            completedAssignments = 30,
            averageScore = 85.5,
            classBreakdown = classBreakdown,
        )
        assertEquals(50, report.totalStudents)
        assertEquals(40, report.totalAssignments)
        assertEquals(30, report.completedAssignments)
        assertEquals(85.5, report.averageScore, 0.01)
        assertEquals(1, report.classBreakdown.size)
    }

    @Test
    fun progressReportData_withEmptyClassBreakdown_initializesCorrectly() {
        val report = ProgressReportData(
            totalStudents = 0,
            totalAssignments = 0,
            completedAssignments = 0,
            averageScore = 0.0,
            classBreakdown = emptyList(),
        )
        assertTrue(report.classBreakdown.isEmpty())
    }

    @Test
    fun classProgress_withAllFields_initializesCorrectly() {
        val classProgress = ClassProgress(
            classId = 1,
            className = "Math Class",
            studentCount = 25,
            completedAssignments = 18,
            totalAssignments = 20,
        )
        assertEquals(1, classProgress.classId)
        assertEquals("Math Class", classProgress.className)
        assertEquals(25, classProgress.studentCount)
        assertEquals(18, classProgress.completedAssignments)
        assertEquals(20, classProgress.totalAssignments)
    }

    @Test
    fun questionResult_withAllFields_initializesCorrectly() {
        val result = QuestionResult(
            questionNumber = 1,
            question = "What is 2+2?",
            myAnswer = "4",
            correctAnswer = "4",
            isCorrect = true,
            confidence = 0.95f,
        )
        assertEquals(1, result.questionNumber)
        assertEquals("What is 2+2?", result.question)
        assertEquals("4", result.myAnswer)
        assertEquals("4", result.correctAnswer)
        assertTrue(result.isCorrect)
        assertEquals(0.95f, result.confidence, 0.01f)
    }

    @Test
    fun questionResult_withIncorrectAnswer_initializesCorrectly() {
        val result = QuestionResult(
            questionNumber = 1,
            question = "What is 2+2?",
            myAnswer = "5",
            correctAnswer = "4",
            isCorrect = false,
            confidence = 0.3f,
        )
        assertFalse(result.isCorrect)
        assertNotEquals(result.myAnswer, result.correctAnswer)
    }

    @Test
    fun achievementStatistics_withAllFields_initializesCorrectly() {
        val stats = AchievementStatistics(
            totalQuestions = 20,
            correctQuestions = 18,
            accuracy = 0.9,
            content = "수와 연산",
        )
        assertEquals(20, stats.totalQuestions)
        assertEquals(18, stats.correctQuestions)
        assertEquals(0.9, stats.accuracy, 0.01)
        assertEquals("수와 연산", stats.content)
    }

    @Test
    fun curriculumReportData_withAllFields_initializesCorrectly() {
        val achievementStats = mapOf(
            "achievement1" to AchievementStatistics(
                totalQuestions = 10,
                correctQuestions = 9,
                accuracy = 0.9,
                content = "Content 1",
            ),
            "achievement2" to AchievementStatistics(
                totalQuestions = 10,
                correctQuestions = 8,
                accuracy = 0.8,
                content = "Content 2",
            ),
        )
        val report = CurriculumReportData(
            totalQuestions = 20,
            totalCorrect = 17,
            overallAccuracy = 0.85,
            achievementStatistics = achievementStats,
        )
        assertEquals(20, report.totalQuestions)
        assertEquals(17, report.totalCorrect)
        assertEquals(0.85, report.overallAccuracy, 0.01)
        assertEquals(2, report.achievementStatistics.size)
    }

    @Test
    fun curriculumReportData_withEmptyAchievementStatistics_initializesCorrectly() {
        val report = CurriculumReportData(
            totalQuestions = 0,
            totalCorrect = 0,
            overallAccuracy = 0.0,
            achievementStatistics = emptyMap(),
        )
        assertTrue(report.achievementStatistics.isEmpty())
    }

    @Test
    fun progressReportData_copy_createsNewInstance() {
        val original = ProgressReportData(
            totalStudents = 50,
            totalAssignments = 40,
            completedAssignments = 30,
            averageScore = 85.5,
            classBreakdown = emptyList(),
        )
        val copy = original.copy(averageScore = 90.0)
        assertEquals(90.0, copy.averageScore, 0.01)
        assertEquals(original.totalStudents, copy.totalStudents)
    }

    @Test
    fun classProgress_copy_createsNewInstance() {
        val original = ClassProgress(1, "Class1", 25, 18, 20)
        val copy = original.copy(className = "Class2")
        assertEquals("Class2", copy.className)
        assertEquals(original.classId, copy.classId)
    }

    @Test
    fun questionResult_copy_createsNewInstance() {
        val original = QuestionResult(1, "Q?", "A", "A", true, 0.9f)
        val copy = original.copy(isCorrect = false)
        assertFalse(copy.isCorrect)
        assertEquals(original.questionNumber, copy.questionNumber)
    }

    @Test
    fun achievementStatistics_copy_createsNewInstance() {
        val original = AchievementStatistics(20, 18, 0.9, "Content")
        val copy = original.copy(accuracy = 0.95)
        assertEquals(0.95, copy.accuracy, 0.01)
        assertEquals(original.totalQuestions, copy.totalQuestions)
    }

    @Test
    fun curriculumReportData_copy_createsNewInstance() {
        val original = CurriculumReportData(20, 17, 0.85, emptyMap())
        val copy = original.copy(overallAccuracy = 0.9)
        assertEquals(0.9, copy.overallAccuracy, 0.01)
        assertEquals(original.totalQuestions, copy.totalQuestions)
    }

    @Test
    fun progressReportData_equality_worksCorrectly() {
        val report1 = ProgressReportData(50, 40, 30, 85.5, emptyList())
        val report2 = ProgressReportData(50, 40, 30, 85.5, emptyList())
        val report3 = ProgressReportData(60, 40, 30, 85.5, emptyList())
        assertEquals(report1, report2)
        assertNotEquals(report1, report3)
    }

    @Test
    fun classProgress_equality_worksCorrectly() {
        val progress1 = ClassProgress(1, "Class1", 25, 18, 20)
        val progress2 = ClassProgress(1, "Class1", 25, 18, 20)
        val progress3 = ClassProgress(2, "Class1", 25, 18, 20)
        assertEquals(progress1, progress2)
        assertNotEquals(progress1, progress3)
    }

    @Test
    fun questionResult_equality_worksCorrectly() {
        val result1 = QuestionResult(1, "Q?", "A", "A", true, 0.9f)
        val result2 = QuestionResult(1, "Q?", "A", "A", true, 0.9f)
        val result3 = QuestionResult(2, "Q?", "A", "A", true, 0.9f)
        assertEquals(result1, result2)
        assertNotEquals(result1, result3)
    }

    @Test
    fun achievementStatistics_equality_worksCorrectly() {
        val stats1 = AchievementStatistics(20, 18, 0.9, "Content")
        val stats2 = AchievementStatistics(20, 18, 0.9, "Content")
        val stats3 = AchievementStatistics(21, 18, 0.9, "Content")
        assertEquals(stats1, stats2)
        assertNotEquals(stats1, stats3)
    }

    @Test
    fun curriculumReportData_equality_worksCorrectly() {
        val report1 = CurriculumReportData(20, 17, 0.85, emptyMap())
        val report2 = CurriculumReportData(20, 17, 0.85, emptyMap())
        val report3 = CurriculumReportData(21, 17, 0.85, emptyMap())
        assertEquals(report1, report2)
        assertNotEquals(report1, report3)
    }
}
