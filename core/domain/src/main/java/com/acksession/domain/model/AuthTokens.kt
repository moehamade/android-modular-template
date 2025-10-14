package com.acksession.domain.model

import android.os.SystemClock

/**
 * Domain model for authentication tokens.
 *
 * Uses SystemClock.elapsedRealtime() instead of System.currentTimeMillis() to prevent
 * device time manipulation. Server validates tokens on every request; this is for client-side optimization.
 *
 * @property accessToken JWT access token for authenticated API requests
 * @property refreshToken JWT refresh token for obtaining new access tokens
 * @property issuedAt Elapsed realtime when tokens were issued (milliseconds since boot)
 * @property accessTokenLifespanMs Access token validity period in milliseconds
 * @property refreshTokenLifespanMs Refresh token validity period in milliseconds
 */
data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val issuedAt: Long = SystemClock.elapsedRealtime(), // Monotonic clock, immune to time changes
    val accessTokenLifespanMs: Long, // e.g., 15 minutes = 15 * 60 * 1000
    val refreshTokenLifespanMs: Long  // e.g., 7 days = 7 * 24 * 60 * 60 * 1000
) {
    companion object {
        private const val ACCESS_TOKEN_BUFFER_MS = 5 * 60 * 1000L // 5 minutes buffer

        /**
         * Create AuthTokens from server response with absolute expiration timestamps.
         *
         * Note: This assumes server timestamps are in UTC milliseconds.
         * We convert them to elapsed realtime + lifespan for local validation.
         */
        fun fromServerResponse(
            accessToken: String,
            refreshToken: String,
            accessTokenExpiresAtUtc: Long,
            refreshTokenExpiresAtUtc: Long
        ): AuthTokens {
            val now = System.currentTimeMillis()
            val elapsedNow = SystemClock.elapsedRealtime()

            // Calculate lifespan based on server expiration times
            val accessTokenLifespan = (accessTokenExpiresAtUtc - now).coerceAtLeast(0)
            val refreshTokenLifespan = (refreshTokenExpiresAtUtc - now).coerceAtLeast(0)

            return AuthTokens(
                accessToken = accessToken,
                refreshToken = refreshToken,
                issuedAt = elapsedNow,
                accessTokenLifespanMs = accessTokenLifespan,
                refreshTokenLifespanMs = refreshTokenLifespan
            )
        }
    }

    /**
     * Check if access token is expired or will expire soon (within 5 minutes).
     *
     * Uses monotonic clock (elapsedRealtime) to prevent device time manipulation.
     *
     * @return true if token is expired or expiring soon, false otherwise
     */
    fun isAccessTokenExpired(): Boolean {
        val elapsedSinceIssued = SystemClock.elapsedRealtime() - issuedAt
        val expirationThreshold = accessTokenLifespanMs - ACCESS_TOKEN_BUFFER_MS
        return elapsedSinceIssued >= expirationThreshold
    }

    /**
     * Check if refresh token is expired.
     *
     * Uses monotonic clock (elapsedRealtime) to prevent device time manipulation.
     *
     * @return true if token is expired, false otherwise
     */
    fun isRefreshTokenExpired(): Boolean {
        val elapsedSinceIssued = SystemClock.elapsedRealtime() - issuedAt
        return elapsedSinceIssued >= refreshTokenLifespanMs
    }

    /**
     * Get remaining time for access token in milliseconds.
     *
     * @return milliseconds until access token expires, or 0 if already expired
     */
    fun getRemainingAccessTokenMs(): Long {
        val elapsedSinceIssued = SystemClock.elapsedRealtime() - issuedAt
        val remaining = accessTokenLifespanMs - elapsedSinceIssued
        return remaining.coerceAtLeast(0)
    }

    /**
     * Get remaining time for refresh token in milliseconds.
     *
     * @return milliseconds until refresh token expires, or 0 if already expired
     */
    fun getRemainingRefreshTokenMs(): Long {
        val elapsedSinceIssued = SystemClock.elapsedRealtime() - issuedAt
        val remaining = refreshTokenLifespanMs - elapsedSinceIssued
        return remaining.coerceAtLeast(0)
    }
}
