package com.example.voicetutor.data.repository.fake

import com.example.voicetutor.data.models.User
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAuthRepositoryWrapper @Inject constructor(
    apiService: ApiService,
) : AuthRepository(apiService) {

    private val fakeRepo = FakeAuthRepository()

    override suspend fun login(email: String, password: String): Result<User> {
        return fakeRepo.login(email, password)
    }

    override suspend fun signup(
        name: String,
        email: String,
        password: String,
        role: UserRole,
    ): Result<User> {
        return fakeRepo.signup(name, email, password, role)
    }

    @Suppress("unused")
    fun clearAll() = fakeRepo.clearAll()

    @Suppress("unused")
    fun reset() = fakeRepo.reset()
}
