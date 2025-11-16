package com.example.domain.config

/**
 * Provider for BuildConfig values.
 * This is injected to allow testing and avoid direct BuildConfig access.
 */
interface BuildConfigProvider {
    val isDebug: Boolean
    val buildType: String
    val environment: String
    val versionName: String
    val apiBaseUrl: String
}
