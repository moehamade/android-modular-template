package com.acksession.datastore.preferences

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataStore manager for authentication preferences.
 * Uses Google Tink for secure token storage with AES256-GCM encryption.
 */
@Singleton
class AuthPreferencesDataSource @Inject constructor(
    private val encryptedAuthStorage: TinkAuthStorage
) {

    /**
     * Save access token (suspend)
     */
    suspend fun saveAccessToken(token: String) {
        encryptedAuthStorage.saveAccessToken(token)
    }

    /**
     * Get access token
     */
    fun getAccessToken(): Flow<String?> {
        return encryptedAuthStorage.getAccessTokenFlow()
    }

    /**
     * Get access token once (synchronous - from cache)
     */
    fun getAccessTokenOnce(): String? {
        return encryptedAuthStorage.getAccessToken()
    }

    /**
     * Save refresh token (suspend)
     */
    suspend fun saveRefreshToken(token: String) {
        encryptedAuthStorage.saveRefreshToken(token)
    }

    /**
     * Get refresh token
     */
    fun getRefreshToken(): Flow<String?> {
        return encryptedAuthStorage.getRefreshTokenFlow()
    }

    /**
     * Get refresh token once (synchronous - from cache)
     */
    fun getRefreshTokenOnce(): String? {
        return encryptedAuthStorage.getRefreshToken()
    }

    /**
     * Save all auth tokens at once (suspend)
     */
    suspend fun saveAuthTokens(
        accessToken: String,
        refreshToken: String,
        issuedAt: Long,
        accessTokenLifespanMs: Long,
        refreshTokenLifespanMs: Long
    ) {
        encryptedAuthStorage.saveAccessToken(accessToken)
        encryptedAuthStorage.saveRefreshToken(refreshToken)
        encryptedAuthStorage.saveTokenMetadata(issuedAt, accessTokenLifespanMs, refreshTokenLifespanMs)
    }

    /**
     * Get issued at timestamp
     */
    fun getIssuedAt(): Flow<Long> {
        return kotlinx.coroutines.flow.flow {
            emit(encryptedAuthStorage.getIssuedAt())
        }
    }

    /**
     * Get access token lifespan
     */
    fun getAccessTokenLifespanMs(): Flow<Long> {
        return kotlinx.coroutines.flow.flow {
            emit(encryptedAuthStorage.getAccessTokenLifespan())
        }
    }

    /**
     * Get refresh token lifespan
     */
    fun getRefreshTokenLifespanMs(): Flow<Long> {
        return kotlinx.coroutines.flow.flow {
            emit(encryptedAuthStorage.getRefreshTokenLifespan())
        }
    }

    /**
     * Check if access token exists and is not expired
     */
    fun isAccessTokenValid(): Flow<Boolean> {
        return kotlinx.coroutines.flow.flow {
            val issuedAt = encryptedAuthStorage.getIssuedAt()
            val lifespanMs = encryptedAuthStorage.getAccessTokenLifespan()
            val token = encryptedAuthStorage.getAccessToken()

            if (token == null || issuedAt == 0L || lifespanMs == 0L) {
                emit(false)
            } else {
                val elapsedSinceIssued = android.os.SystemClock.elapsedRealtime() - issuedAt
                val bufferTime = 5 * 60 * 1000L // 5 minutes buffer
                emit(elapsedSinceIssued < (lifespanMs - bufferTime))
            }
        }
    }

    /**
     * Check if user is authenticated (has valid tokens)
     */
    fun isAuthenticated(): Flow<Boolean> {
        return encryptedAuthStorage.isAuthenticated()
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
     * Get current user ID
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
     * Clear all authentication data (tokens + user ID) (non-blocking)
     */
    fun clearAll() {
        encryptedAuthStorage.clearAll()
    }
}
