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
    }

    @Test
    fun recentAssignment_allProperties_set() {
        val assignment = RecentAssignment(
            id = "100",
            title = "복잡한 과제"
        )
        
        assertEquals("100", assignment.id)
        assertEquals("복잡한 과제", assignment.title)
    }
}

