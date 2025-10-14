package com.acksession.data.local.datasource

import com.acksession.datastore.preferences.EncryptedAuthStorage
import com.acksession.domain.model.AuthTokens
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Token manager for authentication operations.
 * Converts between storage layer (EncryptedAuthStorage) and domain models (AuthTokens).
 */
@Singleton
class AuthTokenManager @Inject constructor(
    private val encryptedAuthStorage: EncryptedAuthStorage
) {
    fun saveAuthTokens(tokens: AuthTokens) {
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
     * Get access token
     */
    suspend fun getAccessToken(): String? {
        return encryptedAuthStorage.getAccessTokenFlow().firstOrNull()
    }

    /**
     * Get refresh token
     */
    suspend fun getRefreshToken(): String? {
        return encryptedAuthStorage.getRefreshTokenFlow().firstOrNull()
    }

    /**
     * Clear all auth tokens
     */
    fun clearAuthTokens() {
        encryptedAuthStorage.clearAuthTokens()
    }

    /**
     * Save current user ID
     */
    fun saveCurrentUserId(userId: String) {
        encryptedAuthStorage.saveCurrentUserId(userId)
    }

    /**
     * Get current user ID as Flow
     */
    fun getCurrentUserId(): Flow<String?> {
        return encryptedAuthStorage.getCurrentUserIdFlow()
    }

    /**
     * Clear current user ID
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
