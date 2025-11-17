package com.example.voicetutor.di

import com.example.voicetutor.data.network.ApiService
import com.example.voicetutor.data.network.FakeApiService
import com.example.voicetutor.data.repository.AuthRepository
import com.example.voicetutor.data.repository.fake.FakeAuthRepositoryWrapper
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

/**
 * Hilt test module that replaces real AuthRepository with FakeAuthRepositoryWrapper.
 * 
 * How it works:
 * - @TestInstallIn replaces the real NetworkModule during tests
 * - Provides FakeAuthRepositoryWrapper (which extends AuthRepository)
 * - The wrapper delegates all calls to FakeAuthRepository (in-memory)
 * - All ViewModel/Screen dependencies get the fake implementation automatically
 * 
 */
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class]  // ← This replaces real network module!
)
object FakeNetworkModule {
    
    // Dummy ApiService - satisfies dependency but is NEVER actually called
    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        return FakeApiService()
    }
    
    @Provides
    @Singleton
    fun provideAuthRepository(apiService: ApiService): AuthRepository {
        // ← Return FakeAuthRepositoryWrapper (extends AuthRepository)
        // It delegates to FakeAuthRepository (in-memory implementation)
        return FakeAuthRepositoryWrapper(apiService)
    }
}
