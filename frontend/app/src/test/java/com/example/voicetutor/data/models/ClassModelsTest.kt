package com.example.voicetutor.data.models

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertNotEquals

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

    @Test
    fun classData_actualStudentCount_withStudentCountAlt_returnsStudentCountAlt() {
        val subject = Subject(id = 1, name = "Math")
        val classData = ClassData(
            id = 1,
            name = "Class1",
            subject = subject,
            description = "Description",
            teacherId = 1,
            studentCount = 10,
            studentCountAlt = 15
        )
        
        assertEquals(15, classData.actualStudentCount)
    }

    @Test
    fun classData_actualStudentCount_withoutStudentCountAlt_returnsStudentCount() {
        val subject = Subject(id = 1, name = "Math")
        val classData = ClassData(
            id = 1,
            name = "Class1",
            subject = subject,
            description = "Description",
            teacherId = 1,
            studentCount = 10,
            studentCountAlt = null
        )
        
        assertEquals(10, classData.actualStudentCount)
    }

    @Test
    fun classData_copy_createsNewInstance() {
        val subject = Subject(id = 1, name = "Math")
        val original = ClassData(
            id = 1,
            name = "Class1",
            subject = subject,
            description = "Description",
            teacherId = 1,
            studentCount = 10
        )
        
        val copy = original.copy(name = "Class2", studentCount = 20)
        
        assertEquals("Class2", copy.name)
        assertEquals(20, copy.studentCount)
        assertEquals(original.id, copy.id)
    }

    @Test
    fun classData_equality_worksCorrectly() {
        val subject = Subject(id = 1, name = "Math")
        val class1 = ClassData(
            id = 1,
            name = "Class1",
            subject = subject,
            description = "Description",
            teacherId = 1,
            studentCount = 10
        )
        val class2 = ClassData(
            id = 1,
            name = "Class1",
            subject = subject,
            description = "Description",
            teacherId = 1,
            studentCount = 10
        )
        val class3 = ClassData(
            id = 2,
            name = "Class1",
            subject = subject,
            description = "Description",
            teacherId = 1,
            studentCount = 10
        )
        
        assertEquals(class1, class2)
        assertNotEquals(class1, class3)
    }

    @Test
    fun enrollmentData_copy_createsNewInstance() {
        val student = Student(id = 1, name = "Student1", email = "s1@test.com", role = UserRole.STUDENT)
        val original = EnrollmentData(student = student, courseClass = null, status = "PENDING")
        
        val copy = original.copy(status = "ENROLLED")
        
        assertEquals("ENROLLED", copy.status)
        assertEquals(original.student, copy.student)
    }

    @Test
    fun enrollmentData_equality_worksCorrectly() {
        val student = Student(id = 1, name = "Student1", email = "s1@test.com", role = UserRole.STUDENT)
        val enrollment1 = EnrollmentData(student = student, courseClass = null, status = "PENDING")
        val enrollment2 = EnrollmentData(student = student, courseClass = null, status = "PENDING")
        val enrollment3 = EnrollmentData(student = student, courseClass = null, status = "ENROLLED")
        
        assertEquals(enrollment1, enrollment2)
        assertNotEquals(enrollment1, enrollment3)
    }

    @Test
    fun messageData_withAllFields_containsCorrectValues() {
        val message = MessageData(
            id = 1,
            title = "Test Message",
            content = "Test Content",
            teacherId = 1,
            teacherName = "Teacher1",
            classId = 1,
            sentAt = "2025-01-01T00:00:00Z"
        )
        
        assertEquals(1, message.id)
        assertEquals("Test Message", message.title)
        assertEquals("Test Content", message.content)
        assertEquals(1, message.teacherId)
        assertEquals("Teacher1", message.teacherName)
        assertEquals(1, message.classId)
        assertEquals("2025-01-01T00:00:00Z", message.sentAt)
    }

    @Test
    fun studentClassStatistics_withAllFields_containsCorrectValues() {
        val stats = StudentClassStatistics(
            averageScore = 85.5f,
            completionRate = 0.75f
        )
        
        assertEquals(85.5f, stats.averageScore)
        assertEquals(0.75f, stats.completionRate)
    }

    @Test
    fun studentStatisticsItem_withAllFields_containsCorrectValues() {
        val item = StudentStatisticsItem(
            studentId = 1,
            averageScore = 85.5f,
            completionRate = 0.75f,
            totalAssignments = 10,
            completedAssignments = 7
        )
        
        assertEquals(1, item.studentId)
        assertEquals(85.5f, item.averageScore)
        assertEquals(0.75f, item.completionRate)
        assertEquals(10, item.totalAssignments)
        assertEquals(7, item.completedAssignments)
    }

    @Test
    fun classStudentsStatistics_withAllFields_containsCorrectValues() {
        val students = listOf(
            StudentStatisticsItem(1, 85.5f, 0.75f, 10, 7),
            StudentStatisticsItem(2, 90.0f, 0.8f, 10, 8)
        )
        val stats = ClassStudentsStatistics(
            overallCompletionRate = 0.775f,
            students = students
        )
        
        assertEquals(0.775f, stats.overallCompletionRate)
        assertEquals(2, stats.students.size)
    }

    @Test
    fun classCompletionRate_withAllFields_containsCorrectValues() {
        val rate = ClassCompletionRate(completionRate = 0.75f)
        
        assertEquals(0.75f, rate.completionRate)
    }

    @Test
    fun classInfo_withAllFields_containsCorrectValues() {
        val info = ClassInfo(id = 1, name = "Class1")
        
        assertEquals(1, info.id)
        assertEquals("Class1", info.name)
    }

    @Test
    fun classData_hashCode_worksCorrectly() {
        val subject = Subject(id = 1, name = "Math")
        val class1 = ClassData(1, "Class1", subject, "Description", 1, null, 10)
        val class2 = ClassData(1, "Class1", subject, "Description", 1, null, 10)
        
        assertEquals(class1.hashCode(), class2.hashCode())
    }

    @Test
    fun enrollmentData_hashCode_worksCorrectly() {
        val student = Student(id = 1, name = "Student1", email = "s1@test.com", role = UserRole.STUDENT)
        val enrollment1 = EnrollmentData(student, null, "PENDING")
        val enrollment2 = EnrollmentData(student, null, "PENDING")
        
        assertEquals(enrollment1.hashCode(), enrollment2.hashCode())
    }
}

