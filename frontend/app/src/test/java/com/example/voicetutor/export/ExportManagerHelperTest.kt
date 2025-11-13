package com.example.voicetutor.export

import org.junit.Test
import org.junit.Assert.*

/**
 * Additional unit tests for ExportManager helper methods and edge cases.
 */
class ExportManagerHelperTest {

    @Test
    fun exportType_name_returnsCorrectName() {
        assertEquals("PDF", ExportType.PDF.name)
        assertEquals("TEXT", ExportType.TEXT.name)
        assertEquals("CSV", ExportType.CSV.name)
        assertEquals("JSON", ExportType.JSON.name)
    }

    @Test
    fun exportData_withAllExportTypes_createsCorrectly() {
        ExportType.values().forEach { type ->
            val data = ExportData(
                title = "Test",
                content = "Content",
                type = type
            )
            assertEquals(type, data.type)
        }
    }

    @Test
    fun exportData_withLargeMetadata_handlesCorrectly() {
        val largeMetadata = (1..100).associate { "key$it" to "value$it" }
        val data = ExportData(
            title = "Test",
            content = "Content",
            type = ExportType.JSON,
            metadata = largeMetadata
        )
        assertEquals(100, data.metadata.size)
    }

    @Test
    fun exportData_withEmptyTitle_handlesCorrectly() {
        val data = ExportData(
            title = "",
            content = "Content",
            type = ExportType.TEXT
        )
        assertTrue(data.title.isEmpty())
    }

    @Test
    fun exportData_withEmptyContent_handlesCorrectly() {
        val data = ExportData(
            title = "Title",
            content = "",
            type = ExportType.TEXT
        )
        assertTrue(data.content.isEmpty())
    }

    @Test
    fun exportResult_withNullError_handlesCorrectly() {
        val result = ExportResult(
            success = true,
            filePath = "/path/to/file",
            error = null
        )
        assertTrue(result.success)
        assertNotNull(result.filePath)
        assertNull(result.error)
    }

    @Test
    fun exportResult_withNullFilePath_handlesCorrectly() {
        val result = ExportResult(
            success = false,
            filePath = null,
            error = "Error message"
        )
        assertFalse(result.success)
        assertNull(result.filePath)
        assertNotNull(result.error)
    }

    @Test
    fun gradeData_withMaxScore_handlesCorrectly() {
        val gradeData = GradeData(
            assignmentName = "Test",
            subject = "Math",
            score = 100,
            maxScore = 100,
            percentage = 100,
            submitDate = "2024-01-01"
        )
        assertEquals(100, gradeData.score)
        assertEquals(100, gradeData.maxScore)
        assertEquals(100, gradeData.percentage)
    }

    @Test
    fun gradeData_withHighPercentage_handlesCorrectly() {
        val gradeData = GradeData(
            assignmentName = "Test",
            subject = "Math",
            score = 95,
            maxScore = 100,
            percentage = 95,
            submitDate = "2024-01-01"
        )
        assertEquals(95, gradeData.percentage)
    }

    @Test
    fun progressData_withZeroValues_handlesCorrectly() {
        val progressData = ProgressData(
            completedAssignments = 0,
            totalAssignments = 0,
            averageScore = 0,
            studyHours = 0,
            subjects = emptyList()
        )
        assertEquals(0, progressData.completedAssignments)
        assertEquals(0, progressData.totalAssignments)
        assertEquals(0, progressData.averageScore)
        assertEquals(0, progressData.studyHours)
        assertTrue(progressData.subjects.isEmpty())
    }

    @Test
    fun progressData_withLargeSubjectList_handlesCorrectly() {
        val subjects = (1..50).map { "Subject$it" }
        val progressData = ProgressData(
            completedAssignments = 10,
            totalAssignments = 20,
            averageScore = 85,
            studyHours = 50,
            subjects = subjects
        )
        assertEquals(50, progressData.subjects.size)
    }

    @Test
    fun exportData_hashCode_isConsistent() {
        val data1 = ExportData("Title", "Content", ExportType.PDF)
        val data2 = ExportData("Title", "Content", ExportType.PDF)
        assertEquals(data1.hashCode(), data2.hashCode())
    }

    @Test
    fun exportResult_hashCode_isConsistent() {
        val result1 = ExportResult(true, "/path/to/file")
        val result2 = ExportResult(true, "/path/to/file")
        assertEquals(result1.hashCode(), result2.hashCode())
    }

    @Test
    fun gradeData_hashCode_isConsistent() {
        val grade1 = GradeData("Test", "Math", 85, 100, 85, "2024-01-01")
        val grade2 = GradeData("Test", "Math", 85, 100, 85, "2024-01-01")
        assertEquals(grade1.hashCode(), grade2.hashCode())
    }

    @Test
    fun progressData_hashCode_isConsistent() {
        val progress1 = ProgressData(10, 20, 85, 50, listOf("Math"))
        val progress2 = ProgressData(10, 20, 85, 50, listOf("Math"))
        assertEquals(progress1.hashCode(), progress2.hashCode())
    }

    @Test
    fun exportData_toString_containsFields() {
        val data = ExportData("Title", "Content", ExportType.PDF)
        val string = data.toString()
        assertTrue(string.contains("Title") || string.contains("PDF"))
    }

    @Test
    fun exportResult_toString_containsFields() {
        val result = ExportResult(true, "/path/to/file")
        val string = result.toString()
        assertTrue(string.contains("true") || string.contains("/path"))
    }

    @Test
    fun gradeData_toString_containsFields() {
        val grade = GradeData("Test", "Math", 85, 100, 85, "2024-01-01")
        val string = grade.toString()
        assertTrue(string.contains("Test") || string.contains("Math"))
    }

    @Test
    fun progressData_toString_containsFields() {
        val progress = ProgressData(10, 20, 85, 50, listOf("Math"))
        val string = progress.toString()
        assertTrue(string.contains("10") || string.contains("Math"))
    }

    @Test
    fun exportData_componentAccess_worksCorrectly() {
        val data = ExportData("Title", "Content", ExportType.PDF, mapOf("key" to "value"))
        val (title, content, type, metadata) = data
        assertEquals("Title", title)
        assertEquals("Content", content)
        assertEquals(ExportType.PDF, type)
        assertEquals(1, metadata.size)
    }

    @Test
    fun exportResult_componentAccess_worksCorrectly() {
        val result = ExportResult(true, "/path/to/file", "error")
        val (success, filePath, error) = result
        assertTrue(success)
        assertEquals("/path/to/file", filePath)
        assertEquals("error", error)
    }

    @Test
    fun gradeData_componentAccess_worksCorrectly() {
        val grade = GradeData("Test", "Math", 85, 100, 85, "2024-01-01")
        val (name, subject, score, maxScore, percentage, date) = grade
        assertEquals("Test", name)
        assertEquals("Math", subject)
        assertEquals(85, score)
        assertEquals(100, maxScore)
        assertEquals(85, percentage)
        assertEquals("2024-01-01", date)
    }

    @Test
    fun progressData_componentAccess_worksCorrectly() {
        val progress = ProgressData(10, 20, 85, 50, listOf("Math"))
        val (completed, total, avgScore, hours, subjects) = progress
        assertEquals(10, completed)
        assertEquals(20, total)
        assertEquals(85, avgScore)
        assertEquals(50, hours)
        assertEquals(1, subjects.size)
    }
}

