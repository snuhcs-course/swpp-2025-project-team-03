package com.example.voicetutor.export

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ExportManager helper functions and data classes.
 * These tests focus on testable logic without requiring Android Context.
 */
class ExportManagerFunctionsTest {

    @Test
    fun exportData_withAllFields_containsCorrectValues() {
        val metadata = mapOf("key1" to "value1", "key2" to "value2")
        val exportData = ExportData(
            title = "Test Export",
            content = "Test Content",
            type = ExportType.PDF,
            metadata = metadata,
        )

        assertEquals("Test Export", exportData.title)
        assertEquals("Test Content", exportData.content)
        assertEquals(ExportType.PDF, exportData.type)
        assertEquals(2, exportData.metadata.size)
    }

    @Test
    fun exportData_withEmptyContent_handlesEmptyString() {
        val exportData = ExportData(
            title = "Test",
            content = "",
            type = ExportType.TEXT,
        )

        assertTrue(exportData.content.isEmpty())
    }

    @Test
    fun exportData_withLongContent_handlesLongString() {
        val longContent = "A".repeat(10000)
        val exportData = ExportData(
            title = "Test",
            content = longContent,
            type = ExportType.TEXT,
        )

        assertEquals(10000, exportData.content.length)
    }

    @Test
    fun exportResult_success_containsFilePath() {
        val result = ExportResult(
            success = true,
            filePath = "/path/to/file.pdf",
        )

        assertTrue(result.success)
        assertEquals("/path/to/file.pdf", result.filePath)
        assertNull(result.error)
    }

    @Test
    fun exportResult_failure_containsError() {
        val result = ExportResult(
            success = false,
            error = "Export failed",
        )

        assertFalse(result.success)
        assertNull(result.filePath)
        assertEquals("Export failed", result.error)
    }

    @Test
    fun exportResult_withNullFilePath_handlesNull() {
        val result = ExportResult(
            success = true,
            filePath = null,
        )

        assertTrue(result.success)
        assertNull(result.filePath)
    }

    @Test
    fun exportResult_copy_createsNewInstance() {
        val original = ExportResult(true, "/path/to/file.pdf")
        val copy = original.copy(success = false, error = "Error")

        assertFalse(copy.success)
        assertEquals("Error", copy.error)
    }

    @Test
    fun exportResult_equality_worksCorrectly() {
        val result1 = ExportResult(true, "/path/to/file.pdf")
        val result2 = ExportResult(true, "/path/to/file.pdf")
        val result3 = ExportResult(false, error = "Error")

        assertEquals(result1, result2)
        assertNotEquals(result1, result3)
    }

    @Test
    fun exportResult_hashCode_worksCorrectly() {
        val result1 = ExportResult(true, "/path/to/file.pdf")
        val result2 = ExportResult(true, "/path/to/file.pdf")

        assertEquals(result1.hashCode(), result2.hashCode())
    }

    @Test
    fun gradeData_withAllFields_containsCorrectValues() {
        val gradeData = GradeData(
            assignmentName = "Assignment 1",
            subject = "Math",
            score = 85,
            maxScore = 100,
            percentage = 85,
            submitDate = "2024-01-01",
        )

        assertEquals("Assignment 1", gradeData.assignmentName)
        assertEquals("Math", gradeData.subject)
        assertEquals(85, gradeData.score)
        assertEquals(100, gradeData.maxScore)
        assertEquals(85, gradeData.percentage)
        assertEquals("2024-01-01", gradeData.submitDate)
    }

    @Test
    fun gradeData_withZeroScore_handlesZero() {
        val gradeData = GradeData(
            assignmentName = "Assignment 1",
            subject = "Math",
            score = 0,
            maxScore = 100,
            percentage = 0,
            submitDate = "2024-01-01",
        )

        assertEquals(0, gradeData.score)
        assertEquals(0, gradeData.percentage)
    }

    @Test
    fun gradeData_withPerfectScore_handlesPerfect() {
        val gradeData = GradeData(
            assignmentName = "Assignment 1",
            subject = "Math",
            score = 100,
            maxScore = 100,
            percentage = 100,
            submitDate = "2024-01-01",
        )

        assertEquals(100, gradeData.score)
        assertEquals(100, gradeData.percentage)
    }

    @Test
    fun gradeData_copy_createsNewInstance() {
        val original = GradeData("Assignment 1", "Math", 85, 100, 85, "2024-01-01")
        val copy = original.copy(score = 90, percentage = 90)

        assertEquals(90, copy.score)
        assertEquals(90, copy.percentage)
        assertEquals(original.assignmentName, copy.assignmentName)
    }

    @Test
    fun gradeData_equality_worksCorrectly() {
        val grade1 = GradeData("Assignment 1", "Math", 85, 100, 85, "2024-01-01")
        val grade2 = GradeData("Assignment 1", "Math", 85, 100, 85, "2024-01-01")
        val grade3 = GradeData("Assignment 2", "Math", 85, 100, 85, "2024-01-01")

        assertEquals(grade1, grade2)
        assertNotEquals(grade1, grade3)
    }

    @Test
    fun gradeData_hashCode_worksCorrectly() {
        val grade1 = GradeData("Assignment 1", "Math", 85, 100, 85, "2024-01-01")
        val grade2 = GradeData("Assignment 1", "Math", 85, 100, 85, "2024-01-01")

        assertEquals(grade1.hashCode(), grade2.hashCode())
    }

    @Test
    fun progressData_withAllFields_containsCorrectValues() {
        val subjects = listOf("Math", "Science", "English")
        val progressData = ProgressData(
            completedAssignments = 10,
            totalAssignments = 20,
            averageScore = 85,
            studyHours = 50,
            subjects = subjects,
        )

        assertEquals(10, progressData.completedAssignments)
        assertEquals(20, progressData.totalAssignments)
        assertEquals(85, progressData.averageScore)
        assertEquals(50, progressData.studyHours)
        assertEquals(3, progressData.subjects.size)
    }

    @Test
    fun progressData_withEmptySubjects_handlesEmptyList() {
        val progressData = ProgressData(
            completedAssignments = 0,
            totalAssignments = 0,
            averageScore = 0,
            studyHours = 0,
            subjects = emptyList(),
        )

        assertTrue(progressData.subjects.isEmpty())
    }

    @Test
    fun progressData_withZeroValues_handlesZeros() {
        val progressData = ProgressData(
            completedAssignments = 0,
            totalAssignments = 0,
            averageScore = 0,
            studyHours = 0,
            subjects = emptyList(),
        )

        assertEquals(0, progressData.completedAssignments)
        assertEquals(0, progressData.averageScore)
    }

    @Test
    fun progressData_copy_createsNewInstance() {
        val original = ProgressData(10, 20, 85, 50, listOf("Math"))
        val copy = original.copy(completedAssignments = 15)

        assertEquals(15, copy.completedAssignments)
        assertEquals(original.totalAssignments, copy.totalAssignments)
    }

    @Test
    fun progressData_equality_worksCorrectly() {
        val progress1 = ProgressData(10, 20, 85, 50, listOf("Math", "Science"))
        val progress2 = ProgressData(10, 20, 85, 50, listOf("Math", "Science"))
        val progress3 = ProgressData(15, 20, 85, 50, listOf("Math", "Science"))

        assertEquals(progress1, progress2)
        assertNotEquals(progress1, progress3)
    }

    @Test
    fun progressData_hashCode_worksCorrectly() {
        val progress1 = ProgressData(10, 20, 85, 50, listOf("Math"))
        val progress2 = ProgressData(10, 20, 85, 50, listOf("Math"))

        assertEquals(progress1.hashCode(), progress2.hashCode())
    }

    @Test
    fun exportType_allTypes_haveUniqueNames() {
        val names = ExportType.values().map { it.name }
        assertEquals(names.size, names.distinct().size)
    }

    @Test
    fun exportType_values_containsAllTypes() {
        val values = ExportType.values()
        assertEquals(4, values.size)
        assertTrue(values.contains(ExportType.PDF))
        assertTrue(values.contains(ExportType.TEXT))
        assertTrue(values.contains(ExportType.CSV))
        assertTrue(values.contains(ExportType.JSON))
    }

    @Test
    fun exportData_toString_containsFields() {
        val exportData = ExportData("Title", "Content", ExportType.PDF)
        val toString = exportData.toString()

        assertTrue(toString.contains("Title"))
        assertTrue(toString.contains("Content"))
        assertTrue(toString.contains("PDF"))
    }

    @Test
    fun exportResult_toString_containsFields() {
        val result = ExportResult(true, "/path/to/file.pdf")
        val toString = result.toString()

        assertTrue(toString.contains("success=true"))
        assertTrue(toString.contains("/path/to/file.pdf"))
    }

    @Test
    fun gradeData_toString_containsFields() {
        val grade = GradeData("Assignment 1", "Math", 85, 100, 85, "2024-01-01")
        val toString = grade.toString()

        assertTrue(toString.contains("Assignment 1"))
        assertTrue(toString.contains("Math"))
        assertTrue(toString.contains("85"))
    }

    @Test
    fun progressData_toString_containsFields() {
        val progress = ProgressData(10, 20, 85, 50, listOf("Math"))
        val toString = progress.toString()

        assertTrue(toString.contains("10"))
        assertTrue(toString.contains("20"))
        assertTrue(toString.contains("85"))
    }

    @Test
    fun exportData_withSpecialCharacters_handlesCorrectly() {
        val exportData = ExportData(
            title = "Test & Export < > \" '",
            content = "Content with\nnewlines\tand\ttabs",
            type = ExportType.TEXT,
        )

        assertTrue(exportData.title.contains("&"))
        assertTrue(exportData.content.contains("\n"))
    }

    @Test
    fun gradeData_withSpecialCharacters_handlesCorrectly() {
        val gradeData = GradeData(
            assignmentName = "Assignment & Test",
            subject = "Math & Science",
            score = 85,
            maxScore = 100,
            percentage = 85,
            submitDate = "2024-01-01",
        )

        assertTrue(gradeData.assignmentName.contains("&"))
        assertTrue(gradeData.subject.contains("&"))
    }
}
