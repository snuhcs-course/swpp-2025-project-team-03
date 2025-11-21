package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.*
import com.example.voicetutor.data.network.ApiResponse
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.CreateClassRequest
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
class ClassRepositoryTest {

    @Mock
    lateinit var apiService: ApiService

    private fun buildSubject(id: Int = 1, name: String = "Math") = Subject(id = id, name = name)

    private fun buildClassData(id: Int = 1, name: String = "Class1") = ClassData(
        id = id,
        name = name,
        subject = buildSubject(),
        description = "Description",
        teacherId = 1,

        studentCount = 10,
        createdAt = "2025-01-01",
    )

    @Test
    fun getClasses_success_returnsClassList() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val classes = listOf(buildClassData(1), buildClassData(2))
        val apiResponse = ApiResponse(
            success = true,
            data = classes,
            message = "Success",
            error = null,
        )
        whenever(apiService.getClasses("1")).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getClasses("1")

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == classes)
    }

    @Test
    fun getClasses_emptyList_returnsEmptyList() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val apiResponse = ApiResponse(
            success = true,
            data = emptyList<ClassData>(),
            message = "Success",
            error = null,
        )
        whenever(apiService.getClasses("1")).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getClasses("1")

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun getClasses_apiFailure_returnsFailure() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"error":"Failed"}""")
        whenever(apiService.getClasses("1")).thenReturn(Response.error(500, errorBody))

        // Act
        val result = repo.getClasses("1")

        // Assert
        assert(result.isFailure)
        // Response.error를 반환하면 response.body()가 null이므로 기본 메시지 반환
        assert(result.exceptionOrNull()?.message?.contains("Unknown error") == true)
    }

    @Test
    fun getClasses_networkException_returnsFailure() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        whenever(apiService.getClasses("1")).thenThrow(RuntimeException("Network error"))

        // Act
        val result = repo.getClasses("1")

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun getClassById_success_returnsClass() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val classData = buildClassData(1)
        val apiResponse = ApiResponse(
            success = true,
            data = classData,
            message = "Success",
            error = null,
        )
        whenever(apiService.getClassById(1)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getClassById(1)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == classData)
    }

    @Test
    fun getClassById_noData_returnsFailure() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val apiResponse = ApiResponse<ClassData>(
            success = true,
            data = null,
            message = "Success",
            error = null,
        )
        whenever(apiService.getClassById(1)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getClassById(1)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "No data")
    }

    @Test
    fun getClassById_apiFailure_returnsFailure() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"error":"Not found"}""")
        whenever(apiService.getClassById(1)).thenReturn(Response.error(404, errorBody))

        // Act
        val result = repo.getClassById(1)

        // Assert
        assert(result.isFailure)
        // Response.error를 반환하면 response.body()가 null이므로 기본 메시지 반환
        assert(result.exceptionOrNull()?.message?.contains("Unknown error") == true)
    }

    @Test
    fun getClassStudents_success_returnsStudentList() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val students = listOf(
            Student(id = 1, name = "Student1", email = "s1@test.com", role = UserRole.STUDENT),
            Student(id = 2, name = "Student2", email = "s2@test.com", role = UserRole.STUDENT),
        )
        val apiResponse = ApiResponse(
            success = true,
            data = students,
            message = "Success",
            error = null,
        )
        whenever(apiService.getClassStudents(1)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getClassStudents(1)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == students)
    }

    @Test
    fun getClassStudents_emptyList_returnsEmptyList() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val apiResponse = ApiResponse(
            success = true,
            data = emptyList<Student>(),
            message = "Success",
            error = null,
        )
        whenever(apiService.getClassStudents(1)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getClassStudents(1)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun getClassStudents_apiFailure_returnsFailure() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"error":"Failed"}""")
        whenever(apiService.getClassStudents(1)).thenReturn(Response.error(500, errorBody))

        // Act
        val result = repo.getClassStudents(1)

        // Assert
        assert(result.isFailure)
    }

    @Test
    fun createClass_success_returnsClass() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val request = CreateClassRequest(
            name = "New Class",
            description = "Description",
            subject_name = "Math",
            teacher_id = 1,
        )
        val classData = buildClassData(1, "New Class")
        val apiResponse = ApiResponse(
            success = true,
            data = classData,
            message = "Success",
            error = null,
        )
        whenever(apiService.createClass(request)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.createClass(request)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == classData)
    }

    @Test
    fun createClass_noData_returnsFailure() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val request = CreateClassRequest(
            name = "New Class",
            description = null,
            subject_name = "Math",
            teacher_id = 1,

        )
        val apiResponse = ApiResponse<ClassData>(
            success = true,
            data = null,
            message = "Success",
            error = null,
        )
        whenever(apiService.createClass(request)).thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.createClass(request)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "No data")
    }

    @Test
    fun createClass_apiFailure_returnsFailure() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val request = CreateClassRequest(
            name = "New Class",
            description = null,
            subject_name = "Math",
            teacher_id = 1,

        )
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"error":"Creation failed"}""")
        whenever(apiService.createClass(request)).thenReturn(Response.error(400, errorBody))

        // Act
        val result = repo.createClass(request)

        // Assert
        assert(result.isFailure)
        // Response.error를 반환하면 response.body()가 null이므로 기본 메시지 반환
        assert(result.exceptionOrNull()?.message?.contains("Unknown error") == true)
    }

    @Test
    fun enrollStudentToClass_withStudentId_success_returnsEnrollment() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val student = Student(id = 1, name = "Student1", email = "s1@test.com", role = UserRole.STUDENT)
        val classData = buildClassData(1)
        val enrollment = EnrollmentData(
            student = student,
            courseClass = classData,
            status = "enrolled",
        )
        val apiResponse = ApiResponse(
            success = true,
            data = enrollment,
            message = "Success",
            error = null,
        )
        whenever(apiService.enrollStudentToClass(1, studentId = 1))
            .thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.enrollStudentToClass(1, studentId = 1)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == enrollment)
    }

    @Test
    fun enrollStudentToClass_noData_returnsFailure() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val apiResponse = ApiResponse<EnrollmentData>(
            success = true,
            data = null,
            message = "Success",
            error = null,
        )
        whenever(apiService.enrollStudentToClass(1, studentId = 1))
            .thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.enrollStudentToClass(1, studentId = 1)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "No data")
    }

    @Test
    fun enrollStudentToClass_apiFailure_returnsFailure() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"error":"Enrollment failed"}""")
        whenever(apiService.enrollStudentToClass(1, studentId = 1))
            .thenReturn(Response.error(400, errorBody))

        // Act
        val result = repo.enrollStudentToClass(1, studentId = 1)

        // Assert
        assert(result.isFailure)
        // Response.error를 반환하면 response.body()가 null이므로 기본 메시지 반환
        assert(result.exceptionOrNull()?.message?.contains("Unknown error") == true)
    }

    @Test
    fun enrollStudentToClass_networkException_returnsFailure() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        whenever(apiService.enrollStudentToClass(1, studentId = 1))
            .thenThrow(RuntimeException("Network error"))

        // Act
        val result = repo.enrollStudentToClass(1, studentId = 1)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun removeStudentFromClass_success_returnsUnit() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val apiResponse = ApiResponse(
            success = true,
            data = Unit,
            message = "Success",
            error = null,
        )
        whenever(apiService.removeStudentFromClass(1, 1))
            .thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.removeStudentFromClass(1, 1)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == Unit)
    }

    @Test
    fun removeStudentFromClass_apiFailure_returnsFailure() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"error":"Failed to remove"}""")
        whenever(apiService.removeStudentFromClass(1, 1))
            .thenReturn(Response.error(500, errorBody))

        // Act
        val result = repo.removeStudentFromClass(1, 1)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message?.contains("Unknown error") == true)
    }

    @Test
    fun removeStudentFromClass_networkException_returnsFailure() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        whenever(apiService.removeStudentFromClass(1, 1))
            .thenThrow(RuntimeException("Network error"))

        // Act
        val result = repo.removeStudentFromClass(1, 1)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun removeClassById_success_returnsUnit() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val apiResponse = ApiResponse(
            success = true,
            data = Unit,
            message = "Success",
            error = null,
        )
        whenever(apiService.removeClassById(1))
            .thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.removeClassById(1)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == Unit)
    }

    @Test
    fun removeClassById_apiFailure_returnsFailure() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"error":"Failed to remove class"}""")
        whenever(apiService.removeClassById(1))
            .thenReturn(Response.error(500, errorBody))

        // Act
        val result = repo.removeClassById(1)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message?.contains("Unknown error") == true)
    }

    @Test
    fun removeClassById_networkException_returnsFailure() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        whenever(apiService.removeClassById(1))
            .thenThrow(RuntimeException("Network error"))

        // Act
        val result = repo.removeClassById(1)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "Network error")
    }

    @Test
    fun getClassStudentsStatistics_success_returnsStatistics() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val statistics = ClassStudentsStatistics(
            overallCompletionRate = 0.75f,
            students = listOf(
                StudentStatisticsItem(
                    studentId = 1,
                    averageScore = 88.0f,
                    completionRate = 0.9f,
                    totalAssignments = 10,
                    completedAssignments = 9,
                ),
            ),
        )
        val apiResponse = ApiResponse(
            success = true,
            data = statistics,
            message = "Success",
            error = null,
        )
        whenever(apiService.getClassStudentsStatistics(1))
            .thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getClassStudentsStatistics(1)

        // Assert
        assert(result.isSuccess)
        assert(result.getOrNull() == statistics)
    }

    @Test
    fun getClassStudentsStatistics_noData_returnsFailure() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val apiResponse = ApiResponse<ClassStudentsStatistics>(
            success = true,
            data = null,
            message = "Success",
            error = null,
        )
        whenever(apiService.getClassStudentsStatistics(1))
            .thenReturn(Response.success(apiResponse))

        // Act
        val result = repo.getClassStudentsStatistics(1)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "No data")
    }

    @Test
    fun getClassStudentsStatistics_apiFailure_returnsFailure() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{"error":"Failed"}""")
        whenever(apiService.getClassStudentsStatistics(1))
            .thenReturn(Response.error(500, errorBody))

        // Act
        val result = repo.getClassStudentsStatistics(1)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message?.contains("Unknown error") == true)
    }

    @Test
    fun getClassStudentsStatistics_networkException_returnsFailure() = runTest {
        // Arrange
        val repo = ClassRepository(apiService)
        whenever(apiService.getClassStudentsStatistics(1))
            .thenThrow(RuntimeException("Network error"))

        // Act
        val result = repo.getClassStudentsStatistics(1)

        // Assert
        assert(result.isFailure)
        assert(result.exceptionOrNull()?.message == "Network error")
    }
}
