package com.example.voicetutor.data.models

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull

@RunWith(JUnit4::class)
class ClassModelsTest {

    @Test
    fun classData_withAllFields_containsCorrectValues() {
        // Arrange
        val subject = Subject(id = 1, name = "Math", code = "MATH")
        val classData = ClassData(
            id = 1,
            name = "Class1",
            subject = subject,
            description = "Description",
            teacherId = 1,
            startDate = "2025-01-01",
            endDate = "2025-12-31",
            studentCount = 10,
            createdAt = "2025-01-01"
        )

        // Assert
        assertEquals(1, classData.id)
        assertEquals("Class1", classData.name)
        assertEquals("Math", classData.subject.name)
        assertEquals(1, classData.teacherId)
        assertEquals(10, classData.studentCount)
    }

    @Test
    fun classData_withEmptyDescription_handlesEmptyString() {
        // Arrange
        val subject = Subject(id = 1, name = "Math")
        val classData = ClassData(
            id = 1,
            name = "Class1",
            subject = subject,
            description = "",
            teacherId = 1,
            startDate = "2025-01-01",
            endDate = "2025-12-31",
            studentCount = 0,
            createdAt = "2025-01-01"
        )

        // Assert
        assertEquals("", classData.description)
    }

    @Test
    fun enrollmentData_withAllFields_containsCorrectValues() {
        // Arrange
        val student = Student(id = 1, name = "Student1", email = "s1@test.com", role = UserRole.STUDENT)
        val subject = Subject(id = 1, name = "Math")
        val courseClass = ClassData(
            id = 1,
            name = "Class1",
            subject = subject,
            description = "",
            teacherId = 1,
            startDate = "2025-01-01",
            endDate = "2025-12-31",
            studentCount = 0,
            createdAt = "2025-01-01"
        )
        val enrollment = EnrollmentData(
            student = student,
            courseClass = courseClass,
            status = "ENROLLED"
        )

        // Assert
        assertEquals(1, enrollment.student.id)
        assertEquals(1, enrollment.courseClass?.id)
        assertEquals("ENROLLED", enrollment.status)
    }

    @Test
    fun enrollmentData_withNullCourseClass_handlesNull() {
        // Arrange
        val student = Student(id = 1, name = "Student1", email = "s1@test.com", role = UserRole.STUDENT)
        val enrollment = EnrollmentData(
            student = student,
            courseClass = null,
            status = "PENDING"
        )

        // Assert
        assertEquals(1, enrollment.student.id)
        assertNull(enrollment.courseClass)
        assertEquals("PENDING", enrollment.status)
    }
}

