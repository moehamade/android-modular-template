package com.acksession.network.di

import com.acksession.network.BuildConfig
import com.acksession.network.qualifier.ApiBaseUrl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import timber.log.Timber
import javax.inject.Singleton

/**
 * Network module providing Retrofit, JSON serializer and logging.
 *
 * This module is in core:network to:
 * 1. Centralize network configuration
 * 2. Keep it reusable across different apps/flavors
 * 3. Separate concerns (app provides URL, core:network provides infrastructure)
 *
 * Note: OkHttpClient is provided by OkHttpModule to handle auth dependencies.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides configured JSON serializer for Retrofit.
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        prettyPrint = false // Disable for production
    }

    /**
     * Provides HTTP logging interceptor.
     *
     * Logging level is conditional on debug/release build:
     * - Debug: BODY level (full request/response logging)
     * - Release: NONE (no logging for security)
     *
     */
    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(
    ): HttpLoggingInterceptor {
        return HttpLoggingInterceptor { message ->
            Timber.tag("OkHttp").d(message)
        }.apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    /**
     * Provides Retrofit instance.
     *
     * The base URL is provided by the app module via @ApiBaseUrl qualifier.
     * OkHttpClient is provided by OkHttpModule with auth interceptors.
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json,
        @ApiBaseUrl baseUrl: String
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(
                json.asConverterFactory("application/json".toMediaType())
            )
            .build()
    }
}
