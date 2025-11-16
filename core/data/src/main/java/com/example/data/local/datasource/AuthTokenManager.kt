package com.example.data.local.datasource

import com.example.datastore.preferences.TinkAuthStorage
import com.example.domain.model.AuthTokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Token manager for authentication operations.
 * Converts between storage layer (TinkAuthStorage) and domain models (AuthTokens).
 *
 * **Thread Safety:**
 * All methods are thread-safe. Synchronous getters (`getAccessToken()`, `getRefreshToken()`)
 * read from atomic in-memory cache and are safe to call from any thread, including
 * OkHttp interceptor threads. Suspend functions update both cache and DataStore atomically.
 *
 * **Performance:**
 * - Synchronous methods (getAccessToken, getRefreshToken): Instant, no I/O (reads from cache)
 * - Suspend methods (saveAuthTokens): Updates cache immediately, persists to DataStore async
 * - Flow methods (getAuthTokens, getCurrentUserId): Reactive, emits from DataStore
 */
@Singleton
class AuthTokenManager @Inject constructor(
    private val encryptedAuthStorage: TinkAuthStorage
) {
    /**
     * Save auth tokens (suspend).
     * Updates both in-memory cache and encrypted DataStore.
     */
    suspend fun saveAuthTokens(tokens: AuthTokens) {
        encryptedAuthStorage.saveAccessToken(tokens.accessToken)
        encryptedAuthStorage.saveRefreshToken(tokens.refreshToken)
        encryptedAuthStorage.saveTokenMetadata(
            issuedAt = tokens.issuedAt,
            accessTokenLifespanMs = tokens.accessTokenLifespanMs,
            refreshTokenLifespanMs = tokens.refreshTokenLifespanMs
        )
    }

    /**
     * Get auth tokens as Flow (reactive)
     */
    fun getAuthTokens(): Flow<AuthTokens?> {
        return encryptedAuthStorage.getAccessTokenFlow().map { accessToken ->
            if (accessToken == null) return@map null

            val refreshToken = encryptedAuthStorage.getRefreshToken()
            val issuedAt = encryptedAuthStorage.getIssuedAt()
            val accessTokenLifespanMs = encryptedAuthStorage.getAccessTokenLifespan()
            val refreshTokenLifespanMs = encryptedAuthStorage.getRefreshTokenLifespan()

            if (refreshToken != null) {
                AuthTokens(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                    issuedAt = issuedAt,
                    accessTokenLifespanMs = accessTokenLifespanMs,
                    refreshTokenLifespanMs = refreshTokenLifespanMs
                )
            } else {
                null
            }
        }
    }

    /**
     * Get access token (synchronous - from cache)
     */
    fun getAccessToken(): String? {
        return encryptedAuthStorage.getAccessToken()
    }

    /**
     * Get refresh token (synchronous - from cache)
     */
    fun getRefreshToken(): String? {
        return encryptedAuthStorage.getRefreshToken()
    }

    /**
     * Clear all auth tokens (non-blocking)
     */
    fun clearAuthTokens() {
        encryptedAuthStorage.clearAuthTokens()
    }

    /**
     * Save current user ID (suspend)
     */
    suspend fun saveCurrentUserId(userId: String) {
        encryptedAuthStorage.saveCurrentUserId(userId)
    }

    /**
     * Get current user ID as Flow
     */
    fun getCurrentUserId(): Flow<String?> {
        return encryptedAuthStorage.getCurrentUserIdFlow()
    }

    /**
     * Clear current user ID (non-blocking)
     */
    fun clearCurrentUserId() {
        encryptedAuthStorage.clearCurrentUserId()
    }

    /**
     * Check if user is authenticated
     */
    fun isAuthenticated(): Flow<Boolean> {
        return encryptedAuthStorage.isAuthenticated()
    }
}
