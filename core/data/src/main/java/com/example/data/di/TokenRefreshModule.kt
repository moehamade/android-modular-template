package com.example.data.di

import com.example.data.local.datasource.AuthTokenManager
import com.example.data.mapper.toAuthTokens
import com.example.network.api.AuthApiService
import com.example.network.dto.RefreshTokenRequest
import com.example.network.interceptor.TokenRefreshCallback
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Singleton

/**
 * Provides the token refresh callback implementation.
 *
 * This module bridges core:network and core:data without creating circular dependency:
 * - core:network defines the TokenRefreshCallback interface
 * - core:data implements it using AuthApiService
 * - Hilt wires them together at runtime
 *
 * Note: Uses runBlocking because OkHttp's Authenticator is synchronous and already
 * runs on background threads.
 */
@Module
@InstallIn(SingletonComponent::class)
object TokenRefreshModule {

    @Provides
    @Singleton
    fun provideTokenRefreshCallback(
        authApiService: AuthApiService,
        authTokenManager: AuthTokenManager
    ): TokenRefreshCallback {
        return object : TokenRefreshCallback {
            override fun refreshTokenSync(refreshToken: String): String? {
                return runBlocking {
                    try {
                        val authResponse = authApiService.refreshToken(RefreshTokenRequest(refreshToken))
                        val newTokens = authResponse.toAuthTokens()

                        authTokenManager.saveAuthTokens(newTokens)

                        Timber.d("Token refreshed successfully in callback")
                        newTokens.accessToken
                    } catch (e: Exception) {
                        Timber.e(e, "Token refresh failed in callback")
                        null
                    }
                }
            }
        }
    }
}
