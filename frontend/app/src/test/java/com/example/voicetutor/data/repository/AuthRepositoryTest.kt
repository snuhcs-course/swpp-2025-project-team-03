package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.LoginRequest
import com.example.voicetutor.data.models.SignupRequest
import com.example.voicetutor.data.models.UserRole
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
import com.example.voicetutor.data.models.LoginResponse
import com.example.voicetutor.data.models.User

@RunWith(MockitoJUnitRunner::class)
class AuthRepositoryTest {

    @Mock
    lateinit var apiService: ApiService

    @Test
    fun login_failure_propagatesErrorMessage() = runTest {
        // Given
        val repo = AuthRepository(apiService)
        val req = LoginRequest(email = "a@ex.com", password = "pw")
        val json = """{"success":false,"error":"로그인에 실패했습니다"}"""
        val errorBody = ResponseBody.create("application/json".toMediaType(), json)
        whenever(apiService.login(req)).thenReturn(Response.error(401, errorBody))

        // When
        val r = repo.login("a@ex.com", "pw")

        // Then
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message?.contains("로그인") == true)
    }

    @Test
    fun signup_failure_propagatesErrorMessage() = runTest {
        // Given
        val repo = AuthRepository(apiService)
        val req = SignupRequest(name = "n", email = "a@ex.com", password = "pw", role = UserRole.STUDENT.name)
        val json = """{"success":false,"error":"회원가입에 실패했습니다"}"""
        val errorBody = ResponseBody.create("application/json".toMediaType(), json)
        whenever(apiService.signup(req)).thenReturn(Response.error(400, errorBody))

        // When
        val r = repo.signup("n", "a@ex.com", "pw", UserRole.STUDENT)

        // Then
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message?.contains("회원가입") == true)
    }

    @Test
    fun login_success_returnsUser() = runTest {
        val repo = AuthRepository(apiService)
        val user = User(id = 1, name = "Alice", email = "a@ex.com", role = UserRole.STUDENT)
        val resp = LoginResponse(success = true, user = user, token = "t", message = null, error = null)
        whenever(apiService.login(LoginRequest("a@ex.com", "pw"))).thenReturn(Response.success(resp))
        val r = repo.login("a@ex.com", "pw")
        assert(r.isSuccess)
        assert(r.getOrNull()?.email == "a@ex.com")
    }

    @Test
    fun signup_success_returnsUser() = runTest {
        val repo = AuthRepository(apiService)
        val user = User(id = 2, name = "Bob", email = "b@ex.com", role = UserRole.TEACHER)
        val resp = LoginResponse(success = true, user = user, token = "t2", message = null, error = null)
        val req = SignupRequest(name = "Bob", email = "b@ex.com", password = "pw", role = UserRole.TEACHER.name)
        whenever(apiService.signup(req)).thenReturn(Response.success(resp))
        val r = repo.signup("Bob", "b@ex.com", "pw", UserRole.TEACHER)
        assert(r.isSuccess)
        assert(r.getOrNull()?.role == UserRole.TEACHER)
    }

    @Test
    fun login_successButNoUser_returnsFailure() = runTest {
        // Arrange
        val repo = AuthRepository(apiService)
        val resp = LoginResponse(success = true, user = null, token = null, message = null, error = null)
        whenever(apiService.login(LoginRequest("a@ex.com", "pw"))).thenReturn(Response.success(resp))

        // Act
        val r = repo.login("a@ex.com", "pw")

        // Assert
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message?.contains("사용자 정보를 찾을 수 없습니다") == true)
    }

    @Test
    fun login_apiFailure_noErrorBody_returnsDefaultMessage() = runTest {
        // Arrange
        val repo = AuthRepository(apiService)
        val errorBody = ResponseBody.create("application/json".toMediaType(), """{}""")
        whenever(apiService.login(LoginRequest("a@ex.com", "pw"))).thenReturn(Response.error(500, errorBody))

        // Act
        val r = repo.login("a@ex.com", "pw")

        // Assert
        assert(r.isFailure)
        // response.body()가 null이므로 기본 메시지 반환
        assert(r.exceptionOrNull()?.message?.contains("로그인에 실패했습니다") == true)
    }

    @Test
    fun signup_successButNoUser_returnsFailure() = runTest {
        // Arrange
        val repo = AuthRepository(apiService)
        val resp = LoginResponse(success = true, user = null, token = null, message = null, error = null)
        val req = SignupRequest(name = "Bob", email = "b@ex.com", password = "pw", role = UserRole.TEACHER.name)
        whenever(apiService.signup(req)).thenReturn(Response.success(resp))

        // Act
        val r = repo.signup("Bob", "b@ex.com", "pw", UserRole.TEACHER)

        // Assert
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message?.contains("회원가입에 실패했습니다") == true)
    }

    @Test
    fun login_networkException_returnsFailure() = runTest {
        // Arrange
        val repo = AuthRepository(apiService)
        org.mockito.kotlin.doAnswer { throw java.net.UnknownHostException("Network error") }
            .whenever(apiService).login(org.mockito.kotlin.any())

        // Act
        val r = repo.login("a@ex.com", "pw")

        // Assert
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message?.contains("Network error") == true)
    }

    @Test
    fun signup_networkException_returnsFailure() = runTest {
        // Arrange
        val repo = AuthRepository(apiService)
        org.mockito.kotlin.doAnswer { throw java.net.UnknownHostException("Network error") }
            .whenever(apiService).signup(org.mockito.kotlin.any())

        // Act
        val r = repo.signup("Bob", "b@ex.com", "pw", UserRole.TEACHER)

        // Assert
        assert(r.isFailure)
        assert(r.exceptionOrNull()?.message?.contains("Network error") == true)
    }
}


