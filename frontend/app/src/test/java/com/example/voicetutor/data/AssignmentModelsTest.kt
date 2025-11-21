package com.example.voicetutor.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AssignmentModelsTest {

    @Test
    fun assignmentStatus_values_containsAllStatuses() {
        // Act
        val values = AssignmentStatus.values()

        // Assert
        assertEquals(3, values.size)
        assertTrue(values.contains(AssignmentStatus.IN_PROGRESS))
        assertTrue(values.contains(AssignmentStatus.COMPLETED))
        assertTrue(values.contains(AssignmentStatus.DRAFT))
    }

    @Test
    fun assignmentStatus_valueOf_worksCorrectly() {
        // Act & Assert
        assertEquals(AssignmentStatus.IN_PROGRESS, AssignmentStatus.valueOf("IN_PROGRESS"))
        assertEquals(AssignmentStatus.COMPLETED, AssignmentStatus.valueOf("COMPLETED"))
        assertEquals(AssignmentStatus.DRAFT, AssignmentStatus.valueOf("DRAFT"))
    }

    @Test
    fun assignmentStatus_ordinal_isCorrect() {
        // Act & Assert
        assertEquals(0, AssignmentStatus.IN_PROGRESS.ordinal)
        assertEquals(1, AssignmentStatus.COMPLETED.ordinal)
        assertEquals(2, AssignmentStatus.DRAFT.ordinal)
    }

    @Test
    fun assignmentStatus_name_isCorrect() {
        // Act & Assert
        assertEquals("IN_PROGRESS", AssignmentStatus.IN_PROGRESS.name)
        assertEquals("COMPLETED", AssignmentStatus.COMPLETED.name)
        assertEquals("DRAFT", AssignmentStatus.DRAFT.name)
    }

    @Test
    fun assignmentFilter_values_containsAllFilters() {
        // Act
        val values = AssignmentFilter.values()

        // Assert
        assertEquals(3, values.size)
        assertTrue(values.contains(AssignmentFilter.ALL))
        assertTrue(values.contains(AssignmentFilter.IN_PROGRESS))
        assertTrue(values.contains(AssignmentFilter.COMPLETED))
    }

    @Test
    fun assignmentFilter_valueOf_worksCorrectly() {
        // Act & Assert
        assertEquals(AssignmentFilter.ALL, AssignmentFilter.valueOf("ALL"))
        assertEquals(AssignmentFilter.IN_PROGRESS, AssignmentFilter.valueOf("IN_PROGRESS"))
        assertEquals(AssignmentFilter.COMPLETED, AssignmentFilter.valueOf("COMPLETED"))
    }

    @Test
    fun assignmentFilter_ordinal_isCorrect() {
        // Act & Assert
        assertEquals(0, AssignmentFilter.ALL.ordinal)
        assertEquals(1, AssignmentFilter.IN_PROGRESS.ordinal)
        assertEquals(2, AssignmentFilter.COMPLETED.ordinal)
    }

    @Test
    fun assignmentFilter_name_isCorrect() {
        // Act & Assert
        assertEquals("ALL", AssignmentFilter.ALL.name)
        assertEquals("IN_PROGRESS", AssignmentFilter.IN_PROGRESS.name)
        assertEquals("COMPLETED", AssignmentFilter.COMPLETED.name)
    }

    @Test
    fun assignmentData_withAllFields_initializesCorrectly() {
        // Act
        val assignment = AssignmentData(
            id = 1,
            title = "Test Assignment",
            subject = "Math",
            className = "Class A",
            dueDate = "2025-12-31",
            submittedCount = 10,
            totalCount = 20,
            status = AssignmentStatus.IN_PROGRESS,
        )

        // Assert
        assertEquals(1, assignment.id)
        assertEquals("Test Assignment", assignment.title)
        assertEquals("Math", assignment.subject)
        assertEquals("Class A", assignment.className)
        assertEquals("2025-12-31", assignment.dueDate)
        assertEquals(10, assignment.submittedCount)
        assertEquals(20, assignment.totalCount)
        assertEquals(AssignmentStatus.IN_PROGRESS, assignment.status)
    }

    @Test
    fun assignmentData_withDifferentStatus_initializesCorrectly() {
        // Act
        val assignment1 = AssignmentData(
            id = 1,
            title = "Assignment 1",
            subject = "Math",
            className = "Class A",
            dueDate = "2025-12-31",
            submittedCount = 10,
            totalCount = 20,
            status = AssignmentStatus.COMPLETED,
        )

        val assignment2 = AssignmentData(
            id = 2,
            title = "Assignment 2",
            subject = "Science",
            className = "Class B",
            dueDate = "2025-12-31",
            submittedCount = 5,
            totalCount = 15,
            status = AssignmentStatus.DRAFT,
        )

        // Assert
        assertEquals(AssignmentStatus.COMPLETED, assignment1.status)
        assertEquals(AssignmentStatus.DRAFT, assignment2.status)
    }

    @Test
    fun assignmentData_equality_worksCorrectly() {
        // Act
        val assignment1 = AssignmentData(
            id = 1,
            title = "Test Assignment",
            subject = "Math",
            className = "Class A",
            dueDate = "2025-12-31",
            submittedCount = 10,
            totalCount = 20,
            status = AssignmentStatus.IN_PROGRESS,
        )

        val assignment2 = AssignmentData(
            id = 1,
            title = "Test Assignment",
            subject = "Math",
            className = "Class A",
            dueDate = "2025-12-31",
            submittedCount = 10,
            totalCount = 20,
            status = AssignmentStatus.IN_PROGRESS,
        )

        val assignment3 = AssignmentData(
            id = 2,
            title = "Test Assignment",
            subject = "Math",
            className = "Class A",
            dueDate = "2025-12-31",
            submittedCount = 10,
            totalCount = 20,
            status = AssignmentStatus.IN_PROGRESS,
        )

        // Assert
        assertEquals(assignment1, assignment2)
        assertNotEquals(assignment1, assignment3)
    }

    @Test
    fun assignmentData_hashCode_worksCorrectly() {
        // Act
        val assignment1 = AssignmentData(
            id = 1,
            title = "Test Assignment",
            subject = "Math",
            className = "Class A",
            dueDate = "2025-12-31",
            submittedCount = 10,
            totalCount = 20,
            status = AssignmentStatus.IN_PROGRESS,
        )

        val assignment2 = AssignmentData(
            id = 1,
            title = "Test Assignment",
            subject = "Math",
            className = "Class A",
            dueDate = "2025-12-31",
            submittedCount = 10,
            totalCount = 20,
            status = AssignmentStatus.IN_PROGRESS,
        )

        // Assert
        assertEquals(assignment1.hashCode(), assignment2.hashCode())
    }

    @Test
    fun assignmentData_copy_worksCorrectly() {
        // Arrange
        val original = AssignmentData(
            id = 1,
            title = "Test Assignment",
            subject = "Math",
            className = "Class A",
            dueDate = "2025-12-31",
            submittedCount = 10,
            totalCount = 20,
            status = AssignmentStatus.IN_PROGRESS,
        )

        // Act
        val copied = original.copy(
            title = "Updated Assignment",
            status = AssignmentStatus.COMPLETED,
        )

        // Assert
        assertEquals(1, copied.id)
        assertEquals("Updated Assignment", copied.title)
        assertEquals("Math", copied.subject)
        assertEquals("Class A", copied.className)
        assertEquals("2025-12-31", copied.dueDate)
        assertEquals(10, copied.submittedCount)
        assertEquals(20, copied.totalCount)
        assertEquals(AssignmentStatus.COMPLETED, copied.status)
        
        // Original should remain unchanged
        assertEquals("Test Assignment", original.title)
        assertEquals(AssignmentStatus.IN_PROGRESS, original.status)
    }

    @Test
    fun assignmentData_toString_containsFields() {
        // Act
        val assignment = AssignmentData(
            id = 1,
            title = "Test Assignment",
            subject = "Math",
            className = "Class A",
            dueDate = "2025-12-31",
            submittedCount = 10,
            totalCount = 20,
            status = AssignmentStatus.IN_PROGRESS,
        )

        val toString = assignment.toString()

        // Assert
        assertTrue(toString.contains("1"))
        assertTrue(toString.contains("Test Assignment"))
        assertTrue(toString.contains("Math"))
        assertTrue(toString.contains("Class A"))
    }

    @Test
    fun assignmentData_destructuring_worksCorrectly() {
        // Arrange
        val assignment = AssignmentData(
            id = 1,
            title = "Test Assignment",
            subject = "Math",
            className = "Class A",
            dueDate = "2025-12-31",
            submittedCount = 10,
            totalCount = 20,
            status = AssignmentStatus.IN_PROGRESS,
        )

        // Act
        val (id, title, subject, className, dueDate, submittedCount, totalCount, status) = assignment

        // Assert
        assertEquals(1, id)
        assertEquals("Test Assignment", title)
        assertEquals("Math", subject)
        assertEquals("Class A", className)
        assertEquals("2025-12-31", dueDate)
        assertEquals(10, submittedCount)
        assertEquals(20, totalCount)
        assertEquals(AssignmentStatus.IN_PROGRESS, status)
    }

    @Test
    fun assignmentData_withZeroCounts_initializesCorrectly() {
        // Act
        val assignment = AssignmentData(
            id = 1,
            title = "Test Assignment",
            subject = "Math",
            className = "Class A",
            dueDate = "2025-12-31",
            submittedCount = 0,
            totalCount = 0,
            status = AssignmentStatus.DRAFT,
        )

        // Assert
        assertEquals(0, assignment.submittedCount)
        assertEquals(0, assignment.totalCount)
        assertEquals(AssignmentStatus.DRAFT, assignment.status)
    }

    @Test
    fun assignmentData_withAllStatusValues_initializesCorrectly() {
        // Act
        val inProgress = AssignmentData(
            id = 1,
            title = "Assignment 1",
            subject = "Math",
            className = "Class A",
            dueDate = "2025-12-31",
            submittedCount = 5,
            totalCount = 10,
            status = AssignmentStatus.IN_PROGRESS,
        )

        val completed = AssignmentData(
            id = 2,
            title = "Assignment 2",
            subject = "Science",
            className = "Class B",
            dueDate = "2025-12-31",
            submittedCount = 10,
            totalCount = 10,
            status = AssignmentStatus.COMPLETED,
        )

        val draft = AssignmentData(
            id = 3,
            title = "Assignment 3",
            subject = "English",
            className = "Class C",
            dueDate = "2025-12-31",
            submittedCount = 0,
            totalCount = 10,
            status = AssignmentStatus.DRAFT,
        )

        // Assert
        assertEquals(AssignmentStatus.IN_PROGRESS, inProgress.status)
        assertEquals(AssignmentStatus.COMPLETED, completed.status)
        assertEquals(AssignmentStatus.DRAFT, draft.status)
    }
}

