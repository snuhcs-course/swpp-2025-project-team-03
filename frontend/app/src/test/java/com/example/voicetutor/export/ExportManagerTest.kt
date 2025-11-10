package com.example.voicetutor.export

import org.junit.Test
import org.junit.Assert.*

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
            metadata = metadata
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
            type = ExportType.TEXT
        )
        
        assertTrue(exportData.metadata.isEmpty())
    }

    @Test
    fun exportResult_creation_success_createsSuccessResult() {
        val exportResult = ExportResult(
            success = true,
            filePath = "/path/to/file.pdf"
        )
        
        assertTrue(exportResult.success)
        assertEquals("/path/to/file.pdf", exportResult.filePath)
        assertNull(exportResult.error)
    }

    @Test
    fun exportResult_creation_failure_createsFailureResult() {
        val exportResult = ExportResult(
            success = false,
            error = "Export failed"
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
            submitDate = "2024-01-01"
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
            subjects = subjects
        )
        
        assertEquals(10, progressData.completedAssignments)
        assertEquals(20, progressData.totalAssignments)
        assertEquals(85, progressData.averageScore)
        assertEquals(50, progressData.studyHours)
        assertEquals(3, progressData.subjects.size)
    }
}

