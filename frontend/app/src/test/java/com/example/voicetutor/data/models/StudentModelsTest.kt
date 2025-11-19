package com.example.voicetutor.data.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class StudentModelsTest {

    @Test
    fun student_withAllFields_containsCorrectValues() {
        // Arrange
        val student = Student(
            id = 1,
            name = "Student1",
            email = "s1@test.com",
            role = UserRole.STUDENT,
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
            role = UserRole.STUDENT,
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
            role = UserRole.STUDENT,
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
                WeeklyProgress(week = "Week1", assignmentsCompleted = 2, averageScore = 80.0),
            ),
            subjectBreakdown = listOf(
                SubjectProgress(
                    subject = "Math",
                    completedAssignments = 5,
                    totalAssignments = 8,
                    averageScore = 90.0,
                ),
            ),
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
            subjectBreakdown = emptyList(),
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
            averageScore = 85.5,
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
            averageScore = 90.0,
        )

        // Assert
        assertEquals("Math", subjectProgress.subject)
        assertEquals(5, subjectProgress.completedAssignments)
        assertEquals(8, subjectProgress.totalAssignments)
        assertEquals(90.0, subjectProgress.averageScore, 0.01)
    }

    @Test
    fun student_copy_createsNewInstance() {
        val original = Student(1, "Student1", "s1@test.com", UserRole.STUDENT)
        val copy = original.copy(name = "Student2")

        assertEquals("Student2", copy.name)
        assertEquals(original.id, copy.id)
        assertEquals(original.email, copy.email)
    }

    @Test
    fun student_equality_worksCorrectly() {
        val student1 = Student(1, "Student1", "s1@test.com", UserRole.STUDENT)
        val student2 = Student(1, "Student1", "s1@test.com", UserRole.STUDENT)
        val student3 = Student(2, "Student1", "s1@test.com", UserRole.STUDENT)

        assertEquals(student1, student2)
        assertNotEquals(student1, student3)
    }

    @Test
    fun allStudentsStudent_copy_createsNewInstance() {
        val original = AllStudentsStudent(1, "Student1", "s1@test.com", UserRole.STUDENT)
        val copy = original.copy(name = "Student2")

        assertEquals("Student2", copy.name)
        assertEquals(original.id, copy.id)
    }

    @Test
    fun allStudentsStudent_equality_worksCorrectly() {
        val student1 = AllStudentsStudent(1, "Student1", "s1@test.com", UserRole.STUDENT)
        val student2 = AllStudentsStudent(1, "Student1", "s1@test.com", UserRole.STUDENT)
        val student3 = AllStudentsStudent(2, "Student1", "s1@test.com", UserRole.STUDENT)

        assertEquals(student1, student2)
        assertNotEquals(student1, student3)
    }

    @Test
    fun studentProgress_copy_createsNewInstance() {
        val original = StudentProgress(
            1,
            10,
            7,
            85.5,
            emptyList(),
            emptyList(),
        )
        val copy = original.copy(completedAssignments = 8)

        assertEquals(8, copy.completedAssignments)
        assertEquals(original.studentId, copy.studentId)
    }

    @Test
    fun studentProgress_equality_worksCorrectly() {
        val progress1 = StudentProgress(1, 10, 7, 85.5, emptyList(), emptyList())
        val progress2 = StudentProgress(1, 10, 7, 85.5, emptyList(), emptyList())
        val progress3 = StudentProgress(2, 10, 7, 85.5, emptyList(), emptyList())

        assertEquals(progress1, progress2)
        assertNotEquals(progress1, progress3)
    }

    @Test
    fun weeklyProgress_copy_createsNewInstance() {
        val original = WeeklyProgress("Week1", 5, 85.5)
        val copy = original.copy(week = "Week2")

        assertEquals("Week2", copy.week)
        assertEquals(original.assignmentsCompleted, copy.assignmentsCompleted)
    }

    @Test
    fun weeklyProgress_equality_worksCorrectly() {
        val weekly1 = WeeklyProgress("Week1", 5, 85.5)
        val weekly2 = WeeklyProgress("Week1", 5, 85.5)
        val weekly3 = WeeklyProgress("Week2", 5, 85.5)

        assertEquals(weekly1, weekly2)
        assertNotEquals(weekly1, weekly3)
    }

    @Test
    fun subjectProgress_copy_createsNewInstance() {
        val original = SubjectProgress("Math", 5, 8, 90.0)
        val copy = original.copy(subject = "Science")

        assertEquals("Science", copy.subject)
        assertEquals(original.completedAssignments, copy.completedAssignments)
    }

    @Test
    fun subjectProgress_equality_worksCorrectly() {
        val progress1 = SubjectProgress("Math", 5, 8, 90.0)
        val progress2 = SubjectProgress("Math", 5, 8, 90.0)
        val progress3 = SubjectProgress("Science", 5, 8, 90.0)

        assertEquals(progress1, progress2)
        assertNotEquals(progress1, progress3)
    }

    @Test
    fun classMessageStudent_withAllFields_containsCorrectValues() {
        val message = ClassMessageStudent(
            id = 1,
            studentId = 1,
            teacherId = 1,
            content = "Test Content",
        )

        assertEquals(1, message.id)
        assertEquals(1, message.studentId)
        assertEquals(1, message.teacherId)
        assertEquals("Test Content", message.content)
    }

    @Test
    fun classMessageStudent_copy_createsNewInstance() {
        val original = ClassMessageStudent(1, 1, 1, "Content")
        val copy = original.copy(content = "New Content")

        assertEquals("New Content", copy.content)
        assertEquals(original.id, copy.id)
    }

    @Test
    fun classMessageStudent_equality_worksCorrectly() {
        val message1 = ClassMessageStudent(1, 1, 1, "Content")
        val message2 = ClassMessageStudent(1, 1, 1, "Content")
        val message3 = ClassMessageStudent(2, 1, 1, "Content")

        assertEquals(message1, message2)
        assertNotEquals(message1, message3)
    }

    @Test
    fun student_hashCode_worksCorrectly() {
        val student1 = Student(1, "Student1", "s1@test.com", UserRole.STUDENT)
        val student2 = Student(1, "Student1", "s1@test.com", UserRole.STUDENT)

        assertEquals(student1.hashCode(), student2.hashCode())
    }

    @Test
    fun studentProgress_hashCode_worksCorrectly() {
        val progress1 = StudentProgress(1, 10, 7, 85.5, emptyList(), emptyList())
        val progress2 = StudentProgress(1, 10, 7, 85.5, emptyList(), emptyList())

        assertEquals(progress1.hashCode(), progress2.hashCode())
    }
}
