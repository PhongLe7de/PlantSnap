package com.plantsnap.data.remote.supabase

import com.plantsnap.BuildConfig
import com.plantsnap.data.repository.ProfileRepositoryImpl
import com.plantsnap.data.repository.SettingsRepositoryImpl
import com.plantsnap.domain.repository.ProfileRepository
import com.plantsnap.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SupabaseModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    companion object {
        @Provides
        @Singleton
        fun provideSupabaseClient(): SupabaseClient {
            return createSupabaseClient(
                supabaseUrl = BuildConfig.SUPABASE_URL,
                supabaseKey = BuildConfig.SUPABASE_KEY
            ) {
                install(Auth) {
                    alwaysAutoRefresh = true
                    autoLoadFromStorage = true

                    scheme = "com.plantsnap"
                    host = "callback"
                    flowType = FlowType.PKCE
                }
                install(ComposeAuth) {
                    googleNativeLogin(serverClientId = BuildConfig.GOOGLE_SERVER_CLIENT_ID)
                }
                install(Postgrest)
            }
        }
    }
}
