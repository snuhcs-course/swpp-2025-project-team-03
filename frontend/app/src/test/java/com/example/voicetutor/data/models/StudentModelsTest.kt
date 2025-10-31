package com.example.voicetutor.data.models

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import assert

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
        assert(student.id == 1)
        assert(student.name == "Student1")
        assert(student.email == "s1@test.com")
        assert(student.role == UserRole.STUDENT)
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
        assert(student.name == null)
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
        assert(progress.studentId == 1)
        assert(progress.totalAssignments == 10)
        assert(progress.completedAssignments == 7)
        assert(progress.averageScore == 85.5)
        assert(progress.weeklyProgress.size == 1)
        assert(progress.subjectBreakdown.size == 1)
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
        assert(progress.weeklyProgress.isEmpty())
        assert(progress.subjectBreakdown.isEmpty())
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
        assert(weekly.week == "Week1")
        assert(weekly.assignmentsCompleted == 5)
        assert(weekly.averageScore == 85.5)
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
        assert(subjectProgress.subject == "Math")
        assert(subjectProgress.completedAssignments == 5)
        assert(subjectProgress.totalAssignments == 8)
        assert(subjectProgress.averageScore == 90.0)
    }
}

