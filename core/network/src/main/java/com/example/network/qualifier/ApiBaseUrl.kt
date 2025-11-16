package com.example.network.qualifier

import javax.inject.Qualifier

/**
 * Qualifier for API base URL.
 *
 * This allows the app module to provide the base URL via Hilt,
 * while keeping the network module decoupled from BuildConfig.
 *
 * Usage in app module:
 * ```
 * @Provides
 * @ApiBaseUrl
 * fun provideApiBaseUrl(): String = BuildConfig.API_BASE_URL
 * ```
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApiBaseUrl
