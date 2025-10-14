package com.acksession.data.di

import com.acksession.data.local.datasource.AuthTokenManager
import com.acksession.data.mapper.toAuthTokens
import com.acksession.data.remote.api.AuthApiService
import com.acksession.data.remote.dto.RefreshTokenRequest
import com.acksession.network.interceptor.TokenRefreshCallback
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

                        // Save new tokens
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
