package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.LoginRequest
import com.example.voicetutor.data.models.LoginResponse
import com.example.voicetutor.data.models.SignupRequest
import com.example.voicetutor.data.models.User
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.data.network.ApiService
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    open suspend fun login(email: String, password: String): Result<User> {
        return try {
            val request = LoginRequest(email, password)
            val response = apiService.login(request)
            val responseBody = response.body()

            println("AuthRepository - Login response code: ${response.code()}")
            println("AuthRepository - Login response success: ${responseBody?.success}")

            if (response.isSuccessful && responseBody?.success == true) {
                val user = responseBody.user  // 'data' 필드가 'user'로 매핑됨
                println("AuthRepository - User: ${user?.email}")
                println("AuthRepository - User.assignments: ${user?.assignments?.size}")
                user?.assignments?.forEach { 
                    println("  - ${it.title}")
                }
                
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(LoginException.Unknown("로그인에 실패했습니다 - 사용자 정보를 받을 수 없습니다"))
                }
            } else {
                val statusCode = response.code()
                val rawMessage = responseBody?.error
                    ?: responseBody?.message
                    ?: parseErrorMessage(response)
                    ?: "로그인에 실패했습니다"
                println("AuthRepository - Login failed: $rawMessage (status: $statusCode)")

                val normalized = rawMessage.lowercase()
                val exception = when {
                    statusCode == 401 || statusCode == 400 && normalized.contains("credential") || normalized.contains("password") || normalized.contains("비밀번호") -> {
                        LoginException.InvalidCredentials("이메일 또는 비밀번호가 올바르지 않습니다. 다시 확인해주세요.")
                    }
                    statusCode == 404 || normalized.contains("not found") || normalized.contains("존재하지") || normalized.contains("등록되지") -> {
                        LoginException.AccountNotFound("해당 이메일로 등록된 계정을 찾을 수 없습니다. 회원가입을 진행하거나 이메일을 다시 확인해주세요.")
                    }
                    statusCode == 423 || normalized.contains("locked") || normalized.contains("suspended") || normalized.contains("잠금") -> {
                        LoginException.AccountLocked("보안상의 이유로 계정이 잠겨 있습니다. 관리자에게 문의해주세요.")
                    }
                    statusCode in 500..599 -> {
                        LoginException.Server("서버에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                    }
                    else -> {
                        LoginException.Unknown(rawMessage.ifBlank { "로그인에 실패했습니다" })
                    }
                }
                Result.failure(exception)
            }
        } catch (e: Exception) {
            println("AuthRepository - Login error: ${e.message}")
            e.printStackTrace()
            val exception = when (e) {
                is LoginException -> e
                is IOException -> LoginException.Network("네트워크 연결을 확인하고 다시 시도해주세요.", e)
                else -> LoginException.Unknown(e.message ?: "로그인 중 알 수 없는 오류가 발생했습니다")
            }
            Result.failure(exception)
        }
    }
    
    open suspend fun signup(name: String, email: String, password: String, role: UserRole): Result<User> {
        return try {
            val signupRequest = SignupRequest(
                name = name,
                email = email,
                password = password,
                role = role.name // UserRole enum을 String으로 변환
            )
            val response = apiService.signup(signupRequest)
            
            println("AuthRepository - Signup response code: ${response.code()}")
            println("AuthRepository - Signup response success: ${response.body()?.success}")
            println("AuthRepository - Signup response body: ${response.body()}")
            
            val responseBody = response.body()
            if (response.isSuccessful && responseBody?.success == true) {
                val user = responseBody.user
                println("AuthRepository - Signup User parsed: ${user?.email}, id: ${user?.id}, role: ${user?.role}")
                
                if (user != null) {
                    Result.success(user)
                } else {
                    println("AuthRepository - Signup User is null!")
                    Result.failure(SignupException.Unknown("회원가입에 실패했습니다 - 사용자 정보를 받을 수 없습니다"))
                }
            } else {
                val statusCode = response.code()
                val errorMsg = responseBody?.error
                    ?: responseBody?.message
                    ?: parseErrorMessage(response)
                    ?: "회원가입에 실패했습니다"
                println("AuthRepository - Signup failed: $errorMsg (status: $statusCode)")
                
                val exception = when {
                    statusCode == 409 -> SignupException.DuplicateEmail(errorMsg)
                    statusCode == 400 && errorMsg.contains("이미 사용 중") -> SignupException.DuplicateEmail(errorMsg)
                    statusCode in 500..599 -> SignupException.Server(errorMsg.ifBlank { "서버에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요." })
                    else -> SignupException.Unknown(errorMsg)
                }
                Result.failure(exception)
            }
        } catch (e: Exception) {
            println("AuthRepository - Signup error: ${e.message}")
            e.printStackTrace()
            val exception = when (e) {
                is SignupException -> e
                is IOException -> SignupException.Network("네트워크 연결을 확인하고 다시 시도해주세요.", e)
                else -> SignupException.Unknown(e.message ?: "회원가입 중 알 수 없는 오류가 발생했습니다")
            }
            Result.failure(exception)
        }
    }

    private fun parseErrorMessage(response: retrofit2.Response<*>): String? {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody.isNullOrBlank()) {
                response.message().takeUnless { it.isNullOrBlank() }
            } else {
                val json = org.json.JSONObject(errorBody)
                when {
                    json.has("error") && !json.isNull("error") -> json.getString("error")
                    json.has("message") && !json.isNull("message") -> json.getString("message")
                    else -> null
                }
            }
        } catch (e: Exception) {
            println("AuthRepository - parseErrorMessage error: ${e.message}")
            null
        }
    }
    
}
