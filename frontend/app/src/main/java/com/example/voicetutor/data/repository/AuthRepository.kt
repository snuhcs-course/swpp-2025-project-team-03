package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.LoginRequest
import com.example.voicetutor.data.models.LoginResponse
import com.example.voicetutor.data.models.SignupRequest
import com.example.voicetutor.data.models.User
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.data.network.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            val request = LoginRequest(email, password)
            val response = apiService.login(request)
            
            println("AuthRepository - Login response code: ${response.code()}")
            println("AuthRepository - Login response success: ${response.body()?.success}")
            
            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()?.user  // 'data' 필드가 'user'로 매핑됨
                println("AuthRepository - User: ${user?.email}")
                println("AuthRepository - User.assignments: ${user?.assignments?.size}")
                user?.assignments?.forEach { 
                    println("  - ${it.title}")
                }
                
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("사용자 정보를 찾을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.error ?: "로그인에 실패했습니다"))
            }
        } catch (e: Exception) {
            println("AuthRepository - Login error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    suspend fun signup(name: String, email: String, password: String, role: UserRole): Result<User> {
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
            
            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()?.user
                println("AuthRepository - Signup User parsed: ${user?.email}, id: ${user?.id}, role: ${user?.role}")
                
                if (user != null) {
                    Result.success(user)
                } else {
                    println("AuthRepository - Signup User is null!")
                    Result.failure(Exception("회원가입에 실패했습니다 - 사용자 정보를 받을 수 없습니다"))
                }
            } else {
                val errorMsg = response.body()?.error ?: "회원가입에 실패했습니다"
                println("AuthRepository - Signup failed: $errorMsg")
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            println("AuthRepository - Signup error: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
}
