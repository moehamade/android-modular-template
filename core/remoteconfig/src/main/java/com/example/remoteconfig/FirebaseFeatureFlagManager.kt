package com.example.remoteconfig

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseFeatureFlagManager @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig,
) : FeatureFlagManager {

    override suspend fun fetchAndActivate(): Result<Boolean> {
        return try {
            val activated = remoteConfig.fetchAndActivate().await()
            Timber.d("Remote config fetched and activated: $activated")
            Result.success(activated)
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch remote config")
            Result.failure(e)
        }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean =
        remoteConfig.getBoolean(key).also { Timber.d("Feature flag: $key = $it") }

    override fun getString(key: String, defaultValue: String): String =
        remoteConfig.getString(key).also { Timber.d("Config: $key = $it") }

    override fun getLong(key: String, defaultValue: Long): Long =
        remoteConfig.getLong(key).also { Timber.d("Config: $key = $it") }

    override fun getDouble(key: String, defaultValue: Double): Double =
        remoteConfig.getDouble(key).also { Timber.d("Config: $key = $it") }
}
