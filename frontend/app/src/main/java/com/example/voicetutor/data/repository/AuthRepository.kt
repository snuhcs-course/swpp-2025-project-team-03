package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.LoginRequest
import com.example.voicetutor.data.models.LoginResponse
import com.example.voicetutor.data.models.SignupRequest
import com.example.voicetutor.data.models.User
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
            
            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()?.user
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("사용자 정보를 찾을 수 없습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.error ?: "로그인에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun signup(signupRequest: SignupRequest): Result<User> {
        return try {
            val response = apiService.signup(signupRequest)
            
            if (response.isSuccessful && response.body()?.success == true) {
                val user = response.body()?.user
                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(Exception("회원가입에 실패했습니다"))
                }
            } else {
                Result.failure(Exception(response.body()?.error ?: "회원가입에 실패했습니다"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
}
