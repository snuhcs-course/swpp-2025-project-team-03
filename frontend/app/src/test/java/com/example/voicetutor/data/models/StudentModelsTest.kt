package com.example.voicetutor.data.models

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

@RunWith(JUnit4::class)
class StudentModelsTest {

    @Test
    fun student_withAllFields_containsCorrectValues() {
        // Arrange
        val student = Student(
            id = 1,
            name = "Student1",
            email = "s1@test.com",
            role = UserRole.STUDENT
        )

        // Assert
        assertEquals(1, student.id)
        assertEquals("Student1", student.name)
        assertEquals("s1@test.com", student.email)
        assertEquals(UserRole.STUDENT, student.role)
    }

    @Test
    fun student_withNullName_handlesNull() {
        // Arrange
        val student = Student(
            id = 1,
            name = null,
            email = "s1@test.com",
            role = UserRole.STUDENT
        )

        // Assert
        assertNull(student.name)
    }

    @Test
    fun allStudentsStudent_withAllFields_containsCorrectValues() {
        // Arrange
        val student = AllStudentsStudent(
            id = 1,
            name = "Student1",
            email = "s1@test.com",
            role = UserRole.STUDENT
        )

        // Assert
        assert(student.id == 1)
        assert(student.name == "Student1")
        assert(student.email == "s1@test.com")
        assert(student.role == UserRole.STUDENT)
    }

    @Test
    fun studentProgress_withAllFields_containsCorrectValues() {
        // Arrange
        val progress = StudentProgress(
            studentId = 1,
            totalAssignments = 10,
            completedAssignments = 7,
            averageScore = 85.5,
            weeklyProgress = listOf(
                WeeklyProgress(week = "Week1", assignmentsCompleted = 2, averageScore = 80.0)
            ),
            subjectBreakdown = listOf(
                SubjectProgress(
                    subject = "Math",
                    completedAssignments = 5,
                    totalAssignments = 8,
                    averageScore = 90.0
                )
            )
        )

        // Assert
        assertEquals(1, progress.studentId)
        assertEquals(10, progress.totalAssignments)
        assertEquals(7, progress.completedAssignments)
        assertEquals(85.5, progress.averageScore, 0.01)
        assertEquals(1, progress.weeklyProgress.size)
        assertEquals(1, progress.subjectBreakdown.size)
    }

    @Test
    fun studentProgress_withEmptyLists_handlesEmptyLists() {
        // Arrange
        val progress = StudentProgress(
            studentId = 1,
            totalAssignments = 0,
            completedAssignments = 0,
            averageScore = 0.0,
            weeklyProgress = emptyList(),
            subjectBreakdown = emptyList()
        )

        // Assert
        assertTrue(progress.weeklyProgress.isEmpty())
        assertTrue(progress.subjectBreakdown.isEmpty())
    }

    @Test
    fun weeklyProgress_withAllFields_containsCorrectValues() {
        // Arrange
        val weekly = WeeklyProgress(
            week = "Week1",
            assignmentsCompleted = 5,
            averageScore = 85.5
        )

        // Assert
        assertEquals("Week1", weekly.week)
        assertEquals(5, weekly.assignmentsCompleted)
        assertEquals(85.5, weekly.averageScore, 0.01)
    }

    @Test
    fun subjectProgress_withAllFields_containsCorrectValues() {
        // Arrange
        val subjectProgress = SubjectProgress(
            subject = "Math",
            completedAssignments = 5,
            totalAssignments = 8,
            averageScore = 90.0
        )

        // Assert
        assertEquals("Math", subjectProgress.subject)
        assertEquals(5, subjectProgress.completedAssignments)
        assertEquals(8, subjectProgress.totalAssignments)
        assertEquals(90.0, subjectProgress.averageScore, 0.01)
    }
}

