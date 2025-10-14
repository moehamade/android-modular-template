package com.acksession.datastore.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

/**
 * Secure storage for authentication tokens using EncryptedSharedPreferences.
 *
 * Uses AES256-GCM encryption with hardware-backed keys when available.
 * Falls back to software implementation on older devices.
 */
@Singleton
class EncryptedAuthStorage @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        try {
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to create EncryptedSharedPreferences, falling back to regular SharedPreferences")
            // Fallback to regular SharedPreferences if encryption fails
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    // Flow-based state holders for reactive updates
    private val accessTokenFlow = MutableStateFlow<String?>(null)
    private val refreshTokenFlow = MutableStateFlow<String?>(null)
    private val userIdFlow = MutableStateFlow<String?>(null)

    init {
        // Initialize flows with current values
        accessTokenFlow.value = encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
        refreshTokenFlow.value = encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
        userIdFlow.value = encryptedPrefs.getString(KEY_USER_ID, null)
    }

    fun saveAccessToken(token: String) {
        encryptedPrefs.edit { putString(KEY_ACCESS_TOKEN, token) }
        accessTokenFlow.value = token
    }

    fun getAccessToken(): String? {
        return encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getAccessTokenFlow(): Flow<String?> = accessTokenFlow

    fun saveRefreshToken(token: String) {
        encryptedPrefs.edit { putString(KEY_REFRESH_TOKEN, token) }
        refreshTokenFlow.value = token
    }

    fun getRefreshToken(): String? {
        return encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun getRefreshTokenFlow(): Flow<String?> = refreshTokenFlow

    fun saveTokenMetadata(
        issuedAt: Long,
        accessTokenLifespanMs: Long,
        refreshTokenLifespanMs: Long
    ) {
        encryptedPrefs.edit {
            putLong(KEY_ISSUED_AT, issuedAt)
                .putLong(KEY_ACCESS_TOKEN_LIFESPAN, accessTokenLifespanMs)
                .putLong(KEY_REFRESH_TOKEN_LIFESPAN, refreshTokenLifespanMs)
        }
    }

    fun getIssuedAt(): Long {
        return encryptedPrefs.getLong(KEY_ISSUED_AT, 0L)
    }

    fun getAccessTokenLifespan(): Long {
        return encryptedPrefs.getLong(KEY_ACCESS_TOKEN_LIFESPAN, 0L)
    }

    fun getRefreshTokenLifespan(): Long {
        return encryptedPrefs.getLong(KEY_REFRESH_TOKEN_LIFESPAN, 0L)
    }

    fun saveCurrentUserId(userId: String) {
        encryptedPrefs.edit { putString(KEY_USER_ID, userId) }
        userIdFlow.value = userId
    }

    fun getCurrentUserId(): String? {
        return encryptedPrefs.getString(KEY_USER_ID, null)
    }

    fun getCurrentUserIdFlow(): Flow<String?> = userIdFlow

    fun clearAuthTokens() {
        encryptedPrefs.edit {
            remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_ISSUED_AT)
                .remove(KEY_ACCESS_TOKEN_LIFESPAN)
                .remove(KEY_REFRESH_TOKEN_LIFESPAN)
        }

        accessTokenFlow.value = null
        refreshTokenFlow.value = null
    }

    fun clearCurrentUserId() {
        encryptedPrefs.edit { remove(KEY_USER_ID) }
        userIdFlow.value = null
    }

    fun clearAll() {
        encryptedPrefs.edit { clear() }
        accessTokenFlow.value = null
        refreshTokenFlow.value = null
        userIdFlow.value = null
    }

    fun isAuthenticated(): Flow<Boolean> {
        return accessTokenFlow.map { accessToken ->
            val refreshToken = encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
            accessToken != null && refreshToken != null
        }
    }

    companion object {
        private const val PREFS_NAME = "encrypted_auth_prefs"

        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_ISSUED_AT = "issued_at"
        private const val KEY_ACCESS_TOKEN_LIFESPAN = "access_token_lifespan"
        private const val KEY_REFRESH_TOKEN_LIFESPAN = "refresh_token_lifespan"
        private const val KEY_USER_ID = "user_id"
    }
}
