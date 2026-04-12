package com.plantsnap.data

import com.google.genai.Client
import com.plantsnap.BuildConfig
import com.plantsnap.data.repository.GeminiRepositoryImpl
import com.plantsnap.domain.repository.GeminiRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GeminiModule {

    @Binds
    @Singleton
    abstract fun bindGeminiRepository(impl: GeminiRepositoryImpl): GeminiRepository

    companion object {

        @Provides
        @Singleton
        fun provideGeminiClient(): Client {
            return Client.builder()
                .apiKey(BuildConfig.GOOGLE_API_KEY)
                .build()
        }
    }
}
