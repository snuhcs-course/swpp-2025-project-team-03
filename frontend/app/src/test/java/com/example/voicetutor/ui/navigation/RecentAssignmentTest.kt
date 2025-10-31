package com.example.voicetutor.ui.navigation

import org.junit.Assert.*
import org.junit.Test

class RecentAssignmentTest {

    @Test
    fun recentAssignment_defaultValues_areCorrect() {
        val assignment = RecentAssignment(
            id = "1",
            title = "테스트 과제",
            subject = "수학",
            progress = 0.5f,
            lastActivity = "2025-01-01",
            isUrgent = false
        )
        
        assertEquals("1", assignment.id)
        assertEquals("테스트 과제", assignment.title)
        assertEquals("수학", assignment.subject)
        assertEquals(0.5f, assignment.progress)
        assertEquals("2025-01-01", assignment.lastActivity)
        assertFalse(assignment.isUrgent)
    }

    @Test
    fun recentAssignment_urgentFlag_isTrue() {
        val assignment = RecentAssignment(
            id = "1",
            title = "긴급 과제",
            subject = "수학",
            progress = 0.3f,
            lastActivity = "2025-01-01",
            isUrgent = true
        )
        
        assertTrue(assignment.isUrgent)
    }

    @Test
    fun recentAssignment_progress_handlesFullProgress() {
        val assignment = RecentAssignment(
            id = "1",
            title = "완료 과제",
            subject = "수학",
            progress = 1.0f,
            lastActivity = "2025-01-01",
            isUrgent = false
        )
        
        assertEquals(1.0f, assignment.progress)
    }

    @Test
    fun recentAssignment_progress_handlesZeroProgress() {
        val assignment = RecentAssignment(
            id = "1",
            title = "시작 안 함",
            subject = "수학",
            progress = 0.0f,
            lastActivity = "2025-01-01",
            isUrgent = false
        )
        
        assertEquals(0.0f, assignment.progress)
    }

    @Test
    fun recentAssignment_allProperties_set() {
        val assignment = RecentAssignment(
            id = "100",
            title = "복잡한 과제",
            subject = "과학",
            progress = 0.75f,
            lastActivity = "2025-12-31",
            isUrgent = true
        )
        
        assertEquals("100", assignment.id)
        assertEquals("복잡한 과제", assignment.title)
        assertEquals("과학", assignment.subject)
        assertEquals(0.75f, assignment.progress)
        assertEquals("2025-12-31", assignment.lastActivity)
        assertTrue(assignment.isUrgent)
    }
}

