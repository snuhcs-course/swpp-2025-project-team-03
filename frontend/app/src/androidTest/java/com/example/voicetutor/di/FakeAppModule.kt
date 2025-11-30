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

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [NetworkModule::class],
)
object FakeNetworkModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        return FakeApiService()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(apiService: ApiService): AuthRepository {
        return FakeAuthRepositoryWrapper(apiService)
    }
}
