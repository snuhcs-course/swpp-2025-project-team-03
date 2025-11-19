package com.example.voicetutor.export

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for ExportManager enums and data classes.
 */
class ExportManagerTest {

    @Test
    fun exportType_enumValues_areCorrect() {
        assertEquals(4, ExportType.values().size)
        assertTrue(ExportType.values().contains(ExportType.PDF))
        assertTrue(ExportType.values().contains(ExportType.TEXT))
        assertTrue(ExportType.values().contains(ExportType.CSV))
        assertTrue(ExportType.values().contains(ExportType.JSON))
    }

    @Test
    fun exportData_creation_withAllFields_createsCorrectInstance() {
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
    fun exportData_creation_withDefaultMetadata_usesEmptyMap() {
        val exportData = ExportData(
            title = "Test Export",
            content = "Test Content",
            type = ExportType.TEXT,
        )

        assertTrue(exportData.metadata.isEmpty())
    }

    @Test
    fun exportResult_creation_success_createsSuccessResult() {
        val exportResult = ExportResult(
            success = true,
            filePath = "/path/to/file.pdf",
        )

        assertTrue(exportResult.success)
        assertEquals("/path/to/file.pdf", exportResult.filePath)
        assertNull(exportResult.error)
    }

    @Test
    fun exportResult_creation_failure_createsFailureResult() {
        val exportResult = ExportResult(
            success = false,
            error = "Export failed",
        )

        assertFalse(exportResult.success)
        assertNull(exportResult.filePath)
        assertEquals("Export failed", exportResult.error)
    }

    @Test
    fun gradeData_creation_withAllFields_createsCorrectInstance() {
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
    fun progressData_creation_withAllFields_createsCorrectInstance() {
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
    fun exportData_copy_createsNewInstance() {
        val original = ExportData("Title", "Content", ExportType.PDF)
        val copy = original.copy(title = "New Title")

        assertEquals("New Title", copy.title)
        assertEquals(original.content, copy.content)
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
    fun gradeData_copy_createsNewInstance() {
        val original = GradeData("Assignment 1", "Math", 85, 100, 85, "2024-01-01")
        val copy = original.copy(score = 90)

        assertEquals(90, copy.score)
        assertEquals(original.assignmentName, copy.assignmentName)
    }

    @Test
    fun progressData_copy_createsNewInstance() {
        val original = ProgressData(10, 20, 85, 50, listOf("Math"))
        val copy = original.copy(completedAssignments = 15)

        assertEquals(15, copy.completedAssignments)
        assertEquals(original.totalAssignments, copy.totalAssignments)
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
    fun progressData_equality_worksCorrectly() {
        val progress1 = ProgressData(10, 20, 85, 50, listOf("Math", "Science"))
        val progress2 = ProgressData(10, 20, 85, 50, listOf("Math", "Science"))
        val progress3 = ProgressData(15, 20, 85, 50, listOf("Math", "Science"))

        assertEquals(progress1, progress2)
        assertNotEquals(progress1, progress3)
    }

    @Test
    fun exportType_allTypes_haveUniqueNames() {
        val names = ExportType.values().map { it.name }
        assertEquals(names.size, names.distinct().size)
    }

    @Test
    fun gradeData_withZeroScore_handlesCorrectly() {
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
    fun progressData_withEmptySubjects_handlesCorrectly() {
        val progressData = ProgressData(
            completedAssignments = 0,
            totalAssignments = 0,
            averageScore = 0,
            studyHours = 0,
            subjects = emptyList(),
        )

        assertTrue(progressData.subjects.isEmpty())
        assertEquals(0, progressData.completedAssignments)
    }

    @Test
    fun exportData_withEmptyContent_handlesCorrectly() {
        val exportData = ExportData(
            title = "Test Export",
            content = "",
            type = ExportType.TEXT,
        )

        assertTrue(exportData.content.isEmpty())
        assertEquals("Test Export", exportData.title)
    }

    @Test
    fun exportResult_withNullFilePath_handlesCorrectly() {
        val exportResult = ExportResult(
            success = true,
            filePath = null,
        )

        assertTrue(exportResult.success)
        assertNull(exportResult.filePath)
    }
}
