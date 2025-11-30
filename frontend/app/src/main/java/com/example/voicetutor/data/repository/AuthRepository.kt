package com.example.voicetutor.data.repository

import com.example.voicetutor.data.models.LoginRequest
import com.example.voicetutor.data.models.SignupRequest
import com.example.voicetutor.data.models.User
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.data.network.ApiService
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class AuthRepository @Inject constructor(
    private val apiService: ApiService,
) {

    open suspend fun login(email: String, password: String): Result<User> {
        return try {
            val request = LoginRequest(email, password)
            val response = apiService.login(request)
            val responseBody = response.body()

            if (response.isSuccessful && responseBody?.success == true) {
                val user = responseBody.user

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
                role = role.name,
            )
            val response = apiService.signup(signupRequest)

            val responseBody = response.body()
            if (response.isSuccessful && responseBody?.success == true) {
                val user = responseBody.user

                if (user != null) {
                    Result.success(user)
                } else {
                    Result.failure(SignupException.Unknown("회원가입에 실패했습니다 - 사용자 정보를 받을 수 없습니다"))
                }
            } else {
                val statusCode = response.code()
                val errorMsg = responseBody?.error
                    ?: responseBody?.message
                    ?: parseErrorMessage(response)
                    ?: "회원가입에 실패했습니다"

                val exception = when {
                    statusCode == 409 -> SignupException.DuplicateEmail(errorMsg)
                    statusCode == 400 && errorMsg.contains("이미 사용 중") -> SignupException.DuplicateEmail(errorMsg)
                    statusCode in 500..599 -> SignupException.Server(errorMsg.ifBlank { "서버에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요." })
                    else -> SignupException.Unknown(errorMsg)
                }
                Result.failure(exception)
            }
        } catch (e: Exception) {
            val exception = when (e) {
                is SignupException -> e
                is IOException -> SignupException.Network("네트워크 연결을 확인하고 다시 시도해주세요.", e)
                else -> SignupException.Unknown(e.message ?: "회원가입 중 알 수 없는 오류가 발생했습니다")
            }
            Result.failure(exception)
        }
    }

    open suspend fun deleteAccount(): Result<Unit> {
        return try {
            val response = apiService.deleteAccount()
            val responseBody = response.body()

            if (response.isSuccessful) {
                if (responseBody?.success == false) {
                    val message = responseBody.error
                        ?: responseBody.message
                        ?: "계정 삭제에 실패했습니다."
                    Result.failure(DeleteAccountException.Unknown(message))
                } else {
                    Result.success(Unit)
                }
            } else {
                val statusCode = response.code()
                val message = responseBody?.error
                    ?: responseBody?.message
                    ?: parseErrorMessage(response)
                    ?: "계정 삭제에 실패했습니다."

                val exception = when (statusCode) {
                    401, 403 -> DeleteAccountException.Unauthorized("계정 삭제를 위해 다시 로그인해주세요.")
                    in 500..599 -> DeleteAccountException.Server("서버에서 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                    else -> DeleteAccountException.Unknown(message)
                }
                Result.failure(exception)
            }
        } catch (e: Exception) {
            val exception = when (e) {
                is DeleteAccountException -> e
                is IOException -> DeleteAccountException.Network("네트워크 연결을 확인하고 다시 시도해주세요.", e)
                else -> DeleteAccountException.Unknown(e.message ?: "계정 삭제 중 알 수 없는 오류가 발생했습니다.")
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
            null
        }
    }
}
