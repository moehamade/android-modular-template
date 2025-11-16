package com.example.network.di

import com.example.network.interceptor.AuthInterceptor
import com.example.network.interceptor.TokenAuthenticator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Module for configuring OkHttpClient with authentication.
 *
 * All interceptors are now in core:network, avoiding circular dependencies.
 *
 * **Timeout Configuration:**
 * - Connect timeout: 30s (time to establish connection)
 * - Read timeout: 30s (time between data packets)
 * - Write timeout: 30s (time to send request body)
 * - Call timeout: 45s (total time for entire request, including retries)
 *
 * The call timeout is particularly important for token refresh operations
 * to prevent hanging indefinitely on slow networks.
 */
@Module
@InstallIn(SingletonComponent::class)
object OkHttpModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            // Logging first (logs the request before modification)
            .addInterceptor(loggingInterceptor)
            // Auth interceptor adds JWT token
            .addInterceptor(authInterceptor)
            // Authenticator handles 401 responses
            .authenticator(tokenAuthenticator)

            // Timeouts (prevent indefinite hanging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .callTimeout(45, TimeUnit.SECONDS) // Total time including token refresh

            // Retry on connection failure
            .retryOnConnectionFailure(true)

            .build()
    }
}
