package com.example.voicetutor.data.models

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import assert

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
        assert(classData.id == 1)
        assert(classData.name == "Class1")
        assert(classData.subject.name == "Math")
        assert(classData.teacherId == 1)
        assert(classData.studentCount == 10)
    }

    @Test
    fun classData_withNullDescription_handlesNull() {
        // Arrange
        val subject = Subject(id = 1, name = "Math")
        val classData = ClassData(
            id = 1,
            name = "Class1",
            subject = subject,
            description = null,
            teacherId = 1,
            startDate = "2025-01-01",
            endDate = "2025-12-31",
            studentCount = 0,
            createdAt = "2025-01-01"
        )

        // Assert
        assert(classData.description == null)
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
            description = null,
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
        assert(enrollment.student.id == 1)
        assert(enrollment.courseClass?.id == 1)
        assert(enrollment.status == "ENROLLED")
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
        assert(enrollment.student.id == 1)
        assert(enrollment.courseClass == null)
        assert(enrollment.status == "PENDING")
    }
}

