package com.acksession.network.di

import com.acksession.network.interceptor.AuthInterceptor
import com.acksession.network.interceptor.TokenAuthenticator
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

            // Timeouts
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

            // Retry on connection failure
            .retryOnConnectionFailure(true)

            .build()
    }
}
