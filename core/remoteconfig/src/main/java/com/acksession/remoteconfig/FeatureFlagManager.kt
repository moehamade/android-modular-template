package com.acksession.remoteconfig

interface FeatureFlagManager {
    suspend fun fetchAndActivate(): Result<Boolean>
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean
    fun getString(key: String, defaultValue: String = ""): String
    fun getLong(key: String, defaultValue: Long = 0L): Long
    fun getDouble(key: String, defaultValue: Double = 0.0): Double
}

object FeatureFlags {
    const val NEW_RECORDING_UI = "new_recording_ui_enabled"
    const val ENABLE_ANALYTICS = "analytics_enabled"
    const val MIN_APP_VERSION = "min_supported_version"
    const val API_TIMEOUT_SECONDS = "api_timeout_seconds"
}
