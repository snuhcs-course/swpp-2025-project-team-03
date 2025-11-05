package com.example.voicetutor.ui.navigation

import org.junit.Assert.*
import org.junit.Test

class RecentAssignmentTest {

    @Test
    fun recentAssignment_defaultValues_areCorrect() {
        val assignment = RecentAssignment(
            id = "1",
            title = "테스트 과제"
        )
        
        assertEquals("1", assignment.id)
        assertEquals("테스트 과제", assignment.title)
        assertNull(assignment.nextQuestionId)
    }

    @Test
    fun recentAssignment_withNextQuestionId() {
        val assignment = RecentAssignment(
            id = "1",
            title = "진행 중 과제",
            nextQuestionId = 42
        )
        
        assertEquals("1", assignment.id)
        assertEquals("진행 중 과제", assignment.title)
        assertEquals(42, assignment.nextQuestionId)
    }

    @Test
    fun recentAssignment_withoutNextQuestionId() {
        val assignment = RecentAssignment(
            id = "2",
            title = "완료된 과제",
            nextQuestionId = null
        )
        
        assertEquals("2", assignment.id)
        assertEquals("완료된 과제", assignment.title)
        assertNull(assignment.nextQuestionId)
    }

    @Test
    fun recentAssignment_allProperties_set() {
        val assignment = RecentAssignment(
            id = "100",
            title = "복잡한 과제",
            nextQuestionId = 999
        )
        
        assertEquals("100", assignment.id)
        assertEquals("복잡한 과제", assignment.title)
        assertEquals(999, assignment.nextQuestionId)
    }
}

