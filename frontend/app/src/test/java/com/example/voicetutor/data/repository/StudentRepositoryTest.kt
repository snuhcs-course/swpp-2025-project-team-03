package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.network.ApiResponse
import com.example.voicetutor.data.network.ApiService
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import retrofit2.Response

@RunWith(MockitoJUnitRunner::class)
class StudentRepositoryTest {

    @Mock
    lateinit var apiService: ApiService

    private fun buildStudent(id: Int = 1, name: String = "Student1") = Student(
        id = id,
        name = name,
        email = "s$id@test.com",
        role = UserRole.STUDENT,
    )

    private fun buildSubject() = Subject(id = 1, name = "Math")

    private fun buildCourseClass() = CourseClass(
        id = 1,
        name = "Class1",
        description = null,
        subject = buildSubject(),
        teacherName = "Teacher1",

        studentCount = 10,
        createdAt = "2025-01-01",
    )

    private fun buildAssignmentData(id: Int = 1) = AssignmentData(
        id = id,
        title = "Assignment $id",
        description = "Description",
        totalQuestions = 5,
        createdAt = null,
        dueAt = "2025-12-31",
        courseClass = buildCourseClass(),
        materials = null,
        grade = "1",
        personalAssignmentStatus = null,
        solvedNum = null,
        personalAssignmentId = null,
    )

    @Test
    fun getAllStudents_withTeacherId_success_returnsStudentList() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        val students = listOf(buildStudent(1), buildStudent(2))
        val apiResponse = ApiResponse(
            success = true,
            data = students,
            message = "Success",
            error = null,
        )
        whenever(apiService.getAllStudents("1", null)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getAllStudents(teacherId = "1")

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == students)
    }

    @Test
    fun getAllStudents_withClassId_success_returnsStudentList() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        val students = listOf(buildStudent(1))
        val apiResponse = ApiResponse(
            success = true,
            data = students,
            message = "Success",
            error = null,
        )
        whenever(apiService.getAllStudents(null, "10")).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getAllStudents(classId = "10")

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == students)
    }

    @Test
    fun getAllStudents_withBothFilters_success_returnsStudentList() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        val students = listOf(buildStudent(1))
        val apiResponse = ApiResponse(
            success = true,
            data = students,
            message = "Success",
            error = null,
        )
        whenever(apiService.getAllStudents("1", "10")).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getAllStudents(teacherId = "1", classId = "10")

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == students)
    }

    @Test
    fun getAllStudents_emptyList_returnsEmptyList() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        val apiResponse = ApiResponse(
            success = true,
            data = emptyList<Student>(),
            message = "Success",
            error = null,
        )
        whenever(apiService.getAllStudents(null, null)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getAllStudents()

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun getAllStudents_apiFailure_returnsFailure() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"error":"Failed"}""")
        whenever(apiService.getAllStudents(null, null)).thenReturn(Response.error(500, errorBody))

        // Act
        val result = repo.getAllStudents()

        // Assert
        assert(result.isFailure)
        // Response.error를 반환하면 response.body()가 null이므로 기본 메시지 반환
        assert(result.exceptionOrNull()?.message?.contains("Unknown error") == true)
    }

    @Test
    fun getAllStudents_networkException_returnsFailure() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        whenever(apiService.getAllStudents(null, null)).thenThrow(RuntimeException("Network error"))

        // Act
        val result = repo.getAllStudents()

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun getStudentById_success_returnsStudent() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        val student = buildStudent(1)
        val apiResponse = ApiResponse(
            success = true,
            data = student,
            message = "Success",
            error = null,
        )
        whenever(apiService.getStudentById(1)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getStudentById(1)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == student)
    }

    @Test
    fun getStudentById_noData_returnsFailure() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        val apiResponse = ApiResponse<Student>(
            success = true,
            data = null,
            message = "Success",
            error = null,
        )
        whenever(apiService.getStudentById(1)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getStudentById(1)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "No data")
    }

    @Test
    fun getStudentById_apiFailure_returnsFailure() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"error":"Not found"}""")
        whenever(apiService.getStudentById(1)).thenReturn(Response.error(404, errorBody))

        // Act
        val result = repo.getStudentById(1)

        // Assert
        assert(result.isFailure)
        // Response.error를 반환하면 response.body()가 null이므로 기본 메시지 반환
        assert(result.exceptionOrNull()?.message?.contains("Unknown error") == true)
    }

    @Test
    fun getStudentAssignments_success_returnsAssignmentList() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        val assignments = listOf(buildAssignmentData(1), buildAssignmentData(2))
        val apiResponse = ApiResponse(
            success = true,
            data = assignments,
            message = "Success",
            error = null,
        )
        whenever(apiService.getStudentAssignments(1)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getStudentAssignments(1)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == assignments)
    }

    @Test
    fun getStudentAssignments_emptyList_returnsEmptyList() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        val apiResponse = ApiResponse(
            success = true,
            data = emptyList<AssignmentData>(),
            message = "Success",
            error = null,
        )
        whenever(apiService.getStudentAssignments(1)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getStudentAssignments(1)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun getStudentAssignments_apiFailure_returnsFailure() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"error":"Failed"}""")
        whenever(apiService.getStudentAssignments(1)).thenReturn(Response.error(500, errorBody))

        // Act
        val result = repo.getStudentAssignments(1)

        // Assert
        assert(result.isFailure)
    }

    @Test
    fun getStudentProgress_success_returnsProgress() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        val progress = StudentProgress(
            studentId = 1,
            totalAssignments = 10,
            completedAssignments = 7,
            averageScore = 85.5,
            weeklyProgress = emptyList(),
            subjectBreakdown = emptyList(),
        )
        val apiResponse = ApiResponse(
            success = true,
            data = progress,
            message = "Success",
            error = null,
        )
        whenever(apiService.getStudentProgress(1)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getStudentProgress(1)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == progress)
    }

    @Test
    fun getStudentProgress_noData_returnsFailure() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        val apiResponse = ApiResponse<StudentProgress>(
            success = true,
            data = null,
            message = "Success",
            error = null,
        )
        whenever(apiService.getStudentProgress(1)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getStudentProgress(1)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "No data")
    }

    @Test
    fun getStudentProgress_apiFailure_returnsFailure() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"error":"Failed"}""")
        whenever(apiService.getStudentProgress(1)).thenReturn(Response.error(500, errorBody))

        // Act
        val result = repo.getStudentProgress(1)

        // Assert
        assert(result.isFailure)
    }

    @Test
    fun getStudentProgress_networkException_returnsFailure() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        whenever(apiService.getStudentProgress(1)).thenThrow(RuntimeException("Network error"))

        // Act
        val result = repo.getStudentProgress(1)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun getStudentClasses_success_returnsClassList() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        val classes = listOf(
            ClassInfo(id = 1, name = "Class1"),
            ClassInfo(id = 2, name = "Class2"),
        )
        val apiResponse = ApiResponse(
            success = true,
            data = classes,
            message = "Success",
            error = null,
        )
        whenever(apiService.getStudentClasses(1)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getStudentClasses(1)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == classes)
    }

    @Test
    fun getStudentClasses_emptyList_returnsEmptyList() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        val apiResponse = ApiResponse(
            success = true,
            data = emptyList<ClassInfo>(),
            message = "Success",
            error = null,
        )
        whenever(apiService.getStudentClasses(1)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getStudentClasses(1)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun getStudentClasses_apiFailure_returnsFailure() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"error":"Failed"}""")
        whenever(apiService.getStudentClasses(1)).thenReturn(Response.error(500, errorBody))

        // Act
        val result = repo.getStudentClasses(1)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message?.contains("Unknown error") == true)
    }

    @Test
    fun getStudentClasses_networkException_returnsFailure() = runTest {
        // Arrange
        val repo = StudentRepository(apiService)
        whenever(apiService.getStudentClasses(1)).thenThrow(RuntimeException("Network error"))

        // Act
        val result = repo.getStudentClasses(1)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "Network error")
    }
}
