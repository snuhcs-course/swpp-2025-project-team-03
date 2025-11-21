package com.example.voicetutor.data.models

import org.junit.Assert.*
import org.junit.Test

class EnumModelsTest {

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
    fun personalAssignmentStatus_values_containsAllStatuses() {
        // Act
        val values = PersonalAssignmentStatus.values()

        // Assert
        assertEquals(3, values.size)
        assertTrue(values.contains(PersonalAssignmentStatus.NOT_STARTED))
        assertTrue(values.contains(PersonalAssignmentStatus.IN_PROGRESS))
        assertTrue(values.contains(PersonalAssignmentStatus.SUBMITTED))
    }

    @Test
    fun personalAssignmentStatus_valueOf_worksCorrectly() {
        // Act & Assert
        assertEquals(PersonalAssignmentStatus.NOT_STARTED, PersonalAssignmentStatus.valueOf("NOT_STARTED"))
        assertEquals(PersonalAssignmentStatus.IN_PROGRESS, PersonalAssignmentStatus.valueOf("IN_PROGRESS"))
        assertEquals(PersonalAssignmentStatus.SUBMITTED, PersonalAssignmentStatus.valueOf("SUBMITTED"))
    }

    @Test
    fun personalAssignmentFilter_values_containsAllFilters() {
        // Act
        val values = PersonalAssignmentFilter.values()

        // Assert
        assertEquals(4, values.size)
        assertTrue(values.contains(PersonalAssignmentFilter.ALL))
        assertTrue(values.contains(PersonalAssignmentFilter.NOT_STARTED))
        assertTrue(values.contains(PersonalAssignmentFilter.IN_PROGRESS))
        assertTrue(values.contains(PersonalAssignmentFilter.SUBMITTED))
    }

    @Test
    fun personalAssignmentFilter_valueOf_worksCorrectly() {
        // Act & Assert
        assertEquals(PersonalAssignmentFilter.ALL, PersonalAssignmentFilter.valueOf("ALL"))
        assertEquals(PersonalAssignmentFilter.NOT_STARTED, PersonalAssignmentFilter.valueOf("NOT_STARTED"))
        assertEquals(PersonalAssignmentFilter.IN_PROGRESS, PersonalAssignmentFilter.valueOf("IN_PROGRESS"))
        assertEquals(PersonalAssignmentFilter.SUBMITTED, PersonalAssignmentFilter.valueOf("SUBMITTED"))
    }
}

