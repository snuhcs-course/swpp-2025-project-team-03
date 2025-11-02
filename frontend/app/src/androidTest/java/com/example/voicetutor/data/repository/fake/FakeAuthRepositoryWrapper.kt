package com.example.voicetutor.data.repository.fake

import com.example.voicetutor.data.models.User
import com.example.voicetutor.data.models.UserRole
import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.repository.AuthRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper that makes FakeAuthRepository compatible with AuthRepository type.
 * 
 *  This delegates all calls to FakeAuthRepository (no real network calls)
 * 
 * Why this wrapper exists:
 * - AuthRepository is a final class (cannot be inherited)
 * - Hilt needs to provide AuthRepository type
 * - This wrapper extends AuthRepository but delegates to fake implementation
 * - The parent ApiService is never actually used
 * 
 * Flow:
 * ViewModel → AuthRepository (this wrapper) → FakeAuthRepository (in-memory)
 */
@Singleton
class FakeAuthRepositoryWrapper @Inject constructor(
    apiService: ApiService
) : AuthRepository(apiService) {
    
    // The actual fake implementation
    private val fakeRepo = FakeAuthRepository()
    
    // Delegate all calls to the fake implementation
    override suspend fun login(email: String, password: String): Result<User> {
        return fakeRepo.login(email, password)
    }
    
    override suspend fun signup(
        name: String,
        email: String,
        password: String,
        role: UserRole
    ): Result<User> {
        return fakeRepo.signup(name, email, password, role)
    }
    
    // Expose helper methods for testing
    fun clearAll() = fakeRepo.clearAll()
    fun reset() = fakeRepo.reset()
}
